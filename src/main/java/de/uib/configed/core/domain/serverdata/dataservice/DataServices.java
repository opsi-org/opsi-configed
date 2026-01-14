/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import de.uib.configed.core.domain.HostInfoCollections;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodExecutor;
import de.uib.configed.core.infrastructure.ServerFacade;

public class DataServices {
	public final HostInfoCollections hostInfoCollections;
	public final ConfigDataService config;
	public final UserRolesConfigDataService userRoles;
	public final DepotDataService depot;
	public final GroupDataService group;
	public final HardwareDataService hardware;
	public final HealthDataService health;
	public final HostDataService host;
	public final LicenseDataService license;
	public final LogDataService log;
	public final ModuleDataService module;
	public final ProductDataService product;
	public final SoftwareDataService software;
	public final CommandDataService command;
	public final UserDataService user;
	public final RPCMethodExecutor rpcMethodExecutor;

	public DataServices(ServerFacade exec, OpsiServiceNOMPersistenceController ctx) {
		hostInfoCollections = new HostInfoCollections(ctx);
		userRoles = new UserRolesConfigDataService(exec, ctx);
		config = new ConfigDataService(exec, ctx);
		depot = new DepotDataService(exec);
		group = new GroupDataService(exec, ctx);
		hardware = new HardwareDataService(exec);
		health = new HealthDataService(exec);
		host = new HostDataService(exec, ctx);
		license = new LicenseDataService(exec);
		log = new LogDataService(exec);
		module = new ModuleDataService(exec);
		product = new ProductDataService(exec, ctx);
		software = new SoftwareDataService(exec, ctx);
		command = new CommandDataService(exec);
		user = new UserDataService(exec);
		rpcMethodExecutor = new RPCMethodExecutor(exec, ctx);

		wireServices();
	}

	private void wireServices() {
		config.setUserRolesConfigDataService(userRoles);

		depot.setUserRolesConfigDataService(userRoles);
		depot.setProductDataService(product);
		depot.setHostInfoCollections(hostInfoCollections);

		group.setUserRolesConfigDataService(userRoles);

		host.setConfigDataService(config);
		host.setHostInfoCollections(hostInfoCollections);
		host.setHostInfoCollections(hostInfoCollections);
		host.setUserRolesConfigDataService(userRoles);

		license.setUserRolesConfigDataService(userRoles);
		license.setModuleDataService(module);

		module.setUserRolesConfigDataService(userRoles);
		module.setHostInfoCollections(hostInfoCollections);

		product.setConfigDataService(config);
		product.setDepotDataService(depot);
		product.setHostInfoCollections(hostInfoCollections);
		product.setUserRolesConfigDataService(userRoles);

		software.setModuleDataService(module);
		software.setLicenseDataService(license);
		software.setUserRolesConfigDataService(userRoles);
		software.setHostInfoCollections(hostInfoCollections);

		command.setUserRolesConfigDataService(userRoles);

		rpcMethodExecutor.setHostDataService(host);
		rpcMethodExecutor.setHostInfoCollections(hostInfoCollections);
	}
}
