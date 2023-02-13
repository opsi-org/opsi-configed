package de.uib.utilities.swing;

import java.util.List;

import javax.swing.JList;

import de.uib.configed.Globals;

public class XList extends JList // org.jdesktop.swingx.JXList
{
	public XList() {
		super();
		configure();
	}

	public XList(List<?> listData) {
		super(listData.toArray());
		configure();
	}

	private void configure() {
		setSelectionBackground(Globals.nimbusSelectionBackground);
		setBackground(Globals.nimbusBackground);
	}

}
