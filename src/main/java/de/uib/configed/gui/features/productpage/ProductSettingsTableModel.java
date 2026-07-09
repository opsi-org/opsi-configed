/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Component;
import java.util.Comparator;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.data.ColoredTableCellRenderer;
import de.uib.configed.gui.data.ColoredTableCellRendererByIndex;
import de.uib.configed.gui.data.InstallationStateTableModel;
import de.uib.configed.gui.share.table.gui.AdaptingCellEditor;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.DynamicCellEditor;
import de.uib.configed.share.logging.Logging;

public class ProductSettingsTableModel {
	private static final int WIDTH_COLUMN_PRODUCT_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_COMPLETE_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_STATE = 60;

	private static final int WIDTH_COLUMN_PRODUCT_SEQUENCE = 40;
	private static final int WIDTH_COLUMN_VERSION_INFO = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_INSTALLATION_INFO = WIDTH_COLUMN_PRODUCT_STATE;

	private ColorTableCellRenderer colorTableCellRenderer;

	private ProductNameTableCellRenderer productNameTableCellRenderer;

	private ColoredTableCellRendererByIndex installationStatusTableCellRenderer;
	private ColoredTableCellRendererByIndex actionRequestTableCellRenderer;
	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;

	private ColoredTableCellRenderer versionInfoTableCellRenderer;
	private ColoredTableCellRenderer actionResultTableCellRenderer;

	private ColoredTableCellRenderer coloredTableCellRenderer;

	private JTable tableProducts;

	private TableRowSorter<TableModel> rowSorter;

	public ProductSettingsTableModel(JTable tableProducts) {
		this.tableProducts = tableProducts;

		initRenderer();
	}

	private void initRenderer() {
		productNameTableCellRenderer = new ProductNameTableCellRenderer();

		colorTableCellRenderer = new ColorTableCellRenderer();

		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex(
				InstallationStatus.getLabel2TextColor());

		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor());

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex();

		coloredTableCellRenderer = new ColoredTableCellRenderer();
		productsequenceTableCellRenderer = new ColoredTableCellRenderer();

		versionInfoTableCellRenderer = new ProductVersionCellRenderer();

		actionResultTableCellRenderer = new ColoredTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe since instanceof returns false if null
				if (value instanceof String stringValue) {
					if (stringValue.startsWith(InstallationStateTableModel.FAILED_DISPLAY_STRING)) {
						setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (stringValue.startsWith(InstallationStateTableModel.SUCCESS_DISPLAY_STRING)) {
						setForeground(Globals.OK_COLOR);
					} else {
						// Don't set foreground if no special result
					}
				}

				return this;
			}
		};
	}

	public void setRenderer(InstallationStateTableModel istm) {
		setRowSorter();

		int colIndex = -1;

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_ID)) > -1) {
			TableColumn nameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_NAME);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_NAME)) > -1) {
			TableColumn completeNameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_COMPLETE_NAME);
			completeNameColumn.setCellRenderer(colorTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_STATUS)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> statesCombo = new JComboBox<>();

			statusColumn.setCellEditor(new AdaptingCellEditor(statesCombo, istm, true));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_REQUEST)) > -1) {
			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> actionsCombo = new JComboBox<>();
			actionColumn.setCellEditor(new AdaptingCellEditor(actionsCombo, istm, true));
			actionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionColumn.setCellRenderer(actionRequestTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_STATE_CHANGE)) > -1) {
			TableColumn laststatechangeColumn = tableProducts.getColumnModel().getColumn(colIndex);
			laststatechangeColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			laststatechangeColumn.setCellRenderer(coloredTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_PRIORITY)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			rowSorter.setComparator(colIndex, Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne));
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_SEQUENCE)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			rowSorter.setComparator(colIndex, Comparator.comparingInt(ProductSettingsTableModel::parseIntOrMinusOne));
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_VERSION_INFO)) > -1) {
			TableColumn versionInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(WIDTH_COLUMN_VERSION_INFO);
			versionInfoColumn.setCellRenderer(versionInfoTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_ACTION)) > -1) {
			TableColumn lastActionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastActionColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			lastActionColumn.setCellRenderer(coloredTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_RESULT)) > -1) {
			TableColumn actionResultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionResultColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			actionResultColumn.setCellRenderer(actionResultTableCellRenderer);

			JComboBox<String> actionResultCombo = new JComboBox<>();
			actionResultColumn.setCellEditor(new DynamicCellEditor(actionResultCombo, istm));
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_PROGRESS)) > -1) {
			TableColumn actionProgressColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionProgressColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			// default (uncolored) rendering for progress
			actionProgressColumn.setCellRenderer(coloredTableCellRenderer);
		}
	}

	private void setRowSorter() {
		rowSorter = new ProductSettingsTableRowSorter(tableProducts.getModel(), tableProducts);

		tableProducts.setRowSorter(rowSorter);
	}

	private static int parseIntOrMinusOne(String s) {
		if (s == null || s.isBlank()) {
			return -1;
		}
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			Logging.debug("not a number ", s);
			return -1;
		}
	}

	private class ProductNameTableCellRenderer extends ColorTableCellRenderer {
		public ProductNameTableCellRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			String stateChange = ((InstallationStateTableModel) table.getModel())
					.getLastStateChange(tableProducts.convertRowIndexToModel(row));

			if (stateChange == null) {
				stateChange = "";
			}

			setToolTipText(
					Configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange);

			return this;
		}
	}

	@SuppressWarnings("java:S2972")
	private static class ProductVersionCellRenderer extends ColoredTableCellRenderer {
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
						Logging.warning(this, " istm.getGlobalProductInfos()).get(productId) == null for productId ",
								productId);
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
}
