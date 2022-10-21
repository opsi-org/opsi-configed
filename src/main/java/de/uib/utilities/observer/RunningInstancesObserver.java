/* 
 * Copyright (C) 2016 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
 
package de.uib.utilities.observer;
import de.uib.utilities.logging.*;
import de.uib.configed.*;


/**
 * Observing RunningInstaces 
 * 
 * @version 2016/01/15                     
 * @author Rupert Roeder
 */

public interface RunningInstancesObserver<T>
{
	public void executeCommandOnInstances(String command, java.util.Set<T> instances);
	
	public void instancesChanged(java.util.Set<T> instances);
	
}
	
	
	
