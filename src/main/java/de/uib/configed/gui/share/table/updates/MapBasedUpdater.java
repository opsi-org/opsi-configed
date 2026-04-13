/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.updates;

import java.util.Map;

public interface MapBasedUpdater {
	String sendUpdate(Map<String, Object> rowmap);

	boolean sendDelete(Map<String, Object> rowmap);
}
