package mn.dbtk.config;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFileChooser;
import mn.dbtk.gui.main.MainWindowActionHandler;

public class ConfigurationDialogActionHandler extends WindowAdapter {
	private MainWindowActionHandler  windowHandler;
	private ConfigurationDialog      dialog;
	private boolean skipHandlingActions = false;
	
	private Configuration           prevSelectedConfiguration = null;
	private Configuration.DBSchema  prevSelectedSchema        = null;
	private Configuration.DBService prevSelectedService = null;
	
	ConfigurationDialogActionHandler(MainWindowActionHandler windowHandler, ConfigurationDialog dialog){
		this.windowHandler = windowHandler;
		this.dialog        = dialog;
		ConfigFile.data.initFromFile();
	}

	public void windowClosing(WindowEvent e) {
		cancel();
	}
	
	public void cancel() {
		ConfigFile.data.initFromFile();
		close();
	}

	public void apply(){
		Configuration.setActive((Configuration) (dialog.configurationCB.getSelectedItem()));
		storeConfiguration();
		storeSchema();
		storeService();
		ConfigFile.data.materializeAsFile();
		Configuration.reload();
	}
	public void submit() {
		apply();
		close();
	}


	public void close() {
		dialog.setVisible(false);
		dialog.dispose();
		windowHandler.reloadMenuBar();
	}

