/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * PanelEnterLicense.java
 * after selecting a pool, one can add license options for it
 * Created 17.02.2009-2015
 */

package de.uib.configed.gui.licenses;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelEnterLicense;
import de.uib.configed.Globals;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class PanelEnterLicense extends MultiTablePanel {
	private static final int MIN_HEIGHT = 50;
	private static final int MIN_PANEL_TABLE_HEIGHT = 60;

	private static final int MIN_FIELD_WIDTH = 40;
	private static final int MIN_FIELD_HEIGHT = 6;

	private PanelGenEditTable panelKeys;
	private PanelGenEditTable panelLicensePools;
	private PanelGenEditTable panelLicenseContracts;

	private String selectedLicensePool = "";
	private ListSelectionListener licensePoolSelectionListener;

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

	private JLabel jLabelSLid1;
	private JLabel jLabelSLid2;
	private JLabel jLabelSLid3;
	private JLabel jLabelSLid4;
	private JLabel jLabelSLid5;
	private JLabel jLabelSLid6;
	private JLabel jLabelTask;
	private JLabel jLabelConfigure;
	private JLabel jLabelSLid3info;
	private JLabel jLabelLKey;

	private FEditDate fEditDate;

	private ControlPanelEnterLicense enterLicenseController;

	private ComboBoxModel<String> emptyComboBoxModel = new DefaultComboBoxModel<>(new String[] { "" });

	public PanelEnterLicense(ControlPanelEnterLicense enterLicenseController) {
		super(enterLicenseController);

		this.enterLicenseController = enterLicenseController;

		initComponents();
		deactivate();
		setupLayout();
		defineListeners();
	}

	private void defineListeners() {
		panelLicenseContracts.getListSelectionModel().addListSelectionListener(this::selectPanelLicenseContracts);

		panelLicensePools.addListSelectionListener(this::selectPanelLicensePools);
	}

	private void selectPanelLicenseContracts(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();

		if (!lsm.isSelectionEmpty()) {
			int selectedRow = lsm.getMinSelectionIndex();
			String keyValue = panelLicenseContracts.getValueAt(selectedRow, 0).toString();

			if (jTextFieldLicenseContract.isEnabled()) {
				jTextFieldLicenseContract.setText(keyValue);
			}
		}
	}

	private void selectPanelLicensePools(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		int i = panelLicensePools.getSelectedRow();

		selectedLicensePool = "";

		if (i > -1) {
			selectedLicensePool = panelLicensePools.getValueAt(i, 0).toString();
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
		if (panelLicensePools.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectLicensepool"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		if (panelLicenseContracts.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.pleaseSelectLicensecontract"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		jTextFieldLicenseID.setEnabled(true);
		jTextFieldLicenseID.setText("l_" + Utils.getSeconds());

		jTextFieldEndOfLicense.setEnabled(true);
		jTextFieldEndOfLicense.setText("");
		jTextFieldLicenseContract.setEnabled(true);
		jTextFieldLicenseContract
				.setText("" + panelLicenseContracts.getValueAt(panelLicenseContracts.getSelectedRow(), 0));
		jTextFieldLicenseContract.setEditable(false);

		jTextFieldLKey.setEnabled(true);

		jButtonSend.setEnabled(true);

		return true;
	}

	private void startStandard() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenseType.setEnabled(true);
		jTextFieldLicenseType.setText("RETAIL");
		jTextFieldLicenseType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("1");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);
	}

	private void startVolume() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenseType.setEnabled(true);
		jTextFieldLicenseType.setText("VOLUME");
		jTextFieldLicenseType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("0");
		jTextFieldMaxInstallations.setEditable(true);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);
	}

	private void startOEM() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenseType.setEnabled(true);
		jTextFieldLicenseType.setText("OEM");
		jTextFieldLicenseType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("1");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setModel(
				new DefaultComboBoxModel<>(enterLicenseController.getChoicesAllHosts().toArray(new String[0])));
		comboClient.setEnabled(true);
	}

	private void startConcurrent() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenseID.setEnabled(true);
		jTextFieldLicenseID.setText("l_" + Utils.getSeconds());
		jTextFieldLicenseType.setEnabled(true);
		jTextFieldLicenseType.setText("CONCURRENT");
		jTextFieldLicenseType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("0");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);
	}

	private void initComponents() {
		panelKeys = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicenseOptionsView"), true, 0,
				new int[] { PanelGenEditTable.POPUP_RELOAD }, false);

		panelKeys.setMasterFrame(ConfigedMain.getLicensesFrame());

		panelLicensePools = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensepool"), false, 0,
				new int[] { PanelGenEditTable.POPUP_RELOAD }, true);

		panelLicensePools.setMasterFrame(ConfigedMain.getLicensesFrame());

		panelLicenseContracts = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleSelectLicensecontract"), true, 1,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);

		panelLicenseContracts.setMasterFrame(ConfigedMain.getLicensesFrame());

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
				if (e.getClickCount() > 1 || e.getButton() != MouseEvent.BUTTON1) {
					if (fEditDate == null) {
						fEditDate = new FEditDate(jTextFieldEndOfLicense.getText());
					} else {
						fEditDate.setStartText(jTextFieldEndOfLicense.getText());
					}

					fEditDate.setCaller(jTextFieldEndOfLicense);
					fEditDate.init();

					Point pointField = jTextFieldEndOfLicense.getLocationOnScreen();
					fEditDate.setLocation((int) pointField.getX() + 30, (int) pointField.getY() + 20);

					fEditDate.setTitle(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid5"));
					fEditDate.setVisible(true);
				}
			}
		});

		jTextFieldLicenseContract = new JTextField();

		jTextFieldLKey = new JTextField();

		jButtonCreateStandard = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.StandardLicense"));
		jButtonCreateStandard.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.StandardLicense.ToolTip"));
		jButtonCreateStandard.addActionListener(event -> startStandard());

		jButtonCreateVolume = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.VolumeLicense"));
		jButtonCreateVolume
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.VolumeLicense.ToolTip"));
		jButtonCreateVolume.addActionListener(event -> startVolume());

		jButtonCreateOEM = new JButton(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.OEMLicense"));
		jButtonCreateOEM
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.OEMLicense.ToolTip"));
		jButtonCreateOEM.addActionListener(event -> startOEM());

		jButtonCreateConcurrent = new JButton(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ConcurrentLicense"));
		jButtonCreateConcurrent.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ConcurrentLicense.ToolTip"));
		jButtonCreateConcurrent.addActionListener(event -> startConcurrent());

		jButtonSend = new JButton(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.Execute"));
		jButtonSend.addActionListener((ActionEvent event) -> {
			deactivate();
			saveCurrentLicenseData();
			jTextFieldLKey.setText("");
		});

		jLabelTask = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.Task") + ":");

		jLabelConfigure = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.ChooseType"));

		jLabelSLid1 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid1"));
		jLabelSLid2 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid2"));
		jLabelSLid3 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid3"));
		jLabelSLid4 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid4"));
		jLabelSLid5 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid5"));
		jLabelSLid6 = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid6"));

		jLabelSLid3info = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelSLid3info"));

		jLabelLKey = new JLabel(Configed.getResourceValue("ConfigedMain.Licenses.EnterLicense.LabelLicenseKey"));
	}

	private void setupLayout() {
		JPanel panelLicenseModel = new JPanel();
		panelLicenseModel.setBorder(BorderFactory.createEtchedBorder());

		GroupLayout panelLicenseModelLayout = new GroupLayout(panelLicenseModel);
		panelLicenseModel.setLayout(panelLicenseModelLayout);
		panelLicenseModelLayout.setHorizontalGroup(panelLicenseModelLayout.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(jLabelSLid4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jLabelSLid3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jLabelSLid2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jLabelSLid1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.UNRELATED)

				.addGroup(panelLicenseModelLayout.createSequentialGroup().addGroup(panelLicenseModelLayout
						.createParallelGroup(Alignment.LEADING, true)
						.addComponent(comboClient, MIN_FIELD_WIDTH, 208, Short.MAX_VALUE)
						.addGroup(panelLicenseModelLayout.createSequentialGroup()
								.addComponent(jTextFieldMaxInstallations, MIN_FIELD_WIDTH, 112,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(jLabelSLid3info, MIN_FIELD_WIDTH, 112, GroupLayout.PREFERRED_SIZE))
						.addComponent(jTextFieldLicenseID, MIN_FIELD_WIDTH, 208, Short.MAX_VALUE)
						.addComponent(jTextFieldLicenseType, MIN_FIELD_WIDTH, 239, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
						.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(panelLicenseModelLayout.createSequentialGroup()
										.addComponent(jLabelSLid6, GroupLayout.PREFERRED_SIZE, 120,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED)

										.addComponent(jTextFieldLicenseContract, MIN_FIELD_WIDTH, 200,
												GroupLayout.PREFERRED_SIZE))
								.addGroup(panelLicenseModelLayout.createSequentialGroup()
										.addComponent(jLabelSLid5, GroupLayout.PREFERRED_SIZE, 120,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED)

										.addComponent(jTextFieldEndOfLicense, MIN_FIELD_WIDTH, 200,
												GroupLayout.PREFERRED_SIZE))))
				.addContainerGap(10, Short.MAX_VALUE));

		panelLicenseModelLayout.setVerticalGroup(panelLicenseModelLayout.createSequentialGroup()
				.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jLabelSLid1, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldLicenseID, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelSLid5, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldEndOfLicense, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jLabelSLid2, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldLicenseType, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE))

				.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jLabelSLid3, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldMaxInstallations, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelSLid3info, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelSLid6, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldLicenseContract, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
								GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(panelLicenseModelLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(jLabelSLid4, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboClient, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)));

		JPanel panelEnterKey = new JPanel();
		panelEnterKey.setBorder(BorderFactory.createEtchedBorder());

		GroupLayout panelEnterKeyLayout = new GroupLayout(panelEnterKey);
		panelEnterKey.setLayout(panelEnterKeyLayout);
		panelEnterKeyLayout.setHorizontalGroup(panelEnterKeyLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelLKey, GroupLayout.PREFERRED_SIZE, 133, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jTextFieldLKey, MIN_FIELD_WIDTH, 326, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(10, Short.MAX_VALUE));

		panelEnterKeyLayout.setVerticalGroup(panelEnterKeyLayout.createParallelGroup(Alignment.BASELINE)
				.addComponent(jLabelLKey, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldLKey, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE));

		JPanel panelTask = new JPanel();
		GroupLayout layoutTask = new GroupLayout(panelTask);
		panelTask.setLayout(layoutTask);

		layoutTask.setHorizontalGroup(layoutTask.createSequentialGroup().addGroup(layoutTask
				.createParallelGroup(Alignment.LEADING)
				.addGroup(layoutTask.createParallelGroup(Alignment.LEADING).addComponent(panelLicenseContracts, 50, 300,
						Short.MAX_VALUE))

				.addGroup(layoutTask.createSequentialGroup()
						.addComponent(jButtonSend, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(1587, Short.MAX_VALUE))
				.addGroup(layoutTask.createSequentialGroup()
						.addComponent(jButtonCreateStandard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jButtonCreateVolume, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jButtonCreateOEM, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(18)
						.addComponent(jButtonCreateConcurrent, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap(1226, Short.MAX_VALUE))
				.addGroup(layoutTask.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(jLabelTask)
						.addContainerGap(1515, Short.MAX_VALUE))
				.addGroup(layoutTask.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(jLabelConfigure)
						.addContainerGap(1515, Short.MAX_VALUE))
				.addGroup(layoutTask.createSequentialGroup()
						.addGroup(layoutTask.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(panelEnterKey, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(panelLicenseModel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE))));

		layoutTask.setVerticalGroup(layoutTask.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelTask).addGap(Globals.MIN_GAP_SIZE)
				.addComponent(panelLicenseContracts, MIN_PANEL_TABLE_HEIGHT, MIN_PANEL_TABLE_HEIGHT, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(jLabelConfigure).addGap(2)
				.addGroup(layoutTask.createParallelGroup(Alignment.BASELINE)
						.addComponent(jButtonCreateStandard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateOEM, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateVolume, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateConcurrent, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(panelLicenseModel, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2)
				.addComponent(panelEnterKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2).addComponent(jButtonSend, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.3);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup().addComponent(panelLicensePools,
				GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addComponent(panelLicensePools, MIN_PANEL_TABLE_HEIGHT, MIN_PANEL_TABLE_HEIGHT, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createSequentialGroup()
				.addGroup(layoutBottomPane.createParallelGroup(Alignment.LEADING)
						.addComponent(panelTask, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(panelKeys, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(panelTask, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE).addComponent(panelKeys, MIN_PANEL_TABLE_HEIGHT - 2 * Globals.LINE_HEIGHT,
						MIN_PANEL_TABLE_HEIGHT - 2 * Globals.LINE_HEIGHT, Short.MAX_VALUE));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));
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

		m.put("licensePoolId", panelLicensePools.getValueAt(panelLicensePools.getSelectedRow(), 0).toString());
		m.put("licenseKey", jTextFieldLKey.getText());

		enterLicenseController.saveNewLicense(m);
	}

	@Override
	public void reset() {
		panelLicensePools.removeListSelectionListener(licensePoolSelectionListener);
		super.reset();
		deactivate();
		panelLicensePools.addListSelectionListener(licensePoolSelectionListener);
		panelLicensePools.moveToValue(selectedLicensePool, 0);
	}

	public PanelGenEditTable getPanelKeys() {
		return panelKeys;
	}

	public PanelGenEditTable getPanelLicensePools() {
		return panelLicensePools;
	}

	public PanelGenEditTable getPanelLicenseContracts() {
		return panelLicenseContracts;
	}
}
