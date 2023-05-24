/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
  * LogEventObserver 
	* description: Implements an interface that receives notifications that a log event occurred
	* organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.utilities.logging;

public interface LogEventObserver {
	void logEventOccurred();
}
