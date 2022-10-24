package de.uib.utilities.table;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;

public class DefaultListCellOptions
			implements ListCellOptions
{
	java.util.List possibleValues;
	java.util.List defaultValues;
	int selectionMode;
	boolean editable;
	boolean nullable;
	String description;

	public DefaultListCellOptions()
	{
		possibleValues = new ArrayList();
		defaultValues = new ArrayList();
		selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
		editable = true;
		nullable = true;
		description = "";
		logging.info(this, "constructed " +  possibleValues +  ", " + defaultValues + ", " + selectionMode
		             + ", " + editable + ", " + nullable);
	}

	public DefaultListCellOptions(
	    java.util.List possibleValues,
	    java.util.List defaultValues,
	    int selectionMode,
	    boolean editable,
	    boolean nullable,
	    String description)

	{
		this.possibleValues = possibleValues;
		this.defaultValues = defaultValues;
		this.selectionMode = selectionMode;
		this.editable = editable;
		this.nullable = nullable;
		if (description == null)
			this.description  = "";
		else
			this.description= description;
		logging.info(this, "constructed with given " +  possibleValues +  ", " + defaultValues + ", " + selectionMode
		             + ", " + editable + ", " + nullable);
	}


	public static ListCellOptions getNewBooleanListCellOptions()
	{
		logging.info("getNewBooleanListCellOptions");
		java.util.List possibleValues = new ArrayList();
		possibleValues.add(true);
		possibleValues.add(false);
		java.util.List defaultValues = new ArrayList();
		defaultValues.add(false);
		boolean editable = false;
		boolean nullable = false;
		return
		    new DefaultListCellOptions(
		        possibleValues,
		        defaultValues,
		        ListSelectionModel.SINGLE_SELECTION,
		        editable,
		        nullable,
		        ""
		    );
	}

	public static ListCellOptions getNewEmptyListCellOptions()
	{
		logging.info("getNewEmptyListCellOptions");
		java.util.List possibleValues = new ArrayList();
		boolean editable = true;
		boolean nullable = true;
		return
		    new DefaultListCellOptions(
		        possibleValues,
		        null, //defaultValues,
		        ListSelectionModel.SINGLE_SELECTION,
		        editable,
		        nullable,
		        ""
		    );
	}

	public static ListCellOptions getNewEmptyListCellOptionsMultiSelection()
	{
		logging.info("getNewBooleanListCellOptionsMultiSelection");
		java.util.List possibleValues = new ArrayList();
		boolean editable = true;
		boolean nullable = true;
		return
		    new DefaultListCellOptions(
		        possibleValues,
		        null, //defaultValues,
		        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
		        editable,
		        nullable,
		        ""
		    );
	}


	public java.util.List getPossibleValues()
	{
		return possibleValues;
	}

	public java.util.List getDefaultValues()
	{
		return defaultValues;
	}

	public void setDefaultValues(java.util.List values)
	{
		defaultValues = values;
	}

	public int getSelectionMode()
	{
		return selectionMode;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public String getDescription()
	{
		return description;
	}



	@Override
	public String toString()
	{
		return "DefaultListCellOptions,  possibleValues: " + possibleValues
		       + "; defaultValues: " + defaultValues
		       + "; selectionMode: " + selectionMode
		       + "; editable: " + editable
		       + "; nullable: " + nullable;
	}

}



