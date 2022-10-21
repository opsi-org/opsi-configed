package de.uib.opsicommand.sshcommand;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */

/**
* This Class handles  SSHCommands.
**/
public abstract class SSHCommandParameterMethodsAbstractFacade
{

	public abstract String[] getSelected_clientnames();
	public abstract String[] getSelected_depotnames();
	// public abstract String getSelectedClientName();
	public abstract String getConfig_serverName();
	public abstract String getConfig_sshserverName();

}