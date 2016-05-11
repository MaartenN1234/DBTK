package mn.dbtk.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class ConfigFile {
	public final static String filename = System.getProperty("user.dir") + "\\dbtkConfigFile.bin";
	public final static boolean NO_CRYPT = true;
	
	public static ConfigFile data = new ConfigFile();
	
	Map<String, String> map;
	
	private ConfigFile(){
		initFromFile();
	}
	
	public void initFromFile(){
		try{
			readFromFile(new File(filename));
		}  catch (FileNotFoundException e){
			try{
				map = new HashMap<String, String>();	
				writeToFile(new File(filename));
				Logger.getGlobal().config("No configuration file found, a new one was created.");
			} catch(IOException e2) {
				Logger.getGlobal().severe("Could neither read or write to configuration file.\r\nRead exception: " + e +"\r\nWrite exception: "+e2);
			}
			map = new HashMap<String, String>();			
		}   catch(IOException | ClassNotFoundException e){
			Logger.getGlobal().warning("Could not load configuration file. " + e);
			map = new HashMap<String, String>();
		}
	}
	public void materializeAsFile(){
		try{
			writeToFile(new File(filename));
		} catch(Exception e){
			Logger.getGlobal().severe("Could write to configuration file. " + e);
		}
	}	
	
	private void readFromFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		Object o = ois.readObject();
		ois.close();
		if (o instanceof Map){
			Map<?,?>            sourceMap = (Map<?,?>) o;
			Map<String, String> resultMap = new HashMap<String, String>();
			for (Object key : sourceMap.keySet()){
				if (key instanceof String){
					Object value = sourceMap.get(key);
					if (value == null || value instanceof String){
						resultMap.put((String) key, (String) value);
					} else {
						throw new ConfigFileReadException("Value for key " + key + " is not an instance of String");
					}
				} else {
					throw new ConfigFileReadException("Key " + key + " is not an instance of String");
				}
			}
			cryptographyOnPasswords(resultMap, Cipher.DECRYPT_MODE);
			map = resultMap;
		} else {
			throw new ConfigFileReadException("No object map is found");
		}
	}
	


	private void writeToFile(File file) throws FileNotFoundException, IOException{
		Map<String, String> tempMap = new HashMap<String, String>(map);
		cryptographyOnPasswords(tempMap, Cipher.ENCRYPT_MODE);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(tempMap);
		oos.close();
	}
	
	private void cryptographyOnPasswords(Map<String, String> map, int mode) {
		// shortcut fix do no cryptography
		if (NO_CRYPT)
			return;

		Cipher cipher          = null;
		SecretKeySpec keySpec  = null;
	    IvParameterSpec ivspec = new IvParameterSpec(new byte[]{-78, -85, -125, -19, -91, 126, 123, -37, 44, -6, -57, -41, -31, -20, -83, 50});
		try{
			keySpec = new SecretKeySpec(new byte []{41, -62, -117, -55, -18, 93, 24, 84, -66, 92, 113, -99, -119, -84, -92, -73}, "AES");
		} catch(Exception e){
			throw new RuntimeException(e);
		}	
		try{
			cipher  = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch(Exception e){
			throw new RuntimeException(e);
		}		
		
		for (String key : map.keySet())
			if (key.toLowerCase().contains("password"))
				try{
					String password   = map.get(key);
				
					byte[] encryptIn  = password.getBytes();
					cipher.init(mode, keySpec, ivspec);
					byte[] encryptOut = cipher.doFinal(encryptIn);

				
					String transformedPassWord = new String(encryptOut);
					map.put(key, transformedPassWord);	
				} catch(Exception e){
					map.put(key, "");
					Logger.getGlobal().severe("Could not decrypt stored password "+e);
				}
	}

	static void put(String mainToken, String subToken, long id, long id2) {
		put(mainToken, subToken, id, ""+ id2);
	}
		
	static void put(String mainToken, String subToken, long id, String value) {
		data.map.put(mainToken + "." + subToken + "." + id, value);
	}
	static boolean exists(String mainToken, long id) {
		final String idEnd = ""+id; 
		for (String s : new ArrayList<String>(data.map.keySet()))
			if (s.startsWith(mainToken) && s.endsWith(idEnd))
				return true;
		
		return false;
	}
	
	static void remove(String mainToken, long id) {
		final String idEnd = ""+id; 
		for (String s : new ArrayList<String>(data.map.keySet()))
			if (s.startsWith(mainToken) && s.endsWith(idEnd))
				data.map.remove(s);
	}
	
	static List<IDSubTokenValueTriple> getPreParsed(String mainToken){
		List<IDSubTokenValueTriple> result = new ArrayList<IDSubTokenValueTriple>();		
		for (String s : data.map.keySet()){
			if (s.startsWith(mainToken)){
				Long id = Long.parseLong(s.substring(s.lastIndexOf('.')+1));
				String subToken = s.substring(s.indexOf('.')+1, s.lastIndexOf('.'));
				String value = ConfigFile.data.map.get(s);
				result.add(new IDSubTokenValueTriple(id, subToken, value));
			}
		}
		return result;
	}
	static class IDSubTokenValueTriple{
		final long   id;
		final String subToken;
		final String value;
		IDSubTokenValueTriple(long id, String subToken, String value){
			this.id       = id;
			this.subToken = subToken;
			this.value    = value;
		}
	}	
	
	
	static class ConfigFileReadException extends IOException{
		private static final long serialVersionUID = -5773833500683630080L;
		private ConfigFileReadException(){}
		private ConfigFileReadException(String message){
			super(message);
		}
	}
}
