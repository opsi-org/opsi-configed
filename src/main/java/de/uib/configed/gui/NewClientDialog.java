/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

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
import javax.swing.text.BadLocationException;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.csv.CSVFormat;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedDocument;
import de.uib.utilities.swing.LabelChecked;
import de.uib.utilities.swing.SeparatedDocument;

public final class NewClientDialog extends FGeneralDialog {

	private static NewClientDialog instance;

	private ConfigedMain main;
	private JTextField jTextHostname;

	private JComboBox<String> jComboDomain;
	private JComboBox<String> jComboDepots;
	private JTextField jTextDescription;
	private JTextField jTextInventoryNumber;
	private JTextArea jTextNotes;
	private JComboBox<String> jComboPrimaryGroup;
	private JComboBox<String> jComboNetboot;
	private JComboBox<String> jComboLocalboot;
	private JTextField systemUUIDField;
	private JTextField macAddressField;
	private JTextField ipAddressField;
	private LabelChecked labelShutdownDefault;
	private LabelChecked labelUefiDefault;
	private LabelChecked labelWanDefault;
	private JCheckBox jCheckUefi;
	private JCheckBox jCheckWan;
	private JCheckBox jCheckShutdownInstall;
	private List<String> depots;

	private List<String> domains;
	private boolean uefiboot;
	private boolean wanConfig;

	private List<String> existingHostNames;

	private int wLeftLabel = Globals.BUTTON_WIDTH + 20;

	private NewClientDialog(ConfigedMain main, List<String> depots) {
		super(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("NewClientDialog.title") + " (" + Globals.APPNAME + ")", false,
				new String[] { Configed.getResourceValue("NewClientDialog.buttonClose"),
						Configed.getResourceValue("NewClientDialog.buttonCreate") },
				700, 680);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.main = main;

		jButton2.addMouseListener(this);
		jButton2.addKeyListener(this);
		jButton2.setDefaultIcon("images/client_small.png");
		jButton2.setIcon(jButton2.getDefaultIcon());

		jButton2.setRunningActionIcon("images/waitingcircle_16.png");

		this.depots = depots;

		init();
		pack();
	}

	public static NewClientDialog getInstance(ConfigedMain main, List<String> depots) {
		if (instance == null) {
			instance = new NewClientDialog(main, depots);
			instance.init();
		} else {
			instance.setLocationRelativeTo(ConfigedMain.getMainFrame());
		}

		return instance;
	}

	public static NewClientDialog getInstance() {
		return instance;
	}

	public static void closeNewClientDialog() {
		if (instance != null) {
			instance.setVisible(false);
		}
	}

	/**
	 * Sets the given domain configuration for new clients It expects that
	 * domains is not empty and the
	 *
	 * @param domains a LinkedList, the first will be taken in the beginning
	 * @since 4.0.7.6.11
	 */
	public void setDomains(final List<String> domains) {
		this.domains = domains;
		jComboDomain.setModel(new DefaultComboBoxModel<>(domains.toArray(new String[0])));
	}

	public void setHostNames(List<String> existingHostNames) {
		this.existingHostNames = existingHostNames;
	}

