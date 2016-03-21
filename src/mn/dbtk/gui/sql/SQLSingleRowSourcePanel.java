package mn.dbtk.gui.sql;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import mn.dbtk.gui.generics.MyJListBase;
import mn.dbtk.sql.DBObjectCache;
import mn.dbtk.sql.DBObjectsModelColumn;
import mn.dbtk.sql.DBObjectsModelRowSource;
import mn.dbtk.sql.ParseSQLHelper;
import mn.dbtk.sql.ParsedSQLStatement.SelectEntry;
import mn.dbtk.sql.ParsedSQLStatement.TableEntry;

public class SQLSingleRowSourcePanel extends JPanel{
	final static String QUERY_RESULT_NAME =  "QUERY SELECT LIST";
	
	
	private boolean isOutputTable;
	private boolean outerjoined;
	String name;
	private List<String> columnNames;
	private boolean expanded;
	private SQLRowSourcePanel sourcePanel;
	private JToggleButton expandButton;
	private MyJListBase   columnList;
	private JTable        columnTable;

	
	public JPanel      head;
	public JScrollPane scrollPane;

	
	
	public SQLSingleRowSourcePanel (SQLRowSourcePanel sourcePanel, boolean expanded, TableEntry te){
		isOutputTable = false;
		this.sourcePanel = sourcePanel;
		this.expanded = expanded;
		this.name = te.alias;
		this.outerjoined = te.outerjoined;
		DBObjectsModelRowSource source = DBObjectCache.cache.getCache(te.expression.trim());
		this.columnNames = new ArrayList<String>();
		if(source == null){
			columnNames.add("*");
		} else {
			for (DBObjectsModelColumn column : source.columns)
				columnNames.add(column.name);
		}
		initGui();
	}
	public SQLSingleRowSourcePanel (SQLRowSourcePanel sourcePanel, boolean expanded, List<SelectEntry> sources){
		isOutputTable = true;
		this.sourcePanel = sourcePanel;
		this.expanded = expanded;
		this.name = QUERY_RESULT_NAME;
		this.columnNames = new ArrayList<String>();
		for (SelectEntry se : sources){
			columnNames.add(se.alias);
		}
		initGui();
	}

	
	private void initGui(){
		EventHandler eventHandler = new EventHandler();
		this.setLayout(new BorderLayout());
		
		head = new JPanel();
		head.setLayout(new BorderLayout());
		
		if(isOutputTable)
			head.add(new JLabel(name), BorderLayout.WEST);
		else
			head.add(getHeadGuiInputTable(), BorderLayout.CENTER);
		
		
		expandButton = new JToggleButton("expand", expanded);
		head.add(expandButton, BorderLayout.EAST);
		this.add(head, BorderLayout.NORTH);
		
		columnList = new MyJListBase(name, columnNames.toArray(new String[]{}), eventHandler);
		columnList.setVisibleRowCount(columnNames.size());
		columnList.addMouseListener(eventHandler);
		Object[][] tableData = new Object[columnNames.size()][1];
		int i=0;
		for (String s : columnNames){
			s = ParseSQLHelper.stripQoutes(s);
			tableData[i++][0] = s;
		}

		columnTable = new JTable(tableData, new Object[]{""});
		columnTable.getModel().addTableModelListener(eventHandler);
		columnTable.addMouseListener(eventHandler);
		columnTable.setPreferredScrollableViewportSize(columnTable.getPreferredSize());
		
		expandButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e){
		    	if(expandButton.isSelected()){
		    		expand();
		    	} else {
		    		collapse();
		    	}
		    }	
		});
		
		if (isOutputTable)
			scrollPane = new JScrollPane(columnTable);
		else
			scrollPane = new JScrollPane(columnList);
		
		if (expanded){
			add(scrollPane, BorderLayout.CENTER);
			sourcePanel.setExpanded(this);
		}
						
	}
	private JComponent getHeadGuiInputTable() {
		JPanel result     = new JPanel();
		result.setLayout(new BorderLayout());
		JLabel nameLabel =  new JLabel(ParseSQLHelper.stripQoutes(name));
		nameLabel.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON3){
					sourcePanel.container.removeFromTableList(name);
				}
			}
		});
		
		final JCheckBox ojBox = new JCheckBox("(+)", outerjoined);
		ojBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				sourcePanel.container.setOuterjoinStatus(name, ojBox.isSelected());
			}
		});
		result.add(nameLabel, BorderLayout.WEST);
		result.add(ojBox, BorderLayout.EAST);
		return result;
	}
	public void expand() {
		add(scrollPane, BorderLayout.CENTER);
		expandButton.getModel().setSelected(true);
		sourcePanel.setExpanded(this);
	}
	
	public void collapse() {
		remove(scrollPane);
		expandButton.getModel().setSelected(false);
		sourcePanel.setCollapsed(this);
	}

	
	private class EventHandler extends MouseAdapter implements TableModelListener, MyJListBase.DropListener{
		public void mouseClicked(MouseEvent e) {
			Point point = e.getPoint();
			if (!isOutputTable){
				if(e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1){
					String columnNameToAdd = columnList.getItemAtPoint(point);
					if(columnNameToAdd != null){
						sourcePanel.container.addToSelectList(name, columnNameToAdd);
					}
				}
			} else {
				if(e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON3){
					int index = columnTable.rowAtPoint(point);
					if(index >= 0 && index < columnList.getModel().getSize()){
						String columnNameRemove = columnList.getModel().getElementAt(index);
						sourcePanel.container.removeFromSelectList(columnNameRemove);
					}
				}
				
			}
			
		}
		public void tableChanged(TableModelEvent e) {
			if (e.getFirstRow() == e.getLastRow()){
				int index = e.getFirstRow();
				String newValue =  ParseSQLHelper.wrapQoutesWhenRequired((String) (columnTable.getModel().getValueAt(index, 0)));
	
				String oldValue = columnList.getModel().getElementAt(index);
				if (newValue.length() == 0)
					sourcePanel.container.removeFromSelectList(oldValue);
				else if (!oldValue.equals(newValue))
					sourcePanel.container.changeAliasInSelectList(oldValue, newValue);
			}
		}
		public void dndComplete(String sourceName, String sourceItem, String targetName, String targetItem) {
			int i = sourceName.compareTo(targetName);
			if (i==0){
				i = sourceItem.compareTo(targetItem);
			}
			
			if (i<0)
				sourcePanel.container.addWhereClause(targetName+"."+targetItem + " = "+ sourceName+"."+ sourceItem);			
			else if (i>0)
				sourcePanel.container.addWhereClause(sourceName+"."+sourceItem + " = "+ targetName+"."+ targetItem);			
		}
	}
}

