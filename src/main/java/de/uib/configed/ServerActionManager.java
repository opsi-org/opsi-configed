/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.FShowListWithComboSelect;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;

public final class ServerActionManager {
	private static ConfigedMain configedMain;

	private static OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// We want a private empty constructor to prevent instantiation of this class
	private ServerActionManager() {
	}

	// We need to init the data since in the beginning they're not there yet
	public static void initData(ConfigedMain configedMain) {
		ServerActionManager.configedMain = configedMain;
	}

	public static void createClients(List<List<Object>> clients) {
		List<String> createdClientNames = clients.stream().map(v -> (String) v.get(0) + "." + v.get(1)).toList();
		persistenceController.getHostInfoCollections().addOpsiHostNames(createdClientNames);
		if (persistenceController.getHostDataService().createClients(clients)) {
			Logging.debug("createClients", clients);
			Logging.checkErrorList();

			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

			configedMain.setRebuiltClientListTableModel(true, true);
			configedMain.activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
			configedMain.setClients(createdClientNames);
		} else {
			persistenceController.getHostInfoCollections().removeOpsiHostNames(createdClientNames);
		}
	}

	public static void createClient(String newClientID, final String[] groups) {
		Logging.checkErrorList();
		persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());

		configedMain.setRebuiltClientListTableModel(true, true);

		if (groups.length == 0 || groups.length > 1 || !configedMain.activateGroup(false, groups[0])) {
			configedMain.activateGroup(false, ClientTree.ALL_CLIENTS_NAME);
		}

		// Sets the client on the table
		configedMain.setClient(newClientID);
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

	public static void processActionRequestsAllProducts() {
		processActionRequests(Collections.emptySet());
	}

	public static void processActionRequestsSelectedProducts() {
		processActionRequests(ConfigedMain.getMainFrame().getClientConfiguration().getPanelLocalbootProductSettings()
				.getProductTable().getSelectedIDs());
	}

	private static void processActionRequests(Set<String> products) {
		if (configedMain.getSelectedClients().isEmpty()) {
			return;
		}

		ChangedDataManager.checkSaveAll(false);

		new AbstractErrorListProducer("opsiclientd processActionRequests") {
			@Override
			protected List<String> getErrors() {
				return persistenceController.getRPCMethodExecutor()
						.processActionRequests(configedMain.getSelectedClients(), products);
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

		persistenceController.getHostDataService().deleteClients(configedMain.getSelectedClients());

		configedMain.deactivateFilter();

		configedMain.refreshClientListKeepingGroup();
	}

	private static boolean confirmActionForSelectedClients(String confirmInfo) {
		FShowList fConfirmActionForClients = new FShowList(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConfigedMain.TitleClientAction"), true,
				new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 350,
				400);

		fConfirmActionForClients.setMessage(confirmInfo + "\n\n"
				+ Utils.getListStringRepresentation(configedMain.getSelectedClients()).replace(";", ""));

		fConfirmActionForClients.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fConfirmActionForClients.setAlwaysOnTop(true);
		fConfirmActionForClients.setVisible(true);

		return fConfirmActionForClients.getResult() == 2;
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

		StringBuilder messageText = new StringBuilder();
		messageText.append(Configed.getResourceValue("ConfigedMain.confirmCopyClient"));
		messageText.append("\n");
		messageText.append(selectedClient.get().getName());
		messageText.append("\n");
		messageText.append("\n");
		messageText.append(Configed.getResourceValue("ConfigedMain.jLabelHostname"));

		Object[] message = new Object[] { messageText.toString(), jTextHostname };

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
				copyClient.copy();

				configedMain.setRebuiltClientListTableModel(true, true);
				configedMain.activateGroup(false, configedMain.getActivatedGroupModel().getGroupName());
				configedMain.setClient(newClientNameWithDomain);
			}
			ConfigedMain.getMainFrame().deactivateLoadingCursor();
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

		FShowListWithComboSelect fChangeDepotForClients = new FShowListWithComboSelect(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.title"), true,
				Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.newDepot"), configedMain.getDepotArray(),
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") });

		fChangeDepotForClients.setLineWrap(false);

		StringBuilder messageBuffer = new StringBuilder(
				"\n" + Configed.getResourceValue("ConfigedMain.fChangeDepotForClients.Moving") + ": \n\n");

		for (String selectedClient : configedMain.getSelectedClients()) {
			messageBuffer.append(selectedClient);
			messageBuffer.append("     (from: ");
			messageBuffer.append(
					persistenceController.getHostInfoCollections().getMapPcBelongsToDepot().get(selectedClient));

			messageBuffer.append(") ");

			messageBuffer.append("\n");
		}

		fChangeDepotForClients.setSize(new Dimension(400, 250));
		fChangeDepotForClients.setMessage(messageBuffer.toString());

		fChangeDepotForClients.setVisible(true);

		if (fChangeDepotForClients.getResult() != 2) {
			return;
		}

		final String targetDepot = (String) fChangeDepotForClients.getChoice();

		if (targetDepot == null || targetDepot.isEmpty()) {
			return;
		}

		Logging.debug(" start moving to another depot");
		persistenceController.getHostInfoCollections().setDepotForClients(configedMain.getSelectedClients(),
				targetDepot);
		Logging.checkErrorList();
		configedMain.refreshClientListKeepingGroup();
	}
}
