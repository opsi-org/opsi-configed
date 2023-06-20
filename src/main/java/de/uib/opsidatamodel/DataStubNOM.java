/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel;

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
import de.uib.configed.Globals;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
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
import de.uib.opsicommand.JSONthroughHTTPS;
import de.uib.opsicommand.OpsiMethodCall;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.table.ListCellOptions;

public class DataStubNOM {

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

	private OpsiserviceNOMPersistenceController persistenceController;

	private Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos;

	private Object2Product2VersionList depot2LocalbootProducts;
	private Object2Product2VersionList depot2NetbootProducts;
	private List<List<Object>> productRows;
	private Map<String, TreeSet<OpsiPackage>> depot2Packages;
	private Map<String, Map<String, List<String>>> product2VersionInfo2Depots;

	// depotId-->productId --> (propertyId --> value)
	private Map<String, Map<String, Map<String, ListCellOptions>>> depot2Product2PropertyDefinitions;

	// depotId-->productId --> (dependencyKey--> value)
	private Map<String, Map<String, List<Map<String, String>>>> depot2product2dependencyInfos;

	private List<Map<String, Object>> productPropertyStates;

	// will only be refreshed when all product data are refreshed
	private List<Map<String, Object>> productPropertyDepotStates;

	private Set<String> hostsWithProductProperties;

	private NavigableMap<String, SWAuditEntry> installedSoftwareInformation;
	private NavigableMap<String, SWAuditEntry> installedSoftwareInformationForLicensing;

	// giving the idents which have the name in their ident
	private NavigableMap<String, Set<String>> name2SWIdents;
	private NavigableMap<String, Map<String, String>> installedSoftwareName2SWinfo;

	// List of idents of software
	private List<String> softwareList;

	// the same with a numbering index
	private NavigableMap<String, Integer> software2Number;

	private Map<String, List<SWAuditClientEntry>> client2software;
	private Map<String, Set<String>> softwareIdent2clients;

	private AuditSoftwareXLicencePool auditSoftwareXLicencePool;

	private Map<String, Map<String, Object>> hostConfigs;

	private NavigableMap<String, LicencepoolEntry> licencepools;

	private Map<String, LicenceContractEntry> licenceContracts;

	private NavigableMap<String, NavigableSet<String>> contractsExpired;
	// date in sql time format, contrad ID
	private NavigableMap<String, NavigableSet<String>> contractsToNotify;
	// date in sql time format, contrad ID

	private List<LicenceUsableForEntry> licenceUsabilities;

	private List<LicenceUsageEntry> licenceUsages;

	private LicencePoolXOpsiProduct licencePoolXOpsiProduct;

	private Map<String, Map<String, Object>> client2HwRows;

	private List<Map<String, Object>> healthData;

	private Map<String, Object> diagnosticData;

	private Map<String, LicenceEntry> licences;

	// We need the argument here since the controller is not yet loaded when calling this constructor
	public DataStubNOM(OpsiserviceNOMPersistenceController persistenceController) {
		this.persistenceController = persistenceController;
		classCounter++;
	}

	public void productDataRequestRefresh() {
		product2versionInfoRequestRefresh();
		productsAllDepotsRequestRefresh();
		productPropertyDefinitionsRequestRefresh();
		productPropertyStatesRequestRefresh();
		productPropertyDepotStatesRequestRefresh();
		productDependenciesRequestRefresh();
	}

	// netbootStatesAndActions
	// localbootStatesAndActions

	// can only return true if overriden in a subclass
	public boolean canCallMySQL() {

		// we cannot call MySQL if version before 4.3
		if (JSONthroughHTTPS.isOpsi43()) {
			return false;
		}

		boolean result = false;

		// test if we can access any table

		String query = "select  *  from " + SWAuditClientEntry.DB_TABLE_NAME + " LIMIT 1 ";

		Logging.info(this, "test, query " + query);

		result = persistenceController.exec.doCall(new OpsiMethodCall("getRawData", new Object[] { query }));

		Logging.info(this, "test result " + result);

		return result;
	}

