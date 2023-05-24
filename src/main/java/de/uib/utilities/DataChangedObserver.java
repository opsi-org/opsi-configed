/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
  * DataChangedObserver 
	* description: Implements an interface that receives notifications that some data have changed
	* organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.utilities;

public interface DataChangedObserver {
	void dataHaveChanged(Object source);
}
