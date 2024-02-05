/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.table.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.swing.NavigationPanel;
import utils.Utils;

public class TableSearchPane extends JPanel implements DocumentListener, KeyListener {
	private static final int BLINK_RATE = 0;
	private static final String FULL_TEXT_SEARCH_PROPERTY = "fullTextSearch";
	private static final String ALL_COLUMNS_SEARCH_PROPERTY = "allColumnsSearch";
	private static final String PROGRESSIVE_SEARCH_PROPERTY = "progressiveSearch";

	public static final int FULL_TEXT_SEARCH = 0;
	public static final int START_TEXT_SEARCH = 1;
	public static final int REGEX_SEARCH = 2;

	private JFrame masterFrame = ConfigedMain.getMainFrame();

	private JTextField fieldSearch;

	private boolean filtering;

	private JComboBox<String> comboSearchFields;
	private JComboBoxToolTip comboSearchFieldsMode;

	private JLabel labelSearch;
	private CheckedLabel checkmarkSearch;
	private CheckedLabel checkmarkSearchProgressive;
	private JLabel labelSearchMode;
	private CheckedLabel filtermark;
	private CheckedLabel checkmarkAllColumns;
	private CheckedLabel checkmarkFullText;

	private JLabel labelFilterMarkGap;

	private NavigationPanel navPane;
	private PanelGenEditTable associatedPanel;

	private Map<JMenuItem, Boolean> searchMenuEntries;

	private JMenuItem popupSearch;
	private JMenuItem popupSearchNext;
	private JMenuItem popupMarkHits;
	private JMenuItem popupMarkAndFilter;
	private JMenuItem popupEmptySearchfield;

	private boolean withRegEx = true;
	private boolean selectMode = true;
	private boolean resetFilterModeOnNewSearch = true;

	private int foundrow = -1;

	private SearchTargetModel targetModel;

	private final Comparator<Object> comparator;

	private enum SearchInputType {
		LINE, PROGRESSIVE
	}

	private SearchInputType searchInputType = SearchInputType.PROGRESSIVE;

	private String savedStatesObjectTag;

	private boolean filteredMode;

	/**
	 * @param SearchTargetModel the model for delivering data and selecting
	 * @param boolean           modifies the search function
	 * @param String            saving of states is activated, the keys are
	 *                          tagged with the parameter
	 */
	public TableSearchPane(SearchTargetModel targetModel, boolean withRegEx, String savedStatesObjectTag) {
		this(null, targetModel, withRegEx, savedStatesObjectTag);
	}

	/**
	 * a search target model is produces for the table in PanelGenEditTable,
	 * keeping access tho PanelGenEditTable public methods
	 * 
	 * @param JTable  the model for delivering data and selecting
	 * @param boolean
	 * @param String  saving of states is activated, the keys are tagged with
	 *                the parameter
	 */
	public TableSearchPane(PanelGenEditTable thePanel, boolean withRegEx, String savedStatesObjectTag) {
		this(thePanel, new SearchTargetModelFromTable(thePanel), withRegEx, savedStatesObjectTag);
	}

	public TableSearchPane(PanelGenEditTable thePanel, SearchTargetModel targetModel, boolean withRegEx,
			String savedStatesObjectTag) {
		associatedPanel = thePanel;
		this.targetModel = targetModel;
		this.withRegEx = withRegEx;
		filtering = true;
		this.savedStatesObjectTag = savedStatesObjectTag;

		comparator = getCollator();

		initSavedStates();
		init();
	}

	/**
	 * a search target model is produces from a JTable the regex parameter
	 * default false is used
	 * 
	 * @param SearchTargetModel
	 * @param String            saving of states is activated, the keys are
	 *                          tagged with the parameter
	 */
	public TableSearchPane(SearchTargetModel targetModel, String savedStatesObjectTag) {
		this(targetModel, false, savedStatesObjectTag);
	}

	private void init() {
		initComponents();
		setupLayout();

		setSearchFieldsAll();
		setNarrow(false);
	}

	private static Collator getCollator() {
		Collator alphaCollator = Collator.getInstance();
		alphaCollator.setStrength(Collator.IDENTICAL);
		return alphaCollator;
	}

