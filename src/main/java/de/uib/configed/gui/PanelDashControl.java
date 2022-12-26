package de.uib.configed.gui;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;

public class PanelDashControl extends JPanel {

	JTextArea messages;
	JCheckBox showDashOnStartup;
	JCheckBox showDashOnLicencesActivation;

	public PanelDashControl() {
		super();
		setBorder(BorderFactory.createLineBorder(Globals.backBlue, 1, true));
		initComponents();
	}

	protected void initComponents() {
		setBackground(Globals.backLightBlue);

		GroupLayout dashLayout = new GroupLayout(this);
		this.setLayout(dashLayout);

		messages = new JTextArea("testxxxxxxxxxxxxxtestyyyyyyyyyyyyyyyytest");
		JScrollPane scrollpaneMessages = new JScrollPane(messages);
		scrollpaneMessages.setBackground(Globals.backLightBlue);
		messages.setBackground(Globals.backLightBlue);

		showDashOnStartup = new JCheckBox(configed.getResourceValue("Dash.showOnProgramStart"), true);
		showDashOnStartup.setFont(Globals.defaultFontSmall);

		showDashOnStartup.addActionListener(ae -> {
			AbstractButton source = (AbstractButton) ae.getSource();
			showDashOnStartupWasSetTo(source.isSelected());
		});

		showDashOnLicencesActivation = new JCheckBox(configed.getResourceValue("Dash.showOnLicencesLoad"), true);
		showDashOnLicencesActivation.setFont(Globals.defaultFontSmall);

		showDashOnLicencesActivation.addActionListener(ae -> {
			AbstractButton source = (AbstractButton) ae.getSource();
			showDashOnLicencesActivationWasSetTo(source.isSelected());
		});

		dashLayout.setVerticalGroup(dashLayout.createSequentialGroup()
				
				.addComponent(showDashOnStartup, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(showDashOnLicencesActivation, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
						Globals.BUTTON_HEIGHT));
		dashLayout.setHorizontalGroup(dashLayout.createParallelGroup()
				
				.addComponent(showDashOnStartup, 100, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(showDashOnLicencesActivation, 100, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	public void setMessage(String s) {
		messages.setText(s);
	}

	public void setShowDashOnStartup(boolean b) {
		showDashOnStartup.setSelected(b);
	}

	protected void showDashOnStartupWasSetTo(boolean b) {
		logging.info(this, "showDashOnStartup was set " + b);
	}

	public void setShowDashOnLicencesActivation(boolean b) {
		showDashOnLicencesActivation.setSelected(b);
	}

	protected void showDashOnLicencesActivationWasSetTo(boolean b) {
		logging.info(this, "showDashOnLicencesActivation was set " + b);
	}

}
