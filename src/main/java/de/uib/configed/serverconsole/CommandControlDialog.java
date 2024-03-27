/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommand;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedDocument;

public final class CommandControlDialog extends FGeneralDialog {
	private static final int FRAME_WIDTH = 850;
	private static final int FRAME_HEIGHT = 600;

	private JPanel buttonPanel = new JPanel();
	private JPanel controlPanel = new JPanel();
	private JPanel commandPanel = new JPanel();
	private JPanel commandlistPanel = new JPanel();
	private JPanel parameterPanel;

	private GroupLayout controlPanelLayout;
	private GroupLayout commandPanelLayout;

	private GroupLayout commandlistPanelLayout;

	private JButton buttonSave;

	private JLabel labelMenuText;
	private JLabel labelParentMenuText;
	private JLabel labelTooltipText;
	private JLabel labelPriority;
	private JLabel labelCommands;

	private JComboBox<String> jComboBoxMenuText;
	private JButton buttonDelete;

	private JComboBox<String> jComboBoxParentMenuText;
	private JTextField jTextFieldTooltipText = new JTextField();
	private JTextField jTextFieldPriority = new JTextField();
	private JTextPane jTextPaneCommands = new JTextPane();

	private ConfigedMain configedMain;
	private final CommandFactory factory;

	private JButton buttonTestCommand;

