/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.logging;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;

@SuppressWarnings("java:S923")
public final class Logging {
	private static String baseLogDirectoryPath;
	private static String logFilenameInUse;

	private static final String LOG_FILE_DELIMITER = "configed";
	public static final String WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY = "APPDATA";
	public static final String ENV_VARIABLE_FOR_USER_DIRECTORY = "user.home";
	private static final String RELATIVE_LOG_DIR_WINDOWS = "opsi.org" + File.separator + "log";
	private static final String RELATIVE_LOG_DIR_UNIX = ".configed";
	private static final String LOGS_DIR = "logs";
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

	private static final String COLORED_LOG_FORMAT = "{color}[%d] [%s]{reset} [%-15s] %s";

	private static String logFormat = "[%d] [%s] [%-15s] %s";

	private static Integer logLevelConsole = LEVEL_WARNING;
	private static Integer logLevelFile = LEVEL_WARNING;

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private static final int MIN_LEVEL_FOR_SHOWING_MESSAGES = LEVEL_ERROR;

	private static final int NUMBER_OF_LOG_FILES = 10;
	private static PrintWriter logFileWriter;
	private static boolean logFileInitialized;

	private static final int MAX_LISTED_ERRORS = 20;
	private static List<String> errorList = new ArrayList<>(MAX_LISTED_ERRORS);

	private static JDialog dialog;
	private static JTextArea jTextArea;

	// private constructor to hide the implicit public one
	private Logging() {
	}

	public static synchronized Integer getLogLevelConsole() {
		return logLevelConsole;
	}

	public static synchronized void setLogLevelConsole(int newLevel) {
		if (newLevel < LEVEL_NONE) {
			logLevelConsole = LEVEL_NONE;
		} else if (newLevel > LEVEL_SECRET) {
			logLevelConsole = LEVEL_SECRET;
		} else {
			logLevelConsole = newLevel;
		}
	}

	public static synchronized void setLogLevelFile(int newLevel) {
		if (newLevel < LEVEL_NONE) {
			logLevelFile = LEVEL_NONE;
		} else if (newLevel > LEVEL_SECRET) {
			logLevelFile = LEVEL_SECRET;
		} else {
			logLevelFile = newLevel;
		}
	}

	public static void setLogLevel(int newLevel) {
		setLogLevelConsole(newLevel);
		setLogLevelFile(newLevel);
	}

	public static synchronized void initLogFile() {
		// Try to initialize only once!
		logFileInitialized = true;
		String logFilename = "";

		try {
			File logDirectory = getDirectory();
			logDirectory = logDirectory.getCanonicalFile();

			info("Logging directory is: ", baseLogDirectoryPath);

			logDirectory.mkdirs();

			logFilename = logDirectory.getAbsolutePath() + File.separator + LOG_FILE_DELIMITER + extension;
			logFilename = new File(logFilename).getAbsolutePath();

			if (NUMBER_OF_LOG_FILES > 0) {
				rotateLogFiles(logFilename, logDirectory);
			}

			logFileWriter = new PrintWriter(new FileOutputStream(logFilename), false, StandardCharsets.UTF_8);
			logFilenameInUse = logFilename;
		} catch (IOException ex) {
			Logging.error(ex, "file ", logFilename, " or directory ", baseLogDirectoryPath, " not found...");
			logFilenameInUse = Configed.getResourceValue("logging.noFileLogging");
		}
	}

	public static synchronized void updateLogfile() {
		File logDirectory = getDirectory();
		File currentLogfile = new File(getCurrentLogfilePath());
		File updatedLogFile = new File(logDirectory.getAbsolutePath() + File.separator + LOG_FILE_DELIMITER + "_"
				+ ConfigedMain.getHost() + "_" + ConfigedMain.getUser() + extension);

		if (NUMBER_OF_LOG_FILES > 0) {
			rotateLogFiles(updatedLogFile.getAbsolutePath(), logDirectory, ConfigedMain.getHost(),
					ConfigedMain.getUser());
		}

		closeLogWriter();
		if (currentLogfile.renameTo(updatedLogFile)) {
			logFilenameInUse = updatedLogFile.getAbsolutePath();
			info("Log file renamed successfully " + updatedLogFile.getName());
		} else {
			info("Failed to rename log file " + currentLogfile.getName());
		}
		reopenLogWriter();
	}

	private static synchronized void closeLogWriter() {
		if (logFileWriter != null) {
			logFileWriter.flush();
			logFileWriter.close();
			logFileWriter = null;
		}
	}

	private static synchronized void reopenLogWriter() {
		try {
			logFileWriter = new PrintWriter(new FileWriter(logFilenameInUse, true));
		} catch (IOException e) {
			System.err.println("Failed to reopen log writer: " + e.getMessage());
		}
	}

