/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection;

import de.uib.utilities.logging.Logging;

/**
 * Any kind of data needed for the operations.
 */
public class SelectData {
	private DataType dataType;
	private Object dataObject;

	// TODO: Dont use toString() method anywhere
	public enum DataType {
		TEXT_TYPE {
			@Override
			public String toString() {
				return "TextType";
			}
		},
		INTEGER_TYPE {
			@Override
			public String toString() {
				return "IntegerType";
			}
		},
		BIG_INTEGER_TYPE {
			@Override
			public String toString() {
				return "BigIntegerType";
			}
		},
		DOUBLE_TYPE {
			@Override
			public String toString() {
				return "DoubleType";
			}
		},
		ENUM_TYPE {
			@Override
			public String toString() {
				return "EnumType";
			}
		},
		DATE_TYPE {
			@Override
			public String toString() {
				return "DateType";
			}
		},
		NONE_TYPE {
			@Override
			public String toString() {
				return "NoneType";
			}
		}
	}

	public SelectData(Object data, DataType type) {
		dataObject = data;
		dataType = type;

		Logging.debug(this.getClass(), "got data, type " + data + ", " + type);

		switch (type) {
		case TEXT_TYPE:
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

		case DATE_TYPE:

			if (!(data instanceof String)) {
				throw new IllegalArgumentException("Data is not a (date) string");
			}
			break;

		case DOUBLE_TYPE:
			if (!(data instanceof Double)) {
				Logging.error(this.getClass(), "Data is no Double");
				throw new IllegalArgumentException("Data is no Double");
			}
			break;
		case ENUM_TYPE:
			if (!(data instanceof String)) {
				Logging.error(this.getClass(), "Data is no String");
				throw new IllegalArgumentException("Data is no String");
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
