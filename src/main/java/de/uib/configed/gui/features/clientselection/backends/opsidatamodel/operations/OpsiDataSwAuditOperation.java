/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.clientselection.backends.opsidatamodel.operations;

import java.util.List;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.ExecutableOperation;
import de.uib.configed.gui.features.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.gui.features.clientselection.operations.SwAuditOperation;
import de.uib.configed.gui.type.SWAuditClientEntry;
import de.uib.configed.share.logging.Logging;

public class OpsiDataSwAuditOperation extends SwAuditOperation implements ExecutableOperation {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public OpsiDataSwAuditOperation(AbstractSelectOperation operation) {
		super(operation);
	}

	@Override
	public boolean doesMatch(OpsiDataClient client) {
		List<SWAuditClientEntry> auditList = client.getSwAuditList();
		for (SWAuditClientEntry swEntry : auditList) {
			String swIdent = swEntry.getSWIdent();
			if (!persistenceController.getDataServices().software.swEntryExists(swEntry)) {
				Logging.info(this, "no swIdent ", swIdent);
				return false;
			}

			client.setCurrentSwAuditValue(
					persistenceController.getDataServices().software.getInstalledSoftwareInformationPD().get(swIdent));
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
