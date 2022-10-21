package de.uib.utilities;

import javax.swing.ComboBoxModel;

/**
   Any implementation of this interface gives  a ComboBoxModel for each pair (row, column)
*/
public interface ComboBoxModeller
{
  /**
    Producing a Combo
 */  
  ComboBoxModel getComboBoxModel (int row,  int column);
}


