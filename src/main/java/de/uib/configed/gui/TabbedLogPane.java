/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.logviewer.logpane.LogPaneMsg;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class TabbedLogPane extends JTabbedPane {
	private LogTabComponent[] textPanes;
	private String[] idents = Utils.getLogTypes();
	private final List<String> identsList;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public TabbedLogPane(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		// We want all the tabs to have equal width
		putClientProperty("JTabbedPane.tabWidthMode", "equal");

		// We want a small gap on top, between the client tabs and the log tabs
		super.setBorder(BorderFactory.createEmptyBorder(Globals.MIN_GAP_SIZE, 0, 0, 0));

		identsList = Arrays.asList(idents);

		textPanes = new LogTabComponent[idents.length];

		for (int i = 0; i < idents.length; i++) {
			initLogTabComponent(i, Configed.getResourceValue("MainFrame.DefaultTextForLogfiles"));
		}

		super.addChangeListener((ChangeEvent e) -> {
			Logging.debug(this, " new logfiles tabindex ", getSelectedIndex());

			String logtype = Utils.getLogType(getSelectedIndex());

			setDocument(logtype, false);
		});

		// We want to have no minimum size restrictions
		// So that users can reduce the size of the client configuration as much as they want
		super.setMinimumSize(new Dimension());
	}

	private void initLogTabComponent(int i, String defaultText) {
		LogTabComponent logTabComponent = new LogTabComponent(defaultText, getFocusTraversalKeysEnabled(),
				configedMain);
		logTabComponent.setLogFileType(idents[i]);
		textPanes[i] = logTabComponent;
		super.addTab(idents[i], textPanes[i].initUI());
	}

	public void setDocument(String logtype, final boolean resetCaret) {
		String document = persistenceController.getLogDataService().getLogfile(configedMain.getSelectedClients().get(0),
				logtype);
		Logging.info(this, "idents.length ", idents.length);

		int i = identsList.indexOf(logtype);
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

	public void setLogview(String logtype) {
		int i = Arrays.asList(Utils.getLogTypes()).indexOf(logtype);
		if (i < 0) {
			return;
		}

		setSelectedIndex(i);
	}
}
