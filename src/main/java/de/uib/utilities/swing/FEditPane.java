/*
 * FEditPane.java
 * recognizes lines which seem to be a link
 * (c) uib 2009-2010,2021
 *
 */

package de.uib.utilities.swing;

/**
 *
 * @author roeder
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.script.CmdLauncher;

public class FEditPane extends FEdit implements DocumentListener, MouseListener, MouseMotionListener {
	public static final Dimension AREA_DIMENSION = new Dimension(600, 300);
	public static final String WINDOWS_LINK_INTERPRETER = "explorer.exe";
	public static final String LINUX_LINK_INTERPRETER = "firefox";
	private JTextPane textpane;

	private LinkSearcher searcher;
	private CmdLauncher cmdLauncher;

	private String[] linesplits;

	private boolean singleLine;

	public FEditPane(String initialText, String hint) {
		super(initialText, hint);
		Logging.info(this, " FEdit constructed for >>" + initialText + "<< title " + hint);

		initFEditText();
		singleLine = false;
	}

	public FEditPane(String initialText) {
		super(initialText);
		initFEditText();
	}

	private void initFEditText() {
		JScrollPane scrollpane = new JScrollPane();
		textpane = new JTextPane();
		scrollpane.setViewportView(textpane);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editingArea.add(scrollpane, BorderLayout.CENTER);

		textpane.setContentType("text/plain");

		textpane.setEditable(true);

		textpane.addKeyListener(this);
		textpane.addMouseListener(this);
		textpane.addMouseMotionListener(this);

		// we only register changes after loading the initial document

		searcher = new LinkSearcher(textpane);
		searcher.setCaseSensitivity(true);
		Highlighter highlighter = new UnderlineHighlighter(null);
		textpane.setHighlighter(highlighter);
		setDataChanged(false);

		cmdLauncher = new CmdLauncher();
		if (Globals.isWindows()) {
			cmdLauncher.setPrefix(WINDOWS_LINK_INTERPRETER);
		} else {
			cmdLauncher.setPrefix(LINUX_LINK_INTERPRETER);
		}

		// HyperlinkListener hyperlinkListener = new

	}

	@Override
	public void setStartText(String s) {
		Logging.debug(this, " FEeditPane setStartText: " + s);
		super.setStartText(s);

		textpane.setText(s);

		searchAndHighlight();

		// we only register changes after loading the initial document
		textpane.getDocument().addDocumentListener(this);

		super.setDataChanged(false);

	}

	private void searchAndHighlight() {

		searcher.searchLinks();
	}

	public boolean isLink(String s0) {

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
	private int startOfMarkedString(String s) {

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

	private String getMarkedLine(int charpos) {
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

	@Override
	public String getText() {

		// set new initial text for use in processWindowEvent
		initialText = textpane.getText();
		return initialText;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textpane) {

			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
					&& e.getKeyCode() == KeyEvent.VK_TAB) {
				buttonCommit.requestFocusInWindow();
			} else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && singleLine) {
				commit();
			}
		}
		super.keyPressed(e);

	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {

		setDataChanged(true);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {

		setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {

		setDataChanged(true);
	}

	@Override
	public void setDataChanged(boolean b) {

		super.setDataChanged(b);
		searchAndHighlight();

	}

	// MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {

		if (e.getClickCount() > 1) {
			Point p = e.getPoint();

			String line = getMarkedLine(textpane.viewToModel2D(p));

			if (line != null) {
				Logging.info(this, " got link " + line);
				cmdLauncher.launch("\"" + line + "\"");
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

	// MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();

		if (getMarkedLine(textpane.viewToModel2D(p)) != null) {
			textpane.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else {
			textpane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	// A simple class that searches for a word in
	// a document and highlights occurrences of that word
	public class LinkSearcher {
		private JTextComponent comp;
		private Highlighter.HighlightPainter painter;
		private boolean cS;

		public LinkSearcher(JTextComponent comp) {
			this.comp = comp;
			this.painter = new UnderlineHighlightPainter(Globals.F_EDIT_PANE_UNDERLINE_HIGHLIGHTER_PAINTER);
		}

		// Set case sensitivity
		public void setCaseSensitivity(boolean cs) {
			this.cS = cs;
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
					content = d.getText(0, d.getLength()).toLowerCase();
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
						// Nothing to do
					}
				}
				startIndex = endIndex + 1;
			}

			return lastFoundIndex;
		}
	}

	// Painter for underlined highlights
	public class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter {
		// The color for the underline
		private Color color;

		public UnderlineHighlightPainter(Color c) {
			color = c;
		}

		@Override
		public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
			// Do nothing: this method will never be called
		}

		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			if (!ConfigedMain.THEMES) {
				g.setColor(color == null ? c.getSelectionColor() : color);
			}

			Rectangle alloc = null;
			if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
				if (bounds instanceof Rectangle) {
					alloc = (Rectangle) bounds;
				} else {
					alloc = bounds.getBounds();
				}
			} else {
				try {
					Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
					if (shape instanceof Rectangle) {
						alloc = (Rectangle) shape;
					} else {
						alloc = shape.getBounds();
					}
				} catch (BadLocationException e) {
					return null;
				}
			}

			FontMetrics fm = c.getFontMetrics(c.getFont());
			int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
			g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
			g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width, baseline + 1);

			return alloc;
		}
	}

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
}
