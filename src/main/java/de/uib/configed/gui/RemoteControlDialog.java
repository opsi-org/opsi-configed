/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class RemoteControlDialog implements DocumentListener {
	private static final int START_HEIGHT_LOGGING_AREA = 80;
	private static final int START_HEIGHT_FRAME = 400;
	private static final int START_WIDTH_FRAME = 500;
	private Map<String, String> meanings;
	private Map<String, Boolean> editableFields;
	private String selText;

	private ConfigedMain configedMain;

	private JList<String> visibleList;
	private JTextField extraField;

	private JTextArea loggingArea;

	JSplitPane splitPane;

	private JOptionPane optionPane;
	private JDialog dialog;

	private String selValue;

	public RemoteControlDialog(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		JComponent panel = initComponents();

		JButton buttonExecute = new JButton(Configed.getResourceValue("buttonExecute"));
		buttonExecute.addActionListener(event -> commit());

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION, null,
				new Object[] { buttonExecute, Configed.getResourceValue("buttonClose") });

		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuRemoteControl"));
		dialog.setModalityType(ModalityType.MODELESS);
	}

	public void setListModel(ListModel<String> model) {
		visibleList.setModel(model);
	}

	public void setCellRenderer(ListCellRenderer<Object> render) {
		visibleList.setCellRenderer(render);
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	public void setMeanings(Map<String, String> meanings) {
		this.meanings = meanings;
	}

	public void setEditableFields(Map<String, Boolean> editable) {
		this.editableFields = editable;
	}

	protected String getValue(String key) {
		return meanings.get(key);
	}

	private JComponent initComponents() {
		visibleList = new JList<>();
		visibleList
				.addListSelectionListener(listSelectionEvent -> valueChanged(listSelectionEvent.getValueIsAdjusting()));
		visibleList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				checkSelected();

				if (e.getClickCount() > 1) {
					commit();
				}
			}
		});

		JScrollPane visibleListScrollPane = new JScrollPane(visibleList);

		extraField = new JTextField();
		extraField.addActionListener(event -> commit());
		extraField.getDocument().addDocumentListener(this);

		loggingArea = new JTextArea();
		loggingArea.setEditable(false);
		loggingArea.setLineWrap(true);

		JPanel topPanel = new JPanel(
				new MigLayout("insets 0, fill, wrap 1", "[grow]", "[grow][" + Globals.GAP_SIZE + "][pref!]"));

		topPanel.add(visibleListScrollPane, "grow");
		topPanel.add(extraField, "grow");

		JScrollPane loggingScrollPane = new JScrollPane(loggingArea);
		loggingScrollPane.setMinimumSize(new Dimension());
		loggingScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, loggingScrollPane);
		splitPane.setResizeWeight(1.0);
		splitPane.setDividerLocation(START_HEIGHT_FRAME - START_HEIGHT_LOGGING_AREA);

		JPanel mainPanel = new JPanel(new MigLayout("insets 0, fill", "[]", "[]0"));
		mainPanel.add(splitPane, "grow, w " + START_WIDTH_FRAME + ", h " + START_HEIGHT_FRAME);

		return mainPanel;
	}

	private void noText() {
		extraField.setEditable(false);
		extraField.setEnabled(false);
		extraField.setText("");
		selText = null;
	}

	private void checkSelected() {
		if (visibleList.getSelectedValue() == null || selValue == null || "".equals(selValue)) {
			noText();
		}
	}

	public void resetValue() {
		visibleList.setSelectedValue(selValue, true);
		checkSelected();
	}

	protected void appendLog(final String s) {
		SwingUtilities.invokeLater(() -> {
			if (s == null) {
				loggingArea.setText("");
			} else {
				loggingArea.append(s);
				loggingArea.setCaretPosition(loggingArea.getText().length());
			}
		});
	}

	public void commit() {
		Logging.debug(this, "getSelectedValue ", visibleList.getSelectedValuesList());

		appendLog(null);

		if (!visibleList.getSelectedValuesList().isEmpty()) {
			String command = visibleList.getSelectedValuesList().getFirst();
			for (String client : configedMain.getSelectedClients()) {
				new RemoteCommandExecutor(this, command, client).execute();
			}
		}
	}

	// interface ListSelectionListener
	private void valueChanged(boolean valueIsAdjusting) {
		// Ignore extra messages.
		if (valueIsAdjusting) {
			return;
		}

		checkSelected();

		if (visibleList.getSelectedValue() != null) {
			selValue = visibleList.getSelectedValue();
		}

		selText = "" + selValue;

		Logging.debug(this, "valueChanged, selText ", selText);
		Logging.debug(this, "valueChanged, meanings.get(selText) ", meanings.get(selText));

		if (meanings != null && selText != null && meanings.get(selText) != null) {
			extraField.setText(meanings.get(selText));
			extraField.setEditable(editableFields.get(selText));
			extraField.setEnabled(editableFields.get(selText));
		}
	}

	private void saveEditedText() {
		if (extraField.isEditable() && selText != null && !selText.isEmpty() && meanings.get(selText) != null) {
			meanings.put(selText, extraField.getText());
		}
	}

	// DocumentListener
	@Override
	public void changedUpdate(DocumentEvent e) {
		saveEditedText();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		saveEditedText();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		saveEditedText();
	}
}
