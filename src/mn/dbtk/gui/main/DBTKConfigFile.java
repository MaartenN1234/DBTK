package mn.dbtk.gui.main;

import java.util.HashMap;
import java.util.Map;

public class DBTKConfigFile {
	public static DBTKConfigFile configFile = new DBTKConfigFile();

	private Map<String, Object> data;
	private DBTKConfigFile(){
	}
	public void loadFrom(String string) {
		data = new HashMap<String, Object>();
	}
	public void writeTo(String string) {

	}
}
