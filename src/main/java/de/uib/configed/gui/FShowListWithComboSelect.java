/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uib.Main;
import de.uib.configed.Globals;

/**
 * This class is intended to show a list in text area
 */
public class FShowListWithComboSelect extends FShowList {
	private JComboBox<String> combo;
	private JLabel labelChoice;

	public FShowListWithComboSelect(JFrame owner, String title, boolean modal, String choiceTitle, String[] choices,
			String[] buttonList) {
		super(owner, title, modal, buttonList);

		labelChoice = new JLabel(choiceTitle + ": ");
		labelChoice.setOpaque(true);
		if (!Main.THEMES) {
			labelChoice.setBackground(Globals.BACKGROUND_COLOR_3);
		}
		northPanel.add(labelChoice);
		combo = new JComboBox<>(choices);
		if (!Main.FONT) {
			combo.setFont(Globals.defaultFontBold);
		}
		northPanel.add(combo);

		super.pack();
	}

	public Object getChoice() {
		return combo.getSelectedItem();
	}
}
