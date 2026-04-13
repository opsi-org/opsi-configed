/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

/*
 * PanelEnterLicense.java
 * after selecting a pool, one can add license options for it
 * Created 17.02.2009-2015
 */

package de.uib.configed.gui.features.licenses;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ControlPanelEditLicenses;
import de.uib.configed.gui.ControlPanelEnterLicense;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.CellDateEditor;
import de.uib.configed.gui.share.table.gui.FilterStateManager.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import de.uib.configed.gui.type.licenses.LicenseEntry;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.DatePicker;
import net.miginfocom.swing.MigLayout;

public class PanelEnterLicense extends MultiTablePanel {
	private static final int MIN_PANEL_TABLE_HEIGHT = 60;

	private static final int MIN_FIELD_WIDTH = 40;

	private PanelGenEdit panelKeys;
	private PanelGenEdit panelLicensePools;
	private PanelGenEdit panelLicenseContracts;

	private String selectedLicensePool = "";

	private JButton jButtonCreateStandard;
	private JButton jButtonCreateVolume;
	private JButton jButtonCreateOEM;
	private JButton jButtonCreateConcurrent;
	private JButton jButtonSend;

	private JTextField jTextFieldLicenseID;
	private JTextField jTextFieldLicenseType;

	private JTextField jTextFieldMaxInstallations;
	private JComboBox<String> comboClient;
	private JTextField jTextFieldEndOfLicense;
	private JTextField jTextFieldLicenseContract;
	private JTextField jTextFieldLKey;

	private ControlPanelEnterLicense enterLicenseController;

	public PanelEnterLicense(ControlPanelEnterLicense enterLicenseController) {
		super(enterLicenseController);

		this.enterLicenseController = enterLicenseController;

		initComponents();
		deactivate();
		setupLayout();
		defineListeners();
	}

	private void defineListeners() {
		panelLicenseContracts.getGenEditTable().getSelectionModel()
				.addListSelectionListener(this::selectPanelLicenseContracts);

		panelLicensePools.addListSelectionListener(this::selectPanelLicensePools);
	}

	private void selectPanelLicenseContracts(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		if (!panelLicenseContracts.getGenEditTable().getSelectionModel().isSelectionEmpty()) {
			int selectedRow = panelLicenseContracts.getGenEditTable().getSelectionModel().getMinSelectionIndex();
			String keyValue = panelLicenseContracts.getGenEditTable().getValueAt(selectedRow, 0).toString();

			if (jTextFieldLicenseContract.isEnabled()) {
				jTextFieldLicenseContract.setText(keyValue);
			}
		}
	}

	private void selectPanelLicensePools(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		int i = panelLicensePools.getGenEditTable().getSelectedRow();

		selectedLicensePool = "";

		if (i > -1) {
			selectedLicensePool = panelLicensePools.getGenEditTable().getValueAt(i, 0).toString();
		}

		panelLicensePools.setTitle(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensepool")
				+ ": " + selectedLicensePool);
	}

	private void deactivate() {
		jTextFieldLicenseID.setEnabled(false);
		jTextFieldLicenseType.setEnabled(false);
		jTextFieldMaxInstallations.setEnabled(false);
		comboClient.setEnabled(false);
		jTextFieldEndOfLicense.setEnabled(false);
		jTextFieldLicenseContract.setEnabled(false);
		jTextFieldLKey.setEnabled(false);
		jButtonSend.setEnabled(false);
	}

