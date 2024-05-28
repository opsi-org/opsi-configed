/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsidatamodel.modulelicense;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.LicensingInfoPanelGenEditTable;
import de.uib.utils.table.gui.LicensingInfoTableCellRenderer;
import de.uib.utils.table.gui.PanelGenEditTable;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapSource;
import de.uib.utils.table.provider.TableSource;
import de.uib.utils.table.updates.MapBasedTableEditItem;

public class LicensingInfoDialog extends FGeneralDialog {
	private static boolean extendedView;
	private static boolean showOnlyAvailableModules = true;

	private LicensingInfoPanelGenEditTable thePanel;
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private LicensingInfoMap licenseMap;

	private TableSource tableSource;

	private List<String> columnNames = new ArrayList<>();
	private Map<String, Map<String, Object>> theSourceMap = new HashMap<>();

	public LicensingInfoDialog(JFrame owner, String title, boolean modal, String[] buttonList, int lastButtonNo,
			int preferredWidth, int preferredHeight, boolean lazyLayout) {
		super(owner, title, modal, buttonList, lastButtonNo, preferredWidth, preferredHeight, lazyLayout);

		super.setCenterPaneInScrollpane(initMainPanel());
		super.setAdditionalPane(initClientInfo());

		super.setupLayout();
		super.setVisible(true);
	}

