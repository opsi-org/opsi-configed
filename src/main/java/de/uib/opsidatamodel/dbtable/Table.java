package de.uib.opsidatamodel.dbtable;

import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

import de.uib.utilities.logging.Logging;

public class Table {
	protected String highTimestampS = new Timestamp(0).toString();

	protected String localTablePath;
	protected String localFileName;

	public Table(String localTablePath) {
		this.localTablePath = localTablePath;
		new File(localTablePath).mkdirs();
	}

	public String getLocalFilePath() {
		return localTablePath;
	}

	public String getHighTimestamp() {

		return highTimestampS;
	}

	public void resetHighTimeStamp() {
		highTimestampS = new Timestamp(0).toString();
	}

	public void compareToHighTimestamp(String s) {
		if (s.compareTo(highTimestampS) > 0) {
			highTimestampS = s;
		}
	}

	protected String valueAssertion(String key, String value) {
		StringBuilder sb = new StringBuilder("");
		sb.append(key);
		sb.append("=");
		sb.append("'");
		if (value == null) {
			value = "";
		}
		sb.append(value);
		sb.append("'");

		return sb.toString();
	}

	protected String conjunction(Map<String, String> rowMap, String[] keys) {
		Logging.debug(this, "conjunction keys " + Arrays.toString(keys));
		if (keys == null || keys.length == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder("(");
		sb.append(valueAssertion(keys[0], rowMap.get(keys[0])));

		for (int i = 1; i < keys.length; i++) {
			sb.append(" and ");
			sb.append(valueAssertion(keys[i], rowMap.get(keys[i])));
		}

		sb.append(")");

		Logging.debug(this, "conjunction " + sb.toString());

		return sb.toString();
	}
}
