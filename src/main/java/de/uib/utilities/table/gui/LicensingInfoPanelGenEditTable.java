package de.uib.utilities.table.gui;

import javax.swing.table.TableColumn;

import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable {

	public LicensingInfoPanelGenEditTable(String title, int maxTableWidth, boolean editing, int generalPopupPosition, // if -1 dont use a standard popup
			// if > 0 the popup is added later after installing another popup
			boolean switchLineColors, int[] popupsWanted, boolean withTablesearchPane) {
		super(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, popupsWanted, withTablesearchPane);

		// theTable.getTableHeader().setDefaultRenderer(new

	}

	@Override
	protected void setCellRenderers() {

		LicensingInfoMap lInfoMap = LicensingInfoMap.getInstance();
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			String name = tableModel.getColumnName(i);
			TableColumn col = theTable.getColumn(name);

			col.setCellRenderer(new LicensingInfoTableCellRenderer(lInfoMap));
		}
	}

	public void setMarkBoldHeaderCellRenderer() {
		theTable.getTableHeader().setDefaultRenderer(new MarkLatestDateBoldHeaderCellRenderer(
				theTable.getTableHeader().getDefaultRenderer(), LicensingInfoMap.getInstance()));
		theTable.getTableHeader().setReorderingAllowed(false);
	}
}
