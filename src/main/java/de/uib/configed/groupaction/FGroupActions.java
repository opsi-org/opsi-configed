/* 
 * FGroupActions 
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.configed.groupaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.IconButton;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;
import de.uib.utilities.thread.WaitCursor;

public class FGroupActions extends SecondaryFrame {
	JPanel topPanel;
	JPanel imageActionPanel;

	JTextField fieldGroupname;
	JTextField fieldInvolvedClientsCount;

	JComboBox comboSelectImage;

	List<String> associatedClients;

	PersistenceController persist;
	ConfigedMain main;

	int hFirstGap = Globals.HFIRST_GAP;

	int firstLabelWidth = Globals.BUTTON_WIDTH;

	public FGroupActions(ConfigedMain main, PersistenceController persist) {
		super();

		this.main = main;
		this.persist = persist;

		define();
		reload();
		setGlobals(Globals.getMap());
		setTitle(Globals.APPNAME + " " + configed.getResourceValue("FGroupAction.title"));

	}

	protected void setGroupLabelling(String label, String clientCount) {
		fieldGroupname.setText(label);
		fieldInvolvedClientsCount.setText(clientCount);
	}

	@Override
	public void start() {
		super.start();
		reload();
	}

	protected void setImages() {
		List<String> imagesCollection = new ArrayList<>();

		imagesCollection.addAll(new TreeSet<>(persist.getCommonProductPropertyValues(associatedClients,
				PersistenceController.localImageRestoreProductKey, PersistenceController.localImagesListPropertyKey)));

		comboSelectImage.setModel(new DefaultComboBoxModel<>(imagesCollection.toArray()));
	}

	private void reload() {
		setGroupLabelling(main.getActivatedGroupModel().getLabel(),
				"" + main.getActivatedGroupModel().getNumberOfClients());

		associatedClients = new ArrayList<>(main.getActivatedGroupModel().getAssociatedClients());
		setImages();
	}

	protected void replay() {
		logging.debug(this, "replay " + comboSelectImage.getSelectedItem());

		if (comboSelectImage.getSelectedItem() == null)
			return;

		String image = (String) comboSelectImage.getSelectedItem();

		List<String> values = new ArrayList<>();
		values.add(image); // selected from common product property values

		WaitCursor waitCursor = new WaitCursor(this);

		persist.setCommonProductPropertyValue(main.getActivatedGroupModel().getAssociatedClients(),
				PersistenceController.localImageRestoreProductKey, PersistenceController.localImageToRestorePropertyKey,
				values);

		Map<String, String> changedValues = new HashMap<>();
		changedValues.put(de.uib.opsidatamodel.productstate.ProductState.KEY_actionRequest, "setup");

		persist.updateProductOnClients(main.getActivatedGroupModel().getAssociatedClients(),
				PersistenceController.localImageRestoreProductKey, OpsiPackage.TYPE_NETBOOT, changedValues);

		waitCursor.stop();
	}

	protected void define() {
		topPanel = new JPanel();

		defineTopPanel(topPanel);

		imageActionPanel = new JPanel();
		imageActionPanel.setBorder(Globals.createPanelBorder());

		defineImageActionPanel(imageActionPanel);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(topPanel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		layout.setHorizontalGroup(
				layout.createParallelGroup().addComponent(topPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(imageActionPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		Containership cs_all = new Containership(getContentPane());
		cs_all.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
				JPanel.class);

		cs_all.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_3 },
				javax.swing.text.JTextComponent.class);

	}

	private void defineImageActionPanel(JPanel panel) {
		JLabel labelCombo = new JLabel(configed.getResourceValue("FGroupAction.existingImages"));

		comboSelectImage = new JComboBox<>();

		JLabel topicLabel = new JLabel(configed.getResourceValue("FGroupAction.replayImage"));

		JButton buttonSetup = new JButton(configed.getResourceValue("FGroupAction.buttonSetup"));
		buttonSetup.setToolTipText(configed.getResourceValue("FGroupAction.buttonSetup.tooltip"));

		buttonSetup.addActionListener(actionEvent -> replay());

		IconButton buttonReload = new IconButton(configed.getResourceValue("FGroupAction.buttonReload"),
				"images/reload16.png", "images/reload16_over.png", "images/reload16_disabled.png", true);

		buttonReload.addActionListener(actionEvent -> reload());

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE * 3, Globals.VGAP_SIZE * 4)
				.addComponent(
						topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSetup, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonReload, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE * 3, Globals.VGAP_SIZE * 4));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE)
				.addComponent(topicLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Short.MAX_VALUE))

				.addGroup(layout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HFIRST_GAP, Globals.HFIRST_GAP)
						.addComponent(labelCombo, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, Globals.BUTTON_WIDTH * 2,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE * 2, Globals.HGAP_SIZE * 4, Globals.HGAP_SIZE * 4)
						.addComponent(buttonSetup, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH)
						.addGap(Globals.HGAP_SIZE * 2, Globals.HGAP_SIZE * 2, Globals.HGAP_SIZE * 2)
						.addComponent(buttonReload, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addGap(Globals.HGAP_SIZE, Globals.HFIRST_GAP, Short.MAX_VALUE))
		//////////////////////////////////////////////////////////////////////
		);

	}

	private void defineTopPanel(JPanel panel) {
		JLabel groupNameLabel = new JLabel(configed.getResourceValue("FGroupAction.groupname"));

		JLabel clientsCountLabel = new JLabel(configed.getResourceValue("FGroupAction.clientcounter"));

		fieldGroupname = new JTextField();
		fieldGroupname.setPreferredSize(Globals.counterfieldDimension);
		fieldGroupname.setEditable(false);

		fieldInvolvedClientsCount = new JTextField("");
		fieldInvolvedClientsCount.setPreferredSize(Globals.counterfieldDimension);
		fieldInvolvedClientsCount.setEditable(false);

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(groupNameLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldGroupname, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(clientsCountLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(fieldInvolvedClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE * 2));

		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(Globals.HGAP_SIZE, Globals.HFIRST_GAP, Globals.HFIRST_GAP)
						.addComponent(groupNameLabel, firstLabelWidth, firstLabelWidth, firstLabelWidth)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(fieldGroupname, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2, Globals.HGAP_SIZE * 2)
						.addComponent(clientsCountLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2)
						.addComponent(fieldInvolvedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE * 2));

	}

}