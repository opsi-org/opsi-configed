package de.uib.utilities.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FShowList;
import de.uib.utilities.thread.WaitCursor;

public class logging implements LogEventSubject

{
	public static String logDirectoryName = null;
	public static String logFilenameInUse = null;

	private static String logfileDelimiter = "configed";
	private static String logfileMarker = null;
	public static final String windowsEnvVariableAppDataDirectory = "APPDATA";
	public static final String envVariableForUserDirectory = "user.home";
	public static final String relativeLogDirWindows = "opsi.org" + File.separator + "log";
	public static final String relativeLogDirUnix = ".configed";
	private static String extension = ".log";

	public static final int LEVEL_SECRET = 9;
	public static final int LEVEL_TRACE = 8;
	public static final int LEVEL_DEBUG = 7;
	public static final int LEVEL_INFO = 6;
	public static final int LEVEL_NOTICE = 5;
	public static final int LEVEL_WARNING = 4;
	public static final int LEVEL_ERROR = 3;
	public static final int LEVEL_CRITICAL = 2;
	public static final int LEVEL_ESSENTIAL = 1;
	public static final int LEVEL_NONE = 0;

	public static final Map<Integer, String> LEVEL_TO_NAME = Map.of(LEVEL_SECRET, "SECRET", LEVEL_TRACE, "TRACE",
			LEVEL_DEBUG, "DEBUG", LEVEL_INFO, "INFO", LEVEL_NOTICE, "NOTICE", LEVEL_WARNING, "WARNING", LEVEL_ERROR,
			"ERROR", LEVEL_CRITICAL, "CRITICAL", LEVEL_ESSENTIAL, "ESSENTIAL", LEVEL_NONE, "NONE");

	public static final Map<Integer, String> LEVEL_TO_COLOR = Map.of(LEVEL_SECRET, "\033[0;33m", LEVEL_TRACE,
			"\033[0;37m", LEVEL_DEBUG, "\033[0;37m", LEVEL_INFO, "\033[1;37m", LEVEL_NOTICE, "\033[1;32m",
			LEVEL_WARNING, "\033[1;33m", LEVEL_ERROR, "\033[0;31m", LEVEL_CRITICAL, "\033[1;31m", LEVEL_ESSENTIAL,
			"\033[1;36m");

	public static String LOG_FORMAT = "[%d] [%s] [%-15s] %s";
	public static String COLORED_LOG_FORMAT = "{color}[%d] [%s]{reset} [%-15s] %s";

	public static boolean COLOR_LOG = true;
	public static Integer LOG_LEVEL_CONSOLE = LEVEL_WARNING;
	public static Integer LOG_LEVEL_FILE = LEVEL_WARNING;

	private static final java.text.SimpleDateFormat loggingDateFormat = new java.text.SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	public static final String levelText(int level) {
		return LEVEL_TO_NAME.get(level);
	}

	private static int MIN_LEVEL_FOR_SHOWING_MESSAGES = LEVEL_ERROR;

	private static int numberOfKeptLogFiles = 3;
	private static PrintWriter logFileWriter = null;
	private static boolean LogFileInitialized = false;
	public static boolean LogFileAvailable = false;

	private static final int maxListedErrors = 20;
	private static Vector<String> errorList = new Vector<String>(maxListedErrors);

	public static FShowList fErrors;

	protected static Vector<LogEventObserver> logEventObservers = new Vector<LogEventObserver>();

	public static void setSuppressConsole(boolean b) {
		setLogLevelConsole(LEVEL_NONE);
	}

	public static void setLogLevelConsole(int newLevel) {
		if (newLevel < LEVEL_NONE)
			newLevel = LEVEL_NONE;
		else if (newLevel > LEVEL_SECRET)
			newLevel = LEVEL_SECRET;
		LOG_LEVEL_CONSOLE = newLevel;
	}

	public static void setLogLevelFile(int newLevel) {
		if (newLevel < LEVEL_NONE)
			newLevel = LEVEL_NONE;
		else if (newLevel > LEVEL_SECRET)
			newLevel = LEVEL_SECRET;
		LOG_LEVEL_FILE = newLevel;
	}

	public static void setLogLevel(int newLevel) {
		setLogLevelConsole(newLevel);
		setLogLevelFile(newLevel);
	}

	public static void setLogfileMarker(String marker) {
		if (logfileMarker != null) {
			debug("logfileMarker already set");
			return;
		}

		if (marker == null || marker.length() == 0)
			logfileMarker = "";
		else

			logfileMarker = "__" + marker.replace('.', '_').replace(":", "__");
	}

