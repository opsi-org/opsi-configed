package de.uib.opsicommand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.uib.utilities.logging.logging;

public class JSONObjectX extends JSONObject {
	private JSONObject master;

	private Map<String, Object> map;
	private List<Object> list;
	private String value = "";

	private boolean beingMap = false;
	private boolean beingList = false;
	private boolean beingString = false;
	private boolean hasElements = false;

	public JSONObjectX(JSONObject jO) {
		master = jO;
		produceList();
		produceMap();
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public boolean isEmpty() {
		return !hasElements;
	}

	public boolean isMap() {
		return beingMap;
	}

	public boolean isList() {
		return beingList;
	}

	public boolean isString() {
		return beingString;
	}

	private void produceMap() {
		beingMap = true;
		beingList = true;
		hasElements = true;

		map = new HashMap<String, Object>();
		try {
			Iterator iter = master.keys();
			// logging.debug(this, "keys " + master.keys());
			if (!iter.hasNext()) {
				hasElements = false;
			}

			while (iter.hasNext()) {
				String key = (String) iter.next();
				// logging.debug(this, "got key " +key);

				if (master.get(key) != null)
					beingList = false;

				if (master.get(key) == null)
					beingMap = false;

				if (!master.isNull(key)) {
					Object value = master.get(key);

					if (value instanceof java.lang.Boolean || value instanceof java.lang.String
							|| value instanceof java.lang.Integer) {
						map.put(key, value);
					} else if (value instanceof org.json.JSONArray) {
						map.put(key, ((JSONArray) value).toList());
					} else if (value instanceof org.json.JSONObject) {
						map.put(key, value); // should only occur on the last level
					}
				}
			}
		} catch (Exception ex) {
			logging.error(this, "json transform exception: " + ex);
		}
	}

	private void produceList() {
		if (master.names() != null)
			list = master.names().toList();
	}

	public List getList() {
		return list;
	}

	public void produceString() {
		if (list.size() == 0) {
			value = "";
			beingString = true;
		}

		else if (list.size() == 1 && !beingMap) {
			value = list.get(1).toString();
			beingString = true;
		}

		else {
			value = master.toString();
			beingString = false;
		}
	}

	public String getString() {
		return value;
	}
}
