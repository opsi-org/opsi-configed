/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

/**
 * Identifiers for internally cached data.
 * <p>
 * {@link CacheIdentifier} is also used in {@link DefaultDataReloadHandler}, for
 * reloading single data. Not all identifiers should have implementation for
 * realoding data, only those that are required. The reason is to reduce the
 * size of {@link DefaultDataReloadHandler} and for adherence to the principle
 * of YAGNI (You Aren't Gonna Need It). Also, the cached data is usually
 * reloaded using specific {@link ReloadHandler} implementations, that are
 * triggered by {@link ReloadEvent}.
 */
public enum CacheIdentifier {

}
