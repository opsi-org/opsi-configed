package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class StandardTableCellRenderer extends DefaultTableCellRenderer {
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix;
	private String separator = ": ";

	public StandardTableCellRenderer() {
		super();
	}

	public StandardTableCellRenderer(String tooltipPrefix) {
		this();
		this.tooltipPrefix = tooltipPrefix;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;
		CellAlternatingColorizer.colorize(jc, isSelected, row % 2 == 0, column % 2 == 0, true);

		if (jc instanceof JLabel) {
			String tooltipText = null;
			if (tooltipPrefix != null && !tooltipPrefix.isEmpty()) {
				tooltipText = Globals.fillStringToLength(tooltipPrefix + separator + value + " ", FILL_LENGTH);
			} else {
				tooltipText = Globals.fillStringToLength(value + " ", FILL_LENGTH);
			}

			((JLabel) jc).setToolTipText(tooltipText);
		}

		if (ConfigedMain.THEMES) {
			if (isSelected) {
				jc.setBackground(Globals.opsiBlue);
				if (FlatLaf.isLafDark()) {
					jc.setForeground(Globals.opsiForegroundDark);
				} else {
					jc.setForeground(Globals.opsiBackgroundLight);
				}
			} else if (FlatLaf.isLafDark()) {
				jc.setBackground(Globals.opsiBackgroundDark);
				jc.setForeground(Globals.opsiForegroundDark);
			} else {
				jc.setBackground(Globals.opsiBackgroundLight);
				jc.setForeground(Globals.opsiForegroundLight);
			}

			if (row % 2 != 0) {
				if (!FlatLaf.isLafDark() || isSelected) {
					jc.setBackground(jc.getBackground().darker());
				} else {
					jc.setBackground(jc.getBackground().brighter());
				}
			}
		}

		return jc;
	}
}
