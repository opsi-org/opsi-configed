/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.ActionResult;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ProductPageManager;
import de.uib.configed.gui.data.ColoredTableCellRenderer;
import de.uib.configed.gui.data.ColoredTableCellRendererByIndex;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.share.table.gui.AdaptingCellEditor;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.DynamicCellEditor;
import de.uib.configed.share.logging.Logging;

public class ProductTableColumnFactory {
	private static final int WIDTH_COLUMN_PRODUCT_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_COMPLETE_NAME = 170;
	private static final int WIDTH_COLUMN_PRODUCT_STATE = 60;

	private static final int WIDTH_COLUMN_PRODUCT_SEQUENCE = 40;
	private static final int WIDTH_COLUMN_VERSION_INFO = WIDTH_COLUMN_PRODUCT_STATE;
	private static final int WIDTH_COLUMN_INSTALLATION_INFO = WIDTH_COLUMN_PRODUCT_STATE;

	private ConfigedMain configedMain;
	private ProductOptionsComboBoxModeller comboBoxModeller;
	private ProductTable productTable;

	private ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	private ColoredTableCellRenderer productsequenceTableCellRenderer;
	private ColoredTableCellRenderer installationInfoTableCellRenderer;

	public ProductTableColumnFactory(ProductOptionsComboBoxModeller comboBoxModeller, ConfigedMain configedMain,
			ProductTable productTable) {
		this.configedMain = configedMain;
		this.comboBoxModeller = comboBoxModeller;
		this.productTable = productTable;

		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex();
		priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		productsequenceTableCellRenderer = new ColoredTableCellRenderer();
		productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

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

	public List<TableColumnConfig> buildProductColumnConfigs(ProductSettingsType type) {
		List<TableColumnConfig> columns = new ArrayList<>();

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_ID)
				.header(ProductTable.getColumnTitle(ProductState.KEY_PRODUCT_ID)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_NAME).toggleable(false)
				.renderer(new ProductNameTableCellRenderer(productTable.getTableViewComponent())).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_NAME)
				.header(ProductTable.getColumnTitle(ProductState.KEY_PRODUCT_NAME)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_COMPLETE_NAME).renderer(new ColorTableCellRenderer()).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_STATUS)
				.header(ProductTable.getColumnTitle(ProductState.KEY_INSTALLATION_STATUS)).editable(true)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_STATE)
				.renderer(new ColoredTableCellRendererByIndex(InstallationStatus.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createInstallationStatusComparator()).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_INSTALLATION_INFO)
				.header(ProductTable.getColumnTitle(ProductState.KEY_INSTALLATION_INFO)).editable(true)
				.prefferedWidth(WIDTH_COLUMN_INSTALLATION_INFO).renderer(installationInfoTableCellRenderer)
				.editor(new DynamicCellEditor(new JComboBox<>(), comboBoxModeller)).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_REQUEST)
				.header(ProductTable.getColumnTitle(ProductState.KEY_ACTION_REQUEST)).editable(true)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_STATE)
				.renderer(new ColoredTableCellRendererByIndex(ActionRequest.getLabel2TextColor()))
				.editor(new AdaptingCellEditor(new JComboBox<>(), comboBoxModeller, true))
				.comparator(createActionRequestComparator()).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_PRODUCT_PRIORITY)
				.header(ProductTable.getColumnTitle(ProductState.KEY_PRODUCT_PRIORITY)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE).renderer(priorityclassTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductTableColumnFactory::parseIntOrMinusOne)).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_ACTION_SEQUENCE)
				.header(ProductTable.getColumnTitle(ProductState.KEY_ACTION_SEQUENCE)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE).renderer(productsequenceTableCellRenderer)
				.comparator(Comparator.comparingInt(ProductTableColumnFactory::parseIntOrMinusOne)).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_LAST_STATE_CHANGE)
				.header(ProductTable.getColumnTitle(ProductState.KEY_LAST_STATE_CHANGE)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_PRODUCT_SEQUENCE).renderer(new ColoredTableCellRenderer()).build());

		columns.add(TableColumnConfig.builder().key(ProductState.KEY_VERSION_INFO)
				.header(ProductTable.getColumnTitle(ProductState.KEY_VERSION_INFO)).editable(false)
				.prefferedWidth(WIDTH_COLUMN_VERSION_INFO).renderer(new ProductVersionCellRenderer(configedMain))
				.build());

		Map<String, Boolean> productDisplayFields = type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS
				? PersistenceControllerFactory.getPersistenceController().getDataServices().product
						.getProductOnClientsDisplayFieldsLocalbootProducts()
				: PersistenceControllerFactory.getPersistenceController().getDataServices().product
						.getProductOnClientsDisplayFieldsNetbootProducts();
		List<String> displayFields = ProductPageManager.getDisplayFieldsList(productDisplayFields);

		Iterator<TableColumnConfig> iterator = columns.iterator();
		while (iterator.hasNext()) {
			TableColumnConfig column = iterator.next();
			if (!displayFields.contains(column.getKey())) {
				columns.set(columns.indexOf(column), column.withVisible(false));
			}
		}

		return columns;
	}

	private static int parseIntOrMinusOne(Object s) {
		if (s == null || ((String) s).isBlank()) {
			return -1;
		}
		try {
			return Integer.parseInt((String) s);
		} catch (NumberFormatException ex) {
			Logging.debug("not a number ", s);
			return -1;
		}
	}

	private static Comparator<Object> createInstallationStatusComparator() {
		List<String> order = List.of(InstallationStatus.KEY_INSTALLED, InstallationStatus.KEY_UNKNOWN,
				InstallationStatus.KEY_NOT_INSTALLED);
		return (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
	}

	private static Comparator<Object> createActionRequestComparator() {
		List<String> order = List.of(ActionRequest.KEY_SETUP, ActionRequest.KEY_UPDATE, ActionRequest.KEY_UNINSTALL,
				ActionRequest.KEY_ALWAYS, ActionRequest.KEY_ONCE, ActionRequest.KEY_CUSTOM, ActionRequest.KEY_NONE);
		return (o1, o2) -> Integer.compare(order.indexOf(o1), order.indexOf(o2));
	}

	private static class ProductNameTableCellRenderer extends ColorTableCellRenderer {
		GenericTableViewComponent tableViewComponent;

		public ProductNameTableCellRenderer(GenericTableViewComponent tableViewComponent) {
			super();
			this.tableViewComponent = tableViewComponent;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			String stateChange = tableViewComponent != null ? tableViewComponent.getRowByModelIndex(row)
					.getValue(ProductState.KEY_LAST_STATE_CHANGE, String.class) : null;
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
		private ConfigedMain configedMain;

		public ProductVersionCellRenderer(ConfigedMain configedMain) {
			super();

			this.configedMain = configedMain;
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
						.equals(ProductConfigurationEngine.UNEQUAL_ADD_STRING + Globals.CONFLICT_STATE_STRING)) {
					setForeground(Globals.PRODUCT_STATUS_MIXED_COLOR);
				} else {
					String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);

					String serverProductVersion = "";
					if (configedMain != null) {
						Map<String, Map<String, Object>> globalinfos = PersistenceControllerFactory
								.getPersistenceController().getDataServices().product
										.getProductGlobalInfosPD(configedMain.getDepotRepresentative());

						if (globalinfos.get(productId) == null) {
							Logging.warning(this,
									" istm.getGlobalProductInfos()).get(productId) == null for productId ", productId);
						} else {
							serverProductVersion = serverProductVersion
									+ globalinfos.get(productId).get(ProductState.KEY_VERSION_INFO);
						}
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