	public void openObjectPathDialog(){
		final JFileChooser chooser = new JFileChooser();
		
		chooser.setDialogTitle("Choose a path containg configuration objects");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		
		//In response to a button click:
		if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
			dialog.objectPath.setText(chooser.getSelectedFile().toString());
		}
	}

	private Configuration getConfigurationFromForm(){
		String configName                   = dialog.nameField.getText();
		String configPath                   = dialog.objectPath.getText();
		Configuration.DBSchema configSchema = (Configuration.DBSchema) (dialog.schemaCB.getSelectedItem());
		
		return new Configuration(configName, configSchema, configPath);
	}
	private void storeConfiguration(){
		storeConfiguration((Configuration) (dialog.configurationCB.getSelectedItem()));
	}
	private void storeConfiguration(Configuration oldConfig){
		if (oldConfig != null){
			Configuration newConfig = getConfigurationFromForm();
			Configuration.removeConfiguration(oldConfig);
			oldConfig.name = newConfig.name;
			oldConfig.objectFilePath = newConfig.objectFilePath;
			oldConfig.schema = newConfig.schema;
			Configuration.addConfiguration(oldConfig);
		}
	}
	
	public void newConfiguration() {
		// create new
		Configuration config = getConfigurationFromForm();
		Configuration.addConfiguration(config);

		// update reference in configuration file
		Configuration.setActive(config);

		// update GUI
		skipHandlingActions = true;
		dialog.configurationCB.addItem(config);
		dialog.deleteConfigButton.setEnabled(true);
		dialog.configurationCB.setSelectedItem(config);
		skipHandlingActions = false;
	}
	
	private Configuration.DBSchema getSchemaFromForm(){
		String schemaName                   = dialog.schemaNameField.getText();
		String schemaUser                   = dialog.userField.getText();
		String schemaPass                   = new String(dialog.passwordField.getPassword());
		Configuration.DBService schemaServ  = (Configuration.DBService) (dialog.serviceCB.getSelectedItem());
		
		return new Configuration.DBSchema(schemaName, schemaUser, schemaPass, schemaServ);
	}
	
	private void storeSchema(){
		storeSchema((Configuration.DBSchema) (dialog.schemaCB.getSelectedItem()));
	}
	private void storeSchema(Configuration.DBSchema oldSchema){
		if (oldSchema != null){
			Configuration.DBSchema newSchema = getSchemaFromForm();
			Configuration.removeSchema(oldSchema);
			oldSchema.name = newSchema.name;
			oldSchema.user = newSchema.user;
			oldSchema.pass = newSchema.pass;
			oldSchema.service = newSchema.service;
			Configuration.addSchema(oldSchema);
		}
	}
	
	public void newSchema() {
		// create new
		Configuration.DBSchema schema = getSchemaFromForm();
		Configuration.addSchema(schema);

		// update reference in configuration file
		Configuration config = (Configuration) (dialog.configurationCB.getSelectedItem());
		if (config != null){
			config.schema = schema;
			Configuration.removeConfiguration(config);
			Configuration.addConfiguration(config);
		}

		// update GUI
		skipHandlingActions = true;
		dialog.schemaCB.addItem(schema);
		dialog.deleteSchemaButton.setEnabled(true);
		dialog.schemaCB.setSelectedItem(schema);
		skipHandlingActions = false;
	}

	private Configuration.DBService getServiceFromForm(){
		String serviceName     = dialog.serviceNameField.getText();
		String serviceHost     = dialog.hostField.getText();
		String servicePortStr  = dialog.portField.getText();
		String serviceService  = dialog.serviceField.getText();
		
		int servicePort = 1521;
		try{
			servicePort = Integer.parseInt(servicePortStr);
		} catch (NumberFormatException e){
		}
		dialog.portField.setText(""+servicePort);
		
		return new Configuration.DBService(serviceName, serviceHost, servicePort, serviceService);

	}
	private void storeService(){
		storeService((Configuration.DBService) (dialog.serviceCB.getSelectedItem()));
	}
	
	private void storeService(Configuration.DBService oldService){
		if (oldService != null){
			Configuration.DBService newService = getServiceFromForm();
			Configuration.removeService(oldService);
			oldService.name = newService.name;
			oldService.host = newService.host;
			oldService.port = newService.port;
			oldService.service = newService.service;
			Configuration.addService(oldService);
		}
	}	
	
	public void newService() {
		// create new
		Configuration.DBService service = getServiceFromForm();
		Configuration.addService(service);

		// update reference in configuration file
		Configuration.DBSchema configSchema = (Configuration.DBSchema) (dialog.schemaCB.getSelectedItem());
		if (configSchema != null){
			configSchema.service = service;
			Configuration.removeSchema(configSchema);
			Configuration.addSchema(configSchema);
		}

		// update GUI
		skipHandlingActions = true;
		dialog.serviceCB.addItem(service);
		dialog.deleteServiceButton.setEnabled(true);
		dialog.serviceCB.setSelectedItem(service);
		skipHandlingActions = false;
	}

	public void configurationChanged() {
		if (!skipHandlingActions) {
			storeConfiguration(prevSelectedConfiguration);
			
			Configuration selected = (Configuration) (dialog.configurationCB.getSelectedItem());
			dialog.deleteConfigButton.setEnabled(selected != null);	
	
			if (selected == null)
				return;
			boolean isEmpty = (selected == Configuration.EMPTY);
			
			if(dialog.configurationCB.getItemCount()==1 || !isEmpty){
				dialog.nameField.setText(isEmpty ? selected.name.replaceAll("\\(","").replaceAll("\\)","") : selected.name);
				dialog.objectPath.setText(selected.objectFilePath);
				dialog.schemaCB.setSelectedItem(selected.schema);
			}		
	
			dialog.addConfigButton.setEnabled(true);	
			dialog.deleteConfigButton.setEnabled(dialog.configurationCB.getItemCount()>0);
			
			schemaChanged();
		} 
		prevSelectedConfiguration = (Configuration) (dialog.configurationCB.getSelectedItem());
	}
	public void schemaChanged() {
		if (!skipHandlingActions){
			storeSchema(prevSelectedSchema);
			Configuration.DBSchema selected = (Configuration.DBSchema) (dialog.schemaCB.getSelectedItem());
			dialog.deleteSchemaButton.setEnabled(selected != null);
	
			if (selected == null)
				return;
			boolean isEmpty = (selected == Configuration.DBSchema.EMPTY);
			
			if(dialog.schemaCB.getItemCount()==1 || !isEmpty){
				dialog.schemaNameField.setText(isEmpty ? selected.name.replaceAll("\\(","").replaceAll("\\)","") : selected.name);
				dialog.userField.setText(selected.user);
				dialog.passwordField.setText(selected.pass);
				dialog.serviceCB.setSelectedItem(selected.service);
			}	
			
			dialog.addSchemaButton.setEnabled(true);	
			dialog.deleteSchemaButton.setEnabled(dialog.schemaCB.getItemCount()>0);
			
			serviceChanged();
		}
		prevSelectedSchema = (Configuration.DBSchema) (dialog.schemaCB.getSelectedItem());
	}
	public void serviceChanged() {
		if (!skipHandlingActions) {
			storeService(prevSelectedService);
			Configuration.DBService selected = (Configuration.DBService) (dialog.serviceCB.getSelectedItem());
			dialog.deleteServiceButton.setEnabled(selected != null);
	
			if (selected == null)
				return;
			boolean isEmpty = (selected == Configuration.DBService.EMPTY);
			
			if(dialog.serviceCB.getItemCount()==1 || !isEmpty){
				dialog.serviceNameField.setText(isEmpty ? selected.name.replaceAll("\\(","").replaceAll("\\)","") : selected.name);
				dialog.hostField.setText(selected.host);
				dialog.portField.setText(""+selected.port);
				dialog.serviceField.setText(selected.service);
			}	
							
			dialog.addServiceButton.setEnabled(true);	
			dialog.deleteServiceButton.setEnabled(dialog.serviceCB.getItemCount()>0);
		}	
		prevSelectedService = (Configuration.DBService) (dialog.serviceCB.getSelectedItem());

	}


	public void deleteService() {
		Configuration.DBService selected = (Configuration.DBService) (dialog.serviceCB.getSelectedItem());
		dialog.serviceCB.removeItem(selected);
		Configuration.removeService(selected);		
	}

	public void deleteSchema() {
		Configuration.DBSchema selected = (Configuration.DBSchema) (dialog.schemaCB.getSelectedItem());
		dialog.schemaCB.removeItem(selected);
		Configuration.removeSchema(selected);		
	}

	public void deleteConfiguration() {
		Configuration selected = (Configuration) (dialog.configurationCB.getSelectedItem());
		dialog.configurationCB.removeItem(selected);
		Configuration.removeConfiguration(selected);		
	}
}



	
	