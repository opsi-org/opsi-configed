package de.uib.messages;

import de.uib.utilities.logging.Logging;

public class LocaleRepresentation
// a String separated by '='
{
	private String value = "";

	public LocaleRepresentation(String name) {
		if (name == null)
			Logging.error(this, "name must not be null");

		value = name;
	}

	public LocaleRepresentation(String name, String iconName) {
		this(name);
		if (iconName != null)
			value = value + "=" + iconName;
	}

	public String getName() {
		int pos = value.indexOf('=');
		if (pos > -1)
			return value.substring(0, pos);

		return value;
	}

	public String getIconName() {
		int pos = value.indexOf('=');
		if (pos > -1)
			return value.substring(pos + 1);

		return "";
	}

	@Override
	public String toString() {
		return value;
	}
}
