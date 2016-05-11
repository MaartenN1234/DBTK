package mn.dbtk.gui.objecttree;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeObjectRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
		    Object value, boolean selected, boolean expanded,
		    boolean leaf, int row, boolean hasFocus) {
				JLabel c = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		        if (value instanceof ProgramObjectNode){
		        	c.setIcon(((ProgramObjectNode) value).po.getIcon());
		        }
		        
		        return c;
		}
}
