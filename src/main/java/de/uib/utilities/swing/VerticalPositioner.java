/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class VerticalPositioner extends JPanel {

	public VerticalPositioner(JComponent firstC, JComponent secondC) {
		super.setLayout(new BorderLayout());
		super.add(firstC, BorderLayout.NORTH);
		super.add(new CenterPositioner(secondC), BorderLayout.CENTER);
	}
}
