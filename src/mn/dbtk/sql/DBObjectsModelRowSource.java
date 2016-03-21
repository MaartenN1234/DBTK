package mn.dbtk.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class DBObjectsModelRowSource {
	public static final int TYPE_TABLE    = 1;
	public static final int TYPE_VIEW     = 2;
	public static final int TYPE_SYNONYM  = 3;
	
	public int                           type;
	public Set<String>                   dependents;
	public List<DBObjectsModelColumn>    columns;
	
	public DBObjectsModelRowSource(){
		columns    = new ArrayList<DBObjectsModelColumn>();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		switch(type){
		case TYPE_TABLE:
			sb.append("TABLE");
			break;
		case TYPE_VIEW:
			sb.append("VIEW");
			break;
		case TYPE_SYNONYM:
			sb.append("SYNONYM");
			break;
		default:
			sb.append("<unknown>");
			break;
		}
		if (dependents!= null){
			sb.append("\n");
			sb.append(dependents.toString());
		}
		for (DBObjectsModelColumn col : columns){
			sb.append("\n");
			sb.append(col.toString());
		}
		
		return sb.toString();
	}
}
