package mn.dbtk.gui.programobjects;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import mn.dbtk.gui.generics.IconProvider;
import mn.dbtk.gui.generics.MyJTable;
import mn.dbtk.programobjects.DBTableProgramObject;
import mn.dbtk.sql.dbcache.DBObjectCache;
import mn.dbtk.sql.dbcache.DBObjectsModelColumn;
import mn.dbtk.sql.dbcache.DBObjectsModelRowSource;

import javax.swing.JCheckBox;

public class DBTableInternalSubPanel extends JPanel {
	private JRadioButton rdbtnNoDate;
	private JRadioButton rdbtnSingleDate;
	private JRadioButton rdbtnDateRange;
	private JCheckBox    chckbxFullAufitTrail;
	private MyJTable     columnGrid;
	
	private boolean propagateChange = true;

	final private DBTableProgramObject programObject;

	public DBTableInternalSubPanel(DBTablePanel masterPanel) {
		programObject = masterPanel.getProgramObjectBypassSync();
		init();
	}

	private void init(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		
		JLabel lblTableType = new JLabel("Table type");
		GridBagConstraints gbc_lblTableType = new GridBagConstraints();
		gbc_lblTableType.insets = new Insets(0, 2, 5, 5);
		gbc_lblTableType.anchor = GridBagConstraints.WEST;
		gbc_lblTableType.gridx = 0;
		gbc_lblTableType.gridy = 0;
		add(lblTableType, gbc_lblTableType);
		
		rdbtnNoDate = new JRadioButton("No date");
		rdbtnNoDate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateGrid(false);
			}
		});
		GridBagConstraints gbc_rdbtnNoDate = new GridBagConstraints();
		gbc_rdbtnNoDate.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNoDate.gridx = 1;
		gbc_rdbtnNoDate.gridy = 0;
		add(rdbtnNoDate, gbc_rdbtnNoDate);
		
		rdbtnSingleDate = new JRadioButton("Single date");
		rdbtnSingleDate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateGrid(false);
			}
		});
		GridBagConstraints gbc_rdbtnSingleDate = new GridBagConstraints();
		gbc_rdbtnSingleDate.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnSingleDate.gridx = 2;
		gbc_rdbtnSingleDate.gridy = 0;
		add(rdbtnSingleDate, gbc_rdbtnSingleDate);
		
		rdbtnDateRange = new JRadioButton("Date Range");
		rdbtnDateRange.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				updateGrid(false);
			}
		});
		GridBagConstraints gbc_rdbtnDateRange = new GridBagConstraints();
		gbc_rdbtnDateRange.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnDateRange.gridx = 3;
		gbc_rdbtnDateRange.gridy = 0;
		add(rdbtnDateRange, gbc_rdbtnDateRange);
		
		 ButtonGroup group = new ButtonGroup();
		 group.add(rdbtnNoDate);
		 group.add(rdbtnSingleDate);
		 group.add(rdbtnDateRange);

		chckbxFullAufitTrail = new JCheckBox("Full audit trail");
		GridBagConstraints gbc_chckbxFullAufitTrail = new GridBagConstraints();
		gbc_chckbxFullAufitTrail.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxFullAufitTrail.gridx = 4;
		gbc_chckbxFullAufitTrail.gridy = 0;
		add(chckbxFullAufitTrail, gbc_chckbxFullAufitTrail);
		
		initGrid();
		GridBagConstraints gbc_grid = new GridBagConstraints();
		gbc_grid.insets = new Insets(0, 2, 5, 5);
		gbc_grid.anchor = GridBagConstraints.NORTHWEST;
		gbc_grid.gridwidth = 6;
		gbc_grid.gridx = 0;
		gbc_grid.gridy = 1;
		add(columnGrid.getScrollPane(), gbc_grid);
		
		reloadFromObjectDefinitionSub();
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
							new String[]  {"",
											"Column Name",
											"Data Type",
											"Column Assingment"},
							new int[]{20, 150, 150, 150}
				);
		
	
		columnGrid.addMouseListener(new MouseAdapter() {
	      public void mousePressed(MouseEvent e) {
	    	  if ( columnGrid.getSelectedRows().length>0 &&  columnGrid.getSelectedColumns().length>0){
	  	        int selectedRow    = columnGrid.getSelectedRow();
		        int selectedColumn = columnGrid.getSelectedColumn();

		        if (selectedColumn == 0 && selectedRow != -1){
		        	if (selectedRow == columnGrid.getRowCount()-1){
		        		addColumn();        		
		        	} else {
		        		deleteColumn(selectedRow) ;
		        	}
		        }
	    	  }

	      }
	    });
	}

	private void updateGrid(boolean wasGrown) {
		int tableType = getTableType();
		programObject.type = tableType;
	    int selectedRow    = columnGrid.getSelectedRow();
        int selectedColumn = columnGrid.getSelectedColumn();
        if (wasGrown && selectedColumn==2 && selectedRow==0){
        	selectedColumn = 1;
        	selectedRow    = columnGrid.getRowCount();
        }
		
		ItemListener tempItemListener = new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						syncObjectFromForm();
					}
				});
			}
		 };

		 CellEditorListener tempCellListener = new CellEditorListener(){
			public void editingStopped(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						syncObjectFromForm();
					}
				});
			}
			public void editingCanceled(ChangeEvent e) {
			}
		 };
		 
		JComboBox<String> tempCombo;
		tempCombo = new JComboBox<String>(new String[]{"KEY", "VALUE"});
		tempCombo.addItemListener(tempItemListener);
		DefaultCellEditor selectorKeyValue    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"DATE", "KEY", "VALUE"});
		tempCombo.addItemListener(tempItemListener);
		DefaultCellEditor selectorDateKeyValue    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"START_DATE", "END_DATE", "KEY", "VALUE"});
		tempCombo.addItemListener(tempItemListener);
		DefaultCellEditor selectorRDateKeyValue    = new DefaultCellEditor(tempCombo);
		tempCombo = new JComboBox<String>(new String[]{"FLOAT", "INTEGER", "DATE", "VARCHAR2"});
		tempCombo.addItemListener(tempItemListener);
		DefaultCellEditor selectorType    = new DefaultCellEditor(tempCombo);
		
		
		TableCellEditor str = columnGrid.getDefaultEditor(String.class);
		str.addCellEditorListener(tempCellListener);
		
		
		List<Object[]>          allRows = new ArrayList<Object[]>();
		List<DefaultCellEditor> editors = new ArrayList<DefaultCellEditor>();
		
		if( programObject != null && programObject.columns != null)
		for (DBObjectsModelColumn column : programObject.columns){
			DefaultCellEditor cellEditorAssign = selectorKeyValue;
			String dataType                    = column.dataType.toUpperCase();
			String specialType                 = column.specialTypeAsString();
			if (dataType.equals("CHAR") || dataType.equals("VARCHAR"))
				dataType ="VARCHAR2";
			
			if (column.dataType.equals("DATE")){
				if (tableType == DBTableProgramObject.TYPE_MEAS){
					cellEditorAssign  = selectorDateKeyValue;
					if (specialType.equals("START_DATE") || specialType.equals("END_DATE")){
						specialType = "DATE";
					}
				} else if  (tableType == DBTableProgramObject.TYPE_TEMPORAL){
					cellEditorAssign  = selectorRDateKeyValue;
					if (specialType.equals("DATE") ){
						specialType = "START_DATE";
					}
				} else {
					if (specialType.equals("START_DATE") || specialType.equals("END_DATE") ||specialType.equals("DATE")){
						specialType = "VALUE";
					}
				}
			} else {
				if (specialType.equals("START_DATE") || specialType.equals("END_DATE") ||specialType.equals("DATE")){
					specialType = "VALUE";
				}
			}
			
			JButton deleteButton = IconProvider.getButton("delete", "Delete column");
			Object [] rowData = new Object[]{deleteButton, column.name, dataType, specialType};
			allRows.add(rowData);
			editors.add(cellEditorAssign);
		}

		JButton addButton = IconProvider.getButton("add", "Add column");
		Object [] rowData = new Object[]{addButton, "", "FLOAT", "VALUE"};
		allRows.add(rowData);
		editors.add(selectorKeyValue);
		
		columnGrid.setData(allRows.toArray(new Object[][]{}));
		for (int i=0; i<editors.size(); i++){
			columnGrid.setEditor(i,1, true, str);
			columnGrid.setEditor(i,2, true, selectorType);
			columnGrid.setEditor(i,3, editors.get(i) != null, editors.get(i));
		}
		
		columnGrid.getColumnModel().getColumn(0).setCellRenderer(new TableCellRenderer(){
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				return (JButton) value;
			}			
		});	
		
		if (selectedRow > -1 && selectedColumn > -1 && selectedRow < columnGrid.getRowCount()){
			columnGrid.setColumnSelectionInterval(selectedColumn, selectedColumn);
			columnGrid.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}


	protected void addColumn() {
		fillProgramObjectFromFormSub();
	}

	protected void deleteColumn(int rownr) {
		TableModel dataModel = columnGrid.getModel();
		dataModel.setValueAt("", rownr,1);
		fillProgramObjectFromFormSub();
	}

	private void syncObjectFromForm(){
		fillProgramObjectFromFormSub();
	}

	public void reloadFromObjectDefinitionSub() {
		propagateChange = false;
		chckbxFullAufitTrail.setSelected(programObject.isFullAuditType);
		
		switch(programObject.type){
			case DBTableProgramObject.TYPE_MEAS:
				rdbtnSingleDate.setSelected(true);
				break;
			case DBTableProgramObject.TYPE_TEMPORAL:
				rdbtnDateRange.setSelected(true);
				break;
			case DBTableProgramObject.TYPE_DEFAULT:
				rdbtnNoDate.setSelected(true);
				break;
		}
		
		updateGrid(false);
		// TODO indices
		
		propagateChange = true;
	}

	private static String safeName(String input){
		return input.toUpperCase().replace(" ","_").substring(0,29< input.length() ? 29 :  input.length());
	}
	public boolean fillProgramObjectFromFormSub() {
		if (!propagateChange)
			return false;
		
		String tableNameNew = safeName(programObject.name);
		boolean hasChanges = programObject.type != getTableType() ||
							 programObject.isFullAuditType != chckbxFullAufitTrail.isSelected() ||
							 !programObject.tableName.equals(tableNameNew);
		
		programObject.type            = getTableType();
		programObject.isFullAuditType = chckbxFullAufitTrail.isSelected();
		programObject.tableName       = tableNameNew;
		
		TableModel dataModel = columnGrid.getModel();
		List<DBObjectsModelColumn> oldCols = programObject.columns; 
		programObject.columns = new ArrayList<DBObjectsModelColumn>();
		int j = 0;
		for(int row=0; row<dataModel.getRowCount(); row++){
			DBObjectsModelColumn newCol = new DBObjectsModelColumn(safeName((String) dataModel.getValueAt(row,1)),
																	(String) dataModel.getValueAt(row,2),
																	(String) dataModel.getValueAt(row,3));
 
			if (newCol.name != null && newCol.name.length()>0){
				programObject.columns.add(newCol);
				hasChanges |=  oldCols.size()<= j || !newCol.equals(oldCols.get(j++));
			}
			
			
		}
		
		hasChanges = hasChanges || (oldCols.size() != programObject.columns.size() );
		
		if (hasChanges)
			updateGrid(oldCols.size() < programObject.columns.size() );
		
		return hasChanges;
		
	}

}
