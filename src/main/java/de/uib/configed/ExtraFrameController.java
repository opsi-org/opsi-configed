/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;

import de.uib.configed.groupaction.FGroupActions;
import de.uib.configed.gui.ClientSelectionDialog;
import de.uib.configed.gui.FDialogRemoteControl;
import de.uib.configed.gui.NewClientDialog;
import de.uib.configed.gui.SavedSearchesDialog;
import de.uib.configed.productaction.FCompleteWinProducts;
import de.uib.configed.serverconsole.EditTerminalCommandsDialog;
import de.uib.configed.type.RemoteControl;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.ListCellRendererByIndex;

/**
 * This class should contain control all these control frames that we have in
 * the configed that we can open and use. This class should therefore never be
 * instantiated because we want to access the methods directly to get access to
 * the extra features and frames
 */
public final class ExtraFrameController {
	private static SavedSearchesDialog savedSearchesDialog;
	private static ClientSelectionDialog clientSelectionDialog;
	private static NewClientDialog newClientDialog;
	private static EditTerminalCommandsDialog commandControlDialog;
	private static FCompleteWinProducts productActionFrame;
	private static FGroupActions groupActionFrame;
	private static FDialogRemoteControl dialogRemoteControl;
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
		initSavedSearchesDialog(configedMain);

		if (clientSelectionDialog == null) {
			clientSelectionDialog = new ClientSelectionDialog(configedMain, configedMain.getClientTablePanel(),
					savedSearchesDialog);
		}

		clientSelectionDialog.show();
	}

	public static void editClientSearch(ConfigedMain configedMain, String name) {
		callClientSelectionDialog(configedMain);
		clientSelectionDialog.loadSearch(name);
	}

	private static void initSavedSearchesDialog(ConfigedMain configedMain) {
		if (savedSearchesDialog == null) {
			Logging.debug("create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog(configedMain.getClientTablePanel(), configedMain);
			savedSearchesDialog.setPreferredScrollPaneSize(new Dimension(300, 400));
			savedSearchesDialog.init();
		}
	}

	public static void clientSelectionGetSavedSearch(ConfigedMain configedMain) {
		Logging.debug("clientSelectionGetSavedSearch");
		initSavedSearchesDialog(configedMain);

		savedSearchesDialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		savedSearchesDialog.setVisible(true);
	}

	public static void reloadDialogs() {
		if (savedSearchesDialog != null) {
			savedSearchesDialog.resetModel();
		}
	}

	public static void callNewClientDialog() {
		if (newClientDialog == null) {
			newClientDialog = new NewClientDialog();
		}

		newClientDialog.setDefaultValues();
		newClientDialog.show();
	}

	public static void startEditTerminalCommandsDialog(ConfigedMain configedMain) {
		Logging.debug("start editing terminal commmands dialog");

		if (commandControlDialog == null) {
			commandControlDialog = new EditTerminalCommandsDialog(configedMain);
		}
		commandControlDialog.setVisible(true);
	}

	public static void startProductActionFrame() {
		Logging.info("startProductActionFrame ");

		if (productActionFrame == null) {
			productActionFrame = new FCompleteWinProducts();
			productActionFrame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		}

		productActionFrame.start();
	}

	public static void startGroupActionFrame(ConfigedMain configedMain) {
		if (groupActionFrame == null) {
			groupActionFrame = new FGroupActions(configedMain);
			groupActionFrame.setSize(1000, 300);
		}

		groupActionFrame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		groupActionFrame.start();
	}

	public static void startRemoteControlFrame(ConfigedMain configedMain,
			OpsiServiceNOMPersistenceController persistenceController) {
		if (dialogRemoteControl == null) {
			dialogRemoteControl = new FDialogRemoteControl(configedMain);
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

			dialogRemoteControl.setMeanings(rcCommands);
			dialogRemoteControl.setEditableFields(commandsEditable);

			// we want to present a sorted list of the keys
			List<String> sortedKeys = new ArrayList<>(remoteControls.keySet());
			sortedKeys.sort(Comparator.comparing(String::toString));
			dialogRemoteControl.setListModel(new DefaultComboBoxModel<>(sortedKeys.toArray(new String[0])));

			dialogRemoteControl.setCellRenderer(new ListCellRendererByIndex(tooltips));

			dialogRemoteControl.setTitle(Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
			dialogRemoteControl.setModal(false);
			dialogRemoteControl.init();
		}

		dialogRemoteControl.resetValue();

		dialogRemoteControl.setSize(800, ConfigedMain.getMainFrame().getHeight() / 2);
		dialogRemoteControl.setLocationRelativeTo(ConfigedMain.getMainFrame());

		dialogRemoteControl.setVisible(true);
		dialogRemoteControl.setDividerLocation(0.8);
	}
}
