package de.uib.utilities.thread;

import javax.swing.*;

public interface WaitingSleeper
{
	public abstract void actAfterWaiting();
	public abstract JProgressBar getProgressBar();
	public abstract JLabel getLabel();
	public abstract long getStartActionMillis();
	public abstract long getWaitingMillis();
	public abstract long getOneProgressBarLengthWaitingMillis();
	public abstract String setLabellingStrategy(long millisLevel);
}
