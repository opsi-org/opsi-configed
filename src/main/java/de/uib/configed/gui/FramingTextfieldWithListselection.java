package de.uib.configed.gui;

/**
 *l
 * Copyright:     Copyright (c) 2022
 * Organisation:  uib
 * @author Rupert Röder
 */

import de.uib.configed.*;
import de.uib.configed.gui.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;

public interface FramingTextfieldWithListselection 
{
	
	public String getTitle();
	
	public String getTextfieldLabel();
	
	public String getListLabel();
	
	public String getListLabelToolTip();
	
	public Vector<String> getListData();
	
	public void setListData( Vector<String> v );
}
	
