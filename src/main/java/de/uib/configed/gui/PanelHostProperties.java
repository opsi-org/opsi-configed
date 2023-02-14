package de.uib.configed.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.datachanges.UpdateCollection;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.datapanel.AbstractEditMapPanel;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.datapanel.SensitiveCellEditorForDataPanel;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.DefaultListCellOptions;
import de.uib.utilities.table.ListCellOptions;

public class PanelHostProperties extends JPanel implements ItemListener {
	// delegate
	protected AbstractEditMapPanel editMapPanel;
	protected JLabel label;
	protected JComboBox<String> combo;
	protected Map<String, Map<String, Object>> multipleMaps;
	protected UpdateCollection updateCollection;

	public PanelHostProperties() {
		buildPanel();
	}

	private void buildPanel() {
		label = new JLabel(Configed.getResourceValue("MainFrame.jLabel_Config"));
		combo = new JComboBox<>();
		combo.setVisible(false);
		combo.addItemListener(this);
		de.uib.configed.gui.helper.PropertiesTableCellRenderer cellRenderer = new de.uib.configed.gui.helper.PropertiesTableCellRenderer();
		Logging.info(this, "buildPanel, produce editMapPanel");
		editMapPanel = new EditMapPanelX(cellRenderer, false, false);
		((EditMapPanelX) editMapPanel)
				.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName()));
		editMapPanel.setShowToolTip(false);

		JPanel header = new JPanel();

		GroupLayout headerLayout = new GroupLayout(header);
		header.setLayout(headerLayout);

		headerLayout.setHorizontalGroup(headerLayout.createSequentialGroup().addGap(10)
				.addComponent(label, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(10)
				.addComponent(combo, 200, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(10));

		headerLayout.setVerticalGroup(
				headerLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(label).addComponent(combo));

		GroupLayout planeLayout = new GroupLayout(this);
		this.setLayout(planeLayout);

		planeLayout.setHorizontalGroup(planeLayout.createSequentialGroup().addGap(20).addGroup(planeLayout
				.createParallelGroup().addComponent(header, GroupLayout.Alignment.CENTER).addComponent(editMapPanel)

		).addGap(20));

		planeLayout.setVerticalGroup(planeLayout.createSequentialGroup().addGap(20)
				.addComponent(header, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(5)
				.addComponent(editMapPanel, Globals.LINE_HEIGHT * 2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGap(20));
	}

	public void initMultipleHostsEditing(String labeltext, ComboBoxModel<String> comboModel,
			Map<String, Map<String, Object>> multipleMaps, UpdateCollection updateCollection,
			Set<String> keysOfReadOnlyEntries) {
		label.setText(labeltext);
		activateCombo(comboModel);

		Logging.debug(this, "initMultipleHosts " + " configs  " + (multipleMaps)

		);

		this.updateCollection = updateCollection;

		this.multipleMaps = multipleMaps;
		editMapPanel.setUpdateCollection(updateCollection);
		editMapPanel.setReadOnlyEntries(keysOfReadOnlyEntries);

		if (comboModel != null && comboModel.getSize() > 0) {
			setMap(comboModel.getElementAt(0));
		}
	}

	// delegated methods
	public void registerDataChangedObserver(DataChangedObserver o) {
		editMapPanel.registerDataChangedObserver(o);
	}

	public void activateCombo(ComboBoxModel<String> model) {
		if (model != null) {
			combo.setModel(model);
		}

		combo.setEnabled((model != null));
		combo.setVisible((model != null));
	}

	protected Map<String, ListCellOptions> deriveOptionsMap(Map<String, Object> m) {
		Map<String, ListCellOptions> result = new HashMap<>();

		for (Entry<String, Object> entry : m.entrySet()) {

			ListCellOptions cellOptions = null;

			if ((entry.getValue()) instanceof java.lang.Boolean) {
				cellOptions = DefaultListCellOptions.getNewBooleanListCellOptions();
			} else {
				cellOptions = DefaultListCellOptions.getNewEmptyListCellOptions();
			}

			Logging.debug(this, "cellOptions: " + cellOptions);

			result.put(entry.getKey(), cellOptions);
		}
		return result;

	}

	protected void setMap(String selectedItem) {
		List editedMaps = new ArrayList<>(1);
		editedMaps.add(multipleMaps.get(selectedItem));
		Logging.debug(this, "setMap " + multipleMaps.get(selectedItem));
		editMapPanel.setEditableMap(multipleMaps.get(selectedItem), deriveOptionsMap(multipleMaps.get(selectedItem)));
		editMapPanel.setStoreData(editedMaps);
	}

	// item listener
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			setMap((String) combo.getSelectedItem());
		}
	}

}
