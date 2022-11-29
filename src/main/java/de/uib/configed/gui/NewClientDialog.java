package de.uib.configed.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * NewClientDialog
 * Copyright:     Copyright (c) 2006-2022
 * Organisation:  uib
 * @author Jan Schneider, Rupert Roeder, Anna Sucher, Naglis Vidziunas
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.csv.CSVFormat;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.CheckedDocument;
import de.uib.utilities.swing.LabelChecked;
import de.uib.utilities.swing.SeparatedDocument;

public class NewClientDialog extends FGeneralDialog

{
	private ConfigedMain main;
	protected JPanel panel;
	protected GroupLayout gpl;
	protected JTextField jTextHostname;
	// protected JTextField jTextDomainname;
	protected JComboBox<String> jComboDomain;
	protected JComboBox<String> jComboDepots;
	protected JTextField jTextDescription;
	protected JTextField jTextInventoryNumber;
	protected JTextArea jTextNotes;
	protected JComboBox<String> jComboPrimaryGroup;
	protected JComboBox<String> jComboNetboot;
	protected JComboBox<String> jComboLocalboot;
	protected JTextField macAddressField;
	protected JTextField ipAddressField;
	protected LabelChecked labelShutdownDefault;
	protected LabelChecked labelUefiDefault;
	protected LabelChecked labelWanDefault;
	protected JCheckBox jCheckUefi;
	protected JCheckBox jCheckWan;
	protected JCheckBox jCheckShutdownInstall;
	protected Vector<String> depots;

	protected JLabel jCSVTemplateLabel;
	protected JButton jCSVTemplateButton;
	protected JLabel jImportLabel;
	protected JButton jImportButton;

	protected Vector<String> groupList;
	protected Vector<String> localbootProducts;
	protected Vector<String> netbootProducts;

	private static NewClientDialog instance;
	private Vector<String> domains;
	private boolean uefiboot;
	private boolean uefibootIsDefault;
	private boolean wanConfig;
	private boolean wanConfigIsDefault;
	private boolean shutdownInstall;
	protected boolean multidepot;

	protected java.util.List<String> existingHostNames;

	// private static boolean macAddressFieldVisible = false;
	// private static boolean macAddressFieldVisibleSet = false;

	protected int wLeftLabel = Globals.buttonWidth + 20;

	private NewClientDialog(ConfigedMain main, Vector<String> depots) {
		super(Globals.mainFrame, configed.getResourceValue("NewClientDialog.title") + " (" + Globals.APPNAME + ")",
				false, new String[] { configed.getResourceValue("NewClientDialog.buttonCreate"),
						configed.getResourceValue("NewClientDialog.buttonClose") },
				700, 600);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.main = main;

		jButton1.addMouseListener(this);
		jButton1.addKeyListener(this);
		jButton1.setDefaultIcon("images/client_small.png");
		jButton1.setIcon(jButton1.getDefaultIcon());
		// jButton1.setRolloverIcon( Globals.createImageIcon( "images/checked_box.png",
		// "Client" ) );
		// jButton1.setRunningActionIcon( "images/client_small_executing.png" );
		jButton1.setRunningActionIcon("images/waitingcircle_16.png");

		if (depots != null && depots.size() > 1) {
			multidepot = true;
		}
		this.depots = depots;

		init();
		pack();
	}

	public static NewClientDialog getInstance(ConfigedMain main, Vector<String> depots) {
		if (instance == null) {
			instance = new NewClientDialog(main, depots);
			instance.init();
		} else {
			// instance.init();
		}
		return instance;
	}

	public static NewClientDialog getInstance() {
		// instance.init();
		return instance;
	}

	public void closeNewClientDialog() {
		if (instance != null) {
			instance.setVisible(false);
		}
	}

