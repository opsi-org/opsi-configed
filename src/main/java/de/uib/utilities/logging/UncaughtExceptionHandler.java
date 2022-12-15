package de.uib.utilities.logging;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static String lastException = "";

	public void uncaughtException(Thread t, Throwable e) {

		de.uib.utilities.thread.WaitCursor.stopAll();

		if (e instanceof Exception) {
			Exception ex = (Exception) e;
			logging.warning("Error in thread " + t, ex);
			logging.error("Unhandled error: " + ex.getMessage() + "\nplease check logfile");
		} else {

			logging.warning("Thread " + t + " (RunTime Error)  " + e);

			if (e instanceof java.lang.OutOfMemoryError) {

				if (!lastException.equals(e.toString())) {
					lastException = e.toString();
					logging.error("Error: out of memory");
				}
			}

		}
	}
}
