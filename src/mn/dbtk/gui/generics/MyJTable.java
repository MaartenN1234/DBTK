package mn.dbtk.gui.generics;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class MyJTable extends JTable {
	private JScrollPane scroller;
	private JPanel      parentPanel;
	
	private TableCellEditor[][] editors;
	private boolean        [][] isEditable;
	
	public MyJTable(JPanel parentPanel, String[] columnNames, int[] columnWidths){
		super();
		this.parentPanel = parentPanel;
		setRowHeight(getRowHeight()+2);

		DefaultTableModel tableModel = new DefaultTableModel(){
			public boolean isCellEditable(int row, int column){
				return isEditable[row][column];		
			}
		};
		tableModel.setColumnIdentifiers(columnNames);
		setModel(tableModel);
		for (int i=0; i<columnWidths.length; i++){
			TableColumn columnName;
			columnName = getColumnModel().getColumn(i);
			columnName.setWidth(columnWidths[i]);
			columnName.setPreferredWidth(columnWidths[i]);
		}
		scroller = new JScrollPane(this);
		setGridSizing();
	}
	
	public void setData(Object[][] data){
		DefaultTableModel dm = (DefaultTableModel) getModel();
		for (int i = dm.getRowCount() - 1; i >= 0; i--) {
		    dm.removeRow(i);
		}
		
		if (data == null || data.length ==0){
			isEditable = new boolean[0][0];
			editors    = new TableCellEditor[0][0];
		} else {
			isEditable = new boolean[data.length][data[0].length];
			editors    = new TableCellEditor[data.length][data[0].length];
			
			for(Object [] rowData : data){
				dm.addRow(rowData);
			}
		}
		setGridSizing();
	}
	
	public TableCellEditor getCellEditor(int row, int column){
		if (editors[row][column]==null)
			return super.getCellEditor(row,column);
		return editors[row][column];
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	    {
	        Component c = super.prepareRenderer(renderer, row, column);

	        if (c instanceof JButton)
				return c;
				
	        
	        if (isEditable[row][column]){
	        	c.setEnabled(true);
	        	c.setBackground(Color.white);
            } else {
            	c.setEnabled(false);
            	c.setBackground(Color.lightGray);
            }


	        return c;
	    }

	
	public void setEditor(int row, int column, boolean isEditable, TableCellEditor editor){
		 this.isEditable[row][column] = isEditable;
		 editors[row][column]         = isEditable ? editor : null;
	}
	
	
	public JScrollPane getScrollPane(){
		return scroller;
	}
	
	private void setGridSizing() {
		setPreferredScrollableViewportSize(getPreferredSize());
		parentPanel.validate();
	}

}
