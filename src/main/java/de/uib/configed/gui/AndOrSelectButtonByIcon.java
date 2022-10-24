package de.uib.configed.gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.util.*;
import de.uib.utilities.logging.*;

/**
 * Select AND or OR
 * Created for the ClientSelectionDialog.
 */
public class AndOrSelectButtonByIcon extends IconAsButton 
{
	private IconAsButton ib;
   
	public AndOrSelectButtonByIcon()
	{
		super("and/or",  "images/boolean_and_or_disabled.png", "images/boolean_and_or_over.png", "images/boolean_and_or.png", null);
		addActionListener( new ButtonActionListener() );
	}

	
    public boolean isAndSelected()
    {
    		logging.debug(this, "isEnabled " + isEnabled());
        return  !isActivated();
    }
    
    public boolean isOrSelected()
    {
    		logging.debug(this, "isEnabled " + isEnabled());
        return  isActivated();
    }
    
    public void selectAnd()
    {
        setActivated(false);
    }
    
    public void selectOr()
    {
        setActivated(true);
    }
    
    
     private void createActionEvents()
    {
        for( ActionListener listener: actionListeners )
        {
            ActionEvent myEvent = new ActionEvent( this, ActionEvent.ACTION_PERFORMED, "AndOrSelectButtonByIcon" );
            listener.actionPerformed( myEvent );
        }
    }
    
    private class ButtonActionListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
        		logging.debug(this, "actionPerformed  "+ e + " activated " + activated);
        		setActivated(!activated);
             //createActionEvents();
        }
    }
}

        