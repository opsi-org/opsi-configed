package de.uib.opsidatamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uib.configed.tree.ClientTree;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;

public class HostGroups extends TreeMap<String, Map<String, String>> {

	OpsiserviceNOMPersistenceController pc;

	public HostGroups(Map<String, Map<String, String>> source, OpsiserviceNOMPersistenceController pc) {
		super(source);

		this.pc = pc;
	}

	HostGroups addSpecialGroups() {
		Logging.debug(this, "addSpecialGroups check");
		List<StringValuedRelationElement> groups = new ArrayList<>();

		// create
		if (get(ClientTree.DIRECTORY_PERSISTENT_NAME) == null) {
			Logging.debug(this, "addSpecialGroups");
			StringValuedRelationElement directoryGroup = new StringValuedRelationElement();

			directoryGroup.put("groupId", ClientTree.DIRECTORY_PERSISTENT_NAME);
			directoryGroup.put("parentGroupId", null);
			directoryGroup.put("description", "root of directory");

			pc.addGroup(directoryGroup, false);

			groups.add(directoryGroup);

			put(ClientTree.DIRECTORY_PERSISTENT_NAME, directoryGroup);

			Logging.debug(this, "addSpecialGroups we have " + this);

		}

		return this;
	}

	public void alterToWorkingVersion() {
		Logging.debug(this, "alterToWorkingVersion we have " + this);

		for (Map<String, String> groupInfo : values()) {
			if (ClientTree.DIRECTORY_PERSISTENT_NAME.equals(groupInfo.get("parentGroupId"))) {
				groupInfo.put("parentGroupId", ClientTree.DIRECTORY_NAME);
			}
		}

		Map<String, String> directoryGroup = get(ClientTree.DIRECTORY_PERSISTENT_NAME);
		if (directoryGroup != null) {
			directoryGroup.put("groupId", ClientTree.DIRECTORY_NAME);
		}

		put(ClientTree.DIRECTORY_NAME, directoryGroup);
		remove(ClientTree.DIRECTORY_PERSISTENT_NAME);
	}
}