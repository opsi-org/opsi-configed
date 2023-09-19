package de.uib.opsidatamodel.serverdata.reload.handler;

import de.uib.configed.type.Object2GroupEntry;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.dataservice.ConfigDataService;
import de.uib.opsidatamodel.serverdata.dataservice.GroupDataService;
import de.uib.opsidatamodel.serverdata.dataservice.HostDataService;

public class HostDataReloadHandler implements ReloadHandler {
	private CacheManager cacheManager;
	private HostDataService hostDataService;
	private ConfigDataService configDataService;
	private GroupDataService groupDataService;

	public HostDataReloadHandler() {
		this.cacheManager = CacheManager.getInstance();
	}

	public void setHostDataService(HostDataService hostDataService) {
		this.hostDataService = hostDataService;
	}

	public void setConfigDataService(ConfigDataService configDataService) {
		this.configDataService = configDataService;
	}

	public void setGroupDataService(GroupDataService groupDataService) {
		this.groupDataService = groupDataService;
	}

	@Override
	public void handle(String event) {
		hostDataService.getHostInfoCollectionsPD().opsiHostsRequestRefresh();

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
