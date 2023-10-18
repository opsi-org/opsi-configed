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

import javax.swing.Icon;
import javax.swing.JMenuItem;

public class JMenuItemFormatted extends JMenuItem {

	public JMenuItemFormatted() {
		super();
	}

	public JMenuItemFormatted(String text) {
		super(text);
	}

	public JMenuItemFormatted(String text, Icon icon) {
		super(text, icon);
	}
}
