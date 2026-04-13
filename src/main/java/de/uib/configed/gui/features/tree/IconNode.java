/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.tree;

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
