/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import de.uib.configed.share.DataChangedKeeper;
import de.uib.configed.share.logging.Logging;

public class GeneralDataChangedKeeper extends DataChangedKeeper {
	@Override
	public void dataHaveChanged(Object source) {
		super.dataHaveChanged(source);
		Logging.info(this, "dataHaveChanged from ", source);

		// anyDataChanged in ConfigedMain
		ChangedDataManager.setDataChanged(super.isDataChanged());
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
