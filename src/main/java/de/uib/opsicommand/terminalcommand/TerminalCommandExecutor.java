/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.terminalcommand;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.java_websocket.handshake.ServerHandshake;

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.TerminalFrame;
import de.uib.messagebus.MessagebusListener;
import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.ThreadLocker;

public class TerminalCommandExecutor implements MessagebusListener {
	private static final Pattern MORE_THAN_ONE_SPACE_PATTERN = Pattern.compile("\\s{2,}",
			Pattern.UNICODE_CHARACTER_CLASS);

	private ConfigedMain configedMain;
	private TerminalFrame terminalFrame;
	private ThreadLocker locker;

	private StringBuilder result;

	private String processId;
	private String commandToExecute;

	private boolean withGUI;

	public TerminalCommandExecutor(ConfigedMain configedMain) {
		this(configedMain, true);
	}

	public TerminalCommandExecutor(ConfigedMain configedMain, boolean withGUI) {
		this.configedMain = configedMain;
		this.terminalFrame = new TerminalFrame(true);
		this.withGUI = withGUI;
		this.locker = new ThreadLocker();
		configedMain.getMessagebus().getWebSocket().registerListener(TerminalCommandExecutor.this);
		result = new StringBuilder();
	}

	public String execute(TerminalCommand command) {
		terminalFrame.setMessagebus(configedMain.getMessagebus());
		if (withGUI) {
			terminalFrame.display();
			terminalFrame.disableUserInputForSelectedWidget();
		}

		new Thread() {
			@Override
			public void run() {
				if (command instanceof TerminalMultiCommand) {
					executeMultiCommand(terminalFrame, (TerminalMultiCommand) command);
				} else {
					commandToExecute = MORE_THAN_ONE_SPACE_PATTERN.matcher(command.getCommand()).replaceAll(" ");
					sendProcessStartRequest();
				}
			}
		}.start();

		return result.toString();
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
				sendProcessStartRequest();
			}
		}
	}

	private void sendProcessStartRequest() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.PROCESS_START_REQUEST.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", "service:config:process");
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("command", commandToExecute.split(" "));
		data.put("shell", true);
		configedMain.getMessagebus().sendMessage(data);
		locker.lock();
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
			processId = (String) message.get("process_id");
			Float processTimeout = ((String) message.get("timeout")) != null
					? Float.parseFloat((String) message.get("timeout"))
					: 0;
			Instant processStartTime = Instant.now();
			if (processTimeout != 0) {
				ProcessStopThread stopThread = new ProcessStopThread(processTimeout, processStartTime);
				stopThread.start();
			}
		}

		if (WebSocketEvent.PROCESS_STOP_EVENT.toString().equals(type)) {
			String stoppedProcessId = (String) message.get("process_id");
			if (stoppedProcessId != null && stoppedProcessId.equals(processId)) {
				locker.unlock();
			}
		}

		if (WebSocketEvent.PROCESS_DATA_READ.toString().equals(type)) {
			String streamToReadFrom = determineStreamFromWhichToRead(message);
			byte[] data = (byte[]) message.get(streamToReadFrom);
			if (withGUI) {
				terminalFrame.writeToWidget(new String(data).replace("\n", "\r\n").getBytes());
			}
			String currentProcessId = (String) message.get("process_id");
			if (currentProcessId.equals(processId) && !"stderr".equals(streamToReadFrom)) {
				result.append(new String(data));
			}
		}
	}

	private static String determineStreamFromWhichToRead(Map<String, Object> message) {
		return message.get("stderr") != null && !(new String((byte[]) message.get("stderr"))).isEmpty() ? "stderr"
				: "stdout";
	}

	@SuppressWarnings({ "java:S2972" })
	private class ProcessStopThread extends Thread {
		private Instant processStartTime;
		private Float processTimeout;

		public ProcessStopThread(Float processTimeout, Instant processStartTime) {
			this.processTimeout = processTimeout;
			this.processStartTime = processStartTime;
		}

		@Override
		public void run() {
			Instant now = Instant.now();
			Duration duration = Duration.between(processStartTime, now);
			while (true) {
				if (duration.getSeconds() >= processTimeout) {
					Map<String, Object> data = new HashMap<>();
					data.put("type", WebSocketEvent.PROCESS_STOP_REQUEST.toString());
					data.put("id", UUID.randomUUID().toString());
					data.put("process_id", processId);
					data.put("sender", "@");
					data.put("channel", "service:config:process");
					data.put("created", System.currentTimeMillis());
					data.put("expires", System.currentTimeMillis() + 10000);
					configedMain.getMessagebus().sendMessage(data);
					break;
				}
			}
		}
	}
}
