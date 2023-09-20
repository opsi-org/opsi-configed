/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.EnumMap;
import java.util.Map;

/**
 * Provides methods for storing and retrieving cache data.
 * <p>
 * Intended usage of this class is to store and retrieve persistent data (i.e.
 * data that lives for a long time; usually for as long as the process does).
 * <p>
 * {@link CacheManager} is based on Singleton design pattern, meaning there can
 * only be one instance throughout the lifetime of the application. The
 * implementation of Singleton design pattern, that {@link CacheManager} uses is
 * thread-safe. You can learn about it more here:
 * https://refactoring.guru/design-patterns/singleton/java/example#example-2
 */
public final class CacheManager {
	// volatile keyword is required for double check lock to work. Although,
	// SonarLint rule makes a valid point.
	@SuppressWarnings({ "java:S3077" })
	private static volatile CacheManager instance;
	private Map<CacheIdentifier, Object> cache = new EnumMap<>(CacheIdentifier.class);

	private CacheManager() {
	}

	/**
	 * Retrieves the current instance of {@link CacheManager}.
	 * <p>
	 * {@link CacheManager} is based on a thread-safe Singleton design pattern:
	 * https://refactoring.guru/design-patterns/singleton/java/example#example-2
	 * 
	 * @return current instance of {@link CacheManager}
	 */
	public static CacheManager getInstance() {
		CacheManager result = instance;
		if (result != null) {
			return result;
		}
		synchronized (CacheManager.class) {
			if (instance == null) {
				instance = new CacheManager();
			}
			return instance;
		}
	}

	/**
	 * Internally cache data.
	 * 
	 * @param <T>        type of data to store internally in cache
	 * @param identifier for identifying stored cache data
	 * @param data       to store internally
	 */
	public <T> void setCachedData(CacheIdentifier identifier, T data) {
		cache.put(identifier, data);
	}

	/**
	 * Retrieve internally stored data.
	 * 
	 * @param <T>        type of data to retrieve from internal cache
	 * @param identifier for identifying stored cache data
	 * @param dataClass  class type of data, that is stored internally
	 * @return internally stored data
	 */
	public <T> T getCachedData(CacheIdentifier identifier, Class<T> dataClass) {
		return dataClass.cast(cache.get(identifier));
	}

	/**
	 * Clear some internally cached data based on {@link CacheIdentifier}.
	 * 
	 * @param identifier cache data to clear
	 */
	public void clearCachedData(CacheIdentifier identifier) {
		cache.remove(identifier);
	}
}
