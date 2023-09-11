/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package utils;

import java.awt.Dimension;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.CopyrightInfos;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import javafx.application.Application;
import javafx.stage.Stage;

public final class Utils {
	private static final int KIBI_BYTE = 1024;
	private static final String[] LOG_TYPES = new String[] { "clientconnect", "instlog", "userlogin", "bootimage",
			"opsiconfd" };
	private static final int[] MAX_LOG_SIZES = new int[] { 4 * KIBI_BYTE * KIBI_BYTE, 8 * KIBI_BYTE * KIBI_BYTE,
			8 * KIBI_BYTE * KIBI_BYTE, 0, 1 * KIBI_BYTE * KIBI_BYTE };

	private static JFrame masterFrame;
	private static Image mainIcon;
	private static Map<String, Object> objects;
	private static boolean disableCertificateVerification;
	private static boolean isMultiFactorAuthenticationEnabled;

	private Utils() {
	}

	public static void formatButtonSmallText(AbstractButton button) {
		if (!Main.FONT) {
			button.setFont(Globals.DEFAULT_FONT_SMALL);
		}
		button.setPreferredSize(new Dimension(45, 20));
		if (!Main.THEMES) {
			button.setForeground(Globals.LIGHT_BLACK);
			button.setBackground(Globals.BACKGROUND_COLOR_6);
		}
		button.setOpaque(false);

		button.setBorderPainted(false);

	}

	public static void showAboutAction(JFrame parent) {
		FTextArea info = new FTextArea(parent, Globals.APPNAME + " Copyright Information", true,
				new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 500, 300);

		StringBuilder message = new StringBuilder();

		for (String line : CopyrightInfos.get()) {
			message.append("\n");
			message.append(line);
		}

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

	public static Map<String, Object> getMap() {
		if (objects == null) {
			objects = new HashMap<>();
			objects.put("mainIcon", getMainIcon());
			objects.put("defaultFont", Globals.DEFAULT_FONT);
			objects.put("APPNAME", Globals.APPNAME);
		}
		return new HashMap<>(objects);
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase(Locale.ROOT).startsWith("windows");
	}

	public static String fillStringToLength(String s, int len) {
		if (s.length() > len) {
			return s;
		}

		StringBuilder result = new StringBuilder(s);
		for (int i = s.length(); i < len; i++) {
			result.append(' ');
		}
		return result.toString();
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
		StringBuilder resultBuilder = new StringBuilder("");

		if (partialvalues.length > 0) {
			resultBuilder.append(partialvalues[0]);

			for (int i = 1; i < partialvalues.length; i++) {
				resultBuilder.append(";");
				resultBuilder.append(partialvalues[i]);
			}
		}

		return resultBuilder.toString();
	}

	public static boolean checkCollection(Object source, String cName, Object c) {
		boolean result = c != null;
		if (result) {
			if (!(c instanceof Collection) && !(c instanceof Map)) {
				Logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		} else {
			Logging.debug(source.getClass().getName() + " " + cName + " is null");
		}
		return result;
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

		if (Main.THEMES) {
			iconPath = "gui/" + Globals.ICON_OPSI;
		} else {
			iconPath = Globals.ICON_RESOURCE_NAME;
		}

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
}
