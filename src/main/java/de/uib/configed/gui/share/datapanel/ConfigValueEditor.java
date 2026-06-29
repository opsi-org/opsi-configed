/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.searchpane.SearchPaneComponent;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModelFromTable;
import de.uib.configed.gui.share.DialogUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.PropertiesCellEditorAndRenderer;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("java:S1200")
public class ConfigValueEditor extends JPanel {
	private DefaultTableModel valuesTableModel;
	private JTable valuesTable;
	private FlatTextField addValueField;
	private TableColumn defaultColumn;

	private boolean isMultiValueMode;

	public ConfigValueEditor() {
		init();
	}

	public void setMultiValueMode(boolean multi) {
		if (this.isMultiValueMode == multi) {
			return;
		}

		this.isMultiValueMode = multi;

		defaultColumn.setHeaderValue(getDefaultColumnHeader());
		defaultColumn.setCellEditor(new DefaultToggleEditor(valuesTable, valuesTableModel, isMultiValueMode));

		if (!multi) {
			enforceSingleSelection();
		}

		valuesTable.getTableHeader().repaint();

		valuesTable.revalidate();
		valuesTable.repaint();
	}

	private void init() {
		valuesTableModel = new DefaultTableModel(new String[] {
				Configed.getResourceValue("ConfigValueEditor.table.valueColumn"), getDefaultColumnHeader() }, 0) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnIndex == 1 ? Boolean.class : String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};

