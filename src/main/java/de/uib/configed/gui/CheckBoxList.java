/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class CheckBoxList extends JList<JCheckBox> {
	private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public CheckBoxList(ListModel<JCheckBox> model) {
		super.setCellRenderer(new CellRenderer());

		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());

				if (index != -1) {
					JCheckBox checkbox = getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		super.setModel(model);
	}

	private class CellRenderer implements ListCellRenderer<JCheckBox> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox checkbox, int index,
				boolean isSelected, boolean cellHasFocus) {

			checkbox.setEnabled(isEnabled());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(noFocusBorder);

			return checkbox;
		}
	}
}
