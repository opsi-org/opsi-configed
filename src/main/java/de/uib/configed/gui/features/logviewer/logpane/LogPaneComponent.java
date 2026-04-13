/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.logviewer.logpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
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
import de.uib.configed.gui.share.SwingUtils;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class LogPaneComponent extends AbstractTeaComponent<LogPaneModel, LogPaneMsg, LogPaneEffect> {
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
		addKeyBindings();

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
		logTextPane.addCaretListener((CaretEvent e) -> {
			int offset = e.getDot();
			if (offset != model.getCaretPosition()) {
				dispatch(new LogPaneMsg.ChangeCaretPosition(e.getDot()));
			}
		});

		TextLineNumber lineNumber = new TextLineNumber(logTextPane);
		lineNumber.setUpdateFont(true);
		lineNumber.setDigitAlignment(TextLineNumber.CENTER);
		lineNumber.setCurrentLineBackground(Globals.getLogPaneCurrentLineBackground());
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

	private void addKeyBindings() {
		SwingUtils.addKeyBindingToJComponent(logTextPane, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
				() -> dispatch(new LogPaneMsg.Search((String) jComboBoxSearch.getEditor().getItem())),
				JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				() -> dispatch(new LogPaneMsg.Search((String) jComboBoxSearch.getEditor().getItem())),
				JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
				() -> dispatch(new LogPaneMsg.Search((String) jComboBoxSearch.getEditor().getItem())),
				JComponent.WHEN_FOCUSED);

		// The Binding with KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0) does not work on some systems (e.g. German keyboard)
		SwingUtils.addKeyBindingToJComponent(logTextPane, KeyStroke.getKeyStroke('/'), jComboBoxSearch::requestFocus,
				JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane,
				KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK),
				() -> dispatch(LogPaneMsg.SimpleMsg.INCREASE_FONT_SIZE), JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane,
				KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
				() -> dispatch(LogPaneMsg.SimpleMsg.DECREASE_FONT_SIZE), JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
				() -> dispatch(LogPaneMsg.SimpleMsg.COPY_CONTENTS), JComponent.WHEN_FOCUSED);

		SwingUtils.addKeyBindingToJComponent(logTextPane,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), () -> {
					String selected = logTextPane.getSelectedText();

					if (selected != null && !selected.isEmpty()) {
						jComboBoxSearch.getEditor().setItem(selected.trim());
					}

					if (jComboBoxSearch != KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) {
						jComboBoxSearch.requestFocusInWindow();
						dispatch(new LogPaneMsg.Search((String) jComboBoxSearch.getEditor().getItem()));
					}
				});
	}

	private void setLayout() {
		JPanel bottomPanel = new JPanel(new MigLayout(
				"insets 0, fillx", "[]" + Globals.GAP_SIZE + "[grow]" + (Globals.GAP_SIZE * 2) + "[]" + Globals.GAP_SIZE
						+ "[][grow]" + (Globals.GAP_SIZE * 2) + "[]" + Globals.GAP_SIZE + "[" + SLIDER_W + "!]",
				"[center]"));
		bottomPanel.add(labelSearch);
		bottomPanel.add(jComboBoxSearch, "growx");
		bottomPanel.add(jToolBar);
		bottomPanel.add(labelDisplayRestriction);
		bottomPanel.add(comboType, "growx");
		bottomPanel.add(labelLevel);
		bottomPanel.add(sliderLevel);

		rootPanel.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow]", "[grow]" + Globals.MIN_GAP_SIZE + "[]"));
		rootPanel.add(jScrollPane, "grow");
		rootPanel.add(bottomPanel, "growx");
	}

	private void initPopupMenu() {
		Map<Integer, Runnable> popups = new HashMap<>();
		popups.put(PopupMenuTrait.POPUP_RELOAD, () -> dispatch(LogPaneMsg.SimpleMsg.RELOAD_LOG));
		popups.put(PopupMenuTrait.POPUP_COPY, () -> dispatch(LogPaneMsg.SimpleMsg.COPY_CONTENTS));
		popups.put(PopupMenuTrait.POPUP_DOWNLOAD, () -> dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_LOG));
		popups.put(PopupMenuTrait.POPUP_FLOATING_COPY, () -> dispatch(LogPaneMsg.SimpleMsg.FLOAT_EXTERNAL));

		if (!Main.isLogviewer()) {
			popups.put(PopupMenuTrait.POPUP_DOWNLOAD_AS_ZIP, () -> dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_LOG_AS_ZIP));
			popups.put(PopupMenuTrait.POPUP_DOWNLOAD_ALL_AS_ZIP,
					() -> dispatch(LogPaneMsg.SimpleMsg.DOWNLOAD_ALL_AS_ZIP));
		}

		PopupMenuTrait.createAndBindJPopupMenu(logTextPane, popups);
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

	public String[] getLines() {
		return logTextPane.getLines();
	}
}
