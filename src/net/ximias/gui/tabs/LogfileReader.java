package net.ximias.gui.tabs;

import net.ximias.datastructures.RandomAccessFileTextReader;
import net.ximias.gui.tabs.log.Filter;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class LogfileReader {
	private final Log log;
	RandomAccessFileTextReader fileReader;
	Thread lineReaderThread = new Thread("Log line reader thread");
	boolean softInterruptLineReader = false; // Needed because interrupt will close file stream.
	private LinkedBlockingDeque<Runnable> onCompletion = new LinkedBlockingDeque<>(10);
	
	private boolean topReached = false;
	
	private Filter filter;
	
	public LogfileReader(Log log, Filter filter) {
		this.log = log;
		this.filter = filter;
	}
	
	public void performOnCompletion(Runnable target) {
		onCompletion.addLast(target);
		if (!lineReaderThread.isAlive()) {
			runOnLineReaderThread(() -> {
			});
		}
	}
	
	private synchronized void runOnLineReaderThread(java.lang.Runnable target) {
		if (lineReaderThread != null && lineReaderThread.isAlive()) {
			interuptOldThread();
		}
		createNewThread(target);
	}
	
	private void interuptOldThread() {
		softInterruptLineReader = true;
		try {
			lineReaderThread.join();
		} catch (InterruptedException ignored) {
		
		}
	}
	
	private void createNewThread(Runnable target) {
		lineReaderThread = new Thread(() -> {
			if (isSoftInterupted()) return;
			target.run();
			execCompletionList().run();
		}, "Log line reader thread");
		softInterruptLineReader = false;
		lineReaderThread.start();
	}
	
	Runnable execCompletionList() {
		return () -> {
			Iterator<Runnable> iter = onCompletion.iterator();
			while (iter.hasNext()) {
				iter.next().run();
				iter.remove();
			}
		};
	}
	
	public synchronized void readFutureLinesOnLineReaderThread(Consumer<LinkedList<String>> consumer) {
		runOnLineReaderThread(() -> {
			consumer.accept(readFutureLines());
		});
	}
	
	public synchronized void readPastLinesOnLineReaderThread(Consumer<LinkedList<String>> consumer) {
		runOnLineReaderThread(() -> {
			consumer.accept(readPastLines());
		});
	}
	
	private LinkedList<String> readFutureLines() {
		LinkedList<String> result = new LinkedList<>();
		
		String target = log.getNewestLoadedMessage();
		topReached = false;
		if (target.isEmpty()) {
			bottomOfFileReached();
			return result;
		}
		
		try {
			if (isSoftInterupted()) return result;
			fileReader.binarySearchLine(target, Comparator.comparing(this::logTimestampComparator));
			
			for (int i = 0; i < Log.LINES_TO_READ; i++) {
				if (isSoftInterupted()) return result;
				
				if (!fileReader.hasNext()) {
					bottomOfFileReached();
					break;
				}
				String line = fileReader.readNextLine();
				if (filter.inFilter(line)) {
					result.addLast(line);
				} else {
					i--;
				}
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private LinkedList<String> readPastLines() {
		String target;
		LinkedList<String> result = new LinkedList<>();
		
		if (log.isPastMessagesEmpty()) {
			target = null;
		} else {
			target = log.getOldestLoadedMessage();
		}
		if (isSoftInterupted()) return result;
		
		try {
			if (target != null) {
				fileReader.binarySearchLine(target, Comparator.comparing(this::logTimestampComparator));
			} else {
				fileReader.seek(fileReader.endOfFile());
			}
			for (int i = 0; i < Log.LINES_TO_READ; i++) {
				if (isSoftInterupted()) return result;
				if (!fileReader.hasPrevious()) {
					System.out.println("Top reached");
					topReached = true;
					break;
				}
				String line = fileReader.readPreviousLine();
				if (filter.inFilter(line)) {
					result.addFirst(line);
				} else {
					i--;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean isSoftInterupted() {
		return softInterruptLineReader;
	}
	
	private String logTimestampComparator(String s) {
		if (!s.contains(Log.COMPARE_SUBSTRING_CHAR + "")) {
			return "0";
		}
		return s.substring(0, s.indexOf(Log.COMPARE_SUBSTRING_CHAR));
	}
	
	private void bottomOfFileReached() {
		topReached = false;
		log.bottomOfFileReached();
	}
	
	public boolean isTopReached() {
		return topReached;
	}
	
	public void setTopReached(boolean topReached) {
		this.topReached = topReached;
	}
}