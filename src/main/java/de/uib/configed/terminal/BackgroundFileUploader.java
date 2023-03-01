package de.uib.configed.messagebus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.utilities.logging.Logging;

@SuppressWarnings("java:S109")
public class BackgroundFileUploader extends SwingWorker<Void, Integer> {
	private static final int DEFAULT_CHUNK_SIZE = 25000;
	private static final int DEFAULT_BUSY_WAIT_IN_MS = 50;

	private List<File> files;
	private Terminal terminal;

	private File currentFile;
	private File previousFile;
	private int totalFilesToUpload;
	private int currentFileUploading;

	public BackgroundFileUploader(List<File> files) {
		this.terminal = Terminal.getInstance();
		this.files = new ArrayList<>(files);
	}

	@Override
	protected void process(List<Integer> chunkSizes) {
		for (Integer chunkSize : chunkSizes) {
			if (currentFile == null) {
				return;
			}

			if (previousFile == null || !previousFile.equals(currentFile)) {
				previousFile = currentFile;
				currentFileUploading += 1;
				terminal.indicateFileUpload(currentFile, currentFileUploading, totalFilesToUpload);
			}

			try {
				terminal.updateFileUploadProgressBar(chunkSize, (int) Files.size(currentFile.toPath()));
			} catch (IOException e) {
				Logging.warning(this, "unable to retrieve file size: ", e);
			}
		}
	}

	@SuppressWarnings("java:S134")
	@Override
	protected Void doInBackground() {
		totalFilesToUpload = files.size();

		for (File file : files) {
			currentFile = file;

			String fileId = UUID.randomUUID().toString();
			sendFileUploadRequest(file, fileId);

			try (FileInputStream reader = new FileInputStream(file)) {
				FileChannel channel = reader.getChannel();
				int chunk = 0;
				int offset = 0;
				int chunkSize = DEFAULT_CHUNK_SIZE;

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
					data.put("type", "file_chunk");
					data.put("id", UUID.randomUUID().toString());
					data.put("sender", "@");
					data.put("channel", terminal.getTerminalChannel());
					data.put("created", System.currentTimeMillis());
					data.put("expires", System.currentTimeMillis() + 10000);
					data.put("file_id", fileId);
					data.put("number", chunk);
					data.put("data", buff);
					data.put("last", last);

					Logging.debug(this, "uploading file chunk: " + data.toString());

					ObjectMapper mapper = new MessagePackMapper();
					byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
					terminal.getMessagebus().send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));

					buff.clear();

					while (!last && terminal.getMessagebus().isBusy()) {
						wait(DEFAULT_BUSY_WAIT_IN_MS);
					}
				}
			} catch (IOException ex) {
				Logging.warning("cannot upload file to server: ", ex);
			}
		}

		return null;
	}

	private void sendFileUploadRequest(File file, String fileId) {
		try {
			Map<String, Object> data = new HashMap<>();
			data.put("type", "file_upload_request");
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminal.getTerminalChannel());
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("file_id", fileId);
			data.put("content_type", "application/octet-stream");
			data.put("name", file.getName());
			data.put("size", Files.size(file.toPath()));
			data.put("terminal_id", terminal.getTerminalId());

			Logging.debug(this, "file upload request: " + data.toString());

			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			terminal.getMessagebus().send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
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
	}
}
