/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.CopyClient.CopyOption;
import de.uib.configed.gui.features.tree.ClientTree;
import de.uib.configed.gui.share.swing.CheckedDocument;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.gui.type.licenses.LicenseUsageEntry;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public final class ServerActionManager {
	public static final String KEY_PROCESS_ACTION_REQUEST_DEFAULT = "";
	public static final String KEY_PROCESS_ACTION_REQUEST_VISIBLE = "visible";
	public static final String KEY_PROCESS_ACTION_REQUEST_HIDDEN = "hidden";

	private static ConfigedMain configedMain;

	private static OpsiServiceNOMPersistenceController persistenceController;

	private static AtomicBoolean isLocalChangeInProgress = new AtomicBoolean(false);

	// We want a private empty constructor to prevent instantiation of this class
	private ServerActionManager() {
	}

	// We need to init the data since in the beginning they're not there yet
	public static void initData(ConfigedMain configedMain, OpsiServiceNOMPersistenceController persistenceController) {
		ServerActionManager.configedMain = configedMain;
		ServerActionManager.persistenceController = persistenceController;
	}

	public static void createClients(List<Map<String, Object>> clients) {
		List<String> createdClientNames = clients.stream()
				.map(v -> (String) v.get(HostInfo.HOSTNAME_KEY) + "." + v.get(HostInfo.CSV_DOMAIN_KEY)).toList();
		isLocalChangeInProgress.set(true);

		try {
			persistenceController.getDataServices().hostInfoCollections.addOpsiHostNames(createdClientNames);
			if (persistenceController.getDataServices().host.createClients(clients)) {
				Logging.debug("createClients", clients);
				Logging.checkErrorList();

				persistenceController.reloadData(ReloadEvent.HOST_DATA_RELOAD.toString());

				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(false, getGroupToActivate(clients));
				configedMain.setClients(createdClientNames);
			} else {
				persistenceController.getDataServices().hostInfoCollections.removeOpsiHostNames(createdClientNames);
			}
		} finally {
			isLocalChangeInProgress.set(false);
		}
	}

	@SuppressWarnings("unchecked")
	private static String getGroupToActivate(List<Map<String, Object>> clients) {
		// We want to activate the group if we create exactly one client in exactly one group
		if (clients.size() == 1) {
			Map<String, Object> client = clients.get(0);
			List<String> groups = (List<String>) client.get(HostInfo.CSV_GROUPS_KEY);
			if (groups.size() == 1) {
				return groups.get(0);
			}
		}
		return ClientTree.ALL_CLIENTS_NAME;
	}

	public static void wakeSelectedClients() {
		Logging.info("wakeUp ", configedMain.getSelectedClients().size());
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoWakeClients")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getDataServices().rpcMethodExecutor
						.wakeOnLanOpsi43(configedMain.getSelectedClients());
			}
		}.start();
	}

	public static void deletePackageCachesOfSelectedClients() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getDataServices().rpcMethodExecutor
						.deletePackageCaches(configedMain.getSelectedClients());
			}
		}.start();
	}

	public static void fireOpsiclientdEventOnSelectedClients(final String event) {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer("opsiclientd " + event) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getDataServices().rpcMethodExecutor.fireOpsiclientdEventOnClients(event,
						configedMain.getSelectedClients());
			}
		}.start();
	}

	public static void processActionRequestsAllProducts(String visibility) {
		processActionRequests(Set.of(), visibility);
	}

	public static void processActionRequestsSelectedProducts(String visibility) {
		processActionRequests(ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration()
				.getPanelLocalbootProductSettings().getProductTableModified().getSelectedIDs(), visibility);
	}

	private static void processActionRequests(Set<String> products, String visibility) {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		ChangedDataManager.checkSaveAll(false);

		new AbstractErrorListProducer("opsiclientd processActionRequests") {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getDataServices().rpcMethodExecutor
						.processActionRequests(configedMain.getSelectedClients(), products, visibility);
			}
		}.start();
	}

	public static void showPopupOnSelectedClients(final String message, final Float seconds) {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoPopup") + " " + message) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getDataServices().rpcMethodExecutor.showPopupOnClients(message,
						configedMain.getSelectedClients(), seconds);
			}
		}.start();
	}

	public static void shutdownSelectedClients() {
		if (persistenceController.getDataServices().userRoles.isGlobalReadOnly()
				|| configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(
				Configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getDataServices().rpcMethodExecutor
							.shutdownClients(configedMain.getSelectedClients());
				}
			}.start();
		}
	}

	public static void rebootSelectedClients() {
		if (persistenceController.getDataServices().userRoles.isGlobalReadOnly()
				|| configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getDataServices().rpcMethodExecutor
							.rebootClients(configedMain.getSelectedClients());
				}
			}.start();
		}
	}

	public static void deleteSelectedClients() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question"))) {
			return;
		}

		isLocalChangeInProgress.set(true);

		try {
			persistenceController.getDataServices().host.deleteClients(configedMain.getSelectedClients());
			configedMain.deactivateFilter();
			configedMain.refreshClientListKeepingGroup();

		} finally {
			isLocalChangeInProgress.set(false);
		}
	}

	private static boolean confirmActionForSelectedClients(String confirmInfo) {
		String message = confirmInfo + "\n\n"
				+ Utils.getListStringRepresentation(configedMain.getSelectedClients()).replace(";", "");

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("ConfigedMain.TitleClientAction"), JOptionPane.YES_NO_OPTION);

		return answer == JOptionPane.YES_OPTION;
	}

	public static void copySelectedClient() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		Optional<HostInfo> selectedClient = persistenceController.getDataServices().hostInfoCollections
				.getMapOfPCInfoMaps().values().stream().filter(hostValues -> hostValues.getString(HostInfo.HOSTNAME_KEY)
						.equals(configedMain.getSelectedClients().get(0)))
				.findFirst();

		if (!selectedClient.isPresent()) {
			return;
		}

		Set<CopyOption> options = EnumSet.allOf(CopyOption.class);

		HostInfo clientToCopy = selectedClient.get();

		String newClientName = confirmCopyClient(clientToCopy, options);
		if (newClientName != null) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			boolean proceed = true;
			if (newClientName.isEmpty()) {
				proceed = false;
			}

			String newClientNameWithDomain = newClientName + "."
					+ Utils.getDomainFromClientName(clientToCopy.getString(HostInfo.HOSTNAME_KEY));
			if (persistenceController.getDataServices().hostInfoCollections.getOpsiHostNames()
					.contains(newClientNameWithDomain)) {
				boolean overwriteExistingHost = ask2OverwriteExistingHost(newClientNameWithDomain);
				if (!overwriteExistingHost) {
					proceed = false;
				}
			}

			Logging.info("copy client with new name ", newClientName);
			if (proceed) {
				persistenceController.getDataServices().hostInfoCollections.addOpsiHostName(newClientNameWithDomain);
				CopyClient copyClient = new CopyClient(clientToCopy, newClientName);
				copyClient.copy(options);

				String selectedGroup = configedMain.getSelectedGroupName();
				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(false, selectedGroup);
				configedMain.setClients(Set.of(newClientNameWithDomain));
			}
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}
	}

	/**
	 * We ask the user for confirmation and options to copy a client.
	 * 
	 * @param clientToCopy
	 * @param options
	 * @return the new client name or null if the user cancelled the action
	 */
	private static String confirmCopyClient(HostInfo clientToCopy, Set<CopyOption> options) {
		JTextField jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));
		CopySuffixAddition copySuffixAddition = new CopySuffixAddition(clientToCopy.getString(HostInfo.HOSTNAME_KEY));
		jTextHostname.setText(copySuffixAddition.add());
		JLabel label = new JLabel(Configed.getResourceValue("ConfigedMain.chooseOptionsToCopy"));
		JCheckBox copyGroups = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.groups.option"), options,
				CopyOption.GROUPS);
		JCheckBox copyProducts = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.products.option"),
				options, CopyOption.PRODUCTS);
		JCheckBox copyProductProperties = createOptionCheckBox(
				Configed.getResourceValue("ConfigedMain.productProperties.option"), options,
				CopyOption.PRODUCT_PROPERTIES);
		JCheckBox copyConfigs = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.configs.option"), options,
				CopyOption.CONFIG_STATES);

		JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "", "[]0"));

		panel.add(jTextHostname, "growx");
		panel.add(label, "gapy " + Globals.GAP_SIZE + "");
		panel.add(copyGroups);
		panel.add(copyProducts);
		panel.add(copyProductProperties);
		panel.add(copyConfigs);

		StringBuilder messageText = new StringBuilder();
		messageText.append(Configed.getResourceValue("ConfigedMain.confirmCopyClient"));
		messageText.append("\n");
		messageText.append(clientToCopy.getString(HostInfo.HOSTNAME_KEY));
		messageText.append("\n");
		messageText.append(Configed.getResourceValue("ConfigedMain.jLabelHostname"));

		Object[] message = new Object[] { messageText.toString(), panel };

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("MainFrame.jMenuCopyClient"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		return answer == JOptionPane.OK_OPTION ? jTextHostname.getText() : null;
	}

	private static JCheckBox createOptionCheckBox(String title, Set<CopyOption> options, CopyOption option) {
		JCheckBox optionCheckBox = new JCheckBox(title);
		optionCheckBox.setSelected(true);
		optionCheckBox.addActionListener(event -> updateOptions(options, option, optionCheckBox.isSelected()));
		return optionCheckBox;
	}

	private static void updateOptions(Set<CopyOption> options, CopyOption option, boolean include) {
		if (include && !options.contains(option)) {
			options.add(option);
		} else {
			options.remove(option);
		}
	}

	private static boolean ask2OverwriteExistingHost(String host) {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
		message.append(" \"");
		message.append(host);
		message.append("\" \n");
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message.toString(),
				Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		return answer == JOptionPane.YES_OPTION;
	}

	public static void resetProductsForSelectedClients(boolean withDependencies, boolean resetLocalbootProducts,
			boolean resetNetbootProducts) {
		String confirmInfoMessage = getConfirmInfoMessage(resetLocalbootProducts, resetNetbootProducts);
		if (configedMain.getSelectedClients().isEmpty() || confirmInfoMessage.isEmpty()
				|| !confirmActionForSelectedClients(confirmInfoMessage)) {
			return;
		}

		ConfigedMain.getMainFrame().activateLoadingCursor();

		if (resetLocalbootProducts) {
			persistenceController.getDataServices().product.resetProducts(configedMain.getSelectedClients(),
					withDependencies, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		}

		if (resetNetbootProducts) {
			persistenceController.getDataServices().product.resetProducts(configedMain.getSelectedClients(),
					withDependencies, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		}

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTY_STATES.toString());

		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().updateProductTab();

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	private static String getConfirmInfoMessage(boolean resetLocalbootProducts, boolean resetNetbootProducts) {
		String confirmInfo = "";
		if (resetLocalbootProducts && resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetProducts.question");
		} else if (resetLocalbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetLocalbootProducts.question");
		} else if (resetNetbootProducts) {
			confirmInfo = Configed.getResourceValue("ConfigedMain.confirmResetNetbootProducts.question");
		} else {
			Logging.warning("cannot reset products because they're neither localboot nor netboot");
		}
		return confirmInfo;
	}

	public static boolean freeAllPossibleLicensesForSelectedClients() {
		Logging.info("freeAllPossibleLicensesForSelectedClients, count ", configedMain.getSelectedClients().size());

		if (configedMain.getSelectedClients().isEmpty()) {
			return true;
		}

		if (!confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.confirmFreeLicenses.question"))) {
			return false;
		}

		for (String client : configedMain.getSelectedClients()) {
			Map<String, List<LicenseUsageEntry>> fClient2LicensesUsageList = persistenceController
					.getDataServices().license.getFClient2LicensesUsageListPD();

			for (LicenseUsageEntry m : fClient2LicensesUsageList.get(client)) {
				persistenceController.getDataServices().license.addDeletionLicenseUsage(client, m.getLicenseId(),
						m.getLicensePool());
			}
		}

		return persistenceController.getDataServices().license.executeCollectedDeletionsLicenseUsage();
	}

	public static void callChangeClientIDDialog() {
		if (configedMain.getSelectedClients().size() != 1) {
			return;
		}

		String newClientName = (String) JOptionPane.showInputDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuChangeClientID.message"),
				Configed.getResourceValue("MainFrame.jMenuChangeClientID"), JOptionPane.DEFAULT_OPTION, null, null,
				configedMain.getSelectedClients().get(0));

		if (newClientName != null) {
			Logging.debug("new name ", newClientName);

			persistenceController.getDataServices().host.renameClient(configedMain.getSelectedClients().get(0),
					newClientName);

			SwingUtilities.invokeLater(() -> {
				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(true, ClientTree.ALL_CLIENTS_NAME);
				Logging.debug("set client refreshClientList");
				configedMain.setClients(Set.of(newClientName));
			});
		}
	}

	public static void callChangeDepotDialog() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		JComboBox<String> depotCombo = new JComboBox<>(configedMain.getDepotArray());

		// We use the StringJoiner to separate these strings of clients with depots with a newline
		StringJoiner stringJoiner = new StringJoiner("\n");
		for (String selectedClient : configedMain.getSelectedClients()) {
			stringJoiner.add(selectedClient + "  (" + persistenceController.getDataServices().hostInfoCollections
					.getMapPcBelongsToDepot().get(selectedClient) + ")");
		}

		JTextArea selectedClientsArea = new JTextArea(stringJoiner.toString());
		selectedClientsArea.setEditable(false);

		// We set the number of rows to be the number of selected clients, but at most 4
		selectedClientsArea.setRows(Math.min(4, configedMain.getSelectedClients().size()));

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				new Object[] { Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving"),
						new JScrollPane(selectedClientsArea),
						"\n" + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), depotCombo },
				Configed.getResourceValue("MainFrame.jMenuChangeDepot"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (answer == JOptionPane.OK_OPTION) {
			Logging.debug(" start moving to another depot");
			persistenceController.getDataServices().hostInfoCollections
					.setDepotForClients(configedMain.getSelectedClients(), (String) depotCombo.getSelectedItem());
			Logging.checkErrorList();
			configedMain.refreshClientListKeepingGroup();
		}
	}

	public static boolean isLocalChangeInProgress() {
		return isLocalChangeInProgress.get();
	}
}
