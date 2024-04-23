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
import de.uib.configed.terminal.AbstractBackgroundFileUploader;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.configed.terminal.WebDAVBackgroundFileUploader;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.utils.ThreadLocker;
import de.uib.utils.logging.Logging;

public class CommandExecutor implements MessagebusListener {
	private static final Pattern MORE_THAN_ONE_SPACE_PATTERN = Pattern.compile("\\s{2,}",
			Pattern.UNICODE_CHARACTER_CLASS);

	private ConfigedMain configedMain;
	private TerminalFrame terminalFrame;
	private ThreadLocker locker;

	private SingleCommand commandToExecute;

	private boolean withGUI = true;
	private boolean stopCommandExecution;
	private CommandProcess commandProcess;
	private WebDAVBackgroundFileUploader fileUploader;

	private int commandNumber;

	private SingleCommand singleCommand;
	private MultiCommand multiCommand;

	private int numberOfCommands = 1;
	private int executeNumberOfCommands;
	private int failedNumberOfCommands;
	private int succededNumberOfCommands;

	public CommandExecutor(ConfigedMain configedMain, SingleCommand singleCommand) {
		initValues(configedMain);
		this.singleCommand = singleCommand;
	}

	public CommandExecutor(ConfigedMain configedMain, MultiCommand multiCommand) {
		initValues(configedMain);
		this.multiCommand = multiCommand;
	}

	public void setWithGUI(boolean withGUI) {
		this.withGUI = withGUI;
	}

	private void initValues(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		this.terminalFrame = new TerminalFrame(true);
		terminalFrame.setOnClose(this::onClose);
		this.locker = new ThreadLocker();
	}

	private void onClose() {
		Logging.info(this, "Terminal frame was closed - stopping command execution");
		stopCommandExecution = true;
		if (commandProcess != null && !commandProcess.hasFinished()) {
			Logging.info(this, "Stopping command");
			commandProcess.sendProcessStopRequest();
		}
		if (fileUploader != null) {
			Logging.devel(this, "Stopping file upload");
			fileUploader.cancel(true);
		}
		configedMain.getMessagebus().getWebSocket().unregisterListener(CommandExecutor.this);
	}

	public JFrame getDialog() {
		return terminalFrame.getFrame();
	}

	public String execute() {
		if (singleCommand == null && multiCommand == null) {
			return null;
		}

		terminalFrame.setMessagebus(configedMain.getMessagebus());
		if (withGUI) {
			terminalFrame.display();
			terminalFrame.disableUserInputForSelectedWidget();
		}

		configedMain.getMessagebus().getWebSocket().registerListener(CommandExecutor.this);

		if (singleCommand != null) {
			startBackgroundThread(() -> {
				execute(singleCommand);
				configedMain.getMessagebus().getWebSocket().unregisterListener(CommandExecutor.this);
			});
		} else {
			startBackgroundThread(this::handleMultiCommand);
		}

		return commandProcess != null ? commandProcess.getResult() : "";
	}

	private void handleMultiCommand() {
		List<SingleCommand> commands = multiCommand.getCommands();
		numberOfCommands = commands.size();
		for (SingleCommand command : commands) {
			if (stopCommandExecution) {
				Logging.info(this, "Cancel further commands execution, since terminal frame window was closed");
				break;
			}
			execute(command);
		}
		configedMain.getMessagebus().getWebSocket().unregisterListener(CommandExecutor.this);
	}

	private void execute(SingleCommand command) {
		executeNumberOfCommands++;
		if (command instanceof SingleCommandFileUpload) {
			SingleCommandFileUpload fileUploadCommand = (SingleCommandFileUpload) command;
			fileUploader = new WebDAVBackgroundFileUploader(terminalFrame,
					new File(fileUploadCommand.getFullSourcePath()), fileUploadCommand.getTargetPath(), withGUI);
			fileUploader.setOnDone(() -> {
				indicateFileUploadIsFinished(fileUploader, fileUploadCommand);
				locker.unlock();
			});
			commandNumber++;
			terminalFrame.writeToWidget("(" + commandNumber + ") " + fileUploadCommand.getSecuredCommand() + "\r\n");
			terminalFrame.uploadFile(fileUploader);
			locker.lock();
		} else {
			startCommandProcess(command);
		}
	}

