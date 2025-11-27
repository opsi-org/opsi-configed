/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.List;

public sealed interface AddClientMsg permits AddClientMsg.LoadInitialDataRequested, AddClientMsg.InitialDataLoaded,
		AddClientMsg.ChangeHostname, AddClientMsg.ChangeDomain, AddClientMsg.ChangeDescription,
		AddClientMsg.ChangeInventory, AddClientMsg.ChangeNotes, AddClientMsg.ChangeSystemUUID, AddClientMsg.ChangeMAC,
		AddClientMsg.ChangeIP, AddClientMsg.ChangeGroups, AddClientMsg.ChangeDepot, AddClientMsg.ChangeNetboot,
		AddClientMsg.ToggleWanSelected, AddClientMsg.ToggleShutdownInstall, AddClientMsg.CreateClient,
		AddClientMsg.ImportCSVRequested, AddClientMsg.CSVImported, AddClientMsg.OpenGroupSelectionDialog,
		AddClientMsg.ConfirmOverwriteHost, AddClientMsg.ConfirmIgnoreNetbios, AddClientMsg.CloseDialog {

	record LoadInitialDataRequested() implements AddClientMsg {
	}

	record InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots, boolean isWanActive,
			boolean defaultWanSelected, boolean defaultShutdown) implements AddClientMsg {
	}

	record ChangeHostname(String value) implements AddClientMsg {
	}

	record ChangeDomain(String value) implements AddClientMsg {
	}

	record ChangeDescription(String value) implements AddClientMsg {
	}

	record ChangeInventory(String value) implements AddClientMsg {
	}

	record ChangeNotes(String value) implements AddClientMsg {
	}

	record ChangeSystemUUID(String value) implements AddClientMsg {
	}

	record ChangeMAC(String value) implements AddClientMsg {
	}

	record ChangeIP(String value) implements AddClientMsg {
	}

	record ChangeGroups(String value) implements AddClientMsg {
	}

	record ChangeDepot(String value) implements AddClientMsg {
	}

	record ChangeNetboot(String value) implements AddClientMsg {
	}

	record ToggleWanSelected(boolean value) implements AddClientMsg {
	}

	record ToggleShutdownInstall(boolean value) implements AddClientMsg {
	}

	record CreateClient() implements AddClientMsg {
	}

	record ImportCSVRequested() implements AddClientMsg {
	}

	record CSVImported(List<List<Object>> rows) implements AddClientMsg {
	}

	record OpenGroupSelectionDialog() implements AddClientMsg {
	}

	record ConfirmOverwriteHost(boolean overwrite) implements AddClientMsg {
	}

	record ConfirmIgnoreNetbios(boolean ignore) implements AddClientMsg {
	}

	record CloseDialog() implements AddClientMsg {
	}
}
