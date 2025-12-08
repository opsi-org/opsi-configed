/**
 * Copyright (c) UIB GmbH <info@uib.de>
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

	@SuppressWarnings({ "java:S103", "java:S1541" })
	static UpdateResult<AddClientModel, AddClientEffect> update(AddClientMsg msg, AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.FieldChangeMsg m -> handleFieldChangeMsg(m, model);
		case AddClientMsg.ActionMsg m -> handleActionMsg(m, model);
		case AddClientMsg.UIMsg m -> handleUIMsg(m, model);
		};
	}

	@SuppressWarnings("java:S1541")
	private static UpdateResult<AddClientModel, AddClientEffect> handleFieldChangeMsg(AddClientMsg.FieldChangeMsg msg,
			AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.FieldChangeMsg.ChangeHostname(String v) -> UpdateResult.noEffect(model.withHostname(v));
		case AddClientMsg.FieldChangeMsg.ChangeDomain(String v) -> UpdateResult.noEffect(model.withSelectedDomain(v));
		case AddClientMsg.FieldChangeMsg.ChangeDescription(String v) -> UpdateResult.noEffect(model.withDescription(v));
		case AddClientMsg.FieldChangeMsg.ChangeInventory(String v) -> UpdateResult
				.noEffect(model.withInventoryNumber(v));
		case AddClientMsg.FieldChangeMsg.ChangeNotes(String v) -> UpdateResult.noEffect(model.withNotes(v));
		case AddClientMsg.FieldChangeMsg.ChangeSystemUUID(String v) -> UpdateResult.noEffect(model.withSystemUUID(v));
		case AddClientMsg.FieldChangeMsg.ChangeMAC(String v) -> UpdateResult.noEffect(model.withMacAddress(v));
		case AddClientMsg.FieldChangeMsg.ChangeIP(String v) -> UpdateResult.noEffect(model.withIpAddress(v));
		case AddClientMsg.FieldChangeMsg.ChangeGroups(String v) -> UpdateResult.noEffect(model.withGroups(v));
		case AddClientMsg.FieldChangeMsg.ChangeDepot(String v) -> UpdateResult.noEffect(model.withSelectedDepot(v));
		case AddClientMsg.FieldChangeMsg.ChangeNetboot(String v) -> UpdateResult
				.noEffect(model.withSelectedNetbootProduct(v));
		case AddClientMsg.FieldChangeMsg.ToggleWanSelected(boolean b) -> UpdateResult
				.noEffect(model.withWanSelected(b));
		case AddClientMsg.FieldChangeMsg.ToggleShutdownInstall(boolean b) -> UpdateResult
				.noEffect(model.withShutdownInstallSelected(b));
		};
	}

	@SuppressWarnings("java:S103")
	private static UpdateResult<AddClientModel, AddClientEffect> handleActionMsg(AddClientMsg.ActionMsg msg,
			AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.ActionMsg.LoadInitialDataRequested m -> UpdateResult.withEffect(model,
				new AddClientEffect.ServiceEffect.LoadInitialData());
		case AddClientMsg.ActionMsg.InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots, List<String> hostnames, boolean isWanActive, boolean defaultWanSelected, boolean defaultShutdown) -> UpdateResult
				.noEffect(model.toBuilder().domains(domains).depots(depots).netbootProducts(netboots)
						.hostnames(hostnames).wanEnabled(isWanActive).wanSelected(isWanActive && defaultWanSelected)
						.shutdownInstallSelected(defaultShutdown)
						.selectedDomain(domains.isEmpty() ? "" : domains.get(0)).build());
		case AddClientMsg.ActionMsg.CSVImportRequested() -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.OpenCSVImportDialog());
		case AddClientMsg.ActionMsg.CSVImported(List<List<Object>> rows, boolean includeRow) -> handleCSVImportedMsg(
				model, rows, includeRow);
		case AddClientMsg.ActionMsg.CreateClient() -> handleCreateClientMsg(model);
		case AddClientMsg.ActionMsg.CloseDialog() -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.CloseDialog());
		};
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleUIMsg(AddClientMsg.UIMsg msg,
			AddClientModel model) {
		return switch (msg) {
		case AddClientMsg.UIMsg.OpenGroupSelectionDialog() -> handleOpenGroupSelectionDialogMsg(model);

		case AddClientMsg.UIMsg.ShowErrorMessage(String title, String message) -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.ShowErrorMessage(title, message));
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
		row.add(model.getMacAddress());
		row.add(model.getDescription());
		row.add(model.getInventoryNumber());
		row.add(model.getNotes());
		row.add(model.getSystemUUID());
		row.add(model.getIpAddress());
		row.add(String.join(",", parseGroups(model.getGroups())));
		row.add(Boolean.toString(model.isWanSelected()));
		row.add(Boolean.toString(model.isShutdownInstallSelected()));
		row.add("");
		row.add(model.getSelectedNetbootProduct());

		BatchProcessor processor = new BatchProcessor(VALIDATORS);
		return processor.processSingleRow(model, row);
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleOpenGroupSelectionDialogMsg(
			AddClientModel model) {
		List<String> preselected = Arrays.asList(parseGroups(model.getGroups()));
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
