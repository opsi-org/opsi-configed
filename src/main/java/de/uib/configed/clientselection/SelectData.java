package de.uib.configed.clientselection;

import de.uib.utilities.logging.logging;

/**
 * Any kind of data needed for the operations.
 */
public class SelectData {
	private DataType dataType;
	private Object dataObject;

	public enum DataType {
		TEXT_TYPE, INTEGER_TYPE, BIT_INTEGER_TYPE, DOUBLE_TYPE, ENUM_TYPE, DATE_TYPE, NONE_TYPE
	}

	public SelectData(Object data, DataType type) {
		dataType = type;
		dataObject = data;

		logging.debug(this, "got data, type " + data + ", " + type);

		switch (type) {
		case TEXT_TYPE:
			if (!(data instanceof String)) {
				logging.error(this, "Data is no String");
				throw new IllegalArgumentException("Data is no String");
			}
			break;
		case INTEGER_TYPE:
			if (!(data instanceof Integer)) {
				logging.error(this, "Data is no Integer");
				throw new IllegalArgumentException("Data is no Integer");
			}
			break;
		case BIT_INTEGER_TYPE:
			if (!(data instanceof Long)) {
				logging.error(this, "Data is no Long");
				throw new IllegalArgumentException("Data is no Long");
			}
			break;

		case DATE_TYPE: {

			if (!(data instanceof String))

				throw new IllegalArgumentException("Data is not a (date) string");

			break;
		}

		case DOUBLE_TYPE:
			if (!(data instanceof Double)) {
				logging.error(this, "Data is no Double");
				throw new IllegalArgumentException("Data is no Double");
			}
			break;
		case ENUM_TYPE:
			if (!(data instanceof String)) {
				logging.error(this, "Data is no String");
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
