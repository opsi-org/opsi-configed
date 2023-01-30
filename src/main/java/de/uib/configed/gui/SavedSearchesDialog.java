package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.list.ListCellRendererByIndex;
import de.uib.utilities.thread.WaitCursor;

public class SavedSearchesDialog extends FEditList {
	private SelectionManager manager;
	private List<String> result;
	private DefaultListModel<Object> model;

	public SavedSearchesDialog() {
		setTitle(Configed.getResourceValue("SavedSearchesDialog.title") + " (" + Globals.APPNAME + ")");
		setModal(false);
		setLeaveOnCommit(false);
		manager = new SelectionManager(null);
		result = new LinkedList<>();

		model = new DefaultListModel<>();

		setEditable(true);
		setListModel(model);
		resetModel();

		buttonAdd.setVisible(true);
		buttonRemove.setVisible(false);
		extraField.setVisible(false);

	}

	public void start() {
		buttonAdd.setEnabled(true);

		resetModel();
	}

	@Override
	protected void createComponents() {
		super.createComponents();

		// redefine buttonCommit
		buttonCommit.setToolTipText(Configed.getResourceValue("SavedSearchesDialog.ExecuteButtonTooltip"));
		buttonCommit.setIcon(Globals.createImageIcon("images/executing_command_red_22.png", ""));
		buttonCommit.setSelectedIcon(Globals.createImageIcon("images/executing_command_red_22.png", ""));
		buttonCommit.setDisabledIcon(Globals.createImageIcon("images/execute_disabled.png", ""));
		buttonCommit.setPreferredSize(new java.awt.Dimension(buttonWidth, Globals.BUTTON_HEIGHT));

		buttonCancel.setToolTipText(Configed.getResourceValue("SavedSearchesDialog.CancelButtonTooltip"));
	}

	@Override
	protected void initComponents() {
		super.initComponents();

		buttonRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionPerformed");
				removeSelectedEntry();
			}
		});

		buttonRemove.setToolTipText(Configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));

		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "actionPerformed on buttonAdd ");
				addElement();
			}
		});

		JMenuItem reload = new JMenuItemFormatted();

		// find itscontext
		reload.setText(Configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Globals.createImageIcon("images/reload16.png", ""));
		reload.setFont(Globals.defaultFont);
		reload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.debug(this, "------------- reload action");
				reloadAction();
			}
		});
		popup.add(reload);

		JMenuItem remove = new JMenuItemFormatted();

		// find itscontext
		remove.setText(Configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));

		remove.setFont(Globals.defaultFont);
		remove.addActionListener(actionEvent -> {
			Logging.debug(this, "------------- remove action");
			removeSelectedEntry();
		});
		popup.add(remove);

		JMenuItem edit = new JMenuItemFormatted();
		edit.setText(Configed.getResourceValue("SavedSearchesDialog.EditSearchMenu"));
		edit.setFont(Globals.defaultFont);
		edit.addActionListener(actionEvent -> editSearch(visibleList.getSelectedValue().toString()));
		popup.add(edit);

	}

	@Override
	public void setVisible(boolean b) {
		Logging.debug(this, "setVisible " + b);
		super.setVisible(b);
	}

	@Override
	public void setDataChanged(boolean b) {
		boolean active = buttonCommit.isEnabled();
		super.setDataChanged(b);
		buttonCommit.setEnabled(active);
	}

	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.debug(this, "SavedSearchesDialog ListSelectionListener valueChanged " + e);

		super.valueChanged(e);

		if (e.getValueIsAdjusting())
			return;

		buttonCommit.setEnabled(!getSelectedList().isEmpty());
	}

	@Override
	public Object getValue() {
		return result;
	}

	@Override
	protected void commit() {
		result = null;

		buttonCommit.setEnabled(false);
		buttonCancel.setEnabled(false);

		WaitCursor waitCursor = new WaitCursor(this, "SavedSearchesDialog.commit");

		try {

			List<String> selected = getSelectedList();
			if (!selected.isEmpty()) {
				manager.loadSearch(selected.get(0));

				// test:

				result = manager.selectClients();
			}
			super.commit();
		} finally {
			buttonCommit.setEnabled(true);
			buttonCancel.setEnabled(true);
			waitCursor.stop();
		}
	}

	@Override
	protected void cancel() {
		result = null;

		super.cancel();
	}

	private void removeSelectedEntry() {
		int index = visibleList.getSelectedIndex();
		Logging.debug(this, "remove selected Entry, list index " + index);

		if (index == -1)
			return;

		Logging.debug(this, "remove entry at " + index);

		removeSavedSearch((String) model.get(index));
		model.remove(index);
	}

	// overwrite to implement persistency
	protected void removeSavedSearch(String name) {
		manager.removeSearch(name);
	}

	protected void reloadAction() {
		/*override in subclass to implement */}

	// overwrite to implement
	protected void addElement() {
		/*override in subclass to implement */}

	// overwrite to implement
	protected void editSearch(String name) {
		/*override in subclass to implement */}

	public void resetModel() {
		Logging.info(this, "resetModel");
		model.removeAllElements();

		de.uib.opsidatamodel.SavedSearches savedSearches = manager.getSavedSearches();
		TreeSet<String> nameSet = new TreeSet<>(manager.getSavedSearchesNames());
		Map<String, String> valueMap = new HashMap<>();
		Map<String, String> descMap = new HashMap<>();

		for (String ele : nameSet) {
			model.addElement(ele);
			valueMap.put(ele, ele);
			descMap.put(ele, savedSearches.get(ele).getDescription());
		}

		setCellRenderer(new ListCellRendererByIndex(valueMap, descMap, null, false, ""));

		initSelection();
	}

	// interface MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {

		if (e.getClickCount() > 1) {
			commit();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}

}