package de.uib.utilities.table;

import java.util.List;

public interface ListCellOptions
// may represent an Opsi 4.0 config
// TODO make this class generic?
{
	public List<Object> getPossibleValues();

	public List<Object> getDefaultValues();

	public void setDefaultValues(List<Object> values);

	public int getSelectionMode();

	public boolean isEditable();

	public boolean isNullable();

	public String getDescription();
}
