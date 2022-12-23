package de.uib.utilities.tree;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.uib.utilities.logging.logging;

public class XTree extends JTree {

	public XTree() {
		super();
		init();
	}

	public XTree(TreeModel model) {
		super(model);
		init();
	}

	protected void init() {
		/*
		 * addMouseWheelListener(new MouseWheelListener(){
		 * public void mouseWheelMoved( MouseWheelEvent e )
		 * {
		 * 
		 * 
		 * int selRow = -1;
		 * 
		 * if (getSelectionRows() == null || getSelectionRows().length == 0)
		 * {
		 * selRow = -1;
		 * }
		 * 
		 * else
		 * selRow = getSelectionRows()[0];
		 * 
		 * 
		 * 
		 * 
		 * int diff = e.getWheelRotation();
		 * 
		 * selRow = selRow + diff;
		 * 
		 * 
		 * if (selRow >= getRowCount())
		 * selRow = getRowCount() -1;
		 * 
		 * int startRow = 0;
		 * 
		 * 
		 * if (selRow < startRow)
		 * selRow = startRow;
		 * 
		 * setSelectionInterval(selRow,selRow);
		 * 
		 * }
		 * }
		 * );
		 */

		MouseMotionListener ml = new MouseAdapter() {
			Cursor infoCursor = new Cursor(Cursor.HAND_CURSOR);
			Cursor defaultCursor = getCursor();

			@Override
			public void mouseMoved(MouseEvent e) {
				TreePath currentPath = getPathForLocation(e.getX(), e.getY());
				if (currentPath == null || getModel() == null) {
					setCursor(defaultCursor);
					return;
				}

				// + " " + currentPath.getLastPathComponent());

				if (getModel().isLeaf(currentPath.getLastPathComponent())) {
					setCursor(infoCursor);
					setSelectionPath(currentPath);
				} else {
					setCursor(defaultCursor);

				}
			}

		};
		addMouseMotionListener(ml);

	}

	public Vector<Integer> getToggledRows(TreePath parent)
	// make public
	{
		Vector<Integer> result = new Vector<>();
		Enumeration<TreePath> enumer = super.getDescendantToggledPaths(parent);
		while (enumer.hasMoreElements()) {
			result.add(getRowForPath(enumer.nextElement()));
		}
		return result;
	}

	public void expandRows(Vector<Integer> rows) {
		logging.debug(this, "expandRows " + rows.size());
		for (Integer row : rows) {

			expandRow(row);
		}
	}

	public void expandAll() {
		for (int row = 0; row < getRowCount(); row++) {

			expandRow(row);
		}
	}

}
