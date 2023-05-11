package de.uib.logviewer.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.configed.gui.LogPane;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ActivityPanel;
import utils.ExtractorUtil;

public class LogFrame extends JFrame implements WindowListener {

	private static JFileChooser chooser;
	private static String fileName;

	//menu system

	private JMenuBar jMenuBar = new JMenuBar();

	private JMenu jMenuFile;

	private JMenu jMenuView;

	private JMenu jMenuHelp = new JMenu();
	private JMenuItem jMenuHelpSupport = new JMenuItem();
	private JMenuItem jMenuHelpDoc = new JMenuItem();
	private JMenuItem jMenuHelpForum = new JMenuItem();
	private JMenuItem jMenuHelpAbout = new JMenuItem();

	private LogPane showLogfile;

	private IconButton iconButtonOpen;
	private IconButton iconButtonReload;
	private IconButton iconButtonSave;
	private IconButton iconButtonCopy;

	private Container baseContainer;

	public LogFrame() {
		super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		baseContainer = super.getContentPane();

		Globals.container1 = baseContainer;

		guiInit();

		UIManager.put("OptionPane.yesButtonText", Configed.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", Configed.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", Configed.getResourceValue("UIManager.cancelButtonText"));
	}

	//------------------------------------------------------------------------------------------
	//configure interaction
	//------------------------------------------------------------------------------------------
	//menus

