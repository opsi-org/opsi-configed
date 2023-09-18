/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostGroups;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.utilities.datastructure.StringValuedRelationElement;
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
	private ConfigDataService configDataService;

	public GroupDataService(AbstractExecutioner exec) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public Map<String, Map<String, String>> getProductGroupsPD() {
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

	public Map<String, Map<String, String>> getHostGroupsPD() {
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
	public Map<String, Set<String>> getFObject2GroupsPD() {
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

	public Map<String, Set<String>> getFGroup2MembersPD() {
		Map<String, Set<String>> fGroup2Members = cacheManager.getCachedData(CacheIdentifier.FGROUP_TO_MEMBERS,
				Map.class);
		if (fGroup2Members == null) {
			fGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId");
		}
		return fGroup2Members;
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

	private static Map<String, Set<String>> projectToFunction(Map<String, Map<String, String>> mappedRelation,
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

	public List<String> getHostGroupIds() {
		Set<String> groups = getHostGroupsPD().keySet();
		groups.remove(ClientTree.DIRECTORY_NAME);
		return new ArrayList<>(groups);
	}

	public Map<String, Set<String>> getFProductGroup2Members() {
		Map<String, Set<String>> fProductGroup2Members = cacheManager
				.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class);
		if (fProductGroup2Members == null) {
			fProductGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId");
			cacheManager.setCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, fProductGroup2Members);
		}
		return fProductGroup2Members;
	}

	public boolean addHosts2Group(List<String> objectIds, String groupId) {
		if (configDataService.isGlobalReadOnly()) {
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
		if (configDataService.isGlobalReadOnly()) {
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

	public boolean addObject2Group(String objectId, String groupId) {
		if (configDataService.isGlobalReadOnly()) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE,
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	public boolean removeHostGroupElements(Iterable<Object2GroupEntry> entries) {
		if (configDataService.isGlobalReadOnly()) {
			return false;
		}

		List<Map<String, Object>> deleteItems = new ArrayList<>();
		for (Object2GroupEntry entry : entries) {
			Map<String, Object> deleteItem = Utils.createNOMitem(Object2GroupEntry.TYPE_NAME);
			deleteItem.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			deleteItem.put(Object2GroupEntry.GROUP_ID_KEY, entry.getGroupId());
			deleteItem.put(Object2GroupEntry.MEMBER_KEY, entry.getMember());

			deleteItems.add(deleteItem);
		}

		boolean result = true;
		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE_OBJECTS,
					new Object[] { deleteItems.toArray() });

			if (exec.doCall(omc)) {
				deleteItems.clear();
			} else {
				result = false;
			}
		}

		return result;
	}

	public boolean removeObject2Group(String objectId, String groupId) {
		if (configDataService.isGlobalReadOnly()) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE,
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	public boolean addGroup(StringValuedRelationElement newgroup) {
		return addGroup(newgroup, true);
	}

	public boolean addGroup(StringValuedRelationElement newgroup, boolean requestRefresh) {
		if (!configDataService.hasServerFullPermissionPD()) {
			return false;
		}

		Logging.debug(this, "addGroup : " + newgroup + " requestRefresh " + requestRefresh);

		String id = newgroup.get("groupId");
		String parentId = newgroup.get("parentGroupId");
		if (parentId == null || parentId.equals(ClientTree.ALL_GROUPS_NAME)) {
			parentId = null;
		}

		parentId = ClientTree.translateToPersistentName(parentId);

		if (id.equalsIgnoreCase(parentId)) {
			Logging.error(this, "Cannot add group as child to itself, group ID " + id);
			return false;
		}

		String description = newgroup.get("description");
		String notes = "";

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_CREATE_HOST_GROUP,
				new Object[] { id, description, notes, parentId });
		boolean result = exec.doCall(omc);
		if (result) {
			//hostGroupsRequestRefresh();
		}

		return result;

	}

	public boolean deleteGroup(String groupId) {
		if (!configDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_DELETE, new String[] { groupId });
		boolean result = exec.doCall(omc);

		if (result) {
			// hostGroupsRequestRefresh();
		}

		return result;
	}

	public boolean updateGroup(String groupId, Map<String, String> updateInfo) {
		if (!configDataService.hasServerFullPermissionPD()) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		if (updateInfo == null) {
			updateInfo = new HashMap<>();
		}

		updateInfo.put("ident", groupId);
		updateInfo.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		if (updateInfo.get("parentGroupId").equals(ClientTree.ALL_GROUPS_NAME)) {
			updateInfo.put("parentGroupId", "null");
		}

		String parentGroupId = updateInfo.get("parentGroupId");
		parentGroupId = ClientTree.translateToPersistentName(parentGroupId);
		updateInfo.put("parentGroupId", parentGroupId);

		Logging.debug(this, "updateGroup " + parentGroupId);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_UPDATE_OBJECT, new Object[] { updateInfo });
		return exec.doCall(omc);
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		if (!configDataService.hasServerFullPermissionPD()) {
			return false;
		}

		Logging.debug(this, "setProductGroup: groupId " + groupId);
		if (groupId == null) {
			return false;
		}

		Logging.info(this, "setProductGroup: groupId " + groupId + " should have members " + productSet);

		boolean result = true;

		Map<String, String> map = new HashMap<>();

		map.put("id", groupId);
		map.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		if (description != null) {
			map.put("description", description);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_CREATE_OBJECTS,
				new Object[] { new Object[] { map } });
		result = exec.doCall(omc);

		HashSet<String> inNewSetnotInOriSet = new HashSet<>(productSet);
		HashSet<String> inOriSetnotInNewSet = new HashSet<>();

		if (getFProductGroup2Members().get(groupId) != null) {
			Set<String> oriSet = getFProductGroup2Members().get(groupId);
			Logging.debug(this, "setProductGroup: oriSet " + oriSet);
			inOriSetnotInNewSet = new HashSet<>(oriSet);
			inOriSetnotInNewSet.removeAll(productSet);
			inNewSetnotInOriSet.removeAll(oriSet);
		}

		Logging.info(this, "setProductGroup: inOriSetnotInNewSet, inNewSetnotInOriSet. " + inOriSetnotInNewSet + ", "
				+ inNewSetnotInOriSet);

		final Map<String, String> typingObject = new HashMap<>();
		typingObject.put("groupType", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		typingObject.put("type", Object2GroupEntry.TYPE_NAME);

		List<Map<String, String>> object2Groups = new ArrayList<>();
		for (String objectId : inOriSetnotInNewSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(m);
		}

		Logging.debug(this, "delete objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			result = result && exec.doCall(
					new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_DELETE_OBJECTS, new Object[] { object2Groups }));
		}

		object2Groups.clear();
		for (String objectId : inNewSetnotInOriSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(m);
		}

		Logging.debug(this, "create new objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			result = result && exec.doCall(
					new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE_OBJECTS, new Object[] { object2Groups }));
		}

		if (result) {
			getFProductGroup2Members().put(groupId, productSet);
		}

		return result;
	}
}
