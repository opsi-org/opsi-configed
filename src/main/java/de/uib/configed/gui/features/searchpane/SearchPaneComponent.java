/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.features.searchpane.view.SearchPaneView;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModel;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.share.logging.Logging;
import lombok.Builder;

@SuppressWarnings("java:S1200")
public class SearchPaneComponent extends AbstractTeaComponent<SearchPaneModel, SearchPaneMsg, SearchPaneEffect>
		implements KeyListener {
	private SearchTargetModel targetModel;
	private FilterKey filterKey;
	private PanelGenEdit associatedPanel;
	private boolean isNarrow;
	private boolean showNavPanel;
	private boolean enableFilterBySelection;
	private JComponent component;

	private SideEffectStrategy sideEffectStrategy;
	private SearchPaneView searchPane;

	@Builder
	public SearchPaneComponent(SearchTargetModel targetModel, FilterKey filterKey, PanelGenEdit associatedPanel,
			boolean isNarrow, boolean showNavPanel, boolean enableFilterBySelection, JComponent component,
			SearchPaneView searchPane) {
		this.targetModel = targetModel;
		this.filterKey = filterKey;
		this.associatedPanel = associatedPanel;
		this.isNarrow = isNarrow;
		this.showNavPanel = showNavPanel;
		this.enableFilterBySelection = enableFilterBySelection;
		this.component = component;
		registerWithComponent();
	}

	public interface SideEffectStrategy {
		/**
		 * Given an effect, return the side effect's action to execute. Return
		 * null if no side effect's action is needed.
		 */
		Runnable getActionFor(SearchPaneEffect effect);
	}

	public void setSideEffectStrategy(SideEffectStrategy sideEffectStrategy) {
		this.sideEffectStrategy = sideEffectStrategy;
	}

	@Override
	protected SearchPaneModel initModel() {
		return SearchPaneModel.builder().filterKey(filterKey).isNarrow(isNarrow).showNavPanel(showNavPanel)
				.enableFilterBySelection(enableFilterBySelection).build();
	}

	@Override
	protected UpdateResult<SearchPaneModel, SearchPaneEffect> updateModel(SearchPaneMsg msg, SearchPaneModel model) {
		return SearchPaneUpdate.update(msg, model);
	}

	@Override
	protected JComponent renderView(SearchPaneModel model, Consumer<SearchPaneMsg> dispatch) {
		Logging.info(this, "Rendering TableSearchPane for model: " + model);

		searchPane = new SearchPaneView(model, targetModel, this, associatedPanel);
		JPanel mainPanel = searchPane.buildPanel(dispatch);
		searchPane.updateViewFromModel(model);

		return mainPanel;
	}

	@Override
	protected void refreshView() {
		if (searchPane != null) {
			searchPane.updateViewFromModel(model);
			searchPane.refresh();
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
		int modelColumnIndex = targetModel.findColumn(searchPane.getColumnAt(col));
		targetModel.applyFilter(query, modelColumnIndex, regex, caseSensitive);
	}

	private void onMarkSelectedAndFilter(boolean isFiltered) {
		if (isFiltered) {
			targetModel.setFiltered(isFiltered);
		} else {
			int[] unfilteredSelection = targetModel.getUnfilteredSelection();

			targetModel.setFiltered(false);

			if (unfilteredSelection.length != 0) {
				targetModel.setSelection(unfilteredSelection);
			}
		}
	}

	private void onMarkAllAndFilter() {
		searchPane.selectFilterMarkBtn(false);
		onSelectAll();
		searchPane.selectFilterMarkBtn(true);
	}

	private void onSearchNextRow() {
		int currentRow = model.getFoundRow() != targetModel.getSelectedRow() ? targetModel.getSelectedRow()
				: model.getFoundRow();
		int foundRow = findNextRow(model.getSearchText(), model.getSearchColumnIndex(), model.isRegexActive(),
				model.isRespectCase(), currentRow + 1);
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
		int modelColumnIndex = targetModel.findColumn(searchPane.getColumnAt(col));
		modelColumnIndex = modelColumnIndex == -1 ? model.getSearchColumnIndex() : modelColumnIndex;

		SearchCriteriaEngine engine = new SearchCriteriaEngine();
		Pattern pattern = engine.getPattern(regex, caseSensitive, query);
		for (int i = startRow; i < rowCount; i++) {
			Object val = targetModel.getValueAt(targetModel.getRowForVisualRow(i),
					targetModel.getColForVisualCol(modelColumnIndex));
			if (engine.matchCell(val, query, pattern, regex, caseSensitive)) {
				foundRow = i;
				break;
			}
		}
		return foundRow;
	}

	public boolean isFilteredBySelection() {
		return model.isFilteredBySelection();
	}

	public boolean isFilteringBySelectionEnabled() {
		return model.isEnableFilterBySelection();
	}

	public void setTargetModel(SearchTargetModel targetModel) {
		this.targetModel = targetModel;
	}

	private void registerWithComponent() {
		component.addKeyListener(this);
		SwingUtils.addKeyBindingToJComponent(component,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
				this::requestSearchFieldFocus, JComponent.WHEN_FOCUSED);
	}

	public void requestSearchFieldFocus() {
		if (searchPane != null) {
			searchPane.requestSearchFieldFocus();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (model.getSearchText().isBlank()) {
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_F8) {
			dispatch(new SearchPaneMsg.ActionMsg.MarkAllAndFilter());
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
