package de.uib.configed.gui;

/**
 * FTextArea
 * Copyright:     Copyright (c) 2001-2005,2018,2021
 * Organisation:  uib
 * @author Rupert Röder
 * @version
 */
import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

public class FTextArea extends FGeneralDialog
{

	JTextArea jTextArea1 = new JTextArea();

	public FTextArea(JFrame owner, String title, boolean modal)
	{
		super(owner, title, modal);
		init();
	}

	public FTextArea(JFrame owner, String title, boolean modal, int lastButtonNo)
	{
		super(owner, title, modal, lastButtonNo);
		init();
	}


	public FTextArea(JFrame owner, String title, String message, boolean modal, int lastButtonNo)
	{
		this (owner, title, modal, lastButtonNo);
		init();
		setMessage (message);
	}

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList)
	{
		super (owner, title, modal, buttonList);
		init();
	}

	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth, int preferredHeight)
	{
		super (owner, title, modal, buttonList, preferredWidth, preferredHeight);
		init(preferredWidth, preferredHeight);
	}
	
	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons, int preferredWidth, int preferredHeight)
	{
		
		super(owner, title, modal, buttonList, icons, buttonList.length, preferredWidth, preferredHeight);
		init(preferredWidth, preferredHeight);
	}
	
	public FTextArea(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons, int preferredWidth, int preferredHeight, JPanel addPane)
	{
		super (owner, title, modal, buttonList, icons, buttonList.length, preferredWidth, preferredHeight, false, addPane);
		initComponents();
		init(preferredWidth, preferredHeight);
		
	}

	public void setMessage (String message)
	{
		jTextArea1.setText(message);
		jTextArea1.setCaretPosition(0);
	}
	
	public JTextComponent getTextComponent()
	{
		return jTextArea1;
	}

	private void init()
	{
		init(700, 100);
	}
	
	@Override
	protected boolean wantToBeRegisteredWithRunningInstances()
	{
		return false;
	}
	

	private void init(int preferredWidth, int preferredHeight)
	{
		allpane.setPreferredSize (new Dimension(preferredWidth, preferredHeight));
		jTextArea1.setLineWrap(true);
		jTextArea1.setWrapStyleWord(true);
		jTextArea1.setOpaque(true);
		//jTextArea1.setBackground(myHintYellow);
		jTextArea1.setBackground(Globals.backgroundWhite);
		jTextArea1.setText("          ");
		jTextArea1.setEditable(false);
		jTextArea1.setFont(Globals.defaultFontBig); //new java.awt.Font("Dialog", 0, 14));

		scrollpane.getViewport().add(jTextArea1, null);

		jTextArea1.addKeyListener(this);

		pack();

	}
	
	
	// KeyListener

	public void keyReleased (KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
		{ shiftPressed = false;
			//System.out.println ("shift released");
		}

		if (e.getKeyCode () == KeyEvent.VK_TAB && !shiftPressed)
		{
			if (e.getSource() == jTextArea1)
			{jButton1.requestFocus();}
		}

		if (e.getKeyCode () == KeyEvent.VK_TAB && shiftPressed)
		{
			if (e.getSource() == jButton1)
			{jTextArea1.requestFocus();}
		}
	}
	
	
	public static void main(String[] args)
	{
		
	}


}



