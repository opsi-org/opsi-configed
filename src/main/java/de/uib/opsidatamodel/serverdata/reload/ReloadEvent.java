/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.reload.handler.ConfigOptionsDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DepotChangeReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.DepotProductPropertiesDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HardwareConfDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.HostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.InstalledSoftwareDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicenseContractDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicenseDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.LicensePoolDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiHostDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.OpsiLicenseReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ProductDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.RelationsASWToLPDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.SoftwareLicense2LicensePoolDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.StatisticsDataReloadHandler;

/**
 * Events that trigger specifc reload handlers, that reload required data.
 * <p>
 * {@link ReloadEvent} and {@link ReloadHandler} implementations are used to
 * reload persistent data that are closely related or have to be reloaded
 * together. Data that only consist of one entry in internal cache is reloaded
 * using {@link DefaultDataReloadHandler} and is identified by the
 * {@link CacheIdentifier}.
 */
public enum ReloadEvent {
	/**
	 * Triggers {@link OpsiHostDataReloadHandler}.
	 */
	OPSI_HOST_DATA_RELOAD("opsi_host_data_reload"),

	/**
	 * Triggers {@link RelationsASWToLPDataReloadHandler}.
	 */
	ASW_TO_LP_RELATIONS_DATA_RELOAD("asw_to_lp_relations_data_reload"),

	/**
	 * Triggers {@link DepotChangeReloadHandler}.
	 */
	DEPOT_CHANGE_RELOAD("depot_change_reload"),

	/**
	 * Triggers {@link ProductDataReloadHandler}.
	 */
	PRODUCT_DATA_RELOAD("product_data_reload"),

	/**
	 * Triggers {@link OpsiLicenseReloadHandler}.
	 */
	OPSI_LICENSE_RELOAD("opsi_license_reload"),

	/**
	 * Triggers {@link ConfigOptionsDataReloadHandler}.
	 */
	CONFIG_OPTIONS_RELOAD("config_options_reload"),

	/**
	 * Triggers {@link HardwareConfDataReloadHandler}.
	 */
	HARDWARE_CONF_RELOAD("hardware_conf_reload"),

	/**
	 * Triggers {@link InstalledSoftwareDataReloadHandler}.
	 */
	INSTALLED_SOFTWARE_RELOAD("installed_software_reload"),

	/**
	 * Triggers {@link LicenseDataReloadHandler}.
	 */
	LICENSE_DATA_RELOAD("license_data_reload"),

	/**
	 * Triggers {@link LicenseContractDataReloadHandler}.
	 */
	LICENSE_CONTRACT_DATA_RELOAD("license_contract_data_reload"),

	/**
	 * Triggers {@link LicensePoolDataReloadHandler}.
	 */
	LICENSE_POOL_DATA_RELOAD("license_pool_data_reload"),

	/**
	 * Triggers {@link SoftwareLicense2LicensePoolDataReloadHandler}.
	 */
	SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD("software_license_to_license_pool_data_reload"),

	/**
	 * Triggers {@link StatisticsDataReloadHandler}.
	 */
	STATISTICS_DATA_RELOAD("statistics_reload_handler"),

	/**
	 * Triggers {@link HostDataReloadHandler}.
	 */
	HOST_DATA_RELOAD("host_data_reload"),

	/**
	 * Trigers {@link DepotProductPropertiesDataReloadHandler}
	 */
	DEPOT_PRODUCT_PROPERTIES_DATA_RELOAD("depot_product_properties_data_reload");
	;

	private String displayName;

	ReloadEvent(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
