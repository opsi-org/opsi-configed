/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * MapBasedUpdater.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.Map;

public interface MapBasedUpdater {
	String sendUpdate(Map<String, Object> rowmap);

	boolean sendDelete(Map<String, Object> rowmap);
}
