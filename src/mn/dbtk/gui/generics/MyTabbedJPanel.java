package mn.dbtk.gui.generics;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MyTabbedJPanel extends JPanel {
	private JTabbedPane container;
	
	public MyTabbedJPanel(JTabbedPane container){
		this.container = container;
	}
	protected void setTab(String title, Icon icon){
		int tabIndex = getTabIndex();
		if (tabIndex >= 0){ 
			if (title != null)
				container.setTitleAt(tabIndex, title);
			if (icon != null)
				container.setIconAt(tabIndex, icon);
		}
	}
	private int getTabIndex(){
		for (int i = 0; i<container.getTabCount(); i++){
			if (this == container.getComponentAt(i))
				return i;
		}
		return -1;
	}
	public void removeFromContainer(){
		container.remove(this);
	}
}
