/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.TextMarkdownPane;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.logging.Logging;

@SuppressWarnings({ "java:S1448" })
public final class Utils {
	private static final String COMPLETE_VERSION_INFO = System.getProperty("java.runtime.version");
	private static final int KIBI_BYTE = 1024;
	private static final String[] LOG_TYPES = new String[] { "bootimage", "clientconnect", "instlog", "opsiconfd",
			"userlogin" };

	private static final Set<String> BLACKLISTED_KEYWORDS_PASSWORD = Set.of("netboot.linux-bootimage.cmdline.pwh");
	private static final Set<String> WHITELISTED_KEYWORDS_PASSWORD = Set.of("netboot.use_host_onetime_password");

	private static Parser markdownParser = Parser.builder()
			.extensions(Arrays.asList(AutolinkExtension.create(), TablesExtension.create())).build();
	private static HtmlRenderer renderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();

	private static JFrame masterFrame;
	private static boolean disableCertificateVerification;
	private static boolean isMultiFactorAuthenticationEnabled;

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
		return LOG_TYPES;
	}

	public static String getLogType(int index) {
		return (index < 0 || index >= LOG_TYPES.length) ? "" : LOG_TYPES[index];
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

		if (BLACKLISTED_KEYWORDS_PASSWORD.contains(keyLowerCase)) {
			return true;
		} else if (WHITELISTED_KEYWORDS_PASSWORD.contains(keyLowerCase)) {
			return false;
		} else {
			return keyLowerCase.indexOf("password") > -1 || keyLowerCase.indexOf("secret") > -1;
		}
	}

	public static String parseMarkdown(String markdown) {
		if (markdown == null) {
			return "";
		}

		Node document = markdownParser.parse(markdown);
		return renderer.render(document);
	}

	public static String createTooltipForPropertyName(String propertyName, Map<String, Object> defaultsMap,
			Map<String, String> descriptionsMap, String additionalTooltipText) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {
			if (additionalTooltipText != null && !additionalTooltipText.isEmpty()) {
				tooltip.append("default (" + additionalTooltipText + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (Utils.isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append(Utils.parseMarkdown(descriptionsMap.get(propertyName)));
		}

		if (tooltip.length() > 200) {
			Logging.debug("tooltip length is ", tooltip.length());
			tooltip.insert(0, "<div style='width: 500px'>");
			tooltip.append("</div>");
		}

		return "<html>" + tooltip + "</html>";
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
			dir = PersistenceControllerFactory.getPersistenceController().getDataServices().config
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
		return switch (obj) {
		case Boolean bool -> bool;
		case String str -> Boolean.valueOf(str);
		default -> false;
		};
	}

	public static boolean compareNumeric(Object realData, long data, BiPredicate<Long, Long> comparison) {
		return switch (realData) {
		case Long longData -> comparison.test(longData, data);
		case Integer integerData -> comparison.test(integerData.longValue(), data);
		case Object o -> {
			Logging.error("data is no BigInteger!", o);
			yield false;
		}
		};
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
		case "server" -> Icons.getThemeSVGRepoIcon("server", size);
		case "notebook" -> Icons.getThemeSVGRepoIcon("laptop", size);
		case "desktop" -> Icons.getThemeSVGRepoIcon("desktop", size);
		case "virtual_machine" -> Icons.getThemeSVGRepoIcon("virtualMachine", size);
		case "convertible" -> Icons.getThemeSVGRepoIcon("convertible", size);
		case "other" -> Icons.getThemeIntellijIcon("questionMark", size);
		default -> Icons.getThemeIntellijIcon("questionMark", size);
		};
	}

	public static void addKeyBindingToJComponent(JComponent component, KeyStroke keyStroke, Runnable runnable,
			int condition) {
		Logging.info(keyStroke.toString(), " added to ", component.getClass().getSimpleName());
		component.getInputMap(condition).put(keyStroke, keyStroke.toString());
		component.getActionMap().put(keyStroke.toString(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Logging.debug(component.getClass().getSimpleName(), " ", keyStroke.toString(), " triggered");
				runnable.run();
			}
		});
	}

	public static void addKeyBindingToJComponent(JComponent component, KeyStroke keyStroke, Runnable runnable) {
		Logging.info(keyStroke.toString(), " added to ", component.getClass().getSimpleName());
		addKeyBindingToJComponent(component, keyStroke, runnable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	public static DocumentListener onDocumentChange(Runnable runnable) {
		return onDocumentChange(runnable, true);
	}

	public static DocumentListener onDocumentChangeWithoutRemoveUpdate(Runnable runnable) {
		return onDocumentChange(runnable, false);
	}

	private static DocumentListener onDocumentChange(Runnable runnable, boolean reactOnChangeUpdate) {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				runnable.run();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (reactOnChangeUpdate) {
					runnable.run();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				runnable.run();
			}
		};
	}

	public static JLabel createBoldLabel(String ressourceId) {
		JLabel label = new JLabel(Configed.getResourceValue(ressourceId));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}

	public static void runOnEventDispatchThread(Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runnable);
		} else {
			runnable.run();
		}
	}

	public static String formatDateTimeStringToLocal(LocalDateTime dateTime) {
		return dateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
				.format(DATE_TIME_FORMATTER);
	}

	public static String formatDateTimeStringToLocal(String dateTimeString) {
		try {
			return formatDateTimeStringToLocal(LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER));
		} catch (DateTimeParseException e) {
			Logging.warning(e, "Could not parse date time string: ", dateTimeString);
			return dateTimeString;
		}
	}

	public static void formatDateTimeStringForMap(Map<String, Object> map, String key) {
		if (map.get(key) instanceof String timestampString && !timestampString.isEmpty()) {
			map.put(key, formatDateTimeStringToLocal(timestampString));
		}
	}

	public static <T> void runSwingWorker(Supplier<T> backgroundTask, Consumer<T> doneTask,
			Consumer<Exception> exceptionHandler) {

		Consumer<Exception> finalExceptionHandler = exceptionHandler != null ? exceptionHandler : ((Exception e) -> {
		});

		SwingWorker<T, Void> worker = new SwingWorker<>() {
			@Override
			protected T doInBackground() {
				return backgroundTask.get();
			}

			@Override
			protected void done() {
				try {
					T result = get();
					doneTask.accept(result);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					finalExceptionHandler.accept(e);
				} catch (ExecutionException e) {
					finalExceptionHandler.accept(e);
				}
			}
		};
		worker.execute();
	}
}
