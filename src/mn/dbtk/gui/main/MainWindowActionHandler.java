package mn.dbtk.gui.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import mn.dbtk.config.ConfigFile;
import mn.dbtk.config.Configuration;
import mn.dbtk.gui.programobjects.AbstractPOPanel;
import mn.dbtk.programobjects.AbstractProgramObject;
import mn.dbtk.sql.SQLConnectionStatics;

public class MainWindowActionHandler extends WindowAdapter {
	public static MainWindowActionHandler handler;
	
	private MainWindow      window;
	private JTabbedPane     mainTabbedPane;
	
	static MainWindowActionHandler init(MainWindow window, JTabbedPane mainTabbedPane){
		if (handler == null){
			handler = new MainWindowActionHandler();
		}
		handler.window         = window;
		handler.mainTabbedPane = mainTabbedPane;
		
		return handler;
	}
	
	private MainWindowActionHandler(){}
	
	public void windowClosing(WindowEvent e) {
		stopApplication();
	}
	
	public void stopApplication() {
		if (closeAllTabs()){
			ConfigFile.data.materializeAsFile();
			SQLConnectionStatics.closePoolConnection();
			System.exit(0);
		}
	}



	public void reloadConfig() {
		Configuration.reload();		
	}

	public void reloadMenuBar() {
		window.reloadMenuBar();
		window.validate();
	}

	public void loadConfig(Configuration config) {
		Configuration.setActive(config);
		ConfigFile.data.materializeAsFile();
		Configuration.reload();
		reloadMenuBar();
	}

	public void focusInMainScreen(AbstractProgramObject programObject, boolean isNewObject) {
		int i = isNewObject ? -1 : getScreenForId(programObject.uid);
		
		if (i==-1){
			addInMainScreen(programObject);
			i = mainTabbedPane.getTabCount()-1;
		} else {
			mainTabbedPane.setSelectedIndex(i);
		}
		AbstractPOPanel<?> subScreen = (AbstractPOPanel<?>) mainTabbedPane.getComponentAt(i);  
		
		subScreen.focusForNameChange();
	}
	private int getScreenForId(String uidCompare){
		for (int i=0; i<mainTabbedPane.getTabCount(); i++){
			if (mainTabbedPane.getComponentAt(i) instanceof AbstractPOPanel){
				AbstractPOPanel<?> panel = (AbstractPOPanel<?>) mainTabbedPane.getComponentAt(i);
				if (panel.getProgramObject().uid.equals(uidCompare)){
					return i;
				}
			}
		}
		return -1;
	}
	private void addInMainScreen(AbstractProgramObject programObject) {
		mainTabbedPane.addTab(programObject.name, programObject.getIcon(), null, null);
		mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount()-1);
		
		AbstractPOPanel<? extends AbstractProgramObject> newSubScreen = programObject.getEditScreen();  
		
		mainTabbedPane.setComponentAt(mainTabbedPane.getTabCount()-1, newSubScreen);

	}
	
	public boolean closeAllTabs() {
		ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
		boolean cancelled = false;
	
		for (int i=0; i<mainTabbedPane.getTabCount(); i++){
			if (mainTabbedPane.getComponentAt(i) instanceof AbstractPOPanel && !cancelled){
				AbstractPOPanel<?> panel = (AbstractPOPanel<?>) mainTabbedPane.getComponentAt(i);
				AbstractProgramObject apo = panel.getProgramObject();
				if (apo.hasChangesSinceLastSave()){
					mainTabbedPane.setSelectedIndex(i);
					cancelled = checkSaveObject(apo);
				}
				removeIndexes.add(i);
			}		
		}
		Collections.reverse(removeIndexes);
		
		if (!cancelled)
			for (int i:removeIndexes)
				mainTabbedPane.removeTabAt(i);
		
		mainTabbedPane.validate();
		
		return !cancelled;
	}
	
	private boolean checkSaveObject(AbstractProgramObject apo) {
		switch(JOptionPane.showConfirmDialog(window, 
				"Do you wish to save the changes in "+apo.name+" ?", 
				"Save changes  ?", JOptionPane.YES_NO_CANCEL_OPTION)) {
		case JOptionPane.CANCEL_OPTION:
			return true;
		case JOptionPane.YES_OPTION:
			apo.save();
			break;
		case JOptionPane.NO_OPTION:
			apo.reload();
			break;
		}
		return false;
	}
}


	
	