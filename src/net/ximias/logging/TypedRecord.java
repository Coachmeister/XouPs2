package net.ximias.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TypedRecord extends LogRecord {
	Category category = Category.GENERAL;
	/**
	 * Construct a LogRecord with the given level and message values.
	 * <p>
	 * The sequence property will be initialized with a new unique value.
	 * These sequence values are allocated in increasing order within a VM.
	 * <p>
	 * The millis property will be initialized to the current time.
	 * <p>
	 * The thread ID property will be initialized with a unique ID for
	 * the current thread.
	 * <p>
	 * All other properties will be initialized to "null".
	 *  @param level a logging level value
	 * @param msg   the raw non-localized logging message (may be null)
	 */
	public TypedRecord(Level level, String msg) {
		super(level, msg);
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public static TypedRecord toTypedRecord(LogRecord record){
		TypedRecord ret = new TypedRecord(record.getLevel(), record.getMessage());
		ret.setMillis(record.getMillis());
		ret.setParameters(record.getParameters());
		ret.setLoggerName(record.getLoggerName());
		ret.setSequenceNumber(record.getSequenceNumber());
		ret.setSourceClassName(record.getSourceClassName());
		ret.setSourceMethodName(record.getSourceMethodName());
		ret.setResourceBundle(record.getResourceBundle());
		ret.setThreadID(record.getThreadID());
		ret.setResourceBundleName(record.getResourceBundleName());
		return ret;
	}
}
