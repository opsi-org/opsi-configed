/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

public class GroupNode extends IconNode {
	private boolean childsArePersistent = true;
	private boolean allowingOnlyGroupChilds;
	private boolean allowingSubGroups = true;
	private boolean immutable;
	private boolean fixed;

	public GroupNode(Object userObject) {
		super(userObject, true);
	}

	public void setChildsArePersistent(boolean b) {
		childsArePersistent = b;
	}

	public boolean isChildsArePersistent() {
		return childsArePersistent;
	}

	public void setAllowsOnlyGroupChilds(boolean b) {
		allowingOnlyGroupChilds = b;
	}

	public boolean allowsOnlyGroupChilds() {
		return allowingOnlyGroupChilds;
	}

	public void setAllowsSubGroups(boolean b) {
		allowingSubGroups = b;
	}

	public boolean allowsSubGroups() {
		return allowingSubGroups;
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
