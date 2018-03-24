package net.ximias.persistence;

import java.io.*;
import java.util.logging.Logger;

class PersistLoader {
	private static Logger logger = Logger.getLogger(PersistLoader.class.getName());
	private static final String FILE_NAME = "applicationData.ser";
	private static Persisted instance;
	static Persisted getInstance(){
		if (instance == null) {
			unPersist();
			Runtime.getRuntime().addShutdownHook(new Thread(PersistLoader::persist));
		}
		return instance;
	}
	
	private static void unPersist() {
		logger.warning("Reading file: "+FILE_NAME);
		try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(FILE_NAME))){
			instance = (Persisted)oin.readObject();
		}catch (FileNotFoundException e){
			instance = instantiateDefault();
		} catch (IOException e) {
			logger.severe("Could not load saved data. Using defaults.");
			instance = instantiateDefault();
		} catch (ClassNotFoundException e) {
			logger.severe("WTF!: This error shouldn't happen. Unless you messed with the "+FILE_NAME+" file, some of the program is missing or saved data got corrupted: "+e);
			logger.severe("WTF!: I'll try to overwrite, and hope you just messed around with the "+FILE_NAME+" file.");
			instance = instantiateDefault();
		}
	}
	
	private static void persist() {
		logger.warning("Writing file: "+FILE_NAME);
		try(ObjectOutput oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
			oos.writeObject(instance);
		} catch (IOException e) {
			logger.severe("Error when trying to save application state to "+FILE_NAME+": "+e);
		}
	}
	
	private static Persisted instantiateDefault(){
		return new Persisted().defaults();
	}
}
