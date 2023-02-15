package de.uib.utilities.swing;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class CenterPositioner extends JPanel {

	public CenterPositioner(JComponent comp) {
		super.setLayout(new FlowLayout());
		super.add(comp);
	}

}
