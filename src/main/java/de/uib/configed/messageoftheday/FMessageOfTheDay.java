/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.messageoftheday;

import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.logging.Logging;

public class FMessageOfTheDay extends FGeneralDialog {
	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private PanelMessageInfos pMsgInfoGeneral;
	private PanelMessageInfos pMsgInfoUser;
	private Map<String, String> motdData = new HashMap<>();

	public FMessageOfTheDay() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("ConfigedMain.MessageOfTheDay.title"), true,
				new String[] { Configed.getResourceValue("buttonClose"), Configed.getResourceValue("buttonOK") }, 700,
				500);
		// TODO: why is the second button only showed when hovered ? Disable if data not changed
		motdData = persistenceController.getConfigDataService().getMessageOfTheDayConfigs();
		define();
	}

	private void define() {
		pMsgInfoGeneral = new PanelMessageInfos(PanelMessageInfos.InfoType.DEVICE, motdData);
		pMsgInfoUser = new PanelMessageInfos(PanelMessageInfos.InfoType.USER, motdData);

		JPanel panel = new JPanel();
		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		JLabel frameTitleLabel = new JLabel(Configed.getResourceValue("MessageOfTheDay.title"));
		gpl.setVerticalGroup(gpl.createSequentialGroup()
				.addComponent(frameTitleLabel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(pMsgInfoGeneral, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(pMsgInfoUser, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		gpl.setHorizontalGroup(gpl.createParallelGroup()
				.addComponent(frameTitleLabel, 30, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(pMsgInfoGeneral, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(pMsgInfoUser, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		scrollpane.getViewport().add(panel);
		scrollpane.setBorder(null);
	}


	@Override
	public void doAction2() {
		Logging.info(this, "store message of the day data");
		Map<String, String> data = new HashMap<>();
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE, pMsgInfoGeneral.getText());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_DEVICE_VALID_UNTIL,
				pMsgInfoGeneral.getValidUntil().toString());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER, pMsgInfoUser.getText());
		data.put(OpsiServiceNOMPersistenceController.CONFIG_KEY_MSG_OF_DAY_USER_VALID_UNTIL,
				pMsgInfoUser.getValidUntil().toString());
		persistenceController.getConfigDataService().setMessageOfTheDayConfigs(data);
	}
}
