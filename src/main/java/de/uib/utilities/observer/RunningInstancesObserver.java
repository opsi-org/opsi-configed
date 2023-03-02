/* 
 * Copyright (C) 2016 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.utilities.observer;

import java.util.Set;

/**
 * Observing RunningInstaces
 * 
 * @version 2016/01/15
 * @author Rupert Roeder
 */

public interface RunningInstancesObserver<T> {
	void executeCommandOnInstances(String command, Set<T> instances);

	void instancesChanged(Set<T> instances);
}
