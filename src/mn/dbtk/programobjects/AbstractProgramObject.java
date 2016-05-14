package mn.dbtk.programobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import mn.dbtk.config.Configuration;
import mn.dbtk.gui.main.MainWindow;
import mn.dbtk.gui.objecttree.ProgramObjectNode;
import mn.dbtk.gui.objecttree.TreeObjectPanel;
import mn.dbtk.gui.programobjects.AbstractPOPanel;


public abstract class AbstractProgramObject{
	// Representation
	public  String  location = "/";
	public  String  name;
	public  String  uid;
	public  String  comments;
	public  boolean deleted;
	String  classType;
	
	private String lastSavedFile;
	
	// Gui cache
	private transient ProgramObjectNode objectTreeNode;
	private transient AbstractPOPanel<? extends AbstractProgramObject> editScreen;
	
	AbstractProgramObject(){
	}
	
	public AbstractProgramObject(String location, String name){
		this.location = location;
		this.name     = name;
		this.comments = "";
		this.deleted  = false;
		this.uid      = ProgramObjectStore.storeAndGetUID(this);
	}
	


	
	// Code generation
	public abstract String createDatabaseScript();

	// Gui respresentation
	public abstract Icon getIcon();
	
	protected abstract AbstractPOPanel<? extends AbstractProgramObject> createEditScreen();

	public AbstractPOPanel<? extends AbstractProgramObject> getEditScreen(){
		if (editScreen == null)
			editScreen = createEditScreen();
		
		return editScreen;		
	}

	public ProgramObjectNode getObjectTreeNode(TreeObjectPanel tree){
		if (objectTreeNode == null)
			objectTreeNode = new ProgramObjectNode(tree, this);
		
		return objectTreeNode;
	}
	
	public void notifyDefinitionUpdate(Object changeSource){
		if (objectTreeNode != null && objectTreeNode != changeSource)
			objectTreeNode.syncToObjectDefinition();
		if (editScreen != null && editScreen != changeSource)
			editScreen.reloadFromObjectDefinition();
		if (TreeObjectPanel.singleton != null && TreeObjectPanel.singleton != changeSource)
			TreeObjectPanel.singleton.refreshObjectList();	
		
	}
	
	public void cleanup() {
		if (editScreen != null)
			editScreen.closeFromObject();
	}

	// Storage markers
	protected final static String blockMarkerStart = "\r\n#9(}z~@";
	private   final static String commentsMarker   = blockMarkerStart + "START comments\r\n";
	private   final static String endMarker        = blockMarkerStart + "ENDDEF AbstractPO\r\n";
	protected final static String subMarker1       = blockMarkerStart + "1\r\n";
	protected final static String subMarker2       = blockMarkerStart + "2\r\n";
	protected final static String subMarker3       = blockMarkerStart + "3\r\n";
	protected final static String subMarker4       = blockMarkerStart + "4\r\n";
	protected final static String subMarker5       = blockMarkerStart + "5\r\n";

	// Write file
	public String definitionAsLineFile(){
		StringBuilder sb = new StringBuilder();
		sb.append("Version  :1\r\n");
		sb.append("Location :"+location+"\r\n");
		sb.append("Name     :"+name+"\r\n");
		sb.append("UID      :"+uid+"\r\n");
		sb.append("Class    :"+this.getClass().getName() +"\r\n");
		sb.append("Deleted  :"+(deleted?"Y":"N")+"\r\n");
		sb.append(commentsMarker);
		sb.append(comments);
		sb.append(endMarker);
		sb.append(specificDefinitionAsLineFile());
		return sb.toString();
	}
	public void save(){
		lastSavedFile = definitionAsLineFile();
		Configuration.getActive().writeToFile(uid+".txt", lastSavedFile);
	}
	public void saveIfNotOpened() {
		boolean isOpened = false;
		JTabbedPane mainTabbedPane = MainWindow.frame.getMainTabbedPane();
		for (int i=0; i<mainTabbedPane.getTabCount(); i++)
			if (mainTabbedPane.getComponentAt(i) instanceof AbstractPOPanel){
				AbstractPOPanel<?> panel = (AbstractPOPanel<?>) mainTabbedPane.getComponentAt(i);
				isOpened |= panel.getProgramObjectBypassSync() == this;
			}

		if (!isOpened)
			save();
	}
	
