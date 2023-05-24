package de.uib.utilities.swing.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import de.uib.Main;
import de.uib.configed.Globals;
import de.uib.utilities.swing.CellAlternatingColorizer;

public class StandardListCellRenderer extends DefaultListCellRenderer {

	// this is the normal preferred height for components with content.
	// We want this height also for empty components
	private static final int CELL_HEIGHT = 20;
	protected static final int FILL_LENGTH = 20;

	protected String tooltipPrefix = "";

	private Color uniformColor;
	private Color uniformSelectedColor;

	private Color selectedEven = Globals.defaultTableSelectedRowDark;
	private Color selectedUneven = Globals.defaultTableSelectedRowBright;
	private Color unselectedEven = Globals.defaultTableCellBgColor2;
	private Color unselectedUneven = Globals.defaultTableCellBgColor1;

	public StandardListCellRenderer() {
		super();
	}

	public StandardListCellRenderer(String tooltipPrefix) {
		super();
		this.tooltipPrefix = tooltipPrefix;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		Dimension prefDimension = c.getPreferredSize();
		prefDimension.setSize(prefDimension.getWidth(), CELL_HEIGHT);
		c.setPreferredSize(prefDimension);

		// condition true if c is null
		if (!(c instanceof JComponent)) {
			return c;
		}

		JComponent jc = (JComponent) c;

		if (uniformColor == null) {
			CellAlternatingColorizer.colorize(jc, isSelected, index % 2 == 0, true, selectedEven, selectedUneven,
					unselectedEven, unselectedUneven);
		} else {
			if (!Main.THEMES) {
				if (isSelected) {
					jc.setBackground(uniformSelectedColor);
				} else {
					jc.setBackground(uniformColor);
				}
			}
		}

		if (!Main.FONT) {
			jc.setFont(Globals.defaultFont);
		}

		if (jc instanceof JLabel) {
			((JLabel) jc).setToolTipText(Globals.fillStringToLength(tooltipPrefix + " " + value + " ", FILL_LENGTH));
		}

		return jc;
	}

	public void setUniformColors(Color c, Color cSelected) {
		// ignore the inherited alterna

		uniformColor = c;
		uniformSelectedColor = cSelected;
	}

	public void setAlternatingColors(Color selectedEvenColor, Color selectedUnevenColor, Color unselectedEvenColor,
			Color unselectedUnevenColor) {
		selectedEven = selectedEvenColor;
		selectedUneven = selectedUnevenColor;
		unselectedEven = unselectedEvenColor;
		unselectedUneven = unselectedUnevenColor;
	}
}
