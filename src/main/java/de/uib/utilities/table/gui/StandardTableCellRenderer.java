package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class StandardTableCellRenderer extends DefaultTableCellRenderer {

	protected String tooltipPrefix = null;
	protected String separator = ": ";

	protected static final int FILL_LENGTH = 20;

	protected int currentRow = -1;

	public StandardTableCellRenderer() {
		super();
	}

	public StandardTableCellRenderer(String tooltipPrefix) {
		this();
		this.tooltipPrefix = tooltipPrefix;
	}

	public void setCurrentRow(int rowNumber) {
		currentRow = rowNumber;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;
		CellAlternatingColorizer.colorize(jc, isSelected, (row % 2 == 0), (column % 2 == 0), true);

		if (jc instanceof JLabel) {
			String tooltipText = null;
			if (tooltipPrefix != null && !tooltipPrefix.equals("")) {
				tooltipText = Globals.fillStringToLength(tooltipPrefix + separator + value + " ", FILL_LENGTH);
			} else {
				tooltipText = Globals.fillStringToLength(value + " ", FILL_LENGTH);
			}

			((JLabel) jc).setToolTipText(tooltipText);
		}

		return jc;
	}

}
