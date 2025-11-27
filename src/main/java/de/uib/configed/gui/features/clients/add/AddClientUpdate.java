/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.Configed;

final class AddClientUpdate {
	private AddClientUpdate() {
	}

	@SuppressWarnings("java:S103")
	static UpdateResult<AddClientModel, AddClientEffect> update(AddClientMsg msg, AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.LoadInitialDataRequested m -> UpdateResult.withEffect(model,
				new AddClientEffect.ServiceEffect.LoadInitialData());
		case AddClientMsg.InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots, boolean isWanActive, boolean defaultWanSelected, boolean defaultShutdown) -> UpdateResult
				.noEffect(model.toBuilder().domains(domains).depots(depots).netbootProducts(netboots)
						.wanEnabled(isWanActive).wanSelected(isWanActive && defaultWanSelected)
						.shutdownInstallSelected(defaultShutdown)
						.selectedDomain(domains.isEmpty() ? "" : domains.get(0)).build());
		case AddClientMsg.ChangeHostname(String v) -> UpdateResult.noEffect(model.withHostname(v));
		case AddClientMsg.ChangeDomain(String v) -> UpdateResult.noEffect(model.withSelectedDomain(v));
		case AddClientMsg.ChangeDescription(String v) -> UpdateResult.noEffect(model.withDescription(v));
		case AddClientMsg.ChangeInventory(String v) -> UpdateResult.noEffect(model.withInventoryNumber(v));
		case AddClientMsg.ChangeNotes(String v) -> UpdateResult.noEffect(model.withNotes(v));
		case AddClientMsg.ChangeSystemUUID(String v) -> UpdateResult.noEffect(model.withSystemUUID(v));
		case AddClientMsg.ChangeMAC(String v) -> UpdateResult.noEffect(model.withMacAddress(v));
		case AddClientMsg.ChangeIP(String v) -> UpdateResult.noEffect(model.withIpAddress(v));
		case AddClientMsg.ChangeGroups(String v) -> UpdateResult.noEffect(model.withGroups(v));
		case AddClientMsg.ChangeDepot(String v) -> UpdateResult.noEffect(model.withSelectedDepot(v));
		case AddClientMsg.ChangeNetboot(String v) -> UpdateResult.noEffect(model.withSelectedNetbootProduct(v));
		case AddClientMsg.ToggleWanSelected(boolean b) -> UpdateResult.noEffect(model.withWanSelected(b));
		case AddClientMsg.ToggleShutdownInstall(boolean b) -> UpdateResult
				.noEffect(model.withShutdownInstallSelected(b));
		case AddClientMsg.OpenGroupSelectionDialog() -> handleOpenGroupSelectionDialogMsg(model);
		case AddClientMsg.ImportCSVRequested() -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.OpenCsvImportDialog());
		case AddClientMsg.CSVImported(List<List<Object>> rows) -> handleCSVImportedMsg(model, rows);
		case AddClientMsg.CreateClient() -> handleCreateClientMsg(model);
		case AddClientMsg.ConfirmOverwriteHost(boolean overwrite) -> UpdateResult.noEffect(model);
		case AddClientMsg.ConfirmIgnoreNetbios(boolean ignore) -> UpdateResult.noEffect(model);
		case AddClientMsg.CloseDialog() -> UpdateResult.withEffect(model, new AddClientEffect.UIEffect.CloseDialog());
		};
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleCSVImportedMsg(AddClientModel model,
			List<List<Object>> rows) {
		for (List<Object> client : rows) {
			if (!AddClientValidator.isBoolean((String) client.get(10))
					|| !AddClientValidator.isBoolean((String) client.get(11))) {
				return UpdateResult.withEffect(model,
						new AddClientEffect.UIEffect.ShowErrorMessage(
								Configed.getResourceValue("NewClientDialog.nonBooleanValue.title"),
								Configed.getResourceValue("NewClientDialog.nonBooleanValue.message")));
			}
		}
		return UpdateResult.withEffect(model, new AddClientEffect.ServiceEffect.CreateMultipleClients(rows));
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleCreateClientMsg(AddClientModel model) {
		return UpdateResult.withEffect(model,
				new AddClientEffect.ServiceEffect.CreateSingleClient(model.getHostname(), model.getSelectedDomain(),
						model.getSelectedDepot(), model.getDescription(), model.getInventoryNumber(), model.getNotes(),
						model.getIpAddress(), model.getSystemUUID(), model.getMacAddress(),
						model.isShutdownInstallSelected(), model.isWanSelected(), parseGroups(model.getGroups()),
						model.getSelectedNetbootProduct()));
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleOpenGroupSelectionDialogMsg(
			AddClientModel model) {
		List<String> preselected = Arrays.asList(model.getGroups().replace("; ", ";").split(";"));
		return UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.OpenGroupSelectionDialog(new ArrayList<>(), preselected));
	}

	private static String[] parseGroups(String groupsText) {
		if (groupsText == null || groupsText.isEmpty()) {
			return new String[] {};
		}
		return groupsText.replace("; ", ";").split(";");
	}
}
