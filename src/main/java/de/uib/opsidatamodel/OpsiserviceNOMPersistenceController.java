/**
*   PersistenceController
*   implementation for the New Object Model (opsi 4.0)
*   description: instances of PersistenceController give
*   access to proxy objects which mediate access to remote objects (and buffer the data)
*
*
* The PersistenceController retrieves its data from a server that is compatible with the
*  opsi data server resp. its stub (proxy)
*  It has a Executioner component that transmits requests to the opsi server and receives the responses.
*
*  There are several classes which implement the Executioner methods in different ways
*  dependent on the used means and protocols
*
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; 
* version  AGPLv3
*
*  copyright:     Copyright (c) 2000-2022
*  organization: uib.de
* @author  R. Roeder
*/

package de.uib.opsidatamodel;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.tree.ClientTree;
import de.uib.configed.type.AbstractMetaConfig;
import de.uib.configed.type.AdditionalQuery;
import de.uib.configed.type.ConfigName2ConfigValue;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.ConfigStateEntry;
import de.uib.configed.type.DatedRowList;
import de.uib.configed.type.HWAuditClientEntry;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.Object2GroupEntry;
import de.uib.configed.type.OpsiHwAuditDeviceClass;
import de.uib.configed.type.OpsiHwAuditDevicePropertyType;
import de.uib.configed.type.OpsiHwAuditDevicePropertyTypes;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.configed.type.RemoteControl;
import de.uib.configed.type.RetrievedMap;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.SavedSearch;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicenceContractEntry;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicencePoolXOpsiProduct;
import de.uib.configed.type.licences.LicenceStatisticsRow;
import de.uib.configed.type.licences.LicenceUsableForEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.connectx.SmbConnect;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.JSONReMapper;
import de.uib.opsicommand.JSONthroughHTTPS;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.dbtable.Host;
import de.uib.opsidatamodel.dbtable.ProductOnClient;
import de.uib.opsidatamodel.dbtable.ProductPropertyState;
import de.uib.opsidatamodel.modulelicense.FGeneralDialogLicensingInfo;
import de.uib.opsidatamodel.modulelicense.FOpsiLicenseMissingText;
import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.opsidatamodel.permission.ModulePermissionValue;
import de.uib.opsidatamodel.permission.UserConfig;
import de.uib.opsidatamodel.permission.UserConfigProducing;
import de.uib.opsidatamodel.permission.UserOpsipermission;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.ExtendedDate;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.datapanel.MapTableModel;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.table.ListCellOptions;

public class OpsiserviceNOMPersistenceController extends AbstractPersistenceController {
	private static final String EMPTYFIELD = "-";
	private static final List NONE_LIST = new ArrayList<>() {
		@Override
		public int size() {
			return -1;
		}
	};

	/* data for checking permissions */
	protected boolean globalReadOnly;

	protected boolean serverFullPermission;

	protected boolean createClientPermission;

	protected boolean depotsFullPermission;
	protected Set<String> depotsPermitted;

	protected boolean hostgroupsOnlyIfExplicitlyStated;
	protected Set<String> hostgroupsPermitted;

	protected boolean productgroupsFullPermission;
	protected Set<String> productgroupsPermitted;

	/* ------------------------------------------ */

	public static final String NAME_REQUIREMENT_TYPE_BEFORE = "before";
	public static final String NAME_REQUIREMENT_TYPE_AFTER = "after";
	public static final String NAME_REQUIREMENT_TYPE_NEUTRAL = "";
	public static final String NAME_REQUIREMENT_TYPE_ON_DEINSTALL = "on_deinstall";

	protected FTextArea licInfoWarnings;

	protected String connectionServer;
	private String user;
	private String userConfigPart;
	private Boolean applyUserSpecializedConfig;

	private static Boolean keyUserRegisterValue = null;

	protected Map<String, List<String>> mapOfMethodSignatures;

	protected List<OpsiProductInfo> productInfos;
	protected Map<String, Map<String, Object>> productGlobalInfos;

	protected Map<String, Map<String, ConfigName2ConfigValue>> productProperties;
	// (pcname -> (productname -> (propertyname -> propertyvalue))) NOM
	private Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties;
	protected Set<String> productsHavingSpecificProperties;
	protected Map<String, Boolean> productHavingClientSpecificProperties;

	// for depot
	protected Map<String, Map<String, ListCellOptions>> productPropertyDefinitions;

	protected AbstractHostInfoCollections hostInfoCollections;

	private String theDepot = "";

	protected String opsiDefaultDomain;

	protected Set<String> permittedProducts;

	private List<String> localbootProductNames;
	private List<String> netbootProductNames;

	private Map<String, List<String>> possibleActions; // product-->possibleActions

	protected List<Map<String, Object>> softwareAuditOnClients;

	// key --> rowmap for auditSoftware

	protected List<Map<String, Object>> relationsAuditHardwareOnHost;

	protected AuditSoftwareXLicencePool relationsAuditSoftwareToLicencePools;

	// TODO Still needed?
	protected Map<String, Map<String, String>> rowmapAuditSoftware;

	// function softwareIdent --> pool
	protected Map<String, String> fSoftware2LicencePool;

	// function pool --> list of assigned software
	protected Map<String, List<String>> fLicencePool2SoftwareList;

	// function pool --> list of assigned software which is not in software table
	protected Map<String, List<String>> fLicencePool2UnknownSoftwareList;

	protected NavigableSet<Object> softwareWithoutAssociatedLicencePool;

	// map key -> rowmap
	protected Map<String, LicenceUsageEntry> rowsLicencesUsage;

	// function host -> list of used licences
	protected Map<String, List<LicenceUsageEntry>> fClient2LicencesUsageList;

	protected Map<String, Map<String, Object>> rowsLicencesReconciliation;

	protected NavigableMap<String, LicenceStatisticsRow> rowsLicenceStatistics;

	protected Map<String, List<Map<String, Object>>> hwAuditConf;

	protected List<String> opsiHwClassNames;
	protected Map<String, OpsiHwAuditDeviceClass> hwAuditDeviceClasses;
	protected OpsiHwAuditDevicePropertyTypes hwAuditDevicePropertyTypes;

	protected List<String> hostColumnNames;
	protected List<String> client2HwRowsColumnNames;
	protected List<String> client2HwRowsJavaclassNames;
	protected List<String> hwInfoClassNames;
	protected List<String> hwTableNames;

	class HostGroups extends TreeMap<String, Map<String, String>> {
		public HostGroups(Map<String, Map<String, String>> source) {
			super(source);
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

				addGroup(directoryGroup, false);

				groups.add(directoryGroup);

				put(ClientTree.DIRECTORY_PERSISTENT_NAME, directoryGroup);

				Logging.debug(this, "addSpecialGroups we have " + this);

			}

			return this;
		}

		void alterToWorkingVersion() {
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

	protected Map<String, Map<String, String>> productGroups;

	protected HostGroups hostGroups;

	protected Map<String, Set<String>> fObject2Groups;

	protected Map<String, Set<String>> fGroup2Members;

	protected Map<String, Set<String>> fProductGroup2Members;

	protected Map<String, String> logfiles;

	private List updateProductOnClientItems;

	private List<LicenceUsageEntry> itemsDeletionLicenceUsage;

	protected Map<String, Object> opsiInformation = new HashMap<>();
	protected JSONObject licencingInfo;
	private LicensingInfoMap licInfoMap;
	private String opsiLicensingInfoVersion;
	private static final String BACKEND_LICENSING_INFO_METHOD_NAME = "backend_getLicensingInfo";

	// the may as read in
	protected Map<String, Object> opsiModulesInfo;

	// the infos that are displayed in the gui
	protected Map<String, Object> opsiModulesDisplayInfo;

	// the resulting info about permission
	protected Map<String, Boolean> opsiModules;

	protected String opsiVersion;

	protected boolean withLicenceManagement = false;
	protected boolean withLocalImaging = false;

	protected boolean withMySQL = false;
	protected boolean withUEFI = false;
	protected boolean withWAN = false;

	protected boolean withLinuxAgent = false;
	protected boolean withUserRoles = false;

	// for internal use, for external cast to:
	protected Map<String, ConfigOption> configOptions;
	protected Map<String, ListCellOptions> configListCellOptions;
	protected Map<String, List<Object>> configDefaultValues;
	protected Map<String, Map<String, Object>> hostConfigs;

	protected RemoteControls remoteControls;
	protected SavedSearches savedSearches;

	protected Map<String, Boolean> productOnClientsDisplayFieldsNetbootProducts;
	protected Map<String, Boolean> productOnClientsDisplayFieldsLocalbootProducts;
	protected Map<String, Boolean> hostDisplayFields;

	protected List<Map<String, Object>> configStateCollection;
	protected List<JSONObject> deleteConfigStateItems;
	protected List<Map<String, Object>> configCollection;

	protected List productPropertyStateUpdateCollection;
	protected List productPropertyStateDeleteCollection;

	protected Map<String, Map<String, Object>> hostUpdates;

	protected NavigableSet<String> productIds;
	protected Map<String, Map<String, String>> productDefaultStates;

	protected List< /* JSON */Object> licenceOnClientDeleteItems;

	protected List<Map<String, Object>> healthData;

	AbstractDataStub dataStub;

	protected Boolean acceptMySQL = null;

	@Override
	public boolean canCallMySQL() {
		if (acceptMySQL == null) {
			acceptMySQL = dataStub.canCallMySQL();
		}

		return acceptMySQL;
	}

	@Override
	public AbstractHostInfoCollections getHostInfoCollections() {
		return hostInfoCollections;
	}

	static Boolean interpretAsBoolean(Object ob, Boolean defaultValue) {
		if (ob == null) {
			return defaultValue;
		}

		if (ob instanceof Boolean) {
			return (Boolean) ob;
		}

		if (ob instanceof Integer) {
			return ((Integer) ob) == 1;
		}

		if (ob instanceof String) {
			return ((String) ob).equals("1");
		}

		Logging.warning("could not find boolean in interpretAsBoolean, returning false");

		// not foreseen value
		return false;
	}

	protected class CheckingEntryMapOfMaps extends LinkedHashMap<String, Map<String, Object>> {}

	protected class DefaultHostInfoCollections extends AbstractHostInfoCollections {
		protected String configServer;
		protected List<String> opsiHostNames;

		protected int countClients = 0;

		protected Map<String, Map<String, Object>> masterDepots;
		protected Map<String, Map<String, Object>> allDepots;
		protected Map<String, Map<String, HostInfo>> depot2Host2HostInfo;
		protected LinkedList<String> depotNamesList;

		protected Map<String, Boolean> mapOfPCs;

		// for some depots
		protected Map<String, HostInfo> mapPCInfomap;

		// all hosts
		protected Map<String, HostInfo> host2hostInfo;

		// essentially client --> all groups with it
		protected Map<String, Set<String>> fNode2Treeparents;
		protected Map<String, String> mapPcBelongsToDepot;

		private ClientTree connectedTree;

		DefaultHostInfoCollections() {

		}

		// deliver data

		private Map<String, Object> hideOpsiHostKey(Map<String, Object> source) {
			Map<String, Object> result = new HashMap<>(source);
			result.put(HostInfo.HOST_KEY_KEY, "****");
			return result;
		}

		@Override
		public void setTree(ClientTree tree) {
			connectedTree = tree;
		}

		@Override
		public String getConfigServer() {
			return configServer;
		}

		protected void checkMapPcBelongsToDepot() {
			if (mapPcBelongsToDepot == null) {
				mapPcBelongsToDepot = new HashMap<>();
			}
		}

		@Override
		public Map<String, String> getMapPcBelongsToDepot() {
			checkMapPcBelongsToDepot();
			return mapPcBelongsToDepot;
		}

		protected Map<String, Boolean> getMapOfPCs() {
			return mapOfPCs;
		}

		@Override
		public List<String> getOpsiHostNames() {
			retrieveOpsiHosts();
			return opsiHostNames;

		}

		@Override
		public int getCountClients() {
			retrieveOpsiHosts();
			return countClients;
		}

		@Override
		public Map<String, Map<String, Object>> getDepots() {
			retrieveOpsiHosts();
			Logging.debug(this, "getDepots masterDepots " + masterDepots);

			return masterDepots;
		}

		@Override
		public LinkedList<String> getDepotNamesList() {
			retrieveOpsiHosts();
			return depotNamesList;
		}

		@Override
		public Map<String, Map<String, Object>> getAllDepots() {
			retrieveOpsiHosts();
			return allDepots;
		}

		@Override
		public Map<String, HostInfo> getMapOfPCInfoMaps() {

			return mapPCInfomap;
		}

		@Override
		public Map<String, HostInfo> getMapOfAllPCInfoMaps() {
			Logging.info(this, "getMapOfAllPCInfoMaps() size " + host2hostInfo.size());
			return host2hostInfo;
		}

		// request data refreshes
		@Override
		public void opsiHostsRequestRefresh() {
			opsiHostNames = null;
			fNode2Treeparents = null;
		}

		// build data
		@Override
		protected void retrieveOpsiHosts() {
			Logging.debug(this, "retrieveOpsiHosts , opsiHostNames == null " + (opsiHostNames == null));

			int countHosts = 0;

			if (opsiHostNames == null) {
				List<Map<String, Object>> opsiHosts = hostRead();
				HostInfo.resetInstancesCount();

				opsiHostNames = new ArrayList<>();
				allDepots = new TreeMap<>();

				masterDepots = new CheckingEntryMapOfMaps();
				depotNamesList = new LinkedList<>();

				countHosts = opsiHosts.size();

				countClients = countHosts;

				host2hostInfo = new HashMap<>();

				Logging.info(this, "retrieveOpsiHosts countHosts " + countClients);

				// find opsi configserver and give it the top position
				for (Map<String, Object> host : opsiHosts) {
					String name = (String) host.get(HostInfo.HOSTNAME_KEY);
					opsiHostNames.add(name);

					for (Entry<String, Object> hostEntry : host.entrySet()) {
						if (JSONReMapper.isNull(hostEntry.getValue())) {
							host.put(hostEntry.getKey(), JSONReMapper.NULL_REPRESENTER);
						}
					}

					boolean isConfigserver = host.get(HostInfo.HOST_TYPE_KEY)
							.equals(HostInfo.HOST_TYPE_VALUE_OPSI_CONFIG_SERVER);

					if (isConfigserver) {
						Logging.info(this, "retrieveOpsiHosts  type opsiconfigserver host " + hideOpsiHostKey(host));

						configServer = name;

						depotNamesList.add(name);

						allDepots.put(name, host);
						countClients--;

						boolean isMasterDepot = interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), true);

						if (isMasterDepot) {
							Map<String, Object> hostMap = new HashMap<>(host);
							masterDepots.put(name, hostMap);
						}

						Object val = host.get(HostInfo.DEPOT_WORKBENCH_KEY);

						if (val != null && !val.equals("")) {
							try {
								String filepath = new URL((String) val).getPath();
								Logging.info(this, "retrieveOpsiHosts workbenchpath " + filepath);

								configedWorkbenchDefaultValue = filepath;
								packageServerDirectoryS = filepath;
							} catch (Exception netex) {
								Logging.error("not a correctly formed file URL: " + val);
							}
						}
					}
				}

				Logging.info(this, "retrieveOpsiHost found masterDepots " + masterDepots.size());
				if (configServer == null) {
					StringBuilder messbuff = new StringBuilder();
					final String baselabel = "PersistenceController.noData";

					messbuff.append(Configed.getResourceValue(baselabel + "0"));
					messbuff.append("\n");
					messbuff.append(Configed.getResourceValue(baselabel + "1") + " " + countHosts);
					messbuff.append("\n");
					messbuff.append("\n");

					for (int i = 2; i <= 4; i++) {
						messbuff.append(Configed.getResourceValue(baselabel + i));
						messbuff.append("\n");
						messbuff.append("\n");
					}

					String message = messbuff.toString();
					Logging.error(this, message);

					FTextArea f = new FTextArea(null, "opsi configed", true,
							new String[] { Configed.getResourceValue("PersistenceController.endApp") }, 500, 400);
					f.setMessage(message);

					f.setVisible(true);

					System.exit(1);
				}

				depot2Host2HostInfo = new TreeMap<>();
				depot2Host2HostInfo.put(configServer, new TreeMap<>());

				// find depots and build entries for them
				for (Map<String, Object> host : opsiHosts) {
					String name = (String) host.get(HostInfo.HOSTNAME_KEY);

					if (name == null) {
						Logging.info(this, "retrieveOpsiHosts, host  " + host);

					}

					if (host.get(HostInfo.HOST_TYPE_KEY).equals(HostInfo.HOST_TYPE_VALUE_OPSI_DEPOT_SERVER)) {
						allDepots.put(name, host);
						countClients--;

						boolean isMasterDepot = interpretAsBoolean(host.get(HostInfo.IS_MASTER_DEPOT_KEY), false);

						if (isMasterDepot) {
							Map<String, Object> hostMap = new HashMap<>(host);
							masterDepots.put(name, hostMap);

							depot2Host2HostInfo.put(name, new TreeMap<>());
						}
					}
				}

				for (Map<String, Object> host : opsiHosts) {
					String name = (String) host.get(HostInfo.HOSTNAME_KEY);
					if (((String) host.get(HostInfo.HOST_TYPE_KEY)).equals(HostInfo.HOST_TYPE_VALUE_OPSI_CLIENT)) {
						boolean depotFound = false;
						String depotId = null;

						if (getConfigs().get(name) == null || getConfigs().get(name).get(CONFIG_DEPOT_ID) == null
								|| ((List<?>) (getConfigs().get(name).get(CONFIG_DEPOT_ID))).isEmpty()) {
							Logging.debug(this,
									"retrieveOpsiHosts client  " + name + " has no config for " + CONFIG_DEPOT_ID);
						} else {
							depotId = (String) ((List<?>) (getConfigs().get(name).get(CONFIG_DEPOT_ID))).get(0);
						}

						if (depotId != null && masterDepots.keySet().contains(depotId)) {
							depotFound = true;
						} else {
							if (depotId != null) {
								Logging.warning("Host " + name + " is in " + depotId + " which is not a master depot");
							}
						}

						Logging.debug(this, "getConfigs for " + name);

						// Get Install by Shutdown

						host.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, isInstallByShutdownConfigured(name));

						// Get UEFI Boot

						host.put(HostInfo.CLIENT_UEFI_BOOT_KEY, isUefiConfigured(name));

						// CHECK WAN STANDARD CONFIG
						if (getConfig(name) != null) {

							Boolean result = false;
							boolean tested = findBooleanConfigurationComparingToDefaults(name, wanConfiguration);

							Logging.debug(this, "host " + name + " wan config " + result);

							if (tested) {
								result = true;
							} else {
								tested = findBooleanConfigurationComparingToDefaults(name, notWanConfiguration);

								if (tested) {
									result = false;
								}
							}

							Logging.debug(this, "host " + name + " wan config " + result);

							host.put(HostInfo.CLIENT_WAN_CONFIG_KEY, result);

						}

						HostInfo hostInfo = null;

						String myDepot = null;

						if (depotFound) {
							host.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);
							hostInfo = new HostInfo(host);
							hostInfo.setInDepot(depotId);
							myDepot = depotId;

							// depot_restriction:

						} else {
							host.put(HostInfo.DEPOT_OF_CLIENT_KEY, configServer);
							hostInfo = new HostInfo(host);
							hostInfo.setInDepot(configServer);
							myDepot = configServer;

							// depot_restriction:

						}

						host2hostInfo.put(name, hostInfo);
						depot2Host2HostInfo.get(myDepot).put(name, hostInfo);
					}
				}

				for (String depot : masterDepots.keySet()) {
					Logging.info(this,
							"retrieveOpsiHosts clients in " + depot + ": " + depot2Host2HostInfo.get(depot).size());
				}

				TreeSet<String> depotNamesSorted = new TreeSet<>(masterDepots.keySet());
				depotNamesSorted.remove(configServer);

				for (String depot : depotNamesSorted) {
					depotNamesList.add(depot);
				}

				// test for depot_restriction:

