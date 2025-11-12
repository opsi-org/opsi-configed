/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

public class LogTextPane extends JTextPane {
	public static final String DEFAULT_TYPE = "(all)";
	private static final int DEFAULT_MAX_SHOW_LEVEL = 4;

	private DocumentSearcher searcher;
	private Highlighter highlighter;

	private TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	private TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	private Integer showLevel = LogPanel.MIN_LEVEL;

	private Integer displayFontSize = 11;

	private String[] lines;

	private final StyleContext styleContext;
	private final Style[] logLevelStyles;

	private int selTypeIndex = -1;
	private boolean showTypeRestricted;

	private LogFileParser parser;

	private Font monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);

	public LogTextPane(String defaultText) {
		styleContext = new StyleContext() {
			@Override
			public Font getFont(AttributeSet attr) {
				return monospacedFont;
			}
		};
		setEditorKit(new WrapEditorKit());

		logLevelStyles = new Style[10];
		setLoglevelStyles();
		parser = new LogFileParser(lines, logLevelStyles);

		super.setCaretColor(Globals.LOG_PANE_CARET_COLOR);

		searcher = new DocumentSearcher(this);
		highlighter = new DefaultHighlighter();
		super.setHighlighter(highlighter);

		super.setEditable(true);
	}

	@Override
	public Dimension getPreferredSize() {
		return getUI().getMinimumSize(this);
	}

	public void setCaseSensitivity(boolean caseSensitivity) {
		searcher.setCaseSensitivity(caseSensitivity);
	}

	public String[] getLines() {
		return lines;
	}

	public void rebuildDocumentWithNewLevel(Object selectedItem) {
		int caretPosition = getCaretPosition();
		int startPosition = 0;
		int oldStartPosition = 0;
		int offset = 0;
		Iterator<Integer> linestartIterator = docLinestartPosition2lineCount.keySet().iterator();

		while (startPosition < caretPosition && linestartIterator.hasNext()) {
			offset = caretPosition - startPosition;
			oldStartPosition = startPosition;
			startPosition = linestartIterator.next();
		}

		int lineNo = 0;
		if (docLinestartPosition2lineCount.get(oldStartPosition) != null) {
			lineNo = docLinestartPosition2lineCount.get(oldStartPosition);
		}

		buildDocument();

		if (lineCount2docLinestartPosition.containsKey(lineNo)) {
			startPosition = lineCount2docLinestartPosition.get(lineNo) + offset;
		} else if (!lineCount2docLinestartPosition.isEmpty()) {
			Iterator<Integer> linesIterator = lineCount2docLinestartPosition.keySet().iterator();
			int nextLineNo = linesIterator.next();

			while (linesIterator.hasNext() && nextLineNo < lineNo) {
				nextLineNo = linesIterator.next();
			}

			startPosition = lineCount2docLinestartPosition.get(nextLineNo) + offset;
		} else {
			Logging.notice(this, "lineCount2docLinestartPosition is empty, so there will be no lines");
		}

		setCaretPosition(startPosition);

		if (selectedItem != null) {
			try {
				scrollRectToVisible(modelToView2D(offset + selectedItem.toString().length()).getBounds());
				highlighter.removeAllHighlights();
			} catch (BadLocationException e) {
				Logging.warning(this, e, "BadLocationException for setting caret in LotPane");
			}
		}

		getCaret().setVisible(true);
	}

	public void buildDocument() {
		Logging.debug(this, "building document");
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(Globals.WAIT_CURSOR));
		} else {
			setCursor(Globals.WAIT_CURSOR);
		}
		// Switch to an blank document temporarily to avoid repaints

		ImmutableDefaultStyledDocument document = new ImmutableDefaultStyledDocument(styleContext);

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			List<LogLine> logLines = parser.getParsedLogLines();
			for (int i = 0; i < logLines.size(); i++) {
				LogLine line = parser.getParsedLogLine(i);
				if (showLine(line)) {
					docLinestartPosition2lineCount.put(document.getLength(), i);
					lineCount2docLinestartPosition.put(i, document.getLength());
					document.insertStringTruely(document.getLength(), line.getText() + '\n', line.getStyle());
				}
			}
		} catch (BadLocationException e) {
			Logging.warning(this, e, "BadLocationException thrown in logging");
		}

		setDocument(document);
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(null));
		} else {
			setCursor(null);
		}
	}

	public void setParsedText(LogTextPane logTextPane) {
		Logging.debug(this, "setParsedText");
		this.lines = logTextPane.lines;
		this.parser = logTextPane.parser;

		this.showTypeRestricted = logTextPane.showTypeRestricted;
		this.selTypeIndex = logTextPane.selTypeIndex;
	}

	public int setLogText(String s) {
		Logging.info(this, "Setting text");
		lines = s.split("\n");

		parser = new LogFileParser(lines, logLevelStyles);
		parser.parse();
		if (lines.length > 1) {
			showLevel = produceInitialMaxShowLevel();
			if (parser.getMaxExistingLevel() < showLevel) {
				showLevel = parser.getMaxExistingLevel();
			} else if (parser.getMinExistingLevel() > showLevel) {
				showLevel = parser.getMinExistingLevel();
			} else {
				// Otherwise keep initially produced max level.
			}
		} else {
			showLevel = 1;
		}

		return showLevel;
	}

	public Integer getShowLevel() {
		return showLevel;
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

	public List<String> getTypesList() {
		return parser.getTypesList();
	}

	public void applyType(Object selectedType) {
		int oldSelTypeIndex = selTypeIndex;
		if (selectedType == null || selectedType.equals(DEFAULT_TYPE)) {
			showTypeRestricted = false;
			selTypeIndex = -1;
		} else {
			showTypeRestricted = true;
			selTypeIndex = parser.getTypesList().indexOf(selectedType);
		}

		if (selTypeIndex != oldSelTypeIndex) {
			buildDocument();
			highlighter.removeAllHighlights();
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

	public Integer getMaxExistingLevel() {
		return parser.getMaxExistingLevel();
	}

	public void removeAllHighlights() {
		highlighter.removeAllHighlights();
		applyFontSize();
	}

	public void reduceFontSize() {
		if (displayFontSize > 10) {
			displayFontSize = (int) ((displayFontSize + 1) / 1.1);
			applyFontSize();
		}
	}

	public void increaseFontSize() {
		displayFontSize = (int) (displayFontSize * 1.1);
		applyFontSize();
	}

	private void applyFontSize() {
		monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);
		setFont(monospacedFont);
	}

	private boolean showLine(LogLine line) {
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

	public static class WrapEditorKit extends StyledEditorKit {
		private transient ViewFactory defaultFactory;

		@Override
		public ViewFactory getViewFactory() {
			if (defaultFactory == null) {
				defaultFactory = new WrapColumnFactory();
			}
			return defaultFactory;
		}
	}

	public static class WrapColumnFactory implements ViewFactory {
		@Override
		public View create(Element elem) {
			String kind = elem.getName();
			if (kind != null) {
				return switch (kind) {
				case AbstractDocument.ContentElementName -> new WrapLabelView(elem);
				case AbstractDocument.ParagraphElementName -> new ParagraphView(elem);
				case AbstractDocument.SectionElementName -> new BoxView(elem, View.Y_AXIS);
				case StyleConstants.ComponentElementName -> new ComponentView(elem);
				case StyleConstants.IconElementName -> new IconView(elem);
				default -> new LabelView(elem);
				};
			}
			return new LabelView(elem);
		}
	}

	public static class WrapLabelView extends LabelView {
		public WrapLabelView(Element elem) {
			super(elem);
		}

		@Override
		public float getMinimumSpan(int axis) {
			if (axis == View.X_AXIS) {
				return 0; // allow wrapping
			}
			return super.getMinimumSpan(axis);
		}
	}
}
