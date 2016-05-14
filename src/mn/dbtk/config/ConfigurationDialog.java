package mn.dbtk.config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import mn.dbtk.gui.generics.IconProvider;
import mn.dbtk.gui.main.MainWindow;
import mn.dbtk.gui.main.MainWindowActionHandler;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JPasswordField;

public class ConfigurationDialog extends JDialog {
    private ConfigurationDialogActionHandler actionHandler;
	private final JPanel contentPanel = new JPanel();
	
	Map<Long, Configuration>           configurations;
	Map<Long, Configuration.DBSchema>  schemas;
	Map<Long, Configuration.DBService> services;
	
	JComboBox<Configuration> configurationCB;
	JButton addConfigButton;
	JButton deleteConfigButton;
	
	JTextField nameField;
	JTextField objectPath;
	JComboBox<Configuration.DBSchema> schemaCB;
	JButton addSchemaButton;
	JButton deleteSchemaButton;
	
	JTextField schemaNameField;
	JTextField userField;
	JPasswordField passwordField;
	JComboBox<Configuration.DBService> serviceCB;
	JButton addServiceButton;
	JButton deleteServiceButton;
	
	JTextField serviceNameField;
	JTextField hostField;
	JTextField portField;
	JTextField serviceField;
	
	

	public ConfigurationDialog(MainWindow mainWindow, MainWindowActionHandler mainWindowHandler) {
		ConfigFile.data.initFromFile();
		configurations = Configuration.getConfigurations();
		schemas        = Configuration.getSchemas();
		services       = Configuration.getServices();
		
		actionHandler  = new ConfigurationDialogActionHandler(mainWindowHandler, this);
		setModal(true);
		setTitle("Configurations");

		setBounds((int) mainWindow.getBounds().getCenterX()-400/2, 
				  (int) mainWindow.getBounds().getCenterY()-525/2, 
				  400, 505);
		
		addWindowListener(actionHandler);
		
		
		// GUI starts here
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblConfigurations = new JLabel("Configuration");
			GridBagConstraints gbc_lblConfigurations = new GridBagConstraints();
			gbc_lblConfigurations.gridwidth = 3;
			gbc_lblConfigurations.anchor = GridBagConstraints.WEST;
			gbc_lblConfigurations.insets = new Insets(0, 0, 5, 5);
			gbc_lblConfigurations.gridx = 0;
			gbc_lblConfigurations.gridy = 0;
			contentPanel.add(lblConfigurations, gbc_lblConfigurations);
		}
		/*
		{
			JButton btnTest_1 = new JButton("test");
			GridBagConstraints gbc_btnTest_1 = new GridBagConstraints();
			gbc_btnTest_1.anchor = GridBagConstraints.WEST;
			gbc_btnTest_1.gridwidth = 2;
			gbc_btnTest_1.insets = new Insets(0, 0, 5, 5);
			gbc_btnTest_1.gridx = 3;
			gbc_btnTest_1.gridy = 0;
			contentPanel.add(btnTest_1, gbc_btnTest_1);
		}*/
		{
			JLabel lblSelectActiveConfiguration = new JLabel("Active");
			GridBagConstraints gbc_lblSelectActiveConfiguration = new GridBagConstraints();
			gbc_lblSelectActiveConfiguration.insets = new Insets(0, 0, 5, 5);
			gbc_lblSelectActiveConfiguration.anchor = GridBagConstraints.EAST;
			gbc_lblSelectActiveConfiguration.gridx = 1;
			gbc_lblSelectActiveConfiguration.gridy = 1;
			contentPanel.add(lblSelectActiveConfiguration, gbc_lblSelectActiveConfiguration);
		}
		{
			configurationCB = new JComboBox<Configuration>(new Vector<Configuration>(configurations.values()));
			if (Configuration.getActive() != Configuration.EMPTY)
				configurationCB.setSelectedItem(Configuration.getActive());
			configurationCB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.configurationChanged();
				}
			});			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 1;
			contentPanel.add(configurationCB, gbc_comboBox);
		}
		{
			addConfigButton = IconProvider.getButton("add","Add a new configuration based on the current one");
			addConfigButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.newConfiguration();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 3;
			gbc_button.gridy = 1;
			contentPanel.add(addConfigButton, gbc_button);
		}
		{
			deleteConfigButton = IconProvider.getButton("delete","Delete configuration");
			deleteConfigButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.deleteConfiguration();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 4;
			gbc_button.gridy = 1;
			contentPanel.add(deleteConfigButton, gbc_button);
		}
		{
			JLabel lblName = new JLabel("Name");
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.insets = new Insets(0, 0, 5, 5);
			gbc_lblName.gridx = 1;
			gbc_lblName.gridy = 2;
			contentPanel.add(lblName, gbc_lblName);
		}
		{
			nameField = new JTextField();
			nameField.setText("example");
			GridBagConstraints gbc_nameField = new GridBagConstraints();
			gbc_nameField.insets = new Insets(0, 0, 5, 5);
			gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
			gbc_nameField.gridx = 2;
			gbc_nameField.gridy = 2;
			contentPanel.add(nameField, gbc_nameField);
			nameField.setColumns(10);
		}
		{
			JLabel lblFilepath = new JLabel("Object Path");
			GridBagConstraints gbc_lblFilepath = new GridBagConstraints();
			gbc_lblFilepath.anchor = GridBagConstraints.EAST;
			gbc_lblFilepath.insets = new Insets(0, 0, 5, 5);
			gbc_lblFilepath.gridx = 1;
			gbc_lblFilepath.gridy = 3;
			contentPanel.add(lblFilepath, gbc_lblFilepath);
		}
		{
			objectPath = new JTextField();
			objectPath.setText("C:\\");
			GridBagConstraints gbc_objectPath = new GridBagConstraints();
			gbc_objectPath.insets = new Insets(0, 0, 5, 5);
			gbc_objectPath.fill = GridBagConstraints.HORIZONTAL;
			gbc_objectPath.gridx = 2;
			gbc_objectPath.gridy = 3;
			contentPanel.add(objectPath, gbc_objectPath);
			objectPath.setColumns(10);
		}
		{
			JButton button = new JButton("...");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.openObjectPathDialog();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.fill = GridBagConstraints.HORIZONTAL;
			gbc_button.insets = new Insets(0, 0, 5, 5);
			gbc_button.gridwidth = 2;
			gbc_button.gridx = 3;
			gbc_button.gridy = 3;
			contentPanel.add(button, gbc_button);
		}
		{
			JLabel lblSchema = new JLabel("Schema");
			GridBagConstraints gbc_lblSchema = new GridBagConstraints();
			gbc_lblSchema.anchor = GridBagConstraints.EAST;
			gbc_lblSchema.insets = new Insets(0, 0, 5, 5);
			gbc_lblSchema.gridx = 1;
			gbc_lblSchema.gridy = 4;
			contentPanel.add(lblSchema, gbc_lblSchema);
		}
		{
			schemaCB = new JComboBox<Configuration.DBSchema>(new Vector<Configuration.DBSchema>(schemas.values()));
			if (Configuration.getActive() != null && Configuration.getActive().schema != Configuration.DBSchema.EMPTY)
				schemaCB.setSelectedItem(Configuration.getActive().schema);			
			schemaCB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.schemaChanged();
				}
			});			
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 4;
			contentPanel.add(schemaCB, gbc_comboBox);
		}
		{
			addSchemaButton = IconProvider.getButton("add","Add a new schema based on the current one");
			addSchemaButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.newSchema();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 3;
			gbc_button.gridy = 4;
			contentPanel.add(addSchemaButton, gbc_button);
		}
		{
			deleteSchemaButton = IconProvider.getButton("delete","Delete schema");
			deleteSchemaButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.deleteSchema();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 4;
			gbc_button.gridy = 4;
			contentPanel.add(deleteSchemaButton, gbc_button);
		}		
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.gridy = 5;
			gbc_separator.gridwidth = 5;
			gbc_separator.fill = GridBagConstraints.HORIZONTAL;
			gbc_separator.insets = new Insets(0, 0, 5, 5);
			gbc_separator.gridx = 0;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JLabel lblSchemas = new JLabel("Schema");
			GridBagConstraints gbc_lblSchemas = new GridBagConstraints();
			gbc_lblSchemas.anchor = GridBagConstraints.WEST;
			gbc_lblSchemas.gridwidth = 3;
			gbc_lblSchemas.insets = new Insets(0, 0, 5, 5);
			gbc_lblSchemas.gridx = 0;
			gbc_lblSchemas.gridy = 6;
			contentPanel.add(lblSchemas, gbc_lblSchemas);
		}/*
		{
			JButton btnTest = new JButton("test");
			GridBagConstraints gbc_btnTest = new GridBagConstraints();
			gbc_btnTest.anchor = GridBagConstraints.WEST;
			gbc_btnTest.gridwidth = 2;
			gbc_btnTest.insets = new Insets(0, 0, 5, 5);
			gbc_btnTest.gridx = 3;
			gbc_btnTest.gridy = 6;
			contentPanel.add(btnTest, gbc_btnTest);
		}*/
		{
			JLabel lblName_1 = new JLabel("Name");
			GridBagConstraints gbc_lblName_1 = new GridBagConstraints();
			gbc_lblName_1.anchor = GridBagConstraints.EAST;
			gbc_lblName_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblName_1.gridx = 1;
			gbc_lblName_1.gridy = 7;
			contentPanel.add(lblName_1, gbc_lblName_1);
		}
		{
			schemaNameField = new JTextField();
			schemaNameField.setText("example");
			GridBagConstraints gbc_schemaNameField = new GridBagConstraints();
			gbc_schemaNameField.insets = new Insets(0, 0, 5, 5);
			gbc_schemaNameField.fill = GridBagConstraints.HORIZONTAL;
			gbc_schemaNameField.gridx = 2;
			gbc_schemaNameField.gridy = 7;
			contentPanel.add(schemaNameField, gbc_schemaNameField);
			schemaNameField.setColumns(10);
		}
		{
			JLabel lblUser = new JLabel("User");
			GridBagConstraints gbc_lblUser = new GridBagConstraints();
			gbc_lblUser.anchor = GridBagConstraints.EAST;
			gbc_lblUser.insets = new Insets(0, 0, 5, 5);
			gbc_lblUser.gridx = 1;
			gbc_lblUser.gridy = 8;
			contentPanel.add(lblUser, gbc_lblUser);
		}
		{
			userField = new JTextField();
			userField.setText("EXAMPLE");
			GridBagConstraints gbc_userField = new GridBagConstraints();
			gbc_userField.fill = GridBagConstraints.BOTH;
			gbc_userField.insets = new Insets(0, 0, 5, 5);
			gbc_userField.gridx = 2;
			gbc_userField.gridy = 8;
			contentPanel.add(userField, gbc_userField);
			userField.setColumns(10);
		}
		{
			JLabel lblPassword = new JLabel("Password");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.anchor = GridBagConstraints.EAST;
			gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblPassword.gridx = 1;
			gbc_lblPassword.gridy = 9;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			passwordField = new JPasswordField();
			GridBagConstraints gbc_passwordField = new GridBagConstraints();
			gbc_passwordField.insets = new Insets(0, 0, 5, 5);
			gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
			gbc_passwordField.gridx = 2;
			gbc_passwordField.gridy = 9;
			contentPanel.add(passwordField, gbc_passwordField);
		}
		{
			JLabel lblService = new JLabel("Service");
			GridBagConstraints gbc_lblService = new GridBagConstraints();
			gbc_lblService.anchor = GridBagConstraints.EAST;
			gbc_lblService.insets = new Insets(0, 0, 5, 5);
			gbc_lblService.gridx = 1;
			gbc_lblService.gridy = 10;
			contentPanel.add(lblService, gbc_lblService);
		}
		{
			serviceCB = new JComboBox<Configuration.DBService>(new Vector<Configuration.DBService>(services.values()));
			if (Configuration.getActive()!= null && Configuration.getActive().schema != null && Configuration.getActive().schema.service != Configuration.DBService.EMPTY)
				serviceCB.setSelectedItem(Configuration.getActive().schema.service);				
			serviceCB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.serviceChanged();
				}
			});			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.insets = new Insets(0, 0, 5, 5);
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = 10;
			contentPanel.add(serviceCB, gbc_comboBox);
		}
		{
			addServiceButton =  IconProvider.getButton("add","Add a new service based on the current one");
			addServiceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.newService();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 3;
			gbc_button.gridy = 10;
			contentPanel.add(addServiceButton, gbc_button);
		}
		{
			deleteServiceButton = IconProvider.getButton("delete","Delete service");
			deleteServiceButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionHandler.deleteService();
				}
			});
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.insets = new Insets(0, 0, 5, 0);
			gbc_button.anchor = GridBagConstraints.WEST;
			gbc_button.gridx = 4;
			gbc_button.gridy = 10;
			contentPanel.add(deleteServiceButton, gbc_button);
		}			

		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.gridwidth = 5;
			gbc_separator.fill = GridBagConstraints.HORIZONTAL;
			gbc_separator.insets = new Insets(0, 0, 5, 5);
			gbc_separator.gridx = 0;
			gbc_separator.gridy = 11;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JLabel lblServices = new JLabel("Service");
			GridBagConstraints gbc_lblServices = new GridBagConstraints();
			gbc_lblServices.insets = new Insets(0, 0, 5, 5);
			gbc_lblServices.anchor = GridBagConstraints.WEST;
			gbc_lblServices.gridwidth = 3;
			gbc_lblServices.gridx = 0;
			gbc_lblServices.gridy = 12;
			contentPanel.add(lblServices, gbc_lblServices);
		}
		{
			JLabel lblName_2 = new JLabel("Name");
			GridBagConstraints gbc_lblName_2 = new GridBagConstraints();
			gbc_lblName_2.anchor = GridBagConstraints.EAST;
			gbc_lblName_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblName_2.gridx = 1;
			gbc_lblName_2.gridy = 13;
			contentPanel.add(lblName_2, gbc_lblName_2);
		}
		{
			serviceNameField = new JTextField();
			serviceNameField.setText("example");
			GridBagConstraints gbc_serviceTextField = new GridBagConstraints();
			gbc_serviceTextField.insets = new Insets(0, 0, 5, 5);
			gbc_serviceTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_serviceTextField.gridx = 2;
			gbc_serviceTextField.gridy = 13;
			contentPanel.add(serviceNameField, gbc_serviceTextField);
			serviceNameField.setColumns(10);
		}
		{
			JLabel lblHost = new JLabel("Host");
			GridBagConstraints gbc_lblHost = new GridBagConstraints();
			gbc_lblHost.anchor = GridBagConstraints.EAST;
			gbc_lblHost.insets = new Insets(0, 0, 5, 5);
			gbc_lblHost.gridx = 1;
			gbc_lblHost.gridy = 14;
			contentPanel.add(lblHost, gbc_lblHost);
		}
		{
			hostField = new JTextField();
			hostField.setText("localhost");
			GridBagConstraints gbc_hostField = new GridBagConstraints();
			gbc_hostField.insets = new Insets(0, 0, 5, 5);
			gbc_hostField.fill = GridBagConstraints.HORIZONTAL;
			gbc_hostField.gridx = 2;
			gbc_hostField.gridy = 14;
			contentPanel.add(hostField, gbc_hostField);
			hostField.setColumns(10);
		}
		{
			JLabel lblPort = new JLabel("Port");
			GridBagConstraints gbc_lblPort = new GridBagConstraints();
			gbc_lblPort.anchor = GridBagConstraints.EAST;
			gbc_lblPort.insets = new Insets(0, 0, 5, 5);
			gbc_lblPort.gridx = 1;
			gbc_lblPort.gridy = 15;
			contentPanel.add(lblPort, gbc_lblPort);
		}
		{
			portField = new JTextField();
			portField.setText("1521");
			GridBagConstraints gbc_portField = new GridBagConstraints();
			gbc_portField.insets = new Insets(0, 0, 5, 5);
			gbc_portField.fill = GridBagConstraints.HORIZONTAL;
			gbc_portField.gridx = 2;
			gbc_portField.gridy = 15;
			contentPanel.add(portField, gbc_portField);
			portField.setColumns(10);
		}
		{
			JLabel lblService_1 = new JLabel("Service");
			GridBagConstraints gbc_lblService_1 = new GridBagConstraints();
			gbc_lblService_1.anchor = GridBagConstraints.EAST;
			gbc_lblService_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblService_1.gridx = 1;
			gbc_lblService_1.gridy = 16;
			contentPanel.add(lblService_1, gbc_lblService_1);
		}
		{
			serviceField = new JTextField();
			serviceField.setText("XS");
			GridBagConstraints gbc_serviceField = new GridBagConstraints();
			gbc_serviceField.insets = new Insets(0, 0, 5, 5);
			gbc_serviceField.fill = GridBagConstraints.HORIZONTAL;
			gbc_serviceField.gridx = 2;
			gbc_serviceField.gridy = 16;
			contentPanel.add(serviceField, gbc_serviceField);
			serviceField.setColumns(10);
		}
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.insets = new Insets(0, 0, 0, 5);
			gbc_separator.anchor = GridBagConstraints.NORTH;
			gbc_separator.gridwidth = 5;
			gbc_separator.fill = GridBagConstraints.HORIZONTAL;
			gbc_separator.gridx = 0;
			gbc_separator.gridy = 17;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionHandler.submit();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						actionHandler.cancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		actionHandler.configurationChanged();
	}

}
