/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class OpsiDataSwAuditOperation extends SwAuditOperation implements ExecutableOperation {

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public OpsiDataSwAuditOperation(AbstractSelectOperation operation) {
		super(operation);
	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		List<SWAuditClientEntry> auditList = oClient.getSwAuditList();
		for (SWAuditClientEntry swEntry : auditList) {

			String swIdent = null;
			Integer swIndex = swEntry.getSWid();
			try {
				swIdent = persistenceController.getSWident(swIndex);
				if (swIdent == null || swIndex == null || swIndex == -1) {
					Logging.info(this, "no swIdent for index " + swIndex);
					return false;
				}
			} catch (Exception ex) {
				Logging.info(this, "no swIdent for index " + swIndex);
				return false;
			}

			oClient.setCurrentSwAuditValue(persistenceController.getInstalledSoftwareInformation().get(swIdent));
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
