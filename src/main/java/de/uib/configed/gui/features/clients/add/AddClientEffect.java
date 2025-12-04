/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.util.List;

public sealed interface AddClientEffect permits AddClientEffect.UIEffect, AddClientEffect.ServiceEffect {
	sealed interface UIEffect extends AddClientEffect permits UIEffect.ShowOverwriteHostDialog,
			UIEffect.ShowOverwriteDepotDialog, UIEffect.ShowNetbiosConfirmDialog, UIEffect.OpenCSVImportDialog,
			UIEffect.OpenGroupSelectionDialog, UIEffect.ShowErrorMessage, UIEffect.CloseDialog {
		record ShowOverwriteHostDialog(String opsiHostKey) implements UIEffect {
		}

		record ShowOverwriteDepotDialog(String opsiHostKey) implements UIEffect {
		}

		record ShowNetbiosConfirmDialog() implements UIEffect {
		}

		record OpenCSVImportDialog() implements UIEffect {
		}

		record OpenGroupSelectionDialog(List<String> availableGroups, List<String> preselected) implements UIEffect {
		}

		record ShowErrorMessage(String title, String message) implements UIEffect {
		}

		record CloseDialog() implements UIEffect {
		}
	}

	sealed interface ServiceEffect extends AddClientEffect
			permits ServiceEffect.LoadInitialData, ServiceEffect.CreateClients {
		record LoadInitialData() implements ServiceEffect {
		}

		record CreateClients(List<List<Object>> rows) implements ServiceEffect {
		}
	}
}
