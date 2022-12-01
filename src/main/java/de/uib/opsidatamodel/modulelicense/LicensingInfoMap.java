package de.uib.opsidatamodel.modulelicense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import de.uib.configed.configed;
import de.uib.opsicommand.JSONObjectX;
import de.uib.utilities.logging.logging;

public class LicensingInfoMap {

	private final static String CLASSNAME = LicensingInfoMap.class.getName();

	public final static String OPSI_LICENSING_INFO_VERSION_OLD = "";
	public final static String OPSI_LICENSING_INFO_VERSION = "2";
	public final static String DISPLAY_INFINITE = "\u221E";
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private static Boolean reducedView = !FGeneralDialogLicensingInfo.extendedView;
	private JSONObject jOResult;
	Map<String, List<Object>> configs;
	private Map<String, Object> clientNumbersMap;
	private Vector<Vector<String>> clientNumbersVector;
	private Set customerIDs;
	private Set customerNames;
	private Map<String, Map<String, Object>> licenses;
	private Vector<String> availableModules;
	private Vector<String> knownModulesVector;
	private Vector<String> obsoleteModules;
	private Vector<String> shownModules;
	private ArrayList<String> datesKeys;
	private Map<String, Map<String, Map<String, Object>>> datesMap;
	private Vector<String> columnNames;
	private Vector<String> classNames;
	private Map<String, Map> tableMap;
	private String latestDateString;
	private String checksum;
	private Boolean closeToLimitWarning = false;
	private Boolean overLimitWarning = false;
	private ArrayList<String> currentCloseToLimitModuleList;
	private ArrayList<String> currentOverLimitModuleList;
	private ArrayList<String> currentTimeWarningModuleList;
	private ArrayList<String> futureOverLimitModuleList;
	private ArrayList<String> futureCloseToLimitModuleList;
	private Set<String> allCloseToLimitModules;
	private Set<String> allOverLimitModules;
	private Integer daysClientLimitWarning;
	private Integer absolutClientLimitWarning;
	private Integer percentClientLimitWarning;
	private Vector<String> disabledWarningModules;

	public static final String RESULT = "result";
	public static final String CLIENT_NUMBERS_INFO = "client_numbers";
	public static final String ALL = "all";
	public static final String MAC_OS = "macos";
	public static final String LINUX = "linux";
	public static final String WINDOWS = "windows";
	public static final String KNOWN_MODULES = "known_modules";
	public static final String AVAILABLE_MODULES = "available_modules";
	public static final String OBSOLETE_MODULES = "obsolete_modules";
	public static final String LICENSES = "licenses";
	public static final String CUSTOMER_ID = "customer_id";
	public static final String CUSTOMER_NAME = "customer_name";
	public static final String CUSTOMER_UNIT = "customer_unit";
	public static final String ID = "id";
	public static final String MODULE_ID = "module_id";
	public static final String VALID_UNTIL = "valid_until";
	public static final String REVOKED_IDS = "revoked_ids";
	public static final String CHECKSUM = "licenses_checksum";
	public static final String DATES = "dates";
	public static final String MODULES = "modules";
	public static final String AVAILABLE = "available";
	public static final String LICENSE_IDS = "license_ids";
	public static final String STATE = "state";
	public static final String STATE_UNLICENSED = "unlicensed";
	public static final String STATE_CLOSE_TO_LIMIT = "close_to_limit";
	public static final String STATE_OVER_LIMIT = "over_limit";
	public static final String STATE_FUTURE_OKAY = "future_okay";
	public static final String STATE_DAYS_WARNING = "days_warning";
	public static final String STATE_DAYS_OKAY = "days_okay";
	public static final String STATE_IGNORE_WARNING = "ignore_warning";
	public static final String CLIENT_NUMBER = "client_number";
	public static final String UNLIMITED_NUMBER = "999999999";
	public static final String CURRENT_OVER_LIMIT = "current_over_limit";
	public static final String CURRENT_CLOSE_TO_LIMIT = "current_close_to_limit";
	public static final String CURRENT_TIME_WARNINGS = "current_time_warnings";
	public static final String FUTURE_OVER_LIMIT = "future_over_limit";
	public static final String FUTURE_CLOSE_TO_LIMIT = "future_close_to_limit";
	public static final String FUTURE_STATE = "future_state";
	public static final String CONFIG = "config";
	public static final String DISABLE_WARNING_FOR_MODULES = "disable_warning_for_modules";
	public static final String CLIENT_LIMIT_WARNING_PERCENT = "client_limit_warning_percent";
	public static final String CLIENT_LIMIT_WARNING_ABSOLUTE = "client_limit_warning_absolute";
	public static final String CLIENT_LIMIT_WARNING_DAYS = "client_limit_warning_days";
	public static final String MODULE_LINUX_AGENT = "linux_agent";
	public static final String MODULE_MACOS_AGENT = "macos_agent";