	@Override
	protected void allLayout() {
		// we could design an adapted layout and infuse it in guiInit

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(Alignment.LEADING).addGroup(southLayout
				.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)

				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(centerPanel, 200, 300, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)

				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE).addComponent(southPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)));
	}

	private void retrieveData() {
		Logging.info(this, "retrieveData extendedView " + extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		licenseMap = LicensingInfoMap.getInstance();
		columnNames = licenseMap.getColumnNames();
		theSourceMap = licenseMap.getTableMap();
	}

	private PanelGenEditTable initMainPanel() {
		retrieveData();

		thePanel = new LicensingInfoPanelGenEditTable("opsi Modules Validation", false, 0,
				new int[] { PanelGenEditTable.POPUP_PRINT, PanelGenEditTable.POPUP_PDF,
						PanelGenEditTable.POPUP_SORT_AGAIN, PanelGenEditTable.POPUP_EXPORT_CSV,
						PanelGenEditTable.POPUP_EXPORT_SELECTED_CSV, PanelGenEditTable.POPUP_RELOAD },
				false) {
			@Override
			public void reload() {
				Logging.info(this, " LicInfoPanelGenTable reload, reduced " + !LicensingInfoDialog.extendedView);
				persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
				persistenceController.reloadData(ReloadEvent.OPSI_LICENSE_RELOAD.toString());
				LicensingInfoMap.requestRefresh();
				licenseMap = LicensingInfoMap.getInstance(
						persistenceController.getModuleDataService().getOpsiLicensingInfoOpsiAdminPD(),
						persistenceController.getConfigDataService().getConfigDefaultValuesPD(),
						!LicensingInfoDialog.extendedView);
				retrieveData();
				tableSource = new MapSource(columnNames, theSourceMap, false);
				buildModel();
				super.reload();
			}
		};

		thePanel.setMarkBoldHeaderCellRenderer();

		tableSource = new MapSource(columnNames, theSourceMap, false);

		buildModel();

		thePanel.setSize(500, 200);

		thePanel.setUpdateController(null);

		thePanel.getTheTable().setRowSorter(null);

		thePanel.getColumnModel().getColumn(0).setPreferredWidth(150);
		thePanel.getColumnModel().getColumn(1).setPreferredWidth(60);

		return thePanel;
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
			thePanel.reload();
		});

		JCheckBox checkShowOnlyAvailableModules = new JCheckBox(
				Configed.getResourceValue("LicensingInfo.buttonShowOnlyAvailableModules"), showOnlyAvailableModules);

		checkShowOnlyAvailableModules.addActionListener((ActionEvent actionEvent) -> {
			showOnlyAvailableModules(checkShowOnlyAvailableModules.isSelected());
			thePanel.reload();
		});

		JButton buttonReload = new JButton(Utils.getIntellijIcon("refresh"));
		buttonReload.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.buttonReload"));
		buttonReload.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		buttonReload.addActionListener((ActionEvent actionEvent) -> {
			LicensingInfoMap.requestRefresh();
			thePanel.reload();
		});

		JPanel panel = new JPanel();
		GroupLayout gLayout = new GroupLayout(panel);
		panel.setLayout(gLayout);

		gLayout.setAutoCreateGaps(true);
		gLayout.setAutoCreateContainerGaps(true);

		gLayout.setHorizontalGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(gLayout.createSequentialGroup()
						.addComponent(buttonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(50)
						.addComponent(checkExtendedView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkShowOnlyAvailableModules, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(gLayout.createSequentialGroup().addComponent(redWarningColor, 10, 10, 10)
						.addComponent(redWarningLabel).addGap(0, 0, Short.MAX_VALUE).addComponent(orangeWarningColor,
								10, 10, 10)
						.addComponent(orangeWarningLabel))
				.addGroup(
						gLayout.createSequentialGroup().addComponent(warningLevelAbsolute).addGap(15).addComponent(
								warningLevelPercent).addGap(15).addComponent(
										warningLevelDays))
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
						.addGap(60)
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(customerTitle)
								.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(customerNames)))));

		gLayout.setVerticalGroup(gLayout.createSequentialGroup()
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(buttonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkExtendedView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkShowOnlyAvailableModules, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(redWarningColor, 10, 10, 10).addComponent(redWarningLabel)
						.addComponent(orangeWarningColor, 10, 10, 10).addComponent(orangeWarningLabel))
				.addGap(15)
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(warningLevelAbsolute)
						.addComponent(warningLevelPercent).addComponent(warningLevelDays))
				.addGap(25)
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(clientTitle)
						.addGap(30).addComponent(customerTitle))
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(gLayout
						.createSequentialGroup()
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(gLayout.createSequentialGroup().addComponent(allClient).addComponent(macos)
										.addComponent(linux).addComponent(windows))
								.addGroup(gLayout.createSequentialGroup().addComponent(allClientNum)
										.addComponent(macosNum).addComponent(linuxNum).addComponent(windowsNum)))
						.addGap(30)
						.addGroup(
								gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(checksumTitle))
						.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(gLayout.createSequentialGroup().addComponent(checksum)).addGap(20)))
						.addGap(30).addGroup(gLayout.createSequentialGroup()
								.addGroup(gLayout.createSequentialGroup().addComponent(customerNames)))));

		return panel;
	}

	private static void setExtendedView(boolean isExtendedView) {
		LicensingInfoDialog.extendedView = isExtendedView;
		Logging.info("extendedView " + extendedView + ", i.e. reduced " + !extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		LicensingInfoMap.requestRefresh();
	}

	private static void showOnlyAvailableModules(boolean showOnlyAvailableModules) {
		LicensingInfoDialog.showOnlyAvailableModules = showOnlyAvailableModules;
		LicensingInfoMap.requestRefresh();
	}

	private void buildModel() {
		List<MapBasedTableEditItem> updateCollection = new ArrayList<>();

		GenTableModel theModel = new GenTableModel(null, new DefaultTableProvider(tableSource), 0, new int[] {},
				thePanel, updateCollection);

		theModel.reset();

		columnNames = theModel.getColumnNames();

		thePanel.setTableModel(theModel);

		thePanel.getTheTable().setDefaultRenderer(Object.class,
				new LicensingInfoTableCellRenderer(LicensingInfoMap.getInstance()));
	}

	public void reload() {
		thePanel.reload();
	}

	public static boolean isExtendedView() {
		return extendedView;
	}

	public static boolean isShowOnlyAvailableModules() {
		return showOnlyAvailableModules;
	}
}
