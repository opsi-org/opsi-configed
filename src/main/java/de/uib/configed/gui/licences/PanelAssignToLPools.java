/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licences;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.ControlPanelAssignToLPools.SoftwareDirectionOfAssignment;
import de.uib.configed.ControlPanelAssignToLPools.SoftwareShowAllMeans;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelStateSwitch;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class PanelAssignToLPools extends MultiTablePanel implements ChangeListener {
	private static final int MIN_V_SIZE = 80;

	private JLabel fieldSelectedLicencePoolId;

	private JLabel fieldCountAssignedStatus;
	private JLabel fieldCountAssignedInEditing;

	private JLabel fieldCountAllWindowsSoftware;
	private JLabel fieldCountDisplayedWindowsSoftware;
	private JLabel fieldCountNotAssignedSoftware;

	private JButton buttonShowAssignedNotExisting;

	private PanelRegisteredSoftware panelRegisteredSoftware;
	private PanelGenEditTable panelLicencepools;
	private PanelGenEditTable panelProductId2LPool;

	private FGlobalSoftwareInfo fMissingSoftwareInfo;
	private FSoftwarename2LicencePool fSoftwarename2LicencePool;

	private PanelStateSwitch<Softwarename2LicencepoolRestriction> panelRadiobuttonsPreselectionForName2Pool;
	private JLabel labelSimilarEntriesExist;

	public PanelAssignToLPools(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.7);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();

		JPanel panelInfoWindowsSoftware = new JPanel();

		JPanel panelInfoConfigWindowsSoftware = new JPanel();

		JLabel titleWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool"));

		JLabel titleWindowsSoftware2 = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool.supplement"));

		JLabel labelSelectedLicencePoolId = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelSelectedLicencePoolId"));

		fieldSelectedLicencePoolId = new JLabel();
		fieldSelectedLicencePoolId.setPreferredSize(new Dimension(250, Globals.LINE_HEIGHT));

		JLabel labelCountAllWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAllWindowsSoftware"));

		fieldCountAllWindowsSoftware = new JLabel();
		fieldCountAllWindowsSoftware.setPreferredSize(Globals.SHORT_LABEL_DIMENSION);

		JLabel labelCountDisplayedWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountDisplayedWindowsSoftware"));

		fieldCountDisplayedWindowsSoftware = new JLabel();
		fieldCountDisplayedWindowsSoftware.setPreferredSize(Globals.SHORT_LABEL_DIMENSION);

		JLabel labelCountNotAssignedSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountNotAssignedSoftware"));

		fieldCountNotAssignedSoftware = new JLabel();
		fieldCountNotAssignedSoftware.setPreferredSize(Globals.SHORT_LABEL_DIMENSION);

		JLabel labelCountAssignedStatus = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedStatus"));

		fieldCountAssignedStatus = new JLabel();
		fieldCountAssignedStatus.setPreferredSize(Globals.SHORT_LABEL_DIMENSION);

		JLabel labelCountAssignedInEditing = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedInEditing"));

		fieldCountAssignedInEditing = new JLabel();
		fieldCountAssignedInEditing.setPreferredSize(Globals.SHORT_LABEL_DIMENSION);

		buttonShowAssignedNotExisting = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing"),
				Utils.createImageIcon("images/edit-table-delete-row-16x16.png", ""));

		buttonShowAssignedNotExisting
				.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing.tooltip"));

		buttonShowAssignedNotExisting.addActionListener((ActionEvent actionEvent) -> {
			fMissingSoftwareInfo.setLocationRelativeTo(ConfigedMain.getLicencesFrame());
			fMissingSoftwareInfo.setVisible(true);
		});

		JLabel labelSupplementSimilar = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries"));

		labelSupplementSimilar.setVisible(true);

		JButton buttonSupplementSimilar = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.button"),
				Utils.createImageIcon("images/edit-table-insert-row-under.png", ""));

		buttonSupplementSimilar.setToolTipText(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.tooltip"));

		buttonSupplementSimilar.addActionListener((ActionEvent e) -> buttonSupplementSimilarAction());

		labelSimilarEntriesExist = new JLabel();
		labelSimilarEntriesExist.setVisible(true);

		panelRadiobuttonsPreselectionForName2Pool = new PanelStateSwitch<>(null,
				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.SHOW_ALL_NAMES,
				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.values(),
				new String[] {
						Configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showAllSwNames"),
						Configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showOnlyNamesWithNotUniformAssignments"),
						Configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showOnlyNamesWithoutAssignments") },

				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.class, null) {
			@Override
			protected void notifyChangeListeners(ChangeEvent e) {
				fSoftwarename2LicencePool.setPreselectionForName2Pool(
						(FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction) this.getValue());
				super.notifyChangeListeners(e);
			}
		};

		panelRadiobuttonsPreselectionForName2Pool.addChangeListener(this);

		JPanel panelWorkNamebased = new JPanel();
		panelWorkNamebased.setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 3, true));
		GroupLayout layoutNamebased = new GroupLayout(panelWorkNamebased);
		panelWorkNamebased.setLayout(layoutNamebased);

		layoutNamebased.setVerticalGroup(layoutNamebased.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutNamebased.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(buttonSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(labelSimilarEntriesExist, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE));

		layoutNamebased
				.setHorizontalGroup(layoutNamebased.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(layoutNamebased.createSequentialGroup().addGap(Globals.GAP_SIZE)
								.addComponent(labelSupplementSimilar, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(buttonSupplementSimilar, Globals.BUTTON_WIDTH / 2,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
								.addComponent(labelSimilarEntriesExist, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE))
						.addGroup(
								layoutNamebased.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(panelRadiobuttonsPreselectionForName2Pool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.MIN_GAP_SIZE)));

		PanelStateSwitch<SoftwareDirectionOfAssignment> panelRadiobuttonsDirectionOfAssignment = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.title"),
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE,
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.values(),
				new String[] {
						Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE"),
						Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.SOFTWARE2POOL") },

				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.class,

				(Enum<SoftwareDirectionOfAssignment> val) -> {
					Logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller).setSoftwareDirectionOfAssignment(
							(ControlPanelAssignToLPools.SoftwareDirectionOfAssignment) val);
				});

		JPanel panelRadiobuttonsDirectionOfAssignmentX = new JPanel();

		GroupLayout layoutBorder = new GroupLayout(panelRadiobuttonsDirectionOfAssignmentX);
		panelRadiobuttonsDirectionOfAssignmentX.setLayout(layoutBorder);
		layoutBorder.setVerticalGroup(layoutBorder
				.createSequentialGroup().addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		layoutBorder.setHorizontalGroup(layoutBorder
				.createSequentialGroup().addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		GroupLayout layoutPanelInfo = new GroupLayout(panelInfoWindowsSoftware);
		panelInfoWindowsSoftware.setLayout(layoutPanelInfo);

		layoutPanelInfo.setHorizontalGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								titleWindowsSoftware, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								panelWorkNamebased, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelSelectedLicencePoolId, 0, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldSelectedLicencePoolId,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								titleWindowsSoftware2, 50, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								panelRadiobuttonsDirectionOfAssignmentX, 20, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))

				));

		layoutPanelInfo.setVerticalGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))

				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware2, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))

				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(panelWorkNamebased, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(panelRadiobuttonsDirectionOfAssignmentX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));

		PanelStateSwitch<SoftwareShowAllMeans> panelRadiobuttonsSoftwareselectionX = new PanelStateSwitch<>(null,
				SoftwareShowAllMeans.ALL, SoftwareShowAllMeans.values(),

				new String[] { Configed.getResourceValue("PanelAssignToLPools.radiobuttonALL"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_OR_ASSIGNED_TO_NOTHING"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_TO_NOTHING") },

				ControlPanelAssignToLPools.SoftwareShowAllMeans.class,

				(Enum<SoftwareShowAllMeans> val) -> {
					Logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller)
							.setSoftwareShowAllMeans((ControlPanelAssignToLPools.SoftwareShowAllMeans) val);
				});

		GroupLayout layoutPanelInfoConfig = new GroupLayout(panelInfoConfigWindowsSoftware);
		panelInfoConfigWindowsSoftware.setLayout(layoutPanelInfoConfig);

		int col0width = labelCountAssignedStatus.getPreferredSize().width;
		if (labelCountAllWindowsSoftware.getPreferredSize().width > col0width) {
			col0width = labelCountAllWindowsSoftware.getPreferredSize().width;
		}

		layoutPanelInfoConfig.setHorizontalGroup(layoutPanelInfoConfig.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutPanelInfoConfig
						.createParallelGroup(
								GroupLayout.Alignment.LEADING)
						.addGroup(
								layoutPanelInfoConfig.createSequentialGroup()
										.addComponent(labelCountAssignedStatus, col0width, col0width, col0width)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(fieldCountAssignedStatus, Globals.BUTTON_WIDTH / 3,
												Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(labelCountAssignedInEditing, 5, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(fieldCountAssignedInEditing, Globals.BUTTON_WIDTH / 3,
												Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(buttonShowAssignedNotExisting, Globals.BUTTON_WIDTH,
												Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
										.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))

						.addGroup(
								layoutPanelInfoConfig.createSequentialGroup()
										.addComponent(labelCountAllWindowsSoftware, col0width, col0width, col0width)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(fieldCountAllWindowsSoftware, Globals.BUTTON_WIDTH / 3,
												Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(labelCountDisplayedWindowsSoftware, 5, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(fieldCountDisplayedWindowsSoftware, Globals.BUTTON_WIDTH / 3,
												Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(labelCountNotAssignedSoftware, 5, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.MIN_GAP_SIZE)
										.addComponent(fieldCountNotAssignedSoftware, Globals.BUTTON_WIDTH / 3,
												Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
										.addGap(Globals.MIN_GAP_SIZE))

						.addComponent(panelRadiobuttonsSoftwareselectionX, 20, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

				));
		layoutPanelInfoConfig.setVerticalGroup(layoutPanelInfoConfig.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)

				.addGap(Globals.BUTTON_HEIGHT)
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelCountAllWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountAllWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(labelCountDisplayedWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountDisplayedWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(labelCountNotAssignedSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountNotAssignedSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))

				// to get the level of the components of the left side
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelCountAssignedStatus, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldCountAssignedStatus, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(labelCountAssignedInEditing, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldCountAssignedInEditing, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(buttonShowAssignedNotExisting, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)

				.addComponent(panelRadiobuttonsSoftwareselectionX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.MIN_GAP_SIZE));

		panelLicencepools = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicencepools"), true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelLicencepools.setResetFilterModeOnNewSearch(false);
		panelLicencepools.setMasterFrame(ConfigedMain.getLicencesFrame());

		panelProductId2LPool = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleProductId2LPool"), true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);

		panelProductId2LPool.setMasterFrame(ConfigedMain.getLicencesFrame());

		panelRegisteredSoftware = new PanelRegisteredSoftware((ControlPanelAssignToLPools) controller);
		panelRegisteredSoftware.setFiltering(true, false);
		panelRegisteredSoftware.setMasterFrame(ConfigedMain.getLicencesFrame());

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(panelLicencepools, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelProductId2LPool, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addComponent(panelLicencepools, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelProductId2LPool, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);

		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutBottomPane.createSequentialGroup()
						.addComponent(panelInfoWindowsSoftware, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addComponent(panelInfoConfigWindowsSoftware, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addComponent(panelRegisteredSoftware, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE));

		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(panelInfoWindowsSoftware).addComponent(panelInfoConfigWindowsSoftware))
				.addComponent(panelRegisteredSoftware, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.MIN_GAP_SIZE));

		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(splitPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(splitPane, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void buttonSupplementSimilarAction() {
		if (!fSoftwarename2LicencePool.isVisible()) {
			fSoftwarename2LicencePool.setLocationRelativeTo(ConfigedMain.getLicencesFrame());
		}

		Logging.info(this, "buttonSupplementSimilar actionPerformed, we have selected "
				+ panelRadiobuttonsPreselectionForName2Pool.getValue());
		fSoftwarename2LicencePool.setPreselectionForName2Pool(
				(FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction) panelRadiobuttonsPreselectionForName2Pool
						.getValue());

		fSoftwarename2LicencePool.setVisible(true);

		panelRegisteredSoftware.callName2Pool(panelRegisteredSoftware.getTableModel().getCursorRow());
	}

	public void setDisplaySimilarExist(boolean b) {
		Logging.info(this, "setDisplaySimilarExist " + b);
		if (b) {
			labelSimilarEntriesExist.setIcon(Utils.createImageIcon("images/checked_box_filled_i_14.png", ""));
			labelSimilarEntriesExist
					.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesExist"));
		} else {
			labelSimilarEntriesExist.setIcon(Utils.createImageIcon("images/checked_box_blue_empty_14.png", ""));
			labelSimilarEntriesExist.setToolTipText(
					Configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesDontExist"));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.info(this, " stateChanged " + e);
		Logging.info(this,
				" stateChanged modelSWnames filterinfo " + fSoftwarename2LicencePool.getModelSWnames().getFilterInfo());
		String resetToSWname = (String) panelRegisteredSoftware.getValueAt(panelRegisteredSoftware.getSelectedRow(),
				panelRegisteredSoftware.getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));
		Logging.info(this, " stateChanged modelSWnames swname  >>" + resetToSWname + "<<");
		fSoftwarename2LicencePool.getModelSWnames().requestReload();
		fSoftwarename2LicencePool.getModelSWnames().reset();
		if (fSoftwarename2LicencePool.getModelSWxLicencepool() == null) {
			return;
		}

		fSoftwarename2LicencePool.getModelSWxLicencepool().requestReload();
		fSoftwarename2LicencePool.getModelSWnames().reset();

		if (fSoftwarename2LicencePool.getModelSWnames().getRowCount() > 0) {
			fSoftwarename2LicencePool.getPanelSWnames().setSelectedRow(0);
			fSoftwarename2LicencePool.getPanelSWnames().moveToValue(resetToSWname, 0, true);
		}
	}

	public JLabel getFieldSelectedLicencePoolId() {
		return fieldSelectedLicencePoolId;
	}

	public JLabel getFieldCountAssignedStatus() {
		return fieldCountAssignedStatus;
	}

	public JLabel getFieldCountAssignedInEditing() {
		return fieldCountAssignedInEditing;
	}

	public JLabel getFieldCountAllWindowsSoftware() {
		return fieldCountAllWindowsSoftware;
	}

	public JLabel getFieldCountDisplayedWindowsSoftware() {
		return fieldCountDisplayedWindowsSoftware;
	}

	public JLabel getFieldCountNotAssignedSoftware() {
		return fieldCountNotAssignedSoftware;
	}

	public JButton getButtonShowAssignedNotExisting() {
		return buttonShowAssignedNotExisting;
	}

	public PanelRegisteredSoftware getPanelRegisteredSoftware() {
		return panelRegisteredSoftware;
	}

	public PanelGenEditTable getPanelLicencepools() {
		return panelLicencepools;
	}

	public PanelGenEditTable getPanelProductId2LPool() {
		return panelProductId2LPool;
	}

	public FGlobalSoftwareInfo getFMissingSoftwareInfo() {
		return fMissingSoftwareInfo;
	}

	public void setFMissingSoftwareInfo(FGlobalSoftwareInfo fMissingSoftwareInfo) {
		this.fMissingSoftwareInfo = fMissingSoftwareInfo;
	}

	public FSoftwarename2LicencePool getFSoftwarename2LicencePool() {
		return fSoftwarename2LicencePool;
	}

	public void setFSoftwarename2LicencePool(FSoftwarename2LicencePool fSoftwarename2LicencePool) {
		this.fSoftwarename2LicencePool = fSoftwarename2LicencePool;
	}
}
