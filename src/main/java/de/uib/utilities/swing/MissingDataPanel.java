/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import javax.swing.JComponent;

public interface MissingDataPanel {
	void setMissingDataPanel(boolean b);

	void setMissingDataPanel(boolean b, JComponent c);
}
