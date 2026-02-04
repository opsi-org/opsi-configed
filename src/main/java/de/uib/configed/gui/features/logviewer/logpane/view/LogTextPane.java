/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneComponent;
import de.uib.configed.gui.features.logviewer.logpane.view.LogFileParser.LogParsedData;
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.swing.WrapEditorKit;
import de.uib.configed.share.logging.Logging;

public class LogTextPane extends JTextPane {
	public static final String DEFAULT_TYPE = "(all)";
	private static final int DEFAULT_MAX_SHOW_LEVEL = 4;

	private DocumentSearcher searcher;
	private Highlighter highlighter;

	private TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	private TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	private Integer showLevel = LogPaneComponent.MIN_LEVEL;

	private Integer displayFontSize = 11;

	private String[] lines;

	private final StyleContext styleContext;
	private final Style[] logLevelStyles;

	private int selTypeIndex = -1;
	private boolean showTypeRestricted;

	private LogFileParser parser;

	private Font monospacedFont;

	private final Highlighter.HighlightPainter caretPainter = new LineHighlightPainter(this,
			Globals.getLogPaneCurrentLineBackground());

	private Object caretLineTag;

	public record CaretContext(int line, int offset) {
	}

	public LogTextPane(int defaultFontSize) {
		this.displayFontSize = defaultFontSize;
		styleContext = new StyleContext() {
			@Override
			public Font getFont(AttributeSet attr) {
				monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);
				return monospacedFont;
			}
		};
		setEditorKit(WrapEditorKit.STYLED.create());

		logLevelStyles = new Style[10];
		setLoglevelStyles();
		parser = new LogFileParser(lines, logLevelStyles);

		super.setSelectionColor(Globals.getLogPaneSelectionBackground());
		super.setCaretColor(Globals.LOG_PANE_CARET_COLOR);

		searcher = new DocumentSearcher(this);
		highlighter = new DefaultHighlighter();
		super.setHighlighter(highlighter);

