/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.Map;

public sealed interface HealthCheckMsg
		permits HealthCheckMsg.SimpleMsg, HealthCheckMsg.ToggleDetails, HealthCheckMsg.RefreshHealthData {

	enum SimpleMsg implements HealthCheckMsg {
		EXPAND_ALL, COLLAPSE_ALL, COPY_HEALTH_INFORMATION, DOWNLOAD_DIAGNOSTIC_DATA
	}

	record ToggleDetails(String key) implements HealthCheckMsg {
	}

	record RefreshHealthData(Map<String, Map<String, Object>> newHealthData) implements HealthCheckMsg {
	}
}