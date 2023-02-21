package de.uib.opsicommand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.uib.utilities.logging.Logging;

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

	@Override
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

		map = new HashMap<>();
		try {
			Iterator<String> iter = master.keys();

			if (!iter.hasNext()) {
				hasElements = false;
			}

			while (iter.hasNext()) {
				String key = iter.next();

				if (master.get(key) != null) {
					beingList = false;
				}

				if (master.get(key) == null) {
					beingMap = false;
				}

				if (!master.isNull(key)) {
					Object masterValue = master.get(key);

					if (masterValue instanceof java.lang.Boolean || masterValue instanceof java.lang.String
							|| masterValue instanceof java.lang.Integer) {
						map.put(key, masterValue);
					} else if (masterValue instanceof org.json.JSONArray) {
						map.put(key, ((JSONArray) masterValue).toList());
					} else if (masterValue instanceof org.json.JSONObject) {
						// should only occur on the last level
						map.put(key, masterValue);
					}
				}
			}
		} catch (Exception ex) {
			Logging.error(this, "json transform exception: " + ex);
		}
	}

	private void produceList() {
		if (master.names() != null) {
			list = master.names().toList();
		}
	}

	public void produceString() {
		if (list.isEmpty()) {
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
