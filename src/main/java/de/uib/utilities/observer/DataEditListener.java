// title: DataEditListener
// description:
// class for interaction with an Observable that reacts to changes in a document
// implements DocumentListener
// uib 2009

package de.uib.utilities.observer;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event .*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;

public class DataEditListener 
	implements 
		DocumentListener,  // for text components
		ItemListener, //for combo boxes
		
		KeyListener
	
{
	public static final String commitRequest = "COMMIT";
	public static final String cancelRequest = "CANCEL";
	protected Object source;
	protected ObservableSubject dataChangedSubject;
	protected boolean withFocusCheck = true;
	
	protected void act()
	{
		if (dataChangedSubject == null)
		{
			logging.info(this, "dataChangedSubject null");
			return;
		}
		//logging.debug(this, "++ act! , withFocusCheck ?  " + withFocusCheck);
		if (!withFocusCheck || (source instanceof JComponent && ((JComponent) source).hasFocus()) )
		{
			//logging.debug(this, "++ act!  ");
			dataChangedSubject.setChanged();
			dataChangedSubject.notifyObservers();
		}
	}
	
	protected void requestAction(String action)
	{
		if (dataChangedSubject == null)
		{
			logging.info(this, "dataChangedSubject null");
			return;
		}
		
		if (!withFocusCheck || (source instanceof JComponent && ((JComponent) source).hasFocus())) 
		{
			//logging.debug(this,  "requestAction  " + action);
			if (! (action.equals(cancelRequest) || action.equals(commitRequest)) )
			{
				dataChangedSubject.setChanged();
				dataChangedSubject.notifyObservers(action);
			}
		}
	}
	
	public DataEditListener(ObservableSubject subject, Object source, boolean withFocusCheck)
	{
		logging.info(this, "constructed , subject  " + subject + ", source " + source);
		this.source = source;
		this.withFocusCheck = withFocusCheck;
		dataChangedSubject = subject;
	}
	
	public DataEditListener(ObservableSubject subject, Object source)
	{
		this(subject, source, true);
	}
	
	public void setWithFocusCheck(boolean b)
	{
		withFocusCheck = b;
	}
	
	public boolean isWithFocusCheck()
	{
		return withFocusCheck;
	}
	
	// DocumentListener interface
	public void changedUpdate(DocumentEvent e)
	{
		//logging.debug(this, "++ changedUpdate on " + source);
		act();
	}
	
	public void insertUpdate(DocumentEvent e)
	{
		//logging.debug(this, "++ insertUpdate on " + source);
		act();
		//logging.debug(this, "++ is still changed " + dataChangedSubject.hasChanged());
	}
	
	public void removeUpdate(DocumentEvent e)
	{
		//logging.debug(this, "++ removeUpdate on " + source);
		act();
	}
	
	//ItemListener interface
	public void itemStateChanged(ItemEvent e)
	{
		//logging.debug(this, "++ ItemEvent on " + source);
		act();
	}
	
	// KeyListener interface
	public void keyPressed(KeyEvent e)
	{
		//logging.debug(this, "keypressed  " + e.getKeyCode());
		
		if (e.getKeyCode() == 10)// KeyEvent.VK_ENTER)
			requestAction(commitRequest);
		
		else if (e.getKeyCode() == 27)//KeyEvent.VK_ESCAPE)
			requestAction(cancelRequest);
		
		else 
			if (!e.isActionKey())
				act();
	}
	
    public void keyReleased(KeyEvent e)
    {
    }
    public void keyTyped(KeyEvent e)
	{
	}
}
	
