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
import javax.swing.text.JTextComponent;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

// A simple class that searches for a word in
// a document and highlights occurrences of that word
public class WordSearcher {
	private JTextComponent comp;
	private Highlighter.HighlightPainter painter;
	private int lastReturnedOffset;
	private boolean cS;

	public WordSearcher(JTextComponent comp) {
		this.comp = comp;
		this.painter = new UnderlineHighlightPainter(Globals.FAILED_COLOR);
		this.lastReturnedOffset = -1;
	}

	// Set case sensitivity
	public void setCaseSensitivity(boolean cs) {
		this.cS = cs;
	}

	public void setLastReturnedOffset(int lastReturnedOffset) {
		this.lastReturnedOffset = lastReturnedOffset;
	}

	// Search for a word and return the offset of the
	// next occurrence. Highlights are added for all
	// occurrences found.
	public int search(String word) {

		Highlighter compHighlighter = comp.getHighlighter();

		// Remove any existing highlights for last word
		Highlighter.Highlight[] highlights = compHighlighter.getHighlights();
		for (int i = 0; i < highlights.length; i++) {
			Highlighter.Highlight h = highlights[i];
			if (h.getPainter() instanceof UnderlineHighlightPainter) {
				compHighlighter.removeHighlight(h);
			}
		}

		if (word == null || word.isEmpty()) {
			return -1;
		}

		// Look for the word we are given - insensitive search
		String content = null;
		try {
			Document d = comp.getDocument();

			if (cS) {
				content = d.getText(0, d.getLength());
			} else {
				content = d.getText(0, d.getLength()).toLowerCase(Locale.ROOT);
			}
		} catch (BadLocationException e) {
			// Cannot happen
			Logging.warning(this, "unexpected exception in search", e);
			return -1;
		}

		if (!cS) {
			word = word.toLowerCase(Locale.ROOT);
		}

		int lastIndex = 0;
		int wordSize = word.length();
		int firstOffset = -1;
		int returnOffset = lastReturnedOffset;

		while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
			int endIndex = lastIndex + wordSize;
			try {
				compHighlighter.addHighlight(lastIndex, endIndex, painter);
			} catch (BadLocationException e) {
				Logging.warning(this, "could not add highlight to comphighlighter", e);
			}
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
		lastReturnedOffset = returnOffset;
		return returnOffset;
	}
}
