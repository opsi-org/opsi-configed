package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
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

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PopupMenuTrait;

public class LogPane extends JPanel implements KeyListener, ActionListener {
	public static final int defaultMaxShowLevel = 3;

	// private int initialMaxShowLevel;

	protected JTextPane jTextPane;
	protected JScrollPane scrollpane;
	protected JPanel commandpane;
	protected JLabel labelSearch;
	// protected JTextField fieldSearch;
	protected JComboBox jComboBoxSearch;
	protected final int fieldH = Globals.LINE_HEIGHT;
	protected JButton buttonSearch;
	protected JCheckBox jCheckBoxCaseSensitive;
	protected JButton buttonFontPlus;
	protected JButton buttonFontMinus;
	protected JLabel labelLevel;
	protected AdaptingSlider sliderLevel;
	protected AdaptingSlider sliderLevel0;
	protected final int sliderH = 35;
	protected final int sliderW = 180;
	protected ChangeListener sliderListener;
	protected JLabel labelDisplayRestriction;
	protected JComboBox comboType;
	protected DefaultComboBoxModel comboModelTypes;
	protected final String defaultType = "(all)";
	// protected final String typePrefix = "event ";

	protected JPanel jTextPanel;
	protected WordSearcher searcher;
	protected Highlighter highlighter;
	protected final StyleContext styleContext;
	protected final Style[] logLevelStyles;
	// public static Object[] levels = new Object[] {MIN_LEVEL, "up to 2", "up to
	// 3", "up to 4", "up to 5", "up to 6", "up to 7", "up to 8", "ALL"};
	public Integer[] levels = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	public List<Integer> levelList = Arrays.asList(levels);

	protected Integer maxLevel = levels[levels.length - 1];
	protected Integer minLevel = levels[0];
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
	final int minDisplayFontSize = 10;
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
		LinkedHashMap<Integer, JLabel> levelMap;
		Hashtable<Integer, JLabel> levelDict;

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

			// setPaintTicks(true);