	public void product2versionInfoRequestRefresh() {
		product2versionInfo2infos = null;
	}

	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		retrieveProductInfos();
		return product2versionInfo2infos;
	}

	private void retrieveProductInfos() {
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

			List<Map<String, Object>> retrievedList = persistenceController.retrieveListOfMapsNOM(callAttributes,
					callFilter, "product_getObjects");

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

			persistenceController.notifyDataRefreshedObservers("product");
		}

	}

	public void productsAllDepotsRequestRefresh() {
		depot2LocalbootProducts = null;
	}

	public Map<String, TreeSet<OpsiPackage>> getDepot2Packages() {
		retrieveProductsAllDepots();
		return depot2Packages;
	}

	public List<List<Object>> getProductRows() {
		retrieveProductsAllDepots();
		return productRows;
	}

	public Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots() {
		retrieveProductsAllDepots();
		return product2VersionInfo2Depots;
	}

	public Object2Product2VersionList getDepot2LocalbootProducts() {
		retrieveProductsAllDepots();
		return depot2LocalbootProducts;
	}

	public Object2Product2VersionList getDepot2NetbootProducts() {
		retrieveProductsAllDepots();
		return depot2NetbootProducts;
	}

	private void retrieveProductsAllDepots() {
		Logging.debug(this, "retrieveProductsAllDepots ? ");
		if (depot2LocalbootProducts != null) {
			Logging.debug(this, "depot2LocalbootProducts " + depot2LocalbootProducts.size());
		}
		if (depot2NetbootProducts != null) {
			Logging.debug(this, "depot2NetbootProducts" + depot2NetbootProducts.size());
		}
		retrieveProductInfos();

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

			List<Map<String, Object>> packages = persistenceController.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productOnDepot_getObjects");

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

				Map<String, List<String>> versionInfo2Depots = product2VersionInfo2Depots.get(p.getProductId());
				if (versionInfo2Depots == null) {
					versionInfo2Depots = new HashMap<>();
					product2VersionInfo2Depots.put(p.getProductId(), versionInfo2Depots);
				}

				List<String> depotsWithThisVersion = versionInfo2Depots.get(p.getVersionInfo());

				if (depotsWithThisVersion == null) {
					depotsWithThisVersion = new ArrayList<>();
					versionInfo2Depots.put(p.getVersionInfo(), depotsWithThisVersion);
				}
				depotsWithThisVersion.add(depot);

				TreeSet<OpsiPackage> depotpackages = depot2Packages.computeIfAbsent(depot, s -> new TreeSet<>());
				depotpackages.add(p);

				List<Object> productRow = new ArrayList<>();

				productRow.add(p.getProductId());

				String productName = null;

				try {
					productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo())
							.getProductName();

					productRow.add(productName);
					p.appendValues(productRow);

					if (depotsWithThisVersion.size() == 1) {
						productRows.add(productRow);
					}
				} catch (Exception ex) {
					Logging.warning(this, "retrieveProductsAllDepots exception " + ex);
					Logging.warning(this, "retrieveProductsAllDepots exception for package  " + p);
					Logging.warning(this, "retrieveProductsAllDepots exception productId  " + p.getProductId());

					Logging.warning(this, "retrieveProductsAllDepots exception for product2versionInfo2infos: of size "
							+ product2versionInfo2infos.size());
					Logging.warning(this,
							"retrieveProductsAllDepots exception for product2versionInfo2infos.get(p.getProductId()) "
									+ product2versionInfo2infos.get(p.getProductId()));
					if (product2versionInfo2infos.get(p.getProductId()) == null) {
						Logging.warning(this, "retrieveProductsAllDepots : product " + p.getProductId()
								+ " seems not to exist in product table");
					}
				}
			}

			persistenceController.notifyDataRefreshedObservers("productOnDepot");
		}
	}

	public void productPropertyDefinitionsRequestRefresh() {
		depot2Product2PropertyDefinitions = null;
	}

	public Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitions() {
		retrieveAllProductPropertyDefinitions();
		return depot2Product2PropertyDefinitions;
	}

	private void retrieveAllProductPropertyDefinitions() {
		retrieveProductsAllDepots();

		if (depot2Product2PropertyDefinitions == null) {
			depot2Product2PropertyDefinitions = new HashMap<>();

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			List<Map<String, Object>> retrieved = persistenceController.retrieveListOfMapsNOM(callAttributes,
					callFilter, "productProperty_getObjects");

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
				String versionInfo = productVersion + Globals.ProductPackageVersionSeparator.FOR_KEY + packageVersion;

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

			Logging.debug(this, "retrieveAllProductPropertyDefinitions ");

			persistenceController.notifyDataRefreshedObservers("productProperty");
		}
	}

	public void productDependenciesRequestRefresh() {
		depot2product2dependencyInfos = null;
	}

	public Map<String, Map<String, List<Map<String, String>>>> getDepot2product2dependencyInfos() {
		retrieveAllProductDependencies();
		return depot2product2dependencyInfos;
	}

	private void retrieveAllProductDependencies() {
		retrieveProductsAllDepots();

		if (depot2product2dependencyInfos == null) {
			depot2product2dependencyInfos = new HashMap<>();

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			List<Map<String, Object>> retrievedList = persistenceController.retrieveListOfMapsNOM(callAttributes,
					callFilter, "productDependency_getObjects");

			for (Map<String, Object> dependencyItem : retrievedList) {
				String productId = "" + dependencyItem.get(OpsiPackage.DB_KEY_PRODUCT_ID);

				String productVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PRODUCT_VERSION);
				String packageVersion = "" + dependencyItem.get(OpsiPackage.SERVICE_KEY_PACKAGE_VERSION);
				String versionInfo = productVersion + Globals.ProductPackageVersionSeparator.FOR_KEY + packageVersion;

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

			persistenceController.notifyDataRefreshedObservers("productDependency");
		}
	}

	public void productPropertyStatesRequestRefresh() {
		Logging.info(this, "productPropertyStatesRequestRefresh");
		productPropertyStates = null;
		hostsWithProductProperties = null;
	}

	public List<Map<String, Object>> getProductPropertyStates() {
		retrieveProductPropertyStates();
		return productPropertyStates;
	}

	private void productPropertyDepotStatesRequestRefresh() {
		Logging.info(this, "productPropertyDepotStatesRequestRefresh");
		productPropertyDepotStates = null;
	}

	public List<Map<String, Object>> getProductPropertyDepotStates(Set<String> depots) {
		retrieveProductPropertyDepotStates(depots);
		return productPropertyDepotStates;
	}

	public void fillProductPropertyStates(Collection<String> clients) {
		Logging.info(this, "fillProductPropertyStates for " + clients);
		if (productPropertyStates == null) {
			productPropertyStates = produceProductPropertyStates(clients, hostsWithProductProperties);
		} else {
			productPropertyStates.addAll(produceProductPropertyStates(clients, hostsWithProductProperties));
		}
	}

	private void retrieveProductPropertyStates() {
		produceProductPropertyStates((Collection<String>) null, hostsWithProductProperties);
	}

	private void retrieveProductPropertyDepotStates(Set<String> depots) {
		Logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots + " depotStates == null "
				+ (productPropertyDepotStates == null));
		if (productPropertyDepotStates == null) {
			productPropertyDepotStates = produceProductPropertyStates(depots, null);
		}

		Logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
	}

	// client is a set of added hosts, host represents the totality and will be
	// updated as a side effect
	private List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients,
			Set<String> hosts) {
		Logging.info(this, "produceProductPropertyStates new hosts " + clients + " old hosts " + hosts);
		List<String> newClients = null;
		if (clients == null) {
			newClients = new ArrayList<>();
		} else {
			newClients = new ArrayList<>(clients);
		}

		if (hosts == null) {
			hosts = new HashSet<>();
		} else {
			newClients.removeAll(hosts);
		}

		List<Map<String, Object>> result = null;

		if (newClients.isEmpty()) {
			// look if propstates is initialized
			result = new ArrayList<>();
		} else {
			hosts.addAll(newClients);

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("objectId", AbstractExecutioner.jsonArray(newClients));

			result = persistenceController.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productPropertyState_getObjects");
		}

		Logging.info(this, "produceProductPropertyStates for hosts " + hosts);

		return result;
	}

	public void installedSoftwareInformationRequestRefresh() {
		installedSoftwareInformation = null;
		installedSoftwareInformationForLicensing = null;
		name2SWIdents = null;
	}

	public List<String> getSoftwareList() {
		retrieveInstalledSoftwareInformation();
		return softwareList;
	}

	public NavigableMap<String, Integer> getSoftware2Number() {
		retrieveInstalledSoftwareInformation();
		return software2Number;
	}

	public String getSWident(Integer i) {
		Logging.debug(this, "getSWident for " + i);
		retrieveInstalledSoftwareInformation();
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
				installedSoftwareInformationRequestRefresh();
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

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformation() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareInformation;
	}

	public NavigableMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareInformationForLicensing;
	}

	public NavigableMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareName2SWinfo;
	}

	public NavigableMap<String, Set<String>> getName2SWIdents() {
		retrieveInstalledSoftwareInformation();
		return name2SWIdents;
	}

	private void retrieveInstalledSoftwareInformation() {
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

			List<Map<String, Object>> li = persistenceController.retrieveListOfMapsNOM(callAttributes, callFilter,
					"auditSoftware_getHashes");

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

			persistenceController.notifyDataRefreshedObservers("software");
		}
	}

	public void softwareAuditOnClientsRequestRefresh() {
		Logging.info(this, "softwareAuditOnClientsRequestRefresh");
		client2software = null;
		softwareIdent2clients = null;
	}

	public void fillClient2Software(String client) {
		Logging.info(this, "fillClient2Software " + client);
		if (client2software == null) {
			retrieveSoftwareAuditOnClients(client);

			return;
		}

		if (client2software.get(client) == null) {
			retrieveSoftwareAuditOnClients(client);
		}
	}

	public void fillClient2Software(List<String> clients) {
		if (clients == null) {
			Logging.info(this, "fillClient2Software for clients null");
		} else {
			Logging.info(this, "fillClient2Software for clients " + clients.size());
		}
		retrieveSoftwareAuditOnClients(clients);
	}

	public Map<String, List<SWAuditClientEntry>> getClient2Software() {
		Logging.info(this, "getClient2Software");
		retrieveInstalledSoftwareInformation();
		return client2software;
	}

	public Map<String, Set<String>> getSoftwareIdent2clients() {
		return softwareIdent2clients;
	}

	private void retrieveSoftwareAuditOnClients(String client) {
		List<String> clients = new ArrayList<>();
		clients.add(client);
		retrieveSoftwareAuditOnClients(clients);
	}

	private void retrieveSoftwareAuditOnClients(final List<String> clients) {
		Logging.info(this, "retrieveSoftwareAuditOnClients used memory on start " + Globals.usedMemory());

		retrieveInstalledSoftwareInformation();
		Logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  clients count: " + clients.size());

		List<String> newClients = new ArrayList<>(clients);

		if (client2software != null) {
			Logging.info(this,
					"retrieveSoftwareAuditOnClients client2Software.keySet size: " + client2software.keySet().size());

			newClients.removeAll(client2software.keySet());
		}

		Logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  new clients count: " + newClients.size());

		final int STEP_SIZE = 100;

		if (client2software == null || softwareIdent2clients == null || !newClients.isEmpty()) {
			while (!newClients.isEmpty()) {
				List<String> clientListForCall = new ArrayList<>();

				for (int i = 0; i < STEP_SIZE && i < newClients.size(); i++) {
					clientListForCall.add(newClients.get(i));
				}

				newClients.removeAll(clientListForCall);

				if (client2software == null) {
					client2software = new HashMap<>();
				}

				if (softwareIdent2clients == null) {
					softwareIdent2clients = new HashMap<>();
				}

				Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

				String[] callAttributes = new String[] {};
				Map<String, Object> callFilter = new HashMap<>();
				callFilter.put("state", 1);
				if (newClients != null) {
					callFilter.put("clientId", AbstractExecutioner.jsonArray(clientListForCall));
				}

				List<Map<String, Object>> softwareAuditOnClients = persistenceController
						.retrieveListOfMapsNOM(callAttributes, callFilter, "auditSoftwareOnClient_getHashes");

				Logging.info(this, "retrieveSoftwareAuditOnClients, finished a request, map size "
						+ softwareAuditOnClients.size());

				for (String clientId : clientListForCall) {
					client2software.put(clientId, new LinkedList<>());
				}

				for (Map<String, Object> item : softwareAuditOnClients) {

					SWAuditClientEntry clientEntry = new SWAuditClientEntry(item);

					String clientId = clientEntry.getClientId();
					String swIdent = clientEntry.getSWident();

					Set<String> clientsWithThisSW = softwareIdent2clients.computeIfAbsent(swIdent,
							s -> new HashSet<>());

					clientsWithThisSW.add(clientId);

					// null not allowed in mysql
					if (clientId != null) {
						List<SWAuditClientEntry> entries = client2software.get(clientId);

						entries.add(clientEntry);
					}

				}

				Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");

			}

			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Globals.usedMemory());
			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Globals.usedMemory());
			persistenceController.notifyDataRefreshedObservers("softwareConfig");
		}
	}

	public void auditSoftwareXLicencePoolRequestRefresh() {
		Logging.info(this, "auditSoftwareXLicencePoolRequestRefresh");
		auditSoftwareXLicencePool = null;
	}

	public AuditSoftwareXLicencePool getAuditSoftwareXLicencePool() {
		retrieveAuditSoftwareXLicencePool();
		return auditSoftwareXLicencePool;
	}

	// AUDIT_SOFTWARE_TO_LICENSE_POOL
	private void retrieveAuditSoftwareXLicencePool() {
		if (auditSoftwareXLicencePool != null) {
			return;
		}

		Logging.info(this, "retrieveAuditSoftwareXLicencePool");

		List<Map<String, Object>> retrieved = persistenceController.retrieveListOfMapsNOM(
				AuditSoftwareXLicencePool.SERVICE_ATTRIBUTES, new HashMap<>(), // callFilter
				"auditSoftwareToLicensePool_getObjects");

		auditSoftwareXLicencePool = new AuditSoftwareXLicencePool();

		for (Map<String, Object> map : retrieved) {
			auditSoftwareXLicencePool.integrateRaw(map);
		}

		Logging.info(this, "retrieveAuditSoftwareXLicencePool retrieved ");
	}

	public void hostConfigsRequestRefresh() {
		Logging.info(this, "hostConfigsRequestRefresh");
		hostConfigs = null;
	}

	public Map<String, Map<String, Object>> getConfigs() {
		retrieveHostConfigs();
		return hostConfigs;
	}

	private void retrieveHostConfigs() {
		if (hostConfigs != null) {
			return;
		}

		Logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		List<Map<String, Object>> retrieved = persistenceController.retrieveListOfMapsNOM(callAttributes, callFilter,
				"configState_getObjects");
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

		persistenceController.notifyDataRefreshedObservers("configState");
	}

	public void licencepoolsRequestRefresh() {
		Logging.info(this, "licencepoolsRequestRefresh");
		licencepools = null;
	}

	public Map<String, LicencepoolEntry> getLicencepools() {
		retrieveLicencepools();
		return licencepools;
	}

	private void retrieveLicencepools() {
		if (licencepools != null) {
			return;
		}

		licencepools = new TreeMap<>();

		if (persistenceController.isWithLicenceManagement()) {
			String[] attributes = new String[] { LicencepoolEntry.ID_KEY, LicencepoolEntry.DESCRIPTION_KEY };

			List<Map<String, Object>> retrieved = persistenceController.retrieveListOfMapsNOM(attributes,
					new HashMap<>(), "licensePool_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicencepoolEntry entry = new LicencepoolEntry(importedEntry);
				licencepools.put(entry.getLicencepoolId(), entry);
			}
		}
	}

	public void licenceContractsRequestRefresh() {
		Logging.info(this, "licenceContractsRequestRefresh");

		licenceContracts = null;
		contractsExpired = null;
		contractsToNotify = null;
	}

	public Map<String, LicenceContractEntry> getLicenceContracts() {
		retrieveLicenceContracts();
		return licenceContracts;
	}

	// date in sql time format, contract ID
	public NavigableMap<String, NavigableSet<String>> getLicenceContractsExpired() {
		retrieveLicenceContracts();
		return contractsExpired;
	}

	// date in sql time format, contract ID
	public NavigableMap<String, NavigableSet<String>> getLicenceContractsToNotify() {
		retrieveLicenceContracts();
		return contractsToNotify;
	}

	// LICENSE_CONTRACT
	private void retrieveLicenceContracts() {
		if (licenceContracts != null) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		licenceContracts = new HashMap<>();
		contractsToNotify = new TreeMap<>();
		contractsExpired = new TreeMap<>();

		if (persistenceController.isWithLicenceManagement()) {

			List<Map<String, Object>> retrieved = persistenceController
					.retrieveListOfMapsNOM("licenseContract_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);

				String notiDate = entry.get(TableLicenceContracts.NOTIFICATION_DATE_KEY);
				if (notiDate != null && notiDate.trim().length() > 0 && notiDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsToNotify.computeIfAbsent(notiDate,
							s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}

				String expireDate = entry.get(TableLicenceContracts.EXPIRATION_DATE_KEY);
				if (expireDate != null && expireDate.trim().length() > 0 && expireDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsExpired.computeIfAbsent(expireDate,
							s -> new TreeSet<>());

					contractSet.add(entry.getId());
				}
			}

			Logging.info(this, "contractsToNotify " + contractsToNotify);
			Logging.info(this, "contractsExpired " + contractsExpired);
		}
	}

	public void licencesRequestRefresh() {
		Logging.info(this, "licencesRequestRefresh");
		licences = null;
	}

	public Map<String, LicenceEntry> getLicences() {
		retrieveLicences();
		return licences;
	}

	// SOFTWARE_LICENSE
	private void retrieveLicences() {
		if (licences != null) {
			return;
		}

		licences = new HashMap<>();

		if (persistenceController.isWithLicenceManagement()) {

			List<Map<String, Object>> retrieved = persistenceController
					.retrieveListOfMapsNOM("softwareLicense_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceEntry entry = new LicenceEntry(importedEntry);
				licences.put(entry.getId(), entry);
			}
		}
	}

	public void licenceUsabilitiesRequestRefresh() {
		Logging.info(this, "licenceUsabilitiesRequestRefresh");
		licenceUsabilities = null;
	}

	public List<LicenceUsableForEntry> getLicenceUsabilities() {
		retrieveLicenceUsabilities();
		return licenceUsabilities;
	}

	// SOFTWARE_LICENSE_TO_LICENSE_POOL
	private void retrieveLicenceUsabilities() {
		if (licenceUsabilities != null) {
			return;
		}

		licenceUsabilities = new ArrayList<>();

		if (persistenceController.isWithLicenceManagement()) {

			List<Map<String, Object>> retrieved = persistenceController
					.retrieveListOfMapsNOM("softwareLicenseToLicensePool_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsableForEntry entry = LicenceUsableForEntry.produceFrom(importedEntry);
				licenceUsabilities.add(entry);
			}
		}
	}

	public void licenceUsagesRequestRefresh() {
		Logging.info(this, "licenceUsagesRequestRefresh");
		licenceUsages = null;
	}

	public List<LicenceUsageEntry> getLicenceUsages() {
		retrieveLicenceUsages();
		return licenceUsages;
	}

	// LICENSE_ON_CLIENT
	private void retrieveLicenceUsages() {
		Logging.info(this, "retrieveLicenceUsages");
		if (licenceUsages != null) {
			return;
		}

		licenceUsages = new ArrayList<>();

		if (persistenceController.isWithLicenceManagement()) {

			List<Map<String, Object>> retrieved = persistenceController
					.retrieveListOfMapsNOM("licenseOnClient_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsageEntry entry = new LicenceUsageEntry(importedEntry);

				licenceUsages.add(entry);
			}
		}
	}

	public void licencePoolXOpsiProductRequestRefresh() {
		Logging.info(this, "licencePoolXOpsiProductRequestRefresh");
		licencePoolXOpsiProduct = null;
	}

	public LicencePoolXOpsiProduct getLicencePoolXOpsiProduct() {
		retrieveLicencePoolXOpsiProduct();
		return licencePoolXOpsiProduct;
	}

	// LICENSE_POOL
	private void retrieveLicencePoolXOpsiProduct() {
		if (licencePoolXOpsiProduct != null) {
			return;
		}

		Logging.info(this, "retrieveLicencePoolXOpsiProduct");

		List<Map<String, Object>> retrieved = persistenceController.retrieveListOfMapsNOM(
				LicencePoolXOpsiProduct.SERVICE_ATTRIBUTES_asArray, new HashMap<>(), // callFilter
				"licensePool_getObjects");
		// integrates two database calls

		licencePoolXOpsiProduct = new LicencePoolXOpsiProduct();

		for (Map<String, Object> map : retrieved) {
			licencePoolXOpsiProduct.integrateRawFromService(map);
		}
	}

	public void client2HwRowsRequestRefresh() {
		Logging.info(this, "client2HwRowsRequestRefresh");
		client2HwRows = null;
	}

	private void retrieveClient2HwRows(String[] hosts) {
		Logging.info(this, "retrieveClient2HwRows( hosts )  for hosts " + hosts.length);

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

		for (String hwClass : persistenceController.getHwInfoClassNames()) {
			Logging.info(this, "retrieveClient2HwRows hwClass " + hwClass);

			Map<String, Map<String, Object>> client2ClassInfos = client2HwRowsForHwClass(hwClass);

			if (!client2ClassInfos.isEmpty()) {
				for (Entry<String, Map<String, Object>> client2ClassInfo : client2ClassInfos.entrySet()) {
					Map<String, Object> allInfosForAClient = client2HwRows.get(client2ClassInfo.getKey());
					// find max lastseen time as last scan time

					String lastseen1 = (String) allInfosForAClient
							.get(OpsiserviceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					String lastseen2 = (String) client2ClassInfo.getValue()
							.get(OpsiserviceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME);
					if (lastseen1 != null && lastseen2 != null) {
						client2ClassInfo.getValue().put(OpsiserviceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME,
								maxTime(lastseen1, lastseen2));
					}

					allInfosForAClient.putAll(client2ClassInfo.getValue());
				}
			}
		}

		Logging.info(this, "retrieveClient2HwRows result size " + client2HwRows.size());

		timeCheck.stop();
		Logging.info(this, "retrieveClient2HwRows finished  ");
		persistenceController.notifyDataRefreshedObservers("client2HwRows");

	}

	private Map<String, Map<String, Object>> client2HwRowsForHwClass(String hwClass) {
		Logging.info(this, "client2HwRowsForHwClass " + hwClass);

		if (client2HwRows == null) {
			return new HashMap<>();
		}

		// z.B. hwClass is DISK_PARTITION

		List<String> specificColumns = new ArrayList<>();
		specificColumns.add("HOST.hostId");

		StringBuilder buf = new StringBuilder("select HOST.hostId, ");
		StringBuilder cols = new StringBuilder("");

		String configTable = OpsiserviceNOMPersistenceController.HW_INFO_CONFIG + hwClass;

		String lastseenCol = configTable + "." + "lastseen";
		specificColumns.add(lastseenCol);
		buf.append(lastseenCol);
		buf.append(", ");

		boolean foundAnEntry = false;

		// build and collect database columnnames
		for (String hwInfoCol : persistenceController.getClient2HwRowsColumnNames()) {
			if (hwInfoCol.startsWith("HOST.")
					|| hwInfoCol.equals(OpsiserviceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME)) {
				continue;
			}

			Logging.info(this,
					"hwInfoCol " + hwInfoCol + " look for " + OpsiserviceNOMPersistenceController.HW_INFO_DEVICE
							+ " as well as " + OpsiserviceNOMPersistenceController.HW_INFO_CONFIG);
			String part0 = hwInfoCol.substring(0, OpsiserviceNOMPersistenceController.HW_INFO_DEVICE.length());

			boolean colFound = false;
			// check if colname is from a CONFIG or a DEVICE table
			if (hwInfoCol.startsWith(hwClass, part0.length())) {
				colFound = true;
				// we found a DEVICE column name
			} else {
				part0 = hwInfoCol.substring(0, OpsiserviceNOMPersistenceController.HW_INFO_CONFIG.length());

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

		String deviceTable = OpsiserviceNOMPersistenceController.HW_INFO_DEVICE + hwClass;

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

		List<List<String>> rows = persistenceController.exec
				.getListOfStringLists(new OpsiMethodCall("getRawData", new Object[] { query })

				);
		Logging.info(this, "retrieveClient2HwRows, finished a request");
		Logging.info(this, "retrieveClient2HwRows, got rows for class " + hwClass);
		Logging.info(this, "retrieveClient2HwRows, got rows,  size  " + rows.size());

		// shrink to one line per client

		Map<String, Map<String, Object>> clientInfo = new HashMap<>();

		for (List<String> row : rows) {
			Map<String, Object> rowMap = clientInfo.get(row.get(0));
			if (rowMap == null) {
				rowMap = new HashMap<>();
				clientInfo.put(row.get(0), rowMap);
			}

			for (int i = 1; i < specificColumns.size(); i++) {
				Object value = rowMap.get(specificColumns.get(i));
				String valInRow = row.get(i);
				if (valInRow == null || valInRow.equals("null")) {
					valInRow = "";
				}

				if (value == null) {
					value = valInRow;
				} else {
					value = value + "|" + valInRow;
				}

				if (specificColumns.get(i).equals(lastseenCol)) {
					String timeS = maxTime((String) value, row.get(i));
					rowMap.put(OpsiserviceNOMPersistenceController.LAST_SEEN_VISIBLE_COL_NAME, timeS);
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

		if (time0 == null || time0.equals("")) {
			return time1;
		}

		if (time1 == null || time1.equals("")) {
			return time0;
		}

		if (time0.compareTo(time1) < 0) {
			return time1;
		}

		return time0;
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		retrieveClient2HwRows(hosts);
		return client2HwRows;
	}

	public List<Map<String, Object>> checkHealth() {
		retrieveHealthData();
		return healthData;
	}

	private void retrieveHealthData() {
		healthData = persistenceController.retrieveListOfMapsNOM("service_healthCheck", new Object[0]);
	}

	public Map<String, Object> getDiagnosticData() {
		retrieveDiagnosticData();
		return diagnosticData;
	}

	private void retrieveDiagnosticData() {
		diagnosticData = persistenceController.retrieveMapNOM("service_getDiagnosticData", new Object[0]);
	}
}
