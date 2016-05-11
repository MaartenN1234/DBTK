package mn.dbtk.gui.programobjects;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import mn.dbtk.config.Configuration;
import mn.dbtk.gui.generics.MyTabbedJPanel;
import mn.dbtk.gui.generics.MyTextAreaBase;
import mn.dbtk.gui.generics.TimeOutThread;
import mn.dbtk.gui.main.MainWindow;
import mn.dbtk.gui.main.MainWindowActionHandler;
import mn.dbtk.programobjects.AbstractProgramObject;
import mn.dbtk.programobjects.ProgramObjectStore;
import mn.dbtk.sql.SQLConnectionStatics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;

import javax.swing.JTextField;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

public abstract class AbstractPOPanel<E extends AbstractProgramObject> extends MyTabbedJPanel {

	final protected E programObject;
	

	protected JTextField     objectLocationTextField;
	protected JTextField     objectNameTextField;
	protected MyTextAreaBase objectCommentsArea;
	protected MyTextAreaBase objectInvalidArea;


	
	/**
	 * Create the panel.
	 */
	public AbstractPOPanel(E programObject) {
		super(MainWindow.frame.getMainTabbedPane());
		this.programObject = programObject;
		setLayout(new BorderLayout(0, 0));
		add(sharedTopPanel(), BorderLayout.NORTH);
		add(new JScrollPane(specificMainPanel()), BorderLayout.CENTER);
		
		reloadFromObjectDefinition();
	}


