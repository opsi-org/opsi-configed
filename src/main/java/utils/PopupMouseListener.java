package utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class PopupMouseListener extends MouseAdapter {

	protected JPopupMenu popupMenu;

	public PopupMouseListener(JPopupMenu popup) {
		popupMenu = popup;
	}

	public void mousePressed(MouseEvent e) {
		// System.out.println ( " mouse pressed ");
		maybeShowPopup(e);

	}

	public void mouseReleased(MouseEvent e) {

		// System.out.println ( " mouse released ");
		maybeShowPopup(e);
	}

	protected void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popupMenu.show(e.getComponent(),
					e.getX(), e.getY());
		}
	}
}
