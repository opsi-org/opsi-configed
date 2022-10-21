package de.uib.configed.tree;

import de.uib.configed.*;
import java.util.*;
import javax.swing.JList;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.jdesktop.swingx.*; 
import de.uib.utilities.logging.*;

public class TreePopupMouseListener extends utils.PopupMouseListener
{
	
	protected ClientTree tree;
	protected Integer acceptedMouseButton;
	
	protected int mouseRow = -1;
	protected  TreePath mousePath = null;
	
	protected JPopupMenu myMenu;
	
	static public int createSubnodePosition = -1;
	static public int editNodePosition = -1;
	static public int deleteNodePosition = -1;
	static public int deleteGroupNodePosition = -1;
	static public int activateElementsPosition = -1;
	static public int removeElementsPosition = -1;
	
	
	
	public TreePopupMouseListener (JPopupMenu popup, ClientTree tree, Integer acceptedMouseButton)
	{
		super(popup);
		myMenu = popup;
		this.tree = tree;
		this.acceptedMouseButton = acceptedMouseButton;
	}
	
	public int getPopupSourceRow()
	{
		return mouseRow;
	}
	
	public TreePath getPopupSourcePath()
	{	
		return mousePath;
	}
	
	
	protected boolean checkAccepted(MouseEvent e) 
	{
		if (!tree.isEnabled())
			return false;
		
		if (acceptedMouseButton != null 	// use criterion 
			&& e.getButton() != acceptedMouseButton) //we accept only one button type
			return false;
		
		mouseRow = tree.getRowForLocation(e.getX(), e.getY());
		mousePath = tree.getPathForLocation(e.getX(), e.getY());
		
		if (mouseRow == -1) //no node selection area
		{
			mousePath = null;
			return false;
		}
		
		
		logging.debug(this, "checkAccepted clickPath  "+ mousePath);
			
		DefaultMutableTreeNode clickNode = (DefaultMutableTreeNode) mousePath.getLastPathComponent(); 
		
		String nodeName = clickNode.getUserObject().toString();
		
		if (tree.getGroupNode(nodeName) != null)
		{
			
			if (clickNode != tree.getGroupNode(nodeName))
			{
				logging.warning(this, "checkAccepted clickNode != tree.getGroupNode(nodeName)");
				clickNode =tree.getGroupNode(nodeName);
			}
		}
		
		logging.debug(this, "checkAccepted clickNode.getParent() " + clickNode.getParent()); 
		
		DefaultMutableTreeNode parentNode =(DefaultMutableTreeNode)(clickNode.getParent());
		String parentName = parentNode.getUserObject().toString();
		
		if (
			//nodeName.equals(ClientTree.FAILED_NAME)
			//||
			nodeName.equals(ClientTree.ALL_NAME)
			||
				(
					((DefaultMutableTreeNode)clickNode.getParent()).getUserObject().toString().equals(ClientTree.ALL_NAME)
					&&
					!nodeName.equals(ClientTree.GROUPS_NAME)
				)
			)
			
			return false; //dont show here any menu
		
		
		((JMenuItem) myMenu.getSubElements()[createSubnodePosition]).setVisible(false); //creation of subgroup
		((JMenuItem) myMenu.getSubElements()[editNodePosition]).setVisible(false); //edit
		((JMenuItem) myMenu.getSubElements()[deleteNodePosition]).setVisible(false); //deletion
		((JMenuItem) myMenu.getSubElements()[deleteGroupNodePosition]).setVisible(false); //deletion
		((JMenuItem) myMenu.getSubElements()[removeElementsPosition]).setVisible(false); //removal of non-groupnode elements
		((JMenuItem) myMenu.getSubElements()[activateElementsPosition]).setVisible(false); //edit
		
		int countVisibleItems = 0;
		
		if (clickNode.getAllowsChildren() )
		{
			//logging.info(this, "checkAccepted " + clickNode + " is GroupNode " + (clickNode instanceof GroupNode) ) 
			
			if ( ((GroupNode)clickNode).allowsSubGroups() )
			{
				((JMenuItem) myMenu.getSubElements()[createSubnodePosition]).setVisible(true); //creation of subgroup
				countVisibleItems ++;
			}
			
			if ( ((GroupNode)clickNode).allowsSubGroups()  && !( ((GroupNode)clickNode ).isFixed() ) )
			{
				((JMenuItem) myMenu.getSubElements()[editNodePosition]).setVisible(true); //edit this node
				countVisibleItems ++;
			}
			
			if ( !( ((GroupNode)clickNode ).isFixed() ) )
			{
				((JMenuItem) myMenu.getSubElements()[deleteGroupNodePosition]).setVisible(true); //deletion
				countVisibleItems ++;
			}
			
			{
				((JMenuItem) myMenu.getSubElements()[activateElementsPosition]).setVisible(true); //activate elements
				countVisibleItems ++;
			}
			
			//if ( ((GroupNode)clickNode).allowsSubGroups() )
			if ( !( ((GroupNode)clickNode ).isFixed() ) ) 
			{
				((JMenuItem) myMenu.getSubElements()[removeElementsPosition]).setVisible(true); //delete non-groupnode elements
				countVisibleItems ++;
			}
			
			
		}
		else
		{
			if ( !( ((GroupNode)parentNode ).isFixed() ) ) 
			{
				((JMenuItem) myMenu.getSubElements()[deleteNodePosition]).setVisible(true);
				countVisibleItems ++;
			}
		}
		
		
		//return true;
		return (countVisibleItems  > 0);
	}
	
	
	protected void maybeShowPopup(MouseEvent e) 
	{
		if (checkAccepted(e))
			super.maybeShowPopup(e);
	}
	
}
