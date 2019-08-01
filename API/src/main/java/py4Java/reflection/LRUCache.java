/*
 * Copyright (c) 2010-2019, sikuli.org, sikulix.com - MIT license
 */
package py4Java.reflection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Implementation of a Least Recently Used cache. Currently used by the
 * ReflectionEngine to cache resolution of Java members.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	public static final int DEFAULT_CACHE_SIZE = 100;
	private static final long serialVersionUID = -3090703237387586885L;
	private int cacheSize;

	public LRUCache() {
		this(DEFAULT_CACHE_SIZE);
	}

	public LRUCache(int cacheSize) {
		super(16, 0.75f, true);
		this.cacheSize = cacheSize;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > cacheSize;
	}

}
