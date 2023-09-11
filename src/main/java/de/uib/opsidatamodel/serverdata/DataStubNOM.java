/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

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
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.ListCellOptions;

public class DataStubNOM {

	private OpsiServiceNOMPersistenceController persistenceController;

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
	public DataStubNOM(OpsiServiceNOMPersistenceController persistenceController) {
		this.persistenceController = persistenceController;
		// classCounter++;
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

	public void product2versionInfoRequestRefresh() {
		product2versionInfo2infos = null;
	}

	public void productsAllDepotsRequestRefresh() {
		depot2LocalbootProducts = null;
	}

	public void productPropertyDefinitionsRequestRefresh() {
		depot2Product2PropertyDefinitions = null;
	}

	public void productDependenciesRequestRefresh() {
		depot2product2dependencyInfos = null;
	}

	public void productPropertyStatesRequestRefresh() {
		Logging.info(this, "productPropertyStatesRequestRefresh");
		productPropertyStates = null;
	}

	private void productPropertyDepotStatesRequestRefresh() {
		Logging.info(this, "productPropertyDepotStatesRequestRefresh");
		productPropertyDepotStates = null;
	}

	public void installedSoftwareInformationRequestRefresh() {
		installedSoftwareInformation = null;
		installedSoftwareInformationForLicensing = null;
		name2SWIdents = null;
	}

	public void softwareAuditOnClientsRequestRefresh() {
		Logging.info(this, "softwareAuditOnClientsRequestRefresh");
		client2software = null;
		softwareIdent2clients = null;
	}

	public void auditSoftwareXLicencePoolRequestRefresh() {
		Logging.info(this, "auditSoftwareXLicencePoolRequestRefresh");
		auditSoftwareXLicencePool = null;
	}

	public void hostConfigsRequestRefresh() {
		Logging.info(this, "hostConfigsRequestRefresh");
		hostConfigs = null;
	}

	public void licencepoolsRequestRefresh() {
		Logging.info(this, "licencepoolsRequestRefresh");
		licencepools = null;
	}

	public void licenceContractsRequestRefresh() {
		Logging.info(this, "licenceContractsRequestRefresh");

		licenceContracts = null;
		contractsToNotify = null;
	}

	public void licencesRequestRefresh() {
		Logging.info(this, "licencesRequestRefresh");
		licences = null;
	}

	public void licenceUsabilitiesRequestRefresh() {
		Logging.info(this, "licenceUsabilitiesRequestRefresh");
		licenceUsabilities = null;
	}

	public void licenceUsagesRequestRefresh() {
		Logging.info(this, "licenceUsagesRequestRefresh");
		licenceUsages = null;
	}

	public void licencePoolXOpsiProductRequestRefresh() {
		Logging.info(this, "licencePoolXOpsiProductRequestRefresh");
		licencePoolXOpsiProduct = null;
	}

	public void client2HwRowsRequestRefresh() {
		Logging.info(this, "client2HwRowsRequestRefresh");
		client2HwRows = null;
	}
}
