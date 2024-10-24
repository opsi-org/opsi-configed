/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;

import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.utils.logging.Logging;

public class DefaultListCellOptions implements ListCellOptions {
	private List<Object> possibleValues;
	private List<Object> defaultValues;
	private int selectionMode;
	private boolean editable;
	private String description;
	private TYPE type;

	public DefaultListCellOptions() {
		possibleValues = new ArrayList<>();
		defaultValues = new ArrayList<>();
		selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		editable = true;
		description = "";
		Logging.info(this, "constructed ", possibleValues, ", ", defaultValues, ", ", selectionMode, ", ", editable);
	}

	public DefaultListCellOptions(List<Object> possibleValues, List<Object> defaultValues, int selectionMode,
			boolean editable, String description, TYPE type) {
		this.possibleValues = possibleValues;
		this.defaultValues = defaultValues;
		this.selectionMode = selectionMode;
		this.editable = editable;
		if (description == null) {
			this.description = "";
		} else {
			this.description = description;
		}
		this.type = type;
		Logging.info(this, "constructed with given ", possibleValues, ", ", defaultValues, ", ", selectionMode, ", ",
				editable);
	}

	public static ListCellOptions getNewBooleanListCellOptions() {
		Logging.info("getNewBooleanListCellOptions");
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(true);
		possibleValues.add(false);
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(false);
		boolean editable = false;
		return new DefaultListCellOptions(possibleValues, defaultValues, ListSelectionModel.SINGLE_SELECTION, editable,
				"", TYPE.BOOL_CONFIG);
	}

	public static ListCellOptions getNewEmptyListCellOptions() {
		Logging.info("getNewEmptyListCellOptions");
		List<Object> possibleValues = new ArrayList<>();
		boolean editable = true;
		return new DefaultListCellOptions(possibleValues, null, ListSelectionModel.SINGLE_SELECTION, editable, "",
				TYPE.UNICODE_CONFIG);
	}

	public static ListCellOptions getNewEmptyListCellOptionsMultiSelection() {
		Logging.info("getNewBooleanListCellOptionsMultiSelection");
		List<Object> possibleValues = new ArrayList<>();
		boolean editable = true;
		return new DefaultListCellOptions(possibleValues, null, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
				editable, "", TYPE.UNICODE_CONFIG);
	}

	@Override
	public List<Object> getPossibleValues() {
		return possibleValues;
	}

	@Override
	public List<Object> getDefaultValues() {
		return defaultValues;
	}

	@Override
	public void setDefaultValues(List<Object> values) {
		defaultValues = values;
	}

	@Override
	public int getSelectionMode() {
		return selectionMode;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "DefaultListCellOptions,  possibleValues: " + possibleValues + "; defaultValues: " + defaultValues
				+ "; selectionMode: " + selectionMode + "; editable: " + editable;
	}

	@Override
	public TYPE getType() {
		return type;
	}
}
