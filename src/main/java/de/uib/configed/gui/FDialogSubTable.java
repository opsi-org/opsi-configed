/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class FDialogSubTable extends FGeneralDialog {
	public FDialogSubTable(JFrame owner, String title, boolean modal, String[] buttonList, int lastButtonNo,
			int preferredWidth, int preferredHeight) {
		super(owner, title, modal, buttonList, lastButtonNo, preferredWidth, preferredHeight, true);
		Logging.info(this.getClass(), "created ");
		additionalPaneMaxWidth = Short.MAX_VALUE;
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(Alignment.LEADING).addGroup(southLayout
				.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(additionalPane, 300, 300, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(centerPanel, 200, 300, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)
				.addComponent(southPanel, 300, 300, Short.MAX_VALUE).addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE).addComponent(southPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)));
	}
}
