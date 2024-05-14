/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.awt.Component;

import javax.swing.JTable;

import de.uib.configed.Globals;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utils.logging.Logging;

public class ProductVersionCellRenderer extends ColoredTableCellRenderer {
	public ProductVersionCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// Safe since instanceof returns false if null
		if (value instanceof String stringValue) {
			if (stringValue.isEmpty()) {
				return this;
			}

			if (stringValue.equals(Globals.CONFLICT_STATE_STRING) || stringValue
					.equals(InstallationStateTableModel.UNEQUAL_ADD_STRING + Globals.CONFLICT_STATE_STRING)) {
				setForeground(Globals.PRODUCT_STATUS_MIXED_COLOR);
			} else {
				String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
				InstallationStateTableModel istm = (InstallationStateTableModel) table.getModel();

				String serverProductVersion = "";

				if (istm.getGlobalProductInfos().get(productId) == null) {
					Logging.warning(this,
							" istm.getGlobalProductInfos()).get(productId) == null for productId " + productId);
				} else {
					serverProductVersion = serverProductVersion
							+ istm.getGlobalProductInfos().get(productId).get(ProductState.KEY_VERSION_INFO);
				}

				if (!stringValue.equals(serverProductVersion)) {
					setForeground(Globals.FAILED_COLOR);
				}
			}
		}

		return this;
	}
}
