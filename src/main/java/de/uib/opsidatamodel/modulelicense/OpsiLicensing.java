/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.modulelicense;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.LicensingInfoPanelGenEditTable;
import de.uib.utils.table.gui.LicensingInfoTableCellRenderer;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapSource;
import de.uib.utils.table.provider.TableSource;
import de.uib.utils.table.updates.MapBasedTableEditItem;

public class OpsiLicensing extends JPanel {
	private static boolean extendedView;
	private static boolean showOnlyAvailableModules = true;

	private LicensingInfoPanelGenEditTable licensingTable;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private LicensingInfoMap licenseMap;

	private TableSource tableSource;

	private List<String> columnNames = new ArrayList<>();
	private Map<String, Map<String, Object>> theSourceMap = new HashMap<>();

	public OpsiLicensing() {
		PanelGenEditTable mainPanel = initLicensingInfoPanel();
		JPanel clientInfo = initClientInfo();

		GroupLayout groupLayout = new GroupLayout(this);
		super.setLayout(groupLayout);

		groupLayout
				.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(mainPanel).addComponent(clientInfo));

		groupLayout
				.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(mainPanel).addComponent(clientInfo));
	}

	private void retrieveData() {
		Logging.info(this, "retrieveData extendedView ", extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		licenseMap = LicensingInfoMap.getInstance();
		columnNames = licenseMap.getColumnNames();
		theSourceMap = licenseMap.getTableMap();
	}

	private PanelGenEditTable initLicensingInfoPanel() {
		retrieveData();

		licensingTable = new LicensingInfoPanelGenEditTable() {
			@Override
			public void reload() {
				Logging.info(this, " LicInfoPanelGenTable reload, reduced ", !OpsiLicensing.extendedView);
				persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
				persistenceController.reloadData(ReloadEvent.OPSI_LICENSE_RELOAD.toString());
				LicensingInfoMap.requestRefresh();
				licenseMap = LicensingInfoMap.getInstance(
						persistenceController.getModuleDataService().getOpsiLicensingInfoOpsiAdminPD(),
						persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
						!OpsiLicensing.extendedView);
				retrieveData();
				tableSource = new MapSource(columnNames, theSourceMap, false);
				buildModel();
				super.reload();
			}
		};

		tableSource = new MapSource(columnNames, theSourceMap, false);

		buildModel();

		licensingTable.getJTable().getColumnModel().getColumn(0).setPreferredWidth(150);
		licensingTable.getJTable().getColumnModel().getColumn(1).setPreferredWidth(60);

		return licensingTable;
	}

	private JPanel initClientInfo() {
		retrieveData();

		JLabel orangeWarningColor = new JLabel();
		orangeWarningColor.setOpaque(true);
		orangeWarningColor.setBackground(Globals.OPSI_WARNING);

		JLabel redWarningColor = new JLabel();
		redWarningColor.setOpaque(true);
		redWarningColor.setBackground(Globals.OPSI_ERROR);

		JLabel orangeWarningLabel = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning") + "</html>");
		JLabel redWarningLabel = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning.over_limit") + "</html>");

		JLabel warningLevelAbsolute = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning.levels.client_absolute") + ": "
						+ licenseMap.getClientLimitWarningAbsolute() + "</html>");
		JLabel warningLevelPercent = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning.levels.client_percent") + ": "
						+ licenseMap.getClientLimitWarningPercent() + "</html>");
		JLabel warningLevelDays = new JLabel("<html>" + Configed.getResourceValue("LicensingInfo.warning.levels.days")
				+ ": " + licenseMap.getClientLimitWarningDays() + "</html>");

		Map<String, Object> clientNumbers = licenseMap.getClientNumbersMap();
		JLabel clientTitle = new JLabel("<html>" + Configed.getResourceValue("LicensingInfo.client.title") + "  ("
				+ persistenceController.getHostInfoCollections().getConfigServer() + ") </html>");
		JLabel allClient = new JLabel(Configed.getResourceValue("LicensingInfo.client.all_clients") + ": ");
		JLabel allClientNum = new JLabel(clientNumbers.get(LicensingInfoMap.ALL).toString());
		JLabel macos = new JLabel(Configed.getResourceValue("LicensingInfo.client.macos_clients") + ": ");
		JLabel macosNum = new JLabel(clientNumbers.get(LicensingInfoMap.MAC_OS).toString());
		JLabel linux = new JLabel(Configed.getResourceValue("LicensingInfo.client.linux_clients") + ": ");
		JLabel linuxNum = new JLabel(clientNumbers.get(LicensingInfoMap.LINUX).toString());
		JLabel windows = new JLabel(Configed.getResourceValue("LicensingInfo.client.windows_clients") + ": ");
		JLabel windowsNum = new JLabel(clientNumbers.get(LicensingInfoMap.WINDOWS).toString());
		JLabel checksumTitle = new JLabel(Configed.getResourceValue("LicensingInfo.client.checksum"));
		JLabel checksum = new JLabel(licenseMap.getCheckSum());

		JLabel customerTitle = new JLabel(Configed.getResourceValue("LicensingInfo.customer.data"));
		Set<String> customerSet = licenseMap.getCustomerNamesSet();
		JLabel customerNames = new JLabel(
				customerSet.toString().replace("[", "<html>").replace(", ", "<br>").replace("]", "</html>"));

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

		JPanel panel = new JPanel();
		GroupLayout gLayout = new GroupLayout(panel);
		panel.setLayout(gLayout);

		gLayout.setAutoCreateGaps(true);
		gLayout.setAutoCreateContainerGaps(true);

		gLayout.setHorizontalGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(gLayout.createSequentialGroup()
						.addComponent(checkExtendedView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkShowOnlyAvailableModules, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gLayout.createSequentialGroup().addComponent(redWarningColor, 10, 10, 10)
						.addComponent(redWarningLabel).addGap(0, 0, Short.MAX_VALUE)
						.addComponent(orangeWarningColor, 10, 10, 10).addComponent(orangeWarningLabel))
				.addGroup(gLayout.createSequentialGroup().addComponent(warningLevelAbsolute).addGap(15)
						.addComponent(warningLevelPercent).addGap(15).addComponent(warningLevelDays))
				.addGroup(gLayout
						.createSequentialGroup().addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

								.addComponent(clientTitle)
								.addGroup(gLayout.createSequentialGroup()
										.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(allClient).addComponent(macos).addComponent(linux)
												.addComponent(windows))
										.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(allClientNum).addComponent(macosNum)
												.addComponent(linuxNum).addComponent(windowsNum)))
								.addComponent(checksumTitle).addComponent(checksum))
						.addGap(60).addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(customerTitle).addComponent(customerNames))));

		gLayout.setVerticalGroup(
				gLayout.createSequentialGroup()
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(checkExtendedView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(checkShowOnlyAvailableModules, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(redWarningColor, 10, 10, 10).addComponent(redWarningLabel)
								.addComponent(orangeWarningColor, 10, 10, 10).addComponent(orangeWarningLabel))
						.addGap(15)
						.addGroup(gLayout
								.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(warningLevelAbsolute)
								.addComponent(warningLevelPercent).addComponent(warningLevelDays))
						.addGap(25)
						.addGroup(gLayout
								.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(clientTitle).addGap(30)
								.addComponent(customerTitle))
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(gLayout.createSequentialGroup()
										.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addGroup(gLayout.createSequentialGroup().addComponent(allClient)
														.addComponent(macos).addComponent(linux).addComponent(windows))
												.addGroup(gLayout.createSequentialGroup().addComponent(allClientNum)
														.addComponent(macosNum).addComponent(linuxNum)
														.addComponent(windowsNum)))
										.addGap(30).addComponent(checksumTitle).addComponent(checksum))
								.addGap(30).addComponent(customerNames)));

		return panel;
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
		List<MapBasedTableEditItem> updateCollection = new ArrayList<>();

		GenTableModel theModel = new GenTableModel(null, new DefaultTableProvider(tableSource), 0, new int[] {},
				licensingTable, updateCollection);

		theModel.reset();

		columnNames = theModel.getColumnNames();

		licensingTable.setTableModel(theModel);

		licensingTable.getJTable().setDefaultRenderer(Object.class,
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
