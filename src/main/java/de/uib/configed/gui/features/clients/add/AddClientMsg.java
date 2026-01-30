/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.List;

public sealed interface AddClientMsg permits AddClientMsg.FieldChangeMsg, AddClientMsg.ActionMsg, AddClientMsg.UIMsg {

	@SuppressWarnings("java:S2972")
	sealed interface FieldChangeMsg extends AddClientMsg
			permits FieldChangeMsg.ChangeHostname, FieldChangeMsg.ChangeDomain, FieldChangeMsg.ChangeDescription,
			FieldChangeMsg.ChangeInventory, FieldChangeMsg.ChangeNotes, FieldChangeMsg.ChangeSystemUUID,
			FieldChangeMsg.ChangeMAC, FieldChangeMsg.ChangeIP, FieldChangeMsg.ChangeGroups, FieldChangeMsg.ChangeDepot,
			FieldChangeMsg.ChangeNetboot, FieldChangeMsg.ToggleWanSelected, FieldChangeMsg.ToggleShutdownInstall {

		record ChangeHostname(String value) implements FieldChangeMsg {
		}

		record ChangeDomain(String value) implements FieldChangeMsg {
		}

		record ChangeDescription(String value) implements FieldChangeMsg {
		}

		record ChangeInventory(String value) implements FieldChangeMsg {
		}

		record ChangeNotes(String value) implements FieldChangeMsg {
		}

		record ChangeSystemUUID(String value) implements FieldChangeMsg {
		}

		record ChangeMAC(String value) implements FieldChangeMsg {
		}

		record ChangeIP(String value) implements FieldChangeMsg {
		}

		record ChangeGroups(String value) implements FieldChangeMsg {
		}

		record ChangeDepot(String value) implements FieldChangeMsg {
		}

		record ChangeNetboot(String value) implements FieldChangeMsg {
		}

		record ToggleWanSelected(boolean value) implements FieldChangeMsg {
		}

		record ToggleShutdownInstall(boolean value) implements FieldChangeMsg {
		}
	}

	sealed interface ActionMsg extends AddClientMsg permits ActionMsg.LoadInitialDataRequested,
			ActionMsg.InitialDataLoaded, ActionMsg.CreateClient, ActionMsg.CSVImportRequested, ActionMsg.CSVImported {
		record LoadInitialDataRequested() implements ActionMsg {
		}

		record InitialDataLoaded(List<String> domains, List<String> depots, List<String> netboots,
				List<String> hostnames, boolean isWanActive, boolean defaultWanSelected, boolean defaultShutdown)
				implements ActionMsg {
		}

		record CreateClient() implements ActionMsg {
		}

		record CSVImportRequested() implements ActionMsg {
		}

		record CSVImported(List<List<Object>> rows, boolean includeRow) implements ActionMsg {
		}
	}

	sealed interface UIMsg extends AddClientMsg permits UIMsg.OpenGroupSelectionDialog, UIMsg.ShowErrorMessage {
		record OpenGroupSelectionDialog() implements UIMsg {
		}

		record ShowErrorMessage(String title, String message) implements UIMsg {
		}
	}
}
