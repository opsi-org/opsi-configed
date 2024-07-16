/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hostconfigs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.guidata.ListMerger;
import de.uib.configed.type.ConfigOption;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.datapanel.EditMapPanelX;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.ExporterToPDF;
import de.uib.utils.table.gui.ColorTableCellRenderer;

public class EditMapPanelForHostConfigs extends EditMapPanelX {
	private boolean includeAdditionalTooltipText;
	private JTree tree;

	public EditMapPanelForHostConfigs(TableCellRenderer tableCellRenderer, boolean keylistExtendible,
			boolean keylistEditable, boolean reloadable, JTree tree, boolean includeAdditionalTooltipText) {
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable);

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
		return new PopupMenuTrait(
				new Integer[] { PopupMenuTrait.POPUP_SAVE, PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF }) {
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
	}

	@Override
	protected void buildPanel() {
		setLayout(new BorderLayout());

		table = new JTable(mapTableModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent jComponent && showToolTip) {
					addTooltip(jComponent, this, names.get(rowIndex), rowIndex);
					setText(jComponent, this, vColIndex, rowIndex);
				}
				return c;
			}
		};

		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseWheelListener(mouseWheelEvent -> reactToMouseWheelEvent(mouseWheelEvent.getWheelRotation()));

		jScrollPane = new JScrollPane(table);

		add(jScrollPane, BorderLayout.CENTER);
	}

	private void addTooltip(JComponent jc, JTable table, String propertyName, int rowIndex) {
		jc.setToolTipText("<html>" + createTooltipForPropertyName(propertyName) + "</html>");

		// check equals with default

		Object defaultValue;

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
		if (vColIndex == 1 && Utils.isKeyForSecretValue((String) table.getValueAt(rowIndex, 0))) {
			if (jComponent instanceof JLabel jLabel) {
				jLabel.setText(Globals.STARRED_STRING);
			} else if (jComponent instanceof JTextComponent jTextComponent) {
				jTextComponent.setText(Globals.STARRED_STRING);
			} else {
				// Do nothing
			}
		}
	}

	private String createTooltipForPropertyName(String propertyName) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {
			if (includeAdditionalTooltipText) {
				tooltip.append("default (" + getPropertyOrigin(propertyName) + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (Utils.isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append("<br/><br/>" + descriptionsMap.get(propertyName));
		}

		return tooltip.toString();
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
