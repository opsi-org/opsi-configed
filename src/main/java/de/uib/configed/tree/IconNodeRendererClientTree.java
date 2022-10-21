package de.uib.configed.tree;

import de.uib.configed.*;
import de.uib.utilities.logging.*;
import java.util.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;


public class IconNodeRendererClientTree extends IconNodeRenderer
{
	
	protected ConfigedMain main;
	
	public static int labelWidth = 200;
	public static int labelHeight = 20;
	
	
	VisualClientNodeNameModifier modifier 
		= new VisualClientNodeNameModifierFactory().getModifier();
		
	public IconNodeRendererClientTree(ConfigedMain main)
	{
		this.main = main;
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
		setBackground(Color.white);
		if (value instanceof IconNode)
		{
			String stringValue =
				//configed.encodeStringFromService (
					tree.convertValueToText(value, sel,expanded, leaf, row, hasFocus)
				//	)
				;
			
			setText(stringValue);
			setToolTipText( ((IconNode) value).getToolTipText());
			
			/*
			//adaption to size of bold font
			java.awt.Dimension newSize = new java.awt.Dimension();
			newSize.setSize(getPreferredSize().getWidth() * 1.3, getPreferredSize().getHeight());
			setPreferredSize(newSize);
			System.out.println("--- newSize  " + stringValue  + " *** "  + newSize.getWidth());
			*/
			
			//Attention: must be a IconNode
			IconNode node = (IconNode)value;
			boolean enabled = tree.isEnabled();
			setEnabled(enabled);
			
			node.setEnabled(enabled);
			
			//logging.info(this, "---  value " + stringValue);
			//logging.info(this, "---  main " + main);
			//logging.info(this, "---  main.getSelectedClients() " + main.getSelectedClients());
			//logging.info(this, "---  main.getSelectedClients().contains((stringValue) " + main.getSelectedClients().contains(stringValue));
			
			//System.out.println (stringValue + " selected! ");
				
			if (!node.getAllowsChildren())  // client
			{
				//System.out.println( " main.getActiveTreeNodes().containsKey(value) " + main.getActiveTreeNodes().containsKey(value) );
				if (
					//sel  
					//||
					//(main.getSelectedClientsInTable().contains(stringValue)
					main.getActiveTreeNodes().containsKey(stringValue)
					)
				{
					setFont(Globals.defaultFontStandardBold);
					
					setIcon(node.getLeafIcon());
					//setBackground((Color) UIManager.get("controlHighlight"));
				}
				else
				{
					//setText( stringValue + "--xxx" );
					setFont(Globals.defaultFont);
					setIcon(node.getNonSelectedLeafIcon());
					//setBackground(Color.white);
					
					//setFont(Globals.defaultFontBig);
					//setForeground(getTextNonSelectionColor());
				}
			}
			else // group
			{
				String visualText = modifier.modify( stringValue ); 
				
				//eliminate_appending_visual_underscores( stringValue );
				
				//logging.info(this, "group name, possibly shortened " + visualText); 
				setText( visualText );
				
				
				
				setIcon(node.getClosedIcon());  //default,will be changed, if clients are childs
				
				if(
					main.getActiveParents().contains(stringValue)
					)
				{
					setIcon(node.getEmphasizedIcon());
				}
				
				if (
					//sel  
					//||
					//(main.getSelectedClientsInTable().contains(stringValue)
						main.getActiveTreeNodes().containsKey(stringValue)
					)
				{
					setFont(Globals.defaultFontStandardBold);
					
					//setBackground((Color) UIManager.get("controlHighlight"));
				}
				else
				{
					setFont(Globals.defaultFont);
					//setBackground(Color.white);
					
					//setFont(Globals.defaultFontBig);
					//setForeground(getTextNonSelectionColor());
					
				}
			}
				
			/*
			if (stringValue.equals( "FAILED"))
				setForeground(Color.RED);
			else
				setForeground(Color.BLACK);
			*/
				/*
			if (expanded)
			{
				setIcon(node.getOpenIcon());
			}
			else
			{
				setIcon(node.getClosedIcon());
			}
			*/
			
			
			
			if (
				tree.getSelectionPath() != null
				&&
				node.equals(tree.getSelectionPath().getLastPathComponent())
				&&
				tree.hasFocus()
			)
				
			{
				//logging.info(this, " we are at this place ");
				//setText(stringValue + "<");
				//setBackground(Globals.backNimbusLight);
				//setFont( getFont().deriveFont( java.awt.Font.ITALIC ));
				Map attributes = getFont().getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				setFont(getFont().deriveFont(attributes));
				
			}
			
			
			
			
			setComponentOrientation(tree.getComponentOrientation());
			return this;
		}
		
		return super.getTreeCellRendererComponent(
			tree, 
			value,
			sel,
			expanded,
			leaf, 
			row,
			hasFocus);
		
	}
}
