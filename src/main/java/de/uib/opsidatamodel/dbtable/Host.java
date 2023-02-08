package de.uib.opsidatamodel.dbtable;

import java.util.Map;

// TODO what is the need of this class? only static values are accessed, never instanciated
public final class Host extends Table {
	public static final String TABLE_NAME = "HOST";
	public static final String ID_COLUMN = TABLE_NAME + ".hostId";
	public static final String HW_ADRESS_COLUMN = TABLE_NAME + ".hardwareAdress";
	public static final String DESCRIPTION_COLUMN = TABLE_NAME + ".description";

	private Host() {
		super("");
	}

	public static Map<java.lang.String, java.lang.Object> db2ServiceRowMap(
			Map<java.lang.String, java.lang.Object> map) {
		map.remove("ident");
		map.put("id", map.get("hostId"));
		map.remove("hostId");

		return map;
	}
}
