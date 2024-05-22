/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.type.ConfigOption;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
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
	private static Image mainIcon;
	private static boolean disableCertificateVerification;
	private static boolean isMultiFactorAuthenticationEnabled;

	private Utils() {
	}

	public static void showAboutAction(JFrame parent) {
		FTextArea info = new FTextArea(parent,
				Configed.getResourceValue("Utils.aboutOpsiConfiged") + " " + Globals.APPNAME, true,
				new String[] { Configed.getResourceValue("buttonClose") }, 500, 300);

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

		info.setMessage(message.toString());
		info.setVisible(true);
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

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase(Locale.ROOT).startsWith("windows");
	}

	public static ImageIcon getSaveIcon() {
		return getThemeIconPNG("images/save", "");
	}

	public static ImageIcon getIntellijIcon(String iconName) {
		String path = Globals.IMAGE_BASE + "intellij/" + iconName;

		path = path + ".svg";

		ColorFilter filter = new ColorFilter();

		filter.add(new Color(108, 112, 126),
				FlatLaf.isLafDark() ? Globals.OPSI_FOREGROUND_DARK : Globals.OPSI_FOREGROUND_LIGHT);
		FlatSVGIcon icon = new FlatSVGIcon(path);
		icon.setColorFilter(filter);

		return icon;
	}

	public static ImageIcon getThemeIconPNG(String pathName, String description) {
		pathName += FlatLaf.isLafDark() ? "_invert.png" : ".png";

		return createImageIcon(pathName, description);
	}

	public static ImageIcon createImageIcon(String path, String description) {
		String xPath = Globals.IMAGE_BASE + path;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL imgURL = cl.getResource(xPath);

		// should have the same result (but seems not to have)
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			Logging.info("Couldn't find file: " + path);
			return null;
		}
	}

	public static ImageIcon createImageIcon(String path, String description, int width, int height) {
		ImageIcon imageIcon = createImageIcon(path, description);

		if (imageIcon == null) {
			return null;
		}

		Image scaledImage = imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);

		return new ImageIcon(scaledImage, description);
	}

	public static void threadSleep(Object caller, long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
			Logging.info(caller, "sleeping interrupted: " + ie);
			Thread.currentThread().interrupt();
		}
	}

	public static List<Object> getNowTimeListValue() {
		return getNowTimeListValue(null);
	}

	public static List<Object> getNowTimeListValue(final String comment) {
		List<Object> result = new ArrayList<>();
		String now = new Timestamp(System.currentTimeMillis()).toString();
		now = now.substring(0, now.indexOf("."));
		if (comment != null) {
			result.add(now + " " + comment);
		} else {
			result.add(now);
		}
		Logging.info("getNowTimeListValue" + result);
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

	public static boolean isKeyForSecretValue(String s) {
		String t = s.toLowerCase(Locale.ROOT);
		return t.indexOf("password") > -1 || t.startsWith("secret");
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

	public static Image getMainIcon() {
		if (mainIcon == null) {
			mainIcon = createMainIcon();
		}
		return mainIcon;
	}

	private static Image createMainIcon() {
		Image mainIcon = null;

		String iconPath;

		iconPath = "gui/" + (Main.isLogviewer() ? Globals.ICON_LOGVIEWER : Globals.ICON_CONFIGED);

		URL resource = Globals.class.getResource(iconPath);
		if (resource == null) {
			Logging.debug("image resource " + iconPath + "  not found");
		} else {
			mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
		}
		return mainIcon;
	}

	public static void setDisableCertificateVerification(boolean disable) {
		disableCertificateVerification = disable;
	}

	public static boolean isCertificateVerificationDisabled() {
		return disableCertificateVerification;
	}

	public static void restrictAccessToFile(File file) {
		try {
			Logging.info(
					"Restricting file's " + file.getAbsolutePath() + " access to only allow owner to modify/read file");
			String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
			Logging.info("Detected operating system " + osName);
			if (osName.contains("win")) {
				boolean readablePermissionChanged = file.setReadable(true, true);
				boolean writablePermissionChanged = file.setWritable(true, true);
				Logging.info("Readable permission for file " + file.getAbsolutePath() + " changed "
						+ readablePermissionChanged);
				Logging.info("Writable permission for file " + file.getAbsolutePath() + " changed "
						+ writablePermissionChanged);
			} else {
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
				Files.setPosixFilePermissions(file.toPath(), permissions);
				Logging.info("Changed file's " + file.getAbsolutePath() + " permission");
			}
		} catch (IOException e) {
			Logging.error("Unable to set default permissions on temp file", e);
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

	public static List<String> takeAsStringList(List<Object> list) {
		List<String> result = new ArrayList<>();

		if (list == null) {
			return result;
		}

		for (Object val : list) {
			result.add((String) val);
		}

		return result;
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

	public static Map<String, Object> createUefiNOMEntry(String clientId, String val) {
		Map<String, Object> item = createNOMitem("ConfigState");
		List<String> values = new ArrayList<>();
		values.add(val);
		item.put("objectId", clientId);
		item.put("values", values);
		item.put("configId", OpsiServiceNOMPersistenceController.CONFIG_DHCPD_FILENAME);
		return item;
	}

	public static Map<String, Object> createNOMitem(String type) {
		Map<String, Object> item = new HashMap<>();
		item.put("type", type);
		return item;
	}

	public static Boolean interpretAsBoolean(Object ob, Boolean defaultValue) {
		Boolean result = false;

		if (ob == null) {
			result = defaultValue;
		} else if (ob instanceof Boolean b) {
			result = b;
		} else if (ob instanceof Integer integer) {
			result = integer == 1;
		} else if (ob instanceof String string) {
			result = "1".equals(string);
		} else {
			/* Not foreseen value. */
			Logging.warning("could not find boolean in interpretAsBoolean, returning false");
			result = false;
		}

		return result;
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

	public static String getListStringRepresentation(List<String> list, Integer max) {
		if (list == null || list.isEmpty()) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		int stop = list.size();
		if (max != null && stop > max) {
			stop = max;
		}

		for (int i = 0; i < stop - 1; i++) {
			result.append(list.get(i));
			result.append(";\n");
		}

		result.append(list.get(stop - 1));

		if (max != null && list.size() > max) {
			result.append(" ... ");
		}

		return result.toString();
	}

	public static boolean includeOpsiHostKey() {
		FTextArea f = new FTextArea(ConfigedMain.getMainFrame(), Configed.getResourceValue("securityWarning"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 400,
				200);
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message1"));
		message.append("\n\n");
		message.append(Configed.getResourceValue("Utils.opsiHostKey.message2"));
		f.setMessage(message.toString());
		f.setVisible(true);
		return f.getResult() == 2;
	}

	public static boolean hasPort(String host) {
		boolean result = false;

		if (host.contains("[") && host.contains("]")) {
			Logging.info("Host is IPv6: " + host);
			result = host.indexOf(":", host.indexOf("]")) != -1;
		} else {
			Logging.info("Host is either IPv4 or FQDN: " + host);
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
}
