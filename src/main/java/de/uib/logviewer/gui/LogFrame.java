package de.uib.logviewer.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.IconButton;
import de.uib.configed.gui.LogPane;
import de.uib.logviewer.Logview;
import de.uib.logviewer.LogviewMain;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ActivityPanel;
import utils.ExtractorUtil;

public class LogFrame extends JFrame implements WindowListener, ActionListener, KeyListener
//,PropertyChangeListener 
{

	protected int dividerLocationCentralPane = 300;
	protected int minHSizeTreePanel = 150;

	public final static int fwidth = 800;
	public final static int fheight = 600;

	final int fwidth_lefthanded = 420;
	final int fwidth_righthanded = fwidth - fwidth_lefthanded;
	final int splitterLeftRight = 15;

	final int labelproductselection_width = 200;
	final int labelproductselection_height = 40;
	final int line_height = 23;

	protected String oldNotes;

	protected Clipboard clipboard;

	protected HashMap<String, String> changedClientInfo = new HashMap();

	LogviewMain main;

	public SizeListeningPanel allPane;

	//menu system

	public static final String ITEM_ADD_CLIENT = "add client";
	public static final String ITEM_DELETE_CLIENT = "remove client";

	Map<String, List<JMenuItem>> menuItemsHost;

	Map<String, List<JMenuItem>> menuItemsOpsiclientdExtraEvent = new HashMap<>();

	JMenuBar jMenuBar1 = new JMenuBar();

	JMenu jMenuFile;
	JMenuItem jMenuFileExit;
	JMenuItem jMenuFileOpen;
	JMenuItem jMenuFileClose;
	JMenuItem jMenuFileSave;
	JMenuItem jMenuFileNew;
	JMenuItem jMenuFileReload;
	//JMenuItem jMenuFileCopy;

	JMenu jMenuView;
	JMenuItem jMenuViewFontsizePlus;
	JMenuItem jMenuViewFontsizeMinus;

	protected static JFileChooser chooser;
	protected static File logDirectory;
	protected static String fileName;
	protected File file;
	Map<String, Integer> labelledDelays;

	Map<String, String> searchedTimeSpans;
	Map<String, String> searchedTimeSpansText;

	JMenuItem jMenuRemoteControl = new JMenuItem();

	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuHelpSupport = new JMenuItem();
	JMenuItem jMenuHelpDoc = new JMenuItem();
	JMenuItem jMenuHelpDocSpecial = new JMenuItem();
	JMenuItem jMenuHelpForum = new JMenuItem();
	JMenuItem jMenuHelpInternalConfiguration = new JMenuItem();
	JMenuItem jMenuHelpAbout = new JMenuItem();
	JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem();
	JMenu jMenuHelpLoglevel = new JMenu();
	JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[Logging.LEVEL_SECRET];

	BorderLayout borderLayout1 = new BorderLayout();
	GroupLayout contentLayout;

	LogPane showLogfile;

	IconButton iconButtonOpen;
	IconButton iconButtonClose;
	IconButton iconButtonSave;
	IconButton iconButtonCopy;
	IconButton iconButtonReload;

	IconButton iconButtonPlus;
	IconButton iconButtonMinus;
	JPanel iconBarPane;
	JPanel icons;
	JPanel iconPane;

	JPopupMenu popupLogfiles = new JPopupMenu();
	JPopupMenu jPopupMenu = new JPopupMenu();

	protected FShowList fListSelectedClients;

	public Container baseContainer;

	class GlassPane extends JComponent {
		GlassPane() {
			super();
			Logging.debug(this, "glass pane initialized");
			setVisible(true);
			setOpaque(true);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					Logging.debug(this, "key typed on glass pane");
				}
			});
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Logging.info(this, "mouse on glass pane");
				}
			});

		}

		@Override
		public void paintComponent(Graphics g) {
			((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5));

			g.setColor(new Color(230, 230, 250));
			g.fillRect(0, 0, getWidth(), getHeight());
		}

	}

	GlassPane glass;

	public LogFrame(LogviewMain main) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		this.main = main;
		baseContainer = this.getContentPane();

		Globals.masterFrame = baseContainer;

		glass = new GlassPane();

		//guiInit(appletHost);
		guiInit();
		//initData();

		UIManager.put("OptionPane.yesButtonText", Logview.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", Logview.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", Logview.getResourceValue("UIManager.cancelButtonText"));

	}

	public class SizeListeningPanel extends JPanel implements ComponentListener {
		SizeListeningPanel() {
			addComponentListener(this);
		}
		//ComponentListener implementation

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		private void moveDivider1(JSplitPane splitpane, JComponent rightpane, int min_right_width, int min_left_width,
				int max_right_width) {
			if (splitpane == null || rightpane == null)
				return;

			int dividerLocation = splitpane.getDividerLocation();
			//dividerLocation initially was (fwidth_lefthanded + splitterLeftRight);
			int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
			int missingSpace = min_right_width - sizeOfRightPanel;
			if (missingSpace > 0 && dividerLocation > min_left_width) {
				splitpane.setDividerLocation(dividerLocation - missingSpace);
			}

			//logging.info(this, "moveDivider1 ");

			if (sizeOfRightPanel > max_right_width) {
				splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - max_right_width));
			}

		}

		private void moveDivider2(JSplitPane splitpane, JComponent rightpane, int min_left_width) {
			if (splitpane == null || rightpane == null)
				return;

			int completeWidth = (int) splitpane.getSize().getWidth();
			int preferred_right_width = (int) rightpane.getPreferredSize().getWidth();

			int dividerabslocation = completeWidth - preferred_right_width - splitterLeftRight;

			if (dividerabslocation < min_left_width)
				dividerabslocation = min_left_width;

			if (dividerabslocation > completeWidth - 20)
				dividerabslocation = completeWidth - 20;

			// result < 0 splitpane resets itself 
			splitpane.setDividerLocation(dividerabslocation);
		}

		@Override
		public void componentResized(ComponentEvent e) {

			try {
				repairSizes();
			} catch (Exception ex) {
				Logging.info(this, "componentResized " + ex);
			}
			Logging.debug(this, "componentResized ready");

		}

		@Override
		public void componentShown(ComponentEvent e) {
		}

		public void repairSizes() {
		}

	}

	//------------------------------------------------------------------------------------------
	//configure interaction
	//------------------------------------------------------------------------------------------
	//menus

	private void setupMenuFile() {
		jMenuFile = new JMenu();

		jMenuFileExit = new JMenuItem();
		jMenuFileOpen = new JMenuItem();
		jMenuFileClose = new JMenuItem();
		jMenuFileSave = new JMenuItem();
		jMenuFileNew = new JMenuItem();
		jMenuFileReload = new JMenuItem();

		//jMenuFileCopy = new JMenuItem();

		jMenuFile.setText(Logview.getResourceValue("MainFrame.jMenuFile"));

		jMenuFileExit.setText(Logview.getResourceValue("MainFrame.jMenuFileExit"));
		jMenuFileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exitAction();
			}
		});

		jMenuFileOpen.setText(Logview.getResourceValue("MainFrame.jMenuFileOpen"));
		jMenuFileOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileName = openFile();
				if (fileName != null) {
					Logging.info(this, "usedmemory " + Globals.usedMemory());
					showLogfile.setMainText(readFile(fileName).toString());
					Logging.info(this, "usedmemory " + Globals.usedMemory());
					// decoding before files restricted to utf-8 
					//showLogfile.setMainText(Normalizer.normalize(readFile(fileName).toString(), Form.NFD)
					//		.replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
					showLogfile.setTitle(fileName);
					setTitle(Globals.APPNAME + " : " + fileName);
					showLogfile.highlighter.removeAllHighlights();
				}
			}

		});
		jMenuFileClose.setText(Logview.getResourceValue("MainFrame.jMenuFileClose"));
		jMenuFileClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.close();
				setTitle(Globals.APPNAME);
			}

		});
		jMenuFileSave.setText(Logview.getResourceValue("MainFrame.jMenuFileSave"));
		jMenuFileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.save();
			}

		});
		jMenuFileNew.setText(Logview.getResourceValue("MainFrame.jMenuFileNew"));
		jMenuFileNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.close();
				setTitle(Globals.APPNAME);
			}

		});
		jMenuFileReload.setText(Logview.getResourceValue("MainFrame.jMenuFileReload"));
		jMenuFileReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileName != null) {
					showLogfile.setFontSize("+");
					showLogfile.reload();
					setTitle(Globals.APPNAME + " : " + fileName);
				}
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
		jMenuViewFontsizePlus = new JMenuItem();
		jMenuViewFontsizeMinus = new JMenuItem();

		jMenuView.setText(Logview.getResourceValue("MainFrame.jMenuView"));

		jMenuViewFontsizePlus.setText(Logview.getResourceValue("MainFrame.jMenuViewFontsizePlus"));
		jMenuViewFontsizePlus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.setFontSize("+");
				showLogfile.reload();
			}
		});
		jMenuViewFontsizeMinus.setText(Logview.getResourceValue("MainFrame.jMenuViewFontsizeMinus"));
		jMenuViewFontsizeMinus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.setFontSize("-");
				showLogfile.reload();
			}
		});
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);

	}

	private void setupMenuHelp() {
		jMenuHelp.setText(Logview.getResourceValue("MainFrame.jMenuHelp"));

		jMenuHelpDoc.setText(Logview.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.showExternalDocument(Globals.opsiDocpage);
			}
		});
		jMenuHelp.add(jMenuHelpDoc);

		jMenuHelpForum.setText(Logview.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.showExternalDocument(Globals.opsiForumpage);
			}
		});
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(Logview.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.showExternalDocument(Globals.opsiSupportpage);
			}
		});
		jMenuHelp.add(jMenuHelpSupport);

		jMenuHelpAbout.setText(Logview.getResourceValue("MainFrame.jMenuHelpAbout"));
		jMenuHelpAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				showAboutAction();
			}
		});

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

	private void setupIcons1() {
		icons = new JPanel();

		iconButtonOpen = new IconButton(Configed.getResourceValue("MainFrame.buttonOpen"), "images/openfile.gif",
				"images/images/openfile.gif", "");
		iconButtonOpen.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
				if (fileName != null) {
					showLogfile.setMainText(readFile(fileName).toString());
					showLogfile.setTitle(fileName);
					setTitle(Globals.APPNAME + " : " + fileName);
					showLogfile.highlighter.removeAllHighlights();
				}
			}
		});
		icons.add(iconButtonOpen);

		iconButtonReload = new IconButton(Configed.getResourceValue("MainFrame.buttonReload"), "images/reload16.png",
				"images/images/reload16.png", "");
		iconButtonReload.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileName != null) {
					showLogfile.reload();
				}
			}
		});
		icons.add(iconButtonReload);

		iconButtonSave = new IconButton(Configed.getResourceValue("MainFrame.jButtonSave"), "images/save.png",
				"images/images/save.png", "");
		iconButtonSave.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileName != null) {
					showLogfile.save();
				}
			}
		});
		icons.add(iconButtonSave);

		iconButtonCopy = new IconButton(Configed.getResourceValue("MainFrame.buttonCopy"), "images/edit-copy.png",
				"images/images/edit-copy.png", "");
		iconButtonCopy.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.floatExternal();
			}
		});
		icons.add(iconButtonCopy);

		iconButtonPlus = new IconButton(Configed.getResourceValue("MainFrame.jButtonPlus"), "images/font-plus.png",
				"images/images/font-plus.png", "");
		iconButtonPlus.setToolTipText("Ctrl - \"+\"");
		iconButtonPlus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.setFontSize("+");
				showLogfile.reload();
			}
		});
		icons.add(iconButtonPlus);

		iconButtonMinus = new IconButton(Configed.getResourceValue("MainFrame.jButtonMinus"), "images/font-minus.png",
				"images/images/font-minus.png", "");
		iconButtonMinus.setToolTipText("Ctrl - \"-\"");

		iconButtonMinus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLogfile.setFontSize("-");
				showLogfile.reload();
			}
		});
		icons.add(iconButtonMinus);

		ActivityPanel activity = new ActivityPanel();
		icons.add(activity);
		new Thread(activity).start();
		icons.setToolTipText("activity indicator");
	}

	public void clear() {
		baseContainer.remove(allPane);
	}

	private void guiInit() {
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

		this.addWindowListener(this);
		this.setFont(Globals.defaultFont);
		this.setIconImage(Globals.mainIcon);

		setupIcons1();
		iconPane = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconPane);
		iconPane.setLayout(layoutIconPane1);

		layoutIconPane1.setHorizontalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addComponent(icons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)));
		layoutIconPane1.setVerticalGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutIconPane1.createSequentialGroup()
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(icons,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		iconPane.add(icons, c);
		iconPane.setBackground(Globals.backLightBlue);

		iconBarPane = new JPanel();
		iconBarPane.setLayout(new GridBagLayout());

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		iconBarPane.add(iconPane, c);

		allPane = new SizeListeningPanel();

		allPane.setLayout(borderLayout1);

		//only one LogPane
		showLogfile = new LogPane("", true) {
			@Override
			protected void reload() {
				int caretPosition = showLogfile.jTextPane.getCaretPosition();
				showLogfile.setMainText(reloadFile(fileName));
				showLogfile.setTitle(fileName);
				showLogfile.jTextPane.setCaretPosition(caretPosition);
				showLogfile.highlighter.removeAllHighlights();
			}

			@Override
			protected void close() {
				fileName = "";
				showLogfile.setMainText("");
				showLogfile.setTitle(fileName);
				showLogfile.highlighter.removeAllHighlights();
			}

			@Override
			protected void save() {
				String fn = openFile();
				if (!fn.equals("") && fn != null) {
					saveToFile(fn, showLogfile.lines);
					showLogfile.setTitle(fn);
				}
			}

			@Override
			protected void paste(String text) {
				fileName = "";
				showLogfile.setMainText(text);
				showLogfile.setTitle(fileName);
				showLogfile.highlighter.removeAllHighlights();
			}

		};

		Globals.masterFrame = baseContainer;
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

		allPane.add(showLogfile, BorderLayout.CENTER);
		allPane.add(iconBarPane, BorderLayout.NORTH);
		baseContainer.add(allPane);

		setupMenuFile();
		setupMenuView();
		setupMenuHelp();
		jMenuBar1.add(jMenuFile);
		jMenuBar1.add(jMenuView);
		jMenuBar1.add(jMenuHelp);

		this.setJMenuBar(jMenuBar1);
		showLogfile.jTextPanel.transferFocus();
		showLogfile.jTextPanel.requestFocusInWindow();

	}

	public void setFocusToJTextPane() {
		Logging.info(this, "setFocusToJTextPane");
		showLogfile.jTextPane.requestFocusInWindow();
	}

	public void startSizing(int width, int height) {
		Logging.info(this, "startSizing width, height " + width + ", " + height);
		setSize(width, height);
	}

	public void exitAction() {
		main.finishApp(true, 0);
	}

	/* WindowListener implementation */
	@Override
	public void windowClosing(WindowEvent e) {
		main.finishApp(true, 0);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		;
	}

	@Override
	public void windowActivated(WindowEvent e) {
		;
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		;
	}

	@Override
	public void windowIconified(WindowEvent e) {
		;
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		;
	}

	//MouseListener implementation
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public void enableAfterLoading() {

	}

	@Override
	public void paint(java.awt.Graphics g) {
		try {
			super.paint(g);
		} catch (java.lang.ClassCastException ex) {
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
			chooser.setDialogTitle(Globals.APPNAME + " " + Logview.getResourceValue("MainFrame.fileChooser"));
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

	private void showDialog(String errorMsg) {
		JOptionPane.showMessageDialog(null, errorMsg, "Attention", JOptionPane.WARNING_MESSAGE);
	}

	private String openFile() {
		if (chooser == null) {
			setFileChooser("");
		}
		int returnVal = chooser.showOpenDialog(Globals.frame1);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (chooser != null) {
				fileName = chooser.getSelectedFile().getAbsolutePath();
			} else {
				Logging.error("Not a valid filename: " + fileName);
				showDialog("Not a valid filename: \n" + fileName);
			}
		}
		return fileName;
	}

	private StringBuilder readFile(String fn) {
		StringBuilder sb = new StringBuilder();
		try {
			file = new File(fn);

			if (file.isDirectory()) {
				Logging.error("This is not a file, it is a directory: " + fn);
				showDialog("This is not a file, it is a directory: \n" + fn);
			} else {
				if (file.exists()) { // TODO
					if (fn.endsWith(".log") || fn.endsWith(".txt") || !fn.contains(".") || fn.endsWith(".ini"))
					// .log, .txt, .ini  and without extension
					{
						sb = readNotCompressedFile(file, sb);
					} else //unknown extension
					{
						//extractable?
						//boolean extractable = true;
						try {
							sb = ExtractorUtil.unzip(file);
						} catch (Exception e) {
							//extractable = false;
							sb = readNotCompressedFile(file, sb);
							Logging.warning("Error ExtractorUtil.unzip: " + e);
						}
						/*
						if (!extractable)
						{
							sb = readNotCompressedFile(file, sb);
						}*/
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

	private StringBuilder readInputStream(InputStream fis) {
		boolean fileRead = false;
		StringBuilder sb = new StringBuilder();

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
			if (br != null) {
				while ((thisLine = br.readLine()) != null) {
					sb.append(thisLine);
					sb.append("\n");
					fileRead = true;
				}
				br.close();
			} else {
				Logging.error("Error in reading file " + file);
				showDialog("Error in reading file " + file);
			}
		} catch (Exception ex) {
			Logging.error("Error reading file: " + ex);
			showDialog("Error reading file: " + ex);
		}
		return sb;
	}

	/*****************************************************************************************/

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
