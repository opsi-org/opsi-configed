/**
 * Copyright (c) uib GmbH <info@uib.de> License: AGPL-3.0 This file is part of
 * opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTree;

import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ProductTree extends JTree {
	public ProductTree() {
		for (Entry<String, Map<String, String>> groupEntry : PersistenceControllerFactory.getPersistenceController()
				.getGroupDataService().getProductGroupsPD().entrySet()) {
			Logging.devel(groupEntry.getKey() + ": " + groupEntry.getValue());
		}
	}
}