package  de.uib.utilities.logging;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler 
{
	
	private static String lastException = "";
	
	public void uncaughtException(Thread t, Throwable e) {
		
		de.uib.utilities.thread.WaitCursor.stopAll();
		
		Integer saveInjectedLogLevel = logging.getInjectedLogLevel();
		//System.out.println(" " + this + " saveInjectedLogLevel " + saveInjectedLogLevel);
		if (saveInjectedLogLevel != null)
			logging.injectLogLevel(logging.LEVEL_INFO);
		//System.out.println(" " + this + " injectedLogLevel " + logging.getInjectedLogLevel());
		
		if (e instanceof Exception)
		{
			logging.warning("Error in thread " + t);
			logging.logTrace((Exception) e);
			logging.error("Not foreseen error: " + ((Exception)e).getMessage() 
				+ "\nplease check logfile");
		}
		else
		{
			
			
			logging.warning("Thread " + t + " (RunTime Error)  " + e);
			
			if ( e instanceof java.lang.OutOfMemoryError )
			{

				if (!lastException.equals(e.toString()))
				{
					lastException = e.toString();
					logging.error("Error: out of memory");
				}
			}
			
			
		}
		
		if (saveInjectedLogLevel != null)
				logging.injectLogLevel(saveInjectedLogLevel);
	}
}
