package de.uib.opsidatamodel.productstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.Globals;

public class ProductState extends HashMap<String, String> {

	private static ProductState DEFAULT;

	public static ProductState getDEFAULT() {
		if (DEFAULT == null)
			DEFAULT = new ProductState(null);
		return DEFAULT;
	}

	public static final List<String> SERVICE_KEYS = new ArrayList<>();
	static {// from 30_configed.conf
		SERVICE_KEYS.add("modificationTime");
		SERVICE_KEYS.add("productId");
		SERVICE_KEYS.add("productVersion");
		SERVICE_KEYS.add("packageVersion");
		SERVICE_KEYS.add("targetConfiguration");
		SERVICE_KEYS.add("lastAction");
		SERVICE_KEYS.add("installationStatus");
		SERVICE_KEYS.add("actionRequest");
		SERVICE_KEYS.add("actionProgress");
		SERVICE_KEYS.add("actionResult");
		SERVICE_KEYS.add("priority");
		SERVICE_KEYS.add("actionSequence");
	}

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

	public static final int columnIndexLastStateChange = DB_COLUMN_NAMES.indexOf("modificationTime");

	// directly taken values
	public static final String KEY_lastStateChange = "stateChange";
	public static final String KEY_productVersion = "productVersion";
	public static final String KEY_packageVersion = "packageVersion";
	public static final String KEY_targetConfiguration = TargetConfiguration.KEY;
	public static final String KEY_lastAction = LastAction.KEY;
	public static final String KEY_installationStatus = InstallationStatus.KEY;
	public static final String KEY_actionRequest = ActionRequest.KEY;
	public static final String KEY_actionProgress = ActionProgress.KEY;
	public static final String KEY_actionResult = ActionResult.KEY;
	public static final String KEY_productId = "productId";

	public static final String KEY_productPriority = "priority";
	public static final String KEY_actionSequence = ActionSequence.KEY;

	// transformed values
	public static final String KEY_installationInfo = InstallationInfo.KEY;
	public static final String KEY_versionInfo = "versionInfo";

	// additional values
	public static final String KEY_position = "position";
	public static final String KEY_productName = "productName";

	protected final Map retrieved;

	public static final List<String> KEYS = new ArrayList<>();
	static {
		KEYS.add(KEY_productId);
		KEYS.add(KEY_productName);

		KEYS.add(KEY_targetConfiguration);
		KEYS.add(KEY_installationStatus);

		KEYS.add(KEY_installationInfo);

		KEYS.add(KEY_actionResult);
		KEYS.add(KEY_actionProgress);
		KEYS.add(KEY_lastAction);

		KEYS.add(KEY_productPriority);
		KEYS.add(KEY_actionSequence);
		KEYS.add(KEY_actionRequest);

		KEYS.add(KEY_versionInfo);

		KEYS.add(KEY_productVersion);
		KEYS.add(KEY_packageVersion);

		KEYS.add(KEY_position);

		KEYS.add(KEY_lastStateChange);
	}

	public static final Map<String, String> key2servicekey = new HashMap<>();
	static {
		key2servicekey.put(KEY_productId, "productId");

		key2servicekey.put(KEY_targetConfiguration, "targetConfiguration");
		key2servicekey.put(KEY_installationStatus, "installationStatus");

		key2servicekey.put(KEY_actionResult, "actionResult");
		key2servicekey.put(KEY_actionProgress, "actionProgress");
		key2servicekey.put(KEY_lastAction, "lastAction");

		key2servicekey.put(KEY_position, "priority");
		key2servicekey.put(KEY_actionSequence, "actionSequence");
		key2servicekey.put(KEY_actionRequest, "actionRequest");

		key2servicekey.put(KEY_productVersion, "productVersion");
		key2servicekey.put(KEY_packageVersion, "packageVersion");

		key2servicekey.put(KEY_lastStateChange, "modificationTime");
	}