	private void setupMenuFile() {
		jMenuFile = new JMenu();

		JMenuItem jMenuFileExit = new JMenuItem();
		JMenuItem jMenuFileOpen = new JMenuItem();
		JMenuItem jMenuFileClose = new JMenuItem();
		JMenuItem jMenuFileSave = new JMenuItem();
		JMenuItem jMenuFileReload = new JMenuItem();

		jMenuFile.setText(Configed.getResourceValue("MainFrame.jMenuFile"));

		jMenuFileExit.setText(Configed.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener((ActionEvent e) -> exitAction());

		jMenuFileOpen.setText(Configed.getResourceValue("LogFrame.jMenuFileOpen"));
		jMenuFileOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileName = openFile();
				if (fileName != null) {
					Logging.info(this, "usedmemory " + Globals.usedMemory());
					showLogfile.setMainText(readFile(fileName).toString());
					Logging.info(this, "usedmemory " + Globals.usedMemory());
					showLogfile.setTitle(fileName);
					setTitle(Globals.APPNAME + " : " + fileName);
					showLogfile.removeAllHighlights();
				}
			}
		});

		jMenuFileClose.setText(Configed.getResourceValue("LogFrame.jMenuFileClose"));
		jMenuFileClose.addActionListener((ActionEvent e) -> {
			showLogfile.close();
			setTitle(Globals.APPNAME);
		});

		jMenuFileSave.setText(Configed.getResourceValue("LogFrame.jMenuFileSave"));
		jMenuFileSave.addActionListener((ActionEvent e) -> showLogfile.save());

		jMenuFileReload.setText(Configed.getResourceValue("MainFrame.jMenuFileReload"));
		jMenuFileReload.addActionListener((ActionEvent e) -> {
			if (fileName != null) {
				showLogfile.reload();
				setTitle(Globals.APPNAME + " : " + fileName);
			}
		});

		jMenuFile.add(jMenuFileOpen);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileClose);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.add(jMenuFileExit);
	}

	private void setupMenuView() {
		jMenuView = new JMenu();
		JMenuItem jMenuViewFontsizePlus = new JMenuItem();
		JMenuItem jMenuViewFontsizeMinus = new JMenuItem();

		jMenuView.setText(Configed.getResourceValue("LogFrame.jMenuView"));

		jMenuViewFontsizePlus.setText(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> {
			showLogfile.increaseFontSize();
			showLogfile.reload();
		});

		jMenuViewFontsizeMinus.setText(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> {
			showLogfile.reduceFontSize();
			showLogfile.reload();
		});

		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);

	}

	private void setupMenuHelp() {
		jMenuHelp.setText(Configed.getResourceValue("MainFrame.jMenuHelp"));

		jMenuHelpDoc.setText(Configed.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener((ActionEvent e) -> Globals.showExternalDocument(Globals.OPSI_DOC_PAGE));

		jMenuHelp.add(jMenuHelpDoc);

		jMenuHelpForum.setText(Configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener((ActionEvent e) -> Globals.showExternalDocument(Globals.OPSI_FORUM_PAGE));
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(Configed.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener((ActionEvent e) -> Globals.showExternalDocument(Globals.OPSI_SUPPORT_PAGE));
		jMenuHelp.add(jMenuHelpSupport);

		jMenuHelpAbout.setText(Configed.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener((ActionEvent e) -> showAboutAction());

		jMenuHelp.add(jMenuHelpAbout);

	}

	public void showAboutAction() {
		FrameInfodialog dlg = new FrameInfodialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setAlwaysOnTop(true);
		dlg.setVisible(true);
	}

	private void setupIcons() {
		iconButtonOpen = new IconButton(Configed.getResourceValue("LogFrame.jMenuFileOpen"), "images/openfile.gif",
				"images/images/openfile.gif", "");
		iconButtonOpen.addActionListener((ActionEvent e) -> {
			openFile();
			if (fileName != null) {
				showLogfile.setMainText(readFile(fileName).toString());
				showLogfile.setTitle(fileName);
				setTitle(Globals.APPNAME + " : " + fileName);
				showLogfile.removeAllHighlights();
			}
		});

		iconButtonReload = new IconButton(Configed.getResourceValue("LogFrame.buttonReload"), "images/reload16.png",
				"images/images/reload16.png", "");
		iconButtonReload.addActionListener((ActionEvent e) -> {
			if (fileName != null) {
				showLogfile.reload();
			}
		});

		iconButtonSave = new IconButton(Configed.getResourceValue("PopupMenuTrait.save"), "images/save.png",
				"images/images/save.png", "");
		iconButtonSave.addActionListener((ActionEvent e) -> {
			if (fileName != null) {
				showLogfile.save();
			}
		});

		iconButtonCopy = new IconButton(Configed.getResourceValue("LogFrame.buttonCopy"), "images/edit-copy.png",
				"images/images/edit-copy.png", "");
		iconButtonCopy.addActionListener((ActionEvent e) -> showLogfile.floatExternal());
	}

	private void guiInit() {

		this.addWindowListener(this);
		this.setFont(Globals.defaultFont);
		this.setIconImage(Globals.mainIcon);

		setupIcons();

		ActivityPanel activity = new ActivityPanel();
		new Thread(activity).start();
		activity.setToolTipText("activity indicator");

		JPanel iconPane = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconPane);
		iconPane.setLayout(layoutIconPane1);

		layoutIconPane1.setHorizontalGroup(layoutIconPane1.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addComponent(iconButtonOpen, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(iconButtonSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(iconButtonCopy, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(activity, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2));

		layoutIconPane1.setVerticalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(iconButtonOpen, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonSave, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(iconButtonCopy, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGroup(layoutIconPane1.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
										.addComponent(activity).addGap(0, 0, Short.MAX_VALUE)))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		//only one LogPane
		showLogfile = new LogPane("", true) {
			@Override
			public void reload() {
				int caretPosition = showLogfile.jTextPane.getCaretPosition();
				showLogfile.setMainText(reloadFile(fileName));
				showLogfile.setTitle(fileName);
				showLogfile.jTextPane.setCaretPosition(caretPosition);
				showLogfile.removeAllHighlights();
			}

			@Override
			public void close() {
				fileName = "";
				showLogfile.setMainText("");
				showLogfile.setTitle(fileName);
				showLogfile.removeAllHighlights();
			}

			@Override
			public void save() {
				String fn = openFile();
				if (!"".equals(fn) && fn != null) {
					saveToFile(fn, showLogfile.lines);
					showLogfile.setTitle(fn);
				}
			}

			@Override
			public void paste(String text) {
				fileName = "";
				showLogfile.setMainText(text);
				showLogfile.setTitle(fileName);
				showLogfile.removeAllHighlights();
			}

		};

		Globals.container1 = baseContainer;
		showLogfile.setMainText("");
		showLogfile.setTitle("unknown");
		setTitle(Globals.APPNAME);
		if (!fileName.equals("")) {
			StringBuilder sbf = readFile(fileName);
			if ((sbf != null) && sbf.length() > 0) {
				showLogfile.setTitle(fileName);
				setTitle(Globals.APPNAME + " : " + fileName);
				showLogfile.setMainText(sbf.toString());
			}
		}

		JPanel allPane = new JPanel();
		allPane.setLayout(new BorderLayout());

		allPane.add(iconPane, BorderLayout.NORTH);
		allPane.add(showLogfile, BorderLayout.CENTER);

		baseContainer.add(allPane);

		setupMenuFile();
		setupMenuView();
		setupMenuHelp();
		jMenuBar.add(jMenuFile);
		jMenuBar.add(jMenuView);
		jMenuBar.add(jMenuHelp);

		this.setJMenuBar(jMenuBar);
		showLogfile.jTextPanel.transferFocus();
		showLogfile.jTextPanel.requestFocusInWindow();

	}

	public void setFocusToJTextPane() {
		Logging.info(this, "setFocusToJTextPane");
		showLogfile.jTextPane.requestFocusInWindow();
	}

	public void exitAction() {
		System.exit(0);
	}

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		exitAction();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowClosed(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowActivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeactivated(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowIconified(WindowEvent e) {
		/* Not needed */}

	@Override
	public void windowDeiconified(WindowEvent e) {
		/* Not needed */}

	@Override
	public void paint(Graphics g) {
		try {
			super.paint(g);
		} catch (ClassCastException ex) {
			Logging.info(this, "the ugly well known exception " + ex);
		}
	}

	/**********************************************************************************************/
	// File operations
	public static void setFileName(String fn) {
		LogFrame.fileName = fn;
		setFileChooser(fn);
	}

	private static void setFileChooser(String fn) {
		if (chooser == null) {
			chooser = new JFileChooser(fn);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("logfiles: .log, .zip, .gz, .7z",
					"log", "zip", "gz", "7z"));
			chooser.setApproveButtonText("O.K.");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(Globals.APPNAME + " " + Configed.getResourceValue("LogFrame.jMenuFileOpen"));
		}
	}

	public String reloadFile(String fn) {
		String rs = "";
		if (fn != null) {
			try {
				rs = readFile(fn).toString();
			} catch (Exception ex) {
				Logging.error(this, "File does not exist: " + fn);
				showDialog("No location: \n" + fn);
			}
		} else {
			Logging.error(this, "File does not exist: " + fn);
			showDialog("No location: \n" + fn);
		}
		return rs;
	}

	public void saveToFile(String fn, String[] logfilelines) {
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(fn);
		} catch (IOException ex) {
			Logging.error("Error opening file: " + fn + "\n --- " + ex);
		}
		int i = 0;
		while (i < logfilelines.length) {
			try {
				fWriter.write(logfilelines[i] + "\n");
				setTitle(Globals.APPNAME + " : " + fn);
			} catch (IOException ex) {
				Logging.error("Error writing file: " + fn + "\n --- " + ex);
			}
			i++;
		}
		try {
			fWriter.close();
		} catch (IOException ex) {
			Logging.error("Error closing file: " + fn + "\n --- " + ex);
		}
	}

	private static void showDialog(String errorMsg) {
		JOptionPane.showMessageDialog(null, errorMsg, "Attention", JOptionPane.WARNING_MESSAGE);
	}

	private String openFile() {
		if (chooser == null) {
			setFileChooser("");
		}
		int returnVal = chooser.showOpenDialog(Globals.frame1);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}
		return fileName;
	}

	private StringBuilder readFile(String fn) {
		StringBuilder sb = new StringBuilder();
		try {
			File file = new File(fn);

			if (file.isDirectory()) {
				Logging.error("This is not a file, it is a directory: " + fn);
				showDialog("This is not a file, it is a directory: \n" + fn);
			} else {
				if (file.exists()) { // TODO
					if (fn.endsWith(".log") || fn.endsWith(".txt") || !fn.contains(".") || fn.endsWith(".ini")) {
						sb = readNotCompressedFile(file, sb);
					} else {
						//unknown extension
						try {
							sb = ExtractorUtil.unzip(file);
						} catch (Exception e) {
							sb = readNotCompressedFile(file, sb);
							Logging.warning("Error ExtractorUtil.unzip: " + e);
						}
					}
				}

			}
		} catch (Exception fc_e) {
			Logging.error("Not a valid filename: " + fn);
			showDialog("Not a valid filename: " + fn);
		}
		return sb;
	}

	private StringBuilder readNotCompressedFile(File file, StringBuilder sb) {
		Logging.info(this, "start readNotCompressedFile");
		InputStream fis;
		try {
			fis = new FileInputStream(file);
			sb = readInputStream(fis);
			fis.close();
		} catch (Exception ex) {
			Logging.error("Error opening file: " + ex);
			showDialog("Error opening file: " + ex);
		}

		return sb;

	}

	private static StringBuilder readInputStream(InputStream fis) {
		StringBuilder sb = new StringBuilder();

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
			while ((thisLine = br.readLine()) != null) {
				sb.append(thisLine);
				sb.append("\n");
			}
			br.close();
		} catch (Exception ex) {
			Logging.error("Error reading file: " + ex);
			showDialog("Error reading file: " + ex);
		}
		return sb;
	}
}
