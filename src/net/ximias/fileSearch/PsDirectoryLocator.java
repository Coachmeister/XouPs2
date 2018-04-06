package net.ximias.fileSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class PsDirectoryLocator implements Runnable{
	private static final String goal = "InputProfile_User.xml";
	private static final String[] priorities = {"planetside*", "common" , "steam*", "ps2", "soe*", "daybreak*", "program*", "game*", "users*", "desktop*"};
	private static final String[] exclusions = {"windows*", "driver*", "document*", "microsoft*", "intel*", "amd*", "nvidia*", "video*"};
	private static final int maxDepth = 2;
	private static final boolean isSearchExhaustive = true;
	private HashSet<File> goalFiles;
	private static PsDirectoryLocator locatorInstance;
	private Thread locatorThread;
	private final ArrayList<resultCallback> callbacks = new ArrayList<>(6);
	
	
	@Override
	public void run() {
		locatorThread = Thread.currentThread();
		Searcher searcher = new Searcher(new SearchProperties(goal, priorities, exclusions, maxDepth, isSearchExhaustive));
		goalFiles = searcher.performSystemWideSearch();
		callbacks.forEach(it->it.onCompleted(goalFiles));
	}
	
	public static PsDirectoryLocator getInstance(){
		if (locatorInstance == null) {
			locatorInstance = new PsDirectoryLocator();
		}
		return locatorInstance;
	}
	
	public synchronized void onFinished(resultCallback callback){
		callbacks.add(callback);
	}
	
	public HashSet<File> peekResult(){
		if (goalFiles != null) {
			return goalFiles;
		}
		return null;
	}
	
	public HashSet<File> getResult() throws InterruptedException {
		locatorThread.join();
		return goalFiles;
	}
}

