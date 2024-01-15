/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.logging;

import java.lang.Thread.UncaughtExceptionHandler;

import de.uib.Main;
import de.uib.configed.Configed;

public class UncaughtConfigedExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {
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
