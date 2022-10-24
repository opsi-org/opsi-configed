package de.uib.utilities.observer.swing;


/*
* JTextFieldObserved.java
* 
* (c) uib 2009-2012
* GPL-licensed
* author Rupert Röder
*
*
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.configed.Globals;
import de.uib.utilities.observer.*;
import de.uib.utilities.logging.*;

public class JTextFieldObserved extends JTextField
		implements KeyListener
{
	protected String startText = "";
	
	protected ObservableSubject globalEditingSubject;
	
	public JTextFieldObserved() 
	{
		this(null);
	}
	
	public JTextFieldObserved(ObservableSubject globalEditingSubject)
	{
		this("", globalEditingSubject);
	}
	
	public JTextFieldObserved(String s, ObservableSubject globalEditingSubject)
	{
		super(s);
		addKeyListener(this);
		setGlobalObservableSubject(globalEditingSubject);
	}
	
	public void setGlobalObservableSubject(ObservableSubject globalEditingSubject)
	{
		logging.debug(this, "setGlobalObservableSubject " + globalEditingSubject);
		this.globalEditingSubject = globalEditingSubject;
		addKeyListener(new DataEditListener(globalEditingSubject, this));
		getDocument().addDocumentListener(new DataEditListener(globalEditingSubject, this));
	}
	
	@Override
	public void setText(String s)
	{
		//logging.debug(this, "setText " + s);
		super.setText(s);
		startText = s;
		setCaretPosition(0);
		
		
	}
	
	//KeyListener
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			//logging.debug(this, "escape");
			setText(startText);
			setCaretPosition(startText.length());
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			transferFocus();
		}
			
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	
}
	
