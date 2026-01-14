/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.clients.add;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import de.uib.configed.gui.AbstractTeaComponent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.ListSelectionDialog;
import de.uib.configed.gui.ServerActionManager;
import de.uib.configed.gui.features.csv.CSVImportDataDialog;
import de.uib.configed.gui.features.csv.CSVImportDataModifier;
import de.uib.configed.gui.features.csv.CSVTemplateCreatorDialog;
import de.uib.configed.gui.share.swing.CheckedDocument;
import de.uib.configed.gui.share.swing.SeparatedDocument;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class AddClientComponent extends AbstractTeaComponent<AddClientModel, AddClientMsg, AddClientEffect> {
	private static final OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private JComboBox<String> jComboDomain;
	private JComboBox<String> jComboDepots;
	private JTextField jTextGroupSelection;
	private JComboBox<String> jComboNetboot;
	private JCheckBox jCheckWan;
	private JCheckBox jCheckShutdownInstall;

	private JDialog dialog;

	public AddClientComponent() {
		super();
	}

	public AddClientComponent(AddClientModel model) {
		super(model);
	}

	@Override
	protected AddClientModel initModel() {
		return AddClientModel.builder().build();
	}

	@Override
	protected UpdateResult<AddClientModel, AddClientEffect> updateModel(AddClientMsg msg, AddClientModel model) {
		return AddClientUpdate.update(msg, model);
	}

	@Override
	protected JComponent renderView(AddClientModel model, Consumer<AddClientMsg> dispatch) {
		Logging.info(this, "initializing new client component");

		JButton buttonCreate = new JButton(Configed.getResourceValue("NewClientDialog.buttonCreate"));
		buttonCreate.addActionListener(actionEvent -> dispatch(new AddClientMsg.ActionMsg.CreateClient()));

		JButton buttonClose = new JButton(Configed.getResourceValue("buttonClose"));
		buttonClose.addActionListener(actionEvent -> dispatch(new AddClientMsg.ActionMsg.CloseDialog()));

		JScrollPane scrollPane = new JScrollPane(createPanel(dispatch));
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				null, new Object[] { buttonCreate, buttonClose });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("NewClientDialog.title"));
		dialog.setMinimumSize(new Dimension());
		dialog.setModal(false);
		dialog.pack();

		return optionPane;
	}

	@Override
	protected void refreshView() {
		if (jCheckWan != null) {
			jCheckWan.setEnabled(model.isWanEnabled());
			jCheckWan.setSelected(model.isWanSelected());
			jCheckWan.setText(model.isWanEnabled() ? Configed.getResourceValue("NewClientDialog.wanConfig")
					: Configed.getResourceValue("NewClientDialog.wan_not_activated"));
		}
		if (jCheckShutdownInstall != null) {
			jCheckShutdownInstall.setSelected(model.isShutdownInstallSelected());
		}

		if (jComboDomain != null && !model.getDomains().isEmpty()) {
			jComboDomain.setModel(new DefaultComboBoxModel<>(model.getDomains().toArray(new String[0])));
			jComboDomain.setSelectedItem(model.getSelectedDomain());
		}
		if (jComboDepots != null && !model.getDepots().isEmpty()) {
			jComboDepots.setModel(new DefaultComboBoxModel<>(model.getDepots().toArray(new String[0])));
			jComboDepots.setSelectedItem(model.getSelectedDepot());
		}
		if (jComboNetboot != null) {
			jComboNetboot.setModel(new DefaultComboBoxModel<>(model.getNetbootProducts().toArray(new String[0])));
			jComboNetboot.setSelectedItem(model.getSelectedNetbootProduct());
		}
	}

	@Override
	protected void handleEffect(AddClientEffect effect) {
		switch (effect) {
		case AddClientEffect.UIEffect e -> handleUIEffect(e);
		case AddClientEffect.ServiceEffect s -> handleServiceEffect(s);
		}
	}

	private void handleUIEffect(AddClientEffect.UIEffect effect) {
		switch (effect) {
		case AddClientEffect.UIEffect.ShowOverwriteHostDialog(String opsiHostKey) -> handleShowOverwriteHostDialogEffect(
				opsiHostKey);
		case AddClientEffect.UIEffect.ShowOverwriteDepotDialog(String opsiHostKey) -> handleShowOverwriteDepotDialogEffect(
				opsiHostKey);
		case AddClientEffect.UIEffect.ShowNetbiosConfirmDialog e -> handleShowNetbiosConfirmDialogEffect();
		case AddClientEffect.UIEffect.OpenCSVImportDialog e -> importCSV();
		case AddClientEffect.UIEffect.OpenGroupSelectionDialog e -> displayGroupSelectionDialog();
		case AddClientEffect.UIEffect.ShowErrorMessage e -> JOptionPane.showMessageDialog(dialog, e.message(),
				e.title(), JOptionPane.ERROR_MESSAGE);
		case AddClientEffect.UIEffect.CloseDialog e -> handleCloseDialogEffect();
		}
	}

	private void handleShowOverwriteHostDialogEffect(String opsiHostKey) {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0"));
		message.append(" \"");
		message.append(opsiHostKey);
		message.append("\" \n");
		message.append(Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1"));
		int answer = JOptionPane.showConfirmDialog(dialog, message.toString(),
				Configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question"), JOptionPane.YES_NO_OPTION);
		dispatch(new AddClientMsg.ActionMsg.CSVImported(model.getRowsToImport(), answer == JOptionPane.YES_OPTION));
	}

	private void handleShowOverwriteDepotDialogEffect(String opsiHostKey) {
		JOptionPane.showMessageDialog(dialog,
				String.format(Configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"), opsiHostKey),
				Configed.getResourceValue("NewClientDialog.OverwriteDepot.Title"), JOptionPane.ERROR_MESSAGE);
	}

	private void handleShowNetbiosConfirmDialogEffect() {
		int answer = JOptionPane.showConfirmDialog(dialog,
				Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message"),
				Configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question"),
				JOptionPane.YES_NO_OPTION);
		dispatch(new AddClientMsg.ActionMsg.CSVImported(model.getRowsToImport(), answer == JOptionPane.YES_OPTION));
	}

	private void handleCloseDialogEffect() {
		if (dialog != null) {
			dialog.setVisible(false);
			dialog.dispose();
		}
	}

	private void handleServiceEffect(AddClientEffect.ServiceEffect effect) {
		switch (effect) {
		case AddClientEffect.ServiceEffect.LoadInitialData e -> loadInitialData();
		case AddClientEffect.ServiceEffect.CreateClients(List<List<Object>> rows) -> ServerActionManager
				.createClients(rows);
		}
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
		dialog.pack();
	}

	private JPanel createPanel(Consumer<AddClientMsg> dispatch) {
		JPanel panel = new JPanel();

		JLabel jLabelHostname = new JLabel(Configed.getResourceValue("NewClientDialog.hostname"));
		jLabelHostname.setFont(jLabelHostname.getFont().deriveFont(Font.BOLD));

		JTextField jTextHostname = new JTextField(new CheckedDocument(
				new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
						'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' },
				-1), "", 17);
		jTextHostname.setToolTipText(Configed.getResourceValue("NewClientDialog.hostnameRules"));
		jTextHostname.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeHostname(jTextHostname.getText()))));

		JLabel jLabelDomainname = new JLabel(Configed.getResourceValue("NewClientDialog.domain"));
		jLabelDomainname.setFont(jLabelDomainname.getFont().deriveFont(Font.BOLD));

		jComboDomain = new JComboBox<>();
		jComboDomain.setEditable(true);
		jComboDomain.addActionListener(a -> dispatch
				.accept(new AddClientMsg.FieldChangeMsg.ChangeDomain((String) jComboDomain.getSelectedItem())));

		JLabel jLabelDescription = new JLabel(Configed.getResourceValue("description"));
		jLabelDescription.setFont(jLabelDescription.getFont().deriveFont(Font.BOLD));

		JTextField jTextDescription = new JTextField();
		jTextDescription.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeDescription(jTextDescription.getText()))));

		JLabel jLabelInventoryNumber = new JLabel(Configed.getResourceValue("NewClientDialog.inventorynumber"));
		jLabelInventoryNumber.setFont(jLabelInventoryNumber.getFont().deriveFont(Font.BOLD));

		JTextField jTextInventoryNumber = new JTextField();
		jTextInventoryNumber.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeInventory(jTextInventoryNumber.getText()))));

		JLabel jLabelDepot = new JLabel(Configed.getResourceValue("NewClientDialog.belongsToDepot"));
		jLabelDepot.setFont(jLabelDepot.getFont().deriveFont(Font.BOLD));

		jComboDepots = new JComboBox<>();
		jComboDepots.addActionListener(a -> dispatch
				.accept(new AddClientMsg.FieldChangeMsg.ChangeDepot((String) jComboDepots.getSelectedItem())));

		JLabel labelGroupSelection = new JLabel(Configed.getResourceValue("NewClientDialog.primaryGroup"));
		labelGroupSelection.setFont(labelGroupSelection.getFont().deriveFont(Font.BOLD));

		jTextGroupSelection = new JTextField();
		jTextGroupSelection.setEnabled(false);
		jTextGroupSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				dispatch.accept(new AddClientMsg.UIMsg.OpenGroupSelectionDialog());
			}
		});

		JLabel jLabelNotes = new JLabel(Configed.getResourceValue("NewClientDialog.notes"));
		jLabelNotes.setFont(jLabelNotes.getFont().deriveFont(Font.BOLD));

		JTextArea jTextNotes = new JTextArea();
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
		jTextNotes.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		jTextNotes.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeNotes(jTextNotes.getText().trim()))));

		JScrollPane jTextNodesScrollPane = new JScrollPane(jTextNotes);
		jTextNodesScrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

		JLabel jLabelSystemUUID = new JLabel(Configed.getResourceValue("NewClientDialog.SystemUUID"));
		jLabelSystemUUID.setFont(jLabelSystemUUID.getFont().deriveFont(Font.BOLD));

		JTextField systemUUIDField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-' }, 36,
				Character.MIN_VALUE, 36, true), "", 36);
		systemUUIDField.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeSystemUUID(systemUUIDField.getText()))));

		JLabel jLabelMacAddress = new JLabel(Configed.getResourceValue("NewClientDialog.HardwareAddress"));
		jLabelMacAddress.setIcon(Icons.getIntellijIcon("info"));
		jLabelMacAddress.setToolTipText(Configed.getResourceValue("NewClientDialog.infoMac"));
		jLabelMacAddress.setFont(jLabelMacAddress.getFont().deriveFont(Font.BOLD));

		JTextField macAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' }, 12, ':',
				2, true), "", 17);
		macAddressField.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeMAC(macAddressField.getText()))));

		JLabel jLabelIpAddress = new JLabel(Configed.getResourceValue("ipAddress"));
		jLabelIpAddress.setIcon(Icons.getIntellijIcon("info"));
		jLabelIpAddress.setToolTipText(Configed.getResourceValue("NewClientDialog.infoIpAddress"));
		jLabelIpAddress.setFont(jLabelIpAddress.getFont().deriveFont(Font.BOLD));

		JTextField ipAddressField = new JTextField(new SeparatedDocument(
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', 'a', 'b', 'c', 'd', 'e', 'f', ':' },
				28, Character.MIN_VALUE, 4, false), "", 24);
		ipAddressField.getDocument().addDocumentListener(Utils.onDocumentChange(
				e -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ChangeIP(ipAddressField.getText()))));

		jCheckShutdownInstall = new JCheckBox(Configed.getResourceValue("NewClientDialog.installByShutdown"));
		jCheckShutdownInstall.addActionListener(a -> dispatch
				.accept(new AddClientMsg.FieldChangeMsg.ToggleShutdownInstall(jCheckShutdownInstall.isSelected())));

		jCheckWan = new JCheckBox(Configed.getResourceValue("NewClientDialog.wanConfig"));
		jCheckWan.addActionListener(
				a -> dispatch.accept(new AddClientMsg.FieldChangeMsg.ToggleWanSelected(jCheckWan.isSelected())));

		jComboDepots = new JComboBox<>();
		jComboDepots.addActionListener(a -> dispatch
				.accept(new AddClientMsg.FieldChangeMsg.ChangeDepot((String) jComboDepots.getSelectedItem())));

		JLabel jLabelNetboot = new JLabel(Configed.getResourceValue("NewClientDialog.netbootProduct"));
		jLabelNetboot.setFont(jLabelNetboot.getFont().deriveFont(Font.BOLD));
		jComboNetboot = new JComboBox<>();
		jComboNetboot.addActionListener(a -> dispatch
				.accept(new AddClientMsg.FieldChangeMsg.ChangeNetboot((String) jComboNetboot.getSelectedItem())));

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		JPanel northPanel = createNorthPanel(dispatch);

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

	private JPanel createNorthPanel(Consumer<AddClientMsg> dispatch) {
		JLabel jCSVTemplateLabel = new JLabel(Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
		JButton jCSVTemplateButton = new JButton(Icons.getIntellijIcon("add"));
		jCSVTemplateButton.addActionListener(actionEvent -> CSVTemplateCreatorDialog.displayCSVTemplateDialog(dialog));

		JLabel jImportLabel = new JLabel(Configed.getResourceValue("NewClientDialog.importLabel"));
		JButton jImportButton = new JButton(Icons.getIntellijIcon("open"));
		jImportButton
				.addActionListener(actionEvent -> dispatch.accept(new AddClientMsg.ActionMsg.CSVImportRequested()));

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

	private void displayGroupSelectionDialog() {
		ListSelectionDialog groupsSelectionDialog = new ListSelectionDialog(dialog,
				Configed.getResourceValue("NewClientDialog.groupSelectionDialog.title"));
		groupsSelectionDialog.setListData(
				PersistenceControllerFactory.getPersistenceController().getDataServices().group.getHostGroupIds());
		groupsSelectionDialog
				.setPreviousSelectionValues(List.of(jTextGroupSelection.getText().replace("; ", ";").split(";")));
		groupsSelectionDialog.show();

		if (groupsSelectionDialog.wasAccepted()) {
			String selectedGroups = Utils.getListStringRepresentation(groupsSelectionDialog.getSelectedValues());
			jTextGroupSelection.setText(selectedGroups);
			jTextGroupSelection.setToolTipText(
					"<html><body><p>" + selectedGroups.replace(";\n", "<br\\ >") + "</p></body></html>");
			dispatch(new AddClientMsg.FieldChangeMsg.ChangeGroups(selectedGroups));
		}
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
				dispatch(new AddClientMsg.ActionMsg.CSVImported(rows, false));
			}
		}
	}

	private void loadInitialData() {
		List<String> domains = persistenceController.getDataServices().config.getDomains();
		List<String> depots = persistenceController.getDataServices().hostInfoCollections.getDepotNamesList();
		Set<String> netbootProductNames = persistenceController.getDataServices().product.getAllNetbootProductNames();
		List<String> netboots = new ArrayList<>();
		netboots.add(null);
		netboots.addAll(netbootProductNames);
		List<String> hostnames = persistenceController.getDataServices().hostInfoCollections.getOpsiHostNames();

		boolean isWanActive = persistenceController.getDataServices().module.isOpsiModuleActive(OpsiModule.VPN);
		boolean defaultWanSelected = persistenceController.getDataServices().config.isWanConfiguredOnConfigserver();
		boolean defaultShutdown = persistenceController.getDataServices().config
				.isInstallByShutdownConfiguredOnConfigserver();

		dispatch(new AddClientMsg.ActionMsg.InitialDataLoaded(domains, depots, netboots, hostnames, isWanActive,
				defaultWanSelected, defaultShutdown));
	}
}
