package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.configed.configed;

public class SSHWgetAuthenticationPanel extends SSHPMInstallPanel {
	final static public String LBLUSER = "lbl_user";
	final static public String LBLNEEDAUTH = "lbl_needAuthentication";
	final static public String CBNEEDAUTH = "cb_needAuthentication";

	private JCheckBox cb_needAuthentication;
	private JLabel lbl_user = new JLabel();
	private JLabel lbl_needAuthentication = new JLabel();
	private JTextField tf_user = new JTextField();
	private JTextField tf_pswd = new JPasswordField();
	private JLabel lbl_pswd = new JLabel();
	private SSHWgetAuthenticationPanel instance;

	public SSHWgetAuthenticationPanel() {
		super();
		initComponents();
		initLayout();
		this.close();
		this.close();
		instance = this;
	}

	public void enable(boolean e) {
		tf_user.setEnabled(e);
		tf_pswd.setEnabled(e);
	}

	private void initComponents() {
		lbl_needAuthentication
				.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication"));
		lbl_needAuthentication.setToolTipText(
				configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication.tooltip"));
		cb_needAuthentication = new JCheckBox();
		cb_needAuthentication.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					instance.open();
				else
					instance.close();
			}
		});
		lbl_user.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.username"));
		lbl_pswd.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.password"));
		((JPasswordField) tf_pswd).setEchoChar('*');
		tf_user.setText(""); // main.USER);
		tf_pswd.setText(""); // main.PASSWORD);

	}

	public void setLabelNeedAuthenticationSize(Dimension size) {
		lbl_needAuthentication.setSize(size);
		lbl_needAuthentication.setPreferredSize(size);
	}

	public void setLabelNeedAuthenticationSize(int width, int height) {
		setLabelNeedAuthenticationSize(new Dimension(width, height));
	}

	public void setLabelSizes(int width, int height) {
		setLabelSizes(new Dimension(width, height));
		setLabelSizes(new Dimension(width, height));
	}

	public void setLabelSizes(Dimension size) {
		lbl_user.setPreferredSize(size);
		lbl_pswd.setPreferredSize(size);
	}

	public JComponent get(String comp) {
		if (comp.equals(LBLNEEDAUTH))
			return lbl_needAuthentication;
		if (comp.equals(CBNEEDAUTH))
			return cb_needAuthentication;
		if (comp.equals(LBLUSER))
			return lbl_user;
		return null;
	}

	public String getUser() {
		return tf_user.getText();
	}

	public String getPw() {
		return new String(((JPasswordField) tf_pswd).getPassword());
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);
		// this.setBorder(new LineBorder(de.uib.configed.Globals.blueGrey));
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()

				.addGap(de.uib.configed.Globals.gapSize)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addGap(de.uib.configed.Globals.gapSize * 2)
								.addComponent(lbl_user, PREF, PREF, PREF)
								.addGap(de.uib.configed.Globals.gapSize))
						.addGroup(layout.createSequentialGroup()
								.addGap(de.uib.configed.Globals.gapSize * 2)
								.addComponent(lbl_pswd, PREF, PREF, PREF)
								.addGap(de.uib.configed.Globals.gapSize)))
				.addGroup(layout.createParallelGroup()
						.addComponent(tf_user, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth,
								de.uib.configed.Globals.buttonWidth * 2)
						.addComponent(tf_pswd, de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth,
								de.uib.configed.Globals.buttonWidth * 2)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(de.uib.configed.Globals.gapSize)
				.addGroup(layout.createParallelGroup()
						.addGap(de.uib.configed.Globals.gapSize)
						.addComponent(lbl_user, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addGap(de.uib.configed.Globals.gapSize)
						.addComponent(tf_user, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addGap(de.uib.configed.Globals.gapSize))
				.addGroup(layout.createParallelGroup()
						.addGap(de.uib.configed.Globals.gapSize)
						.addComponent(lbl_pswd, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addGap(de.uib.configed.Globals.gapSize)
						.addComponent(tf_pswd, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addGap(de.uib.configed.Globals.gapSize)));
	}
}