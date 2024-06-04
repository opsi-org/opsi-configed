/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Globals;

public class FEditRecord extends FEdit implements DocumentListener {
	private static final int MIN_FIELD_WIDTH = 60;
	private static final int MIN_LABEL_WIDTH = 30;
	private static final int MAX_LABEL_WIDTH = 100;

	private JPanel recordPane;

	private Map<String, JLabel> labelfields;
	private Map<String, JTextField> datafields;

	private Map<String, String> data;
	private Map<String, String> labels;
	private Map<String, String> hints;
	private Map<String, Boolean> editable;
	private Map<String, Boolean> secrets;

	public FEditRecord(String hint) {
		super("", hint);
	}

	private void initRecordPane() {
		recordPane = new JPanel();
		recordPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					commit();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancel();
				} else {
					// We want to do nothing on other keys
				}
			}
		});

		GroupLayout baseLayout = new GroupLayout(recordPane);
		recordPane.setLayout(baseLayout);

		if (data == null) {
			return;
		}

		initLabelAndDataFields();

		GroupLayout.ParallelGroup hGroup = baseLayout.createParallelGroup();

		for (String key : data.keySet()) {
			hGroup.addGroup(baseLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
					.addComponent(labelfields.get(key), MIN_LABEL_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_LABEL_WIDTH)
					.addGap(Globals.MIN_GAP_SIZE)
					.addComponent(datafields.get(key), MIN_FIELD_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGap(Globals.MIN_GAP_SIZE));
		}

		baseLayout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = baseLayout.createSequentialGroup();

		vGroup.addGap(Globals.GAP_SIZE);
		for (String key : data.keySet()) {
			vGroup.addGap(Globals.GAP_SIZE);
			vGroup.addGroup(baseLayout.createParallelGroup()
					.addComponent(labelfields.get(key), Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
					.addComponent(datafields.get(key), Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));
		}
		vGroup.addGap(Globals.GAP_SIZE);

		baseLayout.setVerticalGroup(vGroup);
	}

	private void initLabelAndDataFields() {
		labelfields = new HashMap<>();
		datafields = new HashMap<>();

		for (Entry<String, String> dataEntry : data.entrySet()) {
			JLabel jLabel = createJLabel(dataEntry);
			labelfields.put(dataEntry.getKey(), jLabel);
			JTextField jTextField = createJTextField(dataEntry);
			datafields.put(dataEntry.getKey(), jTextField);
		}
	}

	private JLabel createJLabel(Entry<String, String> dataEntry) {
		JLabel jLabel = new JLabel();

		if (labels == null || labels.get(dataEntry.getKey()) == null) {
			jLabel.setText("");
		} else {
			jLabel.setText(labels.get(dataEntry.getKey()));
		}

		return jLabel;
	}

	private JTextField createJTextField(Entry<String, String> dataEntry) {
		JTextField jTextField;

		if (secrets != null && !secrets.isEmpty() && Boolean.TRUE.equals(secrets.get(dataEntry.getKey()))) {
			jTextField = new JPasswordField();
			jTextField.getDocument().addDocumentListener(this);
		} else {
			jTextField = new RevertibleTextField();
			jTextField.getDocument().addDocumentListener(this);
		}

		if (dataEntry.getValue() != null) {
			jTextField.setText("" + dataEntry.getValue());
		} else {
			jTextField.setText("");
		}

		if (hints != null) {
			jTextField.setToolTipText(hints.get(dataEntry.getKey()));
		}

		if (editable != null && editable.get(dataEntry.getKey()) != null) {
			jTextField.setEditable(editable.get(dataEntry.getKey()));
			jTextField.setEnabled(editable.get(dataEntry.getKey()));
		} else {
			jTextField.setEditable(false);
			jTextField.setEnabled(false);
		}

		jTextField.addKeyListener(this);

		return jTextField;
	}

	public void setRecord(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable, Map<String, Boolean> secrets) {
		this.data = data;
		this.labels = labels;
		this.editable = editable;
		this.hints = hints;
		this.secrets = secrets;

		initRecordPane();

		editingArea.add(recordPane, BorderLayout.CENTER);
	}

	public Map<String, String> getData() {
		for (String key : data.keySet()) {
			if (datafields.get(key) instanceof JPasswordField jPasswordField) {
				data.put(key, new String(jPasswordField.getPassword()));
			} else {
				data.put(key, datafields.get(key).getText());
			}
		}
		return data;
	}

	@Override
	public void insertUpdate(DocumentEvent document) {
		setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent document) {
		setDataChanged(true);
	}

	@Override
	public void changedUpdate(DocumentEvent document) {
		setDataChanged(true);
	}
}
