package de.uib.configed.clientselection.serializers;

import java.io.IOException;
import java.io.StringReader;
import java.util.Deque;
import java.util.LinkedList;

import de.uib.utilities.logging.logging;

/**
 * This is a small parser for the JSON-like syntax of the OpsiDataSerializer.
 * Things different from JSON: - values of the type SelectData.DataType are not
 * in quotes - all other values are in quotes
 */
class JsonParser {
	private StringReader reader;
	private PositionType currentPosition = PositionType.JSON_VALUE;
	private String currentValue = null;
	private boolean inList = false;
	Deque<PositionType> stack;

	public enum PositionType {
		OBJECT_BEGIN, OBJECT_END, LIST_BEGIN, LIST_END, JSON_NAME, JSON_VALUE
	}

	public JsonParser(String input) {
		reader = new StringReader(input);
		stack = new LinkedList<>();
	}

	public boolean next() throws IOException {
		int i;

		while ((i = reader.read()) != -1) {

			logging.debug(this, (char) i + " " + currentPosition.toString());
			managePosition();
			char c = (char) i;
			if (Character.isWhitespace(c))
				continue;
			if (c == ':' && currentPosition == PositionType.JSON_NAME) {
				currentPosition = PositionType.JSON_VALUE;
				continue;
			}
			if (c == ',' && currentPosition == PositionType.JSON_VALUE) {
				if (!inList)
					currentPosition = PositionType.JSON_NAME;
				continue;
			}
			if (c == '{' && currentPosition == PositionType.JSON_VALUE) {
				currentPosition = PositionType.OBJECT_BEGIN;
				inList = false;
				return true;
			}
			if (c == '}' && currentPosition == PositionType.JSON_VALUE) {
				currentPosition = PositionType.OBJECT_END;
				return true;
			}
			if (c == '[' && currentPosition == PositionType.JSON_VALUE) {
				currentPosition = PositionType.LIST_BEGIN;
				inList = true;
				return true;
			}
			if (c == ']' && currentPosition == PositionType.JSON_VALUE) {
				currentPosition = PositionType.LIST_END;
				inList = false;
				return true;
			}
			if ((c == '"' || Character.isLetter(c))
					&& (currentPosition == PositionType.JSON_VALUE || currentPosition == PositionType.JSON_NAME)) {
				currentValue = getNextValue(c);
				return true;
			}
			throw new IllegalArgumentException("Unexpected character: " + c);
		}

		return false;
	}

	public PositionType getPositionType() {
		return currentPosition;
	}

	public String getValue() {
		return currentValue;
	}

	private String getNextValue(char c) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(c);
		int i = reader.read();
		if (c == '"') {
			while (i != -1 && ((char) i) != '"') {
				builder.append((char) i);
				i = reader.read();
			}
			if (i == -1)
				throw new IllegalArgumentException("Unexpected EOF");
			builder.append((char) i);
			return builder.toString();
		} else {
			while (i != -1 && (Character.isLetterOrDigit((char) i) || ((char) i) == '.')) {
				builder.append((char) i);
				i = reader.read();
			}
			reader.skip(-1);
			return builder.toString();
		}
	}

	private void managePosition() {
		if (currentPosition == PositionType.OBJECT_BEGIN) {
			stack.push(PositionType.OBJECT_BEGIN);
			currentPosition = PositionType.JSON_NAME;
		} else if (currentPosition == PositionType.LIST_BEGIN) {
			stack.push(PositionType.LIST_BEGIN);
			currentPosition = PositionType.JSON_VALUE;
		} else if (currentPosition == PositionType.OBJECT_END || currentPosition == PositionType.LIST_END) {
			stack.pop();
			currentPosition = stack.peek();
			logging.debug(this, "managePosition: " + currentPosition.toString());
			if (currentPosition == PositionType.OBJECT_BEGIN) {
				inList = false;
			} else {
				inList = true;
			}
			currentPosition = PositionType.JSON_VALUE;
		}
	}
}
