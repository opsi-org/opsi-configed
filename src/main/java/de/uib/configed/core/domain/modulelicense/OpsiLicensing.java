/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.domain.modulelicense;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.StringIntegerRowSorter;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.LicensingInfoPanelGenEditTable;
import de.uib.configed.gui.share.table.gui.LicensingInfoTableCellRenderer;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class OpsiLicensing extends JPanel {
	private static final int LABEL_COLOR_SIZE = 10;
	private static boolean extendedView;
	private static boolean showOnlyAvailableModules = true;

	private LicensingInfoPanelGenEditTable licensingTable;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private LicensingInfoMap licenseMap;

	private List<String> columnNames = new ArrayList<>();
	private Map<String, Map<String, Object>> theSourceMap = new HashMap<>();

	public OpsiLicensing() {
		PanelGenEdit mainPanel = initLicensingInfoPanel();
		JPanel clientInfo = initClientInfo();

		super.setLayout(new MigLayout("insets 0, wrap 1", "[grow]", "[grow]0[]"));

		super.add(mainPanel, "grow, push");
		super.add(clientInfo, "gapbottom " + Globals.MIN_GAP_SIZE);
	}

	private void retrieveData() {
		Logging.info(this, "retrieveData extendedView ", extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		licenseMap = LicensingInfoMap.getInstance();
		columnNames = licenseMap.getColumnNames();
		theSourceMap = licenseMap.getTableMap();
	}

	private PanelGenEdit initLicensingInfoPanel() {
		retrieveData();

		licensingTable = new LicensingInfoPanelGenEditTable() {
			@Override
			public void reload() {
				Logging.info(this, " LicInfoPanelGenTable reload, reduced ", !OpsiLicensing.extendedView);
				persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
				persistenceController.reloadData(ReloadEvent.OPSI_LICENSE_RELOAD.toString());
				LicensingInfoMap.requestRefresh();
				licenseMap = LicensingInfoMap.getInstance(
						persistenceController.getDataServices().module.getOpsiLicensingInfoOpsiAdminPD(),
						persistenceController.getDataServices().config.getConfigDefaultValuesPD(),
						!OpsiLicensing.extendedView);
				retrieveData();
				buildModel();
				super.reload();
			}
		};

		buildModel();

		licensingTable.getGenEditTable().getColumnModel().getColumn(0).setPreferredWidth(150);
		licensingTable.getGenEditTable().getColumnModel().getColumn(1).setPreferredWidth(60);

		return licensingTable;
	}

	private static JLabel createColorLabel(Color color) {
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setBackground(color);
		return label;
	}

	private JPanel initClientInfo() {
		retrieveData();

		JLabel okColor = createColorLabel(Globals.OPSI_OK);
		JLabel warningColor = createColorLabel(Globals.OPSI_WARNING);
		JLabel errorColor = createColorLabel(Globals.OPSI_ERROR);

		JLabel okLabel = new JLabel(Configed.getResourceValue("LicensingInfo.ok"));
		JLabel warningLabel = new JLabel(Configed.getResourceValue("LicensingInfo.warning"));
		JLabel errorLabel = new JLabel(Configed.getResourceValue("LicensingInfo.warning.over_limit"));

		JLabel warningLevelAbsolute = new JLabel(
				Configed.getResourceValue("LicensingInfo.warning.levels.client_absolute") + ": "
						+ licenseMap.getClientLimitWarningAbsolute());
		JLabel warningLevelPercent = new JLabel(Configed.getResourceValue("LicensingInfo.warning.levels.client_percent")
				+ ": " + licenseMap.getClientLimitWarningPercent());
		JLabel warningLevelDays = new JLabel(Configed.getResourceValue("LicensingInfo.warning.levels.days") + ": "
				+ licenseMap.getClientLimitWarningDays());

		JLabel checksumTitle = new JLabel(Configed.getResourceValue("LicensingInfo.client.checksum"));
		JLabel checksum = new JLabel(licenseMap.getCheckSum());

		checksumTitle.setToolTipText(Configed.getResourceValue("LicensingInfo.client.checksum.info"));

		checksum.setToolTipText(Configed.getResourceValue("LicensingInfo.client.checksum.info"));

		JCheckBox checkExtendedView = new JCheckBox(Configed.getResourceValue("LicensingInfo.buttonExtendedView"),
				extendedView);

		checkExtendedView.addActionListener((ActionEvent actionEvent) -> {
			setExtendedView(checkExtendedView.isSelected());
			licensingTable.reload();
		});

		JCheckBox checkShowOnlyAvailableModules = new JCheckBox(
				Configed.getResourceValue("LicensingInfo.buttonShowOnlyAvailableModules"), showOnlyAvailableModules);

		checkShowOnlyAvailableModules.addActionListener((ActionEvent actionEvent) -> {
			showOnlyAvailableModules(checkShowOnlyAvailableModules.isSelected());
			licensingTable.reload();
		});

		JPanel panel = new JPanel(new MigLayout("insets 10", "", "[]0"));

		panel.add(checkExtendedView, "split 2");
		panel.add(checkShowOnlyAvailableModules, "wrap");

		panel.add(okColor,
				"w " + LABEL_COLOR_SIZE + "!, h " + LABEL_COLOR_SIZE + "!, split 6, gaptop " + Globals.GAP_SIZE);
		panel.add(okLabel, "growx 0, gapright " + Globals.GAP_SIZE * 2 + ", gaptop " + Globals.GAP_SIZE);

		panel.add(warningColor, "w " + LABEL_COLOR_SIZE + "!, h " + LABEL_COLOR_SIZE + "!, gaptop " + Globals.GAP_SIZE);
		panel.add(warningLabel, "growx 0, gapright " + Globals.GAP_SIZE * 2 + ", gaptop " + Globals.GAP_SIZE);

		panel.add(errorColor, "w " + LABEL_COLOR_SIZE + "!, h " + LABEL_COLOR_SIZE + "!, gaptop " + Globals.GAP_SIZE);
		panel.add(errorLabel, "growx 0, gapright " + Globals.GAP_SIZE * 2 + ", gaptop " + Globals.GAP_SIZE + ", wrap");

		panel.add(warningLevelAbsolute, "split 3, gaptop 15 ");
		panel.add(warningLevelPercent, "gapleft " + Globals.GAP_SIZE * 2);
		panel.add(warningLevelDays, "gapleft " + Globals.GAP_SIZE * 2 + ", wrap");

		JPanel osInstallationPanel = initOSInstallationPanel();
		panel.add(osInstallationPanel, "gaptop 25, wrap");

		panel.add(checksumTitle, "gaptop 30, wrap");
		panel.add(checksum, "growx, gaptop " + Globals.MIN_GAP_SIZE);

		return panel;
	}

	private JPanel initOSInstallationPanel() {
		Map<String, Object> clientNumbers = licenseMap.getClientNumbersMap();
		JLabel clientTitle = new JLabel(Configed.getResourceValue("LicensingInfo.client.title") + "  ("
				+ persistenceController.getDataServices().hostInfoCollections.getConfigServer() + ")");
		JLabel allClient = new JLabel(Configed.getResourceValue("LicensingInfo.client.all_clients") + ": ");
		JLabel allClientNum = new JLabel(clientNumbers.get(LicensingInfoMap.ALL).toString());
		JLabel macos = new JLabel(Configed.getResourceValue("LicensingInfo.client.macos_clients") + ": ");
		JLabel macosNum = new JLabel(clientNumbers.get(LicensingInfoMap.MAC_OS).toString());
		JLabel linux = new JLabel(Configed.getResourceValue("LicensingInfo.client.linux_clients") + ": ");
		JLabel linuxNum = new JLabel(clientNumbers.get(LicensingInfoMap.LINUX).toString());
		JLabel windows = new JLabel(Configed.getResourceValue("LicensingInfo.client.windows_clients") + ": ");
		JLabel windowsNum = new JLabel(clientNumbers.get(LicensingInfoMap.WINDOWS).toString());

		JLabel customerTitle = new JLabel(Configed.getResourceValue("LicensingInfo.customer.data"));
		Set<String> customerSet = licenseMap.getCustomerNamesSet();
		JLabel customerNames = new JLabel(
				customerSet.toString().replace("[", "<html>").replace(", ", "<br>").replace("]", "</html>"));

		JPanel osInstallationPanel = new JPanel(new MigLayout("insets 0, wrap 1", "[pref!][grow]60[pref!]"));

		osInstallationPanel.add(clientTitle, "cell 0 0");
		osInstallationPanel.add(customerTitle, "cell 2 0");

		JPanel osInstallationDataPanel = new JPanel(new MigLayout("insets 0, wrap 1", "[pref!][pref!]"));

		osInstallationDataPanel.add(allClient, "cell 0 0");
		osInstallationDataPanel.add(allClientNum, "cell 1 0");

		osInstallationDataPanel.add(macos, "cell 0 1");
		osInstallationDataPanel.add(macosNum, "cell 1 1");

		osInstallationDataPanel.add(linux, "cell 0 2");
		osInstallationDataPanel.add(linuxNum, "cell 1 2");

		osInstallationDataPanel.add(windows, "cell 0 3");
		osInstallationDataPanel.add(windowsNum, "cell 1 3");

		osInstallationPanel.add(osInstallationDataPanel,
				"cell 0 1, alignx left, aligny top, gaptop " + Globals.GAP_SIZE);
		osInstallationPanel.add(customerNames, "cell 2 1, alignx left, aligny top, gaptop " + Globals.GAP_SIZE);

		return osInstallationPanel;
	}

	private static void setExtendedView(boolean isExtendedView) {
		OpsiLicensing.extendedView = isExtendedView;
		Logging.info("extendedView ", extendedView, ", i.e. reduced ", !extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		LicensingInfoMap.requestRefresh();
	}

	private static void showOnlyAvailableModules(boolean showOnlyAvailableModules) {
		OpsiLicensing.showOnlyAvailableModules = showOnlyAvailableModules;
		LicensingInfoMap.requestRefresh();
	}

	private void buildModel() {
		GenTableModel theModel = new GenTableModel(null,
				DefaultTableProvider.createWithMapSource(columnNames, theSourceMap), 0, new int[] {}, licensingTable,
				new ArrayList<>());

		theModel.reset();

		columnNames = theModel.getColumnNames();

		licensingTable.setTableModel(theModel);

		// With this we can assure that the row sorter is set up correctly
		// and we have the correct integer columns from 2 to the end of the model
		// (assuming the first two columns are not integer columns)
		licensingTable.getGenEditTable().setRowSorter(new StringIntegerRowSorter(theModel,
				IntStream.range(2, theModel.getColumnCount()).boxed().collect(Collectors.toSet())));

		licensingTable.getGenEditTable().setDefaultRenderer(Object.class,
				new LicensingInfoTableCellRenderer(LicensingInfoMap.getInstance()));
	}

	public static boolean isExtendedView() {
		return extendedView;
	}

	public static boolean isShowOnlyAvailableModules() {
		return showOnlyAvailableModules;
	}

	public void reload() {
		licensingTable.reload();
	}
}
