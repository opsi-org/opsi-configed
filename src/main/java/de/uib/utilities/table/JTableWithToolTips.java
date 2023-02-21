package de.uib.utilities.table;

/* 
*	Copyright uib (uib.de) 2008
*	Author Rupert Röder
*
*/
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class JTableWithToolTips extends JTable {

	public JTableWithToolTips() {
		super();
	}

	public JTableWithToolTips(TableModel tm) {
		super(tm);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, colIndex);

		if (c instanceof JComponent) {
			JComponent jc = (JComponent) c;
			String valstr = "";

			if (c instanceof JLabel) {
				valstr = ((JLabel) c).getText();
			} else {
				Object val = getValueAt(rowIndex, colIndex);

				if (val instanceof Integer) {
					valstr = " " + val;
				} else if (val instanceof String) {
					valstr = (String) val;
				}
			}

			if (jc.getToolTipText() == null) {
				jc.setToolTipText(valstr);
			}

			return jc;
		}
		return c;
	}

}
