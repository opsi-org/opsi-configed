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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.java_websocket.handshake.ServerHandshake;

import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.ui.JediTermWidget;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FSelectionList;
import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public final class Terminal implements MessagebusListener {
	private static final int DEFAULT_TERMINAL_COLUMNS = 80;
	private static final int DEFAULT_TERMINAL_ROWS = 24;
	private static final int DEFAULT_TIME_TO_BLOCK_IN_MS = 5000;
	private static final int FONT_SIZE_MIN_LIMIT = 8;
	private static final int FONT_SIZE_MAX_LIMIT = 62;
	private static final int FONT_SIZE_SCALING_FACTOR = 2;

	private JediTermWidget widget;
	private JFrame frame;
	private JProgressBar fileUploadProgressBar;
	private JLabel uploadedFilesLabel;
	private JLabel fileNameLabel;
	private JPanel southPanel;
	private JScrollBar scrollBar;

	private Messagebus messagebus;
	private String terminalChannel;
	private String terminalId;

	private CountDownLatch locker;

	private TerminalSettingsProvider settingsProvider;

	private WebSocketInputStream webSocketInputStream;

	private boolean ignoreKeyEvent;

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public Messagebus getMessagebus() {
		return messagebus;
	}

	public void setTerminalChannel(String value) {
		this.terminalChannel = value;
	}

	public String getTerminalChannel() {
		return this.terminalChannel;
	}

	public void setTerminalId(String value) {
		this.terminalId = value;
	}

	public String getTerminalId() {
		return this.terminalId;
	}

	public int getColumnCount() {
		return widget.getTerminalDisplay().getColumnCount();
	}

	public int getRowCount() {
		return widget.getTerminalDisplay().getRowCount();
	}

	public boolean ignoreKeyEvent() {
		return ignoreKeyEvent;
	}

	public void lock() {
		try {
			locker = new CountDownLatch(1);
			if (locker.await(DEFAULT_TIME_TO_BLOCK_IN_MS, TimeUnit.MILLISECONDS)) {
				Logging.info(this, "thread was unblocked");
			} else {
				Logging.info(this, "time ellapsed");
			}
		} catch (InterruptedException ie) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	public void unlock() {
		locker.countDown();
	}

	private JediTermWidget createTerminalWidget() {
		if (settingsProvider == null) {
			settingsProvider = new TerminalSettingsProvider();
		}

		widget = new JediTermWidget(DEFAULT_TERMINAL_COLUMNS, DEFAULT_TERMINAL_ROWS, settingsProvider);
		widget.setDropTarget(new FileUpload(this));

		scrollBar = new JScrollBar();
		widget.getTerminalPanel().init(scrollBar);

		widget.getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_PLUS && (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
					setFontSize((int) settingsProvider.getTerminalFontSize() + FONT_SIZE_SCALING_FACTOR);
					ignoreKeyEvent = true;
				} else if (e.getKeyCode() == KeyEvent.VK_MINUS
						&& (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
					setFontSize((int) settingsProvider.getTerminalFontSize() - FONT_SIZE_SCALING_FACTOR);
					ignoreKeyEvent = true;
				} else {
					ignoreKeyEvent = false;
				}
			}
		});

		return widget;
	}

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setIconImage(Utils.getMainIcon());
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
				widget.stop();
				frame.dispose();
				frame = null;
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
			Terminal terminal = new Terminal();
			terminal.display();
			messagebus.connectTerminal(terminal);
		});

		JMenuItem jMenuItemSession = new JMenuItem(Configed.getResourceValue("Terminal.menuBar.fileMenu.session"));
		jMenuItemSession.addActionListener((ActionEvent e) -> displaySessionsDialog());

		JMenuItem jMenuItemDarkTheme = new JRadioButtonMenuItem(
				Configed.getResourceValue("Terminal.settings.theme.dark"));
		JMenuItem jMenuItemLightTheme = new JRadioButtonMenuItem(
				Configed.getResourceValue("Terminal.settings.theme.light"));
		jMenuItemDarkTheme.addActionListener((ActionEvent e) -> {
			jMenuItemDarkTheme.setSelected(true);
			jMenuItemLightTheme.setSelected(false);
			setSelectedTheme(Configed.getResourceValue("Terminal.settings.theme.dark"));
		});
		jMenuItemLightTheme.addActionListener((ActionEvent e) -> {
			jMenuItemDarkTheme.setSelected(false);
			jMenuItemLightTheme.setSelected(true);
			setSelectedTheme(Configed.getResourceValue("Terminal.settings.theme.light"));
		});
		jMenuItemDarkTheme.setSelected(true);
		jMenuItemLightTheme.setSelected(false);

		JMenu jMenuTheme = new JMenu("Theme");
		jMenuTheme.add(jMenuItemDarkTheme);
		jMenuTheme.add(jMenuItemLightTheme);

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
		Collections.sort(clientsConnectedByMessagebus);
		sessionsDialog.setListData(clientsConnectedByMessagebus);
		sessionsDialog.setVisible(true);

		if (sessionsDialog.getResult() == 2) {
			changeSession(sessionsDialog.getSelectedValue());
		}
	}

	private JMenu createViewMenu() {
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> setFontSize(
				(int) settingsProvider.getTerminalFontSize() + FONT_SIZE_SCALING_FACTOR));

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> setFontSize(
				(int) settingsProvider.getTerminalFontSize() + FONT_SIZE_SCALING_FACTOR));

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
		if (selectedTheme.equals(Configed.getResourceValue("Terminal.settings.theme.light"))) {
			TerminalSettingsProvider.setTerminalLightTheme();
		} else {
			TerminalSettingsProvider.setTerminalDarkTheme();
		}
		if (widget != null) {
			widget.repaint();
		}
	}

	private void setFontSize(int fontSize) {
		if (((int) settingsProvider.getTerminalFontSize() == FONT_SIZE_MIN_LIMIT && fontSize <= FONT_SIZE_MIN_LIMIT)
				|| ((int) settingsProvider.getTerminalFontSize() == FONT_SIZE_MAX_LIMIT
						&& fontSize >= FONT_SIZE_MAX_LIMIT)) {
			return;
		}

		TerminalSettingsProvider.setTerminalFontSize(fontSize);
		resizeTerminal();
	}

	private void resizeTerminal() {
		JediTerminal.ensureTermMinimumSize(widget.getTerminalPanel().getTerminalSizeFromComponent());
		widget.getTypeAheadManager().onResize();
		widget.getTerminalStarter().postResize(widget.getTerminalPanel().getTerminalSizeFromComponent(),
				RequestOrigin.User);
		widget.getTerminal().reset();
		widget.getTerminalPanel().init(scrollBar);
		widget.repaint();
	}

	private void changeSession(String session) {
		terminalId = null;
		terminalChannel = null;
		widget.stop();
		widget.getTerminal().reset();
		messagebus.connectTerminal(this, produceSessionChannel(session));
	}

	private static String produceSessionChannel(String session) {
		return PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getDepotNamesList()
				.contains(session) ? ("service:depot:" + session + ":terminal") : ("host:" + session);
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
				.addGap(0, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
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

	public void close() {
		messagebus.getWebSocket().unregisterListener(this);
		SwingUtilities.invokeLater(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
	}

	public void connectWebSocketTty() {
		TtyConnector connector = new WebSocketTtyConnector(this, new WebSocketOutputStream(messagebus.getWebSocket()),
				webSocketInputStream);
		widget.setTtyConnector(connector);
		widget.start();
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
		if (terminalId != null && !String.format("session:%s", terminalId).equals(message.get("channel"))) {
			return;
		}

		Logging.trace(this, "Messagebus message received: " + message.toString());
		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_OPEN_EVENT.toString().equals(type)) {
			webSocketInputStream = new WebSocketInputStream();
			webSocketInputStream.init();
			setTerminalId((String) message.get("terminal_id"));
			setTerminalChannel((String) message.get("back_channel"));
			if (frame != null) {
				frame.setTitle(getTerminalChannel());
			}
			unlock();
		} else if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type)) {
			close();
		} else if (WebSocketEvent.TERMINAL_DATA_READ.toString().equals(type)) {
			try {
				if (webSocketInputStream != null) {
					webSocketInputStream.write((byte[]) message.get("data"));
				}
			} catch (IOException e) {
				Logging.error("failed to write message: ", e);
			}
		} else if (WebSocketEvent.FILE_UPLOAD_RESULT.toString().equals(type)) {
			Map<String, Object> data = new HashMap<>();
			data.put("type", WebSocketEvent.TERMINAL_DATA_WRITE.toString());
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminalChannel);
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("terminal_id", terminalId);
			data.put("data", ((String) message.get("path")).getBytes(StandardCharsets.UTF_8));
			messagebus.sendMessage(data);
		} else {
			// Other events are handled by other listeners.
		}
	}
}
