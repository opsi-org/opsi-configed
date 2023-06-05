/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Map;

public class FEditRecord extends FEdit {
	private AbstractRecordPane recordPane;

	public FEditRecord(String hint) {
		super("", hint);
		recordPane = new AbstractRecordPane() {
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

			@Override
			public void keyReleased(KeyEvent arg0) {
				/* Not needed */}

			@Override
			public void keyTyped(KeyEvent arg0) {
				/* Not needed */}
		};
	}

	public void setRecord(Map<String, String> data, Map<String, String> labels, Map<String, String> hints,
			Map<String, Boolean> editable, Map<String, Boolean> secrets) {
		recordPane.setData(data, labels, hints, editable, secrets);

		editingArea.add(recordPane, BorderLayout.CENTER);
	}

	public Map<String, String> getData() {
		return recordPane.getData();
	}
}
