/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneMsg;
import de.uib.configed.share.logging.Logging;

public class TabbedLogPane extends AbstractConfigurationTab {
	// This is the index of "instlog" in LogTabComponent.LOG_TYPES
	private static final int DEFAULT_SELECTED_INDEX = 2;
	private LogTabComponent[] textPanes;
	private final List<String> logTypesList;

	private ConfigedMain configedMain;

	private JTabbedPane tabbedPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public TabbedLogPane(ConfigedMain configedMain) {
		super(false, true);
		this.configedMain = configedMain;

		tabbedPane = new JTabbedPane();
		super.setComponent(tabbedPane);

		// We want all the tabs to have equal width
		tabbedPane.putClientProperty("JTabbedPane.tabWidthMode", "equal");

		// We want a small gap on top, between the client tabs and the log tabs
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(Globals.MIN_GAP_SIZE, 0, 0, 0));

		logTypesList = List.of(LogTabComponent.LOG_TYPES);

		textPanes = new LogTabComponent[LogTabComponent.LOG_TYPES.length];

		for (int i = 0; i < LogTabComponent.LOG_TYPES.length; i++) {
			initLogTabComponent(i, Configed.getResourceValue("MainFrame.DefaultTextForLogfiles"));
		}

		// We need to set the index before adding the change listener
		// to avoid triggering the action on startup
		tabbedPane.setSelectedIndex(DEFAULT_SELECTED_INDEX);

		tabbedPane.addChangeListener(event -> updateContent());

		// We want to have no minimum size restrictions
		// So that users can reduce the size of the client configuration as much as they want
		super.setMinimumSize(new Dimension());
	}

	@Override
	protected void updateContent() {
		Logging.debug(this, "setLogPage");
		setDocument(LogTabComponent.LOG_TYPES[tabbedPane.getSelectedIndex()], false);
	}

	private void initLogTabComponent(int i, String defaultText) {
		LogTabComponent logTabComponent = new LogTabComponent(defaultText, getFocusTraversalKeysEnabled(),
				configedMain);
		logTabComponent.setLogFileType(LogTabComponent.LOG_TYPES[i]);
		textPanes[i] = logTabComponent;
		tabbedPane.addTab(LogTabComponent.LOG_TYPES[i], textPanes[i].initUI());
	}

	public void setDocument(String logtype, final boolean resetCaret) {
		String document = persistenceController.getDataServices().log
				.getLogfile(configedMain.getSelectedClients().get(0), logtype);
		Logging.info(this, "logTypes.length ", LogTabComponent.LOG_TYPES.length);

		int index = logTypesList.indexOf(logtype);
		Logging.info(this, "setDocument ", index, " document == null ", (document == null));
		if (index < 0 || index >= LogTabComponent.LOG_TYPES.length) {
			return;
		}

		if (document == null) {
			textPanes[index].dispatch(new LogPaneMsg.ParseLogRequested(document, resetCaret));
			textPanes[index].dispatch(new LogPaneMsg.ChangeTitle(""));
			return;
		}

		String selectedClient = configedMain.getSelectedClients().size() == 1 ? configedMain.getSelectedClients().get(0)
				: "";

		textPanes[index].dispatch(new LogPaneMsg.ChangeTitle(LogTabComponent.LOG_TYPES[index] + " " + selectedClient));
		textPanes[index].dispatch(new LogPaneMsg.ChangeInfo(selectedClient));
		textPanes[index].dispatch(new LogPaneMsg.ParseLogRequested(document));
	}
}
