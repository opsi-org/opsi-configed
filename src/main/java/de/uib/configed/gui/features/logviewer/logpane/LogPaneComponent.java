/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;

import de.uib.configed.app.Main;
import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.logviewer.logpane.view.AdaptingSlider;
import de.uib.configed.gui.features.logviewer.logpane.view.LogTextPane;
import de.uib.configed.gui.features.logviewer.logpane.view.TextLineNumber;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class LogPaneComponent extends AbstractTeaComponent<LogPaneModel, LogPaneMsg, LogPaneEffect>
		implements KeyListener {
	private static final int SLIDER_W = 180;

	public static final int MIN_LEVEL = 1;
	public static final int MAX_LEVEL = 9;

	private static final int TYPES_LIST_MAX_SHOW_COUNT = 25;

	protected LogTextPane logTextPane;

	private JScrollPane jScrollPane;
	private JLabel labelSearch;

	private JComboBox<String> jComboBoxSearch;

	private JToolBar jToolBar;
	private JLabel labelLevel;
	private AdaptingSlider sliderLevel;
	private JLabel labelDisplayRestriction;
	private JComboBox<String> comboType;
	private DefaultComboBoxModel<String> comboModelTypes;

	private JPanel rootPanel;
	private ActionListener comboTypeListener;

	public LogPaneComponent() {
		super();
	}

	public LogPaneComponent(LogPaneModel model) {
		super(model);
	}

	@Override
	protected LogPaneModel initModel() {
		return LogPaneModel.builder().build();
	}

	@Override
	protected UpdateResult<LogPaneModel, LogPaneEffect> updateModel(LogPaneMsg msg, LogPaneModel model) {
		return LogPaneUpdate.update(msg, model);
	}

	@Override
	protected JComponent renderView(LogPaneModel model, Consumer<LogPaneMsg> dispatch) {
		Logging.info(this, "initializing");

		initComponents();

		setLayout();

		if (model.isWithPopup()) {
			initPopupMenu();
		}

		return rootPanel;
	}

	@Override
	protected void refreshView() {
		adaptSlider();
		adaptComboType();

		activateShowLevel(model.getShowLevel());
		logTextPane.applyFontSize(model.getFontSize());
		logTextPane.setShowLevel(model.getShowLevel());
		logTextPane.applyType(model.getSelectedType());

		if (model.isNeedsRebuild()) {
			LogTextPane.CaretContext context = logTextPane.getCaretContext(model.getCaretPosition());
			logTextPane.buildDocument();
			logTextPane.setCaretPosition(logTextPane.computeCaretFromContext(context));
			logTextPane.getCaret().setVisible(true);
		}
	}

	@Override
	protected void handleEffect(LogPaneEffect effect) {
		switch (effect) {
		case LogPaneEffect.SimpleEffect e -> handleSimpleEffect(e);
		}
	}

	private void handleSimpleEffect(LogPaneEffect.SimpleEffect e) {
		switch (e) {
		case PERFORM_SEARCH -> search();
		case RELOAD_LOG -> reload();
		case COPY_CONTENTS -> copyTextToClipboard();
		case PARSE_LOG -> parse(model.getLogText());
		case DOWNLOAD_LOG -> download();
		case DOWNLOAD_LOG_AS_ZIP -> downloadAsZip();
		case DOWNLOAD_ALL_AS_ZIP -> downloadAllAsZip();
		case FLOAT_EXTERNAL -> floatExternal();
		}
	}

	private void initComponents() {
		rootPanel = new JPanel();
		logTextPane = new LogTextPane(model.getFontSize());
		logTextPane.addKeyListener(this);
		logTextPane.getInputMap().put(KeyStroke.getKeyStroke("ctrl C"), "copyRaw");
		logTextPane.getActionMap().put("copyRaw", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatch(LogPaneMsg.SimpleMsg.COPY_CONTENTS);
			}
		});
		logTextPane.addCaretListener((CaretEvent e) -> {
			int offset = e.getDot();
			if (offset != model.getCaretPosition()) {
				dispatch(new LogPaneMsg.ChangeCaretPosition(e.getDot()));
			}
		});

		TextLineNumber lineNumber = new TextLineNumber(logTextPane);
		lineNumber.setUpdateFont(true);
		lineNumber.setCurrentLineForeground(Globals.OPSI_MAGENTA);
		jScrollPane = new JScrollPane();
		jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		jScrollPane.getViewport().add(logTextPane);
		jScrollPane.setRowHeaderView(lineNumber);
		rootPanel.add(jScrollPane, BorderLayout.CENTER);

		labelSearch = new JLabel(Configed.getResourceValue("search"));

		JToggleButton buttonCaseSensitive = new JToggleButton(Icons.getIntellijIcon("matchCase"));
		buttonCaseSensitive.setSelectedIcon(Icons.getSelectedIntellijIcon("matchCase"));
		buttonCaseSensitive.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		buttonCaseSensitive.setSelected(false);
		buttonCaseSensitive.addActionListener(
				event -> dispatch(new LogPaneMsg.ToggleCaseSensitivity(buttonCaseSensitive.isSelected())));

		jComboBoxSearch = new JComboBox<>(model.getSearchHistory().toArray(String[]::new));
		jComboBoxSearch.setToolTipText(Configed.getResourceValue("TextPane.jComboBoxSearch.toolTip"));
		jComboBoxSearch.setEditable(true);
		((JTextField) jComboBoxSearch.getEditor().getEditorComponent())
				.putClientProperty("JTextField.trailingComponent", buttonCaseSensitive);
		jComboBoxSearch.addActionListener((ActionEvent event) -> {
			dispatch(new LogPaneMsg.Search(jComboBoxSearch.getEditor().getItem().toString()));
			logTextPane.requestFocusInWindow();
		});

		JButton buttonSearch = new JButton(Icons.getIntellijIcon("search"));
		buttonSearch.addActionListener(
				event -> dispatch(new LogPaneMsg.Search(jComboBoxSearch.getEditor().getItem().toString())));

		JButton buttonFontPlus = new JButton(Icons.getIntellijIcon("zoomIn"));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("LogPane.fontPlus"));
		buttonFontPlus.addActionListener(event -> dispatch(LogPaneMsg.SimpleMsg.INCREASE_FONT_SIZE));

		JButton buttonFontMinus = new JButton(Icons.getIntellijIcon("zoomOut"));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("LogPane.fontMinus"));
		buttonFontMinus.addActionListener(event -> dispatch(LogPaneMsg.SimpleMsg.DECREASE_FONT_SIZE));

		jToolBar = new JToolBar();
		jToolBar.add(buttonSearch);
		jToolBar.add(buttonFontMinus);
		jToolBar.add(buttonFontPlus);

		labelLevel = new JLabel(Configed.getResourceValue("TextPane.jLabel_level"));

		Logging.info(this, "levels minL, maxL ", model.getMinLevel(), ", ", model.getMaxLevel());

		sliderLevel = new AdaptingSlider(this, model.getMinLevel(), model.getMaxLevel(),
				model.getShowLevel() == MIN_LEVEL ? logTextPane.produceInitialMaxShowLevel() : model.getShowLevel());
		sliderLevel.setValue(model.getShowLevel());

		labelDisplayRestriction = new JLabel(Configed.getResourceValue("TextPane.EventType"));

		comboModelTypes = new DefaultComboBoxModel<>();
		comboModelTypes.addAll(model.getTypesList());

		comboType = new JComboBox<>(comboModelTypes);
		comboType.setEnabled(!model.getTypesList().isEmpty());
		comboType.setEditable(false);
		comboTypeListener = event -> dispatch(new LogPaneMsg.ChangeEventType((String) comboType.getSelectedItem()));
		comboType.addActionListener(comboTypeListener);
		comboType.setSelectedItem(model.getSelectedType());
	}

	private void setLayout() {
		GroupLayout layoutCommandpane = new GroupLayout(rootPanel);
		rootPanel.setLayout(layoutCommandpane);

		layoutCommandpane
				.setHorizontalGroup(
						layoutCommandpane.createParallelGroup().addComponent(jScrollPane)
								.addGroup(
										layoutCommandpane.createSequentialGroup().addGap(Globals.GAP_SIZE)
												.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(jComboBoxSearch, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addGap(Globals.GAP_SIZE * 2)
												.addComponent(jToolBar, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE * 2)
												.addComponent(labelDisplayRestriction, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(comboType, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
												.addGap(Globals.GAP_SIZE * 2)
												.addComponent(labelLevel, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(Globals.GAP_SIZE)
												.addComponent(sliderLevel, SLIDER_W, SLIDER_W, SLIDER_W)
												.addGap(Globals.GAP_SIZE)));

		layoutCommandpane.setVerticalGroup(layoutCommandpane.createSequentialGroup().addComponent(jScrollPane)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutCommandpane.createParallelGroup(Alignment.CENTER)
						.addComponent(labelSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jToolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelDisplayRestriction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(sliderLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));
	}

	private void initPopupMenu() {
		Integer[] popups;

		if (Main.isLogviewer()) {
			popups = new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_COPY,
					PopupMenuTrait.POPUP_DOWNLOAD, PopupMenuTrait.POPUP_FLOATING_COPY };
		} else {
			popups = new Integer[] { PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_COPY,
					PopupMenuTrait.POPUP_DOWNLOAD, PopupMenuTrait.POPUP_DOWNLOAD_AS_ZIP,
					PopupMenuTrait.POPUP_DOWNLOAD_ALL_AS_ZIP, PopupMenuTrait.POPUP_FLOATING_COPY };
		}

		new PopupMenuTrait(popups, new JComponent[] { logTextPane }) {
			@Override
			public void action(int p) {
				treatPopupAction(p);
			}
		};
	}

	private void treatPopupAction(int p) {
		switch (p) {
		case PopupMenuTrait.POPUP_RELOAD:
			dispatch(LogPaneMsg.SimpleMsg.RELOAD_LOG);
			break;
		case PopupMenuTrait.POPUP_COPY:
			dispatch(LogPaneMsg.SimpleMsg.COPY_CONTENTS);
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD:
			dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_LOG);
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD_AS_ZIP:
			dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_LOG_AS_ZIP);
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD_ALL_AS_ZIP:
			dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_ALL_AS_ZIP);
			break;
		case PopupMenuTrait.POPUP_FLOATING_COPY:
			dispatch(LogPaneMsg.SimpleMsg.FLOAT_EXTERNAL);
			break;
		default:
			Logging.warning(this, "no case found for popupMenuTrait in LogPane");
			break;
		}
	}

	public Integer getMaxExistingLevel() {
		return model.getMaxExistingLevel();
	}

	public void setCaretPosition(int caretPosition) {
		dispatch(new LogPaneMsg.ChangeCaretPosition(caretPosition));
	}

	private void copyTextToClipboard() {
		String selectedText = logTextPane.getSelectedText();
		String textToCopy = (selectedText != null && !selectedText.isEmpty()) ? selectedText : logTextPane.getText();
		StringSelection selection = new StringSelection(textToCopy);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}

	public void reload() {
		Logging.info(this, "reload action");
		setLevelWithoutAction(logTextPane.produceInitialMaxShowLevel());
	}

	public void download() {
		Logging.debug(this, "save action");
	}

	protected void downloadAsZip() {
		Logging.debug(this, "save as zip action");
	}

	protected void downloadAllAsZip() {
		Logging.debug(this, "save all as zip action");
	}

	private void floatExternal() {
		LogPaneComponent copyOfMe = new LogPaneComponent(
				model.toBuilder().minLevel(MIN_LEVEL).withPopup(false).build());
		externalize(copyOfMe, model.getTitle(), rootPanel.getParent().getSize());
	}

	public void externalize(String title, Dimension size) {
		externalize(this, title, size);
	}

	private void externalize(LogPaneComponent logPane, String title, Dimension size) {
		JDialog dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setContentPane(logPane.initUI());
		dialog.setSize(size);
		dialog.setResizable(true);
		dialog.setLocationRelativeTo(rootPanel.getParent());
		dialog.setVisible(true);

		logPane.logTextPane.setParsedText(logTextPane);
		logPane.logTextPane.setShowLevel(model.getShowLevel());
		logPane.logTextPane.buildDocument();
		logPane.logTextPane.setCaretPosition(model.getCaretPosition());
		logPane.logTextPane.getCaret().setVisible(true);
	}

	public String getInfo() {
		return model.getInfo();
	}

	private void setLevelWithoutAction(Object l) {
		Logging.debug(this, "setLevel ", l);

		Integer level = sliderLevel.getValue();
		if (level != l) {
			sliderLevel.removeChangeListener(sliderLevel);
			sliderLevel.setValue((Integer) l);
			sliderLevel.addChangeListener(sliderLevel);
		}
	}

	private void activateShowLevel(int level) {
		if (level > model.getMaxExistingLevel()) {
			level = model.getMaxExistingLevel();
			sliderLevel.setValue(level);
			return;
		} else {
			sliderLevel.setValue(level);
		}

		if (Configed.getSavedStates() != null) {
			Configed.getSavedStates().setProperty("savedMaxShownLogLevel", String.valueOf(level));
		}
	}

	private void adaptComboType() {
		comboType.setEnabled(false);
		comboType.removeActionListener(comboTypeListener);
		comboModelTypes.removeAllElements();

		if (!model.getTypesList().isEmpty()) {
			for (String type : model.getTypesList()) {
				comboModelTypes.addElement(type);
			}
			comboType.setEnabled(true);

			int maxRowCount = model.getTypesList().size() + 1;
			if (maxRowCount > TYPES_LIST_MAX_SHOW_COUNT) {
				maxRowCount = TYPES_LIST_MAX_SHOW_COUNT;
			}

			comboType.setMaximumRowCount(maxRowCount);
		}
		comboType.setSelectedItem(model.getSelectedType());
		comboType.addActionListener(comboTypeListener);
	}

	private void adaptSlider() {
		sliderLevel.produceLabels();
	}

	private void parse(String s) {
		int showLevel = logTextPane.parse(s);
		dispatch(new LogPaneMsg.LogParsed(logTextPane.getParsedData(), showLevel));
	}

	private void search() {
		Logging.debug(this, "Searching string in log");
		Object item = jComboBoxSearch.getEditor().getItem();

		if (item == null || item.toString().isEmpty()) {
			Logging.info(this, "item to search for is null or empty, do nothing");
			return;
		}

		// does not exist
		if (jComboBoxSearch.getSelectedIndex() <= -1) {
			jComboBoxSearch.addItem(item.toString());
			jComboBoxSearch.repaint();
		}

		logTextPane.search(item.toString());
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, "KeyEvent ", e);

		if (e.getKeyCode() == KeyEvent.VK_F3 || e.getKeyCode() == KeyEvent.VK_ENTER) {
			dispatch(new LogPaneMsg.Search(jComboBoxSearch.getEditor().getItem().toString()));
		} else if (e.getKeyCode() == KeyEvent.VK_PLUS && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			Logging.info(this, "Ctrl-Plus");
			dispatch(LogPaneMsg.SimpleMsg.INCREASE_FONT_SIZE);
		} else if (e.getKeyCode() == KeyEvent.VK_MINUS && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			Logging.info(this, "Ctrl-Minus");
			dispatch(LogPaneMsg.SimpleMsg.DECREASE_FONT_SIZE);
		} else {
			// Do nothing on other keys on jTextPane
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == logTextPane) {
			if (e.getKeyChar() == '/' || e.getKeyChar() == '\u0006') {
				jComboBoxSearch.requestFocus();
			}

			if (e.getKeyChar() == 'n' || e.getKeyChar() == '\u000c' || e.getKeyCode() == KeyEvent.VK_F3) {
				search();
				dispatch(new LogPaneMsg.Search(jComboBoxSearch.getEditor().getItem().toString()));
			}
			e.consume();
		}
	}

	public String[] getLines() {
		return logTextPane.getLines();
	}
}
