/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.UIManager;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.share.Icons;
import de.uib.configed.share.logging.Logging;

public class DepotListCellRenderer extends DefaultListCellRenderer {
	Map<String, Map<String, Object>> extendedInfo;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ImageIcon configServerConnectedIcon = Icons.getSelectedIntellijIcon("circle_checkmark");
	private ImageIcon configServerDisconnectedIcon = Icons.getSelectedIntellijIcon("circle");
	private ImageIcon connectedIcon = Icons.getIntellijIcon("checkmark", Globals.OPSI_OK);

	public DepotListCellRenderer(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
		Logging.debug(this, "setInfo ", extendedInfo);
		this.extendedInfo = extendedInfo;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		String tooltipText = null;
		String depot = (String) value;

		if (extendedInfo != null && extendedInfo.get(depot) != null
				&& extendedInfo.get(depot).get("description") != null
				&& !("" + extendedInfo.get(depot).get("description")).isEmpty()) {
			tooltipText = extendedInfo.get(depot).get("description").toString();
		}

		setConnectionIcon(depot);

		if (!persistenceController.getDataServices().userRoles.hasDepotPermission(depot)) {
			setEnabled(false);
			setBackground(UIManager.getColor("List.background"));
			setForeground(UIManager.getColor("List.foreground"));
			setToolTipText("Depot " + depot + " " + Configed.getResourceValue("Permission.depot.not_accessible"));
		} else {
			setToolTipText(tooltipText);
		}

		return this;
	}

	private void setConnectionIcon(String depot) {
		if (configedMain.isHostConnected(depot)) {
			setIcon(connectedIcon);
		} else if (depot != null
				&& depot.equals(persistenceController.getDataServices().hostInfoCollections.getConfigServer())) {
			if (Messagebus.getInstance().isConnected()) {
				setIcon(configServerConnectedIcon);
			} else {
				setIcon(configServerDisconnectedIcon);
			}
		} else {
			BufferedImage emptyImage = new BufferedImage(connectedIcon.getIconWidth(), 1, BufferedImage.TYPE_INT_ARGB);
			setIcon(new ImageIcon(emptyImage));
		}
	}
}
