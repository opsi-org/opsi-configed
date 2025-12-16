/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.terminal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.core.infrastructure.messagebus.WebSocketEvent;
import de.uib.configed.share.logging.Logging;

public class MessagebusBackgroundFileUploader extends AbstractBackgroundFileUploader {
	private static final int MAX_CHUNK_SIZE = 1_500_000;
	private static final int MIN_CHUNK_SIZE = 8000;
	private static final int DEFAULT_CHUNK_SIZE = 25000;
	private static final int DEFAULT_BUSY_WAIT_IN_MS = 50;
	private static final int LATENCY_WINDOW_SIZE = 10;
	private static final double LOW_LATENCY_THRESHOLD = 50.0;
	private static final double HIGH_LATENCY_THRESHOLD = 200.0;

	private FileUploadQueue queue;
	private TerminalWidget terminalWidget;
	private String destinationDir;

	private boolean visualizeProgress;

	public MessagebusBackgroundFileUploader(TerminalFrame terminal, TerminalWidget terminalWidget,
			FileUploadQueue queue) {
		this(terminal, terminalWidget, queue, null);
	}

	public MessagebusBackgroundFileUploader(TerminalFrame terminal, TerminalWidget terminalWidget,
			FileUploadQueue queue, String destinationDir) {
		super(terminal, true);
		this.terminalWidget = terminalWidget;
		this.queue = queue;
		this.destinationDir = destinationDir;
	}

	@Override
	protected void upload() {
		File file = null;

		while ((file = queue.get()) != null) {
			currentFile = file;

			uploadedFiles += 1;
			if (visualizeProgress) {
				updateTotalFilesToUpload();
			}

			String fileId = UUID.randomUUID().toString();
			sendFileUploadRequest(file, fileId);

			try (FileInputStream reader = new FileInputStream(file)) {
				uploadFileInChunks(file, reader.getChannel(), fileId);
			} catch (IOException ex) {
				Logging.warning(ex, "cannot upload file to server: ");
			}

			queue.remove(file);
		}
	}

	private void uploadFileInChunks(File file, FileChannel channel, String fileId) throws IOException {
		int chunk = 0;
		int offset = 0;
		int chunkSize = (int) Math.min(channel.size(), DEFAULT_CHUNK_SIZE);
		double[] latencyMeasurements = new double[LATENCY_WINDOW_SIZE];
		int currentLatencyIndex = 0;
		int numLatencyMeasurements = 0;

		while (true) {
			ByteBuffer buff = ByteBuffer.allocate(chunkSize);
			int bytesRead = channel.read(buff);
			if (bytesRead <= 0) {
				break;
			}

			offset += bytesRead;
			chunk++;
			boolean last = offset >= Files.size(file.toPath());

			publish(offset);

			buff.flip();

			Map<String, Object> data = prepareChunkData(fileId, chunk, buff, last);
			Logging.debug(this, "uploading file chunk: ", data);

			sendChunk(data);

			double latency = measureLatency(last);
			latencyMeasurements[currentLatencyIndex] = latency;
			numLatencyMeasurements = Math.min(numLatencyMeasurements + 1, LATENCY_WINDOW_SIZE);

			double movingAverageLatency = calculateMovingAverageLatency(numLatencyMeasurements, latencyMeasurements);
			chunkSize = adjustChunkSize(chunkSize, movingAverageLatency);

			currentLatencyIndex = (currentLatencyIndex + 1) % LATENCY_WINDOW_SIZE;
		}
	}

	private Map<String, Object> prepareChunkData(String fileId, int chunk, ByteBuffer buff, boolean last) {
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.FILE_CHUNK.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", Messagebus.CONNECTION_USER_CHANNEL);
		data.put("channel", terminalWidget.getTerminalChannel());
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("file_id", fileId);
		data.put("number", chunk);
		data.put("data", buff);
		data.put("last", last);
		return data;
	}

	private void sendChunk(Map<String, Object> data) throws IOException {
		ObjectMapper mapper = new MessagePackMapper();
		byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
		terminalWidget.getMessagebus().sendMessage(ByteBuffer.wrap(dataJsonBytes));
	}

	private double measureLatency(boolean last) {
		long startWaitingTime = System.currentTimeMillis();
		while (!last && terminalWidget.getMessagebus().isBusy()) {
			wait(DEFAULT_BUSY_WAIT_IN_MS);
		}
		return (System.currentTimeMillis() - startWaitingTime);
	}

	private static int adjustChunkSize(int currentChunkSize, double movingAverageLatency) {
		if (movingAverageLatency < LOW_LATENCY_THRESHOLD && currentChunkSize < MAX_CHUNK_SIZE) {
			return Math.min(currentChunkSize * 2, MAX_CHUNK_SIZE);
		} else if (movingAverageLatency > HIGH_LATENCY_THRESHOLD && currentChunkSize > MIN_CHUNK_SIZE) {
			return Math.max(currentChunkSize / 2, MIN_CHUNK_SIZE);
		} else {
			return currentChunkSize;
		}
	}

	private static double calculateMovingAverageLatency(int numLatencyMeasurements, double[] latencyMeasurements) {
		double sum = 0;
		for (int i = 0; i < numLatencyMeasurements; i++) {
			sum += latencyMeasurements[i];
		}
		return sum / numLatencyMeasurements;
	}

	private void sendFileUploadRequest(File file, String fileId) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("type", WebSocketEvent.FILE_UPLOAD_REQUEST.toString());
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", Messagebus.CONNECTION_USER_CHANNEL);
			data.put("channel", terminalWidget.getTerminalChannel());
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("file_id", fileId);
			data.put("content_type", "application/octet-stream");
			data.put("name", file.getName());
			if (destinationDir != null && !destinationDir.isEmpty()) {
				data.put("destination_dir", destinationDir);
			}
			data.put("size", Files.size(file.toPath()));
			data.put("terminal_id", terminalWidget.getTerminalId());

			Logging.debug(this, "file upload request: ", data);

			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			terminalWidget.getMessagebus().sendMessage(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
			isFileUploadSuccessfull = true;
		} catch (JsonProcessingException ex) {
			isFileUploadSuccessfull = false;
			Logging.warning(this, ex, "error occurred while processing JSON: ");
		} catch (IOException ex) {
			isFileUploadSuccessfull = false;
			Logging.warning(this, ex, "unable to retrieve file size: ");
		}
	}

	private void wait(int miliseconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(miliseconds);
		} catch (InterruptedException ex) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}
}
