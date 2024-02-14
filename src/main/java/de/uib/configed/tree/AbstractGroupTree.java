
/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;

public abstract class AbstractGroupTree extends JTree implements TreeSelectionListener {
	public final GroupNode rootNode = new GroupNode("top");

	protected AbstractGroupTree() {
		init();
	}

	private void init() {
		super.addTreeSelectionListener(this);

		createTopNodes();

		setRootVisible(false);
		setShowsRootHandles(true);
	}

	abstract void createTopNodes();
}