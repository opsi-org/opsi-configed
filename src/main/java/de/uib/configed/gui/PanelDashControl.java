package de.uib.configed.gui;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import de.uib.configed.Globals;
import de.uib.configed.Configed;
import de.uib.utilities.logging.Logging;

public class PanelDashControl extends JPanel {

	JTextArea messages;
	JCheckBox showDashOnStartup;
	JCheckBox showDashOnLicencesActivation;

	public PanelDashControl() {
		super();
		setBorder(BorderFactory.createLineBorder(Globals.BACKGROUND_COLOR_6, 1, true));
		initComponents();
	}

	protected void initComponents() {
		setBackground(Globals.BACKGROUND_COLOR_7);

		GroupLayout dashLayout = new GroupLayout(this);
		this.setLayout(dashLayout);

		messages = new JTextArea("testxxxxxxxxxxxxxtestyyyyyyyyyyyyyyyytest");
		JScrollPane scrollpaneMessages = new JScrollPane(messages);
		scrollpaneMessages.setBackground(Globals.BACKGROUND_COLOR_7);
		messages.setBackground(Globals.BACKGROUND_COLOR_7);

		showDashOnStartup = new JCheckBox(Configed.getResourceValue("Dash.showOnProgramStart"), true);
		showDashOnStartup.setFont(Globals.defaultFontSmall);

		showDashOnStartup.addActionListener(ae -> {
			AbstractButton source = (AbstractButton) ae.getSource();
			showDashOnStartupWasSetTo(source.isSelected());
		});

		showDashOnLicencesActivation = new JCheckBox(Configed.getResourceValue("Dash.showOnLicencesLoad"), true);
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
		Logging.info(this, "showDashOnStartup was set " + b);
	}

	public void setShowDashOnLicencesActivation(boolean b) {
		showDashOnLicencesActivation.setSelected(b);
	}

	protected void showDashOnLicencesActivationWasSetTo(boolean b) {
		Logging.info(this, "showDashOnLicencesActivation was set " + b);
	}

}
