/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.function.Supplier;

import javax.swing.JMenu;

public record ClientMenuItemConfig(String resourceKey, Runnable action, Supplier<JMenu> subMenuSupplier, String icon,
		boolean invertedIcon, boolean dependOnSelectionCount, boolean readOnly) {
	public static ClientMenuItemConfig item(String resourceKey, Runnable action) {
		return new ClientMenuItemConfig(resourceKey, action, null, null, false, false, false);
	}

	public static ClientMenuItemConfig submenu(String resourceKey, Supplier<JMenu> supplier) {
		return new ClientMenuItemConfig(resourceKey, null, supplier, null, false, false, false);
	}

	public ClientMenuItemConfig withIcon(String icon) {
		return new ClientMenuItemConfig(resourceKey, action, subMenuSupplier, icon, invertedIcon,
				dependOnSelectionCount, readOnly);
	}

	public ClientMenuItemConfig withInvertedIcon(boolean invertedIcon) {
		return new ClientMenuItemConfig(resourceKey, action, subMenuSupplier, icon, invertedIcon,
				dependOnSelectionCount, readOnly);
	}

	public ClientMenuItemConfig dependOnSelectionCount(boolean depend) {
		return new ClientMenuItemConfig(resourceKey, action, subMenuSupplier, icon, invertedIcon, depend, readOnly);
	}

	public ClientMenuItemConfig readOnly(boolean readOnly) {
		return new ClientMenuItemConfig(resourceKey, action, subMenuSupplier, icon, invertedIcon,
				dependOnSelectionCount, readOnly);
	}
}