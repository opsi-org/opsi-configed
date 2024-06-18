/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.dataservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import de.uib.configed.gui.FSoftwarename2LicensePool;
import de.uib.configed.type.HostInfo;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licenses.AuditSoftwareXLicensePool;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.configed.type.licenses.LicenseStatisticsRow;
import de.uib.configed.type.licenses.LicenseUsableForEntry;
import de.uib.configed.type.licenses.LicenseUsageEntry;
import de.uib.configed.type.licenses.LicensepoolEntry;
import de.uib.opsicommand.AbstractPOJOExecutioner;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.HostInfoCollections;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.RPCMethodName;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.ExtendedInteger;
import de.uib.utils.Utils;
import de.uib.utils.datastructure.StringValuedRelationElement;
import de.uib.utils.logging.Logging;

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
	private static final String LINUX_SUBVERSION_MARKER = "lin:";

	private static final Set<String> linuxSWnameMarkers = new HashSet<>();
	static {
		linuxSWnameMarkers.add("linux");
		linuxSWnameMarkers.add("Linux");
		linuxSWnameMarkers.add("lib");
		linuxSWnameMarkers.add("ubuntu");
	}

	private CacheManager cacheManager;
	private AbstractPOJOExecutioner exec;
	private OpsiServiceNOMPersistenceController persistenceController;
	private ModuleDataService moduleDataService;
	private UserRolesConfigDataService userRolesConfigDataService;
	private LicenseDataService licenseDataService;
	private HostInfoCollections hostInfoCollections;

	public SoftwareDataService(AbstractPOJOExecutioner exec,
			OpsiServiceNOMPersistenceController persistenceController) {
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

	public NavigableSet<Object> getSoftwareWithoutAssociatedLicensePoolPD() {
		retrieveRelationsAuditSoftwareToLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL, NavigableSet.class);
	}

	public List<String> getSoftwareListByLicensePoolPD(String licensePoolId) {
		retrieveRelationsAuditSoftwareToLicensePoolsPD();
		Map<String, List<String>> fLicensePool2SoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
		List<String> result = fLicensePool2SoftwareList.get(licensePoolId);
		return result == null ? new ArrayList<>() : result;
	}

	public List<String> getUnknownSoftwareListForLicensePoolPD(String licensePoolId) {
		retrieveRelationsAuditSoftwareToLicensePoolsPD();
		Map<String, List<String>> fLicensePool2UnknownSoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST, Map.class);
		List<String> result = fLicensePool2UnknownSoftwareList.get(licensePoolId);
		return result == null ? new ArrayList<>() : result;
	}

	public Map<String, String> getFSoftware2LicensePoolPD() {
		retrieveRelationsAuditSoftwareToLicensePoolsPD();
		return cacheManager.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
	}

	public String getFSoftware2LicensePoolPD(String softwareIdent) {
		retrieveRelationsAuditSoftwareToLicensePoolsPD();
		Map<String, String> fSoftware2LicensePool = cacheManager
				.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
		return fSoftware2LicensePool.get(softwareIdent);
	}

	public void retrieveRelationsAuditSoftwareToLicensePoolsPD() {
		if (cacheManager.isDataCached(Arrays.asList(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL,
				CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST,
				CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL))) {
			return;
		}

		Map<String, String> fSoftware2LicensePool = new HashMap<>();
		Map<String, List<String>> fLicensePool2SoftwareList = new HashMap<>();
		Map<String, List<String>> fLicensePool2UnknownSoftwareList = new HashMap<>();
		NavigableSet<String> softwareWithoutAssociatedLicensePool = new TreeSet<>(
				getInstalledSoftwareInformationForLicensingPD().keySet());

		cacheManager.setCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, fSoftware2LicensePool);
		cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, fLicensePool2SoftwareList);
		cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST,
				fLicensePool2UnknownSoftwareList);
		cacheManager.setCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL,
				softwareWithoutAssociatedLicensePool);

		if (!moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return;
		}

		AuditSoftwareXLicensePool relationsAuditSoftwareToLicensePools = getAuditSoftwareXLicensePoolPD();
		if (relationsAuditSoftwareToLicensePools == null) {
			Logging.warning(this, "retrieveRelationsAuditSoftwareToLicensePools is null");
			return;
		}

		for (StringValuedRelationElement retrieved : relationsAuditSoftwareToLicensePools) {
			SWAuditEntry entry = new SWAuditEntry(retrieved);
			String licensePoolKEY = retrieved.get(LicensepoolEntry.ID_SERVICE_KEY);
			String swKEY = entry.getIdent();

			if (fSoftware2LicensePool.get(swKEY) != null && !fSoftware2LicensePool.get(swKEY).equals(licensePoolKEY)) {
				Logging.error("software with ident \"" + swKEY + "\" has assigned license pool "
						+ fSoftware2LicensePool.get(swKEY) + " as well as " + licensePoolKEY);
			}
			fSoftware2LicensePool.put(swKEY, licensePoolKEY);

			List<String> softwareIds = fLicensePool2SoftwareList.computeIfAbsent(licensePoolKEY,
					v -> new ArrayList<>());
			if (softwareIds.indexOf(swKEY) == -1) {
				if (getInstalledSoftwareInformationForLicensingPD().get(swKEY) == null) {
					Logging.warning(this, "license pool " + licensePoolKEY
							+ " is assigned to a not listed software with ID " + swKEY);
					List<String> unknownSoftwareIds = fLicensePool2UnknownSoftwareList.computeIfAbsent(licensePoolKEY,
							s -> new ArrayList<>());
					unknownSoftwareIds.add(swKEY);
				} else {
					softwareIds.add(swKEY);
					softwareWithoutAssociatedLicensePool.remove(swKEY);
				}
			}
		}

		Logging.info(this, "retrieveRelationsAuditSoftwareToLicensePools,  softwareWithoutAssociatedLicensePool "
				+ softwareWithoutAssociatedLicensePool.size());
	}

	public AuditSoftwareXLicensePool getAuditSoftwareXLicensePoolPD() {
		retrieveAuditSoftwareXLicensePoolPD();
		return cacheManager.getCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL,
				AuditSoftwareXLicensePool.class);
	}

	public void retrieveAuditSoftwareXLicensePoolPD() {
		if (cacheManager.isDataCached(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL)) {
			return;
		}

		Logging.info(this, "retrieveAuditSoftwareXLicensePool");
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_GET_OBJECTS,
				new Object[] { AuditSoftwareXLicensePool.SERVICE_ATTRIBUTES, new HashMap<>() });
		List<Map<String, Object>> retrieved = exec.getListOfMaps(omc);
		AuditSoftwareXLicensePool auditSoftwareXLicensePool = new AuditSoftwareXLicensePool();
		for (Map<String, Object> map : retrieved) {
			auditSoftwareXLicensePool.integrateRaw(map);
		}
		cacheManager.setCachedData(CacheIdentifier.AUDIT_SOFTWARE_XL_LICENSE_POOL, auditSoftwareXLicensePool);
		Logging.info(this, "retrieveAuditSoftwareXLicensePool retrieved ");
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

	public Set<String> getSoftwareListPD() {
		retrieveInstalledSoftwareInformationPD();
		return cacheManager.getCachedData(CacheIdentifier.SOFTWARE_LIST, Set.class);
	}

	public boolean swEntryExists(SWAuditClientEntry swAuditClientEntry) {
		Logging.info(this, "Check if software ident " + swAuditClientEntry.getSWIdent() + " entry exists");
		retrieveInstalledSoftwareInformationPD();
		boolean swIdent = false;
		Set<String> softwareList = getSoftwareListPD();
		if (softwareList == null || !softwareList.contains(swAuditClientEntry.getSWIdent())) {
			if (softwareList != null) {
				Logging.info(this, "Until now existing installed software entries " + softwareList.size());
			}

			int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					String.format(Configed.getResourceValue("DataStub.reloadSoftwareInformation.text"),
							swAuditClientEntry.getSWIdent(), swAuditClientEntry.getClientId()),
					Configed.getResourceValue("DataStub.reloadSoftwareInformation.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (returnedOption == JOptionPane.YES_OPTION) {
				Logging.info(this, "Reloading installed software information");
				persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
				softwareList = getSoftwareListPD();
				Logging.info(this, "Now existing installed software entries " + softwareList.size());
				if (softwareList.contains(swAuditClientEntry.getSWIdent())) {
					Logging.info(this, "Found software ident " + swAuditClientEntry.getSWIdent() + " after reload");
					swIdent = true;
				}
			}

			if (!swIdent) {
				Logging.warning(this, "Missing installed software entry " + swAuditClientEntry.getSWIdent());
			}
		} else {
			swIdent = true;
		}

		return swIdent;
	}

	public void retrieveInstalledSoftwareInformationPD() {
		if (cacheManager.isDataCached(
				Arrays.asList(CacheIdentifier.SOFTWARE_LIST, CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION,
						CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING, CacheIdentifier.NAME_TO_SW_IDENTS,
						CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO))) {
			return;
		}

		String[] callAttributes = new String[] { SWAuditEntry.NAME, SWAuditEntry.VERSION, SWAuditEntry.SUB_VERSION,
				SWAuditEntry.LANGUAGE, SWAuditEntry.ARCHITECTURE, SWAuditEntry.WINDOWS_SOFTWARE_ID };
		Map<String, Object> callFilter = new HashMap<>();
		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_GET_OBJECTS,
				new Object[] { callAttributes, callFilter });
		List<Map<String, Object>> list = exec.getListOfMaps(omc);

		NavigableMap<String, SWAuditEntry> installedSoftwareInformation = new TreeMap<>();
		NavigableMap<String, SWAuditEntry> installedSoftwareInformationForLicensing = new TreeMap<>();
		NavigableMap<String, Set<String>> name2SWIdents = new TreeMap<>();
		NavigableMap<String, Map<String, String>> installedSoftwareName2SWinfo = new TreeMap<>();
		NavigableMap<String, Map<String, Map<String, String>>> name2ident2infoWithPool = new TreeMap<>();

		int i = 0;
		Logging.info(this, "getInstalledSoftwareInformation build map");

		for (Map<String, Object> map : list) {
			i++;
			SWAuditEntry entry = new SWAuditEntry(map);
			String swName = entry.get(SWAuditEntry.NAME);
			String swIdent = entry.getIdent();
			installedSoftwareInformation.put(swIdent, entry);

			if (showForLicensing(entry, swName)) {
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
				infoWithPool.put(LicensepoolEntry.ID_SERVICE_KEY, "x " + i);
			}
		}

		cacheManager.setCachedData(CacheIdentifier.SOFTWARE_LIST, installedSoftwareInformation.keySet());
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION, installedSoftwareInformation);
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_INFORMATION_FOR_LICENSING,
				installedSoftwareInformationForLicensing);
		cacheManager.setCachedData(CacheIdentifier.INSTALLED_SOFTWARE_NAME_TO_SW_INFO, installedSoftwareName2SWinfo);
		cacheManager.setCachedData(CacheIdentifier.NAME_TO_SW_IDENTS, name2SWIdents);
		persistenceController.notifyPanelCompleteWinProducts();
	}

	private static boolean showForLicensing(SWAuditEntry entry, String swName) {
		for (String marker : linuxSWnameMarkers) {
			String version = entry.get(SWAuditEntry.VERSION);
			if (swName.indexOf(marker) > -1 || version.indexOf(marker) > -1) {
				return false;
			}
		}

		return !entry.get(SWAuditEntry.SUB_VERSION).startsWith(LINUX_SUBVERSION_MARKER);
	}

	public Map<String, List<SWAuditClientEntry>> getSoftwareAuditOnClients(Collection<String> clients) {
		Map<String, List<SWAuditClientEntry>> client2software = new HashMap<>();
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Utils.usedMemory());
		Logging.info(this, "retrieveSoftwareAuditOnClients clients cound: " + clients.size());
		final int STEP_SIZE = 100;

		Iterator<String> clientIterator = clients.iterator();
		while (clientIterator.hasNext()) {
			List<String> clientListForCall = new ArrayList<>();

			for (int i = 0; i < STEP_SIZE && clientIterator.hasNext(); i++) {
				clientListForCall.add(clientIterator.next());
			}

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
			if (swEntryExists(entry)) {
				result.put(entry.getSWIdent(),
						entry.getExpandedMap(getInstalledSoftwareInformationPD().get(entry.getSWIdent())));
			}
		}

		return result;
	}

	// returns the ID of the edited data record
	public String editSoftwareLicense(String softwareLicenseId, String licenseContractId, String licenseType,
			String maxInstallations, String boundToHost, String expirationDate) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return "";
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

		RPCMethodName methodName = getMethodNameForLicenseType(licenseType);

		OpsiMethodCall omc = new OpsiMethodCall(methodName,
				new String[] { softwareLicenseId, licenseContractId, maxInstallations, boundToHost, expirationDate });

		if (exec.doCall(omc)) {
			return softwareLicenseId;
		} else {
			Logging.error(this, "could not execute " + methodName + "  with softwareLicenseId " + softwareLicenseId
					+ " and licenseContractId " + licenseContractId);

			return "";
		}
	}

	private RPCMethodName getMethodNameForLicenseType(String licenseType) {
		RPCMethodName methodName = null;
		switch (licenseType) {
		case LicenseEntry.VOLUME:
			methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_VOLUME;
			break;
		case LicenseEntry.OEM:
			methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_OEM;
			break;
		case LicenseEntry.CONCURRENT:
			methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_CONCURRENT;
			break;
		case LicenseEntry.RETAIL:
			methodName = RPCMethodName.SOFTWARE_LICENSE_CREATE_RETAIL;
			break;
		default:
			Logging.notice(this, "encountered UNKNOWN license type");
			break;
		}

		return methodName;
	}

	public boolean deleteSoftwareLicense(String softwareLicenseId) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_DELETE,
				new Object[] { softwareLicenseId });
		return exec.doCall(omc);
	}

	public String editRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId, String licenseKey) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()) {
			return "";
		}

		if (moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
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
		if (!userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return false;
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.SOFTWARE_LICENSE_TO_LICENSE_POOL_DELETE,
				new String[] { softwareLicenseId, licensePoolId });
		return exec.doCall(omc);
	}

	public void setFSoftware2LicensePool(String softwareIdent, String licensePoolId) {
		Map<String, String> fSoftware2LicensePool = cacheManager
				.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
		fSoftware2LicensePool.put(softwareIdent, licensePoolId);
		cacheManager.setCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, fSoftware2LicensePool);
	}

	public boolean removeAssociations(String licensePoolId, List<String> softwareIds) {
		Logging.info(this, "removeAssociations licensePoolId, softwareIds " + licensePoolId + ", " + softwareIds);

		if (licensePoolId == null || softwareIds == null || !userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return false;
		}

		List<Map<String, String>> deleteItems = new ArrayList<>();

		for (String swIdent : softwareIds) {
			Map<String, String> item = new HashMap<>();
			item.put("ident", swIdent + ";" + licensePoolId);
			item.put("type", "AuditSoftwareToLicensePool");
			deleteItems.add(item);
		}

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
				new Object[] { deleteItems });
		boolean result = exec.doCall(omc);

		Map<String, String> fSoftware2LicensePool = cacheManager
				.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
		Map<String, List<String>> fLicensePool2SoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
		Map<String, List<String>> fLicensePool2UnknownSoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_UNKNOWN_SOFTWARE_LIST, Map.class);

		if (result) {
			fSoftware2LicensePool.keySet().removeAll(softwareIds);

			if (fLicensePool2SoftwareList.get(licensePoolId) != null) {
				fLicensePool2SoftwareList.get(licensePoolId).removeAll(softwareIds);
			}

			if (fLicensePool2UnknownSoftwareList.get(licensePoolId) != null) {
				fLicensePool2UnknownSoftwareList.get(licensePoolId).removeAll(softwareIds);
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

		if (!userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return false;
		}

		Map<String, SWAuditEntry> instSwI = getInstalledSoftwareInformationForLicensingPD();

		Map<String, List<String>> fLicensePool2SoftwareList = cacheManager
				.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
		List<String> oldEntries = fLicensePool2SoftwareList.computeIfAbsent(licensePoolId, arg -> new ArrayList<>());

		List<String> oldEntriesTruely = new ArrayList<>(oldEntries);
		List<String> softwareToAssignTruely = new ArrayList<>(softwareToAssign);

		Set<String> entriesToRemove = new HashSet<>();

		// we work only with real changes
		softwareToAssignTruely.removeAll(oldEntries);
		oldEntriesTruely.removeAll(softwareToAssign);
		oldEntriesTruely.retainAll(instSwI.keySet());

		Logging.info(this, "setWindowsSoftwareIds2LPool softwareToAssignTruely " + softwareToAssignTruely);
		Logging.info(this, "setWindowsSoftwareIds2LPool oldEntriesTruely " + oldEntriesTruely);

		boolean result = updateLicensepoolsOnServer(onlyAdding, oldEntriesTruely, entriesToRemove, licensePoolId,
				instSwI, softwareToAssignTruely);
		// we build the correct data locally
		if (result) {
			Set<String> intermediateSet = new HashSet<>(fLicensePool2SoftwareList.get(licensePoolId));
			intermediateSet.removeAll(entriesToRemove);
			intermediateSet.addAll(softwareToAssign);
			// dont delete old entries but avoid double entries
			List<String> newList = new ArrayList<>(intermediateSet);
			fLicensePool2SoftwareList.put(licensePoolId, newList);

			NavigableSet<Object> softwareWithoutAssociatedLicensePool = cacheManager
					.getCachedData(CacheIdentifier.SOFTWARE_WITHOUT_ASSOCIATED_LICENSE_POOL, NavigableSet.class);
			softwareWithoutAssociatedLicensePool.addAll(entriesToRemove);
			softwareWithoutAssociatedLicensePool.removeAll(softwareToAssign);

			Logging.info(this, "setWindowsSoftwareIds2LPool licensePool, fLicensePool2SoftwareList " + licensePoolId
					+ " : " + fLicensePool2SoftwareList.get(licensePoolId));

			for (String ident : newList) {
				// give zero length parts as ""
				String[] parts = ident.split(";", -1);
				String swName = parts[1];

				Set<String> swIdents = getName2SWIdentsPD().computeIfAbsent(swName, key -> new TreeSet<>());
				swIdents.add(ident);

				Logging.info(this,
						"setWindowsSoftwareIds2LPool, collecting all idents for a name (even if not belonging to the pool), add ident "
								+ ident + " to set for name " + swName);
			}
		}

		return result;
	}

	private boolean updateLicensepoolsOnServer(boolean onlyAdding, List<String> oldEntriesTruely,
			Set<String> entriesToRemove, String licensePoolId, Map<String, SWAuditEntry> instSwI,
			List<String> softwareToAssignTruely) {
		boolean result = true;

		if (!onlyAdding) {
			List<Map<String, String>> deleteItems = new ArrayList<>();

			for (String swIdent : oldEntriesTruely) {
				// software exists in audit software
				entriesToRemove.add(swIdent);
				Map<String, String> item = new HashMap<>();
				item.put("ident", swIdent + ";" + licensePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				deleteItems.add(item);

				Logging.info(this, "" + instSwI.get(swIdent));
			}
			Logging.info(this, "entriesToRemove " + entriesToRemove);
			Logging.info(this, "deleteItems " + deleteItems);

			if (!deleteItems.isEmpty()) {
				OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_DELETE_OBJECTS,
						new Object[] { deleteItems });
				result = exec.doCall(omc);
			}

			if (!result) {
				return false;
			} else {
				// do it locally
				instSwI.keySet().removeAll(entriesToRemove);
			}
		}

		List<Map<String, String>> createItems = new ArrayList<>();

		for (String swIdent : softwareToAssignTruely) {
			Map<String, String> item = new HashMap<>();
			item.put("ident", swIdent + ";" + licensePoolId);
			item.put("type", "AuditSoftwareToLicensePool");
			createItems.add(item);
		}

		Logging.info(this, "setWindowsSoftwareIds2LPool, createItems " + createItems);

		OpsiMethodCall omc = new OpsiMethodCall(RPCMethodName.AUDIT_SOFTWARE_TO_LICENSE_POOL_CREATE_OBJECTS,
				new Object[] { createItems });

		return exec.doCall(omc);
	}

	// we have got a SW from software table, therefore we do not serve the unknown
	// software list
	public String editPool2AuditSoftware(String softwareID, String licensePoolIDOld, String licensePoolIDNew) {
		if (!userRolesConfigDataService.hasServerFullPermissionPD()
				|| !moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT)) {
			return "";
		}

		boolean ok = false;
		Logging.info(this, "editPool2AuditSoftware ");

		if (licensePoolIDOld != null && !licensePoolIDOld.equals(FSoftwarename2LicensePool.VALUE_NO_LICENSE_POOL)) {
			// there was an association, we delete it)

			List<String> swIds = new ArrayList<>();
			swIds.add(softwareID);
			ok = removeAssociations(licensePoolIDOld, swIds);

			if (!ok) {
				Logging.warning(this, "editPool2AuditSoftware " + " failed");
			}
		}

		if (FSoftwarename2LicensePool.VALUE_NO_LICENSE_POOL.equals(licensePoolIDNew)) {
			// nothing to do, we deleted the entry
			ok = true;
		} else {
			List<Map<String, Object>> readyObjects = new ArrayList<>();
			Map<String, Object> item;

			Map<String, String> swMap = AuditSoftwareXLicensePool.produceMapFromSWident(softwareID);
			swMap.put(LicensepoolEntry.ID_SERVICE_KEY, licensePoolIDNew);

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
			Map<String, String> fSoftware2LicensePool = cacheManager
					.getCachedData(CacheIdentifier.FSOFTWARE_TO_LICENSE_POOL, Map.class);
			Map<String, List<String>> fLicensePool2SoftwareList = cacheManager
					.getCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, Map.class);
			Logging.info(this, "fSoftware2LicensePool == null " + (fSoftware2LicensePool == null));

			if (fSoftware2LicensePool != null) {
				Logging.info(this, "fSoftware2LicensePool.get( softwareID ) " + fSoftware2LicensePool.get(softwareID));
				fSoftware2LicensePool.put(softwareID, licensePoolIDNew);
			}
			List<String> fLicensePoolSoftwareList = fLicensePool2SoftwareList.computeIfAbsent(licensePoolIDNew,
					arg -> new ArrayList<>());

			Logging.info(this, "fLicensePool2SoftwareList.get( licensePoolIDNew ) " + fLicensePoolSoftwareList);

			fLicensePoolSoftwareList.add(softwareID);
			cacheManager.setCachedData(CacheIdentifier.FLICENSE_POOL_TO_SOFTWARE_LIST, fLicensePool2SoftwareList);
		}

		return "";
	}

	public Map<String, Map<String, Object>> getLicensesReconciliationPD() {
		retrieveLicenseStatisticsPD();
		return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class);
	}

	public Map<String, LicenseStatisticsRow> getLicenseStatistics() {
		retrieveLicenseStatisticsPD();
		return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS, Map.class);
	}

	// side effects of this method: rowsLicensesReconciliation
	public void retrieveLicenseStatisticsPD() {
		if (!moduleDataService.isOpsiModuleActive(OpsiModule.LICENSE_MANAGEMENT) || cacheManager.isDataCached(Arrays
				.asList(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, CacheIdentifier.ROWS_LICENSES_STATISTICS))) {
			return;
		}

		Logging.info(this, "retrieveLicenseStatistics");
		Map<String, Map<String, Object>> rowsLicensesReconciliation = getRowsLicenseReconciliation();
		checkLicensesReconciliationUsedBySWInventory(rowsLicensesReconciliation);

		retrieveInstalledSoftwareInformationPD();
		retrieveRelationsAuditSoftwareToLicensePoolsPD();

		// table SOFTWARE_LICENSE_TO_LICENSE_POOL
		Map<String, ExtendedInteger> pool2allowedUsagesCount = getPool2AllowedUsagesCount();
		Logging.debug(this, " pool2allowedUsagesCount " + pool2allowedUsagesCount);

		// table LICENSE_ON_CLIENT
		Logging.info(this, " license usages ");
		List<LicenseUsageEntry> licenseUsages = licenseDataService.getLicenseUsagesPD();
		TreeMap<String, Integer> pool2opsiUsagesCount = new TreeMap<>();
		Map<String, Set<String>> pool2opsiUsages = new TreeMap<>();
		for (LicenseUsageEntry licenseUsage : licenseUsages) {
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

		// all used licenses for pools
		Logging.info(this, "  retrieveStatistics  collect pool2installationsCount");
		Map<String, Integer> pool2installationsCount = getPool2InstallationsCount();
		Map<String, LicenseStatisticsRow> rowsLicenseStatistics = new TreeMap<>();
		// table LICENSE_POOL
		Map<String, LicensepoolEntry> licensePools = licenseDataService.getLicensePoolsPD();
		for (String licensePoolId : licensePools.keySet()) {
			LicenseStatisticsRow rowMap = new LicenseStatisticsRow(licensePoolId);
			rowsLicenseStatistics.put(licensePoolId, rowMap);

			rowMap.setAllowedUsagesCount(pool2allowedUsagesCount.get(licensePoolId));
			rowMap.setOpsiUsagesCount(pool2opsiUsagesCount.get(licensePoolId));
			rowMap.setSWauditUsagesCount(pool2installationsCount.get(licensePoolId));

			Set<String> listOfUsingClients = pool2opsiUsages.get(licensePoolId);

			setUsedByOpsiToTrue(licensePoolId, listOfUsingClients, rowsLicensesReconciliation);
		}

		cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, rowsLicensesReconciliation);
		cacheManager.setCachedData(CacheIdentifier.ROWS_LICENSES_STATISTICS, rowsLicenseStatistics);

		Logging.debug(this, "rowsLicenseStatistics " + rowsLicenseStatistics);
	}

	private void setUsedByOpsiToTrue(String licensePoolId, Set<String> listOfUsingClients,
			Map<String, Map<String, Object>> rowsLicensesReconciliation) {
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

	private Map<String, Map<String, Object>> getRowsLicenseReconciliation() {
		if (cacheManager.isDataCached(CacheIdentifier.ROWS_LICENSES_RECONCILIATION)) {
			return cacheManager.getCachedData(CacheIdentifier.ROWS_LICENSES_RECONCILIATION, Map.class);
		}

		Map<String, Map<String, Object>> rowsLicensesReconciliation = new HashMap<>();
		Map<String, LicensepoolEntry> licensePools = licenseDataService.getLicensePoolsPD();
		Map<String, List<Object>> configDefaultValues = cacheManager
				.getCachedData(CacheIdentifier.CONFIG_DEFAULT_VALUES, Map.class);
		List<String> extraHostFields = Utils.takeAsStringList(configDefaultValues.get(
				OpsiServiceNOMPersistenceController.KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PANEL_LICENSES_RECONCILIATION));
		Map<String, HostInfo> clientMap = hostInfoCollections.getMapOfAllPCInfoMaps();
		for (Entry<String, HostInfo> clientEntry : clientMap.entrySet()) {
			for (String pool : licensePools.keySet()) {
				Map<String, Object> rowMap = new HashMap<>();

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
		Logging.info(this, " license usabilities ");
		List<LicenseUsableForEntry> licenseUsabilities = licenseDataService.getLicenseUsabilitiesPD();
		TreeMap<String, ExtendedInteger> pool2allowedUsagesCount = new TreeMap<>();
		for (LicenseUsableForEntry licenseUsability : licenseUsabilities) {
			String pool = licenseUsability.getLicensePoolId();
			String licenseId = licenseUsability.getLicenseId();

			// value up this step
			ExtendedInteger count = pool2allowedUsagesCount.get(pool);

			Map<String, LicenseEntry> licenses = licenseDataService.getLicensesPD();

			// not yet initialized
			if (count == null) {
				count = licenses.get(licenseId).getMaxInstallations();
				pool2allowedUsagesCount.put(pool, count);
			} else {
				ExtendedInteger result = count.add(licenses.get(licenseId).getMaxInstallations());
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
		// require this licensepool
		// add the clients which have this software installed
		TreeMap<String, TreeSet<String>> pool2clients = new TreeMap<>();
		// we take Set since we count only one usage per client
		AuditSoftwareXLicensePool auditSoftwareXLicensePool = getAuditSoftwareXLicensePoolPD();
		List<String> opsiHostNames = hostInfoCollections.getOpsiHostNames();
		Map<String, Set<String>> swId2clients = getSoftwareIdentOnClients(opsiHostNames);
		for (StringValuedRelationElement swXpool : auditSoftwareXLicensePool) {
			Logging.debug(this, " retrieveStatistics1 relationElement  " + swXpool);
			String pool = swXpool.get(LicensepoolEntry.ID_SERVICE_KEY);

			TreeSet<String> clientsServedByPool = pool2clients.computeIfAbsent(pool, s -> new TreeSet<>());

			String swIdent = swXpool.get(AuditSoftwareXLicensePool.SW_ID);

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
				Set<String> clientsWithThisSW = softwareIdent2clients.computeIfAbsent(clientEntry.getSWIdent(),
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