	private boolean checkAndStart() {
		if (panelLicensePools.getGenEditTable().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectLicensepool"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		if (panelLicenseContracts.getGenEditTable().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectLicensecontract"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		jTextFieldLicenseID.setEnabled(true);
		jTextFieldLicenseID
				.setText("l_" + LocalDateTime.now().format(ControlPanelEditLicenses.DATE_TIME_FORMATTER_UNDERSCORE));

		jTextFieldEndOfLicense.setEnabled(true);
		jTextFieldEndOfLicense.setText("");
		jTextFieldLicenseContract.setEnabled(true);
		jTextFieldLicenseContract.setText(
				"" + panelLicenseContracts.getValueAt(panelLicenseContracts.getGenEditTable().getSelectedRow(), 0));
		jTextFieldLicenseContract.setEditable(false);

		jTextFieldLKey.setEnabled(true);

		jButtonSend.setEnabled(true);

		return true;
	}

	private void startLicense(String licenseType, String maxInstallations, Collection<String> clients,
			boolean enableLicenseID) {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenseType.setEnabled(true);
		jTextFieldLicenseType.setText(licenseType);
		jTextFieldLicenseType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText(maxInstallations);
		jTextFieldMaxInstallations.setEditable(false);

		if (clients == null) {
			comboClient.setEnabled(false);
			comboClient.removeAllItems();
			comboClient.addItem("");
		} else {
			comboClient.setModel(new DefaultComboBoxModel<>(clients.toArray(new String[0])));
			comboClient.setEnabled(true);
		}

		if (enableLicenseID) {
			jTextFieldLicenseID.setEnabled(true);
			jTextFieldLicenseID.setText(
					"l_" + LocalDateTime.now().format(ControlPanelEditLicenses.DATE_TIME_FORMATTER_UNDERSCORE));
		}
	}

	private void initComponents() {
		panelKeys = new PanelGenEdit(Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicenseOptionsView"),
				true, 0, new int[] { PopupMenuTrait.POPUP_RELOAD }, false);
		panelKeys.setFilterKey(FilterKey.LICENSE_KEYS_ENTER_TABLE);

		panelLicensePools = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensepool"), false, 0,
				new int[] { PopupMenuTrait.POPUP_RELOAD }, true);
		panelLicensePools.setFilterKey(FilterKey.LICENSE_POOL_ENTER_TABLE);

		panelLicenseContracts = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensecontract"), true, 1,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelLicenseContracts.setFilterKey(FilterKey.LICENSE_CONTRACTS_ENTER_TABLE);

		jTextFieldLicenseID = new JTextField();
		jTextFieldLicenseType = new JTextField();
		jTextFieldMaxInstallations = new JTextField();

		comboClient = new JComboBox<>();

		comboClient.setPreferredSize(new Dimension(200, 20));

		jTextFieldEndOfLicense = new JTextField();

		jTextFieldEndOfLicense.setEditable(false);
		jTextFieldEndOfLicense.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFXPanel jfxPanel = new JFXPanel();
				DatePicker datePicker = CellDateEditor.createDatePicker(jTextFieldEndOfLicense.getText(), jfxPanel);

				int answer = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(), jfxPanel,
						Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid5"),
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

				if (answer == 0 && datePicker.getValue() != null) {
					jTextFieldEndOfLicense.setText(datePicker.getValue().toString());
				}
			}
		});

		jTextFieldLicenseContract = new JTextField();

		jTextFieldLKey = new JTextField();

		jButtonCreateStandard = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.StandardLicense"));
		jButtonCreateStandard.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.StandardLicense.ToolTip"));
		jButtonCreateStandard.addActionListener(event -> startLicense("RETAIL", "1", null, false));

		jButtonCreateVolume = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.VolumeLicense"));
		jButtonCreateVolume
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.VolumeLicense.ToolTip"));
		jButtonCreateVolume.addActionListener(event -> startLicense("VOLUME", "0", null, false));

		jButtonCreateOEM = new JButton(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.OEMLicense"));
		jButtonCreateOEM
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.OEMLicense.ToolTip"));
		jButtonCreateOEM.addActionListener(
				event -> startLicense("OEM", "1", enterLicenseController.getChoicesAllHosts(), false));

		jButtonCreateConcurrent = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ConcurrentLicense"));
		jButtonCreateConcurrent.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ConcurrentLicense.ToolTip"));
		jButtonCreateConcurrent.addActionListener(event -> startLicense("CONCURRENT", "0", null, true));

		jButtonSend = new JButton(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.Execute"));
		jButtonSend.addActionListener((ActionEvent event) -> {
			deactivate();
			saveCurrentLicenseData();
			jTextFieldLKey.setText("");
		});
	}

	private void setupLayout() {
		JSplitPane splitPane = createSplitPane();
		this.setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE, "[grow,fill]", "[grow,fill]"));
		this.add(splitPane, "grow, push");
	}

	private JSplitPane createSplitPane() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.3);

		splitPane.setTopComponent(createTopPane());
		splitPane.setBottomComponent(createBottomPane());

		return splitPane;
	}

	private JPanel createTopPane() {
		JPanel topPane = new JPanel(new MigLayout("insets 0, wrap 1", "[grow,fill]", "[]" + Globals.MIN_GAP_SIZE));
		topPane.add(panelLicensePools, "grow, push, hmin " + MIN_PANEL_TABLE_HEIGHT + ", h " + MIN_PANEL_TABLE_HEIGHT);
		return topPane;
	}

	private JPanel createBottomPane() {
		JPanel bottomPane = new JPanel(
				new MigLayout("insets 0, wrap 1", "[grow,fill]", "[grow]" + Globals.GAP_SIZE + "[grow, 0:0]"));
		bottomPane.add(createPanelTask(), "grow, push");
		bottomPane.add(panelKeys, "grow, push, hmin 0");
		return bottomPane;
	}

	private JPanel createPanelTask() {
		JPanel panelTask = new JPanel(
				new MigLayout("insets 0, wrap 1", "[grow]", "[pref!]" + Globals.MIN_GAP_SIZE + "[grow]"
						+ Globals.MIN_GAP_SIZE + "[pref!]2[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]2[pref!]2[pref!]"));

		panelTask.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.Task") + ":"));
		panelTask.add(panelLicenseContracts,
				"grow, push, hmin " + MIN_PANEL_TABLE_HEIGHT + ", h " + MIN_PANEL_TABLE_HEIGHT);
		panelTask.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ChooseType")));

		panelTask.add(jButtonCreateStandard, "split 4, sizegroup btns");
		panelTask.add(jButtonCreateVolume, "gapleft 18, sizegroup btns");
		panelTask.add(jButtonCreateOEM, "gapleft 18, sizegroup btns");
		panelTask.add(jButtonCreateConcurrent, "gapleft 18, sizegroup btns, wrap");

		panelTask.add(createPanelLicenseModel(), "growx");
		panelTask.add(createPanelEnterKey(), "growx");
		panelTask.add(jButtonSend);

		return panelTask;
	}

	private JPanel createPanelLicenseModel() {
		JPanel panelLicenseModel = new JPanel(new MigLayout("insets " + Globals.GAP_SIZE, "[pref!]50[pref!]", "[]0"));
		panelLicenseModel.setBorder(BorderFactory.createEtchedBorder());

		panelLicenseModel.add(createLeftBlock());
		panelLicenseModel.add(createRightBlock());

		return panelLicenseModel;
	}

	private JPanel createLeftBlock() {
		JPanel left = new JPanel(new MigLayout("insets 0", "[left, 120!][grow]", "[]0"));
		left.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid1")));
		left.add(jTextFieldLicenseID, "growx, pushx, w 326!, wrap");
		left.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid2")));
		left.add(jTextFieldLicenseType, "w 326!, wrap");

		JPanel maxPanel = new JPanel(new MigLayout("insets 0", "[112!][grow]", "[]"));
		maxPanel.add(jTextFieldMaxInstallations, "cell 0 0, growx");
		maxPanel.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid3info")),
				"cell 1 0, growx, gapleft " + Globals.MIN_GAP_SIZE);
		left.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid3")), "alignx left");
		left.add(maxPanel, "wrap");

		left.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid4")));
		left.add(comboClient, "growx, pushx, w 326!, wrap");

		return left;
	}

	private JPanel createRightBlock() {
		JPanel right = new JPanel(new MigLayout("insets 0", "[left, 120!][200!]", "[]10[]"));
		right.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid5")));
		right.add(jTextFieldEndOfLicense, "growx, wrap");
		right.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid6")));
		right.add(jTextFieldLicenseContract, "growx, wrap");
		return right;
	}

	private JPanel createPanelEnterKey() {
		JPanel panelEnterKey = new JPanel(new MigLayout("insets 0", "[]", "[]"));
		panelEnterKey.setBorder(BorderFactory.createEtchedBorder());

		panelEnterKey.add(new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelLicenseKey")),
				"w 120!, gapleft " + Globals.GAP_SIZE);
		panelEnterKey.add(jTextFieldLKey, "wmin " + MIN_FIELD_WIDTH + ", w 326, growx");

		return panelEnterKey;
	}

	private void saveCurrentLicenseData() {
		Map<String, String> m = new HashMap<>();

		m.put(LicenseEntry.ID_KEY, jTextFieldLicenseID.getText());
		m.put(LicenseEntry.LICENSE_CONTRACT_ID_KEY, jTextFieldLicenseContract.getText());
		m.put(LicenseEntry.TYPE_KEY, jTextFieldLicenseType.getText());
		m.put(LicenseEntry.MAX_INSTALLATIONS_KEY,
				LicenseEntry.produceNormalizedCount(jTextFieldMaxInstallations.getText()));
		m.put(LicenseEntry.BOUND_TO_HOST_KEY, comboClient.getSelectedItem().toString());
		m.put(LicenseEntry.EXPIRATION_DATE_KEY, jTextFieldEndOfLicense.getText());

		String contractSendValue = jTextFieldLicenseContract.getText();
		if ("null".equals(contractSendValue)) {
			contractSendValue = "";
		}

		m.put("licenseContractId", contractSendValue);

		m.put("licensePoolId",
				panelLicensePools.getValueAt(panelLicensePools.getGenEditTable().getSelectedRow(), 0).toString());
		m.put("licenseKey", jTextFieldLKey.getText());

		enterLicenseController.saveNewLicense(m);
	}

	@Override
	public void reset() {
		super.reset();
		deactivate();
		panelLicensePools.moveToValue(selectedLicensePool, 0);
	}

	public PanelGenEdit getPanelKeys() {
		return panelKeys;
	}

	public PanelGenEdit getPanelLicensePools() {
		return panelLicensePools;
	}

	public PanelGenEdit getPanelLicenseContracts() {
		return panelLicenseContracts;
	}
}
