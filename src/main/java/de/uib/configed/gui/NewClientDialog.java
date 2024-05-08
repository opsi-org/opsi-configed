/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;

import org.apache.commons.csv.CSVFormat;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.csv.CSVFormatDetector;
import de.uib.configed.gui.csv.CSVImportDataDialog;
import de.uib.configed.gui.csv.CSVImportDataModifier;
import de.uib.configed.gui.csv.CSVTemplateCreatorDialog;
import de.uib.configed.type.HostInfo;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;
import de.uib.utils.swing.SeparatedDocument;

public final class NewClientDialog extends FGeneralDialog implements KeyListener {
	private static final int WIDTH_LEFT_LABEL = Globals.BUTTON_WIDTH + 20;

	private ConfigedMain configedMain;

	private JComboBox<String> jComboDomain;
	private JComboBox<String> jComboDepots;
	private JTextField jTextGroupsSelection;
	private JComboBox<String> jComboNetboot;
	private JTextField jTextHostname;
	private JTextField jTextDescription;
	private JTextField jTextInventoryNumber;
	private JTextField systemUUIDField;
	private JTextField macAddressField;
	private JTextField ipAddressField;
	private JTextArea jTextNotes;
	private JCheckBox jCheckUefi;
	private JCheckBox jCheckWan;
	private JCheckBox jCheckShutdownInstall;

	private List<String> depots;
	private List<String> domains;
	private List<String> newDomainsList;
	private List<String> existingHostNames;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public NewClientDialog(ConfigedMain configedMain, List<String> depots) {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("NewClientDialog.title"), false, new String[] {
				Configed.getResourceValue("buttonClose"), Configed.getResourceValue("NewClientDialog.buttonCreate") },
				730, 670);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		this.configedMain = configedMain;
		this.depots = depots;

