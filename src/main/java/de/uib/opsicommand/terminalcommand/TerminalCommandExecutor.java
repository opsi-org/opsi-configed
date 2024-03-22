/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.ThreadLocker;
import de.uib.utilities.logging.Logging;

public class TerminalCommandExecutor implements MessagebusListener {
	private static final Pattern MORE_THAN_ONE_SPACE_PATTERN = Pattern.compile("\\s{2,}",
			Pattern.UNICODE_CHARACTER_CLASS);

	private ConfigedMain configedMain;
	private TerminalFrame terminalFrame;
	private ThreadLocker locker;

	private String commandToExecute;

	private boolean withGUI;
	private TerminalCommandProcess commandProcess;

	public TerminalCommandExecutor(ConfigedMain configedMain) {
		this(configedMain, true);
	}

	public TerminalCommandExecutor(ConfigedMain configedMain, boolean withGUI) {
		this.configedMain = configedMain;
		this.terminalFrame = new TerminalFrame(true);
		this.withGUI = withGUI;
		this.locker = new ThreadLocker();
		configedMain.getMessagebus().getWebSocket().registerListener(TerminalCommandExecutor.this);
	}

	public String execute(TerminalCommand command) {
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		if (withGUI) {
			terminalFrame.display();
			terminalFrame.disableUserInputForSelectedWidget();
		}

		Thread backgroundThread = new Thread() {
			@Override
			public void run() {
				if (command instanceof TerminalMultiCommand) {
					executeMultiCommand(terminalFrame, (TerminalMultiCommand) command);
				} else {
					commandToExecute = MORE_THAN_ONE_SPACE_PATTERN.matcher(command.getCommand()).replaceAll(" ");
					commandProcess = new TerminalCommandProcess(configedMain, locker, commandToExecute);
					commandProcess.sendProcessStartRequest();
				}
			}
		};
		backgroundThread.start();

		if (!withGUI) {
			try {
				backgroundThread.join();
			} catch (InterruptedException e) {
				Logging.warning(this, "Thread was interrupted");
				Thread.currentThread().interrupt();
			}
		}

		return commandProcess != null ? commandProcess.getResult() : "";
	}

	private void executeMultiCommand(TerminalFrame terminalFrame, TerminalMultiCommand multiCommand) {
		List<TerminalCommand> commands = multiCommand.getCommands();
		for (int i = 0; i < commands.size(); i++) {
			TerminalCommand currentCommand = commands.get(i);
			if (currentCommand instanceof TerminalCommandFileUpload) {
				TerminalCommandFileUpload fileUploadCommand = (TerminalCommandFileUpload) currentCommand;
				terminalFrame.uploadFile(new File(fileUploadCommand.getFullSourcePath()),
						fileUploadCommand.getTargetPath(), withGUI, () -> locker.unlock());
				locker.lock();
			} else {
				commandToExecute = MORE_THAN_ONE_SPACE_PATTERN.matcher(currentCommand.getCommand()).replaceAll(" ");
				commandProcess = new TerminalCommandProcess(configedMain, locker, commandToExecute);
				commandProcess.sendProcessStartRequest();
			}
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not required to implement.
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// Not required to implement.
	}

	@Override
	public void onError(Exception ex) {
		// Not required to implement.
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		String type = (String) message.get("type");

		if (WebSocketEvent.PROCESS_START_EVENT.toString().equals(type)) {
			if (withGUI) {
				terminalFrame.writeToWidget(("Executing command: " + commandToExecute + "\r\n").getBytes());
			}
			commandProcess.onStart(message);
		}

		if (WebSocketEvent.PROCESS_STOP_EVENT.toString().equals(type)) {
			commandProcess.onStop(message);
		}

		if (WebSocketEvent.PROCESS_DATA_READ.toString().equals(type)) {
			String data = commandProcess.onDataRead(message);
			if (withGUI) {
				terminalFrame.writeToWidget(data.replace("\n", "\r\n").getBytes());
			}
		}
	}
}
