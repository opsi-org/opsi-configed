/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.licenses;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ControlPanelAssignToLPools;
import de.uib.configed.gui.GlobalSoftwareInfoDialog;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.Softwarename2LicensePoolDialog;
import de.uib.configed.gui.ControlPanelAssignToLPools.SoftwareDirectionOfAssignment;
import de.uib.configed.gui.ControlPanelAssignToLPools.SoftwareShowAllMeans;
import de.uib.configed.gui.Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction;
import de.uib.configed.gui.share.swing.PanelStateSwitch;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.logging.Logging;

public class PanelAssignToLPools extends MultiTablePanel implements ChangeListener {
	private JLabel fieldSelectedLicensePoolId;

	private JLabel fieldCountAssignedStatus;
	private JLabel fieldCountAssignedInEditing;

	private JLabel fieldCountAllWindowsSoftware;
	private JLabel fieldCountDisplayedWindowsSoftware;
	private JLabel fieldCountNotAssignedSoftware;

	private JButton buttonShowAssignedNotExisting;

	private PanelRegisteredSoftware panelRegisteredSoftware;
	private PanelGenEdit panelLicensepools;
	private PanelGenEdit panelProductId2LPool;

	private GlobalSoftwareInfoDialog fMissingSoftwareInfo;
	private Softwarename2LicensePoolDialog fSoftwarename2LicensePool;

