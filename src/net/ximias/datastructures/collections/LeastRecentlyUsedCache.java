package net.ximias.datastructures.collections;

import net.ximias.logging.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V> {
	private final int cacheSize;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * @param cacheSize The maximum amount of elements in the cache.
	 */
	public LeastRecentlyUsedCache(int cacheSize) {
		super(cacheSize + 1, 1, true);
		this.cacheSize = cacheSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		boolean remove = size() > cacheSize;
		if (remove) {
			if (eldest.getValue() instanceof Future) {
				try {
					//TODO Remove this mess.
					logger.network().warning("Cache at capacity. Evicting: " + ((Future) eldest.getValue()).get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			} else {
				logger.network().warning("Cache at capacity. Evicting: " + eldest.getValue());
			}
		}
		return remove;
	}
}