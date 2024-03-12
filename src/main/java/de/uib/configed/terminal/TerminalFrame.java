/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSelectionList;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.messages.Messages;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public final class TerminalFrame implements MessagebusListener {
	private JFrame frame;
	private JProgressBar fileUploadProgressBar;
	private JLabel uploadedFilesLabel;
	private JLabel fileNameLabel;
	private JPanel southPanel;

	private Messagebus messagebus;

	private TerminalTabbedPane tabbedPane;

	private boolean restrictView;

	public TerminalFrame() {
		this(false);
	}

	public TerminalFrame(boolean restrictView) {
		this.restrictView = restrictView;
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
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
				tabbedPane.removeAllTerminalTabs();
				close();
			}

			@Override
			public void windowActivated(WindowEvent e) {
				setSelectedTheme(TerminalSettingsProvider.getTerminalTheme());
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
		jMenuItemNewWindow.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemNewWindow.addActionListener((ActionEvent e) -> openNewWindow());

		JMenuItem jMenuItemNewSession = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewSession"));
		jMenuItemNewSession.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemNewSession.addActionListener((ActionEvent e) -> openNewSession());

		JMenuItem jMenuItemChangeSession = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.changeSession"));
		jMenuItemChangeSession.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemChangeSession.addActionListener((ActionEvent e) -> displaySessionsDialog());

		JMenu jMenuTheme = new JMenu(Configed.getResourceValue("theme"));
		ButtonGroup groupThemes = new ButtonGroup();

		for (final String theme : Messages.getAvailableThemes()) {
			JMenuItem themeItem = new JRadioButtonMenuItem(Messages.getThemeTranslation(theme));
			themeItem.setSelected(TerminalSettingsProvider.getTerminalTheme().equals(theme));
			jMenuTheme.add(themeItem);
			groupThemes.add(themeItem);
			themeItem.addActionListener((ActionEvent e) -> setSelectedTheme(theme));
		}

		JMenu menuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));
		menuFile.setEnabled(!restrictView);
		menuFile.add(jMenuItemNewWindow);
		menuFile.add(jMenuItemNewSession);
		menuFile.add(jMenuItemChangeSession);
		menuFile.add(jMenuTheme);
		return menuFile;
	}

	public void openNewWindow() {
		if (restrictView) {
			return;
		}

		TerminalFrame terminalFrame = new TerminalFrame();
		terminalFrame.setMessagebus(messagebus);
		terminalFrame.display();
	}

	public void openNewSession() {
		if (restrictView) {
			return;
		}

		tabbedPane.addTerminalTab();
		displaySessionsDialog();
	}

	public void displaySessionsDialog() {
		if (restrictView) {
			return;
		}

		FSelectionList sessionsDialog = new FSelectionList(frame, Configed.getResourceValue("Terminal.session.title"),
				true, new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") },
				500, 300, "sessionlist");
		List<String> clientsConnectedByMessagebus = new ArrayList<>(PersistenceControllerFactory
				.getPersistenceController().getHostDataService().getMessagebusConnectedClients());
		clientsConnectedByMessagebus.add("Configserver");
		Collections.sort(clientsConnectedByMessagebus);
		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.setVisible(true);

		if (sessionsDialog.getResult() == 2) {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				tabbedPane.getSelectedTerminalWidget().close();
				tabbedPane.resetTerminalWidgetOnSelectedTab();
				tabbedPane.openSessionOnSelectedTab(sessionsDialog.getSelectedValue());
			}
		}
	}

	private JMenu createViewMenu() {
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				widget.increaseFontSize();
			}
		});

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> {
			TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
			if (widget != null) {
				widget.decreaseFontSize();
			}
		});

		JMenu jMenuView = new JMenu(Configed.getResourceValue("LogFrame.jMenuView"));
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);
		return jMenuView;
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		tabbedPane = new TerminalTabbedPane(this);
		tabbedPane.setMessagebus(messagebus);
		tabbedPane.init();
		tabbedPane.addTerminalTab();
		tabbedPane.openSessionOnSelectedTab("Configserver");
		tabbedPane.getSelectedTerminalWidget().requestFocus();

		northLayout
				.setVerticalGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addComponent(tabbedPane, 0, 0, Short.MAX_VALUE)));

		return northPanel;
	}

	private void setSelectedTheme(String selectedTheme) {
		TerminalSettingsProvider.setTerminalTheme(selectedTheme);
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget != null) {
			widget.changeTheme();
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

	public void execute(String command) {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		widget.getTerminalPanel().getTerminalOutputStream().sendString(command + "\r", true);
	}

	public void disableUserInputForSelectedWidget() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		widget.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				widget.setDisableUserInput(true);
				event.consume();
			}

			@Override
			public void keyReleased(KeyEvent event) {
				widget.setDisableUserInput(false);
				event.consume();
			}
		});
		widget.setViewRestricted(true);
	}

	public void uploadFile(File file) {
		FileUploadQueue queue = new FileUploadQueue();
		queue.add(file);
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		BackgroundFileUploader fileUploader = new BackgroundFileUploader(this, widget, queue);
		fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + 1);
		fileUploader.execute();
	}

	public void display() {
		if (frame == null) {
			createAndShowGUI();
		} else {
			frame.setVisible(true);
		}

		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
	}

	public void changeTitle() {
		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (frame != null && widget != null) {
			frame.setTitle(widget.getTitle());
		}
	}

	public void close() {
		frame.dispose();
		messagebus.getWebSocket().unregisterListener(this);
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		close();
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		if (tabbedPane.getTabCount() == 0) {
			close();
		}

		TerminalWidget widget = tabbedPane.getSelectedTerminalWidget();
		if (widget == null || !widget.isMessageForThisChannel(message)) {
			return;
		}

		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type) && tabbedPane.getTabCount() == 1) {
			close();
		}
	}
}
