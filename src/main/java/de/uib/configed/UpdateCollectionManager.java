/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utils.logging.Logging;

public final class UpdateCollectionManager {
	private static UpdateCollection updateCollection = new UpdateCollection();

	// private construction so this class will never be instantiated
	private UpdateCollectionManager() {
	}

	public static void addToGlobalUpdateCollection(UpdateCollection newCollection) {
		updateCollection.add(newCollection);
	}

	public static void removeFromGlobalUpdateCollection(UpdateCollection newCollection) {
		updateCollection.remove(newCollection);
	}

	public static void doCall() {
		updateCollection.doCall();
	}

	public static int getSizeOfGlobalUpdateCollection() {
		return updateCollection.size();
	}

	public static void clearGlobalUpdateCollection() {
		Logging.info("we clear the update collection ");
		updateCollection.clear();
	}

	public static void cancelGlobalUpdateCollection() {
		updateCollection.cancel();
	}
}
