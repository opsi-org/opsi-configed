/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;

/**
 * Interface for providing specifc {@link ReloadHandler} implementation to
 * reload specific data, that is closely related or have to be reloaded
 * together.
 */
public interface ReloadHandler {
	/**
	 * Executes reloading processes of persistent data.
	 * <p>
	 * This method specifies what data and in what order it should be reloaded.
	 * 
	 * @param event that triggered this {@link ReloadHandler} (only used in
	 *              {@link DefaultDataReloadHandler} to reload different data
	 *              identified with {@link CacheIdentifier})
	 */
	void handle(String event);
}
