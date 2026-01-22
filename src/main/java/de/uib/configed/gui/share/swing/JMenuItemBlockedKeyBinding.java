/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class JMenuItemBlockedKeyBinding extends JMenuItem {

	public JMenuItemBlockedKeyBinding(String text) {
		super(text);
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		// We are going to disable the functionality of the key binding, because here the key binding
		// should only be shown graphically, but not be active. If it was active, it would become
		// active globally since we add these Items to the MenuBar. Instead, we directly add
		// the key binding to the whole client view (a JSplitPane) in ClientConfiguration.
		return false;
	}
}
