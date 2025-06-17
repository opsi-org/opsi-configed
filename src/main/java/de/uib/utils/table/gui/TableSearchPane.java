/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Collator;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.FeatureActivationChecker;
import de.uib.utils.FeatureActivationChecker.Feature;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;

public class TableSearchPane extends JPanel implements DocumentListener, KeyListener {
	private FlatTextField flatTextFieldSearch;

	private JComboBox<String> comboSearchFields;

	private JLabel labelSearch;

	private JToggleButton respectCase;
	private JToggleButton regexActive;
	private JToggleButton filtermark;

	private JToggleButton buttonShowHideExtraOptions;

	private JPanel navPane;
	private PanelGenEditTable associatedPanel;

	private JMenuItem popupSearch;
	private JMenuItem popupMarkHits;
	private JMenuItem popupMarkAndFilter;
	private JMenuItem popupEmptySearchfield;

	private boolean selectMode = true;

	private int foundrow = -1;

	private SearchTargetModel targetModel;

	private final Collator comparator;

	private FilterKey filterKey;

	/**
	 * Provides search functionality for tables.
	 * 
	 * @param targetModel the model for delivering data and selecting
	 */
	public TableSearchPane(SearchTargetModel targetModel) {
		this(null, targetModel);
	}

	/**
	 * Provides search functionality for tables.
	 * 
	 * @param thePanel    the model for delivering data and selecting
	 * @param targetModel the model for delivering data and selecting
	 */
	public TableSearchPane(PanelGenEditTable thePanel) {
		this(thePanel, new SearchTargetModelFromTable(thePanel, thePanel.getJTable()));
	}

	public TableSearchPane(PanelGenEditTable thePanel, SearchTargetModel targetModel) {
		associatedPanel = thePanel;
		this.targetModel = targetModel;

		comparator = getCollator();

		init();
	}

	public void setFilterKey(FilterKey filterKey) {
		this.filterKey = filterKey;
	}

	private void init() {
		initComponents();
		initPopup();
		setupLayout();
		setSearchFieldsAll();
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

		filtermark.setVisible(!FeatureActivationChecker.isFeatureActivated(Feature.NEW_FILTER));
	}

	public boolean isFiltering() {
		return filtermark.isVisible();
	}

	public void setSelectMode(boolean selectMode) {
		this.selectMode = selectMode;
	}

	/**
	 * serve graphical filtermark
	 */
	public void setFilterMark(boolean selected) {
		filtermark.setSelected(selected);
	}

	private void setFiltered(boolean filtered) {
		targetModel.setFiltered(filtered);

		popupSearch.setEnabled(!filtered);
		popupMarkHits.setEnabled(!filtered);
		popupMarkAndFilter.setEnabled(!filtered);
		popupEmptySearchfield.setEnabled(!filtered);
	}

	public boolean isFilteredMode() {
		return filtermark.isSelected();
	}

