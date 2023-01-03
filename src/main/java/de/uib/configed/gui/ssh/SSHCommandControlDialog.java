package de.uib.configed.gui.ssh;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.CheckedDocument;

/**
 * @inheritDoc Graphical user interface for editing sshcommands.
 **/
public class SSHCommandControlDialog extends FGeneralDialog {
	/** button panel instance **/
	private JPanel buttonPanel = new JPanel();
	/** command control panel instance **/
	private JPanel controlPanel = new JPanel();
	/** command - commands control panel instance **/
	private JPanel centerPanel = new JPanel();
	private JPanel commandlistPanel = new JPanel();
	private JPanel parameterPanel;

	/** command control panel Layout instance **/
	private GroupLayout controlPanelLayout;
	/** command - commands control panel Layout instance **/
	private GroupLayout centerPanelLayout;

	private GroupLayout commandlistPanelLayout;

	/** Save Button instance **/
	private JButton buttonSave;
	/** Close Button instance **/
	private JButton buttonClose;

	/** JLabel menu text instance **/
	private JLabel labelMenuText = new JLabel();
	/** JLabel parent menu text instance **/
	private JLabel labelParentMenuText = new JLabel();
	/** JLabel tolbl_parentMenuTextoltip text instance **/
	private JLabel labelTooltipText = new JLabel();
	/** JLabel priority instance **/
	private JLabel labelPriority = new JLabel();
	/** JLabel need sudo instance **/
	private JLabel labelNeedSudo = new JLabel();
	/** JLabel commands instance **/
	private JLabel labelCommands = new JLabel();

	/** JComboBox menu text instance **/
	private JComboBox<String> jComboBoxMenuText;
	/** IconButton delete menu text instance **/
	private de.uib.configed.gui.IconButton buttonDelete;

	/** JComboBox parent menu text instance **/
	private JComboBox<String> jComboBoxParentMenuText;
	/** JTextField tooltip text instance **/
	private JTextField jTextFieldTooltipText = new JTextField();
	/** JTextField priority instance **/
	private JTextField jTextFieldPriority = new JTextField();
	/** JCheckBox need Sudo instance **/
	private JCheckBox jComboBoxNeedSudo = new JCheckBox("");
	/** JTextPane commands instance **/
	private JTextPane jTextPaneommands = new JTextPane();

	/** MainFrame instance **/
	private JFrame main;
	private ConfigedMain cmain;
	/** SSHCommandFactory instance **/
	private final SSHCommandFactory factory = SSHCommandFactory.getInstance();
	/** This instance / Design Patter: singelton **/
	private static SSHCommandControlDialog instance;

	private JButton buttonTestCommand;
	/**
	 * Graphical user interface for editing sshcommands.
	 * 
	 * @param owner Usually the MainFrame
	 **/
	private int thisWidth = 850;
	private int thisHeight = 600;

