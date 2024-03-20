/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.UIManager;

import org.jdesktop.swingx.icon.EmptyIcon;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class DepotListCellRenderer extends DefaultListCellRenderer {
	Map<String, Map<String, Object>> extendedInfo;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ImageIcon connectedIcon = Utils.createImageIcon("bootstrap/check_green.png", "");
	private Set<String> clientsConnectedByMessagebus = persistenceController.getHostDataService()
			.getMessagebusConnectedClients();

	public DepotListCellRenderer(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
		Logging.debug(this, "setInfo " + extendedInfo);
		this.extendedInfo = extendedInfo;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		String tooltipText = null;

		String key = "";

		if (value != null) {
			key = "" + value;
		}

		if (extendedInfo != null && extendedInfo.get(key) != null && extendedInfo.get(key).get("description") != null
				&& !("" + extendedInfo.get(key).get("description")).isEmpty()) {
			tooltipText = "" + extendedInfo.get(value).get("description");
		}

		if (ServerFacade.isOpsi43()) {
			if (configedMain.getConnectedClientsByMessagebus().contains(value)) {
				setIcon(connectedIcon);
			} else {
				setIcon(new EmptyIcon(connectedIcon.getIconWidth(), 0));
			}
		}

		String depot = (String) value;
		if (!persistenceController.getUserRolesConfigDataService().hasDepotPermission(depot)) {
			setEnabled(false);
			setBackground(UIManager.getColor("List.background"));
			setForeground(UIManager.getColor("List.foreground"));
			setToolTipText("Depot " + depot + " " + Configed.getResourceValue("Permission.depot.not_accessible"));
		} else {
			setToolTipText(tooltipText);
		}

		return this;
	}
}
