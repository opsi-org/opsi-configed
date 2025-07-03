/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model represents the immutable state of the HealthCheck UI.
 */
public class HealthCheckModel {
	private final Map<String, Map<String, Object>> healthData;
	private final boolean allDetailsShown;
	private final boolean anyDetailsShown;

	public HealthCheckModel(Map<String, Map<String, Object>> healthData, boolean allDetailsShown,
			boolean anyDetailsShown) {
		// Defensive copy for immutability
		this.healthData = healthData == null ? Collections.emptyMap()
				: Collections.unmodifiableMap(new HashMap<>(healthData));
		this.allDetailsShown = allDetailsShown;
		this.anyDetailsShown = anyDetailsShown;
	}

	public Map<String, Map<String, Object>> getHealthData() {
		return healthData;
	}

	public boolean isAllDetailsShown() {
		return allDetailsShown;
	}

	public boolean isAnyDetailsShown() {
		return anyDetailsShown;
	}

	public HealthCheckModel withHealthData(Map<String, Map<String, Object>> newHealthData) {
		return new HealthCheckModel(newHealthData, this.allDetailsShown, this.anyDetailsShown);
	}

	public HealthCheckModel withAllDetailsShown(boolean allDetailsShown) {
		return new HealthCheckModel(this.healthData, allDetailsShown, this.anyDetailsShown);
	}

	public HealthCheckModel withAnyDetailsShown(boolean anyDetailsShown) {
		return new HealthCheckModel(this.healthData, this.allDetailsShown, anyDetailsShown);
	}

	public static HealthCheckModel initial(Map<String, Map<String, Object>> healthData) {
		boolean allShown = true;
		boolean anyShown = false;
		if (healthData != null) {
			for (Map<String, Object> details : healthData.values()) {
				boolean show = Boolean.TRUE.equals(details.get("showDetails"));
				anyShown = anyShown || show;
				if (!show && details.get("details") != null && !((String) details.get("details")).isEmpty()) {
					allShown = false;
				}
			}
		}
		return new HealthCheckModel(healthData, allShown, anyShown);
	}
}
