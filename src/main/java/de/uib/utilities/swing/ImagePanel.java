/*
*	ImagePanel.java
*/

package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import de.uib.utilities.Globals;
import de.uib.utilities.logging.*;

public class ImagePanel extends JPanel 
{
	
	Image image;
	
	public ImagePanel(Image image) {
		this.image = image;
	//image = de.uib.configed.Globals.createImage("images/waitingcircle.gif");
	//Toolkit.getDefaultToolkit().createImage("e:/java/spin.gif");
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
				g.drawImage(image, 0, 0, this);
		}
	}
}
	
