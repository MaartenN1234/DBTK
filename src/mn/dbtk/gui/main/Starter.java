package mn.dbtk.gui.main;

import mn.dbtk.config.Configuration;


public class Starter {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		MainWindow.frame.setVisible(true);
		Configuration.reload();
	}
}
