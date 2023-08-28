/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * JMenuItemFormatted.java
 *
 */

package de.uib.utilities.swing;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.uib.Main;
import de.uib.configed.Globals;

public class JMenuItemFormatted extends JMenuItem {
	private Font specialFont = Globals.DEFAULT_FONT_BIG;

	public JMenuItemFormatted() {
		super();
		if (!Main.FONT) {
			super.setFont(specialFont);
		}
	}

	public JMenuItemFormatted(String text) {
		super(text);
		if (!Main.FONT) {
			super.setFont(specialFont);
		}
	}

	public JMenuItemFormatted(String text, Icon icon) {
		super(text, icon);
		if (!Main.FONT) {
			super.setFont(specialFont);
		}
	}
}
