package de.uib.configed.gui;
/**
 * GroupsManager
 * Copyright:     Copyright (c) 2006
 * Organisation:  uib
 * @author Rupert Röder
 */


import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.Vector;
import javax.swing.text.*;
import java.text.MessageFormat;

import de.uib.utilities.swing.*;

public class GroupnameChoice extends FGeneralDialog 
  implements DocumentListener, ListSelectionListener
{
 
   private ConfigedMain main;
   
   protected int selIndex = -1;
   
   protected XList groups;
   private Vector dataVector;
   
   protected String resultString = "";
   
   
   JTextField groupnameField;
   
   public GroupnameChoice( String extraTitle, ConfigedMain main, Vector v, int selectedIndex)
   {
      super (null, extraTitle + " (" + Globals.APPNAME + ")" , true, new String[]{"ok", "Close"}, 300, 200);
      setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);
			try // in an applet context this is not possible without a security problem
			{
			 setAlwaysOnTop (true);
			}
			catch (Exception ex)
			{
				toFront();
			}
      this.main = main;
      
      
      dataVector = v;
      
      groups = new XList( v );
      groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      groups.setFont(Globals.defaultFontBig);
      groups.setBackground(Globals.backgroundLightGrey);
      
      groups.addListSelectionListener(this);
      
      selIndex = selectedIndex;
      
      
      groupnameField = new JTextField ();
      groupnameField.setFont(Globals.defaultFontBold);
      groupnameField.setPreferredSize (new Dimension ( this.getWidth() * 9 / 10  , Globals.lineHeight + 5));
      
      groupnameField.getDocument().addDocumentListener(this);
      
      if (selIndex >-1 && selIndex < v.size())
         groups.setSelectedIndex (selIndex);
      else
         selIndex = -1;

       if (selIndex == -1)
         groupnameField.setText("");
       
      JLabel labelExistingGroups = new JLabel (configed.getResourceValue("GroupnameChoice.existingGroups") + ":");
      labelExistingGroups.setFont (Globals.defaultFontBig);
      JPanel panelExistingGroups = new JPanel (new GridLayout(1, 2));
      panelExistingGroups.add ( labelExistingGroups);
      panelExistingGroups.add ( new JLabel (""));
      
 
      allpane.add(
      
           new VerticalPositioner
               ( 
			   new SurroundPanel (groupnameField),
			   panelExistingGroups
               ),                                 
        BorderLayout.NORTH);
        
      scrollpane.getViewport().add(groups);
      
      pack();
      setVisible(true);
   }
   
   
   public String getResultString()
   {
      return resultString;
   }
   
   public void doAction1()
   {
      result = 1;
      resultString = groupnameField.getText();
      leave();
   }
  
   public void doAction2()
   {
      result = -1;
      resultString = "";
      leave();
   }
   
   
  protected void textvalueChanged()
  {
     
    if (dataVector.contains (groupnameField.getText()))
     {
       groups.setSelectedValue (groupnameField.getText(), true);
       selIndex = groups.getSelectedIndex();
     }
     else
     {
       groups.clearSelection();
       selIndex = -1;
     }
     
     //System.out.println ("text value changed  selIndex " + selIndex + "   ?? " + groups.getSelectedIndex());
  }

   // DocumentListener for Document in groupnameField
  public void insertUpdate(DocumentEvent e)
  {
    textvalueChanged(); 
  }
  
  public void removeUpdate(DocumentEvent e)
  {
    textvalueChanged();
  }
  
  public void changedUpdate(DocumentEvent e)
  {
  }
   
  // ListSelectionListener
  public void valueChanged (ListSelectionEvent e)
  {
      selIndex = groups.getSelectedIndex();
      
      if (selIndex == -1)
         return;
      
      if 
       (groupnameField.getText() == null
       ||   !groupnameField.getText().equals ( (String) groups.getSelectedValue() )) 
      {  
         groupnameField.setText(  (String) groups.getSelectedValue() );
      }
  }
  
}
  
   
 
 
 
 
