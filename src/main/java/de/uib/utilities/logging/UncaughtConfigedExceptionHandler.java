package de.uib.utilities.logging;

public class UncaughtConfigedExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static String lastException = "";

	@Override
	public void uncaughtException(Thread t, Throwable e) {

		de.uib.utilities.thread.WaitCursor.stopAll();

		if (e instanceof Exception) {
			Exception ex = (Exception) e;
			Logging.warning("Error in thread " + t, ex);
			Logging.error("Unhandled error: " + ex.getMessage() + "\nplease check logfile");
		} else {

			Logging.warning("Thread " + t + " (RunTime Error)  " + e);

			if (e instanceof OutOfMemoryError && !lastException.equals(e.toString())) {
				lastException = e.toString();
				Logging.error("Error: out of memory");
			}
		}
	}
}
