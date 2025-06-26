/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.AbstractExportTable;
import de.uib.utils.table.CursorrowObserver;
import de.uib.utils.table.ExporterToCSV;
import de.uib.utils.table.ExporterToPDF;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.RowNoTableModelFilterCondition;
import de.uib.utils.table.TableModelFilter;
import de.uib.utils.table.updates.UpdateController;

public class PanelGenEdit extends JPanel
		implements TableModelListener, ListSelectionListener, ComponentListener, CursorrowObserver {
	public static final int POPUP_DELETE_ROW = 1;

	public static final int POPUP_CANCEL = 3;

	public static final int POPUP_SORT_AGAIN = 5;

	private static final int[] POPUPS_EXPORT = new int[] { PopupMenuTrait.POPUP_SEPARATOR,
			PopupMenuTrait.POPUP_EXPORT_CSV, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV };

	private static final Map<Integer, String> keyNames = new HashMap<Integer, String>() {
		@Override
		public String put(Integer key, String value) {
			// checking that not the same int key is used twice

			if (get(key) != null) {
				Logging.error("duplicate key setting ", key, ", until now ", get(key), " now ", value);
			}
			return super.put(key, value);
		}
	};

	private List<Integer> internalpopups;

	private JMenuItem menuItemDeleteRelation;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemCancel;

	private JScrollPane scrollpane;
	protected GenEditTable genEditTable;
	protected GenTableModel tableModel;

	private JButton buttonCommit;
	private JButton buttonCancel;
	private JLabel jLabelTitle;

	private JPopupMenu popupMenu;

	private boolean dataChanged;

	private UpdateController updateController;

	private boolean editing = true;

	private boolean awareOfSelectionListener;
	private boolean awareOfTableChangedListener = true;

	private boolean withTablesearchPane;

	protected TableSearchPane tableSearchPane;

	private String title = "";

	private int generalPopupPosition;

	private int popupIndex;

	private int oldrowcount = -1;

	private AbstractExportTable exportTable;

	private Map<Integer, Runnable> popupCreators = Map.of(PopupMenuTrait.POPUP_SEPARATOR, () -> addPopupItem(null),
			PopupMenuTrait.POPUP_SAVE, () -> {
				menuItemSave = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.saveData"));
				menuItemSave.setEnabled(false);
				menuItemSave.addActionListener(actionEvent -> commit());
				addPopupItem(menuItemSave);
			}, POPUP_CANCEL, () -> {
				menuItemCancel = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.abandonNewData"));
				menuItemCancel.setEnabled(false);
				menuItemCancel.addActionListener(actionEvent -> cancel());
				addPopupItem(menuItemCancel);
			}, PopupMenuTrait.POPUP_RELOAD, () -> addPopupItemReload(), POPUP_SORT_AGAIN, () -> {
				JMenuItem menuItemSortAgain = new JMenuItem(
						Configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
				menuItemSortAgain.addActionListener(actionEvent -> genEditTable.sortAgainAsConfigured());

				addPopupItem(menuItemSortAgain);
			}, POPUP_DELETE_ROW, () -> addPopupMenuDeleteRow(), PopupMenuTrait.POPUP_PRINT, () -> {
				JMenuItem menuItemPrint = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.print"));
				Icons.addIntellijIconToMenuItem(menuItemPrint, "print");
				menuItemPrint.addActionListener(actionEvent -> print());

				addPopupItem(menuItemPrint);
			}, PopupMenuTrait.POPUP_EXPORT_CSV, () -> {
				JMenuItem menuItemExportCSV = exportTable.getMenuItemExport();
				addPopupItem(menuItemExportCSV);
			}, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV, () -> {
				JMenuItem menuItemExportSelectedCSV = exportTable.getMenuItemExportSelected();
				addPopupItem(menuItemExportSelectedCSV);
			}, PopupMenuTrait.POPUP_PDF, () -> {
				JMenuItem menuItemPDF = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"));
				Icons.addThemeIconInvertedToMenuItem(menuItemPDF, "anyType");
				menuItemPDF.addActionListener(actionEvent -> exportTable());

				addPopupItem(menuItemPDF);
			});

	private FilterKey filterKey;

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition, int[] popupsWanted,
			boolean withTablesearchPane) {
		this.withTablesearchPane = withTablesearchPane;

		this.generalPopupPosition = generalPopupPosition;

		this.internalpopups = new ArrayList<>();
		if (popupsWanted != null) {
			for (int wantedPopup : popupsWanted) {
				this.internalpopups.add(wantedPopup);
				Logging.info(this, "add popup ", wantedPopup);
			}
		} else {
			this.internalpopups.add(PopupMenuTrait.POPUP_RELOAD);

			this.internalpopups.add(PopupMenuTrait.POPUP_PDF);
		}

		Logging.info(this, "internalpopups ", giveMenuitemNames(internalpopups));

		this.internalpopups = supplementBefore(PopupMenuTrait.POPUP_RELOAD, POPUPS_EXPORT, this.internalpopups);

		Logging.info(this, "internalpopups supplemented ", giveMenuitemNames(internalpopups));

		if (title != null) {
			this.title = title;
		}

		this.editing = editing;

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.hasServerFullPermissionPD()) {
			this.editing = false;
		}

		initComponents();
	}

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition, int[] popupsWanted) {
		this(title, editing, generalPopupPosition, popupsWanted, false);
	}

	public PanelGenEdit(String title, boolean editing, int generalPopupPosition) {
		this(title, editing, generalPopupPosition, null);
	}

	public void setFilterKey(FilterKey filterKey) {
		this.filterKey = filterKey;
		tableSearchPane.setFilterKey(filterKey);
	}

	private static final List<String> giveMenuitemNames(List<Integer> popups) {
		List<String> result = new ArrayList<>();

		for (int el : popups) {
			result.add(keyNames.get(el));
		}

		return result;
	}

	@Override
	public void requestFocus() {
		genEditTable.requestFocus();

		if (withTablesearchPane) {
			tableSearchPane.requestFocus();
		}
	}

	public void setUpdateController(UpdateController updateController) {
		this.updateController = updateController;
	}

	public void addListSelectionListener(ListSelectionListener l) {
		genEditTable.getSelectionModel().addListSelectionListener(l);
	}

	private void initComponents() {
		addComponentListener(this);

		jLabelTitle = new JLabel(title);

		if (title == null || title.isEmpty()) {
			jLabelTitle.setVisible(false);
		}

		genEditTable = new GenEditTable();

		exportTable = new ExporterToCSV(genEditTable);

		tableSearchPane = new TableSearchPane(this);
		tableSearchPane.setVisible(withTablesearchPane);

		// add the popup to the scrollpane for the case that the table is empty
		scrollpane = new JScrollPane();

		// we prefer the simple behaviour:

		genEditTable.getSelectionModel().addListSelectionListener(this);

		scrollpane = new JScrollPane();
		scrollpane.setViewportView(genEditTable);

		JPanel controlPanel = initControlPanel();

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(jLabelTitle,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(tableSearchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE).addComponent(controlPanel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(tableSearchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(scrollpane, 20, 100, Short.MAX_VALUE)

				.addComponent(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		if (generalPopupPosition == 0) {
			// if -1 dont use a standard popup
			// if > 0 the popup is added later after installing another popup
			addPopupmenuStandardpart();
		}
	}

	private JPanel initControlPanel() {
		JPanel controlPanel = new JPanel();

		if (!editing) {
			controlPanel.setVisible(false);
			return controlPanel;
		}

		buttonCommit = new JButton(Icons.getIntellijIcon("checkmark"));
		buttonCommit.setToolTipText(Configed.getResourceValue("save"));
		buttonCommit.addActionListener(action -> commit());

		buttonCancel = new JButton(Icons.getIntellijIcon("close"));
		buttonCancel.setToolTipText(Configed.getResourceValue("buttonClose"));
		buttonCancel.addActionListener(action -> cancel());

		GroupLayout layout = new GroupLayout(controlPanel);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		setDataChanged(false);

		return controlPanel;
	}

	public void setDeleteAllowed(boolean deleteAllowed) {
		genEditTable.setDeleteAllowed(deleteAllowed);
	}

	public void reload() {
		getParent().setCursor(Globals.WAIT_CURSOR);

		Logging.info(this, "in PanelGenEditTable reload()");
		tableModel.requestReload();
		tableModel.reset();
		setDataChanged(false);

		getParent().setCursor(null);
	}

	public void setTitle(String title) {
		Logging.info(this, "setTitle ", title);
		this.title = title;
		jLabelTitle.setText(title);
	}

	private static List<Integer> supplementBefore(int insertpoint, final int[] injectKeys,
			final List<Integer> listOfKeys) {
		List<Integer> augmentedList = new ArrayList<>();

		boolean found = false;

		Set<Integer> setOfKeys = new HashSet<>();

		for (int key : listOfKeys) {
			if (key == insertpoint) {
				found = true;
				addMissingKeys(injectKeys, setOfKeys, augmentedList);
			}

			augmentedList.add(key);
			setOfKeys.add(key);
		}

		if (!found) {
			addMissingKeys(injectKeys, setOfKeys, augmentedList);
		}

		return augmentedList;
	}

	private static void addMissingKeys(int[] injectKeys, Set<Integer> setOfKeys, List<Integer> augmentedList) {
		for (int type : injectKeys) {
			if (!setOfKeys.contains(type)) {
				augmentedList.add(type);
				setOfKeys.add(type);
			}
		}
	}

	private void addPopupmenuStandardpart() {
		Logging.info(this, "addPopupmenuStandardpart, internalpopups ", giveMenuitemNames(internalpopups));

		if (generalPopupPosition > 0) {
			// add separator if a real position is given
			popupMenu.addSeparator();
		}

		internalpopups = supplementBefore(PopupMenuTrait.POPUP_RELOAD, POPUPS_EXPORT, internalpopups);

		Logging.info(this, "addPopupmenuStandardpart, supplemented internalpopups ", giveMenuitemNames(internalpopups));

		for (int popuptype : internalpopups) {
			Runnable popupCreator = popupCreators.get(popuptype);
			if (popupCreator == null) {
				Logging.warning(this, "no popup factory found for popuptype ", popuptype);
				continue;
			}

			popupCreator.run();
		}
	}

	private void addPopupItemReload() {
		JMenuItem menuItemReload = new JMenuItem(Configed.getResourceValue("reloadData"));
		Icons.addIntellijIconToMenuItem(menuItemReload, "refresh");

		// does not work
		menuItemReload.addActionListener(actionEvent -> reload());
		if (popupIndex > 1) {
			popupMenu.addSeparator();
		}

		addPopupItem(menuItemReload);
	}

	private void addPopupMenuDeleteRow() {
		menuItemDeleteRelation = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.deleteRow"));
		menuItemDeleteRelation.setEnabled(false);
		menuItemDeleteRelation.addActionListener(actionEvent -> genEditTable.deleteRelation());
		addPopupItem(menuItemDeleteRelation);
	}

	private void print() {
		try {
			genEditTable.print();
		} catch (PrinterException ex) {
			Logging.error(ex, "Printing error ");
		}
	}

	private void exportTable() {
		Map<String, String> metaData = new HashMap<>();
		metaData.put("header", title);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "");

		ExporterToPDF pdfExportTable = new ExporterToPDF(genEditTable);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	public void addPopupItem(JMenuItem item) {
		if (popupMenu == null) {
			// for the first item, we create the menu
			popupMenu = new JPopupMenu();
			genEditTable.addMouseListener(new PopupMouseListener(popupMenu));

			// add the popup to the scrollpane if the table is empty
			scrollpane.addMouseListener(new PopupMouseListener(popupMenu));
		}

		if (item == null) {
			if (popupIndex > 1) {
				popupMenu.addSeparator();
			}

			return;
		}

		popupMenu.add(item);

		// prevents circle
		popupIndex++;
		if (popupIndex == generalPopupPosition) {
			addPopupmenuStandardpart();
		}
	}

	public JScrollPane getTheScrollpane() {
		return scrollpane;
	}

	public void setSortOrder(Map<Integer, SortOrder> sortDescriptor) {
		genEditTable.setSortDescriptor(sortDescriptor);
	}

	public void setTableModel(GenTableModel m) {
		genEditTable.setRowSorter(null);
		// just in case there was one

		genEditTable.setModel(m);
		tableModel = m;
		tableModel.addCursorrowObserver(this);

		genEditTable.setSorter();

		setDataChanged(false);

		setModelFilteringBySelection();
	}

	/**
	 * set special comparator for a column
	 */
	public void setComparator(String colName, Comparator<Object> comparator) {
		Logging.info(this, "setComparator ", colName, " compare by ", comparator);
		int modelCol = tableModel.getColumnNames().indexOf(colName);

		if (modelCol < 0) {
			Logging.warning(this, "invalid column name");
			return;
		}
		DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>) genEditTable.getRowSorter();
		if (sorter == null) {
			Logging.warning(this, "no sorter");
		} else {
			sorter.setComparator(modelCol, comparator);
		}
	}

	/**
	 * set columns for which the searchpane shall work
	 */
	public void setSearchColumns(Integer[] cols) {
		if (!withTablesearchPane) {
			Logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		tableSearchPane.setSearchFields(cols);
	}

	/**
	 * set all columns for column selection in search pane; requires the correct
	 * model is initialized
	 */
	public void setSearchColumnsAll() {
		if (!withTablesearchPane) {
			Logging.debug(this, "setSearchColumns: no search panel");
			return;
		}

		tableSearchPane.setSearchFieldsAll();
	}

	private void setModelFilteringBySelection() {
		if (tableSearchPane.isFiltering() && tableModel != null
				&& tableModel.getFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION) == null) {
			RowNoTableModelFilterCondition filterBySelectionCondition = new RowNoTableModelFilterCondition();
			TableModelFilter filterBySelection = new TableModelFilter(filterBySelectionCondition, false, false);

			tableModel.chainFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION, filterBySelection);
		}
	}

	public void setDataChanged(boolean b) {
		if (!editing) {
			return;
		}

		Logging.info(this, "setDataChanged ", b);
		dataChanged = b;
		buttonCommit.setEnabled(b);
		if (menuItemSave != null) {
			menuItemSave.setEnabled(b);
		}

		buttonCancel.setEnabled(b);
		if (menuItemCancel != null) {
			menuItemCancel.setEnabled(b);
		}
	}

	public boolean isDataChanged() {
		Logging.info(this, "isDataChanged ", dataChanged);
		return dataChanged;
	}

	public void stopCellEditing() {
		if (genEditTable.getCellEditor() != null) {
			// we are editing
			Logging.info(this, "we are editing a cell");
			genEditTable.getCellEditor().stopCellEditing();
		} else {
			Logging.info(this, "no cell editing");
		}
	}

	public void commit() {
		stopCellEditing();

		if (updateController == null) {
			return;
		}

		if (updateController.saveChanges()) {
			setDataChanged(false);
		}
	}

	public void cancel() {
		if (updateController == null) {
			return;
		}

		if (updateController.cancelChanges()) {
			setDataChanged(false);
		}
	}

	public JTable getJTable() {
		return genEditTable;
	}

	public GenTableModel getTableModel() {
		return tableModel;
	}

	public TableSearchPane getTableSearchPane() {
		return tableSearchPane;
	}

	public void setSelectedRow(int row) {
		genEditTable.setSelectedRow(row);
	}

	public void setSelection(int[] selection) {
		Logging.info(this, "setSelection --- ", Arrays.toString(selection));
		genEditTable.getSelectionModel().clearSelection();
		for (int i = 0; i < selection.length; i++) {
			genEditTable.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}

	public int getSelectedRowInModelTerms() {
		return genEditTable.getSelectedRowInModelTerms();
	}

	public void setValueAt(Object value, int row, int col) {
		tableModel.setValueAt(value, genEditTable.convertRowIndexToModel(row),
				genEditTable.convertColumnIndexToModel(col));
	}

	public Object getValueAt(int row, int col) {
		return tableModel.getValueAt(genEditTable.convertRowIndexToModel(row),
				genEditTable.convertColumnIndexToModel(col));
	}

	public void setAwareOfSelectionListener(boolean awareOfSelectionListener) {
		Logging.debug(this, "setAwareOfSelectionListener  ", awareOfSelectionListener);

		this.awareOfSelectionListener = awareOfSelectionListener;
	}

	public boolean isAwareOfSelectionListener() {
		return awareOfSelectionListener;
	}

	public void setAwareOfTableChangedListener(boolean b) {
		Logging.debug(this, "setAwareOfTableChangedListener  ", b);

		awareOfTableChangedListener = b;
	}

	public boolean isAwareOfTableChangedListener() {
		return awareOfTableChangedListener;
	}

	public List<String> getSelectedKeys() {
		List<String> result = new ArrayList<>();

		if (tableModel.getKeyCol() < 0) {
			return result;
		}

		if (tableModel.isUsingFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION)) {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				result.add(tableModel.getValueAt(genEditTable.convertRowIndexToModel(i), tableModel.getKeyCol())
						.toString());
			}
		} else {
			for (int i = 0; i < genEditTable.getSelectedRowCount(); i++) {
				result.add(tableModel.getValueAt(genEditTable.convertRowIndexToModel(genEditTable.getSelectedRows()[i]),
						tableModel.getKeyCol()).toString());
			}
		}

		return result;
	}

	public void setSelectedValues(List<String> values, int col) {
		genEditTable.clearSelection();

		if (values == null || values.isEmpty()) {
			return;
		}

		genEditTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		Iterator<String> iter = values.iterator();

		while (iter.hasNext()) {
			int viewRow = genEditTable.findViewRowFromValue(iter.next(), col);

			genEditTable.getSelectionModel().addSelectionInterval(viewRow, viewRow);
		}
	}

	public int findViewRowFromValue(Object value, int col) {
		return genEditTable.findViewRowFromValue(value, col);
	}

	public boolean moveToValue(String value, int col) {
		return moveToValue(value, col, true);
	}

	public boolean moveToValue(String value, int col, boolean selecting) {
		Logging.info(this, "moveToValue ", value, " col ", col, " selecting ", selecting);
		int viewrow = genEditTable.findViewRowFromValue(value, col);
		if (viewrow > -1) {
			tableModel.setCursorRow(genEditTable.convertRowIndexToModel(viewrow));
		}

		genEditTable.scrollRectToVisible(genEditTable.getCellRect(viewrow, col, false));

		if (viewrow == -1) {
			return false;
		}

		if (selecting) {
			setSelectedRow(viewrow);
		}

		return true;
	}

	public void moveToKeyValue(String keyValue) {
		if (keyValue == null) {
			return;
		}

		if (tableModel.getKeyCol() > -1) {
			moveToValue(keyValue, tableModel.getKeyCol());
		} else {
			boolean found = false;

			// try to use pseudokey
			int viewrow = 0;

			while (viewrow < tableModel.getRowCount()) {
				String[] partialkeys = new String[tableModel.getFinalCols().size()];

				for (int j = 0; j < tableModel.getFinalCols().size(); j++) {
					partialkeys[j] = tableModel
							.getValueAt(genEditTable.convertRowIndexToModel(viewrow), tableModel.getFinalCols().get(j))
							.toString();
				}

				if (keyValue.equals(Utils.pseudokey(partialkeys))) {
					found = true;
					break;
				} else {
					viewrow++;
				}
			}

			if (found) {
				setSelectedRow(viewrow);
			} else {
				// try value for col 0 as target for search
				moveToValue(keyValue, 0);
			}
		}
	}

	public boolean setCursorToFirstRow() {
		if (tableModel.getRowCount() > 0) {
			tableModel.setCursorRow(genEditTable.convertRowIndexToModel(0));
			genEditTable.scrollRectToVisible(genEditTable.getCellRect(0, 0, true));
		}

		return true;
	}

	public boolean setCursorToLastRow() {
		if (tableModel.getRowCount() > 0) {
			tableModel.setCursorRow(genEditTable.convertRowIndexToModel(tableModel.getRowCount() - 1));
			genEditTable.scrollRectToVisible(genEditTable.getCellRect(tableModel.getRowCount() - 1, 0, true));
		}
		return true;
	}

	public boolean advanceCursor(int d) {
		int viewCursorRow = -1;
		if (tableModel.getCursorRow() > -1) {
			viewCursorRow = genEditTable.convertRowIndexToView(tableModel.getCursorRow());
		}

		Logging.info(this, "advanceCursor from ", viewCursorRow);
		int nextViewCursorRow = viewCursorRow + d;
		Logging.info(this, "advanceCursor to ", nextViewCursorRow);
		if (nextViewCursorRow < tableModel.getRowCount() && nextViewCursorRow >= 0) {
			tableModel.setCursorRow(genEditTable.convertRowIndexToModel(nextViewCursorRow));
		}

		genEditTable.scrollRectToVisible(genEditTable.getCellRect(nextViewCursorRow, 0, true));

		return true;
	}

	public void moveToLastRow() {
		genEditTable.moveToRow(tableModel.getRowCount() - 1);
	}

	public void restoreFilter() {
		if (filterKey == null) {
			Logging.warning(this, "Filter key is null");
			return;
		}
		tableSearchPane.restoreFilter();
	}

	// TableModelListener
	@Override
	public void tableChanged(TableModelEvent e) {
		Logging.debug(this, " tableChanged ", "source ", e.getSource(), " col ", e.getColumn());
		if (tableModel != null) {
			Logging.debug(this, "tableChanged,  whereas tableModel.getColMarkCursorRow() is ",
					tableModel.getColMarkCursorRow());
		}

		if (awareOfTableChangedListener && tableModel != null
				&& !(tableModel.getColMarkCursorRow() > -1 && e.getColumn() == tableModel.getColMarkCursorRow())) {
			Logging.info(this, " tableChanged, datachanged set to true");
			setDataChanged(true);
			if (tableModel != null && oldrowcount != tableModel.getRowCount()) {
				oldrowcount = tableModel.getRowCount();
			}
		}
	}

	//
	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.debug(this, "ListSelectionEvent ", e);
		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		Logging.debug(this, "ListSelectionEvent not more adjusting");

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (awareOfSelectionListener) {
			setDataChanged(true);
		}

		Logging.info(this, "rows selected: ", lsm.getSelectedItemsCount());

		if (menuItemDeleteRelation != null) {
			menuItemDeleteRelation.setEnabled(!lsm.isSelectionEmpty());
		}
	}

	// ComponentListener for table

	@Override
	public void componentResized(ComponentEvent e) {
		genEditTable.showSelectedRow();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentMoved(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentShown(ComponentEvent e) {
		/* Not needed */}

	// CursorrowObserver
	@Override
	public void rowUpdated(int modelrow) {
		Logging.info(this, " in PanelGenEditTable rowUpdated to modelrow ", modelrow);
	}
}
