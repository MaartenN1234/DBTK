package mn.dbtk.config;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import mn.dbtk.programobjects.ProgramObjectStore;
import mn.dbtk.sql.SQLConnectionStatics;
import mn.dbtk.sql.dbcache.DBObjectCache;


public class Configuration {
	
	private static final String MarkingTokenDBService     = "DBService";
	private static final String MarkingTokenDBSchema      = "DBSchema";
	private static final String MarkingTokenConfiguration = "Configuration";	

	private static Configuration active = Configuration.EMPTY;
	
	public synchronized static Map<Long, DBService> getServices(){
		Map<Long, DBService> result = new HashMap<Long, DBService>();
		List<ConfigFile.IDSubTokenValueTriple> preParsed = ConfigFile.getPreParsed(MarkingTokenDBService);
		for (ConfigFile.IDSubTokenValueTriple triple : preParsed){
			if (!result.containsKey(triple.id))
				result.put(triple.id, new DBService(triple.id));
			
			if (triple.subToken.equals("name")){
				result.get(triple.id).name = triple.value;
			} else if (triple.subToken.equals("host")){
				result.get(triple.id).host = triple.value;
			} else if (triple.subToken.equals("port")){
				result.get(triple.id).port = Integer.parseInt(triple.value);
			} else if (triple.subToken.equals("service")){
				result.get(triple.id).service = triple.value;
			}	
		}
		return result;
	}
	public synchronized static void addService(DBService service){
		if (ConfigFile.exists(MarkingTokenDBService, service.id))
			return;		

		ConfigFile.put(MarkingTokenDBService, "name",    service.id, service.name);
		ConfigFile.put(MarkingTokenDBService, "host",    service.id, service.host);
		ConfigFile.put(MarkingTokenDBService, "port",    service.id, service.port);
		ConfigFile.put(MarkingTokenDBService, "service", service.id, service.service);
	}
	public synchronized static void removeService(DBService service){
		ConfigFile.remove(MarkingTokenDBService, service.id);
	}	
	
	public synchronized static Map<Long, DBSchema> getSchemas(){
		Map<Long, DBSchema>  result   = new HashMap<Long, DBSchema>();
		Map<Long, DBService> services = getServices();
		
		List<ConfigFile.IDSubTokenValueTriple> preParsed = ConfigFile.getPreParsed(MarkingTokenDBSchema);
		for (ConfigFile.IDSubTokenValueTriple triple : preParsed){
			if (!result.containsKey(triple.id))
				result.put(triple.id, new DBSchema(triple.id));
			
			if (triple.subToken.equals("name")){
				result.get(triple.id).name = triple.value;
			} else if (triple.subToken.equals("service")){
				result.get(triple.id).service = services.get(Long.parseLong(triple.value));
			} else if (triple.subToken.equals("user")){
				result.get(triple.id).user = triple.value;
			} else if (triple.subToken.equals("password")){
				result.get(triple.id).pass = triple.value;
			}	
		}
		
		return result;
	}
	public synchronized static void addSchema(DBSchema schema){
		if (ConfigFile.exists(MarkingTokenDBSchema, schema.id))
			return;		

		ConfigFile.put(MarkingTokenDBSchema, "name",    schema.id, schema.name);
		ConfigFile.put(MarkingTokenDBSchema, "service", schema.id, schema.service == null ? -1:schema.service.id);
		ConfigFile.put(MarkingTokenDBSchema, "user",    schema.id, schema.user);
		ConfigFile.put(MarkingTokenDBSchema, "password",schema.id, schema.pass);		
	}
	public synchronized static void removeSchema(DBSchema schema){
		ConfigFile.remove(MarkingTokenDBSchema, schema.id);
	}	
	public synchronized static Map<Long,Configuration> getConfigurations(){
		Map<Long, Configuration> result  = new HashMap<Long, Configuration>();
		Map<Long, DBSchema>      schemas = getSchemas();
		
		List<ConfigFile.IDSubTokenValueTriple> preParsed = ConfigFile.getPreParsed(MarkingTokenConfiguration);
		for (ConfigFile.IDSubTokenValueTriple triple : preParsed){
			if (!result.containsKey(triple.id))
				result.put(triple.id, new Configuration(triple.id));
			
			if (triple.subToken.equals("name")){
				result.get(triple.id).name = triple.value;
			} else if (triple.subToken.equals("schema")){
				result.get(triple.id).schema = schemas.get(Long.parseLong(triple.value));
			} else if (triple.subToken.equals("path")){
				result.get(triple.id).objectFilePath = triple.value;
			}	
		}
		
		return result;
	}
	public synchronized static void addConfiguration(Configuration configuration){
		if (ConfigFile.exists(MarkingTokenConfiguration, configuration.id))
			return;		

		ConfigFile.put(MarkingTokenConfiguration, "name",   configuration.id, configuration.name);
		ConfigFile.put(MarkingTokenConfiguration, "schema", configuration.id, configuration.schema == null ? -1 : configuration.schema.id);
		ConfigFile.put(MarkingTokenConfiguration, "path",   configuration.id, configuration.objectFilePath);
	}
	public synchronized static void removeConfiguration(Configuration configuration){
		ConfigFile.remove(MarkingTokenConfiguration, configuration.id);
	}	
	
	
	