	private JComponent sharedTopPanel(){
		JPanel result = new JPanel();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		result.setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		result.add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		
		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				programObject.reload();
			}
		});
		panel.add(btnReset);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				programObject.reload();
				removeFromContainer();
			}
		});
		panel.add(btnClose);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveObject();
			}
		});
		panel.add(btnSave);
		
		JButton btnSaveClose = new JButton("Save & Close");
		btnSaveClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				saveObject();
				removeFromContainer();				
			}
		});
		panel.add(btnSaveClose);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				checkDeleteObject();
			}
		});
		panel.add(btnDelete);
		
		JButton btnGenerate = new JButton("Generate to Database");
		btnGenerate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				generateToDataBase();
			}
		});
		panel.add(btnGenerate);
		
		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(2, 2, 2, 2);
		gbc_lblName.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		result.add(lblName, gbc_lblName);
		
		objectNameTextField = new JTextField(programObject.name);
		objectNameTextField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				fillProgramObjectFromFormDelayed();
			}
		});
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.insets = new Insets(2, 2, 2, 2);
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 1;
		result.add(objectNameTextField, gbc_textField_1);
		objectNameTextField.setColumns(10);
		
		JLabel lblLocation = new JLabel("Location");
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.insets = new Insets(2, 2, 2, 2);
		gbc_lblLocation.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblLocation.gridx = 2;
		gbc_lblLocation.gridy = 1;
		result.add(lblLocation, gbc_lblLocation);
		
		objectLocationTextField = new JTextField(programObject.location);
		objectLocationTextField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				fillProgramObjectFromFormDelayed();
			}
		});		
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(2, 2, 2, 2);
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 1;
		result.add(objectLocationTextField, gbc_textField);
		objectLocationTextField.setColumns(10);
		
		JLabel lblComments = new JLabel("Comments");
		GridBagConstraints gbc_lblComments = new GridBagConstraints();
		gbc_lblComments.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblComments.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblComments.insets = new Insets(2, 2, 2, 2);		
		gbc_lblComments.gridx = 0;
		gbc_lblComments.gridy = 2;
		result.add(lblComments, gbc_lblComments);	
		
		objectCommentsArea = new MyTextAreaBase(programObject.comments,5,0);
		objectCommentsArea.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				fillProgramObjectFromFormDelayed();
			}
		});			
		GridBagConstraints gbc_commentsField = new GridBagConstraints();
		gbc_commentsField.fill = GridBagConstraints.HORIZONTAL;
		gbc_commentsField.insets = new Insets(2, 2, 2, 2);	
		gbc_commentsField.gridx = 1;
		gbc_commentsField.gridwidth = 3;
		gbc_commentsField.gridy = 2;
		result.add(objectCommentsArea.getScrollPane(), gbc_commentsField);		

		return result;
	}
	









	protected void generateToDataBase() {
		String script = programObject.createDatabaseScript();
		if (script != null && script.length()>0){
			try {
				SQLConnectionStatics.executeSQL(script);
			} catch (SQLException e) {
				Logger.getGlobal().warning("Generation to database failed " + e.toString());
			}
		}
	}


	protected abstract JComponent specificMainPanel();

	public void focusForNameChange() {
		objectNameTextField.requestFocusInWindow();		
		objectNameTextField.selectAll();
	}

	public void reloadFromObjectDefinition() {
		objectNameTextField.setText(programObject.name);
		objectLocationTextField.setText(programObject.location);
		objectCommentsArea.setText(programObject.comments);
		
		
		setTab(programObject.name, programObject.getIcon());
		reloadFromObjectDefinitionSub();
		updateValidStatus();
		validate();
		
	}

	private void updateValidStatus() {
		List<String> msgs = programObject.getObjectInvalidStatusMessages();
		
		if (msgs.isEmpty()){
			if (objectInvalidArea != null){
				objectInvalidArea.setText("");
	
				remove(objectInvalidArea.getScrollPane());

				objectInvalidArea = null;
				validate();
			}
		} else {
			String msgText = msgs.stream().map(s -> s + "\r\n").collect(Collectors.joining());

			if (objectInvalidArea != null){
				objectInvalidArea.setText(msgText);
			} else {
				
				objectInvalidArea = new MyTextAreaBase(msgText,5,0);
				objectInvalidArea.setEnabled(false);
				objectInvalidArea.setDragEnabled(false);
				objectInvalidArea.setDisabledTextColor(Color.RED);
				objectInvalidArea.setFont(objectInvalidArea.getFont().deriveFont(Font.BOLD));
				
				
				add(objectInvalidArea.getScrollPane(), BorderLayout.SOUTH);
				
				validate();
			}
		}
		
	}


	public E getProgramObject(){
		fillProgramObjectFromForm();
		return programObject;
	}
	public E getProgramObjectBypassSync(){
		return programObject;
	}
	
	public void saveObject() {
		fillProgramObjectFromForm();
		programObject.save();
	}
	
	protected void fillProgramObjectFromForm() {
		boolean hasChanges = !objectNameTextField.getText().equals(programObject.name) ||
							 !objectLocationTextField.getText().equals(programObject.location) ||
							 !objectCommentsArea.getText().equals(programObject.comments);
		programObject.name     = objectNameTextField.getText();
		programObject.location = objectLocationTextField.getText();
		programObject.comments = objectCommentsArea.getText();
		
		boolean subHasChanges = fillProgramObjectFromFormSub();

		setTab(programObject.name, programObject.getIcon());
		
		updateValidStatus();
		validate();

		if (hasChanges || subHasChanges)
			programObject.notifyDefinitionUpdate(this);
	}
	

	private TimeOutThread timeOutThread;
	void fillProgramObjectFromFormDelayed() {
		if(timeOutThread != null && timeOutThread.isAlive()){
			timeOutThread.setDelay(250);
		} else {
			timeOutThread = new TimeOutThread(250){
								protected void runSub(){
									fillProgramObjectFromForm();
								}
							};
		}
	}


	protected void checkDeleteObject() {
		switch(JOptionPane.showConfirmDialog(this, 
				"Are you sure you would like to delete the table definition ?", 
				"Delete table definition ?", JOptionPane.YES_NO_OPTION)) {
		case JOptionPane.YES_OPTION:
			deleteObject();
			removeFromContainer();
			break;
		case JOptionPane.NO_OPTION:
			break;
		}
	}
	protected void deleteObject() {
		programObject.deleted = true;
		programObject.save();
		programObject.notifyDefinitionUpdate(this);
	}
	
	
	protected abstract void reloadFromObjectDefinitionSub();
	protected abstract boolean fillProgramObjectFromFormSub();
	

	public void closeFromObject() {
		removeFromContainer();
	}
}
