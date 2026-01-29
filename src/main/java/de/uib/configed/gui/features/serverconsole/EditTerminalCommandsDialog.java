/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.serverconsole;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
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

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommand;
import de.uib.configed.gui.share.swing.CheckedDocument;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

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

	private CommandExecutor executor;

	public EditTerminalCommandsDialog(ConfigedMain configedMain) {
		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("feature.permissionDenied.message"),
					Configed.getResourceValue("permissionDenied"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		this.configedMain = configedMain;
		this.executor = new CommandExecutor(configedMain);
		factory = CommandFactory.getInstance();

		initComponents();
		JPanel panel = init();

		JButton saveButton = new JButton(Configed.getResourceValue("save"));
		saveButton.addActionListener(actionEvent -> save());

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { saveButton, Configed.getResourceValue("buttonCancel") });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuCommandControl"));
		dialog.setModal(false);
		dialog.pack();

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	private JPanel init() {
		Logging.debug(this, "init setting up components ");

		JPanel controlPanel = initControlPanel();
		JPanel commandPanel = initCommandsPanel();

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0[grow]"));
		panel.add(controlPanel, "growx");
		panel.add(commandPanel, "grow");

		((CommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
				actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(dialog));

		((CommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
				.addActionListener(actionEvent -> ((CommandControlParameterMethodsPanel) parameterPanel)
						.doActionParamAdd(jTextAreaCommands));

		updateLists("");
		updateSelectedCommand();

		return panel;
	}

	public JDialog getDialog() {
		return dialog;
	}

	private void initComponents() {
		jComboBoxMenuText = new JComboBox<>();
		jComboBoxMenuText.addItem("");
		jComboBoxMenuText.setToolTipText(Configed.getResourceValue("CommandControlDialog.menuText.tooltip"));
		jComboBoxMenuText.setEditable(true);
		final JTextComponent editor = (JTextComponent) jComboBoxMenuText.getEditor().getEditorComponent();
		jComboBoxMenuText.addItemListener((ItemEvent itemEvent) -> {
			updateSelectedCommand(editor.getText());
			canCommandBeSaved();
		});

		jComboBoxParentMenuText = new JComboBox<>();
		jComboBoxParentMenuText.addItemListener(itemEvent -> canCommandBeSaved());
		jComboBoxParentMenuText.addItem(CommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS);
		jComboBoxParentMenuText.setEditable(true);

		jTextFieldDescription.setToolTipText(Configed.getResourceValue("CommandControlDialog.tooltipText.tooltip"));

		jTextFieldPriority = new JTextField(
				new CheckedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(CommandFactory.DEFAULT_POSITION), 1);
		jTextFieldPriority.setToolTipText(Configed.getResourceValue("CommandControlDialog.priority.tooltip"));
		jTextFieldPriority.setColumns(4);
	}

	private JPanel initControlPanel() {
		JButton buttonDelete = new JButton(Icons.getIntellijIcon("remove"));
		buttonDelete.setToolTipText(Configed.getResourceValue("CommandControlDialog.rm_menuText.tooltip"));
		buttonDelete.addActionListener(actionEvent -> deleteCommand());

		JLabel labelMenuText = Utils.createBoldLabel("CommandControlDialog.menuText");
		JLabel labelParentMenuText = Utils.createBoldLabel("CommandControlDialog.parentMenuText");
		JLabel labelTooltipText = Utils.createBoldLabel("description");
		JLabel labelPriority = Utils.createBoldLabel("CommandControlDialog.priority");

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new MigLayout("insets 0, fillx, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		controlPanel.add(labelMenuText);
		controlPanel.add(jComboBoxMenuText, "split 2, growx");
		controlPanel.add(buttonDelete, "align center, gapbottom " + Globals.GAP_SIZE + ", wrap");
		controlPanel.add(labelParentMenuText);
		controlPanel.add(jComboBoxParentMenuText, "growx, gapbottom " + Globals.GAP_SIZE);
		controlPanel.add(labelTooltipText);
		controlPanel.add(jTextFieldDescription, "growx, gapbottom " + Globals.GAP_SIZE);
		controlPanel.add(labelPriority);
		controlPanel.add(jTextFieldPriority, "gapbottom " + Globals.GAP_SIZE);

		return controlPanel;
	}

	private JPanel initCommandsPanel() {
		parameterPanel = new CommandControlParameterMethodsPanel(this, configedMain);

		JLabel labelCommands = Utils.createBoldLabel("CommandControlDialog.commands");
		labelCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));

		JButton buttonTestCommand = new JButton(Icons.getIntellijIcon("run"));
		buttonTestCommand.setToolTipText(Configed.getResourceValue("CommandControlDialog.btnTestCommand"));
		buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());

		jTextAreaCommands.setToolTipText(Configed.getResourceValue("CommandControlDialog.commands.tooltip"));
		jTextAreaCommands.setRows(5);
		jTextAreaCommands.setColumns(30);
		JScrollPane jScrollPane = new JScrollPane(jTextAreaCommands);

		JPanel commandListPanel = new JPanel();
		commandListPanel.setLayout(new MigLayout("insets 0, fill, gapy " + Globals.GAP_SIZE + ", wrap 1", "", "[]0"));
		commandListPanel.add(labelCommands, "split 2");
		commandListPanel.add(buttonTestCommand, "align right, gapbefore push, wrap");
		commandListPanel.add(jScrollPane, "grow, push, gapbottom " + Globals.GAP_SIZE);
		commandListPanel.add(parameterPanel);

		return commandListPanel;
	}

	private void updateLists(String selectedCommand) {
		Logging.info(this, "updateLists selectedCommand ", selectedCommand);
		jComboBoxMenuText.removeAllItems();
		jComboBoxParentMenuText.removeAllItems();

		addItemsToComboBox(jComboBoxMenuText, factory.getCommandMenuNames());
		addItemsToComboBox(jComboBoxParentMenuText, factory.getCommandMenuParents());

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
		if (menuText == null || menuText.isEmpty()) {
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

		updateLists("");
		updateSelectedCommand("");
		ConfigedMain.getMainFrame().reloadServerConsoleMenu();
	}

	private void save() {
		Logging.info(this, "doAction2 savecommand ");
		MultiCommandTemplate command = getCommandNow();

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
		MultiCommandTemplate command = getCommandNow();

		Logging.debug(this, "doActionTestCommand buildCommand ", command);
		Logging.debug(this, "doActionTestCommand buildCommand commandlist ", command.getCommands());

		Utils.runSwingWorker(() -> {
			executor.setMultiCommand(command);
			executor.execute();
			return null;
		}, (Void _) -> jTextAreaCommands.requestFocus(), null);
	}

	private boolean canCommandBeSaved() {
		boolean commandCanBeSaved = false;

		if (jComboBoxMenuText.getSelectedItem() != null) {
			Logging.info(this, "canCommandBeSaved menuText ", jComboBoxMenuText.getSelectedItem());
			MultiCommandTemplate tempCommand = getCommandNow();
			Logging.debug(this, "canCommandBeSaved command ", tempCommand);

			Logging.debug(this, "canCommandBeSaved is command saved ", factory.isCommandEqualSavedCommand(tempCommand));
			commandCanBeSaved = !factory.isCommandEqualSavedCommand(tempCommand);
		}

		return commandCanBeSaved;
	}

	private MultiCommandTemplate getCommandNow() {
		Logging.debug(this, "getCommandNow ");

		String parent = (String) jComboBoxParentMenuText.getSelectedItem();
		int prio = 0;
		try {
			prio = Integer.parseInt(!jTextFieldPriority.getText().isEmpty() ? jTextFieldPriority.getText() : "0");
		} catch (NumberFormatException e) {
			Logging.warning(e, "Cannot get value from priority field Exception: ");
		}
		List<String> coms = new ArrayList<>();
		for (String c : jTextAreaCommands.getText().split("\n")) {
			if (!(c == null || c.isBlank())) {
				coms.add(c);
			}
		}

		String menuText = (String) jComboBoxMenuText.getSelectedItem();

		MultiCommandTemplate tempCommand = CommandFactory.buildCommand(generateId(menuText), parent, menuText,
				jTextFieldDescription.getText(), prio, coms);
		Logging.debug(this, "getCommandNow command: ", tempCommand);

		return tempCommand;
	}
}
