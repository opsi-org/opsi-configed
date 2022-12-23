/*
*	ImagePanel.java
*/

package de.uib.utilities.swing;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	Image image;

	public ImagePanel(Image image) {
		this.image = image;
		
		
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
	}
}
