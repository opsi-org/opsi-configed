package de.uib.utilities.swing;

import java.util.ArrayList;

import javax.swing.JList;

import de.uib.configed.Globals;

public class XList extends JList // org.jdesktop.swingx.JXList
{
	public XList() {
		super();
		configure();
	}

	public XList(ArrayList<?> listData) {
		super(listData.toArray());
		configure();
	}

	protected void configure() {
		setSelectionBackground(Globals.nimbusSelectionBackground);
		setBackground(Globals.nimbusBackground);
	}

}
