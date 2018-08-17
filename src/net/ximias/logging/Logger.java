package net.ximias.logging;

import java.util.MissingResourceException;
import java.util.logging.LogRecord;

public class Logger extends java.util.logging.Logger {
	protected Logger(String name, String resourceBundleName) throws MissingResourceException {
		super(name, resourceBundleName);
	}
	
	public enum Categories{
		GENERAL("general"),
		APPLICATION("application"),
		EFFECTS("effects"),
		NETWORK("network");
		
		private String name;
		Categories(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public java.util.logging.Logger general(){
		return this;
	}
	
	public java.util.logging.Logger application(){
		return getTypedLogger(Categories.APPLICATION);
	}
	
	public java.util.logging.Logger effects(){
		return getTypedLogger(Categories.EFFECTS);
	}
	
	public java.util.logging.Logger network(){
		return getTypedLogger(Categories.NETWORK);
	}
	
	@Override
	public void log(LogRecord record) {
		record.setMessage("[general] "+record.getMessage());
		System.out.println("log: "+record.getMessage());
		super.log(record);
	}
	
	public static Logger getLogger(String name){
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		return new Logger(logger.getName()+" #general",logger.getResourceBundleName());
	}
	
	
	public Logger getTypedLogger(Categories category){
		return new Logger(getName()+" #"+category,getResourceBundleName()){
			@Override
			public void log(LogRecord record) {
				record.setMessage("["+category.getName()+"] "+record.getMessage());
				super.log(record);
			}
		};
	}
}
