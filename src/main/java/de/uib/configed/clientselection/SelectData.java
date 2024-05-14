/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import de.uib.utils.logging.Logging;

/**
 * Any kind of data needed for the operations.
 */
public class SelectData {
	private DataType dataType;
	private Object dataObject;

	public enum DataType {
		TEXT_TYPE("TextType"), INTEGER_TYPE("IntegerType"), BIG_INTEGER_TYPE("BigIntegerType"),
		DOUBLE_TYPE("DoubleType"), DATE_TYPE("DataType"), NONE_TYPE("NoneType");

		private final String displayName;

		DataType(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	public SelectData(Object data, DataType type) {
		dataObject = data;
		dataType = type;

		Logging.debug(this.getClass(), "got data, type " + data + ", " + type);

		switch (type) {
		case TEXT_TYPE, DATE_TYPE:
			if (!(data instanceof String)) {
				Logging.error(this.getClass(), "Data is no String");
				throw new IllegalArgumentException("Data is no String");
			}
			break;
		case INTEGER_TYPE:
			if (!(data instanceof Integer)) {
				Logging.error(this.getClass(), "Data is no Integer");
				throw new IllegalArgumentException("Data is no Integer");
			}
			break;
		case BIG_INTEGER_TYPE:
			if (!(data instanceof Long)) {
				Logging.error(this.getClass(), "Data is no Long");
				throw new IllegalArgumentException("Data is no Long");
			}
			break;
		case DOUBLE_TYPE:
			if (!(data instanceof Double)) {
				Logging.error(this.getClass(), "Data is no Double");
				throw new IllegalArgumentException("Data is no Double");
			}
			break;
		case NONE_TYPE:
			break;
		}
	}

	public DataType getType() {
		return dataType;
	}

	public Object getData() {
		return dataObject;
	}
}
