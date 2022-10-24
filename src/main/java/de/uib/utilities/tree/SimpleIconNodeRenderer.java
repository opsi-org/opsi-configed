package de.uib.utilities.tree;

import de.uib.configed.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;


public class SimpleIconNodeRenderer extends DefaultTreeCellRenderer
{
	public static int labelWidth = 300;
	public static int labelHeight = 22;
	protected Font emphasized;
	protected Font standard;
	public static Dimension preferred = new Dimension(labelWidth, labelHeight);
	
	
	private Font deriveFont0(Font font)
	{
		HashMap<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
		return font.deriveFont(map);
	}
	
	private Font deriveFont1(Font font)
	{
		HashMap<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
		//map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		//map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
		//map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
		return font.deriveFont(map);
	}
		
	public SimpleIconNodeRenderer()
	{
		super();
		//setHorizontalTextPosition(SwingConstants.LEFT);
		setOpaque(true);
		
		//standard = deriveFont0(Globals.defaultFontBig);
		//emphasized = deriveFont1(Globals.defaultFontBig);
		standard = Globals.defaultFontBig;
		//emphasized = deriveFont1(standard); //
		emphasized = Globals.defaultFontStandardBold;
		
		setFont(standard);
		setForeground(Globals.lightBlack);
		setTextSelectionColor(Globals.lightBlack);
		setBackground(Color.white);
		setBorder(new javax.swing.border.EmptyBorder( new Insets(0,0,0,0)));
		setPreferredSize(preferred);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
							boolean sel,
							boolean expanded,
							boolean leaf, 
							int row,
							boolean hasFocus)
	{
		
		/*
		if (row == 0)
			setPreferredSize(new java.awt.Dimension(labelWidth, 50));
		else
			setPreferredSize(preferred);
		*/
		
		if (value instanceof SimpleIconNode)
		{
		
			String stringValue =
				//configed.encodeStringFromService (
					tree.convertValueToText(value, sel,expanded, leaf, row, hasFocus)
				//	)
				;
				
			
			setText(stringValue);
			setToolTipText( ((SimpleIconNode) value).getToolTipText() );
			
			//adaption to size of bold font??
			
			
			//Attention: must be a SimpleIconNode
			SimpleIconNode node = (SimpleIconNode)value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			node.setEnabled(enabled);
			
			
			if (row == 0)
			{
				setFont(emphasized);
			}
			else 
			{
				setFont(standard);
			}
			
				
			
			if (sel  && row != 0) //assuming that row 0 contains sort of header
			{
				setBackground(Globals.backLightBlue);
				//setFont(emphasized);
				//setForeground(Color.blue);//Globals.unknownBlue);
			}
			else
			{
				setBackground(Color.white);
				//setBackground(Globals.backgroundWhite);
				//setFont(standard);
				//setForeground(Globals.lightBlack);
			}
			
			if (leaf)
			{
				setIcon(node.getLeafIcon());
			}
			else 
				
			{
				if (expanded)
				{
					setIcon(node.getOpenIcon());
				}
				else
				{
					setIcon(node.getClosedIcon());
				}
				
			}
			
			if (!sel)
				setIcon(node.getNonSelectedIcon());
					
			
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
