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
	private Icon closedIcon;

	private Icon leafIcon;

	private Icon openIcon;

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

	public Icon getClosedIcon() {
		return closedIcon;
	}

	// set the icon as default for all types of icons
	public void setIcon(Icon anIcon) {
		setClosedIcon(anIcon);
		setLeafIcon(anIcon);
		setOpenIcon(anIcon);
	}

	public void setClosedIcon(Icon aClosedIcon) {
		closedIcon = aClosedIcon;
	}

	public Icon getLeafIcon() {
		return leafIcon;
	}

	public void setLeafIcon(Icon aLeafIcon) {
		leafIcon = aLeafIcon;
	}

	public Icon getOpenIcon() {
		return openIcon;
	}

	public void setOpenIcon(Icon anOpenIcon) {
		openIcon = anOpenIcon;
	}
}
