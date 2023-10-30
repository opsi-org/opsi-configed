/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import de.uib.configed.gui.logpane.UnderlineHighlightPainter;
import de.uib.utilities.logging.Logging;

// A simple class that searches for a word in
// a document and highlights occurrences of that word
public class LinkSearcher {
	private JTextComponent comp;
	private Highlighter.HighlightPainter painter;
	private boolean cS;

	private String[] linesplits;

	public LinkSearcher(JTextComponent comp) {
		this.comp = comp;
		this.painter = new UnderlineHighlightPainter();
	}

	// Set case sensitivity
	public void setCaseSensitivity(boolean cs) {
		this.cS = cs;
	}

	private static boolean isLink(String s0) {

		if (s0 == null) {
			return false;
		}

		if (s0.length() > 2 && s0.startsWith("\\\\")) {
			return true;
		}

		// check URI very roughly
		int linkpos = s0.indexOf(":");
		if (linkpos < 0) {
			return false;
		}

		// s0 has : but ends there
		return linkpos + 1 < s0.length();
	}

	// return first pos in line of recognized string
	// returns -1 if nothing is recognized
	private static int startOfMarkedString(String s) {

		if (s == null) {
			return -1;
		}

		int pos = 0;
		while (pos < s.length() && s.charAt(pos) == ' ') {
			pos++;
		}
		if (pos == s.length()) {
			return -1;
		}

		String s0 = s.substring(pos).trim();

		if (isLink(s0)) {
			return pos;
		}

		return -1;
	}

	public String getMarkedLine(int charpos) {
		boolean found = false;
		int i = 0;
		int startIndex = 0;
		String line = null;
		String result = null;
		while (!found && i < linesplits.length) {
			line = linesplits[i];
			int endIndex = startIndex + line.length();
			if (startIndex <= charpos && charpos <= endIndex && startOfMarkedString(line) >= 0) {
				found = true;
				result = line;
			}
			startIndex = endIndex + 1;
			i++;
		}
		return result;
	}

	// Search for a select string and return the offset of the
	// next occurrence. Highlights are added for all
	// occurrences found.
	public int searchLinks() {
		Highlighter highlighter = comp.getHighlighter();

		// Remove any existing highlights for last word
		Highlighter.Highlight[] highlights = highlighter.getHighlights();
		for (int i = 0; i < highlights.length; i++) {
			Highlighter.Highlight h = highlights[i];
			if (h.getPainter() instanceof UnderlineHighlightPainter) {
				highlighter.removeHighlight(h);
			}
		}

		String content = null;

		try {
			Document d = comp.getDocument();

			if (cS) {
				content = d.getText(0, d.getLength());
			} else {
				content = d.getText(0, d.getLength()).toLowerCase(Locale.ROOT);
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "Exception thrown when getting Document: " + e);
			return -1;
		}

		linesplits = content.split("\n");

		int startIndex = 0;
		int lastFoundIndex = 0;

		for (int i = 0; i < linesplits.length; i++) {
			String line = linesplits[i];

			int endIndex = startIndex + line.length();

			int posInLine = startOfMarkedString(line);
			int len = line.trim().length();
			if (posInLine >= 0) {
				lastFoundIndex = startIndex;
				try {
					highlighter.addHighlight(startIndex + posInLine, startIndex + posInLine + len, painter);
				} catch (BadLocationException e) {
					Logging.warning(this, "could not find location for highlighter", e);
				}
			}
			startIndex = endIndex + 1;
		}

		return lastFoundIndex;
	}
}
