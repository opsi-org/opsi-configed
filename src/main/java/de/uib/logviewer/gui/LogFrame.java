/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.logviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.MenuBarController;
import de.uib.configed.gui.logpane.LogPaneMsg;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.utils.ExtractorUtil;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.WindowsPositionManager;
import de.uib.utils.logging.Logging;

public class LogFrame extends JFrame {
	private static final Pattern IS_FILE_EXTENSION_NUMBER_PATTERN = Pattern.compile("\\d+");

	private static String fileName = "";
	private StandaloneLogPane logPane;

	public LogFrame() {
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		guiInit();
	}

	@Override
	public void setTitle(String filename) {
		if (filename == null || filename.isEmpty()) {
			super.setTitle("opsi-logviewer (" + Globals.APPNAME + ")");
		} else {
			super.setTitle("opsi-logviewer (" + Globals.APPNAME + ") : " + filename);
		}
	}

	//------------------------------------------------------------------------------------------
	//configure interaction
	//------------------------------------------------------------------------------------------
	//menus

	private JMenu setupMenuFile() {
		JMenuItem jMenuFileOpen = new JMenuItem(Configed.getResourceValue("LogFrame.jMenuFileOpen"));
		Icons.addIntellijIconToMenuItem(jMenuFileOpen, "open");
		jMenuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		jMenuFileOpen.addActionListener(actionEvent -> openFileInLogFrame());

		JMenuItem jMenuFileClose = new JMenuItem(Configed.getResourceValue("LogFrame.jMenuFileClose"));
		Icons.addIntellijIconToMenuItem(jMenuFileClose, "close");
		jMenuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		jMenuFileClose.addActionListener(actionEvent -> closeFile());

		JMenuItem jMenuFileSave = new JMenuItem(Configed.getResourceValue("LogFrame.jMenuFileSave"));
		Icons.addIntellijIconToMenuItem(jMenuFileSave, "save");
		jMenuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSave.addActionListener(actionEvent -> logPane.download());

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		Icons.addIntellijIconToMenuItem(jMenuFileReload, "refresh");
		jMenuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		jMenuFileReload.addActionListener(actionEvent -> reloadFile());

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		Icons.addThemeIconToMenuItem(jMenuFileExit, "exit");
		jMenuFileExit.addActionListener(actionEvent -> Main.endApp(Main.NO_ERROR));

		JMenu jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));
		jMenuFile.add(jMenuFileOpen);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileClose);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.add(MenuBarController.createJMenuTheme(this::restartLogFrame));
		jMenuFile.add(Messages.createJMenuLanguages(this::restartLogFrame));
		jMenuFile.add(jMenuFileExit);
		return jMenuFile;
	}

	private void restartLogFrame() {
		LogFrame.this.dispose();
		Logging.info(this, "Initialize new logviewer");
		Logviewer.init();
	}

	private JMenu setupMenuView() {
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.zoomIn"));
		Icons.addIntellijIconToMenuItem(jMenuViewFontsizePlus, "zoomIn");
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener(actionEvent -> logPane.increaseFontSize());

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.zoomOut"));
		Icons.addIntellijIconToMenuItem(jMenuViewFontsizeMinus, "zoomOut");
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener(actionEvent -> logPane.reduceFontSize());

		JMenu jMenuView = new JMenu(Configed.getResourceValue("LogFrame.jMenuView"));
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);
		return jMenuView;
	}

	private JMenu setupMenuHelp() {
		JMenu jMenuHelp = new JMenu(Configed.getResourceValue("MainFrame.jMenuHelp"));
		MenuBarController.addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		MenuBarController.addLogfileMenus(jMenuHelp, this);

		jMenuHelp.addSeparator();

		MenuBarController.addCreditsMenus(jMenuHelp, this);

		return jMenuHelp;
	}

	private JToolBar createIconsToolbar() {
		JButton iconButtonOpen = new JButton(Icons.getIntellijIcon("open"));
		iconButtonOpen.setToolTipText(Configed.getResourceValue("LogFrame.jMenuFileOpen"));
		iconButtonOpen.addActionListener(actionEvent -> openFileInLogFrame());

		JButton iconButtonReload = new JButton(Icons.getIntellijIcon("refresh"));
		iconButtonReload.setToolTipText(Configed.getResourceValue("LogFrame.buttonReload"));
		iconButtonReload.addActionListener(actionEvent -> reloadFile());

		JButton iconButtonSave = new JButton(Icons.getIntellijIcon("download"));
		iconButtonSave.setToolTipText(Configed.getResourceValue("download"));
		iconButtonSave.addActionListener((ActionEvent e) -> {
			if (fileName != null && !fileName.isEmpty()) {
				logPane.download();
			}
		});

		JButton iconButtonCopy = new JButton(Icons.getIntellijIcon("copy"));
		iconButtonCopy.setToolTipText(Configed.getResourceValue("LogFrame.buttonCopy"));
		iconButtonCopy.addActionListener(actionEvent -> logPane.dispatch(new LogPaneMsg.FloatExternal()));

		JToolBar jToolBar = new JToolBar();
		jToolBar.add(iconButtonOpen);
		jToolBar.add(iconButtonReload);
		jToolBar.add(iconButtonSave);
		jToolBar.add(iconButtonCopy);

		return jToolBar;
	}

	@Override
	public void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			WindowsPositionManager.saveWindowProperties(LogFrame.this, WindowsPositionManager.LOGVIEWER);
			Main.endApp(Main.NO_ERROR);
		}
	}

	private void guiInit() {
		this.setIconImage(Icons.getMainIcon());

		JToolBar jToolBar = createIconsToolbar();

		logPane = new StandaloneLogPane(this);
		JComponent panel = logPane.initUI();

		GroupLayout layoutIconPane1 = new GroupLayout(getContentPane());
		getContentPane().setLayout(layoutIconPane1);

		layoutIconPane1
				.setHorizontalGroup(layoutIconPane1.createParallelGroup().addComponent(jToolBar).addComponent(panel));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jToolBar).addGap(Globals.MIN_GAP_SIZE).addComponent(panel));

		JMenuBar jMenuBar = new JMenuBar();
		jMenuBar.add(setupMenuFile());
		jMenuBar.add(setupMenuView());
		jMenuBar.add(setupMenuHelp());
		this.setJMenuBar(jMenuBar);
	}

	public void initLogPane() {
		if (fileName != null && !fileName.isEmpty()) {
			String logText = readFile(fileName);
			if (!logText.isEmpty()) {
				logPane.setTitle(fileName);
				setTitle(fileName);
				logPane.dispatch(new LogPaneMsg.SetLogText(logText));
			} else {
				setEmptyData();
			}
		} else {
			setEmptyData();
		}
	}

	private void setEmptyData() {
		logPane.dispatch(new LogPaneMsg.SetLogText(""));
		logPane.setTitle("");
		setTitle(null);
	}

	/**********************************************************************************************/
	// File operations
	public static void setFileName(String fn) {
		LogFrame.fileName = fn;
	}

	public static String openFile(String title) {
		JFileChooser chooser = new JFileChooser(fileName);
		chooser.setFileHidingEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
				"logfiles: .log, .zip, .gz, .7z, .txt", "log", "zip", "gz", "7z", "txt"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(title);

		int returnVal = chooser.showOpenDialog(Main.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}

		return fileName;
	}

	public String getFileName() {
		return fileName;
	}

	private void openFileInLogFrame() {
		openFile(Configed.getResourceValue("LogFrame.jMenuFileOpen"));

		if (fileName != null && !fileName.isEmpty()) {
			Logging.info(this, "Used memory ", Utils.usedMemory());
			logPane.dispatch(new LogPaneMsg.SetLogText(readFile(fileName)));
			Logging.info(this, "Used memory ", Utils.usedMemory());
			logPane.setTitle(fileName);
			setTitle(fileName);
			logPane.removeAllHighlights();
		}
	}

	private void closeFile() {
		logPane.close();
		setTitle(null);
	}

	private void reloadFile() {
		if (fileName != null && !fileName.isEmpty()) {
			logPane.reload();
			setTitle(fileName);
		}
	}

	public String readFile(String fileName) {
		String result = "";
		File file = new File(fileName);

		if (file.isDirectory()) {
			Logging.error("This is not a file, it is a directory: ", fileName);
			resetFileName();
			JOptionPane.showMessageDialog(this,
					Configed.getResourceValue("LogFrame.notFileButDirectory") + " " + fileName, null,
					JOptionPane.WARNING_MESSAGE);
		} else if (file.exists()) {
			String fileExtension = getFileExtension(fileName);
			if (!isFileExtensionSupported(fileExtension)) {
				Logging.error(this, "File with extension ", fileExtension, " is unsupported");
				JOptionPane.showMessageDialog(this,
						String.format(Configed.getResourceValue("LogFrame.unsupportedFileExtension.message"),
								fileExtension),
						Configed.getResourceValue("LogFrame.unsupportedFileExtension.title"),
						JOptionPane.ERROR_MESSAGE);
				result = "";
			} else if (fileName.endsWith(".log") || fileName.endsWith(".txt")
					|| IS_FILE_EXTENSION_NUMBER_PATTERN.matcher(fileExtension).matches() || !fileName.contains(".")) {
				result = readNotCompressedFile(file);
			} else {
				result = readCompressedFile(file);
			}
		} else {
			Logging.error("This file does not exist: ", fileName);
			resetFileName();
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("LogFrame.fileDoesNotExist") + " " + fileName,
					null, JOptionPane.WARNING_MESSAGE);
		}

		return result;
	}

	private String readCompressedFile(File file) {
		String result = "";
		TreeMap<String, String> files = new TreeMap<>(ExtractorUtil.unzip(file));
		if (!files.isEmpty()) {
			Entry<String, String> firstFile = files.firstEntry();
			setFileName(firstFile.getKey());
			result = firstFile.getValue();
			files.remove(firstFile.getKey());
			openRestFilesFromZIP(files);
		}
		return result;
	}

	private static String getFileExtension(String fileName) {
		String fileExtension = "";
		if (fileName.lastIndexOf(".") != -1) {
			fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		}
		return fileExtension;
	}

	private static boolean isFileExtensionSupported(String fileExtension) {
		if (fileExtension.isEmpty()) {
			return true;
		}
		boolean matchesCompressedFileExtension = "zip".equals(fileExtension) || "gz".equals(fileExtension)
				|| "7z".equals(fileExtension);
		boolean matchesNotCompressedFileExtension = "log".equals(fileExtension) || "txt".equals(fileExtension)
				|| IS_FILE_EXTENSION_NUMBER_PATTERN.matcher(fileExtension).matches();
		return matchesCompressedFileExtension || matchesNotCompressedFileExtension;
	}

	private void openRestFilesFromZIP(Map<String, String> files) {
		if (files == null || files.isEmpty()) {
			return;
		}
		for (Entry<String, String> entry : files.entrySet()) {
			StandaloneLogPane externalLogPane = new StandaloneLogPane(this);
			externalLogPane.externalize(entry.getKey(), getSize());
			externalLogPane.setTitle(entry.getKey());
			// externalLogPane.setLogText(entry.getValue());
			externalLogPane.dispatch(new LogPaneMsg.SetLogText(entry.getValue()));
		}
	}

	private String readNotCompressedFile(File file) {
		Logging.info(this, "Start readNotCompressedFile");
		String result = "";
		try (InputStream fis = new FileInputStream(file)) {
			result = readInputStream(fis);
		} catch (IOException ex) {
			Logging.error(ex, "Error opening file: ");
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("LogFrame.errorOpeningFile") + "\n" + ex,
					null, JOptionPane.WARNING_MESSAGE);
		}

		return result;
	}

	private String readInputStream(InputStream fis) {
		StringBuilder sb = new StringBuilder();

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
			while ((thisLine = br.readLine()) != null) {
				sb.append(thisLine);
				sb.append("\n");
			}
			br.close();
		} catch (IOException ex) {
			Logging.error(ex, "Error reading file: ");
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("LogFrame.errorReadingFile") + "\n" + ex,
					null, JOptionPane.WARNING_MESSAGE);
		}
		return sb.toString();
	}

	public static void resetFileName() {
		fileName = "";
	}
}
