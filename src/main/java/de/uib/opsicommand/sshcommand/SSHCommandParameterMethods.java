package de.uib.opsicommand.sshcommand;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import de.uib.configed.gui.ssh.SSHConnectionOutputDialog;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2017 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */
import de.uib.configed.type.HostInfo;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.ssh.SSHOutputCollector;

/**
 * This Class handles SSHCommands.
 **/
public class SSHCommandParameterMethods extends SSHCommandParameterMethodsAbstractFacade {

	/** default parameter replace id beginns with <<< **/
	public static String replacement_default_1 = "<<<";
	/** default parameter replace id ends with >>> **/
	public static String replacement_default_2 = ">>>";
	public static String paramSplitterDefault = "><";

	private ConfigedMain main;
	private static SSHCommandParameterMethods instance;

	public static final String methodInteractiveElement = Configed
			.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv");
	public static final String methodGetSelectedClientNames = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getSelectedClientNames");
	public static final String methodGetSelectedClientIPs = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getSelectedClientIPs");
	public static final String methodGetSelectedDepotNames = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getSelectedDepotNames");
	public static final String methodGetSelectedDepotIPs = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getSelectedDepotIPs");
	public static final String methodGetConfigServerName = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getConfigServerName");
	public static final String methodGetConnectedSSHServerName = Configed
			.getResourceValue("SSHConnection.CommandControl.method.getConnectedSSHServerName");
	public static final String methodOptionSelection = Configed
			.getResourceValue("SSHConnection.CommandControl.method.optionSelection");

	protected static final Map<String, String> methods = new HashMap<>();

	private String[] formats;

	private SSHCommandParameterMethods(ConfigedMain main) {
		methods.put(methodInteractiveElement, methodInteractiveElement);
		methods.put(methodGetSelectedClientNames, "getSelectedClientNames");
		methods.put(methodGetSelectedClientIPs, "getSelectedClientIPs");
		methods.put(methodGetSelectedDepotNames, "getSelectedDepotNames");
		methods.put(methodGetSelectedDepotIPs, "getSelectedDepotIPs");
		methods.put(methodGetConfigServerName, "getConfigServerName");
		methods.put(methodGetConnectedSSHServerName, "getConnectedSSHServerName");
		methods.put(methodOptionSelection, "ssh://path/to/file");

		this.main = main;
		instance = this;
		init();
	}

	public static SSHCommandParameterMethods getInstance(ConfigedMain m) {
		if (instance != null)
			return instance;
		else
			return new SSHCommandParameterMethods(m);
	}

	public static SSHCommandParameterMethods getInstance() {
		if (instance != null)
			return instance;
		else
			return new SSHCommandParameterMethods(null);
	}

