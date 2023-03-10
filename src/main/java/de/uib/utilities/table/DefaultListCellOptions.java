package de.uib.utilities.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;

import de.uib.utilities.logging.Logging;

public class DefaultListCellOptions implements ListCellOptions {
	List possibleValues;
	List defaultValues;
	int selectionMode;
	boolean editable;
	boolean nullable;
	String description;

	public DefaultListCellOptions() {
		possibleValues = new ArrayList<>();
		defaultValues = new ArrayList<>();
		selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		editable = true;
		nullable = true;
		description = "";
		Logging.info(this, "constructed " + possibleValues + ", " + defaultValues + ", " + selectionMode + ", "
				+ editable + ", " + nullable);
	}

	public DefaultListCellOptions(List possibleValues, List defaultValues, int selectionMode, boolean editable,
			boolean nullable, String description)

	{
		this.possibleValues = possibleValues;
		this.defaultValues = defaultValues;
		this.selectionMode = selectionMode;
		this.editable = editable;
		this.nullable = nullable;
		if (description == null)
			this.description = "";
		else
			this.description = description;
		Logging.info(this, "constructed with given " + possibleValues + ", " + defaultValues + ", " + selectionMode
				+ ", " + editable + ", " + nullable);
	}

	public static ListCellOptions getNewBooleanListCellOptions() {
		Logging.info("getNewBooleanListCellOptions");
		List possibleValues = new ArrayList<>();
		possibleValues.add(true);
		possibleValues.add(false);
		List defaultValues = new ArrayList<>();
		defaultValues.add(false);
		boolean editable = false;
		boolean nullable = false;
		return new DefaultListCellOptions(possibleValues, defaultValues, ListSelectionModel.SINGLE_SELECTION, editable,
				nullable, "");
	}

	public static ListCellOptions getNewEmptyListCellOptions() {
		Logging.info("getNewEmptyListCellOptions");
		List possibleValues = new ArrayList<>();
		boolean editable = true;
		boolean nullable = true;
		return new DefaultListCellOptions(possibleValues, null, ListSelectionModel.SINGLE_SELECTION, editable, nullable,
				"");
	}

	public static ListCellOptions getNewEmptyListCellOptionsMultiSelection() {
		Logging.info("getNewBooleanListCellOptionsMultiSelection");
		List possibleValues = new ArrayList<>();
		boolean editable = true;
		boolean nullable = true;
		return new DefaultListCellOptions(possibleValues, null, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
				editable, nullable, "");
	}

	@Override
	public List getPossibleValues() {
		return possibleValues;
	}

	@Override
	public List getDefaultValues() {
		return defaultValues;
	}

	@Override
	public void setDefaultValues(List values) {
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
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "DefaultListCellOptions,  possibleValues: " + possibleValues + "; defaultValues: " + defaultValues
				+ "; selectionMode: " + selectionMode + "; editable: " + editable + "; nullable: " + nullable;
	}

}
