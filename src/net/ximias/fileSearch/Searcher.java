package net.ximias.fileSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

public class Searcher {
	private SearchProperties properties;
	private int currentDepth = 0;
	public Searcher(SearchProperties properties) {
		this.properties = properties;
	}
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public HashSet<File> performSystemWideSearch(){
		File[] roots = File.listRoots();
		Node[] rootNodes = new Node[roots.length];
		ArrayList<Node> bredth = new ArrayList<>(200);
		for (int i = 0; i < roots.length; i++) {
			File root = roots[i];
			rootNodes[i] = new Node(root, properties);
		}
		
		bredth.addAll(Arrays.asList(rootNodes));
		
		HashSet<File> result = new HashSet<>(32);
		searchToList(bredth, result);
		return result;
	}
	
	public File singleSearch(File dir) {
		if (dir.getName().equals(properties.goal)) return dir;
		ArrayList<Node> breadth = new ArrayList<>();
		breadth.add(new Node(dir, properties));
		
		while (!breadth.isEmpty()) {
			for (Node rootNode : breadth) {
				if (rootNode.hasPriority()) {
					if (rootNode.expandPriority()) {
						return (rootNode.getGoalFile());
					}
				}
			}
			
			for (Node rootNode : breadth) {
				if (rootNode.expand()) {
					return (rootNode.getGoalFile());
				}
			}
			ArrayList<Node> next = new ArrayList<>(200);
			for (Node node : breadth) {
				next.addAll(node.getAllChildren());
			}
			currentDepth++;
			if (currentDepth > properties.maxDepth) return null;
			logger.info("No result in prioritized directories. Expanded search to: " + next.size() + " directories.");
			breadth = next;
		}
		logger.warning("Single search found no result");
		return null;
	}
	
	private void searchToList(ArrayList<Node> breadth, Collection<File> result){
		while (!breadth.isEmpty()){
			for (Node rootNode : breadth) {
				if (rootNode.hasPriority()){
					if (rootNode.expandPriority()){
						result.add(rootNode.getGoalFile());
					}
				}
			}
			
			for (Node rootNode : breadth) {
				if (rootNode.expand()){
					result.add(rootNode.getGoalFile());
				}
			}
			ArrayList<Node> next = new ArrayList<>(200);
			for (Node node : breadth) {
				next.addAll(node.getAllChildren());
			}
			currentDepth++;
			if (currentDepth>properties.maxDepth) return;
			if (!properties.isSearchExhaustive && !result.isEmpty()) return;
			System.out.println("No result in prioritized directories. Expanded search to: "+next.size()+" directories.");
			breadth = next;
		}
	}
}
