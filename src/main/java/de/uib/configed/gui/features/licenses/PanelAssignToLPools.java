/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.licenses;

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
import de.uib.configed.gui.ControlPanelAssignToLPools.SoftwareDirectionOfAssignment;
import de.uib.configed.gui.ControlPanelAssignToLPools.SoftwareShowAllMeans;
import de.uib.configed.gui.GlobalSoftwareInfoDialog;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.Softwarename2LicensePoolDialog;
import de.uib.configed.gui.Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction;
import de.uib.configed.gui.share.swing.PanelStateSwitch;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

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
		splitPane.setResizeWeight(0.5);

		createLicenseTables();

		JPanel topPane = new JPanel(new MigLayout("insets 0, wrap 1", "[grow, fill]", "[grow][grow]"));
		topPane.add(panelLicensepools, "grow, push, hmin 0");
		topPane.add(panelProductId2LPool, "grow, push, hmin 0");

		JPanel bottomPane = new JPanel(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", wrap 2", "[grow, fill]",
				"[0:pref:pref, shp 100][0:0:max, grow, shp 110]"));

		bottomPane.add(createInfoWindowsSoftwarePanel(), "growx, pushx");
		bottomPane.add(createInfoConfigWindowsSoftwarePanel(), "growx, pushx, aligny top");

		bottomPane.add(panelRegisteredSoftware, "span 2, grow, push");

		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		setLayout(new MigLayout("insets " + Globals.MIN_GAP_SIZE, "[grow, fill]", "[grow, fill]"));
		add(splitPane, "grow, push");
	}

	private JPanel createInfoWindowsSoftwarePanel() {
		JPanel panel = new JPanel(new MigLayout("insets " + Globals.MIN_GAP_SIZE + ", wrap 1", "[grow, fill]", "[]0"));

		panel.add(label("PanelAssignToLPools.Licenses.SectiontitleWindowsSoftware2LPool"));

		JLabel labelSelectedLicensePoolId = label("PanelAssignToLPools.labelSelectedLicensePoolId");
		fieldSelectedLicensePoolId = new JLabel();
		panel.add(labelSelectedLicensePoolId, "split 2");
		panel.add(fieldSelectedLicensePoolId, "growx, wrap");

		panel.add(label("PanelAssignToLPools.Licenses.SectiontitleWindowsSoftware2LPool.supplement"));
		panel.add(createWorkNameBasedPanel(), "growx, push");
		panel.add(createDirectionOfAssignmentPanelPanel(), "growx, push");

		return panel;
	}

	private JPanel createWorkNameBasedPanel() {
		JPanel panel = new JPanel(new MigLayout("insets " + Globals.MIN_GAP_SIZE, "[grow, fill]", "[]0"));
		panel.setBorder(new LineBorder(UIManager.getColor("Component.borderColor"), 3, true));

		JLabel labelSupplementSimilar = label("PanelAssignToLPools.Licenses.supplementSimilarSWEntries");
		JButton buttonSupplementSimilar = button("PanelAssignToLPools.Licenses.supplementSimilarSWEntries.button",
				"PanelAssignToLPools.Licenses.supplementSimilarSWEntries.tooltip", this::buttonSupplementSimilarAction);

		jCheckBoxSimilarEntriesExist = new JCheckBox();
		jCheckBoxSimilarEntriesExist.setEnabled(false);

		panel.add(labelSupplementSimilar, "split 3");
		panel.add(buttonSupplementSimilar);
		panel.add(jCheckBoxSimilarEntriesExist, "gapleft push, wrap");

		panelRadiobuttonsPreselectionForName2Pool = createNamePreselectionPanel();
		panel.add(panelRadiobuttonsPreselectionForName2Pool, "span, growx, gaptop " + Globals.MIN_GAP_SIZE);

		return panel;
	}

	private PanelStateSwitch<Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction> createNamePreselectionPanel() {
		return new PanelStateSwitch<>(null,
				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.SHOW_ALL_NAMES,
				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.values(),
				new String[] { text("PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showAllSwNames"), text(
						"PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showOnlyNamesWithNotUniformAssignments"),
						text("PanelAssignToLPools.Licenses.supplementSimilarSWEntries.showOnlyNamesWithoutAssignments") },
				Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction.class, null) {
			@Override
			protected void notifyChangeListeners(ChangeEvent e) {
				fSoftwarename2LicensePool.setPreselectionForName2Pool(
						(Softwarename2LicensePoolDialog.Softwarename2LicensepoolRestriction) getValue());
				super.notifyChangeListeners(e);
			}
		};
	}

	private JPanel createDirectionOfAssignmentPanelPanel() {
		PanelStateSwitch<SoftwareDirectionOfAssignment> dirSwitch = createDirectionOfAssignmentPanel();
		JPanel panel = new JPanel(new MigLayout("insets " + Globals.MIN_GAP_SIZE, "[grow, fill]", ""));
		panel.add(dirSwitch, "growx, pushx");
		return panel;
	}

	private PanelStateSwitch<ControlPanelAssignToLPools.SoftwareDirectionOfAssignment> createDirectionOfAssignmentPanel() {
		return new PanelStateSwitch<>(
				Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.title"),
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE,
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.values(),
				new String[] { text("PanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE"),
						text("PanelAssignToLPools.SoftwareDirectionOfAssignment.SOFTWARE2POOL") },
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.class,
				val -> ((ControlPanelAssignToLPools) controller).setSoftwareDirectionOfAssignment(
						(ControlPanelAssignToLPools.SoftwareDirectionOfAssignment) val));
	}

	private JPanel createInfoConfigWindowsSoftwarePanel() {
		JPanel panel = new JPanel(new MigLayout("insets 0", "", "[]0"));
		fieldCountAllWindowsSoftware = new JLabel();
		fieldCountDisplayedWindowsSoftware = new JLabel();
		fieldCountNotAssignedSoftware = new JLabel();

		panel.add(label("PanelAssignToLPools.labelCountAllWindowsSoftware"),
				"split 6, gapright " + Globals.MIN_GAP_SIZE);
		panel.add(fieldCountAllWindowsSoftware, " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(label("PanelAssignToLPools.labelCountDisplayedWindowsSoftware"), " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(fieldCountDisplayedWindowsSoftware, " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(label("PanelAssignToLPools.labelCountNotAssignedSoftware"), " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(fieldCountNotAssignedSoftware, " gapright " + Globals.MIN_GAP_SIZE + ", wrap");

		fieldCountAssignedStatus = new JLabel();
		fieldCountAssignedInEditing = new JLabel();
		buttonShowAssignedNotExisting = button("PanelAssignToLPools.buttonAssignedButMissing",
				"PanelAssignToLPools.buttonAssignedButMissing.tooltip", () -> fMissingSoftwareInfo.show());

		panel.add(label("PanelAssignToLPools.labelCountAssignedStatus"), "split 5, gapright " + Globals.MIN_GAP_SIZE);
		panel.add(fieldCountAssignedStatus, " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(label("PanelAssignToLPools.labelCountAssignedInEditing"), " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(fieldCountAssignedInEditing, " gapright " + Globals.MIN_GAP_SIZE);
		panel.add(buttonShowAssignedNotExisting, " gapright " + Globals.MIN_GAP_SIZE + ", wrap");

		panel.add(createSoftwareSelectionPanel(), "gaptop " + Globals.GAP_SIZE);

		return panel;
	}

	private PanelStateSwitch<ControlPanelAssignToLPools.SoftwareShowAllMeans> createSoftwareSelectionPanel() {
		return new PanelStateSwitch<>(null, SoftwareShowAllMeans.ALL, SoftwareShowAllMeans.values(),
				new String[] { text("PanelAssignToLPools.radiobuttonALL"),
						text("PanelAssignToLPools.radiobuttonASSIGNED_OR_ASSIGNED_TO_NOTHING"),
						text("PanelAssignToLPools.radiobuttonASSIGNED_TO_NOTHING") },
				ControlPanelAssignToLPools.SoftwareShowAllMeans.class, val -> ((ControlPanelAssignToLPools) controller)
						.setSoftwareShowAllMeans((ControlPanelAssignToLPools.SoftwareShowAllMeans) val));
	}

	private void createLicenseTables() {
		panelLicensepools = new PanelGenEdit(text("ConfigedMain.Licenses.SectiontitleLicensepools"), true, 1,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelLicensepools.setFilterKey(FilterKey.LICENSE_POOL_POOLS_TABLE);

		panelProductId2LPool = new PanelGenEdit(text("ConfigedMain.Licenses.SectiontitleProductId2LPool"), true, 1,
				new int[] { PanelGenEditPopupManager.POPUP_DELETE_ROW, PopupMenuTrait.POPUP_SAVE,
						PanelGenEditPopupManager.POPUP_CANCEL, PopupMenuTrait.POPUP_RELOAD },
				true);
		panelProductId2LPool.setFilterKey(FilterKey.LICENSE_PRODUCT_ID_TO_LICENSE_POOL_TABLE);

		panelRegisteredSoftware = new PanelRegisteredSoftware((ControlPanelAssignToLPools) controller);
		panelRegisteredSoftware.setFilterKey(FilterKey.LICENSE_REGISTERED_SOFTWARE_TABLE);
		panelRegisteredSoftware.getTableSearchPane().setFiltering();
	}

	private JLabel label(String key) {
		return new JLabel(text(key));
	}

	private String text(String key) {
		return Configed.getResourceValue(key);
	}

	private JButton button(String key, String tooltipKey, Runnable action) {
		JButton btn = new JButton(Configed.getResourceValue(key));
		btn.setToolTipText(Configed.getResourceValue(tooltipKey));
		btn.addActionListener(e -> action.run());
		return btn;
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
