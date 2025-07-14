/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;

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

	public static void createClients(List<List<Object>> clients) {
		List<String> createdClientNames = clients.stream().map(v -> (String) v.get(0) + "." + v.get(1)).toList();
		isLocalChangeInProgress.set(true);

		try {
			persistenceController.getHostInfoCollections().addOpsiHostNames(createdClientNames);
			if (persistenceController.getHostDataService().createClients(clients)) {
				Logging.debug("createClients", clients);
				Logging.checkErrorList();

				persistenceController.reloadData(CacheIdentifier.FHOST_TO_GROUPS.toString());

				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
				configedMain.setClients(createdClientNames);
			} else {
				persistenceController.getHostInfoCollections().removeOpsiHostNames(createdClientNames);
			}
		} finally {
			isLocalChangeInProgress.set(false);
		}
	}

	public static void createClient(String newClientID, final String[] groups) {
		Logging.checkErrorList();
		isLocalChangeInProgress.set(true);
		try {
			persistenceController.reloadData(CacheIdentifier.FHOST_TO_GROUPS.toString());

			configedMain.setRebuiltClientListTableModel(true, true);

			if (groups.length == 0 || groups.length > 1 || !configedMain.activateGroup(false, groups[0])) {
				configedMain.activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			}

			// Sets the client on the table
			configedMain.setClient(newClientID);
		} finally {
			isLocalChangeInProgress.set(false);
		}
	}

	public static void wakeSelectedClients() {
		Logging.info("wakeUp ", configedMain.getSelectedClients().size());
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoWakeClients")) {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor().wakeOnLanOpsi43(configedMain.getSelectedClients());
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
				return persistenceController.getRPCMethodExecutor()
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
				return persistenceController.getRPCMethodExecutor().fireOpsiclientdEventOnClients(event,
						configedMain.getSelectedClients());
			}
		}.start();
	}

	public static void processActionRequestsAllProducts(String visibility) {
		processActionRequests(Collections.emptySet(), visibility);
	}

	public static void processActionRequestsSelectedProducts(String visibility) {
		processActionRequests(ConfigedMain.getMainFrame().getClientConfiguration().getPanelLocalbootProductSettings()
				.getProductTable().getSelectedIDs(), visibility);
	}

	private static void processActionRequests(Set<String> products, String visibility) {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		ChangedDataManager.checkSaveAll(false);

		new AbstractErrorListProducer("opsiclientd processActionRequests") {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor()
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
				return persistenceController.getRPCMethodExecutor().showPopupOnClients(message,
						configedMain.getSelectedClients(), seconds);
			}
		}.start();
	}

	public static void shutdownSelectedClients() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(
				Configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoShutdownClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor()
							.shutdownClients(configedMain.getSelectedClients());
				}
			}.start();
		}
	}

	public static void rebootSelectedClients() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		if (confirmActionForSelectedClients(Configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question"))) {
			new AbstractErrorListProducer(Configed.getResourceValue("ConfigedMain.infoRebootClients")) {
				@Override
				protected List<String> getErrors() {
					return persistenceController.getRPCMethodExecutor()
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
			persistenceController.getHostDataService().deleteClients(configedMain.getSelectedClients());
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
				Configed.getResourceValue("ConfigedMain.TitleClientAction"), 0);

		return answer == 0;
	}

	public static void copySelectedClient() {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		Optional<HostInfo> selectedClient = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().values()
				.stream().filter(hostValues -> hostValues.getName().equals(configedMain.getSelectedClients().get(0)))
				.findFirst();

		if (!selectedClient.isPresent()) {
			return;
		}

		JTextField jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));
		CopySuffixAddition copySuffixAddition = new CopySuffixAddition(selectedClient.get().getName());
		jTextHostname.setText(copySuffixAddition.add());

		EnumSet<CopyClient.CopyOption> options = EnumSet.allOf(CopyClient.CopyOption.class);

		JLabel label = new JLabel(Configed.getResourceValue("ConfigedMain.chooseOptionsToCopy"));
		JCheckBox copyGroups = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.groups.option"), options,
				CopyClient.CopyOption.GROUPS);
		JCheckBox copyProducts = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.products.option"),
				options, CopyClient.CopyOption.PRODUCTS);
		JCheckBox copyProductProperties = createOptionCheckBox(
				Configed.getResourceValue("ConfigedMain.productProperties.option"), options,
				CopyClient.CopyOption.PRODUCT_PROPERTIES);
		JCheckBox copyConfigs = createOptionCheckBox(Configed.getResourceValue("ConfigedMain.configs.option"), options,
				CopyClient.CopyOption.CONFIG_STATES);

		JPanel panel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(panel);
		panel.setLayout(groupLayout);

		StringBuilder messageText = new StringBuilder();
		messageText.append(Configed.getResourceValue("ConfigedMain.confirmCopyClient"));
		messageText.append("\n");
		messageText.append(selectedClient.get().getName());
		messageText.append("\n");
		messageText.append(Configed.getResourceValue("ConfigedMain.jLabelHostname"));

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(jTextHostname)
				.addGap(Globals.GAP_SIZE).addComponent(label).addComponent(copyGroups).addComponent(copyProducts)
				.addComponent(copyProductProperties).addComponent(copyConfigs));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(jTextHostname)
				.addGap(Globals.GAP_SIZE).addComponent(label).addComponent(copyGroups).addComponent(copyProducts)
				.addComponent(copyProductProperties).addComponent(copyConfigs));

		Object[] message = new Object[] { messageText.toString(), panel };

		int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), message,
				Configed.getResourceValue("MainFrame.jMenuCopyClient"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

		if (answer == 0) {
			ConfigedMain.getMainFrame().activateLoadingCursor();
			String newClientName = jTextHostname.getText();
			boolean proceed = true;
			if (newClientName.isEmpty()) {
				proceed = false;
			}

			HostInfo clientToCopy = selectedClient.get();
			String newClientNameWithDomain = newClientName + "."
					+ Utils.getDomainFromClientName(clientToCopy.getName());
			if (persistenceController.getHostInfoCollections().getOpsiHostNames().contains(newClientNameWithDomain)) {
				boolean overwriteExistingHost = ask2OverwriteExistingHost(newClientNameWithDomain);
				if (!overwriteExistingHost) {
					proceed = false;
				}
			}

			Logging.info("copy client with new name ", newClientName);
			if (proceed) {
				persistenceController.getHostInfoCollections().addOpsiHostName(newClientNameWithDomain);
				CopyClient copyClient = new CopyClient(clientToCopy, newClientName);
				copyClient.copy(options);

				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(false, configedMain.getSelectedGroupName());
				configedMain.setClient(newClientNameWithDomain);
			}
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
		}
	}

	private static JCheckBox createOptionCheckBox(String title, EnumSet<CopyClient.CopyOption> options,
			CopyClient.CopyOption option) {
		JCheckBox optionCheckBox = new JCheckBox(title);
		optionCheckBox.setSelected(true);
		optionCheckBox.addActionListener(event -> updateOptions(options, option, optionCheckBox.isSelected()));
		return optionCheckBox;
	}

	private static void updateOptions(Set<CopyClient.CopyOption> options, CopyClient.CopyOption option,
			boolean include) {
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

		return answer == 0;
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
			persistenceController.getProductDataService().resetProducts(configedMain.getSelectedClients(),
					withDependencies, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
		}

		if (resetNetbootProducts) {
			persistenceController.getProductDataService().resetProducts(configedMain.getSelectedClients(),
					withDependencies, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
		}

		persistenceController.reloadData(CacheIdentifier.PRODUCT_PROPERTIES.toString());

		ConfigedMain.getMainFrame().getClientConfiguration().updateProductTab();

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
					.getLicenseDataService().getFClient2LicensesUsageListPD();

			for (LicenseUsageEntry m : fClient2LicensesUsageList.get(client)) {
				persistenceController.getLicenseDataService().addDeletionLicenseUsage(client, m.getLicenseId(),
						m.getLicensePool());
			}
		}

		return persistenceController.getLicenseDataService().executeCollectedDeletionsLicenseUsage();
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

			persistenceController.getHostDataService().renameClient(configedMain.getSelectedClients().get(0),
					newClientName);

			SwingUtilities.invokeLater(() -> {
				configedMain.refreshClientListActivateALL();
				Logging.debug("set client refreshClientList");
				configedMain.setClient(newClientName);
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
			stringJoiner.add(selectedClient + "  ("
					+ persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient)
					+ ")");
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
			persistenceController.getHostInfoCollections().setDepotForClients(configedMain.getSelectedClients(),
					(String) depotCombo.getSelectedItem());
			Logging.checkErrorList();
			configedMain.refreshClientListKeepingGroup();
		}
	}

	public static boolean isLocalChangeInProgress() {
		return isLocalChangeInProgress.get();
	}
}
