/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Component;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.guidata.ColoredTableCellRenderer;
import de.uib.configed.guidata.ColoredTableCellRendererByIndex;
import de.uib.configed.guidata.InstallationStateTableModel;
import de.uib.configed.guidata.ProductVersionCellRenderer;
import de.uib.opsidatamodel.productstate.ActionProgress;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.ActionResult;
import de.uib.opsidatamodel.productstate.ActionSequence;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.opsidatamodel.productstate.TargetConfiguration;
import de.uib.utilities.IntComparatorForStrings;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.list.StandardListCellRenderer;
import de.uib.utilities.table.gui.AdaptingCellEditorValuesByIndex;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.DynamicCellEditor;
import de.uib.utilities.table.gui.StandardTableCellRenderer;

public class ProductSettingsTableModel {
	private static final int WIDTH_COLUMN_PRODUCT_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_COMPLETE_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_STATE = 60;

	private static final int WIDTH_COLUMN_PRODUCT_SEQUENCE = 40;
	private static final int WIDTH_COLUMN_VERSION_INFO = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PRODUCT_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_PACKAGE_VERSION = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_INSTALLATION_INFO = WIDTH_COLUMN_PRODUCT_STATE;

	private ListCellRenderer<Object> standardListCellRenderer;

	private TableCellRenderer productNameTableCellRenderer;
	private TableCellRenderer productCompleteNameTableCellRenderer;

	private TableCellRenderer targetConfigurationTableCellRenderer;
	private TableCellRenderer installationStatusTableCellRenderer;
	private TableCellRenderer actionProgressTableCellRenderer;
	private TableCellRenderer lastActionTableCellRenderer;
	private TableCellRenderer actionResultTableCellRenderer;
	private TableCellRenderer actionRequestTableCellRenderer;
	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer productversionTableCellRenderer;
	private ColoredTableCellRenderer packageversionTableCellRenderer;

	private ColoredTableCellRenderer versionInfoTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	private ColoredTableCellRenderer lastStateChangeTableCellRenderer;

	private List<? extends SortKey> currentSortKeys;

	private JTable tableProducts;

	public ProductSettingsTableModel(JTable tableProducts) {
		this.tableProducts = tableProducts;

		initRenderer();
	}

