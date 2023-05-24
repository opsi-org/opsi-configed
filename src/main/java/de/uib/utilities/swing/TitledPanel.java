/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.Main;
import de.uib.configed.Globals;

public class TitledPanel extends JPanel {
	private JLabel label1;
	private JLabel label2;

	public TitledPanel() {
		this("", "");
	}

	public TitledPanel(String title1, String title2) {
		label1 = new JLabel();
		label2 = new JLabel();
		initGui(title1, title2);
	}

	public void setTitle(String s1, String s2) {
		label1.setText(s1);
		label2.setText(s2);
		label2.setVisible(s2 != null);
	}

	private void initGui(String title1, String title2) {
		if (!Main.THEMES) {
			setBackground(Globals.BACKGROUND_COLOR_7);
		}

		if (!Main.FONT) {
			label1.setFont(Globals.defaultFontBig);
			label2.setFont(Globals.defaultFontBig);
		}

		GroupLayout innerLayout = new GroupLayout(this);
		this.setLayout(innerLayout);
		innerLayout
				.setVerticalGroup(innerLayout.createSequentialGroup()
						.addGap(2 * Globals.VGAP_SIZE, 3 * Globals.VGAP_SIZE, 3 * Globals.VGAP_SIZE)
						.addComponent(label1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(label2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Short.MAX_VALUE));

		innerLayout.setHorizontalGroup(innerLayout.createParallelGroup().addGroup(innerLayout.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
				.addComponent(label1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(innerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(label2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)));

		setTitle(title1, title2);
	}
}
