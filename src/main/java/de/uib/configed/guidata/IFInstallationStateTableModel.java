package de.uib.configed.guidata;

import java.util.Map;

import javax.swing.table.TableModel;

import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.ComboBoxModeller;

public interface IFInstallationStateTableModel extends TableModel, ComboBoxModeller {
	public int getColumnIndex(String columnName);

	public void clearCollectChangedStates();

	public String getLastStateChange(int row);

	public Map<String, Map<String, Object>> getGlobalProductInfos();

	// not used public void setActionRequestWithCondition(ActionRequest ar,

	public boolean infoIfNoClientsSelected();

	public void initCollectiveChange();

	public void collectiveChangeActionRequest(String productId, ActionRequest ar);

	public void finishCollectiveChange();

}
