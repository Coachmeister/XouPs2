package net.ximias.logging;

import javafx.application.Platform;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CollectionLogAppender extends Handler {
	Collection<String> collection;
	
	public CollectionLogAppender(Collection<String> collection) {
		this.collection = collection;
		WebLogFormatter formatter = new WebLogFormatter();
		setFormatter(formatter);
	}
	
	@Override
	public void publish(LogRecord record) {
		Platform.runLater(() -> collection.add(getFormatter().format(record)));
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() throws SecurityException {
	}
}
