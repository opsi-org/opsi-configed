/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.formdev.flatlaf.FlatLaf;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;
import utils.Utils;

public class LogPane extends JPanel implements KeyListener {
	public static final int DEFAULT_MAX_SHOW_LEVEL = 4;

	private static final int DEFAULT_WIDTH = 1212;
	private static final int DEFAULT_HEIGHT = 511;

	private static final int SLIDER_H = 35;
	private static final int SLIDER_W = 180;
	private static final String DEFAULT_TYPE = "(all)";

	private static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 9;

	private JTextPane jTextPane;
	private JLabel labelSearch;

	private JComboBox<String> jComboBoxSearch;

	private JButton buttonSearch;
	private JCheckBox jCheckBoxCaseSensitive;
	private JButton buttonFontPlus;
	private JButton buttonFontMinus;
	private JLabel labelLevel;
	private AdaptingSlider sliderLevel;
	private JLabel labelDisplayRestriction;
	private JComboBox<String> comboType;
	private DefaultComboBoxModel<String> comboModelTypes;

	private DocumentSearcher searcher;
	private Highlighter highlighter;
	private final StyleContext styleContext;
	private final Style[] logLevelStyles;

	private Integer minLevel = MIN_LEVEL;
	private Integer showLevel = minLevel;

	private boolean showTypeRestricted;
	private int typesListMaxShowCount = 25;

	private TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	private TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	private int selTypeIndex = -1;

	private String title;
	private String info;

