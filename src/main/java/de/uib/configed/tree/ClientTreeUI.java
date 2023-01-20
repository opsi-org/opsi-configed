package de.uib.configed.tree;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;

// TODO remove as soon as themes are used
public class ClientTreeUI extends BasicTreeUI {
	public static ComponentUI createUI(JComponent c) {
		return new ClientTreeUI();
	}
}
