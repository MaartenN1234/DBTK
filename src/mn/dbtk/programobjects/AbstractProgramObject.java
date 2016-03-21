package mn.dbtk.programobjects;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractProgramObject {
	protected final static String blockMarkerStart = "\r\n#9(}z~@";
	private   final static String commentsMarker   = blockMarkerStart + "START comments\r\n";
	private   final static String endMarker        = blockMarkerStart + "ENDDEF AbstractPO\r\n";
	protected final static String subMarker1       = blockMarkerStart + "1\r\n";
	protected final static String subMarker2       = blockMarkerStart + "2\r\n";
	protected final static String subMarker3       = blockMarkerStart + "3\r\n";
	protected final static String subMarker4       = blockMarkerStart + "4\r\n";
	protected final static String subMarker5       = blockMarkerStart + "5\r\n";
	
	public  String  location = "/";
	public  String  name;
	public  String  uid;
	public  String  comments;
	String  classType;
	public  boolean deleted;
	
	AbstractProgramObject(){
	}
	
	public AbstractProgramObject(String location, String name){
		this.location = location;
		this.name     = name;
		this.comments = "";
		this.deleted  = false;
		this.uid      = ProgramObjectFactory.getNewUID();
				
	}
	
	public abstract String createDatabaseScript();
	
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
	
	protected abstract String specificDefinitionAsLineFile();
	
	
	
	void initFromLineFile(String data) throws StoredObjectParseException{
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

	protected abstract void specificInitFromLineFile(String data) throws StoredObjectParseException;
	
}
