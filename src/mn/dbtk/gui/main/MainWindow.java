package mn.dbtk.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JSplitPane;

import mn.dbtk.config.Configuration;
import mn.dbtk.config.ConfigurationDialog;
import mn.dbtk.gui.generics.LoggingHandler;
import mn.dbtk.gui.objecttree.TreeObjectPanel;
import mn.dbtk.programobjects.DBTableProgramObject;

import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTabbedPane;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


import javax.swing.SwingConstants;


public class MainWindow extends JFrame {

	public static MainWindow frame = new MainWindow();
	
	private MainWindowActionHandler actionHandler;
	private JTabbedPane             mainTabbedPane;
	

	/**
	 * Create the frame.
	 */
	private MainWindow() {
		actionHandler  = MainWindowActionHandler.init(this, null);

		setTitle("Database toolkit");
		setBounds(100, 50, 1024, 768);
		addWindowListener(actionHandler);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		reloadMenuBar();
		
		getContentPane().setLayout(new BorderLayout(0, 0));

		getContentPane().add(getToolBarPanel(), BorderLayout.NORTH);
		
		
		JSplitPane splitPaneright       = new  JSplitPane(JSplitPane.VERTICAL_SPLIT,   false,
				 										  getMainTabbelPane(), getBottomTabbedPane());
		JSplitPane splitPaneCombination = new  JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
											      		  getLeftTabbedPane(), splitPaneright);
		splitPaneright.setResizeWeight(1);
		splitPaneright.setDividerSize(3);
		splitPaneCombination.setDividerSize(3);
		add(splitPaneCombination, BorderLayout.CENTER);
		
		MainWindowActionHandler.init(this, mainTabbedPane);
	}
	

	void reloadMenuBar(){
		setJMenuBar(createMenuBar());
	}


	/* Menu bars */
	private JMenuBar createMenuBar(){
		JMenuBar result = new JMenuBar();
		result.add(createFileMenu());
		result.add(createConfigMenu());
		result.add(createObjectMenu());
		return result;
	}

	private JMenu createObjectMenu() {
		JMenu result = new JMenu("Object");
				
		JMenuItem mntmAddTable = new JMenuItem("Add Table");
		mntmAddTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionHandler.focusInMainScreen(new DBTableProgramObject("/", "New table"), true);
			}
		});
		result.add(mntmAddTable);
		
		result.add(new JSeparator());

		JMenuItem mntmCloseAll = new JMenuItem("Close All");
		mntmCloseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionHandler.closeAllTabs();
			}
		});
		result.add(mntmCloseAll);		
		
		return result;
	}

	
	private JMenu createFileMenu() {
		JMenu result = new JMenu("File");
				
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionHandler.stopApplication();
			}
		});
		result.add(mntmExit);
		return result;
	}

	private JMenu createConfigMenu() {
		JMenu result = new JMenu("Configuration");
		
		JMenuItem mntmReconnect = new JMenuItem("Reload");
		mntmReconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionHandler.reloadConfig();
			}
		});
		if(Configuration.getActive() == Configuration.EMPTY)
			mntmReconnect.setEnabled(false);

		result.add(mntmReconnect);
		
		result.add(new JSeparator());
		
		boolean hasAnyConfigurations = false;
		for (final Configuration config : Configuration.getConfigurations().values()){
			JRadioButtonMenuItem chckbxmntmConfigDummy = new JRadioButtonMenuItem(config.name);
			if (config.equals(Configuration.getActive()))
					chckbxmntmConfigDummy.setSelected(true);
			
			chckbxmntmConfigDummy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.loadConfig(config);
				}
			});
			
			result.add(chckbxmntmConfigDummy);
			hasAnyConfigurations = true;
		}
		
		if(hasAnyConfigurations)
			result.add(new JSeparator());
		
		JMenuItem mntmNew = new JMenuItem("Edit ...");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConfigurationDialog dialog = new ConfigurationDialog(MainWindow.this, actionHandler);
				dialog.setVisible(true);
			}
		});
		result.add(mntmNew);
		return result;
	}
	
	/* Tool bars */
	private JPanel getToolBarPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
		JToolBar toolBar = new JToolBar();
		
		JButton btnDummyButton = new JButton("Reload");
		btnDummyButton.setHorizontalAlignment(SwingConstants.LEADING);
		btnDummyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionHandler.reloadConfig();
			}
		});
		toolBar.add(btnDummyButton);
		
		result.add(toolBar);
		return result;
	}
	
	/* Left Part */
	private Component getLeftTabbedPane() {
		JTabbedPane leftTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		leftTabbedPane.addTab("Hierarchy", null, getAllObjectsHierarchyPane(), null);
		leftTabbedPane.addTab("Search",    null, getSearchAllObjectsPane(),    null);
		leftTabbedPane.setMinimumSize(new Dimension(250,1));
		return leftTabbedPane;
	}
	

	private Component getSearchAllObjectsPane() {
		JPanel searchAllObjects = new SearchPanel();
		
		return searchAllObjects;
	}

	private Component getAllObjectsHierarchyPane() {
		return TreeObjectPanel.singleton;
	}

	/* Bottom Part */
	private Component getBottomTabbedPane() {
		JTabbedPane bottomTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		JTextArea outputConsole = new JTextArea("", 4, 200);
		(new LoggingHandler()).setConsole(outputConsole);
		bottomTabbedPane.addTab("Console", null, new JScrollPane(outputConsole, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), null);
		bottomTabbedPane.setMinimumSize(new Dimension(1,100));
		return bottomTabbedPane;
	}
	
	/* Main Part */
	private JTabbedPane getMainTabbelPane() {
		mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
				
		JPanel panel = new JPanel();
		mainTabbedPane.addTab("Empty tab", null, panel, null);
		
		return mainTabbedPane;
	}


	public JTabbedPane getMainTabbedPane() {
		return mainTabbedPane;
	}
}
