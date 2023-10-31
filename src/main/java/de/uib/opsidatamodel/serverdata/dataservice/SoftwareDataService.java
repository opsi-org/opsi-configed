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

import javax.swing.JOptionPane;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.configed.type.licences.LicenceStatisticsRow;
import de.uib.configed.type.licences.LicenceUsableForEntry;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsicommand.AbstractExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.datastructure.StringValuedRelationElement;
import de.uib.utilities.logging.Logging;
import utils.Utils;

/**
 * Provides methods for working with software data on the server.
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
public class SoftwareDataService {
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

	private CacheManager cacheManager;
	private AbstractExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ModuleDataService moduleDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private LicenseDataService licenseDataService;
	private HostInfoCollections hostInfoCollections;

	public SoftwareDataService(AbstractExecutioner exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.cacheManager = CacheManager.getInstance();
		this.exec = exec;
		this.persistenceController = persistenceController;
	}

	public void setUserRolesConfigDataService(UserRolesConfigDataService userRolesConfigDataService) {
		this.userRolesConfigDataService = userRolesConfigDataService;
	}

	public void setModuleDataService(ModuleDataService moduleDataService) {
		this.moduleDataService = moduleDataService;
	}

	public void setLicenseDataService(LicenseDataService licenseDataService) {
		this.licenseDataService = licenseDataService;
	}

	public void setHostInfoCollections(HostInfoCollections hostInfoCollections) {
		this.hostInfoCollections = hostInfoCollections;
	}

	public NavigableSet<Object> getSoftwareWithoutAssociatedLicencePoolPD() {
		retrieveRelationsAuditSoftwareToLicencePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL, NavigableSet.class);
	}

	public List<String> getSoftwareListByLicencePoolPD(String licencePoolId) {
		retrieveRelationsAuditSoftwareToLicencePoolsPD();
		Map<String, List<String>> fLicensePool2SoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
		List<String> result = fLicensePool2SoftwareList.get(licencePoolId);
		return result == null ? new ArrayList<>() : result;
	}

	public List<String> getUnknownSoftwareListForLicencePoolPD(String licencePoolId) {
		retrieveRelationsAuditSoftwareToLicencePoolsPD();
		Map<String, List<String>> fLicencePool2UnknownSoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST, Map.class);
		List<String> result = fLicencePool2UnknownSoftwareList.get(licencePoolId);
		return result == null ? new ArrayList<>() : result;
	}

	public Map<String, String> getFSoftware2LicensePoolPD() {
		retrieveRelationsAuditSoftwareToLicencePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
	}

	public String getFSoftware2LicencePoolPD(String softwareIdent) {
		retrieveRelationsAuditSoftwareToLicencePoolsPD();
		Map<String, String> fSoftware2LicencePool = cacheManager
				.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
		return fSoftware2LicencePool.get(softwareIdent);
	}

	public void retrieveRelationsAuditSoftwareToLicencePoolsPD() {
		if (cacheManager.getCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL,
				NavigableSet.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST, Map.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class) != null) {
			return;
		}

		if (!moduleDataService.isWithLicenceManagementPD()) {
			return;
		}

		AuditSoftwareXLicencePool relationsAuditSoftwareToLicencePools = getAuditSoftwareXLicencePoolPD();

		if (relationsAuditSoftwareToLicencePools == null) {
			Logging.warning(this, "retrieveRelationsAuditSoftwareToLicencePools is null");
			return;
		}

		// function softwareIdent --> pool
		Map<String, String> fSoftware2LicencePool = new HashMap<>();
		// function pool --> list of assigned software
		Map<String, List<String>> fLicencePool2SoftwareList = new HashMap<>();
		// function pool --> list of assigned software
		Map<String, List<String>> fLicencePool2UnknownSoftwareList = new HashMap<>();

		NavigableSet<String> softwareWithoutAssociatedLicencePool = new TreeSet<>(
				getInstalledSoftwareInformationForLicensingPD().keySet());

		for (StringValuedRelationElement retrieved : relationsAuditSoftwareToLicencePools) {
			SWAuditEntry entry = new SWAuditEntry(retrieved);
			String licencePoolKEY = retrieved.get(LicencepoolEntry.ID_SERVICE_KEY);
			String swKEY = entry.getIdent();

			// build row for software table
			LinkedHashMap<String, String> row = new LinkedHashMap<>();

			for (String colName : SWAuditEntry.getDisplayKeys()) {
				row.put(colName, entry.get(colName));
			}

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
				if (getInstalledSoftwareInformationForLicensingPD().get(swKEY) == null) {
					Logging.warning(this, "license pool " + licencePoolKEY
							+ " is assigned to a not listed software with ID " + swKEY + " data row " + row);
					// we serve the fLicencePool2UnknownSoftwareList only in case that a key is
					// found
					List<String> unknownSoftwareIds = fLicencePool2UnknownSoftwareList.computeIfAbsent(licencePoolKEY,
							s -> new ArrayList<>());
					unknownSoftwareIds.add(swKEY);
				} else {
					softwareIds.add(swKEY);
					softwareWithoutAssociatedLicencePool.remove(swKEY);
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, fSoftware2LicencePool);
		cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, fLicencePool2SoftwareList);
		cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST,
				fLicencePool2UnknownSoftwareList);
		cacheManager.setCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL,
				softwareWithoutAssociatedLicencePool);

		Logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools,  softwareWithoutAssociatedLicencePool "
				+ softwareWithoutAssociatedLicencePool.size());
	}

	public AuditSoftwareXLicencePool getAuditSoftwareXLicencePoolPD() {
		retrieveAuditSoftwareXLicencePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL,
				AuditSoftwareXLicencePool.class);
	}

	public void retrieveAuditSoftwareXLicencePoolPD() {
		if (cacheManager.getCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL,
				AuditSoftwareXLicencePool.class) != null) {
			return;
		}

		Logging.info(this, "retrieveAuditSoftwareXLicencePool");
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_GET_OBJECTS,
				new Object[] { AuditSoftwareXLicencePool.SERVICE_ATTRIBUTES, new HashMap<>() });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = new AuditSoftwareXLicencePool();
		for (Map<String, Object> map : retrieved) {
			auditSoftwareXLicencePool.integrateRaw(map);
		}
		cacheManager.setCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL, auditSoftwareXLicencePool);
		Logging.info(this, "retrieveAuditSoftwareXLicencePool retrieved ");
	}

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformationPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, NavigableMap.class);
	}

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensingPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
				NavigableMap.class);
	}

	public NavigableMap<String, Map<String, String>> getInstalledSoftwareName2SWinfoPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO, NavigableMap.class);
	}

	public NavigableMap<String, Set<String>> getName2SWIdentsPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, NavigableMap.class);
	}

	public List<String> getSoftwareListPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, List.class);
	}

	public NavigableMap<String, Integer> getSoftware2NumberPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER, NavigableMap.class);
	}

	public String getSWident(Integer i) {
		Logging.debug(this, "getSWident for " + i);
		retrieveInstalledSoftwareInformationPD();
		String swIdent = null;
		List<String> softwareList = getSoftwareListPD();
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
				persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
				if (i > -1 && softwareList != null && softwareList.size() >= i + 1) {
					infoFound = true;
				}
			}

			if (!infoFound) {
				Logging.warning(this, "missing softwareList entry " + i + " " + softwareList);
			}
		} else {
			swIdent = softwareList.get(i);
		}
		return swIdent;
	}

	public void retrieveInstalledSoftwareInformationPD() {
		if (cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, List.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER, NavigableMap.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION,
						NavigableMap.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
						NavigableMap.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, NavigableMap.class) != null
				&& cacheManager.getCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO,
						NavigableMap.class) != null) {
			return;
		}

		String[] callAttributes = new String[] { SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME),
				SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION),
				SWAuditEntry.key2serverKey.get(SWAuditEntry.SUB_VERSION),
				SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE),
				SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE),
				SWAuditEntry.key2serverKey.get(SWAuditEntry.WINDOWS_SOFTWARE_ID) };
		Map<String, Object> callFilter = new HashMap<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> li = exec.getListOfMaps(omc);
		Iterator<Map<String, Object>> iter = li.iterator();

		NavigableMap<String, SWAuditEntry> installedSoftwareInformation = new TreeMap<>();
		NavigableMap<String, SWAuditEntry> installedSoftwareInformationForLicensing = new TreeMap<>();
		NavigableMap<String, Set<String>> name2SWIdents = new TreeMap<>();
		NavigableMap<String, Map<String, String>> installedSoftwareName2SWinfo = new TreeMap<>();
		NavigableMap<String, Map<String, Map<String, String>>> name2ident2infoWithPool = new TreeMap<>();

		int i = 0;
		Logging.info(this, "getInstalledSoftwareInformation build map");

		while (iter.hasNext()) {
			i++;
			SWAuditEntry entry = new SWAuditEntry(iter.next());
			String swName = entry.get(SWAuditEntry.NAME);
			String swIdent = entry.getIdent();
			installedSoftwareInformation.put(swIdent, entry);

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

				Map<String, Map<String, String>> ident2infoWithPool = name2ident2infoWithPool.computeIfAbsent(swName,
						s -> new TreeMap<>());
				Map<String, String> infoWithPool = ident2infoWithPool.computeIfAbsent(entry.getIdent(),
						s -> new LinkedHashMap<>());
				infoWithPool.put(SWAuditEntry.ID, entry.getIdent());
				infoWithPool.put(LicencepoolEntry.ID_SERVICE_KEY, "x " + i);
			}
		}

		List<String> softwareList = new ArrayList<>(installedSoftwareInformation.keySet());
		NavigableMap<String, Number> software2Number = new TreeMap<>();
		int n = 0;
		for (String sw : softwareList) {
			if (sw.startsWith("NULL")) {
				Logging.info(this, "retrieveInstalledSoftwareInformation, we get index " + n + " for " + sw);
			}
			software2Number.put(sw, n);
			n++;
		}

		cacheManager.setCachedData(CacheIdentifier.SOFTWARE_LIST, softwareList);
		cacheManager.setCachedData(CacheIdentifier.SOFTWARE_TO_NUMBER, software2Number);
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, installedSoftwareInformation);
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
				installedSoftwareInformationForLicensing);
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO, installedSoftwareName2SWinfo);
		cacheManager.setCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, name2SWIdents);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	public Map<String, List<SWAuditClientEntry>> getSoftwareAuditOnClients(List<String> clients) {
		Map<String, List<SWAuditClientEntry>> client2software = new HashMap<>();
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Utils.usedMemory());
		Logging.info(this, "retrieveSoftwareAuditOnClients clients cound: " + clients.size());

		final int STEP_SIZE = 100;

		while (!clients.isEmpty()) {
			List<String> clientListForCall = new ArrayList<>();

			for (int i = 0; i < STEP_SIZE && i < clients.size(); i++) {
				clientListForCall.add(clients.get(i));
			}

			clients.removeAll(clientListForCall);

			Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("clientId", clientListForCall);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_ON_CLIENT_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> softwareAuditOnClients = exec.getListOfMaps(omc);

			Logging.info(this,
					"retrieveSoftwareAuditOnClients, finished a request, map size " + softwareAuditOnClients.size());

			for (String clientId : clientListForCall) {
				client2software.put(clientId, new LinkedList<>());
			}

			for (Map<String, Object> item : softwareAuditOnClients) {
				SWAuditClientEntry clientEntry = new SWAuditClientEntry(item);
				String clientId = clientEntry.getClientId();

				if (clientId != null) {
					List<SWAuditClientEntry> entries = client2software.get(clientId);
					entries.add(clientEntry);
				}
			}

			Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");
		}

		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());

		return client2software;
	}

	public String getLastSoftwareAuditModification(Map<String, List<SWAuditClientEntry>> entries, String clientId) {
		String result = "";

		if (entries == null || entries.isEmpty()) {
			return result;
		}

		List<SWAuditClientEntry> swAuditClientEntries = entries.get(clientId);
		if (!entries.isEmpty() && swAuditClientEntries != null && !swAuditClientEntries.isEmpty()) {
			result = swAuditClientEntries.get(0).getLastModification();
		}

		return result;
	}

	public Map<String, Map<String, Object>> retrieveSoftwareAuditData(Map<String, List<SWAuditClientEntry>> entries,
			String clientId) {
		Map<String, Map<String, Object>> result = new TreeMap<>();

		if (entries == null || entries.isEmpty()) {
			return result;
		}

		List<SWAuditClientEntry> swAuditClientEntries = entries.get(clientId);
		for (SWAuditClientEntry entry : swAuditClientEntries) {
			if (entry.getSWid() != null && entry.getSWid() != -1) {
				result.put("" + entry.getSWid(),
						entry.getExpandedMap(getInstalledSoftwareInformationPD(), getSWident(entry.getSWid())));
			}
		}

		return result;
	}

	// returns the ID of the edited data record
	public String editSoftwareLicence(String softwareLicenseId, String licenceContractId, String licenceType,
			String maxInstallations, String boundToHost, String expirationDate) {
		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return "";
		}

		String result = "";

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			RPCMethodName methodName = null;
			switch (licenceType) {
			case LicenceEntry.VOLUME:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_VOLUME;
				break;
			case LicenceEntry.OEM:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_OEM;
				break;
			case LicenceEntry.CONCURRENT:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_CONCURRENT;
				break;
			case LicenceEntry.RETAIL:
				methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_RETAIL;
				break;
			default:
				Logging.notice(this, "encountered UNKNOWN license type");
				break;
			}

			// The jsonRPC-calls would fail sometimes if we use empty / blank Strings...
			if (maxInstallations != null && maxInstallations.isBlank()) {
				maxInstallations = null;
			}

			if (boundToHost != null && boundToHost.isBlank()) {
				boundToHost = null;
			}

			if (expirationDate != null && expirationDate.isBlank()) {
				expirationDate = null;
			}

			OpsiMethodCall omc = new OpsiMethodCall(methodName, new String[] { softwareLicenseId, licenceContractId,
					maxInstallations, boundToHost, expirationDate });

			if (exec.doCall(omc)) {
				result = softwareLicenseId;
			} else {
				Logging.error(this, "could not execute " + methodName + "  with softwareLicenseId " + softwareLicenseId
						+ " and licenseContractId " + licenceContractId);
			}
		}

		return result;
	}

	public boolean deleteSoftwareLicence(String softwareLicenseId) {
		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return false;
		}

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_DELETE,
					new Object[] { softwareLicenseId });
			return exec.doCall(omc);
		}

		return false;
	}

	public String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId, String licenseKey) {
		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return "";
		}

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_CREATE,
					new String[] { softwareLicenseId, licensePoolId, licenseKey });

			if (!exec.doCall(omc)) {
				Logging.error(this, "cannot create softwarelicense to licensepool relation");
				return "";
			}
		}

		return Utils.pseudokey(new String[] { softwareLicenseId, licensePoolId });
	}

	public boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId) {
		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return false;
		}

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_FROM_LICENSE_POOL_DELETE,
					new String[] { softwareLicenseId, licensePoolId });
			return exec.doCall(omc);
		}

		return false;
	}

	public void setFSoftware2LicencePool(String softwareIdent, String licencePoolId) {
		Map<String, String> fSoftware2LicencePool = cacheManager
				.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
		fSoftware2LicencePool.put(softwareIdent, licencePoolId);
		cacheManager.setCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, fSoftware2LicencePool);
	}

	public boolean removeAssociations(String licencePoolId, List<String> softwareIds) {
		Logging.info(this, "removeAssociations licensePoolId, softwareIds " + licencePoolId + ", " + softwareIds);

		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return false;
		}

		boolean result = false;

		if (licencePoolId == null || softwareIds == null) {
			return result;
		}

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			List<Map<String, String>> deleteItems = new ArrayList<>();

			for (String swIdent : softwareIds) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licencePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				deleteItems.add(item);
			}

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
					new Object[] { deleteItems.toArray() });
			result = exec.doCall(omc);

			Map<String, String> fSoftware2LicencePool = cacheManager
					.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
			Map<String, List<String>> fLicencePool2SoftwareList = cacheManager
					.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
			Map<String, List<String>> fLicencePool2UnknownSoftwareList = cacheManager
					.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST, Map.class);

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

	public boolean setWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, false);
	}

	public boolean addWindowsSoftwareIds2LPool(String licensePoolId, List<String> softwareToAssign) {
		return setWindowsSoftwareIds2LPool(licensePoolId, softwareToAssign, true);
	}

	private boolean setWindowsSoftwareIds2LPool(String licensePoolId, final List<String> softwareToAssign,
			boolean onlyAdding) {
		Logging.debug(this, "setWindowsSoftwareIds2LPool  licensePoolId,  softwareToAssign:" + licensePoolId + " , "
				+ softwareToAssign);

		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return false;
		}

		boolean result = true;

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
			Map<String, SWAuditEntry> instSwI = getInstalledSoftwareInformationForLicensingPD();

			Map<String, List<String>> fLicencePool2SoftwareList = cacheManager
					.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
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
				ArrayList<Map<String, String>> deleteItems = new ArrayList<>();

				for (String swIdent : oldEntriesTruely) {
					// software exists in audit software
					if (instSwI.get(swIdent) != null) {
						entriesToRemove.add(swIdent);
						Map<String, String> item = new HashMap<>();
						item.put("ident", swIdent + ";" + licensePoolId);
						item.put("type", "AuditSoftwareToLicensePool");
						deleteItems.add(item);

						Logging.info(this, "" + instSwI.get(swIdent));
					}
				}
				Logging.info(this, "entriesToRemove " + entriesToRemove);
				Logging.info(this, "deleteItems " + deleteItems);

				if (!deleteItems.isEmpty()) {
					OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
							new Object[] { deleteItems.toArray() });
					result = exec.doCall(omc);
				}

				if (!result) {
					return false;
				} else {
					// do it locally
					instSwI.keySet().removeAll(entriesToRemove);
				}
			}

			ArrayList<Map<String, String>> createItems = new ArrayList<>();

			for (String swIdent : softwareToAssignTruely) {
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licensePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				createItems.add(item);
			}

			Logging.info(this, "setWindowsSoftwareIds2LPool, createItems " + createItems);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS,
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

				NavigableSet<Object> softwareWithoutAssociatedLicencePool = cacheManager
						.getCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL, NavigableSet.class);
				softwareWithoutAssociatedLicencePool.addAll(entriesToRemove);
				softwareWithoutAssociatedLicencePool.removeAll(softwareToAssign);

				Logging.info(this, "setWindowsSoftwareIds2LPool licencePool, fLicencePool2SoftwareList " + licensePoolId
						+ " : " + fLicencePool2SoftwareList.get(licensePoolId));

				for (String ident : newList) {
					// give zero length parts as ""
					String[] parts = ident.split(";", -1);
					String swName = parts[1];
					if (getName2SWIdentsPD().get(swName) == null) {
						getName2SWIdentsPD().put(swName, new TreeSet<>());
					}
					getName2SWIdentsPD().get(swName).add(ident);

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
	public String editPool2AuditSoftware(String softwareID, String licensePoolIDOld, String licencePoolIDNew) {
		if (Boolean.FALSE.equals(userRolesConfigDataService.hasServerFullPermissionPD())) {
			return "";
		}

		String result = "";

		boolean ok = false;
		Logging.info(this, "editPool2AuditSoftware ");

		if (Boolean.TRUE.equals(moduleDataService.isWithLicenceManagementPD())) {
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
				List<Map<String, Object>> readyObjects = new ArrayList<>();
				Map<String, Object> item;

				Map<String, String> swMap = AuditSoftwareXLicencePool.produceMapFromSWident(softwareID);
				swMap.put(LicencepoolEntry.ID_SERVICE_KEY, licencePoolIDNew);

				item = Utils.createNOMitem("AuditSoftwareToLicensePool");
				item.putAll(swMap);
				// create the edited entry

				readyObjects.add(item);

				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS,
						new Object[] { readyObjects });

				Logging.info(this, "editPool2AuditSoftware call " + omc);
				if (exec.doCall(omc)) {
					ok = true;
				} else {
					Logging.warning(this, "editPool2AuditSoftware " + omc + " failed");
				}
			}

			Logging.info(this, "editPool2AuditSoftware ok " + ok);

			if (ok) {
				Map<String, String> fSoftware2LicencePool = cacheManager
						.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
				Map<String, List<String>> fLicencePool2SoftwareList = cacheManager
						.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
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
				cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, fLicencePool2SoftwareList);
			}

			return result;
		}

		return "???";
	}

	public Map<String, Map<String, Object>> getLicensesReconciliationPD() {
		retrieveLicenseStatisticsPD();
		return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class);
	}

	public Map<String, LicenceStatisticsRow> getLicenseStatistics() {
		return retrieveLicenseStatisticsPD();
	}

	// poolId -> LicenceStatisticsRow
	public Map<String, LicenceStatisticsRow> retrieveLicenseStatisticsPD() {
		// side effects of this method: rowsLicencesReconciliation
		if (!moduleDataService.isWithLicenceManagementPD()) {
			return new HashMap<>();
		}

		Logging.info(this, "retrieveLicenseStatistics");
		Map<String, Map<String, Object>> rowsLicensesReconciliation = getRowsLicenseReconciliation();
		checkLicensesReconciliationUsedBySWInventory(rowsLicensesReconciliation);

		retrieveInstalledSoftwareInformationPD();
		retrieveRelationsAuditSoftwareToLicencePoolsPD();

		// table SOFTWARE_LICENSE_TO_LICENSE_POOL
		Map<String, ExtendedInteger> pool2allowedUsagesCount = getPool2AllowedUsagesCount();
		Logging.debug(this, " pool2allowedUsagesCount " + pool2allowedUsagesCount);

		// table LICENSE_ON_CLIENT
		Logging.info(this, " licence usages ");
		List<LicenceUsageEntry> licenseUsages = licenseDataService.getLicenseUsagesPD();
		TreeMap<String, Integer> pool2opsiUsagesCount = new TreeMap<>();
		Map<String, Set<String>> pool2opsiUsages = new TreeMap<>();
		for (LicenceUsageEntry licenseUsage : licenseUsages) {
			String pool = licenseUsage.getLicensePool();
			Integer usageCount = pool2opsiUsagesCount.computeIfAbsent(pool, s -> Integer.valueOf(0));
			Set<String> usingClients = pool2opsiUsages.computeIfAbsent(pool, s -> new TreeSet<>());
			String clientId = licenseUsage.getClientId();
			if (clientId != null) {
				usageCount = usageCount + 1;
				pool2opsiUsagesCount.put(pool, usageCount);
				usingClients.add(clientId);
			}
		}

		// all used licences for pools
		Logging.info(this, "  retrieveStatistics  collect pool2installationsCount");
		Map<String, Integer> pool2installationsCount = getPool2InstallationsCount();
		Map<String, LicenceStatisticsRow> rowsLicenseStatistics = new TreeMap<>();
		// table LICENSE_POOL
		Map<String, LicencepoolEntry> licensePools = licenseDataService.getLicensePoolsPD();
		for (String licensePoolId : licensePools.keySet()) {
			LicenceStatisticsRow rowMap = new LicenceStatisticsRow(licensePoolId);
			rowsLicenseStatistics.put(licensePoolId, rowMap);

			rowMap.setAllowedUsagesCount(pool2allowedUsagesCount.get(licensePoolId));
			rowMap.setOpsiUsagesCount(pool2opsiUsagesCount.get(licensePoolId));
			rowMap.setSWauditUsagesCount(pool2installationsCount.get(licensePoolId));

			Set<String> listOfUsingClients = pool2opsiUsages.get(licensePoolId);

			Logging.debug(this, "pool  " + licensePoolId + " used_by_opsi on clients : " + listOfUsingClients);

			if (listOfUsingClients != null) {
				for (String client : listOfUsingClients) {
					String pseudokey = Utils.pseudokey(new String[] { client, licensePoolId });

					if (rowsLicensesReconciliation.get(pseudokey) == null) {
						Logging.warning("client " + client + " or license pool ID " + licensePoolId + " do not exist");
					} else {
						rowsLicensesReconciliation.get(pseudokey).put("used_by_opsi", true);
					}
				}
			}
		}

		cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, rowsLicensesReconciliation);

		Logging.debug(this, "rowsLicenceStatistics " + rowsLicenseStatistics);
		return rowsLicenseStatistics;
	}

	private Map<String, Map<String, Object>> getRowsLicenseReconciliation() {
		if (cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class) != null) {
			return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class);
		}

		Map<String, Map<String, Object>> rowsLicensesReconciliation = new HashMap<>();
		Map<String, LicencepoolEntry> licensePools = licenseDataService.getLicensePoolsPD();
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		List<String> extraHostFields = Utils.takeAsStringList(configDefaultValues.get(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENCES_RECONCILIATION));
		Map<String, HostInfo> clientMap = hostInfoCollections.getMapOfAllPCInfoMaps();
		for (Entry<String, HostInfo> clientEntry : clientMap.entrySet()) {
			for (String pool : licensePools.keySet()) {
				HashMap<String, Object> rowMap = new HashMap<>();

				rowMap.put(OpsiServiceNOMPersistenceController.HOST_KEY, clientEntry.getKey());

				for (String fieldName : extraHostFields) {
					rowMap.put(fieldName, clientEntry.getValue().getMap().get(fieldName));
				}

				rowMap.put("licensePoolId", pool);
				rowMap.put("used_by_opsi", false);
				rowMap.put("SWinventory_used", false);
				String pseudokey = Utils.pseudokey(new String[] { clientEntry.getKey(), pool });
				rowsLicensesReconciliation.put(pseudokey, rowMap);
			}
		}

		return rowsLicensesReconciliation;
	}

	private void checkLicensesReconciliationUsedBySWInventory(
			Map<String, Map<String, Object>> rowsLicensesReconciliation) {
		Map<String, String> fSoftware2LicensePool = getFSoftware2LicensePoolPD();
		List<String> opsiHostNames = hostInfoCollections.getOpsiHostNames();
		Map<String, Set<String>> swId2clients = getSoftwareIdentOnClients(opsiHostNames);
		for (String softwareIdent : getInstalledSoftwareInformationForLicensingPD().keySet()) {
			String licensePoolId = fSoftware2LicensePool.get(softwareIdent);
			Logging.debug(this, "software " + softwareIdent + " installed on " + swId2clients.get(softwareIdent));

			if (licensePoolId == null || swId2clients.get(softwareIdent) == null) {
				continue;
			}

			for (String client : swId2clients.get(softwareIdent)) {
				String pseudokey = Utils.pseudokey(new String[] { client, licensePoolId });

				if (rowsLicensesReconciliation.get(pseudokey) == null) {
					Logging.warning("client " + client + " or license pool ID " + licensePoolId + " do not exist");
				} else {
					rowsLicensesReconciliation.get(pseudokey).put("SWinventory_used", true);
				}
			}
		}
	}

	private Map<String, ExtendedInteger> getPool2AllowedUsagesCount() {
		Logging.info(this, " licence usabilities ");
		List<LicenceUsableForEntry> licenceUsabilities = licenseDataService.getLicenseUsabilitiesPD();
		TreeMap<String, ExtendedInteger> pool2allowedUsagesCount = new TreeMap<>();
		for (LicenceUsableForEntry licenceUsability : licenceUsabilities) {
			String pool = licenceUsability.getLicencePoolId();
			String licenceId = licenceUsability.getLicenceId();

			// value up this step
			ExtendedInteger count = pool2allowedUsagesCount.get(pool);

			Map<String, LicenceEntry> licences = licenseDataService.getLicensesPD();

			// not yet initialized
			if (count == null) {
				count = licences.get(licenceId).getMaxInstallations();
				pool2allowedUsagesCount.put(pool, count);
			} else {
				ExtendedInteger result = count.add(licences.get(licenceId).getMaxInstallations());
				pool2allowedUsagesCount.put(pool, result);
			}
		}
		return pool2allowedUsagesCount;
	}

	private Map<String, Integer> getPool2InstallationsCount() {
		TreeMap<String, Integer> pool2installationsCount = new TreeMap<>();
		for (Entry<String, TreeSet<String>> poolEntry : getPool2Clients().entrySet()) {
			pool2installationsCount.put(poolEntry.getKey(), poolEntry.getValue().size());
		}
		return pool2installationsCount;
	}

	private TreeMap<String, TreeSet<String>> getPool2Clients() {
		// require this licencepool
		// add the clients which have this software installed
		TreeMap<String, TreeSet<String>> pool2clients = new TreeMap<>();
		// we take Set since we count only one usage per client
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = getAuditSoftwareXLicencePoolPD();
		List<String> opsiHostNames = hostInfoCollections.getOpsiHostNames();
		Map<String, Set<String>> swId2clients = getSoftwareIdentOnClients(opsiHostNames);
		for (StringValuedRelationElement swXpool : auditSoftwareXLicencePool) {
			Logging.debug(this, " retrieveStatistics1 relationElement  " + swXpool);
			String pool = swXpool.get(LicencepoolEntry.ID_SERVICE_KEY);

			TreeSet<String> clientsServedByPool = pool2clients.computeIfAbsent(pool, s -> new TreeSet<>());

			String swIdent = swXpool.get(AuditSoftwareXLicencePool.SW_ID);

			Logging.debug(this, " retrieveStatistics1 swIdent " + swIdent);

			if (swId2clients.get(swIdent) != null) {
				Logging.debug(this, "pool " + pool + " serves clients " + swId2clients.get(swIdent));
				clientsServedByPool.addAll(swId2clients.get(swIdent));
			}
		}
		return pool2clients;
	}

	private Map<String, Set<String>> getSoftwareIdentOnClients(final List<String> clients) {
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Utils.usedMemory());
		int stepSize = 100;
		Map<String, Set<String>> softwareIdent2clients = new HashMap<>();
		while (!clients.isEmpty()) {
			List<String> clientListForCall = new ArrayList<>();

			for (int i = 0; i < stepSize && i < clients.size(); i++) {
				clientListForCall.add(clients.get(i));
			}

			clients.removeAll(clientListForCall);

			Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("clientId", clientListForCall);

			OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_ON_CLIENT_GET_OBJECTS,
					new Object[] { callAttributes, callFilter });
			List<Map<String, Object>> softwareAuditOnClients = exec.getListOfMaps(omc);

			Logging.info(this,
					"retrieveSoftwareAuditOnClients, finished a request, map size " + softwareAuditOnClients.size());

			for (Map<String, Object> item : softwareAuditOnClients) {
				SWAuditClientEntry clientEntry = new SWAuditClientEntry(item);
				Set<String> clientsWithThisSW = softwareIdent2clients.computeIfAbsent(clientEntry.getSWident(),
						s -> new HashSet<>());
				clientsWithThisSW.add(clientEntry.getClientId());
			}

			Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");
		}

		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Utils.usedMemory());
		persistenceController.notifyPanelCompleteWinProducts();

		return softwareIdent2clients;
	}
}
