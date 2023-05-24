/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.thread;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

public interface WaitingSleeper {
	void actAfterWaiting();

	JProgressBar getProgressBar();

	JLabel getLabel();

	long getStartActionMillis();

	long getWaitingMillis();

	long getOneProgressBarLengthWaitingMillis();

	String setLabellingStrategy(long millisLevel);
}
