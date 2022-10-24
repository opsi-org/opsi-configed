package de.uib.utilities.swing;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import de.uib.utilities.logging.*;

public class JComboBoxToolTip extends javax.swing.JComboBox {
	
	private Map<String, String> selectValues;
	
	protected java.awt.Color listBackgroundColorSelected;
	protected java.awt.Color listBackgroundColorUnselected;
	protected java.awt.Color listForegroundColor;
	
	protected boolean addEmpty = false;
	
	public JComboBoxToolTip () {
		super(); // as it is
		
		listBackgroundColorSelected = de.uib.utilities.Globals.backgroundWhite;
		listBackgroundColorUnselected = de.uib.utilities.Globals.backgroundLightGrey;
		listForegroundColor = de.uib.utilities.Globals.lightBlack;
		
	}
	
	
	Vector<String> tooltips = new Vector<String>();
	
	protected class NewComboBoxRenderer extends BasicComboBoxRenderer {
	    public Component getListCellRendererComponent(JList list, Object value,
	        int index, boolean isSelected, boolean cellHasFocus) {
	      if (isSelected) {
	        setBackground( listBackgroundColorSelected ); //list.getSelectionBackground());
	        setForeground(  listForegroundColor ); //list.getSelectionForeground());
	        logging.debug(this,  "index, tooltips " + index + ", " +  tooltips );
	        if (-1 < index 
	        	&& index < tooltips.size() // we had an error only on linux with openjdk 8
	        ){
	          list.setToolTipText(tooltips.get(index));
	        }
	      } else {
	        setBackground( listBackgroundColorSelected  ); // java.awt.Color.YELLOW); // list.getBackground());
	        setForeground( listForegroundColor ); //list.getForeground());
	      }
	      setFont(list.getFont());
	      setText((value == null) ? "" : value.toString());
	      return this;
	    }
	  }

	
	public void setToolTips () {
		this.setRenderer(new NewComboBoxRenderer());
	}
	
	public void setValues(Map <String, String> v, boolean addEmpty)
	{
		this.addEmpty = addEmpty;
		setValues(v);
	}
	
	
	public void setValues(Map <String, String> v)
	{
		selectValues = v;
		setComboValues();
		
		this.setToolTips();
	}
	
	
	protected void setComboValues()
	{
		Set iterableKeys;
		String iterValue;
		boolean addE = addEmpty && !selectValues.containsKey("");
		
		this.removeAllItems();
		
		tooltips = new Vector<String>();
		
		if (addE)
		{
			addItem("");
			tooltips.add("");
		}
		
		if (selectValues != null)
		{
			for (String key : selectValues.keySet())
			{
				addItem(key);
				tooltips.add(selectValues.get(key));
			}
			
		}
		
	}

}
