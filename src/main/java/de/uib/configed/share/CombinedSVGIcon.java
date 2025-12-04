/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.share;

import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Combines two SVG icons by painting one on top of the other with scaling and
 * offset. I created this class to keep the properties of SVG icons like
 * scalability, especially when used in high-DPI settings.
 */
public class CombinedSVGIcon implements Icon {
	private final Icon baseIcon;
	private final Icon overlayIcon;

	private final double overlayScale;
	private final double overlayOffsetX;
	private final double overlayOffsetY;

	public CombinedSVGIcon(Icon baseIcon, Icon overlayIcon, double overlayScale, double overlayOffsetX,
			double overlayOffsetY) {
		this.baseIcon = baseIcon;
		this.overlayIcon = overlayIcon;
		this.overlayScale = overlayScale;
		this.overlayOffsetX = overlayOffsetX;
		this.overlayOffsetY = overlayOffsetY;
	}

	@Override
	public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g.create();
		baseIcon.paintIcon(c, g2, x, y);

		g2.translate(x + overlayOffsetX * getIconWidth(), y + overlayOffsetY * getIconHeight());

		g2.scale(overlayScale, overlayScale);
		overlayIcon.paintIcon(c, g2, 0, 0);

		g2.dispose();
	}

	@Override
	public int getIconWidth() {
		return baseIcon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return baseIcon.getIconHeight();
	}
}
