/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.depot;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Transferable for drags originating from the ClientTable. Supports the plain
 * string flavor and additionally a local marker flavor, so that drop targets
 * (e.g. the DepotsList) can verify that the drag really comes from the
 * ClientTable.
 */
public class ClientSelectionTransferable implements Transferable {
	public static final DataFlavor CLIENT_LIST_FLAVOR = new DataFlavor(ClientSelectionTransferable.class,
			"OpsiClientSelection");

	private final String data;

	public ClientSelectionTransferable(String data) {
		this.data = data;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { CLIENT_LIST_FLAVOR, DataFlavor.stringFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return CLIENT_LIST_FLAVOR.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return CLIENT_LIST_FLAVOR.equals(flavor) ? this : data;
	}
}
