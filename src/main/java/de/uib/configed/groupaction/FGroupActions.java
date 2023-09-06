/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
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
import javax.swing.border.LineBorder;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.GlassPane;
import de.uib.configed.gui.IconButton;
import de.uib.configed.type.OpsiPackage;
import de.uib.opsidatamodel.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.Containership;
import de.uib.utilities.swing.SecondaryFrame;
import utils.Utils;

public class FGroupActions extends SecondaryFrame {

	private GlassPane glassPane;

	private JTextField fieldGroupname;
	private JTextField fieldInvolvedClientsCount;

	private JComboBox<String> comboSelectImage;

	private List<String> associatedClients;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain main;

	private int firstLabelWidth = Globals.BUTTON_WIDTH;

	public FGroupActions(ConfigedMain main) {
		super();

		this.main = main;

		define();
		reload();

		super.setGlobals(Utils.getMap());
		super.setTitle(Globals.APPNAME + " " + Configed.getResourceValue("FGroupAction.title"));

		glassPane = new GlassPane();
		super.setGlassPane(glassPane);
	}

	private void setGroupLabelling(String label, String clientCount) {
		fieldGroupname.setText(label);
		fieldInvolvedClientsCount.setText(clientCount);
	}

	@Override
	public void start() {
		super.start();
		reload();
	}

	private void setImages() {
		List<String> imagesCollection = new ArrayList<>();

		imagesCollection.addAll(new TreeSet<>(persistenceController.getCommonProductPropertyValues(associatedClients,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_LIST_PROPERTY_KEY)));

		comboSelectImage.setModel(new DefaultComboBoxModel<>(imagesCollection.toArray(new String[0])));
	}

	private void reload() {
		setGroupLabelling(main.getActivatedGroupModel().getLabel(),
				"" + main.getActivatedGroupModel().getNumberOfClients());

		associatedClients = new ArrayList<>(main.getActivatedGroupModel().getAssociatedClients());
		setImages();
	}

	private void replay() {
		Logging.debug(this, "replay " + comboSelectImage.getSelectedItem());

		if (comboSelectImage.getSelectedItem() == null) {
			return;
		}

		String image = (String) comboSelectImage.getSelectedItem();

		List<String> values = new ArrayList<>();

		// selected from common product property values
		values.add(image);

		glassPane.activate(true);

		persistenceController.setCommonProductPropertyValue(main.getActivatedGroupModel().getAssociatedClients(),
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY, values);

		Map<String, String> changedValues = new HashMap<>();
		changedValues.put(ProductState.KEY_ACTION_REQUEST, "setup");

		persistenceController.updateProductOnClients(main.getActivatedGroupModel().getAssociatedClients(),
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY, OpsiPackage.TYPE_NETBOOT,
				changedValues);

		glassPane.activate(false);
	}

	private void define() {
		JPanel topPanel = new JPanel();

		defineTopPanel(topPanel);

		JPanel imageActionPanel = new JPanel();
		imageActionPanel.setBorder(new LineBorder(Globals.BACKGROUND_COLOR_6, 2, true));

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

		if (!Main.THEMES) {
			Containership csAll = new Containership(getContentPane());
			csAll.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_7 },
					JPanel.class);

			csAll.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.BACKGROUND_COLOR_3 },
					javax.swing.text.JTextComponent.class);
		}
	}

	private void defineImageActionPanel(JPanel panel) {
		JLabel labelCombo = new JLabel(Configed.getResourceValue("FGroupAction.existingImages"));

		comboSelectImage = new JComboBox<>();

		JLabel topicLabel = new JLabel(Configed.getResourceValue("FGroupAction.replayImage"));

		JButton buttonSetup = new JButton(Configed.getResourceValue("FGroupAction.buttonSetup"));
		buttonSetup.setToolTipText(Configed.getResourceValue("FGroupAction.buttonSetup.tooltip"));

		buttonSetup.addActionListener(actionEvent -> replay());

		IconButton buttonReload = new IconButton(Configed.getResourceValue("FGroupAction.buttonReload"),
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
		JLabel groupNameLabel = new JLabel(Configed.getResourceValue("FGroupAction.groupname"));

		JLabel clientsCountLabel = new JLabel(Configed.getResourceValue("FGroupAction.clientcounter"));

		fieldGroupname = new JTextField();
		fieldGroupname.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
		fieldGroupname.setEditable(false);

		fieldInvolvedClientsCount = new JTextField("");
		fieldInvolvedClientsCount.setPreferredSize(Globals.COUTNER_FIELD_DIMENSION);
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
