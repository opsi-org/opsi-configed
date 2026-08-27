/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.gui.features.searchpane.SearchPaneComponent;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModelFromJList;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModelFromJList.FilterContext;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ListSelectionDialog {
	public static final Dimension DEFAULT_MULTI_LINE_EDITOR_SIZE = new Dimension(400, 200);

	protected ListSelectionList listSelectionList;
	protected SearchPaneComponent searchPane;

	private FlatTextField editingTextField;
	private JButton addMultiLineValueButton;

	protected JOptionPane jOptionPane;
	protected JDialog dialog;

	private SearchTargetModelFromJList searchTargetModel;

	public ListSelectionDialog(Component owner, String title) {
		this(owner, title, false);
	}

	public ListSelectionDialog(Component owner, String title, boolean editable) {
		JPanel panel = createPanel(editable);
		jOptionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		DialogUtils.enableDialogResizing(jOptionPane);

		dialog = jOptionPane.createDialog(owner, title);
		dialog.pack();
		dialog.setLocationRelativeTo(owner != null ? owner : ConfigedMain.getMainFrame());
	}

	private JPanel createPanel(boolean editable) {
		Logging.info(this, "createCenterPanel");

		listSelectionList = new ListSelectionList();
		listSelectionList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && editingTextField != null
						&& listSelectionList.getSelectedValuesList().size() == 1) {
					if (listSelectionList.getSelectedValue().contains("\n")) {
						addMultilineItem(listSelectionList.getSelectedValue(), true);
					} else {
						editingTextField.setText(listSelectionList.getSelectedValue());
					}
				}
			}
		});

		JScrollPane listScrollPane = new JScrollPane(listSelectionList);
		listScrollPane.setPreferredSize(new Dimension(200, 200));

		searchTargetModel = new SearchTargetModelFromJList(listSelectionList, new ArrayList<>(), new ArrayList<>());
		searchPane = SearchPaneComponent.builder().targetModel(searchTargetModel).isNarrow(true)
				.component(listSelectionList).build();

		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow]",
				"[pref!]" + Globals.GAP_SIZE + "[grow]" + Globals.GAP_SIZE + "[pref!]"));
		panel.add(searchPane.initUI(), "growx");
		panel.add(listScrollPane, "grow");

		if (editable) {
			createEditableOptions();
			panel.add(editingTextField, "split 2, hidemode 3, growx, gapy " + Globals.GAP_SIZE);
			addMultiLineValueButton = new JButton();
			addMultiLineValueButton.setIcon(Icons.getIntellijIcon("openNewTab"));
			addMultiLineValueButton.addActionListener(actionEvent -> addMultilineItem(null, false));
			addMultiLineValueButton.setToolTipText(Configed.getResourceValue("ListSelectionDialog.addMultiLineValue"));
			panel.add(addMultiLineValueButton, "hidemode 3, gapy " + Globals.GAP_SIZE + ", wrap");
		}

		return panel;
	}

	private void createEditableOptions() {
		editingTextField = new FlatTextField();
		JButton addValueButton = new JButton(Icons.getIntellijIcon("add"));
		addValueButton.addActionListener(actionEvent -> addItem(editingTextField.getText()));

		editingTextField.setTrailingComponent(addValueButton);
		editingTextField.setShowClearButton(true);
		editingTextField.addActionListener(actionEvent -> addItem(editingTextField.getText()));
	}

	private void addMultilineItem(String initialText, boolean edit) {
		JTextArea textArea = new JTextArea(initialText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(DEFAULT_MULTI_LINE_EDITOR_SIZE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		DialogUtils.enableDialogResizing(optionPane);

		JDialog multiLineItemDialog = optionPane.createDialog(dialog,
				edit ? Configed.getResourceValue("ListSelectionDialog.editMultiLineValue")
						: Configed.getResourceValue("ListSelectionDialog.addMultiLineValue"));
		multiLineItemDialog.pack();
		multiLineItemDialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
			if (edit) {
				removeItem(initialText);
			}

			addItem(textArea.getText());
		}
	}

	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	public void show() {
		show(dialog.getParent());
	}

	public void show(Container parent) {
		dialog.setLocationRelativeTo(parent);
		dialog.pack();
		// Workaround: Schedule two nested invokeLater() calls to ensure the search field reliably gains focus.
		// This accounts for focus-stealing components (e.g., dialog activation or defualt buttons) that may
		// override the focus requests. Without this delay, the search field may not receive focus when the
		// dialog is shown.
		SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> searchPane.requestSearchFieldFocus()));
		dialog.setVisible(true);
	}

	public boolean wasAccepted() {
		Object result = jOptionPane.getValue();
		return result != null && result.equals(JOptionPane.OK_OPTION);
	}

	public void setListData(List<String> v) {
		listSelectionList.setListData(v.toArray(String[]::new));
		searchTargetModel = new SearchTargetModelFromJList(listSelectionList, v, v);
		searchPane.setTargetModel(searchTargetModel);
	}

	public void setEditable(boolean editable) {
		editingTextField.setText(null);
		editingTextField.setVisible(editable);
		addMultiLineValueButton.setVisible(editable);
	}

	public void setModel(ListModel<String> model) {
		listSelectionList.setModel(model);

		// Without this the search won't work
		updateSearchTargetModel(model);
	}

	private void updateSearchTargetModel(ListModel<String> model) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			String element = model.getElementAt(i);
			list.add(element);
		}

		FilterContext filterContext = searchTargetModel.getFilterContext();
		searchTargetModel = new SearchTargetModelFromJList(listSelectionList, list, list, filterContext);
		searchPane.setTargetModel(searchTargetModel);
	}

	public String getSelectedValue() {
		return listSelectionList.getSelectedValue();
	}

	public List<String> getSelectedValues() {
		return listSelectionList.getSelectedValuesList();
	}

	public List<String> getValues() {
		List<String> values = new ArrayList<>();

		for (int i = 0; i < listSelectionList.getModel().getSize(); i++) {
			values.add(listSelectionList.getModel().getElementAt(i));
		}

		return values;
	}

	public void setPreviousSelectionValues(Collection<String> previouslySelectedValues) {
		listSelectionList.setPreviousSelectionValues(previouslySelectedValues);
	}

	public void setSingleSelection() {
		listSelectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setMultiSelection() {
		listSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void setAlwaysOnTop(boolean value) {
		dialog.setAlwaysOnTop(value);
	}

	public void setNonDeselectableValues(Collection<String> nonDeselectableValues) {
		listSelectionList.setNonDeselectableValues(nonDeselectableValues);
	}

	public void addPopupMenu(JPopupMenu popupMenu) {
		listSelectionList.addMouseListener(new PopupMouseListener(popupMenu));
	}

	private void addItem(String element) {
		listSelectionList.addItem(element);

		// Without this the search won't work
		updateSearchTargetModel(listSelectionList.getOriginalModel());
		listSelectionList.updateSelection();
	}

	public void removeItems(Iterable<String> elements) {
		elements.forEach(element -> listSelectionList.removeItem(element));

		// Without this the search won't work
		updateSearchTargetModel(listSelectionList.getOriginalModel());
	}

	private void removeItem(String element) {
		removeItems(List.of(element));
	}
}
