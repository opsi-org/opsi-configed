/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.gui.Configed;

public class ProductSettingsTableRowSorter extends TableRowSorter<TableModel> {
	private final JTable tableProducts;

	public ProductSettingsTableRowSorter(TableModel model, JTable tableProducts) {
		super(model);
		this.tableProducts = tableProducts;
	}

	@Override
	protected boolean useToString(int column) {
		return true;
	}

	@Override
	public Comparator<?> getComparator(int column) {
		Comparator<?> comparator = null;
		String columnName = tableProducts.getColumnName(column);
		if (columnName.equals(Configed.getResourceValue("InstallationStateTableModel.productId"))) {
			comparator = Comparator.comparing(String::toString);
		} else if (columnName.equals(Configed.getResourceValue("InstallationStateTableModel.installationStatus"))) {
			List<String> order = List.of(InstallationStatus.KEY_INSTALLED, InstallationStatus.KEY_UNKNOWN,
					InstallationStatus.KEY_NOT_INSTALLED);
			comparator = (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
		} else if (columnName.equals(Configed.getResourceValue("InstallationStateTableModel.actionRequest"))) {
			List<String> order = List.of(ActionRequest.KEY_SETUP, ActionRequest.KEY_UPDATE, ActionRequest.KEY_UNINSTALL,
					ActionRequest.KEY_ALWAYS, ActionRequest.KEY_ONCE, ActionRequest.KEY_CUSTOM, ActionRequest.KEY_NONE);
			comparator = (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
		} else {
			comparator = super.getComparator(column);
		}

		return comparator;
	}
}
