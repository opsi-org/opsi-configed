/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.gui.features.clients.add.AddClientComponent;
import de.uib.configed.gui.features.clients.add.AddClientMsg;
import de.uib.configed.gui.features.groupaction.GroupActionsDialog;
import de.uib.configed.gui.features.productaction.CompleteWinProductsDialog;
import de.uib.configed.gui.features.serverconsole.EditTerminalCommandsDialog;
import de.uib.configed.gui.share.swing.list.ListCellRendererByIndex;
import de.uib.configed.gui.type.RemoteControl;
import de.uib.configed.share.logging.Logging;

/**
 * This class should contain control all these control frames that we have in
 * the configed that we can open and use. This class should therefore never be
 * instantiated because we want to access the methods directly to get access to
 * the extra features and frames
 */
public final class ExtraFrameController {
	private static SavedSearchesDialog savedSearchesDialog;
	private static ClientSelectionDialog clientSelectionDialog;
	private static AddClientComponent addClientComponent;
	private static EditTerminalCommandsDialog commandControlDialog;
	private static CompleteWinProductsDialog completeWinProductsPanel;
	private static GroupActionsDialog groupActionFrame;
	private static RemoteControlDialog remoteControlDialog;
	private static Map<String, RemoteControl> remoteControls;

	// We have a private empty constructor to prevent instantiation
	private ExtraFrameController() {
	}

	public static void callNewClientSelectionDialog(ConfigedMain configedMain) {
		if (clientSelectionDialog != null) {
			clientSelectionDialog = null;
		}
		callClientSelectionDialog(configedMain);
	}

	public static void callClientSelectionDialog(ConfigedMain configedMain) {
		callClientSelectionDialog(configedMain, null);
	}

	public static void callClientSelectionDialog(ConfigedMain configedMain, String searchName) {
		initSavedSearchesDialog(configedMain);

		if (clientSelectionDialog == null) {
			clientSelectionDialog = new ClientSelectionDialog(configedMain, savedSearchesDialog);
		}

		clientSelectionDialog.show(searchName);
	}

	public static void editClientSearch(ConfigedMain configedMain, String name) {
		callClientSelectionDialog(configedMain, name);
	}

	private static void initSavedSearchesDialog(ConfigedMain configedMain) {
		if (savedSearchesDialog == null) {
			Logging.debug("create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog(configedMain);
		}
	}

	public static void clientSelectionGetSavedSearch(ConfigedMain configedMain) {
		Logging.debug("clientSelectionGetSavedSearch");
		initSavedSearchesDialog(configedMain);

		savedSearchesDialog.show();
	}

	public static void reloadDialogs() {
		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}
	}

	public static void callAddClientDialog() {
		if (addClientComponent == null) {
			addClientComponent = new AddClientComponent();
			addClientComponent.initUI();
		}

		addClientComponent.dispatch(new AddClientMsg.ActionMsg.LoadInitialDataRequested());
		addClientComponent.show();
	}

	public static void startEditTerminalCommandsDialog(ConfigedMain configedMain) {
		Logging.debug("start editing terminal commmands dialog");

		if (commandControlDialog == null) {
			commandControlDialog = new EditTerminalCommandsDialog(configedMain);
		}

		commandControlDialog.show();
	}

	public static void startProductActionFrame() {
		Logging.info("startProductActionFrame ");
		ConfigedMain.getMainFrame().activateLoadingCursor();

		if (completeWinProductsPanel == null) {
			completeWinProductsPanel = new CompleteWinProductsDialog();
		}

		completeWinProductsPanel.show();

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	public static void startGroupActionFrame(ConfigedMain configedMain) {
		if (groupActionFrame == null) {
			groupActionFrame = new GroupActionsDialog(configedMain);
		}

		groupActionFrame.show();
	}

	public static void startRemoteControlFrame(ConfigedMain configedMain,
			OpsiServiceNOMPersistenceController persistenceController) {
		if (remoteControlDialog == null) {
			remoteControlDialog = new RemoteControlDialog(configedMain);
		}

		if (remoteControls == null
				|| !remoteControls.equals(persistenceController.getConfigDataService().getRemoteControlsPD())) {
			remoteControls = persistenceController.getConfigDataService().getRemoteControlsPD();

			Logging.debug("remoteControls ", remoteControls);

			Map<String, String> tooltips = new HashMap<>();
			Map<String, String> rcCommands = new HashMap<>();
			Map<String, Boolean> commandsEditable = new HashMap<>();

			for (Entry<String, RemoteControl> entry : remoteControls.entrySet()) {
				RemoteControl rc = entry.getValue();
				if (rc.getDescription() != null && rc.getDescription().length() > 0) {
					tooltips.put(entry.getKey(), rc.getDescription());
				} else {
					tooltips.put(entry.getKey(), rc.getCommand());
				}
				rcCommands.put(entry.getKey(), rc.getCommand());
				Boolean editable = Boolean.valueOf(rc.getEditable());

				commandsEditable.put(entry.getKey(), editable);
			}

			remoteControlDialog.setMeanings(rcCommands);
			remoteControlDialog.setEditableFields(commandsEditable);

			// we want to present a sorted list of the keys
			List<String> sortedKeys = new ArrayList<>(remoteControls.keySet());
			sortedKeys.sort(Comparator.comparing(String::toString));
			remoteControlDialog.setListModel(new DefaultComboBoxModel<>(sortedKeys.toArray(new String[0])));

			remoteControlDialog.setCellRenderer(new ListCellRendererByIndex(tooltips));
		}

		remoteControlDialog.resetValue();

		remoteControlDialog.show();
	}

	public static void resetCompleteWinProductsPanel() {
		completeWinProductsPanel = null;
	}

	public static void deleteInstances() {
		savedSearchesDialog = null;
		clientSelectionDialog = null;
		commandControlDialog = null;
		addClientComponent = null;
		completeWinProductsPanel = null;
		groupActionFrame = null;
		remoteControlDialog = null;
	}
}
