package de.uib.configed.gui;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;

public class DepotsList extends JList<String> implements ComponentListener {

	private MyListCellRenderer myListCellRenderer;
	private List<String> saveV;

	private Map<String, Map<String, Object>> depotInfo;

	public DepotsList(AbstractPersistenceController persist) {
		if (!ConfigedMain.THEMES) {
			super.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			super.setSelectionBackground(Globals.defaultTableCellSelectedBgColor);
			super.setSelectionForeground(Globals.DEPOTS_LIST_FOREGROUND_COLOR);
		}

		super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		myListCellRenderer = new MyListCellRenderer(persist);
		super.setCellRenderer(myListCellRenderer);
	}

	public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
		myListCellRenderer.setInfo(extendedInfo);
		this.depotInfo = extendedInfo;
	}

	public Map<String, Map<String, Object>> getDepotInfo() {
		return depotInfo;
	}

	public void setListData(List<String> v) {
		super.setListData(v.toArray(new String[0]));
		saveV = v;
	}

	public List<String> getListData() {
		return saveV;
	}

	// interface ComponentListene
	@Override
	public void componentHidden(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentMoved(ComponentEvent e) {
		/* Not needed */}

	@Override
	public void componentResized(ComponentEvent e) {
		ensureIndexIsVisible(getSelectedIndex());
	}

	@Override
	public void componentShown(ComponentEvent e) {
		ensureIndexIsVisible(getSelectedIndex());
	}
	// ===

	private int getIndexOf(Object value) {
		return saveV.indexOf(value);
	}

	public void selectAll() {
		getSelectionModel().setValueIsAdjusting(true);
		setSelectionInterval(0, getModel().getSize() - 1);
		getSelectionModel().setValueIsAdjusting(false);
	}

	public void addToSelection(List<String> depots) {
		if (depots == null || depots.isEmpty()) {
			return;
		}

		getSelectionModel().setValueIsAdjusting(true);

		for (String depot : depots) {
			int i = getIndexOf(depot);
			if (i > -1) {
				addSelectionInterval(i, i);
			}
		}
		getSelectionModel().setValueIsAdjusting(false);
	}

	private static class MyListCellRenderer extends DefaultListCellRenderer {
		private static final int FILL_LENGTH = 30;

		Map<String, Map<String, Object>> extendedInfo;

		AbstractPersistenceController persist;

		public MyListCellRenderer(AbstractPersistenceController persist) {
			super();
			this.persist = persist;
		}

		public void setInfo(Map<String, Map<String, Object>> extendedInfo) {
			Logging.debug(this, "setInfo " + extendedInfo);
			this.extendedInfo = extendedInfo;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (!(c instanceof JComponent)) {
				return c;
			}

			JComponent jc = (JComponent) c;

			if (jc instanceof JLabel) {
				String tooltipText = null;

				String key = "";

				if (value != null) {
					key = "" + value;
				}

				if (extendedInfo != null && extendedInfo.get(key) != null
						&& extendedInfo.get(key).get("description") != null
						&& !("" + extendedInfo.get(key).get("description")).isEmpty()) {
					tooltipText = "" + extendedInfo.get(value).get("description");
				} else {
					tooltipText = key;
				}

				tooltipText = Globals.fillStringToLength(tooltipText + " ", FILL_LENGTH);

				String depot = (String) value;
				if (!persist.hasDepotPermission(depot)) {
					if (!ConfigedMain.THEMES) {
						((JLabel) jc).setBackground(Globals.BACKGROUND_COLOR_3);
					}

					((JLabel) jc).setToolTipText(
							"Depot " + depot + " " + Configed.getResourceValue("Permission.depot.not_accessible"));
				} else {
					((JLabel) jc).setToolTipText(tooltipText);
				}
			}

			return jc;
		}
	}
}
