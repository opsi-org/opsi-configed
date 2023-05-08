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

/**
 * Copyright (C) 2008-2009, 2017 uib.de
 * 
 * @author rupert roeder
 */
public class PanelLicencesReconciliation extends MultiTablePanel {

	public PanelGenEditTable panelReconciliation;

	private int minVSize = 50;
	private int tablesMaxWidth = 1000;
	private int buttonHeight = 15;
	private int buttonWidth = 140;

	private ControlPanelLicencesReconciliation licencesReconciliationController;

	/** Creates new form panelLicencesReconciliation */
	public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController) {
		super(licencesReconciliationController);
		this.licencesReconciliationController = licencesReconciliationController;
		initComponents();
	}

	private void initComponents() {

		panelReconciliation = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), tablesMaxWidth, false, // editing
				0, true, null, true);
		panelReconciliation.setMasterFrame(Globals.frame1);
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
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE))

		);
	}

}
