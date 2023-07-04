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
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
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
import javax.swing.text.JTextComponent;
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

public class LogPane extends JPanel implements KeyListener, ActionListener {
	public static final int DEFAULT_MAX_SHOW_LEVEL = 3;

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

	private WordSearcher searcher;
	private Highlighter highlighter;
	private final StyleContext styleContext;
	private final Style[] logLevelStyles;

	private Integer minLevel = MIN_LEVEL;
	private Integer maxExistingLevel = minLevel;
	private Integer showLevel = minLevel;

	private boolean showTypeRestricted;
	private List<String> typesList;
	private int typesListMaxShowCount = 25;

	private int[] lineTypes;

	private TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	private TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	private int selTypeIndex = -1;

	private String title;
	private String info;

	private Integer displayFontSize = 11;
	private Font monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);

	private ImmutableDefaultStyledDocument document;

	public String[] lines;
	private int[] lineLevels;
	private Style[] lineStyles;

	public LogPane(String defaultText, boolean withPopup) {
		super(new BorderLayout());

		// Set variables
		Logging.info(this, "initializing");
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

		initComponents(defaultText);

		setLayout();

		if (withPopup) {
			initPopupMenu();
		}
	}

	private void setLoglevelStyles() {

		logLevelStyles[1] = styleContext.addStyle("loglevel essential", null);
		StyleConstants.setForeground(logLevelStyles[1], Globals.logColorEssential);

		logLevelStyles[2] = styleContext.addStyle("loglevel critical", null);
		StyleConstants.setForeground(logLevelStyles[2], Globals.logColorCritical);

		logLevelStyles[3] = styleContext.addStyle("loglevel error", null);
		StyleConstants.setForeground(logLevelStyles[3], Globals.logColorError);

		logLevelStyles[4] = styleContext.addStyle("loglevel warning", null);
		StyleConstants.setForeground(logLevelStyles[4], Globals.logColorWarning);

		logLevelStyles[5] = styleContext.addStyle("loglevel notice", null);
		StyleConstants.setForeground(logLevelStyles[5], Globals.logColorNotice);

		logLevelStyles[6] = styleContext.addStyle("loglevel info", null);
		if (FlatLaf.isLafDark()) {
			StyleConstants.setForeground(logLevelStyles[6], Globals.logColorInfoDark);
		} else {
			StyleConstants.setForeground(logLevelStyles[6], Globals.logColorInfoLight);
		}

		logLevelStyles[7] = styleContext.addStyle("loglevel debug", null);
		if (FlatLaf.isLafDark()) {
			StyleConstants.setForeground(logLevelStyles[7], Globals.logColorDebugDark);
		} else {
			StyleConstants.setForeground(logLevelStyles[7], Globals.logColorDebugLight);
		}
		logLevelStyles[8] = styleContext.addStyle("loglevel debug2", null);
		StyleConstants.setForeground(logLevelStyles[8], Globals.logColorTrace);

		logLevelStyles[9] = styleContext.addStyle("loglevel confidential", null);
		StyleConstants.setForeground(logLevelStyles[9], Globals.logColorSecret);
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

		searcher = new WordSearcher(jTextPane);
		searcher.setCaseSensitivity(false);
		highlighter = new DefaultHighlighter();
		jTextPane.setHighlighter(highlighter);

		if (defaultText != null) {
			jTextPane.setText(defaultText);
		}

		jTextPane.setOpaque(true);
		if (!Main.THEMES) {
			jTextPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		jTextPane.setEditable(true);
		if (!Main.FONT) {
			jTextPane.setFont(Globals.defaultFont);
		}

		jTextPane.addKeyListener(this);

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getViewport().add(jTextPane);
		super.add(scrollpane, BorderLayout.CENTER);

		labelSearch = new JLabel(Configed.getResourceValue("TextPane.jLabel_search"));
		if (!Main.FONT) {
			labelSearch.setFont(Globals.defaultFont);
		}

		jComboBoxSearch = new JComboBox<>();
		jComboBoxSearch.setEditable(true);
		jComboBoxSearch.addActionListener(this);

		buttonSearch = new JButton(Configed.getResourceValue("TextPane.jButton_search"));
		if (!Main.FONT) {
			buttonSearch.setFont(Globals.defaultFont);
		}
		buttonSearch.addActionListener(this);
		buttonSearch.addKeyListener(this);

		jCheckBoxCaseSensitive = new JCheckBox(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive"));
		jCheckBoxCaseSensitive.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		jCheckBoxCaseSensitive.setSelected(false);
		jCheckBoxCaseSensitive.addActionListener(this);

		buttonFontPlus = new JButton(Globals.createImageIcon("images/font-plus.png", ""));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("LogPane.fontPlus"));
		buttonFontPlus.addActionListener(this);

		buttonFontMinus = new JButton(Globals.createImageIcon("images/font-minus.png", ""));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("LogPane.fontMinus"));
		buttonFontMinus.addActionListener(this);

		labelLevel = new JLabel(Configed.getResourceValue("TextPane.jLabel_level"));
		if (!Main.FONT) {
			labelLevel.setFont(Globals.defaultFont);
		}

		Logging.info(this, "levels minL, maxL " + MIN_LEVEL + ", " + MAX_LEVEL);

		sliderLevel = new AdaptingSlider(this, MIN_LEVEL, MAX_LEVEL, produceInitialMaxShowLevel());

		labelDisplayRestriction = new JLabel(Configed.getResourceValue("TextPane.EventType"));
		if (!Main.FONT) {
			labelDisplayRestriction.setFont(Globals.defaultFont);
		}

		comboModelTypes = new DefaultComboBoxModel<>();
		comboType = new JComboBox<>(comboModelTypes);
		if (!Main.FONT) {
			comboType.setFont(Globals.defaultFont);
		}
		comboType.setEnabled(false);
		comboType.setEditable(false);

		comboType.addActionListener(actionEvent -> applyType());
	}

	private void setLayout() {
		JPanel commandpane = new JPanel();
		GroupLayout layoutCommandpane = new GroupLayout(commandpane);
		commandpane.setLayout(layoutCommandpane);

		layoutCommandpane.setHorizontalGroup(layoutCommandpane.createParallelGroup(Alignment.LEADING)
				.addGroup(layoutCommandpane.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(jComboBoxSearch, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(buttonSearch, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH / 2,
								Globals.BUTTON_WIDTH / 2)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(jCheckBoxCaseSensitive, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH)

						.addComponent(buttonFontPlus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonFontMinus, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(labelDisplayRestriction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(comboType, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(labelLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(sliderLevel, SLIDER_W, SLIDER_W, SLIDER_W)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
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

				).addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

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
			selTypeIndex = typesList.indexOf(selType);
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
		return maxExistingLevel;
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

	public String getFilenameFromTitle() {
		return title.replace(" ", "_").replace(".", "_") + ".log";
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
			/* Should be empty, because we don't want it to be able to be editable*/
		}

		@Override
		public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			/* Should be empty, because we don't want it to be able to be editable*/
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {
			/* Should be empty, because we don't want it to be able to be editable*/
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
		Logging.debug(this, "save all as zip action");
	}

	public void floatExternal() {
		if (document == null) {
			return;
		}
		// set text did not run
		// probably we already are in a floating instance

		LogPane copyOfMe;
		GeneralFrame externalView;

		copyOfMe = new LogPane("", false);

		externalView = new GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(Main.getMainFrame());

		copyOfMe.setLevelWithoutAction(showLevel);
		copyOfMe.setParsedText(lines, lineLevels, lineStyles, lineTypes, typesList, showTypeRestricted, selTypeIndex,
				maxExistingLevel);
		copyOfMe.getTextComponent().setCaretPosition(jTextPane.getCaretPosition());
		copyOfMe.adaptSlider();

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
		int result = 1;

		int savedMaxShownLogLevel = DEFAULT_MAX_SHOW_LEVEL;
		try {
			if (Configed.savedStates != null) {
				savedMaxShownLogLevel = Integer.valueOf(Configed.savedStates.getProperty("savedMaxShownLogLevel"));
			}
		} catch (NumberFormatException ex) {
			Logging.warning(this,
					"savedMaxShownLogLevel could not be read, value "
							+ Configed.savedStates.getProperty("savedMaxShownLogLevel") + ", fallback to "
							+ DEFAULT_MAX_SHOW_LEVEL);

			Configed.savedStates.setProperty("savedMaxShownLogLevel", String.valueOf(DEFAULT_MAX_SHOW_LEVEL));
		}

		Logging.info(this, "produceInitialMaxShowLevel " + result);

		return savedMaxShownLogLevel;
	}

	private void buildDocument() {
		Logging.debug(this, "building document");
		setCursor(Globals.WAIT_CURSOR);
		// Switch to an blank document temporarily to avoid repaints

		document = new ImmutableDefaultStyledDocument(styleContext);

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			int i = 0;

			while (i < lines.length) {
				// we check if we got a new call

				boolean show = false;

				if (lineLevels[i] <= sliderLevel.getValue()) {
					show = true;
				}

				if (show && showTypeRestricted && lineTypes[i] != selTypeIndex) {
					show = false;
				}

				if (show) {

					docLinestartPosition2lineCount.put(document.getLength(), i);
					lineCount2docLinestartPosition.put(i, document.getLength());

					String no = "(" + i + ")";
					document.insertStringTruely(document.getLength(), String.format("%-10s", no) + lines[i] + '\n',
							lineStyles[i]);
				}

				i++;
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "BadLocationException thrown in logging: " + e);
		}
		jTextPane.setDocument(document);
		SwingUtilities.invokeLater(() -> setCursor(null));
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
		if (level > maxExistingLevel) {
			level = maxExistingLevel;
			sliderLevel.setValue(level);
			return;
		}

		if (Configed.savedStates != null) {
			Configed.savedStates.setProperty("savedMaxShownLogLevel", String.valueOf(level));
		}

		Integer oldLevel = showLevel;
		showLevel = level;
		Logging.info(this, "activateShowLevel level, oldLevel, maxExistingLevel " + level + " , " + oldLevel + ", "
				+ maxExistingLevel);

		if (!oldLevel.equals(level) && (level < maxExistingLevel || oldLevel < maxExistingLevel)) {
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

		} else {
			Iterator<Integer> linesIterator = lineCount2docLinestartPosition.keySet().iterator();
			int nextLineNo = 0;

			if (linesIterator.hasNext()) {
				nextLineNo = linesIterator.next();
			}

			while (linesIterator.hasNext() && nextLineNo < lineNo) {
				nextLineNo = linesIterator.next();
			}

			startPosition = lineCount2docLinestartPosition.get(nextLineNo) + offset;

		}

		try {
			jTextPane.setCaretPosition(startPosition);

			jTextPane.scrollRectToVisible(jTextPane
					.modelToView2D(offset + jComboBoxSearch.getSelectedItem().toString().length()).getBounds());
			jTextPane.getCaret().setVisible(true);
			highlighter.removeAllHighlights();
		} catch (BadLocationException e) {
			Logging.warning(this, "BadLocationException for setting caret in LotPane: " + e);
		}
	}

	private Style getStyleByLevelNo(int lev) {
		Style result = null;

		if (lev < logLevelStyles.length) {
			result = logLevelStyles[lev];
		} else {
			result = logLevelStyles[logLevelStyles.length - 1];
		}

		return result;
	}

	private void parse() {

		char levC = '0';
		int lev = 0;
		maxExistingLevel = 0;
		lineLevels = new int[lines.length];
		lineStyles = new Style[lines.length];

		lineTypes = new int[lines.length];
		typesList = new ArrayList<>();

		StringBlock nextBlock = new StringBlock();
		StringBlock testBlock = new StringBlock();

		Style lineStyle = getStyleByLevelNo(0);

		int countLines = lines.length;
		for (int i = 0; i < countLines; i++) {
			// keep last levC if not newly set
			if (lines[i].length() >= 3 && lines[i].charAt(0) == '[' && lines[i].charAt(2) == ']') {
				levC = lines[i].charAt(1);
			}
			if (Character.isDigit(levC)) {
				lev = Character.getNumericValue(levC);
				if (lev > maxExistingLevel) {
					maxExistingLevel = lev;
				}

				lineLevels[i] = lev;

				lineStyle = getStyleByLevelNo(lev);
			}
			lineStyles[i] = lineStyle;

			// search type
			String type = "";
			int typeIndex = 0;
			int nextStartI = 0;
			nextBlock.setString(lines[i]);
			testBlock.setString(lines[i]);
			nextBlock.forward(nextStartI, '[', ']');

			if (nextBlock.hasFound()) {

				nextStartI = nextBlock.getIEnd() + 1;

				testBlock.forward(nextStartI, '(', ')');
				if (testBlock.hasFound()) {
					nextStartI = testBlock.getIEnd() + 1;
				}
				nextBlock.forward(nextStartI, '[', ']');
			}

			if (nextBlock.hasFound()) {

				nextStartI = nextBlock.getIEnd() + 1;
				nextBlock.forward(nextStartI, '[', ']');
			}

			if (nextBlock.hasFound()) {

				type = nextBlock.getContent();

				typeIndex = typesList.indexOf(type);
				if (typeIndex == -1) {
					typeIndex = typesList.size();
					typesList.add(type);
				}
			}

			lineTypes[i] = typeIndex;
		}

		adaptComboType();
	}

	private void adaptComboType() {
		comboType.setEnabled(false);
		comboModelTypes.removeAllElements();

		if (!typesList.isEmpty()) {
			comboModelTypes.addElement(DEFAULT_TYPE);
			for (String type : typesList) {
				comboModelTypes.addElement(type);
			}
			comboType.setEnabled(true);

			int maxRowCount = typesList.size() + 1;
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

		parse();
		if (lines.length > 1) {
			showLevel = produceInitialMaxShowLevel();
			if (maxExistingLevel < showLevel) {
				showLevel = maxExistingLevel;
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

	public void setMainText(String s) {
		Logging.info(this, "setMainText ...");
		Logging.info(this, "usedmemory " + Globals.usedMemory());
		if (s == null) {
			Logging.warning(this, "wont set main text, argument s is null");
			return;
		}

		Logging.info(this, "Setting text");

		lines = s.split("\n");
		parse();
		if (lines.length > 1) {
			if (maxExistingLevel > 4) {
				showLevel = 5;
			} else {
				showLevel = maxExistingLevel;
			}
			adaptSlider();
		} else {
			showLevel = 1;
			sliderLevel.produceLabels();
		}

		sliderLevel.setValue(showLevel);
		buildDocument();
		Logging.info(this, "usedmemory " + Globals.usedMemory());

		jTextPane.setCaretPosition(0);
		jTextPane.getCaret().setVisible(true);
	}

	private void setParsedText(final String[] lines, final int[] lineLevels, final Style[] lineStyles,
			final int[] lineTypes, final List<String> typesList, boolean showTypeRestricted, int selTypeIndex,
			int maxExistingLevel) {
		Logging.debug(this, "setParsedText");
		this.lines = lines;
		this.lineLevels = lineLevels;
		this.maxExistingLevel = maxExistingLevel;
		this.lineStyles = lineStyles;
		this.lineTypes = lineTypes;
		this.typesList = typesList;
		adaptComboType();
		this.showTypeRestricted = showTypeRestricted;
		this.selTypeIndex = selTypeIndex;
		buildDocument();
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
		// change 08/2015: set lastReturnedOffset to start search at last caretPosition
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
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == jTextPane) {
			if (e.getKeyChar() == '/' || e.getKeyChar() == '\u0006' /* ctrl-f */ ) {
				editSearchString();
			}

			// f g h i j k l
			// 6 7 8 9 10 11 12
			if (e.getKeyChar() == 'n' || e.getKeyChar() == '\u000c' || e.getKeyCode() == KeyEvent.VK_F3) {
				search();
			}
			e.consume();
		}
	}

	// Interface ActionListener

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == buttonSearch) {
			search();
		} else if (e.getSource() == jComboBoxSearch) {
			search();
			jTextPane.requestFocusInWindow();
		} else if (e.getSource() == jCheckBoxCaseSensitive) {
			searcher.setCaseSensitivity(jCheckBoxCaseSensitive.isSelected());
		} else if (e.getSource() == buttonFontPlus) {
			increaseFontSize();
		} else if (e.getSource() == buttonFontMinus) {
			reduceFontSize();
		} else {
			Logging.warning(this, "unexpected action event on source " + e.getSource());
		}
	}
}
