package de.uib.utilities.swing;

import java.awt.event.KeyListener;
/*
* RecordPane.java
* 
* (c) uib 2012
* GPL-licensed
* author Rupert Röder
*
*
*/
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.observer.swing.JTextFieldObserved;

public abstract class AbstractRecordPane extends JPanel implements KeyListener {
	private static final int LINE_HEIGHT = Globals.LINE_HEIGHT;
	private static final int MIN_FIELD_WIDTH = 60;
	private static final int MAX_FIELD_WIDTH = Short.MAX_VALUE;
	private static final int MIN_LABEL_WIDTH = 30;
	private static final int MAX_LABEL_WIDTH = 100;

	// GUI
	protected Map<String, JLabel> labelfields;
	protected Map<String, JTextFieldObserved> datafields;

	// Data
	protected Map<String, String> data;
	protected Map<String, String> labels;
	protected Map<String, String> hints;
	protected Map<String, Boolean> editable;

	protected AbstractRecordPane() {
	}

	protected AbstractRecordPane(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		init(data, labels, hints, editable);
	}

	public void setData(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		init(data, labels, hints, editable);
	}

	private void init(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		this.data = data;
		this.labels = labels;
		this.editable = editable;
		this.hints = hints;

		initComponents();
	}

	protected void initComponents() {

		if (!ConfigedMain.THEMES) {
			setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		labelfields = new HashMap<>();
		datafields = new HashMap<>();

		GroupLayout baseLayout = new GroupLayout(this);
		this.setLayout(baseLayout);

		if (data == null) {
			return;
		}

		for (Entry<String, String> dataEntry : data.entrySet()) {
			JLabel jLabel = new JLabel();

			if (labels == null || labels.get(dataEntry.getKey()) == null) {
				jLabel.setText("");
			} else {
				jLabel.setText(labels.get(dataEntry.getKey()));
			}

			jLabel.setFont(Globals.defaultFontBig);
			labelfields.put(dataEntry.getKey(), jLabel);

			JTextFieldObserved jTextField = new JTextFieldObserved();
			if (dataEntry.getValue() != null) {
				jTextField.setText("" + dataEntry.getValue());
			} else {
				jTextField.setText("");
			}

			jTextField.setFont(Globals.defaultFontBig);
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

			datafields.put(dataEntry.getKey(), jTextField);
		}

		GroupLayout.ParallelGroup hGroup = baseLayout.createParallelGroup();

		for (String key : data.keySet()) {
			hGroup.addGroup(baseLayout.createSequentialGroup()
					.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
					.addComponent(labelfields.get(key), MIN_LABEL_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_LABEL_WIDTH)
					.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
					.addComponent(datafields.get(key), MIN_FIELD_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_FIELD_WIDTH)
					.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2));
		}

		baseLayout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = baseLayout.createSequentialGroup();

		vGroup.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE);
		for (String key : data.keySet()) {
			vGroup.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE);
			vGroup.addGroup(baseLayout.createParallelGroup()
					.addComponent(labelfields.get(key), LINE_HEIGHT, LINE_HEIGHT, LINE_HEIGHT)
					.addComponent(datafields.get(key), LINE_HEIGHT, LINE_HEIGHT, LINE_HEIGHT));
		}
		vGroup.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE);

		baseLayout.setVerticalGroup(vGroup);

	}

	public Map<String, String> getData() {
		for (String key : data.keySet()) {
			data.put(key, datafields.get(key).getText());
		}
		return data;
	}

}
