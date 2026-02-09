/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.licenses;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.core.domain.serverdata.CacheIdentifier;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.AbstractControlMultiTablePanel;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.ControlPanelAssignToLPools;
import de.uib.configed.gui.ControlPanelEditLicenses;
import de.uib.configed.gui.ControlPanelEnterLicense;
import de.uib.configed.gui.ControlPanelLicensesReconciliation;
import de.uib.configed.gui.ControlPanelLicensesStatistics;
import de.uib.configed.gui.ControlPanelLicensesUsage;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.type.licenses.LicenseEntry;
import de.uib.configed.share.AbstractDataChangedKeeper;
import de.uib.configed.share.logging.Logging;

public class LicenseManagement extends JTabbedPane implements ChangeListener {
	public enum LicensesTabStatus {
		LICENSEPOOL, ENTER_LICENSE, EDIT_LICENSE, USAGE, RECONCILIATION, STATISTICS
	}

	private List<LicensesTabStatus> tabOrder;

	private Map<LicensesTabStatus, MultiTablePanel> licensesPanels = new EnumMap<>(LicensesTabStatus.class);
	private LicensesTabStatus licensesStatus;

	private Map<LicensesTabStatus, String> licensesPanelsTabNames = new EnumMap<>(LicensesTabStatus.class);

	private List<AbstractControlMultiTablePanel> allControlMultiTablePanels;

	// global table providers for license management
	private DefaultTableProvider licensePoolTableProvider;
	private DefaultTableProvider licenseOptionsTableProvider;
	private DefaultTableProvider licenseContractsTableProvider;
	private DefaultTableProvider softwarelicensesTableProvider;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public LicenseManagement(ConfigedMain configedMain) {
		super(SwingConstants.TOP);

		this.configedMain = configedMain;

		allControlMultiTablePanels = new ArrayList<>();
		tabOrder = new ArrayList<>();

		initTableData();
		initTabs();

		super.addChangeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		int newVisualIndex = getSelectedIndex();

		LicensesTabStatus newStatus = tabOrder.get(newVisualIndex);

		// report state change request to controller and look, what it produces
		licensesStatus = reactToStateChangeRequest(newStatus);

		// if the controller did not accept the new index set it back
		// observe that we get a recursion since we initiate another state change
		// the recursion breaks since newVisualIndex is identical with
		// the old and does not yield a different value
		if (newStatus != licensesStatus) {
			super.removeChangeListener(this);
			setSelectedIndex(tabOrder.indexOf(licensesStatus));
			super.addChangeListener(this);
		}
	}

