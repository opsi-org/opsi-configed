package de.uib.utilities.table;

import java.util.List;

public interface ListCellOptions
// may represent an Opsi 4.0 config
// and is for this purpose implemented in de.uib.configed.type;ConfigOption
{
	public List<Object> getPossibleValues();

	public List<Object> getDefaultValues();

	public void setDefaultValues(List<Object> values);

	public int getSelectionMode();

	public boolean isEditable();

	public boolean isNullable();

	public String getDescription();
}
