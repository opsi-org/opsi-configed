/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandParameterParser;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;

public class CommandControlParameterMethodsPanel extends JPanel {
	private EditTerminalCommandsDialog editTerminalCommandsDialog;

	private JLabel jLabelParamMethods = new JLabel();
	private JLabel jLabelParamFormats = new JLabel();
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
		jLabelParamMethods.setText(Configed.getResourceValue("CommandControlDialog.parameterMethods"));
		jLabelParamMethods.setFont(jLabelParamMethods.getFont().deriveFont(Font.BOLD));

		jLabelParamFormats.setText(Configed.getResourceValue("CommandControlDialog.parameterFormats"));
		jLabelParamFormats.setFont(jLabelParamFormats.getFont().deriveFont(Font.BOLD));

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
			boolean enabled = ((String) jComboBoxParameterMethods.getSelectedItem())
					.equals(Configed.getResourceValue("CommandControlDialog.cbElementInteractiv"));

			jComboBoxParameterFormats.setEnabled(enabled);
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

		GroupLayout thisLayout = new GroupLayout(this);
		setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createParallelGroup()
				.addComponent(jLabelParamMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParameterMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelParamFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParameterFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(thisLayout.createSequentialGroup()
						.addComponent(jButtonTestParam, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(jButtonAddParam, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addComponent(jLabelParamMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParameterMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelParamFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParameterFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonTestParam, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonAddParam, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));
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
					Configed.getResourceValue("CommandControlDialog.parameterTest.title"),
					JOptionPane.INFORMATION_MESSAGE);
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
