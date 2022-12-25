package de.uib.configed.clientselection.serializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.type.SavedSearch;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.utilities.logging.logging;

public class OpsiDataSerializer extends de.uib.configed.clientselection.Serializer {
	private PersistenceController controller;
	private JsonParser parser;
	private SelectData.DataType lastDataType;
	private Map<String, String> searches;
	public static final int dataVersion = 2;
	private int searchDataVersion;

	public OpsiDataSerializer(SelectionManager manager) {
		super(manager);
		controller = PersistenceControllerFactory.getPersistenceController();
		searches = new HashMap<>();
		searchDataVersion = dataVersion;
	}

	@Override
	public List<String> getSaved() {
		HashSet<String> set = new HashSet<>();
		set.addAll(searches.keySet());
		set.addAll(controller.getSavedSearches().keySet());
		return new LinkedList<>(set);
	}

	@Override
	public SavedSearches getSavedSearches() {
		return controller.getSavedSearches();
	}

	@Override
	public void remove(String name) {
		if (searches.containsKey(name))
			searches.remove(name);
		// do something with the controller
	}

	@Override
	protected Map<String, Object> decipher(String serialization) throws WrongVersionException {
		Map<String, Object> map = new HashMap<>();
		parser = new JsonParser(serialization);
		try {
			if (!parser.next() || parser.getPositionType() != JsonParser.PositionType.ObjectBegin)
				return map;
		} catch (IOException e) {
			logging.error(this, e.getMessage(), e);
			return map;
		}
		map = parseObject();
		int version;
		if (!map.containsKey("version"))
			version = 1;
		else
			version = Integer.valueOf((String) map.get("version"));
		searchDataVersion = version;
		return (Map) map.get("data");
	}

	@Override
	protected Map<String, Object> getData(String name) throws WrongVersionException {

		// we take version from server and not the (possibly edited own version! )
		searches.put(name, controller.getSavedSearches().get(name).getSerialization());

		// controller.getSavedSearches().get(name)

		String serialization = searches.get(name);
		return decipher(serialization);
	}

	@Override
	protected void saveData(String name, String description, Map<String, Object> data) {
		String jsonString;
		try {
			jsonString = "{ \"version\" : \"" + dataVersion + "\", \"data\" : ";
			jsonString += createJsonRecursive(data);
			jsonString += " }";
		} catch (IllegalArgumentException e) {
			logging.error(this, "Saving failed: " + e.getMessage(), e);
			return;
		}

		logging.info(this, name + ": " + jsonString);
		searches.put(name, jsonString);
		SavedSearch saveObj = new SavedSearch(name, jsonString, description);
		controller.saveSearch(saveObj);
	}

	@Override
	protected int getSearchDataVersion() {
		return searchDataVersion;
	}

	private static String objectToString(Object object) {
		if (object == null)
			return "null";
		if (object instanceof String)
			return "\"" + (String) object + "\"";
		if (object instanceof Integer)
			return "\"" + String.valueOf((Integer) object) + "\"";
		if (object instanceof Long)
			return "\"" + String.valueOf((Long) object) + "\"";
		if (object instanceof SelectData.DataType)
			return object.toString();
		if (object instanceof String[]) {
			String result = "[ ";
			String[] data = (String[]) object;
			for (int i = 0; i < data.length - 1; i++) {
				result += objectToString(data[i]);
				result += ", ";
			}
			return result + objectToString(data[data.length - 1]) + " ]";
		}
		throw new IllegalArgumentException("Unknown type");
	}

	public static String createJsonRecursive(Map<String, Object> objects) {
		StringBuilder builder = new StringBuilder(255);
		builder.append("{ ");
		builder.append("\"element\" : ");

		builder.append(objectToString(objects.get("element")));
		builder.append(", ");

		if (objects.containsKey(keySubelementName)) // compatibility with refinements
		{
			builder.append("\"");
			builder.append(keySubelementName);
			builder.append("\" : ");
			builder.append(objectToString(objects.get(keySubelementName)));
			builder.append(", ");
		}

		builder.append("\"elementPath\" : ");
		builder.append(objectToString(objects.get("elementPath")));
		builder.append(", \"operation\" : ");
		builder.append(objectToString(objects.get("operation")));
		builder.append(", \"dataType\" : ");
		builder.append(objectToString(objects.get("dataType")));
		builder.append(", \"data\" : ");
		builder.append(objectToString(objects.get("data")));
		builder.append(", \"children\" : ");
		List<Map<String, Object>> children = (List) objects.get("children");
		if (children == null) {
			builder.append("null");
		} else {
			builder.append("[ ");
			Iterator<Map<String, Object>> childIterator = children.iterator();
			while (childIterator.hasNext()) {
				builder.append(createJsonRecursive(childIterator.next()));
				if (childIterator.hasNext())
					builder.append(", ");
			}
			builder.append(" ]");
		}
		builder.append(" }");

		return builder.toString();
	}

	private Map<String, Object> parseObject() {
		Map<String, Object> result = new HashMap<>();
		String name = null;
		try {
			while (parser.next()) {
				switch (parser.getPositionType()) {
				case ObjectBegin:
					result.put(name, parseObject());
					break;
				case ObjectEnd:
					return result;
				case ListBegin:
					result.put(name, parseList(name));
					break;
				case JsonName:
					name = parser.getValue();
					name = name.substring(1, name.length() - 1);
					logging.debug(this, name);
					break;
				case JsonValue:
					result.put(name, stringToObject(parser.getValue(), name));
					break;
				default:
					throw new IllegalArgumentException("Type " + parser.getPositionType() + " not expected here");
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("IOException in parser", e);
		}
		throw new IllegalArgumentException("Reached EOF");
	}

	private Object parseList(String name) {
		List<Object> list = new LinkedList<>();
		boolean done = false;
		try {
			while (!done && parser.next()) {
				switch (parser.getPositionType()) {
				case ListEnd:
					done = true;
					break;
				case ObjectBegin:
					list.add(parseObject());
					break;
				case JsonValue:
					list.add(stringToObject(parser.getValue(), ""));
					break;
				default:
					throw new IllegalArgumentException("Type " + parser.getPositionType() + " not expected here");
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("IOException in parser", e);
		}
		logging.debug(this, "parseList " + list);
		if (!done)
			throw new IllegalArgumentException("Unexpected EOF");
		if (name.equals("elementPath"))
			return list.toArray(new String[0]);
		return list;
	}

	private Object stringToObject(String value, String name) {
		logging.debug(this, "stringToObject: " + name);
		if (value.equals("null"))
			return null;
		if (name.equals("data")) {
			value = value.substring(1, value.length() - 1);
			switch (lastDataType) {
			case NoneType:
				return null;
			case TextType:
			case EnumType:
				return value;
			case DoubleType:
				return Double.valueOf(value);
			case IntegerType:
				return Integer.valueOf(value);
			case BigIntegerType:
				return Long.valueOf(value);
			case DateType:
				return value;
			default:
				throw new IllegalArgumentException("Type " + lastDataType + " not expected here");
			}
		}
		if (value.startsWith("\""))
			return value.substring(1, value.length() - 1);
		if (name.equals("dataType")) {
			lastDataType = SelectData.DataType.valueOf(value);
			return lastDataType;
		}
		throw new IllegalArgumentException(value + " was not expected here");
	}

}
