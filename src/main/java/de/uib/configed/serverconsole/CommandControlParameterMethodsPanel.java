/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.Dimension;
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
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class CommandControlParameterMethodsPanel extends JPanel {
	private JDialog main;

	private JLabel jLabelParamMethods = new JLabel();
	private JLabel jLabelParamFormats = new JLabel();
	private JLabel jLabelEmpty = new JLabel();
	private JComboBox<String> jComboBoxParameterMethods;
	private JComboBox<String> jComboBoxParameterFormats;
	private JButton jButtonAddParam;
	private JButton jButtonTestParam;

	private ConfigedMain configedMain;

	public CommandControlParameterMethodsPanel(JDialog owner, ConfigedMain configedMain) {
		super();
		Logging.info(this.getClass(), "SSHCommandControlParameterMethodsPane  main ", main);
		main = owner;
		this.configedMain = configedMain;
		init();

		initLayout();
	}

	private void init() {
		Logging.debug(this, "init setting up components ");
		Dimension jComboBoxDim = new Dimension(Globals.FIRST_LABEL_WIDTH + Globals.GAP_SIZE, Globals.BUTTON_HEIGHT);
		Dimension jButtonDim = new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT);

		jLabelEmpty.setPreferredSize(jComboBoxDim);
		jLabelParamMethods.setText(Configed.getResourceValue("CommandControlDialog.parameterMethods"));

		jLabelParamFormats.setText(Configed.getResourceValue("CommandControlDialog.parameterFormats"));

		CommandParameterParser parameterParser = new CommandParameterParser(configedMain);
		jComboBoxParameterFormats = new JComboBox<>(parameterParser.getParameterFormats());
		Logging.info(this, "cb_parameter_formats lightweight ", jComboBoxParameterFormats.isLightWeightPopupEnabled());

		jComboBoxParameterFormats.setPreferredSize(jComboBoxDim);

		// we have to delimit it so that is constrained to the component (in Windows)
		jComboBoxParameterFormats.setMaximumRowCount(5);

		jComboBoxParameterMethods = new JComboBox<>(CommandParameterParser.getParameterMethodLocalNames());
		jComboBoxParameterMethods
				.setSelectedItem(Configed.getResourceValue("CommandControlDialog.cbElementInteractiv"));
		jComboBoxParameterMethods.setPreferredSize(jComboBoxDim);
		jComboBoxParameterMethods.setMaximumRowCount(5);

		jComboBoxParameterFormats.setEnabled(false);

		jComboBoxParameterMethods.addItemListener((ItemEvent itemEvent) -> {
			boolean enabled = ((String) jComboBoxParameterMethods.getSelectedItem())
					.equals(Configed.getResourceValue("CommandControlDialog.cbElementInteractiv"))
					|| ((String) jComboBoxParameterMethods.getSelectedItem())
							.equals(Configed.getResourceValue("CommandControlParameterMethodsPanel.optionSelection"));

			jComboBoxParameterFormats.setEnabled(enabled);
		});

		jButtonTestParam = new JButton(Utils.getIntellijIcon("run"));
		jButtonTestParam.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnTestParamMethod"));
		jButtonTestParam.setPreferredSize(jButtonDim);

		jButtonAddParam = new JButton(Utils.getIntellijIcon("add"));
		jButtonAddParam.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnAddParamMethod"));
		jButtonAddParam.setSize(jButtonDim);
		jButtonAddParam.setPreferredSize(jButtonDim);
		setComponentsEnabledRO();
	}

	private void setComponentsEnabledRO() {
		jButtonTestParam.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		jButtonAddParam.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
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
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3)
				.addGroup(thisLayout.createParallelGroup()
						.addGroup(thisLayout.createSequentialGroup()
								.addGroup(thisLayout.createParallelGroup()
										.addComponent(jLabelParamMethods, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabelParamFormats, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.MIN_GAP_SIZE * 2)
								.addGroup(thisLayout.createParallelGroup()
										.addComponent(jComboBoxParameterMethods, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH)
										.addComponent(jComboBoxParameterFormats, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH))
								.addGap(Globals.MIN_GAP_SIZE * 3, Globals.MIN_GAP_SIZE * 3, Short.MAX_VALUE))

						.addGroup(thisLayout.createSequentialGroup().addComponent(jLabelEmpty, 10, 10, Short.MAX_VALUE)
								.addComponent(jButtonTestParam, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(jButtonAddParam, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH))

				).addGap(Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3));
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxParameterMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(thisLayout.createSequentialGroup().addComponent(jLabelParamMethods,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxParameterFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(thisLayout.createSequentialGroup().addComponent(jLabelParamFormats,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jButtonAddParam, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jButtonTestParam, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jLabelEmpty, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE * 4, Globals.GAP_SIZE * 4, Globals.GAP_SIZE * 4));

		repaint();
		revalidate();
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

			JOptionPane.showMessageDialog(main, showThisText,
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
