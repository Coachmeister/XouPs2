package net.ximias.logging;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FileLogAppender extends Handler {
	private final RandomAccessFile logFile;
	public static final Charset UTF16 = Charset.forName("UTF16");
	@SuppressWarnings("ResultOfMethodCallIgnored") // File stuff returns a boolean.
	public FileLogAppender() {
		WebLogFormatter formatter = new WebLogFormatter();
		setFormatter(formatter);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH-mm");
		File log = new File("logs/" + dateFormatter.format(new Date()) + ".log");
		log.getParentFile().mkdirs();
		File[] logs = log.getParentFile().listFiles();
		while (Objects.requireNonNull(logs).length > 5) {
			//noinspection OptionalGetWithoutIsPresent Check performed above.
			Arrays.stream(logs).min(Comparator.comparing(File::getName)).get().delete();
			logs = log.getParentFile().listFiles();
		}
		try {
			log.createNewFile();
			logFile = new RandomAccessFile(log, "rw");
		} catch (IOException e) {
			throw new Error("Could not create log file: " + e);
		}
		
	}
	
	@Override
	public void publish(LogRecord record) {
		try {
			synchronized (logFile){
				logFile.seek(logFile.length());
				
				ByteBuffer buf = UTF16.encode(getFormatter().format(record) + '\n');
				
				//System.out.println(new String(UTF16.decode(buf).array()));
				while (buf.hasRemaining()){
					logFile.write(buf.get());
				}
			}
			
		} catch (IOException e) {
			throw new Error("Could not append to log file: "+e);
		}
		
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() throws SecurityException {
	}
	
	@Override
	protected void finalize() throws Throwable {
		// Because the logFile may be in use elsewhere even after close.
		logFile.close();
	}
	
	public RandomAccessFile getLogFile() {
		return logFile;
	}
}
