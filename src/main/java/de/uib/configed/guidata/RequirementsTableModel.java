package de.uib.configed.guidata;

import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.productstate.InstallationStatus;
import de.uib.utilities.logging.logging;

public class RequirementsTableModel extends javax.swing.table.AbstractTableModel {

	final String initString = "";
	private String actualProduct = "";
	TreeSet keySet;
	Object[] keyArray;
	final Object[] zeroArray = new Object[] {};

	Map requMap;
	Map requBeforeMap;
	Map requAfterMap;
	Map requDeinstallMap;

	PersistenceController perCon;

	protected static Vector<String> rowType;
	protected static int noOfRowTypes;
	static {
		rowType = new Vector<>();
		rowType.add("KEYROW");
		rowType.add("SETUP REQUIREMENT");
		rowType.add("UNINSTALL REQUIREMENT");
		noOfRowTypes = rowType.size();

	}

	public RequirementsTableModel(PersistenceController persis) {
		logging.info(this, "creating");
		perCon = persis;
		setActualProduct(""); // initializing
	}

	private void retrieveRequirements(String depotId, String product) {
		// if depotId == null the depot representative is used
		requMap = perCon.getProductRequirements(depotId, product);
		requBeforeMap = perCon.getProductPreRequirements(depotId, product);
		requAfterMap = perCon.getProductPostRequirements(depotId, product);
		requDeinstallMap = perCon.getProductDeinstallRequirements(depotId, product);
	}

	public void setActualProduct(String product)
	// we assume that the productId determines the requirements since we are on a
	// preselected depot
	{
		setActualProduct(null, product);
	}

	public void setActualProduct(String depotId, String product) {
		this.actualProduct = product;

		keySet = null;
		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;
		keyArray = zeroArray;

		if (product != null && !product.trim().equals("")) {
			retrieveRequirements(depotId, product);

			keySet = new TreeSet<>();
			if (requMap != null && requMap.keySet() != null) {
				keySet.addAll(new TreeSet<>(requMap.keySet()));
			}
			if (requBeforeMap != null && requBeforeMap.keySet() != null) {
				keySet.addAll(new TreeSet<>(requBeforeMap.keySet()));
			}
			if (requAfterMap != null && requAfterMap.keySet() != null) {
				keySet.addAll(new TreeSet<>(requAfterMap.keySet()));
			}
			if (requDeinstallMap != null && requDeinstallMap.keySet() != null) {
				keySet.addAll(new TreeSet<>(requDeinstallMap.keySet()));
			}
			if (keySet != null) {
				keyArray = keySet.toArray();
			} else {
				keyArray = zeroArray;
			}
		}

		fireTableDataChanged();

	}

	@Override
	public int getColumnCount() {
		return 4;// remove uninstall 5;
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
			result = configed.getResourceValue("ProductInfoPane.RequirementsTable.requiredProduct");
			break;
		/*
		 * case 1 : result = "Benötigt (=on)"; break;
		 * case 2 : result = "Vorher benötigt"; break;
		 * case 3 : result = "Danach benötigt"; break;
		 * case 4 : result = "Bei deinstall"; break;
		 */
		case 1:
			result = configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeDefault");
			break;
		case 2:
			result = configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeBefore");
			break;
		case 3:
			result = configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementTypeAfter");
			break;
		// case 4 : result = "uninstall"; break;
		};

