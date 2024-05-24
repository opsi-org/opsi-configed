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
import javax.swing.JScrollPane;
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
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;

public final class CommandControlDialog extends FGeneralDialog {
	private static final int FRAME_WIDTH = 850;
	private static final int FRAME_HEIGHT = 600;

	private JPanel parameterPanel;

	private JButton buttonSave = new JButton(Configed.getResourceValue("CommandControlDialog.ButtonSave"));

	private JComboBox<String> jComboBoxMenuText;

	private JComboBox<String> jComboBoxParentMenuText;
	private JTextField jTextFieldTooltipText = new JTextField();
	private JTextField jTextFieldPriority = new JTextField();
	private JTextPane jTextPaneCommands = new JTextPane();

	private ConfigedMain configedMain;
	private final CommandFactory factory;

	public CommandControlDialog(ConfigedMain configedMain) {
		super(null, Configed.getResourceValue("MainFrame.jMenuCommandControl"));
		this.configedMain = configedMain;
		factory = CommandFactory.getInstance();
		parameterPanel = new CommandControlParameterMethodsPanel(this, configedMain);
		init();

		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationRelativeTo(ConfigedMain.getMainFrame());
		this.setVisible(true);
	}

	private void init() {
		Logging.debug(this, "init setting up components ");

		parameterPanel.setVisible(true);

		getContentPane().add(initControlPanel(), BorderLayout.NORTH);
		getContentPane().add(initCommandPanel(), BorderLayout.CENTER);
		getContentPane().add(initButtonPanel(), BorderLayout.SOUTH);

		this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight());

		repaint();
		revalidate();

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