	private static final void initLogFile() {
		LogFileInitialized = true; // Try to initialize only once!
		LogFileAvailable = false;
		String logFilename = "";

		try {
			File logDirectory;
			if (logDirectoryName == null || logDirectoryName.isEmpty()) {
				if (System.getenv(logging.windowsEnvVariableAppDataDirectory) != null)
					// Windows
					logDirectory = new File(
							System.getenv(windowsEnvVariableAppDataDirectory) + File.separator + relativeLogDirWindows);
				else
					logDirectory = new File(
							System.getProperty(envVariableForUserDirectory) + File.separator + relativeLogDirUnix);
			} else {
				logDirectory = new File(logDirectoryName);
			}
			logDirectory = logDirectory.getCanonicalFile();
			logDirectoryName = logDirectory.getAbsolutePath();

			info("Logging directory is: " + logDirectoryName);

			logDirectory.mkdirs();

			logFilename = logDirectory.getAbsolutePath() + File.separator + logfileDelimiter + logfileMarker
					+ extension;
			logFilename = new File(logFilename).getAbsolutePath();
			File logFile = new File(logFilename);

			if (numberOfKeptLogFiles > 0) {
				String[] logFilenames = new String[numberOfKeptLogFiles];
				File[] logFiles = new File[numberOfKeptLogFiles];

				for (int i = 0; i < numberOfKeptLogFiles; i++) {
					logFilenames[i] = logDirectory.getAbsolutePath() + File.separator + logfileDelimiter + logfileMarker
							+ "___" + i + extension;
					logFiles[i] = new File(logFilenames[i]);
				}

				for (int i = numberOfKeptLogFiles - 1; i > 0; i--) {
					if (logFiles[i - 1].exists()) {
						logFiles[i - 1].renameTo(logFiles[i]);
					}
				}

				if (logFile.exists())
					logFile.renameTo(logFiles[0]);
			}

			logFileWriter = new PrintWriter(new FileOutputStream(logFilename));
			logFilenameInUse = logFilename;
			LogFileAvailable = true;
		} catch (Exception ex) {
			System.out.print(ex);
			logFilenameInUse = configed.getResourceValue("logging.noFileLogging");
		}
	}

	public static final synchronized void init() {
		initLogFile();
	}

	public static String getCurrentLogfilePath() {
		return logFilenameInUse;
	}

	private static boolean showOnGUI(int level) {
		return (level <= MIN_LEVEL_FOR_SHOWING_MESSAGES);
	}

	private static String now() {

		return loggingDateFormat.format(new java.util.Date());
	}

	private static void addErrorToList(String mesg, String time) {
		while (errorList.size() >= maxListedErrors) {
			errorList.removeElementAt(0);
		}
		errorList.add(String.format("[%s] %s", time, mesg));

		for (int i = 0; i < logEventObservers.size(); i++) {
			logEventObservers.get(i).logEventOccurred(new LogEvent(null, "", -1, true));
		}
	}

	public static String getIntegers(int[] s) {
		if (s == null)
			return null;

		StringBuilder result = new StringBuilder("{");

		for (int j = 0; j < s.length; j++) {
			result.append("\"");
			result.append("" + s[j]);
			result.append("\"");
			if (j < s.length - 1)
				result.append(", ");
		}

		result.append("}");

		return result.toString();
	}

	public static String getStrings(int[] s) {
		if (s == null)
			return null;

		Integer[] t = new Integer[s.length];
		for (int i = 0; i < s.length; i++)
			t[i] = s[i];

		return getStrings(t);
	}

	public static String getSize(Object[] a) {
		if (a == null)
			return null;

		return "" + a.length;
	}

	public static String getSize(Collection c) {
		if (c == null)
			return null;

		return "" + c.size();
	}

	public static String getStrings(Object[] s) {
		if (s == null)
			return null;

		StringBuilder result = new StringBuilder("{");

		for (int j = 0; j < s.length; j++) {
			result.append("\"");
			result.append("" + s[j]);
			result.append("\"");
			if (j < s.length - 1)
				result.append(", ");
		}

		result.append("}");

		return result.toString();
	}

	public static synchronized void log(int level, String mesg, Object caller, Throwable ex) {
		if (level > LOG_LEVEL_CONSOLE && level > LOG_LEVEL_FILE) {
			return;
		}

		String curTime = now();
		String context = Thread.currentThread().getName();
		if (caller != null) {
			mesg += "   (" + caller.getClass().getName() + ")";
		}

		String exMesg = "";
		if (ex != null) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			exMesg = "\n" + sw.toString();
		}

		if (level <= LOG_LEVEL_CONSOLE) {
			String format = LOG_FORMAT;
			if (COLOR_LOG) {
				format = COLORED_LOG_FORMAT.replace("{color}", LEVEL_TO_COLOR.get(level)).replace("{reset}", "\033[0m");
			}
			System.err.println(String.format(format, level, curTime, context, mesg) + exMesg);
		}
		if (level <= LOG_LEVEL_FILE) {
			if (!LogFileInitialized) {
				initLogFile();
			}
			if (logFileWriter != null) {
				logFileWriter.println(String.format(LOG_FORMAT, level, curTime, context, mesg) + exMesg);
				logFileWriter.flush();
			}
		}

