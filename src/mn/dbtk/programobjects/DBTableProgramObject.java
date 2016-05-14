package mn.dbtk.programobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;

import mn.dbtk.gui.generics.IconProvider;
import mn.dbtk.gui.programobjects.AbstractPOPanel;
import mn.dbtk.gui.programobjects.DBTablePanel;
import mn.dbtk.sql.dbcache.DBObjectCache;
import mn.dbtk.sql.dbcache.DBObjectsModelColumn;
import mn.dbtk.sql.dbcache.DBObjectsModelRowSource;

public class DBTableProgramObject extends AbstractProgramObject {
	public final static int TYPE_DEFAULT  = 1;
	public final static int TYPE_MEAS     = 2;
	public final static int TYPE_TEMPORAL = 3;
	
	public boolean isFullAuditType                      = false;
	public boolean isInternalType                       = false;
	public int     type                                 = TYPE_DEFAULT;
	public String  tableName                            = null;
	public List<DBObjectsModelColumn> columns           = null;
	public List<List<String>>         additionalIndexes = null;
	
	
	DBTableProgramObject() {
		 super();
	}
	
	public DBTableProgramObject(String location, String name) {
		super(location, name);
	}

	public String createDatabaseScript() {
		if (!isInternalType)
			return "";
		StringBuffer result = new StringBuffer();
		{ 
			String packDatamodelParameters = "p_table_name => '"+tableName+"', "+
											 "p_table_type => '"+type+"', "+
											 "p_full_audit => '"+(isFullAuditType?"Y":"N")+"', "+
											 "p_column_names => '"+columns.stream().map(c -> c.name+",").collect(Collectors.joining())+"', "+			                             
											 "p_column_types => '"+columns.stream().map(c -> c.dataType+",").collect(Collectors.joining())+"', "+			                             
											 "p_column_special => '"+columns.stream().map(c -> c.specialType+",").collect(Collectors.joining())+"', "+			                             
											 "";
			result.append("BEGIN\r\n\tZZDBTK_PACK_DATAMODEL.ENSURE_TABLE_EXIST("+packDatamodelParameters+");\r\nEND;\r\n/\r\n\r\n");
		}
		for(List<String> index :additionalIndexes){
			String packDatamodelParameters = "p_table_name    => '"+tableName+"', "+
											 "p_index_columns => '"+index.stream().map(c -> c+",").collect(Collectors.joining())+"', "+			                             
											 	"";

			result.append("BEGIN\r\n\tZZDBTK_PACK_DATAMODEL.ENSURE_INDEX_EXIST("+packDatamodelParameters+");\r\nEND;\r\n/\r\n\r\n");
		}
		return result.toString(); 
	}

