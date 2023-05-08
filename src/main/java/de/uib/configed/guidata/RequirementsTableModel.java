package de.uib.configed.guidata;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.utilities.logging.Logging;

public class RequirementsTableModel extends AbstractTableModel {

	protected static List<String> rowType;
	protected static int noOfRowTypes;

	Set<String> keySet;
	private Object[] keyArray;
	private final Object[] zeroArray = new Object[] {};

	private Map<String, String> requMap;
	private Map<String, String> requBeforeMap;
	private Map<String, String> requAfterMap;
	private Map<String, String> requDeinstallMap;

	private AbstractPersistenceController perCon;

	static {
		rowType = new ArrayList<>();
		rowType.add("KEYROW");
		rowType.add("SETUP REQUIREMENT");
		rowType.add("UNINSTALL REQUIREMENT");
		noOfRowTypes = rowType.size();

	}

	public RequirementsTableModel(AbstractPersistenceController persis) {
		Logging.info(this, "creating");
		perCon = persis;

		init();
	}

	private void retrieveRequirements(String depotId, String product) {
		// if depotId == null the depot representative is used
		requMap = perCon.getProductRequirements(depotId, product);
		requBeforeMap = perCon.getProductPreRequirements(depotId, product);
		requAfterMap = perCon.getProductPostRequirements(depotId, product);
		requDeinstallMap = perCon.getProductDeinstallRequirements(depotId, product);
	}

	private void init() {
		// we assume that the productId determines the requirements since we are on a
		// preselected depot
		setActualProduct(null, "");
	}

	public void setActualProduct(String depotId, String product) {

		keySet = null;
		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;
		keyArray = zeroArray;

		if (product != null && !product.trim().isEmpty()) {
			retrieveRequirements(depotId, product);

			keySet = new TreeSet<>();
			if (requMap != null && requMap.keySet() != null) {
				keySet.addAll(requMap.keySet());
			}
			if (requBeforeMap != null && requBeforeMap.keySet() != null) {
				keySet.addAll(requBeforeMap.keySet());
			}
			if (requAfterMap != null && requAfterMap.keySet() != null) {
				keySet.addAll(requAfterMap.keySet());
			}
			if (requDeinstallMap != null && requDeinstallMap.keySet() != null) {
				keySet.addAll(requDeinstallMap.keySet());
			}
			keyArray = keySet.toArray();

		}

		fireTableDataChanged();

	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return keyArray.length * noOfRowTypes;
	}

	@Override
	public String getColumnName(int col) {
		String result = "";
		switch (col) {
		case 0:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requiredProduct");
			break;

		case 1:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeDefault");
			break;
		case 2:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeBefore");
			break;
		case 3:
			result = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeAfter");
			break;
		default:
			Logging.warning(this, "no case found for col in getColumnName");
			break;
		}

		return result;

	}

	@Override
	public Object getValueAt(int row, int col) {

		String myKey = (String) keyArray[row / noOfRowTypes];

		int rowTypeIndex = row % noOfRowTypes;

		Object result = null;
		String indent = "     ";

		if (col == 0) {
			switch (rowTypeIndex) {

			case 0:
				result = myKey;
				break;
			case 1:
				result = indent + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " setup";
				break;
			case 2:
				result = indent + Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " uninstall";
				break;
			default:
				Logging.warning(this, "no case found for rowTypeIndex in getValueAt");
				break;
			}
		} else {
			switch (col) {
			case 1:
				if (rowTypeIndex == 1) {
					if (requMap != null) {
						result = requMap.get(myKey);
					}
				} else if (rowTypeIndex == 2 && requDeinstallMap != null) {
					result = requDeinstallMap.get(myKey);
				}
				break;

			case 2:
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requBeforeMap != null) {
					result = requBeforeMap.get(myKey);
				}
				break;

			case 3:
				// otherwise, result will remain null
				if (rowTypeIndex == 1 && requAfterMap != null) {
					result = requAfterMap.get(myKey);
				}
				break;

			default:
				Logging.warning(this, "no case found for col in getValueAt");
				break;
			}

			if (result != null) {
				result = "(" + result + ")";
			}

		}
		return result;
	}

	public MyTableCellRenderer getTableCellRenderer() {
		return new MyTableCellRenderer();
	}

	protected static class MyTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			MyColorizer colorizer = new MyColorizer(String.valueOf(value));

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			colorizer.colorize(cell, row, column);

			if (cell instanceof JComponent) {
				((JComponent) cell).setToolTipText("" + value);
			}

			// warning
			String cellValue = String.valueOf(value);
			if ((column == 2 || column == 3)
					&& (cellValue.equals("(" + InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED) + ":)")
							|| cellValue.equals("(:" + ActionRequest.getLabel(ActionRequest.UNINSTALL) + ")"))) {

				((JLabel) cell).setIcon(Globals.createImageIcon("images/warning.png", "warning"));

				((JLabel) cell).setHorizontalTextPosition(SwingConstants.LEADING);

				((JLabel) cell).setToolTipText(Configed.getResourceValue("ProductInfoPane.RequirementsTable.warning"));

				return cell;

			} else {
				((JLabel) cell).setIcon(null);
			}

			return cell;
		}
	}

	protected static class MyColorizer {
		String cellValue;

		protected MyColorizer(String value) {
			cellValue = value;
		}

		public void colorize(Component cell, int row, int col) {
			if (!ConfigedMain.THEMES) {
				cell.setForeground(Globals.lightBlack);
			}

			int kindOfRow = row % 3;

			if (!ConfigedMain.THEMES) {
				switch (kindOfRow) {
				case 0:
					cell.setBackground(Globals.BACKGROUND_COLOR_7);
					break;
				case 1:
					cell.setBackground(Globals.BACKGROUND_COLOR_8);
					break;
				case 2:
					cell.setBackground(Globals.BACKGROUND_COLOR_8);
					break;
				default:
					Logging.warning(this, "no case found for kindOfRow in colorize");
					break;
				}

				if (kindOfRow == 2 && col > 1) {
					cell.setBackground(Globals.BACKGROUND_COLOR_4);
				}
			}
		}
	}

	protected static class MyWarningColorizer {
		public void colorize(Component cell) {
			if (!ConfigedMain.THEMES) {
				cell.setBackground(Globals.ACTION_COLOR);
			}
		}
	}

	protected static class MyTableCellRendererWarning extends DefaultTableCellRenderer {
		MyWarningColorizer colorizer = new MyWarningColorizer();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			colorizer.colorize(cell);

			if (cell instanceof JComponent) {
				((JComponent) cell).setToolTipText("" + value);
			}

			return cell;
		}
	}

}