	public void setGroupList(Iterable<String> groupList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboPrimaryGroup.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String group : groupList) {
			model.addElement(group);
		}
		jComboPrimaryGroup.setModel(model);
		jComboPrimaryGroup.setSelectedIndex(0);

	}

	public void setProductNetbootList(Iterable<String> productList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboNetboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList) {
			model.addElement(product);
		}
		jComboNetboot.setModel(model);
		jComboNetboot.setSelectedIndex(0);
	}

	public void setProductLocalbootList(Iterable<String> productList) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboLocalboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList) {
			model.addElement(product);
		}
		jComboLocalboot.setModel(model);
		jComboLocalboot.setSelectedIndex(0);
	}

	public void useConfigDefaults(Boolean shutdownINSTALLIsDefault, Boolean uefiIsDefault, boolean wanIsDefault) {
		labelUefiDefault.setValue(uefiIsDefault);
		labelWanDefault.setValue(wanIsDefault);
		labelShutdownDefault.setValue(shutdownINSTALLIsDefault);

		jCheckUefi.setVisible(!uefiIsDefault);
		jCheckWan.setVisible(!wanIsDefault);
		jCheckShutdownInstall.setVisible(!shutdownINSTALLIsDefault);
	}

	private void init() {
		JPanel panel = new JPanel();
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);
		if (!Main.THEMES) {
			panel.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		JLabel jCSVTemplateLabel = new JLabel(Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		JButton jCSVTemplateButton = new JButton(Configed.getResourceValue("NewClientDialog.csvTemplateButton"));
		jCSVTemplateButton.addActionListener((ActionEvent e) -> createCSVTemplate());

		JLabel jImportLabel = new JLabel(Configed.getResourceValue("NewClientDialog.importLabel"));
		JButton jImportButton = new JButton(Configed.getResourceValue("NewClientDialog.importButton"));
		jImportButton.addActionListener((ActionEvent e) -> importCSV());

		JLabel jLabelHostname = new JLabel();
		jLabelHostname.setText(Configed.getResourceValue("NewClientDialog.hostname"));
		jTextHostname = new JTextField(new CheckedDocument(/* allowedChars */ new char[] { '-', '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
				'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' }, -1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));

		JLabel jLabelDomainname = new JLabel();
		jLabelDomainname.setText(Configed.getResourceValue("NewClientDialog.domain"));
		jComboDomain = new JComboBox<>();
		jComboDomain.setEditable(true);

		JLabel jLabelDescription = new JLabel();
		jLabelDescription.setText(Configed.getResourceValue("NewClientDialog.description"));
		jTextDescription = new JTextField();

		JLabel jLabelInventoryNumber = new JLabel();
		jLabelInventoryNumber.setText(Configed.getResourceValue("NewClientDialog.inventorynumber"));
		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel();
		jLabelDepot.setText(Configed.getResourceValue("NewClientDialog.belongsToDepot"));
		jComboDepots = new JComboBox<>(depots.toArray(new String[0]));
		if (!Main.FONT) {
			jComboDepots.setFont(Globals.defaultFontBig);
		}

		JLabel labelPrimaryGroup = new JLabel(Configed.getResourceValue("NewClientDialog.primaryGroup"));
		jComboPrimaryGroup = new JComboBox<>(new String[] { "a", "ab" });
		jComboPrimaryGroup.setMaximumRowCount(10);
		if (!Main.FONT) {
			jComboPrimaryGroup.setFont(Globals.defaultFontBig);
		}

		JLabel jLabelNetboot = new JLabel();
		jLabelNetboot.setText(Configed.getResourceValue("NewClientDialog.netbootProduct"));
		jComboNetboot = new JComboBox<>(new String[] { "a", "ab" });
		jComboNetboot.setMaximumRowCount(10);
		if (!Main.FONT) {
			jComboNetboot.setFont(Globals.defaultFontBig);
		}

		JLabel jLabelLocalboot = new JLabel();
		jLabelLocalboot.setText(Configed.getResourceValue("NewClientDialog.localbootProduct"));
		jComboLocalboot = new JComboBox<>(new String[] { "a", "ab" });
		jComboLocalboot.setMaximumRowCount(10);
		if (!Main.FONT) {
			jComboLocalboot.setFont(Globals.defaultFontBig);
		}
		jComboLocalboot.setEnabled(false);

		JLabel jLabelNotes = new JLabel();
		jLabelNotes.setText(Configed.getResourceValue("NewClientDialog.notes"));
		jTextNotes = new JTextArea();
		jTextNotes.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				jTextNotes.setText(jTextNotes.getText().trim());
				// remove tab at end of text, inserted by navigating while in the panel
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				/* Not needed */}

		});

		jTextNotes.addKeyListener(this);
		// we shall extend the KeyListener from the superclass method for jTextNotes to
		// handle backtab (below)

		jTextNotes.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					String newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
					Logging.debug(this, "newPiece: '" + newPiece + "'");

					if ("\t".equals(newPiece)) {
						systemUUIDField.requestFocus();
					}
				} catch (BadLocationException ex) {
					Logging.warning(this, "BadLocationException thrown: ", ex);
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				/* Not needed */}

			@Override
			public void removeUpdate(DocumentEvent e) {
				/* Not needed */}

		});

		jTextNotes.setBorder(BorderFactory.createLineBorder(Globals.NEW_CLIENT_DIALOG_BORDER_COLOR));

		JLabel labelInfoMac = new JLabel(Configed.getResourceValue("NewClientDialog.infoMac"));
		if (!Main.FONT) {
			labelInfoMac.setFont(Globals.defaultFontBig);
		}

		JLabel labelInfoIP = new JLabel(Configed.getResourceValue("NewClientDialog.infoIpAddress"));
		if (!Main.FONT) {
			labelInfoIP.setFont(Globals.defaultFontBig);
		}

		JLabel jLabelSystemUUID = new JLabel();
		jLabelSystemUUID.setText(Configed.getResourceValue("NewClientDialog.SystemUUID"));
		jLabelSystemUUID.setVisible(Main.THEMES);
		systemUUIDField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36, Character.MIN_VALUE, 36, true), "",
				36);

		systemUUIDField.addKeyListener(this);
		systemUUIDField.addMouseListener(this);
		systemUUIDField.setVisible(Main.THEMES);

		JLabel jLabelMacAddress = new JLabel();
		jLabelMacAddress.setText(Configed.getResourceValue("NewClientDialog.HardwareAddress"));
		macAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);

		JLabel jLabelIpAddress = new JLabel();
		jLabelIpAddress.setText(Configed.getResourceValue("NewClientDialog.IpAddress"));
		ipAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' }, 28, Character.MIN_VALUE, 4, false),
				"", 24);

		labelShutdownDefault = new LabelChecked();
		labelShutdownDefault.setText(Configed.getResourceValue("NewClientDialog.installByShutdown") + " "
				+ Configed.getResourceValue("NewClientDialog.serverDefault"));

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(Configed.getResourceValue("NewClientDialog.installByShutdown"));

		labelUefiDefault = new LabelChecked();
		labelUefiDefault.setText(Configed.getResourceValue("NewClientDialog.boottype") + " "
				+ Configed.getResourceValue("NewClientDialog.serverDefault"));

		if (!main.getPersistenceController().isWithUEFI()) {
			labelUefiDefault.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			labelUefiDefault.setEnabled(false);

		}

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(Configed.getResourceValue("NewClientDialog.installByShutdown"));

		jCheckUefi = new JCheckBox();
		jCheckUefi.setText(Configed.getResourceValue("NewClientDialog.boottype") + " "
				+ Configed.getResourceValue("NewClientDialog.clientspecific"));

		if (!main.getPersistenceController().isWithUEFI()) {
			jCheckUefi.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			jCheckUefi.setEnabled(false);
		}

		labelWanDefault = new LabelChecked();
		labelWanDefault.setText(Configed.getResourceValue("NewClientDialog.wanConfig") + " "
				+ Configed.getResourceValue("NewClientDialog.serverDefault"));

		if (!main.getPersistenceController().isWithWAN()) {
			labelWanDefault.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
		}

		jCheckWan = new JCheckBox();
		jCheckWan.setText(Configed.getResourceValue("NewClientDialog.wanConfig") + " "
				+ Configed.getResourceValue("NewClientDialog.clientspecific"));
		if (!main.getPersistenceController().isWithWAN()) {
			jCheckWan.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
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
				/////// SYSTEM UUID
				.addGroup(gpl.createSequentialGroup().addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
						.addComponent(jLabelSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
						.addComponent(systemUUIDField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
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
				/////// IP-ADDRESS
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
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)));
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

				/////// SYSTEM UUID
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelSystemUUID))
				.addComponent(systemUUIDField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
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
										Globals.BUTTON_HEIGHT)));

		final GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
				BorderFactory.createLineBorder(Globals.NEW_CLIENT_DIALOG_BORDER_COLOR)));

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
		setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private void createClients(List<List<Object>> clients) {
		Iterator<List<Object>> iter = clients.iterator();
		List<List<Object>> modifiedClients = new ArrayList<>();

		while (iter.hasNext()) {
			List<Object> client = iter.next();

			if (!isBoolean((String) client.get(12)) || !isBoolean((String) client.get(13))
					|| !isBoolean((String) client.get(14))) {
				FTextArea fInfo = new FTextArea(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.title") + " (" + Globals.APPNAME
								+ ") ",
						false, new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);

				StringBuilder message = new StringBuilder("");
				message.append(Configed.getResourceValue("NewClientDialog.nonBooleanValue.message"));
				fInfo.setMessage(message.toString());
				fInfo.setAlwaysOnTop(true);
				fInfo.setVisible(true);

				return;
			}

			String hostname = (String) client.get(0);
			String selectedDomain = (String) client.get(1);

			if (checkClientCorrectness(hostname, selectedDomain)) {
				treatSelectedDomainForNewClient(selectedDomain);
				modifiedClients.add(client);
			}
		}

		main.createClients(modifiedClients);
	}

	private static boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	private void createClient(final String hostname, final String selectedDomain, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String systemUUID, final String macaddress, final boolean shutdownInstall, final boolean uefiboot,
			final boolean wanConfig, final String group, final String netbootProduct, final String localbootProduct) {

		if (checkClientCorrectness(hostname, selectedDomain)) {
			main.createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress,
					systemUUID, macaddress, shutdownInstall, uefiboot, wanConfig, group, netbootProduct,
					localbootProduct);

			treatSelectedDomainForNewClient(selectedDomain);
		}
	}

	/*
	 * Does things that should be done for the selected Domain of every new created
	 * client; Don't really know what's happening here (TODO)
	 */
	private void treatSelectedDomainForNewClient(final String selectedDomain) {
		List<String> editableDomains = new ArrayList<>();
		List<Object> saveDomains = new ArrayList<>();
		int order = 0;
		saveDomains.add("" + order + ":" + selectedDomain);
		editableDomains.add(selectedDomain);
		Logging.info(this, "createClient domains" + domains);

		domains.remove(selectedDomain);

		for (String domain : domains) {
			order++;
			saveDomains.add("" + order + ":" + domain);

			editableDomains.add(domain);
		}

		Logging.debug(this, "createClient editableDomains " + editableDomains);
		main.setEditableDomains(editableDomains);
		setDomains(editableDomains);

		Logging.debug(this, "createClient saveDomains " + saveDomains);
		main.getPersistenceController().writeDomains(saveDomains);
	}

	private boolean checkClientCorrectness(String hostname, String selectedDomain) {
		boolean goOn = true;

		if (hostname == null || hostname.isEmpty()) {
			goOn = false;
		}
		if (selectedDomain == null || selectedDomain.isEmpty()) {
			goOn = false;
		}

		String opsiHostKey = "" + hostname + "." + selectedDomain;
		if (goOn && existingHostNames != null && existingHostNames.contains(opsiHostKey)) {

			if (depots.contains(opsiHostKey)) {
				JOptionPane.showMessageDialog(this,
						opsiHostKey + "\n" + Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
						Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title") + " (" + Globals.APPNAME
								+ ")",
						JOptionPane.WARNING_MESSAGE);
				goOn = false;
			}

			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question") + " (" + Globals.APPNAME
							+ ") ",
					true, new String[] { Configed.getResourceValue("FGeneralDialog.no"),
							Configed.getResourceValue("FGeneralDialog.yes") });
			StringBuilder message = new StringBuilder("");
			message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
			message.append(" \"");
			message.append(opsiHostKey);
			message.append("\" \n");
			message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));
			fQuestion.setMessage(message.toString());
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1) {
				goOn = false;
			}
		}

		if (goOn && hostname.length() > 15) {
			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question") + " ("
							+ Globals.APPNAME + ") ",
					true, new String[] { Configed.getResourceValue("FGeneralDialog.no"),
							Configed.getResourceValue("FGeneralDialog.yes") });
			StringBuilder message = new StringBuilder("");
			message.append(Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"));
			fQuestion.setMessage(message.toString());
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1) {
				goOn = false;
			}
		}

		boolean onlyNumbers = true;
		int i = 0;
		while (onlyNumbers && hostname != null && i < hostname.length()) {
			if (!Character.isDigit(hostname.charAt(i))) {
				onlyNumbers = false;
			}
			i++;
		}

		if (goOn && onlyNumbers) {
			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Question") + " ("
							+ Globals.APPNAME + ") ",
					true, new String[] { Configed.getResourceValue("FGeneralDialog.no"),
							Configed.getResourceValue("FGeneralDialog.yes") },
					350, 100);
			StringBuilder message = new StringBuilder("");
			message.append(Configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Message"));
			fQuestion.setMessage(message.toString());
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1) {
				goOn = false;
			}
		}

		return goOn;
	}

	private void importCSV() {
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV (.csv)", "csv");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showOpenDialog(ConfigedMain.getMainFrame());

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String csvFile = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!csvFile.endsWith(".csv")) {
				csvFile = csvFile.concat(".csv");
			}
			CSVImportDataDialog csvImportDataDialog = createCSVImportDataDialog(csvFile);

			if (csvImportDataDialog == null) {
				return;
			}

			if (csvImportDataDialog.getResult() == 2) {
				CSVImportDataModifier modifier = csvImportDataDialog.getModifier();
				List<List<Object>> rows = modifier.getRows();

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
			Logging.error(this, "Unable to read CSV file", e);
		}

		List<String> columnNames = new ArrayList<>();

		columnNames.add("hostname");
		columnNames.add("selectedDomain");
		columnNames.add("depotID");
		columnNames.add("description");
		columnNames.add("inventorynumber");
		columnNames.add("notes");
		columnNames.add("systemUUID");
		columnNames.add("macaddress");
		columnNames.add("ipaddress");
		columnNames.add("group");
		columnNames.add("netbootProduct");
		columnNames.add("localbootProduct");
		columnNames.add("wanConfig");
		columnNames.add("uefiBoot");
		columnNames.add("shutdownInstall");

		if (format.hasHeader() && !format.hasExpectedHeaderNames(columnNames)) {
			FTextArea fInfo = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.title") + " ("
							+ Globals.APPNAME + ") ",
					false, new String[] { Configed.getResourceValue("FGeneralDialog.ok") }, 400, 200);
			StringBuilder message = new StringBuilder("");
			message.append(Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.message") + " "
					+ columnNames.toString().replace("[", "").replace("]", ""));
			fInfo.setMessage(message.toString());
			fInfo.setLocationRelativeTo(this);
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

	public static void createCSVTemplate() {
		List<String> columnNames = new ArrayList<>();

		columnNames.add("hostname");
		columnNames.add("selectedDomain");
		columnNames.add("depotID");
		columnNames.add("description");
		columnNames.add("inventorynumber");
		columnNames.add("notes");
		columnNames.add("systemUUID");
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
	protected void preAction2() {
		super.preAction2();
		jButton2.setIcon(jButton2.getRunningActionIcon());

	}

	@Override
	protected void postAction2() {
		super.postAction2();
		jButton2.setIcon(jButton2.getDefaultIcon());
	}

	/* This method gets called when button 1 is pressed */
	@Override
	public void doAction1() {
		result = 1;
		setVisible(false);
	}

	/* This method is called when button 2 is pressed */
	@Override
	public void doAction2() {
		Logging.info(this, "doAction2");

		result = 2;

		String hostname = jTextHostname.getText();
		String selectedDomain = (String) jComboDomain.getSelectedItem();
		String depotID = (String) jComboDepots.getSelectedItem();
		String description = jTextDescription.getText();
		String inventorynumber = jTextInventoryNumber.getText();
		String notes = jTextNotes.getText().trim();
		String systemUUID = systemUUIDField.getText();
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

		boolean shutdownInstall = jCheckShutdownInstall.getSelectedObjects() != null;

		createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress, systemUUID,
				macaddress, shutdownInstall, uefiboot, wanConfig, group, netbootProduct, localbootProduct);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getSource() == jTextNotes
				&& (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
				&& e.getKeyCode() == KeyEvent.VK_TAB) {
			jTextDescription.requestFocusInWindow();
		} else {
			Logging.info(this, "keyPressed source " + e.getSource());
			super.keyPressed(e);
		}
	}
}
