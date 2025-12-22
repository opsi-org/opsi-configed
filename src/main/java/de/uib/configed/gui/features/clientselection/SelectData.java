/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection;

import de.uib.configed.share.logging.Logging;

/**
 * Any kind of data needed for the operations.
 */
public class SelectData {
	private DataType dataType;
	private Object dataObject;

	public enum DataType {
		TEXT_TYPE("TextType"), INTEGER_TYPE("IntegerType"), BIG_INTEGER_TYPE("BigIntegerType"),
		DOUBLE_TYPE("DoubleType"), DATE_TYPE("DataType"), NONE_TYPE("NoneType"), BOOLEAN_TYPE("BooleanType");

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

		Logging.debug(this, "got data, type ", data, ", ", type);

		Class<?> expectedClass = switch (type) {
		case TEXT_TYPE, DATE_TYPE -> String.class;
		case INTEGER_TYPE -> Integer.class;
		case BIG_INTEGER_TYPE -> Long.class;
		case DOUBLE_TYPE -> Double.class;
		case BOOLEAN_TYPE -> Boolean.class;
		case NONE_TYPE -> null;
		};

		if (expectedClass != null && !expectedClass.isInstance(data)) {
			String warningText = "data type mismatch: expected " + expectedClass.getName() + " but got "
					+ data.getClass().getName();
			Logging.error(this, warningText);
		}
	}

	public DataType getType() {
		return dataType;
	}

	public Object getData() {
		return dataObject;
	}
}
