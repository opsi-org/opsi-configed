package de.uib.opsidatamodel.dbtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Host extends Table {
	public static final String tableName = "HOST";
	public static final String idColumn = tableName + ".hostId";
	public static final String hwAddressColumn = tableName + ".hardwareAdress";
	public static final String descriptionColumn = tableName + ".description";

	public static List<String> columns;
	static {
		columns = new ArrayList<>();
		columns.add("hostId");
		columns.add("type");
		columns.add("description");
		columns.add("notes");
		columns.add("hardwareAddress");
		columns.add("ipAddress");
		columns.add("inventoryNumber");
		columns.add("created");
		columns.add("lastSeen");
		columns.add("opsiHostKey");
		columns.add("oneTimePassword");
		columns.add("maxBandwidth");
		columns.add("depotLocalUrl");
		columns.add("depotRemoteUrl");
		columns.add("depotWebdavUrl");
		columns.add("repositoryRemoteUrl");
		columns.add("isMasterDepot");
		columns.add("masterDepotId");
	}

	public static String columnsString;
	static {
		columnsString = Arrays.toString(columns.toArray(new String[] {}));
		columnsString = columnsString.substring(1);
		columnsString = columnsString.substring(0, columnsString.length() - 1);
	}

	public static String dbColumnsString;
	static {
		StringBuilder buf = new StringBuilder();
		for (String col : columns) {
			buf.append(tableName);
			buf.append(".");
			buf.append(col);
			buf.append(",");
		}
		dbColumnsString = buf.toString().substring(0, columnsString.length() - 1);
	}

	public static List<String> primaryKey;
	public static String primaryKeyString;
	static {
		primaryKey = new ArrayList<>();
		primaryKey.add("hostId");
		primaryKeyString = primaryKey.get(0);
	}

	private static Map<String, String> key2servicekey = new HashMap<>();
	static {
		for (String key : columns) {
			key2servicekey.put(key, key);
		}
		key2servicekey.put("hostId", "id");
	}
	public static de.uib.utilities.Mapping<String, String> serviceKeyMapping = new de.uib.utilities.Mapping(
			key2servicekey);

	public Host(String localTablePath) {
		super(localTablePath);
	}

	public static Map<java.lang.String, java.lang.Object> db2ServiceRowMap(
			Map<java.lang.String, java.lang.Object> map) {
		map.remove("ident");
		map.put("id", map.get("hostId"));
		map.remove("hostId");

		return map;
	}

}
