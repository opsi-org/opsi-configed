package de.uib.utilities.swing;

import javax.swing.*;

public interface MissingDataPanel
{
	void setMissingDataPanel(boolean b);
	
	/* Java 8
	default void setMissingDataPanel(boolean b, JComponent c)
	{
		 setMissingDataPanel(b);
	}
	*/
	
	void setMissingDataPanel(boolean b, JComponent c);
}
	
	
