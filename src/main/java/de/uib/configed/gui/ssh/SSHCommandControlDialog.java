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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

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
	// private JPanel parameterHelpPanel = new JPanel();
	/** command control panel Layout instance **/
	private GroupLayout controlPanelLayout;
	/** command - commands control panel Layout instance **/
	private GroupLayout centerPanelLayout;
	// private BorderLayout centerPanelLayout;
	private GroupLayout commandlistPanelLayout;
	// private GroupLayout parameterPanelLayout;
	/** Save Button instance **/
	private JButton btn_save;
	/** Close Button instance **/
	private JButton btn_close;

	/** JLabel menu text instance **/
	private JLabel lbl_menuText = new JLabel();
	/** JLabel parent menu text instance **/
	private JLabel lbl_parentMenuText = new JLabel();
	/** JLabel tooltip text instance **/
	private JLabel lbl_tooltipText = new JLabel();
	/** JLabel priority instance **/
	private JLabel lbl_priority = new JLabel();
	/** JLabel need sudo instance **/
	private JLabel lbl_needSudo = new JLabel();
	/** JLabel commands instance **/
	private JLabel lbl_commands = new JLabel();

	/** JComboBox menu text instance **/
	private JComboBox cb_menuText;
	/** IconButton delete menu text instance **/
	private de.uib.configed.gui.IconButton btn_del;

	/** JComboBox parent menu text instance **/
	private JComboBox cb_parentMenuText;
	/** JTextField tooltip text instance **/
	private JTextField tf_tooltipText = new JTextField();
	/** JTextField priority instance **/
	private JTextField tf_priority = new JTextField();
	/** JCheckBox need Sudo instance **/
	private JCheckBox cb_needSudo = new JCheckBox("");
	/** JTextPane commands instance **/
	private JTextPane tp_commands = new JTextPane();

	/** MainFrame instance **/
	private JFrame main;
	private ConfigedMain cmain;
	/** SSHCommandFactory instance **/
	private final SSHCommandFactory factory = SSHCommandFactory.getInstance();
	/** This instance / Design Patter: singelton **/
	private static SSHCommandControlDialog instance;

	// private JButton btn_changeHelpPanelStatus;
	private JButton btn_test_command;
	/**
	 * Graphical user interface for editing sshcommands.
	 * 
	 * @param owner Usually the MainFrame
	 **/
	private int thisWidth = 850;
	private int thisHeight = 600;

	// private boolean helpPanelStatus = true;
	private SSHCommandControlDialog(ConfigedMain cm, JFrame owner) {
		super(null, configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
		logging.info(this, "SSHCommandControlDialog instance " + instance + " main " + main);
		main = owner;
		cmain = cm;
		parameterPanel = new SSHCommandControlParameterMethodsPanel(this, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3,
				Globals.GAP_SIZE * 2, 0);
		init();
		pack();
		this.centerOn(owner);
		this.setSize(thisWidth, thisHeight);
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
		instance.setVisible(true);
		return instance;
	}

	private void setCenterLayout() {
		logging.debug(this, "setCenterLayout ");
		centerPanelLayout.setAutoCreateGaps(true);
		// if (helpPanelStatus)
		{
			centerPanelLayout.setHorizontalGroup(centerPanelLayout.createParallelGroup().addComponent(commandlistPanel)
					.addComponent(parameterPanel));
			centerPanelLayout.setVerticalGroup(centerPanelLayout.createSequentialGroup().addComponent(commandlistPanel)
					.addComponent(parameterPanel));
		}
		// else
		// {
		// centerPanelLayout.setHorizontalGroup( centerPanelLayout.createParallelGroup()
		// .addComponent(commandlistPanel)
		// );
		// centerPanelLayout.setVerticalGroup( centerPanelLayout.createSequentialGroup()
		// .addComponent(commandlistPanel)
		// );
		// }
		parameterPanel.setVisible(true);
	}

	/** Init components **/
	private void init() {
		logging.debug(this, "init setting up components ");

		controlPanel.setBackground(Globals.backLightBlue);
		centerPanel.setBackground(Globals.backLightBlue);
		commandlistPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);

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

		Dimension tf_dim = new Dimension(Globals.FIRST_LABEL_WIDTH - Globals.GRAPHIC_BUTTON_WIDTH,
				Globals.BUTTON_HEIGHT);
		Dimension tf_dim_long = new Dimension(Globals.FIRST_LABEL_WIDTH, Globals.BUTTON_HEIGHT);
		Dimension btn_dim = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH, Globals.BUTTON_HEIGHT);

		{
			lbl_parentMenuText = new JLabel();
			cb_parentMenuText = new JComboBox();
			lbl_menuText = new JLabel();
			cb_menuText = new JComboBox();
			lbl_tooltipText = new JLabel();
			lbl_priority = new JLabel();
			// btn_save = new JButton();
			tf_priority = new JTextField(new CheckedDocument(/* allowedChars */
					new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
					String.valueOf(factory.position_default), 1);
			lbl_needSudo = new JLabel();
			lbl_commands = new JLabel();
			tp_commands = new JTextPane();
			// btn_changeHelpPanelStatus= new de.uib.configed.gui.IconButton(
			// de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnShowActionHelp")
			// ,
			// "images/help.gif", "images/help.gif", "images/help.gif",true
			// );
			btn_test_command = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnTestCommand"),
					"images/executing_command_red_22.png", "images/executing_command_red_22.png",
					"images/executing_command_red_22.png", true);
			btn_del = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.rm_menuText.tooltip"),
					"images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png", true);
			btn_save = new IconButton(
					de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
					"images/apply_over.gif", " ", "images/apply_disabled.gif", false);
			btn_close = new IconButton(de.uib.configed.configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
					"images/cancel-32.png", "images/cancel_over-32.png", " ", true);
		}
		{
			// btn_save.setText(configed.getResourceValue("SSHConnection.buttonSave"));
			lbl_menuText.setText(configed.getResourceValue("SSHConnection.CommandControl.menuText"));
			lbl_parentMenuText.setText(configed.getResourceValue("SSHConnection.CommandControl.parentMenuText"));
			lbl_tooltipText.setText(configed.getResourceValue("SSHConnection.CommandControl.tooltipText"));
			lbl_priority.setText(configed.getResourceValue("SSHConnection.CommandControl.priority"));
			lbl_needSudo.setText(configed.getResourceValue("SSHConnection.CommandControl.needSudo"));
			lbl_commands.setText(configed.getResourceValue("SSHConnection.CommandControl.commands"));

			cb_parentMenuText.addItem(factory.parentdefaultForOwnCommands); // parentNull
			cb_menuText.addItem(factory.menuNew);

			cb_menuText.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.menuText.tooltip"));
			tf_tooltipText
					.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.tooltipText.tooltip"));

			tf_priority.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.priority.tooltip"));
			cb_needSudo.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.needSudo.tooltip"));
			lbl_commands.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));
			tp_commands.setToolTipText(configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));

			lbl_menuText.setPreferredSize(tf_dim);
			lbl_parentMenuText.setPreferredSize(tf_dim);
			cb_parentMenuText.setPreferredSize(tf_dim);
			cb_menuText.setPreferredSize(tf_dim);
			lbl_tooltipText.setPreferredSize(tf_dim);
			tf_tooltipText.setPreferredSize(tf_dim);
			lbl_priority.setPreferredSize(tf_dim);
			tf_priority.setPreferredSize(btn_dim);
			cb_needSudo.setSize(btn_dim);
			btn_del.setSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
			lbl_needSudo.setPreferredSize(tf_dim);
			cb_needSudo.setPreferredSize(btn_dim);
			tp_commands.setPreferredSize(tf_dim_long);
			// btn_changeHelpPanelStatus.setPreferredSize(new
			// Dimension(Globals.graphicButtonWidth + 15 ,Globals.buttonHeight));
			btn_test_command.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
			btn_del.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));

			cb_menuText.setEditable(true);
			cb_parentMenuText.setEditable(true);
			tf_priority.setColumns(4);

			final JTextComponent editor = (JTextComponent) cb_menuText.getEditor().getEditorComponent();
			cb_menuText.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (editor.getText().trim().equals(factory.menuNew)) {
						editor.setSelectionStart(0);
						editor.setSelectionEnd(editor.getText().length());
					}
					updateSelectedCommand(editor.getText());
					checkAllTexts();
				}
			});
			cb_parentMenuText.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					checkAllTexts();
				}
			});
		}
		{

			tf_tooltipText.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void removeUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events
				}
			});
		}
		{
			tf_priority.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void removeUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events
				}
			});
		}
		{
			cb_needSudo.addItemListener((ItemEvent e) -> checkAllTexts());
		}
		{
			tp_commands.getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void removeUpdate(DocumentEvent e) {
					checkAllTexts();
				}

				public void changedUpdate(DocumentEvent e) {
					// Plain text components do not fire these events
				}
			});
		}
		{
			// if (!(Globals.isGlobalReadOnly()))
			// btn_changeHelpPanelStatus.addActionListener(new ActionListener()
			// {
			// public void actionPerformed(ActionEvent e)
			// {
			// showPanel();
			// }
			// });
			showPanel();
			if (!(Globals.isGlobalReadOnly()))
				btn_test_command.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						doActionTestCommand();
					}
				});

			final SSHCommandControlDialog caller = this;
			if (!(Globals.isGlobalReadOnly()))
				((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonTest()
						.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								((SSHCommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller);
							}
						});

			if (!(Globals.isGlobalReadOnly()))
				((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
						.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								((SSHCommandControlParameterMethodsPanel) parameterPanel)
										.doActionParamAdd((JTextComponent) tp_commands);
							}
						});
		}

		updateLists(true);
		updateSelectedCommand();
		{
			if (!(Globals.isGlobalReadOnly()))
				btn_del.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String menu = (String) cb_menuText.getSelectedItem();
						factory.deleteSSHCommandByMenu(menu);

						cb_menuText.setSelectedItem(factory.menuNew);
						updateLists(true, factory.menuNew);
						updateSelectedCommand(factory.menuNew);
						factory.reloadServerMenu();
					}
				});
		}
		{
			if (!(Globals.isGlobalReadOnly()))
				btn_save.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						doAction1();
					}
				});
		}
		{
			btn_close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doAction2();
				}
			});

			buttonPanel.add(btn_save);
			buttonPanel.add(btn_close);
		}
		initLayout();
		// if (Globals.isGlobalReadOnly())
		setComponentsEnabled_RO(!Globals.isGlobalReadOnly());
	}

	/**
	 * Set components for read_only mode
	 * 
	 * @param value False if mode is readonly - setEditable/setEnabled to false
	 **/
	private void setComponentsEnabled_RO(boolean value) {
		logging.info(this, "setComponentsEnabled_RO value " + value);
		cb_needSudo.setEnabled(value);

		cb_parentMenuText.setEnabled(value);
		cb_parentMenuText.setEditable(value);

		// cb_menuText.setEnabled(value);

		tf_tooltipText.setEnabled(value);
		tf_tooltipText.setEditable(value);

		tf_priority.setEnabled(value);
		tf_priority.setEditable(value);

		tp_commands.setEnabled(value);
		tp_commands.setEditable(value);

		btn_del.setEnabled(value);
		btn_test_command.setEnabled(value);
	}

	/** Init grouplayout **/
	private void initLayout() {
		logging.debug(this, "initLayout ");
		commandlistPanelLayout
				.setHorizontalGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
						.addGroup(commandlistPanelLayout.createParallelGroup()
								.addGroup(commandlistPanelLayout.createSequentialGroup()
										.addComponent(lbl_commands, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addComponent(btn_test_command,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
								// .addComponent(btn_changeHelpPanelStatus, GroupLayout.PREFERRED_SIZE,
								// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								).addGap(Globals.MIN_GAP_SIZE).addComponent(tp_commands, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE * 3));
		commandlistPanelLayout.setVerticalGroup(commandlistPanelLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(commandlistPanelLayout.createParallelGroup()
						.addGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(lbl_commands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.MIN_GAP_SIZE).addComponent(btn_test_command, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)
				// .addComponent(btn_changeHelpPanelStatus, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				).addGap(Globals.MIN_GAP_SIZE)
				.addComponent(tp_commands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 1));

		controlPanelLayout.setHorizontalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(lbl_tooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_menuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_parentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_priority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_needSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(controlPanelLayout.createParallelGroup().addGroup(controlPanelLayout.createSequentialGroup()
						.addComponent(cb_menuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGroup(controlPanelLayout.createParallelGroup()
								.addComponent(tf_priority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(cb_needSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(btn_del, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)))
						.addComponent(tf_tooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(cb_parentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE * 3));
		controlPanelLayout.setVerticalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(lbl_menuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_menuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_del, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(cb_parentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_parentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(tf_tooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_tooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(lbl_priority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_priority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(lbl_needSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_needSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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
			cb_menuText.removeAllItems();
			cb_parentMenuText.removeAllItems();
			factory.retrieveSSHCommandListRequestRefresh();
		}

		java.util.List<SSHCommand_Template> commands = factory.retrieveSSHCommandList();
		java.util.List<String> commandMenus = factory.getSSHCommandMenuNames();
		java.util.List<String> commandParents = factory.getSSHCommandMenuParents();
		for (String menu : commandMenus)
			if (((DefaultComboBoxModel) cb_menuText.getModel()).getIndexOf(menu) == -1)
				cb_menuText.addItem(menu);

		for (String parent : commandParents)
			if (((DefaultComboBoxModel) cb_parentMenuText.getModel()).getIndexOf(parent) == -1)
				cb_parentMenuText.addItem(parent);

		if ((selectedCommand == null) || (selectedCommand.trim().equals("")))
			selectedCommand = SSHCommandFactory.menuNew;
		cb_menuText.setSelectedItem(selectedCommand);
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
		if ((menuText != null) && (menuText.equals(factory.menuNew)))
			menuText = null;
		if ((menuText != null) && (menuText.length() > 0)) {
			SSHCommand_Template thiscommand = factory.getSSHCommandByMenu(menuText);
			if (thiscommand != null) {
				logging.debug(this, "updateSelectedCommand menu " + thiscommand.getMenuText() + " parent "
						+ thiscommand.getParentMenuText());
				StringBuffer combuf = new StringBuffer("");
				for (SSHCommand c : thiscommand.getCommands()) {
					String rawCommand = c.getCommandRaw();
					if ((rawCommand != null) && (rawCommand != ""))
						combuf.append(rawCommand).append("\n");
				}
				updateComponents(thiscommand.getParentMenuText(), thiscommand.getToolTipText(),
						thiscommand.getPriority(), thiscommand.needSudo(), combuf.toString());
			}
		} else
			updateComponents(factory.parentdefaultForOwnCommands/* parentNull */, "", factory.position_default, false,
					"");
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
		if ((parent == null) || (parent.trim() == "")) {
			// parent = factory.parentNull;
			parent = factory.parentdefaultForOwnCommands;
		}
		cb_parentMenuText.setSelectedItem(parent);
		tf_tooltipText.setText(tooltip);
		tf_priority.setText(String.valueOf(prio));
		cb_needSudo.setSelected(ns);
		tp_commands.setText(coms);
	}

	/* This method is called when button 1 (save) is pressed */
	public void doAction1() {
		logging.info(this, "doAction1 savecommand ");
		String menuText = (String) cb_menuText.getSelectedItem();
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
		return name.replaceAll(" ", "_").toLowerCase().trim();
	}

	public void doActionTestCommand() {
		logging.info(this, "doActionTestCommand testCommand building command ...");
		SSHCommand_Template command = getCommandNow(true /* testing */);
		if (command == null)
			return;
		if (command.getMenuText() == null)
			command.setMenuText(factory.menuNew);
		logging.debug(this, "doActionTestCommand buildCommand " + command.toString());
		logging.debug(this, "doActionTestCommand buildCommand commandlist " + command.commandlistToString());

		new Thread() {
			public void run() {
				new SSHConnectExec(cmain, command); // .starting(command);
			}
		}.start();

	}

	private void checkAllTexts() {
		if (!Globals.isGlobalReadOnly()) {
			if (cb_menuText.getSelectedItem() != null)
				if (!((String) cb_menuText.getSelectedItem()).trim().equals(factory.menuNew)) {
					logging.info(this, "checkAllTexts menuText " + cb_menuText.getSelectedItem());
					SSHCommand_Template tmp_com = getCommandNow();
					logging.debug(this, "checkAllTexts command " + tmp_com);
					if (tmp_com == null)
						return;
					boolean isNotSaved = !factory.isSSHCommandEqualSavedCommand(tmp_com);
					logging.debug(this, "checkAllTexts factory.isSSHCommandEqualSavedCommand(tmp_com) "
							+ factory.isSSHCommandEqualSavedCommand(tmp_com));
					logging.debug(this, "checkAllTexts isNotSaved " + isNotSaved);
					btn_save.setEnabled(isNotSaved);
				} else
					btn_save.setEnabled(false);
			else
				btn_save.setEnabled(false);
		}
	}

	public SSHCommand_Template getCommandNow() {
		return getCommandNow(false);
	}

	public SSHCommand_Template getCommandNow(boolean testing) {
		logging.debug(this, "getCommandNow ");
		String menuText = (String) cb_menuText.getSelectedItem();
		if (!testing)
			if (menuText.trim().equals(factory.menuNew))
				return null;
		String parent = (String) cb_parentMenuText.getSelectedItem();
		int prio = 0;
		try {
			prio = Integer.valueOf(tf_priority.getText());
		} catch (Exception e) {
			logging.warning("Cannot get value from priority field Exception: " + e);
		} ;
		LinkedList<String> coms = new LinkedList<String>();
		for (String c : tp_commands.getText().split("\n"))
			if (!((c == null) || (c.trim().equals(""))))
				coms.add(c);
		SSHCommand_Template tmp_com = factory.buildSSHCommand(generateId((String) cb_menuText.getSelectedItem()),
				parent, menuText, ((String) tf_tooltipText.getText()), prio, cb_needSudo.isSelected(), coms);
		logging.debug(this, "getCommandNow command: " + tmp_com);
		return tmp_com;
	}

	/* This method gets called when button 2 (cancel) is pressed */
	public void doAction2() {
		super.doAction2();
	}

	private void showPanel() {
		logging.info(this, "showPanel helpPanelStatus always true");
		// if (helpPanelStatus)
		{
			setCenterLayout();
			centerPanel.setSize(centerPanel.getWidth(), centerPanel.getHeight() + parameterPanel.getHeight());
			this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight());
		}
		// else
		// {
		// setCenterLayout();
		// centerPanel.setSize(centerPanel.getWidth(),
		// centerPanel.getHeight()-parameterPanel.getHeight());
		// this.setSize(this.getWidth(), this.getHeight()-parameterPanel.getHeight());
		// }
		// helpPanelStatus = !helpPanelStatus;
		repaint();
		revalidate();
	}
}
