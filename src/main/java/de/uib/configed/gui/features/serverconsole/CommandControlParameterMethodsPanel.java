/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.HeadlessException;
import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandParameterParser;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CommandControlParameterMethodsPanel extends JPanel {
	private EditTerminalCommandsDialog editTerminalCommandsDialog;

	private JLabel jLabelParamMethods;
	private JLabel jLabelParamFormats;
	private JComboBox<String> jComboBoxParameterMethods;
	private JComboBox<String> jComboBoxParameterFormats;
	private JButton jButtonAddParam;
	private JButton jButtonTestParam;

	private ConfigedMain configedMain;

	public CommandControlParameterMethodsPanel(EditTerminalCommandsDialog editTerminalCommandsDialog,
			ConfigedMain configedMain) {
		super();
		Logging.info(this, "SSHCommandControlParameterMethodsPane  main ", editTerminalCommandsDialog);
		this.editTerminalCommandsDialog = editTerminalCommandsDialog;
		this.configedMain = configedMain;
		init();

		initLayout();
	}

	private void init() {
		Logging.debug(this, "init setting up components ");
		jLabelParamMethods = SwingUtils.createBoldLabel("CommandControlDialog.parameterMethods");
		jLabelParamFormats = SwingUtils.createBoldLabel("CommandControlDialog.parameterFormats");

		CommandParameterParser parameterParser = new CommandParameterParser(configedMain);
		jComboBoxParameterFormats = new JComboBox<>(parameterParser.getParameterFormats());
		Logging.info(this, "cb_parameter_formats lightweight ", jComboBoxParameterFormats.isLightWeightPopupEnabled());

		// we have to delimit it so that is constrained to the component (in Windows)
		jComboBoxParameterFormats.setMaximumRowCount(5);

		jComboBoxParameterMethods = new JComboBox<>(CommandParameterParser.getParameterMethodLocalNames());
		jComboBoxParameterMethods
				.setSelectedItem(Configed.getResourceValue("CommandControlDialog.cbElementInteractiv"));
		jComboBoxParameterMethods.setMaximumRowCount(5);

		jComboBoxParameterFormats.setEnabled(false);

		jComboBoxParameterMethods.addItemListener((ItemEvent itemEvent) -> {
			boolean isInteractiveParameterMethod = ((String) jComboBoxParameterMethods.getSelectedItem())
					.equals(Configed.getResourceValue("CommandControlDialog.cbElementInteractiv"));
			jComboBoxParameterFormats.setEnabled(!isInteractiveParameterMethod);
		});

		jButtonTestParam = new JButton(Icons.getIntellijIcon("run"));
		jButtonTestParam.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnTestParamMethod"));

		jButtonAddParam = new JButton(Icons.getIntellijIcon("add"));
		jButtonAddParam.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnAddParamMethod"));
	}

	public JButton getButtonAdd() {
		return jButtonAddParam;
	}

	public JButton getButtonTest() {
		return jButtonTestParam;
	}

	private void initLayout() {
		Logging.debug(this, "initLayout ");

		setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap", "", "[]0"));
		add(jLabelParamMethods);
		add(jComboBoxParameterMethods, "gapbottom " + Globals.GAP_SIZE);
		add(jLabelParamFormats);
		add(jComboBoxParameterFormats, "gapbottom " + Globals.GAP_SIZE);
		add(jButtonTestParam, "split 2, gapright " + Globals.GAP_SIZE);
		add(jButtonAddParam, "wrap");
	}

	public void doActionTestParam(JDialog caller) {
		String paramText = "";
		if (((String) jComboBoxParameterMethods.getSelectedItem())
				.equals(CommandParameterParser.METHOD_INTERACTIVE_ELEMENT)) {
			Logging.debug("CREATING PARAM TEXT... ");
			paramText = CommandParameterParser.REPLACEMENT_DEFAULT_1
					+ CommandParameterParser.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ CommandParameterParser.REPLACEMENT_DEFAULT_2;
			Logging.debug("CREATED PARAM TEXT: ", paramText);
		} else {
			paramText = CommandParameterParser.REPLACEMENT_DEFAULT_1
					+ CommandParameterParser.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ CommandParameterParser.PARAM_SPLITTER_DEFAULT + jComboBoxParameterFormats.getSelectedItem()
					+ CommandParameterParser.REPLACEMENT_DEFAULT_2;
		}

		Logging.debug("PARAM TEXT: ", paramText);
		try {
			Logging.info(this, "actionPerformed(testParamMethod) parameterText ", paramText);
			CommandParameterParser parameterParser = new CommandParameterParser(configedMain);
			String result = "echo \"{0}\"".replace("{0}", parameterParser.testParameter(paramText));
			Logging.info(this, "actionPerformed(testParamMethod) result ", result);
			String showThisText = "echo \"{0}\"".replace("{0}", paramText) + ":\n" + result;
			if (result.equals(Configed.getResourceValue("CommandControlDialog.parameterTest.failed"))) {
				showThisText = Configed.getResourceValue("CommandControlDialog.parameterTest.failed");
			}

			JOptionPane.showMessageDialog(editTerminalCommandsDialog.getDialog(), showThisText,
					Configed.getResourceValue("CommandControlDialog.parameterTest.title"), JOptionPane.PLAIN_MESSAGE);
		} catch (HeadlessException ble) {
			Logging.warning(this, ble, "Testing parameter-method failed.");
		}
		if (caller != null) {
			caller.setVisible(true);
		}
	}

	public void doActionParamAdd(JTextComponent component) {
		String paramText = "";
		if (((String) jComboBoxParameterMethods.getSelectedItem())
				.equals(CommandParameterParser.METHOD_INTERACTIVE_ELEMENT)) {
			paramText = CommandParameterParser.REPLACEMENT_DEFAULT_1
					+ CommandParameterParser.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ CommandParameterParser.REPLACEMENT_DEFAULT_2;
		} else {
			paramText = CommandParameterParser.REPLACEMENT_DEFAULT_1
					+ CommandParameterParser.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ CommandParameterParser.PARAM_SPLITTER_DEFAULT + jComboBoxParameterFormats.getSelectedItem()
					+ CommandParameterParser.REPLACEMENT_DEFAULT_2;
		}

		try {
			component.getDocument().insertString(component.getCaretPosition(), paramText, null);
		} catch (BadLocationException ble) {
			Logging.warning(this, ble, " BadLocationException  add parameter method to command failed.");
		}
	}
}
