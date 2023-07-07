/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.modulelicense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.opsicommand.POJOReMapper;
import de.uib.utilities.logging.Logging;

public final class LicensingInfoMap {

	public static final String RESULT = "result";
	public static final String CLIENT_NUMBERS_INFO = "client_numbers";
	public static final String ALL = "all";
	public static final String MAC_OS = "macos";
	public static final String LINUX = "linux";
	public static final String WINDOWS = "windows";
	public static final String KNOWN_MODULES = "known_modules";
	public static final String AVAILABLE_MODULES = "available_modules";
	public static final String OBSOLETE_MODULES = "obsolete_modules";
	public static final String LICENSES_ID = "licenses";
	public static final String CUSTOMER_ID = "customer_id";
	public static final String CUSTOMER_NAME = "customer_name";
	public static final String CUSTOMER_UNIT = "customer_unit";
	public static final String ID = "id";
	public static final String MODULE_ID = "module_id";
	public static final String VALID_UNTIL = "valid_until";
	public static final String REVOKED_IDS = "revoked_ids";
	public static final String CHECKSUM_ID = "licenses_checksum";
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
	public static final String STATE_DAYS_OVER = "days_over";
	public static final String STATE_DAYS_OKAY = "days_okay";
	public static final String STATE_IGNORE_WARNING = "ignore_warning";
	public static final String STATE_OKAY = "state_okay";
	public static final String CLIENT_NUMBER = "client_number";
	public static final String UNLIMITED_NUMBER = "999999999";
	public static final String FUTURE_STATE = "future_state";
	public static final String CONFIG = "config";
	public static final String DISABLE_WARNING_FOR_MODULES = "disable_warning_for_modules";
	public static final String CLIENT_LIMIT_WARNING_PERCENT = "client_limit_warning_percent";
	public static final String CLIENT_LIMIT_WARNING_ABSOLUTE = "client_limit_warning_absolute";
	public static final String CLIENT_LIMIT_WARNING_DAYS = "client_limit_warning_days";
	public static final String MODULE_LINUX_AGENT = "linux_agent";
	public static final String MODULE_MACOS_AGENT = "macos_agent";

	public static final String CONFIG_KEY = "licensing";

	public static final int CLIENT_LIMIT_WARNING_PERCENT_DEFAULT = 95;
	public static final int CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT = 5;
	public static final int CLIENT_LIMIT_WARNING_DAYS_DEFAULT = 30;

	private static final String CLASSNAME = LicensingInfoMap.class.getName();

	public static final String DISPLAY_INFINITE = "\u221E";

	private static LicensingInfoMap instance;
	private static LicensingInfoMap instanceComplete;
	private static LicensingInfoMap instanceReduced;

	private static boolean reducedView = !FGeneralDialogLicensingInfo.extendedView;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Map<String, Object> jOResult;
	private Map<String, List<Object>> configs;
	private Map<String, Object> clientNumbersMap;
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
	private Map<String, Map<String, Object>> tableMap;
	private String latestDateString;
	private String checksum;
	private Set<String> currentCloseToLimitModuleList;
	private Set<String> currentOverLimitModuleList;
	private Set<String> currentTimeWarningModuleList;
	private Set<String> currentTimeOverModuleList;
	private Integer daysClientLimitWarning;
	private Integer absolutClientLimitWarning;
	private Integer percentClientLimitWarning;
	private List<String> disabledWarningModules;

	private LicensingInfoMap(Map<String, Object> jsonObj, Map<String, List<Object>> configVals, Boolean reduced) {
		Logging.info(CLASSNAME + "generate with reducedView " + reduced + " at the moment ignored, we set false");
		reducedView = reduced;

		jOResult = POJOReMapper.remap(jsonObj.get(RESULT), new TypeReference<Map<String, Object>>() {
		});

		configs = configVals;
		produceConfigs();
		checksum = produceChecksum();
		clientNumbersMap = produceClientNumbersMap();
		licenses = produceLicenses();
		obsoleteModules = produceObsoleteModules();
		availableModules = produceAvailableModules();
		knownModulesList = produceKnownModules();
		shownModules = produceShownModules();

		datesKeys = produceDatesKeys();
		latestDateString = findLatestChangeDateString();
		datesMap = produceDatesMap();
		tableMap = produceTableMapFromDatesMap(datesMap);
		customerNames = produceCustomerNameSet();

		instance = this;
	}

