/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.guidata;

import java.util.Map;
import java.util.TreeSet;

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

	void updateTable(String clientId, TreeSet<String> productId, String[] attributes);

	void updateTable(String clientId, String[] attributes);
}
