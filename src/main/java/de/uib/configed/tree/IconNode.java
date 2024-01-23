/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.tree;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

public class IconNode extends DefaultMutableTreeNode {
	private Icon icon;

	private String toolTipText;

	private Map<String, Object> deviceInfo;

	public IconNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		deviceInfo = null;
	}

	public IconNode(Object userObject) {
		this(userObject, true);
	}

	public void setToolTipText(String s) {
		toolTipText = s;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public void setDeviceInfo(Map<String, Object> deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public Map<String, Object> getDeviceInfo() {
		return deviceInfo;
	}

	// set the icon as default for all types of icons
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public Icon getIcon() {
		return icon;
	}
}
