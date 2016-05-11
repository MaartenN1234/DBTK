package mn.dbtk.gui.generics;

import java.awt.Insets;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;


public class IconProvider {
	final private static String classpath     = "/mn/dbtk/resources/icons/";
	final private static Insets defaultInsets = new Insets(0, 0, 0, 0);
	
	public static Icon get(String iconName){
		URL  location = IconProvider.class.getResource(classpath+iconName+".png");
		if (location == null)
		 	return null;
		Icon result = new ImageIcon(location);
		return result;
	}
	
	public static JButton getButton(String iconName, String toolTipText){
		JButton result = new JButton(get(iconName));
		result.setMargin(defaultInsets);
		result.setToolTipText(toolTipText);
		return result;
	}
	public static  JToggleButton getToggleButton(String normalIconName, String toggleIconName, boolean selected, String toolTipText){
		JToggleButton result = new JToggleButton(get(normalIconName), selected);
		result.setSelectedIcon(get(toggleIconName));
		result.setMargin(defaultInsets);
		result.setToolTipText(toolTipText);
		return result;	
	}
}
