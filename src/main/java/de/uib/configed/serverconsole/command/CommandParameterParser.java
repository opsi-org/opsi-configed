/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.ValueSelectorList;
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public final class CommandParameterParser {
	public static final String REPLACEMENT_DEFAULT_1 = "<<<";
	public static final String REPLACEMENT_DEFAULT_2 = ">>>";
	public static final String PARAM_SPLITTER_DEFAULT = "><";

	public static final Pattern paramSplitterDefaultPattern = Pattern.compile(PARAM_SPLITTER_DEFAULT);

	public static final String METHOD_INTERACTIVE_ELEMENT = Configed
			.getResourceValue("CommandControlDialog.cbElementInteractiv");
	public static final String METHOD_GET_SELECTED_CLIENT_NAMES = Configed
			.getResourceValue("CommandParameterParser.method.getSelectedClientNames");
	public static final String METHOD_GET_SELECTED_CLIENT_IPS = Configed
			.getResourceValue("CommandParameterParser.method.getSelectedClientIPs");
	public static final String METHOD_GET_SELECTED_DEPOT_NAMES = Configed
			.getResourceValue("CommandParameterParser.method.getSelectedDepotNames");
	public static final String METHOD_GET_SELECTED_DEPOT_IPS = Configed
			.getResourceValue("CommandParameterParser.method.getSelectedDepotIPs");
	public static final String METHOD_GET_CONFIG_SERVER_NAME = Configed
			.getResourceValue("CommandParameterParser.method.getConfigServerName");
	public static final String METHOD_OPTION_SELECTION = Configed
			.getResourceValue("CommandParameterParser.method.optionSelection");

	private static final String BRACKETS_NONE = " x ";
	private static final String BRACKETS_SQUARE = "[x]";

	private static final Map<String, String> methods = new HashMap<>();

	private ConfigedMain configedMain;

	private String[] formats;

	private boolean canceled;
	private Component outputDia;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public CommandParameterParser(ConfigedMain configedMain) {
		methods.put(METHOD_INTERACTIVE_ELEMENT, METHOD_INTERACTIVE_ELEMENT);
		methods.put(METHOD_GET_SELECTED_CLIENT_NAMES, "getSelectedClientNames");
		methods.put(METHOD_GET_SELECTED_CLIENT_IPS, "getSelectedClientIPs");
		methods.put(METHOD_GET_SELECTED_DEPOT_NAMES, "getSelectedDepotNames");
		methods.put(METHOD_GET_SELECTED_DEPOT_IPS, "getSelectedDepotIPs");
		methods.put(METHOD_GET_CONFIG_SERVER_NAME, "getConfigServerName");
		methods.put(METHOD_OPTION_SELECTION, "script://path/to/file");

		this.configedMain = configedMain;
		init();
	}

	public static String[] getParameterMethodLocalNames() {
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Entry<String, String> entry : methods.entrySet()) {
			mymethods[counter] = entry.getKey();
			counter++;
		}
		Arrays.sort(mymethods);
		return mymethods;
	}

	public static String[] getParameterMethods() {
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Entry<String, String> entry : methods.entrySet()) {
			mymethods[counter] = entry.getValue();
			counter++;
		}
		return mymethods;
	}

	public static String getMethodFromName(String name) {
		for (Entry<String, String> entry : methods.entrySet()) {
			if (name.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		return name;
	}

	public String[] getParameterFormats() {
		if (formats != null) {
			return formats;
		}
		return new String[0];
	}

	public SingleCommand parseParameter(final SingleCommand command, CommandExecutor caller) {
		Logging.info(this, "parseParameter command " + command.getCommandRaw());
		if (caller instanceof CommandExecutor) {
			outputDia = caller.getDialog();
		} else {
			Logging.warning(this, "caller has unexpected type in parseParameter " + caller.getClass());
		}
		SingleCommand commandCopy = new SingleCommandTemplate(command);
		List<String> params = command.getParameterList();
		for (String param : params) {
			if (command.getCommandRaw().contains(param)) {
				String[] splittedParameter = splitParameter(param);
				String result = callMethod(splittedParameter[0], splittedParameter[1]);
				if (result == null) {
					canceled = true;
				} else {
					Logging.debug(this, "parseParameter command " + command.getCommandRaw());
					Logging.debug(this, "parseParameter param " + param);
					Logging.debug(this, "parseParameter result " + result);
					commandCopy.setCommand(commandCopy.getCommandRaw().replace(param, result));
					Logging.debug(this, "parseParameter command " + command.getCommandRaw());
				}
			}
		}
		Logging.info(this, "parseParameter command " + commandCopy.getCommandRaw());
		return commandCopy;
	}

	public String parseParameterToString(SingleCommand command, CommandExecutor caller) {
		SingleCommand c = parseParameter(command, caller);
		return c.getCommandRaw();
	}

	public String testParameter(String param) {
		String[] splittedParameter = splitParameter(param);
		String result = callMethod(splittedParameter[0], splittedParameter[1]);
		if (result == null) {
			return Configed.getResourceValue("CommandControlDialog.parameterTest.failed");
		}

		if (result.contains("script://")) {
			return result.replace("script://", "");
		} else {
			return result;
		}
	}

	private void init() {
		formats = new String[9];
		formats[0] = "x y z ...";
		formats[1] = "x,y,z,...";
		formats[2] = "[x,y,z,...]";
		formats[3] = "'x' 'y' 'z' '...'";
		formats[4] = "'x', 'y', 'z', '...'";
		formats[5] = "['x','y','z','...']";
		formats[6] = "\"x\" \"y\" \"z\" \"...\"";
		formats[7] = "\"x\",\"y\",\"z\",\"...\"";
		formats[8] = "[\"x\",\"y\",\"z\",\"...\"]";
	}

	public String[] splitParameter(String m) {
		Logging.info(this, "splitParameter param " + m);
		if (m.startsWith(REPLACEMENT_DEFAULT_1) && m.contains(REPLACEMENT_DEFAULT_2)) {
			m = m.replace(REPLACEMENT_DEFAULT_1, "").replace(REPLACEMENT_DEFAULT_2, "");
		}

		Logging.info(this, "splitParameter param " + m);
		String[] splitted = new String[2];
		splitted[0] = m;
		splitted[1] = "";

		if (m.contains(PARAM_SPLITTER_DEFAULT)) {
			splitted = paramSplitterDefaultPattern.split(m);

			Logging.info(this, "splitParameter method " + splitted[0]);
			Logging.info(this, "splitParameter format " + splitted[1]);
		}

		return splitted;
	}

	public static String getTranslatedMethod(String localeMethod) {
		String method = "";
		for (Entry<String, String> entry : methods.entrySet()) {
			if (entry.getKey().equals(localeMethod)) {
				method = entry.getValue();
			}
		}
		return method;
	}

	private String callMethod(String method, String format) {
		Logging.info(this, "callMethod method " + method + " format " + format);
		String result = "";
		method = method.trim();
		if (method.equals(methods.get(METHOD_GET_SELECTED_CLIENT_NAMES))) {
			Logging.info(this, "getSelected_clientnames " + getSelectedClientNames());
			result = formatResult(getSelectedClientNames(), format);
		} else if (method.equals(methods.get(METHOD_GET_SELECTED_CLIENT_IPS))) {
			Logging.info(this, "getSelected_clientIPs " + getSelectedClientIPs());
			result = formatResult(getSelectedClientIPs(), format);
		} else if (method.equals(methods.get(METHOD_GET_SELECTED_DEPOT_NAMES))) {
			result = formatResult(getSelectedDepotNames(), format);
		} else if (method.equals(methods.get(METHOD_GET_SELECTED_DEPOT_IPS))) {
			result = formatResult(getSelectedDepotIPs(), format);
		} else if (method.equals(methods.get(METHOD_GET_CONFIG_SERVER_NAME))) {
			result = formatResult(getConfigServerName(), format);
		} else if (method.contains("script://")) {
			result = getSelectedValue(method);
			Logging.info(this, "callMethod replace \"" + method + "\" with \"" + result + "\"");
		} else if (format.isEmpty()) {
			result = getUserText(method, outputDia);
			Logging.info(this, "callMethod replace \"" + method + "\" with \"" + result + "\"");
		} else {
			Logging.warning(this, "unexpected method " + method + " and format " + format);
		}

		return result;
	}

	private String formatResult(String result, String format) {
		String[] strarr = new String[1];
		strarr[0] = result;
		return formatResult(strarr, format);
	}

	private String formatResult(String[] result, String format) {
		String formatedResult = "";
		String f = format.replace(" ", "");
		Logging.info(this, "callMethod format f " + f);
		switch (f) {
		case "xyz":
		case "xyz...":
			formatedResult = Arrays.toString(result).replace("[", "").replace(",", " ").replace("]", "");
			break;
		case "x,y,z":
		case "x,y,z,...":
			formatedResult = Arrays.toString(result).replace("[", "").replace("]", "");
			break;
		case "[x,y,z]":
		case "[x,y,z,...]":
			formatedResult = Arrays.toString(result);
			break;

		case "'x''y''z'":
		case "'x''y''z''...'":
			Logging.info(this, "formatResult switch case [3] " + "'x''y''z''...'" + " || " + "'x''y''z'");
			formatedResult = createFormattedDataSourceString(result, "'", BRACKETS_NONE, " ");
			break;
		case "'x','y','z'":
		case "'x','y','z','...'":
			Logging.info(this, "formatResult switch case [3] " + "'x''y''z''...'" + " || " + "'x''y''z'");
			formatedResult = createFormattedDataSourceString(result, "'", BRACKETS_NONE, ",");
			break;
		case "\"x\"\"y\"\"z\"":
		case "\"x\"\"y\"\"z\"\"...\"":
			Logging.info(this, "formatResult switch case [4] " + "\"x\"\"y\"\"z\"\"...\"" + " || " + "\"x\"\"y\"\"z\"");
			formatedResult = createFormattedDataSourceString(result, "\"", BRACKETS_NONE, " ");
			break;
		case "\"x\",\"y\",\"z\"":
		case "\"x\",\"y\",\"z\",\"...\"":
			Logging.info(this,
					"formatResult switch case [5] " + "\"x\",\"y\",\"z\",\"...\"" + " || " + "\"x\",\"y\",\"z\"");
			formatedResult = createFormattedDataSourceString(result, "\"", BRACKETS_NONE, ",");
			break;
		case "['x','y','z']":
		case "['x','y','z','...']":
			Logging.info(this, "formatResult switch case [5] " + "['x','y','z']" + " || " + "['x','y','z','...']");
			formatedResult = createFormattedDataSourceString(result, "'", BRACKETS_SQUARE, ",");
			break;
		case "[\"x\",\"y\",\"z\"]":
		case "[\"x\",\"y\",\"z\",\"...\"]":
			Logging.info(this,
					"formatResult switch case [5] " + "[\"x\",\"y\",\"z\"]" + " || " + "[\"x\",\"y\",\"z\",\"...\"]");
			formatedResult = createFormattedDataSourceString(result, "\"", BRACKETS_SQUARE, ",");
			break;
		default:
			Logging.warning(this, "cannot format into \"" + format + "\" with \"" + Arrays.toString(result) + "\"");
			break;
		}
		return formatedResult;
	}

	private String createFormattedDataSourceString(String[] strArr, String beginEndElement, String beginEndString,
			String separator) {
		replaceElements(strArr, beginEndElement);
		Logging.info(this, "createFormattedDataSourceString[ ]  strArr " + Arrays.toString(strArr));
		String formatedResult = createStringOfArray(strArr, beginEndString, separator);
		Logging.info(this, "createFormattedDataSourceString[ ] formated_result " + formatedResult);

		return formatedResult;
	}

	private void replaceElements(String[] strArrToReplace, String beginEndOfElement) {
		for (int i = 0; i < strArrToReplace.length; i++) {
			strArrToReplace[i] = strArrToReplace[i].replace(strArrToReplace[i],
					beginEndOfElement + strArrToReplace[i] + beginEndOfElement);
			Logging.info(this, "formatResult[] result[i] " + strArrToReplace[i]);
		}
	}

	private String createStringOfArray(String[] strArrToReplace, String beginEndOfString, String separator) {
		String result;

		Logging.info(this, "createStringOfArray strArrToReplace " + strArrToReplace);
		Logging.info(this, "createStringOfArray strArrToReplace.length " + strArrToReplace.length + "if statement: "
				+ (strArrToReplace.length > 1));
		if (strArrToReplace.length > 1) {
			result = Arrays.toString(strArrToReplace).replace("[", beginEndOfString.split("x")[0])
					.replace(",", separator).replace("]", beginEndOfString.split("x")[1]);
		} else {
			result = Arrays.toString(strArrToReplace).replace("[", beginEndOfString.split("x")[0]).replace("]",
					beginEndOfString.split("x")[1]);
		}
		Logging.info(this, "createStringOfArray result " + result);
		return result;
	}

	public static String arrayToStringAsList(Object[] list) {
		return Arrays.toString(list).replace("[", "").replace("]", "");
	}

	public static String arrayToString(Object[] list) {
		return Arrays.toString(list).replace("[", "").replace(",", " ").replace("]", "");
	}

	private String getUserText(String text, Component dialog) {
		if (dialog == null) {
			dialog = ConfigedMain.getMainFrame();
		}
		Logging.debug(this, "getUserText text " + text);
		final JTextField field = new JTextField();

		final JOptionPane opPane = new JOptionPane(new Object[] { new JLabel(text), field },
				JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				super.selectInitialValue();
				((Component) field).requestFocusInWindow();
			}
		};
		final JDialog jdialog = opPane.createDialog(dialog,
				Globals.APPNAME + " " + Configed.getResourceValue("CommandParameterParser.Input"));
		jdialog.setSize(400, 150);
		jdialog.setVisible(true);

		if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION) {
			return field.getText().trim();
		}

		return null;
	}

	private String getConfigServerName() {
		List<String> depots = persistenceController.getHostInfoCollections().getDepotNamesList();
		for (String depot : depots) {
			if (depot.startsWith(ConfigedMain.getHost())) {
				Logging.debug(this, "getConfig_serverName " + ConfigedMain.getHost());
				return depot;
			}
		}

		Logging.debug(this, "getConfig_serverName " + ConfigedMain.getHost());

		return ConfigedMain.getHost();
	}

	private String[] getSelectedClientIPs() {
		Logging.debug(this, "getSelected_clientIPs " + configedMain.getSelectedClients());

		String[] clientIPs = new String[configedMain.getSelectedClients().size()];
		int counter = 0;
		for (String name : configedMain.getSelectedClients()) {
			HostInfo hostInfo = persistenceController.getHostInfoCollections().getMapOfAllPCInfoMaps().get(name);
			if (hostInfo != null) {
				clientIPs[counter] = hostInfo.getIpAddress();
				counter++;
			} else {
				Logging.debug(this, "getSelected_clientIPs host " + name + " HostInfo null");
			}
		}
		return clientIPs;
	}

	private String[] getSelectedClientNames() {
		Logging.debug(this, "getSelected_clientnames  " + configedMain.getSelectedClients());
		return configedMain.getSelectedClients().toArray(new String[0]);
	}

	private String[] getSelectedDepotNames() {
		Logging.debug(this, "getSelected_depotnames  " + configedMain.getSelectedDepots());
		return configedMain.getSelectedDepots().toArray(new String[0]);
	}

	private String[] getSelectedDepotIPs() {
		Logging.debug(this, "getSelected_depotIPs " + configedMain.getSelectedDepots());

		String[] depotIPs = new String[configedMain.getSelectedDepots().size()];
		int counter = 0;
		for (String name : configedMain.getSelectedDepots()) {
			String depotip = (String) persistenceController.getHostInfoCollections().getDepots().get(name)
					.get(HostInfo.CLIENT_IP_ADDRESS_KEY);
			Logging.info(this, "getSelected_depotIPs host " + name + " depotip " + depotip);
			if (depotip != null) {
				depotIPs[counter] = depotip;
				counter++;
			}
		}
		return depotIPs;
	}

	private ValueSelectorList fillValueSelectorList(final List<String> values) {
		final DepotsList valueList = new DepotsList(configedMain);
		valueList.setVisible(true);
		final Map<String, Object> extendedInfo = new TreeMap<>();
		final Map<String, Map<String, Object>> info = new TreeMap<>();
		final List<String> data = new ArrayList<>();

		for (final String val : values) {
			extendedInfo.put(val, val);
			info.put(val, extendedInfo);
			data.add(val);
		}

		valueList.setListData(data);
		valueList.setInfo(info);

		final ValueSelectorList valueSelectorList = new ValueSelectorList(valueList, true);
		valueSelectorList.setVisible(true);

		return valueSelectorList;
	}

	private static JOptionPane createValueSelectorDialog(final ValueSelectorList valueSelectorList) {
		final JScrollPane valueScrollPane = valueSelectorList.getScrollpaneDepotslist();
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(valueSelectorList, BorderLayout.NORTH);
		panel.add(valueScrollPane, BorderLayout.CENTER);

		final JOptionPane opPane = new JOptionPane(new Object[] { panel }, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION) {
			@Override
			public void selectInitialValue() {
				super.selectInitialValue();
				((Component) valueScrollPane).requestFocusInWindow();
			}
		};

		opPane.addHierarchyListener((HierarchyEvent hierarchyEvent) -> {
			Window window = SwingUtilities.getWindowAncestor(opPane);
			if (window instanceof Dialog) {
				Dialog dialog = (Dialog) window;
				if (!dialog.isResizable()) {
					dialog.setResizable(true);
				}
			}
		});

		final JDialog jdialog = opPane.createDialog(opPane, Globals.APPNAME);
		jdialog.setSize(400, 250);
		jdialog.setVisible(true);

		return opPane;
	}

	private String getSelectedValue(String method) {
		if (methods.get(METHOD_OPTION_SELECTION).equals(method)) {
			final List<String> values = new ArrayList<>();
			values.add("Test 1");
			values.add("Test 2");
			values.add("Test 3");
			final ValueSelectorList valueSelectorList = fillValueSelectorList(values);
			final JOptionPane opPane = createValueSelectorDialog(valueSelectorList);

			if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION) {
				return valueSelectorList.getSelectedValue();
			}

			return "";
		}

		final String scriptFile = method.replace("script://", "");
		final SingleCommand cmd = new SingleCommandTemplate("", scriptFile, "");
		final ScriptExecutioner exe = new ScriptExecutioner(cmd);
		exe.execute();

		return exe.getValue();
	}

	private class ScriptExecutioner {
		private String value;
		private SingleCommand cmd;

		public ScriptExecutioner(SingleCommand cmd) {
			this.cmd = cmd;
		}

		public String getValue() {
			return value;
		}

		public void execute() {
			CommandExecutor executor = new CommandExecutor(configedMain, cmd);
			executor.setWithGUI(false);
			List<String> values = Arrays.asList(executor.execute().split("\n"));
			value = retrieveSelectedValue(values);
		}

		private String retrieveSelectedValue(List<String> values) {
			final ValueSelectorList valueSelectorList = fillValueSelectorList(values);
			final JOptionPane opPane = createValueSelectorDialog(valueSelectorList);

			if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION) {
				return valueSelectorList.getSelectedValue();
			}

			return null;
		}
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}