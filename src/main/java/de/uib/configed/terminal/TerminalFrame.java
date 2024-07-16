/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FSelectionList;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.permission.UserServerConsoleConfig;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public final class TerminalFrame implements MessagebusListener {
	private JFrame frame;

	private Messagebus messagebus;

	// User roles configs
	private boolean depotsConfigured = UserConfig.getCurrentUserConfig()
			.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED);
	private boolean clientsConfigured = UserConfig.getCurrentUserConfig()
			.getBooleanValue(UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED);
	private List<Object> allowedDepots = UserConfig.getCurrentUserConfig()
			.getValues(UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE);
	private List<Object> forbiddenItems = UserConfig.getCurrentUserConfig()
			.getValues(UserServerConsoleConfig.KEY_TERMINAL_ACCESS_FORBIDDEN);

	private TerminalTabbedPane tabbedPane;
	private TerminalFileUploadProgressIndicator fileUploadProgressIndicator;

	private boolean restrictView;
	private Runnable callback;
	private String session;
	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public TerminalFrame(ConfigedMain main) {
		this(false);
		this.configedMain = main;
	}

	public TerminalFrame(boolean restrictView) {
		this.restrictView = restrictView;
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public TerminalTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public TerminalFileUploadProgressIndicator getTerminalFileUploadProgressIndicator() {
		return fileUploadProgressIndicator;
	}

	public void setOnClose(Runnable callback) {
		this.callback = callback;
	}

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setIconImage(Utils.getMainIcon());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		TerminalMenuBar menuBar = new TerminalMenuBar(this, restrictView);
		menuBar.init();
		frame.setJMenuBar(menuBar);

		JPanel allPane = new JPanel();

		GroupLayout allLayout = new GroupLayout(allPane);
		allPane.setLayout(allLayout);

		JPanel northPanel = createNorthPanel();
		fileUploadProgressIndicator = new TerminalFileUploadProgressIndicator();
		fileUploadProgressIndicator.init();
		fileUploadProgressIndicator.setVisible(false);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(fileUploadProgressIndicator));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup().addComponent(northPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(allLayout.createSequentialGroup().addComponent(fileUploadProgressIndicator)));

		frame.add(allPane);

		frame.setSize(600, 400);
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tabbedPane.removeAllTerminalTabs();
				close();
			}

			@Override
			public void windowActivated(WindowEvent e) {
				focusOnSelectedWidget();
			}
		});
	}

	public void openNewWindow() {
		if (restrictView) {
			return;
		}

		TerminalFrame terminalFrame = new TerminalFrame(this.configedMain);
		terminalFrame.setMessagebus(messagebus);
		terminalFrame.display();
	}

	public void openNewSession() {
		if (restrictView) {
			return;
		}

		tabbedPane.addTerminalTab();
		displaySessionsDialog();
	}

	public void displaySessionsDialog() {
		if (restrictView) {
			return;
		}
		FSelectionList sessionsDialog = new FSelectionList(frame, Configed.getResourceValue("Terminal.session.title"),
				true, new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") },
				500, 300);

		// result list (allowed clients and depots connected by message bus)
		List<String> clientsConnectedByMessagebus = getAllowedDevices();
		// sort list of clients and depots and display dialog
		Collections.sort(clientsConnectedByMessagebus);
		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		sessionsDialog.setVisible(true);

		if (sessionsDialog.getResult() == 2) {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				tabbedPane.getSelectedTerminalWidget().close();
				tabbedPane.resetTerminalWidgetOnSelectedTab();
				tabbedPane.openSessionOnSelectedTab(sessionsDialog.getSelectedValue());
			}
		}
	}

	/**
	 * Filter the list of connectable devices (clients and depots) by user roles
	 * 'priviliges.host.depotaccess.*' and 'connect.terminal.forbidden'
	 * 
	 * @return List of allowed devices (clients and depots/server)
	 */
	private List<String> getAllowedDevices() {
		// result list (allowed clients and depots connected by message bus)
		List<String> resultListAllowedDevices = new ArrayList<>();

		if (isConfigServerAllowed(forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER),
				depotsConfigured, allowedDepots)) {
			resultListAllowedDevices.add("Configserver");
		}

		// list of *all depots* to e.g. distinguish between depots and clients in list of connected devices
		List<String> depotsList = persistenceController.getHostInfoCollections().getDepotNamesList();
		Logging.info(this, "terminal, depotsList: " + depotsList);

		Set<String> allClientsDepotsConnected2Msgbus = getAllowedHostsByUserRolesHosts(depotsList);
		filterByMsgbusForbiddenConfig(resultListAllowedDevices, allClientsDepotsConnected2Msgbus, depotsList);
		return resultListAllowedDevices;
	}

	/**
	 * Check if configserver is allowed by user roles
	 * 'priviliges.host.depotaccess.*' and msgbus-settings
	 * 'connect.terminal.forbidden'
	 * 
	 * @param forbiddenConfigServer true if configserver is forbidden by
	 *                              'connect.terminal.forbidden'
	 * @param depotsConfigured      true if user roles
	 *                              'priviliges.host.depotaccess.*' are
	 *                              configured
	 * @param allowedDepots         List of allowed depots by user role
	 *                              'priviliges.host.depotaccess.depots'
	 * @return true if configserver is allowed
	 */
	private boolean isConfigServerAllowed(boolean forbiddenConfigServer, boolean depotsConfigured,
			List<Object> allowedDepots) {
		Logging.debug(this, "terminal, allowedDepots: " + allowedDepots);
		String configserverName = persistenceController.getHostInfoCollections().getConfigServer();
		boolean allowed = (!forbiddenConfigServer && (!depotsConfigured || allowedDepots.contains(configserverName)));
		Logging.debug(this, "terminal, configserver allowed (" + allowed + "): " + configserverName
				+ " (depotsConfigured: " + depotsConfigured + "allowedDepots: " + allowedDepots + ", ");
		return allowed;
	}

	/**
	 * Filter clients and depots by configured permissions (user roles
	 * 'priviliges.host.depotaccess.depots' and
	 * 'priviliges.host.groupaccess.hostgroups') This is done by getting the
	 * connected devices (clients and depots) by message bus and filtering them
	 * by the user roles. The result is a list of clients and depots allowed by
	 * user roles (priviliges.host.depotaccess.depots and
	 * priviliges.host.groupaccess.hostgroups)
	 * 
	 * @param allDepots List of all server/depots (including forbidden items,
	 *                  etc.)
	 * @return Set of clients and depots allowed by user roles
	 */
	private Set<String> getAllowedHostsByUserRolesHosts(List<String> allDepots) {
		Set<String> allClientsDepotsConnected2Msgbus = persistenceController.getHostDataService()
				.getMessagebusConnectedClients();
		Set<String> allClientsDepotsConnected2MsgbusCopy = new HashSet<>(allClientsDepotsConnected2Msgbus);
		Logging.info(this, "terminal, allClientsDepotsConnected2Msgbus: " + allClientsDepotsConnected2Msgbus);

		// list of clients allowed by user roles
		Set<String> clientsOfAllowedDepots = persistenceController.getHostInfoCollections()
				.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients());
		Logging.info(this, "terminal, clientsForDepots: " + clientsOfAllowedDepots);

		// filter clients and depots by configured permissions (user roles)
		if (depotsConfigured || clientsConfigured) {
			for (String clientOrDepot : allClientsDepotsConnected2MsgbusCopy) {
				boolean isDepot = allDepots.contains(clientOrDepot);

				if (isDepot && depotsConfigured && !allowedDepots.contains(clientOrDepot)) {
					allClientsDepotsConnected2Msgbus.remove(clientOrDepot);
				} else if (!isDepot && !clientsOfAllowedDepots.contains(clientOrDepot)) {
					allClientsDepotsConnected2Msgbus.remove(clientOrDepot);
				} else {
					// pass
				}
			}
			Logging.info(this,
					"terminal, allAllowedClientsAndDepots (without client2Depot): " + allClientsDepotsConnected2Msgbus);
		}
		return allClientsDepotsConnected2Msgbus;
	}

	/**
	 * Filter the resultlist (clients and depots allowed by user roles) by the
	 * new user role config 'connect.terminal.forbidden' This method updates the
	 * resultList parameter
	 * 
	 * @param resultList                         List of clients and depots
	 *                                           allowed by user roles. it will
	 *                                           be shown to the user
	 * @param allClientsDepotsAllowedByPrivilege Set of clients and depots
	 *                                           allowed by user roles
	 *                                           (priviliges.host.depotaccess.depots
	 *                                           and
	 *                                           priviliges.host.groupaccess.hostgroups)
	 * @param depotsList                         List of all server/depots
	 *                                           (including forbidden items,
	 *                                           etc.)
	 */
	private void filterByMsgbusForbiddenConfig(List<String> resultList, Set<String> allClientsDepotsAllowedByPrivilege,
			List<String> depotsList) {

		boolean forbiddenDepots = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_DEPOTS);
		boolean forbiddenClients = forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CLIENTS);
		// filter clients and depots by configured permissions (mostly msg bus settings cause user roles already filtered)
		if (forbiddenDepots && forbiddenClients) {
			// pass. no clients or depots allowed
		} else if (!forbiddenDepots && !forbiddenClients) {
			// filtered by allowedDepots and allowedClients (user roles)
			resultList.addAll(allClientsDepotsAllowedByPrivilege);
		} else {
			// either depots or clients forbidden so we split them
			Set<String> allDepots = new HashSet<>();
			for (String depot : depotsList) {
				if (allClientsDepotsAllowedByPrivilege.contains(depot)) {
					allClientsDepotsAllowedByPrivilege.remove(depot);
					allDepots.add(depot);
				}
			}
			// now we have two lists:
			// * allClientsDepotsAllowedByPrivilege contains only clients (we removed the depots)
			// * allDepots contains only depots
			if (!forbiddenClients) {
				Logging.debug(this, "terminal, allAllowedClients: " + allClientsDepotsAllowedByPrivilege);
				resultList.addAll(allClientsDepotsAllowedByPrivilege);
			} else {
				// !forbiddenDepots
				Logging.info(this, "terminal, allAllowedDepots: " + allDepots);
				resultList.addAll(allDepots);
			}
		}
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		tabbedPane = new TerminalTabbedPane(this);
		tabbedPane.setMessagebus(messagebus);
		tabbedPane.init();
		tabbedPane.addTerminalTab();
		if (!restrictView) {
			if (session != null) {
				tabbedPane.openSessionOnSelectedTab(session);
			} else {
				displaySessionsDialog();
			}
		} else {
			tabbedPane.getSelectedTerminalWidget().connectPipedTty();
		}

		tabbedPane.getSelectedTerminalWidget().requestFocus();

		northLayout
				.setVerticalGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE)));

		return northPanel;
	}

	private void focusOnSelectedWidget() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget != null) {
			widget.requestFocusInWindow();
		}
	}

	public void writeToWidget(String message) {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget != null) {
			widget.write(message.getBytes(StandardCharsets.UTF_8));
		}
	}

	public void disableUserInputForSelectedWidget() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		widget.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				widget.setDisableUserInput(true);
				event.consume();
			}

			@Override
			public void keyReleased(KeyEvent event) {
				widget.setDisableUserInput(false);
				event.consume();
			}
		});
		widget.setViewRestricted(true);
	}

	@SuppressWarnings({ "java:S2325" })
	public void uploadFile(AbstractBackgroundFileUploader fileUploader) {
		fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + 1);
		fileUploader.execute();
	}

	public void display() {
		if (frame == null) {
			createAndShowGUI();
		} else {
			frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
			frame.setVisible(true);
		}

		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
	}

	public void changeTitle() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (frame != null && widget != null) {
			frame.setTitle(widget.getTitle());
		}
	}

	public void close() {
		if (callback != null) {
			callback.run();
		}
		frame.dispose();
		messagebus.getWebSocket().unregisterListener(this);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		close();
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		if (tabbedPane.getTabCount() == 0) {
			close();
		}

		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget == null || !widget.isMessageForThisChannel(message)) {
			return;
		}

		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type) && tabbedPane.getTabCount() == 1) {
			close();
		}
	}
}
