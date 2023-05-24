/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;

// TODO remove as soon as themes are used
public class ClientTreeUI extends BasicTreeUI {
	public static ComponentUI createUI() {
		return new ClientTreeUI();
	}
}
