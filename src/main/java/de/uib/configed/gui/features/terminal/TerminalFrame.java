/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.terminal;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.core.domain.permission.UserServerConsoleConfig;
import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.core.infrastructure.messagebus.MessagebusListener;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public final class TerminalFrame implements MessagebusListener {
	private JFrame frame;

	private Messagebus messagebus;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// User roles configs
	private boolean fullDepotsPermission = persistenceController.getDataServices().userRoles
			.hasDepotsFullPermissionPD();
	private boolean fullClientsPermission = persistenceController.getDataServices().userRoles
			.isAccessToHostgroupsOnlyIfExplicitlyStatedPD();
	private Set<Object> allowedDepots = persistenceController.getDataServices().userRoles.getPermittedDepots();
	private List<Object> forbiddenItems = persistenceController.getDataServices().userRoles.terminalsForbidden();

	private TerminalTabbedPane tabbedPane;
	private TerminalFileUploadProgressIndicator fileUploadProgressIndicator;

	private boolean restrictView;
	private Runnable callback;
	private String session;
	private ConfigedMain configedMain;

	public TerminalFrame(ConfigedMain configedMain) {
		this(false);
		this.configedMain = configedMain;
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
		frame.setIconImage(Icons.getMainIcon());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		TerminalMenuBar menuBar = new TerminalMenuBar(this, restrictView);
		menuBar.init();
		frame.setJMenuBar(menuBar);

		JPanel northPanel = createNorthPanel();
		fileUploadProgressIndicator = new TerminalFileUploadProgressIndicator();
		fileUploadProgressIndicator.init();
		fileUploadProgressIndicator.setVisible(false);

		JPanel allPane = new JPanel(new MigLayout("insets 0, fill, wrap 1", "[grow]", "[grow]0[pref!]"));
		allPane.add(northPanel, "grow");
		allPane.add(fileUploadProgressIndicator, "growx, hidemode 3");

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

		displaySessionsDialog();
		if (session != null) {
			tabbedPane.addTerminalTab();
			tabbedPane.openSessionOnSelectedTab(session);
		}
	}

	public void changeSession() {
		if (restrictView) {
			return;
		}

		displaySessionsDialog();
		if (session != null) {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				tabbedPane.getSelectedTerminalWidget().close();
				tabbedPane.resetTerminalWidgetOnSelectedTab();
				tabbedPane.openSessionOnSelectedTab(session);
			}
		}
	}

	private void displaySessionsDialog() {
		if (restrictView) {
			return;
		}

		// We want to show the dialog on the owner of the terminal frame only if it is visible
		Component owner;
		if (frame != null && frame.isVisible()) {
			owner = frame;
		} else {
			owner = ConfigedMain.getMainFrame();
		}

		ListSelectionDialog sessionsDialog = new ListSelectionDialog(owner,
				Configed.getResourceValue("Terminal.session.title"));

		// result list (allowed clients and depots connected by message bus)
		List<String> clientsConnectedByMessagebus = getAllowedDevices();

		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.show();

		if (sessionsDialog.wasAccepted()) {
			session = sessionsDialog.getSelectedValue();
		} else {
			session = null;
		}
	}

	/**
	 * Filter the list of connectable devices (clients and depots) by user roles
	 * 'priviliges.host.depotaccess.*' and 'connect.terminal.forbidden'
	 * 
	 * @return List of allowed devices (clients and depots/server)
	 */
	private List<String> getAllowedDevices() {
		// Result list (allowed clients and depots connected by message bus)
		List<String> resultListAllowedDevices = new ArrayList<>();

		if (isConfigServerAllowed(forbiddenItems.contains(UserServerConsoleConfig.KEY_OPT_CONFIGSERVER),
				fullDepotsPermission, allowedDepots)) {
			resultListAllowedDevices.add("Configserver");
		}

		// Don't allow connection to clients when VPN module is not available
		if (!persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.VPN)) {
			return resultListAllowedDevices;
		}

		// list of *all depots* to e.g. distinguish between depots and clients in list of connected devices
		List<String> depotsList = persistenceController.getDataServices().hostInfoCollections.getAllDepotNamesList();
		Logging.info(this, "terminal, depotsList: ", depotsList);

		Set<String> allowedHosts = getAllowedHostsByUserRolesHosts(depotsList);
		List<String> filteredHosts = filterByMsgbusForbiddenConfig(allowedHosts, depotsList);
		separateDepotsAndClients(resultListAllowedDevices, filteredHosts, depotsList);

		return resultListAllowedDevices;
	}

	private static void separateDepotsAndClients(List<String> resultList, List<String> filteredHosts,
			List<String> depotsList) {
		Set<String> uniqueHosts = new LinkedHashSet<>(filteredHosts);
		List<String> depots = new ArrayList<>();
		List<String> clients = new ArrayList<>();

		for (String host : uniqueHosts) {
			(depotsList.contains(host) ? depots : clients).add(host);
		}

		depots.sort(String.CASE_INSENSITIVE_ORDER);
		clients.sort(String.CASE_INSENSITIVE_ORDER);

		resultList.addAll(depots);
		resultList.addAll(clients);
	}

	/**
	 * Check if configserver is allowed by user roles
	 * 'priviliges.host.depotaccess.*' and msgbus-settings
	 * 'connect.terminal.forbidden'
	 * 
	 * @param forbiddenConfigServer true if configserver is forbidden by
	 *                              'connect.terminal.forbidden'
	 * @param fullDepotsPermission  true if user roles
	 *                              'priviliges.host.depotaccess.*' are not
	 *                              configured
	 * @param allowedDepots         List of allowed depots by user role
	 *                              'priviliges.host.depotaccess.depots'
	 * @return true if configserver is allowed
	 */
	private boolean isConfigServerAllowed(boolean forbiddenConfigServer, boolean fullDepotsPermission,
			Set<Object> allowedDepots) {
		Logging.debug(this, "terminal, allowedDepots: ", allowedDepots);
		String configserverName = persistenceController.getDataServices().hostInfoCollections.getConfigServer();
		boolean allowed = (!forbiddenConfigServer
				&& (fullDepotsPermission || allowedDepots.contains(configserverName)));
		Logging.debug(this, "terminal, configserver allowed (", allowed, "): ", configserverName,
				" (depotsConfigured: ", fullDepotsPermission, "allowedDepots: ", allowedDepots, ", ");
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
		Set<String> allClientsDepotsConnected2Msgbus = persistenceController.getDataServices().host
				.getMessagebusConnectedClients();
		Set<String> allClientsDepotsConnected2MsgbusCopy = new HashSet<>(allClientsDepotsConnected2Msgbus);
		Logging.info(this, "terminal, allClientsDepotsConnected2Msgbus: ", allClientsDepotsConnected2Msgbus);

		// list of clients allowed by user roles
		Set<String> clientsOfAllowedDepots = persistenceController.getDataServices().hostInfoCollections
				.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients());
		Logging.info(this, "terminal, clientsForDepots: ", clientsOfAllowedDepots);

		// filter clients and depots by configured permissions (user roles)
		if (!fullDepotsPermission || !fullClientsPermission) {
			for (String clientOrDepot : allClientsDepotsConnected2MsgbusCopy) {
				boolean isDepot = allDepots.contains(clientOrDepot);

				if (isDepot && !fullDepotsPermission && !allowedDepots.contains(clientOrDepot)) {
					allClientsDepotsConnected2Msgbus.remove(clientOrDepot);
				} else if (!isDepot && !clientsOfAllowedDepots.contains(clientOrDepot)) {
					allClientsDepotsConnected2Msgbus.remove(clientOrDepot);
				} else {
					// pass
				}
			}
			Logging.info(this, "terminal, allAllowedClientsAndDepots (without client2Depot): ",
					allClientsDepotsConnected2Msgbus);
		}
		return allClientsDepotsConnected2Msgbus;
	}

	/**
	 * Filter the resultlist (clients and depots allowed by user roles) by the
	 * new user role config 'connect.terminal.forbidden' This method updates the
	 * resultList parameter
	 * 
	 * @param allClientsDepotsAllowedByPrivilege Set of clients and depots
	 *                                           allowed by user roles
	 *                                           (priviliges.host.depotaccess.depots
	 *                                           and
	 *                                           priviliges.host.groupaccess.hostgroups)
	 * @param depotsList                         List of all server/depots
	 *                                           (including forbidden items,
	 *                                           etc.)
	 * @return List of clients and depots allowed by user roles. It will be
	 *         shown to the user.
	 */
	private List<String> filterByMsgbusForbiddenConfig(Set<String> allClientsDepotsAllowedByPrivilege,
			List<String> depotsList) {
		List<String> resultList = new ArrayList<>();
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
				Logging.debug(this, "terminal, allAllowedClients: ", allClientsDepotsAllowedByPrivilege);
				resultList.addAll(allClientsDepotsAllowedByPrivilege);
			} else {
				// !forbiddenDepots
				Logging.info(this, "terminal, allAllowedDepots: ", allDepots);
				resultList.addAll(allDepots);
			}
		}
		return resultList;
	}

	private JPanel createNorthPanel() {

		tabbedPane = new TerminalTabbedPane(this);
		tabbedPane.setMessagebus(messagebus);
		tabbedPane.init();
		addAndInitTerminalTab();

		tabbedPane.getSelectedTerminalWidget().requestFocus();

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new MigLayout("insets 0, fill", "[grow]", "[grow]"));
		northPanel.add(tabbedPane, "grow, hmin 0, wmin 0");

		return northPanel;
	}

	public void focusOnSelectedWidget() {
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

	public static void uploadFile(AbstractBackgroundFileUploader fileUploader) {
		fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + 1);
		fileUploader.execute();
	}

	public void display() {
		if (frame == null) {
			if (session == null) {
				displaySessionsDialog();
			}
			if (restrictView || session != null) {
				createAndShowGUI();
			}
		} else {
			frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
			frame.setVisible(true);

			if (tabbedPane.getTabCount() == 0) {
				addAndInitTerminalTab();
			}
		}

		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
	}

	private void addAndInitTerminalTab() {
		tabbedPane.addTerminalTab();
		if (!restrictView && session != null) {
			tabbedPane.openSessionOnSelectedTab(session);
		} else {
			tabbedPane.getSelectedTerminalWidget().connectPipedTty();
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
		SwingUtilities.invokeLater(() -> frame.dispose());
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
