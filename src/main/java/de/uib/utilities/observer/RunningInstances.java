/* 
 * Copyright (C) 2016 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.utilities.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

/**
 * Allows to produce a static member of a class in order to count instances of
 * that class and check for running instances on ending the program. Implements
 * an observable pattern so that another class can react to relevant events or
 * execute something on all instances
 * 
 * @version 2016/01/15
 * @author Rupert Roeder
 */

public class RunningInstances<T> {
	// private static AtomicInteger objectCounting = new AtomicInteger();
	// private static boolean allStopped = false;

	private Boolean reallyLeave = null;
	private String classname;
	private String askForLeave;

	// collect instances of a class in this map
	private ConcurrentHashMap<T, String> instances;

	// the observers
	private List<RunningInstancesObserver<T>> observers;

	public RunningInstances(Class type, String askForLeave) {
		this.classname = type.getName();
		this.askForLeave = askForLeave;
		logging.info(this, "created for class " + classname);
		instances = new ConcurrentHashMap<T, String>();
		observers = new ArrayList<>();

	}

	public void add(T instance, String description) {
		instances.put(instance, description);
		sendChangeEvent();
	}

	public void forget(T instance) {
		instances.remove(instance);
		sendChangeEvent();
	}

	public Set<T> getAll() {
		return instances.keySet();
	}

	public int size() {
		return instances.keySet().size();
	}

	public boolean isEmpty() {
		boolean result = instances.keySet().isEmpty();
		logging.info(this, " isEmpty " + result);
		return result;
	}

	public boolean askStop() {
		reallyLeave = false;
		int returnedOption = JOptionPane.NO_OPTION;
		returnedOption = JOptionPane.showOptionDialog(Globals.mainFrame, askForLeave,
				Globals.APPNAME + " " + configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (returnedOption == JOptionPane.YES_OPTION)
			reallyLeave = true;
		else
			reallyLeave = false;

		return reallyLeave;
	}

	@Override
	public String toString() {
		return instances.toString();
	}

	// observable implementation
	public void addObserver(RunningInstancesObserver<T> observer) {
		observers.add(observer);
	}

	public void removeObserver(RunningInstancesObserver<T> observer) {
		observers.remove(observer);
	}

	protected void sendChangeEvent() {
		logging.debug(this, "sendChangeEvent observers: " + observers.size());
		for (RunningInstancesObserver aFollower : observers) {
			aFollower.instancesChanged(getAll());
		}

	}

}
