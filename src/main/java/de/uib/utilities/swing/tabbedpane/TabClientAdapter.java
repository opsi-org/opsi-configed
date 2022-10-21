package de.uib.utilities.swing.tabbedpane;

import javax.swing.*;
import de.uib.utilities.logging.*;

public class TabClientAdapter extends JPanel
	implements TabClient
{
  
	public TabClientAdapter()
	{
		super();
	//System.out.println("-- TabClientAdapter created and made visible ");
	}
  
	public void reset()
	{
		logging.info(this, "TabClientAdapter.reset() ");
	}
	
	public boolean mayLeave()
	{
		boolean result = true;
		logging.debug(this, "TabClientAdapter.mayLeave() " + result);
		return result;
	}
	 
}
