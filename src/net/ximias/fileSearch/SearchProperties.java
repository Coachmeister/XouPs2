package net.ximias.fileSearch;

/**
 * Defines the properties of a search.
 */
public class SearchProperties {
	public final String goal;
	public final String[] exclusions;
	public final String[] priorities;
	public final int maxDepth;
	public final boolean isSearchExhaustive;
	
	/**
	 * @param goal               The file to locate, including extention.
	 * @param priorities         The directory names with precedence.
	 * @param exclusions         The directory names to be ignored in search.
	 * @param maxDepth           The maximum depth of non-prioritized directories to search through.
	 * @param isSearchExhaustive
	 */
	public SearchProperties(String goal, String[] priorities, String[] exclusions, int maxDepth, boolean isSearchExhaustive) {
		this.goal = goal;
		this.exclusions = exclusions;
		this.priorities = priorities;
		this.maxDepth = maxDepth;
		this.isSearchExhaustive = isSearchExhaustive;
	}
}
