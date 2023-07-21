/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;
import java.util.Map;

public class FEditRecord extends FEdit {
	private RecordPane recordPane;

	public FEditRecord(String hint) {
		super("", hint);

		recordPane = new RecordPane(this);
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
