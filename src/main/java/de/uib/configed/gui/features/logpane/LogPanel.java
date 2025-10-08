/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.logpane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
import javax.swing.ScrollPaneConstants;

import de.uib.configed.app.Main;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;

public class LogPanel extends JPanel implements KeyListener {
	private static final int SLIDER_W = 180;

	public static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 9;

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

	private String title;
	private String info;

	public LogPanel(String defaultText, boolean withPopup) {
		Logging.info(this, "initializing");
		title = "";
		info = "";

		initComponents(defaultText);

		setLayout();

		if (withPopup) {
			initPopupMenu();
		}
	}

	private void initComponents(String defaultText) {
		logTextPane = new LogTextPane(defaultText);
		if (defaultText != null) {
			logTextPane.setText(defaultText);
		}

		logTextPane.addKeyListener(this);

		jScrollPane = new JScrollPane();
		jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		jScrollPane.getViewport().add(logTextPane);
		super.add(jScrollPane, BorderLayout.CENTER);

		labelSearch = new JLabel(Configed.getResourceValue("search"));

		JToggleButton buttonCaseSensitive = new JToggleButton(Icons.getIntellijIcon("matchCase"));
		buttonCaseSensitive.setSelectedIcon(Icons.getSelectedIntellijIcon("matchCase"));
		buttonCaseSensitive.setToolTipText(Configed.getResourceValue("TextPane.jCheckBoxCaseSensitive.toolTip"));
		buttonCaseSensitive.setSelected(false);
		buttonCaseSensitive
				.addActionListener(event -> logTextPane.setCaseSensitivity(buttonCaseSensitive.isSelected()));

		jComboBoxSearch = new JComboBox<>();
		jComboBoxSearch.setToolTipText(Configed.getResourceValue("TextPane.jComboBoxSearch.toolTip"));
		jComboBoxSearch.setEditable(true);
		((JTextField) jComboBoxSearch.getEditor().getEditorComponent())
				.putClientProperty("JTextField.trailingComponent", buttonCaseSensitive);
		jComboBoxSearch.addActionListener((ActionEvent event) -> {
			search();
			logTextPane.requestFocusInWindow();
		});

		JButton buttonSearch = new JButton(Icons.getIntellijIcon("search"));
		buttonSearch.addActionListener(event -> search());

		JButton buttonFontPlus = new JButton(Icons.getIntellijIcon("zoomIn"));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("LogPane.fontPlus"));
		buttonFontPlus.addActionListener(event -> increaseFontSize());

