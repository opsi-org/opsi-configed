/*
 * MapItemsUpdateController.java
 *
 * By uib, www.uib.de, 2009
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.thread.WaitCursor;

public class MapItemsUpdateController implements UpdateController {
	GenTableModel tablemodel;
	PanelGenEditTable panel;
	MapBasedUpdater updater;
	TableUpdateCollection updateCollection;

	public MapItemsUpdateController(PanelGenEditTable panel, GenTableModel model, MapBasedUpdater updater,
			TableUpdateCollection updateCollection) {
		this.panel = panel;
		this.tablemodel = model;
		this.updater = updater;
		this.updateCollection = updateCollection;
	}

	@Override
	public boolean saveChanges() {
		Logging.debug(this, "saveChanges");

		WaitCursor waitCursor = new WaitCursor();

		// true until failure
		boolean success = true;

		List<MapBasedTableEditItem> successfullInsertsWithNewKeys = new ArrayList<>();

		Iterator<TableEditItem> iter = updateCollection.iterator();

		String lastKeyValue = "";

		while (iter.hasNext() && success) {

			MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();

			Logging.debug(this, " handling updateItem " + updateItem);

			if (updateItem.getSource() == this.tablemodel) {

				if (updateItem instanceof MapDeliveryItem) {
					String result = updater.sendUpdate(updateItem.getRowAsMap());

					success = result != null;
					if (success && updateItem.keyChanged()) {
						successfullInsertsWithNewKeys.add(updateItem);

						lastKeyValue = result;
					}
				} else if (updateItem instanceof MapDeleteItem) {
					success = updater.sendDelete(updateItem.getRowAsMap());
				} else {
					Logging.error("update item type not supported");
					success = false;
				}
			}
		}

		if (success) {
			// we remove the update items
			// (either all or no one)
			iter = updateCollection.iterator();
			while (iter.hasNext()) {
				MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
				if (updateItem.getSource() == tablemodel) {
					iter.remove();
					// this can be safely done according to an Iterator guarantee
				}
			}

			Logging.info(this, " we start with the new data set, reload request  " + tablemodel.isReloadRequested());
			tablemodel.startWithCurrentData();
			tablemodel.reset();

			Logging.info(this, "saveChanges lastKeyValue " + lastKeyValue);
			panel.moveToKeyValue(lastKeyValue);

			waitCursor.stop();
		} else {
			if (!successfullInsertsWithNewKeys.isEmpty()) {
				// we have to remove all update items ...
				while (iter.hasNext()) {
					MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
					if (updateItem.getSource() == tablemodel) {
						iter.remove();
						// this can be safely done according to an Iterator guarantee
					}
				}

				// ... and look what we have in the database
				tablemodel.requestReload();
				tablemodel.reset();
				// we have valid data again, even if not the expected ones
				success = true;
			}

			waitCursor.stop();
			Logging.checkErrorList(null);

		}

		return success;
	}

	@Override
	public boolean cancelChanges() {

		Iterator<TableEditItem> iter = updateCollection.iterator();
		while (iter.hasNext()) {
			MapBasedTableEditItem updateItem = (MapBasedTableEditItem) iter.next();
			if (updateItem.getSource() == tablemodel) {
				iter.remove();
				// this can be safely done according to an Iterator guarantee
			}
		}

		tablemodel.invalidate();
		tablemodel.reset();

		return true;
	}
}
