/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.searchpane.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.searchpane.SearchPaneModel;
import de.uib.configed.gui.features.searchpane.SearchPaneMsg;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.FilterStateManager;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.TableFilterState;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("java:S1200")
public class SearchPaneView {
	private SearchTargetModel targetModel;
	private FilterKey filterKey;
	private PanelGenEdit associatedPanel;

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

	private SearchPaneModel model;
	private KeyListener keyListener;

	private ItemListener searchColumnItemListener;

	public SearchPaneView(SearchPaneModel model, SearchTargetModel targetModel, KeyListener keyListener,
			PanelGenEdit associatedPanel) {
		this.model = model;
		this.targetModel = targetModel;
		this.keyListener = keyListener;
		this.associatedPanel = associatedPanel;
	}

	public JPanel buildPanel(Consumer<SearchPaneMsg> dispatch) {
		initComponents(dispatch);
		initPopup(dispatch);

		return mainPanel;
	}

	private void initComponents(Consumer<SearchPaneMsg> dispatch) {
		mainPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][pref!]", "[]0"));

		initNavPane(dispatch);

		searchField = new FlatTextField();
		searchField.setLeadingIcon(new FlatSearchIcon());
		searchField.setShowClearButton(true);
		searchField.getDocument().addDocumentListener(SwingUtils.onDocumentChange(() -> {
			if (filterKey != null) {
				handleFilterState();
			}
			dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ChangeSearchText(searchField.getText()));
		}));
		searchField.addActionListener(actionEvent -> dispatch.accept(new SearchPaneMsg.ActionMsg.SearchNext()));
		searchField.addKeyListener(keyListener);

		searchColumnItemListener = (ItemEvent e) -> {
			if (e.getStateChange() == 1) {
				dispatch.accept(
						new SearchPaneMsg.FieldChangeMsg.ChangeSearchColumnIndex(searchColumnCombo.getSelectedIndex()));
			}
		};

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
		filterMarkBtn.setVisible(filterKey != null || model.isEnableFilterBySelection());
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

	private void handleFilterState() {
		if (searchField.getText() == null || searchField.getText().isBlank()) {
			FilterStateManager.removeFilterState(filterKey);
		} else {
			FilterStateManager.saveFilterState(filterKey, new TableFilterState(searchField.getText(),
					model.getSearchColumnIndex(), model.isRegexActive(), model.isRespectCase()));
		}
	}

	private void initPopup(Consumer<SearchPaneMsg> dispatch) {
		popupSearchNext = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnext"));
		popupSearchNext.addActionListener(actionEvent -> dispatch.accept(new SearchPaneMsg.ActionMsg.SearchNext()));
		popupSearchNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

		popupNewSearch = new JMenuItem(Configed.getResourceValue("SearchPane.popup.searchnew"));
		popupNewSearch.addActionListener(
				(ActionEvent actionEvent) -> dispatch.accept(new SearchPaneMsg.ActionMsg.ResetSearch()));

		popupMarkAllAndFilter = new JMenuItem(Configed.getResourceValue("SearchPane.popup.markAndFilter"));
		popupMarkAllAndFilter.addActionListener(
				(ActionEvent actionEvent) -> dispatch.accept(new SearchPaneMsg.ActionMsg.MarkAllAndFilter()));
		popupMarkAllAndFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

		popupEmptySearchfield = new JMenuItem(Configed.getResourceValue("SearchPane.popup.empty"));
		popupEmptySearchfield.addActionListener(
				actionEvent -> dispatch.accept(new SearchPaneMsg.FieldChangeMsg.ChangeSearchText("")));
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
		int newRow = model.getNavigatedToRow() + n;
		if (newRow >= targetModel.getRowCount()) {
			newRow = model.getNavigatedToRow();
		}
		return newRow;
	}

	public void updateViewFromModel(SearchPaneModel model) {
		this.model = model;

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

		filterMarkBtn.setVisible(model.isEnableFilterBySelection());
		filterMarkBtn.setSelected(model.isFilteredBySelection());

		popupMarkAllAndFilter.setVisible(model.isEnableFilterBySelection());
		popupMarkAllAndFilter.setEnabled(!model.getSearchText().isEmpty() && !model.isFilteredBySelection());
		popupNewSearch.setEnabled(model.isFilteredBySelection());
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

	public void refresh() {
		if (mainPanel == null) {
			return;
		}

		mainPanel.revalidate();
		mainPanel.repaint();
	}

	public void selectFilterMarkBtn(boolean select) {
		filterMarkBtn.setSelected(select);
	}

	public String getColumnAt(int col) {
		return searchColumnCombo.getItemAt(col);
	}

	public void requestSearchFieldFocus() {
		searchField.requestFocusInWindow();
	}
}
