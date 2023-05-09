package de.uib.utilities.thread;

import javax.swing.SwingWorker;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class WaitingCycle extends SwingWorker<Void, Integer> {
	private int maxWaitSecs;

	private boolean ready;
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

		while (!ready && !timeoutReached && !stopped) {
			Logging.debug(this, " WaitingCycle waits signal " + waitSecs);
			// === serves like an external task
			waitSecs++;
			Globals.threadSleep(this, 1000);

			setProgress(100 * waitSecs / maxWaitSecs);

			publish(waitSecs);

			timeoutReached = waitSecs > maxWaitSecs;

		}

		Logging.info(this, " doInBackground finish time in millis " + System.currentTimeMillis());

		Logging.info(this,
				" doInBackground finished: ready, stopped, waitSecs " + ready + ", " + stopped + ", " + waitSecs);

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

	public void setReady() {
		ready = true;
	}

	public boolean isReady() {
		return ready;
	}

	public void stop() {
		Logging.info(this, "stop");
		stopped = true;
		cancel(true);
	}
}
