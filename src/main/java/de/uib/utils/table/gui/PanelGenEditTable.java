/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.DefaultRowSorter;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.configed.gui.IconButton;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
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

public class PanelGenEditTable extends JPanel implements TableModelListener, ListSelectionListener, KeyListener,
		MouseListener, ComponentListener, CursorrowObserver {
	public static final int POPUP_SEPARATOR = PopupMenuTrait.POPUP_SEPARATOR; // 0
	public static final int POPUP_DELETE_ROW = 1;

	public static final int POPUP_CANCEL = 3;

	public static final int POPUP_RELOAD = PopupMenuTrait.POPUP_RELOAD; // 4

	public static final int POPUP_SORT_AGAIN = 5;

	public static final int POPUP_SAVE = PopupMenuTrait.POPUP_SAVE; // 8

	public static final int POPUP_FLOATINGCOPY = PopupMenuTrait.POPUP_FLOATINGCOPY; // 14

	public static final int POPUP_PDF = PopupMenuTrait.POPUP_PDF; // 21

	public static final int POPUP_EXPORT_CSV = PopupMenuTrait.POPUP_EXPORT_CSV; // 23
	public static final int POPUP_EXPORT_SELECTED_CSV = PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV; // 24

	public static final int POPUP_PRINT = PopupMenuTrait.POPUP_PRINT; // 30

	private static final int[] POPUPS_EXPORT = new int[] { POPUP_SEPARATOR, POPUP_EXPORT_CSV,
			POPUP_EXPORT_SELECTED_CSV, };

	private static final Map<Integer, String> keyNames = new HashMap<Integer, String>() {
		@Override
		public String put(Integer key, String value) {
			// checking that not the same int key is used twice

			if (get(key) != null) {
				Logging.error("duplicate key setting " + key + ", until now " + get(key) + " now " + value);
			}
			return super.put(key, value);
		}
	};

	private List<Integer> internalpopups;

	private JMenuItem menuItemDeleteRelation;
	private JMenuItem menuItemSave;
	private JMenuItem menuItemCancel;

	private JScrollPane scrollpane;
	protected JTable theTable;
	protected GenTableModel tableModel;

	private IconButton buttonCommit;
	private IconButton buttonCancel;
	private JLabel jLabelTitle;

	private JPopupMenu popupMenu;

	private boolean dataChanged;

	private UpdateController myController;

	private boolean editing = true;

	private boolean deleteAllowed = true;

	private boolean awareOfSelectionListener;
	private boolean awareOfTableChangedListener = true;

	private boolean withTablesearchPane;

	protected TableSearchPane searchPane;

	private boolean filteringActive;

	private String title = "";

	private int generalPopupPosition;

	private int popupIndex;

	private int oldrowcount = -1;

	private Map<Integer, SortOrder> sortDescriptor;
	private Map<Integer, SortOrder> specialSortDescriptor;

	private AbstractExportTable exportTable;

	public PanelGenEditTable(String title, boolean editing, int generalPopupPosition, int[] popupsWanted,
			boolean withTablesearchPane) {
		this.withTablesearchPane = withTablesearchPane;

		this.generalPopupPosition = generalPopupPosition;

		this.internalpopups = new ArrayList<>();
		if (popupsWanted != null) {
			for (int wantedPopup : popupsWanted) {
				this.internalpopups.add(wantedPopup);
				Logging.info(this.getClass(), "add popup " + wantedPopup);
			}
		} else {
			this.internalpopups.add(POPUP_RELOAD);

			this.internalpopups.add(POPUP_PDF);
		}

		Logging.info(this.getClass(), "internalpopups " + giveMenuitemNames(internalpopups));

		this.internalpopups = supplementBefore(POPUP_RELOAD, POPUPS_EXPORT, this.internalpopups);

		Logging.info(this.getClass(), "internalpopups supplemented " + giveMenuitemNames(internalpopups));

		if (title != null) {
			this.title = title;
		}

		this.editing = editing;

		if (!isServerFullPermission()) {
			this.editing = false;
		}

		initComponents();
	}

	public PanelGenEditTable(String title, boolean editing, int generalPopupPosition, int[] popupsWanted) {
		this(title, editing, generalPopupPosition, popupsWanted, false);
	}

	public PanelGenEditTable(String title, boolean editing, int generalPopupPosition) {
		this(title, editing, generalPopupPosition, null);
	}

	public PanelGenEditTable(String title, boolean editing) {
		this(title, editing, 0);
	}

	public PanelGenEditTable() {
		this("", true);
	}

	private static boolean isServerFullPermission() {
		if (PersistenceControllerFactory.getPersistenceController() == null) {
			return false;
		}
		return PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.hasServerFullPermissionPD();
	}

	private static final List<String> giveMenuitemNames(List<Integer> popups) {
		List<String> result = new ArrayList<>();

		for (int el : popups) {
			result.add(keyNames.get(el));
		}

		return result;
	}

	/**
	 * sets frame to return to e.g. from option dialogs
	 *
	 * @param javax.swing.JFrame
	 */
	public void setMasterFrame(JFrame masterFrame) {
		if (searchPane != null) {
			searchPane.setMasterFrame(masterFrame);
		}
	}

	@Override
	public void requestFocus() {
		if (theTable != null) {
			theTable.requestFocus();
		}

		if (withTablesearchPane) {
			searchPane.requestFocus();
		}
	}

	public void setUpdateController(UpdateController c) {
		myController = c;
	}

	public void addListSelectionListener(ListSelectionListener l) {
		getListSelectionModel().addListSelectionListener(l);
	}

	public void removeListSelectionListener(ListSelectionListener l) {
		getListSelectionModel().removeListSelectionListener(l);
	}

	private void initComponents() {
		addComponentListener(this);

		jLabelTitle = new JLabel(title);

		if (title == null || title.isEmpty()) {
			jLabelTitle.setVisible(false);
		}

		theTable = new JTable();

		exportTable = new ExporterToCSV(theTable);

		searchPane = new TableSearchPane(this, true);

		searchPane.setVisible(withTablesearchPane);

		theTable.getTableHeader().addMouseListener(this);

		// add the popup to the scrollpane for the case that the table is empty
		scrollpane = new JScrollPane();

		// NOT WORK

		theTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());

		theTable.addMouseListener(this);

		// we prefer the simple behaviour:
		theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theTable.getTableHeader().setReorderingAllowed(false);

		theTable.addKeyListener(this);

		getListSelectionModel().addListSelectionListener(this);

		theTable.setDragEnabled(true);
		theTable.setDropMode(DropMode.ON);

		theTable.setAutoCreateRowSorter(false);

		scrollpane = new JScrollPane();
		scrollpane.setViewportView(theTable);

		JPanel controlPanel = initControlPanel();

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(jLabelTitle,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE).addComponent(controlPanel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
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

		buttonCommit = new IconButton(Configed.getResourceValue("save"), "images/apply.png", "images/apply_over.png",
				"images/apply_disabled.png");

		buttonCommit.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);

		buttonCancel = new IconButton(Configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip"),
				"images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png");

		buttonCancel.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);

		buttonCommit.addActionListener(action -> commit());
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

	public void setColumnSelectionAllowed(boolean b) {
		// destroys search function

		theTable.setColumnSelectionAllowed(b);
	}

	public void setDeleteAllowed(boolean b) {
		deleteAllowed = b;
	}

	private void sortAgainAsConfigured() {
		Logging.debug(this, "sortAgainAsConfigured " + specialSortDescriptor);

		if (specialSortDescriptor != null && !specialSortDescriptor.isEmpty()) {
			sortDescriptor = specialSortDescriptor;
		}

		if (sortDescriptor != null && !sortDescriptor.isEmpty()) {
			int selRow = getSelectedRow();

			Object selVal = null;
			if (selRow > -1 && tableModel.getKeyCol() > -1) {
				selVal = tableModel.getValueAt(theTable.convertRowIndexToModel(selRow), tableModel.getKeyCol());
			}

			setSortOrder(sortDescriptor);
			((DefaultRowSorter<?, ?>) theTable.getRowSorter()).sort();
			setSorter();

			if (selVal != null) {
				int viewRow = findViewRowFromValue(selVal, tableModel.getKeyCol());
				moveToRow(viewRow);
				setSelectedRow(viewRow);
			}
		}
	}

	public void requestReload() {
		tableModel.requestReload();
	}

	/*
	 * reproduces data from source
	 * if reload is requested data are loaded completely new
	 */
	public void reset() {
		tableModel.reset();
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
		Logging.info(this, "setTitle " + title);
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
		Logging.info(this, "addPopupmenuStandardpart, internalpopups " + giveMenuitemNames(internalpopups));

		if (generalPopupPosition > 0) {
			// add separator if a real position is given
			popupMenu.addSeparator();
		}

		internalpopups = supplementBefore(POPUP_RELOAD, POPUPS_EXPORT, internalpopups);

		Logging.info(this,
				"addPopupmenuStandardpart, supplemented internalpopups " + giveMenuitemNames(internalpopups));

		for (int popuptype : internalpopups) {
			switch (popuptype) {
			case POPUP_SEPARATOR:
				addPopupItem(null);
				break;

			case POPUP_SAVE:
				menuItemSave = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.saveData"));
				menuItemSave.setEnabled(false);
				menuItemSave.addActionListener(actionEvent -> commit());
				addPopupItem(menuItemSave);
				break;

			case POPUP_CANCEL:
				menuItemCancel = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.abandonNewData"));
				menuItemCancel.setEnabled(false);
				menuItemCancel.addActionListener(actionEvent -> cancel());
				addPopupItem(menuItemCancel);
				break;

			case POPUP_RELOAD:
				addPopupItemReload();
				break;

			case POPUP_SORT_AGAIN:
				JMenuItem menuItemSortAgain = new JMenuItem(
						Configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
				menuItemSortAgain.addActionListener(actionEvent -> sortAgainAsConfigured());

				addPopupItem(menuItemSortAgain);
				break;

			case POPUP_DELETE_ROW:
				addPopupMenuDeleteRow();
				break;

			case POPUP_PRINT:
				JMenuItem menuItemPrint = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.print"));
				menuItemPrint.addActionListener((ActionEvent actionEvent) -> print());

				addPopupItem(menuItemPrint);
				break;

			case POPUP_FLOATINGCOPY:
				addPopupMenuFloatingCopy();
				break;

			case POPUP_EXPORT_CSV:
				JMenuItem menuItemExportCSV = exportTable.getMenuItemExport();
				addPopupItem(menuItemExportCSV);
				break;

			case POPUP_EXPORT_SELECTED_CSV:
				JMenuItem menuItemExportSelectedCSV = exportTable.getMenuItemExportSelected();
				addPopupItem(menuItemExportSelectedCSV);
				break;

			case POPUP_PDF:
				JMenuItem menuItemPDF = new JMenuItem(Configed.getResourceValue("FGeneralDialog.pdf"),
						Utils.createImageIcon("images/acrobat_reader16.png", ""));
				menuItemPDF.addActionListener((ActionEvent actionEvent) -> exportTable());

				addPopupItem(menuItemPDF);
				break;

			default:
				Logging.warning(this, "no case found for popuptype in addPopupmenuStandardpart");
				break;
			}
		}
	}

	private void addPopupItemReload() {
		JMenuItem menuItemReload = new JMenuItem(Configed.getResourceValue("reloadData"),
				Utils.createImageIcon("images/reload16.png", ""));

		// does not work
		menuItemReload.addActionListener(actionEvent -> reload());
		if (popupIndex > 1) {
			popupMenu.addSeparator();
		}

		addPopupItem(menuItemReload);
	}

	private void addPopupMenuFloatingCopy() {
		JMenuItem menuItemFloatingCopy = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.floatingCopy"));
		menuItemFloatingCopy.addActionListener(actionEvent -> floatExternal());

		if (popupIndex > 1) {
			popupMenu.addSeparator();
		}

		addPopupItem(menuItemFloatingCopy);
	}

	private void addPopupMenuDeleteRow() {
		menuItemDeleteRelation = new JMenuItem(Configed.getResourceValue("PanelGenEditTable.deleteRow"));
		menuItemDeleteRelation.setEnabled(false);
		menuItemDeleteRelation.addActionListener((ActionEvent actionEvent) -> deleteRelation());
		addPopupItem(menuItemDeleteRelation);
	}

	private void print() {
		try {
			theTable.print();
		} catch (PrinterException ex) {
			Logging.error("Printing error ", ex);
		}
	}

	private void deleteRelation() {
		if (getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("PanelGenEditTable.noRowSelected"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
		} else if (deleteAllowed) {
			tableModel.deleteRow(getSelectedRowInModelTerms());
		} else {
			Logging.warning(this, "nothing to delete, since nothing selected or deleting not allowed");
		}
	}

	private void exportTable() {
		Map<String, String> metaData = new HashMap<>();
		metaData.put("header", title);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "");

		ExporterToPDF pdfExportTable = new ExporterToPDF(theTable);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4Landscape();
		pdfExportTable.execute(null, false);
	}

	public void addPopupItem(JMenuItem item) {
		if (popupMenu == null) {
			// for the first item, we create the menu
			popupMenu = new JPopupMenu();
			theTable.addMouseListener(new PopupMouseListener(popupMenu));

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
		this.sortDescriptor = sortDescriptor;
	}

	private List<SortKey> buildSortkeysFromColumns() {
		Logging.debug(this, "buildSortkeysFromColumns,  sortDescriptor " + sortDescriptor);
		List<SortKey> sortKeys = new ArrayList<>();

		if (tableModel.getColumnCount() == 0) {
			return new ArrayList<>();
		} else if (sortDescriptor == null) {
			// default sorting
			sortDescriptor = new LinkedHashMap<>();

			if (tableModel.getKeyCol() > -1) {
				sortKeys.add(new SortKey(tableModel.getKeyCol(), SortOrder.ASCENDING));

				sortDescriptor.put(tableModel.getKeyCol(), SortOrder.ASCENDING);
			} else if (tableModel.getFinalCols() != null && !tableModel.getFinalCols().isEmpty()) {
				for (Integer col : tableModel.getFinalCols()) {
					sortKeys.add(new SortKey(col, SortOrder.ASCENDING));

					sortDescriptor.put(col, SortOrder.ASCENDING);
				}
			} else {
				sortKeys = null;
			}
		} else {
			for (Entry<Integer, SortOrder> entry : sortDescriptor.entrySet()) {
				sortKeys.add(new SortKey(entry.getKey(), entry.getValue()));
			}
		}

		return sortKeys;
	}

	private void setSorter() {
		Logging.info(this, "setSorter");

		if (tableModel == null) {
			return;
		}

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);

		List<SortKey> sortKeys = buildSortkeysFromColumns();

		if (sortKeys != null && !sortKeys.isEmpty()) {
			sorter.setSortKeys(sortKeys);
		}

		theTable.setRowSorter(sorter);
	}

	public void setTableModel(GenTableModel m) {
		theTable.setRowSorter(null);
		// just in case there was one

		theTable.setModel(m);
		tableModel = m;
		tableModel.addCursorrowObserver(this);

		setSorter();

		setDataChanged(false);

		setModelFilteringBySelection();
	}

	/**
	 * set special comparator for a column
	 */
	public void setComparator(String colName, Comparator<Object> comparator) {
		Logging.info(this, "setComparator " + colName + " compare by " + comparator);
		int modelCol = tableModel.getColumnNames().indexOf(colName);

		if (modelCol < 0) {
			Logging.warning(this, "invalid column name");
			return;
		}
		DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>) theTable.getRowSorter();
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

		searchPane.setSearchFields(cols);
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

		searchPane.setSearchFieldsAll();
	}

	/**
	 * set search mode possible values TablesearchPane.FULL_TEXT_SEARCH
	 * TablesearchPane.START_TEXT_SEARCH = 1; TablesearchPane.REGEX_SEARCH
	 */
	public void setSearchMode(TableSearchPane.SearchMode mode) {
		searchPane.setSearchMode(mode);
	}

	/**
	 * sets a filter symbol belonging to searchPane
	 *
	 * @parameter boolean
	 */
	public void showFilterIcon(boolean b) {
		searchPane.showFilterIcon(b);
	}

	private void setFilteringActive(boolean b) {
		filteringActive = b;
	}

	private void setModelFilteringBySelection() {
		if (filteringActive && tableModel != null
				&& tableModel.getFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION) == null) {
			RowNoTableModelFilterCondition filterBySelectionCondition = new RowNoTableModelFilterCondition();
			TableModelFilter filterBySelection = new TableModelFilter(filterBySelectionCondition, false, false);

			tableModel.chainFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION, filterBySelection);
		}
	}

	/**
	 * activates popupMark or this as well as popupMarkAndFilter in context menu
	 *
	 * @parameter boolean
	 */
	public void setFiltering(boolean filtering) {
		if (filtering) {
			setFilteringActive(filtering);
			// lazy activation
		}
		searchPane.setFiltering(filtering);
	}

	/**
	 * sets an alternative ActionListener
	 *
	 * @parameter ActionListener
	 */
	public void setFiltermarkActionListener(ActionListener li) {
		searchPane.setFiltermarkActionListener(li);
	}

	/**
	 * sets the filter symbol to filtered/not filtered @ parameter boolean
	 */
	public void showFiltered(boolean b) {
		searchPane.setFilterMark(b);
	}

	/**
	 * set if a search results in a new selection
	 */
	public void setSearchSelectMode(boolean select) {
		searchPane.setSelectMode(select);
	}

	public void setDataChanged(boolean b) {
		if (!editing) {
			return;
		}

		Logging.info(this, "setDataChanged " + b);
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
		Logging.info(this, "isDataChanged " + dataChanged);
		return dataChanged;
	}

	public void stopCellEditing() {
		if (theTable.getCellEditor() != null) {
			// we are editing
			Logging.info(this, "we are editing a cell");
			theTable.getCellEditor().stopCellEditing();
		} else {
			Logging.info(this, "no cell editing");
		}
	}

	public void commit() {
		stopCellEditing();

		if (myController == null) {
			return;
		}

		if (myController.saveChanges()) {
			setDataChanged(false);
		}
	}

	public void cancel() {
		if (myController == null) {
			return;
		}

		if (myController.cancelChanges()) {
			setDataChanged(false);
		}
	}

	private void deleteCurrentRow() {
		if (!deleteAllowed) {
			return;
		}

		if (getSelectedRowCount() > 0) {
			tableModel.deleteRow(getSelectedRowInModelTerms());
		}
	}

	public JTable getTheTable() {
		return theTable;
	}

	public GenTableModel getTableModel() {
		return tableModel;
	}

	public TableColumnModel getColumnModel() {
		return theTable.getColumnModel();
	}

	public ListSelectionModel getListSelectionModel() {
		return theTable.getSelectionModel();
	}

	public TableSearchPane getTheSearchpane() {
		return searchPane;
	}

	/**
	 * set the selection model for the table conceived as a list the usage of
	 * any other model than the default ListSelectionModel.SINGLE_SELECTION may
	 * be not fully supported
	 */
	public void setListSelectionMode(int selectionMode) {
		theTable.setSelectionMode(selectionMode);
	}

	public int getSelectedRowCount() {
		return theTable.getSelectedRowCount();
	}

	public int getSelectedRow() {
		return theTable.getSelectedRow();
	}

	public void setSelectedRow(int row) {
		theTable.setRowSelectionInterval(row, row);

		showSelectedRow();
	}

	public void setSelection(int[] selection) {
		Logging.info(this, "setSelection --- " + Arrays.toString(selection));
		theTable.getSelectionModel().clearSelection();
		for (int i = 0; i < selection.length; i++) {
			theTable.getSelectionModel().addSelectionInterval(selection[i], selection[i]);
		}
	}

	public void showSelectedRow() {
		int row = getSelectedRow();
		if (row != -1) {
			theTable.scrollRectToVisible(theTable.getCellRect(row, 0, false));
		}
	}

	public int getSelectedRowInModelTerms() {
		return theTable.convertRowIndexToModel(theTable.getSelectedRow());
	}

	public void setValueAt(Object value, int row, int col) {
		tableModel.setValueAt(value, theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
	}

	public Object getValueAt(int row, int col) {
		return tableModel.getValueAt(theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
	}

	public void setAwareOfSelectionListener(boolean b) {
		Logging.debug(this, "setAwareOfSelectionListener  " + b);

		awareOfSelectionListener = b;
	}

	public boolean isAwareOfSelectionListener() {
		return awareOfSelectionListener;
	}

	public void setAwareOfTableChangedListener(boolean b) {
		Logging.debug(this, "setAwareOfTableChangedListener  " + b);

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
				result.add(
						tableModel.getValueAt(theTable.convertRowIndexToModel(i), tableModel.getKeyCol()).toString());
			}
		} else {
			for (int i = 0; i < theTable.getSelectedRowCount(); i++) {
				result.add(tableModel.getValueAt(theTable.convertRowIndexToModel(theTable.getSelectedRows()[i]),
						tableModel.getKeyCol()).toString());
			}
		}

		return result;
	}

	public void setSelectedValues(List<String> values, int col) {
		getListSelectionModel().clearSelection();

		if (values == null || values.isEmpty()) {
			return;
		}

		setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		Iterator<String> iter = values.iterator();

		while (iter.hasNext()) {
			int viewRow = findViewRowFromValue(iter.next(), col);

			getListSelectionModel().addSelectionInterval(viewRow, viewRow);
		}
	}

	public int findViewRowFromValue(Object value, int col) {
		Logging.debug(this, "findViewRowFromValue value, col " + value + ", " + col);

		if (value == null) {
			return -1;
		}

		String val = value.toString();

		for (int viewrow = 0; viewrow < tableModel.getRowCount(); viewrow++) {
			Object compareValue = tableModel.getValueAt(theTable.convertRowIndexToModel(viewrow), col);

			if ((compareValue == null && val.isEmpty())
					|| (compareValue != null && val.equals(compareValue.toString()))) {
				return viewrow;
			}
		}

		return -1;
	}

	public boolean moveToValue(String value, int col) {
		return moveToValue(value, col, true);
	}

	public boolean moveToValue(String value, int col, boolean selecting) {
		Logging.info(this, "moveToValue " + value + " col " + col + " selecting " + selecting);
		int viewrow = findViewRowFromValue(value, col);
		if (viewrow > -1) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(viewrow));
		}

		theTable.scrollRectToVisible(theTable.getCellRect(viewrow, col, false));

		if (viewrow == -1) {
			return false;
		}

		if (selecting) {
			setSelectedRow(viewrow);
		}

		return true;
	}

	public boolean moveToKeyValue(String keyValue) {
		if (keyValue == null) {
			return false;
		}

		boolean found = false;

		if (tableModel.getKeyCol() > -1) {
			found = moveToValue(keyValue, tableModel.getKeyCol());
		} else {
			// try to use pseudokey
			int viewrow = 0;

			while (viewrow < tableModel.getRowCount()) {
				String[] partialkeys = new String[tableModel.getFinalCols().size()];

				for (int j = 0; j < tableModel.getFinalCols().size(); j++) {
					partialkeys[j] = tableModel
							.getValueAt(theTable.convertRowIndexToModel(viewrow), tableModel.getFinalCols().get(j))
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
				found = moveToValue(keyValue, 0);
			}
		}

		return found;
	}

	public void moveToRow(int n) {
		if (tableModel.getRowCount() == 0) {
			return;
		}

		if (getSelectedRowCount() != 1) {
			return;
		}

		if (n < 0 || n >= theTable.getRowCount()) {
			return;
		}

		theTable.scrollRectToVisible(theTable.getCellRect(n, 0, true));
		theTable.setRowSelectionInterval(n, n);
		tableModel.setCursorRow(theTable.convertRowIndexToModel(n));
	}

	public boolean setCursorToFirstRow() {
		if (tableModel.getRowCount() > 0) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(0));
			theTable.scrollRectToVisible(theTable.getCellRect(0, 0, true));
		}

		return true;
	}

	public boolean setCursorToLastRow() {
		if (tableModel.getRowCount() > 0) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(tableModel.getRowCount() - 1));
			theTable.scrollRectToVisible(theTable.getCellRect(tableModel.getRowCount() - 1, 0, true));
		}
		return true;
	}

	public boolean advanceCursor(int d) {
		int viewCursorRow = -1;
		if (tableModel.getCursorRow() > -1) {
			viewCursorRow = theTable.convertRowIndexToView(tableModel.getCursorRow());
		}

		Logging.info(this, "advanceCursor from " + viewCursorRow);
		int nextViewCursorRow = viewCursorRow + d;
		Logging.info(this, "advanceCursor to " + nextViewCursorRow);
		if (nextViewCursorRow < tableModel.getRowCount() && nextViewCursorRow >= 0) {
			tableModel.setCursorRow(theTable.convertRowIndexToModel(nextViewCursorRow));
		}

		theTable.scrollRectToVisible(theTable.getCellRect(nextViewCursorRow, 0, true));

		return true;
	}

	public void moveToLastRow() {
		moveToRow(tableModel.getRowCount() - 1);
	}

	// TableModelListener
	@Override
	public void tableChanged(TableModelEvent e) {
		Logging.debug(this, " tableChanged " + "source " + e.getSource() + " col " + e.getColumn());
		if (tableModel != null) {
			Logging.debug(this,
					"tableChanged,  whereas tableModel.getColMarkCursorRow() is " + tableModel.getColMarkCursorRow());
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

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == theTable && e.getKeyCode() == KeyEvent.VK_DELETE) {
			deleteCurrentRow();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	//
	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.debug(this, "ListSelectionEvent " + e);
		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		Logging.debug(this, "ListSelectionEvent not more adjusting");

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (awareOfSelectionListener) {
			setDataChanged(true);
		}

		Logging.info(this, "rows selected: " + lsm.getSelectedItemsCount());

		if (menuItemDeleteRelation != null) {
			menuItemDeleteRelation.setEnabled(!lsm.isSelectionEmpty());
		}
	}

	// MouseListener, hook for subclasses
	@Override
	public void mouseClicked(MouseEvent e) {
		/* For implementation in subclass */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* For implementation in subclass */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* For implementation in subclass */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* For implementation in subclass */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* For implementation in subclass */}

	// ComponentListener for table

	@Override
	public void componentResized(ComponentEvent e) {
		showSelectedRow();
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
		Logging.info(this, " in PanelGenEditTable rowUpdated to modelrow " + modelrow);
	}

	private void floatExternal() {
		PanelGenEditTable copyOfMe;
		GeneralFrame externalView;

		copyOfMe = new PanelGenEditTable(title, false);

		copyOfMe.setTableModel(tableModel);

		externalView = new GeneralFrame(null, "hallo", false);
		externalView.addPanel(copyOfMe);
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(ConfigedMain.getMainFrame());

		externalView.setVisible(true);
	}
}
