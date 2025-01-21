/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.itextpdf.text.Font;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommand;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;

public final class EditTerminalCommandsDialog {
	private JPanel parameterPanel;

	private JComboBox<String> jComboBoxMenuText;

	private JComboBox<String> jComboBoxParentMenuText;
	private JTextField jTextFieldDescription = new JTextField();
	private JTextField jTextFieldPriority = new JTextField();
	private JTextArea jTextAreaCommands = new JTextArea();

	private ConfigedMain configedMain;
	private CommandFactory factory;

	private JOptionPane optionPane;
	private JDialog dialog;

	public EditTerminalCommandsDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		factory = CommandFactory.getInstance();

		JPanel panel = init();

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { Configed.getResourceValue("save"), Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuCommandControl"));
	}

	public void show() {
		dialog.setVisible(true);

		waitForUserInput();
	}

	private void waitForUserInput() {
		Object selectedValue = optionPane.getValue();
		if (selectedValue != null && selectedValue.equals(Configed.getResourceValue("save"))) {
			save();
		}
	}

	private JPanel init() {
		Logging.debug(this, "init setting up components ");

		JPanel controlPanel = initControlPanel();
		JPanel commandPanel = initCommandsPanel();

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(controlPanel).addComponent(commandPanel));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(controlPanel).addGap(Globals.GAP_SIZE)
				.addComponent(commandPanel));

		((CommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
				actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(dialog));

		((CommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
				.addActionListener(actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel)
						.doActionParamAdd(jTextAreaCommands));

		updateLists();
		updateSelectedCommand();

		return panel;
	}

	public JDialog getDialog() {
		return dialog;
	}

	private JPanel initControlPanel() {
		jComboBoxMenuText = new JComboBox<>();
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
		jComboBoxParentMenuText.addItemListener(itemEvent -> canCommandBeSaved());
		jComboBoxParentMenuText.addItem(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS);
		jComboBoxParentMenuText.setEditable(true);

		JButton buttonDelete = new JButton(Icons.getIntellijIcon("remove"));
		buttonDelete.setToolTipText(Configed.getResourceValue("CommandControlDialog.rm_menuText.tooltip"));
		buttonDelete.addActionListener(actionEvent -> deleteCommand());

		JLabel labelMenuText = new JLabel(Configed.getResourceValue("CommandControlDialog.menuText"));
		labelMenuText.setFont(labelMenuText.getFont().deriveFont(Font.BOLD));
		JLabel labelParentMenuText = new JLabel(Configed.getResourceValue("CommandControlDialog.parentMenuText"));
		labelParentMenuText.setFont(labelParentMenuText.getFont().deriveFont(Font.BOLD));
		JLabel labelTooltipText = new JLabel(Configed.getResourceValue("description"));
		labelTooltipText.setFont(labelTooltipText.getFont().deriveFont(Font.BOLD));
		JLabel labelPriority = new JLabel(Configed.getResourceValue("CommandControlDialog.priority"));
		labelPriority.setFont(labelPriority.getFont().deriveFont(Font.BOLD));

		jTextFieldDescription.setToolTipText(Configed.getResourceValue("CommandControlDialog.tooltipText.tooltip"));

		jTextFieldPriority = new JTextField(
				new CheckedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(CommandFactory.DEFAULT_POSITION), 1);
		jTextFieldPriority.setToolTipText(Configed.getResourceValue("CommandControlDialog.priority.tooltip"));
		jTextFieldPriority.setColumns(4);

		JPanel controlPanel = new JPanel();
		GroupLayout controlPanelLayout = new GroupLayout(controlPanel);
		controlPanel.setLayout(controlPanelLayout);

		controlPanelLayout.setHorizontalGroup(controlPanelLayout.createParallelGroup()
				.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(controlPanelLayout.createSequentialGroup()
						.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		controlPanelLayout.setVerticalGroup(controlPanelLayout.createSequentialGroup()
				.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		return controlPanel;
	}

	private JPanel initCommandsPanel() {
		parameterPanel = new CommandControlParameterMethodsPanel(this, configedMain);

		JPanel commandListPanel = new JPanel();
		GroupLayout commandlistPanelLayout = new GroupLayout(commandListPanel);
		commandListPanel.setLayout(commandlistPanelLayout);

		JLabel labelCommands = new JLabel(Configed.getResourceValue("CommandControlDialog.commands"));
		labelCommands.setFont(labelCommands.getFont().deriveFont(Font.BOLD));
		labelCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));

		JButton buttonTestCommand = new JButton(Icons.getIntellijIcon("run"));
		buttonTestCommand.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnTestCommand"));
		buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());

		jTextAreaCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));
		jTextAreaCommands.setRows(5);
		jTextAreaCommands.setColumns(30);
		JScrollPane jScrollPane = new JScrollPane(jTextAreaCommands);

		commandlistPanelLayout.setHorizontalGroup(commandlistPanelLayout.createParallelGroup()
				.addGroup(commandlistPanelLayout.createSequentialGroup()
						.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE).addComponent(buttonTestCommand,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(parameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		commandlistPanelLayout.setVerticalGroup(commandlistPanelLayout.createSequentialGroup()
				.addGroup(commandlistPanelLayout.createParallelGroup()
						.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonTestCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(parameterPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		return commandListPanel;
	}

	private void updateLists() {
		updateLists(null);
	}

	private void updateLists(String selectedCommand) {
		Logging.info(this, "updateLists selectedCommand ", selectedCommand);
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
		Logging.info(this, "updateSelectedCommand menuText ", menuText);
		if (menuText == null || menuText.isEmpty() || menuText.equals(CommandFactory.MENU_NEW)) {
			updateComponents(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS, "", CommandFactory.DEFAULT_POSITION, "");
		} else {
			updateComponentsMenuText(menuText);
		}
	}

	private void updateComponentsMenuText(String menuText) {
		MultiCommandTemplate thiscommand = factory.getCommandByMenu(menuText);
		if (thiscommand != null) {
			Logging.debug(this, "updateSelectedCommand menu ", thiscommand.getMenuText(), " parent ",
					thiscommand.getParentMenuText());
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
		jTextFieldDescription.setText(tooltip);
		jTextFieldPriority.setText(String.valueOf(prio));
		jTextAreaCommands.setText(coms);
	}

	private void deleteCommand() {
		String menu = (String) jComboBoxMenuText.getSelectedItem();
		factory.deleteCommandByMenu(menu);

		jComboBoxMenuText.setSelectedItem(CommandFactory.MENU_NEW);
		updateLists(CommandFactory.MENU_NEW);
		updateSelectedCommand(CommandFactory.MENU_NEW);
		ConfigedMain.getMainFrame().reloadServerConsoleMenu();
	}

	private void save() {
		Logging.info(this, "doAction2 savecommand ");
		MultiCommandTemplate command = getCommandNow();
		if (command == null) {
			return;
		}
		Logging.debug(this, "doAction2 savecommand ", command);

		String menuText = (String) jComboBoxMenuText.getSelectedItem();

		if (factory.saveCommand(command)) {
			updateLists(menuText);
			updateSelectedCommand(menuText);
			ConfigedMain.getMainFrame().reloadServerConsoleMenu();
		} else {
			JOptionPane.showInternalMessageDialog(dialog,
					Configed.getResourceValue("CommandControlDialog.couldnotsave.title"),
					Configed.getResourceValue("CommandControlDialog.couldnotsave"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private static String generateId(String name) {
		return name.replace(" ", "_").toLowerCase(Locale.ROOT).trim();
	}

	private void doActionTestCommand() {
		Logging.info(this, "doActionTestCommand testCommand building command ...");
		MultiCommandTemplate command = getCommandNow(true);
		if (command == null) {
			return;
		}
		if (command.getMenuText() == null) {
			command.setMenuText(CommandFactory.MENU_NEW);
		}
		Logging.debug(this, "doActionTestCommand buildCommand ", command);
		Logging.debug(this, "doActionTestCommand buildCommand commandlist ", command.getCommands());

		new Thread() {
			@Override
			public void run() {
				CommandExecutor executor = new CommandExecutor(configedMain, command);
				executor.execute();
			}
		}.start();
	}

	private boolean canCommandBeSaved() {
		boolean commandCanBeSaved = false;

		if (jComboBoxMenuText.getSelectedItem() != null
				&& !((String) jComboBoxMenuText.getSelectedItem()).trim().equals(CommandFactory.MENU_NEW)) {
			Logging.info(this, "canCommandBeSaved menuText ", jComboBoxMenuText.getSelectedItem());
			MultiCommandTemplate tempCommand = getCommandNow();
			Logging.debug(this, "canCommandBeSaved command ", tempCommand);
			if (tempCommand == null) {
				return commandCanBeSaved;
			}
			Logging.debug(this, "canCommandBeSaved is command saved ", factory.isCommandEqualSavedCommand(tempCommand));
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
			Logging.warning(e, "Cannot get value from priority field Exception: ");
		}
		List<String> coms = new LinkedList<>();
		for (String c : jTextAreaCommands.getText().split("\n")) {
			if (!(c == null || c.isBlank())) {
				coms.add(c);
			}
		}

		MultiCommandTemplate tempCommand = CommandFactory.buildCommand(
				generateId((String) jComboBoxMenuText.getSelectedItem()), parent, menuText,
				jTextFieldDescription.getText(), prio, coms);
		Logging.debug(this, "getCommandNow command: ", tempCommand);

		return tempCommand;
	}
}
