/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Globals;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.SearchTargetModel;
import de.uib.utils.table.gui.SearchTargetModelFromJList;
import de.uib.utils.table.gui.TableSearchPane;

public class ListSelectionDialog {
	private JList<String> jList;
	private TableSearchPane searchPane;

	private FlatTextField editingTextField;

	private JOptionPane jOptionPane;
	private JDialog dialog;

	public ListSelectionDialog(Component owner, String title) {
		this(owner, title, false);
	}

	public ListSelectionDialog(Component owner, String title, boolean editable) {
		JPanel panel = createPanel(editable);
		jOptionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

		dialog = jOptionPane.createDialog(owner, title);
	}

	private JPanel createPanel(boolean editable) {
		Logging.info(this, "createCenterPanel");

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		jList = new JList<>();
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane listScrollPane = new JScrollPane(jList);
		listScrollPane.setPreferredSize(new Dimension(200, 200));

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(jList, new ArrayList<>(),
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
				GroupLayout.PREFERRED_SIZE);

		// Add additional component if not null
		if (editable) {
			editingTextField = new FlatTextField();
			JButton addValueButton = new JButton(Icons.getIntellijIcon("add"));
			addValueButton.addActionListener(actionEvent -> addItem(editingTextField.getText()));

			editingTextField.setTrailingComponent(addValueButton);
			editingTextField.setShowClearButton(true);
			editingTextField.addActionListener(actionEvent -> addItem(editingTextField.getText()));

			horizontalGroup.addComponent(editingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					Short.MAX_VALUE);
			verticalGroup.addGap(Globals.GAP_SIZE);
			verticalGroup.addComponent(editingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
					GroupLayout.PREFERRED_SIZE);
		}

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);

		return panel;
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
		dialog.setVisible(true);
	}

	public boolean wasAccepted() {
		Object result = jOptionPane.getValue();
		return result != null && result.equals(JOptionPane.OK_OPTION);
	}

	public void setListData(List<String> v) {
		jList.setListData(v.toArray(String[]::new));
		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(jList, v, v);
		searchPane.setTargetModel(searchTargetModel);
	}

	public void setEditable(boolean editable) {
		editingTextField.setText(null);
		editingTextField.setVisible(editable);
	}

	public void setModel(ListModel<String> model) {
		jList.setModel(model);
	}

	public void addItem(String element) {
		DefaultListModel<String> model = (DefaultListModel<String>) jList.getModel();
		if (!model.contains(element)) {
			model.addElement(element);
			jList.addSelectionInterval(model.size() - 1, model.size() - 1);
			jList.ensureIndexIsVisible(jList.getMaxSelectionIndex());
		}
	}

	public String getSelectedValue() {
		return jList.getSelectedValue();
	}

	public List<String> getSelectedValues() {
		return jList.getSelectedValuesList();
	}

	public void setPreviousSelectionValues(Collection<String> previouslySelectedValues) {
		int[] indices = getPreviouslySelectedIndicesFromValues(previouslySelectedValues);
		jList.setSelectedIndices(indices);

		jList.ensureIndexIsVisible(jList.getSelectedIndex());
	}

	private int[] getPreviouslySelectedIndicesFromValues(Collection<String> previouslySelectedValues) {
		int[] indices = new int[previouslySelectedValues.size()];
		int n = 0;
		for (int i = 0; i < jList.getModel().getSize(); i++) {
			if (previouslySelectedValues.contains(jList.getModel().getElementAt(i))) {
				indices[n] = i;
				n++;
			}
		}
		return indices;
	}

	public void setSingleSelection() {
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setMultiSelection() {
		jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
}
