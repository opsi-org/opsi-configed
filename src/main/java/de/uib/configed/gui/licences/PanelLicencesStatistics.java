/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelLicencesStatistics.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.GroupLayout;
import javax.swing.ListSelectionModel;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelLicencesStatistics extends MultiTablePanel {

	public PanelGenEditTable panelStatistics;

	private int minVSize = 50;

	/** Creates new form panelLicencesStatistics */
	public PanelLicencesStatistics(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {

		panelStatistics = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleStatistics"), 1000, false, // editing
				0, true, null, true);
		panelStatistics.setMasterFrame(Globals.frame1);
		panelStatistics.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// supply implementation of SearchTargetModelFromTable.setFiltered
		panelStatistics.showFilterIcon(true);
		panelStatistics.setFiltering(true);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								// for testing purposes:

								// Short.MAX_VALUE)
								.addComponent(panelStatistics, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()

						.addComponent(panelStatistics, minVSize, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE))

		);
	}
}
