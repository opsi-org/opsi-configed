/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.reload.handler;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.dataservice.DataServices;
import de.uib.configed.core.domain.serverdata.reload.ReloadDispatcher;

/**
 * Abstract Class for providing specifc {@link AbstractReloadHandler}
 * implementation to reload specific data, that is closely related or have to be
 * reloaded together.
 * <p>
 * Every {@link AbstractReloadHandler} implementation <b>must</b> be registered
 * in {@link ReloadDispatcher} in order for reloading to work.
 * {@link AbstractReloadHandler} registration is done in
 * {@link OpsiServiceNOMPersistenceController}.
 * <p>
 * {@link AbstractReloadHandler} is based on event bus design pattern.
 */
public abstract class AbstractReloadHandler {
	protected DataServices dataServices;

	AbstractReloadHandler(DataServices dataServices) {
		this.dataServices = dataServices;
	}

	/**
	 * Executes reloading processes of persistent data.
	 * <p>
	 * This method specifies what data and in what order it should be reloaded.
	 * 
	 * @param event that triggered this {@link AbstractReloadHandler} (only used
	 *              in {@link DefaultDataReloadHandler} to reload different data
	 *              identified with {@link CacheIdentifier})
	 */
	public abstract void handle(String event);
}
