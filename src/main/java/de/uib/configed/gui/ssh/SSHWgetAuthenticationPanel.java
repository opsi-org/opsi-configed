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
	public static final String LBLUSER = "lbl_user";
	public static final String LBLNEEDAUTH = "lbl_needAuthentication";
	public static final String CBNEEDAUTH = "cb_needAuthentication";

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

	@Override
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
		// this.setBorder(new LineBorder(Globals.blueGrey));
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()

				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(lbl_user, PREF, PREF, PREF).addGap(Globals.GAP_SIZE))
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(lbl_pswd, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)))
				.addGroup(layout.createParallelGroup()
						.addComponent(tf_user, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2)
						.addComponent(tf_pswd, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(lbl_user, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)
						.addComponent(tf_user, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(lbl_pswd, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)
						.addComponent(tf_pswd, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)));
	}
}