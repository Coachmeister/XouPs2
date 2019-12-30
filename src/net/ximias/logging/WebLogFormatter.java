package net.ximias.logging;

import sun.util.logging.LoggingSupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class WebLogFormatter extends Formatter {
	private final Date date = new Date();
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static final String format = LoggingSupport.getSimpleFormat();
	private static boolean verbose = false;
	
	@Override
	public String format(LogRecord recordIn) {
		TypedRecord record;
		if (!(recordIn instanceof TypedRecord)) {
			record = TypedRecord.toTypedRecord(recordIn);
		} else {
			record = (TypedRecord) recordIn;
		}
		date.setTime(record.getMillis());
		String source;
		if (record.getSourceClassName() != null) {
			source = verbose ? record.getSourceClassName() : record.getSourceClassName().substring(record.getSourceClassName().lastIndexOf('.') + 1);
			if (record.getSourceMethodName() != null) {
				source += " at " + record.getSourceMethodName();
			}
		} else {
			source = record.getLoggerName();
		}
		String message = formatMessage(record);
		
		String throwable = "";
		if (record.getThrown() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println();
			record.getThrown().printStackTrace(pw);
			pw.close();
			throwable = sw.toString();
		}
		
		return (
				dateFormatter.format(date) + " " +
				"[" + record.getCategory().getName().toUpperCase() + "] " +
				"[" + record.getLevel() + "]" + " " +
				"[" + source + "]" + " " +
				message + " " +
				throwable);
	}
}
