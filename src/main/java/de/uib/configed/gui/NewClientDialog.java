/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.ServerActionManager;
import de.uib.configed.gui.csv.CSVImportDataDialog;
import de.uib.configed.gui.csv.CSVImportDataModifier;
import de.uib.configed.gui.csv.CSVTemplateCreatorDialog;
import de.uib.opsidatamodel.serverdata.OpsiModule;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.CheckedDocument;
import de.uib.utils.swing.SeparatedDocument;

public final class NewClientDialog extends FGeneralDialog {
	private static final int WIDTH_LEFT_LABEL = Globals.BUTTON_WIDTH + 20;
	private static final int WIDTH_INPUT_BOX = Globals.BUTTON_WIDTH * 2;

	private JComboBox<String> jComboDomain;
	private JComboBox<String> jComboDepots;
	private JTextField jTextGroupSelection;
	private JComboBox<String> jComboNetboot;
	private JTextField jTextHostname;
	private JTextField jTextDescription;
	private JTextField jTextInventoryNumber;
	private JTextField systemUUIDField;
	private JTextField macAddressField;
	private JTextField ipAddressField;
	private JTextArea jTextNotes;
	private JCheckBox jCheckWan;
	private JCheckBox jCheckShutdownInstall;

	private List<String> domains;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public NewClientDialog() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("NewClientDialog.title"), false, new String[] {
				Configed.getResourceValue("buttonClose"), Configed.getResourceValue("NewClientDialog.buttonCreate") },
				730, 670);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

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
		jComboDomain.setModel(new DefaultComboBoxModel<>(domains.toArray(new String[0])));
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

	public void setDefaultValues() {
		jCheckWan.setSelected(persistenceController.getConfigDataService()
				.isWanConfigured(persistenceController.getHostInfoCollections().getConfigServer()));
		jCheckShutdownInstall.setSelected(persistenceController.getConfigDataService()
				.isInstallByShutdownConfigured(persistenceController.getHostInfoCollections().getConfigServer()));
	}

	private void init() {
		JLabel jLabelHostname = new JLabel(Configed.getResourceValue("NewClientDialog.hostname"));
		jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));

		JLabel jLabelDomainname = new JLabel(Configed.getResourceValue("NewClientDialog.domain"));

		initComboDomain();

		JLabel jLabelDescription = new JLabel(Configed.getResourceValue("NewClientDialog.description"));

		jTextDescription = new JTextField();

		JLabel jLabelInventoryNumber = new JLabel(Configed.getResourceValue("NewClientDialog.inventorynumber"));

		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel(Configed.getResourceValue("NewClientDialog.belongsToDepot"));

		jComboDepots = new JComboBox<>(
				persistenceController.getHostInfoCollections().getDepotNamesList().toArray(new String[0]));

		JLabel labelGroupSelection = new JLabel(Configed.getResourceValue("NewClientDialog.primaryGroup"));
		jTextGroupSelection = new JTextField();
		jTextGroupSelection.setEnabled(false);
		jTextGroupSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				displayGroupSelectionDialog();
			}
		});

		JLabel jLabelNetboot = new JLabel(Configed.getResourceValue("NewClientDialog.netbootProduct"));

		jComboNetboot = new JComboBox<>();
		jComboNetboot.setMaximumRowCount(10);
		Set<String> netbootProductNames = persistenceController.getProductDataService().getAllNetbootProductNames();
		setJComboBoxModel(jComboNetboot, netbootProductNames);

		JLabel jLabelNotes = new JLabel(Configed.getResourceValue("NewClientDialog.notes"));

		jTextNotes = new JTextArea();

		// This will cause the focus to go to the next (or last) component when pressing the tab (with shift)
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

		jTextNotes.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		JLabel labelInfoMac = new JLabel(Configed.getResourceValue("NewClientDialog.infoMac"));

		JLabel labelInfoIP = new JLabel(Configed.getResourceValue("NewClientDialog.infoIpAddress"));

		JLabel jLabelSystemUUID = new JLabel(Configed.getResourceValue("NewClientDialog.SystemUUID"));

		systemUUIDField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36,
				Character.MIN_VALUE, 36, true), "", 36);

		JLabel jLabelMacAddress = new JLabel(Configed.getResourceValue("NewClientDialog.HardwareAddress"));

		macAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':',
				2, true), "", 17);

		JLabel jLabelIpAddress = new JLabel(Configed.getResourceValue("NewClientDialog.IpAddress"));

		ipAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' },
				28, Character.MIN_VALUE, 4, false), "", 24);

		jCheckShutdownInstall = new JCheckBox(Configed.getResourceValue("NewClientDialog.installByShutdown"));

		jCheckWan = new JCheckBox(Configed.getResourceValue("NewClientDialog.wanConfig"));
		if (!persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)) {
			jCheckWan.setText(Configed.getResourceValue("NewClientDialog.wan_not_activated"));
			jCheckWan.setEnabled(false);
		}

		JPanel panel = new JPanel();
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		gpl.setHorizontalGroup(gpl.createParallelGroup()
				/////// HOSTNAME
				.addGroup(gpl.createSequentialGroup().addGroup(gpl.createParallelGroup()
						.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jLabelHostname,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(jTextHostname,
								Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
						.addGap(Globals.MIN_GAP_SIZE)
						.addGroup(gpl.createParallelGroup()
								.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
										jLabelDomainname, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
								.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
										jComboDomain, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)))
						.addGap(Globals.MIN_GAP_SIZE))
				/////// DESCRIPTION + INVENTORY
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelDescription, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelInventoryNumber, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// NOTES
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(jTextNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// SYSTEM UUID
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(systemUUIDField, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// MAC-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoMac, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(macAddressField, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// IP-ADDRESS
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelIpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(labelInfoIP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(gpl.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(ipAddressField, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
						.addGap(Globals.MIN_GAP_SIZE))
				/////// InstallByShutdown and WAN
				.addGroup(
						gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
								.addComponent(jCheckShutdownInstall, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(0, 0, Short.MAX_VALUE)
								.addComponent(jCheckWan, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(0, 0, Short.MAX_VALUE))
				// depot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelDepot, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jComboDepots, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
						.addGap(Globals.MIN_GAP_SIZE))
				// group
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelGroupSelection, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextGroupSelection, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
						.addGap(Globals.MIN_GAP_SIZE))
				// netboot
				.addGroup(gpl.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelNetboot, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jComboNetboot, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX, WIDTH_INPUT_BOX)
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
				.addGap(Globals.MIN_GAP_SIZE).addComponent(jLabelNotes).addComponent(jTextNotes)
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
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelGroupSelection, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jTextGroupSelection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
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
		groupsSelectionDialog.setMultiSelection();
		groupsSelectionDialog.setListData(
				PersistenceControllerFactory.getPersistenceController().getGroupDataService().getHostGroupIds());
		groupsSelectionDialog
				.setPreviousSelectionValues(Arrays.asList(jTextGroupSelection.getText().replace("; ", ";").split(";")));
		groupsSelectionDialog.setVisible(true);

		if (groupsSelectionDialog.getResult() == 2) {
			String selectedGroups = Utils.getListStringRepresentation(groupsSelectionDialog.getSelectedValues());
			jTextGroupSelection.setText(selectedGroups);
			jTextGroupSelection.setToolTipText(
					"<html><body><p>" + selectedGroups.replace(";\n", "<br\\ >") + "</p></body></html>");
		}
	}

	private void initComboDomain() {
		domains = persistenceController.getConfigDataService().getDomains();

		jComboDomain = new JComboBox<>();
		jComboDomain.setEditable(true);
		setDomains();
	}

	private void createNorthPanel() {
		JLabel jCSVTemplateLabel = new JLabel(Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		JButton jCSVTemplateButton = new JButton(Configed.getResourceValue("NewClientDialog.csvTemplateButton"));
		jCSVTemplateButton.addActionListener(actionEvent -> CSVTemplateCreatorDialog.displayCSVTemplateDialog());

		JLabel jImportLabel = new JLabel(Configed.getResourceValue("NewClientDialog.importLabel"));
		JButton jImportButton = new JButton(Configed.getResourceValue("NewClientDialog.importButton"));
		jImportButton.addActionListener(actionEvent -> importCSV());

		final GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"))));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jCSVTemplateLabel)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jCSVTemplateButton, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jImportLabel)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jImportButton, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
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

			if (!isBoolean((String) client.get(10)) || !isBoolean((String) client.get(11))) {
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

		ServerActionManager.createClients(modifiedClients);
	}

	private static boolean isBoolean(String bool) {
		return bool.isEmpty() || bool.equalsIgnoreCase(Boolean.TRUE.toString())
				|| bool.equalsIgnoreCase(Boolean.FALSE.toString());
	}

	private void createClient(final String hostname, final String selectedDomain, final String depotID,
			final String description, final String inventorynumber, final String notes, final String ipaddress,
			final String systemUUID, final String macaddress, final boolean shutdownInstall, final boolean wanConfig,
			final String[] groups, final String netbootProduct) {
		if (checkClientCorrectness(hostname, selectedDomain)) {
			Logging.debug(this, "createClient ", hostname, ", ", selectedDomain, ", ", depotID, ", ", description, ", ",
					inventorynumber, ", ", notes, shutdownInstall, ", ", wanConfig, ", ", Arrays.toString(groups), ", ",
					netbootProduct);

			String newClientID = hostname + "." + selectedDomain;

			persistenceController.getHostInfoCollections().addOpsiHostName(newClientID);
			if (persistenceController.getHostDataService().createClient(hostname, selectedDomain, depotID, description,
					inventorynumber, notes, ipaddress, systemUUID, macaddress, shutdownInstall, wanConfig, groups,
					netbootProduct)) {
				ServerActionManager.createClient(newClientID, groups);
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
		Logging.info(this, "createClient domains", domains);

		domains.remove(selectedDomain);

		for (String domain : domains) {
			order++;
			saveDomains.add("" + order + ":" + domain);

			editableDomains.add(domain);
		}

		Logging.debug(this, "createClient editableDomains ", editableDomains);

		domains = editableDomains;

		setDomains();

		Logging.debug(this, "createClient saveDomains ", saveDomains);
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
		List<String> existingHostNames = persistenceController.getHostInfoCollections().getOpsiHostNames();

		if (existingHostNames != null && existingHostNames.contains(opsiHostKey)) {
			if (persistenceController.getHostInfoCollections().getDepotNamesList().contains(opsiHostKey)) {
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
			CSVImportDataDialog csvImportDataDialog = CSVImportDataDialog.createCSVImportDataDialog(this, csvFile);

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
		if (!jTextGroupSelection.getText().isEmpty()) {
			groups = jTextGroupSelection.getText().replace("; ", ";").split(";");
		} else {
			groups = new String[] {};
		}
		String netbootProduct = (String) jComboNetboot.getSelectedItem();

		boolean wanConfig = persistenceController.getModuleDataService().isOpsiModuleActive(OpsiModule.VPN)
				&& jCheckWan.getSelectedObjects() != null;
		boolean shutdownInstall = jCheckShutdownInstall.getSelectedObjects() != null;

		createClient(hostname, selectedDomain, depotID, description, inventorynumber, notes, ipaddress, systemUUID,
				macaddress, shutdownInstall, wanConfig, groups, netbootProduct);
	}
}