				Logging.info(this, "retrieveOpsiHosts  HostInfo instances counter " + HostInfo.getInstancesCount());
				Logging.info(this, "retrieveOpsiHosts  hostnames size " + opsiHostNames.size());
				Logging.info(this, "retrieveOpsiHosts   depotNamesList size " + depotNamesList.size());

			}
		}

		@Override
		public Map<String, Set<String>> getFNode2Treeparents() {
			retrieveFNode2Treeparents();
			return fNode2Treeparents;
		}

		protected void retrieveFNode2Treeparents() {
			retrieveOpsiHosts();

			if (fNode2Treeparents == null) {
				fNode2Treeparents = new HashMap<>();
			}

			if (connectedTree != null) {
				for (String host : opsiHostNames) {
					fNode2Treeparents.put(host, connectedTree.collectParentIDs(host));
				}
			}
		}

		@Override
		public Map<String, Boolean> getClientListForDepots(String[] depots, Set<String> allowedClients) {
			retrieveOpsiHosts();

			Logging.debug(this, " ------ building pcList");
			mapPcBelongsToDepot = new HashMap<>();

			mapOfPCs = new HashMap<>();
			mapPCInfomap = new HashMap<>();

			List<String> depotList = new ArrayList<>();
			for (String depot : depots) {
				if (getDepotPermission(depot)) {
					depotList.add(depot);
				}
			}

			for (String depot : depotList) {
				if (depot2Host2HostInfo.get(depot) == null) {
					Logging.info(this, "getPcListForDepots depot " + depot + " is null");
				} else {
					for (String clientName : depot2Host2HostInfo.get(depot).keySet()) {
						HostInfo hostInfo = depot2Host2HostInfo.get(depot).get(clientName);

						if (allowedClients != null && !allowedClients.contains(clientName)) {
							continue;
						}

						mapOfPCs.put(clientName, false);

						mapPCInfomap.put(clientName, hostInfo);
						mapPcBelongsToDepot.put(clientName, depot);
					}
				}
			}

			return mapOfPCs;
		}

		protected void setDepot(String clientName, String depotId) {
			// set config
			if (getConfigs().get(clientName) == null) {
				getConfigs().put(clientName, new HashMap<>());
			}
			List<String> depotList = new ArrayList<>();
			depotList.add(depotId);
			getConfigs().get(clientName).put(CONFIG_DEPOT_ID, depotList);

			// set in mapPC_Infomap
			HostInfo hostInfo = mapPCInfomap.get(clientName);

			Logging.info(this, "setDepot, hostinfo for client " + clientName + " : " + mapPCInfomap.get(clientName));

			hostInfo.put(HostInfo.DEPOT_OF_CLIENT_KEY, depotId);

			String oldDepot = mapPcBelongsToDepot.get(clientName);
			Logging.info(this, "setDepot clientName, oldDepot " + clientName + ", " + oldDepot);
			// set in mapPcBelongsToDepot
			mapPcBelongsToDepot.put(clientName, depotId);

			depot2Host2HostInfo.get(oldDepot).remove(clientName);
			depot2Host2HostInfo.get(depotId).put(clientName, hostInfo);
		}

		@Override
		public void setDepotForClients(String[] clients, String depotId) {
			if (!getDepotPermission(depotId)) {
				return;
			}

			List<String> depots = new ArrayList<>();

			ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
			depots.add(depotId);

			config.put(CONFIG_DEPOT_ID, depots);
			for (int i = 0; i < clients.length; i++) {
				// collect data
				setAdditionalConfiguration(clients[i], config);
			}
			// send data
			setAdditionalConfiguration(false);

			// change transitory data
			for (int i = 0; i < clients.length; i++) {
				setDepot(clients[i], depotId);
			}

			// we hope to have completely changed the internal data
		}

		// update derived data (caution!), does not create a HostInfo
		@Override
		public void addOpsiHostName(String newName) {
			opsiHostNames.add(newName);
		}

		@Override
		public void addOpsiHostNames(String[] newNames) {
			opsiHostNames.addAll(Arrays.asList(newNames));
		}

		// for table
		@Override
		public void updateLocalHostInfo(String hostId, String property, Object value) {
			if (mapPCInfomap != null && mapPCInfomap.get(hostId) != null) {
				mapPCInfomap.get(hostId).put(property, value);
				Logging.info(this, "updateLocalHostInfo " + hostId + " - " + property + " : " + value);
			}
		}

		@Override
		public void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo) {
			Logging.debug(this, "setLocalHostInfo " + " " + hostId + ", " + depotId + ", " + hostInfo);
			mapPCInfomap.put(hostId, hostInfo);
			depot2Host2HostInfo.get(depotId).put(hostId, hostInfo);
		}
	}

	// package visibility, the constructor is called by PersistenceControllerFactory
	OpsiserviceNOMPersistenceController(String server, String user, String password) {
		Logging.info(this, "start construction, \nconnect to " + server + " as " + user);
		this.connectionServer = server;
		this.user = user;

		Logging.debug(this, "create");

		hostInfoCollections = new DefaultHostInfoCollections();

		exec = new JSONthroughHTTPS(server, user, password);

		execs.put(server, exec);

		hwAuditConf = new HashMap<>();

		initMembers();
	}

	protected void initMembers() {
		if (dataStub == null) {
			dataStub = new DataStubNOM(this);
		}
	}

	// final in order to avoid deactiviating by override
	private final boolean setAgainUserRegistration(final boolean userRegisterValueFromConfigs) {
		Logging.info(this, "setAgainUserRegistration, userRoles can be used " + withUserRoles);

		boolean resultVal = userRegisterValueFromConfigs;

		if (!withUserRoles) {
			return resultVal;
		}

		Boolean locallySavedValueUserRegister = null;
		if (Configed.savedStates == null) {
			Logging.trace(this, "savedStates.saveRegisterUser not initialized");
		} else {
			locallySavedValueUserRegister = Configed.savedStates.saveRegisterUser.deserializeAsBoolean();
			Logging.info(this, "setAgainUserRegistration, userRegister was activated " + locallySavedValueUserRegister);

			if (userRegisterValueFromConfigs) {
				if (locallySavedValueUserRegister == null || !locallySavedValueUserRegister) {
					// we save true
					Configed.savedStates.saveRegisterUser.serialize(true);
				}
			} else {
				if (locallySavedValueUserRegister != null && locallySavedValueUserRegister) {
					// if true was locally saved but is not the value from service then we ask
					Logging.warning(this, "setAgainUserRegistration, it seems that user check has been deactivated");

					FTextArea dialog = new FTextArea(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("RegisterUserWarning.dialog.title"),

							true,
							new String[] { Configed.getResourceValue("RegisterUserWarning.dialog.button1"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button2"),
									Configed.getResourceValue("RegisterUserWarning.dialog.button3") },
							new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
									Globals.createImageIcon("images/edit-delete.png", ""),
									Globals.createImageIcon("images/executing_command_red_16.png", "") },
							500, 200);
					StringBuilder msg = new StringBuilder(

							Configed.getResourceValue("RegisterUserWarning.dialog.info1"));
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.info2"));// At the moment,
																										// user control
																										// is not more
																										// active!
					msg.append("\n");
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option1"));// Ignore warning
																										// and
																										// continue?
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option2"));// No more
																										// warning
																										// (locally)?
					msg.append("\n" + Configed.getResourceValue("RegisterUserWarning.dialog.option3"));// Re-activate
																										// user check
																										// (on the opsi
																										// server)?

					dialog.setMessage("" + msg);
					dialog.setVisible(true);
					int result = dialog.getResult();
					Logging.info(this, "setAgainUserRegistration, reaction via option " + dialog.getResult());

					switch (result) {
					case 1:
						Logging.info(this, "setAgainUserRegistration ignore ");
						break;

					case 2:
						Logging.info(this, "setAgainUserRegistration remove warning locally ");
						// remove from store
						Configed.savedStates.saveRegisterUser.serialize(null);
						Configed.savedStates.store();
						break;

					case 3:
						Logging.info(this, "setAgainUserRegistration reactivate user check ");
						resultVal = true;
						break;

					default:
						Logging.warning(this, "no case found for result in setAgainUserRegistration");
						break;
					}
				}
			}
		}

		return resultVal;
	}

	private String userPart() {
		if (userConfigPart != null) {
			return userConfigPart;
		}

		if (applyUserSpecializedConfig()) {
			userConfigPart = KEY_USER_ROOT + ".{" + user + "}.";
		} else {
			userConfigPart = UserConfig.KEY_USER_ROLE_ROOT + ".{" + UserConfig.DEFAULT_ROLE_NAME + "}.";
		}

		Logging.info(this, "userConfigPart initialized, " + userConfigPart);

		return userConfigPart;
	}

	@Override
	public final void checkConfiguration() {
		retrieveOpsiModules();
		Logging.info(this, "checkConfiguration, modules " + opsiModules);
		initMembers();

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

		globalReadOnly = checkReadOnlyBySystemuser();

		serverFullPermission = !globalReadOnly;
		depotsFullPermission = true;
		hostgroupsOnlyIfExplicitlyStated = false;
		productgroupsFullPermission = true;
		createClientPermission = true;

		keyUserRegisterValue = isUserRegisterActivated();
		boolean correctedUserRegisterVal = setAgainUserRegistration(keyUserRegisterValue);

		boolean setUserRegisterVal = !keyUserRegisterValue && correctedUserRegisterVal;

		if (setUserRegisterVal) {
			keyUserRegisterValue = true;
		}

		if (Boolean.TRUE.equals(keyUserRegisterValue)) {
			keyUserRegisterValue = checkUserRolesModule();
		}

		if (serverPropertyMap.get(KEY_USER_REGISTER) == null || setUserRegisterVal) {
			List<Object> readyObjects = new ArrayList<>();
			Map<String, Object> item = createJSONBoolConfig(KEY_USER_REGISTER, keyUserRegisterValue,
					"without given values the primary value setting is false");
			readyObjects.add(AbstractExecutioner.jsonMap(item));

			OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

			exec.doCall(omc);
		}

		applyUserSpecializedConfig();

		List<Object> readyConfigObjects = new UserConfigProducing(applyUserSpecializedConfig(),
				getHostInfoCollections().getConfigServer(), getHostInfoCollections().getDepotNamesList(),
				getHostGroupIds(), getProductGroups().keySet(), getConfigDefaultValues(), getConfigOptions()).produce();

		if (readyConfigObjects == null) {
			Logging.warning(this, "readyObjects for userparts " + null);
		} else {

			if (!readyConfigObjects.isEmpty()) {

				OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
						new Object[] { AbstractExecutioner.jsonArray(readyConfigObjects) });

				exec.doCall(omc);
			}

			Logging.info(this, "readyObjects for userparts " + readyConfigObjects.size());
		}

		checkPermissions();

		if (serverFullPermission) {
			checkStandardConfigs();
		}
	}

	@Override
	public AbstractExecutioner retrieveWorkingExec(String depot) {

		Logging.debug(this, "retrieveWorkingExec , compare depotname " + depot + " to config server "
				+ hostInfoCollections.getConfigServer() + " ( named as " + connectionServer + ")");

		if (depot.equals(hostInfoCollections.getConfigServer())) {
			Logging.debug(this, "retrieveWorkingExec for config server");
			return exec;
		}

		String password = (String) getHostInfoCollections().getDepots().get(depot).get(HostInfo.HOST_KEY_KEY);

		AbstractExecutioner exec1 = new JSONthroughHTTPS(depot, depot, password);

		if (makeConnection(exec1)) {
			Logging.info(this, "retrieveWorkingExec new for server " + depot);
			return exec1;
		}

		Logging.info(this, "no connection to server " + depot);

		return AbstractExecutioner.getNoneExecutioner();
	}

	@Override
	protected boolean makeConnection() {
		return makeConnection(exec);
	}

	protected boolean makeConnection(AbstractExecutioner exec1) {
		// set by executioner

		Logging.info(this, "trying to make connection");
		boolean result = false;
		try {
			result = exec1.doCall(new OpsiMethodCall("authenticated", new String[] {}));

			if (!result) {
				Logging.info(this, "connection does not work");
			}

		} catch (ClassCastException ex) {
			Logging.info(this, "JSONthroughHTTPS failed to make connection");
		}

		result = result && getConnectionState().getState() == ConnectionState.CONNECTED;
		Logging.info(this, "tried to make connection result " + result);
		return result;
	}

	@Override
	public String getOpsiCACert() {
		OpsiMethodCall omc = new OpsiMethodCall("getOpsiCACert", new Object[0]);
		return exec.getStringResult(omc);
	}

	// we delegate method calls to the executioner
	@Override
	public ConnectionState getConnectionState() {
		return exec.getConnectionState();
	}

	@Override
	public void setConnectionState(ConnectionState state) {
		exec.setConnectionState(state);
	}

	@Override
	public boolean isGlobalReadOnly() {
		return globalReadOnly;
	}

	protected boolean checkReadOnlyBySystemuser() {
		boolean result = false;

		Logging.info(this, "checkReadOnly");
		if (exec.getBooleanResult(new OpsiMethodCall("accessControl_userIsReadOnlyUser", new String[] {}))) {
			result = true;
			Logging.info(this, "checkReadOnly " + globalReadOnly);
		}

		return result;
	}

	@Override
	public Map<String, Map<String, Object>> getDepotPropertiesForPermittedDepots() {
		Map<String, Map<String, Object>> depotProperties = getHostInfoCollections().getAllDepots();
		LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = new LinkedHashMap<>();

		String configServer = getHostInfoCollections().getConfigServer();
		if (getDepotPermission(configServer)) {
			depotPropertiesForPermittedDepots.put(configServer, depotProperties.get(configServer));
		}

		for (Entry<String, Map<String, Object>> depotProperty : depotProperties.entrySet()) {
			if (!depotProperty.getKey().equals(configServer) && getDepotPermission(depotProperty.getKey())) {
				depotPropertiesForPermittedDepots.put(depotProperty.getKey(), depotProperty.getValue());
			}
		}

		return depotPropertiesForPermittedDepots;
	}

	private boolean checkFullPermission(Set<String> permittedEntities, final String keyUseList, final String keyList,
			final Map<String, List<Object>> serverPropertyMap) {
		Logging.info(this, "checkFullPermission  key name,  defaultResult true " + keyUseList);

		boolean fullPermission = true;

		if (serverPropertyMap.get(keyUseList) != null) {
			fullPermission = !(Boolean) (serverPropertyMap.get(keyUseList).get(0));
			// we don't give full permission if the config doesn't exist

			// we didn't configure anything, therefore we revoke the setting
			if (serverPropertyMap.get(keyList) == null) {
				fullPermission = true;
				Logging.info(this, "checkFullPermission not configured keyList " + keyList);
			}
		}

		Logging.info(this, "checkFullPermission  key for list,  fullPermission " + keyList + ", " + fullPermission);

		// we didn't configure anything, therefore we revoke the setting
		if (!fullPermission && serverPropertyMap.get(keyList) != null) {
			for (Object val : serverPropertyMap.get(keyList)) {
				permittedEntities.add((String) val);
			}
		}

		Logging.info(this, "checkFullPermission   result " + fullPermission);
		Logging.info(this, "checkFullPermission   produced list " + permittedEntities);

		return fullPermission;
	}

	@Override
	public void checkPermissions() {
		UserOpsipermission.ActionPrivilege serverActionPermission;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

		// variable for simplifying the use of the map
		String configKey = null;

		// already specified via systemuser group
		if (!globalReadOnly) {
			// lookup if we have a config for it and set it though not set by group
			configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			Logging.info(this, "checkPermissions  configKey " + configKey);
			globalReadOnly = (serverPropertyMap.get(configKey) != null)
					&& (Boolean) (serverPropertyMap.get(configKey).get(0));
		}

		Logging.info(this, " checkPermissions globalReadOnly " + globalReadOnly);

		if (globalReadOnly) {
			serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
		} else {
			// is default!!
			boolean mayWriteOnOpsiserver = true;

			configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
			Logging.info(this, "checkPermissions  configKey " + configKey);

			if (serverPropertyMap.get(configKey) != null) {
				Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
				mayWriteOnOpsiserver = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
			}

			Logging.info(this, " checkPermissions mayWriteOnOpsiserver " + mayWriteOnOpsiserver);
			if (mayWriteOnOpsiserver) {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_WRITE;
			} else {
				serverActionPermission = UserOpsipermission.ActionPrivilege.READ_ONLY;
			}
		}

		serverFullPermission = (serverActionPermission == UserOpsipermission.ActionPrivilege.READ_WRITE);

		configKey = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_CREATECLIENT;
		Logging.info(this, " checkPermissions key " + configKey);
		if (serverPropertyMap.get(configKey) != null && withUserRoles) {
			Logging.info(this, " checkPermissions  value  " + (serverPropertyMap.get(configKey).get(0)));
			createClientPermission = (Boolean) ((serverPropertyMap.get(configKey)).get(0));
		}

		String configKeyUseList = null;
		String configKeyList = null;

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		depotsPermitted = new HashSet<>();

		depotsFullPermission = checkFullPermission(depotsPermitted,
				// true,
				configKeyUseList, configKeyList, serverPropertyMap);
		Logging.info(this,
				"checkPermissions depotsFullPermission (false means, depots must be specified " + depotsFullPermission);
		Logging.info(this, "checkPermissions depotsPermitted " + depotsPermitted);

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_HOSTGROUPS_ACCESSIBLE;
		hostgroupsPermitted = new HashSet<>();

		// false, //not only as specified but always
		hostgroupsOnlyIfExplicitlyStated = checkFullPermission(hostgroupsPermitted, configKeyUseList, configKeyList,
				serverPropertyMap);

		if (hostgroupsOnlyIfExplicitlyStated) {
			hostgroupsPermitted = null;
		}

		Logging.info(this, "checkPermissions hostgroupsPermitted " + hostgroupsPermitted);

		configKeyUseList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPACCESS_ONLY_AS_SPECIFIED;
		configKeyList = userPart() + UserOpsipermission.PARTKEY_USER_PRIVILEGE_PRODUCTGROUPS_ACCESSIBLE;
		productgroupsPermitted = new HashSet<>();

		productgroupsFullPermission = checkFullPermission(productgroupsPermitted,
				// false,
				configKeyUseList, configKeyList, serverPropertyMap);

		permittedProducts = null;

		if (!productgroupsFullPermission) {
			permittedProducts = new TreeSet<>();

			for (String group : productgroupsPermitted) {
				Set<String> products = getFProductGroup2Members().get(group);
				if (products != null) {
					permittedProducts.addAll(products);
				}
			}
		}

		Logging.info(this, "checkPermissions permittedProducts " + permittedProducts);
	}

	@Override
	public boolean isServerFullPermission() {
		return serverFullPermission;
	}

	@Override
	public boolean isCreateClientPermission() {
		return createClientPermission;
	}

	@Override
	public boolean isDepotsFullPermission() {
		return depotsFullPermission;
	}

	@Override
	public boolean getDepotPermission(String depotId) {
		if (depotsFullPermission) {
			return true;
		}

		boolean result = false;

		if (depotsPermitted != null) {
			result = depotsPermitted.contains(depotId);
		}

		return result;
	}

	@Override
	public boolean accessToHostgroupsOnlyIfExplicitlyStated() {
		return hostgroupsOnlyIfExplicitlyStated;
	}

	@Override
	public Set<String> getHostgroupsPermitted() {
		Set<String> result = null;
		if (!hostgroupsOnlyIfExplicitlyStated) {
			result = hostgroupsPermitted;
		}

		Logging.info(this, "getHostgroupsPermitted " + result);

		return result;
	}

	@Override
	public boolean getHostgroupPermission(String hostgroupId) {
		if (hostgroupsOnlyIfExplicitlyStated) {
			return true;
		}

		boolean result = false;

		if (hostgroupsPermitted != null) {
			result = hostgroupsPermitted.contains(hostgroupId);
		}

		Logging.info(this, "getHostgroupPermission false for " + hostgroupId);

		if (!result) {
			Logging.info(this, "getHostgroupPermission, permitted " + hostgroupsPermitted);
		}

		return result;
	}

	@Override
	public boolean isProductgroupsFullPermission() {
		return productgroupsFullPermission;
	}

	@Override
	public boolean getProductgroupPermission(String productgroupId) {
		if (productgroupsFullPermission) {
			return true;
		}

		boolean result = false;

		if (productgroupsPermitted != null) {
			result = productgroupsPermitted.contains(productgroupId);
		}

		return result;
	}

	@Override
	public boolean installPackage(String filename) {
		return installPackage(filename, true, "");
	}

	public boolean installPackage(String filename, boolean force, String tempDir) {
		String method = "depot_installPackage";

		Logging.notice(this, method);
		boolean result = exec.doCall(new OpsiMethodCall(method, new Object[] { filename, force }));
		Logging.info(this, "installPackage result " + result);

		return result;
	}

	@Override
	public boolean setRights(String path) {
		String method = "setRights";
		Logging.info(this, "setRights for path " + path);

		String[] args = new String[] { path };

		if (path == null) {
			args = new String[] {};
		}

		return exec.getBooleanResult(new OpsiMethodCall(method, args));
	}

	@Override
	public List<Map<String, Object>> hostRead() {
		String[] callAttributes = new String[] {};
		Map callFilter = new HashMap<>();

		TimeCheck timer = new TimeCheck(this, "HOST_read").start();
		Logging.notice(this, "host_getObjects");
		List<Map<String, Object>> opsiHosts = exec
				.getListOfMaps(new OpsiMethodCall("host_getObjects", new Object[] { callAttributes, callFilter }));
		timer.stop();

		return opsiHosts;
	}

	@Override
	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion,
			String packageVersion, boolean includeFailedInstallations) {
		String[] callAttributes = new String[] {};

		HashMap<String, String> callFilter = new HashMap<>();
		callFilter.put(ProductOnClient.PRODUCT_ID, productId);
		callFilter.put(ProductOnClient.PRODUCT_TYPE, ProductOnClient.LOCALBOOT_ID);

		List<Map<String, Object>> retrievedList = retrieveListOfMapsNOM(callAttributes, callFilter,
				"productOnClient_getObjects");

		List<String> result = new ArrayList<>();

		for (Map<String, Object> m : retrievedList) {
			String client = (String) m.get(ProductOnClient.CLIENT_ID);

			String clientProductVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
			String clientPackageVersion = (String) m.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);

			Object clientProductState = m.get(ProductState.KEY_INSTALLATION_STATUS);

			if (
			// has state unknown, probably because of a failed installation)
			(includeFailedInstallations
					&& InstallationStatus.getLabel(InstallationStatus.UNKNOWN).equals(clientProductState)) ||
			// has wrong product version
					(InstallationStatus.getLabel(InstallationStatus.INSTALLED).equals(clientProductState)
							&& ((!JSONReMapper.equalsNull(clientProductVersion)
									&& !productVersion.equals(clientProductVersion))
									|| (!JSONReMapper.equalsNull(clientPackageVersion)
											&& !packageVersion.equals(clientPackageVersion))))) {
				Logging.debug("getClientsWithOtherProductVersion hit " + m);
				result.add(client);
			}
		}

		Logging.info(this, "getClientsWithOtherProductVersion globally " + result.size());

		// should be done otherwere by preselection of depots

		return result;
	}

	@Override
	public boolean areDepotsSynchronous(Set<String> depots) {
		OpsiMethodCall omc = new OpsiMethodCall("areDepotsSynchronous", new Object[] { depots.toArray() });
		return exec.getBooleanResult(omc);
	}

	protected Map<String, ConfigOption> extractSubConfigOptionsByInitial(final String s) {
		HashMap<String, ConfigOption> result = new HashMap<>();
		getConfigOptions();
		for (Entry<String, ConfigOption> configOption : configOptions.entrySet()) {
			if (configOption.getKey().startsWith(s) && configOption.getKey().length() > s.length()) {
				String xKey = configOption.getKey().substring(s.length());
				result.put(xKey, configOption.getValue());
			}
		}

		return result;
	}

	protected List<Object> buildWANConfigOptions(List<Object> readyObjects) {
		// NOT_WAN meta configs
		Map<String, Object> item = createJSONBoolConfig(AbstractMetaConfig.CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY
				+ "." + CONFIG_CLIENTD_EVENT_GUISTARTUP, true, "meta configuration for default not wan behaviour");

		readyObjects.add(AbstractExecutioner.jsonMap(item));

		item = createJSONBoolConfig(
				AbstractMetaConfig.CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "."
						+ CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN,
				true, "meta configuration for default not wan behaviour");

		readyObjects.add(AbstractExecutioner.jsonMap(item));

		item = createJSONBoolConfig(
				AbstractMetaConfig.CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "."
						+ CONFIG_CLIENTD_EVENT_NET_CONNECTION,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(AbstractExecutioner.jsonMap(item));

		item = createJSONBoolConfig(
				AbstractMetaConfig.CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + "." + CONFIG_CLIENTD_EVENT_TIMER,
				false, "meta configuration for default not wan behaviour");

		readyObjects.add(AbstractExecutioner.jsonMap(item));

		return readyObjects;
	}

	@Override
	public Boolean isInstallByShutdownConfigured(String host) {
		return getHostBooleanConfigValue(KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, host, true, null);
	}

	@Override
	public Boolean isWanConfigured(String host) {
		Logging.info(this, " isWanConfigured wanConfiguration  " + wanConfiguration + " for host " + host);
		return findBooleanConfigurationComparingToDefaults(host, wanConfiguration);
	}

	@Override
	public Boolean isUefiConfigured(String hostname) {
		Boolean result = false;

		if (getConfigs().get(hostname) != null && getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME) != null
				&& !((List) (getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME))).isEmpty()) {
			String configValue = (String) ((List) (getConfigs().get(hostname).get(CONFIG_DHCPD_FILENAME))).get(0);

			if (configValue.indexOf(EFI_STRING) >= 0) {
				// something similar should work, but not this:

				result = true;
			}
		} else if (getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME) != null
				&& !((List) (getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME))).isEmpty()) {
			String configValue = (String) ((List) (getConfigDefaultValues().get(CONFIG_DHCPD_FILENAME))).get(0);

			if (configValue.indexOf(EFI_STRING) >= 0) {
				// something similar should work, but not this:
				result = true;
			}
		}

		return result;
	}

	private Boolean valueFromConfigStateAsExpected(Map<String, Object> configs, String configKey, boolean expectValue) {
		Logging.debug(this, "valueFromConfigStateAsExpected configKey " + configKey);
		boolean result = false;

		if (configs != null && configs.get(configKey) != null && !((List) (configs.get(configKey))).isEmpty()) {
			Logging.debug(this, "valueFromConfigStateAsExpected configKey, values " + configKey + ", valueList "
					+ configs.get(configKey) + " expected " + expectValue);

			Object value = ((List) configs.get(configKey)).get(0);

			if (value instanceof Boolean) {
				if (((Boolean) value).equals(expectValue)) {
					result = true;
				}
			} else if (value instanceof String) {
				if (((String) value).equalsIgnoreCase("" + expectValue)) {
					result = true;
				}
			} else {
				Logging.error(this, "it is not a boolean and not a string, how to handle it ? " + " value " + value);
			}

			Logging.debug(this, "valueFromConfigStateAsExpected " + result);

		}
		return result;
	}

	@Override
	public boolean configureInstallByShutdown(String clientId, boolean shutdownInstall) {
		return setHostBooleanConfigValue(KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN, clientId, shutdownInstall);
	}

	// for checking if WAN default configuration is set
	protected boolean findBooleanConfigurationComparingToDefaults(String host,
			Map<String, List<Object>> defaultConfiguration) {
		boolean tested = false;
		for (Entry<String, List<Object>> configuration : defaultConfiguration.entrySet()) {
			tested = valueFromConfigStateAsExpected(getConfig(host), configuration.getKey(),
					(Boolean) (configuration.getValue().get(0)));
			if (!tested) {
				break;
			}
		}

		return tested;
	}

	protected Map<String, ConfigOption> getWANConfigOptions() {
		Map<String, ConfigOption> allWanConfigOptions = extractSubConfigOptionsByInitial(
				AbstractMetaConfig.CONFIG_KEY + "." + WAN_PARTKEY);

		Logging.info(this, " getWANConfigOptions   " + allWanConfigOptions);

		Map<String, ConfigOption> notWanConfigOptions = extractSubConfigOptionsByInitial(
				AbstractMetaConfig.CONFIG_KEY + "." + NOT_WAN_CONFIGURED_PARTKEY + ".");

		notWanConfiguration = new HashMap<>();
		wanConfiguration = new HashMap<>();

		List<Object> values = null;

		for (Entry<String, ConfigOption> notWanConfigOption : notWanConfigOptions.entrySet()) {
			if (notWanConfigOption.getValue().getType() != ConfigOption.TYPE.BoolConfig) {
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

		Logging.info(this, "getWANConfigOptions wanConfiguration " + wanConfiguration);
		Logging.info(this, "getWANConfigOptions notWanConfiguration  " + notWanConfiguration);

		return allWanConfigOptions;
	}

	private List<Object> addWANConfigStates(String clientId, boolean wan, List<Object> jsonObjects) {
		getWANConfigOptions();

		Logging.debug(this,
				"addWANConfigState  wanConfiguration " + wanConfiguration + "\n " + wanConfiguration.size());
		Logging.debug(this, "addWANConfigState  wanConfiguration.keySet() " + wanConfiguration.keySet() + "\n "
				+ wanConfiguration.keySet().size());

		Logging.debug(this,
				"addWANConfigState  notWanConfiguration " + notWanConfiguration + "\n " + notWanConfiguration.size());
		Logging.debug(this, "addWANConfigState  notWanConfiguration.keySet() " + notWanConfiguration.keySet() + "\n "
				+ notWanConfiguration.keySet().size());

		setConfig(notWanConfiguration); // set the collection
		Logging.info(this, "set notWanConfiguration members where no entry exists ----------------------------- ");
		setConfig(true); // send to opsiserver only new configs

		Map<String, List<Object>> specifiedConfiguration;

		if (wan) {
			specifiedConfiguration = wanConfiguration;
		} else {
			specifiedConfiguration = notWanConfiguration;
		}

		if (jsonObjects == null) {
			jsonObjects = new ArrayList<>();
		}

		for (Entry<String, List<Object>> config : specifiedConfiguration.entrySet()) {
			Logging.info(this, "addWANConfigState configId " + config.getKey());
			Map<String, Object> item = createNOMitem(ConfigStateEntry.TYPE);

			item.put(ConfigStateEntry.CONFIG_ID, config.getKey());

			Logging.info(this, "addWANConfigState values " + config.getValue());

			item.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(config.getValue()));

			item.put(ConfigStateEntry.OBJECT_ID, clientId);

			Logging.info(this, "addWANConfigState configId, item " + config.getKey() + ", " + item);

			// locally, hopefully the RPC call will work
			if (getConfigs().get(clientId) == null) {
				Logging.info(this, "addWANConfigState; until now, no config(State) existed for client " + clientId
						+ " no local update");
				getConfigs().put(clientId, new HashMap<>());
			}

			getConfigs().get(clientId).put(config.getKey(), config.getValue());

			// prepare for JSON RPC
			jsonObjects.add(AbstractExecutioner.jsonMap(item));
		}

		return jsonObjects;
	}

	@Override
	public boolean setWANConfigs(String clientId, boolean wan) {
		boolean result = false;
		Logging.info(this, "setWANConfigs " + clientId + " . " + wan);

		List<Object> jsonObjects = addWANConfigStates(clientId, wan, null);

		OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
		result = exec.doCall(omc);

		return result;
	}

	private Object createUefiJSONEntry(String clientId, String val) {
		Map<String, Object> item = createNOMitem("ConfigState");
		List<String> values = new ArrayList<>();
		values.add(val);
		item.put("objectId", clientId);
		item.put("values", AbstractExecutioner.jsonArray(values));
		item.put("configId", CONFIG_DHCPD_FILENAME);

		return AbstractExecutioner.jsonMap(item);
	}

	@Override
	public boolean configureUefiBoot(String clientId, boolean uefiBoot) {
		boolean result = false;

		Logging.info(this, "configureUefiBoot, clientId " + clientId + " " + uefiBoot);

		List<String> values = new ArrayList<>();

		if (uefiBoot) {
			values.add(EFI_DHCPD_FILENAME);

			List<Object> jsonObjects = new ArrayList<>();
			jsonObjects.add(createUefiJSONEntry(clientId, EFI_DHCPD_FILENAME));

			OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
			result = exec.doCall(omc);
		} else {
			values.add(EFI_DHCPD_NOT);

			List<Object> jsonObjects = new ArrayList<>();
			jsonObjects.add(createUefiJSONEntry(clientId, EFI_DHCPD_NOT));

			OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
			result = exec.doCall(omc);
		}

		// locally
		if (result) {
			if (getConfigs().get(clientId) == null) {
				getConfigs().put(clientId, new HashMap<>());
			}

			Logging.info(this,
					"configureUefiBoot, configs for clientId " + clientId + " " + getConfigs().get(clientId));
			getConfigs().get(clientId).put(CONFIG_DHCPD_FILENAME, values);
		}

		return result;
	}

	@Override
	public boolean createClients(List<List<Object>> clients) {
		List<Object> clientsJsonObject = new ArrayList<>();
		List<Object> productsNetbootJsonObject = new ArrayList<>();
		List<Object> groupsJsonObject = new ArrayList<>();
		List<Object> configStatesJsonObject = new ArrayList<>();

		for (List<Object> client : clients) {
			String hostname = (String) client.get(0);
			String domainname = (String) client.get(1);
			String depotId = (String) client.get(2);
			String description = (String) client.get(3);
			String inventorynumber = (String) client.get(4);
			String notes = (String) client.get(5);
			String systemUUID = (String) client.get(6);
			String macaddress = (String) client.get(7);
			String ipaddress = (String) client.get(8);
			String group = (String) client.get(9);
			String productNetboot = (String) client.get(10);
			boolean wanConfig = Boolean.parseBoolean((String) client.get(12));
			boolean uefiBoot = Boolean.parseBoolean((String) client.get(13));
			boolean shutdownInstall = Boolean.parseBoolean((String) client.get(14));

			String newClientId = hostname + "." + domainname;

			Map<String, Object> hostItem = createNOMitem("OpsiClient");
			hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
			hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
			hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
			hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
			hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
			hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
			hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);

			clientsJsonObject.add(AbstractExecutioner.jsonMap(hostItem));

			Map<String, Object> itemDepot = createNOMitem(ConfigStateEntry.TYPE);
			List<String> valuesDepot = new ArrayList<>();
			valuesDepot.add(depotId);
			itemDepot.put(ConfigStateEntry.OBJECT_ID, newClientId);
			itemDepot.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(valuesDepot));
			itemDepot.put(ConfigStateEntry.CONFIG_ID, CONFIG_DEPOT_ID);

			configStatesJsonObject.add(AbstractExecutioner.jsonMap(itemDepot));

			if (uefiBoot) {
				configStatesJsonObject.add(createUefiJSONEntry(newClientId, EFI_DHCPD_FILENAME));
			}

			if (wanConfig) {
				configStatesJsonObject = addWANConfigStates(newClientId, true, configStatesJsonObject);
			}

			if (shutdownInstall) {
				List<Object> valuesShI = new ArrayList<>();
				valuesShI.add(true);

				Map<String, Object> itemShI = createNOMitem(ConfigStateEntry.TYPE);
				itemShI.put(ConfigStateEntry.OBJECT_ID, newClientId);
				itemShI.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(valuesShI));
				itemShI.put(ConfigStateEntry.CONFIG_ID, KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);

				Logging.info(this, "create client, config item for shutdownInstall " + itemShI);

				configStatesJsonObject.add(AbstractExecutioner.jsonMap(itemShI));
			}

			if (group != null && !group.isEmpty()) {
				Logging.info(this, "createClient" + " group " + group);
				Map<String, Object> itemGroup = createNOMitem(Object2GroupEntry.TYPE_NAME);
				itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
				itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
				itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
				groupsJsonObject.add(AbstractExecutioner.jsonMap(itemGroup));
			}

			if (productNetboot != null && !productNetboot.isEmpty()) {
				Logging.info(this, "createClient" + " productNetboot " + productNetboot);
				Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
				itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productNetboot);
				itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
				itemProducts.put("clientId", newClientId);
				itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
				productsNetbootJsonObject.add(AbstractExecutioner.jsonMap(itemProducts));
			}

			HostInfo hostInfo = new HostInfo(hostItem);
			if (depotId == null || depotId.equals("")) {
				depotId = getHostInfoCollections().getConfigServer();
			}
			hostInfo.setInDepot(depotId);
			hostInfo.setUefiBoot(uefiBoot);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);

			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);
		}

		OpsiMethodCall omc = new OpsiMethodCall("host_createObjects",
				new Object[] { AbstractExecutioner.jsonArray(clientsJsonObject) });
		boolean result = exec.doCall(omc);

		if (result) {
			if (!configStatesJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("configState_updateObjects",
						new Object[] { AbstractExecutioner.jsonArray(configStatesJsonObject) });
				result = exec.doCall(omc);
			}

			if (!groupsJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("objectToGroup_createObjects",
						new Object[] { AbstractExecutioner.jsonArray(groupsJsonObject) });
				result = exec.doCall(omc);
			}

			if (!productsNetbootJsonObject.isEmpty()) {
				omc = new OpsiMethodCall("productOnClient_createObjects",
						new Object[] { AbstractExecutioner.jsonArray(productsNetbootJsonObject) });
				result = exec.doCall(omc);
			}
		}

		return result;
	}

	@Override
	public boolean createClient(String hostname, String domainname, String depotId, String description,
			String inventorynumber, String notes, String ipaddress, String systemUUID, String macaddress,
			boolean shutdownInstall, boolean uefiBoot, boolean wanConfig, String group, String productNetboot,
			String productLocalboot) {
		if (!getDepotPermission(depotId)) {
			return false;
		}

		boolean result = false;

		if (inventorynumber == null) {
			inventorynumber = "";
		}

		if (description == null) {
			description = "";
		}

		if (notes == null) {
			notes = "";
		}

		if (ipaddress.equals("")) {
			ipaddress = null;
			// null works, "" does not in the opsi call
		}

		if (group == null) {
			group = "";
		}

		String newClientId = hostname + "." + domainname;

		Map<String, Object> hostItem = createNOMitem("OpsiClient");
		hostItem.put(HostInfo.HOSTNAME_KEY, newClientId);
		hostItem.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
		hostItem.put(HostInfo.CLIENT_NOTES_KEY, notes);
		hostItem.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, systemUUID);
		hostItem.put(HostInfo.CLIENT_MAC_ADRESS_KEY, macaddress);
		hostItem.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipaddress);
		hostItem.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventorynumber);

		OpsiMethodCall omc = new OpsiMethodCall("host_createObjects",
				new Object[] { AbstractExecutioner.jsonMap(hostItem) });
		result = exec.doCall(omc);

		HostInfo hostInfo = new HostInfo(hostItem);

		if (result) {
			List<Object> jsonObjects = new ArrayList<>();

			Map<String, Object> itemDepot = createNOMitem(ConfigStateEntry.TYPE);
			List<String> valuesDepot = new ArrayList<>();
			valuesDepot.add(depotId);
			itemDepot.put(ConfigStateEntry.OBJECT_ID, newClientId);
			itemDepot.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(valuesDepot));
			itemDepot.put(ConfigStateEntry.CONFIG_ID, CONFIG_DEPOT_ID);

			jsonObjects.add(AbstractExecutioner.jsonMap(itemDepot));

			if (uefiBoot) {
				jsonObjects.add(createUefiJSONEntry(newClientId, EFI_DHCPD_FILENAME));
			}

			if (wanConfig) {
				jsonObjects = addWANConfigStates(newClientId, true, jsonObjects);
			}

			if (shutdownInstall) {
				List<Object> valuesShI = new ArrayList<>();
				valuesShI.add(true);

				Map<String, Object> itemShI = createNOMitem(ConfigStateEntry.TYPE);
				itemShI.put(ConfigStateEntry.OBJECT_ID, newClientId);
				itemShI.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(valuesShI));
				itemShI.put(ConfigStateEntry.CONFIG_ID, KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN);

				Logging.info(this, "create client, config item for shutdownInstall " + itemShI);

				jsonObjects.add(AbstractExecutioner.jsonMap(itemShI));
			}

			omc = new OpsiMethodCall("configState_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });

			result = exec.doCall(omc);
		}

		if ((result) && ((group != null) && (!group.isEmpty()))) {
			Logging.info(this, "createClient" + " group " + group);
			List<Object> jsonObjects = new ArrayList<>();
			Map<String, Object> itemGroup = createNOMitem(Object2GroupEntry.TYPE_NAME);
			itemGroup.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			itemGroup.put(Object2GroupEntry.GROUP_ID_KEY, group);
			itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
			jsonObjects.add(AbstractExecutioner.jsonMap(itemGroup));
			omc = new OpsiMethodCall("objectToGroup_createObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
			result = exec.doCall(omc);
		}

		if ((result) && ((productNetboot != null) && (!productNetboot.isEmpty()))) {
			Logging.info(this, "createClient" + " productNetboot " + productNetboot);
			List<Object> jsonObjects = new ArrayList<>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productNetboot);
			itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
			jsonObjects.add(AbstractExecutioner.jsonMap(itemProducts));
			omc = new OpsiMethodCall("productOnClient_createObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
			result = exec.doCall(omc);
		}

		if ((result) && ((productLocalboot != null) && (!productLocalboot.isEmpty()))) {
			Logging.info(this, "createClient" + " productLocalboot " + productLocalboot);
			List<Object> jsonObjects = new ArrayList<>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DB_KEY_PRODUCT_ID, productLocalboot);
			itemProducts.put(OpsiPackage.SERVICE_KEY_PRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_ACTION_REQUEST), "setup");
			jsonObjects.add(AbstractExecutioner.jsonMap(itemProducts));
			omc = new OpsiMethodCall("productOnClient_createObjects",
					new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
			result = exec.doCall(omc);
		}

		if (result) {
			if (depotId == null || depotId.equals("")) {
				depotId = getHostInfoCollections().getConfigServer();
			}
			hostInfo.setInDepot(depotId);
			hostInfo.setUefiBoot(uefiBoot);
			hostInfo.setWanConfig(wanConfig);
			hostInfo.setShutdownInstall(shutdownInstall);
			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);

			Logging.info(this, " createClient hostInfo " + hostInfo);
		}

		return result;
	}

	@Override
	public boolean renameClient(String hostname, String newHostname) {
		if (globalReadOnly) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall("host_renameOpsiClient", new String[] { hostname, newHostname });
		hostInfoCollections.opsiHostsRequestRefresh();
		return exec.doCall(omc);
	}

	@Override
	public void deleteClient(String hostId) {
		if (globalReadOnly) {
			return;
		}

		OpsiMethodCall omc = new OpsiMethodCall("host_delete", new String[] { hostId });
		exec.doCall(omc);
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	@Override
	public void deleteClients(String[] hostIds) {
		if (globalReadOnly) {
			return;
		}

		for (String hostId : hostIds) {
			OpsiMethodCall omc = new OpsiMethodCall("host_delete", new String[] { hostId });
			exec.doCall(omc);
		}
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	// hostControl methods
	private List<String> collectErrorsFromResponsesByHost(Map<String, Object> responses, String callingMethodName) {
		List<String> errors = new ArrayList<>();

		for (Entry<String, Object> response : responses.entrySet()) {
			JSONObject jO = (JSONObject) response.getValue();
			String error = JSONReMapper.getErrorFromResponse(jO);

			if (error != null) {
				error = response.getKey() + ":\t" + error;
				Logging.info(callingMethodName + ",  " + error);
				errors.add(error);
			}
		}

		return errors;
	}

	@Override
	public List<String> deletePackageCaches(String[] hostIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControlSafe_opsiclientdRpc",
				new Object[] { "cacheService_deleteCache", new Object[] {}, hostIds });

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "deleteCache");
	}

	@Override
	public Map<String, List<String>> getHostSeparationByDepots(String[] hostIds) {
		Map<String, Set<String>> hostSeparationByDepots = new HashMap<>();

		for (String hostId : hostIds) {
			String depotId = getHostInfoCollections().getMapPcBelongsToDepot().get(hostId);

			hostSeparationByDepots.computeIfAbsent(depotId, arg -> new HashSet<>()).add(hostId);
		}

		Map<String, List<String>> result = new HashMap<>();
		for (Entry<String, Set<String>> hostSeparationEntry : hostSeparationByDepots.entrySet()) {
			result.put(hostSeparationEntry.getKey(), new ArrayList<>(hostSeparationEntry.getValue()));
		}

		return result;
	}

	@Override
	public List<String> wakeOnLan(String[] hostIds) {
		return wakeOnLan(getHostSeparationByDepots(hostIds));
	}

	protected List<String> wakeOnLan(Map<String, List<String>> hostSeparationByDepot) {
		Map<String, Object> responses = new HashMap<>();

		Map<String, AbstractExecutioner> executionerForDepots = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			Logging.info(this,
					"from depot " + hostSeparationEntry.getKey() + " we have hosts " + hostSeparationEntry.getValue());

			AbstractExecutioner exec1 = executionerForDepots.get(hostSeparationEntry.getKey());

			Logging.info(this, "working exec for depot " + hostSeparationEntry.getKey() + " " + (exec1 != null));

			if (exec1 == null) {
				exec1 = retrieveWorkingExec(hostSeparationEntry.getKey());
			}

			if (exec1 != null && exec1 != AbstractExecutioner.getNoneExecutioner()) {
				OpsiMethodCall omc = new OpsiMethodCall("hostControl_start",
						new Object[] { hostSeparationEntry.getValue().toArray(new String[0]) });

				Map<String, Object> responses1 = exec1.getMapResult(omc);
				responses.putAll(responses1);
			}
		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	@Override
	public List<String> wakeOnLan(Set<String> hostIds, Map<String, List<String>> hostSeparationByDepot,
			Map<String, AbstractExecutioner> execsByDepot) {
		Map<String, Object> responses = new HashMap<>();

		for (Entry<String, List<String>> hostSeparationEntry : hostSeparationByDepot.entrySet()) {
			if (hostSeparationEntry.getValue() != null && !hostSeparationEntry.getValue().isEmpty()) {
				Set<String> hostsToWake = new HashSet<>(hostIds);
				hostsToWake.retainAll(hostSeparationEntry.getValue());

				if (execsByDepot.get(hostSeparationEntry.getKey()) != null
						&& execsByDepot.get(hostSeparationEntry.getKey()) != AbstractExecutioner.getNoneExecutioner()
						&& !hostsToWake.isEmpty()) {
					Logging.debug(this, "wakeOnLan execute for " + hostsToWake);
					OpsiMethodCall omc = new OpsiMethodCall("hostControl_start",
							new Object[] { hostsToWake.toArray(new String[0]) });

					Map<String, Object> responses1 = execsByDepot.get(hostSeparationEntry.getKey()).getMapResult(omc);
					responses.putAll(responses1);
				}
			}

		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");
	}

	@Override
	public List<String> fireOpsiclientdEventOnClients(String event, String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_fireEvent", new Object[] { event, clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	@Override
	public List<String> showPopupOnClients(String message, String[] clientIds, Float seconds) {
		OpsiMethodCall omc;

		if (seconds == 0.0) {
			omc = new OpsiMethodCall("hostControl_showPopup", new Object[] { message, clientIds });
		} else {
			omc = new OpsiMethodCall("hostControl_showPopup",
					new Object[] { message, clientIds, "True", "True", seconds });
		}

		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");

	}

	@Override
	public List<String> shutdownClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_shutdown", new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "shutdownClients");
	}

	@Override
	public List<String> rebootClients(String[] clientIds) {
		OpsiMethodCall omc = new OpsiMethodCall("hostControl_reboot", new Object[] { clientIds });
		Map<String, Object> responses = exec.getMapResult(omc);
		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	@Override
	public Map<String, Object> reachableInfo(String[] clientIds) {
		Logging.info(this, "reachableInfo ");
		Object[] callParameters = new Object[] {};

		String methodName = "hostControl_reachable";
		if (clientIds != null) {
			Logging.info(this, "reachableInfo for clientIds " + clientIds.length);
			callParameters = new Object[] { clientIds };
			methodName = "hostControlSafe_reachable";
		}

		// background call, do not show waiting info
		return exec.getMapResult(new OpsiMethodCall(methodName, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT));
	}

	@Override
	public Map<String, Integer> getInstalledOsOverview() {
		Logging.info(this, "getInstalledOsOverview");

		Map<String, Object> producedLicencingInfo = getLicencingInfo();
		Map<String, Integer> map = new HashMap<>();
		JSONObject jO = (JSONObject) producedLicencingInfo.get("client_numbers");

		try {
			Iterator<String> iter = jO.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				map.put(key, (Integer) jO.get(key));
			}
		} catch (JSONException jex) {
			Logging.error(this, "Exception on getting Map " + jex.toString());
		}

		return map;
	}

	@Override
	public Map<String, Object> getLicencingInfo() {
		Logging.info(this, "getLicensingInfo");

		Object[] callParameters = { true, true, true };
		String methodName = "backend_getLicensingInfo";
		OpsiMethodCall omc = new OpsiMethodCall(methodName, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT);

		return exec.getMapResult(omc);
	}

	@Override
	public List<Map<String, Object>> getModules() {
		Logging.info(this, "getModules");

		Map<String, Object> producedLicencingInfo = getLicencingInfo();
		return JSONReMapper.getListOfMaps((JSONArray) producedLicencingInfo.get("licenses"));
	}

	@Override
	public Map<String, String> sessionInfo(String[] clientIds) {
		Map<String, String> result = new HashMap<>();

		Object[] callParameters = new Object[] {};
		if (clientIds != null && clientIds.length > 0) {
			callParameters = new Object[] { clientIds };
		}
		String methodname = "hostControl_getActiveSessions";

		Map<String, Object> result0 = JSONReMapper.getResponses(
				exec.retrieveJSONObject(new OpsiMethodCall(methodname, callParameters, OpsiMethodCall.BACKGROUND_DEFAULT // background																																									
				))); // call, do not show waiting info

		for (Entry<String, Object> resultEntry : result0.entrySet()) {
			StringBuilder value = new StringBuilder();

			if (resultEntry.getValue() instanceof String) {
				// error

				String errorStr = (String) resultEntry.getValue();
				value = new StringBuilder("no response");
				if (errorStr.indexOf("Opsi timeout") > -1) {
					int i = errorStr.indexOf("(");
					if (i > -1) {
						value.append("   " + errorStr.substring(i + 1, errorStr.length() - 1));
					} else {
						value.append(" (opsi timeout)");
					}
				} else if (errorStr.indexOf(methodname) > -1) {
					value.append("  (" + methodname + " not valid)");
				} else if (errorStr.indexOf("Name or service not known") > -1) {
					value.append(" (name or service not known)");
				}
			} else if (resultEntry.getValue() instanceof List) {
				// should then hold

				List<?> sessionlist = (List<?>) resultEntry.getValue();
				for (Object element : sessionlist) {
					Map<String, Object> session = JSONReMapper.getMapObject((JSONObject) element);

					String username = "" + session.get("UserName");
					String logondomain = "" + session.get("LogonDomain");

					if (!value.toString().equals("")) {
						value.append("; ");
					}

					value.append(username + " (" + logondomain + "\\" + username + ")");
				}
			}

			result.put(resultEntry.getKey(), value.toString());
		}

		return result;
	}

	// executes all updates collected by setHostDescription ...
	@Override
	public void updateHosts() {
		if (globalReadOnly) {
			return;
		}

		// checkHostPermission is done in updateHost

		if (hostUpdates == null) {
			return;
		}

		List<Object> updates = new ArrayList<>();
		for (Map<String, Object> hostUpdateValue : hostUpdates.values()) {
			updates.add(AbstractExecutioner.jsonMap(hostUpdateValue));
		}

		OpsiMethodCall omc = new OpsiMethodCall("host_updateObjects", new Object[] { updates.toArray() });

		if (exec.doCall(omc)) {
			hostUpdates.clear();
		}
	}

	protected void updateHost(String hostId, String property, String value) {
		if (hostUpdates == null) {
			hostUpdates = new HashMap<>();
		}

		Map<String, Object> hostUpdateMap = hostUpdates.get(hostId);

		if (hostUpdateMap == null) {
			hostUpdateMap = new HashMap<>();
		}

		hostUpdateMap.put("ident", hostId);
		hostUpdateMap.put(HostInfo.HOST_TYPE_KEY, "OpsiClient");
		hostUpdateMap.put(property, value);

		hostUpdates.put(hostId, hostUpdateMap);
	}

	@Override
	public void setHostDescription(String hostId, String description) {
		updateHost(hostId, HostInfo.CLIENT_DESCRIPTION_KEY, description);
	}

	@Override
	public void setClientInventoryNumber(String hostId, String inventoryNumber) {
		updateHost(hostId, HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventoryNumber);
	}

	@Override
	public void setClientOneTimePassword(String hostId, String oneTimePassword) {
		updateHost(hostId, HostInfo.CLIENT_ONE_TIME_PASSWORD_KEY, oneTimePassword);
	}

	@Override
	public void setHostNotes(String hostId, String notes) {
		updateHost(hostId, HostInfo.CLIENT_NOTES_KEY, notes);
	}

	@Override
	public void setSystemUUID(String hostId, String uuid) {
		updateHost(hostId, HostInfo.CLIENT_SYSTEM_UUID_KEY, uuid);
	}

	@Override
	public void setMacAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_MAC_ADRESS_KEY, address);
	}

	@Override
	public void setIpAddress(String hostId, String address) {
		updateHost(hostId, HostInfo.CLIENT_IP_ADDRESS_KEY, address);
	}

	// opsi 3 compatibility
	@Override
	public String getMacAddress(String hostId) {
		return "";
	}

	@Override
	public Map<String, Map<String, String>> getProductGroups() {
		if (productGroups != null) {
			return productGroups;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		Map<String, Map<String, String>> result = exec.getStringMappedObjectsByKey(
				new OpsiMethodCall("group_getObjects", new Object[] { callAttributes, callFilter }), "ident",
				new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" });

		productGroups = result;

		return productGroups;
	}

	@Override
	public void productGroupsRequestRefresh() {
		productGroups = null;
	}

	@Override
	public Map<String, Map<String, String>> getHostGroups() {
		if (hostGroups != null) {
			return hostGroups;
		}

		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		hostGroups = new HostGroups(exec.getStringMappedObjectsByKey(
				new OpsiMethodCall("group_getObjects", new Object[] { callAttributes, callFilter }), "ident",
				new String[] { "id", "parentGroupId", "description" },
				new String[] { "groupId", "parentGroupId", "description" }));

		Logging.debug(this, "getHostGroups " + hostGroups);

		hostGroups = hostGroups.addSpecialGroups();
		Logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups.alterToWorkingVersion();

		Logging.debug(this, "getHostGroups rebuilt" + hostGroups);

		return hostGroups;
	}

	@Override
	public void hostGroupsRequestRefresh() {
		hostGroups = null;
	}

	@Override
	public void fGroup2MembersRequestRefresh() {
		fGroup2Members = null;
	}

	@Override
	public void fProductGroup2MembersRequestRefresh() {
		fProductGroup2Members = null;
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

	@Override
	public Map<String, Set<String>> getFGroup2Members() {
		if (fGroup2Members == null) {
			fGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId");
		}

		return fGroup2Members;
	}

	@Override
	public Map<String, Set<String>> getFProductGroup2Members() {
		if (fProductGroup2Members == null) {
			fProductGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId");
		}

		return fProductGroup2Members;
	}

	// returns the function that yields for a given groupId all objects which belong
	// to the group
	private Map<String, Set<String>> retrieveFGroup2Members(String groupType, String memberIdName) {
		String[] callAttributes = new String[] {};
		Map<String, String> callFilter = new HashMap<>();
		callFilter.put("groupType", groupType);

		Map<String, Map<String, String>> mappedRelations =

				exec.getStringMappedObjectsByKey(
						new OpsiMethodCall("objectToGroup_getObjects", new Object[] { callAttributes, callFilter }),
						"ident", new String[] { "objectId", "groupId" }, new String[] { memberIdName, "groupId" });

		return projectToFunction(mappedRelations, "groupId", memberIdName);
	}

	public void fObject2ProductGroupsRequestRefresh() {
		fObject2Groups = null;
	}

	@Override
	public void fObject2GroupsRequestRefresh() {
		fObject2Groups = null;
	}

	// returns the function that yields for a given clientId all groups to which the
	// client belongs
	@Override
	public Map<String, Set<String>> getFObject2Groups() {
		if (fObject2Groups == null) {
			Map<String, Map<String, String>> mappedRelations =

					exec.getStringMappedObjectsByKey(new OpsiMethodCall("objectToGroup_getObjects", new String[] {}),
							"ident", new String[] { "objectId", "groupId" }, new String[] { "clientId", "groupId" },
							ClientTree.getTranslationsFromPersistentNames());

			fObject2Groups = projectToFunction(mappedRelations, "clientId", "groupId");

		}

		return fObject2Groups;
	}

	@Override
	public boolean addHosts2Group(List<String> objectIds, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		Logging.info(this, "addHosts2Group hosts " + objectIds);

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);

		List<Object> jsonObjects = new ArrayList<>();

		for (String ob : objectIds) {
			Map<String, Object> item = createNOMitem(Object2GroupEntry.TYPE_NAME);
			item.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			item.put(Object2GroupEntry.MEMBER_KEY, ob);
			item.put(Object2GroupEntry.GROUP_ID_KEY, persistentGroupId);
			jsonObjects.add(AbstractExecutioner.jsonMap(item));
		}

		Logging.info(this, "addHosts2Group persistentGroupId " + persistentGroupId);

		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_createObjects",
				new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });

		return exec.doCall(omc);
	}

	@Override
	public boolean addObject2Group(String objectId, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		Logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_create",
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	@Override
	public boolean removeHostGroupElements(List<Object2GroupEntry> entries) {
		if (globalReadOnly) {
			return false;
		}

		List<Object> deleteItems = new ArrayList<>();
		for (Object2GroupEntry entry : entries) {
			Map<String, Object> deleteItem = createNOMitem(Object2GroupEntry.TYPE_NAME);
			deleteItem.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			deleteItem.put(Object2GroupEntry.GROUP_ID_KEY, entry.getGroupId());
			deleteItem.put(Object2GroupEntry.MEMBER_KEY, entry.getMember());

			deleteItems.add(AbstractExecutioner.jsonMap(deleteItem));
		}

		boolean result = true;
		if (!deleteItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_deleteObjects",
					new Object[] { deleteItems.toArray() });

			if (exec.doCall(omc)) {
				deleteItems.clear();
			} else {
				result = false;
			}
		}

		return result;
	}

	@Override
	public boolean removeObject2Group(String objectId, String groupId) {
		if (globalReadOnly) {
			return false;
		}

		String persistentGroupId = ClientTree.translateToPersistentName(groupId);
		OpsiMethodCall omc = new OpsiMethodCall("objectToGroup_delete",
				new String[] { Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId });

		return exec.doCall(omc);
	}

	@Override
	public boolean addGroup(StringValuedRelationElement newgroup) {
		return addGroup(newgroup, true);
	}

	private boolean addGroup(StringValuedRelationElement newgroup, boolean requestRefresh) {
		if (!serverFullPermission) {
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

		OpsiMethodCall omc = new OpsiMethodCall("group_createHostGroup",
				new Object[] { id, description, notes, parentId });
		boolean result = exec.doCall(omc);
		if (result) {
			hostGroupsRequestRefresh();
		}

		return result;

	}

	@Override
	public boolean deleteGroup(String groupId) {
		if (!serverFullPermission) {
			return false;
		}

		if (groupId == null) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall("group_delete", new String[] { groupId });
		boolean result = exec.doCall(omc);

		if (result) {
			hostGroupsRequestRefresh();
		}

		return result;
	}

	@Override
	public boolean updateGroup(String groupId, Map<String, String> updateInfo) {
		if (!serverFullPermission) {
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

		OpsiMethodCall omc = new OpsiMethodCall("group_updateObject",
				new Object[] { AbstractExecutioner.jsonMap(updateInfo) });
		return exec.doCall(omc);
	}

	@Override
	public boolean setProductGroup(String groupId, String description, Set<String> productSet) {
		if (!serverFullPermission) {
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

		OpsiMethodCall omc = new OpsiMethodCall("group_createObjects",
				new Object[] { new Object[] { AbstractExecutioner.jsonMap(map) } });
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

		List<JSONObject> object2Groups = new ArrayList<>();
		for (String objectId : inOriSetnotInNewSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(AbstractExecutioner.jsonMap(m));
		}

		Logging.debug(this, "delete objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			Object jsonArray = AbstractExecutioner.jsonArray(object2Groups);
			result = result
					&& exec.doCall(new OpsiMethodCall("objectToGroup_deleteObjects", new Object[] { jsonArray }));
		}

		object2Groups.clear();
		for (String objectId : inNewSetnotInOriSet) {
			Map<String, String> m = new HashMap<>(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(AbstractExecutioner.jsonMap(m));
		}

		Logging.debug(this, "create new objects " + object2Groups);

		if (!object2Groups.isEmpty()) {
			Object jsonArray = AbstractExecutioner.jsonArray(object2Groups);
			result = result
					&& exec.doCall(new OpsiMethodCall("objectToGroup_createObjects", new Object[] { jsonArray }));
		}

		if (result) {
			getFProductGroup2Members().put(groupId, productSet);
		}

		return result;
	}

	@Override
	public List<String> getHostGroupIds() {
		Set<String> groups = getHostGroups().keySet();
		groups.remove(ClientTree.DIRECTORY_NAME);

		return new ArrayList<>(groups);
	}

	@Override
	public void hwAuditConfRequestRefresh() {
		hwAuditConf.clear();
		hwAuditDeviceClasses = null;
		client2HwRowsColumnNames = null;

		if (opsiHwClassNames != null) {
			opsiHwClassNames.clear();
		}
		opsiHwClassNames = null;
	}

	// partial version of produceHwAuditDeviceClasses()
	private List<String> produceHwClasses(List<Map<String, Object>> hwAuditConf) {
		List<String> result = new ArrayList<>();

		for (Map<String, Object> hwAuditClass : hwAuditConf) {
			if (!(hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) instanceof Map)) {
				Logging.warning(this, "getAllHwClassNames illegal hw config item, having hwAuditClass.get Class "
						+ hwAuditClass.get(hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY)));
				if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) != null) {
					Logging.warning(this,
							"getAllHwClassNames illegal hw config item,  hwAuditClass.get Class is of class "
									+ hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY).getClass());
				}

				continue;
			}
			String hwClass = (String) (((Map) (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY)))
					.get(OpsiHwAuditDeviceClass.OPSI_KEY));

			result.add(hwClass);
		}

		return result;
	}

	@Override
	public List<Map<String, Object>> getOpsiHWAuditConf() {
		if (hwAuditConf == null) {
			Logging.warning("hwAuditConf is null in getOpsiHWAuditConf");
			return new ArrayList<>();
		} else if (!hwAuditConf.containsKey("")) {
			hwAuditConf.put("",
					exec.getListOfMapsOfListsOfMaps(new OpsiMethodCall("auditHardware_getConfig", new String[] {})));
			if (hwAuditConf.get("") == null) {
				Logging.warning(this, "got no hardware config");
			}
		}
		return hwAuditConf.get("");
	}

	@Override
	public List<Map<String, Object>> getOpsiHWAuditConf(String locale) {
		hwAuditConf.putIfAbsent(locale, exec
				.getListOfMapsOfListsOfMaps(new OpsiMethodCall("auditHardware_getConfig", new String[] { locale })));

		return hwAuditConf.get(locale);
	}

	@Override
	public List<String> getAllHwClassNames() {
		if (opsiHwClassNames == null) {
			opsiHwClassNames = produceHwClasses(getOpsiHWAuditConf());
		}

		Logging.info(this, "getAllHwClassNames, hw classes " + opsiHwClassNames);

		return opsiHwClassNames;
	}

	@Override
	public Map<String, OpsiHwAuditDeviceClass> getHwAuditDeviceClasses() {
		if (hwAuditDeviceClasses == null) {
			produceHwAuditDeviceClasses();
		}

		return hwAuditDeviceClasses;
	}

	@Override
	public void softwareAuditOnClientsRequestRefresh() {
		Logging.info(this, "softwareAuditOnClientsRequestRefresh");
		dataStub.softwareAuditOnClientsRequestRefresh();
	}

	@Override
	public void fillClient2Software(List<String> clients) {
		dataStub.fillClient2Software(clients);
	}

	@Override
	public Map<String, List<SWAuditClientEntry>> getClient2Software() {
		return dataStub.getClient2Software();
	}

	/*
	 * the method is only additionally called because of the retry mechanism
	 */
	@Override
	public DatedRowList getSoftwareAudit(String clientId) {
		DatedRowList result = getSoftwareAuditOnce(clientId, true);

		// we retry once
		if (result == null) {
			result = getSoftwareAuditOnce(clientId, false);
		}

		return result;
	}

	// closely related to retrieveSoftwareAuditData(String clientId)
	private DatedRowList getSoftwareAuditOnce(String clientId, boolean withRetry) {
		dataStub.fillClient2Software(clientId);

		List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);

		if (entries == null) {
			return null;
		}

		List<String[]> list = new ArrayList<>();
		String dateS = null;

		if (!entries.isEmpty()) {
			dateS = entries.get(0).getLastModification();
		}

		for (SWAuditClientEntry entry : entries) {
			if (entry.getSWid() != null) {
				int i = entry.getSWid();

				Logging.debug(this, "getSoftwareAudit,  ID " + i + " for client entry " + entry);
				if (i == -1 && withRetry) {
					Logging.info(this, "getSoftwareAudit,  not found client entry " + entry);
					int returnedOption = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(),
							Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.message") + " "
									+ entry.getSWident()
									+ Configed.getResourceValue(
											"PersistenceController.reloadSoftwareInformation.question")
									+ Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.info"),
							Configed.getResourceValue("PersistenceController.reloadSoftwareInformation.title"),
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					switch (returnedOption) {
					case JOptionPane.NO_OPTION:
						break;

					case JOptionPane.YES_OPTION:
						installedSoftwareInformationRequestRefresh();
						softwareAuditOnClientsRequestRefresh();
						return null;

					case JOptionPane.CANCEL_OPTION:
						return null;

					default:
						Logging.warning(this, "no case found for returnedOption in getSoftwareAuditOnce");
						break;
					}
				} else {
					if (i != -1) {
						list.add(entry.getExpandedData(getInstalledSoftwareInformation(), getSWident(i)));
					}
				}
			}
		}

		Logging.info(this, "getSoftwareAuditBase for client, list.size " + clientId + ", " + list.size());
		return new DatedRowList(list, dateS);
	}

	@Override
	public String getLastSoftwareAuditModification(String clientId) {
		String result = "";

		if (clientId != null && !clientId.equals("") && dataStub.getClient2Software() != null
				&& dataStub.getClient2Software().get(clientId) != null
				&& !dataStub.getClient2Software().get(clientId).isEmpty()) {
			result = dataStub.getClient2Software().get(clientId).get(0).getLastModification();
		}

		return result;
	}

	@Override
	public Map<String, Map<String, Object>> retrieveSoftwareAuditData(String clientId) {
		Map<String, Map<String, Object>> result = new TreeMap<>();

		if (clientId == null || clientId.equals("")) {
			return result;
		}

		dataStub.fillClient2Software(clientId);

		List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);

		if (entries == null) {
			return result;
		}

		for (SWAuditClientEntry entry : entries) {
			if (entry.getSWid() != null && entry.getSWid() != -1) {
				result.put("" + entry.getSWid(),
						entry.getExpandedMap(getInstalledSoftwareInformation(), getSWident(entry.getSWid())));
			}
		}

		return result;
	}

	@Override
	public Map<String, List<Map<String, Object>>> getHardwareInfo(String clientId, boolean asHTMLtable) {
		if (clientId == null) {
			return null;
		}

		Map<String, List<Map<String, Object>>> info = exec
				.getMapOfListsOfMaps(new OpsiMethodCall("getHardwareInformation_hash", new String[] { clientId }));

		// the first element is a default scantime
		if (info.size() > 1) {
			return info;
		}

		return null;
	}

	@Override
	public void auditHardwareOnHostRequestRefresh() {
		relationsAuditHardwareOnHost = null;
	}

	@Override
	public List<Map<String, Object>> getHardwareOnClient() {
		if (relationsAuditHardwareOnHost == null) {
			Map<String, String> filterMap = new HashMap<>();
			filterMap.put("state", "1");
			relationsAuditHardwareOnHost = exec.getListOfMaps(
					new OpsiMethodCall("auditHardwareOnHost_getHashes", new Object[] { new String[0], filterMap }));
		}

		return relationsAuditHardwareOnHost;
	}

	/* multiclient hwinfo */

	@Override
	public List<String> getHostColumnNames() {
		retrieveClient2HwRowsColumnNames();
		return hostColumnNames;
	}

	@Override
	public List<String> getClient2HwRowsColumnNames() {
		retrieveClient2HwRowsColumnNames();
		return client2HwRowsColumnNames;
	}

	@Override
	public List<String> getClient2HwRowsJavaclassNames() {
		retrieveClient2HwRowsColumnNames();
		return client2HwRowsJavaclassNames;
	}

	protected void produceHwAuditDeviceClasses() {
		hwAuditDeviceClasses = new TreeMap<>();

		if (getOpsiHWAuditConf().isEmpty()) {
			Logging.error(this, "no hwaudit config found ");
			return;
		}

		for (Map<String, Object> hwAuditClass : getOpsiHWAuditConf()) {
			if (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) == null
					|| hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) == null
					|| !(hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY) instanceof Map)
					|| !(hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY) instanceof List)) {
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
			String hwClass = (String) (((Map) (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY)))
					.get(OpsiHwAuditDeviceClass.OPSI_KEY));

			OpsiHwAuditDevicePropertyType firstSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			firstSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.FIRST_SEEN_COL_NAME);
			firstSeen.setOpsiDbColumnType("timestamp");
			firstSeen.setUiName("first seen");
			OpsiHwAuditDevicePropertyType lastSeen = new OpsiHwAuditDevicePropertyType(hwClass);
			lastSeen.setOpsiDbColumnName(OpsiHwAuditDeviceClass.LAST_SEEN_COL_NAME);
			lastSeen.setOpsiDbColumnType("timestamp");
			lastSeen.setUiName("last seen");

			OpsiHwAuditDeviceClass hwAuditDeviceClass = new OpsiHwAuditDeviceClass(hwClass);
			hwAuditDeviceClasses.put(hwClass, hwAuditDeviceClass);

			hwAuditDeviceClass.setLinuxQuery((String) (((Map) (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY)))
					.get(OpsiHwAuditDeviceClass.LINUX_KEY)));
			hwAuditDeviceClass.setWmiQuery((String) (((Map) (hwAuditClass.get(OpsiHwAuditDeviceClass.CLASS_KEY)))
					.get(OpsiHwAuditDeviceClass.WMI_KEY)));

			Logging.info(this, "hw audit class " + hwClass);

			for (Object m : (List<?>) hwAuditClass.get(OpsiHwAuditDeviceClass.LIST_KEY)) {
				if (!(m instanceof Map)) {
					Logging.warning(this, "getAllHwClassNames illegal VALUES item, m " + m);
					continue;
				}

				Map<?, ?> ma = (Map<?, ?>) m;

				if (ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY).equals("i")) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));
					devProperty.setUiName((String) ma.get(OpsiHwAuditDeviceClass.UI_KEY));

					hwAuditDeviceClass.addHostRelatedProperty(devProperty);
					hwAuditDeviceClass.setHostConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE).toLowerCase());

				} else if (ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY).equals("g")) {
					OpsiHwAuditDevicePropertyType devProperty = new OpsiHwAuditDevicePropertyType(hwClass);
					devProperty.setOpsiDbColumnName((String) ma.get(OpsiHwAuditDeviceClass.OPSI_KEY));
					devProperty.setOpsiDbColumnType((String) ma.get(OpsiHwAuditDeviceClass.TYPE_KEY));
					devProperty.setUiName((String) ma.get(OpsiHwAuditDeviceClass.UI_KEY));

					hwAuditDeviceClass.addHwItemRelatedProperty(devProperty);
					hwAuditDeviceClass.setHwItemConfigKey((OpsiHwAuditDeviceClass.CONFIG_KEY + "." + hwClass + "_"
							+ OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE).toLowerCase());
				} else {
					Logging.warning(this, "getAllHwClassNames illegal value for key " + OpsiHwAuditDeviceClass.SCOPE_KEY
							+ " " + ma.get(OpsiHwAuditDeviceClass.SCOPE_KEY));
				}
			}

			hwAuditDeviceClass.addHostRelatedProperty(firstSeen);
			hwAuditDeviceClass.addHostRelatedProperty(lastSeen);

			Logging.info(this, "hw audit class " + hwAuditDeviceClass);
		}

		Logging.info(this, "produceHwAuditDeviceClasses hwAuditDeviceClasses size " + hwAuditDeviceClasses.size());
	}

	@Override
	public List<String> getHwInfoClassNames() {
		retrieveClient2HwRowsColumnNames();
		Logging.info(this, "getHwInfoClassNames " + hwInfoClassNames);
		return hwInfoClassNames;
	}

	private String cutClassName(String columnName) {
		String result = null;

		if (!columnName.startsWith("HOST") && columnName.startsWith(HW_INFO_CONFIG)) {
			result = columnName.substring(HW_INFO_CONFIG.length());
			result = result.substring(0, result.indexOf('.'));
		} else if (columnName.startsWith(HW_INFO_DEVICE)) {
			result = columnName.substring(HW_INFO_DEVICE.length());
			result = result.substring(0, result.indexOf('.'));
		} else {
			Logging.warning(this, "cutClassName " + "unexpected columnName " + columnName);
		}

		return result;
	}

	protected void retrieveClient2HwRowsColumnNames() {
		getConfigOptions();

		Logging.info(this, "retrieveClient2HwRowsColumnNames " + "client2HwRowsColumnNames == null "
				+ (client2HwRowsColumnNames == null));
		if (client2HwRowsColumnNames == null || client2HwRowsJavaclassNames == null || hwInfoClassNames == null) {
			hostColumnNames = new ArrayList<>();

			hostColumnNames.add(Host.ID_COLUMN);
			hostColumnNames.add(Host.DESCRIPTION_COLUMN);
			hostColumnNames.add(Host.HW_ADRESS_COLUMN);
			hostColumnNames.add(LAST_SEEN_VISIBLE_COL_NAME);

			getConfigOptions();
			// there is produced client2HwRowsColumnNames

			client2HwRowsColumnNames = new ArrayList<>(hostColumnNames);

			for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
				OpsiHwAuditDeviceClass hwAuditDeviceClass = hwClass.getValue();

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHostProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = HW_INFO_CONFIG + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
						client2HwRowsColumnNames.add(col);
					}
				}

				for (OpsiHwAuditDevicePropertyType deviceProperty : hwAuditDeviceClass.getDeviceHwItemProperties()) {
					if (deviceProperty.getDisplayed() != null && deviceProperty.getDisplayed()) {
						String col = HW_INFO_DEVICE + hwClass.getKey() + "." + deviceProperty.getOpsiDbColumnName();
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
		}
	}

	@Override
	public void client2HwRowsRequestRefresh() {
		hostColumnNames = null;
		client2HwRowsColumnNames = null;
		hwInfoClassNames = null;
		dataStub.client2HwRowsRequestRefresh();
	}

	@Override
	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		Map<String, Map<String, Object>> client2HwRows = dataStub.getClient2HwRows(hosts);

		for (String host : hosts) {
			Logging.info(this, "getClient2HwRows host " + host);

			if (client2HwRows.get(host) == null) {
				Logging.info(this, "getClient2HwRows for host " + host + " is null");
			}
		}

		return client2HwRows;
	}

	private Map<String, Object> produceHwAuditColumnConfig(String configKey,
			List<OpsiHwAuditDevicePropertyType> deviceProperties, Map<String, Boolean> tableConfigUpdates) {
		List<Object> oldDefaultValues = new ArrayList<>();

		if (configOptions.get(configKey) != null) {
			oldDefaultValues = configOptions.get(configKey).getDefaultValues();
		}

		Logging.info(this, "produceHwAuditColumnConfig " + oldDefaultValues);

		List<Object> possibleValues = new ArrayList<>();
		for (OpsiHwAuditDevicePropertyType deviceProperty : deviceProperties) {
			possibleValues.add(deviceProperty.getOpsiDbColumnName());
		}

		Logging.info(this, "produceConfig, possibleValues " + possibleValues);

		List<Object> newDefaultValues = new ArrayList<>();
		for (Object value : possibleValues) {
			if (oldDefaultValues.contains(value)) {
				// was in default values and no change, or value is in (old) default values and
				// set again
				if ((tableConfigUpdates.get(value) == null) || Boolean.TRUE.equals(tableConfigUpdates.get(value))) {
					newDefaultValues.add(value);
				}
			} else if (tableConfigUpdates.get(value) != null && tableConfigUpdates.get(value)) {
				// change, value is now configured
				newDefaultValues.add(value);
			}
		}

		Map<String, Object> configItem = createJSONConfig(ConfigOption.TYPE.UnicodeConfig, configKey, // key
				"", false, // editable
				true, // multivalue
				newDefaultValues, possibleValues);

		Logging.info(this, "produceConfig, created an item " + configItem);

		return configItem;
	}

	@Override
	public boolean saveHwColumnConfig(Map<String, Map<String, Boolean>> updateItems) {
		getConfigOptions();

		List<Object> readyObjects = new ArrayList<>();

		for (Entry<String, OpsiHwAuditDeviceClass> hwClass : hwAuditDeviceClasses.entrySet()) {
			OpsiHwAuditDeviceClass hwAuditDeviceClass = hwAuditDeviceClasses.get(hwClass.getKey());

			// case hostAssignedTableType
			String configKey = hwAuditDeviceClass.getHostConfigKey();
			String configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HOST_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HOST configIdent " + configIdent);

			Map<String, Boolean> tableConfigUpdates = updateItems.get(configIdent.toUpperCase());

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the host configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {
				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHostProperties(), tableConfigUpdates);

				readyObjects.add(AbstractExecutioner.jsonMap(configItem));

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well

				// now, we have got them in a view model

				Logging.info(this, "saveHwColumnConfig, locally saving " // + configOption
						+ " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, old configOption for key" + " " + hwAuditDeviceClass.getHostConfigKey()
								+ " " + configOptions.get(hwAuditDeviceClass.getHostConfigKey()) + " "
								+ configOptions.get(hwAuditDeviceClass.getHostConfigKey()).getClass());

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);
			}

			// case hwItemAssignedTableType
			configKey = hwAuditDeviceClass.getHwItemConfigKey();
			configIdent = hwClass.getKey() + "_" + OpsiHwAuditDeviceClass.HW_ITEM_ASSIGNED_TABLE_TYPE;

			Logging.debug(this, " saveHwColumnConfig for HW configIdent " + configIdent);

			tableConfigUpdates = updateItems.get(configIdent.toUpperCase());

			if (tableConfigUpdates != null) {
				Logging.info(this,
						" saveHwColumnConfig tableConfigUpdates  for the hw configIdent,  " + tableConfigUpdates);
			}

			// we have got updates for this table configuration
			if (tableConfigUpdates != null) {

				Map<String, Object> configItem = produceHwAuditColumnConfig(configKey,
						hwAuditDeviceClass.getDeviceHwItemProperties(), tableConfigUpdates);

				readyObjects.add(AbstractExecutioner.jsonMap(configItem));

				Logging.info(this, " saveHwColumnConfig, added configItem " + configItem);

				// save the data locally, we hope that the upload later will work as well
				// now, we have got them in a view model

				Logging.info(this, "saveHwColumnConfig, produce a ConfigOption from configItem " + configItem);

				Logging.info(this, "saveHwColumnConfig, locally saving " // + configOption
						+ " key " + hwAuditDeviceClass.getHwItemConfigKey());

				Logging.info(this,
						"saveHwColumnConfig, we had configOption for key" + " "
								+ hwAuditDeviceClass.getHwItemConfigKey() + " "
								+ configOptions.get(hwAuditDeviceClass.getHwItemConfigKey()) + " "
								+ configOptions.get(hwAuditDeviceClass.getHwItemConfigKey()).getClass());

				ConfigOption configOption = new ConfigOption(configItem);

				configOptions.put(hwAuditDeviceClass.getHostConfigKey(), configOption);

			}
		}

		Logging.info(this, "saveHwColumnConfig readyObjects " + readyObjects.size());
		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		return exec.doCall(omc);

	}

	@Override
	public Map<String, String> getEmptyLogfiles() {
		logfiles = new HashMap<>();
		String[] logtypes = Globals.getLogTypes();

		for (int i = 0; i < logtypes.length; i++) {
			logfiles.put(logtypes[i], "");
		}

		return logfiles;
	}

	@Override
	public Map<String, String> getLogfiles(String clientId, String logtype) {
		String[] logtypes = Globals.getLogTypes();

		if (logfiles == null) {
			getEmptyLogfiles();
		}

		int i = Arrays.asList(Globals.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			Logging.error("illegal logtype: " + logtype);
			return logfiles;
		}

		Logging.debug(this, "------------- getLogfile logtye " + logtype);

		String s = "";
		try {
			Logging.debug(this, "OpsiMethodCall readLog " + logtypes[i] + " max size " + Globals.getMaxLogSize(i));

			try {
				if (Globals.getMaxLogSize(i) == 0) {
					s = exec.getStringResult(new OpsiMethodCall("readLog", new String[] { logtype, clientId }));
				} else {
					s = exec.getStringResult(new OpsiMethodCall("readLog",
							new String[] { logtype, clientId, String.valueOf(Globals.getMaxLogSize(i)) }));
				}

			} catch (OutOfMemoryError e) {
				s = "--- file too big for showing, enlarge java memory  ---";
			}
		} catch (Exception ex) {
			s = "not found, " + ex;
		}

		logfiles.put(logtype, s);

		return logfiles;
	}

	@Override
	public Map<String, String> getLogfiles(String clientId) {
		logfiles = new HashMap<>();

		String[] logtypes = Globals.getLogTypes();

		for (int i = 0; i < logtypes.length; i++) {
			getLogfiles(clientId, logtypes[i]);
		}

		return logfiles;
	}

	@Override
	public void depotChange() {
		Logging.info(this, "depotChange");
		productGlobalInfos = null;
		possibleActions = null;
		productIds = null;
		netbootProductNames = null;
		localbootProductNames = null;
		retrieveProducts();
		retrieveProductPropertyDefinitions();
		getProductGlobalInfos(theDepot);

	}

	@Override
	public void productDataRequestRefresh() {
		dataStub.productDataRequestRefresh();
		productpropertiesRequestRefresh();
		depot2product2properties = null;
		productGroups = null;
		depotChange();
	}

	@Override
	public List<String> getAllProductNames(String depotId) {
		OpsiMethodCall cmd = new OpsiMethodCall("getProductIds_list", new String[] { "", depotId, "" });

		return exec.getStringListResult(cmd);
	}

	@Override
	public List<String> getProvidedLocalbootProducts(String depotId) {
		OpsiMethodCall cmd = new OpsiMethodCall("getProvidedLocalBootProductIds_list", new String[] { depotId });

		return exec.getStringListResult(cmd);
	}

	@Override
	public List<String> getProvidedNetbootProducts(String depotId) {
		OpsiMethodCall cmd = new OpsiMethodCall("getProvidedNetBootProductIds_list", new String[] { depotId });

		return exec.getStringListResult(cmd);
	}

	@Override
	public List<String> getAllLocalbootProductNames(String depotId) {
		Logging.debug(this, "getAllLocalbootProductNames for depot " + depotId);
		Logging.info(this, "getAllLocalbootProductNames, producing " + (localbootProductNames == null));
		if (localbootProductNames == null) {
			// opsi 4.0

			Map productOrderingResult = exec
					.getMapOfLists(new OpsiMethodCall("getProductOrdering", new String[] { depotId }));

			List<String> sortedProducts = (List<String>) productOrderingResult.get("sorted");
			if (sortedProducts == null) {
				sortedProducts = new ArrayList<>();
			}

			List<String> notSortedProducts = (List<String>) productOrderingResult.get("not_sorted");
			if (notSortedProducts == null) {
				notSortedProducts = new ArrayList<>();
			}

			Logging.info(this, "not ordered " + (notSortedProducts.size() - sortedProducts.size()) + "");

			notSortedProducts.removeAll(sortedProducts);
			Logging.info(this, "missing: " + notSortedProducts);

			localbootProductNames = sortedProducts;
			localbootProductNames.addAll(notSortedProducts);

			// we don't have a productsgroupsFullPermission)
			if (permittedProducts != null) {
				localbootProductNames.retainAll(permittedProducts);
			}
		}

		Logging.info(this, "localbootProductNames sorted, size " + localbootProductNames.size());

		return localbootProductNames;
	}

	@Override
	public List<String> getAllLocalbootProductNames() {
		return getAllLocalbootProductNames(theDepot);
	}

	@Override
	public void retrieveProducts() {
		retrieveDepotProducts(theDepot);
	}

	protected void retrieveDepotProducts(String depotId) {
		Logging.debug(this, "retrieveDepotProducts for " + depotId);

		if (dataStub.getDepot2NetbootProducts().get(depotId) != null) {
			netbootProductNames = new ArrayList<>(dataStub.getDepot2NetbootProducts().get(depotId).keySet());
		} else {
			netbootProductNames = new ArrayList<>();
		}

		// we don't have a productsgroupsFullPermission)
		if (permittedProducts != null) {
			netbootProductNames.retainAll(permittedProducts);
		}

		// for localboot products, we have to look for ordering information
		localbootProductNames = getAllLocalbootProductNames(depotId);
	}

	@Override
	public List<String> getAllDepotsWithIdenticalProductStock(String depot) {
		List<String> result = new ArrayList<>();

		TreeSet<OpsiPackage> first = dataStub.getDepot2Packages().get(depot);
		Logging.info(this, "getAllDepotsWithIdenticalProductStock " + first);

		for (String testdepot : getHostInfoCollections().getAllDepots().keySet()) {
			if (depot.equals(testdepot) || (first == null && dataStub.getDepot2Packages().get(testdepot) == null)
					|| (first != null && first.equals(dataStub.getDepot2Packages().get(testdepot)))) {
				result.add(testdepot);
			}
		}
		Logging.info(this, "getAllDepotsWithIdenticalProductStock  result " + result);

		return result;
	}

	@Override
	public List<String> getAllNetbootProductNames(String depotId) {
		if (netbootProductNames == null) {
			retrieveDepotProducts(depotId);
		}
		return netbootProductNames;
	}

	@Override
	public List<String> getAllNetbootProductNames() {
		return getAllNetbootProductNames(theDepot);
	}

	@Override
	public List<String> getWinProducts(String depotId, String depotProductDirectory) {
		List<String> winProducts = new ArrayList<>();
		if (depotProductDirectory == null) {
			return winProducts;
		}

		boolean smbMounted = new File(depotProductDirectory).exists();

		for (String product : new TreeSet<>(getAllNetbootProductNames(depotId))) {
			if (!smbMounted // probably not on Windows, take every product to correct path manually
					|| new File(
							depotProductDirectory + File.separator + product + File.separator + SmbConnect.DIRECTORY_PE)
									.exists() // win 6.x
					|| new File(depotProductDirectory + File.separator + product + File.separator
							+ SmbConnect.DIRECTORY_I368).exists() // XP
			) {
				winProducts.add(product);
			}
		}

		return winProducts;
	}

	@Override
	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		return dataStub.getProduct2versionInfo2infos();
	}

	@Override
	public Object2Product2VersionList getDepot2LocalbootProducts() {
		return dataStub.getDepot2LocalbootProducts();
	}

	@Override
	public Object2Product2VersionList getDepot2NetbootProducts() {
		return dataStub.getDepot2NetbootProducts();
	}

	private void retrieveProductGlobalInfos(String depotId) {

		Logging.info(this, "retrieveProductGlobalInfos , depot " + depotId);

		productGlobalInfos = new HashMap<>();
		possibleActions = new HashMap<>();

		for (String productId : dataStub.getProduct2versionInfo2infos().keySet()) {
			if (dataStub.getProduct2versionInfo2infos().get(productId) == null) {
				Logging.warning(this, "retrieveProductGlobalInfos productId == null for product " + productId);
			}

			if (dataStub.getProduct2versionInfo2infos().get(productId) != null) {
				String versionInfo = null;
				Map<String, OpsiProductInfo> productAllInfos = dataStub.getProduct2versionInfo2infos().get(productId);

				// look for associated product on depot info
				Product2VersionList product2VersionList = dataStub.getDepot2LocalbootProducts().get(depotId);
				if (product2VersionList != null && product2VersionList.get(productId) != null
						&& !product2VersionList.get(productId).isEmpty()) {
					versionInfo = product2VersionList.get(productId).get(0);
				}

				if (versionInfo == null) {
					product2VersionList = dataStub.getDepot2NetbootProducts().get(depotId);

					if (product2VersionList != null && product2VersionList.get(productId) != null
							&& !product2VersionList.get(productId).isEmpty()) {
						versionInfo = product2VersionList.get(productId).get(0);
					}
				}

				// if found go on

				if (versionInfo != null && productAllInfos.get(versionInfo) != null) {
					OpsiProductInfo productInfo = productAllInfos.get(versionInfo);

					possibleActions.put(productId, productInfo.getPossibleActions());

					Map<String, Object> aProductInfo = new HashMap<>();

					aProductInfo.put("actions", productInfo.getPossibleActions());

					aProductInfo.put(ProductState.KEY_PRODUCT_ID, productId
					// productInfo.getProductId()
					);
					aProductInfo.put(ProductState.KEY_VERSION_INFO,
							Globals.ProductPackageVersionSeparator.formatKeyForDisplay(productInfo.getVersionInfo()));

					aProductInfo.put(ProductState.KEY_PRODUCT_PRIORITY, productInfo.getPriority());

					aProductInfo.put(ProductState.KEY_PRODUCT_NAME,
							// OpsiProductInfo.SERVICEkeyPRODUCT_NAME,
							productInfo.getProductName());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION, productInfo.getDescription());

					aProductInfo.put(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE, productInfo.getAdvice());

					aProductInfo.put(ProductState.KEY_PRODUCT_VERSION, productInfo.getProductVersion());

					aProductInfo.put(ProductState.KEY_PACKAGE_VERSION, productInfo.getPackageVersion());

					aProductInfo.put(OpsiPackage.SERVICE_KEY_LOCKED, productInfo.getLockedInfo());

					Logging.debug(this, "productInfo " + aProductInfo);

					productGlobalInfos.put(productId, aProductInfo);
				}
			}
		}

		Logging.info(this, "retrieveProductGlobalInfos  found number  " + productGlobalInfos.size());
	}

	private void checkProductGlobalInfos(String depotId) {
		Logging.info(this, "checkProductGlobalInfos for Depot " + depotId);
		if (!theDepot.equals(depotId)) {
			Logging.warning(this, "depot irregular, preset " + theDepot);
		}
		if (depotId == null || depotId.equals("")) {
			Logging.notice(this, "checkProductGlobalInfos called for no depot");
		}
		Logging.debug(this, "checkProductGlobalInfos depotId " + depotId + " productGlobaInfos  = null "
				+ (productGlobalInfos == null) + " possibleActions = null " + (possibleActions == null));
		if (possibleActions == null || productGlobalInfos == null || theDepot == null || !theDepot.equals(depotId)) {
			retrieveProductGlobalInfos(depotId);
		}
	}

	// map with key productId
	@Override
	public Map<String, List<String>> getPossibleActions(String depotId) {
		Logging.debug(this, "getPossibleActions depot irregular " + !theDepot.equals(depotId));
		checkProductGlobalInfos(depotId);
		return possibleActions;
	}

	@Override
	public Map<String, List<Map<String, String>>> getMapOfProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfProductStatesAndActions for : " + Logging.getStrings(clientIds));

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		if (clientIds == null || clientIds.length == 0) {
			return result;
		}

		Map<String, List<Map<String, String>>> states = getProductStatesNOM(clientIds);

		if (states != null) {
			return states;
		}

		return result;
	}

	private Map<String, List<Map<String, String>>> getLocalBootProductStates(String[] clientIds) {
		return getLocalBootProductStatesNOM(clientIds);
	}

	protected Map<String, List<Map<String, String>>> getProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", AbstractExecutioner.jsonArray(Arrays.asList(clientIds)));

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {
			String client = (String) m.get("clientId");

			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(JSONReMapper.giveEmptyForNull(m), true));
		}
		return result;
	}

	protected Map<String, List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", AbstractExecutioner.jsonArray(Arrays.asList(clientIds)));
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get(ProductOnClient.CLIENT_ID);
			List<Map<String, String>> states1Client = result.computeIfAbsent(client, arg -> new ArrayList<>());

			Map<String, String> aState = new ProductState(JSONReMapper.giveEmptyForNull(m), true);
			states1Client.add(aState);

		}

		return result;
	}

	@Override
	public Map<String, List<Map<String, String>>> getMapOfLocalbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + Logging.getStrings(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return null;
		}

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		Map<String, List<Map<String, String>>> states = null;

		states = getLocalBootProductStates(clientIds);

		if (states != null) {
			return states;
		}

		return result;
	}
	// test ende*/

	protected Map<String, List<Map<String, String>>> getNetBootProductStatesNOM(String[] clientIds) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId", AbstractExecutioner.jsonArray(Arrays.asList(clientIds)));
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);

		List<Map<String, Object>> productOnClients = exec.getListOfMaps(
				new OpsiMethodCall("productOnClient_getHashes", new Object[] { callAttributes, callFilter }));

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		for (Map<String, Object> m : productOnClients) {

			String client = (String) m.get("clientId");
			result.computeIfAbsent(client, arg -> new ArrayList<>())
					.add(new ProductState(JSONReMapper.giveEmptyForNull(m), true));

		}
		return result;
	}

	@Override
	public Map<String, List<Map<String, String>>> getMapOfNetbootProductStatesAndActions(String[] clientIds) {
		Logging.debug(this, "getMapOfNetbootProductStatesAndActions for : " + Logging.getStrings(clientIds));

		if (clientIds == null || clientIds.length == 0) {
			return null;
		}

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		Map<String, List<Map<String, String>>> states = getNetBootProductStatesNOM(clientIds);

		if (states != null) {
			return states;
		}
		return result;
	}

	protected boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues,
			List updateItems) {
		Map<String, Object> values = new HashMap<>();

		values.put("productType", OpsiPackage.giveProductType(producttype));
		values.put("type", "ProductOnClient");
		values.put("clientId", pcname);
		values.put("productId", productname);
		values.putAll(updateValues);

		Logging.debug(this, "updateProductOnClient, values " + values);
		updateItems.add(AbstractExecutioner.jsonMap(values));

		return true;
	}

	@Override
	public boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues) {
		if (updateProductOnClientItems == null) {
			updateProductOnClientItems = new ArrayList<>();
		}

		return updateProductOnClient(pcname, productname, producttype, updateValues, updateProductOnClientItems);
	}

	// hopefully we get only updateItems for allowed clients
	public boolean updateProductOnClients(List updateItems) {
		Logging.info(this, "updateProductOnClients ");

		if (globalReadOnly) {
			return false;
		}

		boolean result = false;

		if (updateItems != null && !updateItems.isEmpty()) {
			Logging.info(this, "updateProductOnClients  updateItems.size " + updateItems.size());

			OpsiMethodCall omc = new OpsiMethodCall("productOnClient_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(updateItems) });

			result = exec.doCall(omc);

			// at any rate
			updateItems.clear();
		}

		return result;
	}

	@Override
	public boolean updateProductOnClients() {
		return updateProductOnClients(updateProductOnClientItems);
	}

	@Override
	public boolean updateProductOnClients(Set<String> clients, String productName, int productType,
			Map<String, String> changedValues) {
		List updateCollection = new ArrayList<>();

		boolean result = true;

		// collect updates for all clients
		for (String client : clients) {
			result = result && updateProductOnClient(client, productName, productType, changedValues, updateCollection);
		}

		// execute
		return result && updateProductOnClients(updateCollection);
	}

	@Override
	public boolean resetLocalbootProducts(String[] selectedClients, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		List<Map<String, Object>> deleteProductItems = new ArrayList<>();

		for (int i = 0; i < selectedClients.length; i++) {
			for (String product : localbootProductNames) {
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
				productOnClientItem.put("clientId", selectedClients[i]);
				productOnClientItem.put("productId", product);

				deleteProductItems.add(productOnClientItem);
			}
		}

		Logging.info(this, "resetLocalbootProducts deleteProductItems.size " + deleteProductItems.size());

		result = resetProducts(deleteProductItems, withDependencies);

		Logging.debug(this, "resetLocalbootProducts result " + result);

		return result;
	}

	@Override
	public boolean resetNetbootProducts(String[] selectedClients, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		List<Map<String, Object>> deleteProductItems = new ArrayList<>();

		for (int i = 0; i < selectedClients.length; i++) {
			for (String product : netbootProductNames) {
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
				productOnClientItem.put("clientId", selectedClients[i]);
				productOnClientItem.put("productId", product);

				deleteProductItems.add(productOnClientItem);
			}
		}

		Logging.info(this, "resetNetbootProducts deleteProductItems.size " + deleteProductItems.size());

		result = resetProducts(deleteProductItems, withDependencies);

		Logging.debug(this, "resetNetbootProducts result " + result);

		return result;
	}

	@Override
	public boolean resetProducts(List<Map<String, Object>> productItems, boolean withDependencies) {
		if (globalReadOnly) {
			return false;
		}

		boolean result = true;

		Logging.info(this, "resetProducts productItems.size " + productItems.size());

		if (!productItems.isEmpty()) {
			OpsiMethodCall omc = new OpsiMethodCall("productOnClient_deleteObjects",
					new Object[] { productItems.toArray() });

			result = exec.doCall(omc);

			Logging.debug(this, "resetProducts result " + result);

			if (result && withDependencies) {
				omc = new OpsiMethodCall("productPropertyState_delete",
						new Object[] { productItems.stream().map(p -> p.get("productId")).toArray(), "*",
								productItems.stream().map(p -> p.get("clientId")).toArray() });

				result = exec.doCall(omc);
			}
		}

		Logging.debug(this, "resetProducts result " + result);

		return result;
	}

	@Override
	public void retrieveProductDependencies() {
		dataStub.getDepot2product2dependencyInfos();
	}

	@Override
	public Map<String, Map<String, Object>> getProductGlobalInfos(String depotId) {
		checkProductGlobalInfos(depotId);
		return productGlobalInfos;
	}

	public Map<String, Object> getProductInfos(String productname) {
		checkProductGlobalInfos(theDepot);
		return productGlobalInfos.get(productname);

	}

	@Override
	public Map<String, Map<String, String>> getProductDefaultStates() {
		if (productIds == null) {
			getProductIds();
		}

		Logging.debug(this, "getProductDefaultStates, count " + productDefaultStates.size());
		return productDefaultStates;
	}

	@Override
	public List<List<Object>> getProductRows() {
		return dataStub.getProductRows();
	}

	@Override
	public Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots() {
		return dataStub.getProduct2VersionInfo2Depots();
	}

	@Override
	public NavigableSet<String> getProductIds() {
		dataStub.getProduct2versionInfo2infos();

		if (productIds == null) {
			productIds = new TreeSet<>();
			productDefaultStates = new TreeMap<>();

			for (String productId : dataStub.getProduct2versionInfo2infos().keySet()) {
				productIds.add(productId);
				ProductState productDefault = new ProductState(null);
				productDefault.put("productId", productId);
				productDefaultStates.put(productId, productDefault);
			}

			Logging.info(this, "getProductIds size / names " + productIds.size() + " / ... ");
		}

		return productIds;
	}

	private List<Map<String, String>> getProductDependencies(String depotId, String productId) {
		List<Map<String, String>> result = null;

		if (dataStub.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = dataStub.getDepot2product2dependencyInfos().get(depotId).get(productId);
		}

		if (result == null) {
			result = new ArrayList<>();
		}

		Logging.debug(this,
				"getProductDependencies for depot, product " + depotId + ", " + productId + " , result " + result);
		return result;
	}

	@Override
	public Map<String, List<Map<String, String>>> getProductDependencies(String depotId) {
		Map<String, List<Map<String, String>>> result = null;

		if (dataStub.getDepot2product2dependencyInfos().get(depotId) != null) {
			result = dataStub.getDepot2product2dependencyInfos().get(depotId);
		} else {
			result = new HashMap<>();
		}

		return result;
	}

	@Override
	public Set<String> extendToDependentProducts(final Set<String> startProductSet, final String depot) {
		HashSet<String> notHandled = new HashSet<>(startProductSet);
		HashSet<String> endResultSet = new HashSet<>(startProductSet);
		HashSet<String> startResultSet = null;

		while (!notHandled.isEmpty()) {
			startResultSet = new HashSet<>(endResultSet);

			for (String prod : notHandled) {
				Logging.info(this, " extendToDependentProducts prod " + prod);
				for (Map<String, String> m : getProductDependencies(depot, prod)) {
					Logging.info(this, " extendToDependentProducts m " + m.get("requiredProductId"));
					endResultSet.add(m.get("requiredProductId"));
				}
			}

			notHandled = new HashSet<>(endResultSet);
			notHandled.removeAll(startResultSet);
		}

		return new TreeSet<>(endResultSet);
	}

	/**
	 * returns a set which depends on the momentarily selected hosts as
	 * specified by a call to retrieveProductProperties
	 */
	protected Set<String> getProductsHavingSpecificProperties() {
		return productsHavingSpecificProperties;
	}

	@Override
	public Boolean hasClientSpecificProperties(String productname) {
		return productHavingClientSpecificProperties.get(productname);
	}

	@Override
	public Map<String, Boolean> getProductHavingClientSpecificProperties() {
		return productHavingClientSpecificProperties;
	}

	List<Map<String, Object>> retrieveListOfMapsNOM(String methodName) {
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		return retrieveListOfMapsNOM(callAttributes, callFilter, methodName);
	}

	List<Map<String, Object>> retrieveListOfMapsNOM(String[] callAttributes, Map callFilter, String methodName) {
		List<Map<String, Object>> retrieved = exec
				.getListOfMaps(new OpsiMethodCall(methodName, new Object[] { callAttributes, callFilter }));
		Logging.debug(this, "retrieveListOfMapsNOM " + retrieved);
		return retrieved;
	}

	List<Map<String, Object>> retrieveListOfMapsNOM(String methodName, Object[] data) {
		List<Map<String, Object>> retrieved = exec.getListOfMaps(new OpsiMethodCall(methodName, data));
		Logging.debug(this, "retrieveListOfMapsNOM " + retrieved);
		return retrieved;
	}

	/**
	 * Collects the common property values of some product for a client
	 * collection,<br \> needed for local imaging handling <br \>
	 * 
	 * @param List<String> clients -
	 * @param String       product
	 * @param String       property
	 */
	@Override
	public List<String> getCommonProductPropertyValues(List<String> clients, String product, String property) {
		Logging.info(this, "getCommonProductPropertyValues for product, property, clients " + product + ", " + property
				+ "  -- " + clients);
		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();
		callFilter.put("objectId", AbstractExecutioner.jsonArray(clients));
		callFilter.put("productId", product);
		callFilter.put("propertyId", property);
		List<Map<String, Object>> properties = retrieveListOfMapsNOM(callAttributes, callFilter,
				"productPropertyState_getObjects");

		Set<String> resultSet = new HashSet<>();

		boolean starting = true;

		for (Map<String, Object> map : properties) {
			Object retrievedValues = ((JSONArray) map.get("values")).toList();

			List<Object> valueList = (List<Object>) retrievedValues;

			Set<String> values = new HashSet<>();

			for (int i = 0; i < valueList.size(); i++) {
				values.add((String) valueList.get(i));
			}

			if (starting) {
				resultSet = values;
				starting = false;
			} else {
				resultSet.retainAll(values);
			}
		}

		Logging.info(this, "getCommonProductPropertyValues " + resultSet);

		return new ArrayList<>(resultSet);
	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 * 
	 * @param clientNames -
	 */
	@Override
	public void retrieveProductProperties(List<String> clientNames) {
		retrieveProductProperties(new HashSet<>(clientNames));
	}

	@Override
	public Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties() {
		retrieveDepotProductProperties();
		return depot2product2properties;
	}

	@Override
	public Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId) {
		Logging.debug(this, "getDefaultProductProperties for depot " + depotId);
		retrieveDepotProductProperties();
		if (depot2product2properties == null) {
			Logging.error("no product properties ");
			return new HashMap<>();
		} else {
			if (depot2product2properties.get(depotId) == null) {
				// initializing state
				return new HashMap<>();
			}

			if (!depot2product2properties.get(depotId).isEmpty()) {
				Logging.info(this, "getDefaultProductProperties for depotId " + depotId + " starts with "
						+ new ArrayList<>(depot2product2properties.get(depotId).keySet()).get(0));
			}

			return depot2product2properties.get(depotId);
		}
	}

	@Override
	public void retrieveDepotProductProperties() {
		if (depot2product2properties != null) {
			return;
		}

		Logging.info(this, "retrieveDepotProductProperties, build depot2product2properties");

		depot2product2properties = new HashMap<>();

		// depot missing ??

		List<Map<String, Object>> retrieved = dataStub
				.getProductPropertyDepotStates(hostInfoCollections.getDepots().keySet());

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get(ProductPropertyState.OBJECT_ID);

			if (!hostInfoCollections.getDepots().keySet().contains(host)) {
				Logging.warning(this, "should be a productPropertyState for a depot, but host " + host);
				continue;
			}

			Map<String, ConfigName2ConfigValue> productproperties1Host = depot2product2properties.computeIfAbsent(host,
					arg -> new HashMap<>());

			ConfigName2ConfigValue properties = productproperties1Host.computeIfAbsent(
					(String) map.get(ProductPropertyState.PRODUCT_ID),
					arg -> new ConfigName2ConfigValue(new HashMap<>()));

			properties.put((String) map.get(ProductPropertyState.PROPERTY_ID),
					((JSONArray) map.get(ProductPropertyState.VALUES)).toList());
			properties.getRetrieved().put((String) map.get(ProductPropertyState.PROPERTY_ID),
					((JSONArray) map.get(ProductPropertyState.VALUES)).toList());

			Logging.debug(this,
					"retrieveDepotProductProperties product properties " + map.get(ProductPropertyState.PRODUCT_ID));
		}

	}

	/**
	 * This method collects properties for all selected clients and all
	 * products,<br \> as a sideeffect, it produces the depot specific default
	 * values <br \>
	 * 
	 * @param clientNames -
	 */
	public void retrieveProductProperties(final Set<String> clientNames) {

		boolean existing = true;

		if (productProperties == null) {
			existing = false;
		} else {
			for (String client : clientNames) {
				if (productProperties.get(client) == null) {
					existing = false;
					break;
				}
			}
		}

		if (existing) {
			return;
		}

		productProperties = new HashMap<>();
		Map<String, Map<String, Map<String, Object>>> productPropertiesRetrieved = new HashMap<>();

		dataStub.fillProductPropertyStates(clientNames);
		List<Map<String, Object>> retrieved = dataStub.getProductPropertyStates();

		Set<String> productsWithProductPropertyStates = new HashSet<>();

		for (Map<String, Object> map : retrieved) {
			String host = (String) map.get("objectId");

			productsWithProductPropertyStates.add((String) map.get("productId"));

			Map<String, Map<String, Object>> productproperties1Client = productPropertiesRetrieved.get(host);

			if (productproperties1Client == null) {
				productproperties1Client = new HashMap<>();
				productPropertiesRetrieved.put(host, productproperties1Client);
			}

			Map<String, Object> properties = productproperties1Client.get(map.get("productId"));
			if (properties == null) {
				properties = new HashMap<>();
				productproperties1Client.put((String) map.get("productId"), properties);
			}

			properties.put((String) map.get("propertyId"), ((JSONArray) map.get("values")).toList());
		}

		Logging.info(this,
				" retrieveProductproperties  productsWithProductPropertyStates " + productsWithProductPropertyStates);

		Map<String, ConfigName2ConfigValue> defaultProperties = getDefaultProductProperties(theDepot);
		Map<String, Map<String, Object>> defaultPropertiesRetrieved = new HashMap<>();
		if (!defaultProperties.isEmpty()) {
			for (Entry<String, ConfigName2ConfigValue> defaultProperty : defaultProperties.entrySet()) {
				defaultPropertiesRetrieved.put(defaultProperty.getKey(), defaultProperty.getValue());
			}
		}

		Set<String> products = defaultPropertiesRetrieved.keySet();

		productsHavingSpecificProperties = new TreeSet<>(products);

		for (String host : clientNames) {
			HashMap<String, ConfigName2ConfigValue> productproperties1Client = new HashMap<>();
			productProperties.put(host, productproperties1Client);

			Map<String, Map<String, Object>> retrievedProperties = productPropertiesRetrieved.get(host);
			if (retrievedProperties == null) {
				retrievedProperties = defaultPropertiesRetrieved;
				productsHavingSpecificProperties.clear();
			}

			for (String product : products) {
				Map<String, Object> retrievedProperties1Product = retrievedProperties.get(product);
				// complete set of default values
				Map<String, Object> properties1Product = new HashMap<>(defaultPropertiesRetrieved.get(product));

				if (retrievedProperties1Product == null) {
					productsHavingSpecificProperties.remove(product);
				} else {
					for (Entry<String, Object> retrievedProperty : retrievedProperties1Product.entrySet()) {
						properties1Product.put(retrievedProperty.getKey(), retrievedProperty.getValue());
					}
				}

				ConfigName2ConfigValue state = new ConfigName2ConfigValue(properties1Product, null);
				productproperties1Client.put(product, state);
			}
		}

		Logging.info(this,
				" retrieveProductproperties productsHavingSpecificProperties " + productsHavingSpecificProperties);

		Map<String, ConfigName2ConfigValue> depotValues = getDefaultProductProperties(theDepot);

		productHavingClientSpecificProperties = new HashMap<>();

		for (String product : products) {
			if (productPropertyDefinitions != null && productPropertyDefinitions.get(product) != null) {
				ConfigName2ConfigValue productPropertyConfig = depotValues.get(product);

				Iterator<String> iterProperties = productPropertyDefinitions.get(product).keySet().iterator();
				while (iterProperties.hasNext()) {
					String property = iterProperties.next();

					if (productPropertyConfig.isEmpty() || productPropertyConfig.get(property) == null) {
						productPropertyDefinitions.get(product).get(property).setDefaultValues(new ArrayList<>());
					} else {
						productPropertyDefinitions.get(product).get(property)
								.setDefaultValues((List) productPropertyConfig.get(property));
					}
				}
			}

			productHavingClientSpecificProperties.put(product, productsHavingSpecificProperties.contains(product));
		}
	}

	/**
	 * @param pcname      - if it changes productproperties should have been set
	 *                    to null.
	 * @param productname
	 */
	@Override
	public Map<String, Object> getProductProperties(String pcname, String productname) {
		Logging.debug(this, "getProductProperties for product, host " + productname + ", " + pcname);

		Set<String> pcs = new TreeSet<>();
		pcs.add(pcname);
		retrieveProductProperties(pcs);

		if (productProperties.get(pcname) == null || productProperties.get(pcname).get(productname) == null) {
			return new HashMap<>();
		}

		return productProperties.get(pcname).get(productname);
	}

	// collect productPropertyState updates and deletions
	public void setProductProperties(String pcname, String productname, Map properties, List updateCollection,
			List deleteCollection) {
		if (!(properties instanceof ConfigName2ConfigValue)) {
			Logging.warning(this, "! properties instanceof ConfigName2ConfigValue ");
			return;
		}

		Iterator propertiesKeyIterator = properties.keySet().iterator();

		Map<String, Object> state = new HashMap<>();

		while (propertiesKeyIterator.hasNext()) {
			String key = (String) propertiesKeyIterator.next();

			state.put("type", "ProductPropertyState");
			state.put("objectId", pcname);
			state.put("productId", productname);
			state.put("propertyId", key);

			List newValue = (List) properties.get(key);

			Map<String, Object> retrievedConfig = ((RetrievedMap) properties).getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null) {
				oldValue = retrievedConfig.get(key);
			}

			if (newValue != oldValue) {
				if (newValue == MapTableModel.nullLIST) {
					Logging.debug(this, "setProductProperties,  requested deletion " + properties.get(key));
					deleteCollection.add(AbstractExecutioner.jsonMap(state));

					// we hope that the update works and directly update the retrievedConfig
					if (retrievedConfig != null) {
						retrievedConfig.remove(key);
					}
				} else {
					state.put("values", AbstractExecutioner.jsonArray(newValue));
					Logging.debug(this, "setProductProperties,  requested update " + properties.get(key)
							+ " for oldValue " + oldValue);
					Logging.debug(this, "setProductProperties,  we have new state " + state);
					updateCollection.add(AbstractExecutioner.jsonMap(state));

					// we hope that the update works and directly update the retrievedConfig
					if (retrievedConfig != null) {
						retrievedConfig.put(key, properties.get(key));
					}
				}
			}
		}
	}

	// collect productPropertyState updates and deletions in standard lists
	@Override
	public void setProductProperties(String pcname, String productname, Map properties) {
		// old version

		if (productPropertyStateUpdateCollection == null) {
			productPropertyStateUpdateCollection = new ArrayList<>();
		}

		if (productPropertyStateDeleteCollection == null) {
			productPropertyStateDeleteCollection = new ArrayList<>();
		}

		setProductProperties(pcname, productname, properties, productPropertyStateUpdateCollection,
				productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections for standard
	// collections
	@Override
	public void setProductProperties() {
		setProductProperties(productPropertyStateUpdateCollection, productPropertyStateDeleteCollection);
	}

	// send productPropertyState updates and clear the collections
	public void setProductProperties(List updateCollection, List deleteCollection) {
		Logging.debug(this, "setProductproperties() ");

		if (globalReadOnly) {
			return;
		}

		if (updateCollection != null && !updateCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall("productPropertyState_updateObjects",
						new Object[] { AbstractExecutioner.jsonArray(updateCollection) }))) {
			updateCollection.clear();
		}

		if (deleteCollection != null && !deleteCollection.isEmpty()
				&& exec.doCall(new OpsiMethodCall("productPropertyState_deleteObjects",
						new Object[] { AbstractExecutioner.jsonArray(deleteCollection) }))) {
			deleteCollection.clear();
		}
	}

	@Override
	public void setCommonProductPropertyValue(Set<String> clientNames, String productName, String propertyName,
			List<String> values) {
		List updateCollection = new ArrayList<>();
		List deleteCollection = new ArrayList<>();

		// collect updates for all clients
		for (String client : clientNames) {
			Map<String, Object> newdata = new ConfigName2ConfigValue(null);

			newdata.put(propertyName, values);

			// collect the updates
			setProductProperties(client, productName, newdata, updateCollection, deleteCollection);
		}
		// execute updates
		setProductProperties(updateCollection, deleteCollection);
	}

	@Override
	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String depotId, String productId) {
		Map<String, ListCellOptions> result = null;

		if (dataStub.getDepot2Product2PropertyDefinitions().get(depotId) == null) {
			result = new HashMap<>();
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions for depot " + depotId);
		} else {
			result = dataStub.getDepot2Product2PropertyDefinitions().get(depotId).get(productId);
		}

		if (result == null) {
			Logging.info("getProductPropertyOptionsMap: no productproperty definitions  for depot, product " + depotId
					+ ", " + productId);
			result = new HashMap<>();
		}

		return result;
	}

	@Override
	public Map<String, ListCellOptions> getProductPropertyOptionsMap(String productId) {
		retrieveProductPropertyDefinitions();
		Map<String, ListCellOptions> result;

		if (productPropertyDefinitions == null) {
			result = new HashMap<>();
		} else {
			result = productPropertyDefinitions.get(productId);
			if (result == null) {
				result = new HashMap<>();
			}
		}

		return result;
	}

	@Override
	public void productPropertyDefinitionsRequestRefresh() {
		dataStub.productPropertyDefinitionsRequestRefresh();
		productPropertyDefinitions = null;
	}

	@Override
	public void retrieveProductPropertyDefinitions() {
		productPropertyDefinitions = dataStub.getDepot2Product2PropertyDefinitions().get(theDepot);
	}

	@Override
	public String getProductTitle(String product) {
		Logging.info(this, "getProductTitle for product " + product + " on depot " + theDepot);
		Logging.info(this, "getProductTitle for productGlobalsInfos found number " + productGlobalInfos.size());
		Logging.info(this, "getProductTitle, productInfos " + productGlobalInfos.get(product));
		Object result = productGlobalInfos.get(product).get(ProductState.KEY_PRODUCT_NAME);
		Logging.info(this, "getProductTitle for product " + result);

		String resultS = null;
		if (result == null) {
			resultS = EMPTYFIELD;
		} else {
			resultS = "" + result;
		}
		return resultS;
	}

	@Override
	public String getProductInfo(String product) {
		String result = "" + productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_DESCRIPTION);
		Logging.debug(this, " getProductInfo for product " + product + ": " + result);

		return result;
	}

	@Override
	public String getProductHint(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiProductInfo.SERVICE_KEY_PRODUCT_ADVICE);
	}

	@Override
	public String getProductVersion(String product) {
		String result = (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);

		if (result == null) {
			result = EMPTYFIELD;
		}

		Logging.debug(this, "getProductVersion which? " + result + " for product: " + product);

		return result;
	}

	@Override
	public String getProductPackageVersion(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
	}

	@Override
	public String getProductLockedInfo(String product) {
		return (String) productGlobalInfos.get(product).get(OpsiPackage.SERVICE_KEY_LOCKED);
	}

	private Map<String, String> getProductRequirements(String depotId, String productname, String requirementType) {
		Map<String, String> result = new HashMap<>();

		String depot = null;
		if (depotId == null) {
			depot = theDepot;
		} else {
			depot = depotId;
		}

		Logging.debug(this,
				"getProductRequirements productname, requirementType  " + productname + ", " + requirementType);

		List<Map<String, String>> dependenciesFor1product = getProductDependencies(depot, productname);

		if (dependenciesFor1product == null) {
			return result;
		}

		for (Map<String, String> aDependency : dependenciesFor1product) {
			Logging.debug(this, " dependency map : " + aDependency);

			if (requirementType.equals(NAME_REQUIREMENT_TYPE_ON_DEINSTALL)
					// we demand information for this type,
					// this is not specified by type in the dependency map
					// but only by the action value
					&& aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.UNINSTALL))) {
				result.put(aDependency.get("requiredProductId"),
						aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
			} else {
				Logging.debug(this, " dependency map : ");

				if ((requirementType.equals(NAME_REQUIREMENT_TYPE_NEUTRAL)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_BEFORE)
						|| requirementType.equals(NAME_REQUIREMENT_TYPE_AFTER))
						&& ((aDependency.get("action")).equals(ActionRequest.getLabel(ActionRequest.SETUP))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ONCE))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.ALWAYS))
								|| aDependency.get("action").equals(ActionRequest.getLabel(ActionRequest.CUSTOM)))
						&& aDependency.get("requirementType").equals(requirementType)) {
					result.put(aDependency.get("requiredProductId"),
							aDependency.get("requiredInstallationStatus") + ":" + aDependency.get("requiredAction"));
				}
			}
		}

		Logging.debug(this, "getProductRequirements depot, productname, requirementType  " + depotId + ", "
				+ productname + ", " + requirementType);
		Logging.info(this, "getProductRequirements " + result);

		return result;
	}

	@Override
	public Map<String, String> getProductPreRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_BEFORE);
	}

	@Override
	public Map<String, String> getProductRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_NEUTRAL);
	}

	@Override
	public Map<String, String> getProductPostRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_AFTER);
	}

	@Override
	public Map<String, String> getProductDeinstallRequirements(String depotId, String productname) {
		return getProductRequirements(depotId, productname, NAME_REQUIREMENT_TYPE_ON_DEINSTALL);
	}

	@Override
	public void productpropertiesRequestRefresh() {
		dataStub.productPropertyStatesRequestRefresh();
		productProperties = null;
	}

	// lazy initializing
	@Override
	public List<String> getMethodSignature(String methodname) {
		if (mapOfMethodSignatures == null) {
			List<Object> methodsList = exec
					.getListResult(new OpsiMethodCall("getPossibleMethods_listOfHashes", new Object[] {}));

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
			}
		}

		Logging.debug(this, "mapOfMethodSignatures " + mapOfMethodSignatures);

		if (mapOfMethodSignatures.get(methodname) == null) {
			return NONE_LIST;
		}

		return mapOfMethodSignatures.get(methodname);
	}

	@Override
	public String getBackendInfos() {
		String bgColor0 = "#dedeff";
		String bgColor1 = "#ffffff";
		String bgColor = "";

		String titleSize = "14px";
		String fontSizeBig = "10px";
		String fontSizeSmall = "8px";
		// are not evaluated at this moment

		OpsiMethodCall omc = new OpsiMethodCall("getBackendInfos_listOfHashes", new String[] {});

		List<Object> list = exec.getListResult(omc);

		StringBuilder buf = new StringBuilder("");

		Map<String, List<Map<String, Object>>> backends = new HashMap<>();

		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> listEntry = exec.getMapFromItem(list.get(i));

			String backendName = "UNKNOWN";

			if (listEntry.containsKey("name")) {
				backendName = (String) listEntry.get("name");
			}

			if (!backends.containsKey(backendName)) {
				backends.put(backendName, new ArrayList<>());
			}

			backends.get(backendName).add(listEntry);
		}

		buf.append("<table border='0' cellspacing='0' cellpadding='0'>\n");

		Iterator<String> backendIterator = backends.keySet().iterator();
		while (backendIterator.hasNext()) {
			String backendName = backendIterator.next();

			buf.append("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append("<font size='" + titleSize + "'><b>" + backendName + "</b></font></td></tr>");

			List<Map<String, Object>> backendEntries = backends.get(backendName);

			for (int i = 0; i < backendEntries.size(); i++) {
				Map listEntry = (Map) backendEntries.get(i);

				Iterator eIt = listEntry.keySet().iterator();

				boolean entryIsEven = false;

				while (eIt.hasNext()) {
					String key = (String) eIt.next();
					if (key.equals("name")) {
						continue;
					}

					entryIsEven = !entryIsEven;
					if (entryIsEven) {
						bgColor = bgColor0;
					} else {
						bgColor = bgColor1;
					}

					Object value = listEntry.get(key);
					buf.append("<tr height='8px'>");
					buf.append("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
							+ fontSizeBig + "'>" + key + "</font></td>");

					if (key.equals("config")) {
						buf.append("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>&nbsp;</font></td>");
						buf.append("</tr>");

						Map<String, Object> configItems = exec.getMapFromItem(value);

						if (!configItems.isEmpty()) {
							Iterator<String> configItemsIterator = configItems.keySet().iterator();

							while (configItemsIterator.hasNext()) {
								String configKey = configItemsIterator.next();

								Object jO = configItems.get(configKey);

								String configVal = "";

								try {
									configVal = jO.toString();
								} catch (Exception jsonEx) {
									Logging.debug(this, jsonEx.toString());
								}
								buf.append("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configKey
										+ "</font></td>");
								buf.append("<td width='200px'  bgcolor='" + bgColor
										+ "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configVal
										+ "</font></td>");
								buf.append("</tr>");
							}
						}
					} else {
						buf.append("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='"
								+ fontSizeBig + "'>" + value + "</font></td>");
						buf.append("</tr>");
					}
				}
				buf.append("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
			}

			buf.append(
					"<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append("</table>\n");

		return buf.toString();
	}

	@Override
	public Map<String, ListCellOptions> getConfigOptions() {
		getHwAuditDeviceClasses();

		if (configListCellOptions == null || configOptions == null || configDefaultValues == null) {
			Logging.debug(this, "getConfigOptions() work");

			List<Object> deleteItems = new ArrayList<>();

			boolean tryIt = true;

			int tryOnceMoreCounter = 0;
			final int stopRepeatingAtThis = 1;

			while (tryIt) {
				tryIt = false;
				tryOnceMoreCounter++;

				configOptions = new HashMap<>();
				configListCellOptions = new HashMap<>();
				configDefaultValues = new HashMap<>();

				remoteControls = new RemoteControls();
				savedSearches = new SavedSearches();

				hwAuditDevicePropertyTypes = new OpsiHwAuditDevicePropertyTypes(hwAuditDeviceClasses);

				// metaConfig for wan configuration is rebuilt in
				// getWANConfigOptions

				List<Map<String, Object>> retrievedList = retrieveListOfMapsNOM("config_getObjects");

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

					String pseudouserProducedByOldVersion = KEY_USER_ROOT + ".{"
							+ UserConfig.ROLE.substring(1, UserConfig.ROLE.length());
					//

					if (key != null && key.startsWith(pseudouserProducedByOldVersion)) {
						Logging.warning(this, "user entry " + key
								+ " produced by a still somewhere running old configed version , please delete user entry "
								+ pseudouserProducedByOldVersion);

						deleteItems.add(AbstractExecutioner.jsonMap(configItem));

						Logging.info(this, "deleteItem " + configItem);

						continue;
					}

					ConfigOption configOption = new ConfigOption(configItem);

					configOptions.put(key, configOption);

					configListCellOptions.put(key, configOption);

					if (configOption.getDefaultValues() == null) {
						Logging.warning(this, "default values missing for config  " + key);

						if (tryOnceMoreCounter <= stopRepeatingAtThis) {
							tryIt = true;
							Logging.warning(this,
									"repeat loading the values , we repeated  " + tryOnceMoreCounter + " times");

							Globals.threadSleep(this, 1000);
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

				Logging.debug(this,
						" getConfigOptions produced hwAuditDevicePropertyTypes " + hwAuditDevicePropertyTypes);
			}

			Logging.info(this, "{ole deleteItems " + deleteItems.size());

			if (!deleteItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall("config_deleteObjects", new Object[] { deleteItems.toArray() });

				if (exec.doCall(omc)) {
					deleteItems.clear();
				}
			}

			getWANConfigOptions();
			Logging.debug(this, "getConfigOptions() work finished");
		}

		return configListCellOptions;
	}

	@Override
	public Map<String, RemoteControl> getRemoteControls() {
		getConfigOptions();
		return remoteControls;
	}

	@Override
	public SavedSearches getSavedSearches() {
		getConfigOptions();
		return savedSearches;
	}

	@Override
	protected boolean setHostBooleanConfigValue(String configId, String hostName, boolean val) {
		Logging.info(this, "setHostBooleanConfigValue " + hostName + " configId " + configId + " val " + val);

		List<Object> values = new ArrayList<>();
		values.add(val);

		Map<String, Object> item = createNOMitem(ConfigStateEntry.TYPE);
		item.put(ConfigStateEntry.OBJECT_ID, hostName);
		item.put(ConfigStateEntry.VALUES_ID, AbstractExecutioner.jsonArray(values));
		item.put(ConfigStateEntry.CONFIG_ID, configId);

		List<Object> jsonObjects = new ArrayList<>();
		jsonObjects.add(AbstractExecutioner.jsonMap(item));

		OpsiMethodCall omc = new OpsiMethodCall("configState_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });

		return exec.doCall(omc);
	}

	protected Boolean getHostBooleanConfigValue(String key, String hostName, boolean useGlobalFallback,
			Boolean defaultVal) {
		Boolean result = null;

		boolean globalDefault = getGlobalBooleanConfigValue(key, null);

		if (getConfigs().get(hostName) != null && getConfigs().get(hostName).get(key) != null
				&& !((List) (getConfigs().get(hostName).get(key))).isEmpty()) {

			result = interpretAsBoolean(((List) (getConfigs().get(hostName).get(key))).get(0), (Boolean) null);

			Logging.debug(this,
					"getHostBooleanConfigValue for key, host " + key + ", " + hostName + " giving " + result);

		}

		if (result == null && useGlobalFallback) {
			result = globalDefault;
			if (result != null) {
				Logging.debug(this, "getHostBooleanConfigValue for key " + key + ", taking global value  " + result);
			}
		}

		if (result == null) {
			Logging.info(this,
					"got no value for key " + key + " and host " + hostName + " setting default " + defaultVal);
			result = defaultVal;
		}

		return result;
	}

	@Override
	public Boolean getGlobalBooleanConfigValue(String key, Boolean defaultVal) {
		Boolean val = defaultVal;
		Object ob = getConfigOptions().get(key);

		Logging.debug(this, "getGlobalBooleanConfigValue key " + key + ", ob " + ob);
		if (ob == null) {
			Logging.warning(this, "getGlobalBooleanConfigValue key " + key + " gives no value, take " + val);
		} else {
			ConfigOption option = (ConfigOption) ob;

			if (option.getType() != ConfigOption.TYPE.BoolConfig) {
				Logging.warning(this, "entry for " + key + " should be boolean");
			} else {
				List li = option.getDefaultValues();
				if (li != null && !li.isEmpty()) {
					val = (Boolean) li.get(0);
				}
				Logging.debug(this, "getGlobalBooleanConfigValue key, defaultValues " + key + ", " + li);
			}
		}

		return val;
	}

	@Override
	public void setGlobalBooleanConfigValue(String key, Boolean val, String description) {
		List<Object> readyObjects = new ArrayList<>();
		Map<String, Object> configItem = createJSONBoolConfig(key, val, description);
		readyObjects.add(AbstractExecutioner.jsonMap(configItem));

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		if (exec.doCall(omc)) {
			ConfigOption configOption = createBoolConfig(key, val, description);
			configOptions.put(key, configOption);
			configListCellOptions.put(key, configOption);
			// entails class cast errors
		}
	}

	@Override
	public Map<String, List<Object>> getConfigDefaultValues() {
		getConfigOptions();
		return configDefaultValues;
	}

	@Override
	public void configOptionsRequestRefresh() {
		Logging.info(this, "configOptionsRequestRefresh");
		configOptions = null;
	}

	@Override
	public void hostConfigsRequestRefresh() {
		dataStub.hostConfigsRequestRefresh();
	}

	@Override
	public Map<String, Map<String, Object>> getConfigs() {
		return dataStub.getConfigs();
	}

	@Override
	public Map<String, Object> getConfig(String objectId) {
		getConfigOptions();

		Map<String, Object> retrieved = getConfigs().get(objectId);

		return new ConfigName2ConfigValue(retrieved, configOptions);
	}

	@Override
	public void setHostValues(Map settings) {
		if (globalReadOnly) {
			return;
		}

		List<JSONObject> hostMaps = new ArrayList<>();

		Map<Object, Object> corrected = new HashMap<>();
		for (Entry setting : (Set<Entry>) settings.entrySet()) {
			if (setting.getValue() instanceof String
					&& ((String) setting.getValue()).trim().equals(JSONReMapper.NULL_REPRESENTER)) {
				corrected.put(setting.getKey(), JSONObject.NULL);
			} else {
				corrected.put(setting.getKey(), setting.getValue());
			}
		}

		hostMaps.add(AbstractExecutioner.jsonMap(corrected));

		exec.doCall(new OpsiMethodCall("host_createObjects", new Object[] { AbstractExecutioner.jsonArray(hostMaps) }));
	}

	// collect config state updates
	@Override
	public void setAdditionalConfiguration(String objectId, ConfigName2ConfigValue settings) {
		if (configStateCollection == null) {
			configStateCollection = new ArrayList<>();
		}

		Set<String> currentKeys = settings.keySet();
		Logging.info(this, "setAdditionalConfigurations current keySet size: " + currentKeys.size());
		if (settings.getRetrieved() != null) {
			Set<String> retrievedKeys = settings.getRetrieved().keySet();

			Logging.info(this, "setAdditionalConfigurations retrieved keys size  " + retrievedKeys.size());

			Set<String> removedKeys = new HashSet<>(retrievedKeys);
			removedKeys.removeAll(currentKeys);
			Logging.info(this, "setAdditionalConfigurations removed " + removedKeys);

			if (!removedKeys.isEmpty()) {
				if (deleteConfigStateItems == null) {
					deleteConfigStateItems = new ArrayList<>();
				}

				for (Object key : removedKeys) {
					String ident = "" + key + ";" + objectId;

					Map<String, Object> item = createNOMitem("ConfigState");
					item.put("ident", ident);
					deleteConfigStateItems.add(AbstractExecutioner.jsonMap(item));
				}
			}
		}

		for (Map.Entry<String, Object> entry : settings.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			Map<String, Object> state = new HashMap<>();

			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", key);
			state.put("values", value);

			Map<String, Object> retrievedConfig = settings.getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null) {
				oldValue = retrievedConfig.get(key);
			}

			if (value != oldValue) {
				configStateCollection.add(state);

				// we hope that the update works and directly update the retrievedConfig
				if (retrievedConfig != null) {
					retrievedConfig.put(key, value);
				}
			}
		}
	}

	// send config updates and clear the collection
	@Override
	public void setAdditionalConfiguration(boolean determineConfigOptions) {
		if (globalReadOnly) {
			return;
		}

		if (Globals.checkCollection(this, "configStateCollection", configStateCollection)
				&& !configStateCollection.isEmpty()) {
			boolean configsChanged = false;

			if (deleteConfigStateItems == null) {
				deleteConfigStateItems = new ArrayList<>();
			}

			// add configId where necessary
			Set<String> usedConfigIds = new HashSet<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();

			List<Object> doneList = new ArrayList<>();

			for (Map<String, Object> configState : configStateCollection) {
				String ident = (String) configState.get("configId");
				usedConfigIds.add(ident);

				List<?> valueList = (List<?>) configState.get("values");

				if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
					typesOfUsedConfigIds.put(ident, "BoolConfig");
				} else {
					typesOfUsedConfigIds.put(ident, "UnicodeConfig");
				}

				if (valueList == MapTableModel.nullLIST) {
					Map<String, Object> item = createNOMitem("ConfigState");
					item.put("objectId", configState.get("objectId"));
					item.put("configId", configState.get("configId"));

					deleteConfigStateItems.add(AbstractExecutioner.jsonMap(item));

					doneList.add(configState);
				}
			}
			Logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: " + usedConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  " + deleteConfigStateItems);
			// not used
			if (!deleteConfigStateItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall("configState_deleteObjects",
						new Object[] { deleteConfigStateItems.toArray() });

				if (exec.doCall(omc)) {
					deleteConfigStateItems.clear();
					configStateCollection.removeAll(doneList);
				}
			}

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall("config_getIdents", new Object[] {}));
			Logging.debug(this, "setAdditionalConfiguration(), existingConfigIds: " + existingConfigIds.size());

			Set<String> missingConfigIds = new HashSet<>(usedConfigIds);
			missingConfigIds.removeAll(existingConfigIds);

			Logging.debug(this, "setAdditionalConfiguration(), missingConfigIds: " + missingConfigIds);
			List<JSONObject> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(AbstractExecutioner.jsonMap(item));
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall("config_createObjects", new Object[] { createItems.toArray() });
				exec.doCall(omc);
				configsChanged = true;
			}

			if (configsChanged) {
				configOptionsRequestRefresh();
				getConfigOptions();
			}

			// build calls

			List<JSONObject> callsConfigName2ConfigValueCollection = new ArrayList<>();
			List<JSONObject> callsConfigCollection = new ArrayList<>();

			for (Map<String, Object> state : configStateCollection) {
				if (determineConfigOptions) {
					ConfigOption configOption = configOptions.get(state.get("configId"));

					Map<String, Object> configForUpdate = new HashMap<>();

					configForUpdate.put("ident", state.get("configId"));
					configForUpdate.put("type", configOption.getRetrieved().get("type"));
					configForUpdate.put("defaultValues", AbstractExecutioner.jsonArray((List) state.get("values")));

					List<Object> possibleValues = (List<Object>) configOption.get("possibleValues");
					for (Object item : (List<?>) state.get("values")) {
						if (possibleValues.indexOf(item) == -1) {
							possibleValues.add(item);
						}
					}
					configForUpdate.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));

					// mapping to JSON
					Logging.debug(this, "setAdditionalConfiguation " + configForUpdate);
					callsConfigCollection.add(AbstractExecutioner.jsonMap(configForUpdate));
				}

				state.put("values", AbstractExecutioner.jsonArray((List<?>) state.get("values")));
				callsConfigName2ConfigValueCollection.add(AbstractExecutioner.jsonMap(state));
			}

			Logging.debug(this, "callsConfigCollection " + callsConfigCollection);
			if (!callsConfigCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall("config_updateObjects",
						new Object[] { AbstractExecutioner.jsonArray(callsConfigCollection) }));
			}

			// do call

			// now we can set the values and clear the collected update items
			exec.doCall(new OpsiMethodCall("configState_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(callsConfigName2ConfigValueCollection) }));

			// at any rate:
			configStateCollection.clear();
		}
	}

	// collect config updates
	@Override
	public void setConfig(Map<String, List<Object>> settings) {
		Logging.debug(this, "setConfig settings " + settings);
		if (configCollection == null) {
			configCollection = new ArrayList<>();
		}

		for (Entry<String, List<Object>> setting : settings.entrySet()) {
			Logging.debug(this, "setConfig,  key, settings.get(key): " + setting.getKey() + ", " + setting.getValue());

			if (setting.getValue() != null) {
				Logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): "
						+ setting.getValue() + " , " + setting.getValue().getClass().getName());

				if (setting.getValue() instanceof List) {
					List oldValue = null;

					if (configOptions.get(setting.getKey()) != null) {
						oldValue = configOptions.get(setting.getKey()).getDefaultValues();
					}

					Logging.info(this, "setConfig, key, oldValue: " + setting.getKey() + ", " + oldValue);

					List<Object> valueList = setting.getValue();

					if (valueList != null && (!valueList.equals(oldValue))) {
						Map<String, Object> config = new HashMap<>();

						config.put("ident", setting.getKey());

						String type = "UnicodeConfig";

						Logging.debug(this, "setConfig, key,  configOptions.get(key):  " + setting.getKey() + ", "
								+ configOptions.get(setting.getKey()));
						if (configOptions.get(setting.getKey()) != null) {
							type = (String) configOptions.get(setting.getKey()).get("type");
						} else {
							if (!valueList.isEmpty() && valueList.get(0) instanceof Boolean) {
								type = "BoolConfig";
							}
						}

						config.put("type", type);

						config.put("defaultValues", valueList);

						List possibleValues = null;
						if (configOptions.get(setting.getKey()) == null) {
							possibleValues = new ArrayList<>();
							if (type.equals(ConfigOption.BOOL_TYPE)) {
								possibleValues.add(true);
								possibleValues.add(false);
							}
						} else {
							possibleValues = configOptions.get(setting.getKey()).getPossibleValues();
						}

						for (Object item : valueList) {
							if (possibleValues.indexOf(item) == -1) {
								possibleValues.add(item);
							}
						}

						config.put("possibleValues", possibleValues);

						configCollection.add(config);
					}
				} else {
					Logging.error("setConfig,  setting.getKey(), setting.getValue(): " + setting.getKey() + ", "
							+ setting.getValue() + " \nUnexpected type");
				}
			}
		}
	}

	// send config updates and clear the collection
	@Override
	public void setConfig() {
		setConfig(false);
	}

	// send config updates, possibly not updating existing

	protected void setConfig(boolean restrictToMissing) {
		Logging.info(this, "setConfig(),  configCollection null " + (configCollection == null));
		if (configCollection != null) {
			Logging.info(this, "setConfig(),  configCollection size  " + configCollection.size());
		}

		if (globalReadOnly) {
			return;
		}

		if (configCollection != null && !configCollection.isEmpty()) {
			// add configId where necessary
			List<String> usedConfigIds = new ArrayList<>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<>();
			for (Map<String, Object> config : configCollection) {
				String ident = (String) config.get("ident");
				usedConfigIds.add(ident);
				typesOfUsedConfigIds.put(ident, (String) config.get("type"));
			}

			Logging.debug(this, "setConfig(), usedConfigIds: " + usedConfigIds);

			List<Object> existingConfigIds = exec
					.getListResult(new OpsiMethodCall("config_getIdents", new Object[] {}));

			Logging.info(this, "setConfig(), existingConfigIds: " + existingConfigIds.size());

			List<String> missingConfigIds = new ArrayList<>(usedConfigIds);
			for (Object configId : existingConfigIds) {
				missingConfigIds.remove(configId);
			}
			Logging.info(this, "setConfig(), missingConfigIds: " + missingConfigIds);
			List<JSONObject> createItems = new ArrayList<>();
			for (String missingId : missingConfigIds) {
				Map<String, Object> item = createNOMitem(typesOfUsedConfigIds.get(missingId));
				item.put("ident", missingId);
				createItems.add(AbstractExecutioner.jsonMap(item));
			}

			if (!createItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall("config_createObjects", new Object[] { createItems.toArray() });
				exec.doCall(omc);
			}

			// remap to JSON types
			List<JSONObject> callsConfigUpdateCollection = new ArrayList<>();
			List<JSONObject> callsConfigDeleteCollection = new ArrayList<>();

			for (Map<String, Object> callConfig : configCollection) {

				if (callConfig.get("defaultValues") == MapTableModel.nullLIST) {
					callsConfigDeleteCollection.add(AbstractExecutioner.jsonMap(callConfig));
				} else {
					Logging.debug(this, "setConfig config with ident " + callConfig.get("ident"));

					boolean isMissing = missingConfigIds.contains(callConfig.get("ident"));

					if (!restrictToMissing || isMissing) {
						callConfig.put("defaultValues",
								AbstractExecutioner.jsonArray((List<?>) callConfig.get("defaultValues")));
						callConfig.put("possibleValues",
								AbstractExecutioner.jsonArray((List<?>) callConfig.get("possibleValues")));
						callsConfigUpdateCollection.add(AbstractExecutioner.jsonMap(callConfig));
					}
				}
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigDeleteCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall("config_deleteObjects",
						new Object[] { AbstractExecutioner.jsonArray(callsConfigDeleteCollection) }));
				configOptionsRequestRefresh();
				// because of referential integrity
				hostConfigsRequestRefresh();
			}

			Logging.debug(this, "setConfig() callsConfigUpdateCollection " + callsConfigUpdateCollection);

			if (!callsConfigUpdateCollection.isEmpty()) {
				exec.doCall(new OpsiMethodCall("config_updateObjects",
						new Object[] { AbstractExecutioner.jsonArray(callsConfigUpdateCollection) }));
				configOptionsRequestRefresh();
			}

			getConfigOptions();
			configCollection.clear();

			Logging.info(this, "setConfig(),  configCollection result: " + configCollection);
		}
	}

	/**
	 * delivers the default domain if it is not existing it retrieves it from
	 * servide
	 */
	@Override
	public String getOpsiDefaultDomain() {
		retrieveOpsiDefaultDomain();
		return opsiDefaultDomain;
	}

	/**
	 * signals that the default domain shall be reloaded from service
	 */
	@Override
	public void requestReloadOpsiDefaultDomain() {
		opsiDefaultDomain = null;
	}

	/**
	 * retrieves default domain from service
	 */
	protected void retrieveOpsiDefaultDomain() {
		if (opsiDefaultDomain == null) {
			Object[] params = new Object[] {};
			opsiDefaultDomain = exec.getStringResult(new OpsiMethodCall("getDomain", params));
		}
	}

	@Override
	public List<String> getDomains() {
		List<String> result = new ArrayList<>();

		if (configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY) == null) {
			Logging.info(this, "no values found for   " + CONFIGED_GIVEN_DOMAINS_KEY);
		} else {
			Logging.info(this, "getDomains " + configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY));

			HashMap<String, Integer> numberedValues = new HashMap<>();
			TreeSet<String> orderedValues = new TreeSet<>();
			TreeSet<String> unorderedValues = new TreeSet<>();

			for (Object item : configDefaultValues.get(CONFIGED_GIVEN_DOMAINS_KEY)) {
				String entry = (String) item;
				int p = entry.indexOf(":");
				if (p == -1 || p == 0) {
					unorderedValues.add(entry);
				} else if (p > 0) {
					// the only regular case
					int orderNumber = -1;
					try {
						orderNumber = Integer.valueOf(entry.substring(0, p));
						String value = entry.substring(p + 1);
						if (numberedValues.get(value) == null || orderNumber < numberedValues.get(value)) {
							orderedValues.add(entry);
							numberedValues.put(value, orderNumber);
						}
					} catch (NumberFormatException x) {
						Logging.warning(this, "illegal order format for domain entry: " + entry);
						unorderedValues.add(entry);
					}
				}
			}

			for (String entry : orderedValues) {
				int p = entry.indexOf(":");
				result.add(entry.substring(p + 1));
			}

			unorderedValues.removeAll(result);

			for (String entry : unorderedValues) {
				result.add(entry);
			}
		}

		Logging.info(this, "getDomains " + result);
		return result;
	}

	@Override
	public void writeDomains(List<Object> domains) {
		String key = CONFIGED_GIVEN_DOMAINS_KEY;
		Map<String, Object> item = createNOMitem("UnicodeConfig");

		item.put("ident", key);
		item.put("description", "saved domains for creating clients");
		item.put("defaultValues", AbstractExecutioner.jsonArray(domains));
		item.put("possibleValues", AbstractExecutioner.jsonArray(domains));
		item.put("editable", true);
		item.put("multiValue", true);

		List<Object> readyObjects = new ArrayList<>();
		readyObjects.add(AbstractExecutioner.jsonMap(item));

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		exec.doCall(omc);

		configDefaultValues.put(key, domains);
	}

	@Override
	public void setDepot(String depotId) {
		Logging.info(this, "setDepot =========== " + depotId);
		theDepot = depotId;
	}

	@Override
	public String getDepot() {
		return theDepot;
	}

	@Override
	public Map<String, SWAuditEntry> getInstalledSoftwareInformation() {

		Logging.info(this, "getInstalledSoftwareInformation");

		return dataStub.getInstalledSoftwareInformation();
	}

	@Override
	public NavigableMap<String, Set<String>> getName2SWIdents() {
		return dataStub.getName2SWIdents();
	}

	@Override
	public Map<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing() {

		return dataStub.getInstalledSoftwareInformationForLicensing();
	}

	// only software relevant of the items for licensing
	@Override
	public NavigableMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo() {
		return dataStub.getInstalledSoftwareName2SWinfo();
	}

	@Override
	public void installedSoftwareInformationRequestRefresh() {
		Logging.info(this, " call installedSoftwareInformationRequestRefresh()");
		dataStub.installedSoftwareInformationRequestRefresh();
	}

	@Override
	public String getSWident(Integer i) {
		return dataStub.getSWident(i);
	}

	@Override
	public List<String> getSoftwareList() {
		return dataStub.getSoftwareList();
	}

	@Override
	public NavigableMap<String, Integer> getSoftware2Number() {
		return dataStub.getSoftware2Number();
	}

	// without internal caching
	@Override
	public Map<String, LicenceContractEntry> getLicenceContracts() {
		dataStub.licenceContractsRequestRefresh();
		return dataStub.getLicenceContracts();
	}

	// date in sql time format, contrad ID
	@Override
	public NavigableMap<String, NavigableSet<String>> getLicenceContractsExpired() {
		dataStub.licenceContractsRequestRefresh();
		return dataStub.getLicenceContractsToNotify();
	}

	// returns the ID of the edited data record
	@Override
	public String editLicenceContract(String licenseContractId, String partner, String conclusionDate,
			String notificationDate, String expirationDate, String notes) {
		if (!serverFullPermission) {
			return "";
		}
		String result = "";

		Logging.debug(this, "editLicenceContract " + licenseContractId);

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("createLicenseContract", new String[] { licenseContractId, partner,
					conclusionDate, notificationDate, expirationDate, notes });

			// the method gives the first letter instead of the complete string as return
			// value, therefore we set it in a shortcut:

			exec.getStringResult(omc);
			result = licenseContractId;
		}

		Logging.debug(this, "editLicenceContract result " + result);

		return result;
	}

	@Override
	public boolean deleteLicenceContract(String licenseContractId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("deleteLicenseContract", new String[] { licenseContractId });
			result = exec.doCall(omc);
		}

		return result;
	}

	// returns the ID of the edited data record
	@Override
	public String editLicencePool(String licensePoolId, String description) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("createLicensePool", new String[] { licensePoolId, description });
			result = exec.getStringResult(omc);
		}

		return result;
	}

	@Override
	public boolean deleteLicencePool(String licensePoolId) {
		Logging.info(this, "deleteLicencePool " + licensePoolId);

		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			// does not get reach into the crucial data structures
			OpsiMethodCall omc = new OpsiMethodCall("deleteLicensePool", new Object[] { licensePoolId, false });
			result = exec.doCall(omc);
			// comes too late
		}

		return result;
	}

	// without internal caching
	@Override
	public Map<String, LicenceEntry> getSoftwareLicences() {
		Map<String, LicenceEntry> softwareLicences = new HashMap<>();

		if (withLicenceManagement) {
			dataStub.licencesRequestRefresh();
			softwareLicences = dataStub.getLicences();
		}
		return softwareLicences;
	}

	// returns the ID of the edited data record
	@Override
	public String editSoftwareLicence(String softwareLicenseId, String licenceContractId, String licenceType,
			String maxInstallations, String boundToHost, String expirationDate) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("createSoftwareLicense", new String[] { softwareLicenseId,
					licenceContractId, licenceType, maxInstallations, boundToHost, expirationDate });
			result = exec.getStringResult(omc);
		}

		return result;
	}

	@Override
	public boolean deleteSoftwareLicence(String softwareLicenseId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("deleteSoftwareLicense", new Object[] { softwareLicenseId, false });
			result = exec.doCall(omc);
		}

		return result;
	}

	// without internal caching; legacy license method
	@Override
	public Map<String, Map<String, Object>> getRelationsSoftwareL2LPool() {
		Map<String, Map<String, Object>> rowsSoftwareL2LPool = new HashMap<>();

		if (withLicenceManagement) {
			List<Object> li0 = exec
					.getListResult(new OpsiMethodCall("getSoftwareLicenses_listOfHashes", new String[] {}));

			Iterator<Object> iter0 = li0.iterator();

			while (iter0.hasNext()) {
				Object ob = iter0.next();

				Map<String, Object> m0 = exec.getMapFromItem(ob);
				String softwareLicenseId = (String) m0.get("softwareLicenseId");

				List<Object> li1 = exec.getListFromItem("" + m0.get("licensePoolIds"));
				Map<String, Object> m1 = exec.getMapFromItem("" + m0.get("licenseKeys"));

				Iterator<Object> iter1 = li1.iterator();

				while (iter1.hasNext()) {
					Map<String, Object> m = new HashMap<>();
					String licensePoolId = (String) iter1.next();
					m.put("licensePoolId", licensePoolId);

					String licenseKey = null;
					if (m1 != null) {
						licenseKey = (String) m1.get(licensePoolId);
					}

					if (licenseKey == null) {
						licenseKey = "";
					}

					m.put("licenseKey", licenseKey);

					m.put("softwareLicenseId", softwareLicenseId);

					rowsSoftwareL2LPool.put(Globals.pseudokey(new String[] { softwareLicenseId, licensePoolId }), m);
				}
			}
		}

		return rowsSoftwareL2LPool;
	}

	@Override
	public String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId, String licenseKey) {
		if (!serverFullPermission) {
			return "";
		}

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("addSoftwareLicenseToLicensePool",
					new String[] { softwareLicenseId, licensePoolId, licenseKey });

			exec.getStringResult(omc);
		}

		return Globals.pseudokey(new String[] { softwareLicenseId, licensePoolId });
	}

	@Override
	public boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("removeSoftwareLicenseFromLicensePool",
					new String[] { softwareLicenseId, licensePoolId });

			result = exec.doCall(omc);
		}

		return result;
	}

	// without internal caching
	@Override
	public Map<String, Map<String, String>> getRelationsProductId2LPool() {
		HashMap<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<>();

		if (withLicenceManagement) {
			dataStub.licencePoolXOpsiProductRequestRefresh();
			dataStub.getLicencePoolXOpsiProduct();
			Logging.info(this, "licencePoolXOpsiProduct size " + dataStub.getLicencePoolXOpsiProduct().size());

			for (StringValuedRelationElement element : dataStub.getLicencePoolXOpsiProduct()) {
				rowsLicencePoolXOpsiProduct
						.put(Globals.pseudokey(new String[] { element.get(LicencePoolXOpsiProduct.LICENCE_POOL_KEY),
								element.get(LicencePoolXOpsiProduct.PRODUCT_ID_KEY) }), element);
			}
		}

		Logging.info(this, "rowsLicencePoolXOpsiProduct size " + rowsLicencePoolXOpsiProduct.size());

		return rowsLicencePoolXOpsiProduct;
	}

	@Override
	public String editRelationProductId2LPool(String productId, String licensePoolId) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("addProductIdsToLicensePool",
					new String[] { productId, licensePoolId });

			exec.doCall(omc);
			result = licensePoolId;
		}

		return result;
	}

	@Override
	public boolean deleteRelationProductId2LPool(String productId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("removeProductIdsFromLicensePool",
					new String[] { productId, licensePoolId });

			result = exec.doCall(omc);
		}

		return result;
	}

	@Override
	public void retrieveRelationsAuditSoftwareToLicencePools() {
		Logging.info(this,
				"retrieveRelationsAuditSoftwareToLicencePools start " + (relationsAuditSoftwareToLicencePools != null));

		if (relationsAuditSoftwareToLicencePools == null) {
			dataStub.auditSoftwareXLicencePoolRequestRefresh();
		} else {
			return;
		}

		relationsAuditSoftwareToLicencePools = dataStub.getAuditSoftwareXLicencePool();

		rowmapAuditSoftware = new TreeMap<>();
		// function softwareIdent --> pool
		fSoftware2LicencePool = new HashMap<>();
		// function pool --> list of assigned software
		fLicencePool2SoftwareList = new HashMap<>();
		// function pool --> list of assigned software
		fLicencePool2UnknownSoftwareList = new HashMap<>();

		softwareWithoutAssociatedLicencePool = new TreeSet<>(getInstalledSoftwareInformationForLicensing().keySet());

		if (!withLicenceManagement) {
			return;
		}

		for (StringValuedRelationElement retrieved : relationsAuditSoftwareToLicencePools) {
			SWAuditEntry entry = new SWAuditEntry(retrieved);
			String licencePoolKEY = retrieved.get(LicencepoolEntry.ID_SERVICE_KEY);
			String swKEY = entry.getIdent();

			// build row for software table
			LinkedHashMap<String, String> row = new LinkedHashMap<>();

			for (String colName : SWAuditEntry.getDisplayKeys()) {
				row.put(colName, entry.get(colName));

			}
			rowmapAuditSoftware.put(swKEY, row);

			// build fSoftware2LicencePool
			if (fSoftware2LicencePool.get(swKEY) != null && !fSoftware2LicencePool.get(swKEY).equals(licencePoolKEY)) {
				Logging.error("software with ident \"" + swKEY + "\" has assigned license pool "
						+ fSoftware2LicencePool.get(swKEY) + " as well as " + licencePoolKEY);
			}
			fSoftware2LicencePool.put(swKEY, licencePoolKEY);

			// build fLicencePool2SoftwareList
			if (fLicencePool2SoftwareList.get(licencePoolKEY) == null) {
				fLicencePool2SoftwareList.put(licencePoolKEY, new ArrayList<>());
			}

			List<String> softwareIds = fLicencePool2SoftwareList.get(licencePoolKEY);
			if (softwareIds.indexOf(swKEY) == -1) {
				if (getInstalledSoftwareInformationForLicensing().get(swKEY) == null) {
					Logging.warning(this, "license pool " + licencePoolKEY
							+ " is assigned to a not listed software with ID " + swKEY + " data row " + row);
					// we serve the fLicencePool2UnknownSoftwareList only in case that a key is
					// found
					List<String> unknownSoftwareIds = fLicencePool2UnknownSoftwareList.get(licencePoolKEY);
					if (unknownSoftwareIds == null) {
						unknownSoftwareIds = new ArrayList<>();
						fLicencePool2UnknownSoftwareList.put(licencePoolKEY, unknownSoftwareIds);
					}
					unknownSoftwareIds.add(swKEY);
				} else {
					softwareIds.add(swKEY);
					softwareWithoutAssociatedLicencePool.remove(swKEY);
				}
			}
		}

		Logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools,  softwareWithoutAssociatedLicencePool "
				+ softwareWithoutAssociatedLicencePool.size());
	}

	@Override
	public NavigableSet<Object> getSoftwareWithoutAssociatedLicencePool() {
		if (softwareWithoutAssociatedLicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		return softwareWithoutAssociatedLicencePool;
	}

	@Override
	public void relationsAuditSoftwareToLicencePoolsRequestRefresh() {
		relationsAuditSoftwareToLicencePools = null;
		softwareWithoutAssociatedLicencePool = null;
		fLicencePool2SoftwareList = null;
		fLicencePool2UnknownSoftwareList = null;
	}

	@Override
	public List<String> getSoftwareListByLicencePool(String licencePoolId) {
		if (fLicencePool2SoftwareList == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		List<String> result = fLicencePool2SoftwareList.get(licencePoolId);
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	@Override
	public List<String> getUnknownSoftwareListForLicencePool(String licencePoolId) {
		if (fLicencePool2UnknownSoftwareList == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}

		List<String> result = fLicencePool2UnknownSoftwareList.get(licencePoolId);
		if (result == null) {
			result = new ArrayList<>();
		}
		return result;
	}

	@Override
	public Map<String, String> getFSoftware2LicencePool() {
		if (fSoftware2LicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}
		return fSoftware2LicencePool;
	}

	@Override
	public String getFSoftware2LicencePool(String softwareIdent) {
		if (fSoftware2LicencePool == null) {
			retrieveRelationsAuditSoftwareToLicencePools();
		}
		return fSoftware2LicencePool.get(softwareIdent);
	}

	@Override
	public void setFSoftware2LicencePool(String softwareIdent, String licencePoolId) {
		fSoftware2LicencePool.put(softwareIdent, licencePoolId);
	}

	@Override
	public boolean removeAssociations(String licencePoolId, List<String> softwareIds) {
		Logging.info(this, "removeAssociations licensePoolId, softwareIds " + licencePoolId + ", " + softwareIds);

		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (licencePoolId == null || softwareIds == null) {
			return result;
		}

		if (withLicenceManagement) {
			List<JSONObject> deleteItems = new ArrayList<>();

			for (String swIdent : softwareIds) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licencePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				deleteItems.add(AbstractExecutioner.jsonMap(item));
			}

			OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_deleteObjects",
					new Object[] { deleteItems.toArray() });
			result = exec.doCall(omc);

			if (result) {
				for (String swIdent : softwareIds) {
					fSoftware2LicencePool.remove(swIdent);
				}

				if (fLicencePool2SoftwareList.get(licencePoolId) != null) {
					fLicencePool2SoftwareList.get(licencePoolId).removeAll(softwareIds);
				}

				if (fLicencePool2UnknownSoftwareList.get(licencePoolId) != null) {
					fLicencePool2UnknownSoftwareList.get(licencePoolId).removeAll(softwareIds);
				}
			}
		}

		return result;
	}

	@Override
	public boolean setWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, false);
	}

	@Override
	public boolean addWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, true);
	}

	private boolean setWindowsSoftwareIds2LPool(String licensePoolId, final List<String> softwareToAssign,
			boolean onlyAdding) {
		Logging.debug(this, "setWindowsSoftwareIds2LPool  licensePoolId,  softwareToAssign:" + licensePoolId + " , "
				+ softwareToAssign);

		if (!serverFullPermission) {
			return false;
		}

		boolean result = true;

		if (withLicenceManagement) {
			Map<String, SWAuditEntry> instSwI = getInstalledSoftwareInformationForLicensing();

			List<String> oldEntries = fLicencePool2SoftwareList.computeIfAbsent(licensePoolId,
					arg -> new ArrayList<>());

			List<String> oldEntriesTruely = new ArrayList<>(oldEntries);
			List<String> softwareToAssignTruely = new ArrayList<>(softwareToAssign);

			Set<String> entriesToRemove = new HashSet<>();

			// we work only with real changes
			softwareToAssignTruely.removeAll(oldEntries);
			oldEntriesTruely.removeAll(softwareToAssign);

			Logging.info(this, "setWindowsSoftwareIds2LPool softwareToAssignTruely " + softwareToAssignTruely);
			Logging.info(this, "setWindowsSoftwareIds2LPool oldEntriesTruely " + oldEntriesTruely);

			if (!onlyAdding) {
				ArrayList<JSONObject> deleteItems = new ArrayList<>();

				for (String swIdent : oldEntriesTruely) {
					// software exists in audit software
					if (instSwI.get(swIdent) != null) {
						entriesToRemove.add(swIdent);
						Map<String, String> item = new HashMap<>();
						item.put("ident", swIdent + ";" + licensePoolId);
						item.put("type", "AuditSoftwareToLicensePool");
						deleteItems.add(AbstractExecutioner.jsonMap(item));

						Logging.info(this, "" + instSwI.get(swIdent));
					}
				}
				Logging.info(this, "entriesToRemove " + entriesToRemove);
				Logging.info(this, "deleteItems " + deleteItems);

				if (!deleteItems.isEmpty()) {
					OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_deleteObjects",
							new Object[] { deleteItems.toArray() });
					result = exec.doCall(omc);
				}

				if (!result) {
					return false;
				} else {
					// do it locally
					for (String swIdent : entriesToRemove) {
						instSwI.remove(swIdent);
					}
				}

			}

			ArrayList<JSONObject> createItems = new ArrayList<>();

			for (String swIdent : softwareToAssignTruely) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licensePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				createItems.add(AbstractExecutioner.jsonMap(item));
			}

			Logging.info(this, "setWindowsSoftwareIds2LPool, createItems " + createItems);

			OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_createObjects",
					new Object[] { createItems.toArray() });

			result = exec.doCall(omc);

			// we build the correct data locally
			if (result) {
				HashSet<String> intermediateSet = new HashSet<>(fLicencePool2SoftwareList.get(licensePoolId));
				intermediateSet.removeAll(entriesToRemove);
				intermediateSet.addAll(softwareToAssign);
				// dont delete old entries but avoid double entries
				List<String> newList = new ArrayList<>(intermediateSet);
				fLicencePool2SoftwareList.put(licensePoolId, newList);

				softwareWithoutAssociatedLicencePool.addAll(entriesToRemove);
				softwareWithoutAssociatedLicencePool.removeAll(softwareToAssign);

				Logging.info(this, "setWindowsSoftwareIds2LPool licencePool, fLicencePool2SoftwareList " + licensePoolId
						+ " : " + fLicencePool2SoftwareList.get(licensePoolId));

				for (String ident : newList) {
					// give zero length parts as ""
					String[] parts = ident.split(";", -1);
					String swName = parts[1];
					if (getName2SWIdents().get(swName) == null) {
						getName2SWIdents().put(swName, new TreeSet<>());
					}
					getName2SWIdents().get(swName).add(ident);

					Logging.info(this,
							"setWindowsSoftwareIds2LPool, collecting all idents for a name (even if not belonging to the pool), add ident "
									+ ident + " to set for name " + swName);
				}
			}
		}

		return result;
	}

	// we have got a SW from software table, therefore we do not serve the unknown
	// software list
	@Override
	public String editPool2AuditSoftware(String softwareID, String licensePoolIDOld, String licencePoolIDNew) {
		if (!serverFullPermission) {
			return "";
		}

		String result = "";

		boolean ok = false;
		Logging.info(this, "editPool2AuditSoftware ");

		if (withLicenceManagement) {
			if (licensePoolIDOld != null && !licensePoolIDOld.equals(FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL)) {
				// there was an association, we delete it)

				List<String> swIds = new ArrayList<>();
				swIds.add(softwareID);
				ok = removeAssociations(licensePoolIDOld, swIds);

				if (!ok) {
					Logging.warning(this, "editPool2AuditSoftware " + " failed");
				}
			}

			if (FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL.equals(licencePoolIDNew)) {
				// nothing to do, we deleted the entry
				ok = true;
			} else {
				List<Object> readyObjects = new ArrayList<>();
				Map<String, Object> item;

				Map<String, String> swMap = AuditSoftwareXLicencePool.produceMapFromSWident(softwareID);
				swMap.put(LicencepoolEntry.ID_SERVICE_KEY, licencePoolIDNew);

				item = createNOMitem("AuditSoftwareToLicensePool");
				item.putAll(swMap);
				// create the edited entry

				readyObjects.add(AbstractExecutioner.jsonMap(item));

				OpsiMethodCall omc = new OpsiMethodCall("auditSoftwareToLicensePool_createObjects",
						new Object[] { AbstractExecutioner.jsonArray(readyObjects) }

				);
				Logging.info(this, "editPool2AuditSoftware call " + omc);
				if (exec.doCall(omc)) {
					ok = true;
				} else {
					Logging.warning(this, "editPool2AuditSoftware " + omc + " failed");
				}
			}

			Logging.info(this, "editPool2AuditSoftware ok " + ok);

			if (ok) {
				Logging.info(this, "fSoftware2LicencePool == null " + (fSoftware2LicencePool == null));

				if (fSoftware2LicencePool != null) {
					Logging.info(this,
							"fSoftware2LicencePool.get( softwareID ) " + fSoftware2LicencePool.get(softwareID));
					fSoftware2LicencePool.put(softwareID, licencePoolIDNew);
				}
				List<String> fLicencePoolSoftwareList = fLicencePool2SoftwareList.computeIfAbsent(licencePoolIDNew,
						arg -> new ArrayList<>());

				Logging.info(this, "fLicencePool2SoftwareList.get( licencePoolIDNew ) " + fLicencePoolSoftwareList);

				fLicencePoolSoftwareList.add(softwareID);
			}

			return result;
		}

		return "???";
	}

	@Override
	public List<String> getServerConfigStrings(String key) {
		getConfigOptions();

		return Globals.takeAsStringList(configDefaultValues.get(key));
	}

	@Override
	public Map<String, LicencepoolEntry> getLicencepools() {
		dataStub.licencepoolsRequestRefresh();
		return dataStub.getLicencepools();
	}

	// poolId -> LicenceStatisticsRow
	protected Map<String, LicenceStatisticsRow> produceLicenceStatistics() {
		// side effects of this method: rowsLicencesReconciliation
		Logging.info(this, "produceLicenceStatistics === ");

		Map<String, List<String>> licencePool2listOfUsingClientsSWInvent = new HashMap<>();

		Map<String, Set<String>> licencePool2setOfUsingClientsSWInvent = new HashMap<>();

		// result
		Map<String, Integer> licencePoolUsagecountSWInvent = new HashMap<>();

		// now we have audit software on client data for all clients
		fillClient2Software(getHostInfoCollections().getOpsiHostNames());
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = dataStub.getAuditSoftwareXLicencePool();

		Map<String, Set<String>> swId2clients = dataStub.getSoftwareIdent2clients();

		if (withLicenceManagement) {
			Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

			if (rowsLicencesReconciliation == null) {
				rowsLicencesReconciliation = new HashMap<>();

				List<String> extraHostFields = getServerConfigStrings(
						KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION);

				Map<String, HostInfo> clientMap = hostInfoCollections.getMapOfAllPCInfoMaps();

				for (Entry<String, HostInfo> clientEntry : clientMap.entrySet()) {
					for (String pool : licencePools.keySet()) {
						HashMap<String, Object> rowMap = new HashMap<>();

						rowMap.put(HWAuditClientEntry.HOST_KEY, clientEntry.getKey());

						for (String fieldName : extraHostFields) {
							rowMap.put(fieldName, clientEntry.getValue().getMap().get(fieldName));
						}

						rowMap.put("licensePoolId", pool);
						rowMap.put("used_by_opsi", false);
						rowMap.put("SWinventory_used", false);
						String pseudokey = Globals.pseudokey(new String[] { clientEntry.getKey(), pool });
						rowsLicencesReconciliation.put(pseudokey, rowMap);
					}
				}
			}

			getInstalledSoftwareInformationForLicensing();

			retrieveRelationsAuditSoftwareToLicencePools();

			// idents
			for (String softwareIdent : getInstalledSoftwareInformationForLicensing().keySet()) {
				String licencePoolId = fSoftware2LicencePool.get(softwareIdent);

				if (licencePoolId != null) {
					List<String> listOfUsingClients = licencePool2listOfUsingClientsSWInvent.get(licencePoolId);
					Set<String> setOfUsingClients = licencePool2setOfUsingClientsSWInvent.get(licencePoolId);

					if (listOfUsingClients == null) {
						listOfUsingClients = new ArrayList<>();
						licencePool2listOfUsingClientsSWInvent.put(licencePoolId, listOfUsingClients);
					}

					if (setOfUsingClients == null) {
						setOfUsingClients = new HashSet<>();
						licencePool2setOfUsingClientsSWInvent.put(licencePoolId, setOfUsingClients);
					}

					Logging.debug(this,
							"software " + softwareIdent + " installed on " + swId2clients.get(softwareIdent));

					if (swId2clients.get(softwareIdent) == null) {
						continue;
					}

					try {
						for (String client : swId2clients.get(softwareIdent)) {
							listOfUsingClients.add(client);
							setOfUsingClients.add(client);
						}
					} catch (Exception ex) {
						Logging.warning(" swId2clients.get(softwareIdent) -" + ex);
					}

					licencePoolUsagecountSWInvent.put(licencePoolId, setOfUsingClients.size());

					for (String client : swId2clients.get(softwareIdent)) {
						String pseudokey = Globals.pseudokey(new String[] { client, licencePoolId });

						if (rowsLicencesReconciliation.get(pseudokey) == null) {
							Logging.warning(
									"client " + client + " or license pool ID " + licencePoolId + " do not exist");
						} else {
							rowsLicencesReconciliation.get(pseudokey).put("SWinventory_used", true);
						}
					}
				}
			}
		}

		// ------------------ retrieve data for statistics

		// table LICENSE_POOL
		Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

		// table SOFTWARE_LICENSE
		Logging.info(this, " licences ");

		// table SOFTWARE_LICENSE_TO_LICENSE_POOL
		Logging.info(this, " licence usabilities ");
		List<LicenceUsableForEntry> licenceUsabilities = dataStub.getLicenceUsabilities();

		// table LICENSE_ON_CLIENT
		Logging.info(this, " licence usages ");
		List<LicenceUsageEntry> licenceUsages = dataStub.getLicenceUsages();

		// software usage according to audit
		// tables
		// AUDIT_SOFTWARE_TO_LICENSE_POOL
		// SOFTWARE_CONFIG
		// leads to getSoftwareAuditOnClients()

		// ----------------- set up data structure

		TreeMap<String, ExtendedInteger> pool2allowedUsagesCount = new TreeMap<>();

		for (LicenceUsableForEntry licenceUsability : licenceUsabilities) {
			String pool = licenceUsability.getLicencePoolId();
			String licenceId = licenceUsability.getLicenceId();

			// value up this step
			ExtendedInteger count = pool2allowedUsagesCount.get(pool);

			// not yet initialized
			if (count == null) {
				count = dataStub.getLicences().get(licenceId).getMaxInstallations();
				pool2allowedUsagesCount.put(pool, count);
			} else {
				ExtendedInteger result = count.add(dataStub.getLicences().get(licenceId).getMaxInstallations());
				pool2allowedUsagesCount.put(pool, result);
			}
		}

		Logging.debug(this, " pool2allowedUsagesCount " + pool2allowedUsagesCount);

		TreeMap<String, Integer> pool2opsiUsagesCount = new TreeMap<>();

		Map<String, Set<String>> pool2opsiUsages = new TreeMap<>();

		for (LicenceUsageEntry licenceUsage : licenceUsages) {
			String pool = licenceUsage.getLicencepool();
			Integer usageCount = pool2opsiUsagesCount.get(pool);
			Set<String> usingClients = pool2opsiUsages.get(pool);

			if (usingClients == null) {
				usingClients = new TreeSet<>();
				pool2opsiUsages.put(pool, usingClients);
			}

			if (usageCount == null) {
				usageCount = Integer.valueOf(0);
				pool2opsiUsagesCount.put(pool, usageCount);
			}

			String clientId = licenceUsage.getClientId();

			if (clientId != null) {
				usageCount = usageCount + 1;
				pool2opsiUsagesCount.put(pool, usageCount);
				usingClients.add(clientId);
			}
		}

		// all used licences for pools

		Logging.info(this, "  retrieveStatistics  collect pool2installationsCount");

		TreeMap<String, TreeSet<String>> pool2clients = new TreeMap<>();
		// we take Set since we count only one usage per client

		TreeMap<String, Integer> pool2installationsCount = new TreeMap<>();

		// require this licencepool
		// add the clients which have this software installed

		for (StringValuedRelationElement swXpool : auditSoftwareXLicencePool) {
			Logging.debug(this, " retrieveStatistics1 relationElement  " + swXpool);
			String pool = swXpool.get(LicencepoolEntry.ID_SERVICE_KEY);

			TreeSet<String> clientsServedByPool = pool2clients.get(pool);

			if (clientsServedByPool == null) {
				clientsServedByPool = new TreeSet<>();
				pool2clients.put(pool, clientsServedByPool);
			}

			String swIdent = swXpool.get(AuditSoftwareXLicencePool.SW_ID);

			Logging.debug(this, " retrieveStatistics1 swIdent " + swIdent);

			if (swId2clients.get(swIdent) != null) {
				Logging.debug(this, "pool " + pool + " serves clients " + swId2clients.get(swIdent));
				clientsServedByPool.addAll(swId2clients.get(swIdent));
			}
		}

		for (Entry<String, TreeSet<String>> poolEntry : pool2clients.entrySet()) {
			pool2installationsCount.put(poolEntry.getKey(), poolEntry.getValue().size());
		}

		rowsLicenceStatistics = new TreeMap<>();

		if (withLicenceManagement) {
			for (String licencePoolId : licencePools.keySet()) {
				LicenceStatisticsRow rowMap = new LicenceStatisticsRow(licencePoolId);
				rowsLicenceStatistics.put(licencePoolId, rowMap);

				rowMap.setAllowedUsagesCount(pool2allowedUsagesCount.get(licencePoolId));
				rowMap.setOpsiUsagesCount(pool2opsiUsagesCount.get(licencePoolId));
				rowMap.setSWauditUsagesCount(pool2installationsCount.get(licencePoolId));

				Set<String> listOfUsingClients = pool2opsiUsages.get(licencePoolId);

				Logging.debug(this, "pool  " + licencePoolId + " used_by_opsi on clients : " + listOfUsingClients);

				if (listOfUsingClients != null) {
					for (String client : listOfUsingClients) {
						String pseudokey = Globals.pseudokey(new String[] { client, licencePoolId });

						if (rowsLicencesReconciliation.get(pseudokey) == null) {
							Logging.warning(
									"client " + client + " or license pool ID " + licencePoolId + " do not exist");
						} else {
							rowsLicencesReconciliation.get(pseudokey).put("used_by_opsi", true);
						}
					}
				}
			}
		}

		Logging.debug(this, "rowsLicenceStatistics " + rowsLicenceStatistics);

		return rowsLicenceStatistics;
	}

	@Override
	public Map<String, LicenceStatisticsRow> getLicenceStatistics() {
		return produceLicenceStatistics();
	}

	@Override
	public void licencesUsageRequestRefresh() {
		rowsLicencesUsage = null;
		fClient2LicencesUsageList = null;
	}

	@Override
	public Map<String, LicenceUsageEntry> getLicencesUsage() {
		retrieveLicencesUsage();
		return rowsLicencesUsage;
	}

	@Override
	public Map<String, List<LicenceUsageEntry>> getFClient2LicencesUsageList() {
		retrieveLicencesUsage();
		return fClient2LicencesUsageList;
	}

	protected void retrieveLicencesUsage() {
		Logging.info(this, "retrieveLicencesUsage with refresh " + (rowsLicencesUsage == null));

		if (rowsLicencesUsage == null) {
			dataStub.licenceUsagesRequestRefresh();
		} else {
			return;
		}

		if (!withLicenceManagement) {
			return;
		}

		rowsLicencesUsage = new HashMap<>();
		fClient2LicencesUsageList = new HashMap<>();

		for (LicenceUsageEntry m : dataStub.getLicenceUsages()) {
			rowsLicencesUsage.put(m.getPseudoKey(), m);

			List<LicenceUsageEntry> licencesUsagesForClient = fClient2LicencesUsageList.get(m.getClientId());

			if (licencesUsagesForClient == null) {
				licencesUsagesForClient = new ArrayList<>();
				fClient2LicencesUsageList.put(m.getClientId(), licencesUsagesForClient);
			}
			licencesUsagesForClient.add(m);
		}
	}

	// retrieves the used software licence - or tries to reserve one - for the given
	// host and licence pool
	@Override
	public String getLicenceUsage(String hostId, String licensePoolId) {
		String result = null;
		Map<String, Object> resultMap = null;

		if (withLicenceManagement) {
			OpsiMethodCall omc0 = new OpsiMethodCall("getOrCreateSoftwareLicenseUsage_hash",
					new String[] { hostId, licensePoolId });

			resultMap = exec.getMapResult(omc0);

			if (!resultMap.isEmpty()) {
				result = Globals.pseudokey(new String[] { "" + resultMap.get(HWAuditClientEntry.HOST_KEY),
						"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	@Override
	public String editLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey,
			String notes) {
		if (!serverFullPermission) {
			return null;
		}

		String result = null;
		Map<String, Object> resultMap = null;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("setSoftwareLicenseUsage",
					new String[] { hostId, licensePoolId, softwareLicenseId, licenseKey, notes });

			resultMap = exec.getMapResult(omc);

			if (!resultMap.isEmpty()) {
				result = Globals.pseudokey(new String[] { "" + resultMap.get(HWAuditClientEntry.HOST_KEY),
						"" + resultMap.get("softwareLicenseId"), "" + resultMap.get("licensePoolId") });
			}
		}

		return result;
	}

	@Override
	public void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (itemsDeletionLicenceUsage == null) {
			itemsDeletionLicenceUsage = new ArrayList<>();
		}

		addDeletionLicenceUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenceUsage);
	}

	protected void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId,
			List<LicenceUsageEntry> deletionItems) {
		if (deletionItems == null) {
			return;
		}

		if (!serverFullPermission) {
			return;
		}

		if (!withLicenceManagement) {
			return;
		}

		LicenceUsageEntry deletionItem = new LicenceUsageEntry(hostId, softwareLicenseId, licensePoolId, "", "");
		deletionItems.add(deletionItem);
	}

	@Override
	public boolean executeCollectedDeletionsLicenceUsage() {
		Logging.info(this, "executeCollectedDeletionsLicenceUsage itemsDeletionLicenceUsage == null "
				+ (itemsDeletionLicenceUsage == null));
		if (itemsDeletionLicenceUsage == null) {
			return true;
		}

		if (!serverFullPermission) {
			return false;
		}

		if (!withLicenceManagement) {
			return false;
		}

		boolean result = false;

		List<Object> jsonPreparedList = new ArrayList<>();
		for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
			jsonPreparedList.add(AbstractExecutioner.jsonMap(item.getNOMobject()));
		}

		OpsiMethodCall omc = new OpsiMethodCall("licenseOnClient_deleteObjects",
				new Object[] { AbstractExecutioner.jsonArray(jsonPreparedList) });

		result = exec.doCall(omc);

		if (result) {
			for (LicenceUsageEntry item : itemsDeletionLicenceUsage) {
				String key = item.getPseudoKey();
				String hostX = item.getClientId();

				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostX).remove(rowmap);

				Logging.debug(this,
						"deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostX));
			}
		}

		itemsDeletionLicenceUsage.clear();

		return result;
	}

	@Override
	public boolean deleteLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId) {
		if (!serverFullPermission) {
			return false;
		}

		boolean result = false;

		if (withLicenceManagement) {
			OpsiMethodCall omc = new OpsiMethodCall("deleteSoftwareLicenseUsage",
					new String[] { hostId, softwareLicenseId, licensePoolId });

			result = exec.doCall(omc);

			if (result) {
				String key = LicenceUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostId).remove(rowmap);
			}

			Logging.info(this,
					"deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostId));
		}

		return result;
	}

	@Override
	public void reconciliationInfoRequestRefresh() {
		Logging.info(this, "reconciliationInfoRequestRefresh");
		rowsLicencesReconciliation = null;
		Logging.info(this, "reconciliationInfoRequestRefresh installedSoftwareInformationRequestRefresh()");
		dataStub.installedSoftwareInformationRequestRefresh();

		relationsAuditSoftwareToLicencePools = null;

		dataStub.softwareAuditOnClientsRequestRefresh();
		dataStub.licencepoolsRequestRefresh();
		dataStub.licencesRequestRefresh();
		dataStub.licenceUsabilitiesRequestRefresh();
		dataStub.licenceUsagesRequestRefresh();
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	@Override
	public Map<String, Map<String, Object>> getLicencesReconciliation() {
		getLicenceStatistics();
		return rowsLicencesReconciliation;
	}

	@Override
	public String editLicencesReconciliation(String clientId, String licensePoolId) {
		return "";
	}

	@Override
	public boolean deleteLicencesReconciliation(String clientId, String licensePoolId) {
		return false;
	}

	private List<String> produceProductOnClientDisplayfieldsLocalboot() {
		if (globalReadOnly) {
			return null;
		}

		List<String> result = getDefaultValuesProductOnClientDisplayFields();

		List<String> possibleValues = getPossibleValuesProductOnClientDisplayFields();

		// create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT);
		item.put("description", "");
		item.put("defaultValues", AbstractExecutioner.jsonArray(result));
		item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		Logging.info(this, "produceProductOnClientDisplayfields_localboot");

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonMap(item) });

		exec.doCall(omc);

		return result;
	}

	@Override
	public void addRoleConfig(String name, String rolename) {
		String configkey = UserConfig.KEY_USER_ROLE_ROOT + ".{" + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	@Override
	public void addUserConfig(String name, String rolename) {
		String configkey = UserConfig.START_USER_KEY + name + "}." + UserConfig.HAS_ROLE_ATTRIBUT;
		addRoleAndUserConfig(configkey, rolename);
	}

	private void addRoleAndUserConfig(String configkey, String rolename) {
		List<Object> readyObjects = new ArrayList<>();
		String role = rolename;

		if (role == null) {
			role = UserConfig.NONE_PROTOTYPE;
		}

		List<Object> selectedValuesRole = new ArrayList<>();
		selectedValuesRole.add(role);

		Map<String, Object> itemRole = AbstractPersistenceController.createJSONConfig(ConfigOption.TYPE.UnicodeConfig,
				configkey, "which role should determine this configuration", false, false, selectedValuesRole,
				selectedValuesRole);

		readyObjects.add(AbstractExecutioner.jsonMap(itemRole));

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		exec.doCall(omc);

		configDefaultValues.put(configkey, selectedValuesRole);
	}

	@Override
	public void userConfigurationRequestReload() {
		Logging.info(this, "userConfigurationRequestReload");
		keyUserRegisterValue = null;
	}

	private final boolean isUserRegisterActivated() {
		boolean result = false;

		Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();
		// dont do anything if we have not got the config
		if (serverPropertyMap.get(KEY_USER_REGISTER) != null && !serverPropertyMap.get(KEY_USER_REGISTER).isEmpty()) {
			result = (Boolean) ((List) (serverPropertyMap.get(KEY_USER_REGISTER))).get(0);
		}
		return result;
	}

	private final boolean checkUserRolesModule() {
		if (Boolean.TRUE.equals(keyUserRegisterValue) && !withUserRoles) {
			keyUserRegisterValue = false;

			SwingUtilities.invokeLater(() -> {
				StringBuilder info = new StringBuilder();
				info.append(Configed.getResourceValue("Permission.modules.missing_user_roles") + "\n");
				info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.1") + "\n");
				info.append(Configed.getResourceValue("Permission.modules.missing_user_roles.2") + "\n");
				info.append(
						KEY_USER_REGISTER + " " + Configed.getResourceValue("Permission.modules.missing_user_roles.3"));
				info.append("\n");

				Logging.warning(this,
						" user role administration configured but not permitted by the modules file " + info);

				FOpsiLicenseMissingText.callInstanceWith(info.toString());
			});
		}

		return keyUserRegisterValue;
	}

	// configurations and algorithms sets KEY_USER_REGISTER_VALUE; should not be
	// overwritten to avoid privileges confusion.
	protected final boolean applyUserConfiguration() {
		// do it only once

		if (keyUserRegisterValue == null) {
			keyUserRegisterValue = isUserRegisterActivated();

			if (Boolean.TRUE.equals(keyUserRegisterValue)) {
				keyUserRegisterValue = checkUserRolesModule();
			}
		}

		Logging.info(this, "applyUserConfiguration result " + keyUserRegisterValue);

		return keyUserRegisterValue;
	}

	@Override
	public Map<String, Boolean> getProductOnClientsDisplayFieldsLocalbootProducts() {
		if (productOnClientsDisplayFieldsLocalbootProducts == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			Logging.debug(this,
					"getProductOnClients_displayFieldsLocalbootProducts()  configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT "
							+ configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT));

			List<String> configuredByService = Globals
					.takeAsStringList(serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT));

			List<String> possibleValuesAccordingToService = new ArrayList<>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT) != null) {
				possibleValuesAccordingToService = (List<String>) configOptions
						.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT).get("possibleValues");
			}

			Logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() possibleValuesAccordingToService "
					+ possibleValuesAccordingToService);

			if (configuredByService.isEmpty() || !((new HashSet<>(getPossibleValuesProductOnClientDisplayFields()))
					.equals(new HashSet<>(possibleValuesAccordingToService)))) {
				// we did not initialize server property
				configuredByService = produceProductOnClientDisplayfieldsLocalboot();
			}

			productOnClientsDisplayFieldsLocalbootProducts = new LinkedHashMap<>();

			if (configuredByService == null) {
				Logging.warning(this, "configuredByService is null");
				return productOnClientsDisplayFieldsLocalbootProducts;
			}

			// key names from ProductState
			productOnClientsDisplayFieldsLocalbootProducts.put("productId", true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_PRODUCT_NAME,
					configuredByService.indexOf(ProductState.KEY_PRODUCT_NAME) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_TARGET_CONFIGURATION,
					configuredByService.indexOf(ProductState.KEY_TARGET_CONFIGURATION) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_INSTALLATION_STATUS, true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_INSTALLATION_INFO,
					configuredByService.indexOf(ProductState.KEY_INSTALLATION_INFO) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_ACTION_REQUEST, true);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_PRODUCT_PRIORITY,
					configuredByService.indexOf(ProductState.KEY_PRODUCT_PRIORITY) > -1);
			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_POSITION,
					configuredByService.indexOf(ProductState.KEY_POSITION) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_LAST_STATE_CHANGE,
					configuredByService.indexOf(ProductState.KEY_LAST_STATE_CHANGE) > -1);

			productOnClientsDisplayFieldsLocalbootProducts.put(ProductState.KEY_VERSION_INFO, true);

		}

		return productOnClientsDisplayFieldsLocalbootProducts;
	}

	@Override
	public void deleteSavedSearch(String name) {
		Logging.debug(this, "deleteSavedSearch " + name);

		List<Object> readyObjects = new ArrayList<>();
		Map<String, Object> item;

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name);
		readyObjects.add(AbstractExecutioner.jsonMap(item));

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name + "." + SavedSearch.DESCRIPTION_KEY);
		readyObjects.add(AbstractExecutioner.jsonMap(item));

		OpsiMethodCall omc = new OpsiMethodCall("config_deleteObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		exec.doCall(omc);
		savedSearches.remove(name);
	}

	@Override
	public void saveSearch(SavedSearch ob) {
		Logging.debug(this, "saveSearch " + ob);

		List<Object> readyObjects = new ArrayList<>();
		// entry of serialization string
		readyObjects.add(produceConfigEntry("UnicodeConfig", SavedSearch.CONFIG_KEY + "." + ob.getName(),
				ob.getSerialization(), ob.getDescription(), false));
		// description entry
		readyObjects.add(produceConfigEntry("UnicodeConfig",
				SavedSearch.CONFIG_KEY + "." + ob.getName() + "." + SavedSearch.DESCRIPTION_KEY, ob.getDescription(),
				"", true));

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

		exec.doCall(omc);
	}

	private List<String> getPossibleValuesProductOnClientDisplayFields() {
		List<String> possibleValues = new ArrayList<>();
		possibleValues.add("productId");
		possibleValues.add(ProductState.KEY_PRODUCT_NAME);
		possibleValues.add(ProductState.KEY_INSTALLATION_STATUS);
		possibleValues.add(ProductState.KEY_INSTALLATION_INFO);
		possibleValues.add(ProductState.KEY_ACTION_REQUEST);
		possibleValues.add(ProductState.KEY_PRODUCT_PRIORITY);
		possibleValues.add(ProductState.KEY_POSITION);
		possibleValues.add(ProductState.KEY_LAST_STATE_CHANGE);
		possibleValues.add(ProductState.KEY_TARGET_CONFIGURATION);
		possibleValues.add(ProductState.KEY_VERSION_INFO);

		return possibleValues;
	}

	private List<String> getDefaultValuesProductOnClientDisplayFields() {
		List<String> result = new ArrayList<>();

		result.add("productId");

		result.add(ProductState.KEY_INSTALLATION_STATUS);
		result.add(ProductState.KEY_INSTALLATION_INFO);
		result.add(ProductState.KEY_ACTION_REQUEST);
		result.add(ProductState.KEY_VERSION_INFO);

		return result;
	}

	private List<String> produceProductOnClientDisplayfieldsNetboot() {
		List<String> result = getDefaultValuesProductOnClientDisplayFields();
		List<String> possibleValues = getPossibleValuesProductOnClientDisplayFields();

		// create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT);
		item.put("description", "");
		item.put("defaultValues", AbstractExecutioner.jsonArray(result));
		item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		Logging.info(this, "produceProductOnClientDisplayfields_netboot");

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonMap(item) });

		exec.doCall(omc);

		return result;
	}

	@Override
	public Map<String, Boolean> getProductOnClientsDisplayFieldsNetbootProducts() {
		if (productOnClientsDisplayFieldsNetbootProducts == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = Globals
					.takeAsStringList(serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT));

			List<String> possibleValuesAccordingToService = new ArrayList<>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT) != null) {
				possibleValuesAccordingToService = (List<String>) configOptions
						.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT).get("possibleValues");
			}

			if (configuredByService.isEmpty() || !((new HashSet<>(getPossibleValuesProductOnClientDisplayFields()))
					.equals(new HashSet<>(possibleValuesAccordingToService)))) {
				// we did not initialize server property
				configuredByService = produceProductOnClientDisplayfieldsNetboot();
			}

			productOnClientsDisplayFieldsNetbootProducts = new LinkedHashMap<>();

			// key names from ProductState
			productOnClientsDisplayFieldsNetbootProducts.put("productId", true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_PRODUCT_NAME,
					(configuredByService.indexOf(ProductState.KEY_PRODUCT_NAME) > -1));

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_TARGET_CONFIGURATION, false);
			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_INSTALLATION_STATUS, true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_INSTALLATION_INFO,
					(configuredByService.indexOf(ProductState.KEY_INSTALLATION_INFO) > -1));

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_ACTION_REQUEST, true);

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_LAST_STATE_CHANGE,
					(configuredByService.indexOf(ProductState.KEY_LAST_STATE_CHANGE) > -1));

			productOnClientsDisplayFieldsNetbootProducts.put(ProductState.KEY_VERSION_INFO, true);
		}

		return productOnClientsDisplayFieldsNetbootProducts;
	}

	private List<String> produceHostDisplayFields(List<String> givenList) {
		List<String> result = null;
		Logging.info(this,
				"produceHost_displayFields configOptions.get(key) " + configOptions.get(KEY_HOST_DISPLAYFIELDS));

		List<String> possibleValues = new ArrayList<>();
		possibleValues.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_SESSION_INFO_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_WAN_CONFIG_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_SYSTEM_UUID_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_MAC_ADDRESS_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_INVENTORY_NUMBER_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_UEFI_BOOT_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CLIENT_INSTALL_BY_SHUTDOWN_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.CREATED_DISPLAY_FIELD_LABEL);
		possibleValues.add(HostInfo.DEPOT_OF_CLIENT_DISPLAY_FIELD_LABEL);

		List<String> defaultValues = new ArrayList<>();
		defaultValues.add(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_DESCRIPTION_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_CONNECTED_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.LAST_SEEN_DISPLAY_FIELD_LABEL);
		defaultValues.add(HostInfo.CLIENT_IP_ADDRESS_DISPLAY_FIELD_LABEL);

		if (givenList == null || givenList.isEmpty()) {
			result = defaultValues;
		} else {
			result = givenList;
			// but not if we want to change the default values:
		}

		// create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_HOST_DISPLAYFIELDS);
		item.put("description", "");
		item.put("defaultValues", AbstractExecutioner.jsonArray(result));
		item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
				new Object[] { AbstractExecutioner.jsonMap(item) });

		exec.doCall(omc);

		return result;
	}

	@Override
	public Map<String, Boolean> getHostDisplayFields() {
		if (hostDisplayFields == null) {
			Map<String, List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = Globals.takeAsStringList(serverPropertyMap.get(KEY_HOST_DISPLAYFIELDS));

			// check if have to initialize the server property
			configuredByService = produceHostDisplayFields(configuredByService);

			hostDisplayFields = new LinkedHashMap<>();
			hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
			// always shown, we put it here because of ordering and repeat the statement
			// after the loop if it has been set to false

			for (String field : HostInfo.ORDERING_DISPLAY_FIELDS) {
				hostDisplayFields.put(field, (configuredByService.indexOf(field) > -1));
			}

			hostDisplayFields.put(HostInfo.HOST_NAME_DISPLAY_FIELD_LABEL, true);
		}

		return hostDisplayFields;
	}

	@Override
	public List<String> getDisabledClientMenuEntries() {
		getConfigOptions();
		return Globals.takeAsStringList(configDefaultValues.get(KEY_DISABLED_CLIENT_ACTIONS));
	}

	@Override
	public List<String> getOpsiclientdExtraEvents() {
		Logging.debug(this, "getOpsiclientdExtraEvents");
		getConfigOptions();
		if (configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS) == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		List<String> result = Globals.takeAsStringList(configDefaultValues.get(KEY_OPSICLIENTD_EXTRA_EVENTS));
		Logging.debug(this, "getOpsiclientdExtraEvents() " + result);
		return result;
	}

	private Object produceConfigEntry(String nomType, String key, Object value, String description) {
		return produceConfigEntry(nomType, key, value, description, true);
	}

	private Object produceConfigEntry(String nomType, String key, Object value, String description, boolean editable) {
		List<Object> possibleValues = new ArrayList<>();
		possibleValues.add(value);

		// defaultValues
		List<Object> defaultValues = new ArrayList<>();
		defaultValues.add(value);

		// create config for service
		Map<String, Object> item;

		item = createNOMitem(nomType);
		item.put("ident", key);
		item.put("description", description);
		item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
		item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
		item.put("editable", editable);
		item.put("multiValue", false);

		return AbstractExecutioner.jsonMap(item);
	}

	private boolean checkStandardConfigs() {
		boolean result = (getConfigOptions() != null);
		Logging.info(this, "checkStandardConfigs, already there " + result);

		if (!result) {
			return false;
		}

		List<Object> defaultValues;
		List<Object> possibleValues;
		Map<String, Object> item;
		String key;
		List<Object> readyObjects = new ArrayList<>();

		// list of domains for new clients
		key = CONFIGED_GIVEN_DOMAINS_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(getOpsiDefaultDomain());

			possibleValues = new ArrayList<>();
			possibleValues.add(getOpsiDefaultDomain());

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(AbstractExecutioner.jsonMap(item));

			configDefaultValues.put(key, defaultValues);
		}

		// search by sql if possible
		key = KEY_SEARCH_BY_SQL;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = createJSONBoolConfig(key, DEFAULTVALUE_SEARCH_BY_SQL,
					"Use SQL calls for search if SQL backend is active");
			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		// global value for install_by_shutdown

		key = KEY_CLIENTCONFIG_INSTALL_BY_SHUTDOWN;

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			item = createJSONBoolConfig(key, DEFAULTVALUE_CLIENTCONFIG_INSTALL_BY_SHUTDOWN,
					"Use install by shutdown if possible");
			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		// product_sort_algorithm

		key = KEY_PRODUCT_SORT_ALGORITHM;
		// defaultValues
		defaultValues = configDefaultValues.get(key);
		Logging.info(this, "checkStandardConfigs:  from server product_sort_algorithm " + defaultValues);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			defaultValues = new ArrayList<>();
			defaultValues.add("algorithm1");

			possibleValues = new ArrayList<>();
			possibleValues.add("algorithm1");
			possibleValues.add("algorithm2");

			// create config for service
			item = createNOMitem("UnicodeConfig");
			item.put("ident", key);
			item.put("description", "algorithm1 = dependencies first; algorithm2 = priorities first");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));

			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", false);
			item.put("multiValue", false);

			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		// extra display fields in licencing

		key = KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION;

		// defaultValues

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			// key not yet configured
			defaultValues = new ArrayList<>();
			// example for standard configuration other than empty
			// extra columns for licence management, page licences reconciliation
			possibleValues = new ArrayList<>();
			possibleValues.add("description");
			possibleValues.add("inventoryNumber");
			possibleValues.add("notes");
			possibleValues.add("ipAddress");
			possibleValues.add("lastSeen");

			// create config for service
			item = createNOMitem("UnicodeConfig");
			item.put("ident", key);
			item.put("description",
					Configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation.ExtraHostFields"));
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));

			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		// remote controls
		String command;
		String description;

		// ping_linux
		key = RemoteControl.CONFIG_KEY + "." + "ping_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "xterm +hold -e ping %host%";
			description = "ping, started in a Linux environment";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// ping_windows
		key = RemoteControl.CONFIG_KEY + "." + "ping_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start ping %host%";
			description = "ping, started in a Windows terminal";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, true,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, linux
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_linux";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "firefox https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called from a Linux environment, firefox recommended";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// connect to opsiclientd timeline, windows
		key = RemoteControl.CONFIG_KEY + "." + "opsiclientd_timeline_windows";

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			command = "cmd.exe /c start https://%host%:4441/info.html";
			description = "opsiclientd  timeline, called rfrom a Windows environment";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, command, description));
			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + RemoteControl.EDITABLE_KEY, false,
					"(command may not be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + RemoteControl.DESCRIPTION_KEY, description, ""));
		}

		// additional queries
		String query;
		StringBuilder qbuf;
		key = AdditionalQuery.CONFIG_KEY + "." + "hosts_with_products";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			qbuf = new StringBuilder("select");
			qbuf.append(" hostId, productId, installationStatus from ");
			qbuf.append(" HOST, PRODUCT_ON_CLIENT ");
			qbuf.append(" WHERE HOST.hostId  = PRODUCT_ON_CLIENT.clientId ");
			qbuf.append(" AND =  installationStatus='installed' ");
			qbuf.append(" order by hostId, productId ");

			query = qbuf.toString();
			description = "all hosts and their installed products";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, query, description));

			readyObjects.add(produceConfigEntry("BoolConfig", key + "." + AdditionalQuery.EDITABLE_KEY, false,
					"(command may be edited)"));
			// description entry
			readyObjects.add(
					produceConfigEntry("UnicodeConfig", key + "." + AdditionalQuery.DESCRIPTION_KEY, description, ""));
		}

		// WAN_CONFIGURATION
		// does it exist?

		Map<String, ConfigOption> wanConfigOptions = getWANConfigOptions();
		if (wanConfigOptions == null || wanConfigOptions.isEmpty()) {
			Logging.info(this, "build default wanConfigOptions");
			readyObjects = buildWANConfigOptions(readyObjects);
		}

		// saved searches

		key = SavedSearch.CONFIG_KEY + "." + "product_failed";

		defaultValues = configDefaultValues.get(key);

		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);

			StringBuilder val = new StringBuilder();
			val.append("{ \"version\" : \"2\", ");
			val.append("\"data\" : {");
			val.append(" \"element\" : null, ");
			val.append(" \"elementPath\" : null,");
			val.append(" \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, ");
			val.append(
					" \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] ");
			val.append("} }");

			String value = val.toString();

			description = "any product failed";

			readyObjects.add(produceConfigEntry("UnicodeConfig", key, value, description));

			// description entry
			readyObjects
					.add(produceConfigEntry("UnicodeConfig", key + "." + SavedSearch.DESCRIPTION_KEY, description, ""));
		}

		// configuration of host menus

		key = KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_DISABLED_CLIENT_ACTIONS);
			// key not yet configured
			defaultValues = new ArrayList<>();
			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();
			possibleValues.add(MainFrame.ITEM_ADD_CLIENT);
			possibleValues.add(MainFrame.ITEM_DELETE_CLIENT);
			possibleValues.add(MainFrame.ITEM_FREE_LICENCES);

			item = createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));

			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", false);
			item.put("multiValue", true);

			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		key = KEY_SSH_DEFAULTWINUSER;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINUSER);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, KEY_SSH_DEFAULTWINUSER_DEFAULT_VALUE,
					"default windows username for deploy-client-agent-script"));
		}

		key = KEY_SSH_DEFAULTWINPW;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINPW);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, KEY_SSH_DEFAULTWINPW_DEFAULT_VALUE,
					"default windows password for deploy-client-agent-script"));
		}

		key = CONFIGED_WORKBENCH_KEY;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add(produceConfigEntry("UnicodeConfig", key, configedWorkbenchDefaultValue,
					"default path to opsiproducts"));
		} else {
			Logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to " + (String) defaultValues.get(0));
			configedWorkbenchDefaultValue = (String) defaultValues.get(0);
		}

		// configuration of opsiclientd extra events

		key = KEY_OPSICLIENTD_EXTRA_EVENTS;

		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.warning(this,
					"checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
			// key not yet configured
			defaultValues = new ArrayList<>();

			defaultValues.add(OPSI_CLIENTD_EVENT_ON_DEMAND);

			configDefaultValues.put(key, defaultValues);

			possibleValues = new ArrayList<>();

			possibleValues.add(OPSI_CLIENTD_EVENT_ON_DEMAND);
			possibleValues.add(OPSI_CLIENTD_EVENT_SILENT_INSTALL);

			item = createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));

			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(AbstractExecutioner.jsonMap(item));
		}

		// for warnings for opsi licences 

		// percentage number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_PERCENT_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(AbstractExecutioner.jsonMap(item));

			configDefaultValues.put(key, defaultValues);
		}

		// absolute number of clients
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_ABSOLUTE_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(AbstractExecutioner.jsonMap(item));

			configDefaultValues.put(key, defaultValues);
		}

		// days limit warning
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();
			defaultValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			possibleValues = new ArrayList<>();
			possibleValues.add(LicensingInfoMap.CLIENT_LIMIT_WARNING_DAYS_DEFAULT);

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", false);

			readyObjects.add(AbstractExecutioner.jsonMap(item));

			configDefaultValues.put(key, defaultValues);
		}

		// modules disabled for warnings
		key = LicensingInfoMap.CONFIG_KEY + "." + LicensingInfoMap.DISABLE_WARNING_FOR_MODULES;
		defaultValues = configDefaultValues.get(key);
		if (defaultValues == null) {
			Logging.info(this, "checkStandardConfigs: create domain list");

			item = createNOMitem("UnicodeConfig");

			defaultValues = new ArrayList<>();

			possibleValues = new ArrayList<>();

			item.put("ident", key);
			item.put("description", "saved domains for creating clients");
			item.put("defaultValues", AbstractExecutioner.jsonArray(defaultValues));
			item.put("possibleValues", AbstractExecutioner.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", true);

			readyObjects.add(AbstractExecutioner.jsonMap(item));

			configDefaultValues.put(key, defaultValues);
		}

		// add metaconfigs

		// Update configs if there are some to update
		if (!readyObjects.isEmpty()) {
			Logging.notice(this, "There are " + readyObjects.size() + "configurations to update, so we do this now:");

			OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects",
					new Object[] { AbstractExecutioner.jsonArray(readyObjects) });

			exec.doCall(omc);
		} else {
			Logging.notice(this, "there are no configurations to update");
		}

		List<JSONObject> defaultUserConfigsObsolete = new ArrayList<>();

		// delete obsolete configs

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(ALL_USER_KEY_START + "ssh")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "handling ssh config key at old location " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(AbstractExecutioner.jsonMap(config));
				}
			}
		}

		for (Entry<String, List<Object>> configEntry : configDefaultValues.entrySet()) {
			if (configEntry.getKey().startsWith(ALL_USER_KEY_START + "{ole.")) {
				defaultValues = configEntry.getValue();

				if (defaultValues != null) {
					// still existing
					Logging.info(this, "removing unwillingly generated entry  " + configEntry.getKey());
					Map<String, Object> config = new HashMap<>();

					config.put("id", configEntry.getKey());

					String type = "BoolConfig";
					config.put("type", type);

					defaultUserConfigsObsolete.add(AbstractExecutioner.jsonMap(config));
				}
			}
		}

		Logging.info(this, "defaultUserConfigsObsolete " + defaultUserConfigsObsolete);

		if (!defaultUserConfigsObsolete.isEmpty()) {
			exec.doCall(new OpsiMethodCall("config_deleteObjects",
					new Object[] { AbstractExecutioner.jsonArray(defaultUserConfigsObsolete) }));
		}

		return true;
	}

	protected ExtendedInteger calculateModulePermission(ExtendedInteger globalMaxClients,
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

	// opsi module information
	@Override
	public void opsiInformationRequestRefresh() {
		opsiInformation = new HashMap<>();
	}

	@Override
	public Map<String, Object> getOpsiModulesInfos() {
		retrieveOpsiModules();
		return opsiModulesDisplayInfo;
	}

	@Override
	public String getOpsiLicencingInfoVersion() {
		retrieveOpsiLicensingInfoVersion();
		return opsiLicensingInfoVersion;
	}

	protected void retrieveOpsiLicensingInfoVersion() {
		if (opsiLicensingInfoVersion == null) {
			Logging.info(this, "retrieveOpsiLicensingInfoVersion getMethodSignature( backend_getLicensingInfo "
					+ getMethodSignature(BACKEND_LICENSING_INFO_METHOD_NAME));

			if (getMethodSignature(BACKEND_LICENSING_INFO_METHOD_NAME) == NONE_LIST) {
				Logging.info(this,
						"method " + BACKEND_LICENSING_INFO_METHOD_NAME + " not existing in this opsi service");
				opsiLicensingInfoVersion = LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD;
			} else {
				opsiLicensingInfoVersion = LicensingInfoMap.OPSI_LICENSING_INFO_VERSION;
			}
		}

	}

	@Override
	public final void opsiLicencingInfoRequestRefresh() {
		licencingInfo = null;
		licInfoMap = null;
		LicensingInfoMap.requestRefresh();
		Logging.info(this, "request worked");
	}

	// is not allowed to be overriden in order to prevent changes
	@Override
	public final JSONObject getOpsiLicencingInfo() {
		if (licencingInfo == null) {
			return retrieveJSONLicensingInfoReduced();
		}

		return licencingInfo;
	}

	private JSONObject retrieveJSONLicensingInfoReduced() {
		retrieveOpsiLicensingInfoVersion();
		if (licencingInfo == null
				&& !opsiLicensingInfoVersion.equals(LicensingInfoMap.OPSI_LICENSING_INFO_VERSION_OLD)) {
			OpsiMethodCall omc = new OpsiMethodCall(BACKEND_LICENSING_INFO_METHOD_NAME,
					new Object[] { true, false, true, false });

			licencingInfo = exec.retrieveJSONObject(omc);
		}

		return licencingInfo;
	}

	@Override
	public String getCustomer() {
		retrieveOpsiModules();
		return (String) opsiModulesDisplayInfo.get("customer");
	}

	private Map<String, Object> produceOpsiInformation() {
		if (!opsiInformation.isEmpty()) {
			// we are initialized
			return opsiInformation;
		}

		OpsiMethodCall omc = new OpsiMethodCall("backend_info", new String[] {});
		opsiInformation = new HashMap<>();

		// method does not exist before opsi 3.4
		if (getMethodSignature("backend_info") != NONE_LIST) {
			opsiInformation = exec.getMapResult(omc);
		}

		opsiVersion = "4";

		if (!opsiInformation.isEmpty()) {
			String value = (String) opsiInformation.get("opsiVersion");
			if (value != null) {
				opsiVersion = value;
			}
		}

		return opsiInformation;
	}

	private void produceOpsiModulesInfo() {
		produceOpsiInformation();

		// has the actual signal if a module is activ
		opsiModules = new HashMap<>();

		// opsiinformation which delivers the service information on checked modules

		// displaying to the user

		getHostInfoCollections().retrieveOpsiHosts(); // for checking number of clients and config states
		Logging.info(this, "getOverLimitModuleList() " + LicensingInfoMap
				.getInstance(getOpsiLicencingInfo(), getConfigDefaultValues(), true).getCurrentOverLimitModuleList());

		licInfoWarnings = null;

		licInfoMap = LicensingInfoMap.getInstance(getOpsiLicencingInfo(), getConfigDefaultValues(),
				!FGeneralDialogLicensingInfo.extendedView);

		List<String> availableModules = licInfoMap.getAvailableModules();

		for (String mod : licInfoMap.getModules()) {
			opsiModules.put(mod, availableModules.indexOf(mod) != -1);
		}

		Logging.info(this, "opsiModules result " + opsiModules);

		withLinuxAgent = (opsiModules.get("linux_agent") != null) && (opsiModules.get("linux_agent"));
		withLicenceManagement = (opsiModules.get("license_management") != null)
				&& (opsiModules.get("license_management"));
		withLocalImaging = (opsiModules.get("local_imaging") != null) && (opsiModules.get("local_imaging"));

		withMySQL = (opsiModules.get("mysql_backend") != null) && opsiModules.get("mysql_backend") && canCallMySQL();
		withUEFI = (opsiModules.get("uefi") != null) && (opsiModules.get("uefi"));
		withWAN = (opsiModules.get("vpn") != null) && (opsiModules.get("vpn"));
		withUserRoles = (opsiModules.get("userroles") != null) && (opsiModules.get("userroles"));

		Logging.info(this, "produceOpsiModulesInfo withUserRoles " + withUserRoles);
		Logging.info(this, "produceOpsiModulesInfo withUEFI " + withUEFI);
		Logging.info(this, "produceOpsiModulesInfo withWAN " + withWAN);
		Logging.info(this, "produceOpsiModulesInfo withLicenceManagement " + withLicenceManagement);
		Logging.info(this, "produceOpsiModulesInfo withMySQL " + withMySQL);

		// sets value to true if we use the mysql backend and informs that we are
		// underlicensed
	}

	private void produceOpsiModulesInfoClassic() {
		produceOpsiInformation();

		// the part of it which delivers the service information on checked
		// modules
		opsiModulesInfo = new HashMap<>();
		// keeps the info for displaying to the user
		opsiModulesDisplayInfo = new HashMap<>();
		opsiVersion = "4";
		HashMap<String, ModulePermissionValue> opsiModulesPermissions = new HashMap<>();
		// has the actual signal if a module is active
		opsiModules = new HashMap<>();

		Map<String, Object> opsiCountModules = new HashMap<>();
		String expiresKey = ModulePermissionValue.KEY_EXPIRES;

		try {
			opsiVersion = (String) opsiInformation.get("opsiVersion");
			Logging.info(this, "opsi version information " + opsiVersion);

			final List<String> missingModulesPermissionInfo = new ArrayList<>();

			// prepare the user info
			opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));

			opsiModulesInfo.remove("signature");
			Logging.info(this, "opsi module information " + opsiModulesInfo);
			opsiModulesInfo.remove("valid");

			opsiModulesDisplayInfo = new HashMap<>(opsiModulesInfo);

			ExtendedDate validUntil = ExtendedDate.INFINITE;
			if (opsiModulesInfo.get(expiresKey) != null) {
				validUntil = new ExtendedDate(opsiModulesInfo.get(expiresKey));
			}

			// analyse the real module info
			opsiCountModules = exec.getMapFromItem(opsiInformation.get("realmodules"));
			getHostInfoCollections().retrieveOpsiHosts();

			ExtendedInteger globalMaxClients = ExtendedInteger.INFINITE;

			int countClients = hostInfoCollections.getCountClients();

			Date today = new Date();
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTime(today);

			Logging.info(this, "opsiModulesInfo " + opsiModulesInfo);

			// read in modules
			for (Entry<String, Object> opsiModuleInfo : opsiModulesInfo.entrySet()) {
				Logging.info(this, "module from opsiModulesInfo, key " + opsiModuleInfo.getKey());
				ModulePermissionValue modulePermission = new ModulePermissionValue(exec, opsiModuleInfo.getValue(),
						validUntil);

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

				modulePermission = new ModulePermissionValue(exec, opsiCountModule.getValue(), validUntil);

				Logging.info(this,
						"handle modules key " + opsiCountModule.getKey() + " permission set " + modulePermission);
				// replace value got from modulesInfo
				opsiModulesPermissions.put(opsiCountModule.getKey(), modulePermission);

				if (opsiCountModule.getValue() != null) {
					opsiModulesDisplayInfo.put(opsiCountModule.getKey(), opsiCountModule.getValue());
				}
			}

			Logging.info(this, "modules resulting step 1 " + opsiModules);
			Logging.info(this, "countModules is  " + opsiCountModules);

			// set values for modules checked by configed
			for (String key : ModulePermissionValue.MODULE_CHECKED.keySet()) {
				ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
				ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
				ExtendedDate expiresForThisModule = modulePermission.getExpires();

				if (modulePermission.getBoolean() != null) {
					opsiModules.put(key, modulePermission.getBoolean());
					Logging.info(this, " retrieveOpsiModules, set opsiModules for key " + key + ": "
							+ modulePermission.getBoolean());
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

						globalMaxClients = calculateModulePermission(globalMaxClients,
								maxClientsForThisModule.getNumber());

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

						if (stopLimit != null && hostInfoCollections.getCountClients() > stopLimit) {
							opsiModules.put(key, false);
						} else {
							if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
								Date expiresDate = expiresForThisModule.getDate();

								if (today.after(expiresDate)) {
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
						&& (key.equals("linux_agent") || (key.equals("userroles") && !isUserRegisterActivated()))) {
					problemToIndicate = false;
				}

				Logging.info(this, "check module " + key + "  problemToIndicate " + problemToIndicate);

				if (problemToIndicate) {
					Logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxAllowedClientsForThisModule
							+ " count " + countClientsInThisBlock);

					if (!expiresForThisModule.equals(ExtendedDate.INFINITE)) {
						Date expiresDate = expiresForThisModule.getDate();
						Calendar noticeCal = Calendar.getInstance();
						noticeCal.setTime(expiresDate);
						noticeCal.add(Calendar.DAY_OF_MONTH, -14);

						if (nowCal.after(noticeCal)) {
							missingModulesPermissionInfo.add("Module " + key + ", expires: " + expiresForThisModule);
						}
					}

					if (!ExtendedInteger.INFINITE.equals(maxAllowedClientsForThisModule)) {
						int startWarningCount = maxAllowedClientsForThisModule.getNumber() - CLIENT_COUNT_WARNING_LIMIT;
						int stopCount = maxAllowedClientsForThisModule.getNumber() + CLIENT_COUNT_TOLERANCE_LIMIT;

						if (countClientsInThisBlock > stopCount) {
							Logging.info(this, "retrieveOpsiModules " + key + " stopCount " + stopCount
									+ " count clients " + countClients);

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
						}
					}
				}
			}

			Logging.info(this, "modules resulting  " + opsiModules);
			Logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " + missingModulesPermissionInfo);

			if (!missingModulesPermissionInfo.isEmpty()) {
				SwingUtilities.invokeLater(() -> {
					StringBuilder info = new StringBuilder("");

					info.append(Configed.getResourceValue("Permission.modules.clientcount.2"));
					info.append(":\n");
					for (String moduleInfo : missingModulesPermissionInfo) {
						info.append(moduleInfo);
						info.append("\n");
					}

					Logging.info(this, "missingModules " + info);
					FOpsiLicenseMissingText.callInstanceWith(info.toString());
				});
			}
		} catch (Exception ex) {
			Logging.warning("opsi module information problem", ex);
		}

		withLinuxAgent = (opsiModules.get("linux_agent") != null) && opsiModules.get("linux_agent");
		withLicenceManagement = (opsiModules.get("license_management") != null)
				&& opsiModules.get("license_management");
		withLocalImaging = (opsiModules.get("local_imaging") != null) && opsiModules.get("local_imaging");

		withMySQL = (opsiModules.get("mysql_backend") != null) && opsiModules.get("mysql_backend");
		withUEFI = (opsiModules.get("uefi") != null) && opsiModules.get("uefi");
		withWAN = (opsiModules.get("vpn") != null) && opsiModules.get("vpn");
		withUserRoles = (opsiModules.get("userroles") != null) && opsiModules.get("userroles");

		Logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
		Logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
		Logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
	}

	@Override
	public final void retrieveOpsiModules() {
		Logging.info(this, "retrieveOpsiModules ");

		licencingInfo = getOpsiLicencingInfo();

		// probably old opsi service version
		if (licencingInfo == null) {
			produceOpsiModulesInfoClassic();
		} else {
			produceOpsiModulesInfo();

		}

		Logging.info(this, " withMySQL " + withMySQL);
		Logging.info(this, " withLinuxAgent " + withLinuxAgent);
		Logging.info(this, " withUserRoles " + withUserRoles);
	}

	@Override
	public boolean isWithLocalImaging() {
		retrieveOpsiModules();
		return withLocalImaging;
	}

	@Override
	public boolean isWithMySQL() {
		return withMySQL;
	}

	@Override
	public boolean isWithUEFI() {
		return withUEFI;
	}

	@Override
	public boolean isWithWAN() {
		return withWAN;
	}

	@Override
	public boolean isWithLinuxAgent() {
		return withLinuxAgent;
	}

	@Override
	public boolean isWithLicenceManagement() {
		return withLicenceManagement;
	}

	@Override
	public boolean isWithUserRoles() {
		return withUserRoles;
	}

	@Override
	public boolean applyUserSpecializedConfig() {
		if (applyUserSpecializedConfig != null) {
			return applyUserSpecializedConfig;
		}

		applyUserSpecializedConfig = withUserRoles && keyUserRegisterValue;
		Logging.info(this, "applyUserSpecializedConfig initialized, " + applyUserSpecializedConfig);

		return applyUserSpecializedConfig;
	}

	@Override
	public String getOpsiVersion() {
		retrieveOpsiModules();
		return opsiVersion;
	}

	@Override
	public boolean handleVersionOlderThan(String minRequiredVersion) {
		return Globals.compareOpsiVersions(opsiVersion, minRequiredVersion) < 0;
	}

	/**
	 * Test if sshcommand methods exists
	 * 
	 * @param method name
	 * @return True if exists
	 */
	@Override
	public boolean checkSSHCommandMethod(String method) {
		// method does not exist before opsi 3.4
		if (getMethodSignature(method) != NONE_LIST) {
			Logging.info(this, "checkSSHCommandMethod " + method + " exists");
			return true;
		}
		Logging.info(this, "checkSSHCommandMethod " + method + " does not exists");
		return false;
	}

	@Override
	public List<Map<String, Object>> getOpsiconfdConfigHealth() {
		return retrieveHealthDetails("opsiconfd_config");
	}

	@Override
	public List<Map<String, Object>> getDiskUsageHealth() {
		return retrieveHealthDetails("disk_usage");
	}

	@Override
	public List<Map<String, Object>> getDepotHealth() {
		return retrieveHealthDetails("depotservers");
	}

	@Override
	public List<Map<String, Object>> getSystemPackageHealth() {
		return retrieveHealthDetails("system_packages");
	}

	@Override
	public List<Map<String, Object>> getProductOnDepotsHealth() {
		return retrieveHealthDetails("product_on_depots");
	}

	@Override
	public List<Map<String, Object>> getProductOnClientsHealth() {
		return retrieveHealthDetails("product_on_clients");
	}

	@Override
	public List<Map<String, Object>> getLicenseHealth() {
		return retrieveHealthDetails("opsi_licenses");
	}

	@Override
	public List<Map<String, Object>> getDeprecatedCalls() {
		return retrieveHealthDetails("deprecated_calls");
	}

	private List<Map<String, Object>> retrieveHealthDetails(String checkId) {
		List<Map<String, Object>> result = new ArrayList<>();

		for (Map<String, Object> data : checkHealth()) {
			if (((String) data.get("check_id")).equals(checkId)) {
				result = JSONReMapper.getListOfMaps((JSONArray) data.get("partial_results"));
				break;
			}
		}

		return result;
	}

	@Override
	public List<Map<String, Object>> checkHealth() {
		if (healthData == null) {
			healthData = dataStub.checkHealth();
		}

		return healthData;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_getObjects"
	 * 
	 * @return command objects
	 */
	@Override
	public List<Map<String, Object>> retrieveCommandList() {
		Logging.info(this, "retrieveCommandList ");

		List<Map<String, Object>> sshCommands = exec.getListOfMaps(
				new OpsiMethodCall("SSHCommand_getObjects", new Object[] { /* callAttributes, callFilter */ }));
		Logging.debug(this, "retrieveCommandList commands " + sshCommands);
		return sshCommands;
	}

	/**
	 * Exec a python-opsi command
	 * 
	 * @param method      name
	 * @param jsonObjects to do sth
	 * @return result true if everything is ok
	 */
	@Override
	public boolean doActionSSHCommand(String method, List<Object> jsonObjects) {
		Logging.info(this, "doActionSSHCommand method " + method);
		if (isGlobalReadOnly()) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall(method, new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
		boolean result = exec.doCall(omc);
		Logging.info(this, "doActionSSHCommand method " + method + " result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_deleteObjects"
	 * 
	 * @param jsonObjects to remove
	 * @return result true if successfull
	 */
	@Override
	public boolean deleteSSHCommand(List<String> jsonObjects) {
		// Strings not object!
		Logging.info(this, "deleteSSHCommand ");
		if (isGlobalReadOnly()) {
			return false;
		}
		OpsiMethodCall omc = new OpsiMethodCall("SSHCommand_deleteObjects",
				new Object[] { AbstractExecutioner.jsonArray(jsonObjects) });
		boolean result = exec.doCall(omc);
		Logging.info(this, "deleteSSHCommand result " + result);
		return result;
	}

	/**
	 * Exec the python-opsi command "SSHCommand_createObjects"
	 * 
	 * @param jsonObjects to create
	 * @return result true if successfull
	 */
	@Override
	public boolean createSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand("SSHCommand_createObjects", jsonObjects);
	}

	/**
	 * Exec the python-opsi command "SSHCommand_updateObjects"
	 * 
	 * @param jsonObjects to update
	 * @return result true if successfull
	 */
	@Override
	public boolean updateSSHCommand(List<Object> jsonObjects) {
		return doActionSSHCommand("SSHCommand_updateObjects", jsonObjects);
	}
}
