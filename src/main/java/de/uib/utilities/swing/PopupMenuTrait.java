package de.uib.utilities.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class PopupMenuTrait extends JPopupMenu {

	public static final int POPUP_SEPARATOR = 0;
	public static final int POPUP_RELOAD = 4;

	public static final int POPUP_SAVE = 8;
	public static final int POPUP_SAVE_AS_ZIP = 9;

	public static final int POPUP_SAVE_LOADED_AS_ZIP = 10;
	public static final int POPUP_SAVE_ALL_AS_ZIP = 11;

	public static final int POPUP_DELETE = 13;
	public static final int POPUP_ADD = 14;

	public static final int POPUP_FLOATINGCOPY = 20;

	public static final int POPUP_PDF = 21;

	public static final int POPUP_EXPORT_CSV = 23;
	public static final int POPUP_EXPORT_SELECTED_CSV = 24;

	// public static final int POPUP_EXPORT_EXCEL = 25;
	// public static final int POPUP_EXPORT_SELECTED_EXCEL = 26;

	public static final int POPUP_PRINT = 30;

	protected JMenuItemFormatted menuItemReload;
	protected Integer[] popups;
	protected java.util.List<Integer> listPopups;

	protected JMenuItemFormatted[] menuItems;

	protected JPopupMenu popupMenu;

	public PopupMenuTrait(Integer[] popups) {
		this.popups = popups;
		listPopups = Arrays.asList(popups);

		menuItems = new JMenuItemFormatted[popups.length];

		for (int i = 0; i < popups.length; i++) {
			addPopup(popups[i]);
		}
	}

	protected void addPopup(final int p) {
		int i;
		switch (p) {
		case POPUP_RELOAD:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.reload"),
					Globals.createImageIcon("images/reload16.png", ""));
			// menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does
			// not work

			addItem(p);

			break;

		case POPUP_FLOATINGCOPY:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.floatingInstance"),
					Globals.createImageIcon("images/edit-copy.png", ""));
			// menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does
			// not work

			addSeparator();
			addItem(p);

			break;

		case POPUP_SAVE:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.save"),
					Globals.createImageIcon("images/save.png", ""));
			// menuItems[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does
			// not work

			addItem(p);

			break;

		case POPUP_SAVE_AS_ZIP:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.saveAsZip"),
					Globals.createImageIcon("images/zip-icon.png", ""));

			addItem(p);

			break;

		case POPUP_SAVE_LOADED_AS_ZIP:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.saveLoadedAsZip"),
					Globals.createImageIcon("images/zip-icon.png", ""));

			addItem(p);

			break;

		case POPUP_SAVE_ALL_AS_ZIP:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PopupMenuTrait.saveAllAsZip"),
					Globals.createImageIcon("images/zip-icon.png", ""));

			addItem(p);

			break;

		case POPUP_PDF:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("FGeneralDialog.pdf"),
					Globals.createImageIcon("images/acrobat_reader16.png", ""));

			addItem(p);

			break;

		case POPUP_EXPORT_CSV:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.exportTableAsCSV")
			// , Globals.createImageIcon("images/acrobat_reader16.png", "")
			);

			addItem(p);

			break;

		case POPUP_EXPORT_SELECTED_CSV:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted(configed.getResourceValue("PanelGenEditTable.exportSelectedRowsAsCSV")
			// , Globals.createImageIcon("images/acrobat_reader16.png", "")
			);

			addItem(p);

			break;

		case POPUP_DELETE:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted("delete", Globals.createImageIcon("images/edit-delete.png", ""));

			addItem(p);

			break;

		case POPUP_ADD:
			i = listPopups.indexOf(p);
			menuItems[i] = new JMenuItemFormatted("add", Globals.createImageIcon("images/list-add.png", ""));

			addItem(p);

			break;

		default:
			logging.info(this, "popuptype " + p + " not implemented");

		}
	}

	public void setText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			logging.info(this, "setText - popup " + popup + " not in list");
			return;
		}

		menuItems[i].setText(s);
	}

	public void setToolTipText(int popup, String s) {
		int i = listPopups.indexOf(popup);
		if (i < 0) {
			logging.info(this, "setToolTipText - popup " + popup + " not in list");
			return;
		}

		menuItems[i].setToolTipText(s);
	}

	protected void addItem(final int p) {
		int i = listPopups.indexOf(p);
		menuItems[i].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action(p);
			}
		});

		add(menuItems[i]);

	}

	public void addPopupListenersTo(JComponent[] components) {
		for (int i = 0; i < components.length; i++) {
			components[i].addMouseListener(new utils.PopupMouseListener(this));
		}
	}

	public void action(int p)
	// should be overwritten for specific actions in subclasses
	{
		logging.debug(this, "action called for type " + p);
	}
}
