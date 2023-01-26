package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.View;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PopupMenuTrait;

public class LogPane extends JPanel implements KeyListener, ActionListener {
	public static final int DEFAULT_MAX_SHOW_LEVEL = 3;

	protected JTextPane jTextPane;
	protected JScrollPane scrollpane;
	protected JPanel commandpane;
	protected JLabel labelSearch;

	protected JComboBox<String> jComboBoxSearch;
	protected static final int FIELD_H = Globals.LINE_HEIGHT;
	protected JButton buttonSearch;
	protected JCheckBox jCheckBoxCaseSensitive;
	protected JButton buttonFontPlus;
	protected JButton buttonFontMinus;
	protected JLabel labelLevel;
	protected AdaptingSlider sliderLevel;
	protected static final int SLIDER_H = 35;
	protected static final int SLIDER_W = 180;
	protected ChangeListener sliderListener;
	protected JLabel labelDisplayRestriction;
	protected JComboBox<String> comboType;
	protected DefaultComboBoxModel<String> comboModelTypes;
	protected static final String DEFAULT_TYPE = "(all)";

	protected JPanel jTextPanel;
	protected WordSearcher searcher;
	protected Highlighter highlighter;
	protected final StyleContext styleContext;
	protected final Style[] logLevelStyles;

	private static final Integer[] LEVELS = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static final List<Integer> levelList = Arrays.asList(LEVELS);

	protected Integer maxLevel = LEVELS[LEVELS.length - 1];
	protected Integer minLevel = LEVELS[0];
	protected Integer maxExistingLevel = minLevel;
	protected Integer showLevel = minLevel;

	protected boolean showTypeRestricted = false;
	protected List<String> typesList;
	protected int typesListMaxShowCount = 25;

	protected int[] lineTypes;

	TreeMap<Integer, Integer> docLinestartPosition2lineCount;
	TreeMap<Integer, Integer> lineCount2docLinestartPosition;

	int selTypeIndex = -1;

	protected String title;
	protected String info;