	public void setNarrow(boolean narrow) {
		if (narrow) {
			setupNarrowLayout();
			buttonShowHideExtraOptions.setVisible(true);
		}

		comboSearchFields.setVisible(!narrow);
		labelSearch.setVisible(!narrow);
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

		flatTextFieldSearch.getDocument().addDocumentListener(this);

		flatTextFieldSearch.addKeyListener(this);

		flatTextFieldSearch.addActionListener(actionEvent -> searchNextRow(selectMode));

		comboSearchFields = new JComboBox<>();
		comboSearchFields.addItemListener((ItemEvent e) -> {
			Object selected = comboSearchFields.getSelectedItem();
			if (selected != null && !flatTextFieldSearch.getText().isBlank()) {
				filter();
			}
		});

		respectCase = new JToggleButton(Icons.getIntellijIcon("matchCase"));
		respectCase.setSelectedIcon(Icons.getSelectedIntellijIcon("matchCase"));
		respectCase.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));

		regexActive = new JToggleButton(Icons.getIntellijIcon("regex"));
		regexActive.setSelectedIcon(Icons.getSelectedIntellijIcon("regex"));
		regexActive.setToolTipText(Configed.getResourceValue("SearchPane.mode.regex"));

		filtermark = new JToggleButton(Icons.getIntellijIcon("funnelRegular"));
		filtermark.setSelectedIcon(Icons.getSelectedIntellijIcon("funnelRegular"));
		filtermark.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filtermark.addItemListener(event -> filtermarkEvent());
		filtermark.setVisible(false);

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(respectCase);
		jToolBar.add(regexActive);
		if (!FeatureActivationChecker.isFeatureActivated(Feature.NEW_FILTER)) {
			jToolBar.addSeparator();
			jToolBar.add(filtermark);
		}

		flatTextFieldSearch.setTrailingComponent(jToolBar);
	}

	private void initPopup() {
		popupSearch = new JMenuItem(Configed.getResourceValue("search"));
		popupSearch.addActionListener(actionEvent -> searchTheRow(selectMode));

		JMenuItem popupSearchNext = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnext"));
		popupSearchNext.addActionListener(actionEvent -> searchNextRow(selectMode));
		popupSearchNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		JMenuItem popupNewSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnew"));
		popupNewSearch.addActionListener((ActionEvent actionEvent) -> {
			setFilterMark(false);
			searchTheRow(0, selectMode);
		});

		popupMarkHits = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markall"));
		popupMarkHits.addActionListener(actionEvent -> markAll());
		popupMarkHits.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		popupMarkHits.setEnabled(!FeatureActivationChecker.isFeatureActivated(Feature.NEW_FILTER));

		popupMarkAndFilter = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markAndFilter"));
		popupMarkAndFilter.addActionListener(actionEvent -> markAllAndFilter());
		popupMarkAndFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		popupMarkAndFilter.setEnabled(!FeatureActivationChecker.isFeatureActivated(Feature.NEW_FILTER));

		popupEmptySearchfield = new JMenuItem(Configed.getResourceValue("SearchPane.popup.empty"));
		popupEmptySearchfield.addActionListener(actionEvent -> flatTextFieldSearch.setText(""));

		popupMarkHits.setVisible(false);
		popupMarkAndFilter.setVisible(false);

		Logging.info(this, "buildMenuSearchfield");
		JPopupMenu searchMenu = new JPopupMenu();
		searchMenu.add(popupSearch);
		searchMenu.add(popupSearchNext);
		if (!FeatureActivationChecker.isFeatureActivated(Feature.NEW_FILTER)) {
			searchMenu.add(popupNewSearch);
			searchMenu.add(popupMarkHits);
			searchMenu.add(popupMarkAndFilter);
		}
		searchMenu.add(popupEmptySearchfield);

		flatTextFieldSearch.setComponentPopupMenu(searchMenu);
	}

	private void initNavigationPanel() {
		Dimension navButtonDimension = new Dimension(30, 19);

		JButton nextButton = new JButton(Icons.getIntellijIcon("playForward"));
		nextButton.setToolTipText(Configed.getResourceValue("NavigationPanel.nextEntryTooltip"));
		nextButton.setPreferredSize(navButtonDimension);
		nextButton.addActionListener(event -> associatedPanel.advanceCursor(+1));

		JButton previousButton = new JButton(Icons.getIntellijIcon("playBack"));
		previousButton.setToolTipText(Configed.getResourceValue("NavigationPanel.previousEntryTooltip"));
		previousButton.setPreferredSize(navButtonDimension);
		previousButton.addActionListener(event -> associatedPanel.advanceCursor(-1));

		JButton firstButton = new JButton(Icons.getIntellijIcon("playFirst"));
		firstButton.setToolTipText(Configed.getResourceValue("NavigationPanel.firstEntryTooltip"));
		firstButton.setPreferredSize(navButtonDimension);
		firstButton.addActionListener(event -> associatedPanel.setCursorToFirstRow());

		JButton lastButton = new JButton(Icons.getIntellijIcon("playLast"));
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
		comboSearchFields.setVisible(buttonShowHideExtraOptions.isSelected());
		labelSearch.setVisible(buttonShowHideExtraOptions.isSelected());
	}

	private void setupNarrowLayout() {
		buttonShowHideExtraOptions = new JToggleButton(Icons.getIntellijIcon("arrowLeft"));
		buttonShowHideExtraOptions.setSelectedIcon(Icons.getIntellijIcon("arrowDown"));
		buttonShowHideExtraOptions
				.setToolTipText(Configed.getResourceValue("SearchPane.narrowLayout.extraOptions.toolTip"));
		buttonShowHideExtraOptions.setFocusable(false);
		buttonShowHideExtraOptions.setVisible(false);
		buttonShowHideExtraOptions.addActionListener(event -> showExtraOptions());

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
						.addComponent(comboSearchFields, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		layoutTablesearchPane.setVerticalGroup(layoutTablesearchPane.createSequentialGroup()
				.addGroup(layoutTablesearchPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(flatTextFieldSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonShowHideExtraOptions, 10, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutTablesearchPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
				.addComponent(flatTextFieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		layoutTablesearchPane.setVerticalGroup(layoutTablesearchPane.createParallelGroup(Alignment.CENTER)
				.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(flatTextFieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private boolean allowSearchAction() {
		return isFiltering() && !filtermark.isSelected();
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
		Logging.debug(this, "setSearchFieldsAll ", targetModel);
		Logging.debug(this, "setSearchFieldsAll target model col count ", targetModel.getColumnCount());

		retainOnlyAllFieldsItem();

		for (int i = 0; i < targetModel.getColumnCount(); i++) {
			String colname = targetModel.getColumnName(i);
			comboSearchFields.addItem(colname);
		}

		comboSearchFields.setSelectedIndex(0);
	}

	@Override
	public void requestFocus() {
		flatTextFieldSearch.requestFocusInWindow();
	}

	private boolean stringContains(final String s, final String part) {
		if (part.length() > s.length()) {
			return false;
		}

		if (part.length() == 0) {
			return true;
		}

		boolean success = false;

		int end = s.length() - part.length() + 1;

		for (int i = 0; !success && i < end; i++) {
			success = comparator.compare(s.substring(i, i + part.length()), part) == 0;
		}

		return success;
	}

	private int findViewRowFromValue(int startviewrow, String value) {
		int viewrow = Math.max(0, startviewrow);
		int column = targetModel.findColumn((String) comboSearchFields.getSelectedItem());

		while (viewrow < targetModel.getRowCount()) {
			if (searchForStringInColumns(viewrow, value, column)) {
				return viewrow;
			} else {
				viewrow++;
			}
		}

		return -1;
	}

	private boolean searchForStringInColumns(int viewrow, String value, int column) {
		if (column != -1) {
			// search in that column
			return searchForStringBasedOnSearchMode(value, column, viewrow);
		}

		// Search in all columns
		for (int j = 0; j < targetModel.getColumnCount(); j++) {
			if (searchForStringBasedOnSearchMode(value, j, viewrow)) {
				// We found the value
				return true;
			}
		}

		return false;
	}

	private boolean searchForStringBasedOnSearchMode(String searchPattern, int column, int row) {
		Object cellValue = targetModel.getValueAt(targetModel.getRowForVisualRow(row),
				targetModel.getColForVisualCol(column));
		if (cellValue == null) {
			return false;
		}

		String cellString = cellValue.toString();

		if (regexActive.isSelected()) {
			Pattern regexPattern = Pattern.compile(".*" + searchPattern + ".*",
					respectCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE);
			return regexPattern.matcher(cellString).matches();
		} else {
			if (!respectCase.isSelected()) {
				cellString = cellString.toLowerCase(Locale.ROOT);
				searchPattern = searchPattern.toLowerCase(Locale.ROOT);
			}

			return stringContains(cellString, searchPattern);
		}
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
		Logging.info(this, " markAllAndFilter filtering active", isFiltering());

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
		int rowCount = targetModel.getRowCount();
		if (rowCount == 0) {
			foundrow = -1;
			return;
		}
		if (foundrow >= rowCount) {
			foundrow = 0;
		}
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
		if (row == -1 || row >= targetModel.getRowCount()) {
			targetModel.setSelection(new int[0]);
			row = 0;
		} else if (select) {
			if (addSelection) {
				targetModel.addSelectedRow(row);
			} else {
				targetModel.setSelectedRow(row);
			}
		} else {
			// make only visible
			targetModel.ensureRowIsVisible(row);
		}

		targetModel.setCursorRow(row);
	}

	private void searchTheRow(final int startrow, final boolean addSelection, final boolean select) {
		final String value = flatTextFieldSearch.getText();

		flatTextFieldSearch.getCaret().setVisible(false);

		// Search only for value longer than one digit
		if (value.length() >= 2) {
			foundrow = findViewRowFromValue(startrow, value);

			if (foundrow > -1 || startrow == 0) {
				setRow(foundrow, addSelection, select);
			} else {
				searchTheRow(0, addSelection, select);
			}
		}

		flatTextFieldSearch.getCaret().setVisible(true);
	}

	// ----------------------------------

	private void filtermarkEvent() {
		Logging.info(this, "actionPerformed on filtermark, isFilteredMode ", filtermark.isSelected());

		// When the filtermark is not pressed it means that this event was not evoked
		// by a click on the button. Then we want to manually control what happens with our list
		// and not select some elements. Usually there happens another selection anyways.
		// Also this prevents an Exception in the product table when the selection active, 
		// but is deactivated due to a change in the product tree
		if (filtermark.isSelected() || !filtermark.getModel().isPressed()) {
			setFiltered(filtermark.isSelected());
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
		documentChanged(e);
	}

	private void documentChanged(DocumentEvent e) {
		if (e.getDocument() == flatTextFieldSearch.getDocument()) {
			filter();
			if (filterKey != null) {
				Logging.info(this, "Saving filter state for filter key ", filterKey);
				FilterStateManager.saveFilterState(filterKey, getFilterState());
			}
		}
	}

	private TableFilterState getFilterState() {
		return new TableFilterState(flatTextFieldSearch.getText(), comboSearchFields.getSelectedIndex(),
				regexActive.isSelected(), respectCase.isSelected());
	}

	public void restoreFilter() {
		if (filterKey == null) {
			Logging.debug(this, "Fitler key (", filterKey, ") is null - we can't proceed");
			return;
		}

		TableFilterState filterState = FilterStateManager.getFilterState(filterKey);
		if (filterState != null) {
			setFilterState(filterState);
			filter();
		} else {
			Logging.debug(this, "Filter state for key ", filterKey, " is null");
		}
	}

	private void setFilterState(TableFilterState state) {
		if (state == null) {
			Logging.debug(this, "Filter state is null");
			return;
		}
		flatTextFieldSearch.setText(state.getSearchText());
		comboSearchFields.setSelectedIndex(state.getSearchColumnIndex());
		regexActive.setSelected(state.isRegexActive());
		respectCase.setSelected(state.isRespectCase());
	}

	private void filter() {
		int columnIndex = targetModel.findColumn((String) comboSearchFields.getSelectedItem());
		String searchText = flatTextFieldSearch.getText();
		targetModel.applyFilter(searchText, columnIndex, regexActive.isSelected(), respectCase.isSelected());
	}

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (flatTextFieldSearch.getText().isBlank()) {
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_F5) {
			if (allowSearchAction()) {
				markAll();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_F8) {
			if (allowSearchAction()) {
				markAllAndFilter();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_F3) {
			searchNextRow(selectMode);
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
