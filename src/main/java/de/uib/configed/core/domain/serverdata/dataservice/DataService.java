/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.domain.serverdata.dataservice;

public class DataService {
	protected final DataServices dataServices;

	protected DataService(DataServices dataServices) {
		this.dataServices = dataServices;
	}
}
