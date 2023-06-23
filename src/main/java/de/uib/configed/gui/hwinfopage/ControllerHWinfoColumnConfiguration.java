/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.IntComparatorForObjects;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.BooleanIconTableCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;

public class ControllerHWinfoColumnConfiguration {

	public static final String COL_LINE_NO = Configed.getResourceValue("HWinfoColumnConfiguration.colLineNo");
	public static final String COL_HOST_VS_ITEM_ASSIGNED = Configed
			.getResourceValue("HWinfoColumnConfiguration.colHostVsItemAssigned");
	public static final String VAL_ASSIGNED_TO_HOST = Configed
			.getResourceValue("HWinfoColumnConfiguration.valAssignedToHost");
	public static final String VAL_ASSIGNED_TO_HW_ITEM = Configed
			.getResourceValue("HWinfoColumnConfiguration.valAssignedToHwItem");
	public static final String COL_USE_IN_QUERY = Configed.getResourceValue("HWinfoColumnConfiguration.colUseInQuery");
	public static final String COL_OPSI_COLUMN_NAME = Configed
			.getResourceValue("HWinfoColumnConfiguration.colOpsiColumnName");
	public static final String COL_OPSI_DB_COLUMN_TYPE = Configed
			.getResourceValue("HWinfoColumnConfiguration.colOpsiDbColumnType");
	public static final String COL_HW_CLASS = Configed.getResourceValue("HWinfoColumnConfiguration.colHwClass");
	public static final String COL_LINUX_QUERY = Configed.getResourceValue("HWinfoColumnConfiguration.colLinuxQuery");
	public static final String COL_WMI_QUERY = Configed.getResourceValue("HWinfoColumnConfiguration.colWMIQuery");

	private static final int KEY_COL = 0;

	public PanelGenEditTable panel;
	private GenTableModel model;

	private List<String> columnNames;

	private Map<String, Map<String, Boolean>> updateItems;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ControllerHWinfoColumnConfiguration() {

		initPanel();
		initModel();
		updateItems = new HashMap<>();
	}

	private void initPanel() {
		panel = new PanelGenEditTable("", 0, true, 0, false,
				new int[] { PanelGenEditTable.POPUP_RELOAD, PanelGenEditTable.POPUP_PDF }, true) {

			@Override
			public void commit() {
				// we collect for each changed line an update item
				super.commit();
				Logging.info(this, "commit, we do the saving");

				Logging.info(this, " we have got updateItems " + updateItems);
				persistenceController.saveHwColumnConfig(updateItems);
				updateItems.clear();

			}

			@Override
			public void reload() {

				persistenceController.hwAuditConfRequestRefresh();
				persistenceController.configOptionsRequestRefresh();
				model.requestReload();

				persistenceController.getConfigOptions();

				model.reset();
				setDataChanged(false);

				panel.moveToValue("true", columnNames.indexOf(COL_USE_IN_QUERY), true);

			}

		};

		panel.setMasterFrame(ConfigedMain.getMainFrame());
		panel.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panel.showFilterIcon(true);
		panel.setFiltering(true);
		panel.setDeleteAllowed(false);
	}