	private SSHCommandControlDialog(ConfigedMain cm, JFrame owner) {
		super(null, configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
		logging.info(this, "SSHCommandControlDialog instance " + instance + " main " + main);
		main = owner;
		cmain = cm;
		parameterPanel = new SSHCommandControlParameterMethodsPanel(this, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3,
				Globals.GAP_SIZE * 2, 0);
		init();
		pack();
		this.setSize(thisWidth, thisHeight);
		this.centerOn(owner);
		this.setVisible(true);
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @param fr the parent Frame usually the MainFrame
	 * @return SSHCommandControlDialog instance
	 **/
	public static SSHCommandControlDialog getInstance(ConfigedMain cm) {
		return getInstance(cm, Globals.mainFrame);
	}

	public static SSHCommandControlDialog getInstance(ConfigedMain cm, JFrame fr) {
		if (instance == null)
			instance = new SSHCommandControlDialog(cm, fr);
		else
			instance.centerOn(fr);

		instance.setVisible(true);
		return instance;
	}

	private void setCenterLayout() {
		logging.debug(this, "setCenterLayout ");
		centerPanelLayout.setAutoCreateGaps(true);

		centerPanelLayout.setHorizontalGroup(
				centerPanelLayout.createParallelGroup().addComponent(commandlistPanel).addComponent(parameterPanel));
		centerPanelLayout.setVerticalGroup(
				centerPanelLayout.createSequentialGroup().addComponent(commandlistPanel).addComponent(parameterPanel));

		parameterPanel.setVisible(true);
	}

	/** Init components **/
	private void init() {
		logging.debug(this, "init setting up components ");

		controlPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		centerPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		commandlistPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);

		centerPanelLayout = new GroupLayout(centerPanel);
		controlPanelLayout = new GroupLayout(controlPanel);
		commandlistPanelLayout = new GroupLayout(commandlistPanel);

		getContentPane().add(controlPanel, BorderLayout.NORTH);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		commandlistPanel.setPreferredSize(new Dimension(thisWidth - 30, 150));
		commandlistPanel.setSize(new Dimension(thisWidth - 30, 150));
		controlPanel.setLayout(controlPanelLayout);
		centerPanel.setLayout(centerPanelLayout);
		commandlistPanel.setLayout(commandlistPanelLayout);

		controlPanel.setBorder(BorderFactory.createTitledBorder(""));
		centerPanel.setBorder(BorderFactory.createTitledBorder(""));
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		setCenterLayout();

		Dimension dimensionJTextField = new Dimension(Globals.FIRST_LABEL_WIDTH - Globals.GRAPHIC_BUTTON_WIDTH,
				Globals.BUTTON_HEIGHT);
		Dimension dimensionJTextFieldLong = new Dimension(Globals.FIRST_LABEL_WIDTH, Globals.BUTTON_HEIGHT);
		Dimension dimensionButton = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH, Globals.BUTTON_HEIGHT);

		labelParentMenuText = new JLabel();
		jComboBoxParentMenuText = new JComboBox<>();
		labelMenuText = new JLabel();
		jComboBoxMenuText = new JComboBox<>();
		labelTooltipText = new JLabel();
		labelPriority = new JLabel();

		jTextFieldPriority = new JTextField(new CheckedDocument(/* allowedChars */
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(factory.position_default), 1);
		labelNeedSudo = new JLabel();
		labelCommands = new JLabel();
		jTextPaneommands = new JTextPane();

		buttonTestCommand = new de.uib.configed.gui.IconButton(
				configed.getResourceValue("SSHConnection.CommandControl.btnTestCommand"),
				"images/executing_command_red_22.png", "images/executing_command_red_22.png",
				"images/executing_command_red_22.png", true);
		buttonDelete = new de.uib.configed.gui.IconButton(
				configed.getResourceValue("SSHConnection.CommandControl.rm_menuText.tooltip"), "images/list-remove.png",
				"images/list-remove.png", "images/list-remove_disabled.png", true);
		buttonSave = new IconButton(configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif", false);
		buttonClose = new IconButton(configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
				"images/cancel-32.png", "images/cancel_over-32.png", " ", true);

		labelMenuText.setText(configed.getResourceValue("SSHConnection.CommandControl.menuText"));
		labelParentMenuText.setText(configed.getResourceValue("SSHConnection.CommandControl.parentMenuText"));
		labelTooltipText.setText(configed.getResourceValue("SSHConnection.CommandControl.tooltipText"));
		labelPriority.setText(configed.getResourceValue("SSHConnection.CommandControl.priority"));
		labelNeedSudo.setText(configed.getResourceValue("SSHConnection.CommandControl.needSudo"));
		labelCommands.setText(configed.getResourceValue("SSHConnection.CommandControl.commands"));

		jComboBoxParentMenuText.addItem(SSHCommandFactory.parentdefaultForOwnCommands); // parentNull
		jComboBoxMenuText.addItem(SSHCommandFactory.menuNew);

		jComboBoxMenuText.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.menuText.tooltip"));
		jTextFieldTooltipText
				.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.tooltipText.tooltip"));

		jTextFieldPriority.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.priority.tooltip"));
		jComboBoxNeedSudo.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.needSudo.tooltip"));
		labelCommands.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));
		jTextPaneommands.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));

		labelMenuText.setPreferredSize(dimensionJTextField);
		labelParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxMenuText.setPreferredSize(dimensionJTextField);
		labelTooltipText.setPreferredSize(dimensionJTextField);
		jTextFieldTooltipText.setPreferredSize(dimensionJTextField);
		labelPriority.setPreferredSize(dimensionJTextField);
		jTextFieldPriority.setPreferredSize(dimensionButton);
		jComboBoxNeedSudo.setSize(dimensionButton);
		buttonDelete.setSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
		labelNeedSudo.setPreferredSize(dimensionJTextField);
		jComboBoxNeedSudo.setPreferredSize(dimensionButton);
		jTextPaneommands.setPreferredSize(dimensionJTextFieldLong);
		// btn_changeHelpPanelStatus.setPreferredSize(new

		buttonTestCommand.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
		buttonDelete.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));

		jComboBoxMenuText.setEditable(true);
		jComboBoxParentMenuText.setEditable(true);
		jTextFieldPriority.setColumns(4);

		final JTextComponent editor = (JTextComponent) jComboBoxMenuText.getEditor().getEditorComponent();
		jComboBoxMenuText.addItemListener(itemEvent -> {
			if (editor.getText().trim().equals(SSHCommandFactory.menuNew)) {
				editor.setSelectionStart(0);
				editor.setSelectionEnd(editor.getText().length());
			}
			updateSelectedCommand(editor.getText());
			checkAllTexts();
		});
		jComboBoxParentMenuText.addItemListener(itemEvent -> checkAllTexts());

		jTextFieldTooltipText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jTextFieldPriority.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jComboBoxNeedSudo.addItemListener(itemEvent -> checkAllTexts());
		jTextPaneommands.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		// btn_changeHelpPanelStatus.addActionListener(new ActionListener()

		showPanel();
		if (!(Globals.isGlobalReadOnly()))
			buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());

		final SSHCommandControlDialog caller = this;
		if (!(Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
					actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller));

		if (!(Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
					.addActionListener(actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel)
							.doActionParamAdd(jTextPaneommands));

		updateLists(true);
		updateSelectedCommand();

		if (!(Globals.isGlobalReadOnly()))
			buttonDelete.addActionListener(actionEvent -> {
				String menu = (String) jComboBoxMenuText.getSelectedItem();
				factory.deleteSSHCommandByMenu(menu);

				jComboBoxMenuText.setSelectedItem(SSHCommandFactory.menuNew);
				updateLists(true, SSHCommandFactory.menuNew);
				updateSelectedCommand(SSHCommandFactory.menuNew);
				factory.reloadServerMenu();
			});

		if (!(Globals.isGlobalReadOnly()))
			buttonSave.addActionListener(actionEvent -> doAction1());
		buttonClose.addActionListener(actionEvent -> doAction2());

		buttonPanel.add(buttonSave);
		buttonPanel.add(buttonClose);

		initLayout();

		setComponentsEnabledRO(!Globals.isGlobalReadOnly());
	}

	/**
	 * Set components for read_only mode
	 * 
	 * @param value False if mode is readonly - setEditable/setEnabled to false
	 **/
	private void setComponentsEnabledRO(boolean value) {
		logging.info(this, "setComponentsEnabledRO value " + value);
		jComboBoxNeedSudo.setEnabled(value);

		jComboBoxParentMenuText.setEnabled(value);
		jComboBoxParentMenuText.setEditable(value);

		jTextFieldTooltipText.setEnabled(value);
		jTextFieldTooltipText.setEditable(value);

		jTextFieldPriority.setEnabled(value);
		jTextFieldPriority.setEditable(value);

		jTextPaneommands.setEnabled(value);
		jTextPaneommands.setEditable(value);

		buttonDelete.setEnabled(value);
		buttonTestCommand.setEnabled(value);
	}

	/** Init grouplayout **/
	private void initLayout() {
		logging.debug(this, "initLayout ");
		commandlistPanelLayout
				.setHorizontalGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
						.addGroup(commandlistPanelLayout.createParallelGroup()
								.addGroup(commandlistPanelLayout.createSequentialGroup()
										.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addComponent(buttonTestCommand,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)

								).addGap(Globals.MIN_GAP_SIZE).addComponent(jTextPaneommands,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE * 3));
		commandlistPanelLayout.setVerticalGroup(commandlistPanelLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(commandlistPanelLayout.createParallelGroup()
						.addGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.MIN_GAP_SIZE).addComponent(buttonTestCommand, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)

				).addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jTextPaneommands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 1));

		controlPanelLayout.setHorizontalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(controlPanelLayout.createParallelGroup()
						.addGroup(controlPanelLayout.createSequentialGroup()
								.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGroup(controlPanelLayout.createParallelGroup()
										.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jComboBoxNeedSudo, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addComponent(jTextFieldTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE * 3));
		controlPanelLayout.setVerticalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(jTextFieldTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE).addGap(Globals.GAP_SIZE * 2));
	}

	/**
	 * Calls
	 * {@link de.uib.configed.gui.ssh.SSHCommandControlDialog.updateLists(requestRefresh,
	 * null) }
	 **/
	private void updateLists(boolean requestRefresh) {
		updateLists(requestRefresh, null);
	}

	/**
	 * Retrieve all needed lists from factory. If refresh is requested retrieve
	 * the newest lists and setSelectedItem to selectedCommand
	 * 
	 * @param requestRefresh  True for request new refresh
	 * @param selectedCommand MenuText for setSelectedItem
	 **/
	private void updateLists(boolean requestRefresh, String selectedCommand) {
		logging.info(this, "updateLists requestRefresh " + requestRefresh + " selectedCommand " + selectedCommand);
		if (requestRefresh) {
			jComboBoxMenuText.removeAllItems();
			jComboBoxParentMenuText.removeAllItems();
			factory.retrieveSSHCommandListRequestRefresh();
		}

		List<String> commandMenus = factory.getSSHCommandMenuNames();
		List<String> commandParents = factory.getSSHCommandMenuParents();
		for (String menu : commandMenus)
			if (((DefaultComboBoxModel<String>) jComboBoxMenuText.getModel()).getIndexOf(menu) == -1)
				jComboBoxMenuText.addItem(menu);

		for (String parent : commandParents)
			if (((DefaultComboBoxModel<String>) jComboBoxParentMenuText.getModel()).getIndexOf(parent) == -1)
				jComboBoxParentMenuText.addItem(parent);

		if ((selectedCommand == null) || (selectedCommand.trim().equals("")))
			selectedCommand = SSHCommandFactory.menuNew;
		jComboBoxMenuText.setSelectedItem(selectedCommand);
	}

	/** Calls {@link updateSelectedCommand(null)} **/
	private void updateSelectedCommand() {
		updateSelectedCommand(null);
	}

	/**
	 * update all dependent components for selected menu
	 * 
	 * @param menuText The selected menu text
	 **/
	public void updateSelectedCommand(String menuText) {
		logging.info(this, "updateSelectedCommand menuText " + menuText);
		if ((menuText != null) && (menuText.equals(SSHCommandFactory.menuNew)))
			menuText = null;
		if ((menuText != null) && (menuText.length() > 0)) {
			SSHCommand_Template thiscommand = factory.getSSHCommandByMenu(menuText);
			if (thiscommand != null) {
				logging.debug(this, "updateSelectedCommand menu " + thiscommand.getMenuText() + " parent "
						+ thiscommand.getParentMenuText());
				StringBuilder combuf = new StringBuilder("");
				for (SSHCommand c : thiscommand.getCommands()) {
					String rawCommand = c.getCommandRaw();
					if ((rawCommand != null) && !rawCommand.equals(""))
						combuf.append(rawCommand).append("\n");
				}
				updateComponents(thiscommand.getParentMenuText(), thiscommand.getToolTipText(),
						thiscommand.getPriority(), thiscommand.needSudo(), combuf.toString());
			}
		} else
			updateComponents(SSHCommandFactory.parentdefaultForOwnCommands/* parentNull */, "",
					factory.position_default, false, "");
	}

	/**
	 * update all dependent components to given values
	 * 
	 * @param parent  The parent menu name
	 * @param tooltip The tooltip text
	 * @param prio    The priority
	 * @param ns      The needSudo state
	 * @param coms    The commands
	 **/
	private void updateComponents(String parent, String tooltip, int prio, boolean ns, String coms) {
		if ((parent == null) || (parent.trim().equals(""))) {

			parent = SSHCommandFactory.parentdefaultForOwnCommands;
		}
		jComboBoxParentMenuText.setSelectedItem(parent);
		jTextFieldTooltipText.setText(tooltip);
		jTextFieldPriority.setText(String.valueOf(prio));
		jComboBoxNeedSudo.setSelected(ns);
		jTextPaneommands.setText(coms);
	}

	/* This method is called when button 1 (save) is pressed */
	@Override
	public void doAction1() {
		logging.info(this, "doAction1 savecommand ");
		String menuText = (String) jComboBoxMenuText.getSelectedItem();
		SSHCommand_Template command = getCommandNow();
		if (command == null)
			return;
		logging.debug(this, "doAction1 savecommand " + command.toString());
		if (factory.saveSSHCommand(command)) {
			updateLists(true, menuText);
			updateSelectedCommand(menuText);
			factory.reloadServerMenu();
		} else
			JOptionPane.showInternalMessageDialog(this,
					configed.getResourceValue("SSHConnection.CommandControl.couldnotsave.title"),
					configed.getResourceValue("SSHConnection.CommandControl.couldnotsave"),
					JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Generates a command id for futured features
	 * 
	 * @param name The command menu text
	 **/
	private String generateId(String name) {
		return name.replace(" ", "_").toLowerCase().trim();
	}

	public void doActionTestCommand() {
		logging.info(this, "doActionTestCommand testCommand building command ...");
		SSHCommand_Template command = getCommandNow(true /* testing */);
		if (command == null)
			return;
		if (command.getMenuText() == null)
			command.setMenuText(SSHCommandFactory.menuNew);
		logging.debug(this, "doActionTestCommand buildCommand " + command.toString());
		logging.debug(this, "doActionTestCommand buildCommand commandlist " + command.commandlistToString());

		new Thread() {
			@Override
			public void run() {
				new SSHConnectExec(cmain, command);
			}
		}.start();

	}

	private void checkAllTexts() {
		if (!Globals.isGlobalReadOnly()) {
			if (jComboBoxMenuText.getSelectedItem() != null) {
				if (!((String) jComboBoxMenuText.getSelectedItem()).trim().equals(SSHCommandFactory.menuNew)) {
					logging.info(this, "checkAllTexts menuText " + jComboBoxMenuText.getSelectedItem());
					SSHCommand_Template tempCommand = getCommandNow();
					logging.debug(this, "checkAllTexts command " + tempCommand);
					if (tempCommand == null)
						return;
					boolean isNotSaved = !factory.isSSHCommandEqualSavedCommand(tempCommand);
					logging.debug(this, "checkAllTexts factory.isSSHCommandEqualSavedCommand(tmp_com) "
							+ factory.isSSHCommandEqualSavedCommand(tempCommand));
					logging.debug(this, "checkAllTexts isNotSaved " + isNotSaved);
					buttonSave.setEnabled(isNotSaved);
				} else
					buttonSave.setEnabled(false);
			} else
				buttonSave.setEnabled(false);
		}
	}

	public SSHCommand_Template getCommandNow() {
		return getCommandNow(false);
	}

	public SSHCommand_Template getCommandNow(boolean testing) {
		logging.debug(this, "getCommandNow ");
		String menuText = (String) jComboBoxMenuText.getSelectedItem();
		if (!testing && menuText.trim().equals(SSHCommandFactory.menuNew))
			return null;
		String parent = (String) jComboBoxParentMenuText.getSelectedItem();
		int prio = 0;
		try {
			prio = Integer.valueOf(jTextFieldPriority.getText());
		} catch (Exception e) {
			logging.warning("Cannot get value from priority field Exception: " + e);
		}
		List<String> coms = new LinkedList<>();
		for (String c : jTextPaneommands.getText().split("\n"))
			if (!((c == null) || (c.trim().equals(""))))
				coms.add(c);
		SSHCommand_Template tempCommand = factory.buildSSHCommand(
				generateId((String) jComboBoxMenuText.getSelectedItem()), parent, menuText,
				(jTextFieldTooltipText.getText()), prio, jComboBoxNeedSudo.isSelected(), coms);
		logging.debug(this, "getCommandNow command: " + tempCommand);
		return tempCommand;
	}

	private void showPanel() {
		logging.info(this, "showPanel helpPanelStatus always true");

		setCenterLayout();
		centerPanel.setSize(centerPanel.getWidth(), centerPanel.getHeight() + parameterPanel.getHeight());
		this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight());

		repaint();
		revalidate();
	}
}
