package de.uib.utilities.tree;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.uib.utilities.logging.Logging;

public class XTree extends JTree {

	public XTree() {
		super();
		init();
	}

	public XTree(TreeModel model) {
		super(model);
		init();
	}

	private void init() {

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

	public List<Integer> getToggledRows(TreePath parent)
	// make public
	{
		List<Integer> result = new ArrayList<>();
		Enumeration<TreePath> enumer = super.getDescendantToggledPaths(parent);
		while (enumer.hasMoreElements()) {
			result.add(getRowForPath(enumer.nextElement()));
		}
		return result;
	}

	public void expandRows(List<Integer> rows) {
		Logging.debug(this, "expandRows " + rows.size());
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
