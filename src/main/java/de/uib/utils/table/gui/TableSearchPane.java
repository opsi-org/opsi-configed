/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class TableSearchPane extends JPanel implements DocumentListener, KeyListener {
	private static final Pattern S_PLUS_PATTERN = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

	public enum SearchMode {
		FULL_TEXT_SEARCH, FULL_TEXT_WITH_ALTERNATIVES_SEARCH, START_TEXT_SEARCH, REGEX_SEARCH
	}

	private FlatTextField flatTextFieldSearch;

	private JComboBox<String> comboSearchFields;
	private JComboBox<String> comboSearchFieldsMode;

	private JLabel labelSearch;
	private JLabel labelSearchMode;
	private JToggleButton filtermark;

	private JButton buttonShowHideExtraOptions;

	private JPanel navPane;
	private PanelGenEditTable associatedPanel;

	private JMenuItem popupSearch;
	private JMenuItem popupSearchNext;
	private JMenuItem popupMarkHits;
	private JMenuItem popupMarkAndFilter;
	private JMenuItem popupEmptySearchfield;

	private boolean withRegEx = true;
	private boolean selectMode = true;

	private int foundrow = -1;

	private SearchTargetModel targetModel;

	private final Comparator<Object> comparator;

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

		comparator = getCollator();

		init();
	}

	private void init() {
		initComponents();
		initPopup();
		setupLayout();

		setSearchFieldsAll();
		setNarrow(false);
	}

	private static Collator getCollator() {
		Collator alphaCollator = Collator.getInstance();
		alphaCollator.setStrength(Collator.IDENTICAL);
		return alphaCollator;
	}

	public void showNavPane() {
		initNavigationPanel();
		navPane.setVisible(true);
	}

	public void setFiltering() {
		popupMarkHits.setVisible(true);
		popupMarkAndFilter.setVisible(true);

		filtermark.setVisible(true);
	}

	public boolean isFiltering() {
		return filtermark.isVisible();
	}

	public void setSelectMode(boolean selectMode) {
		this.selectMode = selectMode;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		flatTextFieldSearch.setEnabled(enabled);
	}

	/**
	 * serve graphical filtermark
	 */
	public void setFilterMark(boolean selected) {
		filtermark.setSelected(selected);
	}

	private void setFiltered(boolean filtered) {
		targetModel.setFiltered(filtered);
		setFilteredMode(filtered);
	}

	/**
	 * express filter status in graphical components
	 */
	public void setFilteredMode(boolean filteredMode) {
		popupSearch.setEnabled(!filteredMode);
		popupSearchNext.setEnabled(!filteredMode);

		popupMarkHits.setEnabled(!filteredMode);
		popupMarkAndFilter.setEnabled(!filteredMode);
		popupEmptySearchfield.setEnabled(!filteredMode);
	}

	public boolean isFilteredMode() {
		return filtermark.isSelected();
	}

	public void setNarrow(boolean narrow) {
		if (narrow) {
			setupNarrowLayout();
		}
		buttonShowHideExtraOptions.setVisible(narrow);
		comboSearchFields.setVisible(!narrow);
		comboSearchFieldsMode.setVisible(!narrow);
		labelSearch.setVisible(!narrow);
		labelSearchMode.setVisible(!narrow);
	}

	public void setTargetModel(SearchTargetModel searchTargetModel) {
		this.targetModel = searchTargetModel;
	}

	private void initComponents() {
		navPane = new JPanel();
		navPane.setVisible(false);

		labelSearch = new JLabel(Configed.getResourceValue("SearchPane.search"));

		flatTextFieldSearch = new FlatTextField();
		flatTextFieldSearch.setLeadingIcon(new FlatSearchIcon());
		flatTextFieldSearch.setShowClearButton(true);

		flatTextFieldSearch.setPreferredSize(Globals.TEXT_FIELD_DIMENSION);

		flatTextFieldSearch.getDocument().addDocumentListener(this);

		flatTextFieldSearch.addKeyListener(this);

		flatTextFieldSearch.addActionListener(actionEvent -> searchNextRow(selectMode));

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

		comboSearchFieldsMode = new JComboBox<>();
		for (String key : tooltipsMap.keySet()) {
			comboSearchFieldsMode.addItem(key);
		}
		comboSearchFieldsMode.setRenderer(new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (isSelected && -1 < index && index < tooltipsMap.size()) {
					list.setToolTipText(new ArrayList<>(tooltipsMap.values()).get(index));
				}

				setText(value != null ? value.toString() : "");
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});
		comboSearchFieldsMode.setSelectedIndex(SearchMode.START_TEXT_SEARCH.ordinal());
		comboSearchFieldsMode.setPreferredSize(Globals.BUTTON_DIMENSION);

		filtermark = new JToggleButton(Utils.getIntellijIcon("funnelRegular"));
		filtermark.setSelectedIcon(Utils.getSelectedIntellijIcon("funnelRegular"));
		filtermark.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filtermark.addItemListener(event -> filtermarkEvent());
		filtermark.setVisible(false);

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(filtermark);
		flatTextFieldSearch.setTrailingComponent(jToolBar);

		buttonShowHideExtraOptions = new JButton(Utils.getThemeIconPNG("bootstrap/caret_left_fill", ""));
		buttonShowHideExtraOptions
				.setToolTipText(Configed.getResourceValue("SearchPane.narrowLayout.extraOptions.toolTip"));
		buttonShowHideExtraOptions.setVisible(false);
		buttonShowHideExtraOptions.addActionListener(event -> showExtraOptions());
	}

	private void initPopup() {
		popupSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.search"));
		popupSearch.addActionListener(actionEvent -> searchTheRow(selectMode));

		popupSearchNext = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnext"));
		popupSearchNext.addActionListener(actionEvent -> searchNextRow(selectMode));
		popupSearchNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		JMenuItem popupNewSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnew"));
		popupNewSearch.addActionListener((ActionEvent actionEvent) -> {
			setFiltered(false);
			searchTheRow(0, selectMode);
		});

		popupMarkHits = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markall"));
		popupMarkHits.addActionListener(actionEvent -> markAll());
		popupMarkHits.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		popupMarkAndFilter = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markAndFilter"));
		popupMarkAndFilter.addActionListener(actionEvent -> markAllAndFilter());
		popupMarkAndFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		popupEmptySearchfield = new JMenuItem(Configed.getResourceValue("SearchPane.popup.empty"));
		popupEmptySearchfield.addActionListener(actionEvent -> flatTextFieldSearch.setText(""));

		popupMarkHits.setVisible(false);
		popupMarkAndFilter.setVisible(false);

		Logging.info(this, "buildMenuSearchfield");
		JPopupMenu searchMenu = new JPopupMenu();
		searchMenu.add(popupSearch);
		searchMenu.add(popupSearchNext);
		searchMenu.add(popupNewSearch);
		searchMenu.add(popupMarkHits);
		searchMenu.add(popupMarkAndFilter);
		searchMenu.add(popupEmptySearchfield);

		flatTextFieldSearch.setComponentPopupMenu(searchMenu);
	}

	private void initNavigationPanel() {
		Dimension navButtonDimension = new Dimension(30, Globals.BUTTON_HEIGHT - 6);

		JButton nextButton = new JButton(Utils.getIntellijIcon("playForward"));
		nextButton.setToolTipText(Configed.getResourceValue("NavigationPanel.nextEntryTooltip"));
		nextButton.setPreferredSize(navButtonDimension);
		nextButton.addActionListener(event -> associatedPanel.advanceCursor(+1));

		JButton previousButton = new JButton(Utils.getIntellijIcon("playBack"));
		previousButton.setToolTipText(Configed.getResourceValue("NavigationPanel.previousEntryTooltip"));
		previousButton.setPreferredSize(navButtonDimension);
		previousButton.addActionListener(event -> associatedPanel.advanceCursor(-1));

		JButton firstButton = new JButton(Utils.getIntellijIcon("playFirst"));
		firstButton.setToolTipText(Configed.getResourceValue("NavigationPanel.firstEntryTooltip"));
		firstButton.setPreferredSize(navButtonDimension);
		firstButton.addActionListener(event -> associatedPanel.setCursorToFirstRow());

		JButton lastButton = new JButton(Utils.getIntellijIcon("playLast"));
		lastButton.setToolTipText(Configed.getResourceValue("NavigationPanel.lastEntryTooltip"));
		lastButton.setPreferredSize(navButtonDimension);
		lastButton.addActionListener(event -> associatedPanel.setCursorToLastRow());

		GroupLayout layout = new GroupLayout(navPane);
		navPane.setLayout(layout);
		navPane.setVisible(false);

		layout.setVerticalGroup(layout.createParallelGroup()
				.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private void showExtraOptions() {
		if (isExtraOptionsHidden) {
			buttonShowHideExtraOptions.setIcon(Utils.getThemeIconPNG("bootstrap/caret_down_fill", ""));
		} else {
			buttonShowHideExtraOptions.setIcon(Utils.getThemeIconPNG("bootstrap/caret_left_fill", ""));
		}

		comboSearchFields.setVisible(isExtraOptionsHidden);
		comboSearchFieldsMode.setVisible(isExtraOptionsHidden);
		labelSearch.setVisible(isExtraOptionsHidden);
		labelSearchMode.setVisible(isExtraOptionsHidden);

		isExtraOptionsHidden = !isExtraOptionsHidden;
	}

	private void setupNarrowLayout() {
		GroupLayout layoutTablesearchPane = new GroupLayout(this);
		setLayout(layoutTablesearchPane);

		layoutTablesearchPane.setHorizontalGroup(layoutTablesearchPane
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutTablesearchPane.createSequentialGroup()
						.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)
						.addComponent(flatTextFieldSearch, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE)
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
						.addComponent(flatTextFieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
				.addComponent(flatTextFieldSearch, Globals.ICON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
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
				.addComponent(buttonShowHideExtraOptions, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(flatTextFieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(labelSearchMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSearchFieldsMode, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private boolean allowSearchAction() {
		return isFiltering() && !filtermark.isSelected() && !flatTextFieldSearch.getText().isEmpty();
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
		flatTextFieldSearch.requestFocusInWindow();
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

	/**
	 * select all rows with value from searchfield
	 */
	private void markAll() {
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
		Logging.info(this, " markAllAndFilter filtering active" + isFiltering());

		filtermark.setSelected(false);
		markAll();
		filtermark.setSelected(true);
	}

	/**
	 * sets an alternative ActionListener for the filtermark
	 * 
	 * @parameter ActionListener
	 */
	public void setFiltermarkActionListener(ActionListener li) {
		filtermark.addActionListener(li);
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
		final String value = flatTextFieldSearch.getText();

		Set<Integer> selectedCols0 = null;

		if (comboSearchFields.getSelectedIndex() > 0) {
			selectedCols0 = new HashSet<>();
			selectedCols0.add(targetModel.findColumn((String) comboSearchFields.getSelectedItem()));
		}

		final Set<Integer> selectedCols = selectedCols0;

		flatTextFieldSearch.getCaret().setVisible(false);

		if (value.length() < 2) {
			setRow(0, false, select);
		} else {
			foundrow = findViewRowFromValue(startrow, value, selectedCols);

			if (foundrow > -1) {
				setRow(foundrow, addSelection, select);
			} else if (startrow > 0) {
				searchTheRow(0, addSelection, select);
			} else {
				setRow(0, false, select);
			}
		}

		flatTextFieldSearch.getCaret().setVisible(true);
	}

	private void addSelectedRow(int row) {
		targetModel.addSelectedRow(row);
	}

	private void setSelectedRow(int row) {
		targetModel.setSelectedRow(row);
	}

	// ----------------------------------

	private void filtermarkEvent() {
		Logging.info(this, "actionPerformed on filtermark, isFilteredMode " + filtermark.isSelected());

		if (filtermark.isSelected()) {
			setFiltered(true);
		} else {
			int[] unfilteredSelection = targetModel.getUnfilteredSelection();

			setFiltered(false);

			if (unfilteredSelection.length != 0) {
				targetModel.setSelection(unfilteredSelection);
			}
		}
	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		documentChanged(e);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == flatTextFieldSearch.getDocument()) {
			filtermark.setSelected(false);

			setRow(0, false, selectMode);
			// go back to start when editing is restarted
		}
	}

	private void documentChanged(DocumentEvent e) {
		if (e.getDocument() == flatTextFieldSearch.getDocument()) {
			filtermark.setSelected(false);
			searchTheRow(selectMode);
		}
	}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F5) {
			if (allowSearchAction()) {
				markAll();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_F8) {
			if (allowSearchAction()) {
				markAllAndFilter();
			}
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
