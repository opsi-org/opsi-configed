/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import de.uib.configed.core.domain.datachanges.DefaultUpdateCollection;
import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.share.logging.Logging;

public final class UpdateCollectionManager {
	private static UpdateCollection mainUpdateCollection = new DefaultUpdateCollection();

	// private construction so this class will never be instantiated
	private UpdateCollectionManager() {
	}

	public static void addToGlobalUpdateCollection(UpdateCollection newCollection) {
		mainUpdateCollection.add(newCollection);
	}

	public static void removeFromGlobalUpdateCollection(UpdateCollection newCollection) {
		mainUpdateCollection.remove(newCollection);
	}

	public static void doCall() {
		mainUpdateCollection.doCall();
	}

	public static int getSizeOfGlobalUpdateCollection() {
		return mainUpdateCollection.size();
	}

	public static void clearGlobalUpdateCollection() {
		Logging.info("we clear the update collection ");
		mainUpdateCollection.clearElements();
	}

	public static void cancelGlobalUpdateCollection() {
		mainUpdateCollection.cancel();
	}
}
