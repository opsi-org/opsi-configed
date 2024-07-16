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
			Logging.warning(exception, "Error in thread ", thread);

			String errorText = Configed.getResourceValue("UncaughtExceptionHandler.notForeseenError") + " "
					+ exception.getMessage();

			if (exception instanceof IllegalComponentStateException) {
				Logging.warning(exception, "IllegalComponentStateException ");
			} else if (exception.getMessage() == null) {
				// according to some internet info it could occure on ground of some
				// optimization in the JIT compiler

				Logging.warning(exception, "exception with null message ");
			} else {
				Logging.error(exception, errorText, "\n",
						Configed.getResourceValue("UncaughtExceptionHandler.pleaseCheckLogfile"));
			}
		} else {
			Logging.warning(throwable, "Thread ", thread, " - RunTime Error");
			if (throwable instanceof OutOfMemoryError) {
				Main.endApp(Main.ERROR_OUT_OF_MEMORY);
			}
		}
	}
}
