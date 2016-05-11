package mn.dbtk.gui.programobjects;

import javax.swing.JPanel;

import mn.dbtk.gui.generics.MyJTable;
import mn.dbtk.programobjects.DBTableProgramObject;
import mn.dbtk.sql.dbcache.DBObjectCache;
import mn.dbtk.sql.dbcache.DBObjectCacheSyncedGUIElements;
import mn.dbtk.sql.dbcache.DBObjectsModelColumn;
import mn.dbtk.sql.dbcache.DBObjectsModelRowSource;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JComboBox;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

public class DBTableExternalSubPanel extends JPanel {
	private DBTablePanel         masterPanel;
	private MyJTable             columnGrid;
	private JRadioButton         rdbtnNoDate;
	private JRadioButton         rdbtnSingleDate;
	private JRadioButton         rdbtnDateRange;
	private JComboBox<String>    selectingTable;
	private JLabel               messageLabel;
	
	final private DBTableProgramObject programObject;
	
	private boolean propagateChange = true;

	
	/**
	 * Create the panel.
	 */
	public DBTableExternalSubPanel(DBTablePanel masterPanel) {
		this.masterPanel             = masterPanel;
		programObject = masterPanel.getProgramObjectBypassSync();
		init();
	}
	
	private void init(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		JLabel lblTableName = new JLabel("Table name");
		GridBagConstraints gbc_lblTableName = new GridBagConstraints();
		gbc_lblTableName.insets = new Insets(0, 2, 3, 5);
		gbc_lblTableName.anchor = GridBagConstraints.WEST;
		gbc_lblTableName.gridx = 0;
		gbc_lblTableName.gridy = 0;
		add(lblTableName, gbc_lblTableName);
		
		selectingTable = new DBObjectCacheSyncedGUIElements.RowsourcesComboBox(programObject != null ? programObject.tableName : null);
		selectingTable.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					updateTableTypeRadioButtons();
					updateGrid();
				}
		});
		GridBagConstraints gbc_SelectingTable = new GridBagConstraints();
		gbc_SelectingTable.gridwidth = 4;
		gbc_SelectingTable.anchor = GridBagConstraints.WEST;
		gbc_SelectingTable.insets = new Insets(0, 0, 3, 5);
		gbc_SelectingTable.gridx = 1;
		gbc_SelectingTable.gridy = 0;
		add(selectingTable, gbc_SelectingTable);
		
		messageLabel = new JLabel("");
		messageLabel.setForeground(Color.RED);
		GridBagConstraints gbc_messageLabel = new GridBagConstraints();
		gbc_messageLabel.insets = new Insets(0, 2, 5, 5);
		gbc_messageLabel.anchor = GridBagConstraints.WEST;
		gbc_messageLabel.gridx = 0;
		gbc_messageLabel.gridwidth = 5;
		gbc_messageLabel.gridy = 1;
		add(messageLabel, gbc_messageLabel);
		
		
		JLabel lblTableType = new JLabel("Table type");
		GridBagConstraints gbc_lblTableType = new GridBagConstraints();
		gbc_lblTableType.insets = new Insets(0, 2, 5, 5);
		gbc_lblTableType.anchor = GridBagConstraints.WEST;
		gbc_lblTableType.gridx = 0;
		gbc_lblTableType.gridy = 2;
		add(lblTableType, gbc_lblTableType);
		
		rdbtnNoDate = new JRadioButton("No date");
		rdbtnNoDate.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					 updateGrid();
					 syncFormToObject();
				}
		});
		GridBagConstraints gbc_rdbtnNoDate = new GridBagConstraints();
		gbc_rdbtnNoDate.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNoDate.gridx = 1;
		gbc_rdbtnNoDate.gridy = 2;
		add(rdbtnNoDate, gbc_rdbtnNoDate);
		
		rdbtnSingleDate = new JRadioButton("Single date");
		rdbtnSingleDate.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					 updateGrid();
					 syncFormToObject();
				}
		});		
		GridBagConstraints gbc_rdbtnSingleDate = new GridBagConstraints();
		gbc_rdbtnSingleDate.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnSingleDate.gridx = 2;
		gbc_rdbtnSingleDate.gridy = 2;
		add(rdbtnSingleDate, gbc_rdbtnSingleDate);
		
		rdbtnDateRange = new JRadioButton("Date Range");
		rdbtnDateRange.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					 updateGrid();
					 syncFormToObject();
				}
		});			
		GridBagConstraints gbc_rdbtnDateRange = new GridBagConstraints();
		gbc_rdbtnDateRange.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnDateRange.gridx = 3;
		gbc_rdbtnDateRange.gridy = 2;
		add(rdbtnDateRange, gbc_rdbtnDateRange);
		
		 ButtonGroup group = new ButtonGroup();
		 group.add(rdbtnNoDate);
		 group.add(rdbtnSingleDate);
		 group.add(rdbtnDateRange);

		
		initGrid();
		GridBagConstraints gbc_grid = new GridBagConstraints();
		gbc_grid.insets = new Insets(0, 0, 0, 5);
		gbc_grid.anchor = GridBagConstraints.NORTHWEST;
		gbc_grid.gridwidth = 5;
		gbc_grid.gridx = 0;
		gbc_grid.gridy = 3;
		add(columnGrid.getScrollPane(), gbc_grid);
		
		updateTableTypeRadioButtons();
		updateGrid();
	}
	

	private int getTableType(){
		int result  = DBTableProgramObject.TYPE_DEFAULT;
		if(rdbtnSingleDate.isSelected()){
			result = DBTableProgramObject.TYPE_MEAS;
		} else if(rdbtnDateRange.isSelected()){
			result = DBTableProgramObject.TYPE_TEMPORAL;
		}
		return result;
	}
	
	private void initGrid(){
		columnGrid = new MyJTable(this,
							new String[]  {"Column Name",
											"Data Type",
											"Is Key ?",
											"Column Assingment"},
							new int[]{150, 150, 50, 150}
				);
	}

	private final Set<String> startDateOptions = new HashSet<String>(Arrays.asList("START_DATE","STARTDATE"));
	private final Set<String> endDateOptions   = new HashSet<String>(Arrays.asList("END_DATE","ENDDATE"));
	private final Set<String> measDateOption   = new HashSet<String>(Arrays.asList("V_DATE","VDATE"));
	
	protected void updateTableTypeRadioButtons() {
		messageLabel.setText("");
		String tableNameSelected = (String) selectingTable.getSelectedItem();
		DBObjectsModelRowSource data = DBObjectCache.cache.getCache(tableNameSelected);
		int     dateCount   = 0;
		boolean hasVDate    = false;
		boolean hasDateInPk = false;
		boolean hasSDate    = false;
		boolean hasEDate    = false;
		
		List<DBObjectsModelColumn> columns = data == null ? programObject.columns : data.columns;
		
		if (columns != null)
			for(DBObjectsModelColumn column : columns)
				if (column.dataType.equals("DATE")){
					boolean isPK = column.specialType==DBObjectsModelColumn.TYPE_NORMAL_PK;
					hasDateInPk |= isPK;
					dateCount++;
					hasVDate |= measDateOption.contains(column.name.toUpperCase());
					hasSDate |= startDateOptions.contains(column.name.toUpperCase());
					hasEDate |= endDateOptions.contains(column.name.toUpperCase());
				}

		
		if (dateCount == 0){
			rdbtnNoDate.setEnabled(true);
			rdbtnSingleDate.setEnabled(false);
			rdbtnDateRange.setEnabled(false);
			rdbtnNoDate.getModel().setSelected(true);
			if (programObject.tableName == tableNameSelected && programObject.type!=DBTableProgramObject.TYPE_DEFAULT){
				messageLabel.setText("Settings changed because of database sync.");
			}
		} else if (dateCount == 1){
			rdbtnNoDate.setEnabled(true);
			rdbtnSingleDate.setEnabled(true);
			rdbtnDateRange.setEnabled(false);
			
			if (programObject.tableName != tableNameSelected || programObject.type==DBTableProgramObject.TYPE_TEMPORAL){
				if (hasDateInPk || hasVDate){
					rdbtnSingleDate.getModel().setSelected(true);
				} else {
					rdbtnNoDate.getModel().setSelected(true);
				}
			}
			
			if (programObject.tableName == tableNameSelected){
				if(programObject.type==DBTableProgramObject.TYPE_TEMPORAL){
					messageLabel.setText("Settings changed because of database sync.");
				} else if (programObject.type==DBTableProgramObject.TYPE_MEAS){
					rdbtnSingleDate.getModel().setSelected(true);
				} else{
					rdbtnNoDate.getModel().setSelected(true);
				} 
			}

		
		} else {
			rdbtnNoDate.setEnabled(true);
			rdbtnSingleDate.setEnabled(true);
			rdbtnDateRange.setEnabled(true);
			if (tableNameSelected != null && !programObject.tableName.equals(tableNameSelected)){
				if (hasVDate){
					rdbtnSingleDate.getModel().setSelected(true);
				} else if (hasSDate || hasEDate){
					rdbtnDateRange.getModel().setSelected(true);	
				} else {
					rdbtnNoDate.getModel().setSelected(true);
				}
			} else {
				if(programObject.type==DBTableProgramObject.TYPE_TEMPORAL){
					rdbtnDateRange.getModel().setSelected(true);	
				} else if (programObject.type==DBTableProgramObject.TYPE_MEAS){
					rdbtnSingleDate.getModel().setSelected(true);
				} else{
					rdbtnNoDate.getModel().setSelected(true);
				} 
				
			}
		}
	}
	private void updateGrid() {
		String tableNameSelected = (String) selectingTable.getSelectedItem();
		int tableType = getTableType();
		programObject.type = tableType;
		
		ItemListener tempListener = new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						syncFormToObject();
					}
				});
			}
		 };
		JComboBox<String> tempCombo;
		tempCombo = new JComboBox<String>(new String[]{"DATE", "KEY"});
		tempCombo.addItemListener(tempListener);
		DefaultCellEditor selectorDateKey    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"DATE", "VALUE"});
		tempCombo.addItemListener(tempListener);
		DefaultCellEditor selectorDateValue    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"START_DATE", "END_DATE", "KEY"});
		tempCombo.addItemListener(tempListener);
		DefaultCellEditor selectorRDateKey    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"START_DATE", "END_DATE", "VALUE"});
		tempCombo.addItemListener(tempListener);
		DefaultCellEditor selectorRDateValue    = new DefaultCellEditor(tempCombo);

		List<Object[]>          allRows = new ArrayList<Object[]>();
		List<DefaultCellEditor> editors = new ArrayList<DefaultCellEditor>();
		

		DBObjectsModelRowSource data = DBObjectCache.cache.getCache(tableNameSelected);
		boolean load           = false;
		boolean loadFromObject = false;
			
		if (tableNameSelected == null){
			messageLabel.setText("No table selected");
		} else if (programObject.tableName.equals(tableNameSelected) && programObject.type==tableType){
			load = true;
			if (data != null && !data.isComparableColumnSet(programObject.columns)){
				messageLabel.setText("Settings changed because of database sync.");
			} else {
				loadFromObject = true;
			}
		} else if(programObject.tableName.equals(tableNameSelected) && data == null) {
			load = true;
		} else if(data == null) {
			messageLabel.setText("Could not read column data.");
		} else if(data.columns.size() == 0) {
			messageLabel.setText("No columns found.");
		} else {
			load = true;
		}
		
		if (load){
			Map <String, String> objectLookup = new HashMap<String, String>();
			
			if (loadFromObject){
				for (DBObjectsModelColumn column : programObject.columns){
					objectLookup.put(column.name, column.specialTypeAsString());
				}
			}
			
			List<DBObjectsModelColumn> columns = data == null ? programObject.columns : data.columns;
				
			// load grid from scratch and overrule from cache 
			for(DBObjectsModelColumn column : columns){
				String            assignValue = "?????";
				DefaultCellEditor cellEditor  = null;
				boolean isPK =  column.specialType==DBObjectsModelColumn.TYPE_NORMAL_PK ||
								column.specialType==DBObjectsModelColumn.TYPE_DATE ||
								column.specialType==DBObjectsModelColumn.TYPE_START_DATE;
				boolean hasFDATEAssigned = false;
				boolean hasSDATEAssigned = false;
				if (column.dataType.equals("DATE")){
					if (tableType == DBTableProgramObject.TYPE_MEAS){
						if (isPK){
							cellEditor  = selectorDateKey;
							assignValue = "KEY";
						} else {
							cellEditor  = selectorDateValue;
							assignValue = "VALUE";
						}
						
						if (!hasFDATEAssigned && isPK){
							assignValue = "DATE";
							hasFDATEAssigned = true;
						}
						if (loadFromObject) assignValue = objectLookup.get(column.name);
						
					} else if  (tableType == DBTableProgramObject.TYPE_TEMPORAL){
						if (isPK){
							cellEditor  = selectorRDateKey;
							assignValue = "KEY";
						} else {
							cellEditor  = selectorRDateValue;
							assignValue = "VALUE";
						}
						
						if (!hasFDATEAssigned && startDateOptions.contains(column.name.toUpperCase())){
							assignValue = "START_DATE";
							hasFDATEAssigned = true;
						}
						if (!hasSDATEAssigned && endDateOptions.contains(column.name.toUpperCase())){
							assignValue = "END_DATE";
							hasSDATEAssigned = true;
						}					

						if (loadFromObject) assignValue = objectLookup.get(column.name);
					} else {// (tableType == DBTableProgramObject.TYPE_DEFAULT)
						if(isPK){
							assignValue = "KEY";
						} else {
							assignValue = "VALUE";
						}
					}
				} else {
					if(isPK){
						assignValue = "KEY";
					} else {
						assignValue = "VALUE";
					}
				}				
				
				Object [] rowData = new Object[]{column.name, column.dataType, new Boolean(isPK), assignValue};
				allRows.add(rowData);
				editors.add(cellEditor);
			}
		}
		
		columnGrid.setData(allRows.toArray(new Object[][]{}));
		for (int i=0; i<editors.size(); i++){
			columnGrid.setEditor(i,3, editors.get(i) != null, editors.get(i));
		}
		
		columnGrid.getColumnModel().getColumn(2).setCellRenderer(columnGrid.getDefaultRenderer(Boolean.class));	
	}


	public void reloadFromObjectDefinitionSub() {
		propagateChange = false;

		messageLabel.setText("");
		selectingTable.setSelectedItem(programObject.tableName);
		switch(programObject.type){
		case DBTableProgramObject.TYPE_DEFAULT:
			rdbtnNoDate.setSelected(true);
			break;
		case DBTableProgramObject.TYPE_MEAS:
			rdbtnSingleDate.setSelected(true);
			break;
		case DBTableProgramObject.TYPE_TEMPORAL:
			rdbtnDateRange.setSelected(true);
			break;
		}
		updateGrid();

		propagateChange = true;
	}
	
	private void syncFormToObject(){
		fillProgramObjectFromFormSub();
	}

	public boolean fillProgramObjectFromFormSub() {
		messageLabel.setText("");
		boolean hasChanges = programObject.tableName == null ||
							!programObject.tableName.equals((String) selectingTable.getSelectedItem()) ||
						     programObject.type != getTableType();

		programObject.tableName = (String) selectingTable.getSelectedItem();
		programObject.type = getTableType();
		
		TableModel dataModel = columnGrid.getModel();
		List<DBObjectsModelColumn> oldCols = programObject.columns; 
		hasChanges = hasChanges || (oldCols.size() != dataModel.getRowCount());
		programObject.columns = new ArrayList<DBObjectsModelColumn>();
		for(int row=0; row<dataModel.getRowCount(); row++){
			DBObjectsModelColumn newCol = new DBObjectsModelColumn((String) dataModel.getValueAt(row,0),
																	(String) dataModel.getValueAt(row,1),
																	(String) dataModel.getValueAt(row,3));
 
			programObject.columns.add(newCol);
			hasChanges |=  row>=oldCols.size() || !newCol.equals(oldCols.get(row));
		}

		return hasChanges;
	}
}
