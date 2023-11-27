/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class NavigationPanel extends JPanel {
	private PanelGenEditTable associatedPanel;

	public NavigationPanel(PanelGenEditTable associatedPanel) {
		this.associatedPanel = associatedPanel;

		initComponents();
	}

	private void initComponents() {
		Logging.info(this, "initComponents");

		Dimension navButtonDimension = new Dimension(30, Globals.BUTTON_HEIGHT - 6);

		JButton nextButton = new JButton(Utils.createImageIcon("images/arrows/arrow_red_16x16-right.png", ""));
		nextButton.setToolTipText(Configed.getResourceValue("NavigationPanel.nextEntryTooltip"));
		nextButton.setPreferredSize(navButtonDimension);
		nextButton.addActionListener(event -> associatedPanel.advanceCursor(+1));

		JButton previousButton = new JButton(Utils.createImageIcon("images/arrows/arrow_red_16x16-left.png", ""));
		previousButton.setToolTipText(Configed.getResourceValue("NavigationPanel.previousEntryTooltip"));
		previousButton.setPreferredSize(navButtonDimension);
		previousButton.addActionListener(event -> associatedPanel.advanceCursor(-1));

		JButton firstButton = new JButton(Utils.createImageIcon("images/arrows/arrow_red_16x16-doubleleft.png", ""));
		firstButton.setToolTipText(Configed.getResourceValue("NavigationPanel.firstEntryTooltip"));
		firstButton.setPreferredSize(navButtonDimension);
		firstButton.addActionListener(event -> associatedPanel.setCursorToFirstRow());

		JButton lastButton = new JButton(Utils.createImageIcon("images/arrows/arrow_red_16x16-doubleright.png", ""));
		lastButton.setToolTipText(Configed.getResourceValue("NavigationPanel.lastEntryTooltip"));
		lastButton.setPreferredSize(navButtonDimension);
		lastButton.addActionListener(event -> associatedPanel.setCursorToLastRow());

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup()
				.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}
}
