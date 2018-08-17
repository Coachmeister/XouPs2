package net.ximias.datastructures.collections;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V>{
	private int cacheSize;
	
	/**
	 * @param cacheSize The maximum amount of elements in the cache.
	 */
	public LeastRecentlyUsedCache(int cacheSize) {
		super(cacheSize+1,1,true);
		this.cacheSize = cacheSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > cacheSize;
	}
}