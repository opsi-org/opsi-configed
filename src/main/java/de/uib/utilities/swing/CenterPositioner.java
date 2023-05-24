/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class CenterPositioner extends JPanel {

	public CenterPositioner(JComponent comp) {
		super.setLayout(new FlowLayout());
		super.add(comp);
	}

}