	public static LicensingInfoMap getInstance(Map<String, Object> jsonObj, Map<String, List<Object>> configVals,
			boolean reduced) {
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

	public static LicensingInfoMap getInstance() {
		if (instance == null) {
			Logging.error(CLASSNAME + " instance  not initialized");
		}

		return instance;
	}

	public static void requestRefresh() {
		instance = null;
	}

	private Map<String, Object> produceClientNumbersMap() {
		return POJOReMapper.remap(jOResult.get(CLIENT_NUMBERS_INFO), new TypeReference<Map<String, Object>>() {
		});
	}

	private Map<String, Map<String, Object>> produceLicenses() {
		Map<String, Map<String, Object>> result = new HashMap<>();

		List<Object> producedLicences = POJOReMapper.remap(jOResult.get(LICENSES_ID),
				new TypeReference<List<Object>>() {
				});

		for (Object producedLicence : producedLicences) {
			Map<String, Object> tmp = new HashMap<>();
			Map<String, Object> originalMap = POJOReMapper.remap(producedLicence,
					new TypeReference<Map<String, Object>>() {
					});

			tmp.put(MODULE_ID, originalMap.get(MODULE_ID));
			tmp.put(VALID_UNTIL, originalMap.get(VALID_UNTIL));
			tmp.put(REVOKED_IDS, originalMap.get(REVOKED_IDS));

			result.put(originalMap.get(ID).toString(), tmp);
		}
		return result;
	}

	private Set<String> produceCustomerNameSet() {
		Set<String> producedCustomerNames = new LinkedHashSet<>();

		List<Object> producedLicences = POJOReMapper.remap(jOResult.get(LICENSES_ID),
				new TypeReference<List<Object>>() {
				});

		for (Object producedLicence : producedLicences) {
			Map<String, Object> originalMap = POJOReMapper.remap(producedLicence,
					new TypeReference<Map<String, Object>>() {
					});
			String customerName = String.valueOf(originalMap.get(CUSTOMER_NAME));

			if (originalMap.get(CUSTOMER_UNIT) != null) {
				producedCustomerNames.add(customerName + " - " + originalMap.get(CUSTOMER_UNIT).toString());
			} else {
				producedCustomerNames.add(customerName);
			}
		}

		return producedCustomerNames;
	}

	private List<String> produceAvailableModules() {
		List<String> result = POJOReMapper.remap(jOResult.get(AVAILABLE_MODULES), new TypeReference<List<String>>() {
		});
		Collections.sort(result);

		return result;

	}

	private List<String> produceKnownModules() {
		List<String> result = availableModules;

		if (jOResult.containsKey(KNOWN_MODULES)) {
			result = POJOReMapper.remap(jOResult.get(KNOWN_MODULES), new TypeReference<List<String>>() {
			});
		}

		Collections.sort(result);
		return result;
	}

	private List<String> produceObsoleteModules() {
		List<String> result = new ArrayList<>();

		if (jOResult.containsKey(OBSOLETE_MODULES)) {

			result = POJOReMapper.remap(jOResult.get(OBSOLETE_MODULES), new TypeReference<List<String>>() {
			});
		}

		Collections.sort(result);
		return result;
	}

	private List<String> produceShownModules() {
		if (!jOResult.containsKey(OBSOLETE_MODULES)) {
			return produceKnownModules();
		}

		List<String> result = new ArrayList<>();

		for (String mod : knownModulesList) {
			if (!obsoleteModules.contains(mod)) {
				result.add(mod);
			}
		}

		Collections.sort(result);
		return result;
	}

	private void produceConfigs() {
		try {
			if (jOResult.containsKey(CONFIG)) {
				Map<String, Object> config = POJOReMapper.remap(jOResult.get(CONFIG),
						new TypeReference<Map<String, Object>>() {
						});

				percentClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_PERCENT).toString());
				absolutClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_ABSOLUTE).toString());
				daysClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_DAYS).toString());
				disabledWarningModules = POJOReMapper.remap(config.get(DISABLE_WARNING_FOR_MODULES),
						new TypeReference<List<String>>() {
						});
			} else {
				String key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_PERCENT;

				if (configs.get(key) != null) {
					percentClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				} else {
					percentClientLimitWarning = CLIENT_LIMIT_WARNING_PERCENT_DEFAULT;
				}

				key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_ABSOLUTE;

				if (configs.get(key) != null) {
					absolutClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				} else {
					absolutClientLimitWarning = CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT;
				}

				key = CONFIG_KEY + "." + CLIENT_LIMIT_WARNING_DAYS;

				if (configs.get(key) != null) {
					daysClientLimitWarning = Integer.parseInt((String) configs.get(key).get(0));
				} else {
					daysClientLimitWarning = CLIENT_LIMIT_WARNING_DAYS_DEFAULT;
				}
			}

		} catch (NumberFormatException ex) {
			Logging.error(this, " produceConfigs ", ex);
		}
	}

	private String produceChecksum() {
		String newChecksum = "";

		if (jOResult.containsKey(CHECKSUM_ID) && jOResult.get(CHECKSUM_ID) != null) {
			newChecksum = jOResult.get(CHECKSUM_ID).toString();
		}

		return newChecksum;
	}

	private List<String> produceDatesKeys() {
		List<String> dates = new ArrayList<>();

		Map<String, Object> datesM = POJOReMapper.remap(jOResult.get(DATES), new TypeReference<Map<String, Object>>() {
		});

		for (Map.Entry<String, Object> entry : datesM.entrySet()) {
			dates.add(entry.getKey());
		}
		Collections.sort(dates);

		LocalDate latest = findLatestChangeDate(dates);

		List<String> reducedDatesKeys = new ArrayList<>();

		if (reducedView) {
			for (String key : dates) {
				if ((LocalDate.parse(key)).compareTo(latest) >= 0) {
					reducedDatesKeys.add(key);
				}
			}

			dates = reducedDatesKeys;
		}

		return dates;
	}

	private Map<String, Map<String, Map<String, Object>>> produceDatesMap() {
		if (currentCloseToLimitModuleList == null) {
			currentCloseToLimitModuleList = new HashSet<>();
		}

		if (currentOverLimitModuleList == null) {
			currentOverLimitModuleList = new HashSet<>();
		}

		if (currentTimeWarningModuleList == null) {
			currentTimeWarningModuleList = new HashSet<>();
		}

		if (currentTimeOverModuleList == null) {
			currentTimeOverModuleList = new HashSet<>();
		}

		Map<String, Map<String, Map<String, Object>>> resultMap = new HashMap<>();

		try {
			Map<String, Map<String, Map<String, Object>>> dates = POJOReMapper.remap(jOResult.get(DATES),
					new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {
					});

			for (String key : datesKeys) {
				Map<String, Object> moduleToDate;
				Map<String, Map<String, Object>> modulesMapToDate = new HashMap<>();

				// iterate over date entries
				moduleToDate = POJOReMapper.remap(dates.get(key).get(MODULES),
						new TypeReference<Map<String, Object>>() {
						});
				// iterate over module entries to every date entry

				// also warning state should be none
				for (String currentModule : shownModules) {
					Map<String, Object> moduleInfo;
					boolean available = availableModules.contains(currentModule);

					if (moduleToDate.containsKey(currentModule)) {
						moduleInfo = POJOReMapper.remap(moduleToDate.get(currentModule),
								new TypeReference<Map<String, Object>>() {
								});
						if (disabledWarningModules != null && disabledWarningModules.contains(currentModule)) {
							moduleInfo.put(STATE, STATE_IGNORE_WARNING);
						}
					} else {
						moduleInfo = new HashMap<>();
						moduleInfo.put(CLIENT_NUMBER, "0");
						moduleInfo.put(LICENSE_IDS, "[]");
						moduleInfo.put(STATE, "unlicensed");
					}

					moduleInfo.put(AVAILABLE, available);
					if (((String) moduleInfo.get(STATE)).equals(STATE_CLOSE_TO_LIMIT)) {

						if (key.equals(latestDateString)) {
							currentCloseToLimitModuleList.add(currentModule);
						}

					} else if (((String) moduleInfo.get(STATE)).equals(STATE_OVER_LIMIT)) {
						if (key.equals(latestDateString)) {
							currentOverLimitModuleList.add(currentModule);
						}
					} else if (key.equals(getLatestDate()) && checkTimeLeft(moduleInfo).equals(STATE_DAYS_WARNING)) {
						moduleInfo.put(STATE, STATE_DAYS_WARNING);
						currentTimeWarningModuleList.add(currentModule);
					} else if (key.equals(getLatestDate()) && checkTimeLeft(moduleInfo).equals(STATE_DAYS_OVER)) {
						moduleInfo.put(STATE, STATE_DAYS_OVER);
						currentTimeOverModuleList.add(currentModule);
					} else {
						// no warnings to add
					}

					String futureCheck = checkFuture(moduleInfo, currentModule, key);
					if (futureCheck != null && moduleInfo.get(STATE) != null
							&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {
						moduleInfo.put(FUTURE_STATE, futureCheck);

					} else {
						moduleInfo.put(FUTURE_STATE, "null");
					}

					modulesMapToDate.put(currentModule, moduleInfo);

				}
				resultMap.put(key, new TreeMap<>(modulesMapToDate));

			}

		} catch (JSONException ex) {
			Logging.error(CLASSNAME + " json exception in produceDatesMap ", ex);
		}

		return new TreeMap<>(checkTimeWarning(resultMap));
	}

	/**
	 * transforms datesMap to be able to use in a table, with dates as columns
	 * and modules as rows
	 */
	private Map<String, Map<String, Object>> produceTableMapFromDatesMap(
			Map<String, Map<String, Map<String, Object>>> datesM) {
		Map<String, Map<String, Object>> resultMap = new HashMap<>();

		columnNames = new ArrayList<>();
		columnNames.add(Configed.getResourceValue("LicensingInfo.modules"));
		columnNames.add(Configed.getResourceValue("LicensingInfo.available"));

		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.Boolean");

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
			line.put(Configed.getResourceValue("LicensingInfo.available"), availableModules.contains(currentModule));

			// rest columns
			for (Map.Entry<String, Map<String, Map<String, Object>>> date : datesM.entrySet()) {
				line.put(date.getKey(), date.getValue().get(currentModule).get(CLIENT_NUMBER).toString());
			}

			resultMap.put(currentModule, line);
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
				if (dateNow.compareTo(thisDate) >= 0) {
					newest = key;
				} else {
					break;
				}

			}
		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " getCurrentlyActiveLicense " + ex);
		}

		return newest;
	}

	private static LocalDate findLatestChangeDate(List<String> dates) {
		LocalDate newest = LocalDate.now();

		LocalDate now = LocalDate.now();

		for (String key : dates) {

			LocalDate thisDate = LocalDate.parse(key);
			if (now.compareTo(thisDate) >= 0) {
				newest = thisDate;
			} else {
				break;
			}
		}

		return newest;
	}

	private String findNextChangeDate() {
		try {
			Date latest = sdf.parse(latestDateString);
			for (String key : datesKeys) {
				Date thisDate = sdf.parse(key);
				if (thisDate.compareTo(latest) > 0) {
					return key;
				}
			}
		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " findNextChangeDate ", ex);
		}

		return null;
	}

	private Long getDaysLeftUntil(String d) {
		try {
			LocalDate now = LocalDate.now();
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			Date date = sdf.parse(d);

			long diffInMillies = Math.abs(date.getTime() - dateNow.getTime());

			return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

		} catch (ParseException ex) {
			Logging.error(CLASSNAME + " getDaysLeftUntilNextChange ", ex);
		}

		return null;
	}

	private String checkTimeLeft(Map<String, Object> moduleInfo) {
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER)
				&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {

			List<?> lics = (List<?>) moduleInfo.get(LICENSE_IDS);
			String validUntil;
			LocalDate now = LocalDate.now();
			Date dateNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

			try {
				for (int i = 0; i < lics.size(); i++) {
					validUntil = licenses.get(lics.get(i)).get(VALID_UNTIL).toString();

					if (dateNow.after(sdf.parse(validUntil))) {
						return STATE_DAYS_OVER;
					}

					Long timeLeft = getDaysLeftUntil(validUntil);

					if (timeLeft <= daysClientLimitWarning) {
						return STATE_DAYS_WARNING;
					}

				}
			} catch (ParseException ex) {
				Logging.error(CLASSNAME + " checkTimeLeft ", ex);
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

				return calculateStateForNumbers(clientNum, futureNum);
			}
		}

		return null;
	}

	private String calculateStateForNumbers(int clientNum, int futureNum) {
		Integer diff = futureNum - clientNum;

		if (diff < 0) {
			return STATE_OVER_LIMIT;
		}

		if (diff <= absolutClientLimitWarning
				|| (futureNum != 0 && clientNum * 100 / futureNum >= percentClientLimitWarning)) {
			return STATE_CLOSE_TO_LIMIT;
		}

		return STATE_FUTURE_OKAY;
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
					currentTimeOverModuleList.remove(modKey);
				}
			}
		}

		return resultMap;

	}

	public Set<String> getCurrentOverLimitModuleList() {
		return currentOverLimitModuleList;
	}

	public String getWarningLevel() {
		if (!currentOverLimitModuleList.isEmpty() || !currentTimeOverModuleList.isEmpty()) {
			return STATE_OVER_LIMIT;
		}

		if (!currentCloseToLimitModuleList.isEmpty() || !currentTimeWarningModuleList.isEmpty()) {
			return STATE_CLOSE_TO_LIMIT;
		}

		return STATE_OKAY;
	}

	public String getLatestDate() {
		return latestDateString;
	}

	public Map<String, Object> getClientNumbersMap() {
		return clientNumbersMap;
	}

	public Set<String> getCustomerNamesSet() {
		return customerNames;
	}

	public Map<String, Map<String, Object>> getTableMap() {
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
