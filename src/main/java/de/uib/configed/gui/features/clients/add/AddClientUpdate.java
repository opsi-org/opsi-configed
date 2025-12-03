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
import de.uib.configed.gui.features.clients.add.AddClientValidator.BooleanValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.HostCollisionValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.HostnameDomainValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.NetbiosValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;

final class AddClientUpdate {
	private static final List<RowValidation> VALIDATORS = List.of(new BooleanValidator(), new HostnameDomainValidator(),
			new HostCollisionValidator(), new NetbiosValidator());

	private AddClientUpdate() {
	}

	@SuppressWarnings("java:S103")
	static UpdateResult<AddClientModel, AddClientEffect> update(AddClientMsg msg, AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.LoadInitialDataRequested m -> UpdateResult.withEffect(model,
				new AddClientEffect.ServiceEffect.LoadInitialData());
		case AddClientMsg.InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots, List<String> hostnames, boolean isWanActive, boolean defaultWanSelected, boolean defaultShutdown) -> UpdateResult
				.noEffect(model.toBuilder().domains(domains).depots(depots).netbootProducts(netboots)
						.hostnames(hostnames).wanEnabled(isWanActive).wanSelected(isWanActive && defaultWanSelected)
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
		case AddClientMsg.CSVImported(List<List<Object>> rows, boolean includeRow) -> handleCSVImportedMsg(model, rows,
				includeRow);
		case AddClientMsg.CreateClient() -> handleCreateClientMsg(model);
		case AddClientMsg.ShowError(String title, String message) -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.ShowErrorMessage(title, message));
		case AddClientMsg.CloseDialog() -> UpdateResult.withEffect(model, new AddClientEffect.UIEffect.CloseDialog());
		};
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleCSVImportedMsg(AddClientModel model,
			List<List<Object>> rows, boolean includeRow) {
		if (rows != null && !rows.isEmpty()) {
			model = model.withRowsToImport(new ArrayList<>(rows));
		}

		if (includeRow && !model.getPendingSingleRow().isEmpty()) {
			var accepted = new ArrayList<>(model.getAcceptedRows());
			accepted.add(model.getPendingSingleRow());
			model = model.withAcceptedRows(accepted).withPendingSingleRow(new ArrayList<>());
		}

		BatchProcessor processor = new BatchProcessor(VALIDATORS);

		return processor.process(model);
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleCreateClientMsg(AddClientModel model) {
		List<Object> row = new ArrayList<>();
		row.add(model.getHostname());
		row.add(model.getSelectedDomain());
		row.add(model.getSelectedDepot());
		row.add(model.getDescription());
		row.add(model.getInventoryNumber());
		row.add(model.getNotes());
		row.add(model.getIpAddress());
		row.add(model.getSystemUUID());
		row.add(model.getMacAddress());
		row.add(model.getSelectedNetbootProduct());
		row.add(Boolean.toString(model.isShutdownInstallSelected()));
		row.add(Boolean.toString(model.isWanSelected()));
		row.add(String.join(";", parseGroups(model.getGroups())));

		BatchProcessor processor = new BatchProcessor(VALIDATORS);
		return processor.processSingleRow(model, row);
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
