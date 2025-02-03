/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utils.Icons;
import de.uib.utils.datapanel.DefaultEditMapPanel;

public class PanelEditClientProperties extends AbstractPanelEditProperties {
	private JLabel jLabelProductProperties;
	private JButton buttonSetValuesFromServerDefaults;
	private JButton buttonRemoveSpecificValues;

	private JPanel titlePanel;

	public PanelEditClientProperties(DefaultEditMapPanel productPropertiesPanel) {
		super(productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	private void initComponents() {
		jLabelProductProperties = new JLabel(Configed.getResourceValue("ProductInfoPane.jLabelProductProperties"));

		buttonSetValuesFromServerDefaults = new JButton(Icons.getIntellijIcon("locked"));
		buttonSetValuesFromServerDefaults
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromServerDefaults"));
		buttonSetValuesFromServerDefaults.addActionListener(actionEvent -> productPropertiesPanel.resetDefaults());

		buttonRemoveSpecificValues = new JButton(Icons.getIntellijIcon("remove"));
		buttonRemoveSpecificValues
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonRemoveSpecificValues"));
		buttonRemoveSpecificValues.addActionListener(actionEvent -> productPropertiesPanel.setVoid());

		GroupLayout layoutEditProperties = new GroupLayout(this);
		setLayout(layoutEditProperties);

		layoutEditProperties.setHorizontalGroup(layoutEditProperties.createSequentialGroup()
				.addComponent(productPropertiesPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutEditProperties.setVerticalGroup(layoutEditProperties.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(productPropertiesPanel, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createSequentialGroup()
				.addComponent(jLabelProductProperties, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(buttonRemoveSpecificValues, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
				.addGap(Globals.MIN_GAP_SIZE));

		titleLayout.setVerticalGroup(titleLayout.createSequentialGroup()
				.addGroup(titleLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelProductProperties, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonRemoveSpecificValues, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean activated) {
		buttonSetValuesFromServerDefaults.setEnabled(activated);
		buttonRemoveSpecificValues.setEnabled(activated);
	}
}
