/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import de.uib.Main;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.UncaughtConfigedExceptionHandler;

public class UncaughtConfigedExceptionHandlerLocalized extends UncaughtConfigedExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {

		ConfigedMain.setProgressComponentStopWaiting();

		if (e instanceof Exception) {
			Logging.warning("Error in thread " + t, e);

			String errorText = Configed.getResourceValue("UncaughtExceptionHandler.notForeseenError") + " "
					+ ((Exception) e).getMessage();

			if (e instanceof java.awt.IllegalComponentStateException) {
				Logging.warning("exception " + e);
			} else if (e.getMessage() == null) {
				// according to some internet info it could occure on ground of some
				// optimization in the JIT compiler

				Logging.warning("exception with null message " + e);
			} else if (e.getMessage().indexOf("javax.swing.plaf.FontUIResource cannot be cast") > -1) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=271611
				Logging.warning(errorText);
			} else if (e.getMessage().indexOf("javax.swing.Painter") > -1) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				Logging.warning(errorText);
			} else if (e.getMessage().indexOf("'bootstrap'") > -1) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				Logging.warning(errorText);
			} else if (e.getMessage().contains("cannot be cast to java.awt.Font")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				Logging.warning(errorText);
			} else if (e.getMessage().contains("cannot be cast to class java.awt.Font")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				Logging.warning(errorText);
			} else if (e.getMessage().contains("javax.swing.plaf.")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				Logging.warning(errorText);
			} else {
				Logging.error(
						errorText + "\n" + Configed.getResourceValue("UncaughtExceptionHandler.pleaseCheckLogfile"), e);
			}
		} else {
			Logging.warning("Thread " + t + " - RunTime Error", e);
			if (e instanceof OutOfMemoryError) {
				Main.endApp(Main.ERROR_OUT_OF_MEMORY);
			}
		}
	}
}
