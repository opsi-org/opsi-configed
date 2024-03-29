/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;

/**
 * Classes implementing this interface can check whether specific clients match
 * a certain condition. These are the only operation classes that can be used in
 * a search.
 */
public interface ExecutableOperation {
	/**
	 * Checks whether the client does match the given criteria. You may need to
	 * set the data first.
	 */
	boolean doesMatch(OpsiDataClient client);
}
