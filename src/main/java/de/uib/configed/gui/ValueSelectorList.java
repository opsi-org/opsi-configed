package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TablesearchPane;

public class ValueSelectorList extends JPanel implements ActionListener {
	private DepotsList valueList;
	private JScrollPane scrollPaneValueList;
	// this will not be shown in this panel but exported for use in other panels

	private JLabel labelValue;
	private JButton buttonSelectValuesWithEqualProperties;
	private JButton buttonSelectValuesAll;

	private TablesearchPane searchPane;

	private boolean multidepot;

	private AbstractPersistenceController persist;

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public ValueSelectorList(DepotsList valueList, boolean multidepot, AbstractPersistenceController persist) {
		this.valueList = valueList;
		this.multidepot = multidepot;
		this.persist = persist;

		List<String> values = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		Map<String, Map<String, Object>> depotInfo = valueList.getDepotInfo();

		for (Entry<String, Map<String, Object>> depotEntry : depotInfo.entrySet()) {
			values.add(depotEntry.getKey());
			if (depotEntry.getValue() == null || depotEntry.getValue().get("description") == null) {
				descriptions.add("");
			} else {
				descriptions.add((String) depotEntry.getValue().get("description"));
			}
		}

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(valueList, values, descriptions);

		searchPane = new TablesearchPane(searchTargetModel, "depotlist");
		searchPane.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);
		searchPane.setSearchFields(new Integer[] { 0, 1 });
		searchPane.setToolTipTextCheckMarkAllColumns(
				Configed.getResourceValue("ValueSelectorList.checkmarkAllColumns.tooltip"));

		initComponents();
		layouting();

	}

	/**
	 * exports the scrollpane which is produced in this class but displayed in
	 * other components
	 *
	 * @return a scrollpane which shows the depotslist
	 */
	public JScrollPane getScrollpaneDepotslist() {
		return scrollPaneValueList;
	}

	/**
	 * gives the color for setting back color of this panel in master panel
	 * 
	 * @return java.awt.Color
	 */
	public Color getMyColor() {
		return Globals.SECONDARY_BACKGROUND_COLOR;
	}

	/**
	 * allows to show that a depot selection change is in progress
	 * 
	 * @param boolean We are in progress
	 */
	public void setChangedDepotSelectionActive(boolean active) {
		if (!ConfigedMain.THEMES) {

			if (active) {
				valueList.setBackground(Globals.BACKGROUND_COLOR_9);
			} else {
				valueList.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
			}

			// colorize as hint that we have changed the depots selection
		}
	}

	private void initComponents() {
		labelValue = new JLabel();
		if (multidepot) {
			labelValue.setText(Configed.getResourceValue("ValueSelectorList.values"));
		} else {
			labelValue.setText(Configed.getResourceValue("ValueSelectorList.value"));
		}

		labelValue.setOpaque(false);

		if (!ConfigedMain.THEMES) {
			labelValue.setBackground(Globals.BACKGROUND_COLOR_7);
		}
		if (!ConfigedMain.FONT) {
			labelValue.setFont(Globals.defaultFontStandardBold);
		}

		buttonSelectValuesWithEqualProperties = new JButton("", Globals.createImageIcon("images/equalplus.png", ""));
		buttonSelectValuesWithEqualProperties
				.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectValuesWithEqualProperties"));
		Globals.formatButtonSmallText(buttonSelectValuesWithEqualProperties);
		buttonSelectValuesWithEqualProperties.addActionListener(this);
		buttonSelectValuesWithEqualProperties.setEnabled(multidepot);

		buttonSelectValuesAll = new JButton("", Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectValuesAll.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectValuesAll"));
		Globals.formatButtonSmallText(buttonSelectValuesAll);
		buttonSelectValuesAll.addActionListener(this);
		buttonSelectValuesAll.setEnabled(multidepot);

		searchPane.setFieldFont(Globals.defaultFont);
		searchPane.setFieldBackground(getMyColor());
		if (!multidepot) {
			searchPane.setEnabled(false);
		}

		if (!ConfigedMain.THEMES) {
			searchPane.setBackground(getMyColor());
		}
		searchPane.setNarrow(true);

		// not visible in this panel

		valueList.setMaximumSize(new Dimension(200, 400));

		scrollPaneValueList = new JScrollPane();
		scrollPaneValueList.getViewport().add(valueList);
		scrollPaneValueList.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneValueList.setPreferredSize(valueList.getMaximumSize());

		if (!ConfigedMain.FONT) {
			valueList.setFont(Globals.defaultFont);
		}
		if (!ConfigedMain.THEMES) {
			valueList.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
	}

	private void layouting() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5, 5, 10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(labelValue,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				).addGap(5, 5, 10).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(searchPane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(5, 5, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(10)
						.addComponent(labelValue, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

						.addGap(10, 10, 10))
				.addGroup(layout.createSequentialGroup().addGap(5, 5, 5)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(5, 5, 5)));
	}

	public String getSelectedValue() {
		return valueList.getSelectedValue();
	}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonSelectValuesAll) {
			Logging.info(this, "action on buttonSelectValuesAll");

			valueList.selectAll();
		} else if (e.getSource() == buttonSelectValuesWithEqualProperties) {
			Logging.info(this, "action on buttonSelectValuesWithEqualProperties");

			if (valueList.getSelectedIndex() > -1) {
				String depotSelected = valueList.getSelectedValue();
				List<String> depotsWithEqualStock = persist.getAllDepotsWithIdenticalProductStock(depotSelected);
				valueList.addToSelection(depotsWithEqualStock);

			}
		}
	}
}
