/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.modulelicense;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

public final class LicensingInfoMap {
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

	private static final DateTimeFormatter GUI_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private static LicensingInfoMap instance;
	private static LicensingInfoMap instanceComplete;
	private static LicensingInfoMap instanceReduced;

	private static boolean reducedView = !OpsiLicensing.isExtendedView();

	private Map<String, Object> clientNumbersMap;
	private Set<String> customerNames;
	private List<String> availableModules;
	private List<String> shownModules;
	private List<LocalDate> datesKeys;
	private Map<String, Map<String, Map<String, Object>>> datesMap;
	private List<String> columnNames;
	private Map<String, Map<String, Object>> tableMap;
	private LocalDate latestDate;
	private String checksum;
	private Set<String> currentCloseToLimitModuleList;
	private Set<String> currentOverLimitModuleList;
	private Set<String> currentTimeWarningModuleList;
	private Set<String> currentTimeOverModuleList;
	private Integer daysClientLimitWarning;
	private Integer absolutClientLimitWarning;
	private Integer percentClientLimitWarning;
	private List<String> disabledWarningModules;

	private LicensingInfoMap(Map<String, Object> licensingInfo, Map<String, List<Object>> configVals, Boolean reduced) {
		Logging.info(getClass(), "generate with reducedView ", reduced, " at the moment ignored, we set false");

		produceConfigs(licensingInfo, configVals);
		checksum = produceChecksum(licensingInfo);
		clientNumbersMap = produceClientNumbersMap(licensingInfo);

		Map<String, Map<String, Object>> licenses = produceLicenses(licensingInfo);

		availableModules = produceAvailableModules(licensingInfo);
		shownModules = produceShownModules(licensingInfo);

		datesKeys = produceDatesKeys(licensingInfo);
		latestDate = findLatestChangeDate(datesKeys);
		datesMap = produceDatesMap(licensingInfo, licenses);
		tableMap = produceTableMapFromDatesMap();
		customerNames = produceCustomerNameSet(licensingInfo);
	}

	public static LicensingInfoMap getInstance(Map<String, Object> licensingInfo, Map<String, List<Object>> configVals,
			boolean reduced) {
		Logging.info("reduced, instance here ", reduced, ", ", instance);

		if (instance == null || instanceComplete == null || instanceReduced == null) {
			instanceComplete = new LicensingInfoMap(licensingInfo, configVals, false);
			instanceReduced = new LicensingInfoMap(licensingInfo, configVals, true);
		}

		if (reduced) {
			instance = instanceReduced;
		} else {
			instance = instanceComplete;
		}

		return instance;
	}

	public static void setReduced(boolean reduced) {
		Logging.info("setReduced instanceReduced ", instanceReduced, " cols ", instanceReduced.getColumnNames());
		Logging.info("setReduced instanceComplete ", instanceComplete, " cols ", instanceComplete.getColumnNames());

		reducedView = reduced;
		if (reduced) {
			instance = instanceReduced;
		} else {
			instance = instanceComplete;
		}
	}

	public static LicensingInfoMap getInstance() {
		if (instance == null) {
			Logging.error(" instance  not initialized");
		}

		return instance;
	}

	public static void requestRefresh() {
		instance = null;
	}

	private static Map<String, Object> produceClientNumbersMap(Map<String, Object> licensingInfo) {
		return POJOReMapper.remap(licensingInfo.get(CLIENT_NUMBERS_INFO));
	}

	private static Map<String, Map<String, Object>> produceLicenses(Map<String, Object> licensingInfo) {
		Map<String, Map<String, Object>> result = new HashMap<>();

		List<Map<String, Object>> producedLicenses = POJOReMapper.remap(licensingInfo.get(LICENSES_ID));

		for (Map<String, Object> originalMap : producedLicenses) {
			Map<String, Object> tmp = new HashMap<>();

			tmp.put(MODULE_ID, originalMap.get(MODULE_ID));
			tmp.put(VALID_UNTIL, originalMap.get(VALID_UNTIL));
			tmp.put(REVOKED_IDS, originalMap.get(REVOKED_IDS));

			result.put(originalMap.get(ID).toString(), tmp);
		}
		return result;
	}

