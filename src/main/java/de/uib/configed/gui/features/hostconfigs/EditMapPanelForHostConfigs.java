/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hostconfigs;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.dataservice.UserRolesConfigDataService;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.data.ListMerger;
import de.uib.configed.gui.share.datapanel.EditMapPanelX;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.ExporterToPDF;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class EditMapPanelForHostConfigs extends EditMapPanelX {
	private boolean includeAdditionalTooltipText;
	private JTree tree;

	public EditMapPanelForHostConfigs(boolean reloadable, JTree tree, boolean isServerConfig,
			boolean includeAdditionalTooltipText) {
		super(isServerConfig, !isServerConfig, reloadable);

		this.tree = tree;
		this.includeAdditionalTooltipText = includeAdditionalTooltipText;
	}

	private void reload() {
		ConfigedMain.getMainFrame().activateLoadingCursor();
		TreePath p = tree.getSelectionPath();
		int row = tree.getRowForPath(p);

		actor.reloadData();
		Logging.info(this, "reloaded, return to ", p);
		if (p != null) {
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}

		ConfigedMain.getMainFrame().deactivateLoadingCursor();
	}

	@Override
	protected JPopupMenu definePopup() {
		Logging.debug(this, " (EditMapPanelGrouped) definePopup ");
		JPopupMenu jPopupMenu = new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF },
				(MouseEvent event) -> {
					updatePopupMenu();
					return true;
				}, new JComponent[] { table, jScrollPane.getViewport() }) {
			@Override
			public void action(int p) {
				switch (p) {
				case PopupMenuTrait.POPUP_RELOAD:
					reload();
					break;

				case PopupMenuTrait.POPUP_SAVE:
					actor.saveData();
					break;
				case PopupMenuTrait.POPUP_PDF:
					createPDF();
					break;

				default:
					Logging.warning(this, "no case found for JPopupMenu in definePopup");
					break;
				}
			}
		};

		jPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// We don't override default behavior.
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// We don't override default behavior.
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				UserRolesConfigDataService userRolesConfigDataService = PersistenceControllerFactory
						.getPersistenceController().getUserRolesConfigDataService();
				boolean canSave = !userRolesConfigDataService.isGlobalReadOnly()
						|| userRolesConfigDataService.canEditOwnServerRole();
				Component saveComponent = jPopupMenu.getComponent(0);
				saveComponent.setEnabled(canSave);
			}
		});

		JMenuItem jPopupMenuCopyToClipBoard = new JMenuItem(
				Configed.getResourceValue("EditMapPanelGroupedForHostConfigs.copyPropertyToClipboard"));
		Icons.addIntellijIconToMenuItem(jPopupMenuCopyToClipBoard, "copy");
		jPopupMenuCopyToClipBoard.addActionListener(actionEvent -> copyPropertyToClipboard());
		jPopupMenu.add(jPopupMenuCopyToClipBoard);

		return jPopupMenu;
	}

	private void copyPropertyToClipboard() {
		int row = table.getSelectedRow();
		if (row == -1) {
			return;
		}

		String propertyName = (String) table.getValueAt(row, 0);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(propertyName), null);
	}

	/**
	 * We override this method since we need a different method to set Tooltip
	 * and Text for the cells in the table.
	 */
	@Override
	protected void prepareRendererForJTable(JComponent jComponent, JTable table, int row, int col) {
		addTooltip(jComponent, table, names.get(row), row);
		setText(jComponent, table, col, row);
	}

	private void addTooltip(JComponent jc, JTable table, String propertyName, int rowIndex) {
		jc.setToolTipText(Utils.createTooltipForPropertyName(propertyName, defaultsMap, descriptionsMap,
				includeAdditionalTooltipText ? getPropertyOrigin(propertyName) : null));

		// check equals with default

		Object defaultValue;

		jc.setFont(jc.getFont().deriveFont(Font.PLAIN));

		if (defaultsMap == null) {
			Logging.warning(this, "no default values available, defaultsMap is null");
		} else if ((defaultValue = defaultsMap.get(table.getValueAt(rowIndex, 0))) == null) {
			Logging.warning(this, "no default Value found");

			jc.setForeground(Globals.OPSI_ERROR);

			jc.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

			jc.setFont(jc.getFont().deriveFont(Font.BOLD));
		} else if (!defaultValue.equals(table.getValueAt(rowIndex, 1))
				|| (originalMap != null && originalMap.containsKey(propertyName))) {
			jc.setFont(jc.getFont().deriveFont(Font.BOLD));
		} else {
			// Do nothing, since it's defaultvalue
		}
	}

	private static void setText(JComponent jComponent, JTable table, int vColIndex, int rowIndex) {
		if (vColIndex == 1 && Utils.isKeyForSecretValue((String) table.getValueAt(rowIndex, 0))
				&& jComponent instanceof JLabel jLabel) {
			jLabel.setText(Globals.STARRED_STRING);
		}
	}

	private String getPropertyOrigin(String propertyName) {
		Map<String, ConfigOption> serverConfigs = PersistenceControllerFactory.getPersistenceController()
				.getConfigDataService().getConfigOptionsPD();

		if (serverConfigs != null && serverConfigs.containsKey(propertyName)
				&& !serverConfigs.get(propertyName).getDefaultValues().equals(defaultsMap.get(propertyName))) {
			return "depot";
		} else {
			return "server";
		}
	}

	private void createPDF() {
		String client = tree.getSelectionPath().getPathComponent(0).toString().trim();

		Logging.info(this, "create report");
		Map<String, String> metaData = new HashMap<>();
		metaData.put("header", Configed.getResourceValue("EditMapPanelGrouped.createPDF.title"));
		metaData.put("title", "Client: " + client);
		metaData.put("subject", "report of table");
		metaData.put("keywords", Configed.getResourceValue("EditMapPanelGrouped.createPDF.title") + " " + client);

		ExporterToPDF pdfExportTable = new ExporterToPDF(createJTableForPDF());
		pdfExportTable.setClient(client);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4();
		pdfExportTable.execute(null, false);
	}

	private JTable createJTableForPDF() {
		DefaultTableModel tableModel = new DefaultTableModel();
		JTable jTable = new JTable(tableModel);

		tableModel.addColumn(Configed.getResourceValue("EditMapPanel.ColumnHeaderName"));
		tableModel.addColumn(Configed.getResourceValue("EditMapPanel.ColumnHeaderValue"));

		List<String> keys = mapTableModel.getKeys();
		Logging.info(this, "createJTableForPDF keys ", keys);
		for (String key : keys) {
			String property = "";

			List<?> listelem = ListMerger.getMergedList((List<?>) mapTableModel.getData().get(key));
			if (!listelem.isEmpty()) {
				property = listelem.get(0).toString();
			}

			if (!key.contains("saved_search")) {
				tableModel.addRow(new Object[] { key, property });
			}
		}
		return jTable;
	}
}
