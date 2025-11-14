
package de.uib.configed.gui.features.healthcheck;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.HealthDataProcessor;
import de.uib.configed.gui.healthcheck.HealthCheckMsg;
import de.uib.configed.gui.healthcheck.HealthCheckUpdate;
import de.uib.configed.gui.healthcheck.HealthCheckUpdate.HealthCheckEffect;
import de.uib.configed.gui.healthcheck.HealthCheckUpdate.HealthCheckModel;

class HealthCheckUpdateTest {

	private HealthCheckModel makeModelWithHealthData(Map<String, Map<String, Object>> healthData) {
		return new HealthCheckModel(healthData);
	}

	private Map<String, Map<String, Object>> makeHealthDataWithShowDetails(Map<String, Boolean> showDetailsMap) {
		Map<String, Map<String, Object>> healthData = new HashMap<>();
		for (Map.Entry<String, Boolean> entry : showDetailsMap.entrySet()) {
			Map<String, Object> details = new HashMap<>();
			details.put("showDetails", entry.getValue());
			healthData.put(entry.getKey(), details);
		}
		return healthData;
	}

	@Test
	void shouldExpanAllDetails_whenExpandAll() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entry1", false);
		initialShowDetails.put("entry2", false);
		initialShowDetails.put("entry3", true);
		HealthCheckModel model = makeModelWithHealthData(makeHealthDataWithShowDetails(initialShowDetails));
		HealthCheckMsg msg = HealthCheckMsg.SimpleMsg.EXPAND_ALL;

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		Map<String, Map<String, Object>> updatedHealthData = result.model().getHealthData();
		assertEquals(true, updatedHealthData.get("entry1").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(true, updatedHealthData.get("entry2").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(true, updatedHealthData.get("entry3").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldCollapseAllDetails_whenCollapseAll() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entry1", false);
		initialShowDetails.put("entry2", true);
		initialShowDetails.put("entry3", true);
		HealthCheckModel model = makeModelWithHealthData(makeHealthDataWithShowDetails(initialShowDetails));
		HealthCheckMsg msg = HealthCheckMsg.SimpleMsg.COLLAPSE_ALL;

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		Map<String, Map<String, Object>> updatedHealthData = result.model().getHealthData();
		assertEquals(false, updatedHealthData.get("entry1").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(false, updatedHealthData.get("entry2").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(false, updatedHealthData.get("entry3").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateHealthData_whenRefreshHealthData() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entry1", false);
		initialShowDetails.put("entry2", true);
		initialShowDetails.put("entry3", true);
		Map<String, Map<String, Object>> initialData = makeHealthDataWithShowDetails(initialShowDetails);
		initialShowDetails.put("entry2", false);
		Map<String, Map<String, Object>> reloadedData = makeHealthDataWithShowDetails(initialShowDetails);
		HealthCheckModel model = makeModelWithHealthData(initialData);
		HealthCheckMsg msg = new HealthCheckMsg.RefreshHealthData(reloadedData);

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		Map<String, Map<String, Object>> updatedHealthData = result.model().getHealthData();
		assertEquals(false, updatedHealthData.get("entry1").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(false, updatedHealthData.get("entry2").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(true, updatedHealthData.get("entry3").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldToggleDetailsOfEntry_whenToggleDetails() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entryA", false);
		initialShowDetails.put("entryB", true);
		HealthCheckModel model = makeModelWithHealthData(makeHealthDataWithShowDetails(initialShowDetails));
		HealthCheckMsg msg = new HealthCheckMsg.ToggleDetails("entryA");

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		Map<String, Map<String, Object>> updatedHealthData = result.model().getHealthData();
		assertEquals(true, updatedHealthData.get("entryA").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertEquals(true, updatedHealthData.get("entryB").get(HealthDataProcessor.KEY_SHOW_DETAILS));
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerCopyEffect_whenCopyHealthInformation() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entryX", false);
		HealthCheckModel model = makeModelWithHealthData(makeHealthDataWithShowDetails(initialShowDetails));
		HealthCheckMsg msg = HealthCheckMsg.SimpleMsg.COPY_HEALTH_INFORMATION;

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(HealthCheckEffect.SimpleEffect.COPY, result.effect().get()));
	}

	@Test
	void shouldTriggerDownloadEffect_whenDownloadDiagnosticData() {
		Map<String, Boolean> initialShowDetails = new HashMap<>();
		initialShowDetails.put("entryX", false);
		HealthCheckModel model = makeModelWithHealthData(makeHealthDataWithShowDetails(initialShowDetails));
		HealthCheckMsg msg = HealthCheckMsg.SimpleMsg.DOWNLOAD_DIAGNOSTIC_DATA;

		UpdateResult<HealthCheckModel, HealthCheckEffect> result = HealthCheckUpdate.update(model, msg);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(HealthCheckEffect.SimpleEffect.DOWNLOAD, result.effect().get()));
	}
}
