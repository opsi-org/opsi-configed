/*
 * JMenuItemFormatted.java
 *
 */

package de.uib.utilities.swing;

/**
 *
 * @author roeder
 */
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import de.uib.configed.Globals;

public class JMenuItemFormatted extends JMenuItem {
	Font specialFont = Globals.defaultFontBig;

	public JMenuItemFormatted() {
		super();
		super.setFont(specialFont);
	}

	public JMenuItemFormatted(String text) {
		super(text);
		super.setFont(specialFont);
	}

	public JMenuItemFormatted(String text, Icon icon) {
		super(text, icon);
		super.setFont(specialFont);
	}
}
