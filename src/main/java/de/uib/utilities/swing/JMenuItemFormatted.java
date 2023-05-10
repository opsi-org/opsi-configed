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

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class JMenuItemFormatted extends JMenuItem {
	private Font specialFont = Globals.defaultFontBig;

	public JMenuItemFormatted() {
		super();
		if (!ConfigedMain.FONT) {
			super.setFont(specialFont);
		}
	}

	public JMenuItemFormatted(String text) {
		super(text);
		if (!ConfigedMain.FONT) {
			super.setFont(specialFont);
		}
	}

	public JMenuItemFormatted(String text, Icon icon) {
		super(text, icon);
		if (!ConfigedMain.FONT) {
			super.setFont(specialFont);
		}
	}
}
