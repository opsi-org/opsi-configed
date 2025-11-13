/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.healthcheck.settings;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.healthcheck.settings.DowntimeType;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsEffect;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsModel;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsMsg;
import de.uib.configed.gui.healthcheck.settings.HealthCheckSettingsUpdate;

class HealthCheckSettingsUpdateTest {

	private HealthCheckSettingsModel makeDefaultSettingsModel() {
		return HealthCheckSettingsModel.initial(List.of(), FlatTriStateCheckBox.State.INDETERMINATE, "", "", false);
	}

	private HealthCheckSettingsModel makeSettingsModelWith(List<String> selectedHosts,
			FlatTriStateCheckBox.State checkActiveState, boolean saveEnabled, String startDowntime,
			String endDowntime) {
		return HealthCheckSettingsModel.initial(selectedHosts, checkActiveState, startDowntime, endDowntime,
				saveEnabled);
	}

	@Test
	void shouldTriggerSelectHostsEffect_whenHostSelectionRequested() {
		HealthCheckSettingsModel initialModel = makeDefaultSettingsModel();
		HealthCheckSettingsMsg msg = new HealthCheckSettingsMsg.HostsSelectionRequested();

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(HealthCheckSettingsEffect.SimpleEffect.SELECT_HOSTS, result.effect().get()));
	}

	@Test
	void shouldUpdateSelectedHosts_whenHostSelected() {
		HealthCheckSettingsModel initialModel = makeDefaultSettingsModel();
		List<String> newHosts = List.of("host1", "host2");
		HealthCheckSettingsMsg msg = new HealthCheckSettingsMsg.HostsSelected(newHosts);

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertEquals(newHosts, result.model().getSelectedHosts());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerSelectDowntimeEffect_whenDowntimeSelectionRequested() {
		HealthCheckSettingsModel initialModel = makeDefaultSettingsModel();
		DowntimeType downtimeType = DowntimeType.START;
		HealthCheckSettingsMsg msg = new HealthCheckSettingsMsg.DowntimeSelectionRequested(downtimeType);

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertEquals(new HealthCheckSettingsEffect.SelectDownTime(downtimeType), result.effect().get()));
	}

	@Test
	void shouldUpdateDowntime_whenDowntimeSelected() {
		HealthCheckSettingsModel initialModel = makeDefaultSettingsModel();
		String downtime = "2024-01-01T00:00";
		HealthCheckSettingsMsg msg = new HealthCheckSettingsMsg.DowntimeSelected(DowntimeType.START, downtime);

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertEquals(downtime, result.model().getStartDowntime());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldToggleActivityAndEnableSave_whenToggleActivity() {
		HealthCheckSettingsModel initialModel = makeSettingsModelWith(List.of("host1"),
				FlatTriStateCheckBox.State.INDETERMINATE, false, "2024-01-01T00:00", "2024-01-01T01:00");
		FlatTriStateCheckBox.State newState = FlatTriStateCheckBox.State.SELECTED;
		HealthCheckSettingsMsg msg = new HealthCheckSettingsMsg.ToggleActivity(newState);

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertEquals(newState, result.model().getCheckActiveState());
		assertTrue(result.model().isSaveEnabled());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerSaveEffect_whenSave() {
		HealthCheckSettingsModel initialModel = makeSettingsModelWith(List.of("host1"),
				FlatTriStateCheckBox.State.SELECTED, true, "2024-01-01T00:00", "2024-01-01T01:00");
		HealthCheckSettingsMsg msg = HealthCheckSettingsMsg.SimpleMsg.SAVE;

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertSame(initialModel, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(HealthCheckSettingsEffect.SimpleEffect.SAVE, result.effect().get()));
	}

	@Test
	void shouldTriggerCloseEffect_whenCancle() {
		HealthCheckSettingsModel initialModel = makeDefaultSettingsModel();
		HealthCheckSettingsMsg msg = HealthCheckSettingsMsg.SimpleMsg.CANCLE;

		UpdateResult<HealthCheckSettingsModel, HealthCheckSettingsEffect> result = HealthCheckSettingsUpdate
				.update(initialModel, msg);

		assertSame(initialModel, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertSame(HealthCheckSettingsEffect.SimpleEffect.CLOSE, result.effect().get()));
	}
}