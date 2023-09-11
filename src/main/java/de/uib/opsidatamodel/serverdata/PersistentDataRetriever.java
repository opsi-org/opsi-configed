package de.uib.opsidatamodel.serverdata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.JSONArray;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.configed.type.OpsiHwAuditDevicePropertyTypes;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicenceContractEntry;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicencePoolXOpsiProduct;
import de.uib.configed.type.licences.LicenceUsableForEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.configed.type.licences.TableLicenceContracts;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsicommand.POJOReMapper;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.HostGroups;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.RemoteControls;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.ModulePermissionValue;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.ExtendedDate;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.table.ListCellOptions;
import utils.ProductPackageVersionSeparator;
import utils.Utils;

/**
 * Provides methods for retrieving data from the server using RPC methods, with
 * internal caching. Data returned is not the original data from the server
 * (sometimes it might be) since it does get processed before returning it.
 * <p>
 * For retrieving data without internally caching it, you can use
 * {@link VolatileDataRetriever}.
 */
@SuppressWarnings({ "unchecked" })
public class PersistentDataRetriever {
	// opsi module information
	private static final int CLIENT_COUNT_WARNING_LIMIT = 10;
	private static final int CLIENT_COUNT_TOLERANCE_LIMIT = 50;

	private static final Set<String> linuxSWnameMarkers = new HashSet<>();
	static {
		linuxSWnameMarkers.add("linux");
		linuxSWnameMarkers.add("Linux");
		linuxSWnameMarkers.add("lib");
		linuxSWnameMarkers.add("ubuntu");
		linuxSWnameMarkers.add("ubuntu");
	}

	private static final Set<String> linuxSubversionMarkers = new HashSet<>();
	static {
		linuxSubversionMarkers.add("lin:");
	}
	private static Integer classCounter = 0;

	private AbstractExecutioner exec;
	private CacheManager cacheManager;
	private OpsiServiceNOMPersistenceController persistenceController;

