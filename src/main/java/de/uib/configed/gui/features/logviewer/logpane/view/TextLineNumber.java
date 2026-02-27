/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import javax.swing.text.Utilities;

import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

/**
 * This class will display line numbers for a related text component. The text
 * component must use the same line height for each line. TextLineNumber
 * supports wrapped lines and will highlight the line number of the current line
 * in the text component. This class was designed to be used as a component
 * added to the row header of a JScrollPane.
 */
// Original TextLineNumber class by tips4java
// Source: https://github.com/tips4java/tips4java/blob/main/source/TextLineNumber.java
// Modifications:
//   - Updated depracted method usage.
//   - Minor changes to reduce SonarQube warnings.
@SuppressWarnings("java:S1200")
public final class TextLineNumber extends JPanel implements CaretListener, PropertyChangeListener {
	public static final float LEFT = 0.0F;
	public static final float CENTER = 0.5F;
	public static final float RIGHT = 1.0F;

	private static final int HEIGHT = Integer.MAX_VALUE - 100_0000;

	//  Text component this TextTextLineNumber component is in sync with

	private JTextComponent component;

	//  Properties that can be changed

	private boolean updateFont;
	private int borderGap;
	private Color currentLineBackground;
	private float digitAlignment;
	private int minimumDisplayDigits;

	//  Keep history information to reduce the number of times the component
	//  needs to be repainted

	private int lastDigits;
	private int lastHeight;
	private int lastLine;

	private HashMap<String, FontMetrics> fonts;

	/**
	 * Create a line number component for a text component. This minimum display
	 * width will be based on 3 digits.
	 *
	 * @param component the related text component
	 */
	public TextLineNumber(JTextComponent component) {
		this(component, 3);
	}

	/**
	 * Create a line number component for a text component.
	 *
	 * @param component            the related text component
	 * @param minimumDisplayDigits the number of digits used to calculate the
	 *                             minimum width of the component
	 */
	public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
		this.component = component;

		setFont(component.getFont());
		setOpaque(false);

		setBorderGap(5);
		setDigitAlignment(RIGHT);
		setMinimumDisplayDigits(minimumDisplayDigits);

