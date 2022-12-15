package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * FDialogSubTable
 * Copyright:     Copyright (c) 2020-2021
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class FDialogSubTable extends FGeneralDialog {

	public FDialogSubTable(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight) {
		super(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, true);
		logging.info(this, "created ");
		additionalPaneMaxWidth = Short.MAX_VALUE;
	}

	@Override
	protected void allLayout() {
		// super.allLayout(); we define an adapted layout
		logging.info(this, "allLayout");
		allpane.setBackground(de.uib.configed.Globals.backLightBlue); // Globals.nimbusBackground);///Globals.backgroundWhite);
																		// //Globals.backLighter);//Globals.backgroundWhite);//(myHintYellow);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null)
			centerPanel = new JPanel();

		centerPanel.setBackground(Color.white);
		centerPanel.setOpaque(true);

		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.hGapSize / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE)// GroupLayout.PREFERRED_SIZE)//Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addComponent(additionalPane, 300, 300, Short.MAX_VALUE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jPanelButtonGrid, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2));

		// southPanel = new JPanel();
		southPanel.setOpaque(false);
		southPanel.setBackground(Color.white); // Color.YELLOW );
		southPanel.setOpaque(true);

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.hGapSize)
				.addComponent(centerPanel, 200, 300, Short.MAX_VALUE).addGap(Globals.hGapSize)
				.addComponent(southPanel, 300, 300, Short.MAX_VALUE).addGap(Globals.hGapSize));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize).addComponent(southPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)));

	}

}
