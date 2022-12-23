/* 
 *
 * (c) uib, www.uib.de, 2009-2013, 2022
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.ListCellOptions;

public abstract class AbstractEditMapPanel extends JPanel {
	protected MapTableModel mapTableModel;

	protected boolean reloadable = false;
	protected boolean showToolTip = true;

	protected boolean keylistExtendible = false;
	protected boolean keylistEditable = true;

	protected boolean optionsEditable = true;

	protected Actor actor;

	protected JPopupMenu popupmenuAtRow;
	protected JPopupMenu popupEditOptions;
	protected JPopupMenu popupNoEditOptions;

	public static class Actor {
		protected void reloadData() {
			logging.info(this, "AbstractEditMapPanel: reloadData");
		}

		protected void saveData() {
			logging.info(this, "AbstractEditMapPanel: saveData");
		}

		protected void deleteData() {
			logging.info(this, "AbstractEditMapPanel: deleteData");
		}
	}

	protected abstract class PropertyHandler {
		MapTableModel mapTableModel;

		public void setMapTableModel(MapTableModel mapTableModel) {
			this.mapTableModel = mapTableModel;
		}

		public abstract void removeValue(String key);

		public String getRemovalMenuText() {
			String s = "";
			logging.debug(this, "getRemovalMenuText " + s);
			return s;
		}

		/*
		 * @Override
		 * public String toString()
		 * {
		 * return this.getClass().getName();
		 * }
		 */
	}

	public AbstractEditMapPanel() {
		actor = new Actor();
		mapTableModel = new MapTableModel();

	}

	public AbstractEditMapPanel(boolean keylistExtendible, boolean keylistEditable, boolean reloadable) {
		this();
		this.keylistExtendible = keylistExtendible;
		this.keylistEditable = keylistEditable;
		this.reloadable = reloadable;

	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}

	protected abstract void buildPanel();

	public abstract void resetDefaults();

	public abstract void setVoid();

	public MapTableModel getTableModel() {
		return mapTableModel;
	}

	public Vector<String> getNames() {
		return mapTableModel.getKeys();
	}

	public void setShowToolTip(boolean b) {
		showToolTip = b;
	}

	public void registerDataChangedObserver(DataChangedObserver o) {
		mapTableModel.registerDataChangedObserver(o);
	}

	/**
	 * set collection (e.g. of clients) where each member stores the changed
	 * data; we assume that it is a collection of maps
	 * 
	 * @param Collection data
	 */
	public void setStoreData(Collection data) {
		mapTableModel.setStoreData(data);
	}

	/**
	 * take a reference to a collection of maps that we will have to use for
	 * updating the data base
	 * 
	 * @param Collection updateCollection
	 */
	public void setUpdateCollection(Collection updateCollection) {
		mapTableModel.setUpdateCollection(updateCollection);
	}

	public void setReadOnlyEntries(java.util.Set<String> keys) {
		mapTableModel.setReadOnlyEntries(keys);
	}

	public void setShowOnlyValues(List<Object> showOnly) {
		mapTableModel.setShowOnlyValues(showOnly);
	}

	public void setEditDenier(java.util.function.Function<String, Boolean> disallow) {
		mapTableModel.setEditDenier(disallow);
	}

	protected void logPopupElements(JPopupMenu popup) {
		MenuElement[] popupElements = popupmenuAtRow.getSubElements();
		int size = popupElements.length;
		logging.debug(this, "logPopupElements " + size);
		/*
		 * for (int i = 0; i < size; i++)
		 * {
		 * logging.info(this, "logPopupElements " + ((JMenuItem)
		 * popupElements[i]).getText());
		 * }
		 */
	}

	/*
	 * private void removeOldPopupElements()
	 * {
	 * MenuElement[] popupElements = popupmenuAtRow.getSubElements();
	 * int size = popupElements.length;
	 * logging.info(this, "removeOldPopupElements " +size);
	 * for (int i = size-1; i >=0; i--)
	 * {
	 * logging.info(this, "removeOldPopupElements " + ((JMenuItem)
	 * popupElements[i]).getText());
	 * popupmenuAtRow.remove(i);
	 * }
	 * }
	 * 
	 * public void setPopupConfiguration(LinkedList<JMenuItem>menuItems)
	 * {
	 * logPopupElements();
	 * 
	 * 
	 * 
	 * logging.info(this, "setPopupConfiguration, set menuitems " +menuItems.size()
	 * );
	 * for (JMenuItem item : menuItems)
	 * {
	 * logging.info(this, "setPopupConfiguration, set " + item.getText());
	 * 
	 * }
	 * 
	 * }
	 * 
	 * public void setPopupConfiguration(JPopupMenu menu)
	 * {
	 * logPopupElements();
	 * 
	 * 
	 * 
	 * MenuElement[] menuElements = menu.getSubElements();
	 * logging.info(this, "setPopupConfiguration, set menu " +menuElements.length);
	 * for (int i = 0; i < menuElements.length; i++)
	 * {
	 * logging.info(this, "setPopupConfiguration, set menu " + ((JMenuItem)
	 * menuElements[i]).getText());
	 * 
	 * }
	 * 
	 * }
	 */

	public void setOptionsEditable(boolean b) {
		logging.debug(this, "AbstractEditMapPanel.setOptionsEditable " + b);

		if (b) {
			popupmenuAtRow = popupEditOptions;
		} else {
			popupmenuAtRow = popupNoEditOptions;
		}
	}

	/**
	 * setting all data for displaying and editing <br />
	 * 
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {
		logging.debug(this, "setEditableMap optionsMap == null? " + (optionsMap == null));
	}

	/**
	 * setting a label <br />
	 * 
	 * @param String s - label text
	 */
	public abstract void setLabel(String s);

	public abstract void init();
}
