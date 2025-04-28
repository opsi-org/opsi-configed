/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.productpage.TextMarkdownPane;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.type.ConfigOption;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import javafx.application.Application;
import javafx.stage.Stage;

@SuppressWarnings({ "java:S1448" })
public final class Utils {
	private static final String COMPLETE_VERSION_INFO = System.getProperty("java.runtime.version");
	private static final int KIBI_BYTE = 1024;
	private static final String[] LOG_TYPES = new String[] { "clientconnect", "instlog", "userlogin", "bootimage",
			"opsiconfd" };
	private static final int[] MAX_LOG_SIZES = new int[] { 4 * KIBI_BYTE * KIBI_BYTE, 8 * KIBI_BYTE * KIBI_BYTE,
			8 * KIBI_BYTE * KIBI_BYTE, 0, 1 * KIBI_BYTE * KIBI_BYTE };

	private static JFrame masterFrame;
	private static boolean disableCertificateVerification;
	private static boolean isMultiFactorAuthenticationEnabled;

	private Utils() {
	}

	public static void showAboutAction(JFrame parent) {
		StringBuilder message = new StringBuilder();
		message.append(Globals.APPNAME + "  " + Configed.getResourceValue("LoginDialog.version") + "  "
				+ Globals.VERSION + " (" + Globals.VERDATE + ") ");
		message.append("\n");
		message.append("The opsi-logviewer is part of the " + Globals.APPNAME + " since version 4.2.22.1\n");
		message.append("______________________________________________________________________\n");
		message.append("\n");
		message.append(Globals.COPYRIGHT1 + "\n");
		message.append(Globals.COPYRIGHT2 + "\n");
		message.append("\n");
		message.append("running on java version " + COMPLETE_VERSION_INFO + "\n");
		message.append("on architecture " + System.getProperty("os.arch"));

		JTextArea jTextArea = new JTextArea(message.toString());
		jTextArea.setEditable(false);
		jTextArea.setCaretPosition(0);

		JOptionPane.showMessageDialog(parent, jTextArea,
				Configed.getResourceValue("Utils.aboutOpsiConfiged") + " " + Globals.APPNAME,
				JOptionPane.PLAIN_MESSAGE);
	}

	public static void showCreditsAction(JFrame parent) {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("FCreditsDialog.message1"));
		message.append("<br>");
		message.append(Configed.getResourceValue("FCreditsDialog.message2"));
		message.append("<br><br>");
		message.append(Configed.getResourceValue("FCreditsDialog.message3"));
		message.append("<br><br>");
		appendCreditsFromFile(message);

		TextMarkdownPane jTextPane = new TextMarkdownPane();
		jTextPane.setText(message.toString());
		jTextPane.setPreferredSize(new Dimension(jTextPane.getPreferredSize().width, 200));

		JScrollPane scrollpane = new JScrollPane(jTextPane);

