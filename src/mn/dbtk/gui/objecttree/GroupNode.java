package mn.dbtk.gui.objecttree;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

public class GroupNode extends DefaultMutableTreeNode {
	private SortedSet<String> groupChildren;
	private SortedSet<String> poChildren;	
	String name;
	TreeObjectPanel tree;
	
	public GroupNode(TreeObjectPanel tree, String string) {
		super();
		this.tree = tree;
		name      = string;
		
		String cleanedFromLastSlash = "/"+(string.endsWith("/") ? string.substring(0,string.length()-1) : string);
		userObject = cleanedFromLastSlash.substring(cleanedFromLastSlash.lastIndexOf("/")+1);
		
		groupChildren = new TreeSet<String>();
		poChildren    = new TreeSet<String>();
	}
	
	public void removeAllChildren(){
		super.removeAllChildren();
		groupChildren.clear();
		poChildren.clear();
	}
	
    public void insert(GroupNode newChild){
    	String newChildName = newChild.name;
    	if(!groupChildren.contains(newChildName)){
    		insert (newChild, groupChildren.headSet(newChildName).size());
    	}
    	groupChildren.add(newChildName);
    }
    public void insert(ProgramObjectNode newChild){
    	String newChildName = newChild.po.name;
    	insert (newChild, groupChildren.size() + poChildren.headSet(newChildName).size());
    	poChildren.add(newChildName);
    }

}
