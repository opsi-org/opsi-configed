/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class GlobalSoftwareInfoDialog {
	private PanelGenEdit panelGlobalSoftware;

	private List<String> columnNames;

	private JOptionPane optionPane;
	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools myController;

	public GlobalSoftwareInfoDialog(ControlPanelAssignToLPools myController) {
		this.myController = myController;

		panelGlobalSoftware = new PanelGenEdit("", false, 2);

		JLabel infoLabel = new JLabel(Configed.getResourceValue("FGlobalSoftwareInfo.info"));

		initDataStructure();

		JPanel panel = new JPanel(new MigLayout("fillx, insets 0, gapy " + Globals.GAP_SIZE, "[grow, fill]", "[]"));
		panel.add(panelGlobalSoftware, "growx, h 250!, wrap");
		panel.add(infoLabel);

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

		panelGlobalSoftware.getGenEditTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
				Logging.info(this, "doAction2 model row ",
						panelGlobalSoftware.getGenEditTable().convertRowIndexToModel(row));
				panelGlobalSoftware.getTableModel()
						.deleteRow(panelGlobalSoftware.getGenEditTable().convertRowIndexToModel(row));
			}
		}
	}

	public PanelGenEdit getPanelGlobalSoftware() {
		return panelGlobalSoftware;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}
}
