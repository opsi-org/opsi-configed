package de.uib.configed.clientselection.backends.opsidatamodel.operations;

import java.util.List;

import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.Client;
import de.uib.configed.clientselection.ExecutableOperation;
import de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataClient;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.utilities.logging.Logging;

public class OpsiDataSwAuditOperation extends SwAuditOperation implements ExecutableOperation {

	private de.uib.opsidatamodel.AbstractPersistenceController controller;

	public OpsiDataSwAuditOperation(AbstractSelectOperation operation) {
		super(operation);

		controller = de.uib.opsidatamodel.PersistenceControllerFactory.getPersistenceController();
	}

	@Override
	public boolean doesMatch(Client client) {
		OpsiDataClient oClient = (OpsiDataClient) client;
		List<SWAuditClientEntry> auditList = oClient.getSwAuditList();
		for (SWAuditClientEntry swEntry : auditList) {

			String swIdent = null;
			Integer swIndex = swEntry.getSWid();
			try {
				swIdent = controller.getSWident(swIndex);
				if (swIdent == null || swIndex == null || swIndex == -1) {
					Logging.info(this, "no swIdent for index " + swIndex);
					return false;
				}
			} catch (Exception ex) {
				Logging.info(this, "no swIdent for index " + swIndex);
				return false;
			}

			oClient.setCurrentSwAuditValue(controller.getInstalledSoftwareInformation().get(swIdent));
			if (((ExecutableOperation) getChildOperations().get(0)).doesMatch(client)) {
				return true;
			}
		}
		return false;
	}
}
