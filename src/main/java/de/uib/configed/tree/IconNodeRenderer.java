package de.uib.configed.tree;

import de.uib.configed.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JTree;
import javax.swing.tree.*;


public class IconNodeRenderer extends DefaultTreeCellRenderer
{
	public static int labelWidth = 200;
	public static int labelHeight = 20;
	
	public IconNodeRenderer()
	{
		super();
		setOpaque(true);
		setForeground(Globals.lightBlack);
		setTextSelectionColor(Globals.lightBlack);
		setBackground(Color.white);
		setBorder(new javax.swing.border.EmptyBorder( new Insets(0,0,0,0)));
		setPreferredSize(new java.awt.Dimension(labelWidth, labelHeight));
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
							boolean sel,
							boolean expanded,
							boolean leaf, 
							int row,
							boolean hasFocus)
	{
		if (value instanceof IconNode)
		{
		
			String stringValue =
				//configed.encodeStringFromService (
					tree.convertValueToText(value, sel,expanded, leaf, row, hasFocus)
				//	)
				;
				
			
			setText(stringValue);
			
			//adaption to size of bold font??
			
			
			//Attention: must be a IconNode
			IconNode node = (IconNode)value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);
			
			if (sel)
			{
				setBackground(Globals.backLightBlue);
				//setFont(Globals.defaultFontBold);
				//setForeground(Color.blue);//Globals.unknownBlue);
			}
			else
			{
				setBackground(Color.white);
				//setBackground(Globals.backgroundWhite);
				//setFont(Globals.defaultFontBig);
				//setForeground(Globals.lightBlack);
			}
			
			if (leaf)
			{
				setIcon(node.getLeafIcon());
			}
			else if (expanded)
			{
				setIcon(node.getOpenIcon());
			}
			else
			{
				setIcon(node.getClosedIcon());
			}
			
			setComponentOrientation(tree.getComponentOrientation());
			return this;
		}
		
		return this;
		
		/*
		Component c = super.getTreeCellRendererComponent(
			tree, 
			value,
			sel,
			expanded,
			leaf, 
			row,
			hasFocus);
		
		//c.setBackground(Globals.backLightBlue);
		
		return c; 
		
		*/
	}
}
