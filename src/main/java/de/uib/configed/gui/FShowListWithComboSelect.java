package de.uib.configed.gui;

/**
 * FShowList
 * Copyright:     Copyright (c) 2001-2006
 * Organisation:  uib
 * @author Rupert Röder
 */
 
import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

/** This class is intended to show a list in text area
*/
public class FShowListWithComboSelect extends FShowList
{
	String[] choices;
	JComboBox combo;
	JLabel labelChoice;
	 
	public  FShowListWithComboSelect(JFrame owner, String title, boolean modal, String choiceTitle, String[] choices, String[] buttonList)
	{
		super (owner, title, modal, buttonList);
		//JPanel panelChoice = new JPanel(new BorderLayout());
		labelChoice = new JLabel(choiceTitle + ": ");
		labelChoice.setOpaque(true);
		labelChoice.setBackground(Globals.backgroundLightGrey);
		northPanel.add(labelChoice);
		combo = new JComboBox (choices);
		combo.setFont(Globals.defaultFontBold);
		northPanel.add(combo);
		//northPanel.setBackground(Color.RED);
		//northPanel.setOpaque(true);
		//HorizontalPositioner northPanel = new HorizontalPositioner (new SurroundPanel(labelChoice), combo);
		//northPanel = panelChoice;
		pack();
		//allpane.add(panelChoice, BorderLayout.NORTH);
		//allpane.setBackground(Color.YELLOW);
		
	}
 
 	public Object getChoice()
	{
		return combo.getSelectedItem();
	}
}
 


