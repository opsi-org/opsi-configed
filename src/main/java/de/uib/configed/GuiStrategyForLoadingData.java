package de.uib.configed;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.DPassword;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.DataLoadingObserver;
import de.uib.utilities.thread.WaitingSleeper;
import de.uib.utilities.thread.WaitingWorker;

public class GuiStrategyForLoadingData implements DataLoadingObserver, WaitingSleeper {
	private static final long WAITING_MILLIS_FOR_LOADING = 50000;
	private static final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;

	private Object observingMesg = Configed.getResourceValue("LoadingObserver.start");

	private WaitingWorker worker;

	private DPassword dPassword;

	public GuiStrategyForLoadingData(DPassword dPassword) {
		this.dPassword = dPassword;

		worker = new WaitingWorker(this);
	}

	public void startWaiting() {
		worker.execute();
	}

	public void stopWaiting() {
		worker.stop();
	}

	public void setReady() {
		worker.setReady();
	}

	// WaitingSleeper
	@Override
	public void actAfterWaiting() {
		Logging.info(this, "actAfterWaiting");
		SwingUtilities.invokeLater(() -> dPassword.setVisible(false));
	}

	@Override
	public JProgressBar getProgressBar() {
		return dPassword.getProgressBar();
	}

	@Override
	public JLabel getLabel() {
		return dPassword.getLabel();
	}

	@Override
	public long getStartActionMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public long getWaitingMillis() {
		return WAITING_MILLIS_FOR_LOADING;
	}

	@Override
	public long getOneProgressBarLengthWaitingMillis() {
		return ESTIMATED_TOTAL_WAIT_MILLIS;
	}

	@Override
	public String setLabellingStrategy(long millisLevel) {
		Logging.debug(this, "setLabellingStrategy millis " + millisLevel);

		// produces strings with ascii null
		return "" + observingMesg + " ... ";

	}

	// DataLoadingObserver
	@Override
	public void gotNotification(Object mesg) {
		observingMesg = mesg;
	}
}