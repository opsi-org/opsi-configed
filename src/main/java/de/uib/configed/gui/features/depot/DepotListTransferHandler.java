/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.depot;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.DepotsList;
import de.uib.configed.gui.ServerActionManager;
import de.uib.configed.share.logging.Logging;

/**
 * TransferHandler for DepotsList to enable drag and drop of clients onto
 * depots. Uses DropMode.ON on the DepotsList to prevent selection changes
 * during drag. Accepts only drags originating from the ClientTable (marked by
 * the ClientSelectionTransferable flavor).
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

		// Only accept drags originating from the ClientTable
		if (!isValidClientDrop(support)) {
			return false;
		}

		String targetDepot = getTargetDepot(support);
		return targetDepot != null
				&& PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.hasDepotPermission(targetDepot);
	}

	private static boolean isValidClientDrop(TransferSupport support) {
		return !PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles.isGlobalReadOnly()
				&& support.getComponent() instanceof JList<?> && support.isDrop()
				&& support.isDataFlavorSupported(ClientSelectionTransferable.CLIENT_LIST_FLAVOR);
	}

	private String getTargetDepot(TransferSupport support) {
		JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
		if (dropLocation == null || dropLocation.getIndex() < 0) {
			return null;
		}
		return depotsList.getModel().getElementAt(dropLocation.getIndex());
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		String targetDepot = getTargetDepot(support);
		Logging.info(this, "Drop on depot: ", targetDepot);

		// Show the dialog after the drop operation has finished, so the DnD
		// operation is not blocked by the modal dialog
		SwingUtilities.invokeLater(() -> ServerActionManager.changeDepotForSelectedClientsWithDialog(targetDepot));
		return true;
	}
}
