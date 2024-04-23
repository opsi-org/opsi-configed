/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JScrollBar;

import org.java_websocket.handshake.ServerHandshake;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalPanel;
import com.jediterm.terminal.ui.settings.SettingsProvider;

import de.uib.messagebus.Messagebus;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.ThreadLocker;
import de.uib.utils.logging.Logging;

public class TerminalWidget extends JediTermWidget implements MessagebusListener {
	public static final int DEFAULT_TERMINAL_COLUMNS = 80;
	public static final int DEFAULT_TERMINAL_ROWS = 24;
	private static final int DEFAULT_TIME_TO_BLOCK_IN_MS = 5000;
	private static final String CONFIG_SERVER_SESSION_CHANNEL = "service:config:terminal";

	private JScrollBar scrollBar;

	private ThreadLocker locker;

	private Messagebus messagebus;
	private String terminalChannel;
	private String terminalId;
	private String sessionChannel;

	private SettingsProvider settingsProvider;
	private TerminalFrame terminalFrame;

	private WebSocketInputStream webSocketInputStream;

	private boolean ignoreKeyEvent;
	private boolean disableUserInput;
	private boolean isViewRestricted;

	public TerminalWidget(TerminalFrame terminal, SettingsProvider settingsProvider) {
		this(terminal, DEFAULT_TERMINAL_COLUMNS, DEFAULT_TERMINAL_ROWS, settingsProvider);
	}

