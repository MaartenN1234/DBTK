package mn.dbtk.gui.objecttree;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mn.dbtk.gui.main.MainWindowActionHandler;
import mn.dbtk.programobjects.AbstractProgramObject;
import mn.dbtk.programobjects.ProgramObjectStore;

public class TreeObjectActionHandler extends DropTargetAdapter implements DragGestureListener, MouseListener{
	private TreeObjectPanel tree;
	
	public TreeObjectActionHandler(TreeObjectPanel tree) {
		this.tree = tree;
	}

	public DefaultMutableTreeNode getNodeAtPoint(Point p){
		TreePath pointPath = tree.getPathForLocation((int) p.getX(), (int) p.getY());
		if (pointPath ==null)
			return null;
		
        Object pointObject = pointPath.getLastPathComponent();
        
        if (pointObject instanceof DefaultMutableTreeNode){
        	return (DefaultMutableTreeNode) pointObject;
        }
        return null;
		
	}
	public void mouseClicked(MouseEvent e)  {
		DefaultMutableTreeNode clickObject = getNodeAtPoint(e.getPoint());
        
        if (clickObject != null && clickObject instanceof ProgramObjectNode){
        	ProgramObjectNode poNode = (ProgramObjectNode) clickObject;
       		MainWindowActionHandler.handler.focusInMainScreen(poNode.po, false);
        }
    }
	
	public void dragGestureRecognized(DragGestureEvent dge) {
		DefaultMutableTreeNode itemAtDragOrigin = getNodeAtPoint(dge.getDragOrigin());
		
		if(itemAtDragOrigin != null){
			Cursor cursor = null;
	        
			if (dge.getDragAction() == DnDConstants.ACTION_MOVE) {
	            cursor = DragSource.DefaultMoveDrop;
	        }	
			if (itemAtDragOrigin instanceof GroupNode){
				GroupNode gn = (GroupNode) itemAtDragOrigin;
				if (!gn.name.equals("/"))
					dge.startDrag(cursor, new TransferableNodeItem(gn.name));				
			} else if (itemAtDragOrigin instanceof ProgramObjectNode){
				ProgramObjectNode pon = (ProgramObjectNode) itemAtDragOrigin;
				dge.startDrag(cursor, new TransferableNodeItem(pon.po.uid));
			}
		}
	}
	public void drop(DropTargetDropEvent dtde) {
		 try {
	          Transferable tr = dtde.getTransferable();
	          if (dtde.isDataFlavorSupported(TransferableNodeItem.flavor)) {
	        	  String itemAtDragOrigin = (String) tr.getTransferData(TransferableNodeItem.flavor);
	        	  
	        	  DefaultMutableTreeNode itemAtDropLocation = getNodeAtPoint(dtde.getLocation());
				  if(itemAtDropLocation!= null){		              
		              dtde.acceptDrop(DnDConstants.ACTION_MOVE);
		              
		              GroupNode targetnode = null;
		              if (itemAtDropLocation instanceof ProgramObjectNode){
		            	  TreeNode par = ((ProgramObjectNode) itemAtDropLocation).getParent();
		            	  if (par instanceof GroupNode){
		            		  targetnode = (GroupNode) par;
		            	  }
		              } else if (itemAtDropLocation instanceof GroupNode){
		            	  targetnode = (GroupNode) itemAtDropLocation;
		              }
		              dndComplete(itemAtDragOrigin, targetnode);
		              
		              dtde.dropComplete(true);
		              return;
				  }
	            }
	            dtde.rejectDrop();
	        } catch (Exception e) {
	          e.printStackTrace();
	          dtde.rejectDrop();
	        }			
	}
	
	private void dndComplete(String itemAtDragOrigin, GroupNode itemAtDropLocation) {
		AbstractProgramObject apo = ProgramObjectStore.getUID(itemAtDragOrigin);
		String targetLocation = itemAtDropLocation.name;
		if (apo != null){
			apo.location = targetLocation;
			apo.notifyDefinitionUpdate(null);
			apo.saveIfNotOpened();
		} else {
			ProgramObjectStore.moveAll(itemAtDragOrigin, targetLocation);
		}
	}

	static private class TransferableNodeItem implements Transferable {
	    protected static DataFlavor flavor = new DataFlavor(String.class, "A string indicating the node");	    
	
	    private static DataFlavor[] supportedFlavors = {flavor};
	    String payload;
	
	    public TransferableNodeItem(String payload) { 
	    	this.payload = payload;
	    }
	
	    public DataFlavor[] getTransferDataFlavors() { 
	    	return supportedFlavors; 
	    }
	
	    public boolean isDataFlavorSupported(DataFlavor flavor) {
	    	return flavor.equals(flavor);
	    }
	    
	   public Object getTransferData(DataFlavor flavor)  throws UnsupportedFlavorException {
	     if (flavor.equals(flavor))
	         return payload;
	     else 
	         throw new UnsupportedFlavorException(flavor);
	   }
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}	
}
