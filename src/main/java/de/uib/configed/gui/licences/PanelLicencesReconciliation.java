/*
 * PanelLicencesReconciliation.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.ControlPanelLicencesReconciliation;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * Copyright (C) 2008-2009, 2017 uib.de
 * 
 * @author rupert roeder
 */
public class PanelLicencesReconciliation extends MultiTablePanel {

	public PanelGenEditTable panelReconciliation;

	protected int minVSize = 50;
	protected int tablesMaxWidth = 1000;
	protected int buttonHeight = 15;
	protected int buttonWidth = 140;

	protected de.uib.configed.ControlPanelLicencesReconciliation licencesReconciliationController;

	/** Creates new form panelLicencesReconciliation */
	public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController) {
		super(licencesReconciliationController);
		this.licencesReconciliationController = licencesReconciliationController;
		initComponents();
	}

	private void initComponents() {

		panelReconciliation = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), tablesMaxWidth, false, // editing
				0, // popupPosition
				true// switchLineColors
				, null // default popups
				, true // searchpane
		);
		panelReconciliation.setMasterFrame(Globals.frame1);
		panelReconciliation.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelReconciliation.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panelReconciliation.setFiltering(true);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
								// for testing purposes:
								// .addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING,
								// javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								// Short.MAX_VALUE)
								.addComponent(panelReconciliation, javax.swing.GroupLayout.Alignment.LEADING,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								// .addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE,
								// javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
								.addComponent(panelReconciliation, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE))

		);
	}

}
