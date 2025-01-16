/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.CommandFactory;
import de.uib.configed.serverconsole.command.CommandWithParameters;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommand;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.messagebus.Messagebus;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserServerConsoleConfig;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;

public class LeftToolBar extends JToolBar {
	// Inititalize it here so that we keep the reference throughout a full reload
	private JPopupMenu jMenuServerConsole = new JPopupMenu(CommandFactory.PARENT_NULL);

	private ConfigedMain configedMain;

	public LeftToolBar(ConfigedMain configedMain) {
		super(SwingConstants.VERTICAL);

		this.configedMain = configedMain;

		setupMenuServerConsole();

		initToolBar();
	}

	private void initToolBar() {
		JButton terminalButton = new JButton(Icons.getIntellijIcon("terminal", 32));
		terminalButton.setToolTipText(CommandFactory.PARENT_NULL);

		// I want the popup on the right of the button, but 
		terminalButton.addActionListener(event -> jMenuServerConsole.show(terminalButton, terminalButton.getWidth(),
				terminalButton.getHeight() - jMenuServerConsole.getPreferredSize().height));

		terminalButton.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly()
				&& UserConfig.getCurrentUserConfig()
						.getBooleanValue(UserServerConsoleConfig.KEY_SERVER_CONSOLE_MENU_ACTIVE));

		add(terminalButton);
	}

	private void setupMenuServerConsole() {
		JMenuItem jMenuCommandControl = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuCommandControl"));
		Icons.addIntellijIconToMenuItem(jMenuCommandControl, "edit");
		jMenuCommandControl
				.addActionListener(actionEvent -> ExtraFrameController.startEditTerminalCommandsDialog(configedMain));
		jMenuServerConsole.add(jMenuCommandControl);
		jMenuServerConsole.addSeparator();

		JMenu menuOpsi = new JMenu(CommandFactory.PARENT_OPSI);
		Icons.addOpsiIconToMenuItem(menuOpsi);
		boolean commandsAreDeactivated = !Boolean.TRUE.equals(UserConfig.getCurrentUserConfig()
				.getBooleanValue(UserServerConsoleConfig.KEY_SERVER_CONSOLE_COMMANDS_ACTIVE));
		Logging.info(this, "setupMenuTerminal commandsAreDeactivated ", commandsAreDeactivated);
		CommandFactory factory = CommandFactory.getInstance();
		factory.retrieveCommandList();
		addCommandsToMenuServer(menuOpsi, commandsAreDeactivated);
		if (menuOpsi.getSubElements().length != 0) {
			menuOpsi.addSeparator();
		}
		addDefaultOpsiCommandsToMenuOpsi(menuOpsi, commandsAreDeactivated);

		jMenuServerConsole.setEnabled(!PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().isGlobalReadOnly()
				&& PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
						.terminalMenuIsActive());

		JMenuItem jMenuTerminal = new JMenuItem(Configed.getResourceValue("Terminal.title"));
		Icons.addIntellijIconToMenuItem(jMenuTerminal, "terminal");

		// check terminal access rights defined by user roles
		List<Object> forbiddenItems = PersistenceControllerFactory.getPersistenceController()
				.getUserRolesConfigDataService().terminalsForbidden();
		boolean forbiddenConfigServer = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER);
		boolean forbiddenDepots = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_DEPOTS);
		boolean forbiddenClients = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CLIENTS);

		if (forbiddenConfigServer && forbiddenDepots && forbiddenClients) {
			Logging.info(this, "setupMenuServerConsole all forbidden");
			String s = " " + Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden");
			jMenuTerminal.setText(jMenuTerminal.getText() + s);
			jMenuTerminal.setEnabled(false);
		} else {
			Logging.info(this, "setupMenuServerConsole forbiddenItems ", forbiddenItems);
			Logging.info(this, "setupMenuServerConsole forbiddenConfigServer ", forbiddenConfigServer,
					" forbiddenDepots ", forbiddenDepots, " forbiddenClients ", forbiddenClients);
			jMenuTerminal.addActionListener((ActionEvent e) -> {
				Messagebus.initMessagebus(configedMain);
				TerminalFrame terminal = new TerminalFrame(configedMain);
				terminal.setMessagebus(Messagebus.getInstance());
				terminal.display();
			});
		}

		jMenuServerConsole.add(jMenuTerminal);
	}

	private void addDefaultOpsiCommandsToMenuOpsi(JMenu menuOpsi, boolean commandsAreDeactivated) {
		for (final SingleCommand command : CommandFactory.getDefaultOpsiCommands()) {
			JMenuItem jMenuOpsiCommand = new JMenuItem(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(
					actionEvent -> ((CommandWithParameters) command).startParameterGui(configedMain));
			jMenuOpsiCommand.setEnabled(!PersistenceControllerFactory.getPersistenceController()
					.getUserRolesConfigDataService().isGlobalReadOnly() && !commandsAreDeactivated);
			menuOpsi.add(jMenuOpsiCommand);
		}
	}

	private void addCommandsToMenuServer(JMenu menuOpsi, boolean commandsAreDeactivated) {
		final CommandFactory factory = CommandFactory.getInstance();
		Map<String, List<MultiCommandTemplate>> sortedComs = factory.getCommandMapSortedByParent();

		Logging.debug(this, "setupMenuServer add commands to menu commands sortedComs ", sortedComs);
		for (Entry<String, List<MultiCommandTemplate>> entry : sortedComs.entrySet()) {
			String parentMenuName = entry.getKey();
			JMenu parentMenu = new JMenu(parentMenuName);

			Logging.info(this, "parent menu text ", parentMenuName);
			if (parentMenuName.equals(CommandFactory.PARENT_OPSI)) {
				jMenuServerConsole.add(menuOpsi);
				jMenuServerConsole.addSeparator();
			}

			addSubCommands(menuOpsi, parentMenu, entry.getValue(), commandsAreDeactivated);
		}
	}

	private void addSubCommands(JMenu menuOpsi, JMenu parentMenu, List<MultiCommandTemplate> listCom,
			boolean commandsAreDeactivated) {
		for (final MultiCommandTemplate com : listCom) {
			JMenuItem jMenuItem = new JMenuItem(com.getMenuText());
			Logging.info(this, "command menuitem text ", com.getMenuText());
			jMenuItem.setToolTipText(com.getToolTipText());
			jMenuItem.addActionListener((ActionEvent e) -> {
				CommandExecutor executor = new CommandExecutor(configedMain, com);
				executor.execute();
			});

			String parentMenuName = parentMenu.getText();
			if (parentMenuName.equals(CommandFactory.PARENT_NULL)) {
				jMenuServerConsole.add(jMenuItem);
			} else if (parentMenuName.equals(CommandFactory.PARENT_OPSI)) {
				menuOpsi.add(jMenuItem);
			} else {
				parentMenu.add(jMenuItem);
				jMenuServerConsole.add(parentMenu);
			}

			jMenuItem.setEnabled(!PersistenceControllerFactory.getPersistenceController()
					.getUserRolesConfigDataService().isGlobalReadOnly() && !commandsAreDeactivated);
		}
	}
}
