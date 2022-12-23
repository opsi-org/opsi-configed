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

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
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

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class FEditPane extends FEdit implements DocumentListener, MouseListener, MouseMotionListener {
	public static final Dimension AREA_DIMENSION = new Dimension(600, 300);
	public static final String WINDOWS_LINK_INTERPRETER = "explorer.exe";
	public static final String LINUX_LINK_INTERPRETER = "firefox";
	private javax.swing.JScrollPane scrollpane;
	private javax.swing.JTextPane textpane;

	protected LinkSearcher searcher;
	protected Highlighter highlighter;
	protected de.uib.utilities.script.CmdLauncher cmdLauncher;

	protected String[] linesplits;

	private boolean singleLine;
	static int count = 0;

	// static final Pattern linkpattern = Pattern.compile(".*.*");

	public FEditPane(String initialText, String hint) {
		super(initialText, hint);;
		logging.info(this, " FEdit constructed for >>" + initialText + "<< title " + hint);

		initFEditText();
		setSingleLine(false);
	}

	public FEditPane(String initialText) {
		super(initialText);
		initFEditText();
	}

	protected void initFEditText() {
		scrollpane = new javax.swing.JScrollPane();
		textpane = new javax.swing.JTextPane();
		scrollpane.setViewportView(textpane);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editingArea.add(scrollpane, BorderLayout.CENTER);

		/*
		 * editingLayout.setHorizontalGroup(
		 * editingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addComponent( scrollpane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		 * );
		 * editingLayout.setVerticalGroup(
		 * editingLayout.createSequentialGroup()
		 * .addComponent( scrollpane, 0, GroupLayout.PREFERRED_SIZE,Short.MAX_VALUE)
		 * );
		 */

		// textpane.setContentType( "text/html" );
		textpane.setContentType("text/plain");

		textpane.setEditable(true);

		textpane.addKeyListener(this);
		textpane.addMouseListener(this);
		textpane.addMouseMotionListener(this);

		// we only register changes after loading the initial document

		searcher = new LinkSearcher(textpane);
		searcher.setCaseSensitivity(true);
		highlighter = new UnderlineHighlighter(null);
		textpane.setHighlighter(highlighter);
		setDataChanged(false);

		cmdLauncher = new de.uib.utilities.script.CmdLauncher();
		if (Globals.isWindows())
			cmdLauncher.setPrefix(WINDOWS_LINK_INTERPRETER);
		else
			cmdLauncher.setPrefix(LINUX_LINK_INTERPRETER);

		// HyperlinkListener hyperlinkListener = new

	}

	protected void setSingleLine(boolean b) {
		singleLine = b;

	}

	@Override
	public void setStartText(String s) {
		logging.debug(this, " FEeditPane setStartText: " + s);
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

		if (s0 == null)
			return false;

		if (s0.length() > 2 && s0.startsWith("\\\\"))
			return true;

		// check URI very roughly
		int linkpos = s0.indexOf(":");
		if (linkpos < 0)
			return false;

		linkpos++;
		if (s0.length() <= linkpos)
			// s0 has : but ends there
			return false;

		return true;

		/*
		 * if ( linkpattern.matcher( s0 ).matches( ) )
		 * {
		 * logging.info(this, "link found in " + s0);
		 * return true;
		 * }
		 * 
		 * return false;
		 */
	}

	private int startOfMarkedString(String s)
	// return first pos in line of recognized string
	// returns -1 if nothing is recognized
	{
		if (s == null)
			return -1;

		int pos = 0;
		while (pos < s.length() && s.charAt(pos) == ' ')
			pos++;
		if (pos == s.length())
			return -1;

		String s0 = s.substring(pos).trim();

		if (isLink(s0))
			return pos;
		return -1;
	}

	protected String getMarkedLine(int charpos) {
		boolean found = false;
		int i = 0;
		int startIndex = 0;
		int endIndex = startIndex;
		String line = null;
		String result = null;
		while (!found && i < linesplits.length) {
			line = linesplits[i];
			endIndex = startIndex + line.length();
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
		// textpane.setText(textpane.getText().replaceAll("\t",""));

		initialText = textpane.getText(); // set new initial text for use in processWindowEvent
		return initialText;
	}

	/*
	 * public void select(int selectionStart, int selectionEnd)
	 * {
	 * textpane.select(selectionStart, selectionEnd);
	 * }
	 */

	@Override
	public void keyReleased(KeyEvent e) {
		super.keyReleased(e);

	}

	@Override
	public void keyTyped(KeyEvent e) {
		super.keyTyped(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == textpane) {

			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
					&& e.getKeyCode() == KeyEvent.VK_TAB)
				buttonCommit.requestFocusInWindow();

			else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && singleLine)
				commit();
		}
		super.keyPressed(e);

	}

	// DocumentListener interface
	@Override
	public void changedUpdate(DocumentEvent e) {
		/*
		 * try{
		 * logging.info(this, "changedUpdate " + e.getDocument().getText(0,
		 * e.getDocument().getLength()));
		 * } catch( BadLocationException ex) {}
		 */

		setDataChanged(true);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		/*
		 * try{
		 * logging.info(this, "insertUpdate " + e.getDocument().getText(0,
		 * e.getDocument().getLength()));
		 * } catch( BadLocationException ex) {}
		 */

		setDataChanged(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		/*
		 * try{
		 * logging.info(this, "removeUpdate, current text >>" +
		 * e.getDocument().getText(0, e.getDocument().getLength()) + "<<");
		 * } catch( BadLocationException ex) {}
		 */

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
				logging.info(this, " got link " + line);
				cmdLauncher.launch("\"" + line + "\"");
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	// MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();

		if (getMarkedLine(textpane.viewToModel2D(p)) != null)
			textpane.setCursor(new Cursor(Cursor.HAND_CURSOR));
		else
			textpane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public static void main(String[] args) {
		logging.debug(" invoking " + FEditPane.class);

		SwingUtilities.invokeLater(() -> {
			logging.setSuppressConsole(false);
			FEditPane f = new FEditPane("abc", "");
			f.init(new Dimension(300, 200));
			f.setVisible(true);

			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < args.length; i++) {
				buf.append(args[i]);
				buf.append("\n");
			}

			f.setStartText(buf.toString());

			count++;

		});

	}

	// A simple class that searches for a word in
	// a document and highlights occurrences of that word
	public class LinkSearcher {
		protected JTextComponent comp;
		protected Highlighter.HighlightPainter painter;
		protected int lastReturnedOffset;
		protected boolean cS = false;

		public LinkSearcher(JTextComponent comp) {
			this.comp = comp;
			this.painter = new UnderlineHighlightPainter(Color.BLUE);
			this.lastReturnedOffset = -1;
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

			/*
			 * if (word == null || word.equals("")) {
			 * return -1;
			 * }
			 * 
			 */

			String content = null;
			try {
				Document d = comp.getDocument();

				if (cS)
					content = d.getText(0, d.getLength());
				else
					content = d.getText(0, d.getLength()).toLowerCase();
			} catch (BadLocationException e) {
				// Cannot happen
			}

			linesplits = content.split("\n");

			int startIndex = 0;
			int endIndex = startIndex;
			int lastFoundIndex = 0;

			for (int i = 0; i < linesplits.length; i++) {
				String line = linesplits[i];

				endIndex = startIndex + line.length();

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
		protected Color color; // The color for the underline

		public UnderlineHighlightPainter(Color c) {
			color = c;
		}

		@Override
		public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
			// Do nothing: this method will never be called
		}

		@Override
		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			g.setColor(color == null ? c.getSelectionColor() : color);

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
					alloc = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
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
		protected final Highlighter.HighlightPainter sharedPainter = new UnderlineHighlightPainter(null);

		// Painter used for this highlighter
		protected Highlighter.HighlightPainter painter;

		public UnderlineHighlighter(Color c) {
			painter = (c == null ? sharedPainter : new UnderlineHighlightPainter(c));
		}

		// Convenience method to add a highlight with
		// the default painter.
		public Object addHighlight(int p0, int p1) throws BadLocationException {
			return addHighlight(p0, p1, painter);
		}

		@Override
		public void setDrawsLayeredHighlights(boolean newValue) {
			// Illegal if false - we only support layered highlights
			if (newValue == false) {
				throw new IllegalArgumentException("UnderlineHighlighter only draws layered highlights");
			}
			super.setDrawsLayeredHighlights(true);
		}
	}

}