	public static final String CONFIG_KEY = "licensing";

	private boolean checked = false;
	private static LicensingInfoMap instance;
	private static LicensingInfoMap instanceComplete;
	private static LicensingInfoMap instanceReduced;

	public static LicensingInfoMap getInstance(JSONObject jsonObj, Map<String, List<Object>> configVals,
			Boolean reduced) {
		logging.info("reduced, instance here " + reduced + ", " + instance);

		if (instance == null || instanceComplete == null || instanceReduced == null) {
			instanceComplete = new LicensingInfoMap(jsonObj, configVals, false);
			instanceReduced = new LicensingInfoMap(jsonObj, configVals, true);
		}

		if (reduced) {
			instance = instanceReduced;
		} else {
			instance = instanceComplete;
		}

		return instance;

	}

	public static void setReduced(boolean reduced) {
		logging.info("setReduced instanceReduced " + instanceReduced + " cols " + instanceReduced.getColumnNames());
		logging.info("setReduced instanceComplete " + instanceComplete + " cols " + instanceComplete.getColumnNames());

		reducedView = reduced;
		if (reduced) {
			instance = instanceReduced;
		} else {
			instance = instanceComplete;
		}
	}

	public static LicensingInfoMap getInstance(JSONObject jsonObj, Map<String, List<Object>> configVals) {
		logging.info("instance here reducedView " + reducedView + ", instance " + instance);
		if (instance == null) {
			instance = getInstance(jsonObj, configVals, reducedView);
		}

		return instance;
	}

	public static LicensingInfoMap getInstance() {
		if (instance == null)
			logging.error(CLASSNAME + " instance  not initialized");

		return instance;
	}

	public static void requestRefresh() {
		instance = null;
	}

	private LicensingInfoMap(JSONObject jsonObj, Map<String, List<Object>> configVals, Boolean reduced) {
		logging.info(this, "generate with reducedView " + reduced + " at the moment ignored, we set false");
		reducedView = reduced;

		try {
			jOResult = jsonObj.getJSONObject(RESULT);
		} catch (Exception ex) {
			logging.error(CLASSNAME + " constructor " + ex);
		}
		datesKeys = new ArrayList<String>();
		configs = configVals;
		produceConfigs();
		checksum = produceChecksum();
		clientNumbersMap = produceClientNumbersMap();
		clientNumbersVector = produceVectorFromClientNumbersMap();
		licenses = produceLicenses();
		obsoleteModules = produceObsoleteModules();
		availableModules = produceAvailableModules();
		knownModulesVector = produceKnownModules();
		shownModules = produceShownModules();
		datesKeys = produceDatesKeys();
		latestDateString = findLatestChangeDateString();
		datesMap = produceDatesMap();
		tableMap = produceTableMapFromDatesMap(datesMap);
		customerIDs = produceCustomerIDSet();
		customerNames = produceCustomerNameSet();

		instance = this;

	}

	private Map<String, Object> produceClientNumbersMap() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		try {
			JSONObject client = jOResult.getJSONObject(CLIENT_NUMBERS_INFO);
			JSONObjectX clientX = new JSONObjectX(client);

			if (!clientX.isMap()) {
				logging.error(CLASSNAME + " map expected " + clientX);
			} else {
				logging.debug(CLASSNAME + " map retrieved");

				result = (HashMap<String, Object>) clientX.getMap();
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + " getClientNumersMap : " + ex.toString());
		}

