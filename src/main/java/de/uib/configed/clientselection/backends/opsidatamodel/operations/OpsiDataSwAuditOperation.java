package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;

import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.SelectOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.utilities.logging.logging;

public class OpsiDataSwAuditOperation extends SwAuditOperation implements ExecutableOperation {

	private de.uib.opsidatamodel.PersistenceController controller;

	public OpsiDataSwAuditOperation(SelectOperation operation) {
		super(operation);

		controller = de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController();
	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		List<SWAuditClientEntry> auditList = oClient.getSwAuditList();
		for (SWAuditClientEntry swEntry : auditList) {
			// logging.info(this, "swIndex " +swIndex);
			// logging.info(this, "swIdent " +controller.getSWident(swIndex));
			String swIdent = null;
			Integer swIndex = swEntry.getSWid();
			try {
				swIdent = controller.getSWident(swIndex);
				if (swIdent == null || swIndex == null || swIndex == -1) {
					logging.info(this, "no swIdent for index " + swIndex);
					return false;
				}
			} catch (Exception ex) {
				logging.info(this, "no swIdent for index " + swIndex);
				return false;
			}

			oClient.setCurrentSwAuditValue(controller.getInstalledSoftwareInformation().get(swIdent));
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client))
				return true;
		}
		return false;
	}
}
