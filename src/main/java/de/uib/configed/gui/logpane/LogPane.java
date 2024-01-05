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
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;
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
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.formdev.flatlaf.FlatLaf;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.GeneralFrame;
import de.uib.configed.gui.logpane.PartialDocumentBuilder.DocumentBuilderResult;
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
	private JScrollPane scrollpane;
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

	private int selTypeIndex = -1;

	private String title;
	private String info;

	private Integer displayFontSize = 11;
	private Font monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);

	private ImmutableDefaultStyledDocument document;

	protected String[] lines;

	private boolean scrolling;
	private int currentLineNumber;
	private int linesToShow;
	private boolean rebuilding;

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

		scrollpane = new JScrollPane();
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getVerticalScrollBar().addAdjustmentListener((AdjustmentEvent event) -> {
			if (scrolling && isAtBottom() && !event.getValueIsAdjusting()) {
				scrolling = false;
				scrollpane.getVerticalScrollBar().setUnitIncrement(0);
				buildDocument();
			}
		});
		scrollpane.addMouseWheelListener((MouseWheelEvent event) -> scrolling = true);
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

	private boolean isAtBottom() {
		int extent = scrollpane.getVerticalScrollBar().getVisibleAmount();
		int max = scrollpane.getVerticalScrollBar().getMaximum();
		int scrollPosition = scrollpane.getVerticalScrollBar().getValue();
		return scrollPosition + extent >= max;
	}

	private void setLayout() {
		JPanel commandpane = new JPanel();
		GroupLayout layoutCommandpane = new GroupLayout(commandpane);
		commandpane.setLayout(layoutCommandpane);

		layoutCommandpane.setHorizontalGroup(layoutCommandpane.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jComboBoxSearch, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(buttonSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jCheckBoxCaseSensitive, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
				.addComponent(buttonFontPlus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(buttonFontMinus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
				.addComponent(labelDisplayRestriction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(comboType, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2, Globals.GAP_SIZE * 2)
				.addComponent(labelLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(sliderLevel, SLIDER_W, SLIDER_W, SLIDER_W)
				.addGap(Globals.GAP_SIZE));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
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
			initDocument();
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
		case PopupMenuTrait.POPUP_SAVE_LOADED_AS_ZIP:
			saveAllAsZip(false);
			break;
		case PopupMenuTrait.POPUP_SAVE_ALL_AS_ZIP:
			saveAllAsZip(true);
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
		initDocument();
	}

	public int getCaretPosition() {
		return jTextPane.getCaretPosition();
	}

	public void setCaretPosition(int caretPosition) {
		jTextPane.setCaretPosition(caretPosition);
	}

	public String getFilenameFromTitle() {
		return title.replace(" ", "_").replace(".", "_") + ".log";
	}

	// We create this class because the JTextPane should be editable only to show the caret position,
	// but then you should not be able to change anything in the Text...
	public static class ImmutableDefaultStyledDocument extends DefaultStyledDocument {
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

	protected void saveAllAsZip(boolean loadMissingDocs) {
		Logging.debug(this, "save all as zip action, loadMissingDocs = " + loadMissingDocs);
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
		copyOfMe.getTextComponent().setCaretPosition(jTextPane.getCaretPosition());
		copyOfMe.adaptSlider();
		externalize(copyOfMe, title);
	}

	public void externalize(String title, Dimension size) {
		externalize(this, title, size);
	}

	public void externalize(LogPane logPane, String title) {
		externalize(logPane, title, this.getSize());
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

	private JTextComponent getTextComponent() {
		return jTextPane;
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

	private void initDocument() {
		initDocument(0);
	}

	private void initDocument(int lineNo) {
		document = new ImmutableDefaultStyledDocument(styleContext);
		jTextPane.setDocument(document);
		scrollpane.getVerticalScrollBar().setValue(0);
		currentLineNumber = 0;
		linesToShow = 0;
		resetViewportSize();
		buildDocument(lineNo);
	}

	private void resetViewportSize() {
		scrollpane.getViewport().setPreferredSize(jTextPane.getPreferredSize());
		scrollpane.revalidate();
		scrollpane.repaint();
	}

	private void buildDocument() {
		buildDocument(0);
	}

	private void buildDocument(int lineNo) {
		if (rebuilding) {
			return;
		}
		rebuilding = true;
		Logging.debug(this, "building document");
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> {
				setCursor(Globals.WAIT_CURSOR);
				scrollpane.setCursor(Globals.WAIT_CURSOR);
				jTextPane.setCursor(Globals.WAIT_CURSOR);
			});
		} else {
			setCursor(Globals.WAIT_CURSOR);
			scrollpane.setCursor(Globals.WAIT_CURSOR);
			jTextPane.setCursor(Globals.WAIT_CURSOR);
		}
		linesToShow += (int) scrollpane.getViewport().getVisibleRect().getHeight();
		PartialDocumentBuilder builder = new PartialDocumentBuilder(document, showTypeRestricted, selTypeIndex,
				sliderLevel.getValue(), parser, linesToShow, lineNo, currentLineNumber);
		builder.setOnDocumentBuilt(this::onDocumentBuilt);
		builder.execute();
	}

	private void onDocumentBuilt(DocumentBuilderResult result) {
		currentLineNumber = result.getLineIndex();
		jTextPane.setDocument(result.getDocument());
		this.document = result.getDocument();
		if (result.getLineStartPosition() > 0) {
			jTextPane.setCaretPosition(result.getLineStartPosition() + 1);
			jTextPane.getCaret().setVisible(true);
			highlighter.removeAllHighlights();
		}
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		setCursor(null);
		jTextPane.setCursor(null);
		scrollpane.setCursor(null);
		rebuilding = false;
		docLinestartPosition2lineCount = new TreeMap<>(result.getDocLinestartPosition2lineCount());
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
		Iterator<Integer> linestartIterator = docLinestartPosition2lineCount.keySet().iterator();

		while (startPosition < caretPosition && linestartIterator.hasNext()) {
			oldStartPosition = startPosition;
			startPosition = linestartIterator.next();
		}

		int lineNo = 0;
		if (docLinestartPosition2lineCount.get(oldStartPosition) != null) {
			lineNo = docLinestartPosition2lineCount.get(oldStartPosition);
		}

		initDocument(lineNo);
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
			}
			adaptSlider();
		} else {
			showLevel = 1;
			sliderLevel.produceLabels();
		}

		sliderLevel.setValue(showLevel);
		initDocument();
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
		initDocument();
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

	private void editSearchString() {
		jComboBoxSearch.requestFocus();
	}

	private void search() {
		Logging.debug(this, "Searching string in log");

		if (jComboBoxSearch.getSelectedItem() == null || jComboBoxSearch.getSelectedItem().toString().isEmpty()) {
			Logging.info(this, "item to search for is null or empty, do nothing");
			return;
		}

		jTextPane.requestFocus();
		jTextPane.setCaretPosition(jTextPane.getCaretPosition());
		searcher.setFullContent(String.join(",", lines));
		searcher.setLastReturnedOffset(jTextPane.getCaretPosition());
		if (searcher.isLastShownElement() && searcher.getCurrentMatch() != searcher.getTotalMatches()
				&& currentLineNumber != lines.length) {
			scrollpane.getVerticalScrollBar().setUnitIncrement(0);
			buildDocument();
		}

		searcher.setHasReachedEnd(currentLineNumber == lines.length);
		int offset = searcher.search(jComboBoxSearch.getSelectedItem().toString());
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

		if (jComboBoxSearch.getSelectedIndex() <= -1) {
			jComboBoxSearch.addItem(jComboBoxSearch.getSelectedItem().toString());
			jComboBoxSearch.repaint();
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
			} else if (e.getSource() == jTextPane && e.getKeyCode() == KeyEvent.VK_PLUS && e.isControlDown()) {
				increaseFontSize();
			} else if (e.getSource() == jTextPane && e.getKeyCode() == KeyEvent.VK_MINUS && e.isControlDown()) {
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
				editSearchString();
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
