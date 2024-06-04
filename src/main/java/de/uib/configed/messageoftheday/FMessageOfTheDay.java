/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.MainFrame;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

/**
 * Represents the overall dialog for the "message of the day" (motd)
 * configuration. This dialog contains two panels, one for the general and one
 * for the user motd config. This dialog is used in the {@link MainFrame} class
 * to show the motd configuration in the top menu "window".
 */
public class FMessageOfTheDay extends FGeneralDialog {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private PanelMessageInfos pMsgInfoGeneral;
	private PanelMessageInfos pMsgInfoUser;
	private JButton resetButton = new JButton(Configed.getResourceValue("MessageOfTheDay.resetButton"));
	private Map<String, String> motdData = new HashMap<>();

	public FMessageOfTheDay() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("ConfigedMain.MessageOfTheDay.title"), false,
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") }, 2,
				700, 500, true);
		motdData = persistenceController.getConfigDataService().getMessageOfTheDayConfigs();
		define();
	}

	private void define() {
		pMsgInfoGeneral = new PanelMessageInfos(this, PanelMessageInfos.InfoType.DEVICE, motdData);
		pMsgInfoUser = new PanelMessageInfos(this, PanelMessageInfos.InfoType.USER, motdData);
		setSaveButtonEnable(false);

		JPanel panel = new JPanel();
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		resetButton.addActionListener(e -> resetData());

		JLabel frameTitleLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.title"));
		gpl.setVerticalGroup(gpl.createSequentialGroup()
				.addGroup(gpl.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(frameTitleLabel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(resetButton, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addComponent(pMsgInfoGeneral, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(pMsgInfoUser, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		gpl.setHorizontalGroup(gpl.createParallelGroup()
				.addGroup(gpl.createSequentialGroup()
						.addComponent(frameTitleLabel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(resetButton, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(pMsgInfoGeneral, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(pMsgInfoUser, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		scrollpane.getViewport().add(panel);
		scrollpane.setBorder(null);
	}

	public void checkDefaultValues() {
		Logging.info("FMessageOfTheDay checkDefaultValues");
		if (pMsgInfoGeneral == null || pMsgInfoUser == null) {
			return;
		}
		boolean txtUserEqualToDefault = pMsgInfoUser.getText()
				.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER));
		boolean txtUserValidUntilEqualToDefault = pMsgInfoUser.getValidUntil()
				.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL));
		boolean txtDeviceEqualToDefault = pMsgInfoGeneral.getText()
				.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE));
		boolean txtDeviceValidUntilEqualToDefault = pMsgInfoGeneral.getValidUntil()
				.equals(motdData.get(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL));

		setSaveButtonEnable(!(txtUserEqualToDefault && txtDeviceEqualToDefault && txtUserValidUntilEqualToDefault
				&& txtDeviceValidUntilEqualToDefault));
	}

	private void resetData() {
		Logging.info("FMessageOfTheDay resetData");
		motdData = persistenceController.getConfigDataService().getMessageOfTheDayConfigs();
		pMsgInfoGeneral.setDataMap(motdData);
		pMsgInfoGeneral.resetData();
		pMsgInfoUser.setDataMap(motdData);
		pMsgInfoUser.resetData();
		setSaveButtonEnable(false);
	}

	public void setSaveButtonEnable(boolean enable) {
		Logging.info("FMessageOfTheDay setSaveButtonEnable " + enable);
		jButton2.setEnabled(enable);
	}

	@Override
	public void doAction2() {
		Logging.info("FMessageOfTheDay doAction2 store");
		Map<String, String> data = new HashMap<>();
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE, pMsgInfoGeneral.getText());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL,
				pMsgInfoGeneral.getValidUntil());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER, pMsgInfoUser.getText());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL,
				pMsgInfoUser.getValidUntil());
		persistenceController.getConfigDataService().setMessageOfTheDayConfigs(data);
		Logging.info("FMessageOfTheDay doAction2 store done: " + data);
		resetData();
		setSaveButtonEnable(false);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		// to ensure that the buttons are visible
		jButton1.repaint();
		jButton2.repaint();
		pMsgInfoGeneral.requestFocus();
	}
}
