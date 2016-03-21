package mn.dbtk.programobjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgramObjectFactory{
	private static EmptyProgramObject proxyLoader = new EmptyProgramObject();
	private static Map<String, Class<? extends AbstractProgramObject>>  typeClassCache = new HashMap<String, Class<? extends AbstractProgramObject>>();
	private static Set<String> usedUIDList = new HashSet<String>();
	
	public static AbstractProgramObject readFromString(String data) throws StoredObjectParseException{
		try {
			proxyLoader.initFromLineFile(data);
			Class<? extends AbstractProgramObject> specificClass = typeClassCache.get(proxyLoader.classType);
			if (specificClass == null){
				Class<?> specificClassG = Class.forName(proxyLoader.classType);
				specificClass = ((AbstractProgramObject) specificClassG.newInstance()).getClass();
				typeClassCache.put(proxyLoader.classType, specificClass);
			}
			AbstractProgramObject result = (AbstractProgramObject) specificClass.newInstance();
			if (usedUIDList.contains(result.uid)){
				result.uid = getNewUID();
			}
			usedUIDList.add(result.uid);
			result.initFromLineFile(data);
			return result;
		} catch (ClassCastException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new StoredObjectParseException("Could not instantiate object", e);
		} 
	}
	
	static String getNewUID() {
		String result = ("" + Math.random()).substring(2);
		while (usedUIDList.contains(result))
			result = ("" + Math.random()).substring(2);
		return result;
	}
	
	private static class EmptyProgramObject  extends AbstractProgramObject{
		public String createDatabaseScript() {
			return null;
		}
	
		protected String specificDefinitionAsLineFile() {
			return null;
		}
	
		protected void specificInitFromLineFile(String data)
				throws StoredObjectParseException {
		}
	}



}
