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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import de.uib.configed.configed;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.HWAuditClientEntry;
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
import de.uib.configed.type.licences.Table_LicenceContracts;
import de.uib.opsicommand.JSONReMapper;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.ListCellOptions;

public class DataStubNOM extends DataStub {

	OpsiserviceNOMPersistenceController persist;

	public static Integer classCounter = 0;

	public DataStubNOM(OpsiserviceNOMPersistenceController controller) {
		this.persist = controller;
		classCounter++;
	}

	@Override
	public void productDataRequestRefresh() {
		product2versionInfoRequestRefresh();
		productsAllDepotsRequestRefresh();
		productPropertyDefinitionsRequestRefresh();
		productPropertyStatesRequestRefresh();
		productPropertyDepotStatesRequestRefresh();
		productDependenciesRequestRefresh();
	}

	//===================================================

	//netbootStatesAndActions
	//localbootStatesAndActions

	//===================================================

	@Override
	public boolean canCallMySQL()
	//can only return true if overriden in a subclass
	{
		return false;
	}

	/*
	@Override
	protected boolean test()
	{
		return true;
	}
	*/

	//===================================================
	protected Map<String, Map<String, OpsiProductInfo>> product2versionInfo2infos;

	@Override
	public void product2versionInfoRequestRefresh() {
		product2versionInfo2infos = null;
	}

	@Override
	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos() {
		retrieveProductInfos();
		return product2versionInfo2infos;
	}

	protected void retrieveProductInfos() {
		logging.debug(this, "retrieveProductInfos data == null " + (product2versionInfo2infos == null));

		if (product2versionInfo2infos == null) {
			ArrayList<String> attribs = new ArrayList<String>();

			for (String key : OpsiPackage.SERVICE_KEYS) {
				attribs.add(key);
			}

			/*
			attribs.remove(OpsiPackage.SERVICEkeyPRODUCT_ID);
			attribs.add("id");
			*/

			for (String scriptKey : ActionRequest.getScriptKeys()) {
				attribs.add(scriptKey);
			}

			attribs.add(OpsiProductInfo.SERVICEkeyUSER_LOGIN_SCRIPT);
			attribs.add(OpsiProductInfo.SERVICEkeyPRIORITY);

			attribs.remove(OpsiPackage.SERVICEkeyPRODUCT_TYPE);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_ADVICE);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_NAME);
			attribs.add(OpsiProductInfo.SERVICEkeyPRODUCT_DESCRIPTION);

			String[] callAttributes = attribs.toArray(new String[] {});

			logging.debug(this, "retrieveProductInfos callAttributes " + Arrays.asList(callAttributes));

			HashMap callFilter = new HashMap();
			//callFilter.put("id", "acroread*");

			persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product");

