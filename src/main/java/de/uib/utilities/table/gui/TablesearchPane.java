/*
 * TablesearchPane.java // originally named SearchPane.java
 *
 * By uib, www.uib.de, 2011-2013, 2017,2020
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.Mapping;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.savedstates.SaveInteger;
import de.uib.utilities.swing.AbstractNavigationPanel;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.swing.JMenuItemFormatted;

public class TablesearchPane extends JPanel implements DocumentListener, KeyListener, ActionListener {
	javax.swing.JFrame masterFrame = ConfigedMain.getMainFrame();

	JTextField fieldSearch;

	private boolean searchActive = false;
	protected boolean filtering = false;

	int blinkrate = 0;
	JComboBox<String> comboSearchFields;
	JComboBoxToolTip comboSearchFieldsMode;

	CheckedLabel markReload;

	JLabel labelSearch;
	CheckedLabel checkmarkSearch;
	CheckedLabel checkmarkSearchProgressive;
	JLabel labelSearchMode;
	CheckedLabel filtermark;
	CheckedLabel checkmarkAllColumns;
	CheckedLabel checkmarkFullText;

	JLabel labelFilterMarkGap;

	AbstractNavigationPanel navPane;
	PanelGenEditTable associatedPanel;
	boolean withNavPane = false;

	JPopupMenu searchMenu;
	LinkedHashMap<JMenuItemFormatted, Boolean> searchMenuEntries;

	JMenuItemFormatted popupSearch;
	JMenuItemFormatted popupSearchNext;
	JMenuItemFormatted popupNewSearch;
	JMenuItemFormatted popupMarkHits;
	JMenuItemFormatted popupMarkAndFilter;
	JMenuItemFormatted popupEmptySearchfield;

	public enum SearchMode {
		FULL_TEXT_SEARCHING_WITH_ALTERNATIVES, FULL_TEXT_SEARCHING_ONE_STRING, START_TEXT_SEARCHING, REGEX_SEARCHING
	}

	public static final int FULL_TEXT_SEARCH = 0;
	public static final int START_TEXT_SEARCH = 1;
	public static final int REGEX_SEARCH = 2;

	protected int preferredColumnIndex = 0;

	protected boolean withRegEx = true;
	protected boolean selectMode = true;
	protected boolean resetFilterModeOnNewSearch = true;

	private int foundrow = -1;

	protected SearchTargetModel targetModel;
	protected PanelGenEditTable associatedTable;

	final Comparator<Object> comparator;
	Map<String, Mapping<Integer, String>> mappedValues;

	public enum SearchInputType {
		LINE, PROGRESSIVE
	}

	protected SearchInputType searchInputType = SearchInputType.PROGRESSIVE;

	protected SaveInteger saveSearchpaneProgressiveSearch;
	protected SaveInteger saveSearchpaneAllColumnsSearch;
	protected SaveInteger saveSearchpaneFullTextSearch;

	/**
	 * main constructor
	 * 
	 * @param SearchTargetModel the model for delivering data and selecting
	 * @param boolean           modifies the search function
	 * @param int               gives the single column ( in natural counting)
	 *                          in the case of single column search
	 * @param String            saving of states is activated, the keys are
	 *                          tagged with the parameter
	 */
	public TablesearchPane(SearchTargetModel targetModel, boolean withRegEx, int prefColNo,
			String savedStatesObjectTag) {
		comparator = Globals.getCollator();
		mappedValues = new HashMap<>();
		this.withRegEx = withRegEx;
		this.preferredColumnIndex = prefColNo;

		filtering = true;

		initSavedStates(savedStatesObjectTag);
		init();

		this.targetModel = targetModel;
	}

	private void init() {
		setSearchFieldsAll();
		initComponents();
		setNarrow(false);
	}

	/**
	 * sets frame to return to e.g. from option dialogs
	 * 
	 * @param javax.swing.JFrame
	 */
	public void setMasterFrame(javax.swing.JFrame masterFrame) {
		this.masterFrame = masterFrame;
	}

	private void initSavedStates(String savedStatesObject) {
		if (savedStatesObject != null) {
			saveSearchpaneProgressiveSearch = new SaveInteger(savedStatesObject + ".progressiveSearch", 0,
					Configed.savedStates);
			saveSearchpaneAllColumnsSearch = new SaveInteger(savedStatesObject + ".allColumnsSearch", 0,
					Configed.savedStates);
			saveSearchpaneFullTextSearch = new SaveInteger(savedStatesObject + ".fullTextSearch", 0,
					Configed.savedStates);
		}
	}

	/**
	 * @param SearchTargetModel the model for delivering data and selecting
	 * @param boolean           modifies the search function
	 * @param String            saving of states is activated, the keys are
	 *                          tagged with the parameter
	 */
	public TablesearchPane(SearchTargetModel targetModel, boolean withRegEx, String savedStatesObjectTag) {
		this(targetModel, withRegEx, 0, savedStatesObjectTag);

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
	public TablesearchPane(PanelGenEditTable thePanel, boolean withRegEx, String savedStatesObjectTag) {
		this(new SearchTargetModelFromTable(thePanel), withRegEx, savedStatesObjectTag);
		associatedPanel = thePanel;
	}

	/**
	 * a search target model is produces from a JTable
	 * 
	 * @param JTable  the model for delivering data and selecting
	 * @param boolean
	 * @param String  saving of states is activated, the keys are tagged with
	 *                the parameter
	 */
	public TablesearchPane(JTable table, boolean withRegEx, String savedStatesObjectTag) {
		this(new SearchTargetModelFromTable(table), withRegEx, savedStatesObjectTag);
	}

	/**
	 * a search target model is produces from a JTable the regex parameter
	 * default false is used
	 * 
	 * @param SearchTargetModel
	 * @param String            saving of states is activated, the keys are
	 *                          tagged with the parameter
	 */
	public TablesearchPane(SearchTargetModel targetModel, String savedStatesObjectTag) {
		this(targetModel, false, savedStatesObjectTag);

	}

	/**
	 * constructor only for testing purposes, its use entails NPEs
	 */
	public TablesearchPane() {
		this(null, null);
	}

	public void setWithNavPane(boolean b) {
		navPane.setVisible(b);
		withNavPane = b;
	}

	public void setMultiSelection(boolean b) {
		popupMarkHits.setVisible(b);
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

	public void setMapping(String columnName, Mapping<Integer, String> mapping) {
		mappedValues.put(columnName, mapping);

	}

	public void setSelectMode(boolean select) {
		this.selectMode = select;
	}

	public void setFieldFont(java.awt.Font font) {
		fieldSearch.setFont(font);

	}

	public void setFieldBackground(java.awt.Color color) {
		fieldSearch.setBackground(color);
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		checkmarkAllColumns.setEnabled(b);
		checkmarkFullText.setEnabled(b);
		fieldSearch.setEnabled(b);
	}

	private boolean filteredMode = false;

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
		if (filtermark.isSelected() == null || filtermark.isSelected() != b) {
			filtermark.setSelected(b);
		}
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

	public void setToolTipTextCheckMarkAllColumns(String s) {
		checkmarkAllColumns.setToolTipText(s);
	}

	private void initComponents() {
		setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		navPane = new AbstractNavigationPanel() {

			@Override
			public void next() {
				associatedPanel.advanceCursor(+1);
			}

			@Override
			public void previous() {
				associatedPanel.advanceCursor(-1);
			}

			@Override
			public void first() {
				associatedPanel.setCursorToFirstRow();
			}

			@Override
			public void last() {
				associatedPanel.setCursorToLastRow();
			}
		};

		navPane.setVisible(false);

		Icon iconReload = Globals.createImageIcon("images/reload_blue16.png", "");
		markReload = new CheckedLabel(iconReload, true);

		// in the moment, it's a proof of concept
		markReload.setVisible(false);

		labelSearch = new JLabel(Configed.getResourceValue("SearchPane.search"));
		labelSearch.setFont(Globals.defaultFont);

		Icon unselectedIconSearch = Globals.createImageIcon("images/loupe_light_16.png", "");
		Icon selectedIconSearch = Globals.createImageIcon("images/loupe_light_16_x.png", "");

		try {
			checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);
		} catch (ClassCastException ex) {
			Logging.warning(this, "strange nimbus exception, retry creating");
			checkmarkSearch = new CheckedLabel(selectedIconSearch, unselectedIconSearch, false);
		}

		checkmarkSearch.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearch.tooltip"));
		checkmarkSearch.addActionListener(this);
		checkmarkSearch.setChangeStateAutonomously(false);

		selectedIconSearch = Globals.createImageIcon("images/loupe_light_16_progressiveselect.png", "");
		unselectedIconSearch = Globals.createImageIcon("images/loupe_light_16_blockselect.png", "");

		boolean active = true;

		if (saveSearchpaneProgressiveSearch != null) {
			active = (saveSearchpaneProgressiveSearch.deserializeAsInt() == 0);
		}

		checkmarkSearchProgressive = new CheckedLabel(selectedIconSearch, unselectedIconSearch, active);
		if (active) {
			searchInputType = SearchInputType.PROGRESSIVE;
		} else {
			searchInputType = SearchInputType.LINE;
		}

		checkmarkSearchProgressive.addActionListener(this);
		checkmarkSearchProgressive.setChangeStateAutonomously(true);
		checkmarkSearchProgressive
				.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkSearchProgressive.tooltip"));

		fieldSearch = new JTextField("");
		fieldSearch.setPreferredSize(Globals.textfieldDimension);

		fieldSearch.setFont(Globals.defaultFontBig);
		fieldSearch.setBackground(Globals.BACKGROUND_COLOR_8);

		fieldSearch.getCaret().setBlinkRate(blinkrate);
		fieldSearch.setToolTipText(Configed.getResourceValue("SearchPane.searchField.toolTip"));

		fieldSearch.getDocument().addDocumentListener(this);

		fieldSearch.addKeyListener(this);

		try {
			popupSearch = new JMenuItemFormatted();
			popupSearchNext = new JMenuItemFormatted();
			popupNewSearch = new JMenuItemFormatted();
			popupMarkHits = new JMenuItemFormatted();
			popupMarkAndFilter = new JMenuItemFormatted();
			popupEmptySearchfield = new JMenuItemFormatted();
		} catch (Exception ex) {
			// TODO WHAT? WHY DOING EXACTLY THE SAME AGAIN?
			// we often get a class cast error
			popupSearch = new JMenuItemFormatted();
			popupSearchNext = new JMenuItemFormatted();
			popupNewSearch = new JMenuItemFormatted();
			popupMarkHits = new JMenuItemFormatted();
			popupMarkAndFilter = new JMenuItemFormatted();
			popupEmptySearchfield = new JMenuItemFormatted();
		}

		searchMenuEntries = new LinkedHashMap<>();
		searchMenuEntries.put(popupSearch, true);
		searchMenuEntries.put(popupSearchNext, true);
		searchMenuEntries.put(popupNewSearch, true);
		searchMenuEntries.put(popupMarkHits, false);
		searchMenuEntries.put(popupMarkAndFilter, false);

		searchMenuEntries.put(popupEmptySearchfield, true);
		buildMenuSearchfield();

		popupSearch.setText("Suchen");
		popupSearch.addActionListener(actionEvent -> searchTheRow(selectMode));

		popupSearchNext.setText(Configed.getResourceValue("SearchPane.popup.searchnext"));

		popupSearchNext.addActionListener(actionEvent -> searchNextRow(selectMode));

		popupNewSearch.setText(Configed.getResourceValue("SearchPane.popup.searchnew"));

		popupNewSearch.addActionListener(actionEvent -> {

			targetModel.setFiltered(false);
			if (resetFilterModeOnNewSearch) {
				setFilteredMode(false);
			}

			searchTheRow(0, selectMode);
		});

		popupMarkHits.setText(Configed.getResourceValue("SearchPane.popup.markall"));

		popupMarkHits.addActionListener(actionEvent -> {
			if (!fieldSearch.getText().equals("")) {
				markAll();
			}
		});

		popupMarkAndFilter.setText(Configed.getResourceValue("SearchPane.popup.markAndFilter"));

		popupMarkAndFilter.addActionListener(actionEvent -> {
			switchFilterOff();
			markAllAndFilter();
			switchFilterOn();
		});

		popupEmptySearchfield.setText(Configed.getResourceValue("SearchPane.popup.empty"));

		popupEmptySearchfield.addActionListener(actionEvent -> fieldSearch.setText(""));

		fieldSearch.addActionListener(actionEvent -> {
			if (searchInputType == SearchInputType.PROGRESSIVE) {
				searchNextRow(selectMode);
			}
		});

		comboSearchFields = new JComboBox<>(new String[] { Configed.getResourceValue("SearchPane.search.allfields") });
		comboSearchFields.setPreferredSize(Globals.lowerButtonDimension);
		comboSearchFields.setFont(Globals.defaultFont);

		setSearchFieldsAll();

		labelSearchMode = new JLabel(Configed.getResourceValue("SearchPane.searchmode.searchmode"));
		labelSearchMode.setFont(Globals.defaultFont);

		Map<String, String> tooltipsMap = new LinkedHashMap<>();

		tooltipsMap.put(Configed.getResourceValue("SearchPane.searchmode.fulltext"),
				Configed.getResourceValue("SearchPane.mode.fulltext.tooltip"));
		tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.starttext"),
				Configed.getResourceValue("SearchPane.mode.starttext.tooltip"));
		if (withRegEx) {
			tooltipsMap.put(Configed.getResourceValue("SearchPane.mode.regex"),
					Configed.getResourceValue("SearchPane.mode.regex.tooltip"));
		}

		try {

			comboSearchFieldsMode = new JComboBoxToolTip();
			comboSearchFieldsMode.setFont(Globals.defaultFont);
		} catch (Exception ex) {

			Logging.warning(this, "strange nimbus exception, retry creating JComboBox " + ex);

			comboSearchFieldsMode = new JComboBoxToolTip();
			comboSearchFieldsMode.setFont(Globals.defaultFont);
		}

		comboSearchFieldsMode.setValues(tooltipsMap, false);
		comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);

		comboSearchFieldsMode.setPreferredSize(Globals.lowerButtonDimension);

		Icon unselectedIconFilter = Globals.createImageIcon("images/filter_14x14_open.png", "");
		Icon selectedIconFilter = Globals.createImageIcon("images/filter_14x14_closed.png", "");
		Icon nullIconFilter = Globals.createImageIcon("images/filter_14x14_inwork.png", "");

		filtermark = new CheckedLabel("", selectedIconFilter, unselectedIconFilter, nullIconFilter, false);
		filtermark.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filtermark.addActionListener(this);

		labelFilterMarkGap = new JLabel("");

		showFilterIcon(filtering);

		Icon unselectedIcon;
		Icon selectedIcon;

		unselectedIcon = Globals.createImageIcon("images/loupe_light_16_singlecolumnsearch.png", "");
		selectedIcon = Globals.createImageIcon("images/loupe_light_16_multicolumnsearch.png", "");

		active = true;
		if (saveSearchpaneAllColumnsSearch != null) {
			active = (saveSearchpaneAllColumnsSearch.deserializeAsInt() == 0);
		}

		checkmarkAllColumns = new CheckedLabel(selectedIcon, unselectedIcon, active);

		checkmarkAllColumns.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkAllColumns.tooltip"));
		checkmarkAllColumns.addActionListener(this);

		unselectedIcon = Globals.createImageIcon("images/loupe_light_16_starttextsearch.png", "");
		selectedIcon = Globals.createImageIcon("images/loupe_light_16_fulltextsearch.png", "");

		active = true;
		if (saveSearchpaneFullTextSearch != null) {
			active = (saveSearchpaneFullTextSearch.deserializeAsInt() == 0);
		}

		checkmarkFullText = new CheckedLabel(selectedIcon, unselectedIcon, active);

		if (active) {
			comboSearchFieldsMode.setSelectedIndex(FULL_TEXT_SEARCH);
		} else {
			comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);
		}

		checkmarkFullText.setToolTipText(Configed.getResourceValue("SearchPane.checkmarkFullText.tooltip"));
		checkmarkFullText.addActionListener(this);

		GroupLayout layoutTablesearchPane = new GroupLayout(this);
		this.setLayout(layoutTablesearchPane);

		int checkedLabelWidth = 18;
		layoutTablesearchPane.setHorizontalGroup(layoutTablesearchPane
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutTablesearchPane.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(markReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(checkmarkSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(fieldSearch, Globals.ICON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(filtermark, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
						.addComponent(labelFilterMarkGap, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2,
								Globals.HGAP_SIZE / 2)
						.addComponent(checkmarkSearchProgressive, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkAllColumns, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
						.addComponent(checkmarkFullText, checkedLabelWidth, checkedLabelWidth, checkedLabelWidth)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(comboSearchFields, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)));

		layoutTablesearchPane.setVerticalGroup(layoutTablesearchPane.createSequentialGroup()

				.addGroup(layoutTablesearchPane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(markReload, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(navPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(filtermark, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelFilterMarkGap, 10, 10, 10)
						.addComponent(checkmarkAllColumns, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkFullText, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkSearch, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(checkmarkSearchProgressive, 10, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSearchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchFieldsMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSearchFields, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

		);

	}

	private void buildMenuSearchfield() {
		Logging.info(this, "buildMenuSearchfield");
		searchMenu = new JPopupMenu();
		for (Entry<JMenuItemFormatted, Boolean> searchMenuEntry : searchMenuEntries.entrySet()) {
			if (Boolean.TRUE.equals(searchMenuEntry.getValue())) {
				searchMenu.add(searchMenuEntry.getKey());
			}
		}
		fieldSearch.setComponentPopupMenu(searchMenu);
		Logging.info(this, "buildMenuSearchfield " + searchMenu);
	}

	public void setSearchFields(Integer[] cols) {
		for (int j = 0; j < cols.length; j++) {
			comboSearchFields.addItem(targetModel.getColumnName(cols[j]));
		}
	}

	public void setSearchFieldsAll() {
		Logging.debug(this, "setSearchFieldsAll " + targetModel);
		if (targetModel != null) {
			Logging.debug(this, "setSearchFieldsAll target model col count " + targetModel.getColumnCount());

			for (int i = 0; i < targetModel.getColumnCount(); i++) {
				String colname = targetModel.getColumnName(i);
				comboSearchFields.addItem(colname);

			}

			if ((saveSearchpaneAllColumnsSearch == null) || saveSearchpaneAllColumnsSearch.deserializeAsInt() == 0
					|| preferredColumnIndex >= targetModel.getColumnCount()) {
				comboSearchFields.setSelectedIndex(0);
			} else {
				comboSearchFields.setSelectedIndex(preferredColumnIndex);
			}
		}

	}

	public void setSearchFields(List<String> fieldList) {
		for (String fieldName : fieldList) {
			if (((DefaultComboBoxModel) comboSearchFields.getModel()).getIndexOf(fieldName) == -1) {
				comboSearchFields.addItem(fieldName);
			}
		}
	}

	public void setSelectedSearchField(String field) {
		comboSearchFields.setSelectedItem(field);
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
		fieldSearch.requestFocus();
	}

	private class Finding {
		boolean success = false;
		int endChar = -1;
	}

	private Finding stringContainsParts(final String colname, final String s, String[] parts) {
		String realS = s;

		if (mappedValues.get(colname) != null) {
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		}

		return stringContainsParts(realS, parts);
	}

	private Finding stringContainsParts(final String s, String[] parts) {
		Finding result = new Finding();

		String remainder = s;

		if (s == null) {
			return result;
		}

		if (parts == null) {
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
				} else {
					if (remainder.length() > 0) {
						remainder = remainder.substring(partSearch.endChar);
					} else {
						result.success = false;
					}
				}
			} else {
				result.success = false;
				searching = false;
			}
		}

		return result;
	}

	private Finding stringContains(final String s, final String part) {
		return stringContains(null, s, part);
	}

	private Finding stringContains(final String colname, final String s, final String part) {
		Finding result = new Finding();

		if (s == null || part == null) {
			return result;
		}

		String realS = s;

		if (colname != null && mappedValues.get(colname) != null) {
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		}

		if (realS == null || part.length() > realS.length()) {
			return result;
		}

		if (part.length() == 0) {
			result.success = true;
			result.endChar = 0;
			return result;
		}

		result.success = false;

		int i = 0;

		int end = realS.length() - part.length() + 1;

		while (!result.success && i < end) {
			result.success = (comparator.compare(realS.substring(i, i + part.length()), part) == 0);
			result.endChar = i + part.length() - 1;
			i++;
		}

		return result;
	}

	private boolean stringStartsWith(final String colname, final String s, final String part) {
		if (s == null) {
			return false;
		}

		if (part == null) {
			return false;
		}

		String realS = s;
		if (mappedValues.get(colname) != null) {
			realS = mappedValues.get(colname).getMapOfStrings().get(s);
		}

		if (part.length() > realS.length()) {
			return false;
		}

		if (part.length() == 0) {
			return true;
		}

		return (comparator.compare(realS.substring(0, part.length()), part) == 0);
	}

	protected int findViewRowFromValue(int startviewrow, Object value, Set<Integer> colIndices, boolean fulltext,
			boolean regex, boolean combineCols) {

		Logging.debug(this,
				"findViewRowFromValue(int startviewrow, Object value, Set colIndices, boolean fulltext, boolean regex): "
						+ startviewrow + ", " + value + ", " + colIndices + ", " + fulltext + ", " + regex + ", "
						+ combineCols);

		if (value == null) {
			return -1;
		}

		String val = value.toString().toLowerCase();

		if (val.length() < 2) {
			return -1;
		}
		// dont start searching for single chars

		String[] valParts = val.split(" ");

		boolean found = false;

		int viewrow = 0;

		if (startviewrow > 0) {
			viewrow = startviewrow;
		}

		Pattern pattern = null;
		if (regex) {
			try {
				if (fulltext) {
					val = ".*" + val + ".*";
				}

				pattern = Pattern.compile(val);
			} catch (java.util.regex.PatternSyntaxException ex) {
				Logging.info(this, "pattern problem " + ex);
				return -1;
			}
		}

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
						String valSJ = ("" + valJ).toLowerCase();

						String colname = targetModel.getColumnName(colJ);

						if (mappedValues.get(colname) != null) {
							valSJ = mappedValues.get(colname).getMapOfStrings().get(valSJ);
						}

						buffRow.append(valSJ);
					}
				}

				String compareVal = buffRow.toString();

				if (compareVal.equals("")) {
					if (val.equals("")) {
						found = true;
					}
				} else {
					found = stringContainsParts(compareVal, valParts).success;
				}

			}

			else {
				for (int j = 0; j < targetModel.getColumnCount(); j++) {

					// we dont compare all values (comparing all values is default)
					if (colIndices != null && !colIndices.contains(j)) {
						continue;
					}

					int colJ = targetModel.getColForVisualCol(j);

					Object compareValue = targetModel.getValueAt(

							targetModel.getRowForVisualRow(viewrow), colJ

					);

					if (compareValue == null) {
						if (val.equals("")) {
							found = true;
						}
					}

					else {
						String compareVal = ("" + compareValue).toLowerCase();

						if (regex) {

							if (pattern.matcher(compareVal).matches()) {
								found = true;
							}
						}

						else {
							if (fulltext) {
								found = stringContainsParts(targetModel.getColumnName(colJ), compareVal,
										valParts).success;
							} else {

								found = stringStartsWith(targetModel.getColumnName(colJ), compareVal, val);

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

	public boolean isSearchActive() {
		return searchActive;
	}

	public void setResetFilterModeOnNewSearch(boolean b) {
		resetFilterModeOnNewSearch = b;
	}

	/**
	 * select all rows with value from searchfield
	 */
	public void markAll() {
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
		Logging.info(this, " markAllAndFilter filtering, disabledSinceWeAreInFilteredModel " + filtering + ", "
				+ disabledSinceWeAreInFilteredMode());
		if (!filtering) {
			return;
		}

		if (!disabledSinceWeAreInFilteredMode() && !fieldSearch.getText().equals("")) {
			markAll();
		}
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

		HashSet<Integer> selectedCols0 = null;

		if (comboSearchFields.getSelectedIndex() > 0) {
			selectedCols0 = new HashSet<>();
			selectedCols0.add(targetModel.findColumn((String) comboSearchFields.getSelectedItem()));
		}

		final Set<Integer> selectedCols = selectedCols0;

		final boolean fulltextSearch = (comboSearchFieldsMode.getSelectedIndex() == FULL_TEXT_SEARCH);
		final boolean regexSearch = (comboSearchFieldsMode.getSelectedIndex() == REGEX_SEARCH);
		final boolean combineCols = fulltextSearch;

		fieldSearch.getCaret().setVisible(false);

		// final de.uib.utilities.thread.WaitCursor waitCursor = new

		if (value.length() < 2) {
			setRow(0, false, select);
		}

		else {
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

	public void scrollRowToVisible(int row) {
		targetModel.ensureRowIsVisible(row);
	}

	public void addSelectedRow(int row) {
		targetModel.addSelectedRow(row);
	}

	public void setSelectedRow(int row) {
		targetModel.setSelectedRow(row);
	}

	// ----------------------------------

	private void switchFilterOff() {
		if (targetModel.isFiltered()) {

			targetModel.setFiltered(false);
			setFilteredMode(false);
		}
	}

	private void switchFilterOn() {
		if (!targetModel.isFiltered()) {

			targetModel.setFiltered(true);
			setFilteredMode(true);

		}
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
			if (!disabledSinceWeAreInFilteredMode() && !fieldSearch.getText().equals("")) {
				markAll();
			}
		}

		else if (e.getKeyCode() == KeyEvent.VK_F8) {
			switchFilterOff();
			markAllAndFilter();
			switchFilterOn();
		}

		else if (e.getKeyCode() == KeyEvent.VK_F3) {
			if (!disabledSinceWeAreInFilteredMode() && !fieldSearch.getText().equals("")) {
				searchNextRow(selectMode);
			}
		}

		else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			Logging.debug(this, "key pressed ENTER on fieldSearch, with content " + fieldSearch.getText()
					+ " searchInputType " + searchInputType);

			if (searchInputType == SearchInputType.LINE && !fieldSearch.getText().equals("")) {
				switchFilterOff();
				markAllAndFilter();
				switchFilterOn();
			}
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
		if (e.getSource() == checkmarkAllColumns) {
			Logging.debug(this, "actionPerformed on checkmarkAllColumns");

			if (Boolean.TRUE.equals(checkmarkAllColumns.isSelected())) {
				// all columns
				comboSearchFields.setSelectedIndex(0);
				if (saveSearchpaneAllColumnsSearch != null) {
					saveSearchpaneAllColumnsSearch.serialize(0);
				}
			} else {
				comboSearchFields.setSelectedIndex(preferredColumnIndex);
				if (saveSearchpaneAllColumnsSearch != null) {
					saveSearchpaneAllColumnsSearch.serialize(1);
				}
			}
		} else if (e.getSource() == checkmarkFullText) {
			Logging.debug(this, "actionPerformed on checkmarkFullText");

			if (Boolean.TRUE.equals(checkmarkFullText.isSelected())) {
				comboSearchFieldsMode.setSelectedIndex(FULL_TEXT_SEARCH);
				if (saveSearchpaneFullTextSearch != null) {
					saveSearchpaneFullTextSearch.serialize(0);
				}
			} else {
				comboSearchFieldsMode.setSelectedIndex(START_TEXT_SEARCH);
				if (saveSearchpaneFullTextSearch != null) {
					saveSearchpaneFullTextSearch.serialize(1);
				}
			}
		} else if (e.getSource() == filtermark) {
			Logging.info(this, "actionPerformed on filtermark, targetModel.isFiltered " + targetModel.isFiltered());

			if (targetModel.isFiltered()) {

				int[] unfilteredSelection = targetModel.getUnfilteredSelection();

				targetModel.setFiltered(false);
				setFilteredMode(false);

				if (unfilteredSelection.length != 0) {
					targetModel.setSelection(unfilteredSelection);
				}
			} else {
				switchFilterOn();
			}
		} else if (e.getSource() == checkmarkSearch) {
			Logging.debug(this,
					"actionPerformed on checkmarkSearch, targetModel.isFiltered " + targetModel.isFiltered());

			fieldSearch.setText("");
		} else if (e.getSource() == checkmarkSearchProgressive) {
			Logging.debug(this,
					"actionPerformed on checkmarkSearchProgressiv, set to  " + checkmarkSearchProgressive.isSelected());
			if (Boolean.TRUE.equals(checkmarkSearchProgressive.isSelected())) {
				searchInputType = SearchInputType.PROGRESSIVE;
				if (saveSearchpaneProgressiveSearch != null) {
					saveSearchpaneProgressiveSearch.serialize(0);
				}
			} else {
				searchInputType = SearchInputType.LINE;
				if (saveSearchpaneProgressiveSearch != null) {
					saveSearchpaneProgressiveSearch.serialize(1);
				}
			}

			Logging.debug(this,
					"actionPerformed on checkmarkSearchProgressiv, searchInputType set to " + searchInputType);
		}
	}

	public void setReloadActionHandler(ActionListener al) {
		checkmarkSearch.addActionListener(al);
	}
}
