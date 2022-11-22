package de.uib.utilities.swing;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class SurroundPanel extends JPanel {
	public SurroundPanel() {
		super();
		setOpaque(false);
		setLayout(new FlowLayout(FlowLayout.CENTER));
	}

	public SurroundPanel(JComponent c) {
		this();
		add(c);
	}

}
