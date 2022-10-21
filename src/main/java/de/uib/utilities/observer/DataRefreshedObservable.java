/* 
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */
 
package de.uib.utilities.observer;

public interface DataRefreshedObservable
{
	public void registerDataRefreshedObserver(DataRefreshedObserver ob);
	
	public void unregisterDataRefreshedObserver(DataRefreshedObserver ob);
	
	public void notifyDataRefreshedObservers(Object mesg);
	
}	
