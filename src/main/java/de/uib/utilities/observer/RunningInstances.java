/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */


package de.uib.utilities.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

/**
 * Allows to produce a static member of a class in order to count instances of
 * that class and check for running instances on ending the program. Implements
 * an observable pattern so that another class can react to relevant events or
 * execute something on all instances
 */

public class RunningInstances<T> {

	private String classname;
	private String askForLeave;

	// collect instances of a class in this map
	private ConcurrentHashMap<T, String> instances;

	// the observers
	private List<RunningInstancesObserver<T>> observers;

	public RunningInstances(Class<?> type, String askForLeave) {
		this.classname = type.getName();
		this.askForLeave = askForLeave;
		Logging.info(this, "created for class " + classname);
		instances = new ConcurrentHashMap<>();
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
		Logging.info(this, " isEmpty " + result);
		return result;
	}

	public boolean askStop() {
		int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(), askForLeave,
				Globals.APPNAME + " " + Configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		return returnedOption == JOptionPane.YES_OPTION;
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

	private void sendChangeEvent() {
		Logging.debug(this, "sendChangeEvent observers: " + observers.size());
		for (RunningInstancesObserver<T> aFollower : observers) {
			aFollower.instancesChanged(getAll());
		}

	}

}
