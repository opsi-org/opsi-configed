/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.tabbedpane;

import javax.swing.JPanel;

import de.uib.utilities.logging.Logging;

public class TabClientAdapter extends JPanel implements TabClient {

	public TabClientAdapter() {
		super();

	}

	@Override
	public void reset() {
		Logging.info(this, "TabClientAdapter.reset() ");
	}

	@Override
	public boolean mayLeave() {
		boolean result = true;
		Logging.debug(this, "TabClientAdapter.mayLeave() " + result);
		return result;
	}
}
