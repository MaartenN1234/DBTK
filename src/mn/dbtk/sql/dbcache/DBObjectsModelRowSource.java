package mn.dbtk.sql.dbcache;

import java.util.ArrayList;
import java.util.HashSet;
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
	
	public boolean equals(Object other){
		if (other == null)
			return false;
		if (other instanceof DBObjectsModelRowSource){
			DBObjectsModelRowSource that = (DBObjectsModelRowSource) other;
			return this.type == that.type &&
				   this.dependents.equals(that.dependents) &&
				   this.columns.equals(that.columns);
		}
		return false;
	}
	public int hashCode(){
		return (dependents.hashCode() * 31 +
				columns.hashCode()) * 31 +
				type;
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
	
	public boolean isComparableColumnSet(List<DBObjectsModelColumn> other){
		if (other == null || other.size() == 0)
			return (columns == null || columns.size()==0);
		if (columns.size() != other.size())
			return false;
		
		Set<String> ours  = new HashSet<String>();
		Set<String> theirs= new HashSet<String>();
		
		for(DBObjectsModelColumn c : columns)
			ours.add(c.name+"\t"+c.dataType);
		for(DBObjectsModelColumn c : other){
			ours.remove(c.name+"\t"+c.dataType);
			theirs.add(c.name+"\t"+c.dataType);
		}
		for(DBObjectsModelColumn c : columns)
			theirs.remove(c.name+"\t"+c.dataType);
		
		return (ours.size()==0 && theirs.size() ==0);
	}
}
