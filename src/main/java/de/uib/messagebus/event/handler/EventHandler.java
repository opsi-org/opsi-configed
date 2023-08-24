/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus.event.handler;

import java.util.Map;

public interface EventHandler {
	void handle(String event, Map<String, Object> eventData);
}