	protected Integer displayFontSize = 11;
	protected Font monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);

	public void setFontSize(String s) {
		if (s.equals("+")) {
			displayFontSize = displayFontSize + 2;
			monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);
			buildDocument();
		} else if (s.equals("-")) {
			if (displayFontSize > 10) {
				displayFontSize = displayFontSize - 2;
				monospacedFont = new Font("Monospaced", Font.PLAIN, displayFontSize);
			}
			buildDocument();
		}
	}

	protected class AdaptingSlider extends JSlider implements ChangeListener {

		int min;
		int max;
		int value;

		public AdaptingSlider(int min, int max, int value) {
			super(min, max, value);
			addChangeListener(this);

			this.min = min;
			this.max = max;
			this.value = value;

			setFont(Globals.defaultFont);

			produceLabels(max);

			setPaintLabels(true);
			setSnapToTicks(true);

		}

		// ChangeListener
		@Override
		public void stateChanged(ChangeEvent e) {
			// for debugging
			Logging.info(this, "min, max, value " + min + ", " + max + ", " + value + " -- ChangeEvent " + e);
		}

		public void produceLabels(int upTo) {

			Map<Integer, JLabel> levelMap = new LinkedHashMap<>();

			for (int i = min; i <= upTo; i++) {
				levelMap.put(i, new JLabel("" + i));
			}

			for (int i = upTo + 1; i <= max; i++)
				levelMap.put(i, new JLabel(" . "));

			try {
				setLabelTable(new Hashtable<>(levelMap));
			} catch (Exception ex) {
				Logging.info(this, "setLabelTable levelDict " + levelMap + " ex " + ex);
			}
		}
	}

	protected PopupMenuTrait popupMenu;

	protected DefaultStyledDocument document;

	protected String[] lines;
	protected int[] lineLevels;
	protected Style[] lineStyles;

	protected void reload() {
		Logging.info(this, "reload action");
		setLevelWithoutAction(produceInitialMaxShowLevel());
	}

	protected void save() {
		Logging.debug(this, "save action");
	}

	protected void saveAsZip() {
		Logging.debug(this, "save as zip action");
	}

	protected void saveAllAsZip(boolean loadMissingDocs) {
		Logging.debug(this, "save all as zip action");
	}

	protected void floatExternal() {
		if (document == null)
			return;
		// set text did not run
		// probably we already are in a floating instance

		LogPane copyOfMe;
		de.uib.configed.gui.GeneralFrame externalView;

		copyOfMe = new LogPane("", false);

		externalView = new de.uib.configed.gui.GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.setLocationRelativeTo(Globals.mainFrame);

		copyOfMe.setLevelWithoutAction(showLevel);
		copyOfMe.setParsedText(lines, lineLevels, lineStyles, lineTypes, typesList, showTypeRestricted, selTypeIndex,
				maxExistingLevel);
		copyOfMe.getTextComponent().setCaretPosition(jTextPane.getCaretPosition());
		copyOfMe.adaptSlider();

		externalView.setVisible(true);
	}

	JTextComponent getTextComponent() {
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

	public LogPane(String defaultText) {
		this(defaultText, true);
	}

	private Integer produceInitialMaxShowLevel() {
		int result = 1;

		int savedMaxShownLogLevel = 0;
		try {
			savedMaxShownLogLevel = Integer.valueOf(Configed.savedStates.savedMaxShownLogLevel.deserialize());

		} catch (NumberFormatException ex) {
			Logging.warning(this, "savedMaxShownLogLevel could not be read, value "
					+ Configed.savedStates.savedMaxShownLogLevel.deserialize());
		}
		if (savedMaxShownLogLevel > 0)
			result = savedMaxShownLogLevel;
		else
			result = DEFAULT_MAX_SHOW_LEVEL;

		Logging.info(this, "produceInitialMaxShowLevel " + result);

		return result;
	}

	public LogPane(String defaultText, boolean withPopup) {
		super(new BorderLayout());
		Logging.info(this, "initializing");
		title = "";
		info = "";

		jTextPanel = new JPanel(new BorderLayout());
		scrollpane = new JScrollPane();
		jTextPane = new JTextPane() {
			@Override
			public Dimension getPreferredSize() {
				return getUI().getMinimumSize(this);
			}
		};
		jTextPane.setCaretColor(Globals.LOG_PANE_CARET_COLOR);
		jTextPane.getCaret().setBlinkRate(0);

		class LogStyleContext extends StyleContext {
			@Override
			public Font getFont(AttributeSet attr) {
				return monospacedFont;
			}
		}
		styleContext = new LogStyleContext();
		logLevelStyles = new Style[10];

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
		StyleConstants.setForeground(logLevelStyles[6], Globals.logColorInfo);

		logLevelStyles[7] = styleContext.addStyle("loglevel debug", null);
		StyleConstants.setForeground(logLevelStyles[7], Globals.logColorDebug);

		logLevelStyles[8] = styleContext.addStyle("loglevel debug2", null);
		StyleConstants.setForeground(logLevelStyles[8], Globals.logColorDebug2);

		logLevelStyles[9] = styleContext.addStyle("loglevel confidential", null);
		StyleConstants.setForeground(logLevelStyles[9], Globals.logColorConfidential);

		searcher = new WordSearcher(jTextPane);
		searcher.setCaseSensitivity(false);
		highlighter = new UnderlineHighlighter(null);
		jTextPane.setHighlighter(highlighter);

		if (defaultText != null)
			jTextPane.setText(defaultText);

		jTextPane.setOpaque(true);
		jTextPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		jTextPane.setEditable(true);
		jTextPane.setFont(Globals.defaultFont);
		jTextPane.addKeyListener(this);

		jTextPanel.add(jTextPane, BorderLayout.CENTER);

		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getViewport().add(jTextPanel);
		add(scrollpane, BorderLayout.CENTER);

		labelSearch = new JLabel(Configed.getResourceValue("TextPane.jLabel_search"));
		labelSearch.setFont(Globals.defaultFont);

		jComboBoxSearch = new JComboBox<>();
		jComboBoxSearch.setEditable(true);
		jComboBoxSearch.addActionListener(this);

		buttonSearch = new JButton(Configed.getResourceValue("TextPane.jButton_search"));
		buttonSearch.setFont(Globals.defaultFont);
		buttonSearch.addActionListener(this);
		buttonSearch.addKeyListener(this);

		jCheckBoxCaseSensitive = new JCheckBox(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive"));
		jCheckBoxCaseSensitive.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		jCheckBoxCaseSensitive.setSelected(false);
		jCheckBoxCaseSensitive.addActionListener(this);

		buttonFontPlus = new JButton(Globals.createImageIcon("images/font-plus.png", ""));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("TextPane.fontPlus"));
		buttonFontPlus.addActionListener(this);

		buttonFontMinus = new JButton(Globals.createImageIcon("images/font-minus.png", ""));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("TextPane.fontMinus"));
		buttonFontMinus.addActionListener(this);

		labelLevel = new JLabel(Configed.getResourceValue("TextPane.jLabel_level"));
		labelLevel.setFont(Globals.defaultFont);

		int minL = 1;
		int maxL = 9;
		int valL = 1;

		Logging.info(this, "levels minL, maxL, valL " + minL + ", " + maxL + ", " + valL);

		sliderLevel = new AdaptingSlider(minL, maxL, produceInitialMaxShowLevel());

		JSpinner spinnerMinLevel = new JSpinner(new SpinnerNumberModel(valL, minL, maxL, 1));

		spinnerMinLevel.setVisible(false); // to develop
		JComponent editor = spinnerMinLevel.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			JTextField field = ((JSpinner.DefaultEditor) editor).getTextField();
			field.setForeground(Globals.BACKGROUND_COLOR_7);
			field.setBackground(Globals.BACKGROUND_COLOR_7);
			Logging.info(this, "spinnerMinLevel set textfield cols 0");

		}

		sliderListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {

				Logging.debug(this, "change event from sliderLevel, " + sliderLevel.getValue());
				if (sliderLevel.getValueIsAdjusting())
					return;

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Logging.debug(this, "activateShowLevel call");
						Cursor startingCursor = sliderLevel.getCursor();
						sliderLevel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						try {
							activateShowLevel();
						} catch (Exception ex) {
							Logging.debug(this, "Exception in activateShowLevel " + ex);
						}
						sliderLevel.setCursor(startingCursor);
					}
				});

			}
		};

		sliderLevel.addChangeListener(sliderListener);
		sliderLevel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Logging.debug(this, "MouseWheelEvent " + e);

				int newIndex = levelList.indexOf(sliderLevel.getValue()) - e.getWheelRotation();

				Logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

				if (newIndex > LEVELS.length - 1)
					newIndex = LEVELS.length - 1;

				else if (newIndex < 0)
					newIndex = 0;

				Logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

				sliderLevel.setValue(levelList.get(newIndex))

				;
			}
		});

		labelDisplayRestriction = new JLabel(Configed.getResourceValue("TextPane.EventType"));
		labelDisplayRestriction.setFont(Globals.defaultFont);

		comboModelTypes = new DefaultComboBoxModel<>();
		comboType = new JComboBox<>(comboModelTypes);
		comboType.setFont(Globals.defaultFont);
		comboType.setEnabled(false);
		comboType.setEditable(false);

		comboType.addActionListener(actionEvent -> {
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
		});

		commandpane = new JPanel();
		GroupLayout layoutCommandpane = new GroupLayout(commandpane);
		commandpane.setLayout(layoutCommandpane);

		layoutCommandpane.setHorizontalGroup(layoutCommandpane
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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

						.addComponent(buttonFontPlus, Globals.GRAPHIC_BUTTON_WIDTH / 2,
								Globals.GRAPHIC_BUTTON_WIDTH / 2, Globals.GRAPHIC_BUTTON_WIDTH / 2)
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonFontMinus, Globals.GRAPHIC_BUTTON_WIDTH / 2,
								Globals.GRAPHIC_BUTTON_WIDTH / 2, Globals.GRAPHIC_BUTTON_WIDTH / 2)
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
						.addComponent(spinnerMinLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(sliderLevel, SLIDER_W, SLIDER_W, SLIDER_W)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layoutCommandpane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxSearch, FIELD_H, FIELD_H, FIELD_H)
						.addComponent(buttonSearch, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxCaseSensitive, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(buttonFontPlus, Globals.GRAPHIC_BUTTON_WIDTH / 2,
								Globals.GRAPHIC_BUTTON_WIDTH / 2, Globals.GRAPHIC_BUTTON_WIDTH / 2)
						.addComponent(buttonFontMinus, Globals.GRAPHIC_BUTTON_WIDTH / 2,
								Globals.GRAPHIC_BUTTON_WIDTH / 2, Globals.GRAPHIC_BUTTON_WIDTH / 2)
						.addComponent(labelDisplayRestriction, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(comboType, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(labelLevel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(spinnerMinLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(sliderLevel, SLIDER_H, SLIDER_H, SLIDER_H)

				).addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		add(commandpane, BorderLayout.SOUTH);

		if (withPopup) {
			popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_SAVE,
					PopupMenuTrait.POPUP_SAVE_AS_ZIP, PopupMenuTrait.POPUP_SAVE_ALL_AS_ZIP,
					PopupMenuTrait.POPUP_FLOATINGCOPY }) {
				@Override
				public void action(int p) {
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
					}

				}
			};

			popupMenu.addPopupListenersTo(new JComponent[] { jTextPane });
		}
	}

	public void buildDocument() {
		Logging.debug(this, "building document");
		jTextPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// Switch to an blank document temporarily to avoid repaints

		document = new DefaultStyledDocument(styleContext);

		int selLevel = levelList.indexOf(sliderLevel.getValue()) + 1;

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			int i = 0;

			while (i < lines.length)
			// we check if we got a new call
			{
				boolean show = false;

				if (lineLevels[i] <= selLevel) {
					show = true;
				}

				if (show && showTypeRestricted && lineTypes[i] != selTypeIndex)
					show = false;

				if (show) {

					docLinestartPosition2lineCount.put(document.getLength(), i);
					lineCount2docLinestartPosition.put(i, document.getLength());

					String no = "(" + i + ")";
					document.insertString(document.getLength(), String.format("%-10s", no) + lines[i] + '\n',
							lineStyles[i]);
				}

				i++;
			}
		} catch (BadLocationException e) {
			Logging.warning(this, "BadLocationException thrown in logging: " + e);
		}
		jTextPane.setDocument(document);
		SwingUtilities.invokeLater(() -> jTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)));
	}

	private void setLevelWithoutAction(Object l) {
		Logging.debug(this, "setLevel " + l);

		Integer levelO = sliderLevel.getValue();
		if (levelO != l && sliderListener != null) {
			sliderLevel.removeChangeListener(sliderListener);
			sliderLevel.setValue((Integer) l);
			sliderLevel.addChangeListener(sliderListener);
		}

	}

	private void activateShowLevel() {

		Integer level = sliderLevel.getValue();
		if (level > maxExistingLevel) {
			level = maxExistingLevel;
			sliderLevel.setValue(level);
			return;
		}

		Configed.savedStates.savedMaxShownLogLevel.serialize(level);

		Integer oldLevel = showLevel;
		showLevel = level;
		Logging.info(this, "activateShowLevel level, oldLevel, maxExistingLevel " + level + " , " + oldLevel + ", "
				+ maxExistingLevel);

		if (!oldLevel.equals(level) && (level < maxExistingLevel || oldLevel < maxExistingLevel)) {

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
			if (docLinestartPosition2lineCount.get(oldStartPosition) != null)
				lineNo = docLinestartPosition2lineCount.get(oldStartPosition);

			buildDocument();

			if (lineCount2docLinestartPosition.containsKey(lineNo)) {
				startPosition = lineCount2docLinestartPosition.get(lineNo) + offset;

			} else {
				Iterator<Integer> linesIterator = lineCount2docLinestartPosition.keySet().iterator();
				int nextLineNo = 0;

				if (linesIterator.hasNext())
					nextLineNo = linesIterator.next();

				while (linesIterator.hasNext() && nextLineNo < lineNo)
					nextLineNo = linesIterator.next();

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

	}

	private class StringBlock {
		String s;
		int iEnd;
		private int contentStart;
		private int contentEnd;
		boolean found;

		char startC;
		char endC;

		void setString(String s) {
			this.s = s;
		}

		String getContent() {
			return s.substring(contentStart, contentEnd).trim();
		}

		boolean hasFound() {

			return found;
		}

		int getIEnd() {
			return iEnd;
		}

		private int findContentEnd() {
			int i = contentStart;
			int result = -1;
			int counterStartC = 0;
			while (result == -1 && i < s.length()) {
				char c = s.charAt(i);

				if (c == startC) {
					counterStartC++;
				} else if (c == endC) {
					if (counterStartC > 0)
						counterStartC--;
					else
						result = i;
				}

				i++;
			}

			return result;
		}

		void forward(int iStart, char startC, char endC) {

			iEnd = iStart;
			found = false;
			this.startC = startC;
			this.endC = endC;

			contentStart = s.indexOf(startC, iStart);
			if (contentStart < 0) {
				// not found
				return;
			}

			if (s.substring(iStart, contentStart).trim().length() > 0) {
				// other chars than white space before contentStart
				return;
			}

			contentStart++;

			contentEnd = findContentEnd();

			if (contentEnd > -1)
				found = true;
			else
				return;

			iEnd = contentEnd;
			iEnd++;

		}

	}

	protected Style getStyleByLevelNo(int lev) {
		Style result = null;

		if (lev < logLevelStyles.length)
			result = logLevelStyles[lev];
		else
			result = logLevelStyles[logLevelStyles.length - 1];

		return result;
	}

	protected void parse() {

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
			if ((lines[i].length() >= 3) && (lines[i].charAt(0) == '[') && (lines[i].charAt(2) == ']')) {
				levC = lines[i].charAt(1);
			}
			if (Character.isDigit(levC)) {
				lev = Character.getNumericValue(levC);
				if (lev > maxExistingLevel)
					maxExistingLevel = lev;

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
			if (maxRowCount > typesListMaxShowCount)
				maxRowCount = typesListMaxShowCount;

			comboType.setMaximumRowCount(maxRowCount);
		}
	}

	public void adaptSlider() {
		sliderLevel.produceLabels(maxExistingLevel);
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
			if (maxExistingLevel < showLevel)
				showLevel = maxExistingLevel;
			adaptSlider();
		} else {
			showLevel = 1;
			sliderLevel.produceLabels(0);
		}

		sliderLevel.setValue(showLevel);
		buildDocument();
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

	public void editSearchString() {
		jComboBoxSearch.requestFocus();
	}

	public void search() {
		Logging.debug(this, "Searching string in log");
		jTextPane.requestFocus();
		searcher.comp.setCaretPosition(jTextPane.getCaretPosition());
		// change 08/2015: set lastReturnedOffset to start search at last caretPosition
		searcher.lastReturnedOffset = jTextPane.getCaretPosition();
		int offset = searcher.search(jComboBoxSearch.getSelectedItem().toString());
		if (jComboBoxSearch.getSelectedIndex() <= -1) { // does not exist
			jComboBoxSearch.addItem(jComboBoxSearch.getSelectedItem().toString());
			jComboBoxSearch.repaint();
		}
		if (offset != -1) {
			try {
				jTextPane.scrollRectToVisible(jTextPane
						.modelToView2D(offset + jComboBoxSearch.getSelectedItem().toString().length()).getBounds());

				jTextPane.setCaretPosition(offset);
				jTextPane.getCaret().setVisible(true);
				searcher.comp.setCaretPosition(offset);
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
			if ((e.getKeyCode() == KeyEvent.VK_F3 || e.getKeyCode() == KeyEvent.VK_ENTER)
					&& jComboBoxSearch.getSelectedItem() != null
					&& !jComboBoxSearch.getSelectedItem().toString().equals("")) {
				search();
			}

			else if (e.getSource() == jTextPane && (e.getKeyChar() == '+')
					&& ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
				Logging.info(this, "Ctrl-Plus");
				setFontSize("+");
			}

			else if (e.getSource() == jTextPane && (e.getKeyChar() == '-')
					&& ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
				Logging.info(this, "Ctrl-Minus");
				setFontSize("-");
			}

		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == jTextPane) {
			if (e.getKeyChar() == '/' || e.getKeyChar() == '\u0006') // ctrl-f
			{
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
		}

		else if (e.getSource() == buttonFontPlus) {
			setFontSize("+");
		}

		else if (e.getSource() == buttonFontMinus) {
			setFontSize("-");
		}
	}

	// A simple class that searches for a word in
	// a document and highlights occurrences of that word
	public class WordSearcher {
		protected JTextComponent comp;
		protected Highlighter.HighlightPainter painter;
		protected int lastReturnedOffset;
		protected boolean cS = false;

		public WordSearcher(JTextComponent comp) {
			this.comp = comp;
			this.painter = new UnderlineHighlightPainter(Globals.FAILED_COLOR);
			this.lastReturnedOffset = -1;
		}

		// Set case sensitivity
		public void setCaseSensitivity(boolean cs) {
			this.cS = cs;
		}

		// Search for a word and return the offset of the
		// next occurrence. Highlights are added for all
		// occurrences found.
		public int search(String word) {

			int firstOffset = -1;
			int returnOffset = lastReturnedOffset;
			Highlighter highlighter = comp.getHighlighter();

			// Remove any existing highlights for last word
			Highlighter.Highlight[] highlights = highlighter.getHighlights();
			for (int i = 0; i < highlights.length; i++) {
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() instanceof UnderlineHighlightPainter) {
					highlighter.removeHighlight(h);
				}
			}

			if (word == null || word.equals("")) {
				return -1;
			}

			// Look for the word we are given - insensitive search
			String content = null;
			try {
				Document d = comp.getDocument();

				if (cS)
					content = d.getText(0, d.getLength());
				else
					content = d.getText(0, d.getLength()).toLowerCase();
			} catch (BadLocationException e) {
				// Cannot happen
				return -1;
			}

			if (!cS)
				word = word.toLowerCase();
			int lastIndex = 0;
			int wordSize = word.length();

			while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
				int endIndex = lastIndex + wordSize;
				try {
					highlighter.addHighlight(lastIndex, endIndex, painter);
				} catch (BadLocationException e) {
					// Nothing to do
				}
				if (firstOffset == -1) {
					firstOffset = lastIndex;
				}
				if ((returnOffset == lastReturnedOffset) && (lastIndex > lastReturnedOffset)) {
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
			if (!newValue) {
				throw new IllegalArgumentException("UnderlineHighlighter only draws layered highlights");
			}
			super.setDrawsLayeredHighlights(true);
		}
	}

}