		if (showOnGUI(level)) {
			addErrorToList(mesg, curTime);
		}
	}

	public static void log(Object caller, int level, String mesg, Throwable ex) {
		log(level, mesg, caller, ex);
	}

	public static void log(Object caller, int level, String mesg) {
		log(level, mesg, caller, null);
	}

	public static void log(int level, String mesg, Throwable ex) {
		log(level, mesg, null, ex);
	}

	public static void log(int level, String mesg) {
		log(level, mesg, null, null);
	}

	public static void secret(Object caller, String mesg) {
		log(caller, LEVEL_SECRET, mesg);
	}

	public static void secret(String mesg) {
		log(LEVEL_SECRET, mesg);
	}

	public static void trace(Object caller, String mesg) {
		log(caller, LEVEL_TRACE, mesg);
	}

	public static void trace(String mesg) {
		log(LEVEL_TRACE, mesg);
	}

	public static void debug(Object caller, String mesg) {
		log(caller, LEVEL_DEBUG, mesg);
	}

	public static void debug(String mesg) {
		log(LEVEL_DEBUG, mesg);
	}

	public static void info(Object caller, String mesg) {
		log(caller, LEVEL_INFO, mesg);
	}

	public static void info(String mesg) {
		log(LEVEL_INFO, mesg);
	}

	public static void notice(Object caller, String mesg) {
		log(caller, LEVEL_NOTICE, mesg);
	}

	public static void notice(String mesg) {
		log(LEVEL_NOTICE, mesg);
	}

	public static void warning(Object caller, String mesg) {
		log(caller, LEVEL_WARNING, mesg);
	}

	public static void warning(String mesg) {
		log(LEVEL_WARNING, mesg);
	}

	public static void warning(String mesg, Throwable ex) {
		log(LEVEL_WARNING, mesg, ex);
	}

	public static void warning(Object caller, String mesg, Throwable ex) {
		log(caller, LEVEL_WARNING, mesg, ex);
	}

	public static void error(Object caller, String mesg) {
		log(caller, LEVEL_ERROR, mesg);
	}

	public static void error(String mesg) {
		log(LEVEL_ERROR, mesg);
	}

	public static void error(String mesg, Throwable ex) {
		log(LEVEL_ERROR, mesg, ex);
	}

	public static void error(Object caller, String mesg, Throwable ex) {
		log(caller, LEVEL_ERROR, mesg, ex);
	}

	public static void critical(Object caller, String mesg) {
		log(caller, LEVEL_CRITICAL, mesg);
	}

	public static void critical(String mesg) {
		log(LEVEL_CRITICAL, mesg);
	}

	public static void critical(Object caller, String mesg, Throwable ex) {
		log(caller, LEVEL_CRITICAL, mesg, ex);
	}

	public static void essential(Object caller, String mesg) {
		log(caller, LEVEL_ESSENTIAL, mesg);
	}

	public static void essential(String mesg) {
		log(LEVEL_ESSENTIAL, mesg);
	}

	public static void devel(Object caller, String mesg) {
		essential(caller, mesg);
	}

	public static void devel(String mesg) {
		essential(mesg);
	}

	public static void clearErrorList() {

		errorList.clear();
	}

	private static List getErrorList() {
		return errorList;
	}

	public static void checkErrorList(JFrame parentFrame) {
		// if errors Occurred show a window with the logged errors

		final JFrame f;
		if (parentFrame == null)
			f = Globals.mainFrame;
		else
			f = parentFrame;

		int errorCount = getErrorList().size();

		info("error list size " + errorCount);

		if (errorCount == 0)
			return;

		if (fErrors == null) {
			WaitCursor.stopAll();

			fErrors = new FShowList(f, Globals.APPNAME + ": problems Occurred", false, new String[] { "ok" }, 400, 300);
		}

		new Thread() {
			@Override
			public void run() {
				fErrors.setMessage(logging.getErrorListAsLines());
				fErrors.setAlwaysOnTop(true);
				fErrors.setVisible(true);
			}
		}.start();

	}

	public static String getErrorListAsLines() {
		StringBuilder result = new StringBuilder("");
		if (!errorList.isEmpty()) {
			for (int i = 0; i < errorList.size(); i++) {
				result.append("\n");
				result.append(errorList.get(i));
			}
		}

		return result.toString();
	}

	public static void debugMap(Object caller, Map m) {
		if (m == null) {
			debug(caller, " is null");
			return;
		}

		Iterator iter = m.keySet().iterator();

		while (iter.hasNext()) {
			Object key = iter.next();

			Object value = m.get(key);

			debug(caller, " key: " + key + ", class " + key.getClass().getName() + ", value " + value + ", class "
					+ value.getClass().getName());
		}
	}

	// used instead of interface LogEventSubject
	public static void registLogEventObserver(LogEventObserver o) {
		logEventObservers.add(o);
	}

	// interface LogEventSubject
	@Override
	public void registerLogEventObserver(LogEventObserver o) {
		// not implemented since static method is needed
	}

}
