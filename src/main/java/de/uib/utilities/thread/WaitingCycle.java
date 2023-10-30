/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.thread;

import javax.swing.SwingWorker;

import de.uib.utilities.logging.Logging;
import utils.Utils;

public class WaitingCycle extends SwingWorker<Void, Integer> {
	private int maxWaitSecs;

	private boolean stopped;
	private boolean timeoutReached;

	public WaitingCycle(int maxWaitSecs) {
		this.maxWaitSecs = maxWaitSecs;
		timeoutReached = false;
	}

	@Override
	public Void doInBackground() {
		int waitSecs = 0;

		long startActionMillis = System.currentTimeMillis();
		Logging.info(this, " doInBackground start " + startActionMillis);

		while (!timeoutReached && !stopped) {
			Logging.debug(this, " WaitingCycle waits signal " + waitSecs);
			// === serves like an external task
			waitSecs++;
			Utils.threadSleep(this, 1000);

			setProgress(100 * waitSecs / maxWaitSecs);

			publish(waitSecs);

			timeoutReached = waitSecs > maxWaitSecs;
		}

		Logging.info(this, " doInBackground finish time in millis " + System.currentTimeMillis());

		Logging.info(this, " doInBackground finished:  stopped, waitSecs " + stopped + ", " + waitSecs);

		if (timeoutReached) {
			Logging.warning(this, " doInBackground finished, timeoutReached");
		}

		return null;
	}

	//
	// Executed in event dispatching thread
	//
	@Override
	public void done() {
		Logging.info(this, "done resp. is stopped ");
	}

	// own methods

	public void stop() {
		Logging.info(this, "stop");
		stopped = true;
		cancel(true);
	}
}