	public PersistentDataRetriever(AbstractExecutioner exec,
			OpsiServiceNOMPersistenceController persistenceController) {
		this.exec = exec;
		this.cacheManager = CacheManager.getInstance();
		this.persistenceController = persistenceController;
		cacheManager.setCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS,
				new HostInfoCollections(persistenceController));
	}

	public HostInfoCollections getHostInfoCollections() {
		return cacheManager.getCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS, HostInfoCollections.class);
	}

	public boolean usesMultiFactorAuthentication() {
		return cacheManager.getCachedData(CacheIdentifier.MFA_ENABLED, Boolean.class);
	}

	public void checkMultiFactorAuthentication() {
		cacheManager.setCachedData(CacheIdentifier.MFA_ENABLED,
				ServerFacade.isOpsi43() && getOTPSecret(ConfigedMain.getUser()) != null);
	}

	private String getOTPSecret(String userId) {
		List<String> callAttributes = new ArrayList<>();
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("id", userId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.USER_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> result = exec.getListOfMaps(omc);

		if (result.isEmpty()) {
			return null;
		}

		Map<String, Object> userDetails = result.get(0);
		String otpSecret = null;
		if (userDetails.containsKey("otpSecret")) {
			otpSecret = (String) userDetails.get("otpSecret");
		}

		return otpSecret;
	}

	public Map<String, Map<String, String>> getProductGroups() {
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS, Map.class) != null) {
			return cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS, Map.class);
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		Map<String, Map<String, String>> result = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.GROUP_GET_OBJECTS, new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS, result);
		return result;
	}

	public Map<String, Map<String, String>> getHostGroups() {
		if (cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS, Map.class) != null) {
			return cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS, Map.class);
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
		HostGroups result = new HostGroups(exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.GROUP_GET_OBJECTS, new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" }));
		Logging.debug(this, "getHostGroups " + result);
		result = result.addSpecialGroups();
		Logging.debug(this, "getHostGroups " + result);
		result.alterToWorkingVersion();
		Logging.debug(this, "getHostGroups rebuilt" + result);
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS, result);
		return result;
	}

	// returns the function that yields for a given clientId all groups to which the
	// client belongs
	public Map<String, Set<String>> getFObject2Groups() {
		Map<String, Set<String>> fObject2Groups = cacheManager.getCachedData(CacheIdentifier.FOBJECT_TO_GROUPS,
				Map.class);
		if (fObject2Groups == null) {
			Map<String, Map<String, String>> mappedRelations = exec.getStringMappedObjectsByKey(
					new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS, new String[] {}), "ident",
					new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" },
					ClientTree.getTranslationsFromPersistentNames());
			fObject2Groups = projectToFunction(mappedRelations, "clientId", "groupId");
			cacheManager.setCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, fObject2Groups);
		}
		return fObject2Groups;
	}

	// returns the function that yields for a given groupId all objects which belong
	// to the group
	public Map<String, Set<String>> retrieveFGroup2Members(String groupType, String memberIdName) {
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", groupType);
		Map<String, Map<String, String>> mappedRelations = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS,
						new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "objectId", "groupId" }, new String[] { memberIdName, "groupId" });
		return projectToFunction(mappedRelations, "groupId", memberIdName);
	}

	private Map<String, Set<String>> projectToFunction(Map<String, Map<String, String>> mappedRelation,
			String originVar, String imageVar) {
		Map<String, Set<String>> result = new TreeMap<>();
		Iterator<String> iter = mappedRelation.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Map<String, String> relation = mappedRelation.get(key);
			String originValue = relation.get(originVar);
			String imageValue = relation.get(imageVar);
			if (imageValue != null) {
				Set<String> assignedSet = result.computeIfAbsent(originValue, arg -> new TreeSet<>());
				assignedSet.add(imageValue);
			}
		}
		return result;
	}

	public List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf(String locale) {
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		return hwAuditConf.computeIfAbsent(locale, s -> exec.getListOfMapsOfListsOfMaps(
				new OpsiMethodCall(RPCMethodName.AUDIT_HARDWARE_GET_CONFIG, new String[] { locale })));
	}

	public List<Map<String, Object>> getHardwareOnClient() {
		List<Map<String, Object>> relationsAuditHardwareOnHost = cacheManager
				.getCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, List.class);
		if (relationsAuditHardwareOnHost == null) {
			Map<String, String> filterMap = new HashMap<>();
			filterMap.put("state", "1");
			relationsAuditHardwareOnHost = exec.getListOfMaps(new OpsiMethodCall(
					RPCMethodName.AUDIT_HARDWARE_ON_HOST_GET_OBJECTS, new Object[] { new String[0], filterMap }));
			cacheManager.setCachedData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST, relationsAuditHardwareOnHost);
		}
		return relationsAuditHardwareOnHost;
	}

	public List<String> getAllNetbootProductNames(String depotId) {
		List<String> netbootProductNames = cacheManager.getCachedData(CacheIdentifier.ALL_NETBOOT_PRODUCT_NAMES,
				List.class);
		if (netbootProductNames != null) {
			Object2Product2VersionList netbootProducts = cacheManager
					.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class);
			netbootProductNames = netbootProducts.get(depotId) != null
					? new ArrayList<>(netbootProducts.get(depotId).keySet())
					: new ArrayList<>();
			cacheManager.setCachedData(CacheIdentifier.ALL_NETBOOT_PRODUCT_NAMES, netbootProductNames);
		}
		return netbootProductNames;
	}

	public List<String> getAllLocalbootProductNames(String depotId) {
		Logging.debug(this, "getAllLocalbootProductNames for depot " + depotId);
		List<String> localbootProductNames = cacheManager.getCachedData(CacheIdentifier.ALL_LOCALBOOT_PRODUCT_NAMES,
				List.class);
		Logging.info(this, "getAllLocalbootProductNames, producing " + (localbootProductNames == null));
		if (localbootProductNames == null) {
			if (ServerFacade.isOpsi43()) {
				localbootProductNames = new ArrayList<>(getDepot2LocalbootProducts().get(depotId).keySet());
			} else {
				Map<String, List<String>> productOrderingResult = exec.getMapOfStringLists(
						new OpsiMethodCall(RPCMethodName.GET_PRODUCT_ORDERING, new String[] { depotId }));

				List<String> sortedProducts = productOrderingResult.get("sorted");
				if (sortedProducts == null) {
					sortedProducts = new ArrayList<>();
				}

				List<String> notSortedProducts = productOrderingResult.get("not_sorted");
				if (notSortedProducts == null) {
					notSortedProducts = new ArrayList<>();
				}

				Logging.info(this, "not ordered " + (notSortedProducts.size() - sortedProducts.size()) + "");

				notSortedProducts.removeAll(sortedProducts);
				Logging.info(this, "missing: " + notSortedProducts);

				localbootProductNames = sortedProducts;
				localbootProductNames.addAll(notSortedProducts);

				// we don't have a productsgroupsFullPermission)
				Set<String> permittedProducts = cacheManager.getCachedData(CacheIdentifier.PERMITTED_PRODUCTS,
						Set.class);
				if (permittedProducts != null) {
					localbootProductNames.retainAll(permittedProducts);
				}
			}
			cacheManager.setCachedData(CacheIdentifier.ALL_LOCALBOOT_PRODUCT_NAMES, localbootProductNames);
		}

		Logging.info(this, "localbootProductNames sorted, size " + localbootProductNames.size());

		return new ArrayList<>(localbootProductNames);
	}

	public List<String> getAllLocalbootProductNames() {
		return getAllLocalbootProductNames(persistenceController.theDepot);
	}

	public Map<String, RemoteControl> getRemoteControls() {
		getConfigOptions();
		return cacheManager.getCachedData(CacheIdentifier.REMOTE_CONTROLS, Map.class);
	}

	public SavedSearches getSavedSearches() {
		getConfigOptions();
		return cacheManager.getCachedData(CacheIdentifier.SAVED_SEARCHES, SavedSearches.class);
	}

	public Map<String, ListCellOptions> getConfigOptions() {
		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = getHwAuditDeviceClasses();
		Map<String, ListCellOptions> configListCellOptions = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, Map.class);
		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);

		if (configListCellOptions == null || configOptions == null || configDefaultValues == null) {
			Logging.debug(this, "getConfigOptions() work");

			List<Map<String, Object>> deleteItems = new ArrayList<>();

			boolean tryIt = true;

			int tryOnceMoreCounter = 0;
			final int STOP_REPEATING_AT_THIS = 1;

			while (tryIt) {
				tryIt = false;
				tryOnceMoreCounter++;

				configOptions = new HashMap<>();
				configListCellOptions = new HashMap<>();
				configDefaultValues = new HashMap<>();

				RemoteControls remoteControls = new RemoteControls();
				SavedSearches savedSearches = new SavedSearches();

				OpsiHwAuditDevicePropertyTypes hwAuditDevicePropertyTypes = new OpsiHwAuditDevicePropertyTypes(
						hwAuditDeviceClasses);

				// metaConfig for wan configuration is rebuilt in
				// getWANConfigOptions

				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_GET_OBJECTS, new Object[0]);
				List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

				Logging.info(this, "configOptions retrieved ");

				for (Map<String, Object> configItem : retrievedList) {
					// map to java type
					for (Entry<String, Object> configItemEntry : configItem.entrySet()) {
						if (configItemEntry.getValue() instanceof JSONArray) {
							configItem.put(configItemEntry.getKey(), ((JSONArray) configItemEntry.getValue()).toList());
						}
					}

					String key = (String) configItem.get("ident");

					// build a ConfigOption from the retrieved item

					// eliminate key produced by old version for role branch

					String pseudouserProducedByOldVersion = OpsiServiceNOMPersistenceController.KEY_USER_ROOT + ".{"
							+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());
					//

					if (key != null && key.startsWith(pseudouserProducedByOldVersion)) {
						Logging.warning(this, "user entry " + key
								+ " produced by a still somewhere running old configed version , please delete user entry "
								+ pseudouserProducedByOldVersion);

						deleteItems.add(configItem);

						Logging.info(this, "deleteItem " + configItem);

						continue;
					}

					ConfigOption configOption = new ConfigOption(configItem);

					configOptions.put(key, configOption);

					configListCellOptions.put(key, configOption);

					if (configOption.getDefaultValues() == null) {
						Logging.warning(this, "default values missing for config  " + key);

						if (tryOnceMoreCounter <= STOP_REPEATING_AT_THIS) {
							tryIt = true;
							Logging.warning(this,
									"repeat loading the values , we repeated  " + tryOnceMoreCounter + " times");

							Utils.threadSleep(this, 1000);
							break;
						}
					}

					configDefaultValues.put(key, configOption.getDefaultValues());

					if (configOption.getDefaultValues() != null && !configOption.getDefaultValues().isEmpty()) {
						remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
						savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
						hwAuditDevicePropertyTypes.checkIn(key, configOption.getDefaultValues());
					}
				}

				cacheManager.setCachedData(CacheIdentifier.REMOTE_CONTROLS, remoteControls);
				cacheManager.setCachedData(CacheIdentifier.SAVED_SEARCHES, savedSearches);

				Logging.debug(this,
						" getConfigOptions produced hwAuditDevicePropertyTypes " + hwAuditDevicePropertyTypes);
			}

			cacheManager.setCachedData(CacheIdentifier.CONFIG_LIST_CELL_OPTIONS, configListCellOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_OPTIONS, configOptions);
			cacheManager.setCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, configDefaultValues);

			Logging.info(this, "{ole deleteItems " + deleteItems.size());

			if (!deleteItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_DELETE_OBJECTS,
						new Object[] { deleteItems.toArray() });

				if (exec.doCall(omc)) {
					deleteItems.clear();
				}
			}

			getWANConfigOptions();
			Logging.debug(this, "getConfigOptions() work finished");
		}

		return configListCellOptions;
	}

	public Map<String, ConfigOption> getWANConfigOptions() {
		Map<String, ConfigOption> allWanConfigOptions = extractSubConfigOptionsByInitial(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "." + OpsiServiceNOMPersistenceController.WAN_PARTKEY);

		Logging.info(this, " getWANConfigOptions   " + allWanConfigOptions);

		Map<String, ConfigOption> notWanConfigOptions = extractSubConfigOptionsByInitial(
				OpsiServiceNOMPersistenceController.CONFIG_KEY + "."
						+ OpsiServiceNOMPersistenceController.NOT_WAN_CONFIGURED_PARTKEY + ".");

		Map<String, List<Object>> notWanConfiguration = new HashMap<>();
		Map<String, List<Object>> wanConfiguration = new HashMap<>();

		List<Object> values = null;

		for (Entry<String, ConfigOption> notWanConfigOption : notWanConfigOptions.entrySet()) {
			if (notWanConfigOption.getValue().getType() != ConfigOption.TYPE.BOOL_CONFIG) {
				notWanConfiguration.put(notWanConfigOption.getKey(), null);
				wanConfiguration.put(notWanConfigOption.getKey(), null);
			} else {
				Boolean b = (Boolean) notWanConfigOption.getValue().getDefaultValues().get(0);

				values = new ArrayList<>();
				values.add(b);
				notWanConfiguration.put(notWanConfigOption.getKey(), values);

				values = new ArrayList<>();
				values.add(!b);
				wanConfiguration.put(notWanConfigOption.getKey(), values);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.WAN_CONFIGURATION, wanConfiguration);
		Logging.info(this, "getWANConfigOptions wanConfiguration " + wanConfiguration);
		cacheManager.setCachedData(CacheIdentifier.NOT_WAN_CONFIGURATION, notWanConfiguration);
		Logging.info(this, "getWANConfigOptions notWanConfiguration  " + notWanConfiguration);

		return allWanConfigOptions;
	}

	private Map<String, ConfigOption> extractSubConfigOptionsByInitial(final String s) {
		HashMap<String, ConfigOption> result = new HashMap<>();
		getConfigOptions();
		Map<String, ConfigOption> configOptions = cacheManager.getCachedData(CacheIdentifier.CONFIG_OPTIONS, Map.class);
		for (Entry<String, ConfigOption> configOption : configOptions.entrySet()) {
			if (configOption.getKey().startsWith(s) && configOption.getKey().length() > s.length()) {
				String xKey = configOption.getKey().substring(s.length());
				result.put(xKey, configOption.getValue());
			}
		}

		return result;
	}

	public Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClasses() {
		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
		if (hwAuditDeviceClasses == null) {
			produceHwAuditDeviceClasses();
		}
		return hwAuditDeviceClasses;
	}

	private void produceHwAuditDeviceClasses() {
		Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = new TreeMap<>();

		if (getOpsiHWAuditConf().isEmpty()) {
			Logging.error(this, "no hwaudit config found ");
			return;
		}

		for (Map<String, List<Map<String, Object>>> hwAuditClass : getOpsiHWAuditConf()) {
			if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) == null
					|| hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) == null) {
				Logging.warning(this, "getAllHwClassNames illegal hw config item, having hwAuditClass.get Class "
						+ hwAuditClass.get("Class"));
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Class is of class "
									+ hwAuditClass.get("Class").getClass());
				}
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Values is of class "
									+ hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY).getClass());
				}

				continue;
			}
			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);

			OpsiHwAuditDevicePropertyType firstSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			firstSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.FIRST_SEEN_COL_NAME);
			firstSeen.setOpsiDbColumnType("timestamp");
			OpsiHwAuditDevicePropertyType lastSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			lastSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.LAST_SEEN_COL_NAME);
			lastSeen.setOpsiDbColumnType("timestamp");

			OpsiHwAuditDeviceClass hwAuditDeviceClass = new OpsiHwAuditDeviceClass(hwClass);
			hwAuditDeviceClasses.put(hwClass, hwAuditDeviceClass);

			hwAuditDeviceClass.setLinuxQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.LINUX_KEY));
			hwAuditDeviceClass.setWmiQuery((String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.WMI_KEY));

			Logging.info(this, "hw audit class " + hwClass);

			for (Object m : (List<?>) hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY)) {
				if (!(m instanceof Map)) {
					Logging.warning(this, "getAllHwClassNames illegal VALUES item, m " + m);
					continue;
				}

				Map<?, ?> ma = (Map<?, ?>) m;

				if ("i".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHostRelatedProperty(devProperty);
					hwAuditDeviceClass.setHostConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));

				} else if ("g".equals(ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY))) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));

					hwAuditDeviceClass.addHwItemRelatedProperty(devProperty);
					hwAuditDeviceClass.setHwItemConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE).toLowerCase(Locale.ROOT));
				} else {
					Logging.warning(this, "getAllHwClassNames illegal value for key " + OpsiHwAuditDeviceClass.SCOPE_KEY
							+ " " + ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY));
				}
			}

			hwAuditDeviceClass.addHostRelatedProperty(firstSeen);
			hwAuditDeviceClass.addHostRelatedProperty(lastSeen);

			Logging.info(this, "hw audit class " + hwAuditDeviceClass);
		}

		cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, hwAuditDeviceClasses);
		Logging.info(this, "produceHwAuditDeviceClasses hwAuditDeviceClasses size " + hwAuditDeviceClasses.size());
	}

	public List<String> getAllHwClassNames() {
		List<String> opsiHwClassNames = cacheManager.getCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES, List.class);
		if (opsiHwClassNames == null) {
			opsiHwClassNames = produceHwClasses(getOpsiHWAuditConf());
		}
		Logging.info(this, "getAllHwClassNames, hw classes " + opsiHwClassNames);
		return opsiHwClassNames;
	}

	// partial version of produceHwAuditDeviceClasses()
	private List<String> produceHwClasses(List<Map<String, List<Map<String, Object>>>> hwAuditConf) {
		List<String> result = new ArrayList<>();
		for (Map<String, List<Map<String, Object>>> hwAuditClass : hwAuditConf) {
			String hwClass = (String) hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).get(0)
					.get(OpsiHwAuditDeviceClass.OPSI_KEY);
			result.add(hwClass);
		}
		cacheManager.setCachedData(CacheIdentifier.OPSI_HW_CLASS_NAMES, result);
		return result;
	}

	private List<Map<String, List<Map<String, Object>>>> getOpsiHWAuditConf() {
		Map<String, List<Map<String, List<Map<String, Object>>>>> hwAuditConf = cacheManager
				.getCachedData(CacheIdentifier.HW_AUDIT_CONF, Map.class);
		if (hwAuditConf == null) {
			Logging.warning("hwAuditConf is null in getOpsiHWAuditConf");
			return new ArrayList<>();
		} else if (!hwAuditConf.containsKey("")) {
			hwAuditConf.put("", exec.getListOfMapsOfListsOfMaps(
					new OpsiMethodCall(RPCMethodName.AUDIT_HARDWARE_GET_CONFIG, new String[] {})));
			if (hwAuditConf.get("") == null) {
				Logging.warning(this, "got no hardware config");
			}
			cacheManager.setCachedData(CacheIdentifier.HW_AUDIT_CONF, hwAuditConf);
		} else {
			// hwAuditConf already contains key "" and is initialized
		}
		return hwAuditConf.get("");
	}

	private void produceOpsiModulesInfo() {
		// has the actual signal if a module is activ
		Map<String, Boolean> opsiModules = new HashMap<>();

		// opsiinformation which delivers the service information on checked modules

		// displaying to the user

		getHostInfoCollections().retrieveOpsiHosts();
		Logging.info(this,
				"getOverLimitModuleList() "
						+ LicensingInfoMap.getInstance(getOpsiLicencingInfoOpsiAdmin(), getConfigDefaultValues(), true)
								.getCurrentOverLimitModuleList());

		LicensingInfoMap licInfoMap = LicensingInfoMap.getInstance(getOpsiLicencingInfoOpsiAdmin(),
				getConfigDefaultValues(), !FGeneralDialogLicensingInfo.isExtendedView());

		List<String> availableModules = licInfoMap.getAvailableModules();

		for (String mod : licInfoMap.getModules()) {
			opsiModules.put(mod, availableModules.indexOf(mod) != -1);
		}

		Logging.info(this, "opsiModules result " + opsiModules);

		cacheManager.setCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT,
				opsiModules.get("license_management") != null && opsiModules.get("license_management"));
		cacheManager.setCachedData(CacheIdentifier.WITH_LOCAL_IMAGING,
				opsiModules.get("local_imaging") != null && opsiModules.get("local_imaging"));
		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQL());
		cacheManager.setCachedData(CacheIdentifier.WITH_UEFI,
				opsiModules.get("uefi") != null && opsiModules.get("uefi"));
		cacheManager.setCachedData(CacheIdentifier.WITH_WAN, opsiModules.get("vpn") != null && opsiModules.get("vpn"));
		cacheManager.setCachedData(CacheIdentifier.WITH_USER_ROLES,
				opsiModules.get("userroles") != null && opsiModules.get("userroles"));

		Logging.info(this, "produceOpsiModulesInfo withUserRoles "
				+ cacheManager.getCachedData(CacheIdentifier.WITH_USER_ROLES, Boolean.class));
		Logging.info(this, "produceOpsiModulesInfo withUEFI "
				+ cacheManager.getCachedData(CacheIdentifier.WITH_UEFI, Boolean.class));
		Logging.info(this, "produceOpsiModulesInfo withWAN "
				+ cacheManager.getCachedData(CacheIdentifier.WITH_WAN, Boolean.class));
		Logging.info(this, "produceOpsiModulesInfo withLicenceManagement "
				+ cacheManager.getCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT, Boolean.class));
		Logging.info(this, "produceOpsiModulesInfo withMySQL "
				+ cacheManager.getCachedData(CacheIdentifier.WITH_MY_SQL, Boolean.class));

		// sets value to true if we use the mysql backend and informs that we are
		// underlicensed
	}

	public Map<String, List<Object>> getConfigDefaultValues() {
		getConfigOptions();
		return cacheManager.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
	}

	public boolean canCallMySQL() {
		Boolean acceptMySQL = cacheManager.getCachedData(CacheIdentifier.ACCEPT_MY_SQL, Boolean.class);
		if (acceptMySQL == null) {
			if (ServerFacade.isOpsi43()) {
				acceptMySQL = false;
			} else {
				// test if we can access any table

				String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME + " LIMIT 1 ";
				Logging.info(this, "test, query " + query);
				acceptMySQL = exec.doCall(new OpsiMethodCall(RPCMethodName.GET_RAW_DATA, new Object[] { query }));
				Logging.info(this, "test result " + acceptMySQL);
			}
			cacheManager.setCachedData(CacheIdentifier.ACCEPT_MY_SQL, acceptMySQL);
		}

		// we cannot call MySQL if version before 4.3
		return acceptMySQL;
	}

	// is not allowed to be overriden in order to prevent changes
	public final Map<String, Object> getOpsiLicencingInfoOpsiAdmin() {
		Map<String, Object> licencingInfoOpsiAdmin = cacheManager
				.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN, Map.class);
		if (licencingInfoOpsiAdmin == null && isOpsiLicencingAvailable() && isOpsiUserAdmin()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.BACKEND_GET_LICENSING_INFO,
					new Object[] { true, false, true, false });

			licencingInfoOpsiAdmin = exec.retrieveResponse(omc);
		}

		return licencingInfoOpsiAdmin;
	}

	public boolean isOpsiUserAdmin() {
		boolean hasIsOpisUserAdminBeenChecked = cacheManager
				.getCachedData(CacheIdentifier.HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED, Boolean.class);
		if (!hasIsOpisUserAdminBeenChecked) {
			retrieveIsOpsiUserAdmin();
		}
		return cacheManager.getCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, Boolean.class);
	}

	private void retrieveIsOpsiUserAdmin() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.ACCESS_CONTROL_USER_IS_ADMIN, new Object[] {});
		Map<String, Object> json = exec.retrieveResponse(omc);

		Boolean isOpsiUserAdmin = cacheManager.getCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, Boolean.class);
		if (json.containsKey("result") && json.get("result") != null) {
			isOpsiUserAdmin = (Boolean) json.get("result");
		} else {
			Logging.warning(this, "cannot check if user is admin, fallback to false...");

			isOpsiUserAdmin = false;
		}
		cacheManager.setCachedData(CacheIdentifier.IS_OPSI_ADMIN_USER, isOpsiUserAdmin);
		cacheManager.setCachedData(CacheIdentifier.HAS_IS_OPSI_USER_ADMIN_BEEN_CHECKED, true);
	}

	public Map<String, Object> getOpsiLicencingInfoNoOpsiAdmin() {
		Logging.info(this, "getLicensingInfoNoOpsiAdmin");

		Map<String, Object> licensingInfoNoOpsiAdmin = cacheManager
				.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN, Map.class);
		Map<String, Object> licensingInfoOpsiAdmin = cacheManager
				.getCachedData(CacheIdentifier.OPSI_LICENSING_INFO_OPSI_ADMIN, Map.class);
		if (licensingInfoOpsiAdmin == null && isOpsiLicencingAvailable()) {
			Object[] callParameters = {};
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.BACKEND_GET_LICENSING_INFO, callParameters,
					OpsiMethodCall.BACKGROUND_DEFAULT);
			licensingInfoNoOpsiAdmin = exec.getMapResult(omc);
			cacheManager.setCachedData(CacheIdentifier.OPSI_LICENSING_INFO_NO_OPSI_ADMIN, licensingInfoNoOpsiAdmin);
		}

		return licensingInfoNoOpsiAdmin;
	}

	public boolean isOpsiLicencingAvailable() {
		retrieveOpsiLicensingInfoVersion();
		return cacheManager.getCachedData(CacheIdentifier.IS_OPSI_LICENSING_AVAILABLE, Boolean.class);
	}

	private void retrieveOpsiLicensingInfoVersion() {
		boolean isOpsiLicencingAvailable = cacheManager.getCachedData(CacheIdentifier.IS_OPSI_LICENSING_AVAILABLE,
				Boolean.class);
		boolean hasOpsiLicencingBeenChecked = cacheManager
				.getCachedData(CacheIdentifier.HAS_OPSI_LICENSING_BEEN_CHECKED, Boolean.class);
		if (!hasOpsiLicencingBeenChecked) {
			Logging.info(this, "retrieveOpsiLicensingInfoVersion getMethodSignature( backend_getLicensingInfo "
					+ getMethodSignature(RPCMethodName.BACKEND_GET_LICENSING_INFO));

			if (getMethodSignature(
					RPCMethodName.BACKEND_GET_LICENSING_INFO) == OpsiServiceNOMPersistenceController.NONE_LIST) {
				Logging.info(this,
						"method " + RPCMethodName.BACKEND_GET_LICENSING_INFO + " not existing in this opsi service");
				isOpsiLicencingAvailable = false;
			} else {
				isOpsiLicencingAvailable = true;
			}

			hasOpsiLicencingBeenChecked = true;
			cacheManager.setCachedData(CacheIdentifier.IS_OPSI_LICENSING_AVAILABLE, isOpsiLicencingAvailable);
			cacheManager.setCachedData(CacheIdentifier.HAS_OPSI_LICENSING_BEEN_CHECKED, hasOpsiLicencingBeenChecked);
		}
	}

	// lazy initializing
	public List<String> getMethodSignature(RPCMethodName methodname) {
		Map<String, List<String>> mapOfMethodSignatures = cacheManager
				.getCachedData(CacheIdentifier.MAP_OF_METHOD_SIGNATURES, Map.class);
		if (mapOfMethodSignatures == null) {
			List<Object> methodsList = exec
					.getListResult(new OpsiMethodCall(RPCMethodName.BACKEND_GET_INTERFACE, new Object[] {}));

			if (!methodsList.isEmpty()) {
				mapOfMethodSignatures = new HashMap<>();

				Iterator<Object> iter = methodsList.iterator();
				while (iter.hasNext()) {
					Map<String, Object> listEntry = exec.getMapFromItem(iter.next());

					String name = (String) listEntry.get("name");
					List<String> signature = new ArrayList<>();

					// should never result
					List<Object> signature1 = exec.getListFromItem(listEntry.get("params").toString());

					// to null
					for (int i = 0; i < signature1.size(); i++) {
						String element = (String) signature1.get(i);

						if (element != null && element.length() > 0 && element.charAt(0) == '*') {
							signature.add(element.substring(1));
						} else {
							signature.add(element);
						}

						Logging.debug(this, "mapOfMethodSignatures  " + i + ":: " + name + ": " + signature);
					}
					mapOfMethodSignatures.put(name, signature);
				}
				cacheManager.setCachedData(CacheIdentifier.MAP_OF_METHOD_SIGNATURES, mapOfMethodSignatures);
			}
		}

		Logging.debug(this, "mapOfMethodSignatures " + mapOfMethodSignatures);

		if (mapOfMethodSignatures.get(methodname.toString()) == null) {
			return OpsiServiceNOMPersistenceController.NONE_LIST;
		}

		return mapOfMethodSignatures.get(methodname.toString());
	}

	public final void retrieveOpsiModules() {
		Logging.info(this, "retrieveOpsiModules ");

		Map<String, Object> licencingInfoOpsiAdmin = getOpsiLicencingInfoOpsiAdmin();

		// probably old opsi service version
		if (licencingInfoOpsiAdmin == null) {
			if (ServerFacade.isOpsi43()) {
				produceOpsiModulesInfoClassicOpsi43();
			} else {
				produceOpsiModulesInfoClassic();
			}
		} else {
			produceOpsiModulesInfo();
		}

		Logging.info(this, " withMySQL " + cacheManager.getCachedData(CacheIdentifier.WITH_MY_SQL, Boolean.class));
		Logging.info(this,
				" withUserRoles " + cacheManager.getCachedData(CacheIdentifier.WITH_USER_ROLES, Boolean.class));
	}

	public Map<String, Object> getOpsiModulesInfos() {
		return cacheManager.getCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, Map.class);
	}

	private void produceOpsiModulesInfoClassicOpsi43() {
		produceOpsiInformation();

		// keeps the info for displaying to the user
		Map<String, Object> opsiModulesDisplayInfo = new HashMap<>();

		HashMap<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		Map<String, Boolean> opsiModules = new HashMap<>();

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		Map<String, Object> opsiInformation = cacheManager.getCachedData(CacheIdentifier.OPSI_INFORMATION, Map.class);
		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));
		Logging.info(this, "opsi module information " + opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("modules"));
		opsiCountModules.keySet()
				.removeAll(exec.getListFromItem(((JSONArray) opsiInformation.get("obsolete_modules")).toString()));
		getHostInfoCollections().retrieveOpsiHosts();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = cacheManager.getCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS, HostInfoCollections.class)
				.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiModuleInfo.getValue(),
					new TypeReference<Map<String, Object>>() {
					});
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleData.get("available"),
					validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}

			if (opsiModuleData.get("available") != null) {
				opsiModulesDisplayInfo.put(opsiModuleInfo.getKey(), opsiModuleData.get("available"));
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);
			Map<String, Object> opsiModuleData = POJOReMapper.remap(opsiCountModule.getValue(),
					new TypeReference<Map<String, Object>>() {
					});

			if ("free".equals(opsiModuleData.get("state"))) {
				continue;
			}

			modulePermission = new ModulePermissionValue(opsiModuleData.get("client_number"), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiModuleData.get("client_number") != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiModuleData.get("client_number"));
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES_DISPLAY_INFO, opsiModulesDisplayInfo);

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (

		String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {

					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && cacheManager
							.getCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS, HostInfoCollections.class)
							.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate
					&& ("linux_agent".equals(key) || ("userroles".equals(key) && !isUserRegisterActivated()))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(
										// locale.
										Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(
										// locale,
										Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// Do nothing when countClientsInThisBlock <= startWarningCount
					}
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only, when info empty
		callOpsiLicenceMissingModules(missingModulesPermissionInfo);

		cacheManager.setCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT,
				opsiModules.get("license_management") != null && opsiModules.get("license_management"));
		cacheManager.setCachedData(CacheIdentifier.WITH_LOCAL_IMAGING,
				opsiModules.get("local_imaging") != null && opsiModules.get("local_imaging"));
		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQL());
		cacheManager.setCachedData(CacheIdentifier.WITH_UEFI,
				opsiModules.get("uefi") != null && opsiModules.get("uefi"));
		cacheManager.setCachedData(CacheIdentifier.WITH_WAN, opsiModules.get("vpn") != null && opsiModules.get("vpn"));
		cacheManager.setCachedData(CacheIdentifier.WITH_USER_ROLES,
				opsiModules.get("userroles") != null && opsiModules.get("userroles"));

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	public final boolean isUserRegisterActivated() {
		boolean result = false;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER) != null
				&& !serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List<?>) serverPropertyMap.get(OpsiServiceNOMPersistenceController.KEY_USER_REGISTER))
					.get(0);
		}
		return result;
	}

	private void callOpsiLicenceMissingModules(List<String> missingModulesPermissionInfo) {
		if (!missingModulesPermissionInfo.isEmpty()) {

			SwingUtilities.invokeLater(() -> {
				StringBuilder info = new StringBuilder();

				info.append(Configed.getResourceValue("Permission.modules.clientcount.2"));
				info.append(":\n");
				for (String moduleInfo : missingModulesPermissionInfo) {
					info.append(moduleInfo + "\n");
				}

				Logging.info(this, "missingModules " + info);
				FOpsiLicenseMissingText.callInstanceWith(info.toString());
			});
		}
	}

	private void produceOpsiModulesInfoClassic() {
		produceOpsiInformation();

		// keeps the info for displaying to the user
		Map<String, Object> opsiModulesDisplayInfo = new HashMap<>();

		HashMap<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		Map<String, Boolean> opsiModules = new HashMap<>();

		Map<String, Object> opsiInformation = cacheManager.getCachedData(CacheIdentifier.OPSI_INFORMATION, Map.class);
		String opsiVersion = (String) opsiInformation.get("opsiVersion");
		Logging.info(this, "opsi version information " + opsiVersion);

		final List<String> missingModulesPermissionInfo = new ArrayList<>();

		// prepare the user info
		Map<String, Object> opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));

		opsiModulesInfo.remove("signature");
		Logging.info(this, "opsi module information " + opsiModulesInfo);
		opsiModulesInfo.remove("valid");

		opsiModulesDisplayInfo = new HashMap<>(opsiModulesInfo);

		ExtendedDate validUntil = ExtendedDate.INFINITE;

		// analyse the real module info
		Map<String, Object> opsiCountModules = exec.getMapFromItem(opsiInformation.get("realmodules"));
		getHostInfoCollections().retrieveOpsiHosts();

		ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

		int countClients = cacheManager.getCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS, HostInfoCollections.class)
				.getCountClients();

		LocalDateTime today = LocalDateTime.now();

		Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

		// read in modules
		for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
			Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo.getKey());
			ModulePermissionValue modulePermission = new ModulePermissionValue(opsiModuleInfo.getValue(), validUntil);

			Logging.info(this, "handle modules key, modulePermission  " + modulePermission);
			Boolean permissionCheck = modulePermission.getBoolean();
			opsiModulesPermissions.put(opsiModuleInfo.getKey(), modulePermission);
			if (permissionCheck != null) {
				opsiModules.put(opsiModuleInfo.getKey(), permissionCheck);
			}
		}

		Logging.info(this, "modules resulting step 0  " + opsiModules);

		// existing
		for (Entry<String, Object> opsiCountModule : opsiCountModules.entrySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(opsiCountModule.getKey());
			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission was " + modulePermission);

			modulePermission = new ModulePermissionValue(opsiCountModule.getValue(), validUntil);

			Logging.info(this,
					"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
			// replace value got from modulesInfo
			opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

			if (opsiCountModule.getValue() != null) {
				opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiCountModule.getValue());
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_INFORMATION, opsiModulesDisplayInfo);

		Logging.info(this, "modules resulting step 1 " + opsiModules);
		Logging.info(this, "countModules is  " + opsiCountModules);

		// set values for modules checked by configed
		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			if (modulePermission.getBoolean() != null) {
				opsiModules.put(key, modulePermission.getBoolean());
				Logging.info(this,
						" retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
			} else {
				opsiModules.put(key, true);
				Logging.info(this, " retrieveOpsiModules " + key + " " + maxClientsForThisModule.getNumber());

				if (maxClientsForThisModule.equals(ExtendedInteger.ZERO)) {
					opsiModules.put(key, false);
				} else {

					Integer warningLimit = null;
					Integer stopLimit = null;

					Logging.info(this,
							" retrieveOpsiModules " + key + " up to now globalMaxClients " + globalMaxClients);

					Logging.info(this, " retrieveOpsiModules " + key + " maxClientsForThisModule.getNumber "
							+ maxClientsForThisModule.getNumber());

					globalMaxClients = calculateModulePermission(globalMaxClients, maxClientsForThisModule.getNumber());

					Logging.info(this,
							" retrieveOpsiModules " + key + " result:  globalMaxClients is " + globalMaxClients);

					Integer newGlobalLimit = globalMaxClients.getNumber();

					// global limit is changed by this module a real warning
					// and error limit exists
					if (newGlobalLimit != null) {
						warningLimit = newGlobalLimit - CLIENT_COUNT_WARNING_LIMIT;
						stopLimit = newGlobalLimit + CLIENT_COUNT_TOLERANCE_LIMIT;
					}

					Logging.info(this, " retrieveOpsiModules " + key + " old  warningLimit " + warningLimit
							+ " stopLimit " + stopLimit);

					if (stopLimit != null && cacheManager
							.getCachedData(CacheIdentifier.HOST_INFO_COLLECTIONS, HostInfoCollections.class)
							.getCountClients() > stopLimit) {
						opsiModules.put(key, false);
					} else {
						if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
							LocalDateTime expiresDate = expiresForThisModule.getDate();

							if (today.isAfter(expiresDate)) {
								opsiModules.put(key, false);
							}
						}
					}
				}
			}
		}

		Logging.info(this, "modules resulting step 2  " + opsiModules);
		Logging.info(this, "count Modules is  " + opsiCountModules);

		for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
			int countClientsInThisBlock = countClients;

			// tests

			Logging.info(this, "check module " + key + " problem on start " + (!(opsiModules.get(key))));
			boolean problemToIndicate = true;
			ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
			ExtendedInteger maxAllowedClientsForThisModule = modulePermission.getMaxClients();
			ExtendedDate expiresForThisModule = modulePermission.getExpires();

			Logging.info(this, "check  module " + key + " maxAllowedClientsForThisModule "
					+ maxAllowedClientsForThisModule + " expiresForThisModule " + expiresForThisModule);

			if (maxAllowedClientsForThisModule.equals(ExtendedInteger.ZERO)) {
				problemToIndicate = false;
			}

			if (problemToIndicate
					&& ("linux_agent".equals(key) || ("userroles".equals(key) && !isUserRegisterActivated()))) {
				problemToIndicate = false;
			}

			Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

			if (problemToIndicate) {
				Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
						+ " count " + countClientsInThisBlock);

				if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
					LocalDateTime noticeDate = expiresForThisModule.getDate().minusDays(14);

					if (today.isAfter(noticeDate)) {
						missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
					}
				}

				if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
					int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
					int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

					if (countClientsInThisBlock > stopCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount + " count clients "
								+ countClients);

						String warningText =

								String.format(
										// locale.
										Configed.getResourceValue("Permission.modules.clientcount.error"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);

						Logging.warning(this, warningText);
					} else if (countClientsInThisBlock > startWarningCount) {
						Logging.info(this, "retrieveOpsiModules " + key + " startWarningCount " + startWarningCount
								+ " count clients " + countClients);

						String warningText =

								String.format(Configed.getResourceValue("Permission.modules.clientcount.warning"),
										"" + countClientsInThisBlock, "" + key,
										"" + maxAllowedClientsForThisModule.getNumber());

						missingModulesPermissionInfo.add(warningText);
						Logging.warning(this, warningText);
					} else {
						// countClientsInThisBlock small enough, so nothing to do
					}
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_MODULES, opsiModules);

		Logging.info(this, "modules resulting  " + opsiModules);
		Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

		// Will be called only when info empty
		callOpsiLicenceMissingModules(missingModulesPermissionInfo);

		cacheManager.setCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT,
				(opsiModules.get("license_management") != null) && opsiModules.get("license_management"));
		cacheManager.setCachedData(CacheIdentifier.WITH_LOCAL_IMAGING,
				(opsiModules.get("local_imaging") != null) && opsiModules.get("local_imaging"));
		cacheManager.setCachedData(CacheIdentifier.WITH_MY_SQL, canCallMySQL());
		cacheManager.setCachedData(CacheIdentifier.WITH_UEFI,
				(opsiModules.get("uefi") != null) && opsiModules.get("uefi"));
		cacheManager.setCachedData(CacheIdentifier.WITH_WAN,
				(opsiModules.get("vpn") != null) && opsiModules.get("vpn"));
		cacheManager.setCachedData(CacheIdentifier.WITH_USER_ROLES,
				(opsiModules.get("userroles") != null) && opsiModules.get("userroles"));

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	private ExtendedInteger calculateModulePermission(ExtendedInteger globalMaxClients,
			final Integer specialMaxClientNumber) {
		Logging.info(this, "calculateModulePermission globalMaxClients " + globalMaxClients + " specialMaxClientNumber "
				+ specialMaxClientNumber);
		Integer maxClients = null;

		if (specialMaxClientNumber != null) {
			int compareResult = globalMaxClients.compareTo(specialMaxClientNumber);
			Logging.info(this, "calculateModulePermission compareResult " + compareResult);

			// the global max client count is reduced, a real warning and error limit exists
			if (compareResult < 0) {

				maxClients = specialMaxClientNumber;
				globalMaxClients = new ExtendedInteger(maxClients);
			} else {
				maxClients = specialMaxClientNumber;
			}
		}

		Logging.info(this, "calculateModulePermission returns " + maxClients);

		if (maxClients == null) {
			return globalMaxClients;
		} else {
			return new ExtendedInteger(maxClients);
		}
	}

	private Map<String, Object> produceOpsiInformation() {
		Map<String, Object> opsiInformation = cacheManager.getCachedData(CacheIdentifier.OPSI_INFORMATION, Map.class);
		if (opsiInformation != null && !opsiInformation.isEmpty()) {
			return opsiInformation;
		}

		RPCMethodName methodName = RPCMethodName.BACKEND_INFO;

		if (ServerFacade.isOpsi43()) {
			methodName = RPCMethodName.BACKEND_GET_LICENSING_INFO;
		}

		OpsiMethodCall omc = new OpsiMethodCall(methodName, new String[] {});
		opsiInformation = new HashMap<>();

		// method does not exist before opsi 3.4
		if (getMethodSignature(methodName) != OpsiServiceNOMPersistenceController.NONE_LIST) {
			opsiInformation = exec.getMapResult(omc);
		}

		cacheManager.setCachedData(CacheIdentifier.OPSI_INFORMATION, opsiInformation);
		return opsiInformation;
	}

	public boolean isWithLocalImaging() {
		retrieveOpsiModules();
		return cacheManager.getCachedData(CacheIdentifier.WITH_LOCAL_IMAGING, Boolean.class);
	}

	public boolean isWithUEFI() {
		return cacheManager.getCachedData(CacheIdentifier.WITH_UEFI, Boolean.class);
	}

	public boolean isWithWAN() {
		return cacheManager.getCachedData(CacheIdentifier.WITH_WAN, Boolean.class);
	}

	public boolean isWithLicenceManagement() {
		return cacheManager.getCachedData(CacheIdentifier.WITH_LICENSE_MANAGEMENT, Boolean.class);
	}

	public Map<String, TreeSet<OpsiPackage>> getDepot2Packages() {
		retrieveProductsAllDepots();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PACKAGES, Map.class);
	}

	public List<List<Object>> getProductRows() {
		retrieveProductsAllDepots();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_ROWS, List.class);
	}

	public Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots() {
		retrieveProductsAllDepots();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
	}

	public Object2Product2VersionList getDepot2LocalbootProducts() {
		retrieveProductsAllDepots();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS,
				Object2Product2VersionList.class);
	}

	public Object2Product2VersionList getDepot2NetbootProducts() {
		retrieveProductsAllDepots();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class);
	}

	private void retrieveProductsAllDepots() {
		Logging.debug(this, "retrieveProductsAllDepots ? ");
		Object2Product2VersionList depot2LocalbootProducts = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS, Object2Product2VersionList.class);
		if (depot2LocalbootProducts != null) {
			Logging.debug(this, "depot2LocalbootProducts " + depot2LocalbootProducts.size());
		}
		Object2Product2VersionList depot2NetbootProducts = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, Object2Product2VersionList.class);
		if (depot2NetbootProducts != null) {
			Logging.debug(this, "depot2NetbootProducts" + depot2NetbootProducts.size());
		}
		retrieveProductInfos();

		List<List<Object>> productRows = cacheManager.getCachedData(CacheIdentifier.PRODUCT_ROWS, List.class);
		Map<String, TreeSet<OpsiPackage>> depot2Packages = cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PACKAGES,
				Map.class);
		Map<String, Map<String, List<String>>> product2VersionInfo2Depots = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
		if (depot2NetbootProducts == null || depot2LocalbootProducts == null || productRows == null
				|| depot2Packages == null) {

			Logging.info(this, "retrieveProductsAllDepots, reload");
			Logging.info(this, "retrieveProductsAllDepots, reload depot2NetbootProducts == null "
					+ (depot2NetbootProducts == null));
			Logging.info(this, "retrieveProductsAllDepots, reload depot2LocalbootProducts == null "
					+ (depot2LocalbootProducts == null));
			Logging.info(this, "retrieveProductsAllDepots, reload productRows == null " + (productRows == null));
			Logging.info(this, "retrieveProductsAllDepots, reload depot2Packages == null " + (depot2Packages == null));

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_ON_DEPOT_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> packages = exec.getListOfMaps(omc);

			depot2LocalbootProducts = new Object2Product2VersionList();
			depot2NetbootProducts = new Object2Product2VersionList();
			product2VersionInfo2Depots = new HashMap<>();

			productRows = new ArrayList<>();

			depot2Packages = new HashMap<>();

			for (Map<String, Object> m : packages) {
				String depot = "" + m.get("depotId");

				if (!persistenceController.hasDepotPermission(depot)) {
					continue;
				}

				OpsiPackage p = new OpsiPackage(m);

				Logging.debug(this, "retrieveProductsAllDepots, opsi package " + p);

				if (p.isNetbootProduct()) {
					depot2NetbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				} else if (p.isLocalbootProduct()) {
					depot2LocalbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				} else {
					Logging.warning(this, "unexpected product type " + p.toString());
				}

				Map<String, List<String>> versionInfo2Depots = product2VersionInfo2Depots
						.computeIfAbsent(p.getProductId(), s -> new HashMap<>());

				List<String> depotsWithThisVersion = versionInfo2Depots.computeIfAbsent(p.getVersionInfo(),
						s -> new ArrayList<>());

				depotsWithThisVersion.add(depot);

				TreeSet<OpsiPackage> depotpackages = depot2Packages.computeIfAbsent(depot, s -> new TreeSet<>());
				depotpackages.add(p);

				List<Object> productRow = new ArrayList<>();

				productRow.add(p.getProductId());

				String productName = null;

				Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos = cacheManager
						.getCachedData(CacheIdentifier.PRODUCT_TO_VERION_INFO_TO_INFOS, Map.class);
				productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo()).getProductName();

				productRow.add(productName);
				p.appendValues(productRow);

				if (depotsWithThisVersion.size() == 1) {
					productRows.add(productRow);
				}
			}

			cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PACKAGES, depot2Packages);
			cacheManager.setCachedData(CacheIdentifier.PRODUCT_ROWS, productRows);
			cacheManager.setCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, product2VersionInfo2Depots);
			cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_LOCALBOOT_PRODUCTS, depot2LocalbootProducts);
			cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_NETBOOT_PRODUCTS, depot2NetbootProducts);
			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		retrieveProductInfos();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_TO_VERION_INFO_TO_INFOS, Map.class);
	}

	private void retrieveProductInfos() {
		Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_TO_VERION_INFO_TO_INFOS, Map.class);
		Logging.debug(this, "retrieveProductInfos data == null " + (product2versionInfo2infos == null));
		if (product2versionInfo2infos == null) {
			List<String> attribs = new ArrayList<>();

			for (String key : OpsiPackage.SERVICE_KEYS) {
				attribs.add(key);
			}

			for (String scriptKey : ActionRequest.getScriptKeys()) {
				attribs.add(scriptKey);
			}

			attribs.add(OpsiProductInfo.SERVICE_KEY_USER_LOGIN_SCRIPT);
			attribs.add(OpsiProductInfo.SERVICE_KEY_PRIORITY);

			attribs.remove(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE);
			attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE);
			attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_NAME);
			attribs.add(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION);

			String[] callAttributes = attribs.toArray(new String[] {});

			Logging.debug(this, "retrieveProductInfos callAttributes " + Arrays.asList(callAttributes));

			Map<String, Object> callFilter = new HashMap<>();

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

			product2versionInfo2infos = new HashMap<>();

			for (Map<String, Object> m : retrievedList) {
				String productId = "" + m.get(OpsiPackage.SERVICE_KEY_PRODUCT_ID0);
				String versionInfo = OpsiPackage.produceVersionInfo("" + m.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION),
						"" + m.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION));

				OpsiProductInfo productInfo = new OpsiProductInfo(m);
				Map<String, OpsiProductInfo> version2productInfos = product2versionInfo2infos.computeIfAbsent(productId,
						arg -> new HashMap<>());

				version2productInfos.put(versionInfo, productInfo);
			}

			Logging.debug(this, "retrieveProductInfos " + product2versionInfo2infos);

			cacheManager.setCachedData(CacheIdentifier.PRODUCT_TO_VERION_INFO_TO_INFOS, product2versionInfo2infos);
			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		retrieveClient2HwRows(hosts);
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS, Map.class);
	}

	private void retrieveClient2HwRows(String[] hosts) {
		Logging.info(this, "retrieveClient2HwRows( hosts )  for hosts " + hosts.length);

		Map<String, Map<String, Object>> client2HwRows = cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS,
				Map.class);
		if (client2HwRows != null) {
			Logging.info(this, "retrieveClient2HwRows client2HwRows.size() " + client2HwRows.size());
			return;
		}

		client2HwRows = new HashMap<>();

		// set default rows
		for (String host : persistenceController.getHostInfoCollections().getOpsiHostNames()) {
			Map<String, Object> nearlyEmptyHwRow = new HashMap<>();
			nearlyEmptyHwRow.put("HOST.hostId", host);

			String hostDescription = "";
			String macAddress = "";
			if (persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().get(host) != null) {
				hostDescription = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().get(host)
						.getDescription();
				macAddress = persistenceController.getHostInfoCollections().getMapOfPCInfoMaps().get(host)
						.getMacAddress();
			}
			nearlyEmptyHwRow.put("HOST.description", hostDescription);
			nearlyEmptyHwRow.put("HOST.hardwareAdress", macAddress);

			client2HwRows.put(host, nearlyEmptyHwRow);
		}

		TimeCheck timeCheck = new TimeCheck(this, " retrieveClient2HwRows all ");
		timeCheck.start();

		for (String hwClass : getHwInfoClassNames()) {
			Logging.info(this, "retrieveClient2HwRows hwClass " + hwClass);

			Map<String, Map<String, Object>> client2ClassInfos = client2HwRowsForHwClass(hwClass);

			if (!client2ClassInfos.isEmpty()) {
				for (Entry<String, Map<String, Object>> client2ClassInfo : client2ClassInfos.entrySet()) {
					Map<String, Object> allInfosForAClient = client2HwRows.get(client2ClassInfo.getKey());
					// find max lastseen time as last scan time

					String lastseen1 = (String) allInfosForAClient
							.get(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					String lastseen2 = (String) client2ClassInfo.getValue()
							.get(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					if (lastseen1 != null && lastseen2 != null) {
						client2ClassInfo.getValue().put(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME,
								maxTime(lastseen1, lastseen2));
					}

					allInfosForAClient.putAll(client2ClassInfo.getValue());
				}
			}
		}

		Logging.info(this, "retrieveClient2HwRows result size " + client2HwRows.size());
		cacheManager.setCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS, client2HwRows);

		timeCheck.stop();
		Logging.info(this, "retrieveClient2HwRows finished  ");
		persistenceController.notifyPanelCompleteWinProducts();
	}

	private Map<String, Map<String, Object>> client2HwRowsForHwClass(String hwClass) {
		Logging.info(this, "client2HwRowsForHwClass " + hwClass);

		Map<String, Map<String, Object>> client2HwRows = cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS,
				Map.class);
		if (client2HwRows == null) {
			return new HashMap<>();
		}

		// z.B. hwClass is DISK_PARTITION

		List<String> specificColumns = new ArrayList<>();
		specificColumns.add("HOST.hostId");

		StringBuilder buf = new StringBuilder("select HOST.hostId, ");
		StringBuilder cols = new StringBuilder("");

		String configTable = OpsiServiceNOMPersistenceController.HW_INFO_CONFIG + hwClass;

		String lastseenCol = configTable + "." + "lastseen";
		specificColumns.add(lastseenCol);
		buf.append(lastseenCol);
		buf.append(", ");

		boolean foundAnEntry = false;

		// build and collect database columnnames
		for (String hwInfoCol : getClient2HwRowsColumnNames()) {
			if (hwInfoCol.startsWith("HOST.")
					|| hwInfoCol.equals(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME)) {
				continue;
			}

			Logging.info(this,
					"hwInfoCol " + hwInfoCol + " look for " + OpsiServiceNOMPersistenceController.HW_INFO_DEVICE
							+ " as well as " + OpsiServiceNOMPersistenceController.HW_INFO_CONFIG);
			String part0 = hwInfoCol.substring(0, OpsiServiceNOMPersistenceController.HW_INFO_DEVICE.length());

			boolean colFound = false;
			// check if colname is from a CONFIG or a DEVICE table
			if (hwInfoCol.startsWith(hwClass, part0.length())) {
				colFound = true;
				// we found a DEVICE column name
			} else {
				part0 = hwInfoCol.substring(0, OpsiServiceNOMPersistenceController.HW_INFO_CONFIG.length());

				if (hwInfoCol.startsWith(hwClass, part0.length())) {
					colFound = true;
					// we found a CONFIG column name
				}

			}

			if (colFound) {
				cols.append(" ");
				cols.append(hwInfoCol);
				cols.append(",");
				specificColumns.add(hwInfoCol);
				foundAnEntry = true;
			}
		}

		if (!foundAnEntry) {
			Logging.info(this, "no columns found for hwClass " + hwClass);
			return new HashMap<>();
		}

		String deviceTable = OpsiServiceNOMPersistenceController.HW_INFO_DEVICE + hwClass;

		String colsS = cols.toString();
		buf.append(colsS.substring(0, colsS.length() - 1));

		buf.append(" \nfrom HOST ");

		buf.append(", ");
		buf.append(deviceTable);
		buf.append(", ");
		buf.append(configTable);

		buf.append("\n where ");

		buf.append("HOST.hostId");
		buf.append(" = ");
		buf.append(configTable);
		buf.append(".hostId");

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(".hardware_id");
		buf.append(" = ");
		buf.append(deviceTable);
		buf.append(".hardware_id");

		buf.append("\nAND ");
		buf.append(configTable);
		buf.append(".state = 1 ");

		String query = buf.toString();

		Logging.info(this, "retrieveClient2HwRows, query " + query);

		List<List<String>> rows = exec
				.getListOfStringLists(new OpsiMethodCall(RPCMethodName.GET_RAW_DATA, new Object[] { query }));
		Logging.info(this, "retrieveClient2HwRows, finished a request");
		Logging.info(this, "retrieveClient2HwRows, got rows for class " + hwClass);
		Logging.info(this, "retrieveClient2HwRows, got rows,  size  " + rows.size());

		// shrink to one line per client

		Map<String, Map<String, Object>> clientInfo = new HashMap<>();

		for (List<String> row : rows) {
			Map<String, Object> rowMap = clientInfo.computeIfAbsent(row.get(0), s -> new HashMap<>());

			for (int i = 1; i < specificColumns.size(); i++) {
				Object value = rowMap.get(specificColumns.get(i));
				String valInRow = row.get(i);
				if (valInRow == null || "null".equals(valInRow)) {
					valInRow = "";
				}

				if (value == null) {
					value = valInRow;
				} else {
					value = value + "|" + valInRow;
				}

				if (specificColumns.get(i).equals(lastseenCol)) {
					String timeS = maxTime((String) value, row.get(i));
					rowMap.put(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME, timeS);
				} else {
					rowMap.put(specificColumns.get(i), value);
				}

			}

		}

		Logging.info(this, "retrieveClient2HwRows, got clientInfo, with size " + clientInfo.size());
		return clientInfo;

		/*
		 * example
		 * SELECT HOST.hostId,
		 * HARDWARE_DEVICE_DISK_PARTITION.name,
		 * HARDWARE_DEVICE_DISK_PARTITION.description
		 * 
		 * from HOST, HARDWARE_DEVICE_DISK_PARTITION, HARDWARE_CONFIG_DISK_PARTITION
		 * where
		 * HOST.hostId = "vbrupertwin7-64.uib.local" and
		 * HARDWARE_DEVICE_DISK_PARTITION.hardware_id =
		 * HARDWARE_CONFIG_DISK_PARTITION.hardware_id
		 * 
		 * and HOST.hostId = HARDWARE_CONFIG_DISK_PARTITION.hostId
		 * 
		 * and HARDWARE_CONFIG_DISK_PARTITION.state=1 
		 * 
		 */

	}

	private static String maxTime(String time0, String time1) {
		if (time0 == null && time1 == null) {
			return null;
		}

		if (time0 == null || "".equals(time0)) {
			return time1;
		}

		if (time1 == null || "".equals(time1)) {
			return time0;
		}

		if (time0.compareTo(time1) < 0) {
			return time1;
		}

		return time0;
	}

	public Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitions() {
		retrieveAllProductPropertyDefinitions();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS, Map.class);
	}

	private void retrieveAllProductPropertyDefinitions() {
		retrieveProductsAllDepots();

		Map<String, Map<String, Map<String, ListCellOptions>>> depot2Product2PropertyDefinitions = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS, Map.class);
		if (depot2Product2PropertyDefinitions == null) {
			depot2Product2PropertyDefinitions = new HashMap<>();

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			Iterator<Map<String, Object>> iter = retrieved.iterator();

			while (iter.hasNext()) {

				Map<String, Object> retrievedMap = iter.next();
				Map<String, Object> adaptedMap = new HashMap<>(retrievedMap);
				// rebuild JSON objects
				Iterator<String> iterInner = retrievedMap.keySet().iterator();
				while (iterInner.hasNext()) {
					String key = iterInner.next();
					adaptedMap.put(key, retrievedMap.get(key));
				}

				ConfigOption productPropertyMap = new ConfigOption(adaptedMap);

				String propertyId = (String) retrievedMap.get("propertyId");
				String productId = (String) retrievedMap.get("productId");

				String productVersion = (String) retrievedMap.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
				String packageVersion = (String) retrievedMap.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
				String versionInfo = productVersion + ProductPackageVersionSeparator.FOR_KEY + packageVersion;

				Map<String, Map<String, List<String>>> product2VersionInfo2Depots = cacheManager
						.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
				if (product2VersionInfo2Depots.get(productId) == null
						|| product2VersionInfo2Depots.get(productId).get(versionInfo) == null) {
					Logging.debug(this,
							"retrieveAllProductPropertyDefinitions: no depot for " + productId + " version "
									+ versionInfo + "  product2VersionInfo2Depots.get(productId) "
									+ product2VersionInfo2Depots.get(productId));

				} else {
					for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
						Map<String, Map<String, ListCellOptions>> product2PropertyDefinitions = depot2Product2PropertyDefinitions
								.computeIfAbsent(depot, s -> new HashMap<>());

						Map<String, ListCellOptions> propertyDefinitions = product2PropertyDefinitions
								.computeIfAbsent(productId, s -> new HashMap<>());

						propertyDefinitions.put(propertyId, productPropertyMap);
					}
				}
			}

			cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_PROPERTY_DEFINITIONS,
					depot2Product2PropertyDefinitions);
			Logging.debug(this, "retrieveAllProductPropertyDefinitions ");

			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public Map<String, Map<String, List<Map<String, String>>>> getDepot2product2dependencyInfos() {
		retrieveAllProductDependencies();
		return cacheManager.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS, Map.class);
	}

	private void retrieveAllProductDependencies() {
		retrieveProductsAllDepots();

		Map<String, Map<String, List<Map<String, String>>>> depot2product2dependencyInfos = cacheManager
				.getCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS, Map.class);
		if (depot2product2dependencyInfos == null) {
			depot2product2dependencyInfos = new HashMap<>();

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_DEPENDENCY_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> retrievedList = exec.getListOfMaps(omc);

			for (Map<String, Object> dependencyItem : retrievedList) {
				String productId = "" + dependencyItem.get(OpsiPackage.DB_KEY_PRODUCT_ID);

				String productVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
				String packageVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
				String versionInfo = productVersion + ProductPackageVersionSeparator.FOR_KEY + packageVersion;

				String action = "" + dependencyItem.get("productAction");
				String requirementType = "";
				if (dependencyItem.get("requirementType") != null) {
					requirementType = "" + dependencyItem.get("requirementType");
				}

				String requiredProductId = "" + dependencyItem.get("requiredProductId");
				String requiredAction = "";
				if (dependencyItem.get("requiredAction") != null) {
					requiredAction = "" + dependencyItem.get("requiredAction");
				}
				String requiredInstallationStatus = "";

				if (dependencyItem.get("requiredInstallationStatus") != null) {
					requiredInstallationStatus = "" + dependencyItem.get("requiredInstallationStatus");
				}

				Map<String, Map<String, List<String>>> product2VersionInfo2Depots = cacheManager
						.getCachedData(CacheIdentifier.PRODUCT_TO_VERSION_INFO_TO_DEPOTS, Map.class);
				if (product2VersionInfo2Depots == null || product2VersionInfo2Depots.get(productId) == null
						|| product2VersionInfo2Depots.get(productId).get(versionInfo) == null) {
					Logging.warning(this, "unexpected null for product2VersionInfo2Depots productId, versionInfo   "
							+ productId + ", " + versionInfo);
					continue;
				}
				for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
					Map<String, List<Map<String, String>>> product2dependencyInfos = depot2product2dependencyInfos
							.computeIfAbsent(depot, s -> new HashMap<>());

					List<Map<String, String>> dependencyInfos = product2dependencyInfos.computeIfAbsent(productId,
							s -> new ArrayList<>());

					Map<String, String> dependencyInfo = new HashMap<>();
					dependencyInfo.put("action", action);
					dependencyInfo.put("requiredProductId", requiredProductId);
					dependencyInfo.put("requiredAction", requiredAction);
					dependencyInfo.put("requiredInstallationStatus", requiredInstallationStatus);
					dependencyInfo.put("requirementType", requirementType);

					dependencyInfos.add(dependencyInfo);
				}
			}

			cacheManager.setCachedData(CacheIdentifier.DEPOT_TO_PRODUCT_TO_DEPENDENCY_INFOS,
					depot2product2dependencyInfos);
			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public List<Map<String, Object>> getProductPropertyStates() {
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_PROPERTY_STATES, List.class);
	}

	public void fillProductPropertyStates(Collection<String> clients) {
		Logging.info(this, "fillProductPropertyStates for " + clients);
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_PROPERTY_STATES, produceProductPropertyStates(clients));
	}

	public List<Map<String, Object>> getProductPropertyDepotStates(Set<String> depots) {
		retrieveProductPropertyDepotStates(depots);
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEPOT_STATES, List.class);
	}

	private void retrieveProductPropertyDepotStates(Set<String> depots) {
		List<Map<String, Object>> productPropertyDepotStates = cacheManager
				.getCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEPOT_STATES, List.class);
		Logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots + " depotStates == null "
				+ (productPropertyDepotStates == null));
		if (productPropertyDepotStates == null) {
			productPropertyDepotStates = produceProductPropertyStates(depots);
			cacheManager.setCachedData(CacheIdentifier.PRODUCT_PROPERTY_DEPOT_STATES, productPropertyDepotStates);
		}
		Logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
	}

	// client is a set of added hosts, host represents the totality and will be
	// updated as a side effect
	private List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients) {
		Logging.info(this, "produceProductPropertyStates new hosts " + clients/* + " old hosts " + hosts */);
		List<String> newClients = null;
		if (clients == null) {
			newClients = new ArrayList<>();
		} else {
			newClients = new ArrayList<>(clients);
		}

		List<Map<String, Object>> result = null;

		if (newClients.isEmpty()) {
			// look if propstates is initialized
			result = new ArrayList<>();
		} else {
			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("objectId", newClients);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.PRODUCT_PROPERTY_STATE_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			result = exec.getListOfMaps(omc);
		}

		return result;
	}

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformation() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, NavigableMap.class);
	}

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
				NavigableMap.class);
	}

	public NavigableMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO, NavigableMap.class);
	}

	public NavigableMap<String, Set<String>> getName2SWIdents() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, NavigableMap.class);
	}

	public Map<String, List<SWAuditClientEntry>> getClient2Software() {
		Logging.info(this, "getClient2Software");
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_SOFTWARE, Map.class);
	}

	public List<String> getSoftwareList() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, List.class);
	}

	public NavigableMap<String, Integer> getSoftware2Number() {
		retrieveInstalledSoftwareInformation();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER, NavigableMap.class);
	}

	public String getSWident(Integer i) {
		Logging.debug(this, "getSWident for " + i);
		retrieveInstalledSoftwareInformation();
		List<String> softwareList = cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, List.class);
		if (softwareList == null || softwareList.size() < i + 1 || i == -1) {
			if (softwareList != null) {
				Logging.info(this, "getSWident " + " until now softwareList.size() " + softwareList.size());
			}

			boolean infoFound = false;

			// try reloading?
			int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("DataStub.reloadSoftwareInformation.text"),
					Configed.getResourceValue("DataStub.reloadSoftwareInformation.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				// installedSoftwareInformationRequestRefresh();
				retrieveInstalledSoftwareInformation();
				if (i > -1 && softwareList.size() >= i + 1) {
					infoFound = true;
				}
			}

			if (!infoFound) {
				Logging.warning(this, "missing softwareList entry " + i + " " + softwareList);
				return null;
			}
		}
		return softwareList.get(i);
	}

	private void retrieveInstalledSoftwareInformation() {
		NavigableMap<String, SWAuditEntry> installedSoftwareInformation = cacheManager
				.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, NavigableMap.class);
		NavigableMap<String, SWAuditEntry> installedSoftwareInformationForLicensing = cacheManager
				.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING, NavigableMap.class);
		NavigableMap<String, Map<String, String>> installedSoftwareName2SWinfo = cacheManager
				.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO, NavigableMap.class);
		NavigableMap<String, Set<String>> name2SWIdents = cacheManager.getCachedData(CacheIdentifier.NAME_TO_SW_IDENTS,
				NavigableMap.class);
		List<String> softwareList = cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, List.class);
		NavigableMap<String, Number> software2Number = cacheManager.getCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER,
				NavigableMap.class);
		if (installedSoftwareInformation == null || name2SWIdents == null) {

			String[] callAttributes = new String[] { SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME),
					// element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION),
					// key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.SUB_VERSION),
					// key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE),
					// key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE),
					SWAuditEntry.key2serverKey.get(SWAuditEntry.WINDOWS_SOFTWARE_ID) };
			Map<String, Object> callFilter = new HashMap<>();

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> li = exec.getListOfMaps(omc);

			Iterator<Map<String, Object>> iter = li.iterator();

			installedSoftwareInformation = new TreeMap<>();
			installedSoftwareInformationForLicensing = new TreeMap<>();
			name2SWIdents = new TreeMap<>();
			installedSoftwareName2SWinfo = new TreeMap<>();
			NavigableMap<String, Map<String, Map<String, String>>> name2ident2infoWithPool = new TreeMap<>();

			int i = 0;
			Logging.info(this, "getInstalledSoftwareInformation build map");

			while (iter.hasNext()) {
				i++;
				Map<String, Object> retrievedEntry = iter.next();

				SWAuditEntry entry = new SWAuditEntry(retrievedEntry);
				String swName = entry.get(SWAuditEntry.NAME);

				installedSoftwareInformation.put(entry.getIdent(), entry);

				boolean showForLicensing = true;
				for (String marker : linuxSWnameMarkers) {
					String version = entry.get(SWAuditEntry.VERSION);

					if (swName.indexOf(marker) > -1 || version.indexOf(marker) > -1) {
						showForLicensing = false;
						break;
					}
				}

				if (showForLicensing && !linuxSubversionMarkers.isEmpty()) {

					String subversion = entry.get(SWAuditEntry.SUB_VERSION);

					for (String marker : linuxSubversionMarkers) {
						if (subversion.startsWith(marker)) {
							showForLicensing = false;
							break;
						}
					}
				}

				if (showForLicensing) {
					installedSoftwareInformationForLicensing.put(entry.getIdent(), entry);

					Set<String> nameSWIdents = name2SWIdents.computeIfAbsent(swName, s -> new TreeSet<>());

					nameSWIdents.add(entry.getIdent());

					Map<String, String> identInfoRow = installedSoftwareName2SWinfo.get(swName);

					String infoString = "";

					if (identInfoRow == null) {
						identInfoRow = new LinkedHashMap<>();
						identInfoRow.put(SWAuditEntry.NAME, swName);
					} else {
						infoString = identInfoRow.get(SWAuditEntry.EXISTING_IDS);
						infoString = infoString + " - ";
					}

					infoString = infoString + entry.getIdentReduced();

					identInfoRow.put(SWAuditEntry.EXISTING_IDS, infoString);

					installedSoftwareName2SWinfo.put(swName, identInfoRow);

					Map<String, Map<String, String>> ident2infoWithPool = name2ident2infoWithPool
							.computeIfAbsent(swName, s -> new TreeMap<>());

					Map<String, String> infoWithPool = ident2infoWithPool.computeIfAbsent(entry.getIdent(),
							s -> new LinkedHashMap<>());

					String licencePoolAssigned = "x " + i;

					infoWithPool.put(SWAuditEntry.ID, entry.getIdent());
					infoWithPool.put(LicencepoolEntry.ID_SERVICE_KEY, licencePoolAssigned);
				}
			}

			softwareList = new ArrayList<>(installedSoftwareInformation.keySet());

			Logging.info(this,
					"retrieveInstalledSoftwareInformation produced softwarelist with entries " + softwareList.size());

			software2Number = new TreeMap<>();
			for (String sw : softwareList) {
				software2Number.put(sw, 0);
			}
			int n = 0;
			for (String sw : software2Number.keySet()) {
				if (sw.startsWith("NULL")) {
					Logging.info(this, "retrieveInstalledSoftwareInformation, we get index " + n + " for " + sw);
				}
				software2Number.put(sw, n);
				n++;
			}

			cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, installedSoftwareInformation);
			cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
					installedSoftwareInformationForLicensing);
			cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO,
					installedSoftwareName2SWinfo);
			cacheManager.setCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, name2SWIdents);
			cacheManager.setCachedData(CacheIdentifier.SOFTWARE_LIST, softwareList);
			cacheManager.setCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER, software2Number);

			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public Map<String, Set<String>> getSoftwareIdent2clients(List<String> clients) {
		Map<String, Set<String>> softwareIdent2clients = cacheManager
				.getCachedData(CacheIdentifier.SOFTWARE_IDENT_TO_CLIENTS, Map.class);
		if (softwareIdent2clients == null) {
			retrieveSoftwareIdentOnClients(clients);
		}
		return softwareIdent2clients;
	}

	private void retrieveSoftwareIdentOnClients(final List<String> clients) {
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Utils.usedMemory());
		final int STEP_SIZE = 100;

		Map<String, Set<String>> softwareIdent2clients = cacheManager
				.getCachedData(CacheIdentifier.SOFTWARE_IDENT_TO_CLIENTS, Map.class);
		if (softwareIdent2clients == null || !clients.isEmpty()) {
			softwareIdent2clients = new HashMap<>();

			while (!clients.isEmpty()) {
				List<String> clientListForCall = new ArrayList<>();

				for (int i = 0; i < STEP_SIZE && i < clients.size(); i++) {
					clientListForCall.add(clients.get(i));
				}

				clients.removeAll(clientListForCall);

				Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

				String[] callAttributes = new String[] {};
				Map<String, Object> callFilter = new HashMap<>();
				callFilter.put("state", 1);
				if (clients != null) {
					callFilter.put("clientId", clientListForCall);
				}

				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_ON_CLIENT_GET_OBJECTS,
						new Object[] { callAttributes, callFilter });
				List<Map<String, Object>> softwareAuditOnClients = exec.getListOfMaps(omc);

				Logging.info(this, "retrieveSoftwareAuditOnClients, finished a request, map size "
						+ softwareAuditOnClients.size());

				for (Map<String, Object> item : softwareAuditOnClients) {
					SWAuditClientEntry clientEntry = new SWAuditClientEntry(item);
					Set<String> clientsWithThisSW = softwareIdent2clients.computeIfAbsent(clientEntry.getSWident(),
							s -> new HashSet<>());
					clientsWithThisSW.add(clientEntry.getClientId());
				}

				Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");
			}

			cacheManager.setCachedData(CacheIdentifier.SOFTWARE_IDENT_TO_CLIENTS, softwareIdent2clients);
			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());
			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());
			persistenceController.notifyPanelCompleteWinProducts();
		}
	}

	public AuditSoftwareXLicencePool getAuditSoftwareXLicencePool() {
		retrieveAuditSoftwareXLicencePool();
		return cacheManager.getCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL,
				AuditSoftwareXLicencePool.class);
	}

	private void retrieveAuditSoftwareXLicencePool() {
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = cacheManager
				.getCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL, AuditSoftwareXLicencePool.class);
		if (auditSoftwareXLicencePool != null) {
			return;
		}

		Logging.info(this, "retrieveAuditSoftwareXLicencePool");

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_GET_OBJECTS,
				new Object[] { AuditSoftwareXLicencePool.SERVICE_ATTRIBUTES, new HashMap<>() });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

		auditSoftwareXLicencePool = new AuditSoftwareXLicencePool();

		for (Map<String, Object> map : retrieved) {
			auditSoftwareXLicencePool.integrateRaw(map);
		}

		cacheManager.setCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL, auditSoftwareXLicencePool);
		Logging.info(this, "retrieveAuditSoftwareXLicencePool retrieved ");
	}

	public Map<String, Map<String, Object>> getConfigs() {
		retrieveHostConfigs();
		return cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS, Map.class);
	}

	private void retrieveHostConfigs() {
		Map<String, Map<String, Object>> hostConfigs = cacheManager.getCachedData(CacheIdentifier.HOST_CONFIGS,
				Map.class);
		if (hostConfigs != null) {
			return;
		}

		Logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.CONFIG_STATE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		hostConfigs = new HashMap<>();

		for (Map<String, Object> listElement : retrieved) {
			Object id = listElement.get("objectId");

			if (id instanceof String && !"".equals(id)) {
				String hostId = (String) id;
				Map<String, Object> configs1Host = hostConfigs.computeIfAbsent(hostId, arg -> new HashMap<>());

				Logging.debug(this, "retrieveHostConfigs objectId,  element " + id + ": " + listElement);

				String configId = (String) listElement.get("configId");

				if (listElement.get("values") == null) {
					configs1Host.put(configId, new ArrayList<>());
					// is a data error but can occur
				} else {
					configs1Host.put(configId, listElement.get("values"));
				}
			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved " + hostConfigs.keySet());

		cacheManager.setCachedData(CacheIdentifier.HOST_CONFIGS, hostConfigs);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public Map<String, LicencepoolEntry> getLicencepools() {
		retrieveLicencepools();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS, Map.class);
	}

	private void retrieveLicencepools() {
		Map<String, LicencepoolEntry> licencepools = cacheManager.getCachedData(CacheIdentifier.LICENSE_POOLS,
				Map.class);
		if (licencepools != null) {
			return;
		}

		licencepools = new TreeMap<>();

		if (isWithLicenceManagement()) {
			String[] attributes = new String[] { LicencepoolEntry.ID_KEY, LicencepoolEntry.DESCRIPTION_KEY };

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS,
					new Object[] { attributes, new HashMap<>() });
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicencepoolEntry entry = new LicencepoolEntry(importedEntry);
				licencepools.put(entry.getLicencepoolId(), entry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOLS, licencepools);
	}

	public Map<String, LicenceContractEntry> getLicenceContracts() {
		retrieveLicenceContracts();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class);
	}

	// date in sql time format, contract ID
	public NavigableMap<String, NavigableSet<String>> getLicenceContractsToNotify() {
		retrieveLicenceContracts();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, NavigableMap.class);
	}

	// LICENSE_CONTRACT
	private void retrieveLicenceContracts() {
		Map<String, LicenceContractEntry> licenceContracts = cacheManager
				.getCachedData(CacheIdentifier.LICENSE_CONTRACTS, Map.class);
		NavigableMap<String, NavigableSet<String>> contractsToNotify = cacheManager
				.getCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, NavigableMap.class);
		if (licenceContracts != null) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		licenceContracts = new HashMap<>();
		contractsToNotify = new TreeMap<>();

		if (isWithLicenceManagement()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_CONTRACT_GET_OBJECTS, new Object[] {});
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);

				String notiDate = entry.get(TableLicenceContracts.NOTIFICATION_DATE_KEY);
				if (notiDate != null && notiDate.trim().length() > 0 && notiDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsToNotify.computeIfAbsent(notiDate,
							s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}
			}

			Logging.info(this, "contractsToNotify " + contractsToNotify);
		}

		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS, licenceContracts);
		cacheManager.setCachedData(CacheIdentifier.LICENSE_CONTRACTS_TO_NOTIFY, contractsToNotify);
	}

	public Map<String, LicenceEntry> getLicences() {
		retrieveLicences();
		return cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class);
	}

	private void retrieveLicences() {
		Map<String, LicenceEntry> licences = cacheManager.getCachedData(CacheIdentifier.LICENSES, Map.class);
		if (licences != null) {
			return;
		}

		licences = new HashMap<>();

		if (isWithLicenceManagement()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_GET_OBJECTS, new Object[0]);
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceEntry entry = new LicenceEntry(importedEntry);
				licences.put(entry.getId(), entry);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.LICENSES, licences);
	}

	public List<LicenceUsableForEntry> getLicenceUsabilities() {
		retrieveLicenceUsabilities();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES, List.class);
	}

	// SOFTWARE_LICENSE_TO_LICENSE_POOL
	private void retrieveLicenceUsabilities() {
		List<LicenceUsableForEntry> licenceUsabilities = cacheManager.getCachedData(CacheIdentifier.LICENSE_USABILITIES,
				List.class);
		if (licenceUsabilities != null) {
			return;
		}

		licenceUsabilities = new ArrayList<>();

		if (isWithLicenceManagement()) {

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_GET_OBJECTS,
					new Object[0]);
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsableForEntry entry = LicenceUsableForEntry.produceFrom(importedEntry);
				licenceUsabilities.add(entry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USABILITIES, licenceUsabilities);
	}

	public List<LicenceUsageEntry> getLicenceUsages() {
		retrieveLicenceUsages();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class);
	}

	private void retrieveLicenceUsages() {
		Logging.info(this, "retrieveLicenceUsages");
		List<LicenceUsageEntry> licenceUsages = cacheManager.getCachedData(CacheIdentifier.LICENSE_USAGE, List.class);
		if (licenceUsages != null) {
			return;
		}

		licenceUsages = new ArrayList<>();

		if (isWithLicenceManagement()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_ON_CLIENT_GET_OBJECTS, new Object[0]);
			List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsageEntry entry = new LicenceUsageEntry(importedEntry);

				licenceUsages.add(entry);
			}
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_USAGE, licenceUsages);
	}

	public LicencePoolXOpsiProduct getLicencePoolXOpsiProduct() {
		retrieveLicencePoolXOpsiProduct();
		return cacheManager.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, LicencePoolXOpsiProduct.class);
	}

	private void retrieveLicencePoolXOpsiProduct() {
		LicencePoolXOpsiProduct licencePoolXOpsiProduct = cacheManager
				.getCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, LicencePoolXOpsiProduct.class);
		if (licencePoolXOpsiProduct != null) {
			return;
		}

		Logging.info(this, "retrieveLicencePoolXOpsiProduct");

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.LICENSE_POOL_GET_OBJECTS,
				new Object[] { LicencePoolXOpsiProduct.SERVICE_ATTRIBUTES_asArray, new HashMap<>() });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		// integrates two database calls

		licencePoolXOpsiProduct = new LicencePoolXOpsiProduct();

		for (Map<String, Object> map : retrieved) {
			licencePoolXOpsiProduct.integrateRawFromService(map);
		}
		cacheManager.setCachedData(CacheIdentifier.LICENSE_POOL_X_OPSI_PRODUCT, licencePoolXOpsiProduct);
	}

	public List<Map<String, Object>> checkHealth() {
		retrieveHealthData();
		return cacheManager.getCachedData(CacheIdentifier.HEALTH_CHECK_DATA, List.class);
	}

	private void retrieveHealthData() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SERVICE_HEALTH_CHECK, new Object[0]);
		cacheManager.setCachedData(CacheIdentifier.HEALTH_CHECK_DATA, exec.getListOfMaps(omc));
	}

	public Map<String, Object> getDiagnosticData() {
		retrieveDiagnosticData();
		return cacheManager.getCachedData(CacheIdentifier.DIAGNOSTIC_DATA, Map.class);
	}

	private void retrieveDiagnosticData() {
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SERVICE_GET_DIAGNOSTIC_DATA, new Object[0]);
		cacheManager.setCachedData(CacheIdentifier.DIAGNOSTIC_DATA, exec.getMapResult(omc));
	}

	public List<String> getClient2HwRowsColumnNames() {
		retrieveClient2HwRowsColumnNames();
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, List.class);
	}

	public List<String> getClient2HwRowsJavaclassNames() {
		retrieveClient2HwRowsColumnNames();
		return cacheManager.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, List.class);
	}

	public List<String> getHwInfoClassNames() {
		retrieveClient2HwRowsColumnNames();
		return cacheManager.getCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, List.class);
	}

	private void retrieveClient2HwRowsColumnNames() {
		getConfigOptions();

		List<String> hostColumnNames = cacheManager.getCachedData(CacheIdentifier.HOST_COLUMN_NAMES, List.class);
		List<String> hwInfoClassNames = cacheManager.getCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, List.class);
		List<String> client2HwRowsColumnNames = cacheManager
				.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, List.class);
		List<String> client2HwRowsJavaclassNames = cacheManager
				.getCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, List.class);

		Logging.info(this, "retrieveClient2HwRowsColumnNames " + "client2HwRowsColumnNames == null "
				+ (client2HwRowsColumnNames == null));
		if (client2HwRowsColumnNames == null || hwInfoClassNames == null || client2HwRowsJavaclassNames == null) {
			hostColumnNames = new ArrayList<>();

			// todo make static variables
			hostColumnNames.add("HOST.hostId");
			hostColumnNames.add("HOST.description");
			hostColumnNames.add("HOST.hardwareAdress");
			hostColumnNames.add(OpsiServiceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);

			getConfigOptions();
			// there is produced client2HwRowsColumnNames

			client2HwRowsColumnNames = new ArrayList<>(hostColumnNames);

			Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses = cacheManager
					.getCachedData(CacheIdentifier.HW_AUDIT_DEVICE_CLASSES, Map.class);
			for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
				OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClass.getValue();

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHostProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = OpsiServiceNOMPersistenceController.HW_INFO_CONFIG + hwClass.getKey() + "."
								+ deviceProperty.getOpsiDbColumnName();
						client2HwRowsColumnNames.add(col);
					}
				}

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHwItemProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = OpsiServiceNOMPersistenceController.HW_INFO_DEVICE + hwClass.getKey() + "."
								+ deviceProperty.getOpsiDbColumnName();
						client2HwRowsColumnNames.add(col);
					}
				}
			}
			client2HwRowsJavaclassNames = new ArrayList<>();
			Set<String> hwInfoClasses = new HashSet<>();

			for (String columnName : client2HwRowsColumnNames) {
				Logging.info(this, "retrieveClient2HwRowsColumnNames col " + columnName);
				client2HwRowsJavaclassNames.add("java.lang.String");
				String className = cutClassName(columnName);
				if (className != null) {
					hwInfoClasses.add(className);
				}
			}

			hwInfoClassNames = new ArrayList<>(hwInfoClasses);

			Logging.info(this, "retrieveClient2HwRowsColumnNames hwInfoClassNames " + hwInfoClassNames);
			cacheManager.setCachedData(CacheIdentifier.HOST_COLUMN_NAMES, hostColumnNames);
			cacheManager.setCachedData(CacheIdentifier.HW_INFO_CLASS_NAMES, hwInfoClassNames);
			cacheManager.setCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_COLUMN_NAMES, client2HwRowsColumnNames);
			cacheManager.setCachedData(CacheIdentifier.CLIENT_TO_HW_ROWS_JAVA_CLASS_NAMES, client2HwRowsJavaclassNames);
		}
	}

	private String cutClassName(String columnName) {
		String result = null;

		if (!columnName.startsWith("HOST")
				&& columnName.startsWith(OpsiServiceNOMPersistenceController.HW_INFO_CONFIG)) {
			result = columnName.substring(OpsiServiceNOMPersistenceController.HW_INFO_CONFIG.length());
			result = result.substring(0, result.indexOf('.'));
		} else if (columnName.startsWith(OpsiServiceNOMPersistenceController.HW_INFO_DEVICE)) {
			result = columnName.substring(OpsiServiceNOMPersistenceController.HW_INFO_DEVICE.length());
			result = result.substring(0, result.indexOf('.'));
		} else {
			Logging.warning(this, "cutClassName " + "unexpected columnName " + columnName);
		}

		return result;
	}
}
