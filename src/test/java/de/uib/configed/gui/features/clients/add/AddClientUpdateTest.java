/**
 * Copyright (c) UIB GmbH <info@uib.de>
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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;

class AddClientUpdateTest {
	private static AddClientModel baseModel() {
		return AddClientModel.builder().hostname("").selectedDomain("").description("").inventoryNumber("").notes("")
				.systemUUID("").macAddress("").ipAddress("").groups("").selectedDepot("").selectedNetbootProduct("")
				.wanEnabled(true).wanSelected(false).shutdownInstallSelected(false)
				.domains(List.of("example.com", "corp.local")).depots(List.of("depot1", "depot2"))
				.netbootProducts(List.of("nb1", "nb2")).build();
	}

	private static List<Object> row(String hostname, String domain, String depot, String desc, String inv, String notes,
			String ip, String uuid, String mac, String netboot, String shutdown, String wan, String groups) {
		return List.of(hostname, domain, depot, mac, desc, inv, notes, uuid, ip, groups, wan, shutdown, netboot);
	}

	@Test
	void shouldTriggerLoadInitDataEffect_whenLoadInitialDataRequested() {
		AddClientModel model = baseModel();
		AddClientMsg msg = new AddClientMsg.ActionMsg.LoadInitialDataRequested();

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate.update(msg, model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.ServiceEffect.LoadInitialData.class, result.effect().get()));
	}

	@Test
	void shouldLoadInitialData_whenInitialDataLoaded() {
		AddClientModel model = baseModel();
		List<String> domains = List.of("domain1.local", "domain2.local");
		List<String> depots = List.of("newdepot1", "newdepot2");
		List<String> netboots = List.of("netboot1", "netboot2");
		List<String> hostnames = List.of("hostname1", "hostname2");
		boolean isWanActive = true;
		boolean defaultWanSelected = false;
		boolean defaultShutdown = true;
		AddClientMsg msg = new AddClientMsg.ActionMsg.InitialDataLoaded(domains, depots, netboots, hostnames,
				isWanActive, defaultWanSelected, defaultShutdown);

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
	void shouldUpdateHostname_whenChangeHostname() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeHostname("h"), model);

		assertEquals("h", result.model().getHostname());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateDomain_whenChangeDomain() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeDomain("d"), model);

		assertEquals("d", result.model().getSelectedDomain());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateDescription_whenChangeDescription() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeDescription("desc"), model);

		assertEquals("desc", result.model().getDescription());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateInventory_whenChangeInventory() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeInventory("inv"), model);

		assertEquals("inv", result.model().getInventoryNumber());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateNotes_whenChangeNotes() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeNotes("n"), model);

		assertEquals("n", result.model().getNotes());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateSystemUUID_whenChangeSystemUUID() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeSystemUUID("uuid"), model);

		assertEquals("uuid", result.model().getSystemUUID());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateMacAddress_whenChangeMAC() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeMAC("mac"), model);

		assertEquals("mac", result.model().getMacAddress());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateIpAddress_whenChangeIP() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeIP("ip"), model);

		assertEquals("ip", result.model().getIpAddress());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateGroups_whenChangeGroups() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeGroups("g1; g2"), model);

		assertEquals("g1; g2", result.model().getGroups());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateDepot_whenChangeDepot() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeDepot("d1"), model);

		assertEquals("d1", result.model().getSelectedDepot());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateNetboot_whenChangeNetboot() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ChangeNetboot("nb1"), model);

		assertEquals("nb1", result.model().getSelectedNetbootProduct());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateWanSelected_whenToggleWanSelected() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ToggleWanSelected(true), model);

		assertTrue(result.model().isWanSelected());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldUpdateShutdownInstall_whenToggleShutdownInstall() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.FieldChangeMsg.ToggleShutdownInstall(true), model);

		assertTrue(result.model().isShutdownInstallSelected());
		assertFalse(result.effect().isPresent());
	}

	@Test
	void shouldTriggerOpenCSVImportDailogEffect_whenCSVImportRequested() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.ActionMsg.CSVImportRequested(), model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.OpenCSVImportDialog.class, result.effect().get()));
	}

	@Test
	void shouldTriggerOpenGroupSelectionDialogEffect_whenOpenGroupSelectionDialog() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.UIMsg.OpenGroupSelectionDialog(), model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.OpenGroupSelectionDialog.class, result.effect().get()));
	}

	@Test
	void shouldTriggerShowErrorMessageEffect_whenShowErrorMessage() {
		AddClientModel model = baseModel();
		String expectedTitle = "title";
		String expectedMessage = "message";
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.UIMsg.ShowErrorMessage(expectedTitle, expectedMessage), model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowErrorMessage.class, result.effect().get()),
				() -> assertEquals(expectedTitle,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect().get()).title()),
				() -> assertEquals(expectedMessage,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect().get()).message()));
	}

	@Test
	void shouldTriggerCreateClients_whenCSVImported() {
		AddClientModel model = baseModel();
		List<List<Object>> rows = new ArrayList<>();
		rows.add(
				row("h2", "dom", "d", "d", "i", "notes", "ip", "uuid", "mac", "netboot", "true", "false", "grp1;grp2"));
		rows.add(
				row("h3", "dom", "d", "d", "i", "notes", "ip", "uuid", "mac", "netboot", "true", "false", "grp1;grp2"));

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.ActionMsg.CSVImported(rows, true), model);

		assertAll(() -> assertEquals(0, result.model().getAcceptedRows().size()),
				() -> assertEquals(0, result.model().getRowsToImport().size()),
				() -> assertEquals(0, result.model().getPendingSingleRow().size()));
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.ServiceEffect.CreateClients.class, result.effect().get()));
	}

	@Test
	void shouldTriggerCreateClients_whenCreateClient() {
		AddClientModel model = baseModel().withHostname("host").withSelectedDomain("dom").withGroups("g1; g2")
				.withSelectedDepot("depot1").withSelectedNetbootProduct("nb1").withShutdownInstallSelected(true)
				.withWanSelected(true).withHostnames(List.of());

		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.ActionMsg.CreateClient(), model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.ServiceEffect.CreateClients.class, result.effect().get()));
	}

	@Test
	void shouldTriggerCloseDialogEffect_whenCloseDialog() {
		AddClientModel model = baseModel();
		UpdateResult<AddClientModel, AddClientEffect> result = AddClientUpdate
				.update(new AddClientMsg.ActionMsg.CloseDialog(), model);

		assertSame(model, result.model());
		assertAll(() -> assertTrue(result.effect().isPresent()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.CloseDialog.class, result.effect().get()));
	}
}