	private void indicateFileUploadIsFinished(AbstractBackgroundFileUploader fileUploader,
			SingleCommandFileUpload fileUploadCommand) {
		String message = fileUploader.isFileUploaded()
				? String.format(Configed.getResourceValue("CommandExecutor.fileUploadSuccessfull"),
						fileUploadCommand.getSourceFileName(), fileUploadCommand.getTargetPath())
				: Configed.getResourceValue("CommandExecutor.fileUploadUnsuccessfull");
		if (fileUploader.isFileUploaded()) {
			succededNumberOfCommands++;
		} else {
			failedNumberOfCommands++;
		}
		terminalFrame.writeToWidget(message + "\r\n");
		terminalFrame.writeToWidget("\r\n");
	}

	private void startCommandProcess(SingleCommand command) {
		commandToExecute = command;
		CommandParameterParser parameterParser = new CommandParameterParser(configedMain);
		String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN
				.matcher(parameterParser.parseParameter(command, this).getCommand()).replaceAll(" ");
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
			onProcessStartEvent(message);
		}

		if (WebSocketEvent.PROCESS_STOP_EVENT.toString().equals(type)) {
			onProcessStopEvent(message);
		}

		if (WebSocketEvent.PROCESS_DATA_READ.toString().equals(type)) {
			onProcessDataRead(message);
		}

		if (WebSocketEvent.PROCESS_ERROR_EVENT.toString().equals(type)) {
			onProcessErrorEvent(message);
		}
	}

	private void onProcessStartEvent(Map<String, Object> message) {
		commandNumber++;
		if (withGUI) {
			String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN.matcher(commandToExecute.getSecuredCommand())
					.replaceAll(" ");
			terminalFrame.writeToWidget("(" + commandNumber + ") " + commandRepresentation + "\r\n");
		}
		commandProcess.onStart(message);
	}

	private void onProcessStopEvent(Map<String, Object> message) {
		if (withGUI) {
			terminalFrame.writeToWidget("\r\n");
		}
		commandProcess.onStop(message);
		if (commandProcess.hasFailed()) {
			failedNumberOfCommands++;
		} else {
			succededNumberOfCommands++;
		}
		if (withGUI) {
			outputEndResult();
		}
	}

	private void onProcessDataRead(Map<String, Object> message) {
		String data = commandProcess.onDataRead(message);
		if (withGUI) {
			terminalFrame.writeToWidget(data.replace("\n", "\r\n"));
		}
	}

	private void onProcessErrorEvent(Map<String, Object> message) {
		failedNumberOfCommands++;
		String error = commandProcess.onError(message);
		if (withGUI) {
			commandNumber++;
			String red = "\u001B[31m";
			String reset = "\u001B[0m";
			String commandRepresentation = MORE_THAN_ONE_SPACE_PATTERN.matcher(commandToExecute.getSecuredCommand())
					.replaceAll(" ");
			String errorMessage = String.format(Configed.getResourceValue("CommandExecutor.processErrorMessage"),
					commandRepresentation);
			terminalFrame.writeToWidget(red + "(" + commandNumber + ") " + errorMessage + " " + error + reset);
			terminalFrame.writeToWidget("\r\n");
			outputEndResult();
		}
	}

	private void outputEndResult() {
		if (numberOfCommands == executeNumberOfCommands) {
			terminalFrame.writeToWidget("----------------------------------------\r\n");
			terminalFrame.writeToWidget(Configed.getResourceValue("CommandExecutor.endResult.failedCommands") + " "
					+ failedNumberOfCommands + "/" + numberOfCommands + "\r\n");
			terminalFrame.writeToWidget(Configed.getResourceValue("CommandExecutor.endResult.succeededCommands") + " "
					+ succededNumberOfCommands + "/" + numberOfCommands + "\r\n");
		}
	}
}
