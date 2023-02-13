package de.uib.utilities.swing;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class SurroundPanel extends JPanel {
	public SurroundPanel() {
		super();
		super.setOpaque(false);
		super.setLayout(new FlowLayout(FlowLayout.CENTER));
	}

	public SurroundPanel(JComponent c) {
		this();
		super.add(c);
	}

}
