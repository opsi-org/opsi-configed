/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import de.uib.configed.core.domain.serverdata.OpsiModule;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.csv.CSVImportDataDialog;
import de.uib.configed.gui.features.csv.CSVImportDataModifier;
import de.uib.configed.gui.features.csv.CSVTemplateCreatorDialog;
import de.uib.configed.gui.share.swing.CheckedDocument;
import de.uib.configed.gui.share.swing.SeparatedDocument;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class NewClientDialog {
	private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

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

	private JOptionPane optionPane;
	private JDialog dialog;

	private List<String> domains;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public NewClientDialog() {
		JButton buttonCreate = new JButton(Configed.getResourceValue("NewClientDialog.buttonCreate"));
		buttonCreate.addActionListener(actionEvent -> create());

		JScrollPane scrollPane = new JScrollPane(createPanel());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { buttonCreate, Configed.getResourceValue("buttonClose") });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("NewClientDialog.title"));
		dialog.setMinimumSize(new Dimension());
		dialog.setModal(false);
		dialog.pack();
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
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

	private JPanel createPanel() {
		JLabel jLabelHostname = new JLabel(Configed.getResourceValue("NewClientDialog.hostname"));
		jLabelHostname.setFont(jLabelHostname.getFont().deriveFont(Font.BOLD));

		jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));

		JLabel jLabelDomainname = new JLabel(Configed.getResourceValue("NewClientDialog.domain"));
		jLabelDomainname.setFont(jLabelDomainname.getFont().deriveFont(Font.BOLD));

		initComboDomain();

		JLabel jLabelDescription = new JLabel(Configed.getResourceValue("description"));
		jLabelDescription.setFont(jLabelDescription.getFont().deriveFont(Font.BOLD));

		jTextDescription = new JTextField();

		JLabel jLabelInventoryNumber = new JLabel(Configed.getResourceValue("NewClientDialog.inventorynumber"));
		jLabelInventoryNumber.setFont(jLabelInventoryNumber.getFont().deriveFont(Font.BOLD));

		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel(Configed.getResourceValue("NewClientDialog.belongsToDepot"));
		jLabelDepot.setFont(jLabelDepot.getFont().deriveFont(Font.BOLD));

		jComboDepots = new JComboBox<>(
				persistenceController.getHostInfoCollections().getDepotNamesList().toArray(new String[0]));

		JLabel labelGroupSelection = new JLabel(Configed.getResourceValue("NewClientDialog.primaryGroup"));
		labelGroupSelection.setFont(labelGroupSelection.getFont().deriveFont(Font.BOLD));

		jTextGroupSelection = new JTextField();
		jTextGroupSelection.setEnabled(false);
		jTextGroupSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				displayGroupSelectionDialog();
			}
		});

		JLabel jLabelNetboot = new JLabel(Configed.getResourceValue("NewClientDialog.netbootProduct"));
		jLabelNetboot.setFont(jLabelNetboot.getFont().deriveFont(Font.BOLD));

		jComboNetboot = new JComboBox<>();
		jComboNetboot.setMaximumRowCount(10);
		Set<String> netbootProductNames = persistenceController.getProductDataService().getAllNetbootProductNames();
		setJComboBoxModel(jComboNetboot, netbootProductNames);

		JLabel jLabelNotes = new JLabel(Configed.getResourceValue("NewClientDialog.notes"));
		jLabelNotes.setFont(jLabelNotes.getFont().deriveFont(Font.BOLD));

		jTextNotes = new JTextArea();

		// This will cause the focus to go to the next (or last) component when pressing the tab (with shift)
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

		JScrollPane jTextNodesScrollPane = new JScrollPane(jTextNotes);
		jTextNodesScrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		JLabel jLabelSystemUUID = new JLabel(Configed.getResourceValue("NewClientDialog.SystemUUID"));
		jLabelSystemUUID.setFont(jLabelSystemUUID.getFont().deriveFont(Font.BOLD));

		systemUUIDField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36,
				Character.MIN_VALUE, 36, true), "", 36);

		JLabel jLabelMacAddress = new JLabel(Configed.getResourceValue("NewClientDialog.HardwareAddress"));
		jLabelMacAddress.setIcon(Icons.getIntellijIcon("info"));
		jLabelMacAddress.setToolTipText(Configed.getResourceValue("NewClientDialog.infoMac"));
		jLabelMacAddress.setFont(jLabelMacAddress.getFont().deriveFont(Font.BOLD));

		macAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':',
				2, true), "", 17);

		JLabel jLabelIpAddress = new JLabel(Configed.getResourceValue("ipAddress"));
		jLabelIpAddress.setIcon(Icons.getIntellijIcon("info"));
		jLabelIpAddress.setToolTipText(Configed.getResourceValue("NewClientDialog.infoIpAddress"));
		jLabelIpAddress.setFont(jLabelIpAddress.getFont().deriveFont(Font.BOLD));

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
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		JPanel northPanel = createNorthPanel();

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(northPanel)
				/////// HOSTNAME
				.addComponent(jLabelHostname, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextHostname, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(jLabelDomainname, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboDomain, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// DESCRIPTION + INVENTORY
				.addComponent(jLabelDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(jLabelInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)

				/////// NOTES
				.addComponent(jLabelNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextNodesScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)

				/////// SYSTEM UUID
				.addComponent(jLabelSystemUUID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(systemUUIDField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// MAC-ADDRESS
				.addComponent(jLabelMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(macAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// IP-ADDRESS
				.addComponent(jLabelIpAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(ipAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				/////// InstallByShutdown and WAN
				.addComponent(jCheckShutdownInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jCheckWan, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				// depot
				.addComponent(jLabelDepot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				// group
				.addComponent(labelGroupSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextGroupSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)

				// netboot
				.addComponent(jLabelNetboot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboNetboot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(northPanel)
				/////// HOSTNAME
				.addGap(Globals.GAP_SIZE).addComponent(jLabelHostname)
				.addComponent(jTextHostname, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(jLabelDomainname)
				.addComponent(jComboDomain, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// DESCRIPTION
				.addGap(Globals.GAP_SIZE).addComponent(jLabelDescription)
				.addComponent(jTextDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// INVENTORY NUMBER
				.addGap(Globals.GAP_SIZE).addComponent(jLabelInventoryNumber)
				.addComponent(jTextInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// NOTES
				.addGap(Globals.GAP_SIZE).addComponent(jLabelNotes)
				.addComponent(jTextNodesScrollPane, 100, 100, Short.MAX_VALUE)

				/////// SYSTEM UUID
				.addGap(Globals.GAP_SIZE).addComponent(jLabelSystemUUID)
				.addComponent(systemUUIDField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// MAC-ADDRESS
				.addGap(Globals.GAP_SIZE).addComponent(jLabelMacAddress)
				.addComponent(macAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// IP-ADDRESS
				.addGap(Globals.GAP_SIZE).addComponent(jLabelIpAddress)
				.addComponent(ipAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// SHUTDOWN and WAN
				.addGap(Globals.GAP_SIZE)
				.addComponent(jCheckShutdownInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jCheckWan, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// depot
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelDepot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// group
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelGroupSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextGroupSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				/////// netboot
				.addGap(Globals.GAP_SIZE)
				.addComponent(jLabelNetboot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboNetboot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		return panel;
	}

	private void displayGroupSelectionDialog() {
		ListSelectionDialog groupsSelectionDialog = new ListSelectionDialog(dialog,
				Configed.getResourceValue("NewClientDialog.groupSelectionDialog.title"));
		groupsSelectionDialog.setListData(
				PersistenceControllerFactory.getPersistenceController().getGroupDataService().getHostGroupIds());
		groupsSelectionDialog
				.setPreviousSelectionValues(Arrays.asList(jTextGroupSelection.getText().replace("; ", ";").split(";")));
		groupsSelectionDialog.show();

		if (groupsSelectionDialog.wasAccepted()) {
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

	private JPanel createNorthPanel() {
		JLabel jCSVTemplateLabel = new JLabel(Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		JButton jCSVTemplateButton = new JButton(Icons.getIntellijIcon("add"));
		jCSVTemplateButton.addActionListener(actionEvent -> CSVTemplateCreatorDialog.displayCSVTemplateDialog(dialog));

		JLabel jImportLabel = new JLabel(Configed.getResourceValue("NewClientDialog.importLabel"));
		JButton jImportButton = new JButton(Icons.getIntellijIcon("open"));
		jImportButton.addActionListener(actionEvent -> importCSV());

		JPanel northPanel = new JPanel();
		final GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);
		northPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		northLayout.setHorizontalGroup(northLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jCSVTemplateLabel)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jCSVTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(
						northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jImportLabel)
								.addGap(Globals.GAP_SIZE).addComponent(jImportButton, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)));

		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jCSVTemplateLabel).addComponent(jCSVTemplateButton, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jImportLabel)
						.addComponent(jImportButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE));

		return northPanel;
	}

	private void createClients(List<List<Object>> clients) {
		Iterator<List<Object>> iter = clients.iterator();
		List<List<Object>> modifiedClients = new ArrayList<>();

		while (iter.hasNext()) {
			List<Object> client = iter.next();

			if (!isBoolean((String) client.get(10)) || !isBoolean((String) client.get(11))) {
				JOptionPane.showMessageDialog(dialog,
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.message"),
						Configed.getResourceValue("NewClientDialog.nonBooleanValue.title"), JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(dialog,
						opsiHostKey + "\n" + Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
						Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title"), JOptionPane.WARNING_MESSAGE);
				return false;
			}

			StringBuilder message = new StringBuilder();
			message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
			message.append(" \"");
			message.append(opsiHostKey);
			message.append("\" \n");
			message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));

			int answer = JOptionPane.showConfirmDialog(dialog, message.toString(),
					Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question"),
					JOptionPane.YES_NO_OPTION);

			if (answer == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		return true;
	}

	private boolean checkHostname(String hostname) {
		if (hostname.length() > 15 || NUMERIC_PATTERN.matcher(hostname).matches()) {
			int answer = JOptionPane.showConfirmDialog(dialog,
					Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"),
					Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question"),
					JOptionPane.YES_NO_OPTION);

			if (answer == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		return true;
	}

	private void importCSV() {
		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jFileChooser.setFileHidingEnabled(false);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV (.csv)", "csv");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showOpenDialog(dialog);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String csvFile = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!csvFile.endsWith(".csv")) {
				csvFile = csvFile.concat(".csv");
			}
			CSVImportDataDialog csvImportDataDialog = CSVImportDataDialog.createCSVImportDataDialog(dialog, csvFile);

			if (csvImportDataDialog == null) {
				return;
			}

			if (csvImportDataDialog.show()) {
				CSVImportDataModifier modifier = csvImportDataDialog.getModifier();
				List<List<Object>> rows = modifier.getRows();
				createClients(rows);
			}
		}
	}

	private void create() {
		Logging.info(this, "doAction2");

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