		valuesTable = new JTable(valuesTableModel);
		valuesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		valuesTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				String displayText = PropertiesCellEditorAndRenderer.formatValue(value);
				JLabel label = new JLabel(displayText);
				label.setOpaque(true);
				label.setToolTipText(displayText);
				ColorTableCellRenderer.colorize(label, isSelected, row, col);
				return label;
			}
		});

		defaultColumn = valuesTable.getColumnModel().getColumn(1);
		defaultColumn.setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				JToggleButton tb = isMultiValueMode ? new JCheckBox() : new JRadioButton();
				tb.setSelected(value != null && (Boolean) value);
				if (!isMultiValueMode) {
					tb.setToolTipText(Configed.getResourceValue("ConfigValueEditor.table.defaultColumn.tooltip"));
				}
				ColorTableCellRenderer.colorize(tb, isSelected, row, col);
				return tb;
			}
		});
		defaultColumn.setCellEditor(new DefaultToggleEditor(valuesTable, valuesTableModel, isMultiValueMode));

		SearchTargetModelFromTable searchTargetModel = new SearchTargetModelFromTable(valuesTable);
		SearchPaneComponent searchPane = new SearchPaneComponent(searchTargetModel, null, true, false, false);

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new MigLayout("insets 0, fill",
				"[grow]" + Globals.MIN_GAP_SIZE + "[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]", "[]"));

		addValueField = new FlatTextField();
		addValueField.setShowClearButton(true);

		JButton addBtn = new JButton(Icons.getIntellijIcon("add"));
		addBtn.addActionListener(e -> addSingleValue(addValueField.getText()));
		addValueField.setTrailingComponent(addBtn);

		JButton addMultiLineButton = new JButton(Icons.getIntellijIcon("openNewTab"));
		addMultiLineButton.setToolTipText(Configed.getResourceValue("ListSelectionDialog.addMultiLineValue"));
		addMultiLineButton.addActionListener(e -> addMultilineValue(null));

		JButton removeButton = new JButton(Icons.getIntellijIcon("remove"));
		removeButton.setToolTipText(Configed.getResourceValue("CreateConfigDialog.removeValues"));
		removeButton.addActionListener(e -> removeSelectedRows());

		controlsPanel.add(addValueField, "growx");
		controlsPanel.add(addMultiLineButton);
		controlsPanel.add(removeButton);

		setLayout(new MigLayout("insets 0, fill, wrap 1", "", "[]0"));
		add(searchPane.initUI(), "growx, gapbottom " + Globals.GAP_SIZE);

		JScrollPane scrollPane = new JScrollPane(valuesTable);
		scrollPane.setPreferredSize(new Dimension(300, 110));
		add(scrollPane, "grow, gapbottom " + Globals.GAP_SIZE);
		add(controlsPanel, "growx");
	}

	private String getDefaultColumnHeader() {
		return isMultiValueMode ? Configed.getResourceValue("ConfigValueEditor.table.defaultColumn")
				: Configed.getResourceValue("ConfigValueEditor.table.defaultColumn.selectOnlyOne");
	}

	private void enforceSingleSelection() {
		int lastChecked = -1;
		for (int i = 0; i < valuesTableModel.getRowCount(); i++) {
			if (Boolean.TRUE.equals(valuesTableModel.getValueAt(i, 1))) {
				lastChecked = i;
			}
		}

		if (lastChecked != -1) {
			for (int i = 0; i < valuesTableModel.getRowCount(); i++) {
				if (i != lastChecked) {
					valuesTableModel.setValueAt(false, i, 1);
				}
			}
		}
	}

	private void addSingleValue(String value) {
		if (value == null || value.isBlank()) {
			return;
		}

		for (int i = 0; i < valuesTableModel.getRowCount(); i++) {
			if (value.equals(valuesTableModel.getValueAt(i, 0))) {
				return;
			}
		}

		valuesTableModel.addRow(new Object[] { value, false });
		addValueField.setText("");
		valuesTable.scrollRectToVisible(valuesTable.getCellRect(valuesTableModel.getRowCount() - 1, 0, true));
	}

	private void addMultilineValue(String initialText) {
		JTextArea textArea = new JTextArea(initialText);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		DialogUtils.enableDialogResizing(optionPane);
		JDialog multiLineItemDialog = optionPane.createDialog(this,
				Configed.getResourceValue("ListSelectionDialog.addMultiLineValue"));
		multiLineItemDialog.pack();
		multiLineItemDialog.setVisible(true);

		if (optionPane.getValue() != null && optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
			addSingleValue(textArea.getText());
		}
	}

	private void removeSelectedRows() {
		int[] rows = valuesTable.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			valuesTableModel.removeRow(rows[i]);
		}
	}

	public List<String> getDefaults() {
		List<String> defaults = new ArrayList<>();
		for (int i = 0; i < valuesTableModel.getRowCount(); i++) {
			if (Boolean.TRUE.equals(valuesTableModel.getValueAt(i, 1))) {
				defaults.add((String) valuesTableModel.getValueAt(i, 0));
			}
		}
		return defaults;
	}

	public List<String> getAllValues() {
		List<String> all = new ArrayList<>();
		for (int i = 0; i < valuesTableModel.getRowCount(); i++) {
			all.add((String) valuesTableModel.getValueAt(i, 0));
		}
		return all;
	}

	@SuppressWarnings("java:S2972")
	private static class DefaultToggleEditor extends AbstractCellEditor implements TableCellEditor {
		private final JToggleButton toggleButton;
		private final DefaultTableModel model;

		public DefaultToggleEditor(JTable table, DefaultTableModel model, boolean isMultiValueMode) {
			this.model = model;
			this.toggleButton = isMultiValueMode ? new JCheckBox() : new JRadioButton();

			toggleButton.addActionListener((ActionEvent e) -> {
				int row = table.convertRowIndexToModel(table.getEditingRow());
				if (row == -1) {
					return;
				}

				if (!isMultiValueMode && toggleButton.isSelected()) {
					uncheckAllExcept(row);
				}

				fireEditingStopped();
			});
		}

		private void uncheckAllExcept(int row) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (i != row) {
					model.setValueAt(false, i, 1);
				}
			}
		}

		@Override
		public Object getCellEditorValue() {
			return toggleButton.isSelected();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			toggleButton.setSelected(value != null && (Boolean) value);
			toggleButton.setEnabled(true);
			return toggleButton;
		}
	}
}
