package net.ximias.logging;

import java.util.MissingResourceException;
import java.util.logging.LogRecord;

public class Logger {
	private final java.util.logging.Logger logger;
	
	protected Logger(java.util.logging.Logger logger) throws MissingResourceException {
		this.logger = logger;
	}
	
	public TypedLogger general(){
		return getTypedLogger(Category.GENERAL);
	}
	
	public TypedLogger application(){
		return getTypedLogger(Category.APPLICATION);
	}
	
	public TypedLogger effects(){
		return getTypedLogger(Category.EFFECTS);
	}
	
	public TypedLogger network(){
		return getTypedLogger(Category.NETWORK);
	}
	
	public static Logger getLogger(String name){
		return new Logger(java.util.logging.Logger.getLogger(name));
	}
	
	private TypedLogger getTypedLogger(Category category){
		return new TypedLogger(logger, category);
	}
}
