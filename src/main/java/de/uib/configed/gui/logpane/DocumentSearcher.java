/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;

import de.uib.utils.logging.Logging;

import javax.swing.text.JTextComponent;

/**
 * A simple class that searches for an expression in a document and highlights
 * occurrences of that expression.
 */
public class DocumentSearcher {
	private JTextComponent comp;
	private Highlighter.HighlightPainter painter;
	private int lastReturnedOffset;
	private boolean caseSensitive;

	public DocumentSearcher(JTextComponent comp) {
		this.comp = comp;
		this.painter = new UnderlineHighlightPainter();
		this.lastReturnedOffset = -1;
	}

	public void setCaseSensitivity(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public void setLastReturnedOffset(int lastReturnedOffset) {
		this.lastReturnedOffset = lastReturnedOffset;
	}

	/**
	 * Search for an expression and return the offset of the next occurrence.
	 * Highlights are added for all occurrences found.
	 */
	public int search(String expression) {
		removeExistingHighlights();

		if (expression == null || expression.isEmpty()) {
			return -1;
		}

		String content = getShownContent();
		if (content == null) {
			Logging.info(this, "No content is displayed. Unable to search for specified expression: " + expression);
			return -1;
		}

		if (!caseSensitive) {
			expression = expression.toLowerCase(Locale.ROOT);
		}

		return getOffset(content, expression);
	}

	private void removeExistingHighlights() {
		Highlight[] highlights = comp.getHighlighter().getHighlights();
		for (Highlight h : highlights) {
			if (h.getPainter() instanceof UnderlineHighlightPainter) {
				comp.getHighlighter().removeHighlight(h);
			}
		}
	}

	private String getShownContent() {
		String content = null;
		try {
			Document d = comp.getDocument();
			if (caseSensitive) {
				content = d.getText(0, d.getLength());
			} else {
				content = d.getText(0, d.getLength()).toLowerCase(Locale.ROOT);
			}
		} catch (BadLocationException e) {
			// Cannot happen
			Logging.warning(this, "unexpected exception in search", e);
		}
		return content;
	}

	private int getOffset(String content, String expression) {
		int lastIndex = 0;
		int expressionSize = expression.length();
		int firstOffset = -1;
		int returnOffset = lastReturnedOffset;

		while ((lastIndex = content.indexOf(expression, lastIndex)) != -1) {
			int endIndex = lastIndex + expressionSize;
			highlightMatch(lastIndex, endIndex);
			if (firstOffset == -1) {
				firstOffset = lastIndex;
			}
			if (returnOffset == lastReturnedOffset && lastIndex > lastReturnedOffset) {
				returnOffset = lastIndex;
			}
			lastIndex = endIndex;
		}

		if (returnOffset == lastReturnedOffset) {
			returnOffset = firstOffset;
		}

		return returnOffset;
	}

	private void highlightMatch(int startIndex, int endIndex) {
		try {
			comp.getHighlighter().addHighlight(startIndex, endIndex, painter);
		} catch (BadLocationException e) {
			Logging.warning(this, "could not add highlight to comphighlighter", e);
		}
	}
}
