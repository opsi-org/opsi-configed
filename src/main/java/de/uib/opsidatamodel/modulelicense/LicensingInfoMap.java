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
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import de.uib.configed.Configed;
import de.uib.opsicommand.JSONObjectX;
import de.uib.utilities.logging.Logging;

public class LicensingInfoMap {

	private static final String CLASSNAME = LicensingInfoMap.class.getName();

	public static final String OPSI_LICENSING_INFO_VERSION_OLD = "";
	public static final String OPSI_LICENSING_INFO_VERSION = "2";
	public static final String DISPLAY_INFINITE = "\u221E";
	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private static Boolean reducedView = !FGeneralDialogLicensingInfo.extendedView;
	private JSONObject jOResult;
	Map<String, List<Object>> configs;
	private Map<String, Object> clientNumbersMap;
	private List<List<String>> clientNumbersList;
	private Set<String> customerIDs;
	private Set<String> customerNames;
	private Map<String, Map<String, Object>> licenses;
	private List<String> availableModules;
	private List<String> knownModulesList;
	private List<String> obsoleteModules;
	private List<String> shownModules;
	private List<String> datesKeys;
	private Map<String, Map<String, Map<String, Object>>> datesMap;
	private List<String> columnNames;
	private List<String> classNames;
	private Map<String, Map> tableMap;
	private String latestDateString;
	private String checksum;
	private List<String> currentCloseToLimitModuleList;
	private List<String> currentOverLimitModuleList;
	private List<String> currentTimeWarningModuleList;
	private List<String> futureOverLimitModuleList;
	private List<String> futureCloseToLimitModuleList;
	private Set<String> allCloseToLimitModules;
	private Set<String> allOverLimitModules;
	private Integer daysClientLimitWarning;
	private Integer absolutClientLimitWarning;
	private Integer percentClientLimitWarning;
	private List<String> disabledWarningModules;

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

	private static LicensingInfoMap instance;
	private static LicensingInfoMap instanceComplete;
	private static LicensingInfoMap instanceReduced;

	public static LicensingInfoMap getInstance(JSONObject jsonObj, Map<String, List<Object>> configVals,
			Boolean reduced) {
		Logging.info("reduced, instance here " + reduced + ", " + instance);

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
		Logging.info("setReduced instanceReduced " + instanceReduced + " cols " + instanceReduced.getColumnNames());
		Logging.info("setReduced instanceComplete " + instanceComplete + " cols " + instanceComplete.getColumnNames());

		reducedView = reduced;
		if (reduced) {
			instance = instanceReduced;
		} else {
			instance = instanceComplete;
		}
	}

	public static LicensingInfoMap getInstance(JSONObject jsonObj, Map<String, List<Object>> configVals) {
		Logging.info("instance here reducedView " + reducedView + ", instance " + instance);
		if (instance == null) {
			instance = getInstance(jsonObj, configVals, reducedView);
		}

		return instance;
	}

	public static LicensingInfoMap getInstance() {
		if (instance == null)
			Logging.error(CLASSNAME + " instance  not initialized");

		return instance;
	}

	public static void requestRefresh() {
		instance = null;
	}

	private LicensingInfoMap(JSONObject jsonObj, Map<String, List<Object>> configVals, Boolean reduced) {
		Logging.info(this, "generate with reducedView " + reduced + " at the moment ignored, we set false");
		reducedView = reduced;

		try {
			jOResult = jsonObj.getJSONObject(RESULT);
		} catch (Exception ex) {
			Logging.error(CLASSNAME + " constructor " + ex);
		}
		datesKeys = new ArrayList<>();
		configs = configVals;
		produceConfigs();
		checksum = produceChecksum();
		clientNumbersMap = produceClientNumbersMap();
		clientNumbersList = produceListFromClientNumbersMap();
		licenses = produceLicenses();
		obsoleteModules = produceObsoleteModules();
		availableModules = produceAvailableModules();
		knownModulesList = produceKnownModules();
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
		Map<String, Object> result = new HashMap<>();
		try {
			JSONObject client = jOResult.getJSONObject(CLIENT_NUMBERS_INFO);
			JSONObjectX clientX = new JSONObjectX(client);

			if (!clientX.isMap()) {
				Logging.error(CLASSNAME + " map expected " + clientX);
			} else {
				Logging.debug(CLASSNAME + " map retrieved");

				result = clientX.getMap();
			}
		} catch (Exception ex) {
			Logging.error(CLASSNAME + " getClientNumersMap : " + ex.toString());
		}

		return result;
	}

