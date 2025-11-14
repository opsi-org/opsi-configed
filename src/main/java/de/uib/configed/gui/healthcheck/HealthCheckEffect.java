/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

public sealed interface HealthCheckEffect permits HealthCheckEffect.SimpleEffect {
	enum SimpleEffect implements HealthCheckEffect {
		COPY, DOWNLOAD
	}
}