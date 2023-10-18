/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.selectionpanel;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;
import utils.Utils;

public class JTableSelectionPanel extends JPanel implements DocumentListener, KeyListener, ActionListener {

	private static final Pattern sPlusPattern = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int MIN_HEIGHT = 200;

	private JScrollPane scrollpane;

	// we put a JTable on a standard JScrollPane
	private JTable table;

	private DefaultListSelectionModel selectionmodel;
	private ConfigedMain configedMain;
	private List<RowSorter.SortKey> primaryOrderingKeys;

	private CheckedLabel checkmarkSearch;

	private JLabel labelSearch;
	private JTextField fieldSearch;

	private JButton buttonMarkAll;
	private JButton buttonInvertSelection;
	private JComboBox<String> comboSearch;

	private JLabel labelSearchMode;
	private JComboBoxToolTip comboSearchMode;

	private TablesearchPane.SearchMode searchMode;

	private int foundrow = -1;

	private int lastCountOfSearchWords;

	public JTableSelectionPanel(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;
		initComponents();
		setupLayout();
	}

	private void initComponents() {
		searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES;

		scrollpane = new JScrollPane();

		if (!Main.THEMES) {
			scrollpane.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);
		}

		table = new JTable();

		table.setDragEnabled(true);

		if (!Main.THEMES) {
			table.setShowGrid(true);
			table.setGridColor(Globals.JTABLE_SELECTION_PANEL_GRID_COLOR);
		}

		table.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		table.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		table.setAutoCreateRowSorter(true);

