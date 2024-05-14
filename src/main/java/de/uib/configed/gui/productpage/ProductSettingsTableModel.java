/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Component;
import java.util.Comparator;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
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
import de.uib.utils.swing.list.StandardListCellRenderer;
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

	private ListCellRenderer<Object> standardListCellRenderer;

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
		standardListCellRenderer = new StandardListCellRenderer();

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

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_TARGET_CONFIGURATION)) > -1) {
			TableColumn targetColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> targetCombo = new JComboBox<>();
			targetCombo.setRenderer(standardListCellRenderer);

			targetColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(targetCombo, istm));
			targetColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			targetColumn.setCellRenderer(colorTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_STATUS)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> statesCombo = new JComboBox<>();
			statesCombo.setRenderer(standardListCellRenderer);

			statusColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(statesCombo, istm));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_REQUEST)) > -1) {
			TableColumn actionColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> actionsCombo = new JComboBox<>();
			actionsCombo.setRenderer(standardListCellRenderer);
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

			installationInfoCombo.setRenderer(standardListCellRenderer);

			DynamicCellEditor cellEditor = new DynamicCellEditor(installationInfoCombo, istm);

			installationInfoColumn.setCellEditor(cellEditor);
		}
	}

	private void setRowSorter() {
		final Comparator<String> myComparator = Comparator.comparing(String::toString);

		rowSorter = new TableRowSorter<>(tableProducts.getModel()) {
			@Override
			protected boolean useToString(int column) {
				return true;
			}

			@Override
			public Comparator<?> getComparator(int column) {
				if (column == 0) {
					return myComparator;
				} else {
					return super.getComparator(column);
				}
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