			setPaintLabels(true);
			setSnapToTicks(true);

		}

		// ChangeListener
		@Override
		public void stateChanged(ChangeEvent e) {
			// for debugging
			logging.info(this, "min, max, value " + min + ", " + max + ", " + value + " -- ChangeEvent " + e);
		}

		public void produceLabels(int upTo) {
			// logging.debug(this, "produceLabels upTo " + upTo);

			levelMap = new LinkedHashMap<>();

			for (int i = min; i <= upTo; i++) {
				levelMap.put(i, new JLabel("" + i));
			}

			for (int i = upTo + 1; i <= max; i++)
				levelMap.put(i, new JLabel(" . "));

			levelDict = new Hashtable<Integer, JLabel>(levelMap);
			try {
				setLabelTable(levelDict);
			} catch (Exception ex) {
				logging.info(this, "setLabelTable levelDict " + levelDict + " ex " + ex);
			}
		}
	}

	protected class ImmutableDefaultStyledDocument extends DefaultStyledDocument {
		ImmutableDefaultStyledDocument() {
			super();
		}

		ImmutableDefaultStyledDocument(StyleContext styles) {
			super(styles);
		}

		public void insertStringTruely(int offs, String str, AttributeSet a) throws BadLocationException {
			super.insertString(offs, str, a);
		}

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			return;
		}

		@Override
		public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			return;
		}

		@Override
		public void remove(int offs, int len) throws BadLocationException {
			return;
		}

	}

	protected PopupMenuTrait popupMenu;

	protected ImmutableDefaultStyledDocument document;

	protected String[] lines;
	protected int[] lineLevels;
	protected Style[] lineStyles;

	protected void reload() {
		logging.info(this, "reload action");
		setLevelWithoutAction(produceInitialMaxShowLevel());
	}

	protected void save() {
		logging.debug(this, "save action");
	}

	protected void saveAsZip() {
		logging.debug(this, "save as zip action");
	}

	protected void saveAllAsZip(boolean loadMissingDocs) {
		logging.debug(this, "save all as zip action");
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
		externalView.centerOn(Globals.mainFrame);

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
			savedMaxShownLogLevel = Integer.valueOf(configed.savedStates.savedMaxShownLogLevel.deserialize());
			// logging.info(this, "savedMaxShownLogLevel " + savedMaxShownLogLevel);
		} catch (NumberFormatException ex) {
			logging.warning(this, "savedMaxShownLogLevel could not be read, value "
					+ configed.savedStates.savedMaxShownLogLevel.deserialize());
		}
		if (savedMaxShownLogLevel > 0)
			result = savedMaxShownLogLevel;
		else
			result = defaultMaxShowLevel;

		logging.info(this, "produceInitialMaxShowLevel " + result);

		return result;
	}

	public LogPane(String defaultText, boolean withPopup) {
		super(new BorderLayout());
		logging.info(this, "initializing");
		title = "";
		info = "";

		// initialMaxShowLevel = produceInitialMaxShowLevel();

		jTextPanel = new JPanel(new BorderLayout());
		scrollpane = new JScrollPane();
		jTextPane = new JTextPane();
		jTextPane.setCaretColor(Color.RED);
		jTextPane.getCaret().setBlinkRate(0);

		// jTextPane.setContentType ("text/ascii; charset=UTF-8");

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
		// StyleConstants.setBold(logLevelStyles[1], true);

		logLevelStyles[2] = styleContext.addStyle("loglevel critical", null);
		StyleConstants.setForeground(logLevelStyles[2], Globals.logColorCritical);
		// StyleConstants.setBold(logLevelStyles[2], true);

		logLevelStyles[3] = styleContext.addStyle("loglevel error", null);
		StyleConstants.setForeground(logLevelStyles[3], Globals.logColorError);
		// StyleConstants.setBold(logLevelStyles[3], true);

		logLevelStyles[4] = styleContext.addStyle("loglevel warning", null);
		StyleConstants.setForeground(logLevelStyles[4], Globals.logColorWarning);
		// StyleConstants.setBold(logLevelStyles[4], true);

		logLevelStyles[5] = styleContext.addStyle("loglevel notice", null);
		StyleConstants.setForeground(logLevelStyles[5], Globals.logColorNotice);
		// StyleConstants.setBold(logLevelStyles[5], true);

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
		jTextPane.setBackground(Globals.backgroundWhite);
		jTextPane.setEditable(true); // we want a caret; therefore we make the document immutable
		jTextPane.setFont(Globals.defaultFont);
		jTextPane.addKeyListener(this);

		jTextPanel.add(jTextPane, BorderLayout.CENTER);

		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpane.getVerticalScrollBar().setUnitIncrement(20);
		scrollpane.getViewport().add(jTextPanel);
		add(scrollpane, BorderLayout.CENTER);

		labelSearch = new JLabel(configed.getResourceValue("TextPane.jLabel_search"));
		labelSearch.setFont(Globals.defaultFont);
		// labelSearch.setPreferredSize(new Dimension (buttonWidth,
		// Globals.buttonHeight));

		jComboBoxSearch = new JComboBox();
		jComboBoxSearch.setEditable(true);
		jComboBoxSearch.addActionListener(this);

		buttonSearch = new JButton(configed.getResourceValue("TextPane.jButton_search"));
		buttonSearch.setFont(Globals.defaultFont);
		buttonSearch.addActionListener(this);
		buttonSearch.addKeyListener(this);

		jCheckBoxCaseSensitive = new JCheckBox(configed.getResourceValue("TextPane.jCheckBoxCaseSensitive"));
		jCheckBoxCaseSensitive.setToolTipText(configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		jCheckBoxCaseSensitive.setSelected(false);
		jCheckBoxCaseSensitive.addActionListener(this);

		buttonFontPlus = new JButton(Globals.createImageIcon("images/font-plus.png", ""));
		buttonFontPlus.setToolTipText(configed.getResourceValue("TextPane.fontPlus"));
		buttonFontPlus.addActionListener(this);

		buttonFontMinus = new JButton(Globals.createImageIcon("images/font-minus.png", ""));
		buttonFontMinus.setToolTipText(configed.getResourceValue("TextPane.fontMinus"));
		buttonFontMinus.addActionListener(this);

		labelLevel = new JLabel(configed.getResourceValue("TextPane.jLabel_level"));
		labelLevel.setFont(Globals.defaultFont);

		int minL = 1;
		int maxL = 9;
		int valL = 1;

		logging.info(this, "levels minL, maxL, valL " + minL + ", " + maxL + ", " + valL);

		sliderLevel = new AdaptingSlider(minL, maxL, produceInitialMaxShowLevel());
		sliderLevel0 = new AdaptingSlider(minL, maxL, produceInitialMaxShowLevel());
		sliderLevel0.setVisible(false); // to develop
		sliderLevel0.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				logging.info(this, "mouseClicked on sliderLevel0 ");
			}
		});

		JSpinner spinnerMinLevel = new JSpinner(new SpinnerNumberModel(valL, minL, maxL, (int) 1));

		spinnerMinLevel.setVisible(false); // to develop
		JComponent editor = spinnerMinLevel.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			JTextField field = ((JSpinner.DefaultEditor) editor).getTextField();
			field.setForeground(Globals.backLightBlue);
			field.setBackground(Globals.backLightBlue);
			logging.info(this, "spinnerMinLevel set textfield cols 0");

		}

		sliderListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// final Cursor startingCursor = sliderLevel.getCursor();
				// sliderLevel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				logging.debug(this, "change event from sliderLevel, " + sliderLevel.getValue());
				if (sliderLevel.getValueIsAdjusting())
					return;

				// activateShowLevel();

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						logging.debug(this, "activateShowLevel call");
						Cursor startingCursor = sliderLevel.getCursor();
						sliderLevel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						try {
							activateShowLevel();
						} catch (Exception ex) {
							logging.debug(this, "Exception in activateShowLevel " + ex);
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
				logging.debug(this, "MouseWheelEvent " + e);

				int newIndex = levelList.indexOf(sliderLevel.getValue()) - e.getWheelRotation();

				logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

				if (newIndex > levels.length - 1)
					newIndex = levels.length - 1;

				else if (newIndex < 0)
					newIndex = 0;

				logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

				sliderLevel.setValue(levelList.get(newIndex))

				;
			}
		});

		labelDisplayRestriction = new JLabel(configed.getResourceValue("TextPane.EventType"));
		labelDisplayRestriction.setFont(Globals.defaultFont);

		comboModelTypes = new DefaultComboBoxModel();
		comboType = new JComboBox(comboModelTypes);
		comboType.setFont(Globals.defaultFont);
		comboType.setEnabled(false);
		comboType.setEditable(false);

		comboType.addActionListener(actionEvent -> {
			int oldSelTypeIndex = selTypeIndex;
			Object selType = comboType.getSelectedItem();
			if (selType == null || selType.equals(defaultType)) {
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

		/*
		 * spinnerLevel = new JSpinner( new SpinnerListModel( levelList) );
		 * ((JSpinner.ListEditor)
		 * spinnerLevel.getEditor()).getTextField().setEditable(false);
		 * spinnerLevel.setValue(showLevel);
		 * spinnerListener = new ChangeListener(){
		 * public void stateChanged(ChangeEvent e)
		 * {
		 * //final Cursor startingCursor = spinnerLevel.getCursor();
		 * //spinnerLevel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		 * logging.debug(this, "change event from spinnerLevel, " +
		 * spinnerLevel.getValue());
		 * 
		 * //activateShowLevel();
		 * 
		 * SwingUtilities.invokeLater(new Runnable()
		 * {
		 * public void run()
		 * {
		 * logging.debug(this, "activateShowLevel call");
		 * Cursor startingCursor = spinnerLevel.getCursor();
		 * spinnerLevel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		 * try
		 * {
		 * activateShowLevel();
		 * }
		 * catch(Exception ex)
		 * {
		 * logging.debug(this, "Exception in activateShowLevel " + ex);
		 * }
		 * spinnerLevel.setCursor(startingCursor);
		 * }
		 * }
		 * );
		 * 
		 * }
		 * }
		 * ;
		 * 
		 * spinnerLevel.addChangeListener( spinnerListener );
		 * spinnerLevel.addMouseWheelListener(new MouseWheelListener(){
		 * public void mouseWheelMoved( MouseWheelEvent e )
		 * {
		 * logging.debug(this, "MouseWheelEvent " + e);
		 * 
		 * 
		 * int newIndex = levelList.indexOf(spinnerLevel.getValue()) -
		 * e.getWheelRotation();
		 * 
		 * logging.debug(this, "MouseWheelEvent newIndex " + newIndex);
		 * 
		 * if (newIndex > maxLevel)
		 * newIndex = maxLevel;
		 * 
		 * else if (newIndex < minLevel)
		 * newIndex = minLevel;
		 * 
		 * 
		 * spinnerLevel.setValue(
		 * levelList.get(newIndex)
		 * )
		 * 
		 * ;
		 * }
		 * }
		 * );
		 */

		/*
		 * comboLevel = new JComboBox(levels);
		 * comboLevel.setMaximumRowCount(levels.length);
		 * comboLevel.setSelectedItem(showLevel);
		 * comboLevel.setFont (Globals.defaultFont);
		 * comboListener = new ActionListener(){
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * logging.debug(this, "action performed on comboLevel, " +
		 * comboLevel.getSelectedItem());
		 * activateShowLevel();
		 * }
		 * }
		 * ;
		 * comboLevel.addActionListener(comboListener);
		 */

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
						// .addGap(Globals.gapSize/2,Globals.gapSize/2,Globals.gapSize)
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
						.addGroup(layoutCommandpane.createParallelGroup()
								.addComponent(sliderLevel, sliderW, sliderW, sliderW)
								.addComponent(sliderLevel0, sliderW, sliderW, sliderW))
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layoutCommandpane.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxSearch, fieldH, fieldH, fieldH)
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
						.addComponent(sliderLevel, sliderH, sliderH, sliderH)
						.addComponent(sliderLevel0, sliderH, sliderH, sliderH)
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE )
				).addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		add(commandpane, BorderLayout.SOUTH);

		if (withPopup) {
			popupMenu = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_SAVE,
					PopupMenuTrait.POPUP_SAVE_AS_ZIP,
					// PopupMenuTrait.POPUP_SAVE_LOADED_AS_ZIP,
					PopupMenuTrait.POPUP_SAVE_ALL_AS_ZIP, PopupMenuTrait.POPUP_FLOATINGCOPY }) {
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

	private void emptyDocument() {
		ImmutableDefaultStyledDocument blank = new ImmutableDefaultStyledDocument();
		try {
			blank.insertStringTruely(0, "building document", lineStyles[0]);
		} catch (BadLocationException e) {
		}

		jTextPane.setDocument(blank);
	}

	public void buildDocument() {
		logging.debug(this, "building document");
		jTextPane.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// Switch to an blank document temporarily to avoid repaints

		document = new ImmutableDefaultStyledDocument(styleContext);

		// int level = levelList.indexOf(comboLevel.getSelectedItem()) + 1;
		// int level = levelList.indexOf(spinnerLevel.getValue()) + 1;
		int selLevel = levelList.indexOf(sliderLevel.getValue()) + 1;

		// logging.debug(this, "building document level " + selLevel );

		docLinestartPosition2lineCount = new TreeMap<>();
		lineCount2docLinestartPosition = new TreeMap<>();

		try {
			int i = 0;

			while (i < lines.length)
			// we check if we got a new call
			{
				boolean show = false;

				if (
				// showLevel == -1 ||
				lineLevels[i] <= selLevel) {
					show = true;
				}

				if (show && showTypeRestricted) {
					if (lineTypes[i] != selTypeIndex)
						show = false;
				}

				if (show) {

					docLinestartPosition2lineCount.put(document.getLength(), i);
					lineCount2docLinestartPosition.put(i, document.getLength());

					// logging.debug(this, " lines i " + i);
					// logging.debug(this, " lines i line ");
					// logging.debug(this, " lines i " + lines[i]);
					String no = "(" + i + ")";
					document.insertStringTruely(document.getLength(), /*
																		 * type + " j1 " + j1 + " j2 " + j2 + " ::: " +
																		 * ": " + lev + "--- " +
																		 */
							// "" + i + ": " + + lineLevels[i] + ":: " +
							String.format("%-10s", no) + lines[i] + '\n', lineStyles[i]);
				}

				i++;
			}
		} catch (BadLocationException e) {
		}
		jTextPane.setDocument(document);
		SwingUtilities.invokeLater(() -> jTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)));
		// logging.debug(this, "Text set" );
	}

	private void setLevelWithoutAction(Object l) {
		logging.debug(this, "setLevel " + l);

		Integer levelO = sliderLevel.getValue();
		if (levelO != l && sliderListener != null) {
			sliderLevel.removeChangeListener(sliderListener);
			sliderLevel.setValue((Integer) l);
			sliderLevel.addChangeListener(sliderListener);
		}

		/*
		 * Object levelO = spinnerLevel.getValue();
		 * if (levelO != l && spinnerListener != null)
		 * {
		 * spinnerLevel.removeChangeListener(spinnerListener);
		 * spinnerLevel.setValue(l);
		 * spinnerLevel.addChangeListener(spinnerListener);
		 * }
		 */

		/*
		 * Object levelO = comboLevel.getSelectedItem();
		 * if (levelO != l && comboListener != null)
		 * {
		 * comboLevel.removeActionListener(comboListener);
		 * comboLevel.setSelectedItem(l);
		 * comboLevel.addActionListener(comboListener);
		 * }
		 */

	}

	private void activateShowLevel() {
		// Object level = comboLevel.getSelectedItem();
		Integer level = sliderLevel.getValue();
		if (level > maxExistingLevel) {
			level = maxExistingLevel;
			sliderLevel.setValue(level);
			return;
		}

		configed.savedStates.savedMaxShownLogLevel.serialize(level);

		Integer oldLevel = showLevel;
		showLevel = level;
		logging.info(this, "activateShowLevel level, oldLevel, maxExistingLevel " + level + " , " + oldLevel + ", "
				+ maxExistingLevel);

		if (oldLevel != level && (level < maxExistingLevel || oldLevel < maxExistingLevel)) {
			// emptyDocument();

			int caretPosition = jTextPane.getCaretPosition();
			// logging.info(this, "caretPosition " + caretPosition);
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

			// logging.info(this, "line no " + lineNo);

			buildDocument();

			// logging.info(this, "finding new startPosition ");

			if (lineCount2docLinestartPosition.containsKey(lineNo)) {
				startPosition = lineCount2docLinestartPosition.get(lineNo) + offset;
				// logging.info(this, "new startPosition " + startPosition);
			} else {
				Iterator<Integer> linesIterator = lineCount2docLinestartPosition.keySet().iterator();
				int nextLineNo = 0;

				if (linesIterator.hasNext())
					nextLineNo = linesIterator.next();

				while (linesIterator.hasNext() && nextLineNo < lineNo)
					nextLineNo = linesIterator.next();

				startPosition = lineCount2docLinestartPosition.get(nextLineNo) + offset;
				// logging.info(this, "found startPosition " + startPosition);
			}

			try {
				jTextPane.setCaretPosition(startPosition);
				jTextPane.scrollRectToVisible(jTextPane.modelToView(startPosition));
				jTextPane.getCaret().setVisible(true);
				highlighter.removeAllHighlights();
			} catch (BadLocationException e) {
			}

		}
		/*
		 * else
		 * jTextPane.setDocument(document); //reset existing document
		 */

	}

	private class StringBlock {
		String s;
		int iStart;
		int iEnd;
		private int contentStart;
		private int contentEnd;
		boolean finish;
		boolean found;
		// String content;
		char startC;
		char endC;

		void setString(String s) {
			this.s = s;
		}

		String getContent() {
			return s.substring(contentStart, contentEnd).trim();
		}

		boolean hasFound() {
			// logging.info(this, "hasFound " + found);
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
			// logging.info(this, "treating line " + s + " we start at " + iStart);
			this.iStart = iStart;
			iEnd = iStart;
			finish = false;
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

			// contentEnd = s.indexOf(endC, contentStart);

			contentEnd = findContentEnd();

			if (contentEnd > -1)
				found = true;
			else
				return;

			iEnd = contentEnd;
			iEnd++;

			// logging.info(this, "forward, found content " + content + " iEnd " + iEnd);

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
		// logging.info(this, "parse lines.length " + lines.length);
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
				lev = new Integer("" + levC).intValue();
				if (lev > maxExistingLevel)
					maxExistingLevel = lev;

				lineLevels[i] = lev;

				lineStyle = getStyleByLevelNo(lev);
			}
			lineStyles[i] = lineStyle;

			// search type
			String type = "";
			int typeIndex = 0; // ""
			int nextStartI = 0;
			nextBlock.setString(lines[i]);
			testBlock.setString(lines[i]);
			nextBlock.forward(nextStartI, '[', ']');

			if (nextBlock.hasFound()) {
				// logging.info(this, "parse found part " + infoPart);

				nextStartI = nextBlock.getIEnd() + 1;

				testBlock.forward(nextStartI, '(', ')');
				if (testBlock.hasFound()) {
					nextStartI = testBlock.getIEnd() + 1;
				}
				nextBlock.forward(nextStartI, '[', ']');
			}

			if (nextBlock.hasFound()) {
				// logging.info(this, "parse found part " + infoPart);

				nextStartI = nextBlock.getIEnd() + 1;
				nextBlock.forward(nextStartI, '[', ']');
			}

			if (nextBlock.hasFound()) {
				// logging.info(this, "parse found part " + infoPart);
				type = nextBlock.getContent();

				typeIndex = typesList.indexOf(type);
				if (typeIndex == -1) {
					typeIndex = typesList.size();
					typesList.add(type);
				}
			}

			lineTypes[i] = typeIndex;

		}

		// logging.info(this, "parse result types " + typesList);
		adaptComboType();

	}

	private void adaptComboType() {
		comboType.setEnabled(false);
		comboModelTypes.removeAllElements();

		// if (typesList.size() > 1)
		if (typesList.size() > 0) {
			comboModelTypes.addElement(defaultType);
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
		if (s == null)
			logging.info(this, "Setting text");
		lines = s.split("\n");
		// logging.debug(this, " lines length " + lines.length);
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
			final int[] lineTypes, final List typesList, boolean showTypeRestricted, int selTypeIndex,
			int maxExistingLevel) {
		logging.debug(this, "setParsedText");
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

	/*
	 * private void setDocument(ImmutableDefaultStyledDocument document)
	 * {
	 * this.document = document;
	 * jTextPane.setDocument(this.document);
	 * }
	 */

	public void editSearchString() {
		jComboBoxSearch.requestFocus();
	}

	public void search() {
		logging.debug(this, "Searching string in log");
		jTextPane.requestFocus();
		searcher.comp.setCaretPosition(jTextPane.getCaretPosition());
		// change 08/2015: set lastReturnedOffset to start search at last caretPosition
		searcher.lastReturnedOffset = jTextPane.getCaretPosition();
		int offset = searcher.search(jComboBoxSearch.getSelectedItem().toString());
		if (!(jComboBoxSearch.getSelectedIndex() > -1)) { // does not exist
			jComboBoxSearch.addItem(jComboBoxSearch.getSelectedItem().toString());
			jComboBoxSearch.repaint();
		}
		if (offset != -1) {
			try {
				jTextPane.scrollRectToVisible(
						jTextPane.modelToView(offset + jComboBoxSearch.getSelectedItem().toString().length()));
				jTextPane.setCaretPosition(offset);
				jTextPane.getCaret().setVisible(true);
				searcher.comp.setCaretPosition(offset);
			} catch (BadLocationException e) {
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		logging.debug(this, "KeyEvent " + e);
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
					&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
				logging.info(this, "Ctrl-Plus");
				setFontSize("+");
			}

			else if (e.getSource() == jTextPane && (e.getKeyChar() == '-')
					&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
				logging.info(this, "Ctrl-Minus");
				setFontSize("-");
			}

		}

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == jTextPane) {
			if (e.getKeyChar() == '/' || e.getKeyChar() == '\u0006') // ctrl-f
			{
				editSearchString();
			}

			// f g h i j k l
			// 6 7 8 9 10 11 12
			if (e.getKeyChar() == 'n' || e.getKeyChar() == '\u000c' || e.getKeyCode() == KeyEvent.VK_F3) // "n", "ctrl-l", "F3" for repeat last search
			{
				search();
			}
			e.consume();
		}

	}

	// Interface ActionListener

	@Override
	public void actionPerformed(ActionEvent e) {
		// logging.info(this, "ActionEvent " + e);
		if (e.getSource() == buttonSearch) {
			search();
		} else if (e.getSource() == jComboBoxSearch) {
			search();
			jTextPane.requestFocusInWindow();
		} else if (e.getSource() == jCheckBoxCaseSensitive) {
			if (jCheckBoxCaseSensitive.isSelected()) {
				searcher.setCaseSensitivity(true);
			} else {
				searcher.setCaseSensitivity(false);
			}
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
			this.painter = new UnderlineHighlightPainter(Color.red);
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
			// logging.info(this, "search");
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
			if (newValue == false) {
				throw new IllegalArgumentException("UnderlineHighlighter only draws layered highlights");
			}
			super.setDrawsLayeredHighlights(true);
		}
	}

}
