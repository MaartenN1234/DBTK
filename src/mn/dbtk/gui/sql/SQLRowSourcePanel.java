package mn.dbtk.gui.sql;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import mn.dbtk.sql.ParsedSQLStatement;
import mn.dbtk.sql.ParsedSQLStatement.TableEntry;

public class SQLRowSourcePanel extends JPanel implements ComponentListener{
	List<SQLSingleRowSourcePanel> lastExpandedPanels;
	JLabel spacer;
	GridBagLayout layout;
	SQLPanel container;
	String additionalExpandedPanel;
	
	final static GridBagConstraints gbcSpacerExpanded =  new GridBagConstraints(
													0, GridBagConstraints.RELATIVE, 1, 1, 1, 100, 
													GridBagConstraints.CENTER, GridBagConstraints.BOTH,
													SQLPanel.NO_INSETS, 0, 0);
	final static GridBagConstraints gbcExpanded =  new GridBagConstraints(
													0, GridBagConstraints.RELATIVE, 1, 1, 1, 0.01, 
													GridBagConstraints.CENTER, GridBagConstraints.BOTH,
													SQLPanel.NO_INSETS, 0, 0);
	final static GridBagConstraints gbcCollapsed =  new GridBagConstraints(
													0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, 
													GridBagConstraints.CENTER, GridBagConstraints.BOTH,
													SQLPanel.NO_INSETS, 0, 0);
	
	
	public SQLRowSourcePanel (SQLPanel container){
		this.container = container;
		
		layout = new GridBagLayout();
		spacer = new JLabel("");
		this.setLayout(layout);
		this.add(spacer, gbcSpacerExpanded);
		this.addComponentListener(this);
	}
	
	private void setSpacer( boolean addSpacer){
		layout.setConstraints(spacer, addSpacer ? gbcSpacerExpanded : gbcCollapsed);
	}
	

	public void syncTo(ParsedSQLStatement pss){
		//maybe improve on this
		this.removeAll();
		
		// add components
		Map<String, SQLSingleRowSourcePanel> addedPanels = new HashMap<String, SQLSingleRowSourcePanel>();

		SQLSingleRowSourcePanel addingPanel = new SQLSingleRowSourcePanel(this, false, pss.selectList);
		addedPanels.put(addingPanel.name, addingPanel);
		this.add(addingPanel, gbcCollapsed);
		for (TableEntry te : pss.tableList){
			addingPanel = new SQLSingleRowSourcePanel(this, false, te);
			addedPanels.put(addingPanel.name, addingPanel);
			this.add(addingPanel, gbcCollapsed);
		}
		this.add(spacer, gbcSpacerExpanded);
		
		// fix expansion
		List<String> expandedNames = getExpandedNames();
		if(additionalExpandedPanel!= null){
			expandedNames.add(additionalExpandedPanel);
			additionalExpandedPanel = null;
		}
		lastExpandedPanels = new LinkedList<SQLSingleRowSourcePanel>();
		for(String name : expandedNames){
			SQLSingleRowSourcePanel expandingPanel = addedPanels.get(name);
			if (expandingPanel != null)
				expandingPanel.expand();
		}
		
	}

	private List<String> getExpandedNames() {
		List<String> result = new ArrayList<String>();
		if (lastExpandedPanels== null){
			result.add(SQLSingleRowSourcePanel.QUERY_RESULT_NAME);
		} else
			for(SQLSingleRowSourcePanel expandedPanel : lastExpandedPanels)
				result.add(expandedPanel.name);
		
		return result;
	}

	public void setExpanded(SQLSingleRowSourcePanel expandedPanel) {
		lastExpandedPanels.remove(expandedPanel);
		lastExpandedPanels.add(expandedPanel);		
		layout.setConstraints(expandedPanel, gbcExpanded);
		collapseWhenRequired();
	}
	public void addAsExpandedPanel(String addedTable) {
		additionalExpandedPanel = addedTable;
		
	}
	public void setCollapsed(SQLSingleRowSourcePanel expandedPanel) {
		lastExpandedPanels.remove(expandedPanel);
		layout.setConstraints(expandedPanel, gbcCollapsed);		
		collapseWhenRequired();
	}	
	
	private void collapseWhenRequired(){
		if (lastExpandedPanels == null || lastExpandedPanels.size() == 0){
			validate();
			return;
		}
		
		setSpacer(true);
		
		JScrollBar sb = lastExpandedPanels.get(lastExpandedPanels.size()-1).scrollPane.getVerticalScrollBar();
		boolean continueWithScrollBar = true;
		
		validate();
		while (sb.isVisible() && continueWithScrollBar){
			if (lastExpandedPanels.size() <= 1){
				setSpacer(false);
				continueWithScrollBar = false;
			} else {
				SQLSingleRowSourcePanel toCollapse = lastExpandedPanels.remove(0);
				toCollapse.collapse();
			}
			validate();
		}

		invalidate();
		Thread t = new Thread(){
			public void run(){
				invalidate();
			}
		};
		t.start();
	}

	public void componentResized(ComponentEvent e) {
		collapseWhenRequired();		
	}

	public void componentMoved(ComponentEvent e) {
		collapseWhenRequired();
	}

	public void componentShown(ComponentEvent e) {
		collapseWhenRequired();
	}

	public void componentHidden(ComponentEvent e) {
	}


}
