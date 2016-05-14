package mn.dbtk.sql.dbcache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

public class DBObjectCache {
	public static final List<String> basicAggregateFunctions = Arrays.asList(new String[]
			{"SUM", "MIN", "MAX", "STDEV", "AVG", "COUNT", "MEDIAN"});
	
	
	public static DBObjectCache cache = new DBObjectCache();
	
	private boolean isLoaded;
	private boolean firstLoadAttempt;
	private Map<String, DBObjectsModelRowSource> internalCache;
	private String lastException;
	private Set<DBObjectCacheLoadListener> listenerSet;
	private Set<String> aggregateFunctions;
	
	private DBObjectCacheDeamon deamonLoadThread;

	private DBObjectCache(){
		resetCache();
		listenerSet = Collections.newSetFromMap(new WeakHashMap<DBObjectCacheLoadListener, Boolean>());
		deamonLoadThread = new DBObjectCacheDeamon(this);
		deamonLoadThread.start();
	}
	private void resetCache(){
		isLoaded           = false;
		firstLoadAttempt   = true;
		internalCache      = null;
		lastException      = null;
		aggregateFunctions = new HashSet<String>(basicAggregateFunctions);
	}

	
	
	public void invalidateAndReload(){
		resetCache();
		deamonLoadThread.determineCache();		
	}
	
	// Access to cache
	private void waitCheckFirstLoadAttempt() {
		while (firstLoadAttempt){
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}		
	}
	public boolean isLoaded(){
		waitCheckFirstLoadAttempt();
		return isLoaded;
	}	
	public DBObjectsModelRowSource getCache(String rowSourceName){
		waitCheckFirstLoadAttempt();
		return isLoaded ? internalCache.get(rowSourceName) : null;
	}
	public Set<String> getAllRowSources(){
		waitCheckFirstLoadAttempt();
		return isLoaded ? internalCache.keySet() : new HashSet<String>();
	}	
	public Set<String> getAggregateFunctions(){
		waitCheckFirstLoadAttempt();
		return aggregateFunctions;
	}
	public String getLastException(){
		return lastException;
	}	

	
	
	// Callback from deamon loader thread	
	synchronized void loadCompleted(Map<String, DBObjectsModelRowSource> newInternalCache,
			Set<String> newAggregateFunctions, String newLastException) {
		boolean hasChanged = false;
		
		if (newInternalCache != null){
			if (!isLoaded)
				Logger.getGlobal().config("Completed cache load for database objects.");
			
			isLoaded = true;
			hasChanged |= !newInternalCache.equals(internalCache);
			internalCache = newInternalCache;
		}
		if (newAggregateFunctions != null){
			isLoaded = true;
			hasChanged |= !newAggregateFunctions.equals(aggregateFunctions);
			aggregateFunctions = newAggregateFunctions;			
		}		
		if (newLastException != null){
			lastException = newLastException;
		}	

		if (firstLoadAttempt && !isLoaded){
			Logger.getGlobal().warning("Initial cache load for database objects failed.\r\n" + newLastException);
		}
		firstLoadAttempt = false;
		
		if (hasChanged)
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			    	notifyCacheLoadListeners();
			    }
			});		
	}
	
	
	// Cache load listener management
	public synchronized void addCacheLoadListener(DBObjectCacheLoadListener l){
		listenerSet.add(l);
	}

	public synchronized void removeCacheLoadListener(DBObjectCacheLoadListener l){
		listenerSet.remove(l);
	}
	private synchronized void notifyCacheLoadListeners() {
		for (DBObjectCacheLoadListener l : listenerSet)
			l.cacheLoaded();
	}
}
