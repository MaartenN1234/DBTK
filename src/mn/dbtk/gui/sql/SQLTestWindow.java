package mn.dbtk.gui.sql;

import java.awt.event.*;

import javax.swing.JFrame;
import mn.dbtk.sql.SQLConnectionStatics;

public class SQLTestWindow extends JFrame implements WindowListener{
	public SQLTestWindow(){
		this.add(new SQLPanel());
		
		this.setTitle("Test window");
		this.setBounds(100,100,1000,600);
		this.addWindowListener(this);
		this.setVisible(true);
	}
	
	
	public static void main(String [] args){
		new SQLTestWindow();
	}


	public void windowOpened(WindowEvent e) {		
	}


	public void windowClosing(WindowEvent e) {
		SQLConnectionStatics.closePoolConnection();
		System.exit(0);
	}


	public void windowClosed(WindowEvent e) {		
	}

	public void windowIconified(WindowEvent e) {
		
	}

	public void windowDeiconified(WindowEvent e) {		
	}


	public void windowActivated(WindowEvent e) {		
	}


	public void windowDeactivated(WindowEvent e) {
	}
	
	
}
