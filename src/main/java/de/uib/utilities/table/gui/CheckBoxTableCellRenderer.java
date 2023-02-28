package de.uib.utilities.table.gui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import de.uib.configed.Globals;

public class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer

{
	javax.swing.border.Border noFocusBorder;
	javax.swing.border.Border focusBorder;

	public CheckBoxTableCellRenderer() {
		super();

		super.setOpaque(true);
		super.setBorderPainted(true);
		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setVerticalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		setForeground(Globals.lightBlack);

		if (isSelected) {
			setBackground(Globals.defaultTableCellSelectedBgColorNotEditable);
		} else if (row % 2 == 0) {
			setBackground(Globals.defaultTableCellBgColor2);
		} else {
			setBackground(Globals.defaultTableCellBgColor1);
		}

		if (hasFocus) {
			if (focusBorder == null) {
				focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
			}
			setBorder(focusBorder);
		} else {
			if (noFocusBorder == null) {
				noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);
			}
			setBorder(noFocusBorder);
		}

		setSelected(Boolean.TRUE.equals(value));
		return this;
	}

}
