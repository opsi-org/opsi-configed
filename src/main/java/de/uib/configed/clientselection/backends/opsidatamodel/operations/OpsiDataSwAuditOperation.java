/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

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
			String swIdent = null;
			Integer swIndex = swEntry.getSWid();

			swIdent = persistenceController.getSoftwareDataService().getSWident(swIndex);
			if (swIdent == null || swIndex == null || swIndex == -1) {
				Logging.info(this, "no swIdent for index " + swIndex);
				return false;
			}

			client.setCurrentSwAuditValue(
					persistenceController.getSoftwareDataService().getInstalledSoftwareInformationPD().get(swIdent));
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
