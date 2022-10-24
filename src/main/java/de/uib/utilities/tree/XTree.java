package de.uib.utilities.tree;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import de.uib.utilities.logging.*;


public class XTree extends JTree 
{
	
	public XTree()
	{
		super();
		init();
	}
	
	public XTree(TreeModel model)
	{
		super(model);
		init();
	}
	
	protected void init()
	{
		/*
		addMouseWheelListener(new MouseWheelListener(){
				public void mouseWheelMoved( MouseWheelEvent e )
				{
					//logging.debug(this, "MouseWheelEvent " + e);
					
					int selRow = -1;
					
					if (getSelectionRows() == null || getSelectionRows().length == 0)
					{
						selRow = -1;
					}
					
					else
						selRow = getSelectionRows()[0];
					
					
					//logging.debug(this, "MouseWheelEvent  sel Row " + selRow);
					
					int diff =  e.getWheelRotation();
					
					selRow = selRow + diff;
					//logging.debug(this, "MouseWheelEvent  sel Row " + selRow);
					
					if (selRow >= getRowCount())
						selRow = getRowCount() -1;
					
					int startRow = 0;
					//if (rootVisible) startRow = 1;
					
					if (selRow < startRow)
						selRow = startRow;
					
					setSelectionInterval(selRow,selRow);
					
				}
			}
		);
		*/
		
		MouseMotionListener ml = new MouseAdapter()
		                         {
			                         Cursor infoCursor = new Cursor(Cursor.HAND_CURSOR);
			                         Cursor defaultCursor = getCursor();

			                         @Override
			                         public void mouseMoved(MouseEvent e)
			                         {
				                         TreePath currentPath = getPathForLocation(e.getX(), e.getY());
				                         if (currentPath == null || getModel() == null)
				                         {
					                         setCursor (defaultCursor);
					                         return;
				                         }

				                         //logging.debug(this, "last path component x, y " + e.getX() + ", " + e.getY() + "   "  + currentPath.getLastPathComponent());
				                         
				                         if (getModel().isLeaf (currentPath.getLastPathComponent()))
				                         {
					                         setCursor (infoCursor);
					                         setSelectionPath(currentPath);
				                         }
				                         else
				                         {
					                         setCursor (defaultCursor);
					                         //tableModel.setData(EMPTY); //stays too long
					                   }
			                         }
			                     
		                         };
		addMouseMotionListener(ml);
		
		
	}
		
	
	public Vector<Integer> getToggledRows(TreePath parent)
	//make public
	{
		Vector<Integer> result = new Vector<Integer>();
		Enumeration<TreePath> enumer = super.getDescendantToggledPaths(parent); 
		while ( enumer.hasMoreElements() )
		{
			result.add( getRowForPath( enumer.nextElement() ) );
		}
		return result;
	}
	
	public void expandRows(Vector<Integer> rows)
	{
		logging.debug(this, "expandRows " + rows.size());
		for (Integer row : rows)
		{
			//logging.debug(this, "expand row " + row);
			expandRow( row );
		}
	}
	
	public void expandAll()
	{
		for (int row = 0; row < getRowCount(); row++)
		{
			//logging.debug(this, "expand row " + row);
			expandRow( row );
		}
	}
		
	
} 
