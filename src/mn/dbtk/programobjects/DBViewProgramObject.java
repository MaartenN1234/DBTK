package mn.dbtk.programobjects;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import mn.dbtk.gui.generics.IconProvider;
import mn.dbtk.gui.programobjects.AbstractPOPanel;

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
		
		specificInitFromLineFileV1Body(body);
	}

	private void specificInitFromLineFileV1Body(String body) {
		selectSQL   = body;		
	}

	public Icon getIcon() {
		return IconProvider.get(isValid() ? "view" : "view_invalid");
	}

	protected AbstractPOPanel<? extends AbstractProgramObject> createEditScreen() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillObjectInvalidStatusMessages(List<String> result) {
		// TODO Auto-generated method stub
		
	}
}