	private static Set<String> produceCustomerNameSet(Map<String, Object> licensingInfo) {
		Set<String> producedCustomerNames = new LinkedHashSet<>();

		List<Map<String, Object>> producedLicenses = POJOReMapper.remap(licensingInfo.get(LICENSES_ID));

		for (Map<String, Object> originalMap : producedLicenses) {
			String customerName = String.valueOf(originalMap.get(CUSTOMER_NAME));

			if (originalMap.get(CUSTOMER_UNIT) != null) {
				producedCustomerNames.add(customerName + " - " + originalMap.get(CUSTOMER_UNIT).toString());
			} else {
				producedCustomerNames.add(customerName);
			}
		}

		return producedCustomerNames;
	}

	private static List<String> produceAvailableModules(Map<String, Object> licensingInfo) {
		List<String> result = POJOReMapper.remap(licensingInfo.get(AVAILABLE_MODULES));
		Collections.sort(result);

		return result;
	}

	private List<String> produceKnownModules(Map<String, Object> licensingInfo) {
		List<String> result = availableModules;

		if (licensingInfo.containsKey(KNOWN_MODULES)) {
			result = POJOReMapper.remap(licensingInfo.get(KNOWN_MODULES));
		}

		Collections.sort(result);
		return result;
	}

	private static List<String> produceObsoleteModules(Map<String, Object> licensingInfo) {
		List<String> result = new ArrayList<>();

		if (licensingInfo.containsKey(OBSOLETE_MODULES)) {
			result = POJOReMapper.remap(licensingInfo.get(OBSOLETE_MODULES));
		}

		Collections.sort(result);
		return result;
	}

	private List<String> produceShownModules(Map<String, Object> licensingInfo) {
		if (!licensingInfo.containsKey(OBSOLETE_MODULES)) {
			return produceKnownModules(licensingInfo);
		}

		List<String> result;

		if (OpsiLicensing.isShowOnlyAvailableModules()) {
			result = new ArrayList<>(availableModules);
		} else {
			result = produceKnownModules(licensingInfo);

		}

		result.removeAll(produceObsoleteModules(licensingInfo));

		Collections.sort(result);
		return result;
	}

