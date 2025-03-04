/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Component;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.guidata.ColoredTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.ProductVersionCellRenderer;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utils.IntComparatorForStrings;
import de.uib.utils.table.gui.AdaptingCellEditorValuesByIndex;
import de.uib.utils.table.gui.ColorTableCellRenderer;
import de.uib.utils.table.gui.DynamicCellEditor;

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
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

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

		installationInfoTableCellRenderer = new ColoredTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String stringValue) {
					if (stringValue.startsWith(ActionResult.getLabel(ActionResult.FAILED))) {
						setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (stringValue.startsWith(ActionResult.getLabel(ActionResult.SUCCESSFUL))) {
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

			statusColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(statesCombo, istm));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_REQUEST)) > -1) {
			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> actionsCombo = new JComboBox<>();
			actionColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(actionsCombo, istm));
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

			rowSorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_SEQUENCE)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			rowSorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_VERSION_INFO)) > -1) {
			TableColumn versionInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(WIDTH_COLUMN_VERSION_INFO);
			versionInfoColumn.setCellRenderer(versionInfoTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_INFO)) > -1) {
			TableColumn installationInfoColumn = tableProducts.getColumnModel().getColumn(colIndex);
			installationInfoColumn.setPreferredWidth(WIDTH_COLUMN_INSTALLATION_INFO);
			installationInfoColumn.setCellRenderer(installationInfoTableCellRenderer);

			JComboBox<String> installationInfoCombo = new JComboBox<>();

			DynamicCellEditor cellEditor = new DynamicCellEditor(installationInfoCombo, istm);

			installationInfoColumn.setCellEditor(cellEditor);
		}
	}

	private void setRowSorter() {
		rowSorter = new TableRowSorter<>(tableProducts.getModel()) {
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
				} else if (columnName
						.equals(Configed.getResourceValue("InstallationStateTableModel.installationStatus"))) {
					List<String> order = Arrays.asList(InstallationStatus.KEY_INSTALLED, InstallationStatus.KEY_UNKNOWN,
							InstallationStatus.KEY_NOT_INSTALLED);
					comparator = (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
				} else if (columnName.equals(Configed.getResourceValue("InstallationStateTableModel.actionRequest"))) {
					List<String> order = Arrays.asList(ActionRequest.KEY_SETUP, ActionRequest.KEY_UPDATE,
							ActionRequest.KEY_UNINSTALL, ActionRequest.KEY_ALWAYS, ActionRequest.KEY_ONCE,
							ActionRequest.KEY_CUSTOM, ActionRequest.KEY_NONE);
					comparator = (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
				} else {
					comparator = super.getComparator(column);
				}

				return comparator;
			}
		};

		tableProducts.setRowSorter(rowSorter);
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
}
