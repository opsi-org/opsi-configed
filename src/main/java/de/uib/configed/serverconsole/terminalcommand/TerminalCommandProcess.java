/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole.terminalcommand;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;

import de.uib.configed.ConfigedMain;
import de.uib.messagebus.WebSocketEvent;
import de.uib.opsicommand.POJOReMapper;
import de.uib.utilities.ThreadLocker;
import de.uib.utilities.logging.Logging;

public class TerminalCommandProcess {
	private String processId;
	private StringBuilder result;
	private ThreadLocker locker;
	private ConfigedMain configedMain;
	private String command;

	public TerminalCommandProcess(ConfigedMain configedMain, ThreadLocker locker, String command) {
		this.configedMain = configedMain;
		this.locker = locker;
		this.command = command;
		this.result = new StringBuilder();
	}

	public String getResult() {
		return result.toString();
	}

	public void sendProcessStartRequest() {
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.PROCESS_START_REQUEST.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", "service:config:process");
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("command", command.split(" "));
		data.put("shell", true);
		configedMain.getMessagebus().sendMessage(data);
		locker.lock();
	}

	public void onStart(Map<String, Object> message) {
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

	public void onStop(Map<String, Object> message) {
		String stoppedProcessId = (String) message.get("process_id");
		if (stoppedProcessId != null && stoppedProcessId.equals(processId)) {
			locker.unlock();
		}
	}

	public String onDataRead(Map<String, Object> message) {
		String streamToReadFrom = determineStreamFromWhichToRead(message);
		byte[] data = (byte[]) message.get(streamToReadFrom);
		String currentProcessId = (String) message.get("process_id");
		if (currentProcessId.equals(processId) && !"stderr".equals(streamToReadFrom)) {
			result.append(new String(data));
		}
		return new String(data);
	}

	private static String determineStreamFromWhichToRead(Map<String, Object> message) {
		return message.get("stderr") != null && !(new String((byte[]) message.get("stderr"))).isEmpty() ? "stderr"
				: "stdout";
	}

	public String onError(Map<String, Object> message) {
		String stoppedProcessId = (String) message.get("process_id");
		if (stoppedProcessId != null && stoppedProcessId.equals(processId)) {
			locker.unlock();
		}
		Map<String, Object> error = POJOReMapper.remap(message.get("error"), new TypeReference<Map<String, Object>>() {
		});
		Logging.warning(this, "Command execution failed: " + error.get("code") + " - " + error.get("message") + ": "
				+ error.get("details"));
		return (String) error.get("message");
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
