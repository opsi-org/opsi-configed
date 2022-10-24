package de.uib.utilities.swing; 

import javax.swing.*;
import java.awt.*;
import java.util.*;
import de.uib.configed.Globals;


public class ImagePlusTextLabel extends JPanel
{
	JLabel textlabel;
	JLabel imagefield;
	int imageWidth;
	
	public ImagePlusTextLabel(int imageWidth) 
	{
		super();
		this.imageWidth = imageWidth;
		initComponents();
	}
	
	
	protected void initComponents()
	{
		
		textlabel = new JLabel();
		textlabel.setHorizontalAlignment(SwingConstants.LEFT);
		imagefield = new JLabel();
		imagefield.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addGap(2)
					.addComponent(imagefield, imageWidth, imageWidth, imageWidth)
					.addGap(2)
					.addComponent(textlabel, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGap(2)
				)
			)
		
		;
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				//.addGap(1)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(imagefield, Globals.lineHeight - 5,  Globals.lineHeight - 5, Globals.lineHeight - 5)
					.addComponent(textlabel,  Globals.lineHeight - 5, Globals.lineHeight - 5, Globals.lineHeight - 5)
				)
			)
		;
			
	}
	
	
	public void setIconVisible(boolean b)
	{
		imagefield.setVisible(b);
	}
	
	public void setText(String text)
	{
		textlabel.setText(text);
	}
	
	public void setIcon(Icon icon)
	{
		imagefield.setIcon(icon);
	}
		
	/*  without questioning if the childcomponent is null it leads to exception because it is set deeply into the Swing machine beforehand */
	public void setFont(Font font)
	{
		if (textlabel != null)
			textlabel.setFont(font);
	}
	
	public Font getFont()
	{
		if (textlabel != null)
			return textlabel.getFont();
		else
			return super.getFont();
	}
	
	public void setBackground(Color bg)
	{
		if (textlabel != null)
			textlabel.setBackground(bg);
		if (imagefield != null)
			imagefield.setBackground(bg);
	}
	
	public void setForeground(Color fg)
	{
		if (textlabel != null)
			textlabel.setForeground(fg);
		if (imagefield != null)
			imagefield.setForeground(fg);
	}
	
	
	
}
