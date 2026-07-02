/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.depot;

import javax.swing.JList;
import javax.swing.TransferHandler;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.ServerActionManager;
import de.uib.configed.share.logging.Logging;

/**
 * TransferHandler for DepotsList to enable drag and drop of clients onto
 * depots. Uses DropMode.ON on the DepotsList to prevent selection changes
 * during drag.
 */
public class DepotListTransferHandler extends TransferHandler {
	private DepotsList depotsList;

	public DepotListTransferHandler(DepotsList depotsList) {
		super();
		this.depotsList = depotsList;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		Logging.debug(this, "DepotListTransferHandler canImport?");

		if (PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()) {
			return false;
		}

		if (!(support.getComponent() instanceof JList<?>)) {
			return false;
		}

		return support.isDrop();
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!(support.getComponent() instanceof JList<?>)) {
			return false;
		}

		JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
		if (dropLocation == null || dropLocation.getIndex() < 0) {
			Logging.debug(this, "Invalid drop location");
			return false;
		}

		String targetDepot = depotsList.getModel().getElementAt(dropLocation.getIndex());
		Logging.info(this, "Drop on depot: ", targetDepot);

		ServerActionManager.changeDepotForSelectedClientsWithDialog(targetDepot);
		return true;
	}
}
