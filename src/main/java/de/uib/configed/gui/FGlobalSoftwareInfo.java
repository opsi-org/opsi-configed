/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.PanelGenEditTable;

public class FGlobalSoftwareInfo {
	private PanelGenEditTable panelGlobalSoftware;

	private List<String> columnNames;

	private JOptionPane optionPane;
	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools myController;

	public FGlobalSoftwareInfo(ControlPanelAssignToLPools myController) {
		this.myController = myController;

		panelGlobalSoftware = new PanelGenEditTable("", false, 2);

		JLabel infoLabel = new JLabel(Configed.getResourceValue("FGlobalSoftwareInfo.info"));

		initDataStructure();

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(panelGlobalSoftware, 250, 250, 250)
				.addGap(Globals.GAP_SIZE).addComponent(infoLabel));
		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(panelGlobalSoftware).addComponent(infoLabel));

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION, null,
				new String[] { Configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove"),
						Configed.getResourceValue("buttonClose") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("FGlobalSoftwareInfo.title"));
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);

		Object selectedValue = optionPane.getValue();
		if (selectedValue != null
				&& selectedValue.equals(Configed.getResourceValue("FGlobalSoftwareInfo.buttonRemove"))) {
			removeAction();
		}
	}

	private void initDataStructure() {
		columnNames = new ArrayList<>();
		columnNames.add("ID");
		for (String key : SWAuditEntry.KEYS_FOR_IDENT) {
			columnNames.add(key);
		}

		panelGlobalSoftware.getJTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void setTableModel(GenTableModel model) {
		panelGlobalSoftware.setTableModel(model);
	}

	public void removeAction() {
		Logging.debug(this, "doAction2");

		Logging.info(this, "removeAssociations for ", " licensePool ", myController.getSelectedLicensePool(),
				" selected SW keys ", panelGlobalSoftware.getSelectedKeys());

		boolean success = persistenceController.getSoftwareDataService()
				.removeAssociations(myController.getSelectedLicensePool(), panelGlobalSoftware.getSelectedKeys());

		if (success) {
			for (String key : panelGlobalSoftware.getSelectedKeys()) {
				int row = panelGlobalSoftware.findViewRowFromValue(key, 0);
				Logging.info(this, "doAction2 key, ", key, ", row ", row);
				Logging.info(this, "doAction2 model row ", panelGlobalSoftware.getJTable().convertRowIndexToModel(row));
				panelGlobalSoftware.getTableModel()
						.deleteRow(panelGlobalSoftware.getJTable().convertRowIndexToModel(row));
			}
		}
	}

	public PanelGenEditTable getPanelGlobalSoftware() {
		return panelGlobalSoftware;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}
}
