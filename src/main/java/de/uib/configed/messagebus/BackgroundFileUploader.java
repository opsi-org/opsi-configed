package de.uib.configed.messagebus;

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

import javax.swing.SwingWorker;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.utilities.logging.Logging;

public class BackgroundFileUploader extends SwingWorker<Void, Void> {
	private static final int DEFAULT_CHUNK_SIZE = 1000000;
	private static final int DEFAULT_TIME_TO_WAIT_IN_MS = 5;

	private List<File> files;
	private Terminal terminal;

	public BackgroundFileUploader(List<File> files) {
		this.terminal = Terminal.getInstance();
		this.files = files;
	}

	@Override
	protected Void doInBackground() {
		if (files.isEmpty()) {
			return null;
		}

		for (File file : files) {
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
					System.out.println("sending: " + data.toString());
					terminal.getMessagebus().send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));

					buff.clear();

					if (!last) {
						wait(DEFAULT_TIME_TO_WAIT_IN_MS);
					}
				}
			} catch (IOException ex) {
				Logging.error("cannot upload file to server: " + file.getAbsolutePath());
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
			Logging.warning(this, "error occurred while processing JSON: " + ex);
		} catch (IOException ex) {
			Logging.warning(this, "unable to retrieve file size: " + ex);
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
