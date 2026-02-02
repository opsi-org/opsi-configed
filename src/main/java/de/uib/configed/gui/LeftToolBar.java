/**
 * Copyright (c) UIB GmbH <info@uib.de>
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
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import de.uib.configed.core.domain.permission.UserServerConsoleConfig;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.features.serverconsole.command.CommandExecutor;
import de.uib.configed.gui.features.serverconsole.command.CommandFactory;
import de.uib.configed.gui.features.serverconsole.command.CommandWithParameters;
import de.uib.configed.gui.features.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.gui.features.serverconsole.command.SingleCommand;
import de.uib.configed.gui.features.terminal.TerminalFrame;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class LeftToolBar extends JToolBar {
	// Inititalize it here so that we keep the reference throughout a full reload
	private JMenu jMenuServerConsole = new JMenu(CommandFactory.PARENT_NULL);

	// We also need a reference to the menu bar version of the server console menu
	private JMenu jMenuServerConsoleMenuBar = new JMenu(CommandFactory.PARENT_NULL);

	private JButton terminalButton = new JButton(Icons.getIntellijIcon("terminal", 32));

	private ConfigedMain configedMain;

	public LeftToolBar(ConfigedMain configedMain) {
		super(SwingConstants.VERTICAL);

		this.configedMain = configedMain;

		loadServerConsoleMenu();

		initToolBar();
	}

	private void initToolBar() {
		terminalButton.setToolTipText(CommandFactory.PARENT_NULL);

		// I want the popup on the right of the button, but 
		terminalButton.addActionListener(
				event -> jMenuServerConsole.getPopupMenu().show(terminalButton, terminalButton.getWidth(),
						terminalButton.getHeight() - jMenuServerConsole.getPopupMenu().getPreferredSize().height));

		add(terminalButton);
	}

	private static void setupMenuServerConsole(ConfigedMain configedMain, JMenu jMenuServerConsole) {
		JMenuItem jMenuCommandControl = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuCommandControl"));
		Icons.addIntellijIconToMenuItem(jMenuCommandControl, "edit");
		jMenuCommandControl
				.addActionListener(actionEvent -> ExtraFrameController.startEditTerminalCommandsDialog(configedMain));
		jMenuCommandControl
				.setEnabled(PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.terminalCommandControlIsActive());
		jMenuServerConsole.add(jMenuCommandControl);
		jMenuServerConsole.addSeparator();

		JMenu menuOpsi = new JMenu(CommandFactory.PARENT_OPSI);
		Icons.addOpsiIconToMenuItem(menuOpsi);
		boolean commandsAreDeactivated = !PersistenceControllerFactory.getPersistenceController()
				.getDataServices().userRoles.terminalCommandsIsActive();

		Logging.info("setupMenuTerminal commandsAreDeactivated ", commandsAreDeactivated);
		CommandFactory factory = CommandFactory.getInstance();
		factory.retrieveCommandList();
		addCommandsToMenuServer(configedMain, jMenuServerConsole, menuOpsi, commandsAreDeactivated);
		if (menuOpsi.getSubElements().length != 0) {
			menuOpsi.addSeparator();
		}
		addDefaultOpsiCommandsToMenuOpsi(configedMain, menuOpsi, commandsAreDeactivated);

		JMenuItem jMenuTerminal = new JMenuItem(Configed.getResourceValue("Terminal.title"));
		Icons.addIntellijIconToMenuItem(jMenuTerminal, "terminal");

		// check terminal access rights defined by user roles
		List<Object> forbiddenItems = PersistenceControllerFactory.getPersistenceController()
				.getDataServices().userRoles.terminalsForbidden();
		boolean forbiddenConfigServer = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER);
		boolean forbiddenDepots = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_DEPOTS);
		boolean forbiddenClients = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CLIENTS);

		if (forbiddenConfigServer && forbiddenDepots && forbiddenClients) {
			Logging.info("setupMenuServerConsole all forbidden");
			String s = " " + Configed.getResourceValue("MainFrame.jMenu.attribute.forbidden");
			jMenuTerminal.setText(jMenuTerminal.getText() + s);
			jMenuTerminal.setEnabled(false);
		} else {
			Logging.info("setupMenuServerConsole forbiddenItems ", forbiddenItems);
			Logging.info("setupMenuServerConsole forbiddenConfigServer ", forbiddenConfigServer, " forbiddenDepots ",
					forbiddenDepots, " forbiddenClients ", forbiddenClients);
			jMenuTerminal.addActionListener((ActionEvent e) -> {
				Messagebus.initMessagebus(configedMain);
				TerminalFrame terminal = new TerminalFrame(configedMain);
				terminal.setMessagebus(Messagebus.getInstance());
				terminal.display();
			});
		}

		jMenuServerConsole.add(jMenuTerminal);
	}

	private static void addDefaultOpsiCommandsToMenuOpsi(ConfigedMain configedMain, JMenu menuOpsi,
			boolean commandsAreDeactivated) {
		for (final SingleCommand command : CommandFactory.getDefaultOpsiCommands()) {
			JMenuItem jMenuOpsiCommand = new JMenuItem(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(
					actionEvent -> ((CommandWithParameters) command).startParameterGui(configedMain));
			jMenuOpsiCommand.setEnabled(!commandsAreDeactivated);
			menuOpsi.add(jMenuOpsiCommand);
		}
	}

	private static void addCommandsToMenuServer(ConfigedMain configedMain, JMenu jMenuServerConsole, JMenu menuOpsi,
			boolean commandsAreDeactivated) {
		final CommandFactory factory = CommandFactory.getInstance();
		Map<String, List<MultiCommandTemplate>> sortedComs = factory.getCommandMapSortedByParent();

		Logging.debug("setupMenuServer add commands to menu commands sortedComs ", sortedComs);
		for (Entry<String, List<MultiCommandTemplate>> entry : sortedComs.entrySet()) {
			String parentMenuName = entry.getKey();
			JMenu parentMenu = new JMenu(parentMenuName);

			Logging.info("parent menu text ", parentMenuName);
			if (parentMenuName.equals(CommandFactory.PARENT_OPSI)) {
				jMenuServerConsole.add(menuOpsi);
				jMenuServerConsole.addSeparator();
			}

			addSubCommands(configedMain, jMenuServerConsole, menuOpsi, parentMenu, entry.getValue(),
					commandsAreDeactivated);
		}
	}

	private static void addSubCommands(ConfigedMain configedMain, JMenu jMenuServerConsole, JMenu menuOpsi,
			JMenu parentMenu, List<MultiCommandTemplate> listCom, boolean commandsAreDeactivated) {
		for (final MultiCommandTemplate com : listCom) {
			JMenuItem jMenuItem = new JMenuItem(com.getMenuText());
			Logging.info("command menuitem text ", com.getMenuText());
			jMenuItem.setToolTipText(com.getToolTipText());
			jMenuItem.addActionListener((ActionEvent e) -> {
				CommandExecutor executor = new CommandExecutor(configedMain, com);
				executor.executeAsync();
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

			jMenuItem.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
					.isGlobalReadOnly() && !commandsAreDeactivated);
		}
	}

	public JMenu getJMenuServerConsoleMenuBar() {
		return jMenuServerConsoleMenuBar;
	}

	private void loadServerConsoleMenu() {
		terminalButton.setEnabled(
				!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()
						&& PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
								.terminalMenuIsActive());

		setupMenuServerConsole(configedMain, jMenuServerConsole);

		jMenuServerConsoleMenuBar.setEnabled(
				!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()
						&& PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
								.terminalMenuIsActive());

		setupMenuServerConsole(configedMain, jMenuServerConsoleMenuBar);
	}

	public void reloadServerConsoleMenu() {
		jMenuServerConsole.removeAll();
		jMenuServerConsoleMenuBar.removeAll();

		loadServerConsoleMenu();
	}
}
