package mn.dbtk.gui.main;

import java.awt.event.*;

import javax.swing.JFrame;
import mn.dbtk.sql.DBObjectCache;
import mn.dbtk.sql.SQLConnectionStatics;

public class DBTKMainScreen extends JFrame {
	public DBTKMainScreen(){
		this.add(new DBTKMainScreen());
		
		setTitle("Main window");
		pack();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		setVisible(true);
	}
	

	public static void main(String [] args){
		DBTKConfigFile.configFile.loadFrom("dbtkConfig.txt");
		new DBTKMainScreen();
	}

	
	public void exit(){
		DBTKConfigFile.configFile.writeTo("dbtkConfig.txt");
		SQLConnectionStatics.closePoolConnection();
		System.exit(0);
	}
}
