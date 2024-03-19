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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
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
	private static final Pattern S_PLUS_PATTERN = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	private static final int BLINK_RATE = 0;

	public enum SearchMode {
		FULL_TEXT_SEARCH, FULL_TEXT_WITH_ALTERNATIVES_SEARCH, START_TEXT_SEARCH, REGEX_SEARCH
	}

	private JFrame masterFrame = ConfigedMain.getMainFrame();

	private JTextField fieldSearch;

	private boolean filtering;

	private JComboBox<String> comboSearchFields;
	private JComboBoxToolTip comboSearchFieldsMode;

	private JLabel labelSearch;
	private CheckedLabel checkmarkSearch;
	private JLabel labelSearchMode;
	private CheckedLabel filtermark;

	private JButton buttonShowHideExtraOptions;
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

	private boolean filteredMode;

	private boolean isExtraOptionsHidden = true;

	/**
	 * Provides search functionality for tables.
	 * 
	 * @param SearchTargetModel the model for delivering data and selecting
	 */
	public TableSearchPane(SearchTargetModel targetModel) {
		this(targetModel, false);
	}

	/**
	 * Provides search functionality for tables.
	 * 
	 * @param targetModel the model for delivering data and selecting
	 * @param withRegex   modifies the search function
	 */
	public TableSearchPane(SearchTargetModel targetModel, boolean withRegEx) {
		this(null, targetModel, withRegEx);
	}

	/**
	 * Provides search functionality for tables.
	 * 
	 * @param thePanel    the model for delivering data and selecting
	 * @param targetModel the model for delivering data and selecting
	 * @param withRegex   modifies the search function
	 */
	public TableSearchPane(PanelGenEditTable thePanel, boolean withRegEx) {
		this(thePanel, new SearchTargetModelFromTable(thePanel), withRegEx);
	}

	public TableSearchPane(PanelGenEditTable thePanel, SearchTargetModel targetModel, boolean withRegEx) {
		associatedPanel = thePanel;
		this.targetModel = targetModel;
		this.withRegEx = withRegEx;
		filtering = true;

		comparator = getCollator();

		init();
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

	private void setFiltered(boolean filtered) {
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
		if (b) {
			setupNarrowLayout();
		}
		showFilterIcon(b);
		buttonShowHideExtraOptions.setVisible(b);
		comboSearchFields.setVisible(!b);
		comboSearchFieldsMode.setVisible(!b);
		labelSearch.setVisible(!b);
		labelSearchMode.setVisible(!b);
	}

	public void setTargetModel(SearchTargetModel searchTargetModel) {
		this.targetModel = searchTargetModel;
	}

	private void initComponents() {
		navPane = new NavigationPanel(associatedPanel);

		navPane.setVisible(false);

		labelSearch = new JLabel(Configed.getResourceValue("SearchPane.search"));

		Icon unselectedIconSearch = Utils.createImageIcon("images/loupe_light_16.png", "");
		Icon selectedIconSearch = Utils.createImageIcon("images/loupe_light_16_x.png", "");

		checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);
		checkmarkSearch.setVisible(true);
		checkmarkSearch.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearch.tooltip"));
		checkmarkSearch.addActionListener(event -> fieldSearch.setText(""));
		checkmarkSearch.setChangeStateAutonomously(false);

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

		fieldSearch.addActionListener((ActionEvent actionEvent) -> searchNextRow(selectMode));

		comboSearchFields = new JComboBox<>(new String[] { Configed.getResourceValue("SearchPane.search.allfields") });
		comboSearchFields.setPreferredSize(Globals.BUTTON_DIMENSION);

		setSearchFieldsAll();

		labelSearchMode = new JLabel(Configed.getResourceValue("SearchPane.searchmode.searchmode"));

		Map<String, String> tooltipsMap = new LinkedHashMap<>();

		tooltipsMap.put(Configed.getResourceValue("SearchPane.searchmode.fulltext"),
				Configed.getResourceValue("SearchPane.mode.fulltext.tooltip"));
		tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.fulltextWithAlternatives"),
				Configed.getResourceValue("SearchPane.mode.fulltextWithAlternatives.tooltip"));
		tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.starttext"),
				Configed.getResourceValue("SearchPane.mode.starttext.tooltip"));
		if (withRegEx) {
			tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.regex"),
					Configed.getResourceValue("SearchPane.mode.regex.tooltip"));
		}

		comboSearchFieldsMode = new JComboBoxToolTip();
		comboSearchFieldsMode.setValues(tooltipsMap, false);
		comboSearchFieldsMode.setSelectedIndex(SearchMode.START_TEXT_SEARCH.ordinal());
		comboSearchFieldsMode.setPreferredSize(Globals.BUTTON_DIMENSION);

		Icon unselectedIconFilter = Utils.createImageIcon("images/filter_14x14_open.png", "");
		Icon selectedIconFilter = Utils.createImageIcon("images/filter_14x14_closed.png", "");

		filtermark = new CheckedLabel(selectedIconFilter, unselectedIconFilter, false);
		filtermark.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filtermark.addActionListener(event -> filtermarkEvent());

		labelFilterMarkGap = new JLabel();

		showFilterIcon(filtering);

		buttonShowHideExtraOptions = new JButton(Utils.getThemeIconPNG("bootstrap/caret_left_fill", ""));
		buttonShowHideExtraOptions
				.setToolTipText(Configed.getResourceValue("SearchPane.narrowLayout.extraOptions.toolTip"));
		buttonShowHideExtraOptions.setVisible(false);
		buttonShowHideExtraOptions.addActionListener(event -> showExtraOptions());
	}

	private void showExtraOptions() {
		if (isExtraOptionsHidden) {
			isExtraOptionsHidden = false;
			buttonShowHideExtraOptions.setIcon(Utils.getThemeIconPNG("bootstrap/caret_down_fill", ""));
		} else {
			isExtraOptionsHidden = true;
			buttonShowHideExtraOptions.setIcon(Utils.getThemeIconPNG("bootstrap/caret_left_fill", ""));
		}
		comboSearchFields.setVisible(!comboSearchFields.isVisible());
		comboSearchFieldsMode.setVisible(!comboSearchFieldsMode.isVisible());
		labelSearch.setVisible(!labelSearch.isVisible());
		labelSearchMode.setVisible(!labelSearchMode.isVisible());
	}

	private void setupNarrowLayout() {
		GroupLayout layoutTablesearchPane = new GroupLayout(this);
		setLayout(layoutTablesearchPane);

		int checkedLabelWidth = 18;
		layoutTablesearchPane
				.setHorizontalGroup(layoutTablesearchPane
						.createParallelGroup(
								GroupLayout.Alignment.LEADING)
						.addGroup(layoutTablesearchPane.createSequentialGroup()
								.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldSearch, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(filtermark, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
								.addComponent(labelFilterMarkGap, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
										Globals.MIN_GAP_SIZE)
								.addComponent(buttonShowHideExtraOptions, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE))
						.addGroup(layoutTablesearchPane.createSequentialGroup().addGap(Globals.GAP_SIZE)
								.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(comboSearchFields, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addGap(Globals.GAP_SIZE)
								.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(comboSearchFieldsMode, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE));

		layoutTablesearchPane.setVerticalGroup(layoutTablesearchPane.createSequentialGroup()
				.addGroup(layoutTablesearchPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(filtermark, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelFilterMarkGap, 10, 10, 10)
						.addComponent(buttonShowHideExtraOptions, 10, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutTablesearchPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchFieldsMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
	}

	private void setupLayout() {
		GroupLayout layoutTablesearchPane = new GroupLayout(this);
		this.setLayout(layoutTablesearchPane);

		layoutTablesearchPane.setHorizontalGroup(layoutTablesearchPane.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(fieldSearch, Globals.ICON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(filtermark, 18, 18, 18)
				.addComponent(labelFilterMarkGap, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(buttonShowHideExtraOptions, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
						Globals.MIN_GAP_SIZE)
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
				.addComponent(buttonShowHideExtraOptions, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkmarkSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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

	public void setSearchMode(SearchMode mode) {
		if (mode == SearchMode.REGEX_SEARCH && !withRegEx) {
			return;
		}

		comboSearchFieldsMode.setSelectedIndex(mode.ordinal());
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

	private int findViewRowFromValue(int startviewrow, Object value, Set<Integer> colIndices) {
		// Search only for value longer than one digit
		if (value == null || value.toString().length() < 2) {
			return -1;
		}

		String valueLower = value.toString().toLowerCase(Locale.ROOT);
		Pattern regexPattern = null;
		SearchMode mode = getSearchMode(comboSearchFieldsMode.getSelectedIndex());
		if (mode == SearchMode.REGEX_SEARCH) {
			regexPattern = Pattern.compile(valueLower);
		}

		int viewrow = Math.max(0, startviewrow);
		boolean found = false;

		while (!found && viewrow < targetModel.getRowCount()) {
			found = searchForStringInColumns(viewrow, colIndices, valueLower, regexPattern, mode);
			if (!found) {
				viewrow++;
			}
		}

		return found ? viewrow : -1;
	}

	private static SearchMode getSearchMode(int i) {
		for (SearchMode mode : SearchMode.values()) {
			if (mode.ordinal() == i) {
				return mode;
			}
		}
		return null;
	}

	@SuppressWarnings("java:S135")
	private boolean searchForStringInColumns(int viewrow, Set<Integer> colIndices, String valueLower,
			Pattern regexPattern, SearchMode mode) {
		boolean found = false;
		for (int j = 0; j < targetModel.getColumnCount(); j++) {
			// we dont compare all values (comparing all values is default)
			if (colIndices != null && !colIndices.contains(j)) {
				continue;
			}

			Object compareValue = targetModel.getValueAt(targetModel.getRowForVisualRow(viewrow),
					targetModel.getColForVisualCol(j));

			if (compareValue != null) {
				String compareValueLower = compareValue.toString().toLowerCase(Locale.ROOT);
				found = searchForStringBasedOnSearchMode(compareValueLower, valueLower, regexPattern, mode);
			}

			if (found) {
				break;
			}
		}
		return found;
	}

	private boolean searchForStringBasedOnSearchMode(String searchString, String searchPattern, Pattern regexPattern,
			SearchMode mode) {
		boolean found = false;
		if (mode == null) {
			return found;
		}

		switch (mode) {
		case REGEX_SEARCH:
			if (regexPattern.matcher(searchString).matches()) {
				found = true;
			}
			break;
		case FULL_TEXT_SEARCH:
			found = stringContainsParts(searchString, searchPattern.split(" ")).success;
			break;
		case FULL_TEXT_WITH_ALTERNATIVES_SEARCH:
			String valLower = searchPattern.toLowerCase(Locale.ROOT);
			List<String> alternativeWords = getWords(valLower);
			found = fullTextSearchingWithAlternatives(alternativeWords, searchString);
			break;
		default:
			found = stringStartsWith(searchString, searchPattern);
		}

		return found;
	}

	private static List<String> getWords(String line) {
		List<String> result = new ArrayList<>();
		String[] splitted = S_PLUS_PATTERN.split(line);
		for (String s : splitted) {
			if (!" ".equals(s)) {
				result.add(s);
			}
		}
		return result;
	}

	private static boolean fullTextSearchingWithAlternatives(List<String> alternativeWords, String compareVal) {
		for (String word : alternativeWords) {
			if (compareVal.indexOf(word) >= 0) {
				return true;
			}
		}
		return false;
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

		fieldSearch.getCaret().setVisible(false);

		if (value.length() < 2) {
			setRow(0, false, select);
		} else {
			foundrow = findViewRowFromValue(startrow, value, selectedCols);

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
		if (filteredMode) {
			setFiltered(false);
		}
	}

	private void switchFilterOn() {
		if (!filteredMode) {
			setFiltered(true);
		}
	}

	private void filtermarkEvent() {
		Logging.info(this, "actionPerformed on filtermark, isFilteredMode " + filteredMode);

		if (filteredMode) {
			int[] unfilteredSelection = targetModel.getUnfilteredSelection();

			setFiltered(false);

			if (unfilteredSelection.length != 0) {
				targetModel.setSelection(unfilteredSelection);
			}
		} else {
			switchFilterOn();
		}
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			switchFilterOff();
			searchTheRow(selectMode);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == fieldSearch.getDocument()) {
			checkmarkSearch.setSelected(fieldSearch.getText().length() > 0);

			switchFilterOff();
			searchTheRow(selectMode);
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
