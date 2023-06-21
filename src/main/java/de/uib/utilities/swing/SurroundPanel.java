/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class SurroundPanel extends JPanel {

	public SurroundPanel(JComponent c) {
		super.setOpaque(false);
		super.setLayout(new FlowLayout(FlowLayout.CENTER));
		super.add(c);
	}
}
