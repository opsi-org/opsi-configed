package de.uib.utilities.swing;

import java.util.*;
import de.uib.configed.*;
import javax.swing.*;


public class LabelChecked extends JLabel
{

	Icon iconChecked = de.uib.configed.Globals.createImageIcon("images/checked_box_blue_14.png", "");
	Icon iconUnchecked = de.uib.configed.Globals.createImageIcon("images/checked_box_blue_empty_14.png","");
	Icon iconEmpty = de.uib.configed.Globals.createImageIcon("images/checked_void.png", "");
	
	
	public LabelChecked()
	{
		this( false );
	}
	
	public LabelChecked( Boolean b)
	{
		super();
		setValue ( b );
	}
	
	public LabelChecked( Icon iconChecked, Icon iconUnchecked, Icon iconEmpty )
	{
		super();
		
		this. iconChecked = iconChecked;
		this. iconUnchecked = iconUnchecked;
		this. iconEmpty = iconEmpty;
		
		setIcon( iconEmpty );
	}
	
	public void setValue (Boolean b)
	{
		if (b == null)
			setEmpty();
		else
			if (b)
				setChecked();
			else
				setUnchecked();
	}
			
	
	public void setChecked()
	{
		setIcon( iconChecked );
	}
	
	public void setUnchecked()
	{
		setIcon( iconUnchecked );
	}
	
	public void setEmpty()
	{
		setIcon( iconEmpty );
	}
}
	
		