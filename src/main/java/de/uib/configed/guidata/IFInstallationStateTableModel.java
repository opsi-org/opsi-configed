package de.uib.configed.guidata;

import java.util.Map;

import javax.swing.table.TableModel;

import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.ComboBoxModeller;

public interface IFInstallationStateTableModel extends TableModel, ComboBoxModeller {
	int getColumnIndex(String columnName);

	void clearCollectChangedStates();

	String getLastStateChange(int row);

	Map<String, Map<String, Object>> getGlobalProductInfos();

	boolean infoIfNoClientsSelected();

	void initCollectiveChange();

	void collectiveChangeActionRequest(String productId, ActionRequest ar);

	void finishCollectiveChange();

	void updateTable(String clientId, String productId, Map<String, String> stateAndAction);
}
