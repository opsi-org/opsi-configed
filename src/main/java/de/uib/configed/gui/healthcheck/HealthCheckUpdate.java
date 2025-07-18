/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.healthcheck;

import java.util.HashMap;
import java.util.Map;

import de.uib.configed.gui.AbstractTeaComponent;
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
	public static AbstractTeaComponent.UpdateResult<HealthCheckModel, HealthCheckEffect> update(HealthCheckModel model,
			HealthCheckMsg msg) {
		return switch (msg) {
		case HealthCheckMsg.ExpandAll ignored -> UpdateResult.noEffect(updateAllShowDetails(model, true));
		case HealthCheckMsg.CollapseAll ignored -> UpdateResult.noEffect(updateAllShowDetails(model, false));
		case HealthCheckMsg.ToggleDetails(String key) -> {
			Map<String, Map<String, Object>> newHealthData = deepCopy(model.getHealthData());
			Map<String, Object> details = newHealthData.get(key);
			if (details != null && details.containsKey("showDetails")) {
				boolean current = Boolean.TRUE.equals(details.get("showDetails"));
				details.put("showDetails", !current);
			}
			yield UpdateResult.noEffect(HealthCheckModel.initial(newHealthData));
		}
		case HealthCheckMsg.HealthDataRefreshed(Map<String, Map<String, Object>> newHealthData) -> UpdateResult
				.noEffect(HealthCheckModel.initial(newHealthData));
		case HealthCheckMsg.CopyHealthInformation ignored -> UpdateResult.withEffect(model,
				new HealthCheckEffect.Copy());
		case HealthCheckMsg.DownloadDiagnosticData ignored -> UpdateResult.withEffect(model,
				new HealthCheckEffect.Download());
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