	/**
	 * sets frame to return to e.g. from option dialogs
	 * 
	 * @param javax.swing.JFrame
	 */
	public void setMasterFrame(JFrame masterFrame) {
		this.masterFrame = masterFrame;
	}

	private void initSavedStates() {
		if (savedStatesObjectTag != null) {
			Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY, "0");
			Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY, "0");
			Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY, "0");
		}
	}

	public void setWithNavPane(boolean b) {
		navPane.setVisible(b);
	}

	public void setFiltering(boolean b, boolean withFilterPopup) {
		searchMenuEntries.put(popupMarkHits, true);
		if (withFilterPopup) {
			searchMenuEntries.put(popupMarkAndFilter, true);
		}

		buildMenuSearchfield();

		filtering = b;
	}

	public void setFiltering(boolean b) {
		setFiltering(b, true);
	}

	public void showFilterIcon(boolean b) {
		filtermark.setVisible(b);
		labelFilterMarkGap.setVisible(b);
	}

	public void setSelectMode(boolean select) {
		this.selectMode = select;
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		checkmarkAllColumns.setEnabled(b);
		checkmarkFullText.setEnabled(b);
		fieldSearch.setEnabled(b);
	}

	private boolean disabledSinceWeAreInFilteredMode() {
		if (filteredMode) {
			Logging.info(this, "disabledSinceWeAreInFilteredMode masterFrame " + masterFrame);
			JOptionPane.showOptionDialog(masterFrame, Configed.getResourceValue("SearchPane.filterIsSet.message"),
					Configed.getResourceValue("SearchPane.filterIsSet.title"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null, null, null);
		}

		return filteredMode;
	}

	/**
	 * serve graphical filtermark
	 */
	public void setFilterMark(boolean b) {
		filtermark.setSelected(b);
	}

	public void setFiltered(boolean filtered) {
		targetModel.setFiltered(filtered);
		setFilteredMode(filtered);
	}

	/**
	 * express filter status in graphical components
	 */
	public void setFilteredMode(boolean b) {
		filteredMode = b;
		popupSearch.setEnabled(!b);
		popupSearchNext.setEnabled(!b);

		popupMarkHits.setEnabled(!b);
		popupMarkAndFilter.setEnabled(!b);
		popupEmptySearchfield.setEnabled(!b);
		setFilterMark(b);
	}

	public boolean isFilteredMode() {
		return filteredMode;
	}

	public void setNarrow(boolean b) {
		showFilterIcon(b);
		checkmarkSearchProgressive.setVisible(b);
		checkmarkAllColumns.setVisible(b);
		checkmarkFullText.setVisible(b);
		comboSearchFields.setVisible(!b);
		comboSearchFieldsMode.setVisible(!b);
		labelSearch.setVisible(!b);
		labelSearchMode.setVisible(!b);
	}

	public void setTargetModel(SearchTargetModel searchTargetModel) {
		this.targetModel = searchTargetModel;
	}

	public void setToolTipTextCheckMarkAllColumns(String s) {
		checkmarkAllColumns.setToolTipText(s);
	}

	private void initComponents() {
		navPane = new NavigationPanel(associatedPanel);

		navPane.setVisible(false);

		labelSearch = new JLabel(Configed.getResourceValue("SearchPane.search"));

		Icon unselectedIconSearch = Utils.createImageIcon("images/loupe_light_16.png", "");
		Icon selectedIconSearch = Utils.createImageIcon("images/loupe_light_16_x.png", "");

		checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);

		checkmarkSearch.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearch.tooltip"));
		checkmarkSearch.addActionListener(event -> fieldSearch.setText(""));
		checkmarkSearch.setChangeStateAutonomously(false);

		selectedIconSearch = Utils.createImageIcon("images/loupe_light_16_progressiveselect.png", "");
		unselectedIconSearch = Utils.createImageIcon("images/loupe_light_16_blockselect.png", "");

		boolean active = true;

		if (Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY) != null) {
			active = Integer.valueOf(Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY)) == 0;
		}

		checkmarkSearchProgressive = new CheckedLabel(selectedIconSearch, unselectedIconSearch, active);
		if (active) {
			searchInputType = SearchInputType.PROGRESSIVE;
		} else {
			searchInputType = SearchInputType.LINE;
		}

		checkmarkSearchProgressive.addActionListener(event -> checkmarkSearchProgressiveEvent());
		checkmarkSearchProgressive.setChangeStateAutonomously(true);
		checkmarkSearchProgressive
				.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearchProgressive.tooltip"));

		fieldSearch = new JTextField();
		fieldSearch.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		fieldSearch.getCaret().setBlinkRate(BLINK_RATE);
		fieldSearch.setToolTipText(Configed.getResourceValue("SearchPane.searchField.toolTip"));

		fieldSearch.getDocument().addDocumentListener(this);

		fieldSearch.addKeyListener(this);

		popupSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.search"));
		popupSearch.addActionListener(actionEvent -> searchTheRow(selectMode));

		popupSearchNext = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnext"));
		popupSearchNext.addActionListener(actionEvent -> searchNextRow(selectMode));

		JMenuItem popupNewSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnew"));
		popupNewSearch.addActionListener((ActionEvent actionEvent) -> {
			targetModel.setFiltered(false);
			if (resetFilterModeOnNewSearch) {
				setFilteredMode(false);
			}

			searchTheRow(0, selectMode);
		});

		popupMarkHits = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markall"));
		popupMarkHits.addActionListener((ActionEvent actionEvent) -> markAll());

		popupMarkAndFilter = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markAndFilter"));
		popupMarkAndFilter.addActionListener((ActionEvent actionEvent) -> {
			switchFilterOff();
			markAllAndFilter();
			switchFilterOn();
		});

		popupEmptySearchfield = new JMenuItem(Configed.getResourceValue("SearchPane.popup.empty"));
		popupEmptySearchfield.addActionListener(actionEvent -> fieldSearch.setText(""));

		searchMenuEntries = new LinkedHashMap<>();
		searchMenuEntries.put(popupSearch, true);
		searchMenuEntries.put(popupSearchNext, true);
		searchMenuEntries.put(popupNewSearch, true);
		searchMenuEntries.put(popupMarkHits, false);
		searchMenuEntries.put(popupMarkAndFilter, false);

		searchMenuEntries.put(popupEmptySearchfield, true);
		buildMenuSearchfield();

		fieldSearch.addActionListener((ActionEvent actionEvent) -> {
			if (searchInputType == SearchInputType.PROGRESSIVE) {
				searchNextRow(selectMode);
			}
		});

		comboSearchFields = new JComboBox<>(new String[] { Configed.getResourceValue("SearchPane.search.allfields") });
		comboSearchFields.setPreferredSize(Globals.BUTTON_DIMENSION);

		setSearchFieldsAll();

		labelSearchMode = new JLabel(Configed.getResourceValue("SearchPane.searchmode.searchmode"));

		Map<String, String> tooltipsMap = new LinkedHashMap<>();

		tooltipsMap.put(Configed.getResourceValue("SearchPane.searchmode.fulltext"),
				Configed.getResourceValue("SearchPane.mode.fulltext.tooltip"));
		tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.starttext"),
				Configed.getResourceValue("SearchPane.mode.starttext.tooltip"));
		if (withRegEx) {
			tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.regex"),
					Configed.getResourceValue("SearchPane.mode.regex.tooltip"));
		}

		comboSearchFieldsMode = new JComboBoxToolTip();

		comboSearchFieldsMode.setValues(tooltipsMap, false);
		comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);

		comboSearchFieldsMode.setPreferredSize(Globals.BUTTON_DIMENSION);

		Icon unselectedIconFilter = Utils.createImageIcon("images/filter_14x14_open.png", "");
		Icon selectedIconFilter = Utils.createImageIcon("images/filter_14x14_closed.png", "");

		filtermark = new CheckedLabel(selectedIconFilter, unselectedIconFilter, false);
		filtermark.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filtermark.addActionListener(event -> filtermarkEvent());

		labelFilterMarkGap = new JLabel();

		showFilterIcon(filtering);

		Icon unselectedIcon;
		Icon selectedIcon;

		unselectedIcon = Utils.createImageIcon("images/loupe_light_16_singlecolumnsearch.png", "");
		selectedIcon = Utils.createImageIcon("images/loupe_light_16_multicolumnsearch.png", "");

		active = true;
		if (Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY) != null) {
			active = Integer.valueOf(Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY)) == 0;
		}

		checkmarkAllColumns = new CheckedLabel(selectedIcon, unselectedIcon, active);

		checkmarkAllColumns.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkAllColumns.tooltip"));
		checkmarkAllColumns.addActionListener(event -> checkmarkAllColumnsEvent());

		unselectedIcon = Utils.createImageIcon("images/loupe_light_16_starttextsearch.png", "");
		selectedIcon = Utils.createImageIcon("images/loupe_light_16_fulltextsearch.png", "");

		active = true;
		if (Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY) != null) {
			active = Integer.valueOf(
					Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY)) == 0;
		}

		checkmarkFullText = new CheckedLabel(selectedIcon, unselectedIcon, active);

		if (active) {
			comboSearchFieldsMode.setSelectedIndex(FULL_TEXT_SEARCH);
		} else {
			comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);
		}

		checkmarkFullText.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkFullText.tooltip"));
		checkmarkFullText.addActionListener(event -> checkmarkFullTextEvent());
	}

	private void setupLayout() {
		GroupLayout layoutTablesearchPane = new GroupLayout(this);
		this.setLayout(layoutTablesearchPane);

		int checkedLabelWidth = 18;
		layoutTablesearchPane.setHorizontalGroup(layoutTablesearchPane.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(fieldSearch, Globals.ICON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(filtermark, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
				.addComponent(labelFilterMarkGap, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(checkmarkSearchProgressive, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(checkmarkAllColumns, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
				.addComponent(checkmarkFullText, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutTablesearchPane.setVerticalGroup(layoutTablesearchPane.createParallelGroup(Alignment.CENTER)
				.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(filtermark, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(labelFilterMarkGap, 10, 10, 10)
				.addComponent(checkmarkAllColumns, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkmarkFullText, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkmarkSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkmarkSearchProgressive, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(labelSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSearchFieldsMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private void buildMenuSearchfield() {
		Logging.info(this, "buildMenuSearchfield");
		JPopupMenu searchMenu = new JPopupMenu();
		for (Entry<JMenuItem, Boolean> searchMenuEntry : searchMenuEntries.entrySet()) {
			if (Boolean.TRUE.equals(searchMenuEntry.getValue())) {
				searchMenu.add(searchMenuEntry.getKey());
			}
		}
		fieldSearch.setComponentPopupMenu(searchMenu);
		Logging.info(this, "buildMenuSearchfield " + searchMenu);
	}

	private boolean allowSearchAction() {
		return !disabledSinceWeAreInFilteredMode() && !fieldSearch.getText().isEmpty();
	}

	private void retainOnlyAllFieldsItem() {
		comboSearchFields.removeAllItems();
		comboSearchFields.addItem(Configed.getResourceValue("SearchPane.search.allfields"));
	}

	public void setSearchFields(Integer[] cols) {
		retainOnlyAllFieldsItem();

		for (int col : cols) {
			comboSearchFields.addItem(targetModel.getColumnName(col));
		}
	}

	public void setSearchFieldsAll() {
		Logging.debug(this, "setSearchFieldsAll " + targetModel);
		Logging.debug(this, "setSearchFieldsAll target model col count " + targetModel.getColumnCount());

		retainOnlyAllFieldsItem();

		for (int i = 0; i < targetModel.getColumnCount(); i++) {
			String colname = targetModel.getColumnName(i);
			comboSearchFields.addItem(colname);
		}

		comboSearchFields.setSelectedIndex(0);
	}

	public void setSearchMode(int a) {
		if (a <= START_TEXT_SEARCH) {
			comboSearchFieldsMode.setSelectedIndex(a);
		} else {
			if (withRegEx) {
				comboSearchFieldsMode.setSelectedIndex(REGEX_SEARCH);
			}
		}
	}

	@Override
	public void requestFocus() {
		fieldSearch.requestFocusInWindow();
	}

	private static class Finding {
		boolean success;
		int endChar = -1;
	}

	private Finding stringContainsParts(final String s, String[] parts) {
		Finding result = new Finding();

		if (s == null || parts == null) {
			return result;
		}

		int len = parts.length;
		if (len == 0) {
			result.success = true;
			return result;
		}

		int i = 0;
		boolean searching = true;
		Finding partSearch;

		String remainder = s;

		while (searching) {
			partSearch = stringContains(remainder, parts[i]);
			if (partSearch.success) {
				i++;
				// look for the next part?
				if (i >= len) {
					// all parts found
					result.success = true;
					result.endChar = partSearch.endChar;
					searching = false;
				} else if (remainder.length() > 0) {
					remainder = remainder.substring(partSearch.endChar);
				} else {
					result.success = false;
				}
			} else {
				result.success = false;
				searching = false;
			}
		}

		return result;
	}

	private Finding stringContains(final String s, final String part) {
		Finding result = new Finding();

		if (s == null || part == null || part.length() > s.length()) {
			return result;
		}

		if (part.length() == 0) {
			result.success = true;
			result.endChar = 0;
			return result;
		}

		result.success = false;

		int i = 0;

		int end = s.length() - part.length() + 1;

		while (!result.success && i < end) {
			result.success = comparator.compare(s.substring(i, i + part.length()), part) == 0;
			result.endChar = i + part.length() - 1;
			i++;
		}

		return result;
	}

	private boolean stringStartsWith(final String s, final String part) {
		if (s == null || part == null || part.length() > s.length()) {
			return false;
		}

		if (part.length() == 0) {
			return true;
		}

		return comparator.compare(s.substring(0, part.length()), part) == 0;
	}

	private int findViewRowFromValue(int startviewrow, Object value, Set<Integer> colIndices, boolean fulltext,
			boolean regex, boolean combineCols) {
		Logging.debug(this,
				"findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext, boolean regex): "
						+ startviewrow + ", " + value + ", " + colIndices + ", " + fulltext + ", " + regex + ", "
						+ combineCols);

		if (value == null) {
			return -1;
		}

		String val = value.toString().toLowerCase(Locale.ROOT);

		if (val.length() < 2) {
			return -1;
		}
		// dont start searching for single chars

		int viewrow = 0;

		if (startviewrow > 0) {
			viewrow = startviewrow;
		}

		Pattern pattern = null;
		if (regex) {
			if (fulltext) {
				val = ".*" + val + ".*";
			}

			pattern = Pattern.compile(val);
		}

		String[] valParts = val.split(" ");

		boolean found = false;

		while (!found && viewrow < targetModel.getRowCount()) {
			if (combineCols) {
				// only fulltext
				StringBuilder buffRow = new StringBuilder();

				for (int j = 0; j < targetModel.getColumnCount(); j++) {
					// we dont compare all values (comparing all values is default)
					if (colIndices != null && !colIndices.contains(j)) {
						continue;
					}

					int colJ = targetModel.getColForVisualCol(j);

					Object valJ = targetModel.getValueAt(targetModel.getRowForVisualRow(viewrow), colJ);

					if (valJ != null) {
						String valSJ = valJ.toString().toLowerCase(Locale.ROOT);

						buffRow.append(valSJ);
					}
				}

				String compareVal = buffRow.toString();

				if (!compareVal.isEmpty()) {
					found = stringContainsParts(compareVal, valParts).success;
				} else if (val.isEmpty()) {
					found = true;
				} else {
					// Do nothing when 'val' is not empty and 'compareValue' is empty
				}
			} else {
				for (int j = 0; j < targetModel.getColumnCount(); j++) {
					// we dont compare all values (comparing all values is default)
					if (colIndices != null && !colIndices.contains(j)) {
						continue;
					}

					int colJ = targetModel.getColForVisualCol(j);

					Object compareValue = targetModel.getValueAt(targetModel.getRowForVisualRow(viewrow), colJ);

					if (compareValue == null) {
						if (val.isEmpty()) {
							found = true;
						}
					} else {
						String compareVal = compareValue.toString().toLowerCase(Locale.ROOT);

						if (regex) {
							if (pattern.matcher(compareVal).matches()) {
								found = true;
							}
						} else {
							if (fulltext) {
								found = stringContainsParts(compareVal, valParts).success;
							} else {
								found = stringStartsWith(compareVal, val);
							}
						}
					}

					if (found) {
						break;
					}
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

	public void setResetFilterModeOnNewSearch(boolean b) {
		resetFilterModeOnNewSearch = b;
	}

	/**
	 * select all rows with value from searchfield
	 */
	private void markAll() {
		if (!allowSearchAction()) {
			return;
		}

		Logging.info(this, "markAll");
		targetModel.setValueIsAdjusting(true);
		targetModel.clearSelection();
		searchTheRow(0, true);

		int startFoundrow = foundrow;

		foundrow = foundrow + 1;

		// adding the next row to selection
		while (foundrow > startFoundrow) {
			getSelectedAndSearch(true, true);
		}
		targetModel.setValueIsAdjusting(false);
	}

	/**
	 * select all rows with value form searchfield, checks the filter
	 */
	private void markAllAndFilter() {
		Logging.info(this, " markAllAndFilter filtering, disabledSinceWeAreInFilteredModel " + filtering);
		if (!filtering) {
			return;
		}

		markAll();
	}

	/**
	 * sets an alternative ActionListener for the filtermark
	 * 
	 * @parameter ActionListener
	 */
	public void setFiltermarkActionListener(ActionListener li) {
		filtermark.removeAllActionListeners();
		filtermark.addActionListener(li);
	}

	/**
	 * sets an alternative tooltip for the filtermark
	 * 
	 * @parameter String
	 */
	public void setFiltermarkToolTipText(String s) {
		filtermark.setToolTipText(s);
	}

	private void searchNextRow(boolean select) {
		foundrow++;
		searchTheRow(foundrow, false, select);
	}

	private void getSelectedAndSearch(boolean addSelection, boolean select) {
		int startrow = 0;
		if (targetModel.getSelectedRow() >= 0) {
			startrow = targetModel.getSelectedRows()[targetModel.getSelectedRows().length - 1] + 1;
		}

		if (startrow >= targetModel.getRowCount()) {
			startrow = 0;
		}

		searchTheRow(startrow, addSelection, select);

		if (foundrow == -1) {
			searchTheRow(0, addSelection, select);
		}
	}

	private void searchTheRow(boolean select) {
		searchTheRow(targetModel.getSelectedRow(), select);
	}

	private void searchTheRow(int startrow, boolean select) {
		searchTheRow(startrow, false, select);
	}

	private void setRow(int row, boolean addSelection, boolean select) {
		if (select) {
			if (addSelection) {
				addSelectedRow(row);
			} else {
				setSelectedRow(row);
			}
		} else {
			// make only visible
			targetModel.ensureRowIsVisible(row);
		}

		targetModel.setCursorRow(row);
	}

	private void searchTheRow(final int startrow, final boolean addSelection, final boolean select) {
		final String value = fieldSearch.getText();

		Set<Integer> selectedCols0 = null;

		if (comboSearchFields.getSelectedIndex() > 0) {
			selectedCols0 = new HashSet<>();
			selectedCols0.add(targetModel.findColumn((String) comboSearchFields.getSelectedItem()));
		}

		final Set<Integer> selectedCols = selectedCols0;

		final boolean fulltextSearch = comboSearchFieldsMode.getSelectedIndex() == FULL_TEXT_SEARCH;
		final boolean regexSearch = comboSearchFieldsMode.getSelectedIndex() == REGEX_SEARCH;
		final boolean combineCols = fulltextSearch;

		fieldSearch.getCaret().setVisible(false);

		if (value.length() < 2) {
			setRow(0, false, select);
		} else {
			foundrow = findViewRowFromValue(startrow, value, selectedCols, fulltextSearch, regexSearch, combineCols);

			if (foundrow > -1) {
				setRow(foundrow, addSelection, select);
			} else {
				if (startrow > 0) {
					searchTheRow(0, addSelection, select);
				} else {
					setRow(0, false, select);
				}
			}
		}

		fieldSearch.getCaret().setVisible(true);
	}

	private void addSelectedRow(int row) {
		targetModel.addSelectedRow(row);
	}

	private void setSelectedRow(int row) {
		targetModel.setSelectedRow(row);
	}

	// ----------------------------------

	private void switchFilterOff() {
		if (isFilteredMode()) {
			setFiltered(false);
		}
	}

	private void switchFilterOn() {
		if (!isFilteredMode()) {
			setFiltered(true);
		}
	}

	private void checkmarkAllColumnsEvent() {
		Logging.debug(this, "actionPerformed on checkmarkAllColumns");

		comboSearchFields.setSelectedIndex(0);
		if (Boolean.TRUE.equals(checkmarkAllColumns.isSelected())) {
			// all columns

			if (Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY, "0");
			}
		} else {
			if (Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + ALL_COLUMNS_SEARCH_PROPERTY, "1");
			}
		}
	}

	private void checkmarkFullTextEvent() {
		Logging.debug(this, "actionPerformed on checkmarkFullText");

		if (Boolean.TRUE.equals(checkmarkFullText.isSelected())) {
			comboSearchFieldsMode.setSelectedIndex(FULL_TEXT_SEARCH);
			if (Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY, "0");
			}
		} else {
			comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);
			if (Configed.getSavedStates().getProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + FULL_TEXT_SEARCH_PROPERTY, "1");
			}
		}
	}

	private void filtermarkEvent() {
		Logging.info(this, "actionPerformed on filtermark, isFilteredMode " + isFilteredMode());

		if (isFilteredMode()) {
			int[] unfilteredSelection = targetModel.getUnfilteredSelection();

			setFiltered(false);

			if (unfilteredSelection.length != 0) {
				targetModel.setSelection(unfilteredSelection);
			}
		} else {
			switchFilterOn();
		}
	}

	private void checkmarkSearchProgressiveEvent() {
		Logging.debug(this,
				"actionPerformed on checkmarkSearchProgressiv, set to  " + checkmarkSearchProgressive.isSelected());
		if (Boolean.TRUE.equals(checkmarkSearchProgressive.isSelected())) {
			searchInputType = SearchInputType.PROGRESSIVE;
			if (Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY, "0");
			}
		} else {
			searchInputType = SearchInputType.LINE;
			if (Configed.getSavedStates()
					.getProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY) != null) {
				Configed.getSavedStates().setProperty(savedStatesObjectTag + "." + PROGRESSIVE_SEARCH_PROPERTY, "1");
			}
		}

		Logging.debug(this, "actionPerformed on checkmarkSearchProgressiv, searchInputType set to " + searchInputType);
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			if (searchInputType == SearchInputType.PROGRESSIVE) {
				switchFilterOff();
				searchTheRow(selectMode);
			}
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			if (searchInputType == SearchInputType.PROGRESSIVE) {
				switchFilterOff();
				searchTheRow(selectMode);
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			switchFilterOff();

			setRow(0, false, selectMode);
			// go back to start when editing is restarted
		}
	}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F5) {
			markAll();
		} else if (e.getKeyCode() == KeyEvent.VK_F8) {
			switchFilterOff();
			markAllAndFilter();
			switchFilterOn();
		} else if (e.getKeyCode() == KeyEvent.VK_F3) {
			if (allowSearchAction()) {
				searchNextRow(selectMode);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			Logging.debug(this, "key pressed ENTER on fieldSearch, with content " + fieldSearch.getText()
					+ " searchInputType " + searchInputType);

			if (searchInputType == SearchInputType.LINE && !fieldSearch.getText().isEmpty()) {
				switchFilterOff();
				markAllAndFilter();
				switchFilterOn();
			}
		} else {
			// We want to do nothing on other keys
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}