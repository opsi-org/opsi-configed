/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.gui.share.table.gui.SearchTargetModel;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromJList;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class ListSelectionDialog {
	public static final Dimension DEFAULT_MULTI_LINE_EDITOR_SIZE = new Dimension(400, 200);

	protected ListSelectionList listSelectionList;
	private JPopupMenu popupMenu;
	protected TableSearchPane searchPane;

	private FlatTextField editingTextField;

	protected JOptionPane jOptionPane;
	protected JDialog dialog;

	public ListSelectionDialog(Component owner, String title) {
		this(owner, title, false);
	}

	public ListSelectionDialog(Component owner, String title, boolean editable) {
		JPanel panel = createPanel(editable);
		jOptionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		Utils.enableDialogResizing(jOptionPane);

		dialog = jOptionPane.createDialog(owner, title);
		dialog.pack();
		dialog.setLocationRelativeTo(owner != null ? owner : ConfigedMain.getMainFrame());
	}

	private JPanel createPanel(boolean editable) {
		Logging.info(this, "createCenterPanel");

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

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

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(listSelectionList, new ArrayList<>(),
				new ArrayList<>());
		searchPane = new TableSearchPane(searchTargetModel);
		searchPane.setNarrow(true);

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		SequentialGroup verticalGroup = layout.createSequentialGroup();

		// Add search pane
		horizontalGroup.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);
		verticalGroup.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE);

		// add list
		horizontalGroup.addComponent(listScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);

		verticalGroup.addGap(Globals.GAP_SIZE);
		verticalGroup.addComponent(listScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);

		// Add additional component if not null
		Logging.info(this, "editable: ", editable);
		if (editable) {
			createEditableOptions(horizontalGroup, verticalGroup);
		}

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);

		return panel;
	}

	private void createEditableOptions(ParallelGroup horizontalGroup, SequentialGroup verticalGroup) {
		editingTextField = new FlatTextField();
		JButton addValueButton = new JButton(Icons.getIntellijIcon("add"));
		addValueButton.addActionListener(actionEvent -> addItem(editingTextField.getText()));

		editingTextField.setTrailingComponent(addValueButton);
		editingTextField.setShowClearButton(true);
		editingTextField.addActionListener(actionEvent -> addItem(editingTextField.getText()));

		popupMenu = new JPopupMenu();

		JMenuItem addItemMenu = new JMenuItem(Configed.getResourceValue("ListSelectionDialog.addMultiLineValue"));
		Icons.addIntellijIconToMenuItem(addItemMenu, "add");
		addItemMenu.addActionListener(actionEvent -> addMultilineItem(null, false));

		JMenuItem editItemMenu = new JMenuItem(Configed.getResourceValue("ListSelectionDialog.editMultiLineValue"));
		Icons.addIntellijIconToMenuItem(editItemMenu, "edit");
		editItemMenu.addActionListener(actionEvent -> addMultilineItem(listSelectionList.getSelectedValue(), true));

		popupMenu.add(addItemMenu);
		popupMenu.add(editItemMenu);

		editingTextField.setComponentPopupMenu(popupMenu);
		listSelectionList.setComponentPopupMenu(popupMenu);

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				editItemMenu.setEnabled(listSelectionList.getSelectedValuesList().size() == 1);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// Not needed here
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// Handle popup menu cancellation
			}
		});

		horizontalGroup.addComponent(editingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				Short.MAX_VALUE);
		verticalGroup.addGap(Globals.GAP_SIZE);
		verticalGroup.addComponent(editingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE);
	}

	private void addMultilineItem(String initialText, boolean edit) {
		JTextArea textArea = new JTextArea(initialText);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(DEFAULT_MULTI_LINE_EDITOR_SIZE);

		JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		Utils.enableDialogResizing(optionPane);

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
		SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> searchPane.requestFocus()));
		dialog.setVisible(true);
	}

	public boolean wasAccepted() {
		Object result = jOptionPane.getValue();
		return result != null && result.equals(JOptionPane.OK_OPTION);
	}

	public void setListData(List<String> v) {
		listSelectionList.setListData(v.toArray(String[]::new));
		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(listSelectionList, v, v);
		searchPane.setTargetModel(searchTargetModel);
	}

	public void setEditable(boolean editable) {
		editingTextField.setText(null);
		editingTextField.setVisible(editable);

		// We need to remove the popup menu from the list if not editable,
		// because the popup menu is for adding values
		listSelectionList.setComponentPopupMenu(editable ? popupMenu : null);
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

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(listSelectionList, list, list);
		searchPane.setTargetModel(searchTargetModel);
	}

	public String getSelectedValue() {
		return listSelectionList.getSelectedValue();
	}

	public List<String> getSelectedValues() {
		return listSelectionList.getSelectedValuesList();
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

	private void addItem(String element) {
		listSelectionList.addItem(element);

		// Without this the search won't work
		updateSearchTargetModel(listSelectionList.getModel());
	}

	private void removeItem(String element) {
		listSelectionList.removeItem(element);

		// Without this the search won't work
		updateSearchTargetModel(listSelectionList.getModel());
	}
}
