/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModel;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.TableFilterState;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class SearchPaneComponent extends AbstractTeaComponent<SearchPaneModel, SearchPaneMsg, SearchPaneEffect>
		implements KeyListener {
	private SearchTargetModel targetModel;
	private FilterKey filterKey;
	private PanelGenEdit associatedPanel;
	private boolean isNarrow;
	private boolean showNavPanel;
	private boolean showFilterMark;

	private FlatTextField searchField;
	private JComboBox<String> searchColumnCombo;
	private JToggleButton respectCaseBtn;
	private JToggleButton regexBtn;
	private JToggleButton filterMarkBtn;
	private JPanel navPane;
	private JPanel mainPanel;

	private JMenuItem popupNewSearch;
	private JMenuItem popupSearchNext;
	private JMenuItem popupMarkAllAndFilter;
	private JMenuItem popupEmptySearchfield;

	private SideEffectStrategy sideEffectStrategy;

	public interface SideEffectStrategy {
		/**
		 * Given an effect, return the side effect's action to execute. Return
		 * null if no side effect's action is needed.
		 */
		Runnable getActionFor(SearchPaneEffect effect);
	}

	private ItemListener searchColumnItemListener = (ItemEvent e) -> {
		if (e.getStateChange() == 1) {
			dispatch(new SearchPaneMsg.FieldChangeMsg.ChangeSearchColumn(searchColumnCombo.getSelectedIndex()));
		}
	};

	public SearchPaneComponent(SearchTargetModel targetModel) {
		this(null, targetModel, null, false, false, false);
	}

	public SearchPaneComponent(SearchTargetModel targetModel, FilterKey filterKey) {
		this(null, targetModel, filterKey, false, false, false);
	}

	public SearchPaneComponent(SearchTargetModel targetModel, FilterKey filterKey, boolean isNarrow, boolean isNavPane,
			boolean showFilterMark) {
		this(null, targetModel, filterKey, isNarrow, isNavPane, showFilterMark);
	}

	public SearchPaneComponent(PanelGenEdit associatedPanel, SearchTargetModel targetModel) {
		this(associatedPanel, targetModel, null, false, false, false);
	}

	public SearchPaneComponent(PanelGenEdit panel, SearchTargetModel targetModel, FilterKey filterKey, boolean isNarrow,
			boolean isNavPane, boolean showFilterMark) {
		super();
		this.targetModel = targetModel;
		this.associatedPanel = panel;
		this.filterKey = filterKey;
		this.isNarrow = isNarrow;
		this.showNavPanel = isNavPane;
		this.showFilterMark = showFilterMark;
	}

	public void setSideEffectStrategy(SideEffectStrategy sideEffectStrategy) {
		this.sideEffectStrategy = sideEffectStrategy;
	}

	@Override
	protected SearchPaneModel initModel() {
		return SearchPaneModel.builder().filterKey(filterKey).isNarrow(isNarrow).showNavPanel(showNavPanel)
				.showFilterMark(showFilterMark).build();
	}

	@Override
	protected UpdateResult<SearchPaneModel, SearchPaneEffect> updateModel(SearchPaneMsg msg, SearchPaneModel model) {
		return TableSearchPaneUpdate.update(msg, model);
	}

	@Override
	protected JComponent renderView(SearchPaneModel model, Consumer<SearchPaneMsg> dispatch) {
		Logging.info(this, "Rendering TableSearchPane for model: " + model);

		if (mainPanel == null) {
			initComponents(dispatch);
			initPopup();
		}

		updateViewFromModel(model);

		return mainPanel;
	}

	@Override
	protected void refreshView() {
		updateViewFromModel(model);
		if (mainPanel != null) {
			mainPanel.revalidate();
			mainPanel.repaint();
		}
	}

	@Override
	protected void handleEffect(SearchPaneEffect effect) {
		switch (effect) {
		case SearchPaneEffect.UIEffect e -> handleUIEffect(e);
		case SearchPaneEffect.ServiceEffect s -> handleServiceEffect(s);
		}
	}

	private void handleUIEffect(SearchPaneEffect.UIEffect effect) {
		switch (effect) {
		case SearchPaneEffect.UIEffect.NavigateToRow(int row) -> onNavigateToRow(row);
		case SearchPaneEffect.UIEffect.SelectAll() -> onSelectAll();
		case SearchPaneEffect.UIEffect.FilterMarkTriggered() -> onFilterMarkTriggered(effect);
		}
	}

	private void onNavigateToRow(int row) {
		if (targetModel != null && row <= targetModel.getRowCount()) {
			if (model.isSelectMode()) {
				targetModel.setSelectedRow(row);
			}
			targetModel.setCursorRow(row);
			targetModel.ensureRowIsVisible(row);
		}
	}

	private void onSelectAll() {
		targetModel.setValueIsAdjusting(true);
		targetModel.clearSelection();

		for (int i = 0; i < targetModel.getRowCount(); i++) {
			targetModel.addSelectedRow(i);
		}

		targetModel.setValueIsAdjusting(false);
	}

	private void onFilterMarkTriggered(SearchPaneEffect effect) {
		if (sideEffectStrategy == null) {
			return;
		}
		Runnable action = sideEffectStrategy.getActionFor(effect);
		if (action != null) {
			action.run();
		}
	}

	@SuppressWarnings("java:S103")
	private void handleServiceEffect(SearchPaneEffect.ServiceEffect effect) {
		if (targetModel == null) {
			return;
		}

		switch (effect) {
		case SearchPaneEffect.ServiceEffect.ApplyFilter(String query, int col, boolean regex, boolean caseSensitive) -> onApplyFilter(
				query, col, regex, caseSensitive);
		case SearchPaneEffect.ServiceEffect.MarkSelectedAndFilter(boolean isFiltered) -> onMarkSelectedAndFilter(
				isFiltered);
		case SearchPaneEffect.ServiceEffect.MarkAllAndFilter() -> onMarkAllAndFilter();
		case SearchPaneEffect.ServiceEffect.SearchNextRow() -> onSearchNextRow();
		case SearchPaneEffect.ServiceEffect.ResetSearch() -> {
			targetModel.setSelectedRow(0);
			targetModel.setCursorRow(0);
		}
		}
	}

	private void onApplyFilter(String query, int col, boolean regex, boolean caseSensitive) {
		int found = findNextRow(query, col, regex, caseSensitive, model.getFoundRow() + 1);
		dispatch(new SearchPaneMsg.EffectResultMsg.SearchCompleted(found));
		int modelColumnIndex = targetModel.findColumn(searchColumnCombo.getItemAt(col));
		targetModel.applyFilter(query, modelColumnIndex, regex, caseSensitive);
	}

	private void onMarkSelectedAndFilter(boolean isFiltered) {
		if (isFiltered) {
			targetModel.setFiltered(isFiltered);
			popupMarkAllAndFilter.setEnabled(!isFiltered);
		} else {
			int[] unfilteredSelection = targetModel.getUnfilteredSelection();

			targetModel.setFiltered(false);
			popupMarkAllAndFilter.setEnabled(true);

			if (unfilteredSelection.length != 0) {
				targetModel.setSelection(unfilteredSelection);
			}
		}
	}

	private void onMarkAllAndFilter() {
		filterMarkBtn.setSelected(false);
		onSelectAll();
		filterMarkBtn.setSelected(true);
	}

	private void onSearchNextRow() {
		int foundRow = findNextRow(model.getSearchText(), model.getSearchColumnIndex(), model.isRegexActive(),
				model.isRespectCase(), model.getFoundRow() + 1);
		foundRow = foundRow == -1 ? 0 : foundRow;
		dispatch(new SearchPaneMsg.EffectResultMsg.SearchCompleted(foundRow));
		if (foundRow <= targetModel.getRowCount()) {
			if (model.isSelectMode()) {
				targetModel.setSelectedRow(foundRow);
			}
			targetModel.setCursorRow(foundRow);
		}
	}

	private int findNextRow(String query, int col, boolean regex, boolean caseSensitive, int startRow) {
		if (query == null || targetModel == null) {
			return -1;
		}

		int rowCount = targetModel.getRowCount();
		if (rowCount == 0) {
			return -1;
		}

		int foundRow = -1;
		int modelColumnIndex = targetModel.findColumn(searchColumnCombo.getItemAt(col));
		modelColumnIndex = modelColumnIndex == -1 ? model.getSearchColumnIndex() : modelColumnIndex;

		for (int i = startRow; i < rowCount; i++) {
			if (matches(targetModel, i, modelColumnIndex, query, regex, caseSensitive)) {
				foundRow = i;
				break;
			}
		}
		return foundRow;
	}

	private static boolean matches(SearchTargetModel model, int row, int col, String query, boolean regex,
			boolean caseSensitive) {
		Object val = model.getValueAt(model.getRowForVisualRow(row), model.getColForVisualCol(col));
		if (val == null) {
			return false;
		}
		String str = val.toString();

		if (regex) {
			Pattern p = Pattern.compile(".*" + query + ".*", caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
			return p.matcher(str).matches();
		} else {
			String q = caseSensitive ? query : query.toLowerCase(Locale.ROOT);
			String s = caseSensitive ? str : str.toLowerCase(Locale.ROOT);
			return s.contains(q);
		}
	}

	private void initComponents(Consumer<SearchPaneMsg> dispatch) {
		mainPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref!]", "[]0"));

		initNavPane(dispatch);

		searchField = new FlatTextField();
		searchField.setLeadingIcon(new FlatSearchIcon());
		searchField.setShowClearButton(true);
		searchField.getDocument().addDocumentListener(SwingUtils.onDocumentChange(() -> {
			if (filterKey != null) {
				if (searchField.getText() == null || searchField.getText().isBlank()) {
					FilterStateManager.removeFilterState(filterKey);
				} else {
					FilterStateManager.saveFilterState(filterKey, new TableFilterState(searchField.getText(),
							model.getSearchColumnIndex(), model.isRegexActive(), model.isRespectCase()));
				}
			}
			dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ChangeSearchText(searchField.getText()));
		}));
		searchField.addActionListener(actionEvent -> dispatch.accept(new SearchPaneMsg.ActionMsg.SearchNext()));
		searchField.addKeyListener(this);

		JLabel searchColumnLabel = new JLabel(Configed.getResourceValue("SearchPane.search"));
		searchColumnCombo = new JComboBox<>();
		searchColumnCombo.addItemListener(searchColumnItemListener);
		searchColumnCombo.setVisible(!model.isNarrow());
		searchColumnLabel.setVisible(!model.isNarrow());

		JToggleButton extraOptionsBtn = new JToggleButton(Icons.getIntellijIcon("arrowLeft"));
		extraOptionsBtn.setSelectedIcon(Icons.getIntellijIcon("arrowDown"));
		extraOptionsBtn.setFocusable(false);
		extraOptionsBtn.setVisible(model.isNarrow());
		extraOptionsBtn.setToolTipText(Configed.getResourceValue("SearchPane.narrowLayout.extraOptions.toolTip"));
		extraOptionsBtn.addActionListener((ActionEvent e) -> {
			searchColumnCombo.setVisible(extraOptionsBtn.isSelected());
			searchColumnLabel.setVisible(extraOptionsBtn.isSelected());
		});

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		respectCaseBtn = new JToggleButton(Icons.getIntellijIcon("matchCase"));
		respectCaseBtn.addActionListener(
				e -> dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ToggleRespectCase(respectCaseBtn.isSelected())));

		regexBtn = new JToggleButton(Icons.getIntellijIcon("regex"));
		regexBtn.addActionListener(
				e -> dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ToggleRegex(regexBtn.isSelected())));

		filterMarkBtn = new JToggleButton(Icons.getIntellijIcon("funnelRegular"));
		filterMarkBtn.setSelectedIcon(Icons.getSelectedIntellijIcon("funnelRegular"));
		filterMarkBtn.setToolTipText(Configed.getResourceValue("SearchPane.filtermark.tooltip"));
		filterMarkBtn.setVisible(filterKey != null || model.isShowFilterMark());
		filterMarkBtn.addItemListener(
				e -> dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ToggleFilterMark(filterMarkBtn.isSelected())));
		filterMarkBtn.addActionListener(event -> dispatch.accept(new SearchPaneMsg.ActionMsg.TriggerFilterMark()));

		toolbar.add(respectCaseBtn);
		toolbar.add(regexBtn);
		toolbar.addSeparator();
		toolbar.add(filterMarkBtn);

		searchField.setTrailingComponent(toolbar);

		mainPanel.add(navPane, "split 3, hidemode 2");
		mainPanel.add(searchField, "growx");

		mainPanel.add(extraOptionsBtn, "wrap, hidemode 3");

		mainPanel.add(searchColumnLabel,
				"gapleft " + Globals.GAP_SIZE + ", gapy " + Globals.GAP_SIZE + ", split 2, hidemode 3");
		mainPanel.add(searchColumnCombo, "growx, hidemode 3, gapy " + Globals.GAP_SIZE + ", wrap");
	}

	private void initPopup() {
		popupSearchNext = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnext"));
		popupSearchNext.addActionListener(actionEvent -> dispatch(new SearchPaneMsg.ActionMsg.SearchNext()));
		popupSearchNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		popupNewSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnew"));
		popupNewSearch
				.addActionListener((ActionEvent actionEvent) -> dispatch(new SearchPaneMsg.ActionMsg.ResetSearch()));

		popupMarkAllAndFilter = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markAndFilter"));
		popupMarkAllAndFilter.addActionListener(
				(ActionEvent actionEvent) -> dispatch(new SearchPaneMsg.ActionMsg.MarkAllAndFilter()));
		popupMarkAllAndFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		popupEmptySearchfield = new JMenuItem(Configed.getResourceValue("SearchPane.popup.empty"));
		popupEmptySearchfield
				.addActionListener(actionEvent -> dispatch(new SearchPaneMsg.FieldChangeMsg.ChangeSearchText("")));
		popupEmptySearchfield.setEnabled(false);

		popupMarkAllAndFilter.setVisible(false);

		Logging.info(this, "buildMenuSearchfield");
		JPopupMenu searchMenu = new JPopupMenu();
		searchMenu.add(popupSearchNext);
		searchMenu.add(popupNewSearch);
		searchMenu.add(popupMarkAllAndFilter);
		searchMenu.add(popupEmptySearchfield);

		searchField.addMouseListener(new PopupMouseListener(searchMenu));
	}

	private void initNavPane(Consumer<SearchPaneMsg> dispatch) {
		navPane = new JPanel(new MigLayout("insets 0", "[][][][]", "[]"));
		navPane.setVisible(model.isShowNavPanel());

		JButton firstButton = createNavigationButton("playFirst", "NavigationPanel.firstEntryTooltip",
				(ActionEvent event) -> {
					if (associatedPanel != null) {
						associatedPanel.setCursorToFirstRow();
					} else {
						dispatch.accept(new SearchPaneMsg.ActionMsg.NavigateToRow(0));
					}
				});

		JButton previousButton = createNavigationButton("playBack", "NavigationPanel.previousEntryTooltip",
				(ActionEvent event) -> {
					if (associatedPanel != null) {
						associatedPanel.advanceCursor(-1);
					} else {
						dispatch.accept(new SearchPaneMsg.ActionMsg.NavigateToRow(advanceRow(-1)));
					}
				});

		JButton nextButton = createNavigationButton("playForward", "NavigationPanel.nextEntryTooltip",
				(ActionEvent event) -> {
					if (associatedPanel != null) {
						associatedPanel.advanceCursor(+1);
					} else {
						dispatch.accept(new SearchPaneMsg.ActionMsg.NavigateToRow(advanceRow(+1)));
					}
				});

		JButton lastButton = createNavigationButton("playLast", "NavigationPanel.lastEntryTooltip",
				(ActionEvent event) -> {
					if (associatedPanel != null) {
						associatedPanel.setCursorToLastRow();
					} else {
						dispatch.accept(new SearchPaneMsg.ActionMsg.NavigateToRow(targetModel.getRowCount() - 1));
					}
				});

		navPane.add(firstButton);
		navPane.add(previousButton);
		navPane.add(nextButton);
		navPane.add(lastButton);
	}

	private static JButton createNavigationButton(String iconName, String tooltipResourceKey, ActionListener action) {
		JButton navigationButton = new JButton(Icons.getIntellijIcon(iconName));
		navigationButton.setToolTipText(Configed.getResourceValue(tooltipResourceKey));
		navigationButton.setPreferredSize(new Dimension(30, 19));
		navigationButton.addActionListener(action);
		return navigationButton;
	}

	private int advanceRow(int n) {
		int newRow = model.getFoundRow() + n;
		if (newRow >= targetModel.getRowCount()) {
			newRow = model.getFoundRow();
		}
		return newRow;
	}

	private void updateViewFromModel(SearchPaneModel model) {
		if (!searchField.getText().equals(model.getSearchText())) {
			searchField.setText(model.getSearchText());
		}

		if (targetModel != null) {
			searchColumnCombo.removeItemListener(searchColumnItemListener);
			computateSearchColumnCombo();

			if (model.getSearchColumnIndex() >= 0 && model.getSearchColumnIndex() < searchColumnCombo.getItemCount()) {
				searchColumnCombo.setSelectedIndex(model.getSearchColumnIndex());
			}
			searchColumnCombo.addItemListener(searchColumnItemListener);
		}

		respectCaseBtn.setSelected(model.isRespectCase());

		regexBtn.setSelected(model.isRegexActive());

		filterMarkBtn.setVisible(model.getFilterKey() != null || model.isShowFilterMark());
		filterMarkBtn.setSelected(model.isFiltered());

		popupMarkAllAndFilter.setVisible(model.getFilterKey() != null || model.isShowFilterMark());
		popupMarkAllAndFilter.setEnabled(!model.getSearchText().isEmpty());
		popupNewSearch.setEnabled(model.isFiltered());
		popupEmptySearchfield.setEnabled(!model.getSearchText().isEmpty());
		popupSearchNext.setEnabled(!model.getSearchText().isEmpty());

		navPane.setVisible(model.isShowNavPanel());
	}

	private void computateSearchColumnCombo() {
		searchColumnCombo.removeAllItems();
		searchColumnCombo.addItem(Configed.getResourceValue("SearchPane.search.allfields"));
		for (int i = 0; i < targetModel.getColumnCount(); i++) {
			if (model.getSearchColumns() == null || model.getSearchColumns().contains(i)) {
				searchColumnCombo.addItem(targetModel.getColumnName(i));
			}
		}
	}

	public boolean isFilterMode() {
		return model.isFiltered();
	}

	public boolean isFiltering() {
		return model.getFilterKey() != null || model.isShowFilterMark();
	}

	public void setTargetModel(SearchTargetModel targetModel) {
		this.targetModel = targetModel;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (model.getSearchText().isBlank()) {
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_F8) {
			if (isFiltering() && !filterMarkBtn.isSelected()) {
				dispatch(new SearchPaneMsg.ActionMsg.SelectAll());
			}
		} else if (e.getKeyCode() == KeyEvent.VK_F3) {
			dispatch(new SearchPaneMsg.ActionMsg.SearchNext());
		} else {
			// We want to do nothing on other keys
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Not needed
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Not needed
	}
}