	private void produceConfigs(Map<String, Object> licensingInfo, Map<String, List<Object>> configs) {
		try {
			if (licensingInfo.containsKey(CONFIG)) {
				Map<String, Object> config = POJOReMapper.remap(licensingInfo.get(CONFIG));

				percentClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_PERCENT).toString());
				absolutClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_ABSOLUTE).toString());
				daysClientLimitWarning = Integer.parseInt(config.get(CLIENT_LIMIT_WARNING_DAYS).toString());
				disabledWarningModules = POJOReMapper.remap(config.get(DISABLE_WARNING_FOR_MODULES));
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
			Logging.error(this, ex, " produceConfigs ");
		}
	}

	private static String produceChecksum(Map<String, Object> licensingInfo) {
		String newChecksum = "";

		if (licensingInfo.containsKey(CHECKSUM_ID) && licensingInfo.get(CHECKSUM_ID) != null) {
			newChecksum = licensingInfo.get(CHECKSUM_ID).toString();
		}

		return newChecksum;
	}

	private static List<LocalDate> produceDatesKeys(Map<String, Object> licensingInfo) {
		Map<String, Object> datesM = POJOReMapper.remap(licensingInfo.get(DATES));

		List<LocalDate> dates = datesM.keySet().stream().map(LocalDate::parse).sorted().toList();

		LocalDate latest = findLatestChangeDate(dates);

		if (reducedView) {
			dates = dates.stream().filter(d -> !d.isBefore(latest)).toList();
		}

		return dates;
	}

	private Map<LocalDate, String> produceDateToTitleMap() {
		Map<LocalDate, String> resultMap = new HashMap<>();
		if (datesKeys.isEmpty()) {
			return resultMap;
		}

		for (int i = 0; i < datesKeys.size() - 1; i++) {
			String title = toGuiDate(datesKeys.get(i)) + " - " + toGuiDate(datesKeys.get(i + 1).minusDays(1));
			resultMap.put(datesKeys.get(i), title);
		}

		resultMap.put(datesKeys.get(datesKeys.size() - 1),
				Configed.getResourceValue("LicensingInfo.from") + " " + toGuiDate(datesKeys.get(datesKeys.size() - 1)));

		return resultMap;
	}

	private static String toGuiDate(LocalDate date) {
		return date.format(GUI_FORMAT);
	}

	private Map<String, Map<String, Map<String, Object>>> produceDatesMap(Map<String, Object> licensingInfo,
			Map<String, Map<String, Object>> licenses) {
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

		Map<String, Map<String, Map<String, Object>>> resultMap = new TreeMap<>();
		Map<String, Map<String, Map<String, Object>>> dates = POJOReMapper.remap(licensingInfo.get(DATES));
		Map<LocalDate, String> dateToTitleMap = produceDateToTitleMap();

		for (LocalDate key : datesKeys) {
			Map<String, Map<String, Object>> modulesMapToDate = new TreeMap<>();

			// iterate over date entries
			Map<String, Object> moduleToDate = dates.get(key.toString()).get(MODULES);
			// iterate over module entries to every date entry

			// also warning state should be none
			for (String currentModule : shownModules) {
				Map<String, Object> moduleInfo = createModuleInfo(currentModule, moduleToDate, key, licenses);

				modulesMapToDate.put(currentModule, moduleInfo);
			}
			resultMap.put(dateToTitleMap.get(key), modulesMapToDate);
		}

		return checkTimeWarning(resultMap);
	}

	private Map<String, Object> createModuleInfo(String currentModule, Map<String, Object> moduleToDate, LocalDate key,
			Map<String, Map<String, Object>> licenses) {
		Map<String, Object> moduleInfo;
		boolean available = availableModules.contains(currentModule);

		if (moduleToDate.containsKey(currentModule)) {
			moduleInfo = POJOReMapper.remap(moduleToDate.get(currentModule));
			if (disabledWarningModules != null && disabledWarningModules.contains(currentModule)) {
				moduleInfo.put(STATE, STATE_IGNORE_WARNING);
			}
		} else {
			moduleInfo = new HashMap<>();
			moduleInfo.put(CLIENT_NUMBER, 0);
			moduleInfo.put(LICENSE_IDS, Collections.emptyList());
			moduleInfo.put(STATE, STATE_UNLICENSED);
		}

		moduleInfo.put(AVAILABLE, available);
		if (key.equals(latestDate)) {
			if (((String) moduleInfo.get(STATE)).equals(STATE_CLOSE_TO_LIMIT)) {
				currentCloseToLimitModuleList.add(currentModule);
			} else if (((String) moduleInfo.get(STATE)).equals(STATE_OVER_LIMIT)) {
				currentOverLimitModuleList.add(currentModule);
			} else if (checkTimeLeft(moduleInfo, licenses).equals(STATE_DAYS_WARNING)) {
				moduleInfo.put(STATE, STATE_DAYS_WARNING);
				currentTimeWarningModuleList.add(currentModule);
			} else if (checkTimeLeft(moduleInfo, licenses).equals(STATE_DAYS_OVER)) {
				moduleInfo.put(STATE, STATE_DAYS_OVER);
				currentTimeOverModuleList.add(currentModule);
			} else {
				// no warnings to add
			}
		}

		String futureCheck = checkFuture(moduleInfo, currentModule, key);
		moduleInfo.put(FUTURE_STATE, futureCheck);

		return moduleInfo;
	}

	/**
	 * transforms datesMap to be able to use in a table, with dates as columns
	 * and modules as rows
	 */
	private Map<String, Map<String, Object>> produceTableMapFromDatesMap() {
		Map<String, Map<String, Object>> resultMap = new TreeMap<>();

		columnNames = new ArrayList<>();
		columnNames.add(Configed.getResourceValue("LicensingInfo.module"));
		columnNames.add(Configed.getResourceValue("LicensingInfo.available"));

		for (Entry<String, Map<String, Map<String, Object>>> date : datesMap.entrySet()) {
			columnNames.add(date.getKey());
		}

		for (String currentModule : shownModules) {
			Map<String, Object> line = new HashMap<>();

			// 1st column
			line.put(Configed.getResourceValue("LicensingInfo.module"), currentModule);

			// 2nd column

			// 3rd column
			line.put(Configed.getResourceValue("LicensingInfo.available"), availableModules.contains(currentModule));

			// rest columns
			for (Entry<String, Map<String, Map<String, Object>>> date : datesMap.entrySet()) {
				line.put(date.getKey(), date.getValue().get(currentModule).get(CLIENT_NUMBER).toString());
			}

			resultMap.put(currentModule, line);
		}

		return resultMap;
	}

	private static LocalDate findLatestChangeDate(List<LocalDate> dates) {
		LocalDate now = LocalDate.now();

		return dates.stream().filter(d -> !d.isAfter(now)).reduce((first, second) -> second).orElse(now);
	}

	private LocalDate findNextChangeDate() {
		return datesKeys.stream().filter(date -> date.isAfter(latestDate)).findFirst().orElse(null);
	}

	private String checkTimeLeft(Map<String, Object> moduleInfo, Map<String, Map<String, Object>> licenses) {
		if (!moduleInfo.get(CLIENT_NUMBER).toString().equals(UNLIMITED_NUMBER)
				&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {
			List<String> lics = POJOReMapper.remap(moduleInfo.get(LICENSE_IDS));

			return checkTimeLeft(lics, licenses);
		}

		return STATE_DAYS_OKAY;
	}

	private String checkTimeLeft(List<String> licenseIds, Map<String, Map<String, Object>> licenses) {
		LocalDate now = LocalDate.now();

		for (String id : licenseIds) {
			String validUntilString = licenses.get(id).get(VALID_UNTIL).toString();

			LocalDate validUntil = LocalDate.parse(validUntilString);

			if (validUntil.isBefore(now)) {
				return STATE_DAYS_OVER;
			}

			long daysLeft = ChronoUnit.DAYS.between(now, validUntil);

			if (daysLeft <= daysClientLimitWarning) {
				return STATE_DAYS_WARNING;
			}
		}

		return STATE_DAYS_OKAY;
	}

	private String checkFuture(Map<String, Object> moduleInfo, String module, LocalDate date) {
		String futureCheck = null;
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

				futureCheck = calculateStateForNumbers(clientNum, futureNum);
			}
		}

		if (futureCheck != null && moduleInfo.get(STATE) != null
				&& !moduleInfo.get(STATE).toString().equals(STATE_IGNORE_WARNING)) {
			return futureCheck;
		} else {
			return "null";
		}
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

		if (resultMap.get(latestDate.toString()) != null) {
			for (Entry<String, Map<String, Object>> mod : resultMap.get(latestDate.toString()).entrySet()) {
				Map<String, Object> val = mod.getValue();
				String modKey = mod.getKey();

				LocalDate nextChangeDate = findNextChangeDate();
				String nextChangeDateString = nextChangeDate != null ? nextChangeDate.toString() : "";

				if (val.get(STATE).toString().equals(STATE_DAYS_WARNING) && resultMap.get(nextChangeDateString)
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
		return latestDate.toString();
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
