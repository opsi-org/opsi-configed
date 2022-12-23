package de.uib.utilities.thread;

import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import de.uib.utilities.logging.logging;

public class WaitingWorker extends SwingWorker<Void, Long> {
	//
	// Main task. Executed in background thread.
	//
	private boolean ready = false;
	private boolean stopped = false;
	protected final JLabel statusLabel;
	protected final JProgressBar progressBar;
	private final long startActionMillis;
	private boolean timeoutReached;

	WaitingSleeper waitingSleeper;

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
		logging.info(this, "stop");
		stopped = true;
		cancel(true);
	}

	@Override
	public Void doInBackground() {

		// startAnotherProcess()

		long timeStepMillis = (long) 500;

		logging.debug(this, " doInBackground waitingMillis " + waitingSleeper.getWaitingMillis());

		long elapsedMillis = 0;
		long elapsedMins = 0;

		// while (progress < 100 && !stopped)
		timeoutReached = (elapsedMillis >= waitingSleeper.getWaitingMillis());
		while (!ready && !timeoutReached && !stopped) {
			try {
				Thread.sleep(timeStepMillis);
			} catch (InterruptedException ignore) {
				logging.info(this, "InterruptedException");
				Thread.currentThread().interrupt();
			}

			long nowMillis = new GregorianCalendar().getTimeInMillis();

			elapsedMillis = nowMillis - startActionMillis;
			elapsedMins = (elapsedMillis / 1000) / 60;

			logging.debug(this, " doInBackground progress  elapsedMillis " + elapsedMillis);
			logging.debug(this, " doInBackground progress totalTimeElapsed  [min] " + elapsedMins);

			publish(elapsedMillis);

			timeoutReached = (elapsedMillis >= waitingSleeper.getWaitingMillis());

			// firePropertyChange("elapsedMins", 0, elapsedMins);

		}

		logging.info(this,
				" doInBackground finished: ready, stopped, elapsedMillis < waitingSleeper.getWaitingMillis() " + ready
						+ ", " + stopped + ", " + (elapsedMillis >= waitingSleeper.getWaitingMillis()));

		if (timeoutReached)
			logging.warning(this, " doInBackground finished, timeoutReached");

		return null;
	}

	//
	// Executed in event dispatching thread
	//
	@Override
	protected void process(List<Long> listOfMillis) {
		// update the steps which are done
		logging.debug(this, "process, we have got list " + listOfMillis);

		long millis = listOfMillis.get(listOfMillis.size() - 1);

		statusLabel.setText(
				// "passed " + giveTimeSpan( millis) +
				waitingSleeper.setLabellingStrategy(millis));
		// " " + configed .getResourceValue("FStartWakeOnLan.timeLeft") + " " +
		// Globals.giveTimeSpan( waitingSleeper.getWaitingMillis() -

		int barLength = progressBar.getMaximum() - progressBar.getMinimum();

		// ":: progressBar.getMinimum() " + progressBar.getMinimum()
		// + ":: millis " + millis + " :: waitingMillis " + waitingMillis + " :: min + "

		logging.debug(this, "process, millis " + millis);
		double proportion = ((double) millis) / (double) waitingSleeper.getOneProgressBarLengthWaitingMillis();
		logging.info(this, "process, millis/estimatedTotalWaitMillis  " + proportion);

		int portion = (int) (barLength * proportion);
		portion = portion % barLength;

		logging.debug(this, "portion " + portion + " barLength  " + barLength);

		progressBar.setValue(progressBar.getMinimum() + portion);

		// progressBar.setValue( ( int ) (progressBar.getMinimum() + (int) ( (barLength

	}

	//
	// Executed in event dispatching thread
	//
	@Override
	public void done() {
		logging.info(this, "done,  stopped is " + stopped);
		if (!stopped)
			waitingSleeper.actAfterWaiting();
	}

	public boolean isTimeoutReached() {
		return timeoutReached;
	}
}
