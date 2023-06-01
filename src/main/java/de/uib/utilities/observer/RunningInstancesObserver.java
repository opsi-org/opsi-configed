/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.observer;

import java.util.Set;

public interface RunningInstancesObserver<T> {
	void executeCommandOnInstances(String command, Set<T> instances);

	void instancesChanged(Set<T> instances);
}
