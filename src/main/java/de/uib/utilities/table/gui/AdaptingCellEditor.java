package de.uib.utilities.table.gui;

import de.uib.configed.Globals;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.swing.*;

public class AdaptingCellEditor extends DefaultCellEditor
 {
   
   JComboBox cc;
   ComboBoxModeller cbm;
   ComboBoxModel nullModel;
   
   public AdaptingCellEditor(JComboBox cc, ComboBoxModeller cbm)
   {
       super(cc);
       this.cc = cc;
       this.cbm = cbm;
	   nullModel = new DefaultComboBoxModel(new String[]{""});
	   //cc.getEditor().getEditorComponent().setBackground(java.awt.Color.blue);
	   
	   cc.setRenderer(new ColoredListCellRenderer());
	   
   }
		   
   
   public Component getTableCellEditorComponent(JTable table,
                                      Object value,
                                      boolean isSelected,
                                      int row,
                                      int column)
    {
		
		int modelRow   	  = table.convertRowIndexToModel(row);
		int modelColumn  = table.convertColumnIndexToModel(column);
		if (cbm == null || cbm.getComboBoxModel (modelRow, modelColumn).getSize() <=1)
		{
			cc.setModel(nullModel);
			
			if (cbm != null && cbm.getComboBoxModel (modelRow, modelColumn).getSize() == 1)
				cc.setToolTipText((String) cbm.getComboBoxModel (modelRow, modelColumn).getElementAt(0));
		}
		else
			cc.setModel (cbm.getComboBoxModel (modelRow, modelColumn)); 
		
		//cc.setToolTipText("hallo");
			
		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column); 
			
		return c;
    }
  
 }
