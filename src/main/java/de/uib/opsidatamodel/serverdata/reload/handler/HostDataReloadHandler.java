package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;

public class HostDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private ConfigDataService configDataService;
	private GroupDataService groupDataService;
	private HostInfoCollections hostInfoCollections;

	public HostDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	@Override
	public void handle(String event) {
		cacheManager.clearCachedData(CacheIdentifier.OPSI_HOST_NAMES);
		hostInfoCollections.retrieveOpsiHostsPD();

		cacheManager.clearCachedData(CacheIdentifier.FNODE_TO_TREE_PARENTS);
		hostInfoCollections.retrieveFNode2TreeparentsPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_CONFIGS);
		configDataService.retrieveHostConfigsPD();

		cacheManager.clearCachedData(CacheIdentifier.HOST_GROUPS);
		groupDataService.retrieveHostGroupsPD();

		cacheManager.clearCachedData(CacheIdentifier.FOBJECT_TO_GROUPS);
		groupDataService.retrieveFObject2GroupsPD();

		cacheManager.clearCachedData(CacheIdentifier.FGROUP_TO_MEMBERS);
		groupDataService.retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId",
				CacheIdentifier.FGROUP_TO_MEMBERS);
	}
}
