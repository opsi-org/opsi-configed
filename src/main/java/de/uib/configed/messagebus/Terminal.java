package de.uib.configed.messagebus;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public final class Terminal {
	private static final int DEFAULT_TERMINAL_COLUMNS = 80;
	private static final int DEFAULT_TERMINAL_ROWS = 24;
	private static final int DEFAULT_TIME_TO_BLOCK_IN_MS = 5000;

	private static Terminal instance;
	private JediTermWidget widget;
	private JFrame frame;

	private Messagebus messagebus;
	private String terminalChannel;
	private String terminalId;

	private CountDownLatch locker;

	private Terminal() {
	}

	public static Terminal getInstance() {
		if (instance == null) {
			instance = new Terminal();
		}

		return instance;
	}

	public void setMessagebus(Messagebus messagebus) {
		this.messagebus = messagebus;
	}

	public void setTerminalChannel(String value) {
		this.terminalChannel = value;
	}

	public String getTerminalChannel() {
		return this.terminalChannel;
	}

	public void setTerminalId(String value) {
		this.terminalId = value;
	}

	public String getTerminalId() {
		return this.terminalId;
	}

	public int getColumnCount() {
		return widget.getTerminalDisplay().getColumnCount();
	}

	public int getRowCount() {
		return widget.getTerminalDisplay().getRowCount();
	}

	public void lock() {
		try {
			locker = new CountDownLatch(1);
			if (locker.await(DEFAULT_TIME_TO_BLOCK_IN_MS, TimeUnit.MILLISECONDS)) {
				Logging.info(this, "thread was unblocked");
			} else {
				Logging.info(this, "time ellapsed");
			}
		} catch (InterruptedException ie) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	public void unlock() {
		locker.countDown();
	}

	private JediTermWidget createTerminalWidget() {
		widget = new JediTermWidget(DEFAULT_TERMINAL_COLUMNS, DEFAULT_TERMINAL_ROWS, new DefaultSettingsProvider());
		widget.setDropTarget(new FileUpload());
		return widget;
	}

	public void createAndShowGUI() {
		frame = new JFrame("Terminal");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setContentPane(createTerminalWidget());
		frame.setIconImage(Globals.mainIcon);
		frame.pack();
		frame.setLocationRelativeTo(ConfigedMain.getMainFrame());
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					messagebus.disconnect();
					widget.stop();
					frame.dispose();
				} catch (InterruptedException ex) {
					Logging.warning(this, "thread was interrupted");
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	public void close() {
		SwingUtilities.invokeLater(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
	}

	public void connectWebSocket() {
		TtyConnector connector = new WebSocketTtyConnector(new WebSocketOutputStream(messagebus.getWebSocket()),
				WebSocketInputStream.getInstance().getReader());
		widget.setTtyConnector(connector);
		widget.start();
	}

	@SuppressWarnings("java:S2972")
	private class WebSocketTtyConnector implements TtyConnector {
		private final InputStreamReader reader;
		private final OutputStream writer;

		public WebSocketTtyConnector(OutputStream outputStream, InputStream inputStream) {
			this.writer = outputStream;
			this.reader = new InputStreamReader(inputStream);
		}

		@Override
		public boolean init(Questioner q) {
			return isConnected();
		}

		@Override
		public void close() {
			try {
				writer.close();
				WebSocketInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public void resize(Dimension termWinSize) {
			Map<String, Object> data = new HashMap<>();
			data.put("type", "terminal_resize_request");
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminalChannel);
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10_000);
			data.put("terminal_id", terminalId);
			data.put("rows", widget.getTerminalDisplay().getRowCount());
			data.put("cols", widget.getTerminalDisplay().getColumnCount());

			try {
				ObjectMapper mapper = new MessagePackMapper();
				byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
				writer.write(dataJsonBytes);
				writer.flush();
			} catch (IOException ex) {
				Logging.warning(this, "cannot resize terminal window: " + data.toString());
			}
		}

		@Override
		public int read(char[] buf, int offset, int length) throws IOException {
			return reader.read(buf, offset, length);
		}

		@Override
		public void write(byte[] bytes) {
			Map<String, Object> data = new HashMap<>();
			data.put("type", "terminal_data_write");
			data.put("id", UUID.randomUUID().toString());
			data.put("sender", "@");
			data.put("channel", terminalChannel);
			data.put("created", System.currentTimeMillis());
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("terminal_id", terminalId);
			data.put("data", bytes);

			try {
				ObjectMapper mapper = new MessagePackMapper();
				byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
				writer.write(dataJsonBytes);
				writer.flush();
			} catch (IOException ex) {
				Logging.warning("cannot send message to server: " + data.toString());
			}
		}

		@Override
		public boolean isConnected() {
			return messagebus.isConnected();
		}

		@Override
		public void write(String string) throws IOException {
			write(string.getBytes());
		}

		@Override
		public int waitFor() {
			return 0;
		}

		@Override
		public boolean ready() throws IOException {
			return reader.ready();
		}
	}

	@SuppressWarnings("java:S2972")
	private class FileUpload extends DropTarget {
		@SuppressWarnings("unchecked")
		private String getFilename(DropTargetDropEvent e) {
			String filename = null;

			List<File> droppedFile;

			try {
				droppedFile = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

				if (droppedFile.size() > 1) {
					return null;
				}

				filename = droppedFile.get(0).toString();
				Logging.info(this, "dropped file: " + filename);
			} catch (UnsupportedFlavorException ex) {
				Logging.warning(this, "this should not happen, unless javaFileListFlavor is no longer supported");
			} catch (IOException ex) {
				Logging.warning(this, "cannot retrieve dropped file");
			}

			return filename;
		}

		@Override
		public synchronized void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			File file = new File(getFilename(e));

			try (FileInputStream reader = new FileInputStream(file)) {
				int chunkSize = 100000;
				String fileId = UUID.randomUUID().toString();
				int chunk = 0;
				int offset = 0;

				Map<String, Object> data = new HashMap<>();
				data.put("type", "file_upload_request");
				data.put("id", UUID.randomUUID().toString());
				data.put("sender", "@");
				data.put("channel", terminalChannel);
				data.put("created", System.currentTimeMillis());
				data.put("expires", System.currentTimeMillis() + 10000);
				data.put("file_id", fileId);
				data.put("content_type", "application/octet-stream");
				data.put("name", file.getName());
				data.put("size", Files.size(file.toPath()));
				data.put("terminal_id", terminalId);

				Logging.debug(this, "file upload request: " + data.toString());

				ObjectMapper mapper = new MessagePackMapper();
				byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
				messagebus.send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));

				byte[] buf = new byte[chunkSize];

				while (reader.read(buf) > 0) {
					offset += chunkSize;
					chunk += 1;
					boolean last = offset >= Files.size(file.toPath());

					data.clear();
					data.put("type", "file_chunk");
					data.put("id", UUID.randomUUID().toString());
					data.put("sender", "@");
					data.put("channel", terminalChannel);
					data.put("created", System.currentTimeMillis());
					data.put("expires", System.currentTimeMillis() + 10000);
					data.put("file_id", fileId);
					data.put("number", chunk);
					data.put("data", new String(buf).replace("\0", "").getBytes());
					data.put("last", last);

					Logging.debug(this, "uploading file chunk: " + data.toString());

					dataJsonBytes = mapper.writeValueAsBytes(data);
					messagebus.send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
				}
			} catch (IOException ex) {
				Logging.error("cannot upload file to server: " + file.getAbsolutePath());
			}
		}
	}
}
