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
import java.util.Iterator;
import java.util.List;
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

	private List<String> existingHostNames;

	// private static boolean macAddressFieldVisible = false;
	// private static boolean macAddressFieldVisibleSet = false;

	protected int wLeftLabel = Globals.BUTTON_WIDTH + 20;

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
			
		}
		return instance;
	}

	public static NewClientDialog getInstance() {
		
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
		jComboDomain.setModel(new DefaultComboBoxModel<>(this.domains));
	}

	public void setHostNames(List<String> existingHostNames) {
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
			@Override
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
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					
					// e.getDocument().getText(e.getOffset(), e.getLength());
					String newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
					logging.debug(this, " --------->" + newPiece + "<");

					// if ( (e.getDocument().getText(e.getOffset(), e.getLength()) ).equals ("\t") )
					if (newPiece.equals("\t")) {
						
						macAddressField.requestFocus();
					}
				} catch (javax.swing.text.BadLocationException ex) {
				}

			}

			@Override
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
		ipAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' }, 28, Character.MIN_VALUE, 4, false),
				"", 24);

		labelShutdownDefault = new LabelChecked();
		labelShutdownDefault.setText(configed.getResourceValue("NewClientDialog.installByShutdown") + " "
				+ configed.getResourceValue("NewClientDialog.serverDefault"));

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(configed.getResourceValue("NewClientDialog.installByShutdown"));

		labelUefiDefault = new LabelChecked();
		labelUefiDefault.setText(configed.getResourceValue("NewClientDialog.boottype") + " "
				+ configed.getResourceValue("NewClientDialog.serverDefault"));
		
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
				.addGroup(gpl.createSequentialGroup().addGroup(gpl.createParallelGroup()
						.addGroup(gpl.createSequentialGroup()
								.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(
										jLabelHostname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
						.addGroup(gpl.createSequentialGroup()
								.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
								.addComponent(jTextHostname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(gpl.createParallelGroup()
								.addGroup(gpl.createSequentialGroup()
										.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
										.addComponent(jLabelDomainname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE))
								.addGroup(gpl.createSequentialGroup()
										.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
										.addComponent(jComboDomain, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)))
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				/////// DESCRIPTION + INVENTORY
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelDescription, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jTextDescription, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelInventoryNumber, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jTextInventoryNumber, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))

				/////// NOTES
				.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(jLabelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE - 2, Globals.VGAP_SIZE - 2, Globals.VGAP_SIZE - 2)
						.addComponent(jTextNotes, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.VGAP_SIZE - 2, Globals.VGAP_SIZE - 2, Globals.VGAP_SIZE - 2))
				/////// MAC-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(jLabelMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(labelInfoMac, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
						.addComponent(macAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(jLabelIpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(labelInfoIP, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
						.addComponent(ipAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				/////// InstallByShutdown
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelShutdownDefault, 2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH,
								3 * Globals.BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jCheckShutdownInstall, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				/////// UEFI
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelUefiDefault, 2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH,
								3 * Globals.BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jCheckUefi, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				/////// WAN
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelWanDefault, 2 * Globals.BUTTON_WIDTH, 3 * Globals.BUTTON_WIDTH,
								3 * Globals.BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jCheckWan, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				// depot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelDepot, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jComboDepots, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				// group
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelPrimaryGroup, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jComboPrimaryGroup, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				// netboot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelNetboot, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jComboNetboot, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
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
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelHostname).addComponent(jLabelDomainname))
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jTextHostname, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboDomain, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// DESCRIPTION
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelDescription)
						.addComponent(jTextDescription, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				/////// INVENTORY NUMBER
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelInventoryNumber)
						.addComponent(jTextInventoryNumber, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				/////// NOTES
				.addGap(Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE)
				.addComponent(jLabelNotes).addComponent(jTextNotes)

				/////// MAC-ADDRESS
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelMacAddress).addComponent(labelInfoMac))
				.addComponent(macAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// IP-ADDRESS
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelIpAddress).addComponent(labelInfoIP))
				.addComponent(ipAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				/////// SHUTDOWN
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelShutdownDefault, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jCheckShutdownInstall, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))

				/////// UEFI
				.addGap(Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelUefiDefault, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jCheckUefi, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				/////// WAN
				.addGap(Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2, Globals.MIN_VGAP_SIZE / 2)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWanDefault, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jCheckWan, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))

				/////// depot
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboDepots, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// group
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelPrimaryGroup, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboPrimaryGroup, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// netboot
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2).addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelNetboot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboNetboot, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
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

		final GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
				BorderFactory.createLineBorder(new Color(122, 138, 153))));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE).addComponent(jCSVTemplateLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jCSVTemplateButton, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE).addComponent(jImportLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jImportButton, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)));

		northLayout.setVerticalGroup(northLayout.createSequentialGroup()
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jCSVTemplateLabel).addComponent(jCSVTemplateButton, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jImportLabel)
						.addComponent(jImportButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE));

		scrollpane.getViewport().add(panel);
		pack();
		centerOn(Globals.mainContainer);
	}

	private void createClients(Vector<Vector<Object>> clients) {
		Iterator<Vector<Object>> iter = clients.iterator();
		Vector<Vector<Object>> modifiedClients = new Vector<>();

		while (iter.hasNext()) {
			Vector<Object> client = iter.next();

			String hostname = (String) client.get(0);
			String selectedDomain = (String) client.get(1);

			if (!isBoolean((String) client.get(11)) || !isBoolean((String) client.get(12))
					|| !isBoolean((String) client.get(13))) {
				FTextArea fInfo = new FTextArea(Globals.mainFrame,
						configed.getResourceValue("NewClientDialog.nonBooleanValue.title") + " (" + Globals.APPNAME
								+ ") ",
						false, new String[] { configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);

				StringBuilder message = new StringBuilder("");
				message.append(configed.getResourceValue("NewClientDialog.nonBooleanValue.message"));
				fInfo.setMessage(message.toString());
				fInfo.setAlwaysOnTop(true);
				fInfo.setVisible(true);

				return;
			}

			if (checkClientCorrectness(hostname, selectedDomain)) {
				treatSelectedDomainForNewClient(selectedDomain);
				modifiedClients.add(client);
			}
		}

		main.createClients(modifiedClients);
	}

	private boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	private void createClient(final String hostname, final String selectedDomain, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String macaddress, final boolean shutdownInstall, final boolean uefiboot, final boolean wanConfig,
			final String group, final String netbootProduct, final String localbootProduct) {

		if (checkClientCorrectness(hostname, selectedDomain)) {
			main.createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress,
					macaddress, shutdownInstall, uefiboot, wanConfig, group, netbootProduct, localbootProduct);

			treatSelectedDomainForNewClient(selectedDomain);
		}
	}

	/*
	 * Does things that should be done for the selected Domain of every new created
	 * client; Don't really know what's happening here (TODO)
	 */
	private void treatSelectedDomainForNewClient(final String selectedDomain) {
		Vector<String> editableDomains = new Vector<>();
		ArrayList<Object> saveDomains = new ArrayList<>();
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
	}

	private boolean checkClientCorrectness(String hostname, String selectedDomain) {
		boolean goOn = true;

		if (hostname == null || hostname.equals("")) {
			goOn = false;
		}
		if (selectedDomain == null || selectedDomain.equals("")) {
			goOn = false;
		}

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
							configed.getResourceValue("FGeneralDialog.yes") });
			StringBuilder message = new StringBuilder("");
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
							configed.getResourceValue("FGeneralDialog.yes") });
			StringBuilder message = new StringBuilder("");
			message.append(configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"));
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				goOn = false;
		}

		boolean onlyNumbers = true;
		int i = 0;
		while (onlyNumbers && hostname != null && i < hostname.length()) {
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
			StringBuilder message = new StringBuilder("");
			message.append(configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Message"));
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				goOn = false;
		}

		return goOn;
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

				createClients(rows);
			}
		}
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
			StringBuilder message = new StringBuilder("");
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