	public String[] getParameterMethodLocalNames() {
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Map.Entry<String, String> entry : methods.entrySet()) {
			mymethods[counter] = entry.getKey();
			counter++;
		}
		Arrays.sort(mymethods);
		return mymethods;
	}

	public String[] getParameterMethods() {
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Map.Entry<String, String> entry : methods.entrySet()) {
			mymethods[counter] = entry.getValue();
			counter++;
		}
		return mymethods;
	}

	public String getMethodFromName(String name) {
		for (Map.Entry<String, String> entry : methods.entrySet()) {
			if (name.equals(entry.getKey()))
				return entry.getValue();
		}
		return name;
	}

	public String[] getParameterFormats() {
		if (formats != null)
			return formats;
		return null;
	}

	public boolean canceled;
	SSHConnectionOutputDialog outputDia = null;

	public SSHCommand parseParameter(final SSHCommand command, SSHConnect caller) {
		Logging.info(this, "parseParameter command " + command.getCommandRaw());
		if (caller instanceof SSHConnectExec)
			outputDia = ((SSHConnectExec) caller).getDialog();
		else if (caller instanceof SSHConnectTerminal)
			outputDia = ((SSHConnectTerminal) caller).getDialog();
		List<String> params = command.getParameterList();
		if ((params != null) && (!params.isEmpty()))
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
						(command).setCommand(command.getCommandRaw().replace(param, result));
						Logging.debug(this, "parseParameter command " + command.getCommandRaw());
					}
				}

			}
		Logging.info(this, "parseParameter command " + command.getCommandRaw());
		return command;
	}

	public String parseParameterToString(SSHCommand command, SSHConnect caller) {
		SSHCommand c = parseParameter(command, caller);
		return c.getCommandRaw();
	}

	public String testParameter(String param) {
		String[] splittedParameter = splitParameter(param);
		String result = callMethod(splittedParameter[0], splittedParameter[1]);
		result = result.contains("ssh://") ? result.replace("ssh://", "") : result;
		if (result == null)
			return Configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed");
		return result;
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
		if ((m.startsWith(replacement_default_1)) && (m.contains(replacement_default_2))) {
			m = m.replace(replacement_default_1, "").replace(replacement_default_2, "");
		}

		Logging.info(this, "splitParameter param " + m);
		String[] splitted = new String[2];
		splitted[0] = m;
		splitted[1] = "";

		if (m.contains(paramSplitterDefault)) {
			splitted[0] = m.split(paramSplitterDefault)[0];
			Logging.info(this, "splitParameter method " + splitted[0]);

			Logging.info(this, "splitParameter method " + splitted[0]);
			splitted[1] = m.split(paramSplitterDefault)[1];
			Logging.info(this, "splitParameter format " + splitted[1]);
		}
		return splitted;

	}

	public String getTranslatedMethod(String localeMethod) {
		String method = "";
		for (Map.Entry<String, String> entry : methods.entrySet()) {
			if (entry.getKey().equals(localeMethod))
				method = entry.getValue();
		}
		return method;

	}

	private String callMethod(String method, String format) {
		Logging.info(this, "callMethod method " + method + " format " + format);
		String result = "";
		method = method.trim();
		if (method.equals(methods.get(methodGetSelectedClientNames))) {
			Logging.info(this, "getSelected_clientnames " + getSelectedClientNames());
			result = formatResult(getSelectedClientNames(), format);
		} else if (method.equals(methods.get(methodGetSelectedClientIPs))) {
			Logging.info(this, "getSelected_clientIPs " + getSelectedClientIPs());
			result = formatResult(getSelectedClientIPs(), format);
		} else if (method.equals(methods.get(methodGetSelectedDepotNames))) {
			result = formatResult(getSelectedDepotNames(), format);
		} else if (method.equals(methods.get(methodGetSelectedDepotIPs))) {
			result = formatResult(getSelected_depotIPs(), format);
		} else if (method.equals(methods.get(methodGetConfigServerName))) {
			result = formatResult(getConfigServerName(), format);
		} else if (method.equals(methods.get(methodGetConnectedSSHServerName))) {
			result = formatResult(getConfigSSHServerName(), format);
		} else if (method.contains("ssh://")) {
			result = getSelectedValue(method);
			Logging.info(this, "callMethod replace \"" + method + "\" with \"" + result + "\"");
		} else if (format.equals("")) {
			result = getUserText(method, outputDia);
			Logging.info(this, "callMethod replace \"" + method + "\" with \"" + result + "\"");
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

	private static final String BRACKETS_NONE = " x ";
	private static final String BRACKETS_SQUARE = "[x]";

	private String createFormattedDataSourceString(String[] strArr, String beginEndElement, String beginEndString,
			String separator) {
		String formatedResult = "!!!Error!!!";
		try {
			strArr = replaceElements(strArr, beginEndElement);
			Logging.info(this, "createFormattedDataSourceString[ ]  strArr " + Arrays.toString(strArr));
			formatedResult = createStringOfArray(strArr, beginEndString, separator);
			Logging.info(this, "createFormattedDataSourceString[ ] formated_result " + formatedResult);
		} catch (Exception e) {
			Logging.error("Error", e);
		}
		return formatedResult;
	}

	private String[] replaceElements(String[] strArrToReplace, String beginEndOfElement) {
		for (int i = 0; i < strArrToReplace.length; i++) {
			strArrToReplace[i] = strArrToReplace[i].replace(strArrToReplace[i],
					beginEndOfElement + strArrToReplace[i] + beginEndOfElement);
			Logging.info(this, "formatResult[] result[i] " + strArrToReplace[i]);
		}
		return strArrToReplace;
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

	public String arrayToStringAsList(Object[] list) {
		return Arrays.toString(list).replace("[", "").replace("]", "");
	}

	public String arrayToString(Object[] list) {
		return Arrays.toString(list).replace("[", "").replace(",", " ").replace("]", "");
	}

	protected String getUserText(String text, Component dialog) {
		if (dialog == null)
			dialog = Globals.mainFrame;
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
				Globals.APPNAME + " " + Configed.getResourceValue("SSHConnection.ParameterDialog.Input"));
		jdialog.setSize(400, 150);
		jdialog.setVisible(true);

		if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION)
			return field.getText().trim();
		return null;
	}

	@Override
	public String getConfigServerName() {
		List<String> depots = main.getPersistenceController().getHostInfoCollections().getDepotNamesList();
		for (String depot : depots)
			if (depot.startsWith(ConfigedMain.HOST)) {
				Logging.debug(this, "getConfig_serverName " + ConfigedMain.HOST);
				return depot;
			}

		Logging.debug(this, "getConfig_serverName " + ConfigedMain.HOST);
		//// peristancecontroller methods for depot :

		return ConfigedMain.HOST;
	}

	@Override
	public String getConfigSSHServerName() {
		Logging.debug(this, "getConfig_sshserverName " + SSHConnectionInfo.getInstance().getHost());
		return SSHConnectionInfo.getInstance().getHost();
	}

	public String[] getSelectedClientIPs() {
		Logging.debug(this, "getSelected_clientIPs " + Arrays.toString(main.getSelectedClients()));
		String[] clientnames = new String[main.getSelectedClients().length];
		System.arraycopy(main.getSelectedClients(), 0, clientnames, 0, main.getSelectedClients().length);

		String[] clientIPs = new String[clientnames.length];
		int counter = 0;
		for (String name : clientnames) {
			HostInfo hostInfo = main.getPersistenceController().getHostInfoCollections().getMapOfAllPCInfoMaps()
					.get(name);
			if (hostInfo != null) {
				clientIPs[counter] = hostInfo.getIpAddress();
				counter++;
			} else
				Logging.debug(this, "getSelected_clientIPs host " + name + " HostInfo null");

		}
		return clientIPs;
	}

	@Override
	public String[] getSelectedClientNames() {
		Logging.debug(this, "getSelected_clientnames  " + Arrays.toString(main.getSelectedClients()));
		String[] clientnames = new String[main.getSelectedClients().length];
		System.arraycopy(main.getSelectedClients(), 0, clientnames, 0, main.getSelectedClients().length);
		return clientnames;
	}

	@Override
	public String[] getSelectedDepotNames() {
		Logging.debug(this, "getSelected_depotnames  " + main.getSelectedDepots());
		return main.getSelectedDepots();
	}

	public String[] getSelected_depotIPs() {
		Logging.debug(this, "getSelected_depotIPs " + main.getSelectedDepots());
		String[] depotnames = new String[main.getSelectedDepots().length];
		System.arraycopy(main.getSelectedDepots(), 0, depotnames, 0, main.getSelectedDepots().length);
		String[] depotIPs = new String[depotnames.length];
		int counter = 0;
		for (String name : depotnames) {
			String depotip = ((String) main.getPersistenceController().getHostInfoCollections().getDepots().get(name)
					.get(HostInfo.CLIENT_IP_ADDRESS_KEY));
			Logging.info(this, "getSelected_depotIPs host " + name + " depotip " + depotip);
			if (depotip != null) {
				depotIPs[counter] = depotip;
				counter++;
			}
		}
		return depotIPs;
	}

	private ValueSelectorList fillValueSelectorList(final List<String> values) {
		final DepotsList valueList = new DepotsList(PersistenceControllerFactory.getPersistenceController());
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

		final ValueSelectorList valueSelectorList = new ValueSelectorList(valueList, true,
				PersistenceControllerFactory.getPersistenceController());
		valueSelectorList.setVisible(true);

		return valueSelectorList;
	}

	private JOptionPane createValueSelectorDialog(final ValueSelectorList valueSelectorList) {
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

		opPane.addHierarchyListener(hierarchyEvent -> {
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
		if (method.equals("ssh://path/to/file")) {
			final List<String> values = new ArrayList<>();
			values.add("Test 1");
			values.add("Test 2");
			values.add("Test 3");
			final ValueSelectorList valueSelectorList = fillValueSelectorList(values);
			final JOptionPane opPane = createValueSelectorDialog(valueSelectorList);

			if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION)
				return valueSelectorList.getSelectedValue();

			return "";
		}

		SSHOutputCollector.getInstance().removeAllValues();
		final SSHConnect caller = new SSHConnect(main);

		final String scriptFile = method.replace("ssh://", "");
		final LinkedList<String> commands = new LinkedList<>();
		commands.add(scriptFile);
		SSHCommandTemplate cmd = new SSHCommandTemplate("", commands, "", false, "", "", 0);

		final SSHCommand cmdScript = parseParameter(cmd, caller);
		final ScriptExecutioner exe = new ScriptExecutioner(cmdScript);
		exe.execute();

		return exe.getValue();
	}

	private class ScriptExecutioner {
		private String value;
		private SSHCommand cmd;
		private SSHOutputCollector sshOutputCollector;

		public ScriptExecutioner(SSHCommand cmd) {
			this.cmd = cmd;

			this.sshOutputCollector = SSHOutputCollector.getInstance();
		}

		public String getValue() {
			return value;
		}

		public void execute() {
			new SSHConnectExec(main, cmd);

			final List<String> values = sshOutputCollector.getValues();
			value = retrieveSelectedValue(values);
		}

		private String retrieveSelectedValue(List<String> values) {
			final ValueSelectorList valueSelectorList = fillValueSelectorList(values);
			final JOptionPane opPane = createValueSelectorDialog(valueSelectorList);

			if (((Integer) opPane.getValue()) == JOptionPane.OK_OPTION)
				return valueSelectorList.getSelectedValue();

			return null;
		}
	}
}
