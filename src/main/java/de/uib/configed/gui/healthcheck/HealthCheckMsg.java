/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

public sealed interface HealthCheckMsg permits HealthCheckMsg.SimpleMsg, HealthCheckMsg.ToggleDetails {

	enum SimpleMsg implements HealthCheckMsg {
		EXPAND_ALL, COLLAPSE_ALL, COPY_HEALTH_REPORT, DOWNLOAD_DIAGNOSTIC_DATA
	}

	record ToggleDetails(String key) implements HealthCheckMsg {
	}
}