	/*
	 * public boolean macAddressFieldIsSet()
	 * {
	 * return macAddressFieldVisibleSet;
	 * }
	 * 
	 * 
	 * public void setMacAddressFieldVisible(boolean b)
	 * {
	 * macAddressFieldVisibleSet = true; //we do this once
	 * macAddressFieldVisible = b;
	 * repaint();
	 * }
	 */

	/**
	 * Sets the given domain configuration for new clients It expects that
	 * domains is not empty and the
	 *
	 * @param domains a LinkedList, the first will be taken in the beginning
	 * @since 4.0.7.6.11
	 */
	public void setDomains(final Vector<String> domains) {
		this.domains = domains;
		jComboDomain.setModel(new DefaultComboBoxModel(this.domains));
	}

	public void setHostNames(java.util.List<String> existingHostNames) {
		this.existingHostNames = existingHostNames;
	}

	public void setGroupList(Vector<String> groupList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboPrimaryGroup.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String group : groupList)
			model.addElement(group);
		jComboPrimaryGroup.setModel(model);
		jComboPrimaryGroup.setSelectedIndex(0);

	}

	public void setProductNetbootList(Vector<String> productList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboNetboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList)
			model.addElement(product);
		jComboNetboot.setModel(model);
		jComboNetboot.setSelectedIndex(0);
	}

	public void setProductLocalbootList(Vector<String> productList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboLocalboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList)
			model.addElement(product);
		jComboLocalboot.setModel(model);
		jComboLocalboot.setSelectedIndex(0);
	}

	public void useConfigDefaults(Boolean shutdownINSTALLIsDefault, Boolean UEFIisDefault, boolean WANisDefault) {
		uefibootIsDefault = UEFIisDefault;
		wanConfigIsDefault = WANisDefault;

		labelUefiDefault.setValue(UEFIisDefault);
		labelWanDefault.setValue(WANisDefault);
		labelShutdownDefault.setValue(shutdownINSTALLIsDefault);

		jCheckUefi.setVisible(!uefibootIsDefault);
		jCheckWan.setVisible(!wanConfigIsDefault);
		jCheckShutdownInstall.setVisible(!shutdownINSTALLIsDefault);

	}

	protected void init() {
		// int width = 300;

		panel = new JPanel();
		gpl = new GroupLayout(panel);
		panel.setLayout(gpl);
		panel.setBackground(Globals.backLightBlue);

		jCSVTemplateLabel = new JLabel(configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		jCSVTemplateButton = new JButton(configed.getResourceValue("NewClientDialog.csvTemplateButton"));
		jCSVTemplateButton.addActionListener((ActionEvent e) -> createCSVTemplate());

		jImportLabel = new JLabel(configed.getResourceValue("NewClientDialog.importLabel"));
		jImportButton = new JButton(configed.getResourceValue("NewClientDialog.importButton"));
		jImportButton.addActionListener((ActionEvent e) -> importCSV());

		JLabel jLabelHostname = new JLabel();
		jLabelHostname.setText(configed.getResourceValue("NewClientDialog.hostname"));
		jTextHostname = new JTextField(new CheckedDocument(/* allowedChars */ new char[] { '-', '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
				'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' }, -1), "", 17);
		jTextHostname.setToolTipText(configed.getResourceValue("NewClientDialog.hostnameRules"));

		JLabel jLabelDomainname = new JLabel();
		jLabelDomainname.setText(configed.getResourceValue("NewClientDialog.domain"));
		jComboDomain = new JComboBox<>();
		jComboDomain.setEditable(true);
		// jTextDomainname = new JTextField(defaultDomain);

		JLabel jLabelDescription = new JLabel();
		jLabelDescription.setText(configed.getResourceValue("NewClientDialog.description"));
		jTextDescription = new JTextField();

		JLabel jLabelInventoryNumber = new JLabel();
		jLabelInventoryNumber.setText(configed.getResourceValue("NewClientDialog.inventorynumber"));
		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel();
		jLabelDepot.setText(configed.getResourceValue("NewClientDialog.belongsToDepot"));
		jComboDepots = new JComboBox<>(depots);
		jComboDepots.setFont(Globals.defaultFontBig);

		JLabel labelPrimaryGroup = new JLabel(configed.getResourceValue("NewClientDialog.primaryGroup"));
		jComboPrimaryGroup = new JComboBox<>(new String[] { "a", "ab" });
		jComboPrimaryGroup.setMaximumRowCount(10);
		jComboPrimaryGroup.setFont(Globals.defaultFontBig);

		JLabel jLabelNetboot = new JLabel();
		jLabelNetboot.setText(configed.getResourceValue("NewClientDialog.netbootProduct"));
		jComboNetboot = new JComboBox<>(new String[] { "a", "ab" });
		jComboNetboot.setMaximumRowCount(10);
		jComboNetboot.setFont(Globals.defaultFontBig);

		JLabel jLabelLocalboot = new JLabel();
		jLabelLocalboot.setText(configed.getResourceValue("NewClientDialog.localbootProduct"));
		jComboLocalboot = new JComboBox<>(new String[] { "a", "ab" });
		jComboLocalboot.setMaximumRowCount(10);
		jComboLocalboot.setFont(Globals.defaultFontBig);
		jComboLocalboot.setEnabled(false);

		JLabel jLabelNotes = new JLabel();
		jLabelNotes.setText(configed.getResourceValue("NewClientDialog.notes"));
		jTextNotes = new JTextArea();
		jTextNotes.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				jTextNotes.setText(jTextNotes.getText().trim());
				// remove tab at end of text, inserted by navigating while in the panel
			}

			@Override
			public void focusLost(FocusEvent arg0) {

			}

		});

		jTextNotes.addKeyListener(this);
		// we shall extend the KeyListener from the superclass method for jTextNotes to
		// handle backtab (below)

		jTextNotes.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			}

			public void insertUpdate(DocumentEvent e) {
				try {
					// System.out.println (" --------->" + e.getDocString newPiece =
					// e.getDocument().getText(e.getOffset(), e.getLength());
					String newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
					logging.debug(this, " --------->" + newPiece + "<");

					// if ( (e.getDocument().getText(e.getOffset(), e.getLength()) ).equals ("\t") )
					if (newPiece.equals("\t")) {
						// System.out.println ("tab");
						macAddressField.requestFocus();
					}
				} catch (javax.swing.text.BadLocationException ex) {
				}

			}

			public void removeUpdate(DocumentEvent e) {
			}
		});

		jTextNotes.setBorder(BorderFactory.createLineBorder(new Color(122, 138, 153)));

		JLabel labelInfoMac = new JLabel(configed.getResourceValue("NewClientDialog.infoMac"));
		labelInfoMac.setFont(Globals.defaultFontBig);

		JLabel labelInfoIP = new JLabel(configed.getResourceValue("NewClientDialog.infoIpAddress"));
		labelInfoIP.setFont(Globals.defaultFontBig);

		JLabel jLabelMacAddress = new JLabel();
		jLabelMacAddress.setText(configed.getResourceValue("NewClientDialog.HardwareAddress"));
		macAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);

		JLabel jLabelIpAddress = new JLabel();
		jLabelIpAddress.setText(configed.getResourceValue("NewClientDialog.IpAddress"));
		ipAddressField = new JTextField(new SeparatedDocument(
				/* allowedChars */ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' }, 12, '.', 3,
				false), "", 24);

		labelShutdownDefault = new LabelChecked();
		labelShutdownDefault.setText(configed.getResourceValue("NewClientDialog.installByShutdown") + " "
				+ configed.getResourceValue("NewClientDialog.serverDefault"));

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(configed.getResourceValue("NewClientDialog.installByShutdown"));

		labelUefiDefault = new LabelChecked();
		labelUefiDefault.setText(configed.getResourceValue("NewClientDialog.boottype") + " "
				+ configed.getResourceValue("NewClientDialog.serverDefault"));
		// labelUefiDefault.setBackground( Color.WHITE );
		if (!main.getPersistenceController().isWithUEFI()) {
			labelUefiDefault.setText(configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			labelUefiDefault.setEnabled(false);

		}

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(configed.getResourceValue("NewClientDialog.installByShutdown"));

		jCheckUefi = new JCheckBox();
		jCheckUefi.setText(configed.getResourceValue("NewClientDialog.boottype") + " "
				+ configed.getResourceValue("NewClientDialog.clientspecific"));

		if (!main.getPersistenceController().isWithUEFI()) {
			jCheckUefi.setText(configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			jCheckUefi.setEnabled(false);
		}

		labelWanDefault = new LabelChecked();
		labelWanDefault.setText(configed.getResourceValue("NewClientDialog.wanConfig") + " "
				+ configed.getResourceValue("NewClientDialog.serverDefault"));
		// labelWanDefault.setBackground( Color.WHITE );
		if (!main.getPersistenceController().isWithWAN()) {
			labelWanDefault.setText(configed.getResourceValue("NewClientDialog.wan_not_activated"));
		}

		jCheckWan = new JCheckBox();
		jCheckWan.setText(configed.getResourceValue("NewClientDialog.wanConfig") + " "
				+ configed.getResourceValue("NewClientDialog.clientspecific"));
		if (!main.getPersistenceController().isWithWAN()) {
			jCheckWan.setText(configed.getResourceValue("NewClientDialog.wan_not_activated"));
			jCheckWan.setEnabled(false);
		}

		gpl.setHorizontalGroup(gpl.createParallelGroup()
				/////// HOSTNAME
				.addGroup(gpl.createSequentialGroup()
						.addGroup(gpl.createParallelGroup().addGroup(gpl.createSequentialGroup()
								.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize).addComponent(
										jLabelHostname, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE))
								.addGroup(gpl.createSequentialGroup()
										.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
										.addComponent(jTextHostname, Globals.buttonWidth, Globals.buttonWidth,
												Short.MAX_VALUE)))
						.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
						.addGroup(gpl.createParallelGroup()
								.addGroup(gpl.createSequentialGroup()
										.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
										.addComponent(jLabelDomainname, Globals.buttonWidth, Globals.buttonWidth,
												Short.MAX_VALUE))
								.addGroup(gpl.createSequentialGroup()
										.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
										.addComponent(jComboDomain, Globals.buttonWidth, Globals.buttonWidth,
												Short.MAX_VALUE)))
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				/////// DESCRIPTION + INVENTORY
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jLabelDescription, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jTextDescription, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jLabelInventoryNumber, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jTextInventoryNumber, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))

				/////// NOTES
				.addGroup(gpl.createSequentialGroup().addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(jLabelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.vGapSize - 2, Globals.vGapSize - 2, Globals.vGapSize - 2)
						.addComponent(jTextNotes, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.vGapSize - 2, Globals.vGapSize - 2, Globals.vGapSize - 2))
				/////// MAC-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(jLabelMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(labelInfoMac, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
						.addComponent(macAddressField, Globals.firstLabelWidth, Globals.firstLabelWidth,
								Globals.firstLabelWidth)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(jLabelIpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(labelInfoIP, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
						.addComponent(ipAddressField, Globals.firstLabelWidth, Globals.firstLabelWidth,
								Globals.firstLabelWidth)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				/////// InstallByShutdown
				.addGroup(
						gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(labelShutdownDefault, 2 * Globals.buttonWidth, 3 * Globals.buttonWidth,
										3 * Globals.buttonWidth)
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jCheckShutdownInstall, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				/////// UEFI
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelUefiDefault, 2 * Globals.buttonWidth, 3 * Globals.buttonWidth,
								3 * Globals.buttonWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jCheckUefi, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				/////// WAN
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelWanDefault, 2 * Globals.buttonWidth, 3 * Globals.buttonWidth,
								3 * Globals.buttonWidth)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jCheckWan, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				// depot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jLabelDepot, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jComboDepots, Globals.buttonWidth, Globals.buttonWidth, 2 * Globals.buttonWidth)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				// group
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelPrimaryGroup, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jComboPrimaryGroup, Globals.buttonWidth, Globals.buttonWidth,
								2 * Globals.buttonWidth)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				// netboot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jLabelNetboot, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jComboNetboot, Globals.buttonWidth, Globals.buttonWidth, 2 * Globals.buttonWidth)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
		// localboot
		// .addGroup( gpl.createSequentialGroup()
		// .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		// .addComponent(jLabelLocalboot, wLeftLabel , wLeftLabel, wLeftLabel)
		// .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		// .addComponent(jComboLocalboot, Globals.buttonWidth, Globals.buttonWidth,
		// 2*Globals.buttonWidth)
		// .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		// )

		);
		gpl.setVerticalGroup(gpl.createSequentialGroup()
				/////// HOSTNAME
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelHostname).addComponent(jLabelDomainname))
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jTextHostname, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(jComboDomain, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				/////// DESCRIPTION
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelDescription)
						.addComponent(jTextDescription, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				/////// INVENTORY NUMBER
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelInventoryNumber)
						.addComponent(jTextInventoryNumber, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				/////// NOTES
				.addGap(Globals.minVGapSize / 2, Globals.minVGapSize / 2, Globals.minVGapSize).addComponent(jLabelNotes)
				.addComponent(jTextNotes)

				/////// MAC-ADDRESS
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelMacAddress).addComponent(labelInfoMac))
				.addComponent(macAddressField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				/////// IP-ADDRESS
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelIpAddress).addComponent(labelInfoIP))
				.addComponent(ipAddressField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)

				/////// SHUTDOWN
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelShutdownDefault, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight)
								.addComponent(jCheckShutdownInstall, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight))

				/////// UEFI
				.addGap(Globals.minVGapSize / 2, Globals.minVGapSize / 2, Globals.minVGapSize / 2)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelUefiDefault, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(jCheckUefi, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				/////// WAN
				.addGap(Globals.minVGapSize / 2, Globals.minVGapSize / 2, Globals.minVGapSize / 2)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWanDefault, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(jCheckWan, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))

				/////// depot
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelDepot, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(jComboDepots, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				/////// group
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelPrimaryGroup, Globals.lineHeight, Globals.lineHeight,
										Globals.lineHeight)
								.addComponent(jComboPrimaryGroup, Globals.buttonHeight, Globals.buttonHeight,
										Globals.buttonHeight))
				/////// netboot
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelNetboot, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(jComboNetboot, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
		/////// localboot
		// .addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
		// .addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
		// .addComponent(jLabelLocalboot,
		/////// Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		// .addComponent(jComboLocalboot,
		/////// Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		// )
		// .addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)

		);

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
				BorderFactory.createLineBorder(new Color(122, 138, 153))));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize).addComponent(jCSVTemplateLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(jCSVTemplateButton, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(
						northLayout.createSequentialGroup().addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jImportLabel).addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jImportButton, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)));

		northLayout.setVerticalGroup(northLayout.createSequentialGroup()
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jCSVTemplateLabel).addComponent(jCSVTemplateButton, Globals.buttonHeight,
								Globals.buttonHeight, Globals.buttonHeight))
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jImportLabel)
						.addComponent(jImportButton, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize));

		scrollpane.getViewport().add(panel);
		pack();
		centerOn(Globals.mainContainer);
	}

	private void createClient(final String hostname, final String selectedDomain, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String macaddress, final boolean shutdownInstall, final boolean uefiboot, final boolean wanConfig,
			final String group, final String netbootProduct, final String localbootProduct) {
		boolean goOn = true;

		if (hostname == null || hostname.equals("")) {
			goOn = false;
		}
		if (selectedDomain == null || selectedDomain.equals("")) {
			goOn = false;
		}

		// logging.debug(this, "doAction1 host, existingHostNames.contains host " +
		// hostname + ". " + selectedDomain + ", "
		// +existingHostNames.contains(hostname + "." + selectedDomain));

		String opsiHostKey = "" + hostname + "." + selectedDomain;
		if (goOn && existingHostNames != null && existingHostNames.contains(opsiHostKey)) {

			if (depots.contains(opsiHostKey)) {
				JOptionPane.showMessageDialog(this,
						opsiHostKey + "\n" + configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
						configed.getResourceValue("NewClientDialog.OverwriteDepot.Title") + " (" + Globals.APPNAME
								+ ")",
						JOptionPane.WARNING_MESSAGE);
				goOn = false;
			}

			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
					configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question") + " (" + Globals.APPNAME
							+ ") ",
					true, new String[] { configed.getResourceValue("FGeneralDialog.no"),
							configed.getResourceValue("FGeneralDialog.yes") },
					350, 100);
			StringBuffer message = new StringBuffer("");
			message.append(configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
			message.append(" \"");
			message.append(opsiHostKey);
			message.append("\" \n");
			message.append(configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				goOn = false;

		}

		if (goOn && hostname.length() > 15) {
			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
					configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question") + " ("
							+ Globals.APPNAME + ") ",
					true, new String[] { configed.getResourceValue("FGeneralDialog.no"),
							configed.getResourceValue("FGeneralDialog.yes") },
					350, 100);
			StringBuffer message = new StringBuffer("");
			message.append(configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"));
			// message.append(" \"");
			// message.append(hostname);
			// message.append("\" \n");
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				goOn = false;
		}

		boolean onlyNumbers = true;
		int i = 0;
		while (onlyNumbers && i < hostname.length()) {
			if (!Character.isDigit(hostname.charAt(i)))
				onlyNumbers = false;
			i++;
		}

		if (goOn && onlyNumbers) {
			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
					configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Question") + " ("
							+ Globals.APPNAME + ") ",
					true, new String[] { configed.getResourceValue("FGeneralDialog.no"),
							configed.getResourceValue("FGeneralDialog.yes") },
					350, 100);
			StringBuffer message = new StringBuffer("");
			message.append(configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Message"));
			// message.append(" \"");
			// message.append(hostname);
			// message.append("\" \n");
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				goOn = false;
		}

		if (goOn) {
			main.createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress,
					macaddress, shutdownInstall, uefiboot, wanConfig, group, netbootProduct, localbootProduct);

			Vector<String> editableDomains = new Vector<String>();
			ArrayList<Object> saveDomains = new ArrayList<Object>();
			int order = 0;
			saveDomains.add("" + order + ":" + selectedDomain);
			editableDomains.add(selectedDomain);
			logging.info(this, "createClient domains" + domains);

			domains.remove(selectedDomain);

			for (String domain : domains) {
				order++;
				saveDomains.add("" + order + ":" + domain);

				editableDomains.add(domain);
			}

			logging.debug(this, "createClient editableDomains " + editableDomains);
			main.setEditableDomains(editableDomains);
			setDomains(editableDomains);

			logging.debug(this, "createClient saveDomains " + saveDomains);
			main.getPersistenceController().writeDomains(saveDomains);

			// creates dead product property (state) for newer client-agent-version
			// therefore omitted
			/*
			 * 
			 * if (jCheckShutdownInstall.getSelectedObjects() != null)
			 * {
			 * main.setInstallByShutdownProductPropertyValue(opsiHostKey, true);
			 * }
			 */

		}
	}

	private void importCSV() {
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV (.csv)", "csv");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showOpenDialog(Globals.mainFrame);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String csvFile = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!csvFile.endsWith(".csv"))
				csvFile = csvFile.concat(".csv");
			CSVImportDataDialog csvImportDataDialog = createCSVImportDataDialog(csvFile);

			if (csvImportDataDialog == null) {
				return;
			}

			if (csvImportDataDialog.getResult() == 1) {
				CSVImportDataModifier modifier = csvImportDataDialog.getModifier();
				Vector<Vector<Object>> rows = modifier.getRows();

				rows.forEach(row -> {
					String hostname = (String) row.get(0);
					String selectedDomain = (String) row.get(1);
					String depotID = (String) row.get(2);
					String description = (String) row.get(3);
					String inventorynumber = (String) row.get(4);
					String notes = (String) row.get(5);
					String macaddress = (String) row.get(6);
					String ipaddress = (String) row.get(7);
					String group = (String) row.get(8);
					String netbootProduct = (String) row.get(9);
					String localbootProduct = (String) row.get(10);

					if (!isBoolean((String) row.get(11)) || !isBoolean((String) row.get(12))
							|| !isBoolean((String) row.get(13))) {
						FTextArea fInfo = new FTextArea(Globals.mainFrame,
								configed.getResourceValue("NewClientDialog.nonBooleanValue.title") + " ("
										+ Globals.APPNAME + ") ",
								false, new String[] { configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);

						StringBuffer message = new StringBuffer("");
						message.append(configed.getResourceValue("NewClientDialog.nonBooleanValue.message"));
						fInfo.setMessage(message.toString());
						fInfo.setAlwaysOnTop(true);
						fInfo.setVisible(true);

						return;
					}

					boolean wanConfig = Boolean.parseBoolean((String) row.get(11));
					boolean uefiboot = Boolean.parseBoolean((String) row.get(12));
					boolean shutdownInstall = Boolean.parseBoolean((String) row.get(13));

					createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress,
							macaddress, shutdownInstall, uefiboot, wanConfig, group, netbootProduct, localbootProduct);
				});
			}
		}
	}

	private boolean isBoolean(String bool) {
		return bool.isEmpty() ? true
				: bool.equalsIgnoreCase(Boolean.TRUE.toString()) || bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	private CSVImportDataDialog createCSVImportDataDialog(String csvFile) {
		CSVFormat format = new CSVFormat();

		try {
			String file = new String(Files.readAllBytes(Paths.get(csvFile)), StandardCharsets.UTF_8);

			if (!file.isEmpty()) {
				format.detectFormat(csvFile);
			}
		} catch (IOException e) {
			logging.error(this, "Unable to read CSV file");
		}

		Vector<String> columnNames = new Vector<>();

		columnNames.add("hostname");
		columnNames.add("selectedDomain");
		columnNames.add("depotID");
		columnNames.add("description");
		columnNames.add("inventorynumber");
		columnNames.add("notes");
		columnNames.add("macaddress");
		columnNames.add("ipaddress");
		columnNames.add("group");
		columnNames.add("netbootProduct");
		columnNames.add("localbootProduct");
		columnNames.add("wanConfig");
		columnNames.add("uefiBoot");
		columnNames.add("shutdownInstall");

		if (format.hasHeader() && !format.hasExpectedHeaderNames(columnNames)) {
			FTextArea fInfo = new FTextArea(Globals.mainFrame,
					configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.title") + " ("
							+ Globals.APPNAME + ") ",
					false, new String[] { configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);
			StringBuffer message = new StringBuffer("");
			message.append(configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.message") + " "
					+ columnNames.toString().replace("[", "").replace("]", ""));
			fInfo.setMessage(message.toString());
			fInfo.centerOn(this);
			fInfo.setAlwaysOnTop(true);
			fInfo.setVisible(true);
			return null;
		}

		CSVImportDataModifier modifier = new CSVImportDataModifier(csvFile, columnNames);
		CSVImportDataDialog csvImportDataDialog = new CSVImportDataDialog(modifier, format);
		JPanel centerPanel = csvImportDataDialog.initPanel();

		if (centerPanel == null) {
			return null;
		}

		csvImportDataDialog.setCenterPaneInScrollpane(centerPanel);
		csvImportDataDialog.setupLayout();
		csvImportDataDialog.setDetectedOptions();
		csvImportDataDialog.setVisible(true);

		return csvImportDataDialog;
	}

	public void createCSVTemplate() {
		Vector<String> columnNames = new Vector<>();

		columnNames.add("hostname");
		columnNames.add("selectedDomain");
		columnNames.add("depotID");
		columnNames.add("description");
		columnNames.add("inventorynumber");
		columnNames.add("notes");
		columnNames.add("macaddress");
		columnNames.add("ipaddress");
		columnNames.add("group");
		columnNames.add("netbootProduct");
		columnNames.add("localbootProduct");
		columnNames.add("wanConfig");
		columnNames.add("uefiBoot");
		columnNames.add("shutdownInstall");

		CSVTemplateCreatorDialog dialog = new CSVTemplateCreatorDialog(columnNames);
		JPanel centerPanel = dialog.initPanel();

		dialog.setCenterPaneInScrollpane(centerPanel);
		dialog.setupLayout();
		dialog.setVisible(true);
	}

	@Override
	protected void preAction1() {
		super.preAction1();
		jButton1.setIcon(jButton1.getRunningActionIcon());
		// Globals.createImageIcon( "images/checked_box.png", "Client" ) );
	}

	@Override
	protected void postAction1() {
		super.postAction1();
		jButton1.setIcon(jButton1.getDefaultIcon());
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		logging.info(this, "doAction1");

		result = 1;

		// FTextArea fText = new FTextArea(Globals.mainFrame, "waiting", false, 0);
		// fText.setVisible(true);

		String hostname = jTextHostname.getText();
		String selectedDomain = (String) jComboDomain.getSelectedItem();
		String depotID = (String) jComboDepots.getSelectedItem();
		String description = jTextDescription.getText();
		String inventorynumber = jTextInventoryNumber.getText();
		String notes = jTextNotes.getText().trim();
		String macaddress = macAddressField.getText();
		String ipaddress = ipAddressField.getText();
		String group = (String) jComboPrimaryGroup.getSelectedItem();
		String netbootProduct = (String) jComboNetboot.getSelectedItem();
		String localbootProduct = (String) jComboLocalboot.getSelectedItem();

		if (main.getPersistenceController().isWithUEFI()) {
			uefiboot = false;
			if (jCheckUefi.getSelectedObjects() != null) {
				uefiboot = true;
			}
		}

		if (main.getPersistenceController().isWithWAN()) {
			wanConfig = false;
			if (jCheckWan.getSelectedObjects() != null) {
				wanConfig = true;
			}
		}

		shutdownInstall = false;
		if (jCheckShutdownInstall.getSelectedObjects() != null) {
			shutdownInstall = true;
		}

		createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress, macaddress,
				shutdownInstall, uefiboot, wanConfig, group, netbootProduct, localbootProduct);
	}

	/* This method gets called when button 2 is pressed */
	@Override
	public void doAction2() {
		result = 2;
		setVisible(false);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == jTextNotes
				&& (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
				&& e.getKeyCode() == KeyEvent.VK_TAB) {
			jTextDescription.requestFocusInWindow();
			// e.consume();
		}

		else {
			logging.info(this, "keyPressed source " + e.getSource());
			// if (e.getSource() == jButton1)
			// preAction1();
			if (e.getSource() == jButton1) {
				// jButton1.setIcon( Globals.createImageIcon(
				// "images/client_small_executing.png", "Client" ) );
			}

			super.keyPressed(e);
		}
	}

}