		JButton buttonClose = new JButton(Configed.getResourceValue("buttonClose"));
		buttonClose.addActionListener(actionEvent -> doAction1());
	}

	private JPanel initControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createTitledBorder(""));

		GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
		controlPanel.setLayout(controlPanelLayout);

		Dimension dimensionJTextField = new Dimension(Globals.FIRST_LABEL_WIDTH - Globals.GRAPHIC_BUTTON_SIZE,
				Globals.BUTTON_HEIGHT);
		Dimension dimensionButton = new Dimension(Globals.GRAPHIC_BUTTON_SIZE, Globals.BUTTON_HEIGHT);

		jComboBoxMenuText = new JComboBox<>();
		jComboBoxMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxMenuText.addItem(CommandFactory.MENU_NEW);
		jComboBoxMenuText.setToolTipText(Configed.getResourceValue("CommandControlDialog.menuText.tooltip"));
		jComboBoxMenuText.setEditable(true);
		final JTextComponent editor = (JTextComponent) jComboBoxMenuText.getEditor().getEditorComponent();
		jComboBoxMenuText.addItemListener((ItemEvent itemEvent) -> {
			if (editor.getText().trim().equals(CommandFactory.MENU_NEW)) {
				editor.setSelectionStart(0);
				editor.setSelectionEnd(editor.getText().length());
			}
			updateSelectedCommand(editor.getText());
			canCommandBeSaved();
		});

		jComboBoxParentMenuText = new JComboBox<>();
		jComboBoxParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxParentMenuText.addItemListener(itemEvent -> canCommandBeSaved());
		jComboBoxParentMenuText.addItem(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS);
		jComboBoxParentMenuText.setEditable(true);
		jComboBoxParentMenuText.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

		JButton buttonDelete = new IconButton(Configed.getResourceValue("CommandControlDialog.rm_menuText.tooltip"),
				"images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png", true);
		buttonDelete.setSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		buttonDelete.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		buttonDelete.addActionListener((ActionEvent actionEvent) -> deleteCommand());
		buttonDelete.setEnabled(!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly());

		JLabel labelMenuText = new JLabel(Configed.getResourceValue("CommandControlDialog.menuText"));
		labelMenuText.setPreferredSize(dimensionJTextField);
		JLabel labelParentMenuText = new JLabel(Configed.getResourceValue("CommandControlDialog.parentMenuText"));
		labelParentMenuText.setPreferredSize(dimensionJTextField);
		JLabel labelTooltipText = new JLabel(Configed.getResourceValue("CommandControlDialog.tooltipText"));
		labelTooltipText.setPreferredSize(dimensionJTextField);
		JLabel labelPriority = new JLabel(Configed.getResourceValue("CommandControlDialog.priority"));
		labelPriority.setPreferredSize(dimensionJTextField);

		jTextFieldTooltipText.setToolTipText(Configed.getResourceValue("CommandControlDialog.tooltipText.tooltip"));
		jTextFieldTooltipText.setPreferredSize(dimensionJTextField);
		jTextFieldTooltipText.getDocument().addDocumentListener(new SaveButtonEnabler());
		jTextFieldTooltipText.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

		jTextFieldPriority = new JTextField(
				new CheckedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(CommandFactory.DEFAULT_POSITION), 1);
		jTextFieldPriority.setToolTipText(Configed.getResourceValue("CommandControlDialog.priority.tooltip"));
		jTextFieldPriority.setPreferredSize(dimensionButton);
		jTextFieldPriority.getDocument().addDocumentListener(new SaveButtonEnabler());
		jTextFieldPriority.setColumns(4);
		jTextFieldPriority.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

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

		return controlPanel;
	}

	private JPanel initCommandPanel() {
		JPanel commandPanel = new JPanel();
		commandPanel.setBorder(BorderFactory.createTitledBorder(""));
		commandPanel.setSize(commandPanel.getWidth(), commandPanel.getHeight() + parameterPanel.getHeight());

		GroupLayout commandPanelLayout = new GroupLayout(commandPanel);
		commandPanel.setLayout(commandPanelLayout);
		commandPanelLayout.setAutoCreateGaps(true);

		JPanel commandListPanel = initCommandListPanel();

		commandPanelLayout.setHorizontalGroup(
				commandPanelLayout.createParallelGroup().addComponent(commandListPanel).addComponent(parameterPanel));
		commandPanelLayout.setVerticalGroup(
				commandPanelLayout.createSequentialGroup().addComponent(commandListPanel).addComponent(parameterPanel));

		return commandPanel;
	}

	private JPanel initCommandListPanel() {
		JPanel commandListPanel = new JPanel();
		commandListPanel.setPreferredSize(new Dimension(FRAME_WIDTH - 30, 150));
		commandListPanel.setSize(new Dimension(FRAME_WIDTH - 30, 150));

		GroupLayout commandlistPanelLayout = new GroupLayout(commandListPanel);
		commandListPanel.setLayout(commandlistPanelLayout);

		JLabel labelCommands = new JLabel(Configed.getResourceValue("CommandControlDialog.commands"));
		labelCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));

		JButton buttonTestCommand = new JButton(Utils.getIntellijIcon("run"));
		buttonTestCommand.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnTestCommand"));
		buttonTestCommand.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_SIZE + 15, Globals.BUTTON_HEIGHT));
		buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());
		buttonTestCommand.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());

		Dimension dimensionJTextFieldLong = new Dimension(Globals.FIRST_LABEL_WIDTH, Globals.BUTTON_HEIGHT);
		jTextPaneCommands = new JTextPane();
		jTextPaneCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));
		jTextPaneCommands.setPreferredSize(dimensionJTextFieldLong);
		jTextPaneCommands.getDocument().addDocumentListener(new SaveButtonEnabler());
		jTextPaneCommands.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly());
		JScrollPane jScrollPane = new JScrollPane(jTextPaneCommands);

		commandlistPanelLayout
				.setHorizontalGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
						.addGroup(commandlistPanelLayout.createParallelGroup()
								.addGroup(commandlistPanelLayout.createSequentialGroup()
										.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addComponent(buttonTestCommand,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)

								).addGap(Globals.MIN_GAP_SIZE).addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
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
				.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 1));

		return commandListPanel;
	}

	private JPanel initButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

		buttonSave.addActionListener(actionEvent -> doAction2());

		JButton buttonClose = new JButton(Configed.getResourceValue("buttonClose"));
		buttonClose.addActionListener(actionEvent -> doAction1());

		buttonPanel.add(buttonClose);
		buttonPanel.add(buttonSave);

		return buttonPanel;
	}

	private void updateLists() {
		updateLists(null);
	}

	private void updateLists(String selectedCommand) {
		Logging.info(this, "updateLists selectedCommand " + selectedCommand);
		jComboBoxMenuText.removeAllItems();
		jComboBoxParentMenuText.removeAllItems();

		addItemsToComboBox(jComboBoxMenuText, factory.getCommandMenuNames());
		addItemsToComboBox(jComboBoxParentMenuText, factory.getCommandMenuParents());

		if (selectedCommand == null || selectedCommand.isBlank()) {
			selectedCommand = CommandFactory.MENU_NEW;
		}

		jComboBoxMenuText.setSelectedItem(selectedCommand);
	}

	private static void addItemsToComboBox(JComboBox<String> comboBox, List<String> items) {
		for (String item : items) {
			if (((DefaultComboBoxModel<String>) comboBox.getModel()).getIndexOf(item) == -1) {
				comboBox.addItem(item);
			}
		}
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
		jComboBoxParentMenuText.setSelectedItem(parent);
		jTextFieldTooltipText.setText(tooltip);
		jTextFieldPriority.setText(String.valueOf(prio));
		jTextPaneCommands.setText(coms);
	}

	private void deleteCommand() {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return;
		}
		String menu = (String) jComboBoxMenuText.getSelectedItem();
		factory.deleteCommandByMenu(menu);

		jComboBoxMenuText.setSelectedItem(CommandFactory.MENU_NEW);
		updateLists(CommandFactory.MENU_NEW);
		updateSelectedCommand(CommandFactory.MENU_NEW);
		ConfigedMain.getMainFrame().reloadServerConsoleMenu();
	}

	@Override
	public void doAction2() {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return;
		}

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
			ConfigedMain.getMainFrame().reloadServerConsoleMenu();
		} else {
			JOptionPane.showInternalMessageDialog(this,
					Configed.getResourceValue("CommandControlDialog.couldnotsave.title"),
					Configed.getResourceValue("CommandControlDialog.couldnotsave"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static String generateId(String name) {
		return name.replace(" ", "_").toLowerCase(Locale.ROOT).trim();
	}

	private void doActionTestCommand() {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return;
		}

		Logging.info(this, "doActionTestCommand testCommand building command ...");
		MultiCommandTemplate command = getCommandNow(true);
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
				CommandExecutor executor = new CommandExecutor(configedMain, command);
				executor.execute();
			}
		}.start();
	}

	private class SaveButtonEnabler implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			buttonSave.setEnabled(canCommandBeSaved());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			buttonSave.setEnabled(canCommandBeSaved());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			// Plain text components do not fire these events
		}
	}

	private boolean canCommandBeSaved() {
		boolean commandCanBeSaved = false;
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			return commandCanBeSaved;
		}

		if (jComboBoxMenuText.getSelectedItem() != null
				&& !((String) jComboBoxMenuText.getSelectedItem()).trim().equals(CommandFactory.MENU_NEW)) {
			Logging.info(this, "canCommandBeSaved menuText " + jComboBoxMenuText.getSelectedItem());
			MultiCommandTemplate tempCommand = getCommandNow();
			Logging.debug(this, "canCommandBeSaved command " + tempCommand);
			if (tempCommand == null) {
				return commandCanBeSaved;
			}
			Logging.debug(this,
					"canCommandBeSaved is command saved " + factory.isCommandEqualSavedCommand(tempCommand));
			commandCanBeSaved = !factory.isCommandEqualSavedCommand(tempCommand);
		}

		return commandCanBeSaved;
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

		MultiCommandTemplate tempCommand = CommandFactory.buildCommand(
				generateId((String) jComboBoxMenuText.getSelectedItem()), parent, menuText,
				jTextFieldTooltipText.getText(), prio, coms);
		Logging.debug(this, "getCommandNow command: " + tempCommand);

		return tempCommand;
	}
}
