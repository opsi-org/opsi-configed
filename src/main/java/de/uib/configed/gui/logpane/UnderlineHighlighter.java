package de.uib.configed.gui.logpane;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class UnderlineHighlighter extends DefaultHighlighter {

	// Shared painter used for default highlighting
	private final Highlighter.HighlightPainter sharedPainter = new UnderlineHighlightPainter(null);

	// Painter used for this highlighter
	private Highlighter.HighlightPainter painter;

	public UnderlineHighlighter(Color c) {
		if (c == null) {
			painter = sharedPainter;
		} else {
			painter = new UnderlineHighlightPainter(c);
		}
	}

	// Convenience method to add a highlight with
	// the default painter.
	public Object addHighlight(int p0, int p1) throws BadLocationException {
		return addHighlight(p0, p1, painter);
	}

	@Override
	public void setDrawsLayeredHighlights(boolean newValue) {
		// Illegal if false - we only support layered highlights
		if (!newValue) {
			throw new IllegalArgumentException("UnderlineHighlighter only draws layered highlights");
		}
		super.setDrawsLayeredHighlights(true);
	}
}
