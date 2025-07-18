/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.HashMap;
import java.util.Map;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

/**
 * Update contains the logic to update the Model in response to a Msg.
 */
public class HealthCheckUpdate {
	private HealthCheckUpdate() {
		// Hide constructor.
	}

	/**
	 * Applies a message to the model and returns a new model.
	 */
	public static UpdateResult<HealthCheckModel, HealthCheckEffect> update(HealthCheckModel model, HealthCheckMsg msg) {
		return switch (msg) {
		case HealthCheckMsg.SimpleMsg m -> handleSimpleMsg(m, model);
		case HealthCheckMsg.ToggleDetails(String key) -> {
			Map<String, Map<String, Object>> newHealthData = deepCopy(model.getHealthData());
			Map<String, Object> details = newHealthData.get(key);
			if (details != null && details.containsKey("showDetails")) {
				boolean current = Boolean.TRUE.equals(details.get("showDetails"));
				details.put("showDetails", !current);
			}
			yield UpdateResult.noEffect(HealthCheckModel.initial(newHealthData));
		}
		case HealthCheckMsg.RefreshHealthData(Map<String, Map<String, Object>> newHealthData) -> UpdateResult
				.noEffect(HealthCheckModel.initial(newHealthData));
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
			details.put("showDetails", show);
		}
		return HealthCheckModel.initial(newHealthData);
	}

	private static Map<String, Map<String, Object>> deepCopy(Map<String, Map<String, Object>> original) {
		Map<String, Map<String, Object>> copy = new HashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry : original.entrySet()) {
			copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
		}
		return copy;
	}
}
