/*
 * PanelGenEditTable.java
 *
 * By uib, www.uib.de, 2008-2017,2020-2021
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import java.awt.Color;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.utilities.Mapping;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.PanelLinedComponents;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.AbstractExportTable;
import de.uib.utilities.table.CursorrowObserver;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.RowNoTableModelFilterCondition;
import de.uib.utilities.table.TableCellRendererByBoolean;
import de.uib.utilities.table.TableCellRendererCurrency;
import de.uib.utilities.table.TableCellRendererDate;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.updates.UpdateController;
import de.uib.utilities.thread.WaitCursor;

public class PanelGenEditTable extends JPanel implements ActionListener, TableModelListener, ListSelectionListener,
		KeyListener, MouseListener, ComponentListener, CursorrowObserver {

	public static final int POPUP_SEPARATOR = PopupMenuTrait.POPUP_SEPARATOR; // 0
	public static final int POPUP_DELETE_ROW = 1;

	public static final int POPUP_CANCEL = 3;

	public static final int POPUP_RELOAD = PopupMenuTrait.POPUP_RELOAD; // 4

	public static final int POPUP_SORT_AGAIN = 5;

	public static final int POPUP_SAVE = PopupMenuTrait.POPUP_SAVE; // 8

	public static final int POPUP_NEW_ROW = 11;
	public static final int POPUP_COPY_ROW = 12;
	public static final int POPUP_FLOATINGCOPY = PopupMenuTrait.POPUP_FLOATINGCOPY; // 14

	public static final int POPUP_PDF = PopupMenuTrait.POPUP_PDF; // 21

	public static final int POPUP_EXPORT_CSV = PopupMenuTrait.POPUP_EXPORT_CSV; // 23
	public static final int POPUP_EXPORT_SELECTED_CSV = PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV; // 24

	public static final int POPUP_PRINT = PopupMenuTrait.POPUP_PRINT; // 30

	public static final int[] POPUPS_NOT_EDITABLE_TABLE_PDF = new int[] { POPUP_RELOAD, POPUP_PDF, POPUP_SORT_AGAIN };
	protected static final int[] POPUPS_MINIMAL = new int[] { POPUP_RELOAD, POPUP_SORT_AGAIN

	};

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

	protected List<Integer> internalpopups;

	protected List<JMenuItem> menuItemsRequesting1SelectedLine;
	protected List<JMenuItem> menuItemsRequestingMultiSelectedLines;

	JMenuItemFormatted menuItemDeleteRelation;
	JMenuItemFormatted menuItemSave;
	JMenuItemFormatted menuItemCancel;
	JMenuItemFormatted menuItemReload;
	JMenuItemFormatted menuItemSortAgain;
	JMenuItemFormatted menuItemPrint;
	JMenuItemFormatted menuItemExportExcel;
	JMenuItemFormatted menuItemExportSelectedExcel;
	JMenuItemFormatted menuItemExportCSV;
	JMenuItemFormatted menuItemExportSelectedCSV;
	JMenuItemFormatted menuItemNewRow;
	JMenuItemFormatted menuItemCopyRelation;
	JMenuItemFormatted menuItemFloatingCopy;
	JMenuItemFormatted menuItemPDF;

	JFrame masterFrame = ConfigedMain.getMainFrame();

	protected Comparator[] comparators;

	protected JScrollPane scrollpane;
	protected JTable theTable;
	protected GenTableModel tableModel;

	protected IconButton buttonCommit;
	protected IconButton buttonCancel;
	protected JLabel label;

	protected JLabel labelRowCount;
	protected JLabel labelMarkedCount;
	protected String textMarkedCount = "selected";
	protected JPanel titlePane;

	protected Color backgroundColorEditFieldsSelected = Globals.defaultTableCellSelectedBgColor;

	protected Color backgroundColorSelected = Globals.defaultTableCellSelectedBgColorNotEditable;

	protected JPopupMenu popupMenu;

	protected boolean dataChanged;

	protected UpdateController myController;

	protected int maxTableWidth = Short.MAX_VALUE;

	protected boolean editing = true;

	protected boolean deleteAllowed = true;

	protected boolean switchLineColors = true;

	protected boolean awareOfSelectionListener;
	protected boolean followSelectionListener = true;
	protected boolean awareOfTableChangedListener = true;

	protected boolean withTablesearchPane;

	protected TablesearchPane searchPane;

	protected RowNoTableModelFilterCondition filterBySelectionCondition;
	protected TableModelFilter filterBySelection;
	protected boolean filteringActive;
	protected boolean selectionEmpty = true;
	protected boolean singleSelection;

	protected String title = "";

	protected int generalPopupPosition;

	protected int popupIndex;

	private int oldrowcount = -1;

	protected Map<Integer, SortOrder> sortDescriptor;
	protected Map<Integer, SortOrder> specialSortDescriptor;

	private AbstractExportTable exportTable;

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition,
			boolean switchLineColors, int[] popupsWanted, boolean withTablesearchPane) {
		this.withTablesearchPane = withTablesearchPane;

		menuItemsRequesting1SelectedLine = new ArrayList<>();
		menuItemsRequestingMultiSelectedLines = new ArrayList<>();

		this.generalPopupPosition = generalPopupPosition;

		this.internalpopups = new ArrayList<>();
		if (popupsWanted != null) {
			for (int j = 0; j < popupsWanted.length; j++) {
				this.internalpopups.add(popupsWanted[j]);
				Logging.info(this, "add popup " + popupsWanted[j]);
			}
		} else {
			this.internalpopups.add(POPUP_RELOAD);

			this.internalpopups.add(POPUP_PDF);
		}

		Logging.info(this, "internalpopups " + giveMenuitemNames(internalpopups));

		this.internalpopups = supplementBefore(POPUP_RELOAD, POPUPS_EXPORT, this.internalpopups);

		Logging.info(this, "internalpopups supplemented " + giveMenuitemNames(internalpopups));

		if (maxTableWidth > 0) {
			this.maxTableWidth = maxTableWidth;
		}

		if (title != null) {
			this.title = title;
		}

		this.editing = editing;

		if (!Globals.isServerFullPermission()) {
			this.editing = false;
		}

		this.switchLineColors = switchLineColors;

		initComponents();
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition,
			boolean switchLineColors, int[] popupsWanted) {
		this(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, popupsWanted, false);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition,
			boolean switchLineColors) {
		this(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, null);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition) {
		this(title, maxTableWidth, editing, generalPopupPosition, false);
	}

	public PanelGenEditTable(String title, int maxTableWidth, boolean editing) {
		this(title, maxTableWidth, editing, 0);
	}

	public PanelGenEditTable(String title, int maxTableWidth) {
		this(title, maxTableWidth, true);
	}

	public PanelGenEditTable(int maxTableWidth) {
		this("", maxTableWidth);
	}

	public PanelGenEditTable() {
		this(0);
	}

	private static final List<String> giveMenuitemNames(List<Integer> popups) {
		List<String> result = new ArrayList<>();

		for (int el : popups) {
			result.add(keyNames.get(el));
		}

		return result;
	}

	protected Object modifyHeaderValue(Object value) {
		return value;
	}

	/**
	 * sets frame to return to e.g. from option dialogs
	 * 
	 * @param javax.swing.JFrame
	 */
	public void setMasterFrame(JFrame masterFrame) {
		this.masterFrame = masterFrame;
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

	public void setAutoResizeMode(int mode) {
		theTable.setAutoResizeMode(mode);
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
		setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		addComponentListener(this);

		buttonCommit = new IconButton(Configed.getResourceValue("PanelGenEditTable.SaveButtonTooltip"),
				"images/apply.png", "images/apply_over.png", "images/apply_disabled.png");

		buttonCommit.setPreferredSize(Globals.smallButtonDimension);
		if (!editing) {
			buttonCommit.setVisible(false);
		}

		buttonCancel = new IconButton(Configed.getResourceValue("PanelGenEditTable.CancelButtonTooltip"),
				"images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png");

		buttonCancel.setPreferredSize(Globals.smallButtonDimension);
		if (!editing) {
			buttonCancel.setVisible(false);
		}

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);
		setDataChanged(false);

		label = new JLabel(title);
		label.setFont(Globals.defaultFontStandardBold);
		if (title == null || title.equals("")) {
			label.setVisible(false);
		}

		labelRowCount = new JLabel(title);
		labelRowCount.setFont(Globals.defaultFontStandardBold);

		labelMarkedCount = new JLabel("");
		labelMarkedCount.setFont(Globals.defaultFont);

		titlePane = new PanelLinedComponents();
		titlePane.setVisible(false);
		titlePane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		theTable = new de.uib.utilities.table.JTableWithToolTips();

		theTable.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		exportTable = new ExporterToCSV(theTable);

		searchPane = new TablesearchPane(this, true, null);

		searchPane.setVisible(withTablesearchPane);

		theTable.getTableHeader().addMouseListener(this);

		// add the popup to the scrollpane for the case that the table is empty
		scrollpane = new JScrollPane();

		// NOT WORK

		if (switchLineColors) {
			theTable.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		} else {
			theTable.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		}

		theTable.addMouseListener(this);

		theTable.setShowHorizontalLines(true);
		theTable.setGridColor(Globals.PANEL_GEN_EDIT_TABLE_GRID_COLOR);

		theTable.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(theTable.getTableHeader().getDefaultRenderer()) {

					@Override
					protected Object modifyValue(Object value) {
						return modifyHeaderValue(value);
					}

				}

				);

		// we prefer the simple behaviour:
		theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theTable.getTableHeader().setReorderingAllowed(false);

		theTable.addKeyListener(this);

		getListSelectionModel().addListSelectionListener(this);

		theTable.setDragEnabled(true);
		theTable.setDropMode(DropMode.ON);

		theTable.setAutoCreateRowSorter(false);

		try {
			scrollpane = new JScrollPane();
			scrollpane.setViewportView(theTable);
			scrollpane.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);
		} catch (ClassCastException ex) {
			// a strange Nimbus exception which occurs sometimes here
			Logging.warning(this, "strange exception on creating scrollpane " + ex);

			scrollpane = new JScrollPane();
			scrollpane.setViewportView(theTable);
			scrollpane.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);

		}

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)

								.addComponent(titlePane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, maxTableWidth)
						.addComponent(scrollpane, GroupLayout.DEFAULT_SIZE, 100, maxTableWidth)
						.addGroup(layout.createSequentialGroup()
								.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)));

		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.CENTER)
								.addComponent(label, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
								.addComponent(titlePane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addComponent(searchPane)
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addComponent(scrollpane, 20, 100, Short.MAX_VALUE)
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE)
								.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
								.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		if (generalPopupPosition == 0) {
			// if -1 dont use a standard popup
			// if > 0 the popup is added later after installing another popup

			addPopupmenuStandardpart();
		}
	}

	public void setColumnSelectionAllowed(boolean b) {
		// destroys search function

		theTable.setColumnSelectionAllowed(b);
	}

	public void setRowSelectionAllowed(boolean b) {
		theTable.setRowSelectionAllowed(b);
	}

	public void setDeleteAllowed(boolean b) {
		deleteAllowed = b;
	}

	public void sortAgainAsConfigured() {
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
			((DefaultRowSorter) getRowSorter()).sort();
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
		Logging.info(this, "in PanelGenEditTable reload()");
		WaitCursor waitCursor = new WaitCursor(this);
		tableModel.requestReload();
		tableModel.reset();
		setDataChanged(false);
		waitCursor.stop();
	}

	public void setTitle(String title) {
		Logging.info(this, "setTitle " + title);
		this.title = title;
		label.setText(title);

	}

	public void setTitlePaneBackground(Color c) {
		if (c == null) {
			titlePane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		} else {
			titlePane.setBackground(c);
		}
	}

	public void setTitlePane(JComponent[] components, int height) {
		titlePane.setVisible(true);
		((PanelLinedComponents) titlePane).setComponents(components, height);
	}

	private static List<Integer> supplementBefore(int insertpoint, final int[] injectKeys,
			final List<Integer> listOfKeys) {
		ArrayList<Integer> augmentedList = new ArrayList<>();

		boolean found = false;

		Set<Integer> setOfKeys = new HashSet<>();

		for (int key : listOfKeys) {
			if (key == insertpoint) {
				found = true;

				for (int type : injectKeys) {

					if (!setOfKeys.contains(type)) {
						augmentedList.add(type);
						setOfKeys.add(type);
					}
				}
			}
			augmentedList.add(key);
			setOfKeys.add(key);
		}

		if (!found) {
			for (int type : injectKeys) {
				if (!setOfKeys.contains(type)) {
					augmentedList.add(type);
					setOfKeys.add(type);
				}
			}
		}
		return augmentedList;
	}

	protected void addPopupmenuStandardpart() {
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
				menuItemSave = new JMenuItemFormatted(Configed.getResourceValue("PanelGenEditTable.saveData"));
				menuItemSave.setEnabled(false);
				menuItemSave.addActionListener(actionEvent -> commit());
				addPopupItem(menuItemSave);

				break;

			case POPUP_CANCEL:
				menuItemCancel = new JMenuItemFormatted(Configed.getResourceValue("PanelGenEditTable.abandonNewData"));
				menuItemCancel.setEnabled(false);
				menuItemCancel.addActionListener(actionEvent -> cancel());
				addPopupItem(menuItemCancel);

				break;

			case POPUP_RELOAD:

				menuItemReload = new JMenuItemFormatted(Configed.getResourceValue("PanelGenEditTable.reload"),
						Globals.createImageIcon("images/reload16.png", ""));

				// does not work
				menuItemReload.addActionListener(actionEvent -> reload());
				if (popupIndex > 1) {
					popupMenu.addSeparator();
				}

				addPopupItem(menuItemReload);

				break;

			case POPUP_SORT_AGAIN:
				menuItemSortAgain = new JMenuItemFormatted(
						Configed.getResourceValue("PanelGenEditTable.sortAsConfigured"));
				menuItemSortAgain.addActionListener(actionEvent -> sortAgainAsConfigured());

				addPopupItem(menuItemSortAgain);

				break;

			case POPUP_DELETE_ROW:
				menuItemDeleteRelation = new JMenuItemFormatted(
						Configed.getResourceValue("PanelGenEditTable.deleteRow"));
				menuItemDeleteRelation.setEnabled(false);
				menuItemDeleteRelation.addActionListener((ActionEvent actionEvent) -> {
					if (getSelectedRowCount() == 0) {
						JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
								Configed.getResourceValue("PanelGenEditTable.noRowSelected"),
								Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

						return;
					} else if (deleteAllowed) {
						tableModel.deleteRow(getSelectedRowInModelTerms());
					}
				});
				addPopupItem(menuItemDeleteRelation);

				break;

			case POPUP_PRINT:
				menuItemPrint = new JMenuItemFormatted(Configed.getResourceValue("PanelGenEditTable.print"));
				menuItemPrint.addActionListener((ActionEvent actionEvent) -> {
					try {
						theTable.print();
					} catch (PrinterException ex) {
						Logging.error("Printing error ", ex);
					}
				});

				addPopupItem(menuItemPrint);

				break;

			case POPUP_FLOATINGCOPY:

				menuItemFloatingCopy = new JMenuItemFormatted(
						Configed.getResourceValue("PanelGenEditTable.floatingCopy"));
				menuItemFloatingCopy.addActionListener(actionEvent -> floatExternal());

				if (popupIndex > 1) {
					popupMenu.addSeparator();
				}

				addPopupItem(menuItemFloatingCopy);
				break;

			case POPUP_EXPORT_CSV:
				menuItemExportCSV = exportTable.getMenuItemExport();
				addPopupItem(menuItemExportCSV);

				break;

			case POPUP_EXPORT_SELECTED_CSV:
				menuItemExportSelectedCSV = exportTable.getMenuItemExportSelected();
				addPopupItem(menuItemExportSelectedCSV);

				break;

			case POPUP_PDF:
				menuItemPDF = new JMenuItemFormatted(Configed.getResourceValue("FGeneralDialog.pdf"),
						Globals.createImageIcon("images/acrobat_reader16.png", ""));
				menuItemPDF.addActionListener((ActionEvent actionEvent) -> {
					try {
						HashMap<String, String> metaData = new HashMap<>();
						metaData.put("header", title);
						metaData.put("subject", "report of table");
						metaData.put("keywords", "");

						ExporterToPDF pdfExportTable = new ExporterToPDF(theTable);
						pdfExportTable.setMetaData(metaData);
						pdfExportTable.setPageSizeA4Landscape();
						pdfExportTable.execute(null, true);

					} catch (Exception ex) {
						Logging.error("PDF printing error " + ex);
					}
				});

				addPopupItem(menuItemPDF);

				break;

			default:
				Logging.warning(this, "no case found for popuptype in addPopupmenuStandardpart");
				break;
			}
		}
	}

	public void addPopupItem(JMenuItem item) {

		if (popupMenu == null) {
			// for the first item, we create the menu
			popupMenu = new JPopupMenu();
			theTable.addMouseListener(new utils.PopupMouseListener(popupMenu));

			// add the popup to the scrollpane if the table is empty
			scrollpane.addMouseListener(new utils.PopupMouseListener(popupMenu));

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

	protected List<RowSorter.SortKey> buildSortkeysFromColumns() {
		Logging.debug(this, "buildSortkeysFromColumns,  sortDescriptor " + sortDescriptor);
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();

		if (tableModel.getColumnCount() == 0) {
			return new ArrayList<>();
		} else if (sortDescriptor == null) {

			// default sorting
			sortDescriptor = new LinkedHashMap<>();

			if (tableModel.getKeyCol() > -1) {
				try {

					sortKeys.add(new RowSorter.SortKey(tableModel.getKeyCol(), SortOrder.ASCENDING));

					sortDescriptor.put(tableModel.getKeyCol(), SortOrder.ASCENDING);
				} catch (Exception ex) {
					Logging.debug(this, "sortkey problem " + ex);
				}

			} else if (tableModel.getFinalCols() != null && !tableModel.getFinalCols().isEmpty()) {
				Iterator<Integer> iter = tableModel.getFinalCols().iterator();

				while (iter.hasNext()) {
					Integer col = iter.next();
					sortKeys.add(new RowSorter.SortKey(col, SortOrder.ASCENDING));

					sortDescriptor.put(col, SortOrder.ASCENDING);
				}
			} else {
				sortKeys = null;
			}

		} else {

			for (Entry<Integer, SortOrder> entry : sortDescriptor.entrySet()) {
				sortKeys.add(new RowSorter.SortKey(entry.getKey(), entry.getValue()));
			}

		}

		return sortKeys;

	}

	private void setSorter() {
		Logging.info(this, "setSorter");

		if (tableModel == null) {
			return;
		}

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel) {
			@Override
			protected boolean useToString(int column) {
				try {
					return super.useToString(column);
				} catch (Exception ex) {
					Logging.debug(this, "column " + column + " ------------------- no way to string");
					return false;
				}
			}

			@Override
			public Comparator<?> getComparator(int column) {
				try {
					Logging.debug(this, " comparator for col " + column + " is " + super.getComparator(column));
					return super.getComparator(column);
				} catch (Exception ex) {
					Logging.warning(this, "column " + column + " ------------------- not getting comparator ");
					return null;
				}

			}
		};

		if (sorter instanceof DefaultRowSorter) {
			// is always the case since TableRowSorter extends DefaultRowSorter

			for (int j = 0; j < tableModel.getColumnCount(); j++) {

				if (comparators[j] != null) {
					Logging.info(this, " set sorter for column " + j + " " + comparators[j]);
					// restore previously explicitly assigned comparator
					((DefaultRowSorter) sorter).setComparator(j, comparators[j]);
				} else if (tableModel.getClassNames().get(j).equals("java.lang.Integer")) {

					((DefaultRowSorter) sorter).setComparator(j, new de.uib.utilities.IntComparatorForStrings());
				}
			}
		}

		List<RowSorter.SortKey> sortKeys = buildSortkeysFromColumns();

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

		comparators = new Comparator[m.getColumnCount()];

		setSorter();

		setDataChanged(false);
		setCellRenderers();

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
		DefaultRowSorter sorter = (DefaultRowSorter) theTable.getRowSorter();
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
	 * set if filter mode is reset on new search (this is default but may be
	 * disabled for special implementations of selection
	 * 
	 * @parameter boolean
	 */
	public void setResetFilterModeOnNewSearch(boolean b) {
		if (!withTablesearchPane) {
			Logging.debug(this, "setResetFilterModeOnNewSearch: no search panel");
			return;
		}
		searchPane.setResetFilterModeOnNewSearch(b);
	}

	/**
	 * set search mode possible values TablesearchPane.FULL_TEXT_SEARCH
	 * TablesearchPane.START_TEXT_SEARCH = 1; TablesearchPane.REGEX_SEARCH
	 */
	public void setSearchMode(int a) {
		searchPane.setSearchMode(a);
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
			filterBySelectionCondition = new RowNoTableModelFilterCondition(theTable);
			filterBySelection = new TableModelFilter(filterBySelectionCondition, false, false);

			tableModel.chainFilter(SearchTargetModelFromTable.FILTER_BY_SELECTION, filterBySelection);
		}
	}

	/**
	 * activates popupMark or this as well as popupMarkAndFilter in context menu
	 * 
	 * @parameter boolean
	 */
	public void setFiltering(boolean b, boolean withFilterPopup) {
		if (b) {
			setFilteringActive(b);
			// lazy activation
		}
		searchPane.setFiltering(b, withFilterPopup);

	}

	/**
	 * activates popupMark and popupMarkAndFilter in context menu
	 * 
	 * @parameter boolean
	 */
	public void setFiltering(boolean b) {
		setFiltering(b, true);
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
	 * sets an alternative tooltip for the filtermark
	 * 
	 * @parameter String
	 */
	public void setFiltermarkToolTipText(String s) {
		searchPane.setFiltermarkToolTipText(s);
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

	/**
	 * transfer mappings to the searchpane
	 */
	public void setMapping(String columnName, Mapping<Integer, String> mapping) {
		searchPane.setMapping(columnName, mapping);
	}

	/**
	 * /** set predefinition of searchfield
	 */
	public void setSelectedSearchField(String field) {
		searchPane.setSelectedSearchField(field);
	}

	/**
	 * should mark the columns which are editable after being generated
	 */
	public void setEmphasizedColumns(int[] cols) {
		if (cols == null) {
			return;
		}

		if (theTable.getColumnModel().getColumns().hasMoreElements()) {

			for (int j = 0; j < cols.length; j++) {
				theTable.getColumnModel().getColumn(cols[j])
						.setCellRenderer(new TableCellRendererConfigured(null, Globals.lightBlack,
								Globals.defaultTableCellBgColor1, Globals.defaultTableCellBgColor2,
								backgroundColorSelected, backgroundColorEditFieldsSelected));
			}
		}
	}

	protected void setTimestampRenderer(String classname, TableColumn col) {

		if (classname.equals("java.sql.Timestamp")) {
			col.setCellRenderer(new TableCellRendererDate());
		}

	}

	protected void setBigDecimalRenderer(String classname, TableColumn col) {
		if (classname.equals("java.math.BigDecimal")) {
			col.setCellRenderer(new TableCellRendererCurrency());
		}

	}

	protected void setBooleanRenderer(String classname, TableColumn col) {
		if (classname.equals("java.lang.Boolean")) {
			col.setCellRenderer(new TableCellRendererByBoolean());
		}

	}

	protected void setCellRenderers() {
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			String name = tableModel.getColumnName(i);
			TableColumn col = theTable.getColumn(name);
			String classname = tableModel.getClassNames().get(i);

			setTimestampRenderer(classname, col);
			setBigDecimalRenderer(classname, col);
			setBooleanRenderer(classname, col);

			if (col.getCellRenderer() == null) {
				// no special renderer set
				col.setCellRenderer(new StandardTableCellRenderer());
			}
		}
	}

	public void setDataChanged(boolean b) {
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

	protected void deleteCurrentRow() {
		if (!deleteAllowed) {
			return;
		}

		if (getSelectedRowCount() > 0) {
			tableModel.deleteRow(getSelectedRowInModelTerms());
		}
	}

	public void setTableColumnInvisible(int col) {

		TableColumn column = null;
		try {
			column = theTable.getColumnModel().getColumn(col);
		} catch (Exception ex) {
			Logging.info(this, "setTableColumnInvisible  " + ex);
		}

		if (column != null) {
			Logging.info(this, "setTableColumnInvisible col " + col);
			column.setWidth(0);
			column.setMaxWidth(100);
			column.setMinWidth(0);
			column.setPreferredWidth(0);
			column.setResizable(true);
			theTable.getTableHeader().resizeAndRepaint();
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

	public TablesearchPane getTheSearchpane() {
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

	public void setSelectedRowFromModel(int row) {
		theTable.setRowSelectionInterval(theTable.convertRowIndexToView(row), theTable.convertRowIndexToView(row));

		theTable.scrollRectToVisible(theTable.getCellRect(theTable.convertRowIndexToView(row), 0, true));
	}

	public void setValueAt(Object value, int row, int col) {
		tableModel.setValueAt(value, theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
	}

	public Object getValueAt(int row, int col) {

		try {
			return tableModel.getValueAt(theTable.convertRowIndexToModel(row), theTable.convertColumnIndexToModel(col));
		} catch (Exception ex) {
			return null;
		}
	}

	public void selectedRowChanged() {
		/* To be implemented in subclass */}

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
			for (int i = 0; i < theTable.getSelectedRows().length; i++) {

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

	protected int findViewRowFromValue(int startviewrow, Object value, int col) {
		Logging.debug(this,
				"findViewRowFromValue startviewrow, value, col " + startviewrow + ", " + value + ", " + col);

		if (value == null) {
			return -1;
		}

		String val = value.toString();

		boolean found = false;

		int viewrow = 0;

		if (startviewrow > 0) {
			viewrow = startviewrow;
		}

		while (!found && viewrow < tableModel.getRowCount()) {
			Object compareValue = tableModel.getValueAt(

					theTable.convertRowIndexToModel(viewrow), col

			);

			if (compareValue == null) {
				if (val == null || val.equals("")) {
					found = true;
				}
			} else {
				String compareVal = compareValue.toString();

				if (val.equals(compareVal)) {
					found = true;
				}
			}

			if (!found) {
				viewrow++;
			}

		}

		if (found) {
			return viewrow;
		}

		return -1;
	}

	public int findViewRowFromValue(Object value, int col) {
		return findViewRowFromValue(0, value, col);
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

			while (!found && viewrow < tableModel.getRowCount()) {
				String[] partialkeys = new String[tableModel.getFinalCols().size()];

				for (int j = 0; j < tableModel.getFinalCols().size(); j++) {
					partialkeys[j] = tableModel
							.getValueAt(theTable.convertRowIndexToModel(viewrow), tableModel.getFinalCols().get(j))
							.toString();
				}

				if (keyValue.equals(Globals.pseudokey(partialkeys))) {
					found = true;
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

	public void moveToModelRow(int n) {
		if (tableModel.getRowCount() == 0) {
			return;
		}

		if (getSelectedRowCount() != 1) {
			return;
		}

		if (n < 0 || n >= theTable.getRowCount()) {
			return;
		}

		theTable.scrollRectToVisible(theTable.getCellRect(theTable.convertRowIndexToView(n), 0, true));
		theTable.setRowSelectionInterval(theTable.convertRowIndexToView(n), theTable.convertRowIndexToView(n));
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

	public boolean canNavigate() {
		return tableModel.getRowCount() > 0 && getSelectedRowCount() == 1;
	}

	public boolean isFirstRow() {
		return getSelectedRow() == 0;
	}

	public boolean isLastRow() {
		return getSelectedRow() == tableModel.getRowCount() - 1;
	}

	public void moveToLastRow() {
		moveToRow(tableModel.getRowCount() - 1);
	}

	public void moveToFirstRow() {
		moveToRow(0);
	}

	public void moveRowBy(int i) {

		if (getSelectedRowCount() != 1) {
			return;
		}

		int n = theTable.getSelectedRow();

		if (i > 0) {
			if (n + i < theTable.getRowCount()) {
				n = n + i;
			}

		} else if (i < 0 && n + i >= 0) {
			n = n + i;
		}

		moveToRow(n);
	}

	public RowSorter getRowSorter() {
		return theTable.getRowSorter();
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
				&& !((tableModel.getColMarkCursorRow() > -1) && (e.getColumn() == tableModel.getColMarkCursorRow()))) {

			Logging.info(this, " tableChanged, datachanged set to true");
			setDataChanged(true);
			if (tableModel != null && oldrowcount != tableModel.getRowCount()) {
				oldrowcount = tableModel.getRowCount();
			}
		}
	}

	//
	// ActionListener interface
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {

			cancel();
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

		if (lsm.isSelectionEmpty()) {
			Logging.info(this, "no rows selected");
			selectionEmpty = true;
			singleSelection = false;
			if (menuItemDeleteRelation != null) {
				menuItemDeleteRelation.setEnabled(false);
			}
		} else {
			selectionEmpty = false;
			int selectedRow = lsm.getMinSelectionIndex();
			if (followSelectionListener) {
				selectedRowChanged();
			}

			if (menuItemDeleteRelation != null) {
				menuItemDeleteRelation.setEnabled(true);
			}

			singleSelection = (selectedRow == lsm.getMaxSelectionIndex());
		}

	}

	public boolean isSelectionEmpty() {
		return selectionEmpty;
	}

	public boolean isSingleSelection() {
		return singleSelection;
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

	/*
	 * extract scrollpane for use in other components
	 */
	public JScrollPane getScrollPane() {
		return scrollpane;
	}

	protected void floatExternal() {

		PanelGenEditTable copyOfMe;
		de.uib.configed.gui.GeneralFrame externalView;

		copyOfMe = new PanelGenEditTable(title, maxTableWidth, false);

		copyOfMe.setTableModel(tableModel);

		externalView = new de.uib.configed.gui.GeneralFrame(null, "hallo", false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(ConfigedMain.getMainFrame());

		externalView.setVisible(true);
	}
}
