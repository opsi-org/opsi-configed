package de.uib.utilities.observer.swing;


/*
* ValueChangeListener.java
* 
* (c) uib 2012
* GPL-licensed
* authors Felix Rohrbach, Rupert Röder
*
*
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

public abstract class ValueChangeListener implements ActionListener, ChangeListener, DocumentListener
{
	abstract protected void actOnChange();
	
	public void actionPerformed( ActionEvent event )
	{
		actOnChange();
	}
	
	public void stateChanged( ChangeEvent e )
	{
		actOnChange();
	}
	
	public void changedUpdate( DocumentEvent e )
	{
		actOnChange();
	}
	
	public void insertUpdate( DocumentEvent e )
	{
		actOnChange();
	}
	
	public void removeUpdate( DocumentEvent e )
	{
		actOnChange();
	}
}
