package de.uib.configed;

import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitCursor;

public class UncaughtExceptionHandlerLocalized extends de.uib.utilities.logging.UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		WaitCursor.stopAll();

		if (configed.fProgress != null) {
			try {
				configed.fProgress.stopWaiting();
				configed.fProgress = null;
			} catch (Exception ex) {
				logging.debug(this, "Exception " + ex);
			}
		}

		Integer saveInjectedLogLevel = logging.getInjectedLogLevel();
		logging.info(this, " " + this + " saveInjectedLogLevel " + saveInjectedLogLevel);
		if (saveInjectedLogLevel != null)
			logging.injectLogLevel(logging.LEVEL_INFO);
		logging.info(this, " " + this + " injectedLogLevel " + logging.getInjectedLogLevel());

		if (e instanceof Exception) {
			logging.warning("Error in thread " + t);
			logging.logTrace(e);

			String errorText = configed.getResourceValue("UncaughtExceptionHandler.notForeseenError") + " "
					+ ((Exception) e).getMessage();

			if (e instanceof java.awt.IllegalComponentStateException) {
				logging.warning("exception " + e);
			}

			else if (e.getMessage() == null)
			//according to some internet info it could occure on ground of some optimization in the JIT compiler
			{
				logging.warning("exception with null message " + e);
			}

			else if (e.getMessage().indexOf("javax.swing.plaf.FontUIResource cannot be cast") > -1) {
				//https://netbeans.org/bugzilla/show_bug.cgi?id=271611
				logging.warning(errorText);
			}

			else if (e.getMessage().indexOf("javax.swing.Painter") > -1) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}

			else if (e.getMessage().indexOf("'bootstrap'") > -1) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}

			else if (e.getMessage().contains("cannot be cast to java.awt.Font")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}

			else if (e.getMessage().contains("cannot be cast to class java.awt.Font")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}

			else if (e.getMessage().contains("javax.swing.plaf.")) {
				// https://netbeans.org/bugzilla/show_bug.cgi?id=230528
				logging.warning(errorText);
			}

			else {
				logging.error(
						errorText + "\n" + configed.getResourceValue("UncaughtExceptionHandler.pleaseCheckLogfile"),
						(Exception) e);
			}
		} else {
			logging.logTrace(e);
			logging.warning("Thread " + t + " - RunTime Error -  " + e);
			if (e instanceof java.lang.OutOfMemoryError) {
				configed.endApp(configed.ERROR_OUT_OF_MEMORY);
			}

		}

		if (saveInjectedLogLevel != null)
			logging.injectLogLevel(saveInjectedLogLevel);

	}
}
