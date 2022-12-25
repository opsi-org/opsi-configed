/*
 * ControllerHWinfoColumnConfiguration
 *
 * Copyright (c) 2018 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 * author: Rupert Röder
 */

package de.uib.configed.gui.hwinfopage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControllerHWinfoColumnConfiguration {
	public PanelGenEditTable panel;
	private GenTableModel model;
	TableUpdateCollection updateCollection;

	Vector<String> columnNames;
	Vector<String> classNames;
	public static final String colLineNo = configed.getResourceValue("HWinfoColumnConfiguration.colLineNo");
	public static final String colHostVsItemAssigned = configed
			.getResourceValue("HWinfoColumnConfiguration.colHostVsItemAssigned");
	public static final String valAssignedToHost = configed
			.getResourceValue("HWinfoColumnConfiguration.valAssignedToHost");
	public static final String valAssignedToHwItem = configed
			.getResourceValue("HWinfoColumnConfiguration.valAssignedToHwItem");
	public static final String colUseInQuery = configed.getResourceValue("HWinfoColumnConfiguration.colUseInQuery");
	public static final String colOpsiColumnName = configed
			.getResourceValue("HWinfoColumnConfiguration.colOpsiColumnName");
	public static final String colOpsiDbColumnType = configed
			.getResourceValue("HWinfoColumnConfiguration.colOpsiDbColumnType");
	public static final String colHwClass = configed.getResourceValue("HWinfoColumnConfiguration.colHwClass");
	public static final String colLinuxQuery = configed.getResourceValue("HWinfoColumnConfiguration.colLinuxQuery");
	public static final String colWMIQuery = configed.getResourceValue("HWinfoColumnConfiguration.colWMIQuery");
	// public static final String colTellAgainHardwareClass =
	

	private Map<String, Map<String, Boolean>> updateItems;

	private class ColumnIdent {
		String dbColumnName;
		String hwClass;
		String tableType;
		String configIdent;

		ColumnIdent(String tableValue) {
			if (tableValue == null)
				return;

			int indexCurly = tableValue.indexOf('{');

			if (indexCurly == -1) {
				dbColumnName = tableValue.trim();
				return;
			}

			dbColumnName = tableValue.substring(0, indexCurly).trim();
			String tableIdent = tableValue.substring(indexCurly + 1);

			String checkType = OpsiHwAuditDeviceClass.hostAssignedTableType + "}";

			if (tableIdent.endsWith(checkType))
				tableType = OpsiHwAuditDeviceClass.hostAssignedTableType;
			else {
				checkType = OpsiHwAuditDeviceClass.hwItemAssignedTableType + "}";
				if (tableIdent.endsWith(checkType))
					tableType = OpsiHwAuditDeviceClass.hwItemAssignedTableType;
			}

			int indexUnderline = tableIdent.lastIndexOf("_");
			hwClass = tableIdent.substring(0, indexUnderline);

			configIdent = hwClass + "_" + tableType;

			logging.debug(this, "from '" + tableValue + "' we get " + " col name " + dbColumnName + " type " + tableType
					+ " hw class " + hwClass);

		}

		ColumnIdent(String hwClass, String tableType, String colName) {
			this.dbColumnName = colName;
			this.hwClass = hwClass;
			this.tableType = tableType;
		}

		String produceColumnCellValue() {
			String result = dbColumnName + " {" + hwClass + "_" + tableType + "}";
			logging.debug(this, "produceColumnCellValue " + result);

			return result;
		}

		@Override
		public String toString() {
			return "dbColumnName " + dbColumnName + " " + "hwClass " + hwClass + " " + "tableType " + tableType;
		}
	}

	ConfigedMain main;
	protected PersistenceController persist;

	static final int keycol = 0;

	public ControllerHWinfoColumnConfiguration(ConfigedMain main, PersistenceController persist) {
		this.main = main;
		this.persist = persist;

		initPanel();
		initModel();
		updateItems = new HashMap<>();

	}

	protected void initPanel() {
		panel = new PanelGenEditTable("", 
				0, // maxTableWidth
				true, // editing
				0, // generalPopupPosition
				false,
				// PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF,
				new int[] { PanelGenEditTable.POPUP_RELOAD, PanelGenEditTable.POPUP_PDF }, true) {

			@Override
			public void commit() {
				super.commit(); // we collect for each changed line an update item
				logging.info(this, "commit, we do the saving");

				logging.info(this, " we have got updateItems " + updateItems);
				persist.saveHwColumnConfig(updateItems);
				updateItems.clear();

			}

			@Override
			public void reload() {

				persist.hwAuditConfRequestRefresh();
				persist.configOptionsRequestRefresh();
				model.requestReload();

				persist.getConfigOptions();

				model.reset();
				setDataChanged(false);

				panel.moveToValue("true", columnNames.indexOf(colUseInQuery), true);

			}

			/*
			 * @Override
			 * protected Object modifyHeaderValue(Object s)
			 * {
			 * if (s != null && s instanceof String && ((String)
			 * s).startsWith(DELETE_PREFIX))
			 * {
			 * String modified = ((String) s).substring(DELETE_PREFIX.length());
			 * return modified;
			 * }
			 * 
			 * return s;
			 * }
			 */

		};

		panel.setMasterFrame(Globals.mainFrame);
		panel.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panel.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panel.setFiltering(true);
		panel.setDeleteAllowed(false);

	}

	protected void initModel() {

		updateCollection = new TableUpdateCollection();
		columnNames = new Vector<>();
		columnNames.add(colLineNo);
		columnNames.add(colHwClass);
		columnNames.add(colLinuxQuery);
		columnNames.add(colWMIQuery);
		columnNames.add(colHostVsItemAssigned);
		columnNames.add(colOpsiColumnName);
		columnNames.add(colUseInQuery);
		columnNames.add(colOpsiDbColumnType);
		
		
		

		classNames = new Vector<>();

		
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		// GenericTableUpdateItemFactory updateItemFactory = new

		MapTableUpdateItemFactory updateItemFactory = new MapTableUpdateItemFactory(columnNames, classNames, keycol);

		model = new GenTableModel(updateItemFactory,

				// tableProvider
				// new de.uib.utilities.table.provider.DefaultTableProvider(sqlSource),
				new DefaultTableProvider(
						new RetrieverMapSource(columnNames, classNames, () -> (Map) getHwColumnConfig())),

				keycol,

				// final columns int array
				new int[] { keycol },

				// table model listener
				panel,

				// TableUpdateCollection updates
				updateCollection) {
			@Override
			public boolean isCellEditable(int row, int col) {
				boolean result = super.isCellEditable(row, col);

				if (result) {

					Object val = getValueAt(row, col);
					if (val == null || (val instanceof String && ((String) val).trim().equals(""))) {
						result = false;
					}
				}

				return result;
			}
		}

		;

		updateItemFactory.setSource(model);

		// for (String hwClass : hwAuditDeviceClasses.keySet() )

		// we got metadata:

		model.setEditableColumns(new int[] {
				// columnNames.indexOf ( colOpsiColumnName ),
				columnNames.indexOf(colUseInQuery) });

		panel.setTableModel(model);
		panel.setEmphasizedColumns(new int[] {
				// columnNames.indexOf ( colOpsiColumnName ),
				columnNames.indexOf(colUseInQuery) });

		

		// Icon iconConfigure =
		

		panel.setTitlePane(
				new JComponent[] { new JLabel(configed.getResourceValue("HWinfoColumnConfiguration.infoTitle")) }, 20);

		panel.setTitlePaneBackground(Globals.backLightBlue);

		LinkedHashMap<Integer, SortOrder> sortDescriptor = new LinkedHashMap<>();

		sortDescriptor.put(keycol, SortOrder.ASCENDING);
		panel.setSortOrder(sortDescriptor);

		panel.setComparator(colLineNo, new de.uib.utilities.IntComparatorForObjects());

		panel.reload();// now sorted

		javax.swing.table.TableColumn col;
		col = panel.getColumnModel().getColumn(columnNames.indexOf(colLineNo));
		col.setMaxWidth(80);
		col.setHeaderValue("");

		col = panel.getColumnModel().getColumn(columnNames.indexOf(colUseInQuery));
		col.setMaxWidth(80);

		Icon iconChecked = Globals.createImageIcon("images/checked_box_blue_14.png", "");
		Icon iconUnchecked = Globals.createImageIcon("images/checked_box_blue_empty_14.png", "");
		Icon iconEmpty = Globals.createImageIcon("images/checked_void.png", "");

		col.setCellRenderer(new de.uib.utilities.table.gui.BooleanIconTableCellRenderer(iconChecked, iconUnchecked,
				iconEmpty, true));

		JCheckBox useCheck = new JCheckBox(iconEmpty);

		col.setCellEditor(new DefaultCellEditor(useCheck));
		// checkbox is not visible, since any click
		// ends editing and lets immediately resurface the cell renderer
		// this is not correct for keys, therefore we set the void icon

		panel.setUpdateController(new MapItemsUpdateController(panel, model, new MapBasedUpdater() {
			@Override
			public String sendUpdate(Map<String, Object> rowmap) {

				

				logging.info(this, "within MapItemsUpdateController sendUpdate " + rowmap);

				buildUpdateItem(

						new ColumnIdent((String) rowmap.get(colOpsiColumnName)), (Boolean) rowmap.get(colUseInQuery));

				return "";
			}

			@Override
			public boolean sendDelete(Map<String, Object> rowmap) {
				logging.info(this, "within MapItemsUpdateController sendDelete " + rowmap);
				// method is not used since we don*t delete rows

				return true;
			}
		}, updateCollection));

	}

	private void buildUpdateItem(ColumnIdent col, Boolean use) {
		logging.info(this, " buildUpdateItem value " + use + " for col ident " + col);

		Map<String, Boolean> tableConfigUpdates = updateItems.get(col.configIdent);

		logging.info(this, "add this item to items for configIdent " + col.configIdent);

		logging.info(this, "add this item to items for configIdent " + col.configIdent);

		if (tableConfigUpdates == null) {
			tableConfigUpdates = new HashMap<>();
			updateItems.put(col.configIdent, tableConfigUpdates);
		}
		tableConfigUpdates.put(col.dbColumnName, use);
	}

	private String formatLineNo(int no) {
		return "(" + no + ")";
	}

	protected Map<String, Map<String, Object>> getHwColumnConfig() {
		Map<String, Map<String, Object>> result = new LinkedHashMap<>();

		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = persist.getHwAuditDeviceClasses();
		int id = 0;

		for (String hwClass : hwAuditDeviceClasses.keySet()) {

			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwAuditDeviceClasses.get(hwClass);
			List<OpsiHwAuditDevicePropertyType> deviceHostProperties = hwAuditDeviceClass.getDeviceHostProperties();
			List<OpsiHwAuditDevicePropertyType> deviceHwItemProperties = hwAuditDeviceClass.getDeviceHwItemProperties();

			// hw class line
			Map<String, Object> lineMap = new LinkedHashMap<>();
			lineMap.put(colLineNo, formatLineNo(id));
			lineMap.put(colHwClass, hwClass);
			lineMap.put(colLinuxQuery, hwAuditDeviceClass.getLinuxQuery());
			lineMap.put(colWMIQuery, hwAuditDeviceClass.getWmiQuery());

			result.put(formatLineNo(id), lineMap);
			id++;

			// colHostVsItemAssigned line
			lineMap = new LinkedHashMap<>();
			lineMap.put(colLineNo, formatLineNo(id));

			lineMap.put(colHostVsItemAssigned, valAssignedToHost);

			result.put(formatLineNo(id), lineMap);
			id++;

			for (OpsiHwAuditDevicePropertyType deviceProperty : deviceHostProperties) {

				lineMap = new LinkedHashMap<>();
				lineMap.put(colLineNo, formatLineNo(id));

				ColumnIdent columnIdent = new ColumnIdent(hwClass, OpsiHwAuditDeviceClass.hostAssignedTableType,
						deviceProperty.getOpsiDbColumnName());

				lineMap.put(colOpsiColumnName, columnIdent.produceColumnCellValue());
				lineMap.put(colOpsiDbColumnType, deviceProperty.getOpsiDbColumnType());

				if (deviceProperty.getDisplayed() == null)
					lineMap.put(colUseInQuery, "" + false);
				else
					lineMap.put(colUseInQuery, "" + deviceProperty.getDisplayed());

				result.put(formatLineNo(id), lineMap);
				id++;
			}

			lineMap = new LinkedHashMap<>();
			lineMap.put(colLineNo, formatLineNo(id));
			lineMap.put(colHostVsItemAssigned, valAssignedToHwItem);

			result.put(formatLineNo(id), lineMap);

			id++;

			for (OpsiHwAuditDevicePropertyType deviceProperty : deviceHwItemProperties) {
				lineMap = new LinkedHashMap<>();
				lineMap.put(colLineNo, formatLineNo(id));

				ColumnIdent columnIdent = new ColumnIdent(hwClass, OpsiHwAuditDeviceClass.hwItemAssignedTableType,
						deviceProperty.getOpsiDbColumnName());

				lineMap.put(colOpsiColumnName, columnIdent.produceColumnCellValue());

				lineMap.put(colOpsiDbColumnType, deviceProperty.getOpsiDbColumnType());

				if (deviceProperty.getDisplayed() == null)
					lineMap.put(colUseInQuery, "" + false);
				else
					lineMap.put(colUseInQuery, "" + deviceProperty.getDisplayed());

				result.put(formatLineNo(id), lineMap);
				id++;
			}

		}

		/*
		 * for (String lineNo : result.keySet())
		 * {
		 * logging.info(this, "getHwColumnConfig " + lineNo + ": " + result.get(lineNo)
		 * );
		 * }
		 */

		return result;

	}

}
