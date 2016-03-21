package mn.dbtk.programobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mn.dbtk.sql.DBObjectsModelColumn;

public class DBTableProgramObject extends AbstractProgramObject {
	public final static int TYPE_DEFAULT  = 1;
	public final static int TYPE_MEAS     = 2;
	public final static int TYPE_TEMPORAL = 3;
	
	public boolean isFullAuditType                      = false;
	public boolean isInternalType                       = false;
	public int     type                                 = TYPE_DEFAULT;
	public List<DBObjectsModelColumn> columns           = null;
	public List<List<String>>         additionalIndexes = null;
	
	
	DBTableProgramObject() {
		 super();
	}
	
	public DBTableProgramObject(String location, String name) {
		super(location, name);
	}

	public String createDatabaseScript() {
		//TODO
		throw new RuntimeException("Not implemented: DBTableProgramObject.createDatabaseScript()");
	}

	protected String specificDefinitionAsLineFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("Version  :1\r\n");
		sb.append("Type     :"+type+"\r\n");
		sb.append("FullAudit:"+(isFullAuditType?"Y":"N")+"\r\n");
		sb.append("Internal :"+(isInternalType?"Y":"N")+"\r\n");
		sb.append(subMarker1);
		for (DBObjectsModelColumn column : columns){
			sb.append(column);sb.append("\r\n");
		}
		sb.append(subMarker2);
		for (List<String> index : additionalIndexes){
			String s = index.toString();
			s = s.substring(1, s.length()-2);
			sb.append(s);sb.append("\r\n");
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
}
