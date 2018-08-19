package net.ximias.datastructures.collections;

import net.ximias.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V>{
	private int cacheSize;
	Logger logger = Logger.getLogger(getClass().getName());
	/**
	 * @param cacheSize The maximum amount of elements in the cache.
	 */
	public LeastRecentlyUsedCache(int cacheSize) {
		super(cacheSize+1,1,true);
		this.cacheSize = cacheSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		boolean remove = size() > cacheSize;
		if (remove) logger.network().warning("Cache at capacity.");
		return remove;
	}
}