	private List<List<String>> produceListFromClientNumbersMap() {
		List<List<String>> result = new ArrayList<>();

		for (Map.Entry<String, Object> entry : clientNumbersMap.entrySet()) {
			List<String> line = new ArrayList<>();
			line.add(entry.getKey());
			line.add(entry.getValue().toString());

			result.add(line);
		}
		List<String> line1 = new ArrayList<>();
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
			Logging.error(CLASSNAME + " produceLicenses " + ex.toString());
		}

		return result;
	}

	private Set<String> produceCustomerIDSet() {
		Set<String> customerIDs = new LinkedHashSet<>();

		try {
			JSONArray licenses = jOResult.getJSONArray(LICENSES);

			for (int i = 0; i < licenses.length(); i++) {
				JSONObject l = licenses.getJSONObject(i);
				customerIDs.add(l.get(CUSTOMER_ID).toString());
			}

		} catch (Exception ex) {
			Logging.error(CLASSNAME + " produceCustomerIdSet " + ex.toString());
		}

		return customerIDs;
	}

	private Set<String> produceCustomerNameSet() {
		Set<String> customerNames = new LinkedHashSet<>();

		try {

			JSONArray licenses = jOResult.getJSONArray(LICENSES);

			for (int i = 0; i < licenses.length(); i++) {
				JSONObject l = licenses.getJSONObject(i);
				String customerName = l.getString(CUSTOMER_NAME);
				if (!l.get(CUSTOMER_UNIT).toString().equals("null"))
					customerNames.add(customerName + " - " + l.get(CUSTOMER_UNIT).toString());
				else
					customerNames.add(customerName);

			}

		} catch (Exception ex) {
			Logging.error(CLASSNAME + " produceCustomerIdSet " + ex.toString());
		}

		return customerNames;
	}

	private List<String> produceAvailableModules() {
		List<String> result = new ArrayList<>();
		JSONArray jsResult = new JSONArray();

		try {

			jsResult = jOResult.getJSONArray(AVAILABLE_MODULES);

			for (int i = 0; i < jsResult.length(); i++) {
				result.add(jsResult.getString(i));
			}
		} catch (Exception ex) {
			Logging.error(CLASSNAME + " getAvailableModules : " + ex);
		}

		Collections.sort(result);
		return result;

	}

	private List<String> produceKnownModules() {
		JSONArray jsResult = new JSONArray();
		List<String> result = new ArrayList<>();

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
			Logging.error(CLASSNAME + " produceKnownModules " + ex);
		}

		Collections.sort(result);
		return result;
	}

	private List<String> produceObsoleteModules() {
		JSONArray jsResult = new JSONArray();
		List<String> result = new ArrayList<>();

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
			Logging.error(CLASSNAME + " produceObsoleteModules " + ex);
		}

		Collections.sort(result);
		return result;
	}

	private List<String> produceShownModules() {
		if (!jOResult.has(OBSOLETE_MODULES))
			return produceKnownModules();

		List<String> result = new ArrayList<>();

		for (String mod : knownModulesList) {
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

				JSONObject config = jOResult.getJSONObject(CONFIG);

				percentClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_PERCENT);
				absolutClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_ABSOLUTE);
				daysClientLimitWarning = config.getInt(CLIENT_LIMIT_WARNING_DAYS);

				JSONArray tmp = config.getJSONArray(DISABLE_WARNING_FOR_MODULES);
				List<String> result = new ArrayList<>();

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
			Logging.error(this, " produceConfigs " + ex);
		}
	}

	private String produceChecksum() {
		String checksum = "";

		try {
			checksum = jOResult.getString(CHECKSUM);
			checksum = jOResult.get("licenses_checksum").toString();
		} catch (Exception ex) {
			Logging.error(CLASSNAME + " produceChecksum : " + ex);
		}

		return checksum;
	}

	private List<String> produceDatesKeys() {
		List<String> dates = new ArrayList<>();

		try {
			JSONObject jsonDates = jOResult.getJSONObject(DATES);
			JSONObjectX datesX = new JSONObjectX(jsonDates);

			Map<String, Object> datesM = datesX.getMap();

			for (Map.Entry<String, Object> entry : datesM.entrySet()) {
				dates.add(entry.getKey());
			}
			Collections.sort(dates);

			Date latest = findLatestChangeDate(dates);

			List<String> reducedDatesKeys = new ArrayList<>();

			if (reducedView) {

				for (String key : dates) {
					if ((sdf.parse(key)).compareTo(latest) >= 0)
						reducedDatesKeys.add(key);
				}

				dates = reducedDatesKeys;
			}

		} catch (Exception ex) {
			Logging.error(CLASSNAME + " produceDatesKeys : " + ex.toString() + ", ");
		}

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
			allCloseToLimitModules = new LinkedHashSet<>();
		if (allOverLimitModules == null)
			allOverLimitModules = new LinkedHashSet<>();

		Map<String, Map<String, Map<String, Object>>> resultMap = new HashMap<>();

		try {
			JSONObject modulesJSOb;
			JSONObjectX modulesJSObX;
			JSONObject dates = jOResult.getJSONObject(DATES);

			for (String key : datesKeys) {
				Map<String, Object> moduleToDate;
				Map<String, Map<String, Object>> modulesMapToDate = new HashMap<>();

				// iterate over date entries
				modulesJSOb = dates.getJSONObject(key).getJSONObject(MODULES);
				modulesJSObX = new JSONObjectX(modulesJSOb);
				moduleToDate = modulesJSObX.getMap();
				// iterate over module entries to every date entry

				// also warning state should be none
				for (String currentModule : shownModules) {

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
			Logging.error(CLASSNAME + " produceDatesMap : " + ex.toString() + ", ");
		}

		return new TreeMap<>(checkTimeWarning(resultMap));
	}

	/**
	 * transforms datesMap to be able to use in a table, with dates as columns
	 * and modules as rows
	 */
	private Map<String, Map> produceTableMapFromDatesMap(Map<String, Map<String, Map<String, Object>>> datesM) {
		Map<String, Map> resultMap = new HashMap<>();

		columnNames = new ArrayList<>();
		columnNames.add(Configed.getResourceValue("LicensingInfo.modules"));
		columnNames.add(Configed.getResourceValue("LicensingInfo.available"));

		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");

		try {

			for (Map.Entry<String, Map<String, Map<String, Object>>> date : datesM.entrySet()) {
				columnNames.add(date.getKey());
				classNames.add("java.lang.String");
			}

			for (String currentModule : shownModules) {
				Map<String, Object> line = new HashMap<>();

				// 1st column
				line.put(Configed.getResourceValue("LicensingInfo.modules"), currentModule);

				// 2nd column

				// 3rd column
				line.put(Configed.getResourceValue("LicensingInfo.available"),
						availableModules.contains(currentModule));

				// rest columns
				for (Map.Entry<String, Map<String, Map<String, Object>>> date : datesM.entrySet()) {
					line.put(date.getKey(), date.getValue().get(currentModule).get(CLIENT_NUMBER).toString());
				}

				resultMap.put(currentModule, line);
			}
		} catch (Exception ex) {
			Logging.error(CLASSNAME + "getTableMapFromDatesMap() " + ex);
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

			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			for (String key : datesKeys) {

				Date thisDate = sdf.parse(key);
				if (dateNow.compareTo(thisDate) >= 0)
					newest = key;

				else
					break;

			}
		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " getCurrentlyActiveLicense " + ex);
		}

		return newest;
	}

	private Date findLatestChangeDate(List<String> dates) {
		Date newest = new Date();
		try {
			LocalDate now = LocalDate.now();

			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			for (String key : dates) {

				Date thisDate = sdf.parse(key);
				if (dateNow.compareTo(thisDate) >= 0)
					newest = thisDate;

				else
					break;

			}
		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " findLatestChangeDate" + ex);
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
			Logging.error(CLASSNAME + " findNextChangeDate " + ex);
		}

		return null;
	}

	private Long getDaysLeftUntil(String d) {
		try {
			LocalDate now = LocalDate.now();
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			Date date = sdf.parse(d);

			long diffInMillies = Math.abs(date.getTime() - dateNow.getTime());
			long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

			return diff;
		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " getDaysLeftUntilNextChange " + ex);
		}

		return null;
	}

	private String checkTimeLeft(Map<String, Object> moduleInfo, String module) {
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER)
				&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {

			List lics = (List) moduleInfo.get(LICENSE_IDS);
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
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER) && date.equals(findNextChangeDate())) {
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

				if (diff < 0)
					return STATE_OVER_LIMIT;

				if (diff <= absolutClientLimitWarning
						| (futureNum != 0 && (clientNum * 100) / futureNum >= percentClientLimitWarning))
					return STATE_CLOSE_TO_LIMIT;

				return STATE_FUTURE_OKAY;
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

				if (val.get(STATE).toString().equals(STATE_DAYS_WARNING) && resultMap.get(findNextChangeDate())
						.get(modKey).get(FUTURE_STATE).toString().equals(STATE_FUTURE_OKAY)) {
					val.put(STATE, STATE_DAYS_OKAY);
					currentTimeWarningModuleList.remove(modKey);
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

	}

	public List<String> getCloseToLimitModuleList() {
		return currentCloseToLimitModuleList;
	}

	public List<String> getCurrentOverLimitModuleList() {
		return currentOverLimitModuleList;
	}

	public List<String> getCurrentDaysWarningModuleList() {
		return currentTimeWarningModuleList;
	}

	/**
	 * @return list of modules for every possible warning state (4)
	 */
	public Map<String, List<String>> getWarnings() {
		Map<String, List<String>> result = new HashMap<>();

		if (currentCloseToLimitModuleList.isEmpty() && currentOverLimitModuleList.isEmpty()
				&& currentTimeWarningModuleList.isEmpty() && futureCloseToLimitModuleList.isEmpty()
				&& futureOverLimitModuleList.isEmpty())
			return null;

		result.put(CURRENT_OVER_LIMIT, currentOverLimitModuleList);
		result.put(CURRENT_CLOSE_TO_LIMIT, currentCloseToLimitModuleList);
		result.put(CURRENT_TIME_WARNINGS, currentTimeWarningModuleList);
		result.put(FUTURE_OVER_LIMIT, futureOverLimitModuleList);
		result.put(FUTURE_CLOSE_TO_LIMIT, futureCloseToLimitModuleList);

		return result;
	}

	public boolean warningExists() {
		Logging.info(this, "warning exists? ");

		boolean result = false;

		Logging.info(this, "warnings currentOverLimitModuleList? " + currentOverLimitModuleList.size());
		Logging.info(this, "warnings currentCloseToLimitModuleList? " + currentCloseToLimitModuleList.size());
		Logging.info(this, "warnings currentTimeWarningModuleList? " + currentTimeWarningModuleList.size());
		Logging.info(this, "warnings futureOverLimitModuleList? " + futureOverLimitModuleList.size());
		Logging.info(this, "warnings futureOverLimitModuleList? " + futureOverLimitModuleList.size());

		result = !currentOverLimitModuleList.isEmpty() || !currentCloseToLimitModuleList.isEmpty()
				|| !currentTimeWarningModuleList.isEmpty();

		Logging.info(this, "warning exists " + result);

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

	public List<List<String>> getClientNumbersList() {
		return clientNumbersList;
	}

	public Set<String> getCustomerIDSet() {
		return customerIDs;
	}

	public Set<String> getCustomerNamesSet() {
		return customerNames;
	}

	public Map<String, Map> getTableMap() {
		return tableMap;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public List<String> getClassNames() {
		return classNames;
	}

	public Map<String, Map<String, Map<String, Object>>> getDatesMap() {
		return datesMap;
	}

	public List<String> getModules() {
		return shownModules;
	}

	public List<String> getAvailableModules() {
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