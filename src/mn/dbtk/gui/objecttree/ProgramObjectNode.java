package mn.dbtk.gui.objecttree;

import javax.swing.tree.DefaultMutableTreeNode;

import mn.dbtk.programobjects.AbstractProgramObject;

public class ProgramObjectNode extends DefaultMutableTreeNode {
	AbstractProgramObject po;
	TreeObjectPanel       tree;
	
	public ProgramObjectNode(TreeObjectPanel tree, AbstractProgramObject po){
		super(po.name, false);
		this.tree = tree;
		this.po   = po;
	}

	public void syncToObjectDefinition() {
		userObject = po.name;
	}
}
