package mn.dbtk.gui.programobjects;

import javax.swing.JComponent;
import javax.swing.JPanel;

import mn.dbtk.gui.main.MainWindowActionHandler;
import mn.dbtk.programobjects.DBTableProgramObject;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;

import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public class DBTablePanel extends AbstractPOPanel<DBTableProgramObject> {

	private DBTableExternalSubPanel externalSubPanel;
	private DBTableInternalSubPanel internalSubPanel;
	private JRadioButton            rdbtnExternal;
	private JRadioButton            rdbtnInternallyManaged;
	private boolean                 isInternal = false;

	
	/**
	 * Create the panel.
	 */
	public DBTablePanel(DBTableProgramObject programObject) {
		super(programObject); 
	}
	
	protected JComponent specificMainPanel(){
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setVgap(2);
		flowLayout.setHgap(2);
		flowLayout.setAlignment(FlowLayout.LEFT);
		result.add(panel, BorderLayout.NORTH);
		
		JLabel lblTable = new JLabel("Table");
		panel.add(lblTable);
		
		rdbtnInternallyManaged = new JRadioButton("Internally managed");
		panel.add(rdbtnInternallyManaged);
		
		rdbtnExternal = new JRadioButton("External");
		panel.add(rdbtnExternal);
		
		final JPanel cardPanel = new JPanel();
		result.add(cardPanel, BorderLayout.CENTER);
		final CardLayout cardPanelLayout = new CardLayout(0, 0);
		cardPanel.setLayout(cardPanelLayout);
		
		externalSubPanel = new DBTableExternalSubPanel(this);
		internalSubPanel = new DBTableInternalSubPanel(this);
		
		cardPanel.add(internalSubPanel, "INT");
		cardPanel.add(externalSubPanel, "EXT");
		
		 ButtonGroup group = new ButtonGroup();
		 group.add(rdbtnInternallyManaged);
		 group.add(rdbtnExternal);

		
		rdbtnInternallyManaged.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnExternal.getModel().isSelected()){
					cardPanelLayout.show(cardPanel, "EXT");
					isInternal = false;	
					externalSubPanel.reloadFromObjectDefinitionSub();
				} else if (rdbtnInternallyManaged.getModel().isSelected()){
					cardPanelLayout.show(cardPanel, "INT");
					isInternal = true;
					internalSubPanel.reloadFromObjectDefinitionSub();
				}
				fillProgramObjectFromForm();
			}			
		});
		rdbtnExternal.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnExternal.getModel().isSelected()){
					cardPanelLayout.show(cardPanel, "EXT");
					isInternal = false;	
					externalSubPanel.reloadFromObjectDefinitionSub();
				} else if (rdbtnInternallyManaged.getModel().isSelected()){
					cardPanelLayout.show(cardPanel, "INT");
					isInternal = true;
					internalSubPanel.reloadFromObjectDefinitionSub();
				}
				fillProgramObjectFromForm();
			}			
		});		
		 
		return result;
	}

	protected void reloadFromObjectDefinitionSub() {
		isInternal = programObject.isInternalType;	
		
		if (isInternal){
			rdbtnInternallyManaged.getModel().setSelected(true);
			internalSubPanel.reloadFromObjectDefinitionSub();
		} else {
			rdbtnExternal.getModel().setSelected(true);
			externalSubPanel.reloadFromObjectDefinitionSub();
		}
	}

	protected boolean fillProgramObjectFromFormSub() {
		boolean hasChanges    = (programObject.isInternalType == isInternal);
		boolean subHasChanges = false;
		programObject.isInternalType = isInternal;
		
		if (isInternal){
			if (internalSubPanel != null)
				subHasChanges = internalSubPanel.fillProgramObjectFromFormSub();
		} else{
			if (externalSubPanel != null)
				subHasChanges = externalSubPanel.fillProgramObjectFromFormSub();
		}
		
		return hasChanges || subHasChanges;
	}
}