	private Integer displayFontSize = 11;
	private Font monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);

	private ImmutableDefaultStyledDocument document;

	protected String[] lines;

	private LogFileParser parser;

	public LogPane(String defaultText, boolean withPopup) {
		super(new BorderLayout());

		Logging.info(this.getClass(), "initializing");
		title = "";
		info = "";

		styleContext = new StyleContext() {
			@Override
			public Font getFont(AttributeSet attr) {
				return monospacedFont;
			}
		};

		logLevelStyles = new Style[10];
		setLoglevelStyles();
		parser = new LogFileParser(lines, logLevelStyles);

		initComponents(defaultText);

		setLayout();

		if (withPopup) {
			initPopupMenu();
		}
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

	private void initComponents(String defaultText) {
		jTextPane = new JTextPane() {
			@Override
			public Dimension getPreferredSize() {
				return getUI().getMinimumSize(this);
			}
		};
		jTextPane.setCaretColor(Globals.LOG_PANE_CARET_COLOR);
		jTextPane.getCaret().setBlinkRate(0);

		searcher = new DocumentSearcher(jTextPane);
		searcher.setCaseSensitivity(false);
		highlighter = new DefaultHighlighter();
		jTextPane.setHighlighter(highlighter);

		if (defaultText != null) {
			jTextPane.setText(defaultText);
		}

		jTextPane.setEditable(true);
		jTextPane.addKeyListener(this);

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getViewport().add(jTextPane);
		super.add(scrollpane, BorderLayout.CENTER);

		labelSearch = new JLabel(Configed.getResourceValue("TextPane.jLabel_search"));

		jComboBoxSearch = new JComboBox<>();
		jComboBoxSearch.setToolTipText(Configed.getResourceValue("TextPane.jComboBoxSearch.toolTip"));
		jComboBoxSearch.setEditable(true);
		jComboBoxSearch.addActionListener((ActionEvent event) -> {
			search();
			jTextPane.requestFocusInWindow();
		});

		buttonSearch = new JButton(Configed.getResourceValue("TextPane.jButton_search"));

		buttonSearch.addActionListener(event -> search());
		buttonSearch.addKeyListener(this);

		jCheckBoxCaseSensitive = new JCheckBox(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive"));
		jCheckBoxCaseSensitive.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		jCheckBoxCaseSensitive.setSelected(false);
		jCheckBoxCaseSensitive
				.addActionListener(event -> searcher.setCaseSensitivity(jCheckBoxCaseSensitive.isSelected()));

		buttonFontPlus = new JButton(Utils.createImageIcon("images/font-plus.png", ""));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("LogPane.fontPlus"));
		buttonFontPlus.addActionListener(event -> increaseFontSize());

		buttonFontMinus = new JButton(Utils.createImageIcon("images/font-minus.png", ""));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("LogPane.fontMinus"));
		buttonFontMinus.addActionListener(event -> reduceFontSize());

		labelLevel = new JLabel(Configed.getResourceValue("TextPane.jLabel_level"));

		Logging.info(this, "levels minL, maxL " + MIN_LEVEL + ", " + MAX_LEVEL);

		sliderLevel = new AdaptingSlider(this, MIN_LEVEL, MAX_LEVEL, produceInitialMaxShowLevel());

		labelDisplayRestriction = new JLabel(Configed.getResourceValue("TextPane.EventType"));

		comboModelTypes = new DefaultComboBoxModel<>();
		comboType = new JComboBox<>(comboModelTypes);

		comboType.setEnabled(false);
		comboType.setEditable(false);

		comboType.addActionListener(actionEvent -> applyType());
	}

	private void setLayout() {
		JPanel commandpane = new JPanel();
		GroupLayout layoutCommandpane = new GroupLayout(commandpane);
		commandpane.setLayout(layoutCommandpane);

		layoutCommandpane.setHorizontalGroup(
				layoutCommandpane.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(jComboBoxSearch, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(buttonSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(jCheckBoxCaseSensitive, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
						.addComponent(buttonFontPlus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
						.addComponent(buttonFontMinus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
						.addComponent(labelDisplayRestriction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(comboType, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
						.addComponent(labelLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(sliderLevel, SLIDER_W, SLIDER_W, SLIDER_W)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layoutCommandpane.createParallelGroup(Alignment.CENTER)
						.addComponent(labelSearch, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jComboBoxSearch, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonSearch, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jCheckBoxCaseSensitive, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(buttonFontPlus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonFontMinus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelDisplayRestriction, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(comboType, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(labelLevel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(sliderLevel, SLIDER_H, SLIDER_H, SLIDER_H)

				).addGap(Globals.MIN_GAP_SIZE));

		super.add(commandpane, BorderLayout.SOUTH);
	}

	private void applyType() {
		int oldSelTypeIndex = selTypeIndex;
		Object selType = comboType.getSelectedItem();
		if (selType == null || selType.equals(DEFAULT_TYPE)) {
			showTypeRestricted = false;
			selTypeIndex = -1;
		} else {
			showTypeRestricted = true;
			selTypeIndex = parser.getTypesList().indexOf(selType);
		}

		if (selTypeIndex != oldSelTypeIndex) {
			buildDocument();
			highlighter.removeAllHighlights();
		}
	}

	private void initPopupMenu() {
		Integer[] popups;

		if (Main.isLogviewer()) {
			popups = new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_SAVE,
					PopupMenuTrait.POPUP_FLOATINGCOPY };
		} else {
			popups = new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_SAVE,
					PopupMenuTrait.POPUP_SAVE_AS_ZIP, PopupMenuTrait.POPUP_SAVE_ALL_AS_ZIP,
					PopupMenuTrait.POPUP_FLOATINGCOPY };
		}

		PopupMenuTrait popupMenu = new PopupMenuTrait(popups) {
			@Override
			public void action(int p) {
				treatPopupAction(p);
			}
		};

		popupMenu.addPopupListenersTo(new JComponent[] { jTextPane });
	}

	private void treatPopupAction(int p) {
		switch (p) {
		case PopupMenuTrait.POPUP_RELOAD:
			reload();
			break;

		case PopupMenuTrait.POPUP_SAVE:
			save();
			break;
		case PopupMenuTrait.POPUP_SAVE_AS_ZIP:
			saveAsZip();
			break;
		case PopupMenuTrait.POPUP_SAVE_ALL_AS_ZIP:
			saveAllAsZip();
			break;
		case PopupMenuTrait.POPUP_FLOATINGCOPY:
			floatExternal();
			break;

		default:
			Logging.warning(this, "no case found for popupMenuTrait in LogPane");
			break;
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
		buildDocument();
	}

	public int getCaretPosition() {
		return jTextPane.getCaretPosition();
	}

	public void setCaretPosition(int caretPosition) {
		jTextPane.setCaretPosition(caretPosition);
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

	public void reload() {
		Logging.info(this, "reload action");
		setLevelWithoutAction(produceInitialMaxShowLevel());
	}

	public void save() {
		Logging.debug(this, "save action");
	}

	protected void saveAsZip() {
		Logging.debug(this, "save as zip action");
	}

	protected void saveAllAsZip() {
		Logging.debug(this, "save all as zip action");
	}

	public void floatExternal() {
		if (document == null) {
			return;
		}
		// set text did not run
		// probably we already are in a floating instance

		LogPane copyOfMe = new LogPane("", false);
		copyOfMe.setLevelWithoutAction(showLevel);
		copyOfMe.setParsedText(lines, showTypeRestricted, selTypeIndex, parser);
		copyOfMe.jTextPane.setCaretPosition(jTextPane.getCaretPosition());
		copyOfMe.adaptSlider();
		externalize(copyOfMe, title, this.getSize());
	}

	public void externalize(String title, Dimension size) {
		externalize(this, title, size);
	}

	public void externalize(LogPane logPane, String title, Dimension size) {
		GeneralFrame externalView = new GeneralFrame(null, title, false);
		externalView.addPanel(logPane);
		if (size.equals(new Dimension(0, 0))) {
			externalView.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		} else {
			externalView.setSize(size);
		}
		externalView.setLocationRelativeTo(Main.getMainFrame());
		externalView.setVisible(true);
	}

	public void setTitle(String s) {
		title = s;
	}

	public void setInfo(String s) {
		info = s;
	}

	public String getInfo() {
		return info;
	}

	private Integer produceInitialMaxShowLevel() {
		int savedMaxShownLogLevel = DEFAULT_MAX_SHOW_LEVEL;
		try {
			if (Configed.getSavedStates() != null) {
				savedMaxShownLogLevel = Integer.valueOf(Configed.getSavedStates().getProperty("savedMaxShownLogLevel"));
			}
		} catch (NumberFormatException ex) {
			Logging.warning(this,
					"savedMaxShownLogLevel could not be read, value "
							+ Configed.getSavedStates().getProperty("savedMaxShownLogLevel") + ", fallback to "
							+ DEFAULT_MAX_SHOW_LEVEL);
			Configed.getSavedStates().setProperty("savedMaxShownLogLevel", String.valueOf(DEFAULT_MAX_SHOW_LEVEL));
		}
		Logging.info(this, "produceInitialMaxShowLevel " + savedMaxShownLogLevel);
		return savedMaxShownLogLevel;
	}

	private void buildDocument() {
		Logging.debug(this, "building document");
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(Globals.WAIT_CURSOR));
		} else {
			setCursor(Globals.WAIT_CURSOR);
		}
		// Switch to an blank document temporarily to avoid repaints

		document = new ImmutableDefaultStyledDocument(styleContext);

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			List<LogLine> logLines = parser.getParsedLogLines();
			for (int i = 0; i < logLines.size(); i++) {
				LogLine line = parser.getParsedLogLine(i);
				if (showLine(line)) {
					docLinestartPosition2lineCount.put(document.getLength(), i);
					lineCount2docLinestartPosition.put(i, document.getLength());

					String lineNumberRepresentation = "(" + line.getLineNumber() + ")";
					document.insertStringTruely(document.getLength(),
							String.format("%-10s", lineNumberRepresentation) + line.getText() + '\n', line.getStyle());
				}
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "BadLocationException thrown in logging: " + e);
		}
		jTextPane.setDocument(document);
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> setCursor(null));
		} else {
			setCursor(null);
		}
	}

	private boolean showLine(LogLine line) {
		boolean show = false;
		if (line.getLogLevel() <= sliderLevel.getValue()) {
			show = true;
		}
		if (show && showTypeRestricted && line.getTypeIndex() != selTypeIndex) {
			show = false;
		}
		return show;
	}

	private void setLevelWithoutAction(Object l) {
		Logging.debug(this, "setLevel " + l);

		Integer levelO = sliderLevel.getValue();
		if (levelO != l) {
			sliderLevel.removeChangeListener(sliderLevel);
			sliderLevel.setValue((Integer) l);
			sliderLevel.addChangeListener(sliderLevel);
		}
	}

	public void activateShowLevel() {
		Integer level = sliderLevel.getValue();
		if (level > parser.getMaxExistingLevel()) {
			level = parser.getMaxExistingLevel();
			sliderLevel.setValue(level);
			return;
		}

		if (Configed.getSavedStates() != null) {
			Configed.getSavedStates().setProperty("savedMaxShownLogLevel", String.valueOf(level));
		}

		Integer oldLevel = showLevel;
		showLevel = level;
		Logging.info(this, "activateShowLevel level, oldLevel, maxExistingLevel " + level + " , " + oldLevel + ", "
				+ parser.getMaxExistingLevel());

		if (!oldLevel.equals(level)
				&& (level < parser.getMaxExistingLevel() || oldLevel < parser.getMaxExistingLevel())) {
			rebuildDocumentWithNewLevel();
		}
	}

	private void rebuildDocumentWithNewLevel() {
		int caretPosition = jTextPane.getCaretPosition();
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

		jTextPane.setCaretPosition(startPosition);

		if (jComboBoxSearch.getSelectedIndex() != -1) {
			try {
				jTextPane.scrollRectToVisible(jTextPane
						.modelToView2D(offset + jComboBoxSearch.getSelectedItem().toString().length()).getBounds());
				highlighter.removeAllHighlights();
			} catch (BadLocationException e) {
				Logging.warning(this, "BadLocationException for setting caret in LotPane: " + e);
			}
		}

		jTextPane.getCaret().setVisible(true);
	}

	private void adaptComboType() {
		comboType.setEnabled(false);
		comboModelTypes.removeAllElements();

		if (!parser.getTypesList().isEmpty()) {
			comboModelTypes.addElement(DEFAULT_TYPE);
			for (String type : parser.getTypesList()) {
				comboModelTypes.addElement(type);
			}
			comboType.setEnabled(true);

			int maxRowCount = parser.getTypesList().size() + 1;
			if (maxRowCount > typesListMaxShowCount) {
				maxRowCount = typesListMaxShowCount;
			}

			comboType.setMaximumRowCount(maxRowCount);
		}
	}

	private void adaptSlider() {
		sliderLevel.produceLabels();
	}

	public void setText(String s) {
		if (s == null) {
			Logging.warning(this, "String in setting text is null");
			return;
		}

		Logging.info(this, "Setting text");
		lines = s.split("\n");

		parser = new LogFileParser(lines, logLevelStyles);
		parser.parse();
		adaptComboType();
		if (lines.length > 1) {
			showLevel = produceInitialMaxShowLevel();
			if (parser.getMaxExistingLevel() < showLevel) {
				showLevel = parser.getMaxExistingLevel();
			} else if (parser.getMinExistingLevel() > showLevel) {
				showLevel = parser.getMinExistingLevel();
			} else {
				// Otherwise keep initially produced max level.
			}
			adaptSlider();
		} else {
			showLevel = 1;
			sliderLevel.produceLabels();
		}

		sliderLevel.setValue(showLevel);
		buildDocument();
		jTextPane.setCaretPosition(0);
		jTextPane.getCaret().setVisible(true);
	}

	private void setParsedText(final String[] lines, boolean showTypeRestricted, int selTypeIndex,
			LogFileParser parser) {
		Logging.debug(this, "setParsedText");
		this.lines = lines;
		this.parser = parser;
		adaptComboType();
		this.showTypeRestricted = showTypeRestricted;
		this.selTypeIndex = selTypeIndex;
		buildDocument();
	}

	private void search() {
		Logging.debug(this, "Searching string in log");

		if (jComboBoxSearch.getSelectedItem() == null || jComboBoxSearch.getSelectedItem().toString().isEmpty()) {
			Logging.info(this, "item to search for is null or empty, do nothing");
			return;
		}

		jTextPane.requestFocus();
		jTextPane.setCaretPosition(jTextPane.getCaretPosition());
		searcher.setLastReturnedOffset(jTextPane.getCaretPosition());
		int offset = searcher.search(jComboBoxSearch.getSelectedItem().toString());

		// does not exist
		if (jComboBoxSearch.getSelectedIndex() <= -1) {
			jComboBoxSearch.addItem(jComboBoxSearch.getSelectedItem().toString());
			jComboBoxSearch.repaint();
		}
		if (offset != -1) {
			try {
				jTextPane.scrollRectToVisible(jTextPane
						.modelToView2D(offset + jComboBoxSearch.getSelectedItem().toString().length()).getBounds());
				jTextPane.setCaretPosition(offset);
				jTextPane.getCaret().setVisible(true);
				jTextPane.setCaretPosition(offset);
			} catch (BadLocationException e) {
				Logging.warning(this, "error with setting the caret in LogPane: " + e);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, "KeyEvent " + e);

		if (e.getSource() == buttonSearch) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				search();
			}
		} else if (e.getSource() == jComboBoxSearch || e.getSource() == jTextPane) {
			if (e.getKeyCode() == KeyEvent.VK_F3 || e.getKeyCode() == KeyEvent.VK_ENTER) {
				search();
			} else if (e.getSource() == jTextPane && e.getKeyCode() == KeyEvent.VK_PLUS
					&& (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				Logging.info(this, "Ctrl-Plus");
				increaseFontSize();
			} else if (e.getSource() == jTextPane && e.getKeyCode() == KeyEvent.VK_MINUS
					&& (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				Logging.info(this, "Ctrl-Minus");
				reduceFontSize();
			} else {
				// Do nothing on other keys on jComboBoxSearch and jTextPane
			}
		} else {
			Logging.warning(this, "unexpected keyevent on source " + e.getSource());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == jTextPane) {
			if (e.getKeyChar() == '/' || e.getKeyChar() == '\u0006') {
				jComboBoxSearch.requestFocus();
			}

			if (e.getKeyChar() == 'n' || e.getKeyChar() == '\u000c' || e.getKeyCode() == KeyEvent.VK_F3) {
				search();
			}
			e.consume();
		}
	}

	public String[] getLines() {
		return lines;
	}
}
