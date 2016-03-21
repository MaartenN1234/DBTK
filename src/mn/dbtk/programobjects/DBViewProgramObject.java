package mn.dbtk.programobjects;

import java.util.Map;

public class DBViewProgramObject extends AbstractProgramObject {

	public String selectSQL = "";
	
	DBViewProgramObject() {
		 super();
	}
	
	public DBViewProgramObject(String location, String name) {
		super(location, name);
	}

	public String createDatabaseScript() {
		return "CREATE OR REPLACE FORCE VIEW "+name + " AS \r\n" +
	           selectSQL;
	}

	protected String specificDefinitionAsLineFile() {
		StringBuilder sb = new StringBuilder();
		sb.append("Version  :1\r\n");
		sb.append(subMarker1);
		sb.append(selectSQL);
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
		
		specificInitFromLineFileV1(parsedHead, body);
	}

	private void specificInitFromLineFileV1(Map<String, String> parsedHead, String body) {
		selectSQL   = body;		

	}
}