		primaryOrderingKeys = new ArrayList<>();
		primaryOrderingKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(table.getTableHeader().getDefaultRenderer()));
		// Ask to be notified of selection changes.
		selectionmodel = (DefaultListSelectionModel) table.getSelectionModel();
		// the default implementation in JTable yields this type

		table.setColumnSelectionAllowed(false);
		// true destroys setSelectedRow etc

		addListSelectionListener(configedMain);

		table.addKeyListener(this);

		scrollpane.getViewport().add(table);

		labelSearch = new JLabel(Configed.getResourceValue("SearchPane.search"));

		Icon unselectedIconSearch = Utils.createImageIcon("images/loupe_light_16.png", "");
		Icon selectedIconSearch = Utils.createImageIcon("images/loupe_light_16_x.png", "");

		checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);
		checkmarkSearch.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearch.tooltip"));
		checkmarkSearch.addActionListener(this);
		checkmarkSearch.setChangeStateAutonomously(false);

		fieldSearch = new JTextField("");
		fieldSearch.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		if (!Main.THEMES) {
			fieldSearch.setBackground(Globals.BACKGROUND_COLOR_8);
		}

		fieldSearch.getCaret().setBlinkRate(0);
		fieldSearch.getDocument().addDocumentListener(this);

		fieldSearch.addKeyListener(this);

		JPopupMenu searchMenu = new JPopupMenu();
		JMenuItem popupSearch = new JMenuItem();
		JMenuItem popupSearchNext = new JMenuItem();
		JMenuItem popupNewSearch = new JMenuItem();
		JMenuItem popupMarkHits = new JMenuItem();
		JMenuItem popupEmptySearchfield = new JMenuItem();
		searchMenu.add(popupSearch);
		searchMenu.add(popupSearchNext);
		searchMenu.add(popupNewSearch);
		searchMenu.add(popupMarkHits);
		searchMenu.add(popupEmptySearchfield);

		popupSearch.setText(Configed.getResourceValue("JTableSelectionPanel.search"));
		popupSearch.addActionListener(actionEvent -> searchTheRow());

		popupSearchNext.setText(Configed.getResourceValue("JTableSelectionPanel.searchnext") + " ( F3 ) ");
		popupSearchNext.addActionListener(actionEvent -> searchTheNextRow());

		popupNewSearch.setText(Configed.getResourceValue("JTableSelectionPanel.searchnew"));
		popupNewSearch.addActionListener(actionEvent -> searchTheRow(0));

		popupMarkHits.setText(Configed.getResourceValue("SearchPane.popup.markall"));

		popupMarkHits.addActionListener(actionEvent -> markAll());

		popupEmptySearchfield.setText(Configed.getResourceValue("JTableSelectionPanel.searchempty"));
		popupEmptySearchfield.addActionListener(actionEvent -> fieldSearch.setText(""));

		fieldSearch.setComponentPopupMenu(searchMenu);

		fieldSearch.addActionListener(actionEvent -> searchTheNextRow());

		Icon markAllIcon = Utils.createImageIcon("images/selection-all.png", "");
		Icon invertSelectionIcon = Utils.createImageIcon("images/selection-invert.png", "");
		buttonMarkAll = new JButton("", markAllIcon);
		buttonMarkAll.setToolTipText(Configed.getResourceValue("SearchPane.popup.markall"));
		buttonInvertSelection = new JButton("", invertSelectionIcon);
		buttonInvertSelection.setToolTipText(Configed.getResourceValue("SearchPane.invertselection"));

		buttonMarkAll.addActionListener((ActionEvent e) -> markAll());

		buttonInvertSelection.addActionListener((ActionEvent e) -> configedMain.invertClientselection());

		labelSearchMode = new JLabel(Configed.getResourceValue("JTableSelectionPanel.searchmode"));

		comboSearchMode = new JComboBoxToolTip();

		Map<String, String> tooltipsMap = new LinkedHashMap<>();
		tooltipsMap.put(Configed.getResourceValue("SearchPane.SearchMode.fulltext_with_alternatives"),
				Configed.getResourceValue("SearchPane.SearchMode.fulltext_with_alternatives.tooltip"));

		tooltipsMap.put(Configed.getResourceValue("SearchPane.SearchMode.fulltext_one_string"),
				Configed.getResourceValue("SearchPane.SearchMode.fulltext_one_string.tooltip"));

		tooltipsMap.put(Configed.getResourceValue("SearchPane.SearchMode.starttext"),
				Configed.getResourceValue("SearchPane.SearchMode.starttext.tooltip"));

		tooltipsMap.put(Configed.getResourceValue("SearchPane.SearchMode.regex"),
				Configed.getResourceValue("SearchPane.SearchMode.regex.tooltip"));

		Logging.info(this, " comboSearchMode tooltipsMap " + tooltipsMap);

		comboSearchMode.setValues(tooltipsMap);
		comboSearchMode.setSelectedIndex(searchMode.ordinal());

		Logging.info(this, "comboSearchMode set index to " + searchMode.ordinal());

		comboSearchMode.setPreferredSize(Globals.BUTTON_DIMENSION);

		comboSearch = new JComboBox<>(
				new String[] { Configed.getResourceValue("ConfigedMain.pclistTableModel.allfields") });
		comboSearch.setPreferredSize(Globals.BUTTON_DIMENSION);
	}

	private void setupLayout() {
		JPanel topPane = new JPanel();
		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);

		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup()

				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(fieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(buttonMarkAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(buttonInvertSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, 2 * Globals.MIN_GAP_SIZE, 2 * Globals.MIN_GAP_SIZE)
				.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(comboSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(comboSearchMode, 100, 200, 300)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addGroup(layoutTopPane.createParallelGroup(Alignment.CENTER)

						.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonMarkAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonInvertSelection, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2));

		JPanel leftPane = this;
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(topPane, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(layoutLeftPane.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
						.addComponent(scrollpane, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public JTable getTable() {
		return table;
	}

	public void setDataPanel() {
		scrollpane.getViewport().setView(table);
	}

	public void setMissingDataPanel() {
		String logoPath;
		if (Main.THEMES) {
			logoPath = Globals.ICON_OPSI;
		} else {
			logoPath = "images/opsi-logo.png";
		}

		JLabel missingData0 = new JLabel(Utils.createImageIcon(logoPath, ""));

		JLabel missingData1 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));

		JLabel missingData2 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));

		JPanel mdPanel = new JPanel();
		if (!Main.THEMES) {
			mdPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		GroupLayout mdLayout = new GroupLayout(mdPanel);
		mdPanel.setLayout(mdLayout);

		mdLayout.setVerticalGroup(mdLayout.createSequentialGroup().addGap(10, 10, Short.MAX_VALUE)
				.addComponent(missingData0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(missingData1, 10, 40, 90).addGap(10, 40, 40).addComponent(missingData2, 10, 40, 80)
				.addGap(10, 10, Short.MAX_VALUE));
		mdLayout.setHorizontalGroup(mdLayout.createSequentialGroup().addGap(10, 10, Short.MAX_VALUE)
				.addGroup(mdLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(missingData0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(missingData1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(missingData2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(10, 10, Short.MAX_VALUE));

		scrollpane.getViewport().setView(mdPanel);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		scrollpane.addMouseListener(l);
		table.addMouseListener(l);
	}

	public boolean isSelectionEmpty() {
		return table.getSelectedRowCount() == 0;
	}

	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public Rectangle getCellRect(int row, int col, boolean includeSpacing) {
		return table.getCellRect(row, col, includeSpacing);
	}

	private Map<Integer, Integer> getSelectionMap() {
		Map<Integer, Integer> selectionMap = new HashMap<>();
		int selectedKeysCount = 0;

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				selectionMap.put(selectedKeysCount, i);
				selectedKeysCount++;
			}
		}

		return selectionMap;
	}

	public Set<String> getSelectedSet() {
		TreeSet<String> result = new TreeSet<>();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				result.add((String) table.getValueAt(i, 0));
			}
		}

		return result;

	}

	public NavigableSet<String> getInvertedSet() {
		TreeSet<String> result = new TreeSet<>();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (!selectionmodel.isSelectedIndex(i)) {
				result.add((String) table.getValueAt(i, 0));
			}
		}

		return result;

	}

	public void initColumnNames() {

		String oldSelected = (String) comboSearch.getSelectedItem();
		List<String> comboSearchItems = new ArrayList<>();
		comboSearchItems.add(Configed.getResourceValue("ConfigedMain.pclistTableModel.allfields"));

		Logging.info(this, "initColumnNames columncount " + table.getColumnCount());

		for (int j = 0; j < table.getColumnCount(); j++) {
			Logging.info(this, "initColumnName col " + j);
			Logging.info(this, "initColumnName name  " + table.getColumnName(j));
			comboSearchItems.add(table.getColumnName(j));
		}

		comboSearch.setModel(new DefaultComboBoxModel<>(comboSearchItems.toArray(new String[0])));

		if (oldSelected != null) {
			comboSearch.setSelectedItem(oldSelected);
		}
	}

	public List<String> getSelectedValues() {
		List<String> valuesList = new ArrayList<>(table.getSelectedRowCount());

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				valuesList.add((String) table.getValueAt(i, 0));
			}
		}

		return valuesList;

	}

	public void clearSelection() {
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.clearSelection();
	}

	public void setSelectedValues(Collection<String> valuesList) {
		String valuesListS = null;
		if (valuesList != null) {
			valuesListS = "" + valuesList.size();
		}

		Logging.info(this, "setSelectedValues " + valuesListS);
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.clearSelection();

		if (valuesList == null || valuesList.isEmpty()) {
			return;
		}

		TreeSet<String> valuesSet = new TreeSet<>(valuesList);
		// because of ordering , we create a TreeSet view of the list

		Logging.info(this, "setSelectedValues, (ordered) set of values, size " + valuesSet.size());

		int lastAddedI = -1;

		ListSelectionListener[] listeners = ((DefaultListSelectionModel) lsm).getListeners(ListSelectionListener.class);

		// remove all listeners
		for (int l = 0; l < listeners.length; l++) {
			lsm.removeListSelectionListener(listeners[l]);
		}

		Logging.info(this, "setSelectedValues, table.getRowCount() " + table.getRowCount());

		for (int i = 0; i < table.getRowCount(); i++) {
			Logging.debug(this, "setSelectedValues checkValue for i " + i + ": " + (String) table.getValueAt(i, 0));

			if (valuesSet.contains(table.getValueAt(i, 0))) {
				lsm.addSelectionInterval(i, i);
				lastAddedI = i;
				Logging.debug(this, "setSelectedValues add interval " + i);
			}
		}

		lsm.removeSelectionInterval(lastAddedI, lastAddedI);

		// get again the listeners
		for (int l = 0; l < listeners.length; l++) {
			lsm.addListSelectionListener(listeners[l]);
		}

		// and repeat the last addition
		if (lastAddedI > -1) {

			lsm.addSelectionInterval(lastAddedI, lastAddedI);
		}

		table.repaint();

		if (!valuesSet.isEmpty()) {
			Object valueToFind = valuesSet.iterator().next();
			moveToValue(valueToFind, 0);
		}

		Logging.info(this, "setSelectedValues  produced " + getSelectedValues().size());

	}

	public void setSelectedValues(String[] values) {
		Set<String> valueSet = new TreeSet<>(Arrays.asList(values));

		setSelectedValues(valueSet);
	}

	public void initSortKeys() {
		table.getRowSorter().setSortKeys(primaryOrderingKeys);
	}

	@SuppressWarnings("java:S1452")
	public List<? extends SortKey> getSortKeys() {
		return table.getRowSorter().getSortKeys();
	}

	public void setSortKeys(List<? extends RowSorter.SortKey> orderingKeys) {
		table.getRowSorter().setSortKeys(orderingKeys);
	}

	public Set<String> getColumnValues(int col) {
		HashSet<String> result = new HashSet<>();
		if (table.getModel() == null || table.getModel().getColumnCount() <= col) {
			return result;
		}

		for (int i = 0; i < table.getModel().getRowCount(); i++) {
			result.add("" + table.getModel().getValueAt(i, col));
		}

		return result;
	}

	public void setModel(TableModel tm) {
		Logging.info(this, "set model with column count " + tm.getColumnCount());

		Logging.info(this, " [JTableSelectionPanel] setModel with row count " + tm.getRowCount());

		tm.addTableModelListener(table);

		table.setModel(tm);
	}

	public DefaultTableModel getSelectedRowsModel() {
		final Map<Integer, Integer> selectionMap = getSelectionMap();

		return new DefaultTableModel() {
			@Override
			public Object getValueAt(int row, int col) {
				return table.getValueAt(selectionMap.get(row), col);
			}

			@Override
			public int getRowCount() {
				return selectionMap.size();
			}

			@Override
			public int getColumnCount() {
				return table.getColumnCount();
			}
		};
	}

	public DefaultTableModel getTableModel() {
		return (DefaultTableModel) table.getModel();
	}

	public TableColumnModel getColumnModel() {
		return table.getColumnModel();
	}

	public void addListSelectionListener(ListSelectionListener lisel) {
		selectionmodel.addListSelectionListener(lisel);
	}

	public void removeListSelectionListener(ListSelectionListener lisel) {
		selectionmodel.removeListSelectionListener(lisel);
	}

	public void fireListSelectionEmpty(Object source) {
		for (ListSelectionListener listener : selectionmodel.getListSelectionListeners()) {
			listener.valueChanged(new ListSelectionEvent(source, 0, 0, false));
		}
	}

	public int findModelRowFromValue(Object value, int col) {
		int result = -1;

		if (value == null) {
			return result;
		}

		boolean found = false;
		int row = 0;

		while (!found && row < getTableModel().getRowCount()) {
			Object compareValue = getTableModel().getValueAt(row, col);

			String compareVal = compareValue.toString();
			String val = value.toString();

			if (val.equals(compareVal)) {
				found = true;
				result = row;
			}

			if (!found) {
				row++;
			}
		}

		return result;
	}

	private int findViewRowFromValue(int startviewrow, Object value, Set<Integer> colIndices) {
		return findViewRowFromValue(startviewrow, value, colIndices, searchMode);
	}

	private static List<String> getWords(String line) {
		List<String> result = new ArrayList<>();
		String[] splitted = sPlusPattern.split(line);
		for (String s : splitted) {
			if (!" ".equals(s)) {
				result.add(s);
			}
		}
		return result;
	}

	private int findViewRowFromValue(final int startviewrow, Object value, Set<Integer> colIndices,
			TablesearchPane.SearchMode searchMode) {

		Logging.info(this, "findViewRowFromValue(int startviewrow, Object value, Set colIndices, searchMode: "
				+ startviewrow + ", " + value + ", " + colIndices + ", " + searchMode);

		if (value == null) {
			return -1;
		}

		int viewrow = 0;

		if (startviewrow > 0) {
			viewrow = startviewrow;
		}

		// describe search parameters

		boolean fulltext = searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES
				|| searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_ONE_STRING;
		// with another data configuration, it could be combined with regex

		String val = value.toString();

		// get pattern for regex search mode if needed
		Pattern pattern = null;
		if (searchMode == TablesearchPane.SearchMode.REGEX_SEARCHING) {

			if (fulltext) {
				val = ".*" + val + ".*";
			}
			try {
				pattern = Pattern.compile(val);
			} catch (java.util.regex.PatternSyntaxException ex) {
				Logging.error(this, "pattern problem " + ex);
				return -1;
			}
		}

		String valLower = val.toLowerCase(Locale.ROOT);

		List<String> alternativeWords = getWords(valLower);
		lastCountOfSearchWords = alternativeWords.size();

		boolean found = false;
		while (!found && viewrow < getTableModel().getRowCount()) {

			for (int j = 0; j < getTableModel().getColumnCount(); j++) {

				// we dont compare all values (comparing all values is default)
				if (colIndices != null && !colIndices.contains(j)) {
					continue;
				}

				Object compareValue = getTableModel().getValueAt(table.convertRowIndexToModel(viewrow),
						table.convertColumnIndexToModel(j));

				if (compareValue == null) {
					found = val == null || val.isEmpty();
				} else {
					String compareVal = ("" + compareValue).toLowerCase(Locale.ROOT);

					switch (searchMode) {
					case REGEX_SEARCHING:

						if (pattern != null) {
							found = pattern.matcher(compareVal).matches();
						}

						break;

					case FULL_TEXT_SEARCHING_WITH_ALTERNATIVES:
						if (fullTextSearchingWithAlternatives(alternativeWords, compareVal)) {
							found = true;
						}
						break;

					case FULL_TEXT_SEARCHING_ONE_STRING:
						found = compareVal.indexOf(valLower) >= 0;
						break;

					default:
						found = compareVal.startsWith(valLower);

					}
				}

				if (found) {

					break;
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

	private static boolean fullTextSearchingWithAlternatives(List<String> alternativeWords, String compareVal) {
		for (String word : alternativeWords) {

			if (compareVal.indexOf(word) >= 0) {
				return true;
			}
		}
		return false;
	}

	public boolean moveToValue(Object value, int col) {
		HashSet<Integer> cols = new HashSet<>();
		cols.add(col);
		int viewrow = findViewRowFromValue(0, value, cols);

		scrollRowToVisible(viewrow);

		return viewrow != -1;
	}

	public void scrollRowToVisible(int row) {

		Rectangle scrollTo = table.getCellRect(row, 0, false);
		table.scrollRectToVisible(scrollTo);
	}

	public void addSelectedRow(int row) {
		if (table.getRowCount() == 0) {
			return;
		}

		table.addRowSelectionInterval(row, row);

		scrollRowToVisible(row);

	}

	public void setSelectedRow(int row) {
		if (table.getRowCount() == 0) {
			return;
		}

		if (row == -1) {
			table.clearSelection();
			return;
		}

		table.setRowSelectionInterval(row, row);

		scrollRowToVisible(row);
	}

	private void searchTheNextRow() {
		searchTheNextRow(false);
	}

	private void markAll() {
		table.clearSelection();
		searchTheRow(0);
		int startFoundrow = foundrow;

		foundrow = foundrow + 1;
		while (foundrow > startFoundrow) {
			// adding the next row to selection
			searchTheNextRow(true);
		}
	}

	private void searchTheNextRow(boolean addSelection) {
		int startrow = 0;
		if (table.getSelectedRow() >= 0) {
			startrow = table.getSelectedRows()[table.getSelectedRows().length - 1] + 1;
		}

		if (startrow >= table.getRowCount()) {
			startrow = 0;
		}

		searchTheRow(startrow, addSelection);

		if (foundrow == -1) {
			searchTheRow(0, addSelection);
		}
	}

	private void searchTheRow() {
		searchTheRow(table.getSelectedRow());
	}

	private void searchTheRow(int startrow) {
		searchTheRow(startrow, false);
	}

	private void searchTheRow(int startrow, boolean addSelection) {

		HashSet<Integer> selectedCols = null;

		if (comboSearch.getSelectedIndex() > 0) {
			selectedCols = new HashSet<>();
			selectedCols.add(getTableModel().findColumn((String) comboSearch.getSelectedItem()));
		}

		switch (comboSearchMode.getSelectedIndex()) {

		case 1:
			searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_ONE_STRING;
			break;
		case 2:
			searchMode = TablesearchPane.SearchMode.START_TEXT_SEARCHING;
			break;
		case 3:
			searchMode = TablesearchPane.SearchMode.REGEX_SEARCHING;
			break;
		default:
			searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES;
			break;
		}

		foundrow = findViewRowFromValue(startrow, fieldSearch.getText(), selectedCols, searchMode);

		if (foundrow > -1) {
			if (addSelection) {
				addSelectedRow(foundrow);
			} else {
				setSelectedRow(foundrow);
			}
		}
	}

	private void searchOnDocumentChange() {
		if (fieldSearch.getText().isEmpty()) {
			setSelectedRow(0);
			lastCountOfSearchWords = 0;
		} else {
			if (searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES
					&& getWords(fieldSearch.getText()).size() > lastCountOfSearchWords) {
				// when a new search word is added instead of extending one
				setSelectedRow(0);
			}

			searchTheRow();
		}
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {

		if (e.getDocument() == fieldSearch.getDocument()) {

			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			searchOnDocumentChange();

		}

	}

	@Override
	public void insertUpdate(DocumentEvent e) {

		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			searchOnDocumentChange();

		}

	}

	@Override
	public void removeUpdate(DocumentEvent e) {

		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);
			searchOnDocumentChange();
		}
	}

	protected void keyPressedOnTable(KeyEvent e) {
		/* for overwriting in subclass */}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {

		if (!(e.getSource() instanceof JTextField)) {
			keyPressedOnTable(e);
		}

		if (e.getKeyCode() == KeyEvent.VK_F5) {
			if (!fieldSearch.getText().isEmpty()) {
				markAll();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_F3) {

			if (!fieldSearch.getText().isEmpty()) {
				searchTheNextRow();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_I && e.isControlDown()) {
			configedMain.invertClientselection();
		} else {
			// Do nothing on other keyPress events
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == checkmarkSearch) {

			fieldSearch.setText("");
		}
	}
}
