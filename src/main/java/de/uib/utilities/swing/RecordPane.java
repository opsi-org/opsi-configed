/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import de.uib.utilities.observer.swing.JTextFieldObserved;

public class RecordPane extends JPanel implements KeyListener, DocumentListener {
	private static final int LINE_HEIGHT = Globals.LINE_HEIGHT;
	private static final int MIN_FIELD_WIDTH = 60;
	private static final int MAX_FIELD_WIDTH = Short.MAX_VALUE;
	private static final int MIN_LABEL_WIDTH = 30;
	private static final int MAX_LABEL_WIDTH = 100;

	// GUI
	private Map<String, JLabel> labelfields;
	private Map<String, JTextField> datafields;

	// Data
	private Map<String, String> data;
	private Map<String, String> labels;
	private Map<String, String> hints;
	private Map<String, Boolean> editable;
	private Map<String, Boolean> secrets;

	private FEditRecord fEditRecord;

	public RecordPane(FEditRecord fEditRecord) {
		this.fEditRecord = fEditRecord;
	}

	public void setData(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable, Map<String, Boolean> secrets) {
		init(data, labels, hints, editable, secrets);
	}

	private void init(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable, Map<String, Boolean> secrets) {
		this.data = data;
		this.labels = labels;
		this.editable = editable;
		this.hints = hints;
		this.secrets = secrets;

		initComponents();
	}

	private void initComponents() {
		GroupLayout baseLayout = new GroupLayout(this);
		this.setLayout(baseLayout);

		if (data == null) {
			return;
		}

		initLabelAndDataFields();

		GroupLayout.ParallelGroup hGroup = baseLayout.createParallelGroup();

		for (String key : data.keySet()) {
			hGroup.addGroup(baseLayout.createSequentialGroup()
					.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
					.addComponent(labelfields.get(key), MIN_LABEL_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_LABEL_WIDTH)
					.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
					.addComponent(datafields.get(key), MIN_FIELD_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_FIELD_WIDTH)
					.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2));
		}

		baseLayout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = baseLayout.createSequentialGroup();

		vGroup.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE);
		for (String key : data.keySet()) {
			vGroup.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE);
			vGroup.addGroup(baseLayout.createParallelGroup()
					.addComponent(labelfields.get(key), LINE_HEIGHT, LINE_HEIGHT, LINE_HEIGHT)
					.addComponent(datafields.get(key), LINE_HEIGHT, LINE_HEIGHT, LINE_HEIGHT));
		}
		vGroup.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE);

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
			jTextField = new JTextFieldObserved();
			jTextField.getDocument().addDocumentListener(this);
		}

		if (dataEntry.getValue() != null) {
			jTextField.setText("" + dataEntry.getValue());
		} else {
			jTextField.setText("");
		}

		jTextField.getCaret().setBlinkRate(0);

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

	public Map<String, String> getData() {
		for (String key : data.keySet()) {
			if (datafields.get(key) instanceof JPasswordField) {
				data.put(key, new String(((JPasswordField) datafields.get(key)).getPassword()));
			} else {
				data.put(key, datafields.get(key).getText());
			}
		}
		return data;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent arg0) {
		/* Not needed */}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			fEditRecord.commit();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			fEditRecord.cancel();
		} else {
			// We want to do nothing on other keys
		}
	}

	@Override
	public void insertUpdate(DocumentEvent document) {
		fEditRecord.setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent document) {
		fEditRecord.setDataChanged(true);
	}

	@Override
	public void changedUpdate(DocumentEvent document) {
		fEditRecord.setDataChanged(true);
	}
}
