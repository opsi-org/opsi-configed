/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

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
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandParameterMethods;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SSHCommandControlParameterMethodsPanel extends JPanel {
	private JDialog main;
	private final SSHCommandFactory factory = SSHCommandFactory.getInstance();

	private JLabel jLabelParamMethods = new JLabel();
	private JLabel jLabelParamFormats = new JLabel();
	private JLabel jLabelEmpty = new JLabel();
	private JComboBox<String> jComboBoxParameterMethods;
	private JComboBox<String> jComboBoxParameterFormats;
	private JButton jButtonAddParam;
	private JButton jButtonTestParam;

	private int lGap = Globals.GAP_SIZE;
	private int rGap = Globals.GAP_SIZE;
	private int uGap = Globals.GAP_SIZE;
	private int oGap = Globals.GAP_SIZE;

	public SSHCommandControlParameterMethodsPanel(JDialog owner, int lg, int rg, int ug, int og) {
		super();
		Logging.info(this.getClass(), "SSHCommandControlParameterMethodsPane  main " + main);
		main = owner;
		init();
		setGapSize(lg, rg, ug, og);
		initLayout();
	}

	public SSHCommandControlParameterMethodsPanel(JDialog owner) {
		super();
		Logging.info(this.getClass(), "SSHCommandControlParameterMethodsPane  main " + main);
		main = owner;
		init();
	}

	/** Init components **/
	private void init() {
		Logging.debug(this, "init setting up components ");
		Dimension jComboBoxDim = new Dimension(Globals.FIRST_LABEL_WIDTH + Globals.GAP_SIZE, Globals.BUTTON_HEIGHT);
		Dimension jButtonDim = new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT);

		jLabelEmpty.setPreferredSize(jComboBoxDim);
		jLabelParamMethods.setText(Configed.getResourceValue("SSHConnection.CommandControl.parameterMethods"));

		jLabelParamFormats.setText(Configed.getResourceValue("SSHConnection.CommandControl.parameterFormats"));

		jComboBoxParameterFormats = new JComboBox<>(factory.getParameterHandler().getParameterFormats());
		Logging.info(this, "cb_parameter_formats lightweight " + jComboBoxParameterFormats.isLightWeightPopupEnabled());

		jComboBoxParameterFormats.setPreferredSize(jComboBoxDim);

		// we have to delimit it so that is constrained to the component (in Windows)
		jComboBoxParameterFormats.setMaximumRowCount(5);

		factory.getParameterHandler();
		jComboBoxParameterMethods = new JComboBox<>(SSHCommandParameterMethods.getParameterMethodLocalNames());
		jComboBoxParameterMethods
				.setSelectedItem(Configed.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv"));
		jComboBoxParameterMethods.setPreferredSize(jComboBoxDim);
		jComboBoxParameterMethods.setMaximumRowCount(5);

		jComboBoxParameterFormats.setEnabled(false);

		jComboBoxParameterMethods.addItemListener((ItemEvent itemEvent) -> {
			boolean enabled = ((String) jComboBoxParameterMethods.getSelectedItem())
					.equals(Configed.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv"))
					|| ((String) jComboBoxParameterMethods.getSelectedItem())
							.equals(Configed.getResourceValue("SSHConnection.CommandControl.method.optionSelection"));

			jComboBoxParameterFormats.setEnabled(enabled);
		});

		jButtonTestParam = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.btnTestParamMethod"),
				"images/executing_command.png", "images/executing_command.png", "images/executing_command.png", true);
		jButtonTestParam.setPreferredSize(jButtonDim);

		jButtonAddParam = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.btnAddParamMethod"),
				"images/list-add.png", "images/list-add.png", "images/list-add_disabled.png", true);
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

	public void setGapSize(int lgap, int rgap, int ugap, int ogap) {
		Logging.info(this, "setGapSize lgap  " + lgap + " rgap " + rgap + " ugap " + ugap + " ogap " + ogap);
		this.lGap = lgap;
		this.rGap = rgap;
		this.uGap = ugap;
		this.oGap = ogap;
	}

	public void initLayout() {
		Logging.debug(this, "initLayout ");

		GroupLayout thisLayout = new GroupLayout(this);
		setLayout(thisLayout);
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addGap(lGap).addGroup(thisLayout
				.createParallelGroup()
				.addGroup(thisLayout.createSequentialGroup().addGroup(thisLayout.createParallelGroup()
						.addComponent(jLabelParamMethods, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelParamFormats, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.MIN_GAP_SIZE * 2)
						.addGroup(thisLayout.createParallelGroup()
								.addComponent(jComboBoxParameterMethods, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										3 * Globals.BUTTON_WIDTH)
								.addComponent(jComboBoxParameterFormats, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										3 * Globals.BUTTON_WIDTH))
						.addGap(Globals.MIN_GAP_SIZE * 3, Globals.MIN_GAP_SIZE * 3, Short.MAX_VALUE))

				.addGroup(thisLayout.createSequentialGroup().addComponent(jLabelEmpty, 10, 10, Short.MAX_VALUE)
						.addComponent(jButtonTestParam, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(jButtonAddParam, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH))

		).addGap(rGap));
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup().addGap(oGap * 2)
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
				.addGap(uGap * 2));

		repaint();
		revalidate();
	}

	public void doActionTestParam(JDialog caller) {
		String paramText = "";
		if (((String) jComboBoxParameterMethods.getSelectedItem())
				.equals(SSHCommandParameterMethods.METHOD_INTERACTIVE_ELEMENT)
				|| ((String) jComboBoxParameterMethods.getSelectedItem())
						.equals(SSHCommandParameterMethods.METHOD_OPTION_SELECTION)) {
			Logging.debug("CREATING PARAM TEXT... ");
			factory.getParameterHandler();
			paramText = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1
					+ SSHCommandParameterMethods.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;
			Logging.debug("CREATED PARAM TEXT: " + paramText);
		} else {
			factory.getParameterHandler();
			paramText = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1
					+ SSHCommandParameterMethods.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ SSHCommandParameterMethods.PARAM_SPLITTER_DEFAULT + jComboBoxParameterFormats.getSelectedItem()
					+ SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;
		}

		Logging.debug("PARAM TEXT: " + paramText);
		try {
			Logging.info(this, "actionPerformed(testParamMethod) parameterText " + paramText);
			String result = "echo \"{0}\"".replace("{0}", factory.getParameterHandler().testParameter(paramText));
			Logging.info(this, "actionPerformed(testParamMethod) result " + result);
			String showThisText = "echo \"{0}\"".replace("{0}", paramText) + ":\n" + result;
			if (result.equals(Configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed"))) {
				showThisText = Configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed");
			}

			JOptionPane.showMessageDialog(main, showThisText,
					Configed.getResourceValue("SSHConnection.CommandControl.parameterTest.title"),
					JOptionPane.INFORMATION_MESSAGE);
		} catch (HeadlessException ble) {
			Logging.warning(this, "Testing parameter-method failed.", ble);
		}
		if (caller != null) {
			caller.setVisible(true);
		}
	}

	public void doActionParamAdd(JTextComponent component) {
		String paramText = "";
		if (((String) jComboBoxParameterMethods.getSelectedItem())
				.equals(SSHCommandParameterMethods.METHOD_INTERACTIVE_ELEMENT)
				|| ((String) jComboBoxParameterMethods.getSelectedItem())
						.equals(SSHCommandParameterMethods.METHOD_OPTION_SELECTION)) {
			factory.getParameterHandler();
			paramText = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1
					+ SSHCommandParameterMethods.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;
		} else {
			factory.getParameterHandler();
			paramText = SSHCommandParameterMethods.REPLACEMENT_DEFAULT_1
					+ SSHCommandParameterMethods.getMethodFromName((String) jComboBoxParameterMethods.getSelectedItem())
					+ SSHCommandParameterMethods.PARAM_SPLITTER_DEFAULT + jComboBoxParameterFormats.getSelectedItem()
					+ SSHCommandParameterMethods.REPLACEMENT_DEFAULT_2;
		}

		try {
			component.getDocument().insertString(component.getCaretPosition(), paramText, null);
		} catch (BadLocationException ble) {
			Logging.warning(this, " BadLocationException  add parameter method to command failed.", ble);
		}
	}
}
