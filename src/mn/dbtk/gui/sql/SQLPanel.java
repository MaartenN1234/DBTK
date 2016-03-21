package mn.dbtk.gui.sql;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import mn.dbtk.gui.generics.MyTextAreaBase;
import mn.dbtk.sql.DBObjectCache;
import mn.dbtk.sql.ParsedSQLStatement;
import mn.dbtk.sql.ParseSQLHelper;
import mn.dbtk.sql.SQLConnectionStatics;
import mn.dbtk.sql.ParsedSQLStatement.SelectEntry;


public class SQLPanel extends JPanel  implements KeyListener{
	public static final Insets NO_INSETS = new Insets(0,0,0,0);
	
	private static final int DEFAULT_MAX_OUTPUT_ROWS = 25;

	private static final int GUI_SELECT_RATIO = 2;
	private static final int GUI_FROM_RATIO   = 1;
	private static final int GUI_WHERE_RATIO  = 5;
	private static final int GUI_FILTER_RATIO = 2;
	private static final int GUI_GRID_RATIO   = 7;

	MyTextAreaBase sqlAreaSelect;
	MyTextAreaBase sqlAreaFrom;
	MyTextAreaBase sqlAreaWhere;
	MyTextAreaBase filterSQLArea;
	
	SQLRowSourcePanel  sqlTableGui;
	
	JLabel         outputMessage;
	SQLResultGrid  outputResultGrid;
	MyTextAreaBase outputSQLWindow;
	
	SQLResultsLoaderThread   loaderThread;
	
	String lastSQL         = null;
	KeyStrokeTimerThread keyStrokeTimerThread;
	
	public SQLPanel(){
		initGUI();
		syncOutputGrid();
	}
	
	/*  GUI Elements */	
	private void initGUI(){
		initGUIGlobalComponents();
		
		this.setLayout(new BorderLayout());
		this.add(initGUIGetTopArea(),BorderLayout.CENTER);
	}
	
	private void initGUIGlobalComponents(){
		sqlAreaSelect = new MyTextAreaBase("*",GUI_SELECT_RATIO,0);
		sqlAreaFrom   = new MyTextAreaBase("WP_TD_IDP, WP_REP_EXRATE_DAY",GUI_FROM_RATIO,0);
		sqlAreaWhere  = new MyTextAreaBase("",GUI_WHERE_RATIO,0);	
		filterSQLArea = new MyTextAreaBase("",GUI_FILTER_RATIO,0);

		sqlAreaSelect.addKeyListener(this);
		sqlAreaFrom.addKeyListener(this);
		sqlAreaWhere.addKeyListener(this);
		filterSQLArea.addKeyListener(this);
		
		sqlTableGui = new SQLRowSourcePanel(this);
		
		outputResultGrid       = new SQLResultGrid(this);	
		outputMessage= new JLabel();
	}
	
