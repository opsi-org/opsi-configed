/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

public class HeaderOptionsPanel extends JPanel {
	public HeaderOptionsPanel(ListModel<JCheckBox> model) {
		init(model);
	}

	private void init(ListModel<JCheckBox> model) {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		CheckBoxList list = new CheckBoxList(model);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);

		JScrollPane scroll = new JScrollPane(list);
		scroll.setAlignmentX(LEFT_ALIGNMENT);

		add(Box.createRigidArea(new Dimension(0, 1)));
		add(scroll);
	}
}
