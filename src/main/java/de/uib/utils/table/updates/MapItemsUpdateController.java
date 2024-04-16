/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.table.updates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uib.utils.logging.Logging;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.PanelGenEditTable;

public class MapItemsUpdateController implements UpdateController {
	private GenTableModel tablemodel;
	private PanelGenEditTable panel;
	private MapBasedUpdater updater;
	private List<MapBasedTableEditItem> updateCollection;

	public MapItemsUpdateController(PanelGenEditTable panel, GenTableModel model, MapBasedUpdater updater,
			List<MapBasedTableEditItem> updateCollection) {
		this.panel = panel;
		this.tablemodel = model;
		this.updater = updater;
		this.updateCollection = updateCollection;
	}

	@Override
	public boolean saveChanges() {
		Logging.debug(this, "saveChanges");

		// true until failure
		boolean success = true;

		List<MapBasedTableEditItem> successfullInsertsWithNewKeys = new ArrayList<>();

		Iterator<MapBasedTableEditItem> iter = updateCollection.iterator();

		String lastKeyValue = "";

		while (iter.hasNext() && success) {
			MapBasedTableEditItem updateItem = iter.next();

			Logging.debug(this, " handling updateItem " + updateItem);

			if (updateItem.getSource() == this.tablemodel) {
				if (updateItem.keyChanged()) {
					String result = updater.sendUpdate(updateItem.getRowAsMap());

					success = result != null;
					if (success && updateItem.keyChanged()) {
						successfullInsertsWithNewKeys.add(updateItem);

						lastKeyValue = result;
					}
				} else {
					success = updater.sendDelete(updateItem.getRowAsMap());
				}
			}
		}

		if (success) {
			// we remove the update items
			// (either all or no one)
			removeItemsWithSource(updateCollection.iterator(), tablemodel);

			Logging.info(this, " we start with the new data set, reload request  " + tablemodel.isReloadRequested());
			tablemodel.startWithCurrentData();
			tablemodel.reset();

			Logging.info(this, "saveChanges lastKeyValue " + lastKeyValue);
			panel.moveToKeyValue(lastKeyValue);
		} else if (!successfullInsertsWithNewKeys.isEmpty()) {
			// we have to remove all update items ...
			removeItemsWithSource(iter, tablemodel);

			// ... and look what we have in the database
			tablemodel.requestReload();
			tablemodel.reset();
			// we have valid data again, even if not the expected ones
			success = true;
		} else {
			Logging.checkErrorList(null);
		}

		return success;
	}

	private static void removeItemsWithSource(Iterator<MapBasedTableEditItem> iter, GenTableModel source) {
		while (iter.hasNext()) {
			if (iter.next().getSource() == source) {
				iter.remove();
				// this can be safely done according to an Iterator guarantee
			}
		}
	}

	@Override
	public boolean cancelChanges() {
		removeItemsWithSource(updateCollection.iterator(), tablemodel);

		tablemodel.invalidate();
		tablemodel.reset();

		return true;
	}
}
