/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.groupaction;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.share.logging.Logging;

public class GroupActionsDialog {
	private JLabel groupNameLabel;
	private JLabel clientsCountLabel;

	private JComboBox<String> comboSelectImage;

	private List<String> associatedClients;

	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public GroupActionsDialog(ConfigedMain configedMain) {
		super();

		this.configedMain = configedMain;

		JButton buttonSetup = new JButton(Configed.getResourceValue("save"));
		buttonSetup.setToolTipText(Configed.getResourceValue("FGroupAction.buttonSetup.tooltip"));
		buttonSetup.addActionListener(actionEvent -> replay());

		JButton buttonReload = new JButton(Configed.getResourceValue("reloadData"));
		buttonReload.addActionListener(actionEvent -> reload());

		JPanel panel = defineImageActionPanel();

		JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { buttonSetup, buttonReload, Configed.getResourceValue("buttonCancel") });

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(), Configed.getResourceValue("FGroupAction.title"));
		dialog.setModal(false);
	}

	private void setGroupLabelling(String label, String clientCount) {
		groupNameLabel.setText(Configed.getResourceValue("FGroupAction.groupname") + ": " + label);
		clientsCountLabel.setText(Configed.getResourceValue("FGroupAction.clientcounter") + ": " + clientCount);
	}

	public void show() {
		reload();

		dialog.pack();
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	private void setImages() {
		Set<String> imagesCollection = new TreeSet<>(
				persistenceController.getProductDataService().getCommonProductPropertyValues(associatedClients,
						OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
						OpsiServiceNOMPersistenceController.LOCAL_IMAGE_LIST_PROPERTY_KEY));

		comboSelectImage.setModel(new DefaultComboBoxModel<>(imagesCollection.toArray(new String[0])));
	}

	private void reload() {
		setGroupLabelling(configedMain.getActivatedGroupModel().getLabel(),
				"" + configedMain.getActivatedGroupModel().getNumberOfClients());

		associatedClients = new ArrayList<>(configedMain.getActivatedGroupModel().getAssociatedClients());
		setImages();
	}

	private void replay() {
		Logging.debug(this, "replay ", comboSelectImage.getSelectedItem());

		if (comboSelectImage.getSelectedItem() == null) {
			return;
		}

		String image = (String) comboSelectImage.getSelectedItem();

		List<String> values = new ArrayList<>();

		// selected from common product property values
		values.add(image);

		dialog.setCursor(Globals.WAIT_CURSOR);

		persistenceController.getProductDataService().setCommonProductPropertyValue(
				configedMain.getActivatedGroupModel().getAssociatedClients(),
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY,
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_TO_RESTORE_PROPERTY_KEY, values);

		Map<String, String> changedValues = new HashMap<>();
		changedValues.put(ProductState.KEY_ACTION_REQUEST, "setup");

		persistenceController.getProductDataService().updateProductOnClients(
				configedMain.getActivatedGroupModel().getAssociatedClients(),
				OpsiServiceNOMPersistenceController.LOCAL_IMAGE_RESTORE_PRODUCT_KEY, OpsiPackage.TYPE_NETBOOT,
				changedValues);

		dialog.setCursor(null);
	}

	private JPanel defineImageActionPanel() {
		JPanel panel = new JPanel();

		// Values will be set in reload method
		groupNameLabel = new JLabel();
		clientsCountLabel = new JLabel();

		JLabel labelCombo = new JLabel(Configed.getResourceValue("FGroupAction.existingImages"));
		labelCombo.setFont(labelCombo.getFont().deriveFont(Font.BOLD));

		comboSelectImage = new JComboBox<>();

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(groupNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(clientsCountLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(labelCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addComponent(groupNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(clientsCountLabel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(labelCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(comboSelectImage, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE));

		return panel;
	}
}
