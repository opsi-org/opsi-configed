/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

import de.uib.configed.core.domain.HostInfoCollections;
import de.uib.configed.core.domain.serverdata.CacheManager;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.RPCMethodExecutor;
import de.uib.configed.core.infrastructure.AbstractPOJOExecutioner;
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

	public final AbstractPOJOExecutioner exec;
	public final OpsiServiceNOMPersistenceController persistenceController;

	public final CacheManager cacheManager = CacheManager.getInstance();

	public DataServices(ServerFacade exec, OpsiServiceNOMPersistenceController persistenceController) {
		this.exec = exec;
		this.persistenceController = persistenceController;

		hostInfoCollections = new HostInfoCollections(this);
		userRoles = new UserRolesConfigDataService(this);
		config = new ConfigDataService(this);
		depot = new DepotDataService(this);
		group = new GroupDataService(this);
		hardware = new HardwareDataService(this);
		health = new HealthDataService(this);
		host = new HostDataService(this);
		license = new LicenseDataService(this);
		log = new LogDataService(this);
		module = new ModuleDataService(this);
		product = new ProductDataService(this);
		software = new SoftwareDataService(this);
		command = new CommandDataService(this);
		user = new UserDataService(this);
		rpcMethodExecutor = new RPCMethodExecutor(this);
	}
}
