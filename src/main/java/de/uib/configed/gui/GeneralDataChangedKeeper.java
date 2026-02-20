/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import de.uib.configed.share.AbstractDataChangedKeeper;
import de.uib.configed.share.logging.Logging;

public class GeneralDataChangedKeeper extends AbstractDataChangedKeeper {
	@Override
	public void dataHaveChanged(Object source) {
		super.dataHaveChanged(source);
		Logging.info(this, "dataHaveChanged from ", source);

		// anyDataChanged in ConfigedMain
		ChangedDataManager.setDataChanged(super.isDataChanged());
	}

	private void saveConfigs() {
		Logging.info(this, "saveConfigs ");

		ConfigedMain.getMainFrame().getMainPanelManager().getClientConfiguration().getProductPageManager()
				.updateProductStates();

		Logging.info(this, "we should now start working on the global update collection of size  ",
				UpdateCollectionManager.getSizeOfGlobalUpdateCollection());

		UpdateCollectionManager.doCall();
		Logging.checkErrorList();

		UpdateCollectionManager.clearGlobalUpdateCollection();
	}

	@Override
	public void save() {
		if (this.dataChanged) {
			saveConfigs();
		}

		this.dataChanged = false;
	}

	@Override
	public void cancel() {
		super.cancel();

		UpdateCollectionManager.cancelGlobalUpdateCollection();
	}
}
