package mn.dbtk.sql.dbcache;

import java.io.Serializable;

import mn.dbtk.programobjects.StoredObjectParseException;

public class DBObjectsModelColumn  {
	public final static int TYPE_UNKNOWN      = -1;
	public final static int TYPE_NORMAL       = 0;
	public final static int TYPE_NORMAL_PK    = 1;
	public final static int TYPE_DATE         = 2;
	public final static int TYPE_START_DATE   = 3;
	public final static int TYPE_END_DATE     = 4;
	
	public String  name;
	public String  dataType;
	public int     specialType;
	
	public DBObjectsModelColumn (String  name, String dataType, int specialType){
		this.name        = name;
		this.dataType    = dataType;
		this.specialType = specialType;
	}
	public DBObjectsModelColumn cloneExType(){
		return new DBObjectsModelColumn(name, dataType, TYPE_UNKNOWN);
	}
	public DBObjectsModelColumn (String  name, String dataType, String specialType){
		this(name, dataType, specialTypeStringAsInt(specialType));
	}	
	public DBObjectsModelColumn (String src) throws StoredObjectParseException{
		String [] tokens = src.split("\t");
		if (tokens.length<2){
			throw new StoredObjectParseException("No tab delimiter found in column definition");
		} else {
			name        = tokens[0].substring(0, tokens[0].lastIndexOf(" "));
			dataType    = tokens[1];
			specialType = tokens[0].endsWith(" "+ TYPE_NORMAL) ? TYPE_NORMAL :
						  tokens[0].endsWith(" "+ TYPE_NORMAL_PK) ? TYPE_NORMAL_PK :
						  tokens[0].endsWith(" "+ TYPE_DATE) ? TYPE_DATE :
						  tokens[0].endsWith(" "+ TYPE_START_DATE) ? TYPE_START_DATE :
						  tokens[0].endsWith(" "+ TYPE_END_DATE) ? TYPE_END_DATE : 
							  TYPE_UNKNOWN;
		}
			
	}
	public boolean equals(Object other){
		if (other == null)
			return false;
		if (other instanceof DBObjectsModelColumn){
			DBObjectsModelColumn that = (DBObjectsModelColumn) other;
			return this.specialType == that.specialType &&
				   this.name.equals(that.name) &&
				   this.dataType.equals(that.dataType);
		}
		return false;
	}
	public int hashCode(){
		return (name + dataType + specialType).hashCode();
	}
	
	public String toString(){
		return name +" "+specialType+"\t"+dataType;
	}

	public String specialTypeAsString() {
		return specialTypeAsString(specialType);
	}
	public static int specialTypeStringAsInt(String s) {
		if (s.equals("VALUE"))
			return TYPE_NORMAL;
		if (s.equals("KEY"))
			return TYPE_NORMAL_PK;
		if (s.equals("DATE"))
			return TYPE_DATE;
		if (s.equals("START_DATE"))
			return TYPE_START_DATE;
		if (s.equals("END_DATE"))
			return TYPE_END_DATE;
		return TYPE_UNKNOWN;
	}
	public static String specialTypeAsString(int i) {
		switch(i){
		case TYPE_NORMAL:
			return "VALUE";
		case TYPE_NORMAL_PK:
			return "KEY";
		case TYPE_DATE:
			return "DATE";
		case TYPE_START_DATE:
			return "START_DATE";
		case TYPE_END_DATE:
			return "END_DATE";
		}
		return "UNKNOWN";
	}
}

