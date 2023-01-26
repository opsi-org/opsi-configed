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

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.ObservableSubject;
import de.uib.utilities.observer.swing.JTextFieldObserved;

public abstract class RecordPane extends JPanel implements KeyListener {
	protected int lineHeight = Globals.LINE_HEIGHT;
	protected int vGapSize = Globals.VGAP_SIZE;
	protected int hGapSize = Globals.HGAP_SIZE;
	protected int minFieldWidth = 60;
	protected int maxFieldWidth = Short.MAX_VALUE;
	protected int minLabelWidth = 30;
	protected int maxLabelWidth = 100;

	protected class TheObservableSubject extends ObservableSubject {
		@Override
		public void notifyObservers() {

			Logging.debug("RecordPane: notifyObservers ");
		}
	}

	protected ObservableSubject editingNotifier;

	// GUI
	protected Map<String, JLabel> labelfields;
	protected Map<String, JTextFieldObserved> datafields;

	// Data
	protected Map<String, String> data;
	protected Map<String, String> labels;
	protected Map<String, String> hints;
	protected Map<String, Boolean> editable;

	public RecordPane() {
		// call of setData necessary
	}

	public RecordPane(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		init(data, labels, hints, editable);
	}

	public void setObservableSubject(ObservableSubject editingNotifier) {
		if (editingNotifier == null) {
			this.editingNotifier = new TheObservableSubject();
		} else
			this.editingNotifier = editingNotifier;

		for (JTextFieldObserved value : datafields.values()) {
			value.setGlobalObservableSubject(this.editingNotifier);
		}
	}

	public void setData(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		init(data, labels, hints, editable);
	}

	protected void init(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		this.data = data;
		this.labels = labels;
		this.editable = editable;
		this.hints = hints;

		initComponents();

	}

	protected void initComponents() {

		setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		labelfields = new HashMap<>();
		datafields = new HashMap<>();

		javax.swing.GroupLayout baseLayout = new javax.swing.GroupLayout(this);
		this.setLayout(baseLayout);

		if (data == null)
			return;

		for (Entry<String, String> dataEntry : data.entrySet()) {
			JLabel jLabel = new JLabel();

			if (labels == null || labels.get(dataEntry.getKey()) == null)
				jLabel.setText("");
			else
				jLabel.setText(labels.get(dataEntry.getKey()));

			jLabel.setFont(Globals.defaultFontBig);
			labelfields.put(dataEntry.getKey(), jLabel);

			JTextFieldObserved jTextField = new JTextFieldObserved();
			if (dataEntry.getValue() != null)
				jTextField.setText("" + dataEntry.getValue());
			else
				jTextField.setText("");

			jTextField.setFont(Globals.defaultFontBig);
			jTextField.getCaret().setBlinkRate(0);

			if (hints != null)
				jTextField.setToolTipText(hints.get(dataEntry.getKey()));

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
					.addComponent(labelfields.get(key), minLabelWidth, GroupLayout.PREFERRED_SIZE, maxLabelWidth)
					.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
					.addComponent(datafields.get(key), minFieldWidth, GroupLayout.PREFERRED_SIZE, maxFieldWidth)
					.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2));
		}

		baseLayout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = baseLayout.createSequentialGroup();

		vGroup.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE);
		for (String key : data.keySet()) {
			vGroup.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE);
			vGroup.addGroup(baseLayout.createParallelGroup()
					.addComponent(labelfields.get(key), lineHeight, lineHeight, lineHeight)
					.addComponent(datafields.get(key), lineHeight, lineHeight, lineHeight));
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