		return result;

	}

	@Override
	public Object getValueAt(int row, int col) {
		// logging.info(this, "getValueAt " + row + ", " + col);
		String myKey = (String) keyArray[row / noOfRowTypes];
		String col0Value = null;

		// logging.info(this, "getValueAt myKey " + myKey );

		int rowTypeIndex = row % noOfRowTypes;

		// logging.info(this, "getValueAt rowTypeIndex " + rowTypeIndex );

		Object result = null;
		String indent = "     ";
		String impossible = null;

		MyWarningColorizer warningColorizer = new MyWarningColorizer();

		if (col == 0) {
			switch (rowTypeIndex) {

			case 0:
				result = myKey;
				break;
			case 1:
				result = indent + configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " setup";
				break;
			case 2:
				result = indent + configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition")
						+ " uninstall";
				break;
			}
		}

		else {
			switch (col) {
			case 1:
				if (rowTypeIndex == 1) {
					if (requMap != null) {
						result = requMap.get(myKey);
						break;
					}
				} else if (rowTypeIndex == 2) {
					if (requDeinstallMap != null) {
						result = requDeinstallMap.get(myKey);
						break;
					}
				}

			case 2:
				if (rowTypeIndex == 1) {
					if (requBeforeMap != null) {
						result = requBeforeMap.get(myKey);
						break;
					}
				} else if (rowTypeIndex == 2)
					result = impossible;

			case 3:
				if (rowTypeIndex == 1) {
					if (requAfterMap != null) {
						result = requAfterMap.get(myKey);
						break;
					}
				} else if (rowTypeIndex == 2)
					result = impossible;
			}

			if (result != null)
				result = "(" + result + ")";

		}
		return result;
	}

	// TODO when is a cell editable? This returns always true...
	@Override
	public boolean isCellEditable(int row, int col) {
		int rowTypeIndex = row % noOfRowTypes;
		if (rowTypeIndex == 1 && col == 0)
			return false;
		if (rowTypeIndex == 2 && col == 0)
			return false;
		return false;
	}

	public MyTableCellRenderer getTableCellRenderer() {
		return new MyTableCellRenderer();
	}

	protected class MyTableCellRenderer extends DefaultTableCellRenderer {
		@Override
		public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			MyColorizer colorizer = new MyColorizer(String.valueOf(value));

			// logging.info(this, " render cell " + row + ", " + column);
			java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);

			colorizer.colorize(cell, isSelected, row, column);

			if (cell instanceof JComponent)
				((JComponent) cell).setToolTipText("" + value);

			// warning
			String cellValue = String.valueOf(value);
			if ((column == 2 || column == 3)
					&& (cellValue.equals("(" + InstallationStatus.getLabel(InstallationStatus.NOT_INSTALLED) + ":)")
							|| cellValue.equals("(:" + ActionRequest.getLabel(ActionRequest.UNINSTALL) + ")"))) {

				((JLabel) cell).setIcon(Globals.createImageIcon("images/warning.png", "warning"));

				((JLabel) cell).setHorizontalTextPosition(JLabel.LEADING);

				((JLabel) cell).setToolTipText(configed.getResourceValue("ProductInfoPane.RequirementsTable.warning"));

				return cell;

			} else {

				((JLabel) cell).setIcon(null);

			}

			return cell;
		}
	}

	protected class MyColorizer {
		String cellValue;

		protected MyColorizer(String value) {
			cellValue = value;
		}

		public void colorize(java.awt.Component cell, boolean isSelected, int row, int col) {
			cell.setForeground(Globals.lightBlack);

			int kindOfRow = row % 3;

			// logging.info(this, "colorize according to " + kindOfRow);

			// if (isSelected)
			{
				switch (kindOfRow) {
				case 0:
					cell.setBackground(Globals.backLightBlue);
					break;
				case 1:
					cell.setBackground(Globals.backVeryLightBlue);
					break;
				case 2:

					// cell.setBackground( Globals.backLighterBlue );
					cell.setBackground(Globals.backVeryLightBlue);
					break;
				}
			}
			/*
			 * else
			 * 
			 * {
			 * switch( kindOfRow )
			 * {
			 * case 0 :
			 * cell.setBackground( Globals.backgroundLightGrey); break;
			 * case 1 :
			 * cell.setBackground( Globals.backLightBlue); break;
			 * case 2:
			 * cell.setBackground( Globals.backVeryLightBlue); break;
			 * }
			 * }
			 */

			if (kindOfRow == 2 && col > 1)
				cell.setBackground(Globals.backgroundGrey);

		}
	}

	protected class MyWarningColorizer {
		public void colorize(java.awt.Component cell, boolean isSelected, int row, int col) {
			cell.setBackground(Globals.actionRed);
		}
	}

	protected class MyTableCellRendererWarning extends DefaultTableCellRenderer {
		MyWarningColorizer colorizer = new MyWarningColorizer();

		@Override
		public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int col) {
			java.awt.Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			colorizer.colorize(cell, isSelected, row, col);

			if (cell instanceof JComponent)
				((JComponent) cell).setToolTipText("" + value);

			return cell;
		}
	}

}
