package mn.dbtk.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class StringResultSetLoaderThread extends Thread{
	private String  sql;
	public  String  exceptionMessage;
	private boolean normalCompletion;
	private List<String[]> results;
	private Connection        conn;
	private PreparedStatement ps;
	
	StringResultSetLoaderThread(String sql){
		this.sql = sql;
		normalCompletion=false;
	}
	
	private void processResultSet(ResultSet rs) throws SQLException{
		results = new ArrayList<String []>();

		while(rs.next()){
			String [] ss = new String[rs.getMetaData().getColumnCount()];
			for (int i = 0; i<ss.length; i++)
				ss[i] = rs.getString(i+1);
			results.add(ss);
		}
	}
	
	public void run(){
		try{
			conn         = SQLConnectionStatics.getConnection();
			ps           = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.setFetchSize(5000);
			processResultSet(rs);
			rs.close();
			ps.close();
			ps   = null;
			conn.close();
			conn = null;
			normalCompletion = true;
		} catch(SQLException e){
			exceptionMessage = e.getMessage();
			if (ps != null) try{ps.close();} catch(SQLException e2){}
			if (conn != null) try{conn.close();} catch(SQLException e2){}
			throw new RuntimeException(exceptionMessage);
		}
	}
	public boolean sync(){
		try {
			join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return normalCompletion;		
	}
	public List<String[]> getResults(){
		return results;
	}

	public void cancel() {
		PreparedStatement psAtomicFetch = ps;
		if (psAtomicFetch != null) try{psAtomicFetch.cancel();} catch(SQLException e){}
	}
}
