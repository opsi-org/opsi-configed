/**
 *
 *  copyright:     Copyright (c) 2014-2018
 *  organization: uib.de
 * @author  R. Roeder
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
import de.uib.opsicommand.JSONReMapper;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.table.ListCellOptions;

public class DataStubNOM {

	protected static final Set<String> linuxSWnameMarkers = new HashSet<>();
	static {
		linuxSWnameMarkers.add("linux");
		linuxSWnameMarkers.add("Linux");
		linuxSWnameMarkers.add("lib");
		linuxSWnameMarkers.add("ubuntu");
		linuxSWnameMarkers.add("ubuntu");
	}

	protected static final Set<String> linuxSubversionMarkers = new HashSet<>();
	static {
		linuxSubversionMarkers.add("lin:");
	}
	protected static Integer classCounter = 0;

	private OpsiserviceNOMPersistenceController persist;

	protected Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos;

	protected Object2Product2VersionList depot2LocalbootProducts;
	protected Object2Product2VersionList depot2NetbootProducts;
	protected List<List<Object>> productRows;
	protected Map<String, TreeSet<OpsiPackage>> depot2Packages;
	protected Map<String, Map<String, List<String>>> product2VersionInfo2Depots;

	// depotId-->productId --> (propertyId --> value)
	protected Map<String, Map<String, Map<String, ListCellOptions>>> depot2Product2PropertyDefinitions;

	// depotId-->productId --> (dependencyKey--> value)
	protected Map<String, Map<String, List<Map<String, String>>>> depot2product2dependencyInfos;

	protected List<Map<String, Object>> productPropertyStates;

	// will only be refreshed when all product data are refreshed
	protected List<Map<String, Object>> productPropertyDepotStates;

	protected Set<String> hostsWithProductProperties;

	protected NavigableMap<String, SWAuditEntry> installedSoftwareInformation;
	protected NavigableMap<String, SWAuditEntry> installedSoftwareInformationForLicensing;

	// giving the idents which have the name in their ident
	protected NavigableMap<String, Set<String>> name2SWIdents;
	protected NavigableMap<String, Map<String, String>> installedSoftwareName2SWinfo;
	protected NavigableMap<String, Map<String, Map<String, String>>> name2ident2infoWithPool;

	// List of idents of software
	protected List<String> softwareList;

	// the same with a numbering index
	protected NavigableMap<String, Integer> software2Number;

	protected Map<String, List<SWAuditClientEntry>> client2software;
	protected Map<String, Set<String>> softwareIdent2clients;

	protected AuditSoftwareXLicencePool auditSoftwareXLicencePool;

	protected Map<String, Map<String, Object>> hostConfigs;

	protected NavigableMap<String, LicencepoolEntry> licencepools;

	protected Map<String, LicenceContractEntry> licenceContracts;

	protected NavigableMap<String, NavigableSet<String>> contractsExpired;
	// date in sql time format, contrad ID
	protected NavigableMap<String, NavigableSet<String>> contractsToNotify;
	// date in sql time format, contrad ID

	protected List<LicenceUsableForEntry> licenceUsabilities;

	protected List<LicenceUsageEntry> licenceUsages;

	protected LicencePoolXOpsiProduct licencePoolXOpsiProduct;

	protected Map<String, Map<String, Object>> client2HwRows;

	protected List<Map<String, Object>> healthData;

	protected Map<String, LicenceEntry> licences;

	public DataStubNOM(OpsiserviceNOMPersistenceController controller) {
		this.persist = controller;
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
		return false;
	}

	public void product2versionInfoRequestRefresh() {
		product2versionInfo2infos = null;
	}

	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		retrieveProductInfos();
		return product2versionInfo2infos;
	}

	protected void retrieveProductInfos() {
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

			persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.loadtable") + " product");

			List<Map<String, Object>> retrievedList = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"product_getObjects");

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

			persist.notifyDataRefreshedObservers("product");
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

	protected void retrieveProductsAllDepots() {
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
			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " productOnDepot");
			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			List<Map<String, Object>> packages = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productOnDepot_getObjects");

			depot2LocalbootProducts = new Object2Product2VersionList();
			depot2NetbootProducts = new Object2Product2VersionList();
			product2VersionInfo2Depots = new HashMap<>();

			productRows = new ArrayList<>();

			depot2Packages = new HashMap<>();

			for (Map<String, Object> m : packages) {
				String depot = "" + m.get("depotId");

				if (!persist.hasDepotPermission(depot)) {
					continue;
				}

				OpsiPackage p = new OpsiPackage(m);

				Logging.debug(this, "retrieveProductsAllDepots, opsi package " + p);

				if (p.isNetbootProduct()) {
					depot2NetbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				} else if (p.isLocalbootProduct()) {
					depot2LocalbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
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

				TreeSet<OpsiPackage> depotpackages = depot2Packages.get(depot);
				if (depotpackages == null) {
					depotpackages = new TreeSet<>();
					depot2Packages.put(depot, depotpackages);
				}
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

			persist.notifyDataRefreshedObservers("productOnDepot");
		}
	}

	public void productPropertyDefinitionsRequestRefresh() {
		depot2Product2PropertyDefinitions = null;
	}

	public Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitions() {
		retrieveAllProductPropertyDefinitions();
		return depot2Product2PropertyDefinitions;
	}

	protected void retrieveAllProductPropertyDefinitions() {
		retrieveProductsAllDepots();

		if (depot2Product2PropertyDefinitions == null) {
			depot2Product2PropertyDefinitions = new HashMap<>();

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " product property");

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productProperty_getObjects");

			Iterator<Map<String, Object>> iter = retrieved.iterator();

			while (iter.hasNext()) {

				Map<String, Object> retrievedMap = iter.next();
				Map<String, Object> adaptedMap = new HashMap<>(retrievedMap);
				// rebuild JSON objects
				Iterator<String> iterInner = retrievedMap.keySet().iterator();
				while (iterInner.hasNext()) {
					String key = iterInner.next();
					adaptedMap.put(key, JSONReMapper.deriveStandard(retrievedMap.get(key)));
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
								.get(depot);
						if (product2PropertyDefinitions == null) {
							product2PropertyDefinitions = new HashMap<>();
							depot2Product2PropertyDefinitions.put(depot, product2PropertyDefinitions);
						}

						Map<String, ListCellOptions> propertyDefinitions = product2PropertyDefinitions.get(productId);

						if (propertyDefinitions == null) {
							propertyDefinitions = new HashMap<>();
							product2PropertyDefinitions.put(productId, propertyDefinitions);
						}

						propertyDefinitions.put(propertyId, productPropertyMap);
					}
				}
			}

			Logging.debug(this, "retrieveAllProductPropertyDefinitions ");

			persist.notifyDataRefreshedObservers("productProperty");
		}
	}

	public void productDependenciesRequestRefresh() {
		depot2product2dependencyInfos = null;
	}

	public Map<String, Map<String, List<Map<String, String>>>> getDepot2product2dependencyInfos() {
		retrieveAllProductDependencies();
		return depot2product2dependencyInfos;
	}

	protected void retrieveAllProductDependencies() {
		retrieveProductsAllDepots();

		if (depot2product2dependencyInfos == null) {
			depot2product2dependencyInfos = new HashMap<>();

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " product dependency");

			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();

			List<Map<String, Object>> retrievedList = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productDependency_getObjects");

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

				if ((product2VersionInfo2Depots == null) || (product2VersionInfo2Depots.get(productId) == null)
						|| (product2VersionInfo2Depots.get(productId).get(versionInfo) == null)) {
					Logging.warning(this, "unexpected null for product2VersionInfo2Depots productId, versionInfo   "
							+ productId + ", " + versionInfo);
					continue;
				}
				for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
					Map<String, List<Map<String, String>>> product2dependencyInfos = depot2product2dependencyInfos
							.get(depot);
					if (product2dependencyInfos == null) {
						product2dependencyInfos = new HashMap<>();
						depot2product2dependencyInfos.put(depot, product2dependencyInfos);
					}

					List<Map<String, String>> dependencyInfos = product2dependencyInfos.get(productId);

					if (dependencyInfos == null) {
						dependencyInfos = new ArrayList<>();
						product2dependencyInfos.put(productId, dependencyInfos);
					}

					Map<String, String> dependencyInfo = new HashMap<>();
					dependencyInfo.put("action", action);
					dependencyInfo.put("requiredProductId", requiredProductId);
					dependencyInfo.put("requiredAction", requiredAction);
					dependencyInfo.put("requiredInstallationStatus", requiredInstallationStatus);
					dependencyInfo.put("requirementType", requirementType);

					dependencyInfos.add(dependencyInfo);
				}
			}

			persist.notifyDataRefreshedObservers("productDependency");
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

	protected void productPropertyDepotStatesRequestRefresh() {
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

	protected void retrieveProductPropertyStates() {
		produceProductPropertyStates((Collection<String>) null, hostsWithProductProperties);
	}

	protected void retrieveProductPropertyDepotStates(Set<String> depots) {
		Logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots + " depotStates == null "
				+ (productPropertyDepotStates == null));
		if (productPropertyDepotStates == null) {
			productPropertyDepotStates = produceProductPropertyStates(depots, null);
		}

		Logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
	}

	// client is a set of added hosts, host represents the totality and will be
	// updated as a side effect
	protected List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients,
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

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " product property state");
			String[] callAttributes = new String[] {};
			Map<String, Object> callFilter = new HashMap<>();
			callFilter.put("objectId", AbstractExecutioner.jsonArray(newClients));

			result = persist.retrieveListOfMapsNOM(callAttributes, callFilter, "productPropertyState_getObjects");
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

	protected void retrieveInstalledSoftwareInformation() {
		if (installedSoftwareInformation == null || name2SWIdents == null) {
			persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.loadtable") + " software");

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

			List<Map<String, Object>> li = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"auditSoftware_getHashes");

			Iterator<Map<String, Object>> iter = li.iterator();

			installedSoftwareInformation = new TreeMap<>();
			installedSoftwareInformationForLicensing = new TreeMap<>();
			name2SWIdents = new TreeMap<>();
			installedSoftwareName2SWinfo = new TreeMap<>();
			name2ident2infoWithPool = new TreeMap<>();

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

					if (name2SWIdents.get(swName) == null) {
						name2SWIdents.put(swName, new TreeSet<>());
					}
					name2SWIdents.get(swName).add(entry.getIdent());

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

					Map<String, Map<String, String>> ident2infoWithPool = name2ident2infoWithPool.get(swName);

					if (ident2infoWithPool == null) {
						ident2infoWithPool = new TreeMap<>();
						name2ident2infoWithPool.put(swName, ident2infoWithPool);
					}

					Map<String, String> infoWithPool = ident2infoWithPool.get(entry.getIdent());

					if (infoWithPool == null) {
						infoWithPool = new LinkedHashMap<>();
						ident2infoWithPool.put(entry.getIdent(), infoWithPool);
					}
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

			persist.notifyDataRefreshedObservers("software");
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

	protected void retrieveSoftwareAuditOnClients() {
		retrieveSoftwareAuditOnClients(new ArrayList<>());
	}

	protected void retrieveSoftwareAuditOnClients(String client) {
		List<String> clients = new ArrayList<>();
		clients.add(client);
		retrieveSoftwareAuditOnClients(clients);
	}

	protected void retrieveSoftwareAuditOnClients(final List<String> clients) {
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
			int step = 1;
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

				persist.notifyDataLoadingObservers(
						Configed.getResourceValue("LoadingObserver.loadtable") + " software config, step " + step);

				Logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

				String[] callAttributes = new String[] {};
				Map<String, Object> callFilter = new HashMap<>();
				callFilter.put("state", 1);
				if (newClients != null) {
					callFilter.put("clientId", AbstractExecutioner.jsonArray(clientListForCall));
				}

				List<Map<String, Object>> softwareAuditOnClients = persist.retrieveListOfMapsNOM(callAttributes,
						callFilter, "auditSoftwareOnClient_getHashes");

				Logging.info(this, "retrieveSoftwareAuditOnClients, finished a request, map size "
						+ softwareAuditOnClients.size());

				for (String clientId : clientListForCall) {
					client2software.put(clientId, new LinkedList<>());
				}

				for (Map<String, Object> item : softwareAuditOnClients) {

					SWAuditClientEntry clientEntry = new SWAuditClientEntry(item, persist);

					String clientId = clientEntry.getClientId();
					String swIdent = clientEntry.getSWident();

					Set<String> clientsWithThisSW = softwareIdent2clients.get(swIdent);
					if (clientsWithThisSW == null) {
						clientsWithThisSW = new HashSet<>();
						softwareIdent2clients.put(swIdent, clientsWithThisSW);
					}

					clientsWithThisSW.add(clientId);

					// null not allowed in mysql
					if (clientId != null) {
						List<SWAuditClientEntry> entries = client2software.get(clientId);

						entries.add(clientEntry);
					}

				}

				Logging.info(this, "retrieveSoftwareAuditOnClients client2software ");

				step++;
			}

			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Globals.usedMemory());
			Logging.info(this, "retrieveSoftwareAuditOnClients used memory on end " + Globals.usedMemory());
			persist.notifyDataRefreshedObservers("softwareConfig");
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
	protected void retrieveAuditSoftwareXLicencePool() {
		if (auditSoftwareXLicencePool != null) {
			return;
		}

		Logging.info(this, "retrieveAuditSoftwareXLicencePool");

		persist.notifyDataLoadingObservers(
				Configed.getResourceValue("LoadingObserver.loadtable") + " AUDIT_SOFTWARE_TO_LICENSE_POOL");

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(
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

	protected void retrieveHostConfigs() {
		if (hostConfigs != null) {
			return;
		}

		Logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.loadtable") + " config state");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		Map<String, Object> callFilter = new HashMap<>();

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
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
					configs1Host.put(configId, ((org.json.JSONArray) (listElement.get("values"))).toList());
				}
			}
		}

		timeCheck.stop();
		Logging.info(this, "retrieveHostConfigs retrieved " + hostConfigs.keySet());

		persist.notifyDataRefreshedObservers("configState");
	}

	public void licencepoolsRequestRefresh() {
		Logging.info(this, "licencepoolsRequestRefresh");
		licencepools = null;
	}

	public Map<String, LicencepoolEntry> getLicencepools() {
		retrieveLicencepools();
		return licencepools;
	}

	protected void retrieveLicencepools() {
		if (licencepools != null) {
			return;
		}

		licencepools = new TreeMap<>();

		if (persist.isWithLicenceManagement()) {
			String[] attributes = new String[] { LicencepoolEntry.ID_KEY, LicencepoolEntry.DESCRIPTION_KEY };

			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " licence pool");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(attributes, new HashMap<>(),
					"licensePool_getObjects");

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
	protected void retrieveLicenceContracts() {
		if (licenceContracts != null) {
			return;
		}

		String today = new java.sql.Date(System.currentTimeMillis()).toString();
		licenceContracts = new HashMap<>();
		contractsToNotify = new TreeMap<>();
		contractsExpired = new TreeMap<>();

		if (persist.isWithLicenceManagement()) {
			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " software license");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("licenseContract_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);

				String notiDate = entry.get(TableLicenceContracts.NOTIFICATION_DATE_KEY);
				if (notiDate != null && (notiDate.trim().length() > 0) && notiDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsToNotify.get(notiDate);

					if (contractSet == null) {
						contractSet = new TreeSet<>();
						contractsToNotify.put(notiDate, contractSet);
					}

					contractSet.add(entry.getId());
				}

				String expireDate = entry.get(TableLicenceContracts.EXPIRATION_DATE_KEY);
				if (expireDate != null && (expireDate.trim().length() > 0) && expireDate.compareTo(today) <= 0) {
					NavigableSet<String> contractSet = contractsExpired.get(expireDate);

					if (contractSet == null) {
						contractSet = new TreeSet<>();
						contractsExpired.put(expireDate, contractSet);
					}

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
	protected void retrieveLicences() {
		if (licences != null) {
			return;
		}

		licences = new HashMap<>();

		if (persist.isWithLicenceManagement()) {
			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " software license");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("softwareLicense_getObjects");

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
	protected void retrieveLicenceUsabilities() {
		if (licenceUsabilities != null) {
			return;
		}

		licenceUsabilities = new ArrayList<>();

		if (persist.isWithLicenceManagement()) {
			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " software_license_TO_license_pool");

			List<Map<String, Object>> retrieved = persist
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
	protected void retrieveLicenceUsages() {
		Logging.info(this, "retrieveLicenceUsages");
		if (licenceUsages != null) {
			return;
		}

		licenceUsages = new ArrayList<>();

		if (persist.isWithLicenceManagement()) {
			persist.notifyDataLoadingObservers(
					Configed.getResourceValue("LoadingObserver.loadtable") + " license_on_client");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("licenseOnClient_getObjects");

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
	protected void retrieveLicencePoolXOpsiProduct() {
		if (licencePoolXOpsiProduct != null) {
			return;
		}

		Logging.info(this, "retrieveLicencePoolXOpsiProduct");

		persist.notifyDataLoadingObservers(
				Configed.getResourceValue("LoadingObserver.loadtable") + " PRODUCT_ID_TO_LICENSE_POOL");

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(
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

	protected void retrieveClient2HwRows(String[] hosts) {
		if (client2HwRows == null) {
			client2HwRows = new HashMap<>();
		}
	}

	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		retrieveClient2HwRows(hosts);
		return client2HwRows;
	}

	public List<Map<String, Object>> checkHealth() {
		retrieveHealthData();
		return healthData;
	}

	protected void retrieveHealthData() {
		persist.notifyDataLoadingObservers(Configed.getResourceValue("LoadingObserver.checkHealth"));
		healthData = persist.retrieveListOfMapsNOM("service_healthCheck", new Object[0]);
	}
}
