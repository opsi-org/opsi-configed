/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane.view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter.LayerPainter;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

import de.uib.configed.share.logging.Logging;

// Painter for underlined highlights
public class UnderlineHighlightPainter extends LayerPainter {
	@Override
	public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
		// Do nothing: this method will never be called
	}

	@Override
	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
		Rectangle alloc = null;
		if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
			if (bounds instanceof Rectangle rectangle) {
				alloc = rectangle;
			} else {
				alloc = bounds.getBounds();
			}
		} else {
			try {
				Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);

				if (shape instanceof Rectangle rectangle) {
					alloc = rectangle;
				} else {
					alloc = shape.getBounds();
				}
			} catch (BadLocationException e) {
				Logging.warning(this, e, "could not get shape for location");
				return null;
			}
		}

		// Resolve underline color from the document's current foreground
		Color underline = c.getForeground();
		try {
			StyledDocument doc = (StyledDocument) c.getDocument();
			AttributeSet as = doc.getCharacterElement(Math.max(0, Math.min(offs0, doc.getLength()))).getAttributes();
			Color fg = (Color) as.getAttribute(StyleConstants.Foreground);
			if (fg != null) {
				underline = fg;
			}
		} catch (ClassCastException e) {
			Logging.debug(this, e, "Non-styled document; keep component foreground");
		}
		g.setColor(underline);

		FontMetrics fm = c.getFontMetrics(c.getFont());
		int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
		g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
		g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width, baseline + 1);

		return alloc;
	}
}
