/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */
package de.uib.configed.gui.features.logviewer.logpane.view;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import de.uib.configed.share.logging.Logging;

public class LineHighlightPainter implements Highlighter.HighlightPainter {
	private final Color color;
	private final JTextPane component;

	public LineHighlightPainter(JTextPane component, Color color) {
		this.color = color;
		this.component = component;
	}

	@Override
	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
		try {
			Element root = component.getDocument().getDefaultRootElement();
			int lineIndex = root.getElementIndex(component.getCaretPosition());
			Element line = root.getElement(lineIndex);
			int lineStart = line.getStartOffset();
			int lineEnd = Math.max(lineStart, line.getEndOffset() - 1);

			Rectangle2D startRect = null;
			Rectangle2D endRect = null;
			if (lineEnd >= lineStart) {
				startRect = component.modelToView2D(lineStart);
				endRect = component.modelToView2D(lineEnd);
			}

			double y;
			double height;
			if (startRect != null && endRect != null) {
				y = startRect.getY();
				height = endRect.getY() + endRect.getHeight() - startRect.getY();
			} else {
				// Fallback: approximate using font metrics
				FontMetrics fm = component.getFontMetrics(component.getFont());
				y = (double) lineIndex * (double) fm.getHeight();
				height = fm.getHeight();
			}

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(color);
			g2.fill(new Rectangle2D.Double(0, y, component.getWidth(), height));
			g2.dispose();
		} catch (BadLocationException e) {
			Logging.warning(this, "Failed to highlight current line", e);
		}
	}
}
