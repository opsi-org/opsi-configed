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

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.productstate.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.table.*;


public abstract class DataStub
{
	
	abstract public boolean test();
	
	abstract public void productDataRequestRefresh();
	
	abstract public void product2versionInfoRequestRefresh();
	abstract public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos();
	
	abstract public void productsAllDepotsRequestRefresh();
	abstract public Map<String, TreeSet<OpsiPackage>> getDepot2Packages();
	abstract public Vector<Vector<Object>> getProductRows();
	abstract public Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots();
	abstract public Object2Product2VersionList getDepot2LocalbootProducts();
	abstract public Object2Product2VersionList getDepot2NetbootProducts();
	
	//abstract public HashMap<String, java.util.List<String>> getProductversion2Depots();
	
	abstract public void productPropertyDefinitionsRequestRefresh();
	abstract public Map<String, Map<String, Map<String, ListCellOptions>>>  getDepot2Product2PropertyDefinitions();
		
	abstract public void productDependenciesRequestRefresh();
	abstract public Map<String, Map<String, java.util.List<Map<String, String>>>>  getDepot2product2dependencyInfos();
	
	abstract public void productPropertyStatesRequestRefresh();
	abstract public void fillProductPropertyStates(Collection<String> clients);
	abstract public java.util.List<Map<String, Object>> getProductPropertyStates();
	
	//abstract protected void productPropertyDepotStatesRequestRefresh();
	//abstract public void fillProductPropertyDepotStates(Collection<String> clients);
	abstract public java.util.List<Map<String, Object>> getProductPropertyDepotStates(java.util.Set<String> depots);
	
	abstract public void installedSoftwareInformationRequestRefresh();
	abstract public ArrayList<String> getSoftwareList();
	abstract public TreeMap<String, Integer> getSoftware2Number();
	abstract public String getSWident(Integer i);
	abstract public TreeMap<String, SWAuditEntry> getInstalledSoftwareInformation();
	abstract public TreeMap<String, SWAuditEntry> getInstalledSoftwareInformationForLicensing();
	abstract public TreeMap<String, Set<String>> getName2SWIdents();
	abstract public TreeMap<String, Map<String, String>> getInstalledSoftwareName2SWinfo();
	
	abstract public void softwareAuditOnClientsRequestRefresh();
	abstract public void fillClient2Software(java.util.List<String> clients);
	abstract public void fillClient2Software(String client);
	abstract public  Map<String, java.util.List<SWAuditClientEntry>> getClient2Software();
	//abstract public  Map<Integer, java.util.List<String>> getSoftwareId2clients();
	abstract public  Map<String, java.util.Set<String>> getSoftwareIdent2clients();

	
	abstract public void  auditSoftwareXLicencePoolRequestRefresh();
	abstract public AuditSoftwareXLicencePool getAuditSoftwareXLicencePool();
	
	abstract public void hostConfigsRequestRefresh();
	abstract public Map<String, Map<String, Object>> getConfigs();
	//host -> (key -> value) 
	//abstract public Map<String, Map<String, java.util.List<Object>>> getConfigValues();
	//host;key -> valuelist
	
	abstract public void licencepoolsRequestRefresh();
	abstract public Map<String, LicencepoolEntry> getLicencepools();
	
	abstract public void licenceContractsRequestRefresh();
	abstract public Map<String, LicenceContractEntry> getLicenceContracts();
	
	abstract public TreeMap<String, TreeSet<String>> getLicenceContractsExpired();
	// date in sql time format, contrad  ID
	
	abstract public TreeMap<String, TreeSet<String>> getLicenceContractsToNotify();
	// date in sql time format, contrad  ID
	
	abstract public void licencesRequestRefresh();
	abstract public java.util.Map<String, LicenceEntry> getLicences();
	
	abstract public void licenceUsabilitiesRequestRefresh();
	abstract public java.util.List<LicenceUsableForEntry> getLicenceUsabilities();
	
	abstract public void licenceUsagesRequestRefresh();
	abstract public  java.util.List <LicenceUsageEntry> getLicenceUsages();
		
	abstract public void licencePoolXOpsiProductRequestRefresh();
	abstract public LicencePoolXOpsiProduct getLicencePoolXOpsiProduct();
	
	abstract public void client2HwRowsRequestRefresh();
	abstract protected void retrieveClient2HwRows(String[] hosts);
	abstract public Map<String, Map<String, Object>> getClient2HwRows(String[] hosts);
	
	static public Set<String> linuxSWnameMarkers = new HashSet<String>();
	static{ linuxSWnameMarkers.add("linux");linuxSWnameMarkers.add("Linux");  linuxSWnameMarkers.add("lib"); linuxSWnameMarkers.add("ubuntu");  linuxSWnameMarkers.add("ubuntu"); }
	static public Set<String> linuxSubversionMarkers = new HashSet<String>();
	static{ linuxSubversionMarkers.add("lin:"); }
	
	
}



