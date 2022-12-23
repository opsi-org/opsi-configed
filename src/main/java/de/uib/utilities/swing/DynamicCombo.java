/*
 * DynamicCombo.java
 *
 * Created on 14.04.2009, 10:36:25
 */

package de.uib.utilities.swing;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uib.configed.Globals;

/**
 * @author roeder
 */
public class DynamicCombo extends javax.swing.JPanel {

	protected javax.swing.JTable table;
	protected javax.swing.table.DefaultTableModel tablemodel;
	protected javax.swing.JComboBox combo;
	protected de.uib.utilities.ComboBoxModeller modelsource;

	protected javax.swing.table.TableColumn col;

	public DynamicCombo(de.uib.utilities.ComboBoxModeller modelsource) {
		this.modelsource = modelsource;
		initComponents();
	}

	public DynamicCombo() {
		this(null);
	}

	private void initComponents() {

		table = new javax.swing.JTable();

		tablemodel = new javax.swing.table.DefaultTableModel(new String[] { "column 0" }, 0);

		tablemodel.addRow(new String[] { "" });
		

		table.setModel(tablemodel);
		table.setSelectionBackground(Globals.backgroundWhite);
		table.setSelectionForeground(Globals.lightBlack);
		table.setShowGrid(false);

		col = table.getColumnModel().getColumn(0);
		col.setHeaderRenderer(null);

		combo = new javax.swing.JComboBox();
		
		
		combo.setBorder(null);

		combo.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// logging.debug(this, "popupMenuCanceled, value>>" + combo.getSelectedItem() +
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// logging.debug(this, "popupMenuWillBecomeInvisible , value>>" +
				
				combo.setSelectedItem(combo.getSelectedItem());
				// ensures that we leave the combo box completely when we set the focus
				// somewhere else
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				// logging.debug(this, "popupMenuWillBecomeVisible, value>>" +
				
			}
		});

		/*
		 * combo.addItemListener(new ItemListener()
		 * {
		 * public void itemStateChanged(ItemEvent e)
		 * {
		 * logging.debug(this, "itemStateChanged ");
		 * }
		 * 
		 * }
		 * );
		 * 
		 * combo.addActionListener(new ActionListener()
		 * {
		 * public void actionPerformed(ActionEvent e)
		 * {
		 * logging.debug(this, "action event ");
		 * combo.setSelectedItem("");
		 * }
		 * 
		 * }
		 * );
		 */

		col.setCellEditor(

				new de.uib.utilities.table.gui.AdaptingCellEditor(combo, modelsource));

		setLayout(new java.awt.BorderLayout());

		add(table);

	}

	@Override
	public void setFont(java.awt.Font font) {
		if (combo != null)
			combo.setFont(font);
	}

	public void setModelSource(de.uib.utilities.ComboBoxModeller modelSource) {
		this.modelsource = modelSource;

		col.setCellEditor(

				new de.uib.utilities.table.gui.AdaptingCellEditor(combo, modelsource));

	}

	public Object getSelectedItem() {
		return table.getValueAt(0, 0);
	}

}
