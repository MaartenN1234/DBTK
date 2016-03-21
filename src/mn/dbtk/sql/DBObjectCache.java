package mn.dbtk.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mn.dbtk.util.MultiSet;

public class DBObjectCache {
	private Map<String, DBObjectsModelRowSource> internalCache;
	private static final boolean PERFORMANCE_OUTPUT = false;
	private Thread reloadThread;
	public  String lastException;
	
	private static final List<String> basicAggregateFunctions = Arrays.asList(new String[]
			{"SUM", "MIN", "MAX", "STDEV", "AVG", "COUNT", "MEDIAN"});

	public List<String> aggregateFunctions;

	
	public static DBObjectCache cache = new DBObjectCache();
	
	private DBObjectCache(){
		aggregateFunctions = new ArrayList<String>(basicAggregateFunctions);
	}
	
	public void fillCache(){
		long l = System.currentTimeMillis();		
		
		final String objectFilterClause = "AND <?>.TABLE_NAME NOT LIKE 'TMP!_%' ESCAPE '!' " +
									"AND <?>.TABLE_NAME NOT LIKE 'T!_%' ESCAPE '!' "+
				                    "AND <?>.TABLE_NAME NOT LIKE 'J!_%' ESCAPE '!' "+
									"AND <?>.TABLE_NAME NOT LIKE '%!_DBG' ESCAPE '!' "+
				                    "\n";
		final String utcSQL    = "WITH PK_COLS AS(\n"+
								"SELECT /*+NO_MERGE USE_HASH(UCC UCS)*/ UCS.TABLE_NAME TN, COLUMN_NAME CN\n"+ 
								"FROM USER_CONS_COLUMNS UCC, USER_CONSTRAINTS UCS\n"+
								"WHERE UCC.CONSTRAINT_NAME = UCS.CONSTRAINT_NAME\n"+
								"AND UCC.OWNER = USER AND UCS.OWNER = USER AND CONSTRAINT_TYPE ='P'\n"+ 
								objectFilterClause.replace("<?>","UCS") +
								objectFilterClause.replace("<?>","UCC") +
								")\n"+
								"SELECT /*+ USE_HASH (UTC PK_COLS) */\n"+ 
								"        TABLE_NAME, COLUMN_NAME, DATA_TYPE,  NVL2(TN,'Y','N') IS_PK, COLUMN_ID\n"+ 
								"FROM   USER_TAB_COLUMNS UTC,  PK_COLS\n"+
								"WHERE TABLE_NAME = TN(+)\n"+
								"AND COLUMN_NAME = CN(+)\n"+
								objectFilterClause.replace("<?>","UTC");
		
		final String refSQL = 	"SELECT NAME,TYPE,REFERENCED_NAME,REFERENCED_TYPE FROM USER_DEPENDENCIES\n"+
								"WHERE TYPE IN('VIEW','SYNONYM')\n"+
								"AND REFERENCED_TYPE IN ('VIEW','SYNONYM','TABLE')\n"+
								"AND REFERENCED_OWNER NOT IN('PUBLIC')\n" +
								objectFilterClause.replace("<?>.TABLE_NAME","NAME");
		
		final String aFcSQL =   "SELECT NAME FROM USER_SOURCE\n"+
				                "WHERE TEXT LIKE '%AGGREGATE USING%' AND TYPE='FUNCTION'";
		
		StringResultSetLoaderThread refLoader = new StringResultSetLoaderThread(refSQL);
		StringResultSetLoaderThread utcLoader = new StringResultSetLoaderThread(utcSQL);
		StringResultSetLoaderThread aFcLoader = new StringResultSetLoaderThread(aFcSQL);
		
		utcLoader.start();
		refLoader.start();
		aFcLoader.start();
		
		if (!refLoader.sync() || !utcLoader.sync() || !aFcLoader.sync()){
			 // interrupt or exception --> abort
			refLoader.cancel();
			utcLoader.cancel();
			aFcLoader.cancel();
			if(refLoader.exceptionMessage!= null)
				lastException = refLoader.exceptionMessage;
			if(utcLoader.exceptionMessage!= null)
				lastException = utcLoader.exceptionMessage;			
			return;
		}

		if (PERFORMANCE_OUTPUT)
			System.out.println("SYNC: "+(System.currentTimeMillis()-l));
			
		internalCache = new HashMap<String, DBObjectsModelRowSource>();
		
		
		// basic mappings
		Map<String, Integer> typeMap    = new HashMap<String, Integer>();
		MultiSet<String, String> depMap = new MultiSet<String, String>();
		Map<String, String> synMap      = new HashMap<String, String>();
		for (String [] ss: refLoader.getResults()){
			int type = -1;
			if (ss[1].toUpperCase().equals("TABLE")){
				type = DBObjectsModelRowSource.TYPE_TABLE;
			} else if (ss[1].toUpperCase().equals("VIEW")){
				type = DBObjectsModelRowSource.TYPE_VIEW;
			} else if (ss[1].toUpperCase().equals("SYNONYM")){
				type = DBObjectsModelRowSource.TYPE_SYNONYM;
			}
			typeMap.put(ss[0],type);
			depMap.add(ss[0],ss[2]);
			if (type == DBObjectsModelRowSource.TYPE_SYNONYM)
				synMap.put(ss[0],ss[2]);
		}
		
		//calculate view/synonym dependencies
		boolean lastIterationChange = true;
		while(lastIterationChange){
			lastIterationChange = false;
			for(String s : depMap.keySet()){
				Set<String> deps = depMap.get(s);
				for(String d : new HashSet<String>(deps)){
					Set<String> subDeps = depMap.get(d);
					if (subDeps!= null)
						lastIterationChange |= deps.addAll(subDeps);
				}
			}
		}
		// cleanup non tables as base deps
		for(String s : depMap.keySet()){
			Set<String> deps = depMap.get(s);
			for(String d : new HashSet<String>(deps)){
				Integer depType = typeMap.get(d);
				if(depType != null && depType != DBObjectsModelRowSource.TYPE_TABLE){
					deps.remove(d);
				}
			}
		}

		
		// fill final arrays 
		//	columns for tables and views
		for (String [] ss: utcLoader.getResults()){
			String tableName  = ss[0];
			DBObjectsModelRowSource rowSource = internalCache.get(tableName);
			if (rowSource == null){
				rowSource = new DBObjectsModelRowSource();
				Integer type = typeMap.get(tableName);
				rowSource.type = (type==null) ? DBObjectsModelRowSource.TYPE_TABLE: type;
				internalCache.put(tableName, rowSource);
			}
						
			int    columnID   = Integer.parseInt(ss[4]);

			DBObjectsModelColumn c = new DBObjectsModelColumn(ss[1], ss[2], ss[3].equals("Y"));
			
			if (columnID< rowSource.columns.size()){
				rowSource.columns.set(columnID-1, c);
			} else{
				while (columnID>rowSource.columns.size())
					rowSource.columns.add(c);
			}
		}
		//  deps for views
		for (String referrer: depMap.keySet()){
			if (!synMap.containsKey(referrer)){
				Set<String> deps = depMap.get(referrer);
				DBObjectsModelRowSource ref = internalCache.get(referrer);
				if (ref!=null)
					ref.dependents = deps;
			}
		}
		//	all for synonyms
		for (String synonym:synMap.keySet()){
			DBObjectsModelRowSource ref =  internalCache.get(synMap.get(synonym));
			if (ref!=null){
				DBObjectsModelRowSource synref = new DBObjectsModelRowSource();
				synref.type = DBObjectsModelRowSource.TYPE_SYNONYM;
				synref.columns = ref.columns;
				if(ref.dependents == null){
					synref.dependents = new HashSet<String>();
					synref.dependents.add(synMap.get(synonym));
				} else {
					synref.dependents = ref.dependents;
				}
				internalCache.put(synonym, synref);
			}
		}
		
		// Load Aggregate functions
		aggregateFunctions = new ArrayList<String>(basicAggregateFunctions);
		for (String [] ss: aFcLoader.getResults())
			aggregateFunctions.add(ss[0]);
		
		
		if (PERFORMANCE_OUTPUT)
			System.out.println("FINAL: "+(System.currentTimeMillis()-l));

	}
	private synchronized void ensureCacheExists(){
		if (internalCache==null){
			fillCache();
		}
	}
	public boolean isLoaded(boolean forceLoad){
		if (forceLoad)
			ensureCacheExists();
		return internalCache!=null;
	}
	
	public DBObjectsModelRowSource getCache(String rowSourceName){
		ensureCacheExists();
		return internalCache.get(rowSourceName);
	}
	public Set<String> getAllRowSources(){
		ensureCacheExists();
		return internalCache.keySet();
	}	
	
	public void clearCache(boolean startReload){
		if (reloadThread != null && reloadThread.isAlive())
			reloadThread.interrupt();
		
		synchronized (this){
			internalCache = null;
			
			if (startReload)
				startLoad();
		}
	}
	
	public void startLoad(){
		if (internalCache == null){
			if (reloadThread != null && reloadThread.isAlive())
				reloadThread.interrupt();
			
			reloadThread = new Thread(){
				public void run(){
					ensureCacheExists();
				}
			};
			reloadThread.start();
		}
	}
}