		JOptionPane.showMessageDialog(parent, scrollpane, Configed.getResourceValue("MainFrame.jMenuHelpCredits"),
				JOptionPane.PLAIN_MESSAGE);
	}

	public static void showMissingLicenseModules(String message) {
		TextMarkdownPane jTextArea = new TextMarkdownPane();
		jTextArea.setText(message);

		JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(), jTextArea,
				Configed.getResourceValue("Permission.modules.title"), JOptionPane.WARNING_MESSAGE);
	}

	private static void appendCreditsFromFile(StringBuilder message) {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("credits.md"),
						StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				message.append(line + "<br>");
			}
		} catch (IOException e) {
			Logging.warning(e, "unable to read credits file");
		}
	}

	public static List<String> readLocallySavedServerNames() {
		TreeMap<Long, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.getSavedStatesLocationName() != null) {
			Logging.info("trying to find saved states in ", Configed.getSavedStatesLocationName());

			savedStatesLocation = new File(Configed.getSavedStatesLocationName());
			savedStatesLocation.mkdirs();
			success = savedStatesLocation.setReadable(true);
		}

		if (!success) {
			Logging.warning("cannot not find saved states in ", Configed.getSavedStatesLocationName());
		}

		if (Configed.getSavedStatesLocationName() == null || !success) {
			Logging.info("searching saved states in ", Utils.getSavedStatesDefaultLocation());
			savedStatesLocation = new File(Utils.getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		Logging.info("saved states location ", savedStatesLocation);

		File[] subdirs = null;

		if (savedStatesLocation != null) {
			subdirs = savedStatesLocation.listFiles(File::isDirectory);

			for (File folder : subdirs) {
				File checkFile = new File(folder + File.separator + Configed.SAVED_STATES_FILENAME);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

				if (elementname.lastIndexOf("_") > -1) {
					elementname = elementname.replace("_", ":");
				}

				sortingmap.put(checkFile.lastModified(), elementname);
			}
		}

		List<String> result = new ArrayList<>();
		for (Long l : sortingmap.descendingKeySet()) {
			result.add(sortingmap.get(l));
		}

		Logging.info("readLocallySavedServerNames  result ", result);

		return result;
	}

	public static String[] getLogTypes() {
		return LOG_TYPES.clone();
	}

	public static String getLogType(int index) {
		return (index < 0 || index >= LOG_TYPES.length) ? "" : LOG_TYPES[index];
	}

	public static int getMaxLogSize(int index) {
		if (index < 0 || index >= MAX_LOG_SIZES.length) {
			Logging.warning("error with index for maxLogSizes");
			return -1;
		}
		return MAX_LOG_SIZES[index];
	}

	public static void threadSleep(Object caller, long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Logging.info(caller, "sleeping interrupted: ", ie);
			Thread.currentThread().interrupt();
		}
	}

	public static List<Object> getNowTimeListValue() {
		List<Object> result = new ArrayList<>();
		String now = new Timestamp(System.currentTimeMillis()).toString();
		now = now.substring(0, now.indexOf("."));
		result.add(now);
		Logging.info("getNowTimeListValue", result);
		return result;
	}

	public static void showExternalDocument(String link) {
		new Application() {
			@Override
			public void start(Stage primaryStage) throws Exception {
				// Empty, because not needed
			}
		}.getHostServices().showDocument(link);
	}

	public static String pseudokey(String[] partialvalues) {
		StringBuilder resultBuilder = new StringBuilder();

		if (partialvalues.length > 0) {
			resultBuilder.append(partialvalues[0]);

			for (int i = 1; i < partialvalues.length; i++) {
				resultBuilder.append(";");
				resultBuilder.append(partialvalues[i]);
			}
		}

		return resultBuilder.toString();
	}

	public static String usedMemory() {
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		return " " + (((total - free) / KIBI_BYTE) / KIBI_BYTE) + " MB ";
	}

	public static String getCLIPasswordParam(String question) {
		return getCLIParam(question, true);
	}

	public static String getCLIParam(String question) {
		return getCLIParam(question, false);
	}

	@SuppressWarnings({ "java:S106" })
	private static String getCLIParam(String question, boolean password) {
		Console con = System.console();
		if (con == null) {
			return "";
		}

		System.out.print(question);
		if (password) {
			return String.valueOf(con.readPassword()).trim();
		}

		try (Scanner sc = new Scanner(con.reader())) {
			return sc.nextLine();
		}
	}

	public static boolean isKeyForSecretValue(String key) {
		String keyLowerCase = key.toLowerCase(Locale.ROOT);
		return keyLowerCase.indexOf("password") > -1 || keyLowerCase.indexOf("secret") > -1;
	}

	public static String getSeconds() {
		String sqlNow = new Timestamp(System.currentTimeMillis()).toString();
		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i + 1);
		time = time.substring(0, time.indexOf('.'));
		return date + "_" + time;
	}

	public static String getDate() {
		String sqlNow = new Timestamp(System.currentTimeMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));
		return sqlNow;
	}

	public static void setMultiFactorAuthenticationEnabled(boolean enabled) {
		isMultiFactorAuthenticationEnabled = enabled;
	}

	public static boolean isMultiFactorAuthenticationEnabled() {
		return isMultiFactorAuthenticationEnabled;
	}

	public static void setMasterFrame(JFrame frame) {
		masterFrame = frame;
	}

	public static JFrame getMasterFrame() {
		return masterFrame;
	}

	public static void setDisableCertificateVerification(boolean disable) {
		disableCertificateVerification = disable;
	}

	public static boolean isCertificateVerificationDisabled() {
		return disableCertificateVerification;
	}

	public static void restrictAccessToFile(File file) {
		try {
			Logging.info("Restricting file's ", file.getAbsolutePath(),
					" access to only allow owner to modify/read file");
			String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			Logging.info("Detected operating system ", osName);
			if (osName.contains("win")) {
				boolean readablePermissionChanged = file.setReadable(true, true);
				boolean writablePermissionChanged = file.setWritable(true, true);
				Logging.info("Readable permission for file ", file.getAbsolutePath(), " changed ",
						readablePermissionChanged);
				Logging.info("Writable permission for file ", file.getAbsolutePath(), " changed ",
						writablePermissionChanged);
			} else {
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
				Files.setPosixFilePermissions(file.toPath(), permissions);
				Logging.info("Changed file's ", file.getAbsolutePath(), " permission");
			}
		} catch (IOException e) {
			Logging.error(e, "Unable to set default permissions on temp file");
		}
	}

	public static String getDomainFromClientName(String clientName) {
		StringBuilder sb = new StringBuilder();
		String[] splittedClientName = clientName.split("\\.");
		for (int i = 1; i < splittedClientName.length; i++) {
			sb.append(splittedClientName[i]);
			if (i != splittedClientName.length - 1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	public static Map<String, Object> createNOMConfig(ConfigOption.TYPE type, String key, String description,
			boolean editable, boolean multiValue, List<Object> defaultValues, List<Object> possibleValues) {
		Map<String, Object> item = createNOMitem(type.toString());
		item.put("id", key.toLowerCase(Locale.ROOT));
		item.put("description", description);
		item.put("editable", editable);
		item.put("multiValue", multiValue);
		item.put("defaultValues", defaultValues);
		item.put("possibleValues", possibleValues);
		return item;
	}

	public static Map<String, Object> createNOMBoolConfig(String key, Boolean value, String description) {
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(true);
		possibleValues.add(false);
		return createNOMConfig(ConfigOption.TYPE.BOOL_CONFIG, key, description, false, false, defaultValues,
				possibleValues);
	}

	public static Map<String, Object> createNOMitem(String type) {
		Map<String, Object> item = new HashMap<>();
		item.put("type", type);
		return item;
	}

	public static String getSavedStatesDefaultLocation() {
		String result;

		if (System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) != null) {
			result = System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) + File.separator + "opsi.org"
					+ File.separator + "configed";
		} else {
			result = System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY) + File.separator + ".configed";
		}

		return result;
	}

	public static String getListStringRepresentation(List<String> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}

		final int MAX = 5;

		StringBuilder result = new StringBuilder();
		int stop = list.size();
		if (stop > MAX) {
			stop = MAX;
		}

		for (int i = 0; i < stop - 1; i++) {
			result.append(list.get(i));
			result.append(";\n");
		}

		result.append(list.get(stop - 1));

		if (list.size() > MAX) {
			result.append(" ... ");
		}

		return result.toString();
	}

	public static boolean includeOpsiHostKey() {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message1"));
		message.append("\n\n");
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message2"));

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("securityWarning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		return answer == JOptionPane.YES_OPTION;
	}

	public static boolean hasPort(String host) {
		boolean result = false;
		if (host == null || host.isEmpty()) {
			return result;
		}
		if (host.contains("[") && host.contains("]")) {
			Logging.info("Host is IPv6: ", host);
			result = host.indexOf(":", host.indexOf("]")) != -1;
		} else {
			Logging.info("Host is either IPv4 or FQDN: ", host);
			result = host.contains(":");
		}

		return result;
	}

	public static String getServerPathFromWebDAVPath(String webDAVPath) {
		String dir = "";
		if (webDAVPath.startsWith("workbench")) {
			dir = PersistenceControllerFactory.getPersistenceController().getConfigDataService()
					.getConfigedWorkbenchDefaultValuePD();
			if (dir.charAt(dir.length() - 1) != '/') {
				dir = dir + "/";
			}
			dir = dir + retrieveEverythingAfterDir(webDAVPath, "workbench/");
		} else if (webDAVPath.startsWith("repository")) {
			dir = CommandFactory.OPSI_PATH_VAR_REPOSITORY + retrieveEverythingAfterDir(webDAVPath, "repository/");
		} else if (webDAVPath.startsWith("depot")) {
			dir = CommandFactory.OPSI_PATH_VAR_DEPOT + retrieveEverythingAfterDir(webDAVPath, "depot/");
		} else {
			Logging.warning("expected repository or workbench");
		}
		return dir;
	}

	private static String retrieveEverythingAfterDir(String filePath, String dir) {
		return filePath.substring(filePath.indexOf(dir) + dir.length());
	}

	public static void enableDialogResizing(JOptionPane optionPane) {
		HierarchyListener listener = new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				Window window = SwingUtilities.getWindowAncestor(optionPane);
				if (window instanceof Dialog dialog && !dialog.isResizable()) {
					dialog.setResizable(true);
					optionPane.removeHierarchyListener(this);
				}
			}
		};
		optionPane.addHierarchyListener(listener);
	}

	public static Boolean toBoolean(Object obj) {
		if (obj instanceof Boolean bool) {
			return bool;
		}
		if (obj instanceof String) {
			return Boolean.valueOf(obj.toString());
		}
		return false;
	}

	public static FlatSVGIcon determineIconBasedOnPlatform(String platform, int size) {
		return switch (platform) {
		case "macos" -> Icons.getThemeSVGRepoIcon("macos", size);
		case "windows" -> Icons.getThemeSVGRepoIcon("windows", size);
		case "linux" -> Icons.getThemeSVGRepoIcon("linux", size);
		default -> Icons.getThemeIntellijIcon("questionMark", size);
		};
	}

	public static FlatSVGIcon determineIconBasedOnDeviceType(String value) {
		return determineIconBasedOnDeviceType(value, 16);
	}

	public static FlatSVGIcon determineIconBasedOnDeviceType(String value, int size) {
		return switch (value) {
		case "notebook" -> Icons.getThemeSVGRepoIcon("laptop", size);
		case "desktop" -> Icons.getThemeSVGRepoIcon("desktop", size);
		case "virtual_machine" -> Icons.getThemeSVGRepoIcon("virtualMachine", size);
		case "convertible" -> Icons.getThemeSVGRepoIcon("convertible", size);
		case "other" -> Icons.getThemeIntellijIcon("questionMark", size);
		default -> Icons.getThemeIntellijIcon("questionMark", size);
		};
	}
}
