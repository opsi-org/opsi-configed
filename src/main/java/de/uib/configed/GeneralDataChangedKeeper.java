/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.Dimension;

import de.uib.configed.gui.FTextArea;
import de.uib.utils.DataChangedKeeper;
import de.uib.utils.logging.Logging;

public class GeneralDataChangedKeeper extends DataChangedKeeper {
	private FTextArea fAskSaveProductConfiguration;

	@Override
	public void dataHaveChanged(Object source) {
		super.dataHaveChanged(source);
		Logging.info(this, "dataHaveChanged from ", source);

		// anyDataChanged in ConfigedMain
		ChangedDataManager.setDataChanged(super.isDataChanged());
	}

	public boolean askSave() {
		boolean result = false;
		if (this.dataChanged) {
			if (fAskSaveProductConfiguration == null) {
				fAskSaveProductConfiguration = new FTextArea(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("PanelGenEditTable.saveData"), true,
						new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });
				fAskSaveProductConfiguration.setMessage(Configed.getResourceValue("ConfigedMain.reminderSaveConfig"));

				fAskSaveProductConfiguration.setSize(new Dimension(300, 220));
			}

			fAskSaveProductConfiguration.setLocationRelativeTo(ConfigedMain.getMainFrame());
			fAskSaveProductConfiguration.setVisible(true);

			result = fAskSaveProductConfiguration.getResult() == 2;

			fAskSaveProductConfiguration.setVisible(false);
		}

		return result;
	}

	private void saveConfigs() {
		Logging.info(this, "saveConfigs ");

		ConfigedMain.getMainFrame().getClientConfiguration().getProductPageManager().updateProductStates();

		Logging.info(this, "we should now start working on the global update collection of size  ",
				UpdateCollectionManager.getSizeOfGlobalUpdateCollection());

		UpdateCollectionManager.doCall();
		Logging.checkErrorList();

		UpdateCollectionManager.clearGlobalUpdateCollection();
	}

	public void save() {
		if (this.dataChanged) {
			saveConfigs();
		}

		this.dataChanged = false;
	}

	public void cancel() {
		Logging.info(this, "cancel");
		this.dataChanged = false;

		UpdateCollectionManager.cancelGlobalUpdateCollection();
	}
}
