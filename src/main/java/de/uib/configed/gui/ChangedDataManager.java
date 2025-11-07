/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.logging.Logging;

public final class ChangedDataManager {
	private static GeneralDataChangedKeeper generalDataChangedKeeper;
	private static ClientInfoDataChangedKeeper clientInfoDataChangedKeeper;
	private static GeneralDataChangedKeeper hostConfigsDataChangedKeeper;

	private static boolean anyDataChanged;

	private static HostInfo hostInfo;

	// This is the save button that is shown in the top toolbar
	private static Component shownSaveButton;

	// Private constructor to prevent instantiation of this class
	private ChangedDataManager() {
	}

	public static void initData(ConfigedMain configedMain, HostInfo hostInfo) {
		ChangedDataManager.hostInfo = hostInfo;

		generalDataChangedKeeper = new GeneralDataChangedKeeper();
		clientInfoDataChangedKeeper = new ClientInfoDataChangedKeeper(configedMain, hostInfo);
		hostConfigsDataChangedKeeper = new GeneralDataChangedKeeper();
	}

	public static GeneralDataChangedKeeper getGeneralDataChangedKeeper() {
		return generalDataChangedKeeper;
	}

	/* ============================================ */

	public static GeneralDataChangedKeeper getHostConfigsDataChangedKeeper() {
		return hostConfigsDataChangedKeeper;
	}

	/* ============================================ */

	public static ClientInfoDataChangedKeeper getClientInfoDataChangedKeeper() {
		return clientInfoDataChangedKeeper;
	}

	public static void setDataChanged(boolean b) {
		setDataChanged(b, true);
	}

	private static void setDataChanged(boolean anyDataChanged, boolean show) {
		Logging.info("setDataChanged ", anyDataChanged, ", showing ", show);
		ChangedDataManager.anyDataChanged = anyDataChanged;

		if (show) {
			ConfigedMain.getMainFrame().saveConfigurationsSetEnabled(anyDataChanged);
			shownSaveButton.setEnabled(anyDataChanged);
		}
	}

	public static void setShownSaveButton(Component shownSaveButton) {
		shownSaveButton.setEnabled(anyDataChanged);
		ChangedDataManager.shownSaveButton = shownSaveButton;
	}

	public static void cancelChanges() {
		Logging.info("cancelChanges ");
		setDataChanged(false);
		generalDataChangedKeeper.cancel();
	}

	public static int checkClose() {
		int result = 0;

		if (anyDataChanged) {
			result = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
					Configed.getResourceValue("buttonClose"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}

		Logging.debug("checkClose result ", result);
		return result;
	}

	// save if not otherwise stated
	public static boolean checkSaveAll(boolean ask) {
		boolean result = true;
		Logging.debug("checkSaveAll: anyDataChanged, ask  ", anyDataChanged, ", ", ask);

		if (anyDataChanged
				&& PersistenceControllerFactory.getPersistenceController().getExecutioner().testConnection(true)) {
			result = saveData(ask);

			setDataChanged(!result);
		}

		return result;
	}

	private static boolean saveData(boolean ask) {
		boolean result = true;
		if (ask) {
			int option = clientInfoDataChangedKeeper.askSave();
			if (option == JOptionPane.YES_OPTION) {
				clientInfoDataChangedKeeper.save();
			} else if (option == JOptionPane.NO_OPTION) {
				// reset to old values when data have been changed 
				hostInfo.resetGui();
			} else {
				// if no data have been changed, and no client selected, we do nothing
				Logging.debug("clientInfoDataChangedKeeper not changed, no save needed");
				result = false;
			}

			option = generalDataChangedKeeper.askSave();
			if (option == JOptionPane.YES_OPTION) {
				generalDataChangedKeeper.save();
			} else if (option == JOptionPane.NO_OPTION) {
				// Here we don't change anything, just discard changes
				generalDataChangedKeeper.cancel();
			} else {
				result = false;
			}

			option = hostConfigsDataChangedKeeper.askSave();
			if (option == JOptionPane.YES_OPTION) {
				hostConfigsDataChangedKeeper.save();
			} else if (option == JOptionPane.NO_OPTION) {
				// Here we don't change anything, just discard changes
				hostConfigsDataChangedKeeper.cancel();
			} else {
				result = false;
			}
		} else {
			clientInfoDataChangedKeeper.save();
			generalDataChangedKeeper.save();
			hostConfigsDataChangedKeeper.save();
		}

		return result;
	}
}