	private JPanel initGUIGetTopArea() {
		JPanel result        = new JPanel();
		JPanel header        = new JPanel();
		
		
		header.setLayout(new BorderLayout());
		header.add(new JLabel("SQL query"), BorderLayout.WEST);
		header.add(initGUIGetTopButtons(), BorderLayout.EAST);
		
		result.setLayout(new GridBagLayout());
		result.add(header, new GridBagConstraints(
				1, 0, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				NO_INSETS, 0, 0));
		result.add(getAddTableCombo(), new GridBagConstraints(
				2, 0, 1, 1, 0, 0, 
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		result.add(sqlTableGui, new GridBagConstraints(
				2, 1, 1, 10, 0, 0, 
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		
		result.add(new JLabel("Select"), new GridBagConstraints(
				0, 1, 1, 1, 0, 0, 
				GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL,
				NO_INSETS, 0, 0));
		result.add(sqlAreaSelect.getScrollPane(), new GridBagConstraints(
				1, 1, 1, 2, 1, GUI_SELECT_RATIO, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		
		result.add(new JLabel("From"), new GridBagConstraints(
				0, 3, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				NO_INSETS, 0, 0));
		result.add(sqlAreaFrom.getScrollPane(), new GridBagConstraints(
				1, 3, 1, 2, 1, GUI_FROM_RATIO, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		
		result.add(new JLabel("Where"), new GridBagConstraints(
				0, 5, 1, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				NO_INSETS, 0, 0));
		result.add(sqlAreaWhere.getScrollPane(), new GridBagConstraints(
				1, 5, 1, 2, 1, GUI_WHERE_RATIO, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		

		JPanel filterAreaHeader = new JPanel();
		filterAreaHeader.setLayout(new BorderLayout());
		filterAreaHeader.add(new JLabel("Additional result filter"), BorderLayout.WEST);		
		filterAreaHeader.add(initGUIGetFilterButtons(),BorderLayout.EAST);


		JPanel outputAreaHeader = new JPanel();	
		outputAreaHeader.setLayout(new BorderLayout());
		outputAreaHeader.add(new JLabel("Results "), BorderLayout.WEST);
		outputAreaHeader.add(outputMessage, BorderLayout.CENTER);
		
		outputSQLWindow = new MyTextAreaBase("");
		outputSQLWindow.setEditable(false);

		
		JPanel     outputArea = new JPanel();
		CardLayout outputAreaCardLayout = new CardLayout();
		
		outputArea.setLayout(outputAreaCardLayout);
		outputArea.add(outputResultGrid,"RESULTS");
		outputArea.add(outputSQLWindow.getScrollPane(),"SQL");
				
		outputAreaHeader.add(initGUIGetResultButtons(outputArea), BorderLayout.EAST);
		
		result.add(filterAreaHeader, new GridBagConstraints(
				0, 7, 2, 1, 1, 0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				NO_INSETS, 0, 0));
		result.add(filterSQLArea.getScrollPane(), new GridBagConstraints(
				0, 8, 2, 1, 1, GUI_FILTER_RATIO, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));
		result.add(outputAreaHeader, new GridBagConstraints(
				0, 9, 2, 1, 1, 0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				NO_INSETS, 0, 0));
		result.add(outputArea, new GridBagConstraints(
				0, 10, 2, 1, 1, GUI_GRID_RATIO, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				NO_INSETS, 0, 0));		

		
		return result;
	}
	
	private JComponent getAddTableCombo() {
		String [] allTables = DBObjectCache.cache.getAllRowSources().toArray(new String[]{});
		Arrays.sort(allTables);
		final JComboBox<String> comboBox = new JComboBox<String>(allTables);
		comboBox.setEditable(false);
		comboBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String addedTable = (String) comboBox.getSelectedItem();
				addToTableList(addedTable);
			}
		});
		
		
		JPanel result  = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		result.add(comboBox);
		return result;
	}

	private JComponent initGUIGetTopButtons() {		
		JButton naturalDateJoinButton = new JButton("date-join");
		JButton naturalFullJoinButton = new JButton("full-join");
		
		naturalDateJoinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	addNormalJoinPredicate(true);
            }	
		});
		naturalFullJoinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	addNormalJoinPredicate(false);
            }	
		});
		
		
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.RIGHT, 5,0));
		result.add(naturalDateJoinButton);
		result.add(naturalFullJoinButton);
		return result;
	}

	private JComponent initGUIGetFilterButtons() {
		JButton clearFilterAreaButton = new JButton("clear");
		clearFilterAreaButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e){
	            	filterSQLArea.setText("");
	            	syncOutputGrid();
	            }	
		});
		
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.RIGHT, 5,0));
		result.add(clearFilterAreaButton);
		return result;
	}
	
	private JComponent initGUIGetResultButtons(JPanel outputArea) {
		final JToggleButton viewSQLButton  = new JToggleButton("SQL");
		final JPanel        areaCardPanel  = outputArea;
		final CardLayout    areaCardLayout = (CardLayout) (outputArea.getLayout());
		
		viewSQLButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e){
		    	if(viewSQLButton.isSelected()){
		    		areaCardLayout.show(areaCardPanel, "SQL");
		    	} else {
		    		areaCardLayout.show(areaCardPanel, "RESULTS");
		    	}
		    	
		    }	
		});
		
		JPanel result = new JPanel();
		result.setLayout(new FlowLayout(FlowLayout.RIGHT, 5,0));
		result.add(viewSQLButton);
		return result;		
	}	
	
	/*  Complex GUI Actions */	
	private void addNormalJoinPredicate(boolean datesOnly) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.addNormalJoinPredicate(datesOnly);
		fillTextAreasFromStatement(sqlStatement, true, true, true, true);
		syncOutputGrid();
	}
	public void addWhereClause(String whereClause) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.addWhereClause(whereClause);
		fillTextAreasFromStatement(sqlStatement, false, false, true, false);
		syncOutputGrid();
	}
	public void addToSelectList(String sourceTableAlias, String columnName) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.addToSelectList(sourceTableAlias, columnName);
		fillTextAreasFromStatement(sqlStatement, true, false, false, false);
		syncOutputGrid();
	}	
	private void addToTableList(String addedTable) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.addToTableList(addedTable);
		sqlTableGui.addAsExpandedPanel(addedTable);
		fillTextAreasFromStatement(sqlStatement, true, true, false, false);
		syncOutputGrid();
	}	
	public void addFilter(String clause){
		if (clause.startsWith("=")){
			filterSQLArea.setText(clause.substring(1));
		} else {
			ParsedSQLStatement sqlStatement = composeSQL(false);
			sqlStatement.addFilterClause(clause);
			fillTextAreasFromStatement(sqlStatement, false, false, false, true);
		}
		syncOutputGrid();
	}
	public void removeFromSelectList(String columnName) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.removeSelectColumn(columnName);
		fillTextAreasFromStatement(sqlStatement, true, false, false, true);
		syncOutputGrid();
	}
	public void setOuterjoinStatus(String name, boolean selected) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.setOuterjoinStatus(name, selected);
		fillTextAreasFromStatement(sqlStatement, false, true, true, false);
		syncOutputGrid();
	}	
	public void removeFromTableList(String columnName) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		if(sqlStatement.isTableUsed(columnName)){
			switch(JOptionPane.showConfirmDialog(this, 
							"The table to be remove is used in other SQL parts, should these parts be adjusted ?", 
							"Delete table ?", JOptionPane.YES_NO_CANCEL_OPTION)) {
				case JOptionPane.CANCEL_OPTION:
					return;
				case JOptionPane.YES_OPTION:
					sqlStatement.removeFromTableList(columnName, true);
					break;
				case JOptionPane.NO_OPTION:
					sqlStatement.removeFromTableList(columnName, false);
					break;
			}
		} else
			sqlStatement.removeFromTableList(columnName, false);
		
		fillTextAreasFromStatement(sqlStatement, true, true, true, true);
		syncOutputGrid();
	}	
	public void changeAliasInSelectList(String oldValue, String newValue) {
		ParsedSQLStatement sqlStatement = composeSQL(false);
		sqlStatement.changeAliasInSelectList(oldValue, newValue);
		fillTextAreasFromStatement(sqlStatement, true, false, false, true);
		syncOutputGrid();
	}
	
	
	private ParsedSQLStatement composeSQL(boolean addLimiter){
		String selectSql = sqlAreaSelect.getText();
		String fromSql   = sqlAreaFrom.getText();
		String whereSql  = sqlAreaWhere.getText();

		String filterSql = filterSQLArea.getText();
		
		if (addLimiter)
			filterSql = filterSql
				         + (filterSQLArea.getText().trim().length()==0 ? "":" AND ")
				         + "ROWNUM <= "+DEFAULT_MAX_OUTPUT_ROWS;
	
		return new ParsedSQLStatement(selectSql, fromSql, whereSql, filterSql);
	}
	
		
	private void fillTextAreasFromStatement(ParsedSQLStatement pss, boolean updateSelectArea,
		boolean updateFromArea, boolean updateWhereArea, boolean updateFilterArea) {
		sqlTableGui.syncTo(pss);
		if (updateSelectArea) sqlAreaSelect.setText(pss.toSQLSelect());
		if (updateFromArea) sqlAreaFrom.setText(pss.toSQLFrom(true));
		if (updateWhereArea) sqlAreaWhere.setText(pss.toSQLWhere(pss.whereClauses, 1, true));
		if (updateFilterArea) filterSQLArea.setText(pss.toSQLWhere(pss.filterClauses, -1, true));
		
	}
	
	private void syncOutputGrid(){
		if(keyStrokeTimerThread != null) 
			keyStrokeTimerThread.stopRunning();
		keyStrokeTimerThread = null;

		ParsedSQLStatement sqlStatement = composeSQL(true);
		if (sqlStatement.parseOk){
			sqlTableGui.syncTo(sqlStatement);
			String sql = sqlStatement.toSQL();
						
			if (ParseSQLHelper.transformToComparableSQL(sql).equals(lastSQL))
				return;		
			lastSQL = ParseSQLHelper.transformToComparableSQL(sql);		
			
			outputSQLWindow.setText(sql);
			outputMessage.setForeground(Color.BLACK);
			outputMessage.setText("- executing " + formatTimeStamp());
			if(loaderThread != null)
				loaderThread.cancel();
			loaderThread = new SQLResultsLoaderThread(sql);
			loaderThread.start();
		} else {
			outputMessage.setForeground(Color.RED);
			outputMessage.setText("- "  + sqlStatement.parseErrors.get(0));	
			StringBuilder sb = new StringBuilder();
			sb.append("Parse errors:\r\n");
			for (String msg:sqlStatement.parseErrors){
				sb.append(msg);
				sb.append("\r\n");
			}
			if(sqlStatement.parseWarnings.size()>0){
				sb.append("Parse warnings:\r\n");
				for (String msg:sqlStatement.parseWarnings){
					sb.append(msg);
					sb.append("\r\n");
				}
			}		
			outputSQLWindow.setText(sb.toString());
		}
	}

	
	/*  Threading delegates */	
	class SQLResultsLoaderThread extends Thread {
		PreparedStatement ps = null;
		ResultSet rs         = null;
		String sql           = null;
		boolean cancelled    = false;

		SQLResultsLoaderThread(String sql){
			this.sql = sql;
		}
		public void cancel(){
			cancelled = true;
			try {
				if (ps!=null && !ps.isClosed())
					ps.cancel();
			} catch (SQLException e) {}
			try {
				if (ps!=null && !ps.isClosed())
					ps.close();
			} catch (SQLException e) {}
		}
		public void run(){
			try {
				if (!cancelled){
					ps = SQLConnectionStatics.prepareSQLSelect(sql);
					rs = ps.executeQuery();
				}
			} catch (SQLException e) {
				if (e.getErrorCode() != -1013){
					outputMessage.setForeground(Color.RED);
					outputMessage.setText("- " + e.getMessage());
				}
				cancelled = true;
			} 
			
			if (!cancelled && rs != null){
				outputMessage.setForeground(Color.BLACK);
				outputMessage.setText("- fetching " + formatTimeStamp());
				try{
					outputResultGrid.loadResultSet(rs);
					outputMessage.setText("");
				}  catch (SQLException e){
					if (e.getErrorCode() != -1013){
						outputMessage.setForeground(Color.RED);
						outputMessage.setText("- " + e.getMessage());
					}	
					throw new RuntimeException(e);
					
				}

			}
			
			if(rs!= null)
				try {rs.close();} catch (SQLException e) {}
			if (ps != null)
				try {ps.close();} catch (SQLException e) {}
		}
	}

	private String formatTimeStamp() {
		SimpleDateFormat fmTS  = new SimpleDateFormat("HH:mm:ss");
		return fmTS.format(new Date());
	}		
	public void keyTyped(KeyEvent e) {
		setKeyStroke();
	}
	public void keyPressed(KeyEvent e) {
		setKeyStroke();
	}
	public void keyReleased(KeyEvent e) {
		setKeyStroke();
	}
	private void setKeyStroke(){
		if (keyStrokeTimerThread == null || keyStrokeTimerThread.completed){
			keyStrokeTimerThread = new KeyStrokeTimerThread();
			keyStrokeTimerThread.start();
		}
		keyStrokeTimerThread.setKeyStroke();
	}

	class KeyStrokeTimerThread extends Thread{
		final long  keyStokeTimeout = 500;
		long nextTimeout = 0;
		boolean completed = false;
		void setKeyStroke(){
			nextTimeout =  System.currentTimeMillis() + keyStokeTimeout;					
		}
		public void stopRunning() {
			nextTimeout = 1;
			completed   = true;
			
		}
		public void run(){
			while(nextTimeout == 0 || System.currentTimeMillis() < nextTimeout){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					interrupt();
				}
			}
			if (!completed){
				completed = true;
				syncOutputGrid();
			}			
		}
		
	}



}
