/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.command;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.terminal.WebDAVBackgroundFileUploader;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.ThreadLocker;
import de.uib.utilities.logging.Logging;

public class CommandExecutor implements MessagebusListener {
	private static final Pattern MORE_THAN_ONE_SPACE_PATTERN = Pattern.compile("\\s{2,}",
			Pattern.UNICODE_CHARACTER_CLASS);

	private ConfigedMain configedMain;
	private TerminalFrame terminalFrame;
	private ThreadLocker locker;

	private SingleCommand commandToExecute;

	private boolean withGUI;
	private CommandProcess commandProcess;

	private int commandNumber;

	public CommandExecutor(ConfigedMain configedMain) {
		this(configedMain, true);
	}

	public CommandExecutor(ConfigedMain configedMain, boolean withGUI) {
		this.configedMain = configedMain;
		this.terminalFrame = new TerminalFrame(true);
		this.withGUI = withGUI;
		this.locker = new ThreadLocker();
		configedMain.getMessagebus().getWebSocket().registerListener(CommandExecutor.this);
	}

	public JFrame getDialog() {
		return terminalFrame.getFrame();
	}

	public String executeSingleCommand(SingleCommand command) {
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		if (withGUI) {
			terminalFrame.display();
			terminalFrame.disableUserInputForSelectedWidget();
		}

		startBackgroundThread(() -> execute(command));

		return commandProcess != null ? commandProcess.getResult() : "";
	}

	public void executeMultiCommand(MultiCommand multiCommand) {
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		if (withGUI) {
			terminalFrame.display();
			terminalFrame.disableUserInputForSelectedWidget();
		}

		startBackgroundThread(() -> {
			List<SingleCommand> commands = multiCommand.getCommands();
			for (SingleCommand command : commands) {
				execute(command);
			}
		});
	}

	private void execute(SingleCommand command) {
		if (command instanceof SingleCommandFileUpload) {
			SingleCommandFileUpload fileUploadCommand = (SingleCommandFileUpload) command;
			WebDAVBackgroundFileUploader fileUploader = new WebDAVBackgroundFileUploader(terminalFrame,
					new File(fileUploadCommand.getFullSourcePath()), fileUploadCommand.getTargetPath(), withGUI,
					() -> locker.unlock());
			terminalFrame.uploadFile(fileUploader);
			locker.lock();
		} else {
			CommandParameterMethods parameterMethods = CommandFactory.getInstance(configedMain)
					.getParameterMethodHandler();
			startCommandProcess(parameterMethods.parseParameter(command, this));
		}
	}

	private void startCommandProcess(SingleCommand command) {
		commandToExecute = command;
		String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN.matcher(command.getCommand()).replaceAll(" ");
		commandProcess = new CommandProcess(configedMain, locker, commandRepresentation);
		commandProcess.sendProcessStartRequest();
	}

	private void startBackgroundThread(Runnable runnable) {
		Thread backgroundThread = new Thread(runnable);
		backgroundThread.start();

		if (!withGUI) {
			try {
				backgroundThread.join();
			} catch (InterruptedException e) {
				Logging.warning(this, "Thread was interrupted");
				Thread.currentThread().interrupt();
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
		if (commandProcess == null || commandToExecute == null) {
			return;
		}

		String type = (String) message.get("type");

		if (WebSocketEvent.PROCESS_START_EVENT.toString().equals(type)) {
			commandNumber++;
			if (withGUI) {
				String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN.matcher(commandToExecute.getSecuredCommand())
						.replaceAll(" ");
				terminalFrame.writeToWidget(("(" + commandNumber + ") " + commandRepresentation + "\r\n").getBytes());
			}
			commandProcess.onStart(message);
		}

		if (WebSocketEvent.PROCESS_STOP_EVENT.toString().equals(type)) {
			if (withGUI) {
				terminalFrame.writeToWidget("\r\n".getBytes());
			}
			commandProcess.onStop(message);
		}

		if (WebSocketEvent.PROCESS_DATA_READ.toString().equals(type)) {
			String data = commandProcess.onDataRead(message);
			if (withGUI) {
				terminalFrame.writeToWidget(data.replace("\n", "\r\n").getBytes());
			}
		}

		if (WebSocketEvent.PROCESS_ERROR_EVENT.toString().equals(type)) {
			String error = commandProcess.onError(message);
			if (withGUI) {
				commandNumber++;
				String red = "\u001B[31m";
				String reset = "\u001B[0m";
				String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN.matcher(commandToExecute.getSecuredCommand())
						.replaceAll(" ");
				String errorMessage = String.format(
						Configed.getResourceValue("TerminalCommandExecutor.processErrorMessage"),
						commandRepresentation);
				terminalFrame.writeToWidget(
						(red + "(" + commandNumber + ") " + errorMessage + " " + error + reset).getBytes());
				terminalFrame.writeToWidget("\r\n".getBytes());
			}
		}
	}
}