		init();
	}

	/**
	 * Sets the given domain configuration for new clients It expects that
	 * domains is not empty and the
	 *
	 * @param domains a LinkedList, the first will be taken in the beginning
	 * @since 4.0.7.6.11
	 */
	public void setDomains() {
		this.domains = newDomainsList;
		jComboDomain.setModel(new DefaultComboBoxModel<>(domains.toArray(new String[0])));
	}

	public void setHostNames(List<String> existingHostNames) {
		this.existingHostNames = existingHostNames;
	}

	private static void setJComboBoxModel(JComboBox<String> comboBox, Iterable<String> list) {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String element : list) {
			model.addElement(element);
		}
		comboBox.setModel(model);
		comboBox.setSelectedIndex(0);
	}

	public void useConfigDefaults(Boolean shutdownINSTALLIsDefault, Boolean uefiIsDefault, boolean wanIsDefault) {
		jCheckUefi.setSelected(uefiIsDefault);
		jCheckWan.setSelected(wanIsDefault);
		jCheckShutdownInstall.setSelected(shutdownINSTALLIsDefault);
	}

	private void init() {
		JLabel jLabelHostname = new JLabel(Configed.getResourceValue("NewClientDialog.hostname"));
		jTextHostname = new JTextField(new CheckedDocument(/* allowedChars */ new char[] { '-', '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
				'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' }, -1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));

		JLabel jLabelDomainname = new JLabel(Configed.getResourceValue("NewClientDialog.domain"));

		initComboDomain();

		JLabel jLabelDescription = new JLabel(Configed.getResourceValue("NewClientDialog.description"));

		jTextDescription = new JTextField();

		JLabel jLabelInventoryNumber = new JLabel(Configed.getResourceValue("NewClientDialog.inventorynumber"));

		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel(Configed.getResourceValue("NewClientDialog.belongsToDepot"));

		jComboDepots = new JComboBox<>(depots.toArray(new String[0]));

		JLabel labelPrimaryGroup = new JLabel(Configed.getResourceValue("NewClientDialog.primaryGroup"));
		jTextGroupsSelection = new JTextField();
		jTextGroupsSelection.setEditable(false);
		jTextGroupsSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				displayGroupSelectionDialog();
			}
		});

		JLabel jLabelNetboot = new JLabel(Configed.getResourceValue("NewClientDialog.netbootProduct"));

		jComboNetboot = new JComboBox<>(new String[] { "a", "ab" });
		jComboNetboot.setMaximumRowCount(10);
		List<String> netbootProductNames = persistenceController.getProductDataService().getAllNetbootProductNames();
		Collections.sort(netbootProductNames);
		setJComboBoxModel(jComboNetboot, netbootProductNames);

		JLabel jLabelNotes = new JLabel(Configed.getResourceValue("NewClientDialog.notes"));

		jTextNotes = new JTextArea();
		jTextNotes.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// remove tab at end of text, inserted by navigating while in the panel
				jTextNotes.setText(jTextNotes.getText().trim());
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				/* Not needed */}
		});

		jTextNotes.addKeyListener(this);
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

		jTextNotes.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		JLabel labelInfoMac = new JLabel(Configed.getResourceValue("NewClientDialog.infoMac"));

		JLabel labelInfoIP = new JLabel(Configed.getResourceValue("NewClientDialog.infoIpAddress"));

		JLabel jLabelSystemUUID = new JLabel(Configed.getResourceValue("NewClientDialog.SystemUUID"));

		systemUUIDField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36, Character.MIN_VALUE, 36, true), "",
				36);

		JLabel jLabelMacAddress = new JLabel(Configed.getResourceValue("NewClientDialog.HardwareAddress"));

		macAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':', 2, true), "", 17);

		JLabel jLabelIpAddress = new JLabel(Configed.getResourceValue("NewClientDialog.IpAddress"));

		ipAddressField = new JTextField(new SeparatedDocument(/* allowedChars */ new char[] { '0', '1', '2', '3', '4',
				'5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' }, 28, Character.MIN_VALUE, 4, false),
				"", 24);

		jCheckShutdownInstall = new JCheckBox(Configed.getResourceValue("NewClientDialog.installByShutdown"));

		jCheckUefi = new JCheckBox(Configed.getResourceValue("NewClientDialog.boottype"));

		if (!persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.UEFI)) {
			jCheckUefi.setText(Configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			jCheckUefi.setEnabled(false);
		}

		jCheckWan = new JCheckBox(Configed.getResourceValue("NewClientDialog.wanConfig"));
		if (!persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.WAN)) {
			jCheckWan.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			jCheckWan.setEnabled(false);
		}

		JPanel panel = new JPanel();
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		GroupLayout.Group uefiVerticalGroup = gpl.createSequentialGroup();
		GroupLayout.Group uefiHorizontalGroup = gpl.createSequentialGroup();

		if (!ServerFacade.isOpsi43()) {
			uefiVerticalGroup.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2)
					.addComponent(jCheckUefi, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);

			uefiHorizontalGroup.addGap(Globals.GAP_SIZE)
					.addComponent(jCheckUefi, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGap(Globals.MIN_GAP_SIZE);
		}

		gpl.setHorizontalGroup(gpl.createParallelGroup()
				/////// HOSTNAME
				.addGroup(gpl.createSequentialGroup()
						.addGroup(gpl.createParallelGroup()
								.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
										jLabelHostname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
								.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
										jTextHostname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
						.addGap(Globals.MIN_GAP_SIZE)
						.addGroup(gpl.createParallelGroup()
								.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
										jLabelDomainname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
								.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
										jComboDomain, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
						.addGap(Globals.MIN_GAP_SIZE))
				/////// DESCRIPTION + INVENTORY
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelDescription, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextDescription, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelInventoryNumber, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextInventoryNumber, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// NOTES
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.GAP_SIZE - 2, Globals.GAP_SIZE - 2, Globals.GAP_SIZE - 2)
						.addComponent(jTextNotes, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE - 2, Globals.GAP_SIZE - 2, Globals.GAP_SIZE - 2))
				/////// SYSTEM UUID
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(systemUUIDField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// MAC-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoMac, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(macAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// IP-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelIpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoIP, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(ipAddressField, Globals.FIRST_LABEL_WIDTH, Globals.FIRST_LABEL_WIDTH,
								Globals.FIRST_LABEL_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// InstallByShutdown and WAN
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jCheckShutdownInstall, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jCheckWan, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// UEFI
				.addGroup(uefiHorizontalGroup)
				// depot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelDepot, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jComboDepots, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				// group
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelPrimaryGroup, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextGroupsSelection, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				// netboot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelNetboot, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE).addComponent(jComboNetboot, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE)));
		gpl.setVerticalGroup(gpl.createSequentialGroup()
				/////// HOSTNAME
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelHostname).addComponent(jLabelDomainname))
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jTextHostname, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboDomain, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// DESCRIPTION
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelDescription)
						.addComponent(jTextDescription, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				/////// INVENTORY NUMBER
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(jLabelInventoryNumber)
						.addComponent(jTextInventoryNumber, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				/////// NOTES
				.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE)
				.addComponent(jLabelNotes).addComponent(jTextNotes)
				/////// SYSTEM UUID
				.addGap(Globals.GAP_SIZE).addGroup(gpl.createParallelGroup().addComponent(jLabelSystemUUID))
				.addComponent(systemUUIDField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// MAC-ADDRESS
				.addGap(Globals.GAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelMacAddress).addComponent(labelInfoMac))
				.addComponent(macAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// IP-ADDRESS
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(gpl.createParallelGroup().addComponent(jLabelIpAddress).addComponent(labelInfoIP))
				.addComponent(ipAddressField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				/////// SHUTDOWN and WAN
				.addGap(Globals.GAP_SIZE)
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jCheckShutdownInstall, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jCheckWan, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				/////// UEFI
				.addGroup(uefiVerticalGroup)
				/////// depot
				.addGap(Globals.GAP_SIZE)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboDepots, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// group
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(labelPrimaryGroup, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jTextGroupsSelection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
				/////// netboot
				.addGap(Globals.MIN_GAP_SIZE).addGroup(
						gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelNetboot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(jComboNetboot, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)));

		createNorthPanel();

		scrollpane.getViewport().add(panel);
	}

	private void displayGroupSelectionDialog() {
		FSelectionList groupsSelectionDialog = new FSelectionList(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("NewClientDialog.groupSelectionDialog.title"), true,
				new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") }, 500,
				300);
		groupsSelectionDialog.enableMultiSelection();
		groupsSelectionDialog.setListData(
				PersistenceControllerFactory.getPersistenceController().getGroupDataService().getHostGroupIds());
		groupsSelectionDialog.setPreviousSelectionValues(
				Arrays.asList(jTextGroupsSelection.getText().replace("; ", ";").split(";")));
		groupsSelectionDialog.setVisible(true);

		if (groupsSelectionDialog.getResult() == 2) {
			String selectedGroups = Utils.getListStringRepresentation(groupsSelectionDialog.getSelectedValues(), 5);
			jTextGroupsSelection.setText(selectedGroups);
			jTextGroupsSelection.setToolTipText(
					"<html><body><p>" + selectedGroups.replace(";\n", "<br\\ >") + "</p></body></html>");
		}
	}

	private void initComboDomain() {
		newDomainsList = persistenceController.getConfigDataService().getDomains();

		jComboDomain = new JComboBox<>();
		jComboDomain.setEditable(true);
		setDomains();
	}

	private void createNorthPanel() {
		JLabel jCSVTemplateLabel = new JLabel(Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		JButton jCSVTemplateButton = new JButton(Configed.getResourceValue("NewClientDialog.csvTemplateButton"));
		jCSVTemplateButton.addActionListener((ActionEvent e) -> displayCSVTemplateDialog());

		JLabel jImportLabel = new JLabel(Configed.getResourceValue("NewClientDialog.importLabel"));
		JButton jImportButton = new JButton(Configed.getResourceValue("NewClientDialog.importButton"));
		jImportButton.addActionListener((ActionEvent e) -> importCSV());

		final GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"))));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jCSVTemplateLabel)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jCSVTemplateButton, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(
						northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jImportLabel)
								.addGap(Globals.GAP_SIZE).addComponent(jImportButton, Globals.BUTTON_WIDTH,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)));

		northLayout
				.setVerticalGroup(
						northLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(jCSVTemplateLabel).addComponent(jCSVTemplateButton,
												Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
								.addGap(Globals.MIN_GAP_SIZE)
								.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(jImportLabel).addComponent(jImportButton, Globals.BUTTON_HEIGHT,
												Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
								.addGap(Globals.MIN_GAP_SIZE));
	}

	private void createClients(List<List<Object>> clients) {
		Iterator<List<Object>> iter = clients.iterator();
		List<List<Object>> modifiedClients = new ArrayList<>();

		while (iter.hasNext()) {
			List<Object> client = iter.next();

			if (!isBoolean((String) client.get(10)) || !isBoolean((String) client.get(11))
					|| !isBoolean((String) client.get(12))) {
				FTextArea fInfo = new FTextArea(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.title"), false,
						new String[] { Configed.getResourceValue("buttonClose") }, 400, 200);
				fInfo.setMessage(Configed.getResourceValue("NewClientDialog.nonBooleanValue.message"));
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

		configedMain.createClients(modifiedClients);
	}

	private static boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	private void createClient(final String hostname, final String selectedDomain, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String systemUUID, final String macaddress, final boolean shutdownInstall, final boolean uefiboot,
			final boolean wanConfig, final String[] groups, final String netbootProduct) {
		if (checkClientCorrectness(hostname, selectedDomain)) {
			Logging.debug(this,
					"createClient " + hostname + ", " + selectedDomain + ", " + depotID + ", " + description + ", "
							+ inventorynumber + ", " + notes + shutdownInstall + ", " + uefiboot + ", " + wanConfig
							+ ", " + Arrays.toString(groups) + ", " + netbootProduct);

			String newClientID = hostname + "." + selectedDomain;

			persistenceController.getHostInfoCollections().addOpsiHostName(newClientID);
			if (persistenceController.getHostDataService().createClient(hostname, selectedDomain, depotID, description,
					inventorynumber, notes, ipaddress, systemUUID, macaddress, shutdownInstall, uefiboot, wanConfig,
					groups, netbootProduct)) {
				configedMain.createClient(newClientID, groups);
			} else {
				persistenceController.getHostInfoCollections().removeOpsiHostName(newClientID);
			}

			treatSelectedDomainForNewClient(selectedDomain);
		}
	}

	/*
	 * Does things that should be done for the selected Domain of every new created
	 * client.
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

		newDomainsList = editableDomains;

		setDomains();

		Logging.debug(this, "createClient saveDomains " + saveDomains);
		persistenceController.getConfigDataService().writeDomains(saveDomains);
	}

	private boolean checkClientCorrectness(String hostname, String selectedDomain) {
		if (!areValuesValid(hostname, selectedDomain)) {
			return false;
		}

		if (!checkOpsiHostKey(hostname + "." + selectedDomain)) {
			return false;
		}

		return checkHostname(hostname);
	}

	private static boolean areValuesValid(String hostname, String selectedDomain) {
		if (hostname == null || hostname.isEmpty()) {
			return false;
		}

		return selectedDomain != null && !selectedDomain.isEmpty();
	}

	private boolean checkOpsiHostKey(String opsiHostKey) {
		if (existingHostNames != null && existingHostNames.contains(opsiHostKey)) {
			if (depots.contains(opsiHostKey)) {
				JOptionPane.showMessageDialog(this,
						opsiHostKey + "\n" + Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
						Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title"), JOptionPane.WARNING_MESSAGE);
				return false;
			}

			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question"), true,
					new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });
			StringBuilder message = new StringBuilder();
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
				return false;
			}
		}

		return true;
	}

	private boolean checkHostname(String hostname) {
		if (hostname.length() > 15) {
			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question"), true,
					new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") });
			fQuestion.setMessage(Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"));
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1) {
				return false;
			}
		}

		boolean onlyNumbers = true;
		int i = 0;
		while (onlyNumbers && i < hostname.length()) {
			if (!Character.isDigit(hostname.charAt(i))) {
				onlyNumbers = false;
			}
			i++;
		}

		if (onlyNumbers) {
			FTextArea fQuestion = new FTextArea(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Question"), true,
					new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") }, 350,
					100);
			fQuestion.setMessage(Configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Message"));
			fQuestion.setLocationRelativeTo(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1) {
				return false;
			}
		}

		return true;
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

			csvImportDataDialog.setVisible(true);
			if (csvImportDataDialog.getResult() == 2) {
				CSVImportDataModifier modifier = csvImportDataDialog.getModifier();
				List<List<Object>> rows = modifier.getRows();
				createClients(rows);
			}
		}
	}

	private static CSVImportDataDialog createCSVImportDataDialog(String csvFile) {
		List<String> columnNames = HostInfo.getKeysForCSV();
		CSVFormatDetector csvFormatDetector = new CSVFormatDetector();
		try {
			csvFormatDetector.detectFormat(csvFile);
			if (csvFormatDetector.hasHeader() && !csvFormatDetector.hasExpectedHeaderNames(columnNames)) {
				displayInfoDialog(Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.title"),
						Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.message") + " "
								+ columnNames.toString().replace("[", "").replace("]", ""));
				return null;
			}
		} catch (IOException e) {
			Logging.error("Unable to detect format of CSV file", e);
		}

		CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter(csvFormatDetector.getDelimiter())
				.setQuote(csvFormatDetector.getQuote()).setCommentMarker('#').setHeader().build();
		CSVImportDataModifier modifier = new CSVImportDataModifier(csvFile, columnNames);
		CSVImportDataDialog csvImportDataDialog = new CSVImportDataDialog(format, modifier);
		JPanel centerPanel = csvImportDataDialog.initPanel();

		if (centerPanel == null) {
			return null;
		}

		csvImportDataDialog.setCenterPaneInScrollpane(centerPanel);
		csvImportDataDialog.setupLayout();
		csvImportDataDialog.setDetectedOptions();

		return csvImportDataDialog;
	}

	private static void displayInfoDialog(String title, String message) {
		FTextArea fInfo = new FTextArea(ConfigedMain.getMainFrame(), title, false,
				new String[] { Configed.getResourceValue("buttonClose") }, 400, 200);
		fInfo.setMessage(message);
		fInfo.setAlwaysOnTop(true);
		fInfo.setVisible(true);
	}

	private static void displayCSVTemplateDialog() {
		List<String> columnNames = HostInfo.getKeysForCSV();
		CSVTemplateCreatorDialog dialog = new CSVTemplateCreatorDialog(columnNames);
		JPanel centerPanel = dialog.initPanel();
		dialog.setCenterPaneInScrollpane(centerPanel);
		dialog.setupLayout();
		dialog.setVisible(true);
	}

	@Override
	public void doAction1() {
		result = 1;
		setVisible(false);
	}

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
		String[] groups;
		if (!jTextGroupsSelection.getText().isEmpty()) {
			groups = jTextGroupsSelection.getText().replace("; ", ";").split(";");
		} else {
			groups = new String[] {};
		}
		String netbootProduct = (String) jComboNetboot.getSelectedItem();

		boolean uefiboot = persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.UEFI)
				&& jCheckUefi.getSelectedObjects() != null;
		boolean wanConfig = persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.WAN)
				&& jCheckWan.getSelectedObjects() != null;
		boolean shutdownInstall = jCheckShutdownInstall.getSelectedObjects() != null;

		createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress, systemUUID,
				macaddress, shutdownInstall, uefiboot, wanConfig, groups, netbootProduct);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_TAB) {
			jTextInventoryNumber.requestFocusInWindow();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