	public static Configuration getActive(){
		String s = ConfigFile.data.map.get("ActiveConfiguration");
		if (s == null){
			active = EMPTY;
		} else {
			Long id = Long.parseLong(s);
			if (active == null || active.id != id){
				active = getConfigurations().get(id);
				if (active == EMPTY)
					Logger.getGlobal().warning("Active configuration not found.");
			}
		}
		
		return active;
	}
	public static void setActive(Configuration newActive){
		ConfigFile.data.map.put("ActiveConfiguration", "" + newActive.id);
		active = newActive;
	}
	public static void reload(){
		active.load();
	}	
	
	
	
	private synchronized static long genNewID(){
		String maxID  = ConfigFile.data.map.get("MAX_ID");
		maxID         = maxID == null ? "1000000" : maxID;
		long   nextID = Long.parseLong(maxID) + 1L;
		ConfigFile.data.map.put("MAX_ID", "" + nextID);
		return nextID;
	}
	
	
	public final static Configuration EMPTY = new Configuration(-1, "(new configuration)", DBSchema.EMPTY, "C:\\");
	
	final long id;
	public String   name;
	DBSchema schema;
	String   objectFilePath;

	private Configuration(long id){
		this(id, null, null, null);
	}
	public Configuration(String name, DBSchema schema, String objectFilePath){
		this(genNewID(), name, schema, objectFilePath);
	}	
	private Configuration(long id, String name, DBSchema schema, String objectFilePath){
		this.id      = id;
		this.name    = name;
		this.schema  = schema;
		this.objectFilePath = objectFilePath;
	}	
	public boolean equals(Object other){
		if (!(other instanceof Configuration))
			return false;
		Configuration ot = (Configuration) other;
		return id == ot.id;
	}	
	public String toString(){
		return name;
	}
	public boolean isFullyDefined(){
		return 	id != -1 &&
				name != null &&
				objectFilePath != null &&
				schema != null &&
					schema.name != null &&
					schema.user != null &&
					schema.pass != null &&
				schema.service != null &&
					schema.service.name != null &&
					schema.service.host != null &&
					schema.service.port != 0 &&
					schema.service.service != null;
	}
	public void writeToFile(String fileName, String data) {
		File f = new File(objectFilePath, fileName);
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(data);
			fw.close();
		} catch (IOException e) {
			Logger.getGlobal().severe("Could not write file "+fileName+ ". " + e);
		}
		
	}
	
	public void load(){
		if (isFullyDefined()){
			SQLConnectionStatics.setConnectionParameters(schema.service.host, schema.service.port, schema.service.service, schema.user, schema.pass);
			// Attempt Load DB objects in cache
			DBObjectCache.cache.invalidateAndReload();
			
			ProgramObjectStore.loadFromPath(new File(objectFilePath));

			Logger.getGlobal().config("Loaded object configuration: " + name + " with "+ProgramObjectStore.getNonDeletedItems().size()+" objects.");
		} else if (this == EMPTY){
			Logger.getGlobal().warning("Could not load empty configuration.");
		} else {
			Logger.getGlobal().warning("Configuration '" + name + "' is not fully defined.");
		}
	}
	
	
	static class DBSchema{
		public final static DBSchema EMPTY = new DBSchema(-1, "(new schema)", "EXAMPLE_USER", "EXAMPLE_USER", DBService.EMPTY);
		

		private DBSchema(long id){
			this(id, null, null, null, null);
		}			
		public DBSchema (String name, String user, String pass, DBService service){
			this(genNewID(), name, user, pass, service);
		}
		private DBSchema(long id, String name, String user, String pass, DBService service){
			this.id   = id;
			this.name = name;
			this.user = user;
			this.pass = pass;
			this.service = service;
		}			
		
		final long id;
		String name;
		DBService service;
		String user;
		String pass;
		public boolean equals(Object other){
			if (!(other instanceof DBSchema))
				return false;
			DBSchema ot = (DBSchema) other;
			return id == ot.id;
		}
		public String toString(){
			return name;
		}
	}
	

	static class DBService{
		public final static DBService EMPTY = new DBService(-1, "(new service)", "localhost", 1521, "XE");
		
		private DBService(long id){
			this(id, null, null, 0, null);
		}		
		public DBService(String name, String host, int port, String service) {
			this(genNewID(), name, host, port, service);
		}
		private DBService(long id, String name, String host, int port, String service) {
			this.id      = id;
			this.name    = name;
			this.host    = host;
			this.port    = port;
			this.service = service;
		}
		
		final long id;		
		String name;
		String host;
		int    port;
		String service;
		
		public boolean equals(Object other){
			if (!(other instanceof DBService))
				return false;
			DBService ot = (DBService) other;
			return id == ot.id;
		}
		
		public String toString(){
			return name;
		}
	}




}