		//  View of the component has not been updated at the time
		//  the DocumentEvent is fired. A document change may affect the number of displayed lines of text.
		// Therefore the lines numbers will also change.
		component.getDocument().addDocumentListener(
				Utils.onDocumentChange(() -> SwingUtilities.invokeLater(this::handleResizeIfNeeded)));
		component.addCaretListener(this);
		component.addPropertyChangeListener("font", this);
	}

	/**
	 * Gets the update font property
	 *
	 * @return the update font property
	 */
	public boolean updateFont() {
		return updateFont;
	}

	/**
	 * Set the update font property. Indicates whether this Font should be
	 * updated automatically when the Font of the related text component is
	 * changed.
	 *
	 * @param updateFont when true update the Font and repaint the line numbers,
	 *                   otherwise just repaint the line numbers.
	 */
	public void setUpdateFont(boolean updateFont) {
		this.updateFont = updateFont;
	}

	/**
	 * Gets the border gap
	 *
	 * @return the border gap in pixels
	 */
	public int getBorderGap() {
		return borderGap;
	}

	/**
	 * The border gap is used in calculating the left and right insets of the
	 * border. Default value is 5.
	 *
	 * @param borderGap the gap in pixels
	 */
	public void setBorderGap(int borderGap) {
		this.borderGap = borderGap;
		Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
		setBorder(inner);
		lastDigits = 0;
		setPreferredWidth();
	}

	/**
	 * Current line background color in the gutter. Falls back to the text
	 * component's selection color when unset.
	 */
	public Color getCurrentLineBackground() {
		Color bg = currentLineBackground;
		return bg != null ? bg : component.getSelectionColor();
	}

	public void setCurrentLineBackground(Color currentLineBackground) {
		this.currentLineBackground = currentLineBackground;
	}

	/**
	 * Gets the digit alignment
	 *
	 * @return the alignment of the painted digits
	 */
	public float getDigitAlignment() {
		return digitAlignment;
	}

	/**
	 * Specify the horizontal alignment of the digits within the component.
	 * Common values would be:
	 * <ul>
	 * <li>TextLineNumber.LEFT
	 * <li>TextLineNumber.CENTER
	 * <li>TextLineNumber.RIGHT (default)
	 * </ul>
	 * 
	 * @param digitalAlignement the horizontal alignment value
	 */
	public void setDigitAlignment(float digitAlignment) {
		if (digitAlignment > 1.0F) {
			this.digitAlignment = 1.0F;
		} else {
			this.digitAlignment = digitAlignment < 0.0F ? -1.0F : digitAlignment;
		}
	}

	/**
	 * Gets the minimum display digits
	 *
	 * @return the minimum display digits
	 */
	public int getMinimumDisplayDigits() {
		return minimumDisplayDigits;
	}

	/**
	 * Specify the mimimum number of digits used to calculate the preferred
	 * width of the component. Default is 3.
	 *
	 * @param minimumDisplayDigits the number digits used in the preferred width
	 *                             calculation
	 */
	public void setMinimumDisplayDigits(int minimumDisplayDigits) {
		this.minimumDisplayDigits = minimumDisplayDigits;
		setPreferredWidth();
	}

	/**
	 * Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth() {
		Element root = component.getDocument().getDefaultRootElement();
		int lines = root.getElementCount();
		int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

		//  Update sizes when number of digits in the line number changes

		if (lastDigits != digits) {
			lastDigits = digits;
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int width = fontMetrics.charWidth('0') * digits;
			Insets insets = getInsets();
			int preferredWidth = insets.left + insets.right + width;

			Dimension d = getPreferredSize();
			d.setSize(preferredWidth, HEIGHT);
			setPreferredSize(d);
			setSize(d);
		}
	}

	/**
	 * Draw the line numbers
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		//	Determine the width of the space available to draw the line number

		FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
		Insets insets = getInsets();
		int availableWidth = getSize().width - insets.left - insets.right;

		//  Determine the rows to draw within the clipped bounds.

		Rectangle clip = g.getClipBounds();
		int rowStartOffset = component.viewToModel2D(new Point(0, clip.y));
		int endOffset = component.viewToModel2D(new Point(0, clip.y + clip.height));

		while (rowStartOffset <= endOffset) {
			try {
				boolean current = isCurrentLine(rowStartOffset);
				if (current) {
					Rectangle2D r = component.modelToView2D(rowStartOffset);
					g.setColor(getCurrentLineBackground());
					g.fillRect(0, (int) r.getY(), getWidth(), (int) r.getHeight());
				}
				g.setColor(current ? getForeground() : Color.GRAY);
				g.setFont(component.getFont());

				//  Get the line number as a string and then determine the
				//  "X" and "Y" offsets for drawing the string.

				String lineNumber = getTextLineNumber(rowStartOffset);
				int stringWidth = fontMetrics.stringWidth(lineNumber);
				int x = getOffsetX(availableWidth, stringWidth) + insets.left;
				int y = getOffsetY(rowStartOffset, fontMetrics);
				g.drawString(lineNumber, x, y);

				//  Move to the next row

				rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
			} catch (BadLocationException e) {
				Logging.error(this, "Failed to paint the component", e);
				break;
			}
		}
	}

	/*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
	 */
	private boolean isCurrentLine(int rowStartOffset) {
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		return root.getElementIndex(rowStartOffset) == root.getElementIndex(caretPosition);
	}

	/*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
	 */
	private String getTextLineNumber(int rowStartOffset) {
		Element root = component.getDocument().getDefaultRootElement();
		int index = root.getElementIndex(rowStartOffset);
		Element line = root.getElement(index);

		return line.getStartOffset() == rowStartOffset ? String.valueOf(index + 1) : "";
	}

	/*
	 *  Determine the X offset to properly align the line number when drawn
	 */
	@SuppressWarnings("java:S2164")
	private int getOffsetX(int availableWidth, int stringWidth) {
		return (int) ((availableWidth - stringWidth) * digitAlignment);
	}

	/*
	 *  Determine the Y offset for the current row
	 */
	private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics) throws BadLocationException {
		//  Get the bounding rectangle of the row

		Rectangle2D r = component.modelToView2D(rowStartOffset);
		int lineHeight = fontMetrics.getHeight();
		int descent = 0;

		//  The text needs to be positioned above the bottom of the bounding
		//  rectangle based on the descent of the font(s) contained on the row.

		if (((int) r.getHeight()) == lineHeight) {
			// default font is being used
			descent = fontMetrics.getDescent();
		} else {
			// We need to check all the attributes for font changes
			if (fonts == null) {
				fonts = new HashMap<>();
			}

			Element root = component.getDocument().getDefaultRootElement();
			int index = root.getElementIndex(rowStartOffset);
			Element line = root.getElement(index);

			for (int i = 0; i < line.getElementCount(); i++) {
				Element child = line.getElement(i);
				AttributeSet as = child.getAttributes();
				String fontFamily = (String) as.getAttribute(StyleConstants.FontFamily);
				Integer fontSize = (Integer) as.getAttribute(StyleConstants.FontSize);
				String key = fontFamily + fontSize;

				FontMetrics fm = fonts.computeIfAbsent(key, (String k) -> {
					Font font = new Font(fontFamily, Font.PLAIN, fontSize);
					return component.getFontMetrics(font);
				});

				descent = Math.max(descent, fm.getDescent());
			}
		}

		int y = (int) (r.getY() + r.getHeight());
		return y - descent;
	}

	//
	//  Implement CaretListener interface
	//
	@Override
	public void caretUpdate(CaretEvent e) {
		//  Get the line the caret is positioned on

		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		int currentLine = root.getElementIndex(caretPosition);

		//  Need to repaint so the correct line number can be highlighted

		if (lastLine != currentLine) {
			getParent().repaint();
			lastLine = currentLine;
		}
	}

	private void handleResizeIfNeeded() {
		try {
			int endPos = component.getDocument().getLength();
			Rectangle2D rect = component.modelToView2D(endPos);

			if (rect != null && ((int) rect.getHeight()) != lastHeight) {
				setPreferredWidth();
				getParent().repaint();
				lastHeight = (int) rect.getHeight();
			}
		} catch (BadLocationException ex) {
			Logging.error(this, "Failed to repaint component", ex);
		}
	}

	//
	//  Implement PropertyChangeListener interface
	//
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof Font newFont) {
			if (updateFont) {
				setFont(newFont);
				lastDigits = 0;
				setPreferredWidth();
			} else {
				getParent().repaint();
			}
		}
	}
}