	private void initTableData() {
		licensesStatus = LicensesTabStatus.LICENSEPOOL;

		// global table providers
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");

		licensePoolTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, ReloadEvent.LICENSE_POOL_DATA_RELOAD,
						persistenceController.getDataServices().license::getLicensePoolsPD));

		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");

		licenseOptionsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, ReloadEvent.SOFTWARE_LICENSE_TO_LICENSE_POOL_DATA_RELOAD,
						persistenceController.getDataServices().license::getRelationsSoftwareL2LPool));

		columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");

		licenseContractsTableProvider = new DefaultTableProvider(
				new RetrieverMapSource(columnNames, ReloadEvent.LICENSE_CONTRACT_DATA_RELOAD,
						persistenceController.getDataServices().license::getLicenseContractsPD));

		columnNames = new ArrayList<>();
		columnNames.add(LicenseEntry.ID_KEY);
		columnNames.add(LicenseEntry.LICENSE_CONTRACT_ID_KEY);
		columnNames.add(LicenseEntry.TYPE_KEY);
		columnNames.add(LicenseEntry.MAX_INSTALLATIONS_KEY);
		columnNames.add(LicenseEntry.BOUND_TO_HOST_KEY);
		columnNames.add(LicenseEntry.EXPIRATION_DATE_KEY);

		softwarelicensesTableProvider = new DefaultTableProvider(new RetrieverMapSource(columnNames,
				CacheIdentifier.LICENSES, persistenceController.getDataServices().license::getLicensesPD));
	}

	private void initTabs() {
		// We load the data here because it's parallelized and more efficient
		persistenceController.reloadData(ReloadEvent.LICENSE_DATA_RELOAD.toString());

		// panelAssignToLPools
		licensesPanelsTabNames.put(LicensesTabStatus.LICENSEPOOL,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicensepools"));
		ControlPanelAssignToLPools controlPanelAssignToLPools = new ControlPanelAssignToLPools(this);
		addTab(LicensesTabStatus.LICENSEPOOL, controlPanelAssignToLPools.getTabClient());
		allControlMultiTablePanels.add(controlPanelAssignToLPools);

		// panelEnterLicense
		licensesPanelsTabNames.put(LicensesTabStatus.ENTER_LICENSE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabNewLicense"));
		ControlPanelEnterLicense controlPanelEnterLicense = new ControlPanelEnterLicense(configedMain, this);
		addTab(LicensesTabStatus.ENTER_LICENSE, controlPanelEnterLicense.getTabClient());
		setEnabledAt(getTabCount() - 1, !persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		allControlMultiTablePanels.add(controlPanelEnterLicense);

		// panelEditLicense
		licensesPanelsTabNames.put(LicensesTabStatus.EDIT_LICENSE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabEditLicense"));
		ControlPanelEditLicenses controlPanelEditLicenses = new ControlPanelEditLicenses(configedMain, this);
		addTab(LicensesTabStatus.EDIT_LICENSE, controlPanelEditLicenses.getTabClient());
		setEnabledAt(getTabCount() - 1, !persistenceController.getDataServices().userRoles.isGlobalReadOnly());
		allControlMultiTablePanels.add(controlPanelEditLicenses);

		// panelUsage
		licensesPanelsTabNames.put(LicensesTabStatus.USAGE,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicenseUsage"));
		ControlPanelLicensesUsage controlPanelLicensesUsage = new ControlPanelLicensesUsage(configedMain, this);
		addTab(LicensesTabStatus.USAGE, controlPanelLicensesUsage.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesUsage);

		// panelReconciliation
		licensesPanelsTabNames.put(LicensesTabStatus.RECONCILIATION,
				Configed.getResourceValue("ConfigedMain.Licenses.TabLicenseReconciliation"));
		ControlPanelLicensesReconciliation controlPanelLicensesReconciliation = new ControlPanelLicensesReconciliation();
		addTab(LicensesTabStatus.RECONCILIATION, controlPanelLicensesReconciliation.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesReconciliation);

		// panelStatistics
		licensesPanelsTabNames.put(LicensesTabStatus.STATISTICS,
				Configed.getResourceValue("ConfigedMain.Licenses.TabStatistics"));
		ControlPanelLicensesStatistics controlPanelLicensesStatistics = new ControlPanelLicensesStatistics();
		addTab(LicensesTabStatus.STATISTICS, controlPanelLicensesStatistics.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicensesStatistics);
	}

	private LicensesTabStatus reactToStateChangeRequest(LicensesTabStatus newState) {
		Logging.debug(this, "reactToStateChangeRequest( newState: ", newState, "), current state ", licensesStatus);

		return switch (licensesPanels.get(licensesStatus).mayLeave()) {
		case JOptionPane.YES_OPTION -> {
			licensesPanels.get(licensesStatus).saveSettings();
			yield newState;
		}
		case JOptionPane.NO_OPTION -> {
			Logging.debug(this, "reactToStateChangeRequest: mayLeave returned false");
			licensesPanels.get(licensesStatus).reset();
			yield newState;
		}
		case AbstractDataChangedKeeper.JOPTIONPANE_DIALOG_NOT_SHOWN -> {
			Logging.debug(this, "reactToStateChangeRequest: mayLeave returned no dialog shown");
			// We want to load the data in the new panel
			licensesPanels.get(newState).load();
			yield newState;
		}
		case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION -> {
			Logging.debug(this, "reactToStateChangeRequest: mayLeave returned cancel");
			// stay in the old state
			yield licensesStatus;
		}
		default -> {
			Logging.debug(this, "reactToStateChangeRequest: mayLeave returned unknown value");
			yield newState;
		}
		};
	}

	public boolean checkSavedLicensesPane() {
		boolean change = false;
		boolean result = false;
		Iterator<AbstractControlMultiTablePanel> iter = allControlMultiTablePanels.iterator();
		while (!change && iter.hasNext()) {
			AbstractControlMultiTablePanel cmtp = iter.next();
			Iterator<PanelGenEdit> iterP = cmtp.getPanelGenEdits().iterator();
			while (!change && iterP.hasNext()) {
				PanelGenEdit p = iterP.next();
				change = p.isDataChanged();
			}
		}

		if (change) {
			int returnedOption = JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.AllowLeaveApp"),
					Configed.getResourceValue("ConfigedMain.Licenses.AllowLeaveApp.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (returnedOption == JOptionPane.YES_OPTION) {
				result = true;
			}
		} else {
			result = true;
		}

		return result;
	}

	public void addTab(LicensesTabStatus status, MultiTablePanel panel) {
		licensesPanels.put(status, panel);
		tabOrder.add(status);
		addTab(licensesPanelsTabNames.get(status), panel);
	}

	public DefaultTableProvider getLicensePoolTableProvider() {
		return licensePoolTableProvider;
	}

	public DefaultTableProvider getLicenseOptionsTableProvider() {
		return licenseOptionsTableProvider;
	}

	public DefaultTableProvider getLicenseContractsTableProvider() {
		return licenseContractsTableProvider;
	}

	public DefaultTableProvider getSoftwarelicensesTableProvider() {
		return softwarelicensesTableProvider;
	}
}
