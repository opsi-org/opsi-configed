package de.uib.utilities.table;

public interface ListCellOptions
// may represent an Opsi 4.0 config
// and is for this purpose implemented in de.uib.configed.type;ConfigOption
{
	public java.util.List<Object> getPossibleValues();

	public java.util.List<Object> getDefaultValues();

	public void setDefaultValues(java.util.List<Object> values);

	public int getSelectionMode();

	public boolean isEditable();

	public boolean isNullable();

	public String getDescription();
}
