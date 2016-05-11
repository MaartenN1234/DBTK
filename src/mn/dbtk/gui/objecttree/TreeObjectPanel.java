package mn.dbtk.gui.objecttree;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mn.dbtk.gui.main.MainWindowActionHandler;
import mn.dbtk.programobjects.AbstractProgramObject;
import mn.dbtk.programobjects.ProgramObjectStore;


public class TreeObjectPanel extends JTree {
	public static TreeObjectPanel singleton = new TreeObjectPanel();
	
	private DefaultTreeModel model;
	private TreeObjectActionHandler actionHandler;
	
	private TreeObjectPanel() {
		super(new DefaultTreeModel(new GroupNode(null, "/")));
		
		model = (DefaultTreeModel) getModel();
		

		getSelectionModel().setSelectionMode
		        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		setCellRenderer(new TreeObjectRenderer());
		
		
		actionHandler = new TreeObjectActionHandler(this);

		setDropMode(DropMode.ON_OR_INSERT);		
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, actionHandler);
        new DropTarget(this, DnDConstants.ACTION_MOVE, actionHandler, true, null);

		
		addMouseListener(actionHandler);

		refreshObjectList();
	}

	
	public void refreshObjectList(){
		// Store for persistent selection in GUI
		Object                prevSelectionO = getSelectionPath() == null ? null : getSelectionPath().getLastPathComponent();
		String                prevSelectedUID= null;
		String                prevSelectedLOC= null;
		if (prevSelectionO instanceof ProgramObjectNode){
			prevSelectedUID = ((ProgramObjectNode) prevSelectionO).po.uid;
		} else if (prevSelectionO instanceof GroupNode){
			prevSelectedLOC = "";
			for (Object o : getSelectionPath().getPath()){
				prevSelectedLOC = prevSelectedLOC + o + "/";
			}
		}
		
		// Store for persistent expansion state in GUI 
		Enumeration<TreePath> prevExpanded   = getExpandedDescendants(new TreePath(new Object[]{model.getRoot()}));
		Set<String>           expandedSet    = new HashSet<String>();
		if (prevExpanded != null)
			while(prevExpanded.hasMoreElements()){
				String expandedNode = "";
				for (Object o : prevExpanded.nextElement().getPath()){
					expandedNode = expandedNode + o + "/";
				}
				expandedSet.add(expandedNode);
			}		
		
		// Clear except Root
		((GroupNode) model.getRoot()).removeAllChildren();
		Map<String, GroupNode> paths = new HashMap<String, GroupNode>();
		paths.put("/", (GroupNode) model.getRoot());

		// LOAD
		TreePath       newSelection = null;
		List<TreePath> newExpanded  = new ArrayList<TreePath>();
		for(AbstractProgramObject apo : ProgramObjectStore.getNonDeletedItems()){
			String location = ("/" + apo.location +"/").replace("//","/").replace("//","/");
			// ensure path exists
			if (!paths.containsKey(location)){
				String pathLocation = "/";
				while(pathLocation.length() <location.length()){
					String parentPathLocation = pathLocation;
					pathLocation = location.substring(0, location.indexOf("/", pathLocation.length()+1)+1);
					if (!paths.containsKey(pathLocation)){
						GroupNode parent = paths.get(parentPathLocation);
						GroupNode newGN  = new GroupNode(this, pathLocation);
						paths.put(pathLocation, newGN);
						parent.insert(newGN);
						
						if (pathLocation.equals(prevSelectedLOC)){
							newSelection = new TreePath(newGN.getPath());
						}
						
						if (expandedSet.contains(pathLocation)){
							newExpanded.add(new TreePath(newGN.getPath()));
						}
					}
					
				}
			}
			
			// place object
			GroupNode parent = paths.get(location);
			ProgramObjectNode pon = apo.getObjectTreeNode(this);
			parent.insert(pon);
			if (apo.uid != null && apo.uid.equals(prevSelectedUID)){
				newSelection = new TreePath(pon.getPath());
			}
		}
		
		//Restore GUI
		model.reload();
		setSelectionPath(newSelection);
		for (TreePath exp : newExpanded)
			setExpandedState(exp, true);
	}	
}
