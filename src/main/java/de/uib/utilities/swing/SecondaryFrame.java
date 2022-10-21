package de.uib.utilities.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import de.uib.utilities.logging.*;

public class SecondaryFrame extends JFrame
	implements WindowListener
{
	
	protected Container masterFrame;
	
	public SecondaryFrame()
	{
		this.masterFrame = de.uib.utilities.Globals.masterFrame; 
		if (masterFrame == null)
			logging.warning(this, "masterFrame yet null");
		addWindowListener(this);
	}
	
	public void setGlobals(Map globals) 
	{
		setIconImage ((Image) globals.get("mainIcon"));
		setTitle((String) globals.get("APPNAME"));
	}
	
	public void start()
	{
		setExtendedState(Frame.NORMAL);
		setVisible(true);
		logging.info(this, "started");
	}
	
	
	
	public void centerOnParent()
	{
		boolean locateExternal = true;
		if (masterFrame == null)
			locateExternal = false;
		
		try 
		{
			int h = getSize().height;
			int w = getSize().width;
		
			if (h > masterFrame.getSize().height)
			{
				h = masterFrame.getSize().height;
			}
			if (w > masterFrame.getSize().width)
			{
				w = masterFrame.getSize().width;
			}
			int startX = masterFrame.getLocationOnScreen().x;
			int startY= masterFrame.getLocationOnScreen().y;
			setLocation( startX + (masterFrame.getSize().width - w) / 2, startY + (masterFrame.getSize().height - h) / 2);
		}
		catch(Exception ex)
		{
			locateExternal = false;
			//master has no location (Applet)
			
		}
		
		if (!locateExternal)
		{
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int startX = (screenSize.width  - getSize().width)/ 2;
			int startY = (screenSize.height - getSize().height)/2;
			setLocation( startX, startY);
		}
			
	}
		

	//for overriding
	protected void callExit()
	{
		setVisible(false);
	}
	
	
	protected void processWindowEvent(WindowEvent e)
	{
		//logging.info(this, "--------processWindowEvent " + e);
		//super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			callExit();
		}
		
	}
	
	
	/* WindowListener implementation */
	public void windowClosing (WindowEvent e)
	{
		//logging.info(this, "-------windowClosing " + e);
		callExit();
	}
	
	public void windowOpened (WindowEvent e) 
	{;}
	public void windowClosed (WindowEvent e) 
	{;}
	public void windowActivated (WindowEvent e) 
	{;}
	public void windowDeactivated (WindowEvent e) 
	{;}
	public void windowIconified (WindowEvent e) 
	{;}
	public void windowDeiconified (WindowEvent e) 
	{;}
	
}
