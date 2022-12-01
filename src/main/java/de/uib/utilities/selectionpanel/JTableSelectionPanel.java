package de.uib.utilities.selectionpanel;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import de.uib.configed.ConfigedMain;
//import utils.TableSorter;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.MissingDataPanel;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;

public class JTableSelectionPanel extends JPanel
		implements DocumentListener, KeyListener, MissingDataPanel, ActionListener {
	JScrollPane scrollpane;
	JTable table; // we put a JTable on a standard JScrollPane
	// TableSorter sorter; //from the Sun Swing tutorial
	DefaultListSelectionModel selectionmodel;
	ConfigedMain main;
	java.util.List<RowSorter.SortKey> primaryOrderingKeys;

	JLabel labelSearch;
	CheckedLabel checkmarkSearch;
	CheckedLabel checkmarkSearchProgressive;

	JButton buttonMarkAll;
	JButton buttonInvertSelection;

	// CheckedLabel markReload;

	JTextField fieldSearch;
	JComboBox<String> comboSearch;
	JComboBox comboSearchMode;

	TablesearchPane.SearchMode searchMode;

	protected int hMin = 200;

	private int foundrow = -1;

	private int lastCountOfSearchWords = 0;

	private TreeMap<String, Integer> rowIndexMap;

	public JTableSelectionPanel(ConfigedMain main) {
		super();
		this.main = main;
		init();
	}

	protected void init() {
		searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES;

		scrollpane = new JScrollPane();

		scrollpane.getViewport().setBackground(Globals.backLightBlue);

		table = new JTable() {
			public Object getValueAt(int row, int col) {
				try {
					return super.getValueAt(row, col);
				} catch (Exception ex) {
					// logging.debug ("******** Exception in getValueAt: " + ex.toString());
					// after change of model (deleting of rows) the row sorter tries to rebuild
					// itself but fails if no values are supplied
					// we get a null pointer exception
					return "";
				}
			}

			/*
			 * public void tableChanged(javax.swing.event.TableModelEvent ev)
			 * {
			 * logging.debug (" ------- tableChanged");
			 * super.tableChanged(ev);
			 * 
			 * }
			 */
		};
		// sorter = new TableSorter( );
		// table = new JTable(sorter);
		table.setDragEnabled(true);
		table.setShowGrid(true);
		table.setGridColor(Color.white);
		table.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		table.setRowHeight(de.uib.utilities.Globals.tableRowHeight);
		// sorter.setTableHeader(table.getTableHeader());
		table.setAutoCreateRowSorter(true);

		primaryOrderingKeys = new ArrayList();
		primaryOrderingKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// table.getTableHeader().setToolTipText(configed.getResourceValue("MainFrame.tableheader_tooltip"));
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(table.getTableHeader().getDefaultRenderer()));
		// Ask to be notified of selection changes.
		selectionmodel = (DefaultListSelectionModel) table.getSelectionModel();
		// the default implementation in JTable yields this type

		table.setColumnSelectionAllowed(false);
		// true destroys setSelectedRow etc

		addListSelectionListener(main);

		table.addKeyListener(this);

		/*
		 * table.addMouseListener(
		 * new MouseAdapter() {
		 * public void mouseReleased(MouseEvent e) {
		 * }
		 * public void mousePressed(MouseEvent e) {
		 * if (e.isPopupTrigger()) {
		 * popupRequested(e.getComponent(), e.getX(), e.getY());
		 * }
		 * }
		 * }
		 * );
		 */

		/*
		 * 
		 * table.getTableHeader().addMouseListener(new MouseAdapter()
		 * {
		 * String[] savedSelection;
		 * public void mousePressed( MouseEvent e )
		 * {
		 * savedSelection = getSelectedValues();
		 * }
		 * public void mouseReleased(MouseEvent e) {
		 * SwingUtilities.invokeLater(new Runnable() {
		 * public void run() {
		 * // Wird erst aufgerufen, wenn alle
		 * // Aufgaben abgearbeitet wurden
		 * setSelectedValues(savedSelection);
		 * }
		 * });
		 * }
		 * 
		 * });
		 */

		scrollpane.getViewport().add(table);
		// test table.setVisible(false);

		JPanel topPane = new JPanel();

		// JLabel labelSearch = new
		// JLabel(configed.getResourceValue("JTableSelectionPanel.searchin"));
		// labelSearch.setPreferredSize(Globals.labelDimension);

		labelSearch = new JLabel(configed.getResourceValue("SearchPane.search"));
		labelSearch.setFont(Globals.defaultFont);

		Icon unselectedIconSearch = de.uib.configed.Globals.createImageIcon("images/loupe_light_16.png", "");
		Icon selectedIconSearch = de.uib.configed.Globals.createImageIcon("images/loupe_light_16_x.png", "");

		checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);
		checkmarkSearch.setToolTipText(configed.getResourceValue("SearchPane.checkmarkSearch.tooltip"));
		checkmarkSearch.addActionListener(this);
		checkmarkSearch.setChangeStateAutonomously(false);

		fieldSearch = new JTextField("");
		fieldSearch.setPreferredSize(Globals.textfieldDimension);
		fieldSearch.setFont(Globals.defaultFont);
		// fieldSearch.setBackground(Globals.backgroundLightGrey);
		fieldSearch.setBackground(Globals.backVeryLightBlue); // Globals.backLightYellow
		fieldSearch.getCaret().setBlinkRate(0);
		fieldSearch.getDocument().addDocumentListener(this);

		fieldSearch.addKeyListener(this);

		JPopupMenu searchMenu = new JPopupMenu();
		JMenuItemFormatted popupSearch = new JMenuItemFormatted();
		JMenuItemFormatted popupSearchNext = new JMenuItemFormatted();
		JMenuItemFormatted popupNewSearch = new JMenuItemFormatted();
		JMenuItemFormatted popupMarkHits = new JMenuItemFormatted();
		JMenuItemFormatted popupEmptySearchfield = new JMenuItemFormatted();
		searchMenu.add(popupSearch);
		searchMenu.add(popupSearchNext);
		searchMenu.add(popupNewSearch);
		searchMenu.add(popupMarkHits);
		searchMenu.add(popupEmptySearchfield);

		popupSearch.setText(configed.getResourceValue("JTableSelectionPanel.search"));
		popupSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTheRow();
			}
		});

		popupSearchNext.setText(configed.getResourceValue("JTableSelectionPanel.searchnext") + " ( F3 ) ");
		popupSearchNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTheNextRow();
			}
		});

		popupNewSearch.setText(configed.getResourceValue("JTableSelectionPanel.searchnew"));
		popupNewSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTheRow(0);
			}
		});

		popupMarkHits.setText(configed.getResourceValue("SearchPane.popup.markall"));// "JTableSelectionPanel.markall")
																						// + " ( F5 ) ");
		popupMarkHits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				markAll();
			}
		});

		popupEmptySearchfield.setText(configed.getResourceValue("JTableSelectionPanel.searchempty"));
		popupEmptySearchfield.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fieldSearch.setText("");
			}
		});

		fieldSearch.setComponentPopupMenu(searchMenu);

		fieldSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTheNextRow();
			}
		});

		Icon markAllIcon = de.uib.configed.Globals.createImageIcon("images/selection-all.png", "");
		Icon invertSelectionIcon = de.uib.configed.Globals.createImageIcon("images/selection-invert.png", "");
		buttonMarkAll = new JButton("", markAllIcon);
		buttonMarkAll.setToolTipText(configed.getResourceValue("SearchPane.popup.markall"));
		buttonInvertSelection = new JButton("", invertSelectionIcon);
		buttonInvertSelection.setToolTipText(configed.getResourceValue("SearchPane.invertselection"));

		buttonMarkAll.addActionListener((ActionEvent e) -> markAll());

		buttonInvertSelection.addActionListener((ActionEvent e) -> main.invertClientselection());

		JLabel labelSearchMode = new JLabel(configed.getResourceValue("JTableSelectionPanel.searchmode"));

		comboSearchMode = new JComboBoxToolTip();

		LinkedHashMap tooltipsMap = new LinkedHashMap();
		tooltipsMap.put(configed.getResourceValue("SearchPane.SearchMode.fulltext_with_alternatives"),
				configed.getResourceValue("SearchPane.SearchMode.fulltext_with_alternatives.tooltip"));

		tooltipsMap.put(configed.getResourceValue("SearchPane.SearchMode.fulltext_one_string"),
				configed.getResourceValue("SearchPane.SearchMode.fulltext_one_string.tooltip"));

		tooltipsMap.put(configed.getResourceValue("SearchPane.SearchMode.starttext"),
				configed.getResourceValue("SearchPane.SearchMode.starttext.tooltip"));

		tooltipsMap.put(configed.getResourceValue("SearchPane.SearchMode.regex"),
				configed.getResourceValue("SearchPane.SearchMode.regex.tooltip"));

		logging.info(this, " comboSearchMode tooltipsMap " + tooltipsMap);

		/*
		 * String[] searchModes = new String[]{
		 * configed.getResourceValue("JTableSelectionPanel.fulltext"),
		 * configed.getResourceValue("JTableSelectionPanel.starttext"),
		 * configed.getResourceValue("JTableSelectionPanel.regex")
		 * };
		 */

		((JComboBoxToolTip) comboSearchMode).setValues(tooltipsMap);
		comboSearchMode.setSelectedIndex(searchMode.ordinal());

		logging.info(this, "comboSearchMode set index to " + searchMode.ordinal());

		// comboSearchMode.setModel(new DefaultComboBoxModel(searchModes));
		comboSearchMode.setPreferredSize(Globals.buttonDimension);

		/*
		 * checkFulltext = new
		 * JCheckBox(configed.getResourceValue("JTableSelectionPanel.fulltext"));
		 * checkFulltext.setSelected(true);
		 * checkRegEx = new
		 * JCheckBox(configed.getResourceValue("JTableSelectionPanel.regex"));
		 * checkRegEx .setSelected(false);
		 */

		comboSearch = new JComboBox<>(
				new String[] { configed.getResourceValue("ConfigedMain.pclistTableModel.allfields") });
		comboSearch.setPreferredSize(Globals.buttonDimension);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);

		layoutTopPane
				.setHorizontalGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutTopPane.createSequentialGroup()
								.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)
								// .addComponent(markReload, GroupLayout.PREFERRED_SIZE,
								// GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
								// .addGap(Globals.minGapSize,Globals.minGapSize,Globals.minGapSize)
								.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(fieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(buttonMarkAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(buttonInvertSelection, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, 2 * Globals.minGapSize, 2 * Globals.minGapSize)
								.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(comboSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minGapSize, Globals.minGapSize, Globals.minGapSize)
								.addComponent(comboSearchMode, 100, 200, 300)
								.addGap(Globals.gapSize, Globals.gapSize, Globals.gapSize)));

		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						// .addComponent(markReload, 10, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonMarkAll, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonInvertSelection, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2));

		JPanel leftPane = this;
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(topPane, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpane, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	/*
	 * public int[] getModelToSortedView()
	 * {
	 * return sorter.getModelToView();
	 * }
	 * 
	 * public void clearSortingState()
	 * {
	 * sorter.clearSortingState();
	 * }
	 */

	public JTable getTable() {
		return table;
	}

	// interface MissingDataPanel
	public void setMissingDataPanel(boolean b) {
		if (b) {
			JLabel missingData0 = new JLabel(Globals.createImageIcon("images/opsi-logo.png", ""));

			JLabel missingData1 = new JLabel(configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));
			missingData1.setFont(Globals.defaultFontTitle);

			JLabel missingData2 = new JLabel(configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));
			missingData2.setFont(Globals.defaultFontTitle);
			/*
			 * JTextArea missingData2 = new JTextArea(configed.getResourceValue(
			 * "JTableSelectionPanel.missingDataPanel.label2"));
			 * missingData2.setFont(Globals.defaultFontTitle);
			 * missingData2.setLineWrap(true);
			 * missingData2.setEditable(false);
			 * missingData2.setBackground(Globals.backLightBlue);
			 * missingData2.setBorder(null);
			 * missingData2.setColumns(80);
			 */

			JPanel mdPanel = new JPanel();
			mdPanel.setBackground(Globals.backLightBlue);

			GroupLayout mdLayout = new GroupLayout(mdPanel);
			mdPanel.setLayout(mdLayout);

			mdLayout.setVerticalGroup(mdLayout.createSequentialGroup().addGap(10, 10, Short.MAX_VALUE)
					.addComponent(missingData0, 10, 80, 90).addComponent(missingData1, 10, 40, 90).addGap(10, 40, 40)
					.addComponent(missingData2, 10, 40, 80).addGap(10, 10, Short.MAX_VALUE));
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
		} else
			scrollpane.getViewport().setView(table);
	}

	public void setMissingDataPanel(boolean b, JComponent c) {
		if (b)
			scrollpane.getViewport().add(c);
		else
			scrollpane.getViewport().add(table);

	}

	public void addMouseListener(MouseListener l) {
		scrollpane.addMouseListener(l);
		table.addMouseListener(l);
	}

	public int convertRowIndexToModel(int i) {
		// logging.debug ( " ----- converting row index to model ");
		return table.convertRowIndexToModel(i);
	}

	public int convertRowIndexToView(int i) {
		return table.convertRowIndexToView(i);
	}

	public boolean isSelectionEmpty() {
		return (table.getSelectedRowCount() == 0);
	}

	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public Rectangle getCellRect(int row, int col, boolean includeSpacing) {
		return table.getCellRect(row, col, includeSpacing);
	}

	public Map<Integer, Integer> getSelectionMap() {
		Map<Integer, Integer> selectionMap = new HashMap<Integer, Integer>();
		int selectedKeysCount = 0;

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				selectionMap.put(selectedKeysCount, i);
				selectedKeysCount++;
			}
		}

		return selectionMap;

	}

	public TreeSet<String> getSelectedSet() {
		TreeSet<String> result = new TreeSet<String>();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				result.add((String) table.getValueAt(i, 0));
			}
		}

		return result;

	}

	public TreeSet<String> getInvertedSet() {
		TreeSet<String> result = new TreeSet<String>();

		for (int i = 0; i < table.getRowCount(); i++) {
			if (!selectionmodel.isSelectedIndex(i)) {
				result.add((String) table.getValueAt(i, 0));
			}
		}

		return result;

	}

	public void initColumnNames() {
		// logging.info(this, "initColumnNames");
		Object oldSelected = comboSearch.getSelectedItem();
		ArrayList<String> comboSearchItems = new ArrayList<String>();
		comboSearchItems.add(configed.getResourceValue("ConfigedMain.pclistTableModel.allfields"));

		try {
			logging.info(this, "initColumnNames columncount " + table.getColumnCount());

			for (int j = 0; j < table.getColumnCount(); j++) {
				logging.info(this, "initColumnName col " + j);
				logging.info(this, "initColumnName name  " + table.getColumnName(j));
				comboSearchItems.add(table.getColumnName(j));
			}

			comboSearch.setModel(new DefaultComboBoxModel<>(comboSearchItems.toArray(new String[0])));

			if (oldSelected != null)
				comboSearch.setSelectedItem(oldSelected);
		} catch (Exception ex) {
			logging.info(this, "initColumnNames " + ex);
		}
	}

	public ArrayList<String> getSelectedValues() {
		ArrayList<String> valuesList = new ArrayList<String>(table.getSelectedRowCount());

		for (int i = 0; i < table.getRowCount(); i++) {
			if (selectionmodel.isSelectedIndex(i)) {
				valuesList.add((String) table.getValueAt(i, 0));
			}
		}

		return valuesList;

		// return (String[]) values.toArray(new String[values.size()]);
	}

	public void clearSelection() {
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.clearSelection();
	}

	public void setSelectedValues(Collection<String> valuesList) {
		String valuesListS = null;
		if (valuesList != null)
			valuesListS = "" + valuesList.size();
		logging.info(this, "setSelectedValues " + valuesListS);
		ListSelectionModel lsm = table.getSelectionModel();
		lsm.clearSelection();

		if (valuesList == null || valuesList.size() == 0)
			return;

		TreeSet<String> valuesSet = new TreeSet<String>(valuesList);
		// because of ordering , we create a TreeSet view of the list

		logging.info(this, "setSelectedValues, (ordered) set of values, size " + valuesSet.size());

		int lastAddedI = -1;

		ListSelectionListener[] listeners = (ListSelectionListener[]) ((DefaultListSelectionModel) lsm)
				.getListeners(ListSelectionListener.class);

		// remove all listeners
		for (int l = 0; l < listeners.length; l++) {
			lsm.removeListSelectionListener(listeners[l]);
		}

		logging.info(this, "setSelectedValues, table.getRowCount() " + table.getRowCount());

		for (int i = 0; i < table.getRowCount(); i++) {
			logging.debug(this, "setSelectedValues checkValue for i " + i + ": " + (String) table.getValueAt(i, 0));

			if (valuesSet.contains(table.getValueAt(i, 0))) {
				lsm.addSelectionInterval(i, i);
				lastAddedI = i;
				logging.debug(this, "setSelectedValues add interval " + i);
			}
		}

		// logging.info(this, "setSelectedValues remove point " + lastAddedI);
		lsm.removeSelectionInterval(lastAddedI, lastAddedI);

		// get again the listeners
		for (int l = 0; l < listeners.length; l++) {
			lsm.addListSelectionListener(listeners[l]);
		}

		// and repeat the last addition
		if (lastAddedI > -1) {
			// logging.info(this, "setSelectedValues add point " + lastAddedI);
			lsm.addSelectionInterval(lastAddedI, lastAddedI);
		}

		table.repaint();

		if (!valuesSet.isEmpty()) {
			Object valueToFind = valuesSet.iterator().next();
			moveToValue(valueToFind, 0);
		}

		logging.info(this, "setSelectedValues  produced " + getSelectedValues().size());

	}

	public void setSelectedValues(String[] values) {
		TreeSet<String> valueList = new TreeSet<String>();
		for (int i = 0; i < values.length; i++) {
			valueList.add(values[i]);
		}

		setSelectedValues(valueList);
	}

	public void selectAll() {
		selectionmodel.setSelectionInterval(0, table.getRowCount());
	}

	public void initSortKeys() {
		table.getRowSorter().setSortKeys(primaryOrderingKeys);
	}

	public java.util.List<? extends RowSorter.SortKey> getSortKeys() {
		return table.getRowSorter().getSortKeys();
	}

	public void setSortKeys(java.util.List<? extends RowSorter.SortKey> orderingKeys) {
		table.getRowSorter().setSortKeys(orderingKeys);
	}

	public void buildRowIndexByCol(int i) {
		int row = 0;

		rowIndexMap = new TreeMap<String, Integer>();

		while (row < getTableModel().getRowCount()) {
			rowIndexMap.put((String) getTableModel().getValueAt(row, i), row);
		}
	}

	public void setValueForKey(Object value, String key, int colInModelTerms) {
		getTableModel().setValueAt(value, rowIndexMap.get(key), colInModelTerms);
	}

	public Set<String> getColumnValues(int col) {
		HashSet<String> result = new HashSet<String>();
		if (table.getModel() == null || table.getModel().getColumnCount() <= col)
			return result;

		for (int i = 0; i < table.getModel().getRowCount(); i++) {
			result.add("" + table.getModel().getValueAt(i, col));
		}

		return result;
	}

	public void setModel(TableModel tm) {
		logging.info(this, "set model with column count " + tm.getColumnCount());
		/*
		 * ArrayList<String> cols = new ArrayList<String>();
		 * for (int i = 0; i < tm.getColumnCount(); i++)
		 * cols.add( tm.getColumnName(i) );
		 * logging.info(this, "set model " + cols);
		 */
		logging.info(this, " [JTableSelectionPanel] setModel with row count " + tm.getRowCount());

		tm.addTableModelListener(table);
		// ((AbstractTableModel) tm).fireTableStructureChanged();

		logging.info(this, "setModel all hosts size "
				+ main.getPersistenceController().getHostInfoCollections().getMapOfAllPCInfoMaps().size());

		// logging.debug(" --- setting model, row count " + table.getRowCount());;

		table.setModel(tm);

		/*
		 * java.text.Collator alphaCollator = java.text.Collator.getInstance();
		 * alphaCollator.setStrength(java.text.Collator.PRIMARY);
		 * 
		 * ((TableRowSorter) table.getRowSorter()).setComparator(0,
		 * de.uib.utilities.Globals.alphaCollator);
		 */

	}

	public DefaultTableModel getSelectedRowsModel() {
		final Map<Integer, Integer> selectionMap = getSelectionMap();

		DefaultTableModel m = new DefaultTableModel() {
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

		return m;
	}

	public DefaultTableModel getTableModel() {
		return (DefaultTableModel) table.getModel();
	}

	public TableColumnModel getColumnModel() {
		return table.getColumnModel();
	}

	public ListSelectionModel getListSelectionModel() {
		return table.getSelectionModel();
	}

	public void listvalueChanged(ListSelectionEvent e) {
		main.valueChanged(e);
	}

	public void addListSelectionListener(ListSelectionListener lisel) {
		selectionmodel.addListSelectionListener(lisel);
	}

	public void removeListSelectionListener(ListSelectionListener lisel) {
		selectionmodel.removeListSelectionListener(lisel);
	}

	public void fireListSelectionEmpty(Object source) {
		for (int i = 0; i < selectionmodel.getListSelectionListeners().length; i++) {
			ListSelectionListener[] listen = selectionmodel.getListSelectionListeners();
			listen[i].valueChanged(new ListSelectionEvent(source, 0, 0, false));
		}
	}

	/*
	 * public void fireListSelectionChanged(Object source)
	 * {
	 * for (int i = 0; i < selectionmodel.getListSelectionListeners().length; i++)
	 * {
	 * ListSelectionListener[] listen = selectionmodel.getListSelectionListeners();
	 * listen[i].valueChanged(new ListSelectionEvent(source, 0,
	 * getModel().getRowCount()-1, false));
	 * }
	 * }
	 */

	public int findModelRowFromValue(Object value, int col) {
		int result = -1;

		if (value == null)
			return result;

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

			if (!found)
				row++;
		}

		return result;
	}

	protected int findViewRowFromValue(int startviewrow, Object value, Set colIndices) {
		return findViewRowFromValue(startviewrow, value, colIndices, searchMode);
	}

	/*
	 * protected int findViewRowFromValue(int startviewrow, Object value, Set
	 * colIndices, boolean fulltext)
	 * {
	 * return findViewRowFromValue(startviewrow, value, colIndices, fulltext,
	 * false);
	 * }
	 */

	private java.util.List<String> getWords(String line) {
		ArrayList<String> result = new ArrayList<String>();
		String[] splitted = line.split("\\s+");
		for (String s : splitted) {
			if (!s.equals(" "))
				result.add(s);
		}
		return result;
	}

	protected int findViewRowFromValue(final int startviewrow, Object value, Set colIndices,
			TablesearchPane.SearchMode searchMode) {

		logging.info(this, "findViewRowFromValue(int startviewrow, Object value, Set colIndices, searchMode: "
				+ startviewrow + ", " + value + ", " + colIndices + ", " + searchMode);

		if (value == null)
			return -1;

		String val = value.toString();

		String valLower = val.toLowerCase();

		boolean found = false;

		int viewrow = 0;

		if (startviewrow > 0)
			viewrow = startviewrow;

		// describe search parameters

		boolean fulltext = (searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES
				|| searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_ONE_STRING);
		// with another data configuration, it could be combined with regex

		// get pattern for regex search mode if needed
		Pattern pattern = null;
		if (searchMode == TablesearchPane.SearchMode.REGEX_SEARCHING) {
			try {
				if (fulltext)
					val = ".*" + val + ".*";
				pattern = Pattern.compile(val);
			} catch (java.util.regex.PatternSyntaxException ex) {
				logging.error(this, "pattern problem " + ex);
				return -1;
			}
		}

		java.util.List<String> alternativeWords = getWords(valLower);
		lastCountOfSearchWords = alternativeWords.size();

		while (!found && viewrow < getTableModel().getRowCount()) {
			// logging.info(this, "findViewRowFromValue loop with viewrow, alternativeWords
			// "
			// + viewrow + ", " + alternativeWords
			// );

			for (int j = 0; j < getTableModel().getColumnCount(); j++) {
				if (colIndices != null // we dont compare all values (comparing all values is default)
						&& !colIndices.contains(j))
					continue;

				Object compareValue = getTableModel().getValueAt(

						table.convertRowIndexToModel(viewrow), table.convertColumnIndexToModel(j)

				);

				// logging.info(this, "findViewRowFromValue compare " + value + " to " +
				// compareValue);

				if (compareValue == null) {
					found = (val == null || val.equals(""));
				}

				else {
					String compareVal = ("" + compareValue).toLowerCase();

					switch (searchMode) {
					case REGEX_SEARCHING: {
						// logging.info(this, " try to match " + value + " with " + compareVal);
						found = pattern.matcher(compareVal).matches();
						break;
					}

					case FULL_TEXT_SEARCHING_WITH_ALTERNATIVES: {
						for (String word : alternativeWords) {
							// logging.info(this, "findViewRowFromValue compare in column " + j + ", " +
							// word+ " to " + compareVal);
							found = (compareVal.indexOf(word) >= 0);
							if (found)
								break;
						}
						break;
					}

					case FULL_TEXT_SEARCHING_ONE_STRING: {
						found = (compareVal.indexOf(valLower) >= 0);
						break;
					}

					default: {
						found = compareVal.startsWith(valLower);
					}
					}
				}

				if (found) {
					// logging.info(this, "findViewRowFromValue identified " + value );
					break;
				}
			}

			if (!found)
				viewrow++;

		}

		/*
		 * logging.info(this,
		 * "findViewRowFromValue loop with  viewrow,  alternativeWords, found "
		 * + viewrow + ",  " + alternativeWords + ", " + found
		 * );
		 */

		if (found) {
			return viewrow;
		}

		return -1;
	}

	/*
	 * protected int findViewRowFromValue(int startviewrow, Object value, Set
	 * colIndices, boolean fulltext, boolean regex)
	 * {
	 * logging.info(this,
	 * "findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext, boolean regex): "
	 * + startviewrow + ", " + value + ", " + colIndices
	 * + ", " + fulltext + ", " + regex);
	 * 
	 * 
	 * if (value == null)
	 * return -1;
	 * 
	 * String val = value.toString();
	 * 
	 * String valLower = val.toLowerCase();
	 * 
	 * boolean found = false;
	 * 
	 * int viewrow = 0;
	 * 
	 * if (startviewrow > 0)
	 * viewrow = startviewrow;
	 * 
	 * Pattern pattern = null;
	 * if (regex)
	 * {
	 * try{
	 * if (fulltext)
	 * val = ".*" + val + ".*";
	 * pattern = Pattern.compile(val);
	 * }
	 * catch(java.util.regex.PatternSyntaxException ex)
	 * {
	 * logging.info(this, "pattern problem " +ex);
	 * return -1;
	 * }
	 * }
	 * 
	 * while (!found && viewrow < getTableModel().getRowCount())
	 * {
	 * for (int j=0; j< getTableModel().getColumnCount(); j++)
	 * {
	 * 
	 * if (
	 * colIndices !=null //we dont compare all values (comparing all values is
	 * default)
	 * &&
	 * !colIndices.contains(j)
	 * )
	 * 
	 * //if (j != 0) //test
	 * continue;
	 * 
	 * Object compareValue =
	 * getTableModel().getValueAt(
	 * 
	 * table.convertRowIndexToModel(viewrow),
	 * table.convertColumnIndexToModel(j)
	 * 
	 * );
	 * 
	 * //logging.info(this, "findViewRowFromValue compare " + value + " to " +
	 * compareValue);
	 * 
	 * if (compareValue == null)
	 * {
	 * if (val == null || val.equals(""))
	 * found = true;
	 * }
	 * 
	 * else
	 * {
	 * String compareVal = "" + compareValue;
	 * 
	 * if (regex)
	 * {
	 * //logging.info(this, " try to match " + value + " with " + compareVal);
	 * if ( pattern.matcher(compareVal).matches() )
	 * found = true;
	 * }
	 * 
	 * else
	 * {
	 * compareVal = compareVal.toLowerCase();
	 * 
	 * if (fulltext)
	 * {
	 * if (compareVal.indexOf(val.toLowerCase()) >= 0)
	 * found = true;
	 * }
	 * 
	 * else
	 * {
	 * if (compareVal.startsWith(valLower))
	 * found = true;
	 * }
	 * }
	 * }
	 * 
	 * if (found)
	 * {
	 * //logging.info(this, "findViewRowFromValue identified " + value );
	 * break;
	 * }
	 * }
	 * 
	 * if (!found)
	 * viewrow++;
	 * 
	 * }
	 * 
	 * 
	 * if (found)
	 * {
	 * return viewrow;
	 * }
	 * 
	 * return -1;
	 * }
	 */

	public boolean moveToValue(Object value, int col) {
		HashSet<Integer> cols = new HashSet<Integer>();
		cols.add(col);
		int viewrow = findViewRowFromValue(0, value, cols);

		scrollRowToVisible(viewrow);
		// Rectangle scrollTo = table.getCellRect(viewrow, 0, false);
		if (viewrow == -1)
			return false;

		return true;
	}

	public void scrollRowToVisible(int row) {
		// table.scrollRectToVisible(table.getCellRect(row, 0, false));

		Rectangle scrollTo = table.getCellRect(row, 0, false);
		table.scrollRectToVisible(scrollTo);
		// logging.info(this, " srollto " + scrollTo);

		/*
		 * does not work properly since we pass correct locations
		 * scrollTo.setLocation(0, (int) (scrollTo.getY() +
		 * scrollpane.getViewport().getExtentSize().getHeight()) - 30 );
		 * table.scrollRectToVisible(scrollTo);
		 * 
		 * logging.info(this, " srollto " + scrollTo);
		 */

	}

	public void addSelectedRow(int row) {
		if (table.getRowCount() == 0)
			return;
		// table.requestFocus();
		table.addRowSelectionInterval(row, row);
		// debug logging.error("addSelectedRow " + row);
		// logging.debug(" --- view row selected " + row);
		scrollRowToVisible(row);
		// table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}

	public void setSelectedRow(int row) {
		if (table.getRowCount() == 0)
			return;

		if (row == -1) {
			table.clearSelection();
			return;
		}
		// table.requestFocus();
		table.setRowSelectionInterval(row, row);
		// debug logging.error("setSelectedRow " + row);
		// logging.debug(" --- view row selected " + row);
		scrollRowToVisible(row);
		// table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}

	private void searchTheNextRow() {
		searchTheNextRow(false);
	}

	private void markAll() {
		table.clearSelection();
		searchTheRow(0);
		int startFoundrow = foundrow;
		// logging.info(this, "markAll foundrow " + foundrow);
		foundrow = foundrow + 1;
		while (foundrow > startFoundrow) {
			searchTheNextRow(true); // adding the next row to selection
			// logging.info(this, "markAll foundrow " + foundrow);
		}
	}

	private void searchTheNextRow(boolean addSelection) {
		int startrow = 0;
		if (table.getSelectedRow() >= 0)
			startrow = table.getSelectedRows()[table.getSelectedRows().length - 1] + 1;

		if (startrow >= table.getRowCount())
			startrow = 0;

		searchTheRow(startrow, addSelection);

		if (foundrow == -1)
			searchTheRow(0, addSelection);
	}

	private void searchTheRow() {
		searchTheRow(table.getSelectedRow());
	}

	private void searchTheRow(int startrow) {
		searchTheRow(startrow, false);
	}

	private void searchTheRow(int startrow, boolean addSelection) {
		String value = fieldSearch.getText();

		if (value.length() > 10 && value.substring(0, 4).equalsIgnoreCase("http") && value.indexOf("host=") > 0)
			value = value.substring(value.indexOf("host=") + ("host=").length());

		// logging.info(this, " searchTheRow for >>" + value + "<<");

		HashSet<Integer> selectedCols = null;
		// selectedCols = new HashSet<Integer>();

		if (comboSearch.getSelectedIndex() > 0) {
			selectedCols = new HashSet<Integer>();
			selectedCols.add(getTableModel().findColumn((String) comboSearch.getSelectedItem()));
		}
		/*
		 * else
		 * {
		 * selectedCols.add(0);
		 * }
		 */

		// logging.info(this, "markAll foundrow " + foundrow);

		searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES;
		switch (comboSearchMode.getSelectedIndex()) {
		// case 0 :;
		case 0:
			searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES;
			break;
		case 1:
			searchMode = TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_ONE_STRING;
			break;
		case 2:
			searchMode = TablesearchPane.SearchMode.START_TEXT_SEARCHING;
			break;
		case 3:
			searchMode = TablesearchPane.SearchMode.REGEX_SEARCHING;
			break;
		}

		// foundrow = findViewRowFromValue(startrow, value, selectedCols,
		// fulltextSearchWithOneString, regexSearch);
		foundrow = findViewRowFromValue(startrow, value, selectedCols, searchMode);

		if (foundrow > -1) {
			if (addSelection)
				addSelectedRow(foundrow);

			else
				setSelectedRow(foundrow);
		}
	}

	public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
		return table.getDefaultRenderer(columnClass);
	}

	private void searchOnDocumentChange() {
		if (fieldSearch.getText().equals("")) {
			setSelectedRow(0);
			lastCountOfSearchWords = 0;
		} else {
			if (searchMode == TablesearchPane.SearchMode.FULL_TEXT_SEARCHING_WITH_ALTERNATIVES
					&& getWords(fieldSearch.getText()).size() > lastCountOfSearchWords)
				// when a new search word is added instead of extending one
				// the condition is not refined but widened; and we have to start the search new
				setSelectedRow(0);

			searchTheRow();
		}
	}

	// DocumentListener interface
	public void changedUpdate(DocumentEvent e) {
		// logging.info(this, "changedUpdate fieldSearch.getText().length() > 0 " +
		// (fieldSearch.getText().length() > 0 ) );
		if (e.getDocument() == fieldSearch.getDocument()) {

			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			searchOnDocumentChange();

		}

	}

	public void insertUpdate(DocumentEvent e) {
		// logging.info(this, "insertUpdate");
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			searchOnDocumentChange();

		}

	}

	public void removeUpdate(DocumentEvent e) {
		// logging.info(this, "removeUpdate");
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);
			searchOnDocumentChange();
		}
	}

	// for overwriting in subclass
	protected void keyPressedOnTable(KeyEvent e) {
	}

	// KeyListener interface
	public void keyPressed(KeyEvent e) {
		// logging.info(this, "keypressed source " + e.getSource());

		if (!(e.getSource() instanceof JTextField))
			keyPressedOnTable(e);

		// logging.info(this, "keypressed " + e);
		if (e.getKeyCode() == KeyEvent.VK_F5) {
			if (!fieldSearch.getText().equals(""))
				markAll();
		}

		else if (e.getKeyCode() == KeyEvent.VK_F3) {
			// logging.info(this, "keypressed search next row");
			if (!fieldSearch.getText().equals(""))
				searchTheNextRow();
		}

		else if (e.getKeyCode() == KeyEvent.VK_F3) {
			// logging.info(this, "keypressed search next row");
			if (!fieldSearch.getText().equals(""))
				searchTheNextRow();
		}

		else if (e.getKeyCode() == KeyEvent.VK_I && e.isControlDown()) {
			main.invertClientselection();
		}

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// ActionListener implementation
	public void actionPerformed(ActionEvent e) {
		// logging.info(this, "ActionEvent " + e);
		if (e.getSource() == checkmarkSearch) {
			// logging.debug(this, "actionPerformed on checkmarkSearch,
			// targetModel.isFiltered " + targetModel.isFiltered());

			// if (checkmarkSearch.isSelected())
			fieldSearch.setText("");
		}
	}

}
