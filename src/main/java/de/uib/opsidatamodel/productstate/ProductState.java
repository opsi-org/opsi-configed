package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class ProductState extends HashMap<String, String> {

	private static ProductState defaultProductState;

	public static final List<String> SERVICE_KEYS = List.of("modificationTime", "productId", "productVersion",
			"packageVersion", "targetConfiguration", "lastAction", "installationStatus", "actionRequest",
			"actionProgress", "actionResult", "priority", "actionSequence");

	public static final Map<String, String> DB_COLUMNS = new LinkedHashMap<>();
	static {
		DB_COLUMNS.put("productId", "VARCHAR(50)");
		DB_COLUMNS.put("productVersion", "VARCHAR(32)");
		DB_COLUMNS.put("packageVersion", "VARCHAR(16)");

		DB_COLUMNS.put("targetConfiguration", "VARCHAR(16)");
		DB_COLUMNS.put("lastAction", "VARCHAR(16)");
		DB_COLUMNS.put("installationStatus", "VARCHAR(16)");
		DB_COLUMNS.put("actionRequest", "VARCHAR(16)");
		DB_COLUMNS.put("actionProgress", "VARCHAR(255)");
		DB_COLUMNS.put("actionResult", "VARCHAR(16)");

		DB_COLUMNS.put("modificationTime", "TIMESTAMP");
	}

	public static final List<String> DB_COLUMN_NAMES = new ArrayList<>(DB_COLUMNS.keySet());

	public static final int COLUMN_INDEX_LAST_STATE_CHANGE = DB_COLUMN_NAMES.indexOf("modificationTime");

	// directly taken values
	public static final String KEY_LAST_STATE_CHANGE = "stateChange";
	public static final String KEY_PRODUCT_VERSION = "productVersion";
	public static final String KEY_PACKAGE_VERSION = "packageVersion";
	public static final String KEY_TARGET_CONFIGURATION = TargetConfiguration.KEY;
	public static final String KEY_LAST_ACTION = LastAction.KEY;
	public static final String KEY_INSTALLATION_STATUS = InstallationStatus.KEY;
	public static final String KEY_ACTION_REQUEST = ActionRequest.KEY;
	public static final String KEY_ACTION_PROGRESS = ActionProgress.KEY;
	public static final String KEY_ACTION_RESULT = ActionResult.KEY;
	public static final String KEY_PRODUCT_ID = "productId";

	public static final String KEY_PRODUCT_PRIORITY = "priority";
	public static final String KEY_ACTION_SEQUENCE = ActionSequence.KEY;

	// transformed values
	public static final String KEY_INSTALLATION_INFO = "installationInfo";
	public static final String KEY_VERSION_INFO = "versionInfo";

	// additional values
	public static final String KEY_POSITION = "position";
	public static final String KEY_PRODUCT_NAME = "productName";

	public static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(KEY_PRODUCT_ID);
		KEYS.add(KEY_PRODUCT_NAME);

		KEYS.add(KEY_TARGET_CONFIGURATION);
		KEYS.add(KEY_INSTALLATION_STATUS);

		KEYS.add(KEY_INSTALLATION_INFO);

		KEYS.add(KEY_ACTION_RESULT);
		KEYS.add(KEY_ACTION_PROGRESS);
		KEYS.add(KEY_LAST_ACTION);

		KEYS.add(KEY_PRODUCT_PRIORITY);
		KEYS.add(KEY_ACTION_SEQUENCE);
		KEYS.add(KEY_ACTION_REQUEST);

		KEYS.add(KEY_VERSION_INFO);

		KEYS.add(KEY_PRODUCT_VERSION);
		KEYS.add(KEY_PACKAGE_VERSION);

		KEYS.add(KEY_POSITION);

		KEYS.add(KEY_LAST_STATE_CHANGE);
	}

	public static final Map<String, String> key2servicekey = new HashMap<>();
	static {
		key2servicekey.put(KEY_PRODUCT_ID, "productId");

		key2servicekey.put(KEY_TARGET_CONFIGURATION, "targetConfiguration");
		key2servicekey.put(KEY_INSTALLATION_STATUS, "installationStatus");

		key2servicekey.put(KEY_ACTION_RESULT, "actionResult");
		key2servicekey.put(KEY_ACTION_PROGRESS, "actionProgress");
		key2servicekey.put(KEY_LAST_ACTION, "lastAction");

		key2servicekey.put(KEY_POSITION, "priority");
		key2servicekey.put(KEY_ACTION_SEQUENCE, "actionSequence");
		key2servicekey.put(KEY_ACTION_REQUEST, "actionRequest");

		key2servicekey.put(KEY_PRODUCT_VERSION, "productVersion");
		key2servicekey.put(KEY_PACKAGE_VERSION, "packageVersion");

		key2servicekey.put(KEY_LAST_STATE_CHANGE, "modificationTime");
	}

	protected final Map<String, String> retrieved;

	public ProductState(Map<String, String> retrievedState, boolean transform) {
		super();
		this.retrieved = retrievedState;
		if (retrieved == null) {
			setDefaultValues();
		} else {
			readRetrieved();
		}

		if (transform) {
			setTransforms();
		}
	}

	public ProductState(Map<String, String> retrievedState) {
		this(retrievedState, true);
	}

	public static ProductState getDefaultProductState() {
		if (defaultProductState == null) {
			defaultProductState = new ProductState(null);
		}

		return defaultProductState;
	}

	private void readRetrieved() {

		put(KEY_PRODUCT_ID, getRetrievedValue(key2servicekey.get(KEY_PRODUCT_ID)));

		put(KEY_TARGET_CONFIGURATION, getRetrievedValue(key2servicekey.get(KEY_TARGET_CONFIGURATION)));
		put(KEY_INSTALLATION_STATUS, getRetrievedValue(key2servicekey.get(KEY_INSTALLATION_STATUS)));

		put(KEY_ACTION_RESULT, getRetrievedValue(key2servicekey.get(KEY_ACTION_RESULT)));
		put(KEY_ACTION_PROGRESS, getRetrievedValue(key2servicekey.get(KEY_ACTION_PROGRESS)));
		put(KEY_LAST_ACTION, getRetrievedValue(key2servicekey.get(KEY_LAST_ACTION)));

		put(KEY_ACTION_REQUEST, getRetrievedValue(key2servicekey.get(KEY_ACTION_REQUEST)));

		put(KEY_PRODUCT_PRIORITY, getRetrievedValue(key2servicekey.get(KEY_POSITION)));
		put(KEY_ACTION_SEQUENCE, getRetrievedValue(key2servicekey.get(KEY_ACTION_SEQUENCE)));

		put(KEY_PRODUCT_VERSION, getRetrievedValue(key2servicekey.get(KEY_PRODUCT_VERSION)));
		put(KEY_PACKAGE_VERSION, getRetrievedValue(key2servicekey.get(KEY_PACKAGE_VERSION)));

		put(KEY_LAST_STATE_CHANGE, getRetrievedValue(key2servicekey.get(KEY_LAST_STATE_CHANGE)));
	}

	@Override
	public String put(String key, String value) {
		if (KEYS.indexOf(key) < 0) {
			Logging.error(this, "key " + key + " not known, value was " + value + " , " + KEYS);
			return null;
		} else {
			return super.put(key, value);
		}
	}

	private void setTransforms() {

		// transformed values
		StringBuilder installationInfo = new StringBuilder();
		// the reverse will be found in in setInstallationInfo in
		// InstallationStateTableModel

		LastAction lastAction = LastAction.produceFromLabel(get(KEY_LAST_ACTION));

		if (!get(KEY_ACTION_PROGRESS).isEmpty()) {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_ACTION_RESULT));
			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(": ");
			}

			installationInfo.append(get(KEY_ACTION_PROGRESS));
			installationInfo.append(" ( ");
			if (lastAction.getVal() > 0) {
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
			}

			installationInfo.append(" ) ");

			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(" ");
			}

		} else {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_ACTION_RESULT));
			if (result.getVal() == ActionResult.SUCCESSFUL || result.getVal() == ActionResult.FAILED) {
				installationInfo.append("");
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
			}
			// else

			if (lastAction.getVal() > 0) {
				installationInfo.append(" (");
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
				installationInfo.append(")");
			}
		}

		put(KEY_INSTALLATION_INFO, installationInfo.toString());

		String versionInfo = "";

		if (!get(KEY_PRODUCT_VERSION).isEmpty()) {
			versionInfo = get(KEY_PRODUCT_VERSION) + Globals.ProductPackageVersionSeparator.FOR_DISPLAY
					+ get(KEY_PACKAGE_VERSION);
		}

		put(KEY_VERSION_INFO, versionInfo);

	}

	private void setDefaultValues() {
		put(KEY_PRODUCT_ID, "");
		put(KEY_PRODUCT_NAME, "");

		put(KEY_TARGET_CONFIGURATION, TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED));
		put(KEY_INSTALLATION_STATUS, InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED));

		put(KEY_ACTION_RESULT, LastAction.getLabel(ActionResult.NONE));
		put(KEY_ACTION_PROGRESS, "");
		put(KEY_LAST_ACTION, LastAction.getLabel(LastAction.NONE));

		put(KEY_ACTION_REQUEST, ActionRequest.getLabel(ActionRequest.NONE));

		put(KEY_PRODUCT_PRIORITY, "");
		put(KEY_ACTION_SEQUENCE, "");

		put(KEY_PRODUCT_VERSION, "");
		put(KEY_PACKAGE_VERSION, "");

		put(KEY_LAST_STATE_CHANGE, "");

	}

	private String getRetrievedValue(String key) {

		if (SERVICE_KEYS.indexOf(key) < 0) {
			Logging.warning("service key " + key + " not known");
			return "";
		}

		if (retrieved.get(key) == null || (retrieved.get(key) instanceof String && "null".equals(retrieved.get(key)))) {
			return "";
		}

		return retrieved.get(key);
	}
}