	public CommandControlDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
		this.configedMain = configedMain;
		factory = CommandFactory.getInstance(configedMain);
		parameterPanel = new CommandControlParameterMethodsPanel(this);
		init();

		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationRelativeTo(ConfigedMain.getMainFrame());
		this.setVisible(true);
	}

	private void setCenterLayout() {
		Logging.debug(this, "setCenterLayout ");
		commandPanelLayout.setAutoCreateGaps(true);

		commandPanelLayout.setHorizontalGroup(
				commandPanelLayout.createParallelGroup().addComponent(commandlistPanel).addComponent(parameterPanel));
		commandPanelLayout.setVerticalGroup(
				commandPanelLayout.createSequentialGroup().addComponent(commandlistPanel).addComponent(parameterPanel));

		parameterPanel.setVisible(true);
	}

	private void init() {
		Logging.debug(this, "init setting up components ");

		commandPanelLayout = new GroupLayout(commandPanel);
		controlPanelLayout = new GroupLayout(controlPanel);
		commandlistPanelLayout = new GroupLayout(commandlistPanel);

		getContentPane().add(controlPanel, BorderLayout.NORTH);
		getContentPane().add(commandPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		commandlistPanel.setPreferredSize(new Dimension(FRAME_WIDTH - 30, 150));
		commandlistPanel.setSize(new Dimension(FRAME_WIDTH - 30, 150));
		controlPanel.setLayout(controlPanelLayout);
		commandPanel.setLayout(commandPanelLayout);
		commandlistPanel.setLayout(commandlistPanelLayout);

		controlPanel.setBorder(BorderFactory.createTitledBorder(""));
		commandPanel.setBorder(BorderFactory.createTitledBorder(""));
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		setCenterLayout();

		Dimension dimensionJTextField = new Dimension(Globals.FIRST_LABEL_WIDTH - Globals.GRAPHIC_BUTTON_SIZE,
				Globals.BUTTON_HEIGHT);
		Dimension dimensionJTextFieldLong = new Dimension(Globals.FIRST_LABEL_WIDTH, Globals.BUTTON_HEIGHT);
		Dimension dimensionButton = new Dimension(Globals.GRAPHIC_BUTTON_SIZE, Globals.BUTTON_HEIGHT);

		jComboBoxParentMenuText = new JComboBox<>();
		jComboBoxMenuText = new JComboBox<>();

		jTextFieldPriority = new JTextField(new CheckedDocument(/* allowedChars */
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(CommandFactory.DEFAULT_POSITION), 1);

		jTextPaneCommands = new JTextPane();

		buttonTestCommand = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.btnTestCommand"),
				"images/executing_command_red_22.png", "images/executing_command_red_22.png",
				"images/executing_command_red_22.png", true);
		buttonDelete = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.rm_menuText.tooltip"),
				"images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png", true);
		buttonSave = new JButton(Configed.getResourceValue("SSHConnection.CommandControl.ButtonSave"));
		JButton buttonClose = new JButton(Configed.getResourceValue("buttonClose"));

		labelMenuText = new JLabel(Configed.getResourceValue("SSHConnection.CommandControl.menuText"));
		labelParentMenuText = new JLabel(Configed.getResourceValue("SSHConnection.CommandControl.parentMenuText"));
		labelTooltipText = new JLabel(Configed.getResourceValue("SSHConnection.CommandControl.tooltipText"));
		labelPriority = new JLabel(Configed.getResourceValue("SSHConnection.CommandControl.priority"));
		labelCommands = new JLabel(Configed.getResourceValue("SSHConnection.CommandControl.commands"));

		jComboBoxParentMenuText.addItem(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS);
		jComboBoxMenuText.addItem(CommandFactory.MENU_NEW);

		jComboBoxMenuText.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.menuText.tooltip"));
		jTextFieldTooltipText
				.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.tooltipText.tooltip"));

		jTextFieldPriority.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.priority.tooltip"));
		labelCommands.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));
		jTextPaneCommands.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));

		labelMenuText.setPreferredSize(dimensionJTextField);
		labelParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxMenuText.setPreferredSize(dimensionJTextField);
		labelTooltipText.setPreferredSize(dimensionJTextField);
		jTextFieldTooltipText.setPreferredSize(dimensionJTextField);
		labelPriority.setPreferredSize(dimensionJTextField);
		jTextFieldPriority.setPreferredSize(dimensionButton);
		buttonDelete.setSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		jTextPaneCommands.setPreferredSize(dimensionJTextFieldLong);

		buttonTestCommand.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		buttonDelete.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));

		jComboBoxMenuText.setEditable(true);
		jComboBoxParentMenuText.setEditable(true);
		jTextFieldPriority.setColumns(4);

		final JTextComponent editor = (JTextComponent) jComboBoxMenuText.getEditor().getEditorComponent();
		jComboBoxMenuText.addItemListener((ItemEvent itemEvent) -> {
			if (editor.getText().trim().equals(CommandFactory.MENU_NEW)) {
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

		jTextPaneCommands.getDocument().addDocumentListener(new DocumentListener() {
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

		showPanel();
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());
		}

		final CommandControlDialog caller = this;
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			((CommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
					actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller));
		}

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			((CommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
					.addActionListener(actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel)
							.doActionParamAdd(jTextPaneCommands));
		}

		updateLists();
		updateSelectedCommand();

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			buttonDelete.addActionListener((ActionEvent actionEvent) -> {
				String menu = (String) jComboBoxMenuText.getSelectedItem();
				factory.deleteCommandByMenu(menu);

				jComboBoxMenuText.setSelectedItem(CommandFactory.MENU_NEW);
				updateLists(CommandFactory.MENU_NEW);
				updateSelectedCommand(CommandFactory.MENU_NEW);
				factory.reloadServerMenu();
			});
		}

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			buttonSave.addActionListener(actionEvent -> doAction2());
			buttonClose.addActionListener(actionEvent -> doAction1());
		}

		buttonPanel.add(buttonClose);
		buttonPanel.add(buttonSave);

		initLayout();

		setComponentsEnabledRO(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());
	}

	private void setComponentsEnabledRO(boolean value) {
		Logging.info(this, "setComponentsEnabledRO value " + value);
		jComboBoxParentMenuText.setEnabled(value);
		jComboBoxParentMenuText.setEditable(value);

		jTextFieldTooltipText.setEnabled(value);
		jTextFieldTooltipText.setEditable(value);

		jTextFieldPriority.setEnabled(value);
		jTextFieldPriority.setEditable(value);

		jTextPaneCommands.setEnabled(value);
		jTextPaneCommands.setEditable(value);

		buttonDelete.setEnabled(value);
		buttonTestCommand.setEnabled(value);
	}

	private void initLayout() {
		Logging.debug(this, "initLayout ");
		commandlistPanelLayout
				.setHorizontalGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
						.addGroup(commandlistPanelLayout.createParallelGroup()
								.addGroup(commandlistPanelLayout.createSequentialGroup()
										.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addComponent(buttonTestCommand,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)

								).addGap(Globals.MIN_GAP_SIZE).addComponent(jTextPaneCommands,
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

				).addGap(Globals.MIN_GAP_SIZE).addComponent(jTextPaneCommands, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
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
								GroupLayout.PREFERRED_SIZE))
				.addGroup(controlPanelLayout.createParallelGroup()
						.addGroup(controlPanelLayout.createSequentialGroup()
								.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGroup(controlPanelLayout.createParallelGroup()
										.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE,
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
				.addGap(Globals.MIN_GAP_SIZE).addGap(Globals.GAP_SIZE * 2));
	}

	private void updateLists() {
		updateLists(null);
	}

	private void updateLists(String selectedCommand) {
		Logging.info(this, "updateLists selectedCommand " + selectedCommand);
		jComboBoxMenuText.removeAllItems();
		jComboBoxParentMenuText.removeAllItems();

		List<String> commandMenus = factory.getCommandMenuNames();
		List<String> commandParents = factory.getCommandMenuParents();
		for (String menu : commandMenus) {
			if (((DefaultComboBoxModel<String>) jComboBoxMenuText.getModel()).getIndexOf(menu) == -1) {
				jComboBoxMenuText.addItem(menu);
			}
		}

		for (String parent : commandParents) {
			if (((DefaultComboBoxModel<String>) jComboBoxParentMenuText.getModel()).getIndexOf(parent) == -1) {
				jComboBoxParentMenuText.addItem(parent);
			}
		}

		if (selectedCommand == null || selectedCommand.isBlank()) {
			selectedCommand = CommandFactory.MENU_NEW;
		}

		jComboBoxMenuText.setSelectedItem(selectedCommand);
	}

	private void updateSelectedCommand() {
		updateSelectedCommand(null);
	}

	private void updateSelectedCommand(String menuText) {
		Logging.info(this, "updateSelectedCommand menuText " + menuText);
		if (menuText == null || menuText.isEmpty() || menuText.equals(CommandFactory.MENU_NEW)) {
			updateComponents(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS, "", CommandFactory.DEFAULT_POSITION, "");
		} else {
			updateComponentsMenuText(menuText);
		}
	}

	private void updateComponentsMenuText(String menuText) {
		MultiCommandTemplate thiscommand = factory.getCommandByMenu(menuText);
		if (thiscommand != null) {
			Logging.debug(this, "updateSelectedCommand menu " + thiscommand.getMenuText() + " parent "
					+ thiscommand.getParentMenuText());
			StringBuilder combuf = new StringBuilder();
			for (SingleCommand c : thiscommand.getCommands()) {
				String rawCommand = c.getCommandRaw();
				if (rawCommand != null && !rawCommand.isEmpty()) {
					combuf.append(rawCommand).append("\n");
				}
			}

			updateComponents(thiscommand.getParentMenuText(), thiscommand.getToolTipText(), thiscommand.getPriority(),
					combuf.toString());
		}
	}

	private void updateComponents(String parent, String tooltip, int prio, String coms) {
		if (parent == null || parent.isBlank()) {
			parent = CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS;
		}
		jComboBoxParentMenuText.setSelectedItem(parent);
		jTextFieldTooltipText.setText(tooltip);
		jTextFieldPriority.setText(String.valueOf(prio));
		jTextPaneCommands.setText(coms);
	}

	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 savecommand ");
		MultiCommandTemplate command = getCommandNow();
		if (command == null) {
			return;
		}
		Logging.debug(this, "doAction2 savecommand " + command.toString());

		String menuText = (String) jComboBoxMenuText.getSelectedItem();

		if (factory.saveCommand(command)) {
			updateLists(menuText);
			updateSelectedCommand(menuText);
			factory.reloadServerMenu();
		} else {
			JOptionPane.showInternalMessageDialog(this,
					Configed.getResourceValue("SSHConnection.CommandControl.couldnotsave.title"),
					Configed.getResourceValue("SSHConnection.CommandControl.couldnotsave"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static String generateId(String name) {
		return name.replace(" ", "_").toLowerCase(Locale.ROOT).trim();
	}

	private void doActionTestCommand() {
		Logging.info(this, "doActionTestCommand testCommand building command ...");
		MultiCommandTemplate command = getCommandNow(true /* testing */);
		if (command == null) {
			return;
		}
		if (command.getMenuText() == null) {
			command.setMenuText(CommandFactory.MENU_NEW);
		}
		Logging.debug(this, "doActionTestCommand buildCommand " + command.toString());
		Logging.debug(this, "doActionTestCommand buildCommand commandlist " + command.commandlistToString());

		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain);
				executor.executeMultiCommand(command);
			}
		}.start();
	}

	private void checkAllTexts() {
		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			if (jComboBoxMenuText.getSelectedItem() != null
					&& !((String) jComboBoxMenuText.getSelectedItem()).trim().equals(CommandFactory.MENU_NEW)) {
				Logging.info(this, "checkAllTexts menuText " + jComboBoxMenuText.getSelectedItem());
				MultiCommandTemplate tempCommand = getCommandNow();
				Logging.debug(this, "checkAllTexts command " + tempCommand);
				if (tempCommand == null) {
					return;
				}
				boolean isNotSaved = !factory.isCommandEqualSavedCommand(tempCommand);
				Logging.debug(this, "checkAllTexts factory.isSSHCommandEqualSavedCommand(tmp_com) "
						+ factory.isCommandEqualSavedCommand(tempCommand));
				Logging.debug(this, "checkAllTexts isNotSaved " + isNotSaved);
				buttonSave.setEnabled(isNotSaved);
			} else {
				buttonSave.setEnabled(false);
			}
		}
	}

	private MultiCommandTemplate getCommandNow() {
		return getCommandNow(false);
	}

	private MultiCommandTemplate getCommandNow(boolean testing) {
		Logging.debug(this, "getCommandNow ");
		String menuText = (String) jComboBoxMenuText.getSelectedItem();
		if (!testing && menuText.trim().equals(CommandFactory.MENU_NEW)) {
			return null;
		}
		String parent = (String) jComboBoxParentMenuText.getSelectedItem();
		int prio = 0;
		try {
			prio = Integer.parseInt(!jTextFieldPriority.getText().isEmpty() ? jTextFieldPriority.getText() : "0");
		} catch (NumberFormatException e) {
			Logging.warning("Cannot get value from priority field Exception: ", e);
		}
		List<String> coms = new LinkedList<>();
		for (String c : jTextPaneCommands.getText().split("\n")) {
			if (!(c == null || c.isBlank())) {
				coms.add(c);
			}
		}

		MultiCommandTemplate tempCommand = CommandFactory.buildSSHCommand(
				generateId((String) jComboBoxMenuText.getSelectedItem()), parent, menuText,
				jTextFieldTooltipText.getText(), prio, coms);
		Logging.debug(this, "getCommandNow command: " + tempCommand);

		return tempCommand;
	}

	private void showPanel() {
		Logging.info(this, "showPanel helpPanelStatus always true");

		setCenterLayout();
		commandPanel.setSize(commandPanel.getWidth(), commandPanel.getHeight() + parameterPanel.getHeight());
		this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight());

		repaint();
		revalidate();
	}
}