		return result;
	}

	private Vector<Vector<String>> produceVectorFromClientNumbersMap() {
		Vector<Vector<String>> result = new Vector<>();

		for (Map.Entry<String, Object> entry : clientNumbersMap.entrySet()) {
			Vector<String> line = new Vector<>();
			line.add(entry.getKey());
			line.add(entry.getValue().toString());

			result.add(line);
		}
		Vector<String> line1 = new Vector<>();
		line1.add(CHECKSUM);
		line1.add(checksum);
		result.add(line1);

		return result;
	}

	private Map<String, Map<String, Object>> produceLicenses() {
		Map<String, Map<String, Object>> result = new HashMap<>();

		try {
			JSONArray licenses = jOResult.getJSONArray(LICENSES);
			for (int i = 0; i < licenses.length(); i++) {
				Map<String, Object> tmp = new HashMap<>();
				JSONObject obj = licenses.getJSONObject(i);

				tmp.put(MODULE_ID, obj.get(MODULE_ID));
				tmp.put(VALID_UNTIL, obj.get(VALID_UNTIL));
				tmp.put(REVOKED_IDS, obj.get(REVOKED_IDS));

				result.put(obj.get(ID).toString(), tmp);
			}

		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceLicenses " + ex.toString());
		}
		// logging.info( " licenses result: " + result);
		return result;
	}

	private Set produceCustomerIDSet() {
		Set<String> customerIDs = new LinkedHashSet<>();

		try {
			JSONArray licenses = jOResult.getJSONArray(LICENSES);

			for (int i = 0; i < licenses.length(); i++) {
				JSONObject l = licenses.getJSONObject(i);
				customerIDs.add(l.getString(CUSTOMER_ID));
			}

		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceCustomerIdSet " + ex.toString());
		}

		return customerIDs;
	}

	private Set produceCustomerNameSet() {
		Set<String> customerNames = new LinkedHashSet<>();

		try {

			JSONArray licenses = jOResult.getJSONArray(LICENSES);

			for (int i = 0; i < licenses.length(); i++) {
				JSONObject l = licenses.getJSONObject(i);
				String customerName = l.getString(CUSTOMER_NAME);
				if (!l.getString(CUSTOMER_UNIT).equals("null"))
					customerNames.add(customerName + " - " + l.getString(CUSTOMER_UNIT));
				else
					customerNames.add(customerName);

			}

		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceCustomerIdSet " + ex.toString());
		}

		return customerNames;
	}

	private Vector<String> produceAvailableModules() {
		Vector<String> result = new Vector<>();
		JSONArray jsResult = new JSONArray();

		try {

			jsResult = jOResult.getJSONArray(AVAILABLE_MODULES);

			for (int i = 0; i < jsResult.length(); i++) {
				result.add(jsResult.getString(i));
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + " getAvailableModules : " + ex);
		}

		Collections.sort(result);
		return result;

	}

	private Vector<String> produceKnownModules() {
		JSONArray jsResult = new JSONArray();
		Vector<String> result = new Vector<>();

		try {
			if (jOResult.has(KNOWN_MODULES)) {

				jsResult = jOResult.getJSONArray(KNOWN_MODULES);

				for (int i = 0; i < jsResult.length(); i++) {
					result.add(jsResult.getString(i));
				}
			} else {
				result = availableModules;
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceKnownModules " + ex);
		}

		Collections.sort(result);
		return result;
	}

	private Vector<String> produceObsoleteModules() {
		JSONArray jsResult = new JSONArray();
		Vector<String> result = new Vector<>();

		try {
			if (jOResult.has(OBSOLETE_MODULES)) {

				jsResult = jOResult.getJSONArray(OBSOLETE_MODULES);

				for (int i = 0; i < jsResult.length(); i++) {
					result.add(jsResult.getString(i));
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceObsoleteModules " + ex);
		}

		Collections.sort(result);
		return result;
	}

	private Vector<String> produceShownModules() {
		if (!jOResult.has(OBSOLETE_MODULES))
			return produceKnownModules();

		Vector<String> result = new Vector<>();

		for (String mod : knownModulesVector) {
			if (!obsoleteModules.contains(mod))
				result.add(mod);
		}

		//
		Collections.sort(result);
		return result;
	}

	private void produceConfigs() {

		try {
			if (jOResult.has(CONFIG)) {
				JSONObject config = new JSONObject();

				config = jOResult.getJSONObject(CONFIG);

				percentClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_PERCENT);
				absolutClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_ABSOLUTE);
				daysClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_DAYS);

				JSONArray tmp = config.getJSONArray(DISABLE_WARNING_FOR_MODULES);
				Vector<String> result = new Vector<>();

				for (int i = 0; i < tmp.length(); i++) {
					result.add(tmp.getString(i));
				}
				disabledWarningModules = result;

			} else {
				String key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_ABSOLUTE;
				if (configs.get(key) != null)
					absolutClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				else
					absolutClientLimitWarning = 5;

				key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_PERCENT;

				if (configs.get(key) != null)
					percentClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				else
					percentClientLimitWarning = 95;

				key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_ABSOLUTE;

				if (configs.get(key) != null)
					daysClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				else
					daysClientLimitWarning = 30;
			}

		} catch (Exception ex) {
			logging.error(this, " produceConfigs " + ex);
		}
	}

	private String produceChecksum() {
		String checksum = new String();

		try {
			checksum = jOResult.getString(CHECKSUM);
			if (jOResult == null)
				checksum = jOResult.get("licenses_checksum").toString();
		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceChecksum : " + ex);
		}

		return checksum;
	}

	private ArrayList<String> produceDatesKeys() {
		ArrayList<String> dates = new ArrayList<String>();

		try {
			JSONObject jsonDates = jOResult.getJSONObject(DATES);
			JSONObjectX datesX = new JSONObjectX(jsonDates);
			Map<String, Object> datesM = new HashMap<String, Object>();

			datesM = (Map<String, Object>) datesX.getMap();

			for (Map.Entry<String, Object> entry : datesM.entrySet()) {
				dates.add(entry.getKey());
			}
			Collections.sort(dates);

			// logging.info(this, " dates keys: " + dates);

			Date latest = findLatestChangeDate(dates);

			ArrayList<String> reducedDatesKeys = new ArrayList<String>();

			if (reducedView) {

				for (String key : dates) {
					if ((sdf.parse(key)).compareTo(latest) >= 0)
						reducedDatesKeys.add(key);
				}
				// if(reducedDatesKeys != null)
				dates = reducedDatesKeys;
			}

		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceDatesKeys : " + ex.toString() + ", ");
		}
		// logging.info(this, " dates keys: " + dates);
		return dates;
	}

	private Map<String, Map<String, Map<String, Object>>> produceDatesMap() {

		if (currentCloseToLimitModuleList == null)
			currentCloseToLimitModuleList = new ArrayList<>();
		if (currentOverLimitModuleList == null)
			currentOverLimitModuleList = new ArrayList<>();
		if (currentTimeWarningModuleList == null)
			currentTimeWarningModuleList = new ArrayList<>();
		if (futureCloseToLimitModuleList == null)
			futureCloseToLimitModuleList = new ArrayList<>();
		if (futureOverLimitModuleList == null)
			futureOverLimitModuleList = new ArrayList<>();
		if (allCloseToLimitModules == null)
			allCloseToLimitModules = new LinkedHashSet<String>();
		if (allOverLimitModules == null)
			allOverLimitModules = new LinkedHashSet<String>();

		Map<String, Map<String, Map<String, Object>>> resultMap = new HashMap<String, Map<String, Map<String, Object>>>();

		try {
			JSONObject modulesJSOb;
			JSONObjectX modulesJSObX;
			JSONObject dates = jOResult.getJSONObject(DATES);

			for (String key : datesKeys) {
				Map<String, Object> moduleToDate = new HashMap<String, Object>();
				Map<String, Map<String, Object>> modulesMapToDate = new HashMap<>();

				// iterate over date entries
				modulesJSOb = dates.getJSONObject(key).getJSONObject(MODULES);
				modulesJSObX = new JSONObjectX(modulesJSOb);
				moduleToDate = modulesJSObX.getMap();
				// iterate over module entries to every date entry
				// new: iterate over all known modules and fill empty ones with 0
				// also warning state should be none
				for (String currentModule : shownModules) {
					// JSONObject moduleInfo = (JSONObject) moduleToDate.get(currentModule);

					JSONObject moduleInfo;
					boolean available = availableModules.contains(currentModule);

					if (moduleToDate.containsKey(currentModule)) {
						moduleInfo = (JSONObject) moduleToDate.get(currentModule);
						if (disabledWarningModules != null && disabledWarningModules.contains(currentModule))
							moduleInfo.put(STATE, STATE_IGNORE_WARNING);
					} else {
						moduleInfo = new JSONObject();
						moduleInfo.put(CLIENT_NUMBER, "0");
						moduleInfo.put(LICENSE_IDS, "[]");
						moduleInfo.put(STATE, "unlicensed");
					}

					moduleInfo.put(AVAILABLE, available);
					JSONObjectX tmp = new JSONObjectX(moduleInfo);
					if (((String) moduleInfo.get(STATE)).equals(STATE_CLOSE_TO_LIMIT)) {
						allCloseToLimitModules.add(currentModule);

						if (key.equals(latestDateString))
							currentCloseToLimitModuleList.add(currentModule);

					} else if (((String) moduleInfo.get(STATE)).equals(STATE_OVER_LIMIT))

					{
						allOverLimitModules.add(currentModule);

						if (key.equals(latestDateString))
							currentOverLimitModuleList.add(currentModule);
					}

					else if (key.equals(getLatestDate())
							&& checkTimeLeft(tmp.getMap(), currentModule).equals(STATE_DAYS_WARNING)) {
						moduleInfo.put(STATE, STATE_DAYS_WARNING);
						currentTimeWarningModuleList.add(currentModule);
					}

					String futureCheck = checkFuture(tmp.getMap(), currentModule, key);
					if (futureCheck != null && !moduleInfo.getString(STATE).equals(STATE_IGNORE_WARNING)) {
						moduleInfo.put(FUTURE_STATE, futureCheck);

						if (futureCheck.equals(STATE_OVER_LIMIT))
							futureOverLimitModuleList.add(currentModule);
						else if (futureCheck.equals(STATE_CLOSE_TO_LIMIT))
							futureCloseToLimitModuleList.add(currentModule);
					} else
						moduleInfo.put(FUTURE_STATE, "null");

					JSONObjectX moduleInfoX = new JSONObjectX(moduleInfo);

					modulesMapToDate.put((String) currentModule, moduleInfoX.getMap());

				}
				resultMap.put(key, new TreeMap<>(modulesMapToDate));

			}

		} catch (Exception ex) {
			logging.error(CLASSNAME + " produceDatesMap : " + ex.toString() + ", ");
		}

		return new TreeMap<>(checkTimeWarning(resultMap));
	}

	/**
	 * transforms datesMap to be able to use in a table, with dates as columns
	 * and modules as rows
	 */
	private Map<String, Map> produceTableMapFromDatesMap(Map<String, Map<String, Map<String, Object>>> datesM) {
		Map<String, Map> resultMap = new HashMap<String, Map>();

		columnNames = new Vector<String>();
		columnNames.add(configed.getResourceValue("LicensingInfo.modules"));
		columnNames.add(configed.getResourceValue("LicensingInfo.available"));
		// columnNames.add(configed.getResourceValue("LicensingInfo.info"));

		classNames = new Vector<String>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		// classNames.add("java.lang.String");

		try {

			for (Map.Entry<String, Map<String, Map<String, Object>>> date : datesM.entrySet()) {
				columnNames.add(date.getKey());
				classNames.add("java.lang.String");
			}

			for (String currentModule : shownModules) {
				Map<String, Object> line = new HashMap<String, Object>();
				// String currentModule = availableModules.get(i).toString();

				// 1st column
				line.put(configed.getResourceValue("LicensingInfo.modules"), currentModule);

				// 2nd column
				// line.put(configed.getResourceValue("LicensingInfo.info"),
				// configed.getResourceValue("LicensingInfo.info"));

				// 3rd column
				line.put(configed.getResourceValue("LicensingInfo.available"),
						availableModules.contains(currentModule));

				// rest columns
				for (Map.Entry<String, Map<String, Map<String, Object>>> date : datesM.entrySet()) {
					line.put(date.getKey(), date.getValue().get(currentModule).get(CLIENT_NUMBER).toString());
				}

				resultMap.put(currentModule, line);
			}
		} catch (Exception ex) {
			logging.error(CLASSNAME + "getTableMapFromDatesMap() " + ex);
		}

		return new TreeMap<>(resultMap);
	}

	/**
	 * gets the date with the currently active licenses
	 */
	private String findLatestChangeDateString() {
		String newest = null;
		try {
			LocalDate now = LocalDate.now();
			// Date dateNow = sdf.parse("2022-04-06");
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			for (String key : datesKeys) {
				// logging.debug("key " + key);
				Date thisDate = sdf.parse(key);
				if (dateNow.compareTo(thisDate) >= 0)
					newest = key;

				else
					break;

			}
		} catch (ParseException ex) {
			logging.error(CLASSNAME + " getCurrentlyActiveLicense " + ex);
		}

		return newest;
	}

	private Date findLatestChangeDate(ArrayList<String> dates) {
		Date newest = new Date();
		try {
			LocalDate now = LocalDate.now();
			// Date dateNow = sdf.parse("2022-04-06");
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			for (String key : dates) {
				// logging.debug("key " + key);
				Date thisDate = sdf.parse(key);
				if (dateNow.compareTo(thisDate) >= 0)
					newest = thisDate;

				else
					break;

			}
		} catch (ParseException ex) {
			logging.error(CLASSNAME + " findLatestChangeDate" + ex);
		}

		return newest;
	}

	private String findNextChangeDate() {
		try {
			Date latest = sdf.parse(latestDateString);
			for (String key : datesKeys) {
				Date thisDate = sdf.parse(key);
				if (thisDate.compareTo(latest) > 0)
					return key;
			}
		} catch (ParseException ex) {
			logging.error(CLASSNAME + " findNextChangeDate " + ex);
		}

		return null;
	}

	private Long getDaysLeftUntil(String d) {
		try {
			LocalDate now = LocalDate.now();
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
			// dateNow = sdf.parse("2023-05-01");
			// Date nextChange = sdf.parse(findNextChangeDate());
			Date date = sdf.parse(d);

			long diffInMillies = Math.abs(date.getTime() - dateNow.getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			return diff;
		} catch (ParseException ex) {
			logging.error(CLASSNAME + " getDaysLeftUntilNextChange " + ex);
		}

		return null;
	}

	private String checkTimeLeft(Map<String, Object> moduleInfo, String module) {
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER)
				&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {

			ArrayList lics = (ArrayList) moduleInfo.get(LICENSE_IDS);
			String validUntil;

			for (int i = 0; i < lics.size(); i++) {

				validUntil = licenses.get(lics.get(i)).get(VALID_UNTIL).toString();

				Long timeLeft = getDaysLeftUntil(validUntil);

				if (timeLeft <= daysClientLimitWarning)
					return STATE_DAYS_WARNING;
			}
		}

		return STATE_DAYS_OKAY;

	}

	/**
	 * @param moduleInfo
	 * @param date
	 * @return
	 */

	private String checkFuture(Map<String, Object> moduleInfo, String module, String date) {
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER)) {

			if (date.equals(findNextChangeDate())) {
				String state = moduleInfo.get(STATE).toString();

				if (!state.equals(STATE_UNLICENSED)) {
					String cNum;
					String fNum;

					if (module.equals(MODULE_MACOS_AGENT)) {
						cNum = clientNumbersMap.get(MAC_OS).toString();

					} else if (module.equals(MODULE_LINUX_AGENT)) {

						cNum = clientNumbersMap.get(LINUX).toString();
					} else {

						cNum = clientNumbersMap.get(ALL).toString();

					}

					fNum = moduleInfo.get(CLIENT_NUMBER).toString();

					Integer futureNum = Integer.parseInt(fNum);
					Integer clientNum = Integer.parseInt(cNum);

					Integer diff = futureNum - clientNum;
					Long daysLeft = getDaysLeftUntil(findNextChangeDate());

					if (diff < 0)
						return STATE_OVER_LIMIT;

					if (diff <= absolutClientLimitWarning
							| (futureNum != 0 && (clientNum * 100) / futureNum >= percentClientLimitWarning))
						return STATE_CLOSE_TO_LIMIT;

					/*
					 * if(daysLeft <= daysClientLimitWarning)
					 * return STATE_DAYS_WARNING;
					 */

					return STATE_FUTURE_OKAY;
				}

			}
		}

		return null;
	}

	private Map<String, Map<String, Map<String, Object>>> checkTimeWarning(
			Map<String, Map<String, Map<String, Object>>> map) {

		Map<String, Map<String, Map<String, Object>>> resultMap = map;

		if (resultMap.get(latestDateString) != null) {
			for (Map.Entry<String, Map<String, Object>> mod : resultMap.get(latestDateString).entrySet()) {
				Map<String, Object> val = mod.getValue();
				String modKey = mod.getKey();

				if (val.get(STATE).toString().equals(STATE_DAYS_WARNING)) {
					if (resultMap.get(findNextChangeDate()).get(modKey).get(FUTURE_STATE).toString()
							.equals(STATE_FUTURE_OKAY)) {
						val.put(STATE, STATE_DAYS_OKAY);
						currentTimeWarningModuleList.remove(modKey);
					}

				}
			}
		}

		return resultMap;

	}

	public void setWarningLevelViaConfig(Map<String, List<Object>> configs) {
		String key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_ABSOLUTE;
		if (configs.get(key) != null)
			absolutClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));

		key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_PERCENT;
		if (configs.get(key) != null)
			percentClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));

		// logging.debug("warning levels: " + absolutClientLimitWarning + ", " +
		// percentClientLimitWarning);
	}

	public ArrayList<String> getCloseToLimitModuleList() {
		return currentCloseToLimitModuleList;
	}

	public ArrayList<String> getCurrentOverLimitModuleList() {
		return currentOverLimitModuleList;
	}

	public ArrayList<String> getCurrentDaysWarningModuleList() {
		return currentTimeWarningModuleList;
	}

	/**
	 * @return list of modules for every possible warning state (4)
	 */
	public Map<String, ArrayList<String>> getWarnings() {
		Map<String, ArrayList<String>> result = new HashMap<>();

		if (currentCloseToLimitModuleList.size() == 0 && currentOverLimitModuleList.size() == 0
				&& currentTimeWarningModuleList.size() == 0 && futureCloseToLimitModuleList.size() == 0
				&& futureOverLimitModuleList.size() == 0)
			return null;

		result.put(CURRENT_OVER_LIMIT, currentOverLimitModuleList);
		result.put(CURRENT_CLOSE_TO_LIMIT, currentCloseToLimitModuleList);
		result.put(CURRENT_TIME_WARNINGS, currentTimeWarningModuleList);
		result.put(FUTURE_OVER_LIMIT, futureOverLimitModuleList);
		result.put(FUTURE_CLOSE_TO_LIMIT, futureCloseToLimitModuleList);

		// System.exit(0);
		return result;
	}

	public boolean warningExists() {
		logging.info(this, "warning exists? ");

		boolean result = false;

		logging.info(this, "warnings currentOverLimitModuleList? " + currentOverLimitModuleList.size());
		logging.info(this, "warnings currentCloseToLimitModuleList? " + currentCloseToLimitModuleList.size());
		logging.info(this, "warnings currentTimeWarningModuleList? " + currentTimeWarningModuleList.size());
		logging.info(this, "warnings futureOverLimitModuleList? " + futureOverLimitModuleList.size());
		logging.info(this, "warnings futureOverLimitModuleList? " + futureOverLimitModuleList.size());

		result = currentOverLimitModuleList.size() > 0 || currentCloseToLimitModuleList.size() > 0
				|| currentTimeWarningModuleList.size() > 0
		// ||
		// futureOverLimitModuleList.size() > 0
		// ||
		// futureOverLimitModuleList.size() > 0
		;

		logging.info(this, "warning exists " + result);

		return result;
	}

	public Set<String> getModulesListWithCloseToLimitWarnings() {
		return allCloseToLimitModules;
	}

	public Set<String> getModulesListWithOverLimitWarnings() {
		return allOverLimitModules;
	}

	public String getLatestDate() {
		return latestDateString;
	}

	public Map<String, Object> getClientNumbersMap() {
		return clientNumbersMap;
	}

	public Vector<Vector<String>> getClientNumbersVector() {
		return clientNumbersVector;
	}

	public Set getCustomerIDSet() {
		return customerIDs;
	}

	public Set getCustomerNamesSet() {
		return customerNames;
	}

	public Map<String, Map> getTableMap() {
		return tableMap;
	}

	public Vector<String> getColumnNames() {
		return columnNames;
	}

	public Vector<String> getClassNames() {
		return classNames;
	}

	public Map<String, Map<String, Map<String, Object>>> getDatesMap() {
		return datesMap;
	}

	public Vector<String> getModules() {
		return shownModules;
	}

	public Vector<String> getAvailableModules() {
		return availableModules;
	}

	public String getCheckSum() {
		return checksum;
	}

	public Integer getClientLimitWarningAbsolute() {
		return absolutClientLimitWarning;
	}

	public Integer getClientLimitWarningPercent() {
		return percentClientLimitWarning;
	}

	public Integer getClientLimitWarningDays() {
		return daysClientLimitWarning;
	}

}