	private void initRenderer() {
		standardListCellRenderer = new StandardListCellRenderer();

		productNameTableCellRenderer = new ProductNameTableCellRenderer("");

		productCompleteNameTableCellRenderer = new StandardTableCellRenderer("");

		targetConfigurationTableCellRenderer = new ColoredTableCellRendererByIndex(
				TargetConfiguration.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_TARGET_CONFIGURATION) + ": ");

		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex(
				InstallationStatus.getLabel2TextColor(), InstallationStatus.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_STATUS) + ": ");

		actionProgressTableCellRenderer = new ActionProgressTableCellRenderer(ActionProgress.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_PROGRESS) + ": ");

		actionResultTableCellRenderer = new ColoredTableCellRendererByIndex(ActionResult.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_RESULT) + ": ");

		lastActionTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_ACTION) + ": ");

		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor(),
				ActionRequest.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_ACTION_REQUEST) + ": ");

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex(ActionSequence.getLabel2DisplayLabel(),
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_PRIORITY) + ": ");

		lastStateChangeTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE));

		productsequenceTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_POSITION));

		productversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PRODUCT_VERSION));

		packageversionTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_PACKAGE_VERSION));

		versionInfoTableCellRenderer = new ProductVersionCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_VERSION_INFO));

		installationInfoTableCellRenderer = new ColoredTableCellRenderer(
				InstallationStateTableModel.getColumnTitle(ProductState.KEY_INSTALLATION_INFO)) {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				// Safe sind instanceof returns false if null
				if (value instanceof String) {
					String val = (String) value;
					if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.FAILED)))) {
						c.setForeground(Globals.PANEL_PRODUCT_SETTINGS_FAILED_COLOR);
					} else if (val.startsWith(
							ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.SUCCESSFUL)))) {
						c.setForeground(Globals.OK_COLOR);
					} else {
						// Don't set foreground if no special result
					}
				}

				return c;
			}
		};
	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		Logging.info(this, "getSortKeys : " + infoSortKeys(currentSortKeys));
		return currentSortKeys;
	}

	public void setSortKeys(List<? extends SortKey> currentSortKeys) {
		Logging.info(this, "setSortKeys: " + infoSortKeys(currentSortKeys));
		if (currentSortKeys != null) {
			tableProducts.getRowSorter().setSortKeys(currentSortKeys);
		}
	}

	public void setRenderer(InstallationStateTableModel istm) {
		final Comparator<String> myComparator = Comparator.comparing(String::toString);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableProducts.getModel()) {
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

		tableProducts.setRowSorter(sorter);
		sorter.addRowSorterListener(event -> currentSortKeys = tableProducts.getRowSorter().getSortKeys());

		tableProducts.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(tableProducts.getTableHeader().getDefaultRenderer()));

		int colIndex = -1;

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_ID)) > -1) {
			TableColumn nameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_NAME);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_NAME)) > -1) {
			TableColumn completeNameColumn = tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_COMPLETE_NAME);
			completeNameColumn.setCellRenderer(productCompleteNameTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_TARGET_CONFIGURATION)) > -1) {
			TableColumn targetColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> targetCombo = new JComboBox<>();
			targetCombo.setRenderer(standardListCellRenderer);

			targetColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(targetCombo, istm));
			targetColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			targetColumn.setCellRenderer(targetConfigurationTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_INSTALLATION_STATUS)) > -1) {
			TableColumn statusColumn = tableProducts.getColumnModel().getColumn(colIndex);

			JComboBox<String> statesCombo = new JComboBox<>();
			statesCombo.setRenderer(standardListCellRenderer);

			statusColumn.setCellEditor(new AdaptingCellEditorValuesByIndex(statesCombo, istm));
			statusColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_PROGRESS)) > -1) {
			TableColumn progressColumn = tableProducts.getColumnModel().getColumn(colIndex);

			progressColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			progressColumn.setCellRenderer(actionProgressTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_LAST_ACTION)) > -1) {
			TableColumn lastactionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastactionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			lastactionColumn.setCellRenderer(lastActionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_ACTION_RESULT)) > -1) {
			TableColumn actionresultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionresultColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_STATE);
			actionresultColumn.setCellRenderer(actionResultTableCellRenderer);
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

			laststatechangeColumn.setCellRenderer(lastStateChangeTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_PRIORITY)) > -1) {
			TableColumn priorityclassColumn = tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);

			sorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_POSITION)) > -1) {
			TableColumn productsequenceColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE);

			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);

			sorter.setComparator(colIndex, new IntComparatorForStrings());
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PRODUCT_VERSION)) > -1) {
			TableColumn productversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			productversionColumn.setPreferredWidth(WIDTH_COLUMN_PRODUCT_VERSION);
			productversionColumn.setCellRenderer(productversionTableCellRenderer);
		}

		if ((colIndex = istm.getColumnIndex(ProductState.KEY_PACKAGE_VERSION)) > -1) {
			TableColumn packageversionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			packageversionColumn.setPreferredWidth(WIDTH_COLUMN_PACKAGE_VERSION);
			packageversionColumn.setCellRenderer(packageversionTableCellRenderer);
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

	private class ProductNameTableCellRenderer extends StandardTableCellRenderer {
		public ProductNameTableCellRenderer(String tooltipPrefix) {
			super(tooltipPrefix);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			// Will be done if c==null is true since instanceof
			// returns false if null
			if (!(c instanceof JComponent)) {
				return c;
			}

			JComponent jc = (JComponent) c;

			String stateChange = ((InstallationStateTableModel) table.getModel())
					.getLastStateChange(tableProducts.convertRowIndexToModel(row));

			if (stateChange == null) {
				stateChange = "";
			}

			stateChange = table.getValueAt(row, column).toString() + ", "
					+ Configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange;

			jc.setToolTipText(stateChange);

			return jc;
		}
	}

	private String infoSortKeys(List<? extends SortKey> sortKeys) {
		if (sortKeys == null) {
			return "null";
		}

		StringBuilder result = new StringBuilder("[");
		for (SortKey key : sortKeys) {
			result.append(key.getColumn() + ".." + key);
		}
		result.append("]");
		Logging.info(this, "infoSortkeys " + result);
		return " (number " + sortKeys.size() + ") ";
	}
}
