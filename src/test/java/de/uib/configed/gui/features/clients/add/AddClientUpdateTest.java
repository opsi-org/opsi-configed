/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

class AddClientUpdateTest {

	private static AddClientModel baseModel() {
		return AddClientModel.builder().hostname("").selectedDomain("").description("").inventoryNumber("").notes("")
				.systemUUID("").macAddress("").ipAddress("").groups("").selectedDepot("").selectedNetbootProduct("")
				.wanEnabled(true).wanSelected(false).shutdownInstallSelected(false)
				.domains(List.of("example.com", "corp.local")).depots(List.of("depot1", "depot2"))
				.netbootProducts(List.of("nb1", "nb2")).withDialog(true).initialized(true).build();
	}

	@Test
	void shouldTriggerLoadInitDataEffect_whenLoadInitDataRequested() {
		AddClientModel model = baseModel();
		AddClientMsg msg = new AddClientMsg.LoadInitDataRequested();

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.ServiceEffect.LoadInitialData.class, result.effect().get()));
	}

	@Test
	void shouldLoadInitialData_whenInitDataLoaded() {
		AddClientModel model = baseModel();
		List<String> domains = List.of("domain1.local", "domain2.local");
		List<String> depots = List.of("newdepot1", "newdepot2");
		List<String> netboots = List.of("netboot1", "netboot2");
		boolean isWanActive = true;
		boolean defaultWanSelected = false;
		boolean defaultShutdown = true;
		AddClientMsg msg = new AddClientMsg.InitDataLoaded(domains, depots, netboots, isWanActive, defaultWanSelected,
				defaultShutdown);

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate.update(msg, model);

		assertAll(() -> assertEquals(domains, result.model().getDomains()),
				() -> assertEquals(depots, result.model().getDepots()),
				() -> assertEquals(netboots, result.model().getNetbootProducts()),
				() -> assertEquals(domains.get(0), result.model().getSelectedDomain()),
				() -> assertTrue(result.model().isWanEnabled()), () -> assertFalse(result.model().isWanSelected()),
				() -> assertTrue(result.model().isShutdownInstallSelected()));
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateSimpleFields_whenChangeMessages() {
		AddClientModel model = baseModel();

		UpdateResult<AddClientModel, AddClientEffect> r1 = AddClientUpdate.update(new AddClientMsg.ChangeHostname("h"),
				model);
		assertEquals("h", r1.model().getHostname());
		assertFalse(r1.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r2 = AddClientUpdate.update(new AddClientMsg.ChangeDomain("d"),
				r1.model());
		assertEquals("d", r2.model().getSelectedDomain());
		assertFalse(r2.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r3 = AddClientUpdate
				.update(new AddClientMsg.ChangeDescription("desc"), r2.model());
		assertEquals("desc", r3.model().getDescription());
		assertFalse(r3.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r4 = AddClientUpdate
				.update(new AddClientMsg.ChangeInventory("inv"), r3.model());
		assertEquals("inv", r4.model().getInventoryNumber());
		assertFalse(r4.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r5 = AddClientUpdate.update(new AddClientMsg.ChangeNotes("n"),
				r4.model());
		assertEquals("n", r5.model().getNotes());
		assertFalse(r5.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r6 = AddClientUpdate
				.update(new AddClientMsg.ChangeSystemUUID("uuid"), r5.model());
		assertEquals("uuid", r6.model().getSystemUUID());
		assertFalse(r6.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r7 = AddClientUpdate.update(new AddClientMsg.ChangeMAC("mac"),
				r6.model());
		assertEquals("mac", r7.model().getMacAddress());
		assertFalse(r7.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r8 = AddClientUpdate.update(new AddClientMsg.ChangeIP("ip"),
				r7.model());
		assertEquals("ip", r8.model().getIpAddress());
		assertFalse(r8.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r9 = AddClientUpdate
				.update(new AddClientMsg.ChangeGroups("g1; g2"), r8.model());
		assertEquals("g1; g2", r9.model().getGroups());
		assertFalse(r9.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r10 = AddClientUpdate.update(new AddClientMsg.ChangeDepot("d1"),
				r9.model());
		assertEquals("d1", r10.model().getSelectedDepot());
		assertFalse(r10.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r11 = AddClientUpdate
				.update(new AddClientMsg.ChangeNetboot("nb1"), r10.model());
		assertEquals("nb1", r11.model().getSelectedNetbootProduct());
		assertFalse(r11.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r12 = AddClientUpdate
				.update(new AddClientMsg.ToggleWanSelected(true), r11.model());
		assertTrue(r12.model().isWanSelected());
		assertFalse(r12.effect().isPresent());

		UpdateResult<AddClientModel, AddClientEffect> r13 = AddClientUpdate
				.update(new AddClientMsg.ToggleShutdownInstall(true), r12.model());
		assertTrue(r13.model().isShutdownInstallSelected());
		assertFalse(r13.effect().isPresent());
	}

	@Test
	void shouldTriggerOpenGroupSelectionDialogEffect_whenOpenGroupSelectionDialog() {
		AddClientModel model = baseModel().withGroups("a; b");

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.OpenGroupSelectionDialog(), model);

		assertSame(model, result.model());
		assertTrue(result.effect().isPresent());
		assertTrue(result.effect().get() instanceof AddClientEffect.UIEffect.OpenGroupSelectionDialog);
	}

	@Test
	void shouldTriggerOpenCSVImportDialog_whenImportCSVRequested() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.ImportCSVRequested(), model);

		assertSame(model, result.model());
		assertTrue(result.effect().isPresent());
		assertTrue(result.effect().get() instanceof AddClientEffect.UIEffect.OpenCsvImportDialog);
	}

	@Test
	void shouldTriggerShowErrorMessageEffect_whenCSVImportedWithNonBooleanValues() {
		AddClientModel model = baseModel();
		List<List<Object>> rows = List
				.of(List.of("host", "dom", "d", "i", "n", "ip", "uuid", "mac", "nbp", "depot", "invalid", "true"));

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.CSVImported(rows), model);

		assertTrue(result.effect().isPresent());
		assertTrue(result.effect().get() instanceof AddClientEffect.UIEffect.ShowErrorMessage);
	}

	@Test
	void shouldTriggerCreateMultipleClientsEffect_whenCSVImported() {
		AddClientModel model = baseModel();
		List<List<Object>> rows = List
				.of(List.of("host", "dom", "d", "i", "n", "ip", "uuid", "mac", "nbp", "depot", "true", "false"));

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.CSVImported(rows), model);

		assertTrue(result.effect().isPresent());
		assertTrue(result.effect().get() instanceof AddClientEffect.ServiceEffect.CreateMultipleClients);
	}

	@Test
	void shouldTriggerCreateSingleClientEffect_whenCreateClient() {
		AddClientModel model = baseModel().withHostname("h").withSelectedDomain("dom").withGroups("g1; g2")
				.withSelectedDepot("depot1").withSelectedNetbootProduct("nb1").withShutdownInstallSelected(true)
				.withWanSelected(true);

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate.update(new AddClientMsg.CreateClient(),
				model);

		assertTrue(result.effect().isPresent());
		assertTrue(result.effect().get() instanceof AddClientEffect.ServiceEffect.CreateSingleClient);
	}
}
