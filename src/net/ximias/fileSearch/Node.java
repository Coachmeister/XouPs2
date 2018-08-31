package net.ximias.fileSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Representation of a directory to be searched through in the file system.
 */
public class Node {
	private final SearchProperties searchProperties;
	private final File nodeFile;
	private boolean hasExpandedPriority = false;
	private File goalFile;
	private TreeMap<Integer, ArrayList<Node>> priorityDirs = new TreeMap<>();
	private ArrayList<Node> dirs;
	
	/**
	 * Used as a representation of a directory to be searched through.
	 * @param nodeFile The File object representing the directory to search through.
	 * @param properties The search Properties. Containing priorities and exclusions.
	 *                   Use "filename" for exact match and "filename*" for contains operation. On priorities and exclusions.
	 */
	public Node(File nodeFile, SearchProperties properties) {
		this.searchProperties = properties;
		this.nodeFile = nodeFile;
	}
	
	/**
	 * Expands priority directories.
	 * @return true, if the goal has been reached.
	 */
	public boolean expandPriority(){
		if (hasExpandedPriority) return false;
		hasExpandedPriority = true;
		if (!populatePriority()) return false;
		if (goalFile !=null) return true;
		
		for (ArrayList<Node> nodes : priorityDirs.values()) {
			for (Node node : nodes) {
				if (node.expandPriority()) {
					goalFile = node.getGoalFile();
					return true;
				}
			}
		}
		priorityDirs = null;
		return false;
	}
	
	/**
	 * Populates priorityDirs with all directories matching the priority search property.
	 * @return true, if operation was successful.
	 */
	private boolean populatePriority() {
		File[] subFiles = nodeFile.listFiles();
		if (subFiles == null) return false;
		
		for (File file : subFiles) {
			if (file.isHidden()) continue;
			if (file.isDirectory()){
				addDirectoryByPriority(file);
			}else{
				if (file.getName().equals(searchProperties.goal)) {
					goalFile = file;
				}
			}
		}
		return true;
	}
	
	/**
	 * Expands none-priority nodes.
	 * @return true, if goal is found.
	 */
	public boolean expand(){
		dirs = new ArrayList<>(50);
		File[] subfiles = nodeFile.listFiles();
		if (subfiles != null) {
			for (File subfile : subfiles) {
				if (subfile.isHidden()) continue;
				if (subfile.isDirectory()){
					addDirectoryIfNotExcluded(subfile);
				}
			}
		}
		
		for (Node dir : dirs) {
			if (dir.hasPriority()){
				if (dir.expandPriority()){
					goalFile = dir.getGoalFile();
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Used for obtaining all children nodes, after expansion.
	 * @return All child nodes
	 */
	public ArrayList<Node> getAllChildren(){
		if (dirs == null) throw new IllegalStateException("expand must be called before getAllChrildren");
		return dirs;
	}
	
	/**
	 * Used to determine if expandPriority has already been called.
	 * @return true, if expandPriority has been called on this node before.
	 */
	public boolean hasPriority() {
		return !hasExpandedPriority;
	}
	
	
	private void addDirectoryByPriority(File subFile) {
		for (int i = 0; i < searchProperties.priorities.length; i++) {
			String priority = searchProperties.priorities[i];
			if (applyRegex(priority, subFile.getName())){
				addToMap(new Node(subFile,searchProperties), i);
			}
		}
	}
	
	private void addDirectoryIfNotExcluded(File subFile) {
		for (String exclusion : searchProperties.exclusions) {
			if (applyRegex(exclusion, subFile.getName())){
				return;
			}
		}
		dirs.add(new Node(subFile,searchProperties));
	}
	
	/**
	 * Initializes arraylist in map, and inserts node at location, if it is not already present.
	 * @param node the node to insert in the list.
	 * @param i the index to insert at.
	 */
	private void addToMap(Node node, int i) {
		priorityDirs.computeIfAbsent(i, k -> new ArrayList<>());
		if (priorityDirs.get(i).contains(node)) return;
		priorityDirs.get(i).add(node);
	}
	
	/**
	 * Used to apply the special kind of regex used for searching.
	 */
	private boolean applyRegex(String regex, String applicant){
		if (regex.endsWith("*")){
			return applicant.toLowerCase().contains(regex.substring(0,regex.length()-1).toLowerCase());
		}else return applicant.equalsIgnoreCase(regex);
		
	}
	
	/**
	 * used to obtain the goal file, if there is any.
	 * @return the goal, if found. Null otherwise.
	 */
	public File getGoalFile() {
		return goalFile;
	}
}
