/**
 * DataStub
 * Holds the data got from the opsi server as a stub for the data base.
 * Acts as an observable for data changes
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2014-2018
 *  organization: uib.de
 * @author  R. Roeder 
 */

/* yet to replace

getLicensePools_listOfHashes()
getSoftwareLicenses_listOfHashes() 
getLicenseContracts_listOfHashes() 
getSoftwareLicenses_listOfHashes()

getServerIds_list()
*/

package de.uib.opsidatamodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

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
import de.uib.utilities.table.ListCellOptions;

public abstract class DataStub {

	public abstract boolean canCallMySQL();

	public abstract void productDataRequestRefresh();

	public abstract void product2versionInfoRequestRefresh();

	public abstract Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos();

	public abstract void productsAllDepotsRequestRefresh();

	public abstract Map<String, TreeSet<OpsiPackage>> getDepot2Packages();

	public abstract Vector<Vector<Object>> getProductRows();

	public abstract Map<String, Map<String, List<String>>> getProduct2VersionInfo2Depots();

	public abstract Object2Product2VersionList getDepot2LocalbootProducts();

	public abstract Object2Product2VersionList getDepot2NetbootProducts();

	public abstract void productPropertyDefinitionsRequestRefresh();

	public abstract Map<String, Map<String, Map<String, ListCellOptions>>> getDepot2Product2PropertyDefinitions();

	public abstract void productDependenciesRequestRefresh();

	public abstract Map<String, Map<String, List<Map<String, String>>>> getDepot2product2dependencyInfos();

	public abstract void productPropertyStatesRequestRefresh();

	public abstract void fillProductPropertyStates(Collection<String> clients);

	public abstract List<Map<String, Object>> getProductPropertyStates();

	public abstract List<Map<String, Object>> getProductPropertyDepotStates(java.util.Set<String> depots);

	public abstract void installedSoftwareInformationRequestRefresh();

	public abstract List<String> getSoftwareList();

	public abstract TreeMap<String, Integer> getSoftware2Number();

	public abstract String getSWident(Integer i);

	public abstract TreeMap<String, SWAuditEntry> getInstalledSoftwareInformation();

	public abstract TreeMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing();

	public abstract TreeMap<String, Set<String>> getName2SWIdents();

	public abstract TreeMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo();

	public abstract void softwareAuditOnClientsRequestRefresh();

	public abstract void fillClient2Software(List<String> clients);

	public abstract void fillClient2Software(String client);

	public abstract Map<String, List<SWAuditClientEntry>> getClient2Software();

	public abstract Map<String, java.util.Set<String>> getSoftwareIdent2clients();

	public abstract void auditSoftwareXLicencePoolRequestRefresh();

	public abstract AuditSoftwareXLicencePool getAuditSoftwareXLicencePool();

	public abstract void hostConfigsRequestRefresh();

	public abstract Map<String, Map<String, Object>> getConfigs();
	// host -> (key -> value)

	public abstract void licencepoolsRequestRefresh();

	public abstract Map<String, LicencepoolEntry> getLicencepools();

	public abstract void licenceContractsRequestRefresh();

	public abstract Map<String, LicenceContractEntry> getLicenceContracts();

	public abstract TreeMap<String, TreeSet<String>> getLicenceContractsExpired();
	// date in sql time format, contrad ID

	public abstract TreeMap<String, TreeSet<String>> getLicenceContractsToNotify();
	// date in sql time format, contrad ID

	public abstract void licencesRequestRefresh();

	public abstract java.util.Map<String, LicenceEntry> getLicences();

	public abstract void licenceUsabilitiesRequestRefresh();

	public abstract List<LicenceUsableForEntry> getLicenceUsabilities();

	public abstract void licenceUsagesRequestRefresh();

	public abstract List<LicenceUsageEntry> getLicenceUsages();

	public abstract void licencePoolXOpsiProductRequestRefresh();

	public abstract LicencePoolXOpsiProduct getLicencePoolXOpsiProduct();

	public abstract void client2HwRowsRequestRefresh();

	protected abstract void retrieveClient2HwRows(String[] hosts);

	public abstract Map<String, Map<String, Object>> getClient2HwRows(String[] hosts);

	public static Set<String> linuxSWnameMarkers = new HashSet<>();
	static {
		linuxSWnameMarkers.add("linux");
		linuxSWnameMarkers.add("Linux");
		linuxSWnameMarkers.add("lib");
		linuxSWnameMarkers.add("ubuntu");
		linuxSWnameMarkers.add("ubuntu");
	}
	public static Set<String> linuxSubversionMarkers = new HashSet<>();
	static {
		linuxSubversionMarkers.add("lin:");
	}

}
