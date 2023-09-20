/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.serverdata.reload;

import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.reload.handler.DefaultDataReloadHandler;
import de.uib.opsidatamodel.serverdata.reload.handler.ReloadHandler;

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
	OPSI_HOST_DATA_RELOAD("opsi_host_data_reload"), ASW_TO_LP_RELATIONS_DATA_RELOAD("asw_to_lp_relations_data_reload"),
	DEPOT_CHANGE_RELOAD("depot_change_reload"), PRODUCT_DATA_RELOAD("product_data_reload"),
	OPSI_LICENSE_RELOAD("opsi_license_reload"), CONFIG_OPTIONS_RELOAD("config_options_reload"),
	CLIENT_HARDWARE_RELOAD("client_hardware_reload"), HARDWARE_CONF_RELOAD("hardware_conf_reload"),
	RECONCILIATION_INFO_RELOAD("reconciliation_info_reload"), INSTALLED_SOFTWARE_RELOAD("installed_software_reload"),
	HOST_CONFIG_RELOAD("host_config_reload"), PRODUCT_PROPERTIES_RELOAD("product_properties_reload"),
	LICENSE_DATA_RELOAD("license_data_reload"), HOST_DATA_RELOAD("host_data_reload"),
	ESSENTIAL_DATA_RELOAD("essential_data_reload");

	private String displayName;

	ReloadEvent(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
