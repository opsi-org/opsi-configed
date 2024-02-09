/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class GroupNode extends DefaultMutableTreeNode {
	private boolean allowingOnlyGroupChilds;
	private boolean immutable;
	private boolean fixed;

	public GroupNode(Object userObject) {
		super(userObject, true);
	}

	public void setAllowsOnlyGroupChilds(boolean b) {
		allowingOnlyGroupChilds = b;
	}

	public boolean allowsOnlyGroupChilds() {
		return allowingOnlyGroupChilds;
	}

	public void setImmutable(boolean b) {
		immutable = b;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public void setFixed(boolean b) {
		fixed = b;
	}

	public boolean isFixed() {
		return fixed;
	}
}