	protected String specificDefinitionAsLineFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("Version  :1\r\n");
		sb.append("TableName:"+tableName+"\r\n");
		sb.append("Type     :"+type+"\r\n");
		sb.append("FullAudit:"+(isFullAuditType?"Y":"N")+"\r\n");
		sb.append("Internal :"+(isInternalType?"Y":"N")+"\r\n");
		sb.append(subMarker1);
		if (columns != null)
			for (DBObjectsModelColumn column : columns){
				sb.append(column);sb.append("\r\n");
			}
		sb.append(subMarker2);
		if (additionalIndexes != null){
			for (List<String> index : additionalIndexes){
				String s = index.toString();
				if (s.length()>2){
					s = s.substring(1, s.length()-1);
					sb.append(s);sb.append("\r\n");
				}
			}
		}
		return sb.toString();
	}

	protected void specificInitFromLineFile(String data) throws StoredObjectParseException {
		String head = getMarkedData(data,subMarker1, true);
		String body = getMarkedData(data,subMarker1, false);
		
		Map<String, String> parsedHead = parseValueMap(head);
		String sVersion = parsedHead.get("VERSION");
		if (sVersion == null){
			throw new StoredObjectParseException("No specific Version information found"); 
		}
		int version = Integer.parseInt(sVersion);
		if (version > 1){
			throw new StoredObjectParseException("Unknown version number "+version); 
		}
		specificInitFromLineFileV1Head(parsedHead);
		specificInitFromLineFileV1Body(body);
	}

	private void specificInitFromLineFileV1Head(Map<String, String> parsedHead) {
		String type_      = parsedHead.get("TYPE");
		String fullaudit_ = parsedHead.get("FULLAUDIT");
		String internal_  = parsedHead.get("INTERNAL");
		if (fullaudit_ == null){
			fullaudit_ = "N";
		}		
		if (internal_ == null){
			internal_ = "N";
		}		
		if (type_ == null){
			type_ = ""+TYPE_DEFAULT;
		}		
		
		tableName       = parsedHead.get("TABLENAME");
		type            = Integer.parseInt(type_);
		isFullAuditType = fullaudit_.toUpperCase().equals("Y");
		isInternalType  = internal_ .toUpperCase().equals("Y");
	}
	private void specificInitFromLineFileV1Body(String body) throws StoredObjectParseException {
		String body1 = getMarkedData(body,subMarker2, true);
		String body2 = getMarkedData(body,subMarker2, false);
		
		columns = new ArrayList<DBObjectsModelColumn>();
		String [] columnData = body1.split("\n");
		for (String column : columnData){
			if (!column.trim().equals(""))
				columns.add(new DBObjectsModelColumn(column.trim()));
		}
		
		additionalIndexes = new ArrayList<List<String>>();
		String [] indexDefs = body2.split("\n");
		for (String indexDef : indexDefs){
			List<String> additionalIndex = new ArrayList<String>();
			String [] indexCols = indexDef.split(",");
			for (String indexCol : indexCols)
				additionalIndex.add(indexCol.trim());

			additionalIndexes.add(additionalIndex);
		}
	}

	
	public Icon getIcon() {
		return IconProvider.get(isValid() ? "rowsource" : "rowsource_invalid");
	}

	protected AbstractPOPanel<? extends AbstractProgramObject> createEditScreen() {
		return new DBTablePanel(this);
	}

	
	protected void fillObjectInvalidStatusMessages(List<String> result) {
		if (columns==null)
			return;
		
		// Consistency checks
		long specialColumnVD = columns.stream().filter(c -> c.specialType == DBObjectsModelColumn.TYPE_DATE).count();
		long specialColumnSD = columns.stream().filter(c -> c.specialType == DBObjectsModelColumn.TYPE_START_DATE).count();
		long specialColumnED = columns.stream().filter(c -> c.specialType == DBObjectsModelColumn.TYPE_END_DATE).count();
		
		switch(type){
		case TYPE_DEFAULT:
			if (specialColumnVD != 0 || specialColumnSD != 0 || specialColumnED != 0)
				result.add("Date columns are assigned, but the table type does not allow for dates.");
			break;
		case TYPE_MEAS:
			if (specialColumnSD != 0 || specialColumnED != 0)
				result.add("Start/End date columns are assigned, but the table type does not allow for start/end dates.");
			if (specialColumnVD == 0)
				result.add("No date columns is assigned, but the table type must have a normal date column.");
			if (specialColumnVD > 1)
				result.add("Multiple date columns are assigned, but the table type can have only one normal date column.");
			break;
		case TYPE_TEMPORAL:
			if (specialColumnVD != 0)
				result.add("Normal date columns are assigned, but the table type does not allow for normal dates.");
			if (specialColumnSD == 0)
				result.add("No start date columns is assigned, but the table type must have a start date column.");
			if (specialColumnSD > 1)
				result.add("Multiple start date columns are assigned, but the table type can have only one start date column.");
			if (specialColumnED == 0)
				result.add("No end date columns is assigned, but the table type must have a end date column.");
			if (specialColumnED > 1)
				result.add("Multiple end date columns are assigned, but the table type can have only one end date column.");
			break;
		}
		
		if (!isInternalType){
			DBObjectsModelRowSource dbRepresentation = DBObjectCache.cache.getCache(tableName);
			if (dbRepresentation != null){
				List<DBObjectsModelColumn> dbRepColumns = dbRepresentation.columns;
				if (dbRepColumns != null){
					Set<DBObjectsModelColumn> dbColumnSet   = dbRepColumns.stream().map(t -> t.cloneExType()).collect(Collectors.toSet());
					Set<DBObjectsModelColumn> onbjColumnSet = columns     .stream().map(t -> t.cloneExType()).collect(Collectors.toSet());
					
					onbjColumnSet.stream().filter(t -> !dbColumnSet.contains(t))
										  .forEach(t -> result.add("Column " + t + " is not present on the database"));
					
					dbColumnSet.stream().filter(t -> !onbjColumnSet.contains(t))
									    .forEach(t -> result.add("Column " + t + " is only present on the database"));
				}
			}
		}
		
		
	}
}
