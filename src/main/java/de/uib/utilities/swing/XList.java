package de.uib.utilities.swing;

import java.util.*;
import de.uib.configed.*;
import javax.swing.*;


public class XList extends JList // org.jdesktop.swingx.JXList
{
	public XList()
	{
		super();
		configure();
	}
	
	public XList(Vector<?> listData)
	{
		super(listData);
		configure();
	}
	
	protected void configure()
	{
		setSelectionBackground(Globals.nimbusSelectionBackground);
		setBackground(Globals.nimbusBackground);
	}
	
}
