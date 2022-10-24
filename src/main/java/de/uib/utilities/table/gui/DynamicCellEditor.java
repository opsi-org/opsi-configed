package de.uib.utilities.table.gui;

import de.uib.configed.Globals;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.swing.*;
import java.awt.Component;

public class DynamicCellEditor extends DefaultCellEditor
 {
   
   JComboBox cc;
   ComboBoxModeller cbm;
   ComboBoxModel nullModel;
   
   
   public DynamicCellEditor(
   	   JComboBox cc, 
   	   ComboBoxModeller cbm,
   	   Set<String> knownKeys
   	   )
   {
       super(cc);
       this.cc = cc;
       this.cbm = cbm;
	   nullModel = new DefaultComboBoxModel(new String[]{""});
	   
	   cc.setRenderer(new CellRendererByIndex(knownKeys, null, 30));
	   
   }
	
   
   public Component getTableCellEditorComponent(JTable table,
                                      Object value,
                                      boolean isSelected,
                                      int row,
                                      int column)
    {
		
		int modelRow   	  = table.convertRowIndexToModel(row);
		int modelColumn  = table.convertColumnIndexToModel(column);
		if (cbm == null || cbm.getComboBoxModel (modelRow, modelColumn) == null || cbm.getComboBoxModel (modelRow, modelColumn).getSize() <=1)
		{
			cc.setModel(nullModel);
			
			if (cbm != null &&  cbm.getComboBoxModel (modelRow, modelColumn) != null && cbm.getComboBoxModel (modelRow, modelColumn).getSize() == 1)
				cc.setToolTipText((String) cbm.getComboBoxModel (modelRow, modelColumn).getElementAt(0));
		}
		else
			cc.setModel (cbm.getComboBoxModel (modelRow, modelColumn)); 
		
		//cc.setToolTipText("hello");
		//cc.setSize(100,20); has no effect	
		
		Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column); 
		if (c instanceof JComponent)
			((JComponent) c). setToolTipText("" + value);
		
		//c.setSize(100,20);  has no effect	
		//System.out.println("this component active");
		return c;
    }
  
 }
