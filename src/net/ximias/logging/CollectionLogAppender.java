package net.ximias.logging;

import javafx.application.Platform;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CollectionLogAppender extends Handler {
	LinkedList<CollectionLogReciever> collection;
	
	public CollectionLogAppender(CollectionLogReciever collection) {
		this.collection = new LinkedList<>();
		this.collection.add(collection);
		WebLogFormatter formatter = new WebLogFormatter();
		setFormatter(formatter);
	}
	
	@Override
	public void publish(LogRecord record) {
		collection.forEach(it->it.receiveMessage(getFormatter().formatMessage(record)));
	}
	
	@Override
	public void flush() {
	}
	
	@Override
	public void close() throws SecurityException {
	}
}
