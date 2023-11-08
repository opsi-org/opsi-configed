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
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostGroups;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
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
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS, Map.class) != null) {
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
		if (cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS, Map.class) != null) {
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
		if (cacheManager.getCachedData(CacheIdentifier.PRODUCT_GROUPS, Map.class) != null
				|| cacheManager.getCachedData(CacheIdentifier.HOST_GROUPS, Map.class) != null) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.GROUP_GET_OBJECTS, new Object[0]);

		List<Map<String, Object>> resultlist = exec.getListOfMaps(omc);

		List<Object> hostGroupsList = new ArrayList<>();
		List<Object> productGroupsList = new ArrayList<>();

		Iterator<Map<String, Object>> iter = resultlist.iterator();

		while (iter.hasNext()) {
			Map<String, Object> entry = iter.next();

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
				hostGroupsList.iterator(), "ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" }, null);

		HostGroups hostGroups = new HostGroups(source);
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups = hostGroups.addSpecialGroups();
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups.alterToWorkingVersion();
		Logging.debug(this, "getHostGroups rebuilt" + hostGroups);
		cacheManager.setCachedData(CacheIdentifier.HOST_GROUPS, hostGroups);

		// Load data for productGroups
		Map<String, Map<String, String>> result = AbstractPOJOExecutioner.generateStringMappedObjectsByKeyResult(
				productGroupsList.iterator(), "ident", new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" }, null);
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
		if (cacheManager.getCachedData(cacheId, Map.class) != null) {
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
		if (cacheManager.getCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, Map.class) != null) {
			return;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		Map<String, Map<String, String>> mappedRelations = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS,
						new Object[] { callAttributes, callFilter }),
				"ident", new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" },
				ClientTree.getTranslationsFromPersistentNames());
		Map<String, Set<String>> fObject2Groups = projectToFunction(mappedRelations, "clientId", "groupId");
		cacheManager.setCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, fObject2Groups);
	}

	public void retrieveAllObject2GroupsPD() {
		// Don't load when one of the two is not null
		// We only want to load, when both are not yet loaded
		if (cacheManager.getCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, Map.class) != null
				|| cacheManager.getCachedData(CacheIdentifier.FPRODUCT_GROUP_TO_MEMBERS, Map.class) != null) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_GET_OBJECTS, new Object[] {});

		List<Map<String, Object>> resultlist = exec.getListOfMaps(omc);

		List<Object> hostGroupsList = new ArrayList<>();
		List<Object> productGroupsList = new ArrayList<>();

		Iterator<Map<String, Object>> iter = resultlist.iterator();

		while (iter.hasNext()) {
			Map<String, Object> entry = iter.next();

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
				.generateStringMappedObjectsByKeyResult(hostGroupsList.iterator(), "ident",
						new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" },
						ClientTree.getTranslationsFromPersistentNames());

		Map<String, Set<String>> fObject2Groups = projectToFunction(mappedRelationsHostGroups, "clientId", "groupId");
		cacheManager.setCachedData(CacheIdentifier.FOBJECT_TO_GROUPS, fObject2Groups);

		// generate data for product groups
		Map<String, Map<String, String>> mappedRelationsProductGroups = AbstractPOJOExecutioner
				.generateStringMappedObjectsByKeyResult(productGroupsList.iterator(), "ident",
						new String[] { "objectId", "groupId" }, new String[] { "productId", "groupId" }, null);

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

	public boolean addObject2Group(String objectId, String groupId) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.OBJECT_TO_GROUP_CREATE,
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	public boolean removeHostGroupElements(Iterable<Object2GroupEntry> entries) {
		if (userRolesConfigDataService.isGlobalReadOnly()) {
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
		if (userRolesConfigDataService.isGlobalReadOnly()) {
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
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
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
			persistenceController.reloadData(CacheIdentifier.HOST_GROUPS.toString());
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

	public boolean updateGroup(String groupId, Map<String, String> updateInfo) {
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
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
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

		Set<String> inNewSetnotInOriSet = new HashSet<>(productSet);
		Set<String> inOriSetnotInNewSet = new HashSet<>();

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
