package mn.dbtk.sql;

import mn.dbtk.programobjects.StoredObjectParseException;

public class DBObjectsModelColumn {
	public String  name;
	public String  type;
	public boolean inPK;
	
	public DBObjectsModelColumn (String  name, String  type, boolean inPK){
		this.name = name;
		this.type = type;
		this.inPK = inPK;
	}
	
	public DBObjectsModelColumn (String src) throws StoredObjectParseException{
		String [] tokens = src.split("\t");
		if (tokens.length<2){
			throw new StoredObjectParseException("No tab delimiter found in column definition");
		} else {
			name = tokens[0].substring(0, tokens[0].lastIndexOf(" "));
			inPK = tokens[0].endsWith("*");
			type = tokens[1];
		}
			
	}
	public String toString(){
		return name +" "+(inPK?"*":"")+"\t"+type;
	}
}

