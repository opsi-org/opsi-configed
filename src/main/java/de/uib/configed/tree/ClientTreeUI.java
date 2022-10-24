package de.uib.configed.tree;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.*;

public class ClientTreeUI extends BasicTreeUI
{
	public static ComponentUI createUI(JComponent c)
	{
		return new ClientTreeUI();
	}
}
