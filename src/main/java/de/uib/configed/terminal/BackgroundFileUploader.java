/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.logging.Logging;

public class BackgroundFileUploader extends SwingWorker<Void, Integer> {
	private static final int MAX_CHUNK_SIZE = 1_500_000;
	private static final int MIN_CHUNK_SIZE = 8000;
	private static final int DEFAULT_CHUNK_SIZE = 25000;
	private static final int DEFAULT_BUSY_WAIT_IN_MS = 50;
	private static final int LATENCY_WINDOW_SIZE = 10;

	private FileUploadQueue queue;
	private TerminalFrame terminal;
	private TerminalWidget terminalWidget;
	private String destinationDir;

	private File currentFile;
	private int totalFilesToUpload;
	private int uploadedFiles;

	public BackgroundFileUploader(TerminalFrame terminal, TerminalWidget terminalWidget, FileUploadQueue queue) {
		this(terminal, terminalWidget, queue, null);
	}

	public BackgroundFileUploader(TerminalFrame terminal, TerminalWidget terminalWidget, FileUploadQueue queue,
			String destinationDir) {
		this.terminal = terminal;
		this.terminalWidget = terminalWidget;
		this.queue = queue;
		this.destinationDir = destinationDir;
	}

	@Override
	protected void process(List<Integer> chunkSizes) {
		for (Integer chunkSize : chunkSizes) {
			if (currentFile == null) {
				return;
			}

			try {
				terminal.updateFileUploadProgressBar(chunkSize, (int) Files.size(currentFile.toPath()));
			} catch (IOException e) {
				Logging.warning(this, "unable to retrieve file size: ", e);
			}
		}
	}

	@Override
	protected Void doInBackground() {
		File file = null;

		while ((file = queue.get()) != null) {
			currentFile = file;

			uploadedFiles += 1;
			updateTotalFilesToUpload();

			String fileId = UUID.randomUUID().toString();
			sendFileUploadRequest(file, fileId);

			try (FileInputStream reader = new FileInputStream(file)) {
				uploadFileInChunks(file, reader.getChannel(), fileId);
			} catch (IOException ex) {
				Logging.warning("cannot upload file to server: ", ex);
			}

			queue.remove(file);
		}

		return null;
	}

	private void uploadFileInChunks(File file, FileChannel channel, String fileId) throws IOException {
		int chunk = 0;
		int offset = 0;
		int chunkSize = DEFAULT_CHUNK_SIZE;
		double[] latencyMeasurements = new double[LATENCY_WINDOW_SIZE];
		int currentLatencyIndex = 0;
		int numLatencyMeasurements = 0;

		if (channel.size() < DEFAULT_CHUNK_SIZE) {
			chunkSize = (int) channel.size();
		}

		ByteBuffer buff = ByteBuffer.allocate(chunkSize);

		while (channel.read(buff) > 0) {
			offset += chunkSize;
			chunk += 1;
			boolean last = offset >= Files.size(file.toPath());

			publish(offset);

			buff.flip();

			Map<String, Object> data = new HashMap<>();
			data.put("type", WebSocketEvent.FILE_CHUNK.toString());
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminalWidget.getTerminalChannel());
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("file_id", fileId);
			data.put("number", chunk);
			data.put("data", buff);
			data.put("last", last);

			Logging.debug(this, "uploading file chunk: " + data.toString());

			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			terminalWidget.getMessagebus().sendMessage(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));

			buff.clear();

			long startWaitingTime = System.currentTimeMillis();
			while (!last && terminalWidget.getMessagebus().isBusy()) {
				wait(DEFAULT_BUSY_WAIT_IN_MS);
			}
			double latency = (double) System.currentTimeMillis() - (double) startWaitingTime;
			latencyMeasurements[currentLatencyIndex] = latency;
			numLatencyMeasurements = Math.min(numLatencyMeasurements + 1, LATENCY_WINDOW_SIZE);
			double movingAverageLatency = calculateMovingAverageLatency(numLatencyMeasurements, latencyMeasurements);
			double scalingFactor = calculateScalingFactor(latency, movingAverageLatency);
			chunkSize = modifyChunkSizeBasedOnScalingFactor(chunkSize, scalingFactor);
			buff = ByteBuffer.allocate(chunkSize);
			currentLatencyIndex = (currentLatencyIndex + 1) % LATENCY_WINDOW_SIZE;
		}
	}

	private static double calculateScalingFactor(double latency, double movingAverageLatency) {
		double percentageDifference = Double.compare(movingAverageLatency, 0.0) == 0 ? 0.0
				: (0.1 * (latency / movingAverageLatency));
		return latency < movingAverageLatency ? (1.0 - percentageDifference) : (1.0 + percentageDifference);
	}

	private static double calculateMovingAverageLatency(int numLatencyMeasurements, double[] latencyMeasurements) {
		double sum = 0;
		for (int i = 0; i < numLatencyMeasurements; i++) {
			sum += latencyMeasurements[i];
		}
		return sum / numLatencyMeasurements;
	}

	private static int modifyChunkSizeBasedOnScalingFactor(int chunkSize, double scalingFactor) {
		int newChunkSize = (int) (chunkSize * scalingFactor);
		return Math.min(Math.max(newChunkSize, MIN_CHUNK_SIZE), MAX_CHUNK_SIZE);
	}

	private void sendFileUploadRequest(File file, String fileId) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("type", WebSocketEvent.FILE_UPLOAD_REQUEST.toString());
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminalWidget.getTerminalChannel());
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("file_id", fileId);
			data.put("content_type", "application/octet-stream");
			data.put("name", file.getName());
			if (destinationDir != null || !destinationDir.isEmpty()) {
				data.put("destination_dir", destinationDir);
			}
			data.put("size", Files.size(file.toPath()));
			data.put("terminal_id", terminalWidget.getTerminalId());

			Logging.debug(this, "file upload request: " + data.toString());

			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			terminalWidget.getMessagebus().sendMessage(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
		} catch (JsonProcessingException ex) {
			Logging.warning(this, "error occurred while processing JSON: ", ex);
		} catch (IOException ex) {
			Logging.warning(this, "unable to retrieve file size: ", ex);
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

	@Override
	protected void done() {
		terminal.showFileUploadProgress(false);
		totalFilesToUpload = 0;
		uploadedFiles = 0;
	}

	public int getTotalFilesToUpload() {
		return totalFilesToUpload;
	}

	public void setTotalFilesToUpload(int totalFiles) {
		this.totalFilesToUpload = totalFiles;
	}

	public void updateTotalFilesToUpload() {
		SwingUtilities.invokeLater(() -> terminal.indicateFileUpload(currentFile, uploadedFiles, totalFilesToUpload));
	}
}
