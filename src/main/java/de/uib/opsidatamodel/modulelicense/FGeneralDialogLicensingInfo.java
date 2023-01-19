package de.uib.opsidatamodel.modulelicense;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.Configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconAsButton;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelLinedComponents;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.LicensingInfoPanelGenEditTable;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.MapSource;
import de.uib.utilities.table.provider.TableSource;
import de.uib.utilities.table.updates.GenericTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class FGeneralDialogLicensingInfo extends FGeneralDialog {

	public LicensingInfoPanelGenEditTable thePanel;
	GenTableModel theModel;
	PersistenceController persist;
	LicensingInfoMap licenseMap;

	TableSource tableSource;

	List<String> columnNames = new ArrayList<>();
	List<String> classNames = new ArrayList<>();
	Map<String, Map> theSourceMap = new HashMap<>();
	Map<String, Map<String, Map<String, Object>>> datesMap = new HashMap<>();
	Map<String, Object> clientNumbers;

	public static boolean extendedView = false;

	JLabel clientTitle = new JLabel();
	JLabel allClient = new JLabel();
	JLabel allClientNum = new JLabel();
	JLabel macos = new JLabel();
	JLabel macosNum = new JLabel();
	JLabel linux = new JLabel();
	JLabel linuxNum = new JLabel();
	JLabel windows = new JLabel();
	JLabel windowsNum = new JLabel();
	JLabel checksumTitle = new JLabel();
	JLabel checksum = new JLabel();
	JLabel checksumInfo = new JLabel();
	JLabel customerTitle = new JLabel();
	Set customerSet;
	JLabel customerNames = new JLabel();

	public FGeneralDialogLicensingInfo(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout, JPanel addPane) {
		super(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout,
				addPane);

		persist = PersistenceControllerFactory.getPersistenceController();
		PanelGenEditTable centerPanel = new PanelGenEditTable();
		JPanel bottomPanel = new JPanel();

		bottomPanel = this.initClientInfo();
		centerPanel = this.initMainPanel();

		this.setCenterPaneInScrollpane(centerPanel);
		this.setAdditionalPane(bottomPanel);

		this.setupLayout();
		this.setVisible(true);
	}

	@Override
	protected void allLayout() {

		// we could design an adapted layout and infuse it in guiInit

		allpane.setBackground(Globals.BACKGROUND_COLOR_7);

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null)
			centerPanel = new JPanel();

		centerPanel.setBackground(Globals.F_GENERAL_DIALOG_LICENSING_INFO_BACKGROUND_COLOR);
		centerPanel.setOpaque(true);

		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)

				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		southPanel.setOpaque(false);
		southPanel.setBackground(Globals.F_GENERAL_DIALOG_LICENSING_INFO_BACKGROUND_COLOR);
		southPanel.setOpaque(true);

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(centerPanel, 200, 300, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE)

				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)));

	}

	private void retrieveData() {
		Logging.info(this, "retrieveData extendedView " + extendedView);
		LicensingInfoMap.setReduced(!extendedView);
		licenseMap = LicensingInfoMap.getInstance();

		columnNames = licenseMap.getColumnNames();
		classNames = licenseMap.getClassNames();
		theSourceMap = licenseMap.getTableMap();

		clientNumbers = licenseMap.getClientNumbersMap();
		clientTitle.setText("<html>" + Configed.getResourceValue("LicensingInfo.client.title") + "  ("
				+ persist.getHostInfoCollections().getConfigServer() + ") </html>");
		allClient.setText(Configed.getResourceValue("LicensingInfo.client.all_clients") + ": ");
		allClientNum.setText(clientNumbers.get(LicensingInfoMap.ALL).toString());
		macos.setText(Configed.getResourceValue("LicensingInfo.client.macos_clients") + ": ");
		macosNum.setText(clientNumbers.get(LicensingInfoMap.MAC_OS).toString());
		linux.setText(Configed.getResourceValue("LicensingInfo.client.linux_clients") + ": ");
		linuxNum.setText(clientNumbers.get(LicensingInfoMap.LINUX).toString());
		windows.setText(Configed.getResourceValue("LicensingInfo.client.windows_clients") + ": ");
		windowsNum.setText(clientNumbers.get(LicensingInfoMap.WINDOWS).toString());
		checksumTitle.setText(Configed.getResourceValue("LicensingInfo.client.checksum"));
		checksum.setText(licenseMap.getCheckSum());
		checksumInfo.setText("<html>" + Configed.getResourceValue("LicensingInfo.client.checksum.info") + "</html>");

		customerTitle.setText(Configed.getResourceValue("LicensingInfo.customer.data"));
		Set customerSet = licenseMap.getCustomerNamesSet();
		customerNames
				.setText(customerSet.toString().replace("[", "<html>").replace(", ", "<br>").replace("]", "</html>"));
	}

	protected PanelGenEditTable initMainPanel() {

		retrieveData();

		thePanel = new LicensingInfoPanelGenEditTable("", // title
				-1, // don't use a definite max table width
				false, // editing
				0, // generalPopupPosition
				true, // switchLineColors

				new int[] { PanelGenEditTable.POPUP_PRINT, PanelGenEditTable.POPUP_PDF,
						PanelGenEditTable.POPUP_SORT_AGAIN, PanelGenEditTable.POPUP_EXPORT_CSV,
						PanelGenEditTable.POPUP_EXPORT_SELECTED_CSV, PanelGenEditTable.POPUP_RELOAD }

				, false // with tableSearchPane

		) {

			@Override
			public void reload() {

				Logging.info(this,
						" LicInfoPanelGenTable reload, reduced " + !FGeneralDialogLicensingInfo.extendedView);
				persist.configOptionsRequestRefresh();
				persist.opsiLicensingInfoRequestRefresh();
				LicensingInfoMap.requestRefresh();
				licenseMap = LicensingInfoMap.getInstance(persist.getOpsiLicensingInfo(),
						persist.getConfigDefaultValues(), !FGeneralDialogLicensingInfo.extendedView);
				retrieveData();

				tableSource = new MapSource(columnNames, classNames, theSourceMap, false);

				buildModel();

				super.reload();

			}
		};

		thePanel.setMarkBoldHeaderCellRenderer();

		tableSource = new MapSource(columnNames, classNames, theSourceMap, false);

		buildModel();

		thePanel.setTitle("opsi Modules Validation");
		thePanel.setSize(500, 200);

		thePanel.setUpdateController(null);

		thePanel.getTheTable().setRowSorter(null);

		thePanel.getColumnModel().getColumn(0).setPreferredWidth(150);
		thePanel.getColumnModel().getColumn(1).setPreferredWidth(60);

		return thePanel;
	}

	protected JPanel initClientInfo() {

		retrieveData();

		JLabel orangeWarningLabel = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning") + "</html>");
		orangeWarningLabel.setIcon(Globals.createImageIcon("images/warning_orange.png", ""));

		JLabel redWarningLabel = new JLabel(
				"<html>" + Configed.getResourceValue("LicensingInfo.warning.over_limit") + "</html>");
		redWarningLabel.setIcon(Globals.createImageIcon("images/warning_red.png", ""));

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
				+ persist.getHostInfoCollections().getConfigServer() + ") </html>");
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
		Set customerSet = licenseMap.getCustomerNamesSet();
		JLabel customerNames = new JLabel(
				customerSet.toString().replace("[", "<html>").replace(", ", "<br>").replace("]", "</html>"));

		clientTitle.setFont(Globals.defaultFontBold);
		checksumTitle.setFont(Globals.defaultFontBold);
		checksumTitle.setToolTipText(Configed.getResourceValue("LicensingInfo.client.checksum.info"));
		checksum.setFont(Globals.defaultFontBold);
		checksum.setToolTipText(Configed.getResourceValue("LicensingInfo.client.checksum.info"));
		customerTitle.setFont(Globals.defaultFontBold);

		JLabel labelExtendedView = new JLabel(Configed.getResourceValue("LicensingInfo.buttonExtendedView"));
		JCheckBox checkExtendedView = new JCheckBox(""

				, extendedView);

		checkExtendedView.addActionListener(actionEvent -> {
			extendedView = checkExtendedView.isSelected();
			Logging.info(this, "extendedView " + extendedView + ", i.e. reduced " + !extendedView);
			LicensingInfoMap.setReduced(!extendedView);
			LicensingInfoMap.requestRefresh();
			thePanel.reload();
		});

		IconAsButton buttonReload = new IconAsButton(Configed.getResourceValue("ClientSelectionDialog.buttonReload"),
				"images/reload_blue16.png", "images/reload_blue16.png", "images/reload_blue16.png",
				"images/reload_blue16.png");

		buttonReload.addActionListener(actionEvent -> {
			LicensingInfoMap.requestRefresh();
			thePanel.reload();
		});

		JComponent[] linedComponents = new JComponent[] { buttonReload, new JLabel("   "), checkExtendedView,
				labelExtendedView

		};

		JPanel extraInfoPanel = new PanelLinedComponents(linedComponents);
		extraInfoPanel.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		extraInfoPanel.setOpaque(true);

		JPanel panel = new JPanel();
		GroupLayout gLayout = new GroupLayout(panel);
		panel.setLayout(gLayout);

		gLayout.setAutoCreateGaps(true);
		gLayout.setAutoCreateContainerGaps(true);

		gLayout.setHorizontalGroup(
				// 1
				gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

						.addGroup(gLayout.createSequentialGroup().addComponent(redWarningLabel).addGap(20)
								.addComponent(orangeWarningLabel))
						.addGroup(gLayout
								.createSequentialGroup().addComponent(warningLevelAbsolute).addGap(15).addComponent(
										warningLevelPercent)
								.addGap(15).addComponent(warningLevelDays))
						.addGroup(gLayout.createSequentialGroup()
								.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

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
								.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(customerTitle)
										.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING)

												.addComponent(customerNames)

										)

								)));

		gLayout.setVerticalGroup(gLayout.createSequentialGroup()

				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(redWarningLabel)
						.addComponent(orangeWarningLabel))
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
								.addGroup(gLayout.createSequentialGroup().addComponent(checksum))

								.addGap(20)))
						.addGap(30).addGroup(gLayout.createSequentialGroup()
								.addGroup(gLayout.createSequentialGroup().addComponent(customerNames)

								))

				));

		JPanel xPanel = new JPanel();
		GroupLayout xLayout = new GroupLayout(xPanel);
		xPanel.setLayout(xLayout);
		xPanel.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		xPanel.setOpaque(true);

		xLayout.setHorizontalGroup(xLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(extraInfoPanel).addComponent(panel));

		xLayout.setVerticalGroup(xLayout.createSequentialGroup().addComponent(extraInfoPanel).addGap(Globals.VGAP_SIZE)
				.addComponent(panel));

		return xPanel;

	}

	private void buildModel() {

		TableUpdateCollection updateCollection = new TableUpdateCollection();

		theModel = new GenTableModel(null, // updateItemFactory,

				// tableProvider
				new de.uib.utilities.table.provider.DefaultTableProvider(tableSource),

				// keycol
				0,

				// final columns int array
				new int[] {},

				// table model listener
				thePanel,

				// TableUpdateCollection updates
				updateCollection);

		GenericTableUpdateItemFactory updateItemFactory = new GenericTableUpdateItemFactory(0);

		updateItemFactory.setSource(theModel);

		theModel.reset();

		columnNames = theModel.getColumnNames();
		classNames = theModel.getClassNames();

		updateItemFactory.setColumnNames(columnNames);
		updateItemFactory.setClassNames(classNames);

		thePanel.setTableModel(theModel);

	}

}