		super.setEditable(true);
		super.addCaretListener(e -> refreshCurrentLineVisuals());
		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				SwingUtilities.invokeLater(() -> refreshCurrentLineVisuals());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				SwingUtilities.invokeLater(() -> refreshCurrentLineVisuals());
			}
		});
		super.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				refreshCurrentLineVisuals();
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		return getUI().getMinimumSize(this);
	}

	@Override
	public void setSelectedTextColor(Color c) {
		// ignore selection foreground changes
	}

	@Override
	public Color getSelectedTextColor() {
		// leave the existing foreground color
		return null;
	}

	private void refreshCurrentLineVisuals() {
		repaintScrollPaneRowHeader();
		updateCaretLineHighlight();
	}

	private void repaintScrollPaneRowHeader() {
		JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
		if (sp != null && sp.getRowHeader() != null) {
			JViewport rh = sp.getRowHeader();
			if (rh.getView() != null) {
				rh.getView().repaint();
			}
			rh.repaint();
		}
	}

	private void updateCaretLineHighlight() {
		if (caretLineTag != null) {
			highlighter.removeHighlight(caretLineTag);
		}

		int caretPos = getCaretPosition();
		int selStart = getSelectionStart();
		int selEnd = getSelectionEnd();

		// If selection includes the caret, skip to avoid clashing with selection
		if (selStart != selEnd && caretPos >= Math.min(selStart, selEnd) && caretPos <= Math.max(selStart, selEnd)) {
			return;
		}

		int pos = getCaretPosition();
		Element root = getDocument().getDefaultRootElement();
		int line = root.getElementIndex(pos);
		int start = root.getElement(line).getStartOffset();
		int end = root.getElement(line).getEndOffset();

		try {
			caretLineTag = highlighter.addHighlight(start, end, caretPainter);
		} catch (BadLocationException e) {
			Logging.warning(this, "Failed to highlight current line", e);
		}
	}

	public void setCaseSensitivity(boolean caseSensitivity) {
		searcher.setCaseSensitivity(caseSensitivity);
	}

	public String[] getLines() {
		return lines;
	}

	public void buildDocument() {
		setCursor(Globals.WAIT_CURSOR);
		highlighter.removeAllHighlights();
		ImmutableDefaultStyledDocument newDoc = buildRawDocument();
		setDocument(newDoc);
		setCursor(null);
	}

	private ImmutableDefaultStyledDocument buildRawDocument() {
		ImmutableDefaultStyledDocument document = new ImmutableDefaultStyledDocument(styleContext);

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			List<LogFileParser.LogLine> logLines = parser.getData().getParsedLogLines();
			for (int i = 0; i < logLines.size(); i++) {
				LogFileParser.LogLine line = logLines.get(i);
				if (showLine(line)) {
					int pos = document.getLength();
					docLinestartPosition2lineCount.put(pos, i);
					lineCount2docLinestartPosition.put(i, pos);
					document.insertStringTruely(pos, line.getText() + '\n', line.getStyle());
				}
			}
		} catch (BadLocationException e) {
			Logging.warning(this, e, "Failed to build document");
		}

		setDocument(document);

		SwingUtils.runOnEventDispatchThread(() -> {
			highlighter.removeAllHighlights();
			refreshCurrentLineVisuals();
			setCursor(null);
		});

		return document;
	}

	public CaretContext getCaretContext(int oldCaret) {
		int line = -1;
		int offset = 0;
		if (docLinestartPosition2lineCount == null) {
			return new CaretContext(line, offset);
		}

		Map.Entry<Integer, Integer> lastEntry = docLinestartPosition2lineCount.floorEntry(oldCaret);
		if (lastEntry != null) {
			line = lastEntry.getValue();
			offset = oldCaret - lastEntry.getKey();
		}
		return new CaretContext(line, offset);
	}

	public int computeCaretFromContext(CaretContext context) {
		Integer newStart = lineCount2docLinestartPosition.get(context.line());
		if (newStart != null) {
			return Math.max(0, newStart + context.offset());
		}
		return 0;
	}

	public void setParsedText(LogTextPane logTextPane) {
		Logging.debug(this, "setParsedText");
		this.lines = logTextPane.lines;
		this.parser = logTextPane.parser;

		this.showTypeRestricted = logTextPane.showTypeRestricted;
		this.selTypeIndex = logTextPane.selTypeIndex;
	}

	public int parse(String s) {
		Logging.info(this, "Setting text");
		lines = s.split("\n");

		parser = new LogFileParser(lines, logLevelStyles);
		parser.parse();

		if (lines.length > 1) {
			showLevel = adjustShowLevel(produceInitialMaxShowLevel(), parser.getData());
		} else {
			showLevel = 1;
		}

		return showLevel;
	}

	private static Integer adjustShowLevel(Integer initialLevel, LogFileParser.LogParsedData data) {
		int minLevel = data.getMinExistingLevel();
		int maxLevel = data.getMaxExistingLevel();

		if (maxLevel < initialLevel) {
			return maxLevel;
		} else if (minLevel > initialLevel) {
			return minLevel;
		} else {
			// Return initial level.
		}
		return initialLevel;
	}

	public void setShowLevel(int showLevel) {
		this.showLevel = showLevel;
	}

	public Integer produceInitialMaxShowLevel() {
		int savedMaxShownLogLevel = DEFAULT_MAX_SHOW_LEVEL;
		try {
			if (Configed.getSavedStates() != null) {
				savedMaxShownLogLevel = Integer.valueOf(Configed.getSavedStates().getProperty("savedMaxShownLogLevel"));
			}
		} catch (NumberFormatException ex) {
			Logging.warning(this, "savedMaxShownLogLevel could not be read, value ",
					Configed.getSavedStates().getProperty("savedMaxShownLogLevel"), ", fallback to ",
					DEFAULT_MAX_SHOW_LEVEL);
			Configed.getSavedStates().setProperty("savedMaxShownLogLevel", String.valueOf(DEFAULT_MAX_SHOW_LEVEL));
		}
		Logging.info(this, "produceInitialMaxShowLevel ", savedMaxShownLogLevel);
		return savedMaxShownLogLevel;
	}

	public LogParsedData getParsedData() {
		return parser.getData();
	}

	public void applyType(Object selectedType) {
		if (selectedType == null || selectedType.equals(DEFAULT_TYPE)) {
			showTypeRestricted = false;
			selTypeIndex = -1;
		} else {
			showTypeRestricted = true;
			selTypeIndex = parser.getData().getTypesList().indexOf(selectedType);
		}
	}

	public void search(String searchString) {
		requestFocus();
		setCaretPosition(getCaretPosition());
		searcher.setLastReturnedOffset(getCaretPosition());
		int offset = searcher.search(searchString);

		if (offset != -1) {
			try {
				scrollRectToVisible(modelToView2D(offset + searchString.length()).getBounds());
				setCaretPosition(offset);
				getCaret().setVisible(true);
				setCaretPosition(offset);
			} catch (BadLocationException e) {
				Logging.warning(this, e, "error with setting the caret in LogPane");
			}
		}
	}

	public void applyFontSize(int size) {
		Rectangle vis = getVisibleRect();
		Point topLeft = new Point(vis.x, vis.y);
		int topOffset = viewToModel2D(topLeft);

		displayFontSize = size;
		monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);
		setFont(monospacedFont);
		revalidate();

		if (topOffset >= 0) {
			restoreViewportPosition(topOffset, vis);
		}
	}

	private void restoreViewportPosition(int topOffset, Rectangle vis) {
		try {
			Rectangle r = modelToView2D(topOffset).getBounds();
			scrollRectToVisible(new Rectangle(r.x, r.y, vis.width, vis.height));
		} catch (BadLocationException e) {
			Logging.warning(this, e, "Failed to restore previous position in LogTextPane while chaning font size");
		}
	}

	private boolean showLine(LogFileParser.LogLine line) {
		boolean show = false;

		if (line.getLogLevel() <= showLevel) {
			show = true;
		}
		if (show && showTypeRestricted && line.getTypeIndex() != selTypeIndex) {
			show = false;
		}

		return show;
	}

	private void setLoglevelStyles() {
		logLevelStyles[1] = styleContext.addStyle("loglevel essential", null);
		StyleConstants.setForeground(logLevelStyles[1], Globals.LOG_COLOR_ESSENTIAL);

		logLevelStyles[2] = styleContext.addStyle("loglevel critical", null);
		StyleConstants.setForeground(logLevelStyles[2], Globals.LOG_COLOR_CRITICAL);

		logLevelStyles[3] = styleContext.addStyle("loglevel error", null);
		StyleConstants.setForeground(logLevelStyles[3], Globals.LOG_COLOR_ERROR);

		logLevelStyles[4] = styleContext.addStyle("loglevel warning", null);
		StyleConstants.setForeground(logLevelStyles[4], Globals.LOG_COLOR_WARNING);

		logLevelStyles[5] = styleContext.addStyle("loglevel notice", null);
		StyleConstants.setForeground(logLevelStyles[5], Globals.LOG_COLOR_NOTICE);

		logLevelStyles[6] = styleContext.addStyle("loglevel info", null);
		if (FlatLaf.isLafDark()) {
			StyleConstants.setForeground(logLevelStyles[6], Globals.LOG_COLOR_INFO_DARK);
		} else {
			StyleConstants.setForeground(logLevelStyles[6], Globals.LOG_COLOR_INFO_LIGHT);
		}

		logLevelStyles[7] = styleContext.addStyle("loglevel debug", null);
		if (FlatLaf.isLafDark()) {
			StyleConstants.setForeground(logLevelStyles[7], Globals.LOG_COLOR_DEBUG_DARK);
		} else {
			StyleConstants.setForeground(logLevelStyles[7], Globals.LOG_COLOR_DEBUG_LIGHT);
		}
		logLevelStyles[8] = styleContext.addStyle("loglevel debug2", null);
		StyleConstants.setForeground(logLevelStyles[8], Globals.LOG_COLOR_TRACE);

		logLevelStyles[9] = styleContext.addStyle("loglevel confidential", null);
		StyleConstants.setForeground(logLevelStyles[9], Globals.LOG_COLOR_SECRET);
	}

	// We create this class because the JTextPane should be editable only to show the caret position,
	// but then you should not be able to change anything in the Text...
	private static class ImmutableDefaultStyledDocument extends DefaultStyledDocument {
		ImmutableDefaultStyledDocument(StyleContext styles) {
			super(styles);
		}

		public void insertStringTruely(int offs, String str, AttributeSet a) throws BadLocationException {
			super.insertString(offs, str, a);
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			// Should be empty, because we don't want it to be able to be editable.
		}

		@Override
		public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			// Should be empty, because we don't want it to be able to be editable.
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {
			// Should be empty, because we don't want it to be able to be editable.
		}
	}
}
