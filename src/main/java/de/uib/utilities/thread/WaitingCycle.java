package de.uib.utilities.thread;

import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingWorker;

import de.uib.utilities.logging.logging;

public class WaitingCycle extends SwingWorker<Void, Integer> {
	private int maxWaitSecs;

	private boolean ready = false;
	private boolean stopped = false;
	private long startActionMillis;
	private boolean timeoutReached;

	public WaitingCycle(int maxWaitSecs) {
		this.maxWaitSecs = maxWaitSecs;
		timeoutReached = false;
	}

	@Override
	public Void doInBackground() {
		int waitSecs = 0;

		startActionMillis = new GregorianCalendar().getTimeInMillis();
		logging.info(this, " doInBackground start " + startActionMillis);

		while (!ready && !timeoutReached && !stopped) {
			logging.debug(this, " WaitingCycle waits signal " + waitSecs);
			// === serves like an external task
			waitSecs++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException iex) {
				logging.info(this, " WaitingCycle interrupt at " + waitSecs);
			}

			setProgress(100 * waitSecs / maxWaitSecs);

			publish(waitSecs);

			timeoutReached = (waitSecs > maxWaitSecs);

		}

		logging.info(this, " doInBackground finish time in millis " + new GregorianCalendar().getTimeInMillis());

		logging.info(this,
				" doInBackground finished: ready, stopped, waitSecs " + ready + ", " + stopped + ", " + waitSecs);

		if (timeoutReached)
			logging.warning(this, " doInBackground finished, timeoutReached");

		return null;

	}

	//
	// Executed in event dispatching thread, override it
	//
	@Override
	protected void process(List<Integer> chunks) {

	}

	//
	// Executed in event dispatching thread
	//
	@Override
	public void done() {
		logging.info(this, "done resp. is stopped ");
	}

	// own methods

	public void setReady() {
		ready = true;
	}

	public boolean isReady() {
		return ready;
	}

	public void stop() {
		logging.info(this, "stop");
		stopped = true;
		cancel(true);
	}
}
