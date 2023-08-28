/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.util.List;

import javax.swing.JList;

import de.uib.Main;
import de.uib.configed.Globals;

public class XList extends JList<String> {

	public XList(List<String> listData) {
		super(listData.toArray(new String[0]));
		configure();
	}

	private void configure() {
		if (!Main.THEMES) {
			setSelectionBackground(Globals.NIMBUS_SELECTION_BACKGROUND);
			setBackground(Globals.NIMBUS_BACKGROUND);
		}
	}

}