			List<Map<String, Object>> retrievedList = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"product_getObjects");

			//logging.info(this,  "retrieveProductInfos retrievedList " + retrievedList);

			product2versionInfo2infos = new HashMap<String, Map<String, OpsiProductInfo>>();

			for (Map<String, Object> m : retrievedList) {
				//logging.info(this, "retrieveProductInfos " + m);
				String productId = "" + m.get(OpsiPackage.SERVICEkeyPRODUCT_ID0);
				String versionInfo = OpsiPackage.produceVersionInfo("" + m.get(OpsiPackage.SERVICEkeyPRODUCT_VERSION),
						"" + m.get(OpsiPackage.SERVICEkeyPACKAGE_VERSION));

				OpsiProductInfo productInfo = new OpsiProductInfo(m);
				Map<String, OpsiProductInfo> version2productInfos = product2versionInfo2infos.get(productId);

				if (version2productInfos == null) {
					version2productInfos = new HashMap<String, OpsiProductInfo>();
					product2versionInfo2infos.put(productId, version2productInfos);
				}
				version2productInfos.put(versionInfo, productInfo);

				/*
				logging.info(this,  "retrieveProductInfos product  -  version2productInfos " + 
					productId + "  -  " +
					version2productInfos);
				*/

				//System.exit(0);

			}

			logging.debug(this, "retrieveProductInfos " + product2versionInfo2infos);

			//lambda
			/*
			
			Map<String, List<Map<String, Object>>> pInfos = new HashMap<String, List<Map<String, Object>>>();
			String keyP = "id";
			
			retrievedList.forEach(
				m-> 
					{
						if ( pInfos.get( m.get(keyP ) ) == null )
							pInfos.put( (String) m.get(keyP), new ArrayList<Map<String, Object>>() );
					}
			);
			
			retrievedList.forEach(
				m-> pInfos.get( (String) m.get(keyP)).add( m )
			);
			*/

			/*
			retrievedList.forEach(
				m->
					{
						String p = (String) m.get(keyP);
						if  (pInfos.get( p ) == null)
							pInfos.put(p, new ArrayList<Map<String, Object>());
						
						List<Map<String, Object> list = pInfos.get( p );
						list.add((Map<String, Object) m);
					}
				);
				
			*/
			//logging.info(this, "lambda expression produced pInfos " + pInfos);
			//System.exit(0);

			persist.notifyDataRefreshedObservers("product");
		}

	}

	//===================================================

	protected Object2Product2VersionList depot2LocalbootProducts;
	protected Object2Product2VersionList depot2NetbootProducts;
	protected Vector<Vector<Object>> productRows;
	protected Map<String, TreeSet<OpsiPackage>> depot2Packages;
	protected Map<String, Map<String, java.util.List<String>>> product2VersionInfo2Depots;

	@Override
	public void productsAllDepotsRequestRefresh() {
		depot2LocalbootProducts = null;
	}

	@Override
	public Map<String, TreeSet<OpsiPackage>> getDepot2Packages() {
		retrieveProductsAllDepots();
		return depot2Packages;
	}

	@Override
	public Vector<Vector<Object>> getProductRows() {
		retrieveProductsAllDepots();
		return productRows;
	}

	@Override
	public Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots() {
		retrieveProductsAllDepots();
		return product2VersionInfo2Depots;
	}

	@Override
	public Object2Product2VersionList getDepot2LocalbootProducts() {
		retrieveProductsAllDepots();
		return depot2LocalbootProducts;
	}

	@Override
	public Object2Product2VersionList getDepot2NetbootProducts() {
		retrieveProductsAllDepots();
		return depot2NetbootProducts;
	}

	protected void retrieveProductsAllDepots() {

		logging.debug(this, "retrieveProductsAllDepots ? ");
		if (depot2LocalbootProducts != null)
			logging.debug(this, "depot2LocalbootProducts " + depot2LocalbootProducts.size());
		if (depot2NetbootProducts != null)
			logging.debug(this, "depot2NetbootProducts" + depot2NetbootProducts.size());
		retrieveProductInfos();

		if (depot2NetbootProducts == null || depot2LocalbootProducts == null || productRows == null
				|| depot2Packages == null) {

			logging.info(this, "retrieveProductsAllDepots, reload");
			logging.info(this, "retrieveProductsAllDepots, reload depot2NetbootProducts == null "
					+ (depot2NetbootProducts == null));
			logging.info(this, "retrieveProductsAllDepots, reload depot2LocalbootProducts == null "
					+ (depot2LocalbootProducts == null));
			logging.info(this, "retrieveProductsAllDepots, reload productRows == null " + (productRows == null));
			logging.info(this, "retrieveProductsAllDepots, reload depot2Packages == null " + (depot2Packages == null));
			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " productOnDepot");
			String[] callAttributes = new String[] {};
			HashMap callFilter = new HashMap();

			List<Map<String, Object>> packages = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productOnDepot_getObjects");

			depot2LocalbootProducts = new Object2Product2VersionList();
			depot2NetbootProducts = new Object2Product2VersionList();
			product2VersionInfo2Depots = new HashMap<String, Map<String, java.util.List<String>>>();

			productRows = new Vector<Vector<Object>>();

			depot2Packages = new HashMap<String, TreeSet<OpsiPackage>>();

			for (Map<String, Object> m : packages) {
				String depot = "" + m.get("depotId");

				if (!persist.getDepotPermission(depot))
					continue;

				OpsiPackage p = new OpsiPackage(m);

				logging.debug(this, "retrieveProductsAllDepots, opsi package " + p);

				if (p.isNetbootProduct()) {
					depot2NetbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				}

				else if (p.isLocalbootProduct()) {
					depot2LocalbootProducts.addPackage(depot, p.getProductId(), p.getVersionInfo());
				}

				Map<String, java.util.List<String>> versionInfo2Depots = product2VersionInfo2Depots
						.get(p.getProductId());
				if (versionInfo2Depots == null) {
					versionInfo2Depots = new HashMap<String, java.util.List<String>>();
					product2VersionInfo2Depots.put(p.getProductId(), versionInfo2Depots);
				}

				java.util.List depotsWithThisVersion = versionInfo2Depots.get(p.getVersionInfo());

				if (depotsWithThisVersion == null) {
					depotsWithThisVersion = new ArrayList<String>();
					versionInfo2Depots.put(p.getVersionInfo(), depotsWithThisVersion);
				}
				depotsWithThisVersion.add(depot);

				TreeSet<OpsiPackage> depotpackages = depot2Packages.get(depot);
				if (depotpackages == null) {
					depotpackages = new TreeSet<OpsiPackage>();
					depot2Packages.put(depot, depotpackages);
				}
				depotpackages.add(p);

				Vector<Object> productRow = new Vector<Object>();

				productRow.add(p.getProductId());

				String productName = null;
				String productLockedInfo = "";

				try {
					productName = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo())
							.getProductName();
					//productLockedInfo = product2versionInfo2infos.get(p.getProductId()).get(p.getVersionInfo()).getLockedInfo();

					productRow.add(productName);
					p.appendValues(productRow);

					//logging.info(this, "retrieveProductsAllDepots package " + p + " name  " + productName;
					//logging.info(this, "retrieveProductsAllDepots productRow " + productRow);

					if (depotsWithThisVersion.size() == 1)
						productRows.add(productRow);
				} catch (Exception ex) {
					logging.warning(this, "retrieveProductsAllDepots exception " + ex);
					logging.warning(this, "retrieveProductsAllDepots exception for package  " + p);
					logging.warning(this, "retrieveProductsAllDepots exception productId  " + p.getProductId());

					logging.warning(this, "retrieveProductsAllDepots exception for product2versionInfo2infos: of size "
							+ product2versionInfo2infos.size());
					logging.warning(this,
							"retrieveProductsAllDepots exception for product2versionInfo2infos.get(p.getProductId()) "
									+ product2versionInfo2infos.get(p.getProductId()));
					if (product2versionInfo2infos.get(p.getProductId()) == null) {
						logging.warning(this, "retrieveProductsAllDepots : product " + p.getProductId()
								+ " seems not to exist in product table");
					}

				}

				//System.exit(0);

			}

			/*
			logging.debug(this, "retrieveDepotProducts localBoot | netBoot " 
				+ "\n"+ depot2LocalbootProducts 
				+ "\n"+ depot2NetbootProducts
				);
			*/

			//logging.info(this, "retrieveProductsAllDepots  product2VersionInfo2Depots " + product2VersionInfo2Depots);
			//System.exit(0);

			persist.notifyDataRefreshedObservers("productOnDepot");
		}

		//logging.debug(this, "getRowsOfProducts " + productRows);
		//System.exit(0);

	}

	//===================================================

	protected Map<String, Map<String, Map<String, ListCellOptions>>> // depotId-->productId --> (propertyId --> value)
	depot2Product2PropertyDefinitions;

	@Override
	public void productPropertyDefinitionsRequestRefresh() {
		depot2Product2PropertyDefinitions = null;
	}

	@Override
	public Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitions() {
		retrieveAllProductPropertyDefinitions();
		return depot2Product2PropertyDefinitions;
	}

	protected void retrieveAllProductPropertyDefinitions() {
		retrieveProductsAllDepots();

		if (depot2Product2PropertyDefinitions == null) {
			depot2Product2PropertyDefinitions = new HashMap<String, Map<String, Map<String, ListCellOptions>>>();

			//HashMap<String, java.util.Set<String>> productListForProductID = new HashMap<String, java.util.Set<String>>();
			//HashMap<String, java.util.Set<String>> productListForProductID_notUnique = new HashMap<String, java.util.Set<String>>();

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " product property");

			String[] callAttributes = new String[] {};
			HashMap callFilter = new HashMap();

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"productProperty_getObjects");

			//logging.debug(this, "retrieved: " + retrieved);

			Iterator iter = retrieved.iterator();

			while (iter.hasNext()) {

				Map<String, Object> retrievedMap = (Map) iter.next();
				Map<String, Object> adaptedMap = new HashMap<String, Object>(retrievedMap);
				//rebuild JSON objects
				Iterator iterInner = retrievedMap.keySet().iterator();
				while (iterInner.hasNext()) {
					String key = (String) iterInner.next();
					adaptedMap.put(key, JSONReMapper.deriveStandard(retrievedMap.get(key)));
				}

				ConfigOption productPropertyMap = new ConfigOption(adaptedMap);

				String propertyId = (String) retrievedMap.get("propertyId");
				String productId = (String) retrievedMap.get("productId");

				//logging.debug(this, "############ product " + productId + "  property " + propertyId  + "  , retrieved map " + retrievedMap);
				//logging.debug(this, "############ product " + productId + "  property " + propertyId  + "  , property map " + productPropertyMap);

				String productVersion = (String) retrievedMap.get(OpsiPackage.SERVICEkeyPRODUCT_VERSION);
				String packageVersion = (String) retrievedMap.get(OpsiPackage.SERVICEkeyPACKAGE_VERSION);
				String versionInfo = productVersion + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey()
						+ packageVersion;

				if (product2VersionInfo2Depots.get(productId) == null
						|| product2VersionInfo2Depots.get(productId).get(versionInfo) == null) {
					logging.debug(this,
							"retrieveAllProductPropertyDefinitions: no depot for " + productId + " version "
									+ versionInfo + "  product2VersionInfo2Depots.get(productId) "
									+ product2VersionInfo2Depots.get(productId));

				} else {
					for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {

						Map<String, Map<String, ListCellOptions>> product2PropertyDefinitions = depot2Product2PropertyDefinitions
								.get(depot);
						if (product2PropertyDefinitions == null) {
							product2PropertyDefinitions = new HashMap<String, Map<String, ListCellOptions>>();
							depot2Product2PropertyDefinitions.put(depot, product2PropertyDefinitions);
						}

						Map<String, ListCellOptions> propertyDefinitions = product2PropertyDefinitions.get(productId);

						if (propertyDefinitions == null) {
							propertyDefinitions = new HashMap<String, ListCellOptions>();
							product2PropertyDefinitions.put(productId, propertyDefinitions);
						}

						propertyDefinitions.put(propertyId, (ListCellOptions) productPropertyMap);

					}
				}

			}

			logging.debug(this, "retrieveAllProductPropertyDefinitions ");
			//+ depot2Product2PropertyDefinitions);
			persist.notifyDataRefreshedObservers("productProperty");

		}

	}

	//===================================================

	protected Map<String, Map<String, java.util.List<Map<String, String>>>> // depotId-->productId --> (dependencyKey--> value)
	depot2product2dependencyInfos;

	@Override
	public void productDependenciesRequestRefresh() {
		depot2product2dependencyInfos = null;
	}

	@Override
	public Map<String, Map<String, java.util.List<Map<String, String>>>> getDepot2product2dependencyInfos() {
		retrieveAllProductDependencies();
		return depot2product2dependencyInfos;
	}

	protected void retrieveAllProductDependencies() {
		retrieveProductsAllDepots();

		if (depot2product2dependencyInfos == null) {
			depot2product2dependencyInfos = new HashMap<String, Map<String, java.util.List<Map<String, String>>>>();

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " product dependency");

			String[] callAttributes = new String[] {};
			HashMap callFilter = new HashMap();

			java.util.List<Map<String, Object>> retrievedList = persist.retrieveListOfMapsNOM(callAttributes,
					callFilter, "productDependency_getObjects");

			for (Map<String, Object> dependencyItem : retrievedList) {
				String productId = "" + dependencyItem.get(OpsiPackage.DBkeyPRODUCT_ID);

				String productVersion = "" + dependencyItem.get(OpsiPackage.SERVICEkeyPRODUCT_VERSION);
				String packageVersion = "" + dependencyItem.get(OpsiPackage.SERVICEkeyPACKAGE_VERSION);
				String versionInfo = productVersion + de.uib.configed.Globals.ProductPackageVersionSeparator.forKey()
						+ packageVersion;

				String action = "" + dependencyItem.get("productAction");
				String requirementType = "";
				if (dependencyItem.get("requirementType") != null)
					requirementType = "" + dependencyItem.get("requirementType");

				String requiredProductId = "" + dependencyItem.get("requiredProductId");
				String requiredAction = "";
				if (dependencyItem.get("requiredAction") != null)
					requiredAction = "" + dependencyItem.get("requiredAction");
				String requiredInstallationStatus = "";
				if (dependencyItem.get("requiredInstallationStatus") != null)
					requiredInstallationStatus = "" + dependencyItem.get("requiredInstallationStatus");

				if ((product2VersionInfo2Depots == null) || (product2VersionInfo2Depots.get(productId) == null)
						|| (product2VersionInfo2Depots.get(productId).get(versionInfo) == null)) {
					logging.warning(this, "unexpected null for product2VersionInfo2Depots productId, versionInfo   "
							+ productId + ", " + versionInfo);
					continue;
				}
				for (String depot : product2VersionInfo2Depots.get(productId).get(versionInfo)) {
					Map<String, java.util.List<Map<String, String>>> product2dependencyInfos = depot2product2dependencyInfos
							.get(depot);
					if (product2dependencyInfos == null) {
						product2dependencyInfos = new HashMap<String, java.util.List<Map<String, String>>>();
						depot2product2dependencyInfos.put(depot, product2dependencyInfos);
					}

					java.util.List<Map<String, String>> dependencyInfos = product2dependencyInfos.get(productId);

					if (dependencyInfos == null) {
						dependencyInfos = new ArrayList<Map<String, String>>();
						product2dependencyInfos.put(productId, dependencyInfos);
					}

					Map<String, String> dependencyInfo = new HashMap<String, String>();
					dependencyInfo.put("action", action);
					dependencyInfo.put("requiredProductId", requiredProductId);
					dependencyInfo.put("requiredAction", requiredAction);
					dependencyInfo.put("requiredInstallationStatus", requiredInstallationStatus);
					dependencyInfo.put("requirementType", requirementType);

					//logging.info(this, "add dependencyInfo depot " + depot + "  product " + productId + ", "  + dependencyInfo);
					dependencyInfos.add(dependencyInfo);
					//logging.info(this, "dependencyInfos "+ dependencyInfos);
				}
			}

			//logging.info(this, "retrieveAllProductDependencies  " + depot2product2dependencyInfos );
			persist.notifyDataRefreshedObservers("productDependency");

		}

	}

	//===================================================

	protected java.util.List<Map<String, Object>> productPropertyStates;
	protected java.util.List<Map<String, Object>> productPropertyDepotStates; //will only be refreshed when all product data are refreshed

	//protected Map<String, Map<String, Map<String, Object>>> host2product2properties_retrieved = new HashMap<String, Map<String, Map <String, Object>>>();
	protected java.util.Set<String> hostsWithProductProperties;
	//protected java.util.Set<String> depotsWithProductProperties;

	public void productPropertyStatesRequestRefresh() {
		logging.info(this, "productPropertyStatesRequestRefresh");
		productPropertyStates = null;
		hostsWithProductProperties = null;
	}

	public java.util.List<Map<String, Object>> getProductPropertyStates() {
		retrieveProductPropertyStates();
		return productPropertyStates;
	}

	protected void productPropertyDepotStatesRequestRefresh() {
		logging.info(this, "productPropertyDepotStatesRequestRefresh");
		productPropertyDepotStates = null;
	}

	public java.util.List<Map<String, Object>> getProductPropertyDepotStates(java.util.Set<String> depots) {
		retrieveProductPropertyDepotStates(depots);
		return productPropertyDepotStates;
	}

	//public Map<String, Map<String, Map<String, Object>>> getHost2product2properties_retrieved  = new HashMap<String, Map<String, Map <String, Object>>>();

	public void fillProductPropertyStates(Collection<String> clients) {
		logging.info(this, "fillProductPropertyStates for " + clients);
		if (productPropertyStates == null) {
			productPropertyStates = produceProductPropertyStates(clients, hostsWithProductProperties);
		} else {
			productPropertyStates.addAll(produceProductPropertyStates(clients, hostsWithProductProperties));
		}
	}

	protected void retrieveProductPropertyStates() {
		produceProductPropertyStates((Collection<String>) null, hostsWithProductProperties);
	}

	protected void retrieveProductPropertyDepotStates(java.util.Set<String> depots) {
		logging.info(this, "retrieveProductPropertyDepotStates for depots " + depots + " depotStates == null "
				+ (productPropertyDepotStates == null));
		if (productPropertyDepotStates == null) {
			productPropertyDepotStates = produceProductPropertyStates(depots, null);
		}

		/*
		if (productPropertyDepotStates == null)
		{
			productPropertyDepotStates = produceProductPropertyStates(depots , null);
			depotsWithProductProperties = depots;
		}
		else
		{
			productPropertyDepotStates = produceProductPropertyStates(depots , depotsWithProductProperties);
		}
		*/

		logging.info(this, "retrieveProductPropertyDepotStates ready  size " + productPropertyDepotStates.size());
	}

	// client is a set of added hosts, host represents the totality and will be updated as a side effect
	protected java.util.List<Map<String, Object>> produceProductPropertyStates(final Collection<String> clients,
			java.util.Set<String> hosts) {
		logging.info(this, "produceProductPropertyStates new hosts " + clients + " old hosts " + hosts);
		java.util.List<String> newClients = null;
		if (clients == null)
			newClients = new ArrayList<String>();
		else
			newClients = new ArrayList<String>(clients);

		if (hosts == null) {
			hosts = new HashSet<String>();
		} else {
			newClients.removeAll(hosts);
		}

		//logging.info(this, "produceProductPropertyStates, new hosts " + clients);

		java.util.List<Map<String, Object>> result = null;

		if (newClients.size() == 0) {
			//look if propstates is initialized
			result = new ArrayList<Map<String, Object>>();
		} else {
			hosts.addAll(newClients);

			//logging.info(this, "produceProductPropertyStates, all hosts " + hosts);

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " product property state");
			String[] callAttributes = new String[] {};//"objectId","productId","propertyId", "values"};
			HashMap callFilter = new HashMap();
			callFilter.put("objectId", persist.exec.jsonArray(newClients));

			result = persist.retrieveListOfMapsNOM(callAttributes, callFilter, "productPropertyState_getObjects");
			//logging.info(this, "propstates: " + propstates);
		}

		logging.info(this, "produceProductPropertyStates for hosts " + hosts);
		/*
		for (Map<String, Object> m : result)
		{
			logging.info(this, "produceProductPropertyStates record " + m);
		}
		*/

		return result;
	}

	//===================================================
	protected TreeMap<String, java.util.List<HWAuditClientEntry>> client2hwAuditHostEntries;
	protected TreeMap<String, Map<String, java.util.List<HWAuditClientEntry>>> client2hwType2hwAuditHostEntries;

	//===================================================
	protected TreeMap<String, SWAuditEntry> installedSoftwareInformation;
	protected TreeMap<String, SWAuditEntry> installedSoftwareInformationForLicensing;
	protected TreeMap<String, Set<String>> name2SWIdents; //giving the idents which have the name in their ident
	protected TreeMap<String, Map<String, String>> installedSoftwareName2SWinfo;
	protected TreeMap<String, Map<String, Map<String, String>>> name2ident2infoWithPool;
	protected ArrayList<String> softwareList; //List of idents of software
	protected TreeMap<String, Integer> software2Number; //the same with a numbering index

	@Override
	public void installedSoftwareInformationRequestRefresh() {
		installedSoftwareInformation = null;
		installedSoftwareInformationForLicensing = null;
		name2SWIdents = null;
	}

	@Override
	public ArrayList<String> getSoftwareList() {
		retrieveInstalledSoftwareInformation();
		return softwareList;
	}

	@Override
	public TreeMap<String, Integer> getSoftware2Number() {
		retrieveInstalledSoftwareInformation();
		return software2Number;
	}

	@Override
	public String getSWident(Integer i) {
		logging.debug(this, "getSWident for " + i);
		retrieveInstalledSoftwareInformation();
		if (softwareList == null || softwareList.size() < i + 1 || i == -1) {
			if (softwareList != null)
				logging.info(this, "getSWident " + " until now softwareList.size() " + softwareList.size());

			boolean infoFound = false;

			//try reloading?
			int returnedOption = javax.swing.JOptionPane.NO_OPTION;
			returnedOption = javax.swing.JOptionPane.showOptionDialog(de.uib.configed.Globals.mainFrame,
					configed.getResourceValue("DataStub.reloadSoftwareInformation.text"),
					configed.getResourceValue("DataStub.reloadSoftwareInformation.title"),
					javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == javax.swing.JOptionPane.YES_OPTION) {
				installedSoftwareInformationRequestRefresh();
				retrieveInstalledSoftwareInformation();
				if (i > -1 && softwareList.size() >= i + 1)
					infoFound = true;

			}

			if (!infoFound) {
				logging.warning(this, "missing softwareList entry " + i + " " + softwareList);
				return null;
			}
		}
		return softwareList.get(i);
	}

	@Override
	public TreeMap<String, SWAuditEntry> getInstalledSoftwareInformation() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareInformation;
	}

	@Override
	public TreeMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareInformationForLicensing;
	}

	@Override
	public TreeMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo() {
		retrieveInstalledSoftwareInformation();
		return installedSoftwareName2SWinfo;
	}

	public TreeMap<String, Set<String>> getName2SWIdents() {
		retrieveInstalledSoftwareInformation();
		return name2SWIdents;
	}

	protected void retrieveInstalledSoftwareInformation() {
		if (installedSoftwareInformation == null || name2SWIdents == null) {

			persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " software");

			String[] callAttributes = new String[] { SWAuditEntry.key2serverKey.get(SWAuditEntry.NAME), // "name", //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.VERSION), // "version",//key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.SUBVERSION), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.LANGUAGE), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.ARCHITECTURE), //key element
					SWAuditEntry.key2serverKey.get(SWAuditEntry.WINDOWSsOFTWAREid) };
			HashMap callFilter = new HashMap();

			List<Map<String, Object>> li = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
					"auditSoftware_getHashes");;

			Iterator iter = li.iterator();

			installedSoftwareInformation = new TreeMap<String, SWAuditEntry>();
			installedSoftwareInformationForLicensing = new TreeMap<String, SWAuditEntry>();
			name2SWIdents = new TreeMap<String, Set<String>>();
			installedSoftwareName2SWinfo = new TreeMap<String, Map<String, String>>();
			name2ident2infoWithPool = new TreeMap<String, Map<String, Map<String, String>>>();

			int i = 0;
			String testKey = "zypper";
			logging.info(this, "getInstalledSoftwareInformation build map");
			//ArrayList<String> foundEntries = new ArrayList<String>();

			while (iter.hasNext()) {
				i++;
				Map retrievedEntry = (Map) iter.next();
				//logging.info(this, "retrievedEntry " + retrievedEntry);
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

				if (showForLicensing && linuxSubversionMarkers.size() > 0) {

					String subversion = entry.get(SWAuditEntry.SUBVERSION);

					for (String marker : linuxSubversionMarkers) {
						//if (subversion.indexOf ( marker ) > -1 )
						if (subversion.startsWith(marker)) {
							showForLicensing = false;
							break;
						}
					}
				}

				if (showForLicensing) {

					installedSoftwareInformationForLicensing.put(entry.getIdent(), entry);

					/*
					Set<String> containingThisName = name2SWIdents.get( swName );
					if ( containingThisName == null )
					{
						containingThisName = new TreeSet<String>();
						name2SWIdents.put( swName, containingThisName );
					}
					containingThisName.add( entry.getIdent() );
					*/

					if (name2SWIdents.get(swName) == null)
						name2SWIdents.put(swName, new TreeSet<String>());
					name2SWIdents.get(swName).add(entry.getIdent());

					Map<String, String> identInfoRow = installedSoftwareName2SWinfo.get(swName);

					String infoString = "";

					if (identInfoRow == null) {
						identInfoRow = new LinkedHashMap<String, String>();
						identInfoRow.put(SWAuditEntry.NAME, swName);

					} else {
						infoString = identInfoRow.get(SWAuditEntry.EXISTING_IDS);
						infoString = infoString + " - ";
					}

					infoString = infoString + entry.getIdentReduced();

					identInfoRow.put(SWAuditEntry.EXISTING_IDS, infoString);

					/*
					if (entry.getIdent().indexOf( "zypper" ) > -1)
					{
						i++;
						logging.info(this, " check zypper i " + i + " info : "  + infoString);
						
					
						logging.info(this, " check zypper i " + i + " infoRow : "  + identInfoRow );
					}
					*/

					installedSoftwareName2SWinfo.put(swName, identInfoRow);

					// --

					Map<String, Map<String, String>> ident2infoWithPool = name2ident2infoWithPool.get(swName);

					if (ident2infoWithPool == null) {
						ident2infoWithPool = new TreeMap<String, Map<String, String>>();
						name2ident2infoWithPool.put(swName, ident2infoWithPool);
					}

					Map<String, String> infoWithPool = ident2infoWithPool.get(entry.getIdent());

					if (infoWithPool == null) {
						infoWithPool = new LinkedHashMap<String, String>();
						ident2infoWithPool.put(entry.getIdent(), infoWithPool);
					}
					String licencePoolAssigned = "x " + i;

					infoWithPool.put(SWAuditEntry.id, entry.getIdent());
					infoWithPool.put(LicencepoolEntry.idSERVICEKEY, licencePoolAssigned);

				}

				//if (i == 4) break;
			}

			/*
			logging.info(this, "getInstalledSoftwareInformation, found for testKey " 
				+ foundEntries.size());
			logging.info(this, "getInstalledSoftwareInformation, found for testKey " + testKey + ":: " +
				foundEntries);
			*/

			softwareList = new ArrayList<String>(installedSoftwareInformation.keySet());

			logging.info(this,
					"retrieveInstalledSoftwareInformation produced softwarelist with entries " + softwareList.size());

			software2Number = new TreeMap<String, Integer>();
			for (String sw : softwareList) {
				software2Number.put(sw, 0);
			}
			int n = 0;
			for (String sw : software2Number.keySet()) {
				if (sw.startsWith("NULL"))
					logging.info(this, "retrieveInstalledSoftwareInformation, we get index " + n + " for " + sw);
				software2Number.put(sw, n);
				n++;
			}

			persist.notifyDataRefreshedObservers("software");

		}
	}

	//===================================================

	protected java.util.List<Map<String, Object>> softwareAuditOnClients;
	protected Map<String, java.util.List<SWAuditClientEntry>> client2software;
	protected Map<String, java.util.Set<String>> softwareIdent2clients;
	//protected Map<Integer, java.util.List<String>> softwareId2clients; 

	protected java.sql.Time SOFTWARE_CONFIG_last_entry = null;

	@Override
	public void softwareAuditOnClientsRequestRefresh() {
		logging.info(this, "softwareAuditOnClientsRequestRefresh");
		softwareAuditOnClients = null;
		client2software = null;
		softwareIdent2clients = null;
		//softwareId2clients = null;
	}

	/*
	public  java.util.List <Map<String, Object>> getSoftwareAuditOnClients()
	{
		logging.debug(this, "getSoftwareAuditOnClients");
		retrieveSoftwareAuditOnClients0();
		return softwareAuditOnClients;
	}
	*/

	@Override
	public void fillClient2Software(String client) {
		logging.info(this, "fillClient2Software " + client);
		if (client2software == null) {
			retrieveSoftwareAuditOnClients(client);
			//logging.info(this, "fillClient2Software " + client2software);
			return;
		}

		if (client2software.get(client) == null)
			retrieveSoftwareAuditOnClients(client);

		//logging.info(this, "fillClient2Software " + client2software);
	}

	@Override
	public void fillClient2Software(java.util.List<String> clients) {
		if (clients == null)
			logging.info(this, "fillClient2Software for clients null");
		else
			logging.info(this, "fillClient2Software for clients " + clients.size());
		retrieveSoftwareAuditOnClients(clients);
	}

	@Override
	public Map<String, java.util.List<SWAuditClientEntry>> getClient2Software()
	//fill the clientlist by fill ...
	{
		logging.info(this, "getClient2Software  ============= ");
		retrieveInstalledSoftwareInformation();
		return client2software;
	}

	/*
	@Override
	public  Map<Integer, java.util.List<String>> getSoftwareId2clients()
	{
		
		//logging.info(this, "getSoftwareId2clients ============= ");
	
		if (softwareId2clients == null)
			logging.info(this, "getSoftwareId2clients ============= null");
		else
		{
			for (Integer key :   softwareId2clients.keySet())
			{
				logging.info(this, "getSoftwareId2clients ===== key " + key + " " 
					+ softwareId2clients.get(key));
			}
		}
		
		return softwareId2clients;
	}
	*/

	@Override
	public Map<String, java.util.Set<String>> getSoftwareIdent2clients()
	//fill the clientlist by fill ...
	{
		for (String ident : softwareIdent2clients.keySet()) {

			//logging.info(this, "getSoftwareIdent2clients = ident == size ===== " + ident + " ===== "
			//+  softwareIdent2clients.get(ident).size() );
		}

		return softwareIdent2clients;
	}

	protected void retrieveSoftwareAuditOnClients() {
		retrieveSoftwareAuditOnClients(new ArrayList<String>());
	}

	protected void retrieveSoftwareAuditOnClients(String client) {
		java.util.List<String> clients = new ArrayList<String>();
		clients.add(client);
		retrieveSoftwareAuditOnClients(clients);
	}

	protected void retrieveSoftwareAuditOnClients(final java.util.List<String> clients) {
		logging.info(this,
				"retrieveSoftwareAuditOnClients used memory on start " + de.uib.utilities.Globals.usedMemory());

		retrieveInstalledSoftwareInformation();
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  clients count ======  " + clients.size());

		java.util.List<String> newClients = new ArrayList<String>(clients);

		if (client2software != null) {
			logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " + "   +++  "
					+ client2software.keySet().size());

			newClients.removeAll(client2software.keySet());
		}

		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " + (client2software == null)
				+ "  new clients count  ====== " + newClients.size());

		int stepSize = 100;

		//if (client2software == null || softwareId2clients == null || newClients.size() > 0)
		if (client2software == null || softwareIdent2clients == null || newClients.size() > 0) {
			int step = 1;
			while (newClients.size() > 0) {
				java.util.List<String> clientListForCall = new ArrayList<String>();

				for (int i = 0; i < stepSize && i < newClients.size(); i++)
					clientListForCall.add(newClients.get(i));

				newClients.removeAll(clientListForCall);

				//logging.info(this, "retrieveSoftwareAuditOnClients for " + clientListForCall.size()  + " clients " + clientListForCall);

				//client2software = new HashMap<String, java.util.List<String>>();
				if (client2software == null)
					client2software = new HashMap<String, java.util.List<SWAuditClientEntry>>();

				if (softwareIdent2clients == null)
					softwareIdent2clients = new HashMap<String, java.util.Set<String>>();
				//if (softwareId2clients == null) softwareId2clients = new HashMap<Integer, java.util.List<String>>();

				persist.notifyDataLoadingObservers(
						configed.getResourceValue("LoadingObserver.loadtable") + " software config, step " + step);

				logging.info(this, "retrieveSoftwareAuditOnClients, start a request");

				String[] callAttributes = new String[] {};
				HashMap callFilter = new HashMap();
				callFilter.put("state", 1);
				if (newClients != null)
					callFilter.put("clientId", persist.exec.jsonArray(clientListForCall));

				java.util.List<Map<String, Object>> softwareAuditOnClients = persist
						.retrieveListOfMapsNOM(callAttributes, callFilter, "auditSoftwareOnClient_getHashes");

				logging.info(this, "retrieveSoftwareAuditOnClients, finished a request, map size "
						+ softwareAuditOnClients.size());

				if (softwareAuditOnClients == null) {
					logging.warning(this, "no auditSoftwareOnClient");
				} else {

					for (String clientId : clientListForCall) {
						client2software.put(clientId, new LinkedList<SWAuditClientEntry>());
					}

					for (Map<String, Object> item : softwareAuditOnClients) {

						SWAuditClientEntry clientEntry = new SWAuditClientEntry(item, persist);

						String clientId = clientEntry.getClientId();
						String swIdent = clientEntry.getSWident();

						/*
						if (swIdent.startsWith("firefox"))
						{
							logging.info(this, " retrieveSoftwareAuditOnClient clientId : swIdent " + clientId + " : "  + swIdent);
						}
						*/

						Set<String> clientsWithThisSW = softwareIdent2clients.get(swIdent);
						if (clientsWithThisSW == null) {
							clientsWithThisSW = new HashSet<String>();
							softwareIdent2clients.put(swIdent, clientsWithThisSW);
						}

						clientsWithThisSW.add(clientId);

						/*
						if (clientEntry.getSWid() == -1)
						{
							logging.info("Missing auditSoftware entry for swIdent " + 
								SWAuditClientEntry.produceSWident(item));
							//item.put(SWAuditEntry.WINDOWSsOFTWAREid, "MISSING");
						}
						else
						*/
						{
							if (clientId != null) //null not allowed in mysql
							{
								java.util.List<SWAuditClientEntry> entries = client2software.get(clientId);

								//variant1
								/*
								if (entries == null)
								{retrieveSoftwareAuditOnClients, start a request");
								
									entries = new LinkedList<SWAuditClientEntry>();
									client2software.put(clientId, entries);
								}
								*/
								entries.add(clientEntry);
							}

						}
					}

				}

				logging.info(this, "retrieveSoftwareAuditOnClients client2software "); // + client2software);

				softwareAuditOnClients = null;

				step++;

			}

			logging.info(this,
					"retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory());
			System.gc();
			logging.info(this,
					"retrieveSoftwareAuditOnClients used memory on end " + de.uib.utilities.Globals.usedMemory());

			persist.notifyDataRefreshedObservers("softwareConfig");
		}
	}

	//===================================================

	protected AuditSoftwareXLicencePool auditSoftwareXLicencePool;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void auditSoftwareXLicencePoolRequestRefresh() {
		logging.info(this, "auditSoftwareXLicencePoolRequestRefresh");
		auditSoftwareXLicencePool = null;
	}

	@Override
	public AuditSoftwareXLicencePool getAuditSoftwareXLicencePool() {
		retrieveAuditSoftwareXLicencePool();
		return auditSoftwareXLicencePool;
	}

	protected void retrieveAuditSoftwareXLicencePool()
	//AUDIT_SOFTWARE_TO_LICENSE_POOL
	{
		if (auditSoftwareXLicencePool != null)
			return;

		logging.info(this, "retrieveAuditSoftwareXLicencePool");

		persist.notifyDataLoadingObservers(
				configed.getResourceValue("LoadingObserver.loadtable") + " AUDIT_SOFTWARE_TO_LICENSE_POOL");

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(
				AuditSoftwareXLicencePool.SERVICE_ATTRIBUTES, new HashMap(), //callFilter
				"auditSoftwareToLicensePool_getObjects");

		auditSoftwareXLicencePool = new AuditSoftwareXLicencePool(getSoftwareList());

		for (Map<String, Object> map : retrieved) {
			//logging.info(this, "retrieved map " + map);

			auditSoftwareXLicencePool.integrateRaw(map);
		}

		logging.info(this, "retrieveAuditSoftwareXLicencePool retrieved ");
		//+ auditSoftwareXLicencePool);

		//logging.info(this, "retrieveAuditSoftwareXLicencePool by licencepool " + auditSoftwareXLicencePool.getFunctionBy(LicencepoolEntry.idKEY));
	}

	//===================================================

	protected Map<String, Map<String, Object>> hostConfigs;
	protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void hostConfigsRequestRefresh() {
		logging.info(this, "hostConfigsRequestRefresh");
		hostConfigs = null;
	}

	@Override
	public Map<String, Map<String, Object>> getConfigs() {
		retrieveHostConfigs();
		return hostConfigs;
	}

	protected void retrieveHostConfigs() {
		if (hostConfigs != null)
			return;

		logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);

		persist.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " config state");

		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();

		String[] callAttributes = new String[] {};
		HashMap callFilter = new HashMap();

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(callAttributes, callFilter,
				"configState_getObjects");
		hostConfigs = new HashMap<String, Map<String, Object>>();

		for (Map<String, Object> listElement : retrieved) {
			Object id = listElement.get("objectId");

			//logging.info(this, "retrieveHostConfigs " + id);

			if (id != null && id instanceof String && !id.equals("")) {
				String hostId = (String) id;
				Map<String, Object> configs1Host = hostConfigs.get(id);
				if (configs1Host == null) {
					configs1Host = new HashMap<String, Object>();
					hostConfigs.put(hostId, configs1Host);
				}

				logging.debug(this, "retrieveHostConfigs objectId,  element " + id + ": " + listElement);

				String configId = (String) listElement.get("configId");

				if (listElement.get("values") == null) {
					configs1Host.put(configId, new ArrayList<Object>());
					//is a data error but can occur
				} else {

					configs1Host.put(configId, ((org.json.JSONArray) (listElement.get("values"))).toList());

				}

			}
		}

		timeCheck.stop();
		logging.info(this, "retrieveHostConfigs retrieved " + hostConfigs.keySet());

		persist.notifyDataRefreshedObservers("configState");

	}

	//===================================================
	protected TreeMap<String, LicencepoolEntry> licencepools;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void licencepoolsRequestRefresh() {
		logging.info(this, "licencepoolsRequestRefresh");
		licencepools = null;
	}

	@Override
	public Map<String, LicencepoolEntry> getLicencepools() {
		retrieveLicencepools();
		return licencepools;
	}

	protected void retrieveLicencepools() {
		if (licencepools != null)
			return;

		licencepools = new TreeMap<String, LicencepoolEntry>();

		if (persist.withLicenceManagement) {
			String[] attributes = new String[] { LicencepoolEntry.idKEY, LicencepoolEntry.descriptionKEY };

			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " licence pool");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(attributes, new HashMap(),
					"licensePool_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicencepoolEntry entry = new LicencepoolEntry(importedEntry);
				licencepools.put(entry.getLicencepoolId(), entry);
			}

		}
	}

	//===================================================
	protected java.util.Map<String, LicenceContractEntry> licenceContracts;
	//protected Table_LicenceContracts tableLicenceContracts; 

	protected TreeMap<String, TreeSet<String>> contractsExpired;
	// date in sql time format, contrad  ID
	protected TreeMap<String, TreeSet<String>> contractsToNotify;
	// date in sql time format, contrad  ID

	@Override
	public void licenceContractsRequestRefresh() {
		logging.info(this, "licenceContractsRequestRefresh");
		//tableLicenceContracts = null;
		licenceContracts = null;
		contractsExpired = null;
		contractsToNotify = null;
	}

	@Override
	public java.util.Map<String, LicenceContractEntry> getLicenceContracts() {
		retrieveLicenceContracts();
		return licenceContracts;
	}

	@Override
	public TreeMap<String, TreeSet<String>> getLicenceContractsExpired()
	// date in sql time format, contrad  ID
	{
		retrieveLicenceContracts();
		return contractsExpired;
	}

	@Override
	public TreeMap<String, TreeSet<String>> getLicenceContractsToNotify()
	// date in sql time format, contrad  ID
	{
		retrieveLicenceContracts();
		return contractsToNotify;
	}

	protected void retrieveLicenceContracts()
	//LICENSE_CONTRACT 
	{
		if (licenceContracts != null)
			return;

		String today = new java.sql.Date(new java.util.Date().getTime()).toString();
		licenceContracts = new HashMap<String, LicenceContractEntry>();
		contractsToNotify = new TreeMap<String, TreeSet<String>>();
		contractsExpired = new TreeMap<String, TreeSet<String>>();

		//tableLicenceContracts = new Table_LicenceContracts();

		if (persist.withLicenceManagement) {
			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " software license");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("licenseContract_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceContractEntry entry = new LicenceContractEntry(importedEntry);
				licenceContracts.put(entry.getId(), entry);

				String notiDate = entry.get(Table_LicenceContracts.notificationDateKEY);
				if (notiDate != null && (notiDate.trim().length() > 0) && notiDate.compareTo(today) <= 0) {
					TreeSet<String> contractSet = contractsToNotify.get(notiDate);

					if (contractSet == null) {
						contractSet = new TreeSet<String>();
						contractsToNotify.put(notiDate, contractSet);
					}

					contractSet.add(entry.getId());
				}

				String expireDate = entry.get(Table_LicenceContracts.expirationDateKEY);
				if (expireDate != null && (expireDate.trim().length() > 0) && expireDate.compareTo(today) <= 0) {
					TreeSet<String> contractSet = contractsExpired.get(expireDate);

					if (contractSet == null) {
						contractSet = new TreeSet<String>();
						contractsExpired.put(expireDate, contractSet);
					}

					contractSet.add(entry.getId());
				}

			}

			logging.info(this, "contractsToNotify " + contractsToNotify);

			logging.info(this, "contractsExpired " + contractsExpired);
		}
	}

	//===================================================
	protected java.util.Map<String, LicenceEntry> licences;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void licencesRequestRefresh() {
		logging.info(this, "licencesRequestRefresh");
		licences = null;
	}

	@Override
	public java.util.Map<String, LicenceEntry> getLicences() {
		retrieveLicences();
		return licences;
	}

	protected void retrieveLicences()
	//SOFTWARE_LICENSE
	{
		if (licences != null)
			return;

		licences = new HashMap<String, LicenceEntry>();

		if (persist.withLicenceManagement) {
			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " software license");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("softwareLicense_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceEntry entry = new LicenceEntry(importedEntry);
				licences.put(entry.getId(), entry);
			}
		}
	}

	//===================================================
	protected java.util.List<LicenceUsableForEntry> licenceUsabilities;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void licenceUsabilitiesRequestRefresh() {
		logging.info(this, "licenceUsabilitiesRequestRefresh");
		licenceUsabilities = null;
	}

	@Override
	public java.util.List<LicenceUsableForEntry> getLicenceUsabilities() {
		retrieveLicenceUsabilities();
		return licenceUsabilities;
	}

	protected void retrieveLicenceUsabilities()
	//SOFTWARE_LICENSE_TO_LICENSE_POOL
	{
		if (licenceUsabilities != null)
			return;

		licenceUsabilities = new ArrayList<LicenceUsableForEntry>();

		if (persist.withLicenceManagement) {
			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " software_license_TO_license_pool");

			List<Map<String, Object>> retrieved = persist
					.retrieveListOfMapsNOM("softwareLicenseToLicensePool_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsableForEntry entry = LicenceUsableForEntry.produceFrom(importedEntry);
				licenceUsabilities.add(entry);
			}
		}

	}

	//===================================================
	protected java.util.List<LicenceUsageEntry> licenceUsages;
	//protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void licenceUsagesRequestRefresh() {
		logging.info(this, "licenceUsagesRequestRefresh");
		licenceUsages = null;
	}

	@Override
	public java.util.List<LicenceUsageEntry> getLicenceUsages() {
		retrieveLicenceUsages();
		return licenceUsages;
	}

	protected void retrieveLicenceUsages()
	//LICENSE_ON_CLIENT
	{
		logging.info(this, "retrieveLicenceUsages");
		if (licenceUsages != null)
			return;

		licenceUsages = new ArrayList<LicenceUsageEntry>();

		if (persist.withLicenceManagement) {
			persist.notifyDataLoadingObservers(
					configed.getResourceValue("LoadingObserver.loadtable") + " license_on_client");

			List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM("licenseOnClient_getObjects");

			for (Map<String, Object> importedEntry : retrieved) {
				LicenceUsageEntry entry = new LicenceUsageEntry(importedEntry);

				licenceUsages.add(entry);
			}
		}

	}

	//===================================================
	protected LicencePoolXOpsiProduct licencePoolXOpsiProduct;

	@Override
	public void licencePoolXOpsiProductRequestRefresh() {
		logging.info(this, "licencePoolXOpsiProductRequestRefresh");
		licencePoolXOpsiProduct = null;
	}

	@Override
	public LicencePoolXOpsiProduct getLicencePoolXOpsiProduct() {
		retrieveLicencePoolXOpsiProduct();
		return licencePoolXOpsiProduct;
	}

	protected void retrieveLicencePoolXOpsiProduct()
	//LICENSE_POOL
	{
		if (licencePoolXOpsiProduct != null)
			return;

		logging.info(this, "retrieveLicencePoolXOpsiProduct");

		persist.notifyDataLoadingObservers(
				configed.getResourceValue("LoadingObserver.loadtable") + " PRODUCT_ID_TO_LICENSE_POOL");

		List<Map<String, Object>> retrieved = persist.retrieveListOfMapsNOM(
				LicencePoolXOpsiProduct.SERVICE_ATTRIBUTES_asArray, new HashMap(), //callFilter
				"licensePool_getObjects");
		//integrates two database calls

		licencePoolXOpsiProduct = new LicencePoolXOpsiProduct();

		for (Map<String, Object> map : retrieved) {
			//logging.info(this, "retrieved map " + map);

			licencePoolXOpsiProduct.integrateRawFromService(map);
		}

	}

	//===================================================  client2HwRows

	protected Map<String, Map<String, Object>> client2HwRows;
	protected java.sql.Time HW_INFO_last_entry = null;

	@Override
	public void client2HwRowsRequestRefresh() {
		logging.info(this, "client2HwRowsRequestRefresh");
		client2HwRows = null;
		//client2HwRowsColumnNames = null;
		//client2HwRowsClassNames = null;
	}

	@Override
	protected void retrieveClient2HwRows(String[] hosts) {
		if (client2HwRows == null) {
			client2HwRows = new HashMap<String, Map<String, Object>>();
		}
	}

	@Override
	public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts) {
		retrieveClient2HwRows(hosts);
		return client2HwRows;
	}

}
