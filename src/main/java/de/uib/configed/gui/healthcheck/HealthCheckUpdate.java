/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import lombok.Value;
import lombok.With;

public class HealthCheckUpdate {
	@Value
	@With
	public static class HealthCheckModel {
		Map<String, Map<String, Object>> healthData;
	}

	public sealed interface HealthCheckEffect permits HealthCheckEffect.SimpleEffect {
		enum SimpleEffect implements HealthCheckEffect {
			COPY, DOWNLOAD
		}
	}

	private HealthCheckUpdate() {
		// Hide constructor.
	}

	public static UpdateResult<HealthCheckModel, HealthCheckEffect> update(HealthCheckModel model, HealthCheckMsg msg) {
		return switch (msg) {
		case HealthCheckMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		case HealthCheckMsg.ToggleDetails(String key) -> {
			Map<String, Map<String, Object>> newHealthData = deepCopy(model.getHealthData());
			Map<String, Object> details = newHealthData.get(key);
			if (details != null && details.containsKey(HealthDataProcessor.KEY_SHOW_DETAILS)) {
				boolean current = Boolean.TRUE.equals(details.get(HealthDataProcessor.KEY_SHOW_DETAILS));
				details.put(HealthDataProcessor.KEY_SHOW_DETAILS, !current);
			}
			yield UpdateResult.noEffect(initModel(newHealthData));
		}
		case HealthCheckMsg.RefreshHealthData(Map<String, Map<String, Object>> newHealthData) -> UpdateResult
				.noEffect(initModel(newHealthData));
		};
	}

	private static UpdateResult<HealthCheckModel, HealthCheckEffect> handleSimpleMsg(HealthCheckMsg.SimpleMsg msg,
			HealthCheckModel model) {
		return switch (msg) {
		case EXPAND_ALL -> UpdateResult.noEffect(updateAllShowDetails(model, true));
		case COLLAPSE_ALL -> UpdateResult.noEffect(updateAllShowDetails(model, false));
		case COPY_HEALTH_INFORMATION -> UpdateResult.withEffect(model, HealthCheckEffect.SimpleEffect.COPY);
		case DOWNLOAD_DIAGNOSTIC_DATA -> UpdateResult.withEffect(model, HealthCheckEffect.SimpleEffect.DOWNLOAD);
		};
	}

	private static HealthCheckModel updateAllShowDetails(HealthCheckModel model, boolean show) {
		Map<String, Map<String, Object>> newHealthData = deepCopy(model.getHealthData());
		for (Map<String, Object> details : newHealthData.values()) {
			details.put(HealthDataProcessor.KEY_SHOW_DETAILS, show);
		}
		return initModel(newHealthData);
	}

	private static HealthCheckModel initModel(Map<String, Map<String, Object>> healthData) {
		return new HealthCheckModel(healthData);
	}

	private static Map<String, Map<String, Object>> deepCopy(Map<String, Map<String, Object>> original) {
		Map<String, Map<String, Object>> copy = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry : original.entrySet()) {
			copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
		}
		return copy;
	}
}
