/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

public class IconNode extends DefaultMutableTreeNode {
	private Icon icon;

	public IconNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	public IconNode(Object userObject) {
		this(userObject, true);
	}

	// set the icon as default for all types of icons
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}
}