	public TerminalWidget(TerminalFrame terminal, int columns, int lines, SettingsProvider settingsProvider) {
		super(columns, lines, settingsProvider);
		this.settingsProvider = settingsProvider;
		this.terminalFrame = terminal;
		this.locker = new ThreadLocker();
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public Messagebus getMessagebus() {
		return messagebus;
	}

	public String getTerminalChannel() {
		return terminalChannel;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public String getSessionChannel() {
		return sessionChannel;
	}

	public String getTitle() {
		return sessionChannel == null || TerminalWidget.CONFIG_SERVER_SESSION_CHANNEL.equals(sessionChannel)
				? PersistenceControllerFactory.getPersistenceController().getHostInfoCollections().getConfigServer()
				: sessionChannel;
	}

	public int getColumnCount() {
		return getTerminalPanel().getTerminalSizeFromComponent().getColumns();
	}

	public int getRowCount() {
		return getTerminalPanel().getTerminalSizeFromComponent().getRows();
	}

	public boolean ignoreKeyEvent() {
		return ignoreKeyEvent;
	}

	public void setIgnoreKeyEvent(boolean ignoreKeyEvent) {
		this.ignoreKeyEvent = ignoreKeyEvent;
	}

	public boolean disableUserInput() {
		return disableUserInput;
	}

	public void setDisableUserInput(boolean disableUserInput) {
		this.disableUserInput = disableUserInput;
	}

	public boolean isViewRestricted() {
		return isViewRestricted;
	}

	public void setViewRestricted(boolean isViewRestricted) {
		this.isViewRestricted = isViewRestricted;
	}

	public void init() {
		if (settingsProvider == null) {
			settingsProvider = new TerminalSettingsProvider();
		}

		setDropTarget(new FileUpload(terminalFrame, this));

		scrollBar = new JScrollBar();
		getTerminalPanel().init(scrollBar);

		getTerminalPanel().addCustomKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_PLUS && e.isControlDown()) {
					increaseFontSize();
					ignoreKeyEvent = true;
				} else if (e.getKeyCode() == KeyEvent.VK_MINUS && e.isControlDown()) {
					decreaseFontSize();
					ignoreKeyEvent = true;
				} else {
					ignoreKeyEvent = false;
				}
			}
		});
	}

	private void setFontSize(int fontSize) {
		if (fontSize <= TerminalSettingsProvider.FONT_SIZE_MIN_LIMIT
				|| fontSize >= TerminalSettingsProvider.FONT_SIZE_MAX_LIMIT) {
			return;
		}

		((TerminalSettingsProvider) settingsProvider).setTerminalFontSize(fontSize);
		resizeTerminal();
	}

	public void increaseFontSize() {
		setFontSize((int) (settingsProvider.getTerminalFontSize() * 1.1));
	}

	public void decreaseFontSize() {
		setFontSize((int) (((double) settingsProvider.getTerminalFontSize() + 1) / 1.1));
	}

	private void resizeTerminal() {
		if (wasTerminalScreenCleared()) {
			resetCursorAfterClearingTerminalScreen();
		}
		TermSize size = getTerminalPanel().getTerminalSizeFromComponent();
		getTtyConnector().resize(size);
		getTerminal().resize(size, RequestOrigin.User);
		getTypeAheadManager().onResize();
		getTerminalPanel().init(scrollBar);
	}

	private boolean wasTerminalScreenCleared() {
		return getTerminal().getCursorY() < 1 || getTerminal().getCursorX() < 1;
	}

	private void resetCursorAfterClearingTerminalScreen() {
		int additionalSpaces = 2;
		getTerminal().cursorPosition(getTerminalTextBuffer().getScreenLines().trim().length() + additionalSpaces,
				getTerminalTextBuffer().getScreenLines().trim().split("\n").length);
	}

	public void openSession(String session) {
		if (!messagebus.getWebSocket().isListenerRegistered(this)) {
			messagebus.getWebSocket().registerListener(this);
		}
		this.sessionChannel = produceSessionChannel(session);
		messagebus.sendTerminalOpenRequest(sessionChannel, DEFAULT_TERMINAL_ROWS, DEFAULT_TERMINAL_COLUMNS);
		locker.lock(DEFAULT_TIME_TO_BLOCK_IN_MS);
		connectWebSocketTty();
	}

	private static String produceSessionChannel(String session) {
		if ("Configserver".equals(session)) {
			return CONFIG_SERVER_SESSION_CHANNEL;
		}
		List<String> depotNames = PersistenceControllerFactory.getPersistenceController().getHostInfoCollections()
				.getDepotNamesList();
		return depotNames.contains(session) ? ("service:depot:" + session + ":terminal") : ("host:" + session);
	}

	private void connectWebSocketTty() {
		TtyConnector connector = new WebSocketTtyConnector(this, new WebSocketOutputStream(messagebus.getWebSocket()),
				webSocketInputStream);
		setTtyConnector(connector);
		start();
	}

	public void connectPipedTty() {
		TtyConnector connector = new PipedTtyConnector(this);
		setTtyConnector(connector);
		start();
	}

	public void write(byte[] message) {
		try {
			if (getTtyConnector() != null) {
				getTtyConnector().write(message);
			}
		} catch (IOException e) {
			Logging.error("failed to write message: ", e);
		}
	}

	@Override
	protected TerminalPanel createTerminalPanel(SettingsProvider settingsProvider, StyleState styleState,
			TerminalTextBuffer terminalTextBuffer) {
		return new TerminalPanel(settingsProvider, terminalTextBuffer, styleState) {
			@Override
			public List<TerminalAction> getActions() {
				List<TerminalAction> actions = super.getActions();
				for (TerminalAction action : actions) {
					if (action.getName().equals(settingsProvider.getPasteActionPresentation().getName())) {
						action.withEnabledSupplier(() -> !isViewRestricted);
					}
				}
				return actions;
			}
		};
	}

	@Override
	public List<TerminalAction> getActions() {
		return List
				.of(new TerminalAction(((TerminalSettingsProvider) settingsProvider).getNewWindowActionPresentation(),
						(KeyEvent input) -> {
							terminalFrame.openNewWindow();
							return true;
						}).withEnabledSupplier(() -> !isViewRestricted).withMnemonicKey(KeyEvent.VK_N),
						new TerminalAction(
								((TerminalSettingsProvider) settingsProvider).getNewSessionActionPresentation(),
								(KeyEvent input) -> {
									terminalFrame.openNewSession();
									return true;
								}).withEnabledSupplier(() -> !isViewRestricted).withMnemonicKey(KeyEvent.VK_T),
						new TerminalAction(
								((TerminalSettingsProvider) settingsProvider).getChangeSessionActionPresentation(),
								(KeyEvent input) -> {
									terminalFrame.displaySessionsDialog();
									return true;
								}).withEnabledSupplier(() -> !isViewRestricted).withMnemonicKey(KeyEvent.VK_S));
	}

	@Override
	public void close() {
		super.close();
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
		if (!isFirstConnection() && !isMessageForThisChannel(message)) {
			return;
		}

		String type = (String) message.get("type");
		if (WebSocketEvent.TERMINAL_OPEN_EVENT.toString().equals(type)) {
			webSocketInputStream = new WebSocketInputStream();
			terminalId = (String) message.get("terminal_id");
			terminalChannel = (String) message.get("back_channel");
			terminalFrame.changeTitle();
			locker.unlock();
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

	public boolean isMessageForThisChannel(Map<String, Object> message) {
		if (terminalChannel == null) {
			return false;
		}

		String currentChannel = terminalChannel;
		if (!terminalChannel.startsWith("session:")) {
			currentChannel = "session:" + terminalId;
		}

		boolean matchesSenderChannel = currentChannel.equals(message.get("channel"));
		boolean matchesReturnChannel = currentChannel.equals(message.get("back_channel"));
		return matchesSenderChannel || matchesReturnChannel;
	}

	private boolean isFirstConnection() {
		return terminalId == null && terminalChannel == null;
	}
}
