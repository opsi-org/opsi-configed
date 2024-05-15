/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.logging;

import java.awt.IllegalComponentStateException;
import java.lang.Thread.UncaughtExceptionHandler;

import de.uib.Main;
import de.uib.configed.Configed;

public class UncaughtConfigedExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if (throwable instanceof Exception exception) {
			Logging.warning("Error in thread " + thread, exception);

			String errorText = Configed.getResourceValue("UncaughtExceptionHandler.notForeseenError") + " "
					+ exception.getMessage();

			if (exception instanceof IllegalComponentStateException) {
				Logging.warning("IllegalComponentStateException " + exception);
			} else if (exception.getMessage() == null) {
				// according to some internet info it could occure on ground of some
				// optimization in the JIT compiler

				Logging.warning("exception with null message " + exception);
			} else {
				Logging.error(
						errorText + "\n" + Configed.getResourceValue("UncaughtExceptionHandler.pleaseCheckLogfile"),
						exception);
			}
		} else {
			Logging.warning("Thread " + thread + " - RunTime Error", throwable);
			if (throwable instanceof OutOfMemoryError) {
				Main.endApp(Main.ERROR_OUT_OF_MEMORY);
			}
		}
	}
}
