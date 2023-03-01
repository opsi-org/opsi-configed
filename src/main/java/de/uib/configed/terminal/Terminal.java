package de.uib.configed.terminal;

import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.WindowConstants;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.messagebus.Messagebus;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ProgressBarPainter;

@SuppressWarnings("java:S109")
public final class Terminal {
	private static final int DEFAULT_TERMINAL_COLUMNS = 80;
	private static final int DEFAULT_TERMINAL_ROWS = 24;
	private static final int DEFAULT_TIME_TO_BLOCK_IN_MS = 5000;

	private static Terminal instance;
	private JediTermWidget widget;
	private JFrame frame;
	private JProgressBar fileUploadProgressBar;
	private JLabel uploadedFilesLabel;
	private JLabel fileNameLabel;
	private JPanel southPanel;

	private Messagebus messagebus;
	private String terminalChannel;
	private String terminalId;

	private CountDownLatch locker;
	private boolean webSocketConnected;

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

	public Messagebus getMessagebus() {
		return messagebus;
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

	public boolean isWebSocketConnected() {
		return webSocketConnected;
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

	private void createAndShowGUI() {
		frame = new JFrame(Configed.getResourceValue("Terminal.title"));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setIconImage(Globals.mainIcon);

		JPanel allPane = new JPanel();
		allPane.setBackground(Globals.BACKGROUND_COLOR_7);

		GroupLayout allLayout = new GroupLayout(allPane);
		allPane.setLayout(allLayout);

		JPanel northPanel = createNorthPanel();
		southPanel = createSouthPanel();

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(southPanel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup().addComponent(northPanel, 0, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE))
				.addGroup(allLayout.createSequentialGroup().addComponent(southPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

		frame.add(allPane);

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

	public JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setOpaque(false);

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		JediTermWidget termWidget = createTerminalWidget();
		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addComponent(termWidget));
		northLayout.setHorizontalGroup(northLayout.createParallelGroup().addComponent(termWidget));

		return northPanel;
	}

	public JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setOpaque(false);
		southPanel.setVisible(false);

		JLabel uploadingFileLabel = new JLabel(Configed.getResourceValue("Terminal.uploadingFile"));
		fileNameLabel = new JLabel();
		uploadedFilesLabel = new JLabel();

		fileUploadProgressBar = new JProgressBar();
		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarPainter(Globals.opsiLogoBlue));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new ProgressBarPainter(Globals.opsiLogoLightBlue));
		fileUploadProgressBar.putClientProperty("Nimbus.Overrides", defaults);
		fileUploadProgressBar.setStringPainted(true);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);
		southLayout.setHorizontalGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(uploadingFileLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(fileNameLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(uploadedFilesLabel, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE)
				.addComponent(fileUploadProgressBar, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));
		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(0, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(southLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(uploadingFileLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fileNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(uploadedFilesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fileUploadProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.HGAP_SIZE));

		return southPanel;
	}

	public void indicateFileUpload(File file, int currentFile, int totalFiles) {
		showFileUploadProgress(true);

		try {
			fileUploadProgressBar.setMaximum((int) Files.size(file.toPath()));
		} catch (IOException e) {
			Logging.warning(this, "unable to retrieve file size: ", e);
		}

		uploadedFilesLabel.setText(currentFile + "/" + totalFiles);
		fileNameLabel.setText(file.getAbsolutePath());
	}

	public void updateFileUploadProgressBar(int progress, int fileSize) {
		if (!southPanel.isVisible()) {
			showFileUploadProgress(true);
		}

		ByteUnitConverter converter = new ByteUnitConverter();
		ByteUnitConverter.ByteUnit byteUnit = converter.detectByteUnit(fileSize);
		String uploadedFileSize = converter.asString(converter.convertByteUnit(progress, byteUnit), byteUnit);
		String totalFileSize = converter.asString(converter.convertByteUnit(fileSize, byteUnit), byteUnit);

		fileUploadProgressBar.setValue(progress);
		fileUploadProgressBar.setString(uploadedFileSize + "/" + totalFileSize);
		fileUploadProgressBar.repaint();
	}

	public void showFileUploadProgress(boolean show) {
		southPanel.setVisible(show);
	}

	public void display() {
		if (frame == null) {
			createAndShowGUI();
		} else {
			frame.setVisible(true);
		}
	}

	public void close() {
		SwingUtilities.invokeLater(() -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));
	}

	public void connectWebSocket() {
		TtyConnector connector = new WebSocketTtyConnector(new WebSocketOutputStream(messagebus.getWebSocket()),
				WebSocketInputStream.getReader());
		widget.setTtyConnector(connector);
		widget.start();

		webSocketConnected = true;
	}

	@SuppressWarnings("java:S2972")
	private class WebSocketTtyConnector implements TtyConnector {
		private final BufferedReader reader;
		private final BufferedOutputStream writer;

		public WebSocketTtyConnector(OutputStream outputStream, InputStream inputStream) {
			this.writer = new BufferedOutputStream(outputStream);
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
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
				webSocketConnected = false;
			} catch (IOException e) {
				Logging.warning(this, "failed to close output/input stream: " + e);
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
			data.put("expires", System.currentTimeMillis() + 10000);
			data.put("terminal_id", terminalId);
			data.put("rows", widget.getTerminalDisplay().getRowCount());
			data.put("cols", widget.getTerminalDisplay().getColumnCount());

			try {
				ObjectMapper mapper = new MessagePackMapper();
				byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
				writer.write(dataJsonBytes);
				writer.flush();
			} catch (IOException ex) {
				Logging.warning(this, "cannot resize terminal window: ", ex);
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
				Logging.warning("cannot send message to server: ", ex);
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
	private static class FileUpload extends DropTarget {
		private FileUploadQueue queue;
		private BackgroundFileUploader fileUploader;

		public FileUpload() {
			this.queue = new FileUploadQueue();
			this.fileUploader = new BackgroundFileUploader(queue);
		}

		@SuppressWarnings("unchecked")
		private List<File> getDroppedFiles(DropTargetDropEvent e) {
			List<File> droppedFiles = null;

			try {
				droppedFiles = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				Logging.info(this, "dropped files: " + droppedFiles);
			} catch (UnsupportedFlavorException ex) {
				Logging.warning(this,
						"this should not happen, unless javaFileListFlavor is no longer supported: " + ex);
			} catch (IOException ex) {
				Logging.warning(this, "cannot retrieve dropped file: ", ex);
			}

			return droppedFiles;
		}

		@Override
		public synchronized void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			List<File> files = getDroppedFiles(e);

			if (files == null || files.isEmpty()) {
				Logging.info(this, "files are null or empty: " + files);
				return;
			}

			queue.addAll(files);

			if (fileUploader.isDone()) {
				fileUploader = new BackgroundFileUploader(queue);
			} else {
				fileUploader.updateTotalFilesToUpload();
			}

			fileUploader.execute();
		}
	}
}
