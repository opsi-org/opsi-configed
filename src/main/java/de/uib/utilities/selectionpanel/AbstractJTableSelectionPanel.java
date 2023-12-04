/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.selectionpanel;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.guidata.SearchTargetModelFromClientTable;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;
import utils.Utils;

public abstract class AbstractJTableSelectionPanel extends JPanel implements KeyListener {
	private static final Pattern sPlusPattern = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int MIN_HEIGHT = 200;

	private JScrollPane scrollpane;

	private TablesearchPane searchPane;

	// we put a JTable on a standard JScrollPane
	private JTable table;

	private DefaultListSelectionModel selectionmodel;
	private ConfigedMain configedMain;
	private List<RowSorter.SortKey> primaryOrderingKeys;

	protected AbstractJTableSelectionPanel(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;
		initComponents();
		setupLayout();
	}

	private void initComponents() {
		scrollpane = new JScrollPane();

		table = new JTable();

		table.setDragEnabled(true);

		table.setDefaultRenderer(Object.class, new StandardTableCellRenderer());

		table.setAutoCreateRowSorter(true);

		primaryOrderingKeys = new ArrayList<>();
		primaryOrderingKeys.add(new SortKey(0, SortOrder.ASCENDING));

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

		searchPane = new TablesearchPane(new SearchTargetModelFromClientTable(table), true, null);
		searchPane.setFiltering(true);

		// filter icon inside searchpane
		searchPane.showFilterIcon(true);
		table.addKeyListener(searchPane);
		table.addKeyListener(this);

		scrollpane.getViewport().add(table);
	}

	private void setupLayout() {
		GroupLayout layoutLeftPane = new GroupLayout(this);
		this.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(searchPane, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpane, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void setFilterMark(boolean b) {
		searchPane.setFilterMark(b);
	}

	public JTable getTable() {
		return table;
	}

	public void setDataPanel() {
		scrollpane.getViewport().setView(table);
	}

	public void setMissingDataPanel() {
		JLabel missingData0 = new JLabel(Utils.createImageIcon(Globals.ICON_OPSI, ""));

		JLabel missingData1 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));

		JLabel missingData2 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));

		JPanel mdPanel = new JPanel();

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

	public void initColumnNames() {
		// New code
		searchPane.setSearchFieldsAll();
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
		for (ListSelectionListener listener : listeners) {
			lsm.removeListSelectionListener(listener);
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
		for (ListSelectionListener listener : listeners) {
			lsm.addListSelectionListener(listener);
		}

		// and repeat the last addition
		if (lastAddedI > -1) {
			lsm.addSelectionInterval(lastAddedI, lastAddedI);
		}

		table.repaint();

		if (!valuesSet.isEmpty()) {
			Object valueToFind = valuesSet.iterator().next();
			moveToValue(valueToFind);
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

	public void setSortKeys(List<? extends SortKey> orderingKeys) {
		table.getRowSorter().setSortKeys(orderingKeys);
	}

	public Set<String> getColumnValues(int col) {
		Set<String> result = new HashSet<>();
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

	public int findModelRowFromValue(Object value) {
		int result = -1;

		if (value == null) {
			return result;
		}

		boolean found = false;
		int row = 0;

		while (!found && row < getTableModel().getRowCount()) {
			Object compareValue = getTableModel().getValueAt(row, 0);

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

	private int findViewRowFromValue(Object value) {
		Logging.info(this, "findViewRowFromValue, value " + value);

		if (value == null) {
			return -1;
		}

		int viewrow = 0;

		String val = value.toString();

		String valLower = val.toLowerCase(Locale.ROOT);

		List<String> alternativeWords = getWords(valLower);

		boolean found = false;
		while (!found && viewrow < getTableModel().getRowCount()) {
			for (int j = 1; j < getTableModel().getColumnCount() && !found; j++) {
				// we dont compare all values (comparing all values is default)

				Object compareValue = getTableModel().getValueAt(table.convertRowIndexToModel(viewrow),
						table.convertColumnIndexToModel(j));

				if (compareValue == null) {
					found = val.isEmpty();
				} else if (fullTextSearchingWithAlternatives(alternativeWords,
						compareValue.toString().toLowerCase(Locale.ROOT))) {
					found = true;
				} else {
					// leave found false, value not found
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

	// TODO refactor, since always called with clientId
	public boolean moveToValue(Object value) {
		int viewrow = findViewRowFromValue(value);

		scrollRowToVisible(viewrow);

		return viewrow != -1;
	}

	public void scrollRowToVisible(int row) {
		Rectangle scrollTo = table.getCellRect(row, 0, false);
		table.scrollRectToVisible(scrollTo);
	}

	// KeyListener interface
	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
