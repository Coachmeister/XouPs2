package net.ximias.persistence;

import java.io.*;
import net.ximias.logging.Logger;


/**
 * Used to save and restore persisted data as a serialized file or create one if needed.
 */
class PersistLoader {
	private static final Logger logger = Logger.getLogger(PersistLoader.class.getName());
	private static final String FILE_NAME = "applicationData.ser";
	private static Persisted instance;
	
	/**
	 * Used by persistence to bootstrap itself.
	 * @return an instance of Persistence. Either loaded or created with defaults.
	 */
	static Persisted getInstance(){
		if (instance == null) {
			unPersist();
			Runtime.getRuntime().addShutdownHook(new Thread(PersistLoader::persist));
		}
		return instance;
	}
	
	/**
	 * Load Persistence instance from file system.
	 * Create defaults if needed.
	 */
	private static void unPersist() {
		logger.application().warning("Reading file: "+FILE_NAME);
		try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(FILE_NAME))){
			instance = (Persisted)oin.readObject();
		}catch (FileNotFoundException e){
			logger.application().warning(FILE_NAME+" not found, creating a new one");
			instance = instantiateDefault();
		} catch (IOException e) {
			logger.application().severe("Could not load saved data. Using defaults: "+e);
			e.printStackTrace();
			instance = instantiateDefault();
		} catch (ClassNotFoundException e) {
			logger.general().severe("WTF!: This error shouldn't happen. Unless you messed with the "+FILE_NAME+" file, some of the program is missing or saved data got corrupted: "+e);
			logger.general().severe("WTF!: I'll try to overwrite, and hope you just messed around with the "+FILE_NAME+" file.");
			instance = instantiateDefault();
		}
	}
	
	/**
	 * Save instance to file system.
	 */
	private static void persist() {
		logger.application().warning("Writing file: "+FILE_NAME);
		try(ObjectOutput oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
			oos.writeObject(instance);
		} catch (IOException e) {
			logger.application().severe("Error when trying to save application state to "+FILE_NAME+": "+e);
		}
	}
	
	private static Persisted instantiateDefault(){
		return new Persisted().defaults();
	}
}
