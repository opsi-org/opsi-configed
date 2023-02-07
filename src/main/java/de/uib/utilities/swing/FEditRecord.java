/*
 * FEditRecord.java
 *
 * (c) uib 2012
 * GPL-licensed
 * author Rupert Röder
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class FEditRecord extends FEdit {
	protected RecordPane recordPane;

	protected Map<String, JLabel> labels;
	protected Map<String, JTextField> textfields;
	protected Map<String, String> data;
	protected Map<String, String> hints;
	protected Map<String, Boolean> editable;

	public FEditRecord(String hint) {
		super("", hint);
		recordPane = new RecordPane() {
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					commit();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancel();
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				/* Not needed */}

			@Override
			public void keyTyped(KeyEvent arg0) {
				/* Not needed */}
		};
	}

	public void setRecord(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable) {
		recordPane.setData(data, labels, hints, editable);

		editingArea.add(recordPane, BorderLayout.CENTER);
	}

	public Map<String, String> getData() {
		return recordPane.getData();
	}
}