		JButton buttonFontMinus = new JButton(Icons.getIntellijIcon("zoomOut"));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("LogPane.fontMinus"));
		buttonFontMinus.addActionListener(event -> reduceFontSize());

		jToolBar = new JToolBar();
		jToolBar.add(buttonSearch);
		jToolBar.add(buttonFontMinus);
		jToolBar.add(buttonFontPlus);

		labelLevel = new JLabel(Configed.getResourceValue("TextPane.jLabel_level"));

		Logging.info(this, "levels minL, maxL ", MIN_LEVEL, ", ", MAX_LEVEL);

		sliderLevel = new AdaptingSlider(this, MIN_LEVEL, MAX_LEVEL, logTextPane.produceInitialMaxShowLevel());

		labelDisplayRestriction = new JLabel(Configed.getResourceValue("TextPane.EventType"));

		comboModelTypes = new DefaultComboBoxModel<>();
		comboType = new JComboBox<>(comboModelTypes);

		comboType.setEnabled(false);
		comboType.setEditable(false);

		comboType.addActionListener(actionEvent -> logTextPane.applyType(comboType.getSelectedItem()));
	}

	private void setLayout() {
		GroupLayout layoutCommandpane = new GroupLayout(this);
		setLayout(layoutCommandpane);

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

		PopupMenuTrait popupMenu = new PopupMenuTrait(popups) {
			@Override
			public void action(int p) {
				treatPopupAction(p);
			}
		};

		popupMenu.addPopupListenersTo(new JComponent[] { logTextPane });
	}

	private void treatPopupAction(int p) {
		switch (p) {
		case PopupMenuTrait.POPUP_RELOAD:
			reload();
			break;
		case PopupMenuTrait.POPUP_COPY:
			copyTextToClipboard();
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD:
			download();
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD_AS_ZIP:
			downloadAsZip();
			break;
		case PopupMenuTrait.POPUP_DOWNLOAD_ALL_AS_ZIP:
			downloadAllAsZip();
			break;
		case PopupMenuTrait.POPUP_FLOATING_COPY:
			floatExternal();
			break;

		default:
			Logging.warning(this, "no case found for popupMenuTrait in LogPane");
			break;
		}
	}

	public Integer getMaxExistingLevel() {
		return logTextPane.getMaxExistingLevel();
	}

	public void removeAllHighlights() {
		logTextPane.removeAllHighlights();
	}

	public void reduceFontSize() {
		logTextPane.reduceFontSize();
	}

	public void increaseFontSize() {
		logTextPane.increaseFontSize();
	}

	public int getCaretPosition() {
		return logTextPane.getCaretPosition();
	}

	public void setCaretPosition(int caretPosition) {
		try {
			logTextPane.setCaretPosition(caretPosition);
		} catch (IllegalArgumentException e) {
			int maxPos = logTextPane.getDocument().getLength();
			int safePos = Math.clamp(caretPosition, 0, maxPos);
			Logging.info(this, "catching IllegalArgumentException ", e.getMessage());
			Logging.info(this, "Failed to restore caret position ", caretPosition,
					": index out of bounds (possibly due to changed log content). Using nearest valid position: ",
					safePos);
			logTextPane.setCaretPosition(safePos);
		}
	}

	public void reload() {
		Logging.info(this, "reload action");
		setLevelWithoutAction(logTextPane.produceInitialMaxShowLevel());
	}

	public void copyTextToClipboard() {
		String selectedText = logTextPane.getSelectedText();
		String textToCopy = (selectedText != null && !selectedText.isEmpty()) ? selectedText : logTextPane.getText();
		StringBuilder sb = removeLineNumbers(textToCopy);
		StringSelection selection = new StringSelection(sb.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}

	private StringBuilder removeLineNumbers(String text) {
		StringBuilder sb = new StringBuilder();
		String[] lines = text.split("\\R");
		for (String line : lines) {
			if (line.matches("^\\(\\d+\\)\\s+.*")) {
				int logStart = line.indexOf(')') + 1;
				while (logStart < line.length() && Character.isWhitespace(line.charAt(logStart))) {
					logStart++;
				}
				sb.append(line.substring(logStart));
			} else {
				sb.append(line);
			}
			sb.append("\n");
		}
		return sb;
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

	public void floatExternal() {
		LogPanel copyOfMe = new LogPanel("", false);
		copyOfMe.setLevelWithoutAction(logTextPane.getShowLevel());
		copyOfMe.logTextPane.setShowLevel(logTextPane.getShowLevel());
		copyOfMe.logTextPane.setParsedText(logTextPane);
		copyOfMe.adaptComboType();
		copyOfMe.logTextPane.buildDocument();

		copyOfMe.logTextPane.setCaretPosition(logTextPane.getCaretPosition());
		copyOfMe.adaptSlider();
		externalize(copyOfMe, title, getParent().getSize());
	}

	public void externalize(String title, Dimension size) {
		externalize(this, title, size);
	}

	private void externalize(LogPanel logPane, String title, Dimension size) {
		JDialog dialog = new JDialog();
		dialog.setTitle(title);
		dialog.setContentPane(logPane);
		dialog.setSize(size);
		dialog.setResizable(true);
		dialog.setLocationRelativeTo(this.getParent());
		dialog.setVisible(true);
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

	private void setLevelWithoutAction(Object l) {
		Logging.debug(this, "setLevel ", l);

		Integer levelO = sliderLevel.getValue();
		if (levelO != l) {
			sliderLevel.removeChangeListener(sliderLevel);
			sliderLevel.setValue((Integer) l);
			sliderLevel.addChangeListener(sliderLevel);
		}
	}

	public void activateShowLevel() {
		Integer level = sliderLevel.getValue();
		if (level > logTextPane.getMaxExistingLevel()) {
			level = logTextPane.getMaxExistingLevel();
			sliderLevel.setValue(level);
			return;
		}

		if (Configed.getSavedStates() != null) {
			Configed.getSavedStates().setProperty("savedMaxShownLogLevel", String.valueOf(level));
		}

		Integer oldLevel = logTextPane.getShowLevel();
		logTextPane.setShowLevel(level);

		Logging.info(this, "activateShowLevel level, oldLevel, maxExistingLevel ", level, " , ", oldLevel, ", ",
				logTextPane.getMaxExistingLevel());

		if (!oldLevel.equals(level)
				&& (level < logTextPane.getMaxExistingLevel() || oldLevel < logTextPane.getMaxExistingLevel())) {
			logTextPane.rebuildDocumentWithNewLevel(jComboBoxSearch.getSelectedItem());
		}
	}

	private void adaptComboType() {
		comboType.setEnabled(false);
		comboModelTypes.removeAllElements();

		if (!logTextPane.getTypesList().isEmpty()) {
			comboModelTypes.addElement(LogTextPane.DEFAULT_TYPE);
			for (String type : logTextPane.getTypesList()) {
				comboModelTypes.addElement(type);
			}
			comboType.setEnabled(true);

			int maxRowCount = logTextPane.getTypesList().size() + 1;
			if (maxRowCount > TYPES_LIST_MAX_SHOW_COUNT) {
				maxRowCount = TYPES_LIST_MAX_SHOW_COUNT;
			}

			comboType.setMaximumRowCount(maxRowCount);
		}
	}

	private void adaptSlider() {
		sliderLevel.produceLabels();
	}

	public void setLogText(String s) {
		int showLevel = logTextPane.setLogText(s);
		adaptComboType();
		adaptSlider();
		sliderLevel.setValue(showLevel);
		logTextPane.buildDocument();
		logTextPane.setCaretPosition(0);
		logTextPane.getCaret().setVisible(true);
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
			search();
		} else if (e.getKeyCode() == KeyEvent.VK_PLUS && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			Logging.info(this, "Ctrl-Plus");
			increaseFontSize();
		} else if (e.getKeyCode() == KeyEvent.VK_MINUS && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			Logging.info(this, "Ctrl-Minus");
			reduceFontSize();
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
			}
			e.consume();
		}
	}

	public String[] getLines() {
		return logTextPane.getLines();
	}
}
