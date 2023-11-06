/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
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

	private static Terminal instance;
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
	private boolean webSocketTtyConnected;

	private TerminalSettingsProvider settingsProvider;
	private String theme;

	private Terminal() {
	}

	public static Terminal getInstance() {
		if (instance == null) {
			instance = new Terminal();
		}

		return instance;
	}

	public static void destroyInstance() {
		instance = null;
	}

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

	public boolean isWebSocketTtyConnected() {
		return webSocketTtyConnected;
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
		widget.setDropTarget(new FileUpload());

		scrollBar = new JScrollBar();
		widget.getTerminalPanel().init(scrollBar);

		return widget;
	}

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setIconImage(Utils.getMainIcon());

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

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		JPanel settingsPanel = createSettingsPanel();
		JediTermWidget termWidget = createTerminalWidget();

		northLayout.setVerticalGroup(
				northLayout.createSequentialGroup().addComponent(settingsPanel, 70, GroupLayout.PREFERRED_SIZE, 70)
						.addComponent(termWidget, 0, 0, Short.MAX_VALUE));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addComponent(settingsPanel, 0, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE))
				.addGroup(northLayout.createSequentialGroup().addComponent(termWidget, 0, 0, Short.MAX_VALUE)));

		return northPanel;
	}

	private JPanel createSettingsPanel() {
		JPanel settingsPanel = new JPanel();

		GroupLayout settingsLayout = new GroupLayout(settingsPanel);
		settingsPanel.setLayout(settingsLayout);

		JComboBox<String> themeComboBox = new JComboBox<>();
		themeComboBox.addItem(Configed.getResourceValue("Terminal.settings.theme.dark"));
		themeComboBox.addItem(Configed.getResourceValue("Terminal.settings.theme.light"));
		themeComboBox.addActionListener((ActionEvent e) -> setTheme((String) themeComboBox.getSelectedItem()));

		if (theme == null) {
			theme = Configed.getResourceValue("Terminal.settings.theme.dark");
		}

		themeComboBox.setSelectedItem(theme);

		JButton buttonFontPlus = new JButton(Utils.createImageIcon("images/font-plus.png", ""));
		buttonFontPlus.setToolTipText(Configed.getResourceValue("TextPane.fontPlus"));
		buttonFontPlus.addActionListener((ActionEvent e) -> {
			TerminalSettingsProvider.setTerminalFontSize((int) settingsProvider.getTerminalFontSize() + 1);
			widget.getTerminalPanel().init(scrollBar);
			widget.repaint();
			resizeTerminal();
		});

		JButton buttonFontMinus = new JButton(Utils.createImageIcon("images/font-minus.png", ""));
		buttonFontMinus.setToolTipText(Configed.getResourceValue("TextPane.fontMinus"));
		buttonFontMinus.addActionListener((ActionEvent e) -> {
			if ((int) settingsProvider.getTerminalFontSize() == 1) {
				return;
			}

			TerminalSettingsProvider.setTerminalFontSize((int) settingsProvider.getTerminalFontSize() - 1);
			widget.getTerminalPanel().init(scrollBar);
			widget.repaint();
			resizeTerminal();
		});

		JLabel themeLabel = new JLabel(Configed.getResourceValue("Terminal.settings.theme"));

		JComboBox<String> hostComboBox = new JComboBox<>();
		Set<String> clientsConnectedByMessagebus = new TreeSet<>(PersistenceControllerFactory.getPersistenceController()
				.getHostDataService().getMessagebusConnectedClients());
		for (String clientConnectedByMessagebus : clientsConnectedByMessagebus) {
			hostComboBox.addItem(clientConnectedByMessagebus);
		}
		JLabel hostLabel = new JLabel(Configed.getResourceValue("Terminal.connection.host"));

		settingsLayout.setHorizontalGroup(settingsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(Globals.GAP_SIZE)
				.addGroup(settingsLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(themeLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(themeComboBox, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(buttonFontPlus, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(buttonFontMinus, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE))
				.addGroup(settingsLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(hostLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(hostComboBox, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)));

		settingsLayout.setVerticalGroup(settingsLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(settingsLayout.createParallelGroup()
						.addComponent(themeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(themeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonFontPlus, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonFontMinus, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(settingsLayout.createParallelGroup()
						.addComponent(hostLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(hostComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE));

		return settingsPanel;
	}

	private void setTheme(String selectedTheme) {
		if (selectedTheme.equals(Configed.getResourceValue("Terminal.settings.theme.light"))) {
			TerminalSettingsProvider.setTerminalLightTheme();
		} else {
			TerminalSettingsProvider.setTerminalDarkTheme();
		}
		theme = selectedTheme;
		if (widget != null) {
			widget.repaint();
		}
	}

	private void resizeTerminal() {
		JediTerminal.ensureTermMinimumSize(widget.getTerminalPanel().getTerminalSizeFromComponent());
		widget.getTypeAheadManager().onResize();
		widget.getTerminalStarter().postResize(widget.getTerminalPanel().getTerminalSizeFromComponent(),
				RequestOrigin.User);
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
		SwingUtilities.invokeLater(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
	}

	public void connectWebSocketTty() {
		WebSocketInputStream.init();
		TtyConnector connector = new WebSocketTtyConnector(new WebSocketOutputStream(messagebus.getWebSocket()),
				WebSocketInputStream.getReader());
		widget.setTtyConnector(connector);
		widget.start();
		webSocketTtyConnected = true;
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
		Logging.trace(this, "Messagebus message received: " + message.toString());
		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_OPEN_EVENT.toString().equals(type)) {
			WebSocketInputStream.init();
			setTerminalId((String) message.get("terminal_id"));
			setTerminalChannel((String) message.get("back_channel"));
			unlock();
		} else if (WebSocketEvent.TERMINAL_CLOSE_EVENT.toString().equals(type)) {
			close();
		} else if (WebSocketEvent.TERMINAL_DATA_READ.toString().equals(type)) {
			try {
				WebSocketInputStream.write((byte[]) message.get("data"));
			} catch (IOException e) {
				Logging.error("failed to write message: ", e);
			}
		} else if (WebSocketEvent.TERMINAL_RESIZE_EVENT.toString().equals(type)) {
			// Resizing is handled by the user, we only notify server by
			// sending terminal_resize_request event. On the client side, there is
			// no need to handle terminal_resize_event.
		} else {
			// Other events are handled by other listeners.
		}
	}
}
