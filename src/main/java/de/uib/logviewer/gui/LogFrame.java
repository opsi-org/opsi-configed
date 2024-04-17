/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.logviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.FCreditsDialog;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.MainFrame;
import de.uib.configed.gui.logpane.LogPane;
import de.uib.logviewer.Logviewer;
import de.uib.messages.Messages;
import de.uib.utils.ExtractorUtil;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class LogFrame extends JFrame {
	private static final Pattern IS_FILE_EXTENSION_NUMBER_PATTERN = Pattern.compile("\\d+");

	private static String fileName = "";
	private StandaloneLogPane logPane;

	private JButton iconButtonOpen;
	private JButton iconButtonReload;
	private JButton iconButtonSave;
	private JButton iconButtonCopy;

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
		jMenuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		jMenuFileOpen.addActionListener((ActionEvent e) -> openFileInLogFrame());

		JMenuItem jMenuFileClose = new JMenuItem(Configed.getResourceValue("LogFrame.jMenuFileClose"));
		jMenuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		jMenuFileClose.addActionListener((ActionEvent e) -> closeFile());

		JMenuItem jMenuFileSave = new JMenuItem(Configed.getResourceValue("LogFrame.jMenuFileSave"));
		jMenuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		jMenuFileSave.addActionListener((ActionEvent e) -> logPane.save());

		JMenuItem jMenuFileReload = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jMenuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		jMenuFileReload.addActionListener((ActionEvent e) -> reloadFile());

		JMenuItem jMenuFileExit = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> Main.endApp(Main.NO_ERROR));

		JMenu jMenuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));
		jMenuFile.add(jMenuFileOpen);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileClose);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.add(MainFrame.createJMenuTheme(this::restartLogFrame));
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
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> logPane.increaseFontSize());

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> logPane.reduceFontSize());

		JMenu jMenuView = new JMenu(Configed.getResourceValue("LogFrame.jMenuView"));
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);
		return jMenuView;
	}

	private JMenu setupMenuHelp() {
		JMenuItem jMenuHelpAbout = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> Utils.showAboutAction(this));

		JMenuItem jMenuHelpCredits = new JMenuItem(Configed.getResourceValue("MainFrame.jMenuHelpCredits"));
		jMenuHelpCredits.addActionListener((ActionEvent e) -> FCreditsDialog.display(this));

		JMenu jMenuHelp = new JMenu(Configed.getResourceValue("MainFrame.jMenuHelp"));
		MainFrame.addHelpLinks(jMenuHelp);

		jMenuHelp.addSeparator();

		MainFrame.addLogfileMenus(jMenuHelp, this);

		jMenuHelp.addSeparator();

		jMenuHelp.add(jMenuHelpCredits);
		jMenuHelp.add(jMenuHelpAbout);
		return jMenuHelp;
	}

	private void setupIcons() {
		iconButtonOpen = new JButton(Utils.createImageIcon("images/openfile.gif", ""));
		iconButtonOpen.setToolTipText(Configed.getResourceValue("LogFrame.jMenuFileOpen"));
		iconButtonOpen.setFocusable(false);
		iconButtonOpen.addActionListener((ActionEvent e) -> openFileInLogFrame());

		iconButtonReload = new JButton(Utils.createImageIcon("images/reload16.png", ""));
		iconButtonReload.setToolTipText(Configed.getResourceValue("LogFrame.buttonReload"));
		iconButtonReload.setFocusable(false);
		iconButtonReload.addActionListener((ActionEvent e) -> reloadFile());

		iconButtonSave = new JButton(Utils.getSaveIcon());
		iconButtonSave.setToolTipText(Configed.getResourceValue("save"));
		iconButtonSave.setFocusable(false);
		iconButtonSave.addActionListener((ActionEvent e) -> {
			if (fileName != null && !fileName.isEmpty()) {
				logPane.save();
			}
		});

		iconButtonCopy = new JButton(Utils.createImageIcon("images/edit-copy.png", ""));
		iconButtonCopy.setToolTipText(Configed.getResourceValue("LogFrame.buttonCopy"));
		iconButtonCopy.setFocusable(false);
		iconButtonCopy.addActionListener((ActionEvent e) -> logPane.floatExternal());
	}

	private void guiInit() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Main.endApp(Main.NO_ERROR);
			}
		});

		this.setIconImage(Utils.getMainIcon());

		setupIcons();

		logPane = new StandaloneLogPane();

		GroupLayout layoutIconPane1 = new GroupLayout(getContentPane());
		getContentPane().setLayout(layoutIconPane1);

		layoutIconPane1
				.setHorizontalGroup(
						layoutIconPane1.createParallelGroup()
								.addGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
										.addComponent(iconButtonOpen, Globals.GRAPHIC_BUTTON_SIZE,
												Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(iconButtonReload, Globals.GRAPHIC_BUTTON_SIZE,
												Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(iconButtonSave, Globals.GRAPHIC_BUTTON_SIZE,
												Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(iconButtonCopy, Globals.GRAPHIC_BUTTON_SIZE,
												Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE)
										.addGap(Globals.MIN_GAP_SIZE))
								.addComponent(logPane));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(iconButtonOpen, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(iconButtonReload, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(iconButtonSave, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE)
						.addComponent(iconButtonCopy, Globals.GRAPHIC_BUTTON_SIZE, Globals.GRAPHIC_BUTTON_SIZE,
								Globals.GRAPHIC_BUTTON_SIZE))
				.addGap(Globals.MIN_GAP_SIZE).addComponent(logPane));

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
				logPane.setText(logText);
			} else {
				setEmptyData();
			}
		} else {
			setEmptyData();
		}
	}

	private void setEmptyData() {
		logPane.setText("");
		logPane.setTitle("");
		setTitle(null);
	}

	private class StandaloneLogPane extends LogPane {
		public StandaloneLogPane() {
			super("", true);
		}

		@Override
		public void reload() {
			int caretPosition = getCaretPosition();
			super.setText(reloadFile(fileName));
			super.setTitle(fileName);
			super.setCaretPosition(caretPosition);
			super.removeAllHighlights();
		}

		public void close() {
			resetFileName();
			super.setText(fileName);
			super.setTitle(fileName);
			super.removeAllHighlights();
		}

		@Override
		public void save() {
			String fn = openFile();
			if (fn != null && !fn.isEmpty()) {
				saveToFile(fn, logPane.lines);
				super.setTitle(fn);
			}
		}

		private String reloadFile(String fn) {
			if (fn != null && !fn.isEmpty()) {
				return readFile(fn);
			} else {
				Logging.error(this, "File does not exist: " + fn);
				JOptionPane.showMessageDialog(this, Configed.getResourceValue("LogFrame.fileDoesNotExist") + " " + fn,
						null, JOptionPane.WARNING_MESSAGE);
				return "";
			}
		}

		private void saveToFile(String filename, String[] logfilelines) {
			try (FileWriter fWriter = new FileWriter(filename, StandardCharsets.UTF_8)) {
				int i = 0;
				while (i < logfilelines.length) {
					fWriter.write(logfilelines[i] + "\n");
					LogFrame.this.setTitle(filename);
					i++;
				}
			} catch (IOException ex) {
				Logging.error(
						"Error encountered while trying to save to file: " + filename + "\n --- ; stop saving to file",
						ex);
			}
		}
	}

	/**********************************************************************************************/
	// File operations
	public static void setFileName(String fn) {
		LogFrame.fileName = fn;
	}

	private static String openFile() {
		JFileChooser chooser = new JFileChooser(fileName);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
				"logfiles: .log, .zip, .gz, .7z, .txt", "log", "zip", "gz", "7z", "txt"));
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(Configed.getResourceValue("LogFrame.jMenuFileOpen"));

		int returnVal = chooser.showOpenDialog(Main.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}

		return fileName;
	}

	private void openFileInLogFrame() {
		openFile();

		if (fileName != null && !fileName.isEmpty()) {
			Logging.info(this, "Used memory " + Utils.usedMemory());
			logPane.setText(readFile(fileName));
			Logging.info(this, "Used memory " + Utils.usedMemory());
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

	private String readFile(String fileName) {
		String result = "";
		File file = new File(fileName);

		if (file.isDirectory()) {
			Logging.error("This is not a file, it is a directory: " + fileName);
			resetFileName();
			JOptionPane.showMessageDialog(this,
					Configed.getResourceValue("LogFrame.notFileButDirectory") + " " + fileName, null,
					JOptionPane.WARNING_MESSAGE);
		} else if (file.exists()) {
			String fileExtension = getFileExtension(fileName);
			if (!isFileExtensionSupported(fileExtension)) {
				Logging.error(this, "File with extension " + fileExtension + " is unsupported");
				FTextArea fUnsupportedFileExtensionInfo = new FTextArea(this,
						Configed.getResourceValue("LogFrame.unsupportedFileExtension.title"),
						Configed.getResourceValue("ConfigedMain.noGroupSelected"), true,
						new String[] { Configed.getResourceValue("buttonClose") }, 400, 200);
				fUnsupportedFileExtensionInfo.setMessage(String
						.format(Configed.getResourceValue("LogFrame.unsupportedFileExtension.message"), fileExtension));
				fUnsupportedFileExtensionInfo.setVisible(true);
				result = "";
			} else if (fileName.endsWith(".log") || fileName.endsWith(".txt")
					|| IS_FILE_EXTENSION_NUMBER_PATTERN.matcher(fileExtension).matches() || !fileName.contains(".")) {
				result = readNotCompressedFile(file);
			} else {
				result = readCompressedFile(file);
			}
		} else {
			Logging.error("This file does not exist: " + fileName);
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
			StandaloneLogPane externalLogPane = new StandaloneLogPane();
			externalLogPane.externalize(entry.getKey(), logPane.getSize());
			externalLogPane.setTitle(entry.getKey());
			externalLogPane.setText(entry.getValue());
		}
	}

	private String readNotCompressedFile(File file) {
		Logging.info(this, "Start readNotCompressedFile");
		String result = "";
		try {
			InputStream fis = new FileInputStream(file);
			result = readInputStream(fis);
			fis.close();
		} catch (IOException ex) {
			Logging.error("Error opening file: " + ex);
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
			Logging.error("Error reading file: " + ex);
			JOptionPane.showMessageDialog(this, Configed.getResourceValue("LogFrame.errorReadingFile") + "\n" + ex,
					null, JOptionPane.WARNING_MESSAGE);
		}
		return sb.toString();
	}

	private static void resetFileName() {
		fileName = "";
	}
}
