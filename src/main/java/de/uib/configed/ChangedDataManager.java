/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Component;

import javax.swing.JOptionPane;

import de.uib.configed.type.HostInfo;
import de.uib.utils.logging.Logging;

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
	public static void checkSaveAll(boolean ask) {
		Logging.debug("checkSaveAll: anyDataChanged, ask  ", anyDataChanged, ", ", ask);

		if (anyDataChanged) {
			// without showing, but must be on first place since we run in this method again
			setDataChanged(false, false);

			if (ask) {
				if (clientInfoDataChangedKeeper.askSave()) {
					clientInfoDataChangedKeeper.save();
				} else {
					// reset to old values
					hostInfo.resetGui();
				}
			} else {
				clientInfoDataChangedKeeper.save();
			}

			if (!ask || generalDataChangedKeeper.askSave()) {
				generalDataChangedKeeper.save();
			}

			if (!ask || hostConfigsDataChangedKeeper.askSave()) {
				hostConfigsDataChangedKeeper.save();
			} else {
				hostConfigsDataChangedKeeper.cancel();
			}

			setDataChanged(false, true);
		}
	}
}
