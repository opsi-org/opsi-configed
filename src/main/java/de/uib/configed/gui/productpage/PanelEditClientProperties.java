package de.uib.configed.gui.productpage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.IconButton;

public class PanelEditClientProperties extends DefaultPanelEditProperties {
	private javax.swing.JLabel jLabelProductProperties;
	private IconButton buttonSetValuesFromServerDefaults;
	private IconButton buttonRemoveSpecificValues;

	private JPanel titlePanel;

	public PanelEditClientProperties(ConfigedMain mainController,
			de.uib.utilities.datapanel.AbstractEditMapPanel productPropertiesPanel) {
		super(mainController, productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	protected void initComponents() {
		jLabelProductProperties = new JLabel(configed.getResourceValue("ProductInfoPane.jLabelProductProperties"));
		jLabelProductProperties.setFont(Globals.defaultFontBold);
		jLabelProductProperties.setForeground(Globals.lightBlack);

		buttonSetValuesFromServerDefaults = new IconButton(
				configed.getResourceValue("ProductInfoPane.buttonSetValuesFromServerDefaults"),
				"images/reset_network_defaults.png", "images/reset_network_defaults_over.png", " ", true);

		buttonSetValuesFromServerDefaults.setPreferredSize(new Dimension(15, 30));

		buttonSetValuesFromServerDefaults.addActionListener((ActionEvent e) -> productPropertiesPanel.resetDefaults());

		buttonRemoveSpecificValues = new IconButton(
				configed.getResourceValue("ProductInfoPane.buttonRemoveSpecificValues"),
				"images/reset_network_eliminate.png", /* "images/edit-delete.png", */
				"images/reset_network_eliminate_over.png", /* "images/edit-delete_over.png", */
				"images/reset_network_eliminate_disabled.png", true);

		buttonRemoveSpecificValues.setPreferredSize(new Dimension(15, 30));

		buttonRemoveSpecificValues.addActionListener((ActionEvent e) -> productPropertiesPanel.setVoid());

		javax.swing.GroupLayout layoutEditProperties = new javax.swing.GroupLayout(this);
		setLayout(layoutEditProperties);

		layoutEditProperties
				.setHorizontalGroup(layoutEditProperties.createSequentialGroup().addGap(hGapSize, hGapSize, hGapSize)
						.addGroup(layoutEditProperties.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layoutEditProperties.createSequentialGroup()
										.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
										// .addComponent(jLabelProductProperties, minHSize, prefHSize,
										// Short.MAX_VALUE)
										.addGap(hGapSize, hGapSize, hGapSize)
										// .addComponent(buttonSetValuesFromServerDefaults, 20, 20, 20)
										.addGap(2, 2, 2)
								// .addComponent(buttonRemoveSpecificValues, 20, 20, 20)
								).addComponent(productPropertiesPanel, minHSize, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))
						.addGap(0, hGapSize, hGapSize));

		layoutEditProperties
				.setVerticalGroup(layoutEditProperties.createSequentialGroup().addGap(minGapVSize, vGapSize, vGapSize)
						.addGroup(layoutEditProperties.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						// .addComponent(jLabelProductProperties, 0, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE)
						// .addComponent(buttonSetValuesFromServerDefaults, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addComponent(buttonRemoveSpecificValues, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						).addGap(minGapVSize, minGapVSize, minGapVSize).addComponent(productPropertiesPanel,
								minTableVSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createSequentialGroup()
				.addComponent(jLabelProductProperties, minHSize, prefHSize, Short.MAX_VALUE)
				.addGap(hGapSize, hGapSize, hGapSize).addComponent(buttonSetValuesFromServerDefaults, 20, 20, 20)
				.addGap(2, 2, 2).addComponent(buttonRemoveSpecificValues, 20, 20, 20));

		titleLayout.setVerticalGroup(titleLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelProductProperties, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonRemoveSpecificValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean activated) {
		jLabelProductProperties.setForeground(activated ? Globals.lightBlack : Globals.greyed);
		buttonSetValuesFromServerDefaults.setEnabled(activated);
		buttonRemoveSpecificValues.setEnabled(activated);
	}

	public void setSpecificPropertiesExisting(String productName, Map<String, Boolean> specificPropertiesExisting) {

		setPropertyResetActivated(false);
		if (specificPropertiesExisting != null && productName != null
				&& specificPropertiesExisting.get(productName) != null)
			setPropertyResetActivated(specificPropertiesExisting.get(productName));
	}

	protected void setPropertyResetActivated(boolean b) {

		buttonRemoveSpecificValues.setEnabled(b);
	}
}
