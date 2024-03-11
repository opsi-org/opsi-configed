/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.tree.AbstractGroupTree;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostGroups;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * Provides methods for working with group data on the server.
 * <p>
 * Classes ending in {@code DataService} represent somewhat of a layer between
 * server and the client. It enables to work with specific data, that is saved
 * on the server.
 * <p>
 * {@code DataService} classes only allow to retrieve and update data. Data may
 * be internally cached. The internally cached data is identified by a method
 * name. If a method name ends in {@code PD}, it means that method either
 * retrieves or it updates internally cached data. {@code PD} stands for
 * {@code Persistent Data}.
 */
@SuppressWarnings({ "unchecked" })
public class GroupDataService {
	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private UserRolesConfigDataService userRolesConfigDataService;
	private OpsiServiceNOMPersistenceController persistenceController;

	public GroupDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public Map<String, Map<String, String>> getProductGroupsPD() {
		retrieveProductGroupsPD();
		return cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS, Map.class);
	}

	public void retrieveProductGroupsPD() {
		if (cacheManager.isDataCached(CacheIdentifier.PRODUCT_GROUPS)) {
			return;
		}
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		Map<String, Map<String, String>> result = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.GROUP_GET_OBJECTS, new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS, result);
	}

	public Map<String, Map<String, String>> getHostGroupsPD() {
		retrieveHostGroupsPD();
		return cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS, Map.class);
	}

	public void retrieveHostGroupsPD() {
		if (cacheManager.isDataCached(CacheIdentifier.HOST_GROUPS)) {
			return;
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
	}

	public void retrieveAllGroupsPD() {
		// Don't load when one of the two is not null
		// We only want to load, when both are not yet loaded
		if (cacheManager.isDataCached(CacheIdentifier.PRODUCT_GROUPS)
				|| cacheManager.isDataCached(CacheIdentifier.HOST_GROUPS)) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_GET_OBJECTS, new Object[0]);

		List<Map<String, Object>> resultlist = exec.getListOfMaps(omc);

		List<Object> hostGroupsList = new ArrayList<>();
		List<Object> productGroupsList = new ArrayList<>();

		for (Map<String, Object> entry : resultlist) {
			if (entry.get("type").equals(Object2GroupEntry.GROUP_TYPE_HOSTGROUP)) {
				hostGroupsList.add(entry);
			} else if (entry.get("type").equals(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP)) {
				productGroupsList.add(entry);
			} else {
				Logging.warning(this, "Unexpected type: " + entry.get(Object2GroupEntry.GROUP_TYPE_KEY));
			}
		}

		// Load data for hostGroups
		Map<String, Map<String, String>> source = AbstractPOJOExecutioner.generateStringMappedObjectsByKeyResult(
				hostGroupsList, "ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });

		HostGroups hostGroups = new HostGroups(source);
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups = hostGroups.addSpecialGroups();
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups.alterToWorkingVersion();
		Logging.debug(this, "getHostGroups rebuilt" + hostGroups);
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS, hostGroups);

		// Load data for productGroups
		Map<String, Map<String, String>> result = AbstractPOJOExecutioner.generateStringMappedObjectsByKeyResult(
				productGroupsList, "ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });
		cacheManager.setCachedData(CacheIdentifier.PRODUCT_GROUPS, result);
	}

	public Map<String, Set<String>> getFProductGroup2Members() {
		retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId",
				CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS);
		return cacheManager.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class);
	}

	public Map<String, Set<String>> getFHostGroup2MembersPD() {
		retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId", CacheIdentifier.FGROUP_TO_MEMBERS);
		return cacheManager.getCachedData(CacheIdentifier.FGROUP_TO_MEMBERS, Map.class);
	}

	// returns the function that yields for a given groupId all objects which belong
	// to the group
	public void retrieveFGroup2Members(String groupType, String memberIdName, CacheIdentifier cacheId) {
		if (cacheManager.isDataCached(cacheId)) {
			return;
		}
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", groupType);
		Map<String, Map<String, String>> mappedRelations = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS,
						new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "objectId", "groupId" }, new String[] { memberIdName, "groupId" });
		cacheManager.setCachedData(cacheId, projectToFunction(mappedRelations, "groupId", memberIdName));
	}

	// returns the function that yields for a given clientId all groups to which the
	// client belongs
	public Map<String, Set<String>> getFObject2GroupsPD() {
		retrieveFObject2GroupsPD();
		return cacheManager.getCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, Map.class);
	}

	public void retrieveFObject2GroupsPD() {
		if (cacheManager.isDataCached(CacheIdentifier.FOBJECT_TO_GROUPS)) {
			return;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		Map<String, Map<String, String>> mappedRelations = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS,
						new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" });
		Map<String, Set<String>> fObject2Groups = projectToFunction(mappedRelations, "clientId", "groupId");
		cacheManager.setCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, fObject2Groups);
	}

	public void retrieveAllObject2GroupsPD() {
		// Don't load when one of the two is not null
		// We only want to load, when both are not yet loaded
		if (cacheManager.isDataCached(CacheIdentifier.FOBJECT_TO_GROUPS)
				|| cacheManager.isDataCached(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS)) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS, new Object[] {});

		List<Map<String, Object>> resultlist = exec.getListOfMaps(omc);

		List<Object> hostGroupsList = new ArrayList<>();
		List<Object> productGroupsList = new ArrayList<>();

		for (Map<String, Object> entry : resultlist) {
			if (entry.get(Object2GroupEntry.GROUP_TYPE_KEY).equals(Object2GroupEntry.GROUP_TYPE_HOSTGROUP)) {
				hostGroupsList.add(entry);
			} else if (entry.get(Object2GroupEntry.GROUP_TYPE_KEY).equals(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP)) {
				productGroupsList.add(entry);
			} else {
				Logging.warning(this, "Unexpected " + Object2GroupEntry.GROUP_TYPE_KEY + ": "
						+ entry.get(Object2GroupEntry.GROUP_TYPE_KEY));
			}
		}

		// Generate data for host groups
		Map<String, Map<String, String>> mappedRelationsHostGroups = AbstractPOJOExecutioner
				.generateStringMappedObjectsByKeyResult(hostGroupsList, "ident", new String[] { "objectId", "groupId" },
						new String[] { "clientId", "groupId" });

		Map<String, Set<String>> fObject2Groups = projectToFunction(mappedRelationsHostGroups, "clientId", "groupId");
		cacheManager.setCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, fObject2Groups);

		// generate data for product groups
		Map<String, Map<String, String>> mappedRelationsProductGroups = AbstractPOJOExecutioner
				.generateStringMappedObjectsByKeyResult(productGroupsList, "ident",
						new String[] { "objectId", "groupId" }, new String[] { "productId", "groupId" });

		cacheManager.setCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS,
				projectToFunction(mappedRelationsProductGroups, "groupId", "productId"));
	}

	private static Map<String, Set<String>> projectToFunction(Map<String, Map<String, String>> mappedRelation,
			String originVar, String imageVar) {
		Map<String, Set<String>> result = new TreeMap<>();
		for (Map<String, String> relation : mappedRelation.values()) {
			String imageValue = relation.get(imageVar);
			if (imageValue != null) {
				String originValue = relation.get(originVar);
				Set<String> assignedSet = result.computeIfAbsent(originValue, arg -> new TreeSet<>());
				assignedSet.add(imageValue);
			}
		}

		return result;
	}

	public List<String> getHostGroupIds() {
		Set<String> groups = getHostGroupsPD().keySet();
		groups.remove(ClientTree.DIRECTORY_NAME);
		return new ArrayList<>(groups);
	}

	public boolean addHosts2Group(List<String> objectIds, String groupId) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		Logging.info(this, "addHosts2Group hosts " + objectIds + " group " + groupId);
		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		List<Map<String, Object>> data = new ArrayList<>();

		for (String ob : objectIds) {
			Map<String, Object> item = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, ob);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			data.add(item);
		}

		Logging.info(this, "addHosts2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { data });
		return exec.doCall(omc);
	}

	public boolean addHost2Groups(String objectId, List<String> groupIds) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		Logging.info(this, "addHost2Groups host " + objectId + " groups " + groupIds);
		List<Map<String, Object>> data = new ArrayList<>();

		for (String groupId : groupIds) {
			String persistentGroupId = ClientTree.translateToPersistentName(groupId);
			Map<String, Object> item = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, objectId);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			data.add(item);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { data });
		return exec.doCall(omc);
	}

	public boolean addObject2Group(String objectId, String groupId, boolean isHostGroup) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);

		String groupType = isHostGroup ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP
				: Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP;
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE,
				new String[] { groupType, persistentGroupId, objectId });

		boolean result = exec.doCall(omc);
		if (result) {
			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());
		}

		return result;
	}

	public boolean removeHostGroupElements(Iterable<Object2GroupEntry> entries, boolean isHostGroup) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		String productType = isHostGroup ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP
				: Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP;

		List<Map<String, Object>> deleteItems = new ArrayList<>();
		for (Object2GroupEntry entry : entries) {
			Map<String, Object> deleteItem = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			deleteItem.put(Object2GroupEntry.GROUP_TYPE_KEY, productType);
			deleteItem.put(Object2GroupEntry.GROUP_ID_KEY, entry.getGroupId());
			deleteItem.put(Object2GroupEntry.MEMBER_KEY, entry.getMember());

			deleteItems.add(deleteItem);
		}

		boolean result = true;
		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE_OBJECTS,
					new Object[] { deleteItems });

			result = exec.doCall(omc);

			if (result) {
				persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());
			}
		}

		return result;
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE,
				new String[] { null, persistentGroupId, objectId });

		boolean result = exec.doCall(omc);

		if (result) {
			persistenceController.reloadData(CacheIdentifier.FOBJECT_TO_GROUPS.toString());
		}

		return result;
	}

	public boolean addGroup(Map<String, String> newgroup, boolean isHostGroup) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		String id = newgroup.get("groupId");
		String parentId = newgroup.get("parentGroupId");
		if (parentId == null || parentId.equals(AbstractGroupTree.ALL_GROUPS_NAME)) {
			parentId = null;
		}

		parentId = ClientTree.translateToPersistentName(parentId);

		if (id.equalsIgnoreCase(parentId)) {
			Logging.error(this, "Cannot add group as child to itself, group ID " + id);
			return false;
		}

		String description = newgroup.get("description");

		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("type",
				isHostGroup ? Object2GroupEntry.GROUP_TYPE_HOSTGROUP : Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		map.put("description", description);
		map.put("parentGroupId", parentId);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_CREATE_OBJECTS, new Object[] { map });
		boolean result = exec.doCall(omc);
		if (result) {
			CacheIdentifier identifier = isHostGroup ? CacheIdentifier.HOST_GROUPS : CacheIdentifier.PRODUCT_GROUPS;
			persistenceController.reloadData(identifier.toString());
		}

		return result;
	}

	public boolean deleteGroup(String groupId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_DELETE, new String[] { groupId });
		boolean result = exec.doCall(omc);

		if (result) {
			persistenceController.reloadData(CacheIdentifier.HOST_GROUPS.toString());
		}

		return result;
	}

	public boolean updateGroup(String groupId, Map<String, String> updateInfo, boolean isHostGroup) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		if (updateInfo == null) {
			updateInfo = new HashMap<>();
		}

		updateInfo.put("ident", groupId);
		if (isHostGroup) {
			updateInfo.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
		} else {
			updateInfo.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		}

		if (updateInfo.get("parentGroupId").equals(AbstractGroupTree.ALL_GROUPS_NAME)) {
			updateInfo.put("parentGroupId", "null");
		}

		String parentGroupId = updateInfo.get("parentGroupId");
		parentGroupId = ClientTree.translateToPersistentName(parentGroupId);
		updateInfo.put("parentGroupId", parentGroupId);

		Logging.debug(this, "updateGroup " + parentGroupId);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_UPDATE_OBJECT, new Object[] { updateInfo });
		boolean result = exec.doCall(omc);

		if (result) {
			CacheIdentifier identifier = isHostGroup ? CacheIdentifier.HOST_GROUPS : CacheIdentifier.PRODUCT_GROUPS;
			persistenceController.reloadData(identifier.toString());
		}

		return result;
	}
}