	public void reload() {
		if(hasChangesSinceLastSave()){
			try {
				loadFromFile(lastSavedFile);
			} catch (StoredObjectParseException e) {
			}
			notifyDefinitionUpdate(this);
		}				
	}
	
	public boolean hasChangesSinceLastSave(){
		return lastSavedFile == null || !lastSavedFile.equals(definitionAsLineFile());
	}
	
	protected abstract String specificDefinitionAsLineFile();
	
	
	// Read file
	void loadFromFile(String data) throws StoredObjectParseException{
		lastSavedFile = data;
		String abstractPOData = getMarkedData(data, endMarker, true);
		String specificPOData = getMarkedData(data, endMarker, false);
		
		if (specificPOData==null){
			throw new StoredObjectParseException("Corrupt file, ENDDEF AbstractPO expected"); 
		}
		
		abstractInitFromLineFile(abstractPOData);
		specificInitFromLineFile(specificPOData);
	}
	
	private void abstractInitFromLineFile(String data) throws StoredObjectParseException {
		comments = getMarkedData(data, commentsMarker, false);
		if (comments == null){
			comments = "";
		}
		
		String abstractPOMapData = getMarkedData(data, commentsMarker, true);
		Map<String, String> parsedData = parseValueMap(abstractPOMapData);
		String sVersion = parsedData.get("VERSION");
		if (sVersion == null){
			throw new StoredObjectParseException("No abstractPO Version information found"); 
		}
		int version = Integer.parseInt(sVersion);
		if (version > 1){
			throw new StoredObjectParseException("Unknown version number "+version); 
		}
		abstractInitFromLineFileV1(parsedData);		
	}
	private void abstractInitFromLineFileV1(Map<String, String> parsedData)  throws StoredObjectParseException{
		String loc_    = parsedData.get("LOCATION");
		String name_   = parsedData.get("NAME");
		String uid_    = parsedData.get("UID");
		String del_    = parsedData.get("DELETED");
		String class_  = parsedData.get("CLASS");
		if (loc_ == null){
			loc_ = "/";
		}		
		if (name_ == null){
			throw new StoredObjectParseException("No abstractPO Name information found"); 
		}	
		if (uid_ == null){
			throw new StoredObjectParseException("No abstractPO UID information found"); 
		}			
		if (class_ == null){
			throw new StoredObjectParseException("No abstractPO Class information found"); 
		}			
		if (del_ == null){
			del_ = "N";
		}
		
		location  = loc_;
		name      = name_;
		uid       = uid_;
		deleted   = del_.toUpperCase().equals("Y");
		classType = class_;
	}	
	
	protected abstract void specificInitFromLineFile(String data) throws StoredObjectParseException;
	
	// Read file parse helper methods
	protected String getMarkedData(String data, String marker, boolean before){
		String result;
		int i = data.indexOf(marker);
		if (i==-1){
			result = before ? data : null;
		} else {
			if (before){
				result = data.substring(0,i);
			} else {
				result = data.substring(i + marker.length());
			}
		}
		return result;
	}
	protected Map<String, String> parseValueMap(String data){
		return parseValueMap(data, "\n", ":");
	}	
	protected Map<String, String> parseValueMap(String data, String lineDelim, String wordDelim) {
		Map<String, String> result = new HashMap<String, String>();
		String [] dataTokens = data.split(lineDelim);
		for(String token:dataTokens){
			String [] subs = token.split(wordDelim);
			if (subs.length>=2){
				result.put(subs[0].trim().toUpperCase(), subs[1].trim());
			}
		}
		return result;
	}

	
	public boolean isValid(){
		return getObjectInvalidStatusMessages().size() == 0;
	}	
	public List<String> getObjectInvalidStatusMessages(){
		List<String> result = new ArrayList<String>();
		if (name == null || name.trim().length() == 0)
			result.add("Object name is of zero length.");			
		
		fillObjectInvalidStatusMessages(result);
		return result;
	}

	protected abstract void fillObjectInvalidStatusMessages(List<String> result);


}
