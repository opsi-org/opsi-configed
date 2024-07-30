/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.observer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.utils.logging.Logging;

/**
 * Allows to produce a static member of a class in order to count instances of
 * that class and check for running instances on ending the program.
 */

public class RunningInstances<T> {
	private String classname;
	private String askForLeave;

	// collect instances of a class in this map
	private Map<T, String> instances;

	public RunningInstances(Class<?> type, String askForLeave) {
		this.classname = type.getName();
		this.askForLeave = askForLeave;
		Logging.info(this.getClass(), "created for class ", classname);
		instances = new ConcurrentHashMap<>();
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
		return instances.size();
	}

	public boolean isEmpty() {
		boolean result = instances.isEmpty();
		Logging.info(this, " isEmpty ", result);
		return result;
	}

	public boolean askStop() {
		int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), askForLeave,
				Configed.getResourceValue("ConfigedMain.Licenses.AllowLeaveApp.title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		return returnedOption == JOptionPane.YES_OPTION;
	}

	@Override
	public String toString() {
		return instances.toString();
	}

	private void sendChangeEvent() {
		Logging.debug(this, "sendChangeEvent to mainFrame");
		if (ConfigedMain.getMainFrame() != null) {
			ConfigedMain.getMainFrame().instancesChanged(getAll());
		}
	}
}
