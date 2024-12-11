/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import de.uib.opsidatamodel.datachanges.DefaultUpdateCollection;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utils.logging.Logging;

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
		mainUpdateCollection.clear();
	}

	public static void cancelGlobalUpdateCollection() {
		mainUpdateCollection.cancel();
	}
}
