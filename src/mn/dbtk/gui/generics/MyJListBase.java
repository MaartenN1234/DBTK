package mn.dbtk.gui.generics;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;


public class MyJListBase extends JList<String>{
	private static final long serialVersionUID = 621178390578346067L;
	static public interface DropListener{
		void dndComplete(String sourceName, String sourceItem, String targetName, String targetItem);
	}	
	
	private String       name;
	private DropListener dropListener;
	
	public MyJListBase(String name, String [] items){
		this(name, items, null);
	}
	
	public MyJListBase(String name, String [] items, DropListener dropListener){
		super(items);
		this.name         = name;
		this.dropListener = dropListener;
		this.setDropMode(DropMode.ON);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if(dropListener != null){
	        EventHandler dndhandler = new EventHandler();
	        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_LINK, dndhandler);
	        new DropTarget(this, DnDConstants.ACTION_LINK, dndhandler, true, null);
        }
	}
	
	public String getItemAtPoint(Point p){
		int index = this.getUI().locationToIndex(this, p);
		if(this.getCellBounds(index, index).contains(p)){
			return this.getModel().getElementAt(index);
		}
		return null;
	}
	

	
	private class EventHandler extends DropTargetAdapter implements DragGestureListener{
		public void dragGestureRecognized(DragGestureEvent dge) {
	        String itemAtDragOrigin = getItemAtPoint(dge.getDragOrigin());
	        
			if(itemAtDragOrigin != null){
				Cursor cursor = null;
		        
				if (dge.getDragAction() == DnDConstants.ACTION_LINK) {
		            cursor = DragSource.DefaultLinkDrop;
		        }	
				dge.startDrag(cursor, new TransferableLabelledListItem(name, itemAtDragOrigin));
			}
		}
		public void drop(DropTargetDropEvent dtde) {
			 try {
		          Transferable tr = dtde.getTransferable();
		          if (dtde.isDataFlavorSupported(TransferableLabelledListItem.flavor)) {
		        	  TransferableLabelledListItem.Payload source = (TransferableLabelledListItem.Payload) tr.getTransferData(TransferableLabelledListItem.flavor);
		        	  
		              String itemAtDropLocation = getItemAtPoint(dtde.getLocation());
					  if(itemAtDropLocation!= null){		              
			              dtde.acceptDrop(DnDConstants.ACTION_LINK);
				            	
			              if (dropListener != null)
			            	  dropListener.dndComplete(source.listLabel, source.item, name, itemAtDropLocation);
			              
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
	}

	static private class TransferableLabelledListItem implements Transferable {
		static private class Payload{
			public String listLabel;
			public String item;
		    public Payload (String listLabel, String item) { 
		    	this.listLabel = listLabel; 
		    	this.item = item;
		    }
		}
	    protected static DataFlavor flavor = new DataFlavor(Payload.class, "A Labelled List Item");	    
	
	    private static DataFlavor[] supportedFlavors = {flavor};
	
	    Payload payload;
	
	    public TransferableLabelledListItem(String listLabel, String item) { 
	    	this.payload = new Payload(listLabel, item);
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
	
}


