/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicencesReconciliation.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ControlPanelLicencesReconciliation;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class PanelLicencesReconciliation extends MultiTablePanel {
	private PanelGenEditTable panelReconciliation;

	private int minVSize = 50;
	private int tablesMaxWidth = 1000;

	/** Creates new form panelLicencesReconciliation */
	public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController) {
		super(licencesReconciliationController);
		initComponents();
	}

	private void initComponents() {
		panelReconciliation = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), tablesMaxWidth, false, // editing
				0, true, null, true);
		panelReconciliation.setMasterFrame(Utils.getMasterFrame());
		panelReconciliation.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelReconciliation.showFilterIcon(true);
		panelReconciliation.setFiltering(true);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup().addContainerGap()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										// for testing purposes:

										// Short.MAX_VALUE)
										.addComponent(panelReconciliation, GroupLayout.Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()

						.addComponent(panelReconciliation, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE))

		);
	}

	public PanelGenEditTable getPanelReconciliation() {
		return panelReconciliation;
	}
}
