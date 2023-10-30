/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JPanel;

import de.uib.configed.Globals;

public class PanelLinedComponents extends JPanel {
	private JComponent[] components;

	private int myHeight;

	public PanelLinedComponents(JComponent[] components) {
		setComponents(components);
	}

	public PanelLinedComponents() {
		super();
	}

	public void setComponents(JComponent[] components, int height) {
		this.components = components;
		myHeight = height;
		defineLayout();
	}

	private void setComponents(JComponent[] components) {
		setComponents(components, Globals.LINE_HEIGHT);
	}

	private void defineLayout() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGap(Globals.GAP_SIZE);
		if (components != null) {
			for (JComponent component : components) {
				hGroup.addComponent(component, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
				hGroup.addGap(Globals.GAP_SIZE);
			}
		}
		layout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGap(0, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2);

		GroupLayout.ParallelGroup vGroup1 = layout.createParallelGroup(Alignment.CENTER);

		if (components != null) {
			for (JComponent component : components) {
				vGroup1.addComponent(component, myHeight, myHeight, myHeight);
			}
		}

		vGroup.addGroup(vGroup1);

		vGroup.addGap(0, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2);
		layout.setVerticalGroup(vGroup);
	}
}
