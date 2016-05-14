package mn.dbtk.gui.generics;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class MyTextAreaBase extends JTextArea {
	private static final long serialVersionUID = 7756443864087821331L;
	private UndoManager undoManager;
	
	public MyTextAreaBase(String string, int i, int j) {
		super(string,i,j);
		init();
	}

	public MyTextAreaBase(String string) {
		this(string, 0, 0);
	}
	
	private void init(){
		setLineWrap(true);
		setWrapStyleWord(true);
		addUndoFunctionality();
		setDragEnabled(true);
	}
	
	public JScrollPane getScrollPane(){
		return new JScrollPane(this);
	}

	@SuppressWarnings("serial")
	private void addUndoFunctionality() {
		undoManager = new UndoManager();
		Document doc = this.getDocument();
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent e) {
		        undoManager.addEdit(e.getEdit());
		    }
		});

		InputMap  im = this.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = this.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo");
		
	
		am.put("Undo", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
		        try {
		            if (undoManager.canUndo()) {
		                undoManager.undo();
		                if (getText().equals("")){
				            if (undoManager.canUndo()) {
				                undoManager.undo();
				            }
		                }
		            }
		        } catch (CannotUndoException exp) {
		        }
		    }
		});
		am.put("Redo", new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        try {
		            if (undoManager.canRedo()) {
		                undoManager.redo();
		                if (getText().equals("")){
				            if (undoManager.canRedo()) {
				                undoManager.redo();
				            }
		                }
		            }
		        } catch (CannotUndoException exp) {
		        }
		    }
		});		
	}
}
