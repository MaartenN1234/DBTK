package mn.dbtk.programobjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javax.swing.Icon;

import mn.dbtk.gui.objecttree.TreeObjectPanel;
import mn.dbtk.gui.programobjects.AbstractPOPanel;

public class ProgramObjectStore {
	private static HashMap<String, AbstractProgramObject>               storage        = new HashMap<String, AbstractProgramObject>();
	private static List<AbstractProgramObject>                          nonDelCache    = new ArrayList<AbstractProgramObject>();
	private static Map<String, Class<? extends AbstractProgramObject>>  typeClassCache = new HashMap<String, Class<? extends AbstractProgramObject>>();
	
	private ProgramObjectStore(){
	}

	
	public static List<AbstractProgramObject> getNonDeletedItems() {
		return nonDelCache;
	}
	
	
	private static void propagateNonDelCacheUpdate(){
		TreeObjectPanel.singleton.refreshObjectList();
	}
	
	static String storeAndGetUID(AbstractProgramObject apo) {
		String result = ("" + Math.random()).substring(2);
		while (storage.containsKey(result))
			result = ("" + Math.random()).substring(2);
		
		storage.put(result, apo);
		if (!apo.deleted){
			nonDelCache.add(apo);
			propagateNonDelCacheUpdate();
		}
		return result;
	}
	
	
	
	public static void loadFromPath(File path){
		DummyLoaderProgramObject    proxyLoader  = new DummyLoaderProgramObject();
		Set<String>                 usedUIDs     = new HashSet<String>(storage.keySet());
		
		// Load from path
		for (File file : path.listFiles()){
			try{
				String         uid  = file.getName().substring(0, file.getName().lastIndexOf("."));
				BufferedReader fr   = new BufferedReader(new FileReader(file));
				StringBuffer   data = new StringBuffer();
				
				String token = fr.readLine();
				while (token != null){
					data.append(token);
					data.append("\r\n");
					token = fr.readLine();
				}
				fr.close();
				
				proxyLoader.loadFromFile(data.toString());				
				AbstractProgramObject currentObject = storage.get(uid);
				
				if (currentObject == null){
					AbstractProgramObject newObject = readFromString(proxyLoader.classType, data.toString());
					storage.put(uid, newObject);
				} else if (currentObject.classType.equals(proxyLoader.classType)){
					currentObject.loadFromFile(data.toString());
					usedUIDs.remove(uid);
				} else {
					AbstractProgramObject newObject = readFromString(proxyLoader.classType, data.toString());
					storage.put(uid, newObject);
					currentObject.cleanup();
				}
			} catch (Exception e) {
				Logger.getGlobal().severe("Could not load object for file "+ file+". "+ e);
			}
		}
		
		// Delete Unused Objects
		for (String toDeleteUID : usedUIDs){
			storage.get(toDeleteUID).cleanup();
			storage.remove(toDeleteUID);
		}
		
		// Fill NonDeltedItem Cache
		nonDelCache = new ArrayList<AbstractProgramObject>();
		for (AbstractProgramObject apo : storage.values())
			if (!apo.deleted)
				nonDelCache.add(apo);
		
		propagateNonDelCacheUpdate();
	}
	
	public static String normalizeLoc(String in){
		if (in.startsWith("/") && in.endsWith("/")){
			if (in.length()==1){
				return "";
			} else {
				return in.substring(1, in.length()-1);
			}
		} else if (in.startsWith("/")){
			return in.substring(1);
		} else if (in.endsWith("/")){
			return in.substring(0, in.length()-1);
		} 
		return in;
	}
	public static void moveAll(String sourceLocation, String targetLocation) {
		String src = normalizeLoc(sourceLocation);
		String append = src.substring(src.lastIndexOf("/")<0?0:src.lastIndexOf("/")+1);
		String tgt = normalizeLoc(targetLocation) +"/"+ append;
		
		long changecount =
				storage.values().stream()
							.filter(t -> t.location.contains(src))
							.peek(t -> t.location = t.location.replace(src, tgt).replace("//","/"))
							.peek(t -> t.saveIfNotOpened())
							.count();
							
		if (changecount > 0)
			propagateNonDelCacheUpdate();
	}
	
	
	private static AbstractProgramObject readFromString(String classType, String data) throws StoredObjectParseException{
		try {
			Class<? extends AbstractProgramObject> specificClass = typeClassCache.get(classType);
			if (specificClass == null){
				Class<?> specificClassG = Class.forName(classType);
				specificClass = ((AbstractProgramObject) specificClassG.newInstance()).getClass();
				typeClassCache.put(classType, specificClass);
			}
			
			AbstractProgramObject result = (AbstractProgramObject) specificClass.newInstance();
			result.loadFromFile(data);
			return result;
		} catch (ClassCastException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new StoredObjectParseException("Could not instantiate object", e);
		} 
	}
	

	

	public static AbstractProgramObject getUID(String uid) {
		return storage.get(uid);
	}



	private static class DummyLoaderProgramObject extends AbstractProgramObject{
		public String createDatabaseScript() {
			return null;
		}
	
		protected String specificDefinitionAsLineFile() {
			return null;
		}
	
		protected void specificInitFromLineFile(String data)
				throws StoredObjectParseException {
		}

		public Icon getIcon() {
			return null;
		}

		protected AbstractPOPanel<? extends AbstractProgramObject> createEditScreen() {
			return null;
		}
		protected void fillObjectInvalidStatusMessages(List<String> result) {
		}
	}



}
