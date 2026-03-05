/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.table.gui;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import de.uib.configed.gui.share.icons.Icons;

public class IconTableCellRenderer<T> extends ColorTableCellRenderer {
	public static final Map<String, Icon> PLATFORM_ICONS = Map.of("macos", Icons.getThemeSVGRepoIcon("macos", 16),
			"windows", Icons.getThemeSVGRepoIcon("windows", 16), "linux", Icons.getThemeSVGRepoIcon("linux", 16));
	public static final Map<String, Icon> DEVICE_ICONS = Map.of("desktop", Icons.getThemeSVGRepoIcon("desktop", 16),
			"notebook", Icons.getThemeSVGRepoIcon("laptop", 16), "virtual_machine",
			Icons.getThemeSVGRepoIcon("virtualMachine", 16), "convertible",
			Icons.getThemeSVGRepoIcon("convertible", 16), "server", Icons.getThemeSVGRepoIcon("server", 16), "other",
			Icons.getThemeIntellijIcon("questionMark", 16));

	private final Map<T, Icon> iconMap;
	private final boolean tooltipFromValue;

	public IconTableCellRenderer(Map<T, Icon> iconMap) {
		this(iconMap, true);
	}

	public IconTableCellRenderer(Map<T, Icon> iconMap, boolean tooltipFromValue) {
		this.iconMap = iconMap;
		this.tooltipFromValue = tooltipFromValue;
		super.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

		Icon icon = iconMap.get(value);
		setIcon(icon);

		if (tooltipFromValue && value != null) {
			setToolTipText(value.toString());
		} else {
			setToolTipText(null);
		}

		return this;
	}

	public static Map<Boolean, Icon> booleanMap(Icon iconForTrue) {
		return booleanMap(iconForTrue, null);
	}

	public static Map<Boolean, Icon> booleanMap(Icon iconForTrue, Icon iconForFalse) {
		return iconForFalse == null ? Map.of(Boolean.TRUE, iconForTrue)
				: Map.of(Boolean.TRUE, iconForTrue, Boolean.FALSE, iconForFalse);
	}
}
