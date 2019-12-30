package net.ximias.fileSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Used for finding the InputProfile_User.xml file. Can be expanded with other goals at a later stage
 * Holds functions for performing an informed search through the file system.
 */

public class PsDirectoryLocator implements Runnable {
	private static final String goal = "InputProfile_User.xml";
	private static final String[] priorities = {"planetside*", "common", "steam*", "ps2", "soe*", "daybreak*", "program*", "game*", "users*", "desktop*"};
	private static final String[] exclusions = {"windows*", "driver*", "document*", "microsoft*", "intel*", "amd*", "nvidia*", "video*"};
	private static final int maxDepth = 2;
	private static final boolean isSearchExhaustive = true;
	private HashSet<File> goalFiles;
	private static PsDirectoryLocator locatorInstance;
	private Thread locatorThread;
	private final ArrayList<ResultCallback> callbacks = new ArrayList<>(6);
	
	/**
	 * Runs a system-wide search.
	 */
	@Override
	public void run() {
		locatorThread = Thread.currentThread();
		Searcher searcher = new Searcher(new SearchProperties(goal, priorities, exclusions, maxDepth, isSearchExhaustive));
		goalFiles = searcher.performSystemWideSearch();
		callbacks.forEach(it -> it.onCompleted(goalFiles));
	}
	
	/**
	 * Used to find a goal file within a directory.
	 *
	 * @param dir the directory to search through.
	 * @return The goal file, if found. Null otherwise.
	 */
	public File locateInSubDirectory(File dir) {
		Searcher searcher = new Searcher(new SearchProperties(goal, priorities, exclusions, 10, false));
		return searcher.singleSearch(dir);
	}
	
	public static PsDirectoryLocator getInstance() {
		if (locatorInstance == null) {
			locatorInstance = new PsDirectoryLocator();
		}
		return locatorInstance;
	}
	
	/**
	 * Allows users to subscribe to an event when the search has been finished.
	 *
	 * @param callback the event listener class. Of type ResultCallback
	 */
	public synchronized void onFinished(ResultCallback callback) {
		callbacks.add(callback);
	}
	
	/**
	 * Peeks the result. This is a non-blocking operation. If the search has not completed, will return null.
	 *
	 * @return The goal if search has completed. null otherwise.
	 */
	public HashSet<File> peekResult() {
		if (goalFiles != null) {
			return goalFiles;
		}
		return null;
	}
	
	/**
	 * Used to obtain the result of the search. This is a blocking operation.
	 *
	 * @return The result of the search.
	 * @throws InterruptedException If thread was interrupted while blocking.
	 */
	public HashSet<File> getResult() throws InterruptedException {
		locatorThread.join();
		return goalFiles;
	}
}