	private void readRetrieved() {

		put(KEY_productId, getRetrievedValue(key2servicekey.get(KEY_productId)));

		put(KEY_targetConfiguration, getRetrievedValue(key2servicekey.get(KEY_targetConfiguration)));
		put(KEY_installationStatus, getRetrievedValue(key2servicekey.get(KEY_installationStatus)));

		put(KEY_actionResult, getRetrievedValue(key2servicekey.get(KEY_actionResult)));
		put(KEY_actionProgress, getRetrievedValue(key2servicekey.get(KEY_actionProgress)));
		put(KEY_lastAction, getRetrievedValue(key2servicekey.get(KEY_lastAction)));

		put(KEY_actionRequest, getRetrievedValue(key2servicekey.get(KEY_actionRequest)));

		put(KEY_productPriority, getRetrievedValue(key2servicekey.get(KEY_position)));
		put(KEY_actionSequence, getRetrievedValue(key2servicekey.get(KEY_actionSequence)));

		put(KEY_productVersion, getRetrievedValue(key2servicekey.get(KEY_productVersion)));
		put(KEY_packageVersion, getRetrievedValue(key2servicekey.get(KEY_packageVersion)));

		put(KEY_lastStateChange, getRetrievedValue(key2servicekey.get(KEY_lastStateChange)));
	}

	public ProductState(Map retrievedState, boolean transform) {
		super();
		this.retrieved = retrievedState;
		if (retrieved == null) {
			setDefaultValues();
		} else {
			readRetrieved();
		}

		if (transform)
			setTransforms();
	}

	public ProductState(Map retrievedState) {
		this(retrievedState, true);
	}

	@Override
	public String put(String key, String value) {
		assert !(KEYS.indexOf(key) < 0) : "key " + key + " not known, value was " + value + " , " + KEYS;
		return super.put(key, value);
	}

	private void setTransforms() {

		// transformed values
		StringBuffer installationInfo = new StringBuffer();
		// the reverse will be found in in setInstallationInfo in
		// InstallationStateTableModel

		LastAction lastAction = LastAction.produceFromLabel(get(KEY_lastAction));

		if (!get(KEY_actionProgress).equals("")) {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_actionResult));
			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(": ");
			}
			installationInfo.append(get(KEY_actionProgress));
			installationInfo.append(" ( ");
			if (lastAction.getVal() > 0)
				installationInfo.append(ActionRequest.getDisplayLabel(lastAction.getVal()));
			installationInfo.append(" ) ");

			if (result.getVal() == ActionResult.FAILED) {
				installationInfo.append(ActionResult.getDisplayLabel(result.getVal()));
				installationInfo.append(" ");
			}

		} else {
			ActionResult result = ActionResult.produceFromLabel(get(KEY_actionResult));
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

		put(KEY_installationInfo, installationInfo.toString());

		String versionInfo = "";

		if (!get(KEY_productVersion).equals(""))
			versionInfo = get(KEY_productVersion) + Globals.ProductPackageVersionSeparator.forDisplay()
					+ get(KEY_packageVersion);

		put(KEY_versionInfo, versionInfo);

	}

	private void setDefaultValues() {
		put(KEY_productId, "");
		put(KEY_productName, "");

		put(KEY_targetConfiguration, TargetConfiguration.getLabel(TargetConfiguration.UNDEFINED));
		put(KEY_installationStatus, InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED));

		put(KEY_actionResult, LastAction.getLabel(ActionResult.NONE));
		put(KEY_actionProgress, "");
		put(KEY_lastAction, LastAction.getLabel(LastAction.NONE));

		put(KEY_actionRequest, ActionRequest.getLabel(ActionRequest.NONE));

		put(KEY_productPriority, "");
		put(KEY_actionSequence, "");

		put(KEY_productVersion, "");
		put(KEY_packageVersion, "");

		put(KEY_lastStateChange, "");

	}

	private String getRetrievedValue(String key) {

		assert !(SERVICE_KEYS.indexOf(key) < 0) : "service key " + key + " not known";

		if (retrieved.get(key) == null || (retrieved.get(key) instanceof String && retrieved.get(key).equals("null")))
			return "";

		String value = retrieved.get(key).toString();
		String predefValue = null;

		if (predefValue != null)
			return predefValue;

		return value;
	}

}
