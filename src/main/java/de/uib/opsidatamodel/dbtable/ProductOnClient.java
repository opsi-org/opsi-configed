package de.uib.opsidatamodel.dbtable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.Mapping;

public class ProductOnClient extends Table {

	public static final String LOCALBOOT_ID = "LocalbootProduct";

	public static final String CLIENT_ID = "clientId";
	public static final String PRODUCT_ID = "productId";
	public static final String PRODUCT_TYPE = "productType";

	private static final String LOCAL_FILE_NAME = "productstates.configed";

	public static List<String> columns;
	static {
		columns = new ArrayList<>(ProductState.DB_COLUMN_NAMES);
		columns.add("clientId");
		columns.add("productType");
	}

	public static String columnsString;
	static {
		columnsString = Arrays.toString(columns.toArray(new String[] {}));
		columnsString = columnsString.substring(1);
		columnsString = columnsString.substring(0, columnsString.length() - 1);
	}

	public static List<String> primaryKey;
	public static String primaryKeyString;
	static {
		primaryKey = new ArrayList<>();
		primaryKey.add(CLIENT_ID);
		primaryKey.add(PRODUCT_ID);
		primaryKey.add(PRODUCT_TYPE);

		StringBuilder sb = new StringBuilder("");
		for (String key : primaryKey) {
			sb.append(key);
			sb.append(";");
		}
		primaryKeyString = sb.toString();
	}

	private static Map<String, String> key2servicekeyX = new HashMap<>(ProductState.key2servicekey);
	static {
		key2servicekeyX.put("clientId", "clientId");
	}
	public static Mapping<String, String> serviceKeyMapping = new Mapping<>(key2servicekeyX);

	public ProductOnClient(String localTablePath) {
		super(localTablePath);
		this.localTablePath = localTablePath + File.separator + LOCAL_FILE_NAME;
	}

	public boolean renew(boolean renew) {
		return renew;
	}

	public void create() {
	}

}
