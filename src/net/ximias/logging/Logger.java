package net.ximias.logging;

import java.util.MissingResourceException;
import java.util.logging.LogRecord;

public class Logger extends java.util.logging.Logger {
	protected Logger(String name, String resourceBundleName) throws MissingResourceException {
		super(name, resourceBundleName);
	}
	
	public java.util.logging.Logger general(){
		return this;
	}
	
	public java.util.logging.Logger application(){
		return getTypedLogger(Category.APPLICATION);
	}
	
	public java.util.logging.Logger effects(){
		return getTypedLogger(Category.EFFECTS);
	}
	
	public java.util.logging.Logger network(){
		return getTypedLogger(Category.NETWORK);
	}
	
	public java.util.logging.Logger logAs(Category category){
		return getTypedLogger(category);
	}
	
	@Override
	public void log(LogRecord record) {
		TypedRecord ret = TypedRecord.toTypedRecord(record);
		ret.setCategory(Category.GENERAL);
		super.log(ret);
	}
	
	public static Logger getLogger(String name){
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
		return new Logger(logger.getName()+" #general",logger.getResourceBundleName());
	}
	
	
	private Logger getTypedLogger(Category category){
		if (category == Category.GENERAL) return this;
		return new Logger(getName()+" #"+category.getName(),getResourceBundleName()){
			@Override
			public void log(LogRecord record) {
				TypedRecord ret = TypedRecord.toTypedRecord(record);
				ret.setCategory(category);
				super.log(ret);
			}
		};
	}
}
