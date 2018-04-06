package net.ximias.fileSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class Searcher {
	private SearchProperties properties;
	private int currentDepth = 0;
	public Searcher(SearchProperties properties) {
		this.properties = properties;
	}
	
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