	private PanelStateSwitch<Softwarename2LicensepoolRestriction> panelRadiobuttonsPreselectionForName2Pool;
	private JCheckBox jCheckBoxSimilarEntriesExist;

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
				Configed.getResourceValue("PanelAssignToLPools.Licenses.SectiontitleWindowsSoftware2LPool"));

		JLabel titleWindowsSoftware2 = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licenses.SectiontitleWindowsSoftware2LPool.supplement"));

		JLabel labelSelectedLicensePoolId = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelSelectedLicensePoolId"));

		fieldSelectedLicensePoolId = new JLabel();

		JLabel labelCountAllWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAllWindowsSoftware"));

		fieldCountAllWindowsSoftware = new JLabel();

		JLabel labelCountDisplayedWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountDisplayedWindowsSoftware"));

		fieldCountDisplayedWindowsSoftware = new JLabel();

		JLabel labelCountNotAssignedSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountNotAssignedSoftware"));

		fieldCountNotAssignedSoftware = new JLabel();

		JLabel labelCountAssignedStatus = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedStatus"));

		fieldCountAssignedStatus = new JLabel();

		JLabel labelCountAssignedInEditing = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedInEditing"));

		fieldCountAssignedInEditing = new JLabel();

		buttonShowAssignedNotExisting = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing"));

		buttonShowAssignedNotExisting
				.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing.tooltip"));

		buttonShowAssignedNotExisting.addActionListener(actionEvent -> fMissingSoftwareInfo.show());

		JLabel labelSupplementSimilar = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licenses.supplementSimilarSWEntries"));

		labelSupplementSimilar.setVisible(true);

		JButton buttonSupplementSimilar = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.Licenses.supplementSimilarSWEntries.button"));

		buttonSupplementSimilar.setToolTipText(
				Configed.getResourceValue("PanelAssignToLPools.Licenses.supplementSimilarSWEntries.tooltip"));

		buttonSupplementSimilar.addActionListener(actionEvents -> buttonSupplementSimilarAction());

		jCheckBoxSimilarEntriesExist = new JCheckBox();
		jCheckBoxSimilarEntriesExist.setEnabled(false);

		panelRadiobuttonsPreselectionForName2Pool = new PanelStateSwitch<>(null,
				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.SHOW_ALL_NAMES,
				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.values(),
				new String[] {
						Configed.getResourceValue(
								"PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showAllSwNames"),
						Configed.getResourceValue(
								"PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showOnlyNamesWithNotUniformAssignments"),
						Configed.getResourceValue(
								"PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showOnlyNamesWithoutAssignments") },

				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.class, null) {
			@Override
			protected void notifyChangeListeners(ChangeEvent e) {
				fSoftwarename2LicensePool.setPreselectionForName2Pool(
						(Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction) this.getValue());
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
						.addComponent(labelSupplementSimilar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSupplementSimilar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxSimilarEntriesExist, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE));

		layoutNamebased
				.setHorizontalGroup(layoutNamebased.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(layoutNamebased.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelSupplementSimilar, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(buttonSupplementSimilar, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
								.addComponent(jCheckBoxSimilarEntriesExist, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE))
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
					Logging.info(this, " produced ", val);
					((ControlPanelAssignToLPools) controller).setSoftwareDirectionOfAssignment(
							(ControlPanelAssignToLPools.SoftwareDirectionOfAssignment) val);
				});

		JPanel panelRadiobuttonsDirectionOfAssignmentX = new JPanel();

		GroupLayout layoutBorder = new GroupLayout(panelRadiobuttonsDirectionOfAssignmentX);
		panelRadiobuttonsDirectionOfAssignmentX.setLayout(layoutBorder);
		layoutBorder
				.setVerticalGroup(
						layoutBorder.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(panelRadiobuttonsDirectionOfAssignment, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE));

		layoutBorder
				.setHorizontalGroup(
						layoutBorder.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(panelRadiobuttonsDirectionOfAssignment, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE));

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
								.addComponent(labelSelectedLicensePoolId, 0, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addGap(Globals.MIN_GAP_SIZE).addComponent(fieldSelectedLicensePoolId,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								titleWindowsSoftware2, 50, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE).addComponent(
								panelRadiobuttonsDirectionOfAssignmentX, 20, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))));

		layoutPanelInfo.setVerticalGroup(layoutPanelInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE))

				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelSelectedLicensePoolId, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldSelectedLicensePoolId, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE))

				.addComponent(panelWorkNamebased, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(panelRadiobuttonsDirectionOfAssignmentX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		PanelStateSwitch<SoftwareShowAllMeans> panelRadiobuttonsSoftwareselectionX = new PanelStateSwitch<>(null,
				SoftwareShowAllMeans.ALL, SoftwareShowAllMeans.values(),

				new String[] { Configed.getResourceValue("PanelAssignToLPools.radiobuttonALL"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_OR_ASSIGNED_TO_NOTHING"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_TO_NOTHING") },

				ControlPanelAssignToLPools.SoftwareShowAllMeans.class,

				(Enum<SoftwareShowAllMeans> val) -> {
					Logging.info(this, " produced ", val);
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
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutPanelInfoConfig.createSequentialGroup()
								.addComponent(labelCountAssignedStatus, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldCountAssignedStatus, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelCountAssignedInEditing, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldCountAssignedInEditing, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(buttonShowAssignedNotExisting, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE))

						.addGroup(layoutPanelInfoConfig.createSequentialGroup()
								.addComponent(labelCountAllWindowsSoftware, col0width, col0width, col0width)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldCountAllWindowsSoftware, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelCountDisplayedWindowsSoftware, 5, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldCountDisplayedWindowsSoftware, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelCountNotAssignedSoftware, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE)
								.addComponent(fieldCountNotAssignedSoftware, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE))

						.addComponent(panelRadiobuttonsSoftwareselectionX, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				));
		layoutPanelInfoConfig
				.setVerticalGroup(
						layoutPanelInfoConfig.createSequentialGroup()
								.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(labelCountAllWindowsSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldCountAllWindowsSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelCountDisplayedWindowsSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldCountDisplayedWindowsSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelCountNotAssignedSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldCountNotAssignedSoftware, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

								// to get the level of the components of the left side
								.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(labelCountAssignedStatus, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldCountAssignedStatus, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelCountAssignedInEditing, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldCountAssignedInEditing, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonShowAssignedNotExisting, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)

								.addComponent(panelRadiobuttonsSoftwareselectionX, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

								.addGap(Globals.GAP_SIZE));

		panelLicensepools = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleLicensepools"), true, 1,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelLicensepools.setFilterKey(FilterKey.LICENSE_POOL_POOLS_TABLE);

		panelProductId2LPool = new PanelGenEdit(
				Configed.getResourceValue("ConfigedMain.Licenses.SectiontitleProductId2LPool"), true, 1,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelProductId2LPool.setFilterKey(FilterKey.LICENSE_PRODUCT_ID_TO_LICENSE_POOL_TABLE);

		panelRegisteredSoftware = new PanelRegisteredSoftware((ControlPanelAssignToLPools) controller);
		panelRegisteredSoftware.setFilterKey(FilterKey.LICENSE_REGISTERED_SOFTWARE_TABLE);
		panelRegisteredSoftware.getTableSearchPane().setFiltering();

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(panelLicensepools, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelProductId2LPool, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

		layoutTopPane.setVerticalGroup(
				layoutTopPane.createSequentialGroup().addComponent(panelLicensepools, 0, 0, Short.MAX_VALUE)
						.addComponent(panelProductId2LPool, 0, 0, Short.MAX_VALUE));

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

		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup()
				.addGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(panelInfoWindowsSoftware, 0, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(panelInfoConfigWindowsSoftware, 0, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(panelRegisteredSoftware, 0, 0, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE));

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
		Logging.info(this, "buttonSupplementSimilar actionPerformed, we have selected ",
				panelRadiobuttonsPreselectionForName2Pool.getValue());
		fSoftwarename2LicensePool.setPreselectionForName2Pool(
				(Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction) panelRadiobuttonsPreselectionForName2Pool
						.getValue());

		fSoftwarename2LicensePool.show();

		panelRegisteredSoftware.callName2Pool(panelRegisteredSoftware.getTableModel().getCursorRow());
	}

	public void setDisplaySimilarExist(boolean b) {
		Logging.info(this, "setDisplaySimilarExist ", b);
		jCheckBoxSimilarEntriesExist.setSelected(b);
		if (b) {
			jCheckBoxSimilarEntriesExist
					.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.Licenses.similarSWEntriesExist"));
		} else {
			jCheckBoxSimilarEntriesExist.setToolTipText(
					Configed.getResourceValue("PanelAssignToLPools.Licenses.similarSWEntriesDontExist"));
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.info(this, " stateChanged ", e);
		Logging.info(this, " stateChanged modelSWnames filterinfo ",
				fSoftwarename2LicensePool.getModelSWnames().getFilterInfo());
		int selectedRow = panelRegisteredSoftware.getGenEditTable().getSelectedRow();
		int columnNameIndex = panelRegisteredSoftware.getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME);
		if (selectedRow == -1 || columnNameIndex == -1) {
			Logging.warning(this, selectedRow == -1 ? "No software is selected" : "SWAuditEntry name column not found");
			return;
		}
		String resetToSWname = (String) panelRegisteredSoftware.getValueAt(selectedRow, columnNameIndex);
		Logging.info(this, " stateChanged modelSWnames swname  >>", resetToSWname, "<<");
		fSoftwarename2LicensePool.getModelSWnames().requestReload();
		fSoftwarename2LicensePool.getModelSWnames().reset();
		if (fSoftwarename2LicensePool.getModelSWxLicensepool() == null) {
			return;
		}

		fSoftwarename2LicensePool.getModelSWxLicensepool().requestReload();
		fSoftwarename2LicensePool.getModelSWnames().reset();

		if (fSoftwarename2LicensePool.getModelSWnames().getRowCount() > 0) {
			fSoftwarename2LicensePool.getPanelSWnames().setSelectedRow(0);
			fSoftwarename2LicensePool.getPanelSWnames().moveToValue(resetToSWname, 0, true);
		}
	}

	public JLabel getFieldSelectedLicensePoolId() {
		return fieldSelectedLicensePoolId;
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

	public PanelGenEdit getPanelLicensepools() {
		return panelLicensepools;
	}

	public PanelGenEdit getPanelProductId2LPool() {
		return panelProductId2LPool;
	}

	public GlobalSoftwareInfoDialog getFMissingSoftwareInfo() {
		return fMissingSoftwareInfo;
	}

	public void setFMissingSoftwareInfo(GlobalSoftwareInfoDialog fMissingSoftwareInfo) {
		this.fMissingSoftwareInfo = fMissingSoftwareInfo;
	}

	public Softwarename2LicensePoolDialog getFSoftwarename2LicensePool() {
		return fSoftwarename2LicensePool;
	}

	public void setFSoftwarename2LicensePool(Softwarename2LicensePoolDialog fSoftwarename2LicensePool) {
		this.fSoftwarename2LicensePool = fSoftwarename2LicensePool;
	}
}
