package de.uib.utilities.swing;

import java.util.List;

import javax.swing.JList;

import de.uib.configed.Globals;

public class XList extends JList<String> {
	public XList() {
		super();
		configure();
	}

	public XList(List<String> listData) {
		super(listData.toArray(new String[0]));
		configure();
	}

	private void configure() {
		setSelectionBackground(Globals.nimbusSelectionBackground);
		setBackground(Globals.nimbusBackground);
	}

}
