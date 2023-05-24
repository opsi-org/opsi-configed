/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.thread;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class WaitingWorker extends SwingWorker<Void, Long> {
	//
	// Main task. Executed in background thread.
	//
	private boolean ready;
	private boolean stopped;
	private final JLabel statusLabel;
	private final JProgressBar progressBar;
	private final long startActionMillis;
	private boolean timeoutReached;

	private WaitingSleeper waitingSleeper;

	public WaitingWorker(WaitingSleeper waitingSleeper) {
		this.waitingSleeper = waitingSleeper;
		this.progressBar = waitingSleeper.getProgressBar();
		this.statusLabel = waitingSleeper.getLabel();
		startActionMillis = waitingSleeper.getStartActionMillis();
		timeoutReached = false;

	}

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

	@Override
	public Void doInBackground() {

		// startAnotherProcess()

		long timeStepMillis = 500;

		Logging.debug(this, " doInBackground waitingMillis " + waitingSleeper.getWaitingMillis());

		long elapsedMillis = 0;
		long elapsedMins = 0;

		timeoutReached = elapsedMillis >= waitingSleeper.getWaitingMillis();
		while (!ready && !timeoutReached && !stopped) {
			Globals.threadSleep(this, timeStepMillis);

			long nowMillis = System.currentTimeMillis();

			elapsedMillis = nowMillis - startActionMillis;
			elapsedMins = (elapsedMillis / 1000) / 60;

			Logging.debug(this, " doInBackgroudnd progress  elapsedMillis " + elapsedMillis);
			Logging.debug(this, " doInBackground progress totalTimeElapsed  [min] " + elapsedMins);

			publish(elapsedMillis);

			timeoutReached = elapsedMillis >= waitingSleeper.getWaitingMillis();

		}

		Logging.info(this,
				" doInBackground finished: ready, stopped, elapsedMillis < waitingSleeper.getWaitingMillis() " + ready
						+ ", " + stopped + ", " + (elapsedMillis >= waitingSleeper.getWaitingMillis()));

		if (timeoutReached) {
			Logging.warning(this, " doInBackground finished, timeoutReached");
		}

		return null;
	}

	//
	// Executed in event dispatching thread
	//
	@Override
	protected void process(List<Long> listOfMillis) {
		// update the steps which are done
		Logging.debug(this, "process, we have got list " + listOfMillis);

		long millis = listOfMillis.get(listOfMillis.size() - 1);

		statusLabel.setText(

				waitingSleeper.setLabellingStrategy(millis));

		int barLength = progressBar.getMaximum() - progressBar.getMinimum();

		Logging.debug(this, "process, millis " + millis);
		double proportion = ((double) millis) / (double) waitingSleeper.getOneProgressBarLengthWaitingMillis();
		Logging.info(this, "process, millis/estimatedTotalWaitMillis  " + proportion);

		int portion = (int) (barLength * proportion);
		portion = portion % barLength;

		Logging.debug(this, "portion " + portion + " barLength  " + barLength);

		progressBar.setValue(progressBar.getMinimum() + portion);
	}

	//
	// Executed in event dispatching thread
	//
	@Override
	public void done() {
		Logging.info(this, "done,  stopped is " + stopped);
		if (!stopped) {
			waitingSleeper.actAfterWaiting();
		}
	}

	public boolean isTimeoutReached() {
		return timeoutReached;
	}
}