	private static synchronized File getDirectory() {
		File logDirectory;

		if (baseLogDirectoryPath != null) {
			logDirectory = new File(baseLogDirectoryPath);
		} else {
			logDirectory = getBaseLogDirectoryBasedOnOS();
			baseLogDirectoryPath = logDirectory.getAbsolutePath();
		}

		if (ConfigedMain.getHost() != null) {
			logDirectory = changeLogDirectory(logDirectory);
		}

		return logDirectory;
	}

	private static File getBaseLogDirectoryBasedOnOS() {
		File baseLogDirectory;

		if (System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) != null) {
			baseLogDirectory = new File(
					System.getenv(WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) + File.separator + RELATIVE_LOG_DIR_WINDOWS);
		} else {
			baseLogDirectory = new File(
					System.getProperty(ENV_VARIABLE_FOR_USER_DIRECTORY) + File.separator + RELATIVE_LOG_DIR_UNIX);
		}

		return baseLogDirectory;
	}

	private static File changeLogDirectory(File logDirectory) {
		String newDirPath = logDirectory.getAbsolutePath() + File.separator + ConfigedMain.getHost() + File.separator
				+ LOGS_DIR;
		File newLogDirectory = new File(newDirPath);

		if (!newLogDirectory.exists()) {
			if (newLogDirectory.mkdirs()) {
				Logging.info("directory created: " + newDirPath);
			} else {
				Logging.warning("failed to create directory: " + newDirPath);
			}
		}

		return newLogDirectory;
	}

	private static void rotateLogFiles(String logFilename, File logDirectory) {
		rotateLogFiles(logFilename, logDirectory, null, null);
	}

	private static void rotateLogFiles(String logFilename, File logDirectory, String host, String user) {
		File logFile = new File(logFilename);
		String[] logFilenames = new String[NUMBER_OF_LOG_FILES];
		File[] logFiles = new File[NUMBER_OF_LOG_FILES];

		for (int i = 0; i < NUMBER_OF_LOG_FILES; i++) {
			if (host != null && user != null) {
				logFilenames[i] = logDirectory.getAbsolutePath() + File.separator + LOG_FILE_DELIMITER + "_" + host
						+ "_" + user + "_" + i + extension;
			} else {
				logFilenames[i] = logDirectory.getAbsolutePath() + File.separator + LOG_FILE_DELIMITER + "_" + i
						+ extension;
			}
			logFiles[i] = new File(logFilenames[i]);
		}

		for (int i = NUMBER_OF_LOG_FILES - 1; i > 0; i--) {
			if (logFiles[i - 1].exists() && !logFiles[i - 1].renameTo(logFiles[i])) {
				Logging.warning("renaming logfile failed for file: ", logFiles[i - 1]);
			}
		}

		if (logFile.exists() && !logFile.renameTo(logFiles[0])) {
			Logging.warning("renaming logfile failed for file: ", logFiles[0]);
		}
	}

	public static String getCurrentLogfilePath() {
		return logFilenameInUse;
	}

	private static boolean showOnGUI(int level) {
		return level != LEVEL_ESSENTIAL && level <= MIN_LEVEL_FOR_SHOWING_MESSAGES;
	}

	private static void addErrorToList(String mesg, String time) {
		while (errorList.size() >= MAX_LISTED_ERRORS) {
			errorList.remove(0);
		}
		errorList.add(String.format("[%s] %s", time, mesg));

		checkErrorList();
	}

	public static String getSize(Collection<String> c) {
		if (c == null) {
			return null;
		}

		return "" + c.size();
	}

	public static synchronized void log(int level, Object caller, Throwable ex, String message, Object... mesg) {
		if (level > logLevelConsole && level > logLevelFile) {
			return;
		}

		StringBuilder result = new StringBuilder(message);
		for (Object o : mesg) {
			// python style
			result.append(o).append(" ");
		}

		String currentTime = formatter.format(LocalDateTime.now());
		String context = Thread.currentThread().getName();
		if (caller instanceof Class) {
			result.append("   (").append(((Class<?>) caller).getName()).append(")");

		} else if (caller != null) {
			result.append("   (").append(caller.getClass().getName()).append(")");

		} else {
			// Do nothing if caller is null
		}

		String exMesg = "";
		if (ex != null) {
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			exMesg = "\n" + sw.toString();
		}

		String loggingMessage = result.toString();

		if (level <= logLevelConsole) {
			String format = COLORED_LOG_FORMAT.replace("{color}", LEVEL_TO_COLOR.get(level)).replace("{reset}",
					"\033[0m");

			System.err.println(String.format(format, level, currentTime, context, loggingMessage) + exMesg);
		}

		if (level <= logLevelFile) {
			if (!logFileInitialized) {
				initLogFile();
			}
			if (logFileWriter != null) {
				logFileWriter.println(String.format(logFormat, level, currentTime, context, loggingMessage) + exMesg);
				logFileWriter.flush();
			}
		}

		if (showOnGUI(level)) {
			addErrorToList(loggingMessage, currentTime);
		}
	}

	public static synchronized void log(int level, Object caller, Throwable ex, Object... mesg) {
		log(level, caller, ex, "", mesg);
	}

	public static void log(Object caller, int level, Throwable ex, Object... mesg) {
		log(level, caller, ex, mesg);
	}

	public static void log(Object caller, int level, Object... mesg) {
		log(level, caller, null, mesg);
	}

	public static void log(int level, Throwable ex, Object... mesg) {
		log(level, null, ex, mesg);
	}

	public static void log(int level, String message, Object... mesg) {
		log(level, null, (Throwable) null, message, mesg);
	}

	public static void secret(Object caller, Object... mesg) {
		log(caller, LEVEL_SECRET, mesg);
	}

	public static void secret(String message, Object... mesg) {
		log(LEVEL_SECRET, message, mesg);
	}

	public static void trace(Object caller, Object... mesg) {
		log(caller, LEVEL_TRACE, mesg);
	}

	public static void trace(String message, Object... mesg) {
		log(LEVEL_TRACE, message, mesg);
	}

	public static void debug(Object caller, Object... mesg) {
		log(caller, LEVEL_DEBUG, mesg);
	}

	public static void debug(String message, Object... mesg) {
		log(LEVEL_DEBUG, message, mesg);
	}

	public static void info(Object caller, Object... mesg) {
		log(caller, LEVEL_INFO, mesg);
	}

	public static void info(String message, Object... mesg) {
		log(LEVEL_INFO, message, mesg);
	}

	public static void notice(Object caller, Object... mesg) {
		log(caller, LEVEL_NOTICE, mesg);
	}

	public static void notice(String message, Object... mesg) {
		log(LEVEL_NOTICE, message, mesg);
	}

	public static void warning(Object caller, Object... mesg) {
		log(caller, LEVEL_WARNING, mesg);
	}

	public static void warning(String message, Object... mesg) {
		log(LEVEL_WARNING, message, mesg);
	}

	public static void warning(Throwable ex, Object... mesg) {
		log(LEVEL_WARNING, ex, mesg);
	}

	public static void warning(Object caller, Throwable ex, Object... mesg) {
		log(caller, LEVEL_WARNING, ex, mesg);
	}

	public static void error(Object caller, Object... mesg) {
		log(caller, LEVEL_ERROR, mesg);
	}

	public static void error(String message, Object... mesg) {
		log(LEVEL_ERROR, message, mesg);
	}

	public static void error(Throwable ex, Object... mesg) {
		log(LEVEL_ERROR, ex, mesg);
	}

	public static void error(Object caller, Throwable ex, Object... mesg) {
		log(caller, LEVEL_ERROR, ex, mesg);
	}

	public static void critical(Object caller, Object... mesg) {
		log(caller, LEVEL_CRITICAL, mesg);
	}

	public static void critical(String message, Object... mesg) {
		log(LEVEL_CRITICAL, message, mesg);
	}

	public static void critical(Object caller, Throwable ex, Object... mesg) {
		log(caller, LEVEL_CRITICAL, ex, mesg);
	}

	public static void essential(Object caller, Object... mesg) {
		log(caller, LEVEL_ESSENTIAL, mesg);
	}

	public static void essential(String message, Object... mesg) {
		log(LEVEL_ESSENTIAL, message, mesg);
	}

	public static void devel(Object caller, Object... mesg) {
		essential(caller, mesg);
	}

	public static void devel(String message, Object... mesg) {
		essential(message, mesg);
	}

	public static void clearErrorList() {
		errorList.clear();
	}

	// if errors Occurred show a window with the logged errors
	public static synchronized void checkErrorList() {
		int errorCount = errorList.size();

		info("error list size ", errorCount);

		if (errorCount == 0) {
			return;
		}

		if (dialog == null) {
			jTextArea = new JTextArea();
			jTextArea.setEditable(false);
			jTextArea.setLineWrap(true);
			jTextArea.setColumns(40);
			jTextArea.setRows(10);

			JScrollPane scrollPane = new JScrollPane(jTextArea);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			JOptionPane optionPane = new JOptionPane(scrollPane);

			dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("problemsOccurred"));
			dialog.setModal(false);

			/**
			 * Whenever the dialog is hidden, we want to clear the errorlist By
			 * trying out different listeners, this was the only one that worked
			 * reliably always and only when the dialog was closed, no matter
			 * which button or key was pressed
			 */
			dialog.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					clearErrorList();
				}
			});
		}

		// Get the text as a string, each element separated by a newline
		jTextArea.setText(errorList.toString().replace("[", "").replace("]", "").replace(",", "\n"));
		dialog.setVisible(true);
	}

	public static void setBaseLogDirectoryPath(String baseLogDirectoryPath) {
		Logging.baseLogDirectoryPath = baseLogDirectoryPath;
	}
}