	private void initModel() {

		List<TableEditItem> updateCollection = new ArrayList<>();
		columnNames = new ArrayList<>();
		columnNames.add(COL_LINE_NO);
		columnNames.add(COL_HW_CLASS);
		columnNames.add(COL_LINUX_QUERY);
		columnNames.add(COL_WMI_QUERY);
		columnNames.add(COL_HOST_VS_ITEM_ASSIGNED);
		columnNames.add(COL_OPSI_COLUMN_NAME);
		columnNames.add(COL_USE_IN_QUERY);
		columnNames.add(COL_OPSI_DB_COLUMN_TYPE);

		List<String> classNames = new ArrayList<>();

		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		MapTableUpdateItemFactory updateItemFactory = new MapTableUpdateItemFactory(columnNames, classNames, KEY_COL);

		model = new GenTableModel(updateItemFactory,

				// tableProvider

				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, this::getHwColumnConfig)),
				KEY_COL, new int[] { KEY_COL }, panel, updateCollection) {
			@Override
			public boolean isCellEditable(int row, int col) {
				boolean result = super.isCellEditable(row, col);

				if (result) {
					Object val = getValueAt(row, col);
					if (val == null || (val instanceof String && ((String) val).trim().isEmpty())) {
						result = false;
					}
				}

				return result;
			}
		};

		updateItemFactory.setSource(model);

		// we got metadata:

		model.setEditableColumns(new int[] { columnNames.indexOf(COL_USE_IN_QUERY) });

		panel.setTableModel(model);
		panel.setEmphasizedColumns(new int[] { columnNames.indexOf(COL_USE_IN_QUERY) });

		panel.setTitlePane(
				new JComponent[] { new JLabel(Configed.getResourceValue("HWinfoColumnConfiguration.infoTitle")) }, 20);

		if (!Main.THEMES) {
			panel.setTitlePaneBackground(Globals.BACKGROUND_COLOR_7);
		}

		LinkedHashMap<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();

		sortDescriptor.put(KEY_COL, SortOrder.ASCENDING);
		panel.setSortOrder(sortDescriptor);

		panel.setComparator(COL_LINE_NO, new IntComparatorForObjects());

		// now sorted
		panel.reload();

		TableColumn col;
		col = panel.getColumnModel().getColumn(columnNames.indexOf(COL_LINE_NO));
		col.setMaxWidth(80);
		col.setHeaderValue("");

		col = panel.getColumnModel().getColumn(columnNames.indexOf(COL_USE_IN_QUERY));
		col.setMaxWidth(80);

		Icon iconChecked = Globals.createImageIcon("images/checked_box_blue_14.png", "");
		Icon iconUnchecked = Globals.createImageIcon("images/checked_box_blue_empty_14.png", "");
		Icon iconEmpty = Globals.createImageIcon("images/checked_void.png", "");

		col.setCellRenderer(new BooleanIconTableCellRenderer(iconChecked, iconUnchecked, true));

		JCheckBox useCheck = new JCheckBox(iconEmpty);

		col.setCellEditor(new DefaultCellEditor(useCheck));
		// checkbox is not visible, since any click
		// ends editing and lets immediately resurface the cell renderer
		// this is not correct for keys, therefore we set the void icon

		panel.setUpdateController(new MapItemsUpdateController(panel, model, new MapBasedUpdater() {
			@Override
			public String sendUpdate(Map<String, Object> rowmap) {

				Logging.info(this, "within MapItemsUpdateController sendUpdate " + rowmap);

				buildUpdateItem(

						new ColumnIdent((String) rowmap.get(COL_OPSI_COLUMN_NAME)),
						(Boolean) rowmap.get(COL_USE_IN_QUERY));

				return "";
			}

			@Override
			public boolean sendDelete(Map<String, Object> rowmap) {
				Logging.info(this, "within MapItemsUpdateController sendDelete " + rowmap);
				// method is not used since we don*t delete rows

				return true;
			}
		}, updateCollection));
	}

	private void buildUpdateItem(ColumnIdent col, Boolean use) {
		Logging.info(this, " buildUpdateItem value " + use + " for col ident " + col);

		Map<String, Boolean> tableConfigUpdates = updateItems.computeIfAbsent(col.getConfigIdent(),
				s -> new HashMap<>());

		Logging.info(this, "add this item to items for configIdent " + col.getConfigIdent());

		Logging.info(this, "add this item to items for configIdent " + col.getConfigIdent());

		tableConfigUpdates.put(col.getDBColumnName(), use);
	}

	private static String formatLineNo(int no) {
		return "(" + no + ")";
	}

	private Map<String, Map<String, Object>> getHwColumnConfig() {
		Map<String, Map<String, Object>> result = new LinkedHashMap<>();

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = persistenceController.getHwAuditDeviceClasses();
		int id = 0;

		for (Entry<String, OpsiHwAuditDeviceClass> hwClassEntry : hwAuditDeviceClasses.entrySet()) {

			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClassEntry.getValue();
			List<OpsiHwAuditDevicePropertyType> deviceHostProperties = hwAuditDeviceClass.getDeviceHostProperties();
			List<OpsiHwAuditDevicePropertyType> deviceHwItemProperties = hwAuditDeviceClass.getDeviceHwItemProperties();

			// hw class line
			Map<String, Object> lineMap = new LinkedHashMap<>();
			lineMap.put(COL_LINE_NO, formatLineNo(id));
			lineMap.put(COL_HW_CLASS, hwClassEntry.getKey());
			lineMap.put(COL_LINUX_QUERY, hwAuditDeviceClass.getLinuxQuery());
			lineMap.put(COL_WMI_QUERY, hwAuditDeviceClass.getWmiQuery());

			result.put(formatLineNo(id), lineMap);
			id++;

			// colHostVsItemAssigned line
			lineMap = new LinkedHashMap<>();
			lineMap.put(COL_LINE_NO, formatLineNo(id));

			lineMap.put(COL_HOST_VS_ITEM_ASSIGNED, VAL_ASSIGNED_TO_HOST);

			result.put(formatLineNo(id), lineMap);
			id++;

			for (OpsiHwAuditDevicePropertyType deviceProperty : deviceHostProperties) {
				lineMap = new LinkedHashMap<>();
				lineMap.put(COL_LINE_NO, formatLineNo(id));

				ColumnIdent columnIdent = new ColumnIdent(hwClassEntry.getKey(),
						OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE, deviceProperty.getOpsiDbColumnName());

				lineMap.put(COL_OPSI_COLUMN_NAME, columnIdent.produceColumnCellValue());
				lineMap.put(COL_OPSI_DB_COLUMN_TYPE, deviceProperty.getOpsiDbColumnType());

				if (deviceProperty.getDisplayed() == null) {
					lineMap.put(COL_USE_IN_QUERY, "" + false);
				} else {
					lineMap.put(COL_USE_IN_QUERY, "" + deviceProperty.getDisplayed());
				}

				result.put(formatLineNo(id), lineMap);
				id++;
			}

			lineMap = new LinkedHashMap<>();
			lineMap.put(COL_LINE_NO, formatLineNo(id));
			lineMap.put(COL_HOST_VS_ITEM_ASSIGNED, VAL_ASSIGNED_TO_HW_ITEM);

			result.put(formatLineNo(id), lineMap);

			id++;

			for (OpsiHwAuditDevicePropertyType deviceProperty : deviceHwItemProperties) {
				lineMap = new LinkedHashMap<>();
				lineMap.put(COL_LINE_NO, formatLineNo(id));

				ColumnIdent columnIdent = new ColumnIdent(hwClassEntry.getKey(),
						OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE, deviceProperty.getOpsiDbColumnName());

				lineMap.put(COL_OPSI_COLUMN_NAME, columnIdent.produceColumnCellValue());

				lineMap.put(COL_OPSI_DB_COLUMN_TYPE, deviceProperty.getOpsiDbColumnType());

				if (deviceProperty.getDisplayed() == null) {
					lineMap.put(COL_USE_IN_QUERY, "" + false);
				} else {
					lineMap.put(COL_USE_IN_QUERY, "" + deviceProperty.getDisplayed());
				}

				result.put(formatLineNo(id), lineMap);
				id++;
			}
		}

		return result;
	}
}
