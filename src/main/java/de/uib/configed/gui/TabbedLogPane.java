/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneMsg;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class TabbedLogPane extends AbstractClientConfigurationTab {
	private LogTabComponent[] textPanes;
	private String[] idents = Utils.getLogTypes();
	private final List<String> identsList;

	private Map<String, String> logfiles = new HashMap<>();

	private ConfigedMain configedMain;

	private JTabbedPane tabbedPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public TabbedLogPane(ConfigedMain configedMain) {
		super(Configed.getResourceValue("MainFrame.TabActiveForSingleClient"), false);
		this.configedMain = configedMain;

		tabbedPane = new JTabbedPane();
		super.setComponent(tabbedPane);

		// We want all the tabs to have equal width
		tabbedPane.putClientProperty("JTabbedPane.tabWidthMode", "equal");

		// We want a small gap on top, between the client tabs and the log tabs
		tabbedPane.setBorder(BorderFactory.createEmptyBorder(Globals.MIN_GAP_SIZE, 0, 0, 0));

		identsList = Arrays.asList(idents);

		textPanes = new LogTabComponent[idents.length];

		for (int i = 0; i < idents.length; i++) {
			initLogTabComponent(i, Configed.getResourceValue("MainFrame.DefaultTextForLogfiles"));
		}

		tabbedPane.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex ", tabbedPane.getSelectedIndex());

			String logtype = Utils.getLogType(tabbedPane.getSelectedIndex());

			// logfile empty?
			if (!logfileExists(logtype)) {
				setDocuments(logtype);
			}
		});

		// We want to have no minimum size restrictions
		// So that users can reduce the size of the client configuration as much as they want
		super.setMinimumSize(new Dimension());
	}

	@Override
	protected void updateContent() {
		Logging.debug(this, "setLogPage");
		ConfigedMain.getMainFrame().getClientConfiguration().setLogFileTab("instlog", false);
		setLogview("instlog");
	}

	private void initLogTabComponent(int i, String defaultText) {
		LogTabComponent logTabComponent = new LogTabComponent(defaultText, getFocusTraversalKeysEnabled(),
				configedMain);
		logTabComponent.setLogFileType(idents[i]);
		textPanes[i] = logTabComponent;
		tabbedPane.addTab(idents[i], textPanes[i].initUI());
	}

	public void setDocuments(String logtype) {
		setDocuments(logtype, false);
	}

	public void setDocuments(String logtype, final boolean resetCaret) {
		Map<String, String> documents = getLogfilesUpdating(logtype);
		Logging.info(this, "idents.length ", idents.length);
		for (String ident : idents) {
			setDocument(ident, documents.get(ident), resetCaret);
		}
	}

	private void setDocument(String ident, final String document, final boolean resetCaret) {
		int i = identsList.indexOf(ident);
		Logging.info(this, "setDocument ", i, " document == null ", (document == null));
		if (i < 0 || i >= idents.length) {
			return;
		}

		if (document == null) {
			textPanes[i].dispatch(new LogPaneMsg.ParseLogRequested(document, resetCaret));
			textPanes[i].dispatch(new LogPaneMsg.ChangeTitle(""));
			return;
		}

		String selectedClient = configedMain.getSelectedClients().size() == 1 ? configedMain.getSelectedClients().get(0)
				: "";

		textPanes[i].dispatch(new LogPaneMsg.ChangeTitle(idents[i] + " " + selectedClient));
		textPanes[i].dispatch(new LogPaneMsg.ChangeInfo(selectedClient));
		textPanes[i].dispatch(new LogPaneMsg.ParseLogRequested(document));
	}

	private boolean logfileExists(String logtype) {
		return !logfiles.get(logtype).isEmpty();
	}

	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate) {
		Logging.info(this, "getLogfilesUpdating ", logtypeToUpdate);

		Logging.debug(this, "log pages set");

		logfiles = persistenceController.getLogDataService().getLogfile(configedMain.getSelectedClients().get(0),
				logtypeToUpdate);
		Logging.debug(this, "log pages set");

		return logfiles;
	}

	public void setLogview(String logtype) {
		int i = Arrays.asList(Utils.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			return;
		}

		tabbedPane.setSelectedIndex(i);
	}
}
