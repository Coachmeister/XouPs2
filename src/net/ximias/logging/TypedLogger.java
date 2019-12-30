package net.ximias.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TypedLogger {
	private final Logger logger;
	private final Category category;
	
	public TypedLogger(Logger logger, Category category) {
		this.logger = logger;
		this.category = category;
	}
	
	public void info(String message) {
		TypedRecord record = new TypedRecord(Level.INFO, message);
		record.setCategory(category);
		getCaller(record);
		logger.log(record);
	}
	
	public void warning(String message) {
		TypedRecord record = new TypedRecord(Level.WARNING, message);
		record.setCategory(category);
		getCaller(record);
		logger.log(record);
	}
	
	public void severe(String message) {
		TypedRecord record = new TypedRecord(Level.SEVERE, message);
		record.setCategory(category);
		getCaller(record);
		logger.log(record);
	}
	
	public void fine(String message) {
		TypedRecord record = new TypedRecord(Level.FINE, message);
		record.setCategory(category);
		getCaller(record);
		logger.log(record);
	}
	
	private void getCaller(LogRecord record) {
		
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		for (int i = 1; i < ste.length; i++) {
			String cname = ste[i].getClassName();
			
			if (cname.startsWith("java.lang.reflect.") || cname.startsWith("sun.reflect.") || isLogger(cname)) continue;
			record.setSourceClassName(ste[i].getClassName());
			record.setSourceMethodName(ste[i].getMethodName());
			return;
		}
	}
	
	private boolean isLogger(String className) {
		return (className.startsWith("net.ximias.logging"));
	}
}
