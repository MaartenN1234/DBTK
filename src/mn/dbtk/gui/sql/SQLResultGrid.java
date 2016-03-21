package mn.dbtk.gui.sql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;




public class SQLResultGrid extends JPanel {
	private SQLPanel sqlFrame;
	
	public SQLResultGrid(SQLPanel sqlFrame){
		setLayout(new BorderLayout());
		this.add(new JScrollPane(new JLabel("No results")), BorderLayout.CENTER);
		this.sqlFrame = sqlFrame;
	}	
	
	public void loadResultSet(ResultSet rs) throws SQLException {
		rs.setFetchSize(500);
		ResultSetMetaData rsmd   = rs.getMetaData();
		
		JPanel [] columnData  = new JPanel[rsmd.getColumnCount()+1];
		JPanel    combined = null;
		int       rowCount = 0;
		

		Font   headerFont   = (new JLabel()).getFont(); 
		Font   gridFont     = headerFont.deriveFont(Font.PLAIN);
		Border columnBorder = new EmptyBorder(0,0,0,3);
		
		SimpleDateFormat todfmt = new SimpleDateFormat("dd-MM-yyyy");


		// fill headers
		for (int i = 0; i<=rsmd.getColumnCount(); i++){
			JPanel column  = new JPanel();
			columnData[i] = new JPanel();
			
			
			final String columnName = i==0 ? "#": rsmd.getColumnName(i);
			JLabel l = new JLabel(columnName);
			l.setFont(headerFont);
			if (i!=0)
				l.addMouseListener(new MouseAdapter(){
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() >= 2)
							if(e.getButton() == MouseEvent.BUTTON3){
								sqlFrame.removeFromSelectList(columnName);
							}
					}
				});			
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.CENTER,5,3));
			JPanel q = new JPanel();
			p.add(l);
			q.setLayout(new BorderLayout());
			q.add(columnData[i], BorderLayout.NORTH);
			column.setLayout(new BorderLayout());
			column.add(p, BorderLayout.NORTH);
			column.add(q, BorderLayout.CENTER);

			
			if (i ==0 ){
				combined = column;
				combined.setBorder(columnBorder);
			} else {
				JPanel    combined2 = new JPanel();
				combined2.setLayout(new BorderLayout());
				combined2.setBorder(columnBorder);
				combined2.add(combined, BorderLayout.CENTER);
				combined2.add(column, BorderLayout.EAST);					
				combined = combined2;
			} 
		}	
		JPanel    combined2 = new JPanel();
		combined2.setLayout(new BorderLayout());
		combined2.add(combined, BorderLayout.WEST);
		combined2.add(new JLabel(), BorderLayout.CENTER);					
		combined = combined2;
		
		
		
		// read all rows
		while(rs.next()){
			StringBuilder fc = new StringBuilder(); 
			rowCount ++;
			for (int i = 1; i<columnData.length; i++){
				String s = "<null>";
				String c = "";
				switch (rsmd.getColumnType(i)) {
				case Types.CHAR:
				case Types.LONGNVARCHAR:
				case Types.LONGVARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
				case Types.VARCHAR:
					// String
					s = rs.getString(i);
					if (rs.wasNull()){
						s = "<null>";
						c = rsmd.getColumnName(i) + " IS NULL";
					} else {
						c = rsmd.getColumnName(i) + "='" +s.replace("'","''")+"'"; 
					}
					fc.append(" AND "+c);
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					// Date
					Date t = rs.getDate(i);
					if (rs.wasNull()){
						s = "<null>";
						c = rsmd.getColumnName(i) + " IS NULL";
					} else {
						s = todfmt.format(t);
						c = rsmd.getColumnName(i) + "=TOD('" +s+"')";
					}
					fc.append(" AND "+c);
					break;
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.REAL:
				case Types.NUMERIC:
					// Double
					double d = rs.getDouble(i);
					if (rs.wasNull()){
						s = "<null>";
						c = rsmd.getColumnName(i) + " IS NULL";						
					} else {
						if (d == Math.round(d) && d>Long.MIN_VALUE && d<Long.MAX_VALUE){
							s = ""+rs.getLong(i);
							c = rsmd.getColumnName(i) + "=" +rs.getLong(i); 	
						} else {
							s = ""+d;
							c = "ROUND("+rsmd.getColumnName(i) + "-" +rs.getDouble(i)+",9)=0";
							if (Math.abs(rs.getDouble(i)) > 1E-9){
								c = "("+c + " OR ROUND("+rsmd.getColumnName(i) + "/" +rs.getDouble(i)+",9)=1)";
							}
						}
					}
					break;
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.TINYINT:
					// Integer
					s = ""+rs.getInt(i);
					if (rs.wasNull()){
						s = "<null>";
						c = rsmd.getColumnName(i) + " IS NULL";		
					} else {
						c = rsmd.getColumnName(i) + "=" +rs.getInt(i);
					}
					fc.append(" AND "+c);
					break;
				default:
					Object o =  rs.getObject(i);
					if (rs.wasNull()){
						s = "<null>";
					} else {
						if(o instanceof oracle.sql.ROWID){
							oracle.sql.ROWID rid = (oracle.sql.ROWID) o;
							s = ""+rowCount;
							c = rsmd.getColumnName(i) + "='" +rid.stringValue()+"'"; 
							fc.append(" AND "+c);
						} else {
							s = rs.getObject(i).toString();
						}
					}
				}				
				JLabel l = new JLabel(s);
				l.setFont(gridFont);
				l.setBackground(Color.WHITE);
				l.setOpaque(true);
				
				if (c!= null){
					final String cf = c;
					final String ncf = "NOT("+c+")";
					l.addMouseListener(new MouseAdapter(){
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() >= 2)
								if(e.getButton() == MouseEvent.BUTTON1){
									sqlFrame.addFilter(cf);
								} else if(e.getButton() == MouseEvent.BUTTON3){
									sqlFrame.addFilter(ncf);
								}
						}
					});
				}
				columnData[i].add(l);					
			}

			JLabel l = new JLabel(""+rowCount);
			l.setFont(gridFont);
			l.setBackground(Color.WHITE);
			l.setOpaque(true);
			
			if (fc.length() > 0){
				String c = fc.toString().substring(5);
				final String cf = "=" + c;
				final String ncf = "NOT(" + c +")";
				l.addMouseListener(new MouseAdapter(){
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() >= 2)
							if(e.getButton() == MouseEvent.BUTTON1){
								sqlFrame.addFilter(cf);
							} else if(e.getButton() == MouseEvent.BUTTON3){
								sqlFrame.addFilter(ncf);
							}
					}
				});
			}
			columnData[0].add(l);					
			
		}


		// size columns
		for (int i = 0; i<columnData.length; i++){
			columnData[i].setLayout(new GridLayout(rowCount,1));
		}

		// create scrollPane
		JScrollPane pane = new JScrollPane(combined);
		pane.getVerticalScrollBar().setUnitIncrement(16);
		pane.getHorizontalScrollBar().setUnitIncrement(16);
		
		// show new data
		this.removeAll();
		this.add(pane, BorderLayout.CENTER);
		this.validate();
	}

}
