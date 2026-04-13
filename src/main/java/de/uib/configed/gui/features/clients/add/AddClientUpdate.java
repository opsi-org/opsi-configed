/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uib.configed.gui.AbstractTeaComponent.UpdateResult;
import de.uib.configed.gui.features.clients.add.AddClientValidator.BooleanValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.HostCollisionValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.HostnameDomainValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.NetbiosValidator;
import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;
import de.uib.configed.gui.type.HostInfo;

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
		case AddClientMsg.FieldChangeMsg.ChangeGroups(List<String> v) -> UpdateResult.noEffect(model.withGroups(v));
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
		case AddClientMsg.ActionMsg.LoadInitialDataRequested _ -> UpdateResult.withEffect(model,
				new AddClientEffect.ServiceEffect.LoadInitialData());
		case AddClientMsg.ActionMsg.InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots, List<String> hostnames, boolean isWanActive, boolean defaultWanSelected, boolean defaultShutdown) -> UpdateResult
				.noEffect(model.toBuilder().domains(domains).depots(depots).netbootProducts(netboots)
						.hostnames(hostnames).wanEnabled(isWanActive).wanSelected(isWanActive && defaultWanSelected)
						.shutdownInstallSelected(defaultShutdown)
						.selectedDomain(domains.isEmpty() ? "" : domains.get(0)).build());
		case AddClientMsg.ActionMsg.CSVImportRequested() -> UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.OpenCSVImportDialog());
		case AddClientMsg.ActionMsg.CSVImported(List<Map<String, Object>> rows, boolean includeRow) -> handleCSVImportedMsg(
				model, rows, includeRow);
		case AddClientMsg.ActionMsg.CreateClient() -> handleCreateClientMsg(model);
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
			List<Map<String, Object>> rows, boolean includeRow) {
		if (rows != null && !rows.isEmpty()) {
			model = model.withRowsToImport(new ArrayList<>(rows));
		}

		if (includeRow && !model.getPendingSingleRow().isEmpty()) {
			var accepted = new ArrayList<>(model.getAcceptedRows());
			accepted.add(model.getPendingSingleRow());
			model = model.withAcceptedRows(accepted).withPendingSingleRow(new HashMap<>());
		}

		BatchProcessor processor = new BatchProcessor(VALIDATORS);
		return processor.process(model);
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleCreateClientMsg(AddClientModel model) {
		Map<String, Object> row = new HashMap<>();
		row.put(HostInfo.HOSTNAME_KEY, model.getHostname());
		row.put(HostInfo.CSV_DOMAIN_KEY, model.getSelectedDomain());
		row.put(HostInfo.DEPOT_OF_CLIENT_KEY, model.getSelectedDepot());
		row.put(HostInfo.CLIENT_MAC_ADDRESS_KEY, model.getMacAddress());
		row.put(HostInfo.CLIENT_DESCRIPTION_KEY, model.getDescription());
		row.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, model.getInventoryNumber());
		row.put(HostInfo.CLIENT_NOTES_KEY, model.getNotes());
		row.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, model.getSystemUUID());
		row.put(HostInfo.CLIENT_IP_ADDRESS_KEY, model.getIpAddress());
		row.put(HostInfo.CSV_GROUPS_KEY, model.getGroups());
		row.put(HostInfo.CLIENT_WAN_CONFIG_KEY, Boolean.toString(model.isWanSelected()));
		row.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, Boolean.toString(model.isShutdownInstallSelected()));
		row.put(HostInfo.HOST_KEY_KEY, "");
		row.put(HostInfo.CSV_NETBOOT_PRODUCT_KEY, model.getSelectedNetbootProduct());

		BatchProcessor processor = new BatchProcessor(VALIDATORS);
		return processor.processSingleRow(model, row);
	}

	private static UpdateResult<AddClientModel, AddClientEffect> handleOpenGroupSelectionDialogMsg(
			AddClientModel model) {
		return UpdateResult.withEffect(model,
				new AddClientEffect.UIEffect.OpenGroupSelectionDialog(new ArrayList<>(), model.getGroups()));
	}
}
