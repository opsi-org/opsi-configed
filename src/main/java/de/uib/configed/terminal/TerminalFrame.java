/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;

import com.jediterm.terminal.ui.JediTermWidget;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSelectionList;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public final class TerminalFrame {
	private static final int DEFAULT_TERMINAL_COLUMNS = 80;
	private static final int DEFAULT_TERMINAL_ROWS = 24;
	private static final String CONFIG_SERVER_SESSION_CHANNEL = "service:config:terminal";

	private TerminalWidget widget;
	private JFrame frame;
	private JProgressBar fileUploadProgressBar;
	private JLabel uploadedFilesLabel;
	private JLabel fileNameLabel;
	private JPanel southPanel;

	private TerminalSettingsProvider settingsProvider;

	public TerminalWidget getTerminalWidget() {
		return widget;
	}

	private JediTermWidget createTerminalWidget() {
		if (settingsProvider == null) {
			settingsProvider = new TerminalSettingsProvider();
		}
		widget = new TerminalWidget(this, DEFAULT_TERMINAL_COLUMNS, DEFAULT_TERMINAL_ROWS, settingsProvider);
		widget.init();

		return widget;
	}

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setIconImage(Utils.getMainIcon());
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setJMenuBar(createJMenuBar());

		JPanel allPane = new JPanel();

		GroupLayout allLayout = new GroupLayout(allPane);
		allPane.setLayout(allLayout);

		JPanel northPanel = createNorthPanel();
		southPanel = createSouthPanel();

		allLayout
				.setVerticalGroup(allLayout.createSequentialGroup()
						.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup().addComponent(northPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGroup(allLayout.createSequentialGroup().addComponent(southPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		frame.add(allPane);

		frame.setSize(600, 400);
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				widget.close();
			}

			@Override
			public void windowActivated(WindowEvent e) {
				setSelectedTheme(TerminalSettingsProvider.getTerminalThemeInUse());
			}
		});
	}

	public JMenuBar createJMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createViewMenu());
		return menuBar;
	}

	private JMenu createFileMenu() {
		JMenuItem jMenuItemNewWindow = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewWindow"));
		jMenuItemNewWindow.addActionListener((ActionEvent e) -> {
			TerminalFrame terminal = new TerminalFrame();
			terminal.display();
			widget.getMessagebus().connectTerminal(terminal);
		});

		JMenuItem jMenuItemSession = new JMenuItem(Configed.getResourceValue("Terminal.menuBar.fileMenu.session"));
		jMenuItemSession.addActionListener((ActionEvent e) -> displaySessionsDialog());

		JMenu jMenuTheme = new JMenu(Configed.getResourceValue("theme"));
		ButtonGroup groupThemes = new ButtonGroup();

		for (final String theme : Messages.getAvailableThemes()) {
			JMenuItem themeItem = new JRadioButtonMenuItem(Messages.getThemeTranslation(theme));
			Logging.debug("selectedTheme in Terminal " + theme);
			themeItem.setSelected(TerminalSettingsProvider.getTerminalThemeInUse().equals(theme));
			jMenuTheme.add(themeItem);
			groupThemes.add(themeItem);

			themeItem.addActionListener((ActionEvent e) -> setSelectedTheme(theme));
		}

		JMenu menuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));
		menuFile.add(jMenuItemNewWindow);
		menuFile.add(jMenuItemSession);
		menuFile.add(jMenuTheme);
		return menuFile;
	}

	private void displaySessionsDialog() {
		FSelectionList sessionsDialog = new FSelectionList(frame, Configed.getResourceValue("Terminal.session.title"),
				true, new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") },
				500, 300);
		List<String> clientsConnectedByMessagebus = new ArrayList<>(PersistenceControllerFactory
				.getPersistenceController().getHostDataService().getMessagebusConnectedClients());
		clientsConnectedByMessagebus.add("Configserver");
		Collections.sort(clientsConnectedByMessagebus);
		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.setVisible(true);

		if (sessionsDialog.getResult() == 2) {
			widget.changeSession(sessionsDialog.getSelectedValue());
		}
	}

	private JMenu createViewMenu() {
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> widget.increaseFontSize());

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> widget.decreaseFontSize());

		JMenu jMenuView = new JMenu(Configed.getResourceValue("LogFrame.jMenuView"));
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);
		return jMenuView;
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		JediTermWidget termWidget = createTerminalWidget();

		northLayout
				.setVerticalGroup(northLayout.createSequentialGroup().addComponent(termWidget, 0, 0, Short.MAX_VALUE));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addComponent(termWidget, 0, 0, Short.MAX_VALUE)));

		return northPanel;
	}

	private void setSelectedTheme(String selectedTheme) {
		TerminalSettingsProvider.setTerminalTheme(selectedTheme);

		if (widget != null) {
			widget.repaint();
		}
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setVisible(false);

		JLabel uploadingFileLabel = new JLabel(Configed.getResourceValue("Terminal.uploadingFile"));
		fileNameLabel = new JLabel();
		uploadedFilesLabel = new JLabel();

		fileUploadProgressBar = new JProgressBar();
		fileUploadProgressBar.setStringPainted(true);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);
		southLayout.setHorizontalGroup(southLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(uploadingFileLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(fileNameLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(uploadedFilesLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(fileUploadProgressBar, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(0, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(southLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(uploadingFileLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fileNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(uploadedFilesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fileUploadProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		return southPanel;
	}

	public void indicateFileUpload(File file, int uploadedFiles, int totalFiles) {
		showFileUploadProgress(true);

		try {
			fileUploadProgressBar.setMaximum((int) Files.size(file.toPath()));
		} catch (IOException e) {
			Logging.warning(this, "unable to retrieve file size: ", e);
		}

		uploadedFilesLabel.setText(uploadedFiles + "/" + totalFiles);
		fileNameLabel.setText(file.getAbsolutePath());
	}

	public void updateFileUploadProgressBar(int progress, int fileSize) {
		if (!southPanel.isVisible()) {
			showFileUploadProgress(true);
		}

		ByteUnitConverter converter = new ByteUnitConverter();
		ByteUnit byteUnit = converter.detectByteUnit(fileSize);
		String uploadedFileSize = converter.asString(converter.convertByteUnit(progress, byteUnit), byteUnit);
		String totalFileSize = converter.asString(converter.convertByteUnit(fileSize, byteUnit), byteUnit);

		fileUploadProgressBar.setValue(progress);
		fileUploadProgressBar.setString(uploadedFileSize + "/" + totalFileSize);
		fileUploadProgressBar.repaint();
	}

	public void showFileUploadProgress(boolean show) {
		southPanel.setVisible(show);
	}

	public void display() {
		if (frame == null) {
			createAndShowGUI();
		} else {
			frame.setVisible(true);
		}

		widget.requestFocus();
	}

	public void changeTitle() {
		if (frame != null) {
			String sessionChannel = widget.getSessionChannel() == null
					|| CONFIG_SERVER_SESSION_CHANNEL.equals(widget.getSessionChannel())
							? PersistenceControllerFactory.getPersistenceController().getHostInfoCollections()
									.getConfigServer()
							: widget.getSessionChannel();
			frame.setTitle(sessionChannel);
		}
	}

	public void close() {
		frame.dispose();
	}
}
