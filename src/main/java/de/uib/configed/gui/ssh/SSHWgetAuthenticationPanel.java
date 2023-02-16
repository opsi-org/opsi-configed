package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public class SSHWgetAuthenticationPanel extends SSHPMInstallPanel {
	public static final String LBLUSER = "lbl_user";
	public static final String LBLNEEDAUTH = "lbl_needAuthentication";
	public static final String CBNEEDAUTH = "cb_needAuthentication";

	private JCheckBox jCheckBoxNeedAuthentication;
	private JLabel jLabeluser = new JLabel();
	private JLabel jLabelNeedAuthentication = new JLabel();
	private JTextField jTextFieldUser = new JTextField();
	private JTextField jTextFieldPassword = new JPasswordField();
	private JLabel jLabelPassword = new JLabel();
	private SSHWgetAuthenticationPanel instance;

	public SSHWgetAuthenticationPanel() {
		super();
		initComponents();
		initLayout();
		this.close();
		this.close();
		instance = this;
	}

	private void initComponents() {
		jLabelNeedAuthentication
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication"));
		jLabelNeedAuthentication.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.wget.needAuthentication.tooltip"));
		jCheckBoxNeedAuthentication = new JCheckBox();
		jCheckBoxNeedAuthentication.addItemListener(itemEvent -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED)
				instance.open();
			else
				instance.close();
		});
		jLabeluser.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.username"));
		jLabelPassword.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.wget.password"));
		((JPasswordField) jTextFieldPassword).setEchoChar('*');
		jTextFieldUser.setText("");
		jTextFieldPassword.setText("");

	}

	public void setLabelNeedAuthenticationSize(Dimension size) {
		jLabelNeedAuthentication.setSize(size);
		jLabelNeedAuthentication.setPreferredSize(size);
	}

	public void setLabelNeedAuthenticationSize(int width, int height) {
		setLabelNeedAuthenticationSize(new Dimension(width, height));
	}

	public void setLabelSizes(int width, int height) {
		setLabelSizes(new Dimension(width, height));
		setLabelSizes(new Dimension(width, height));
	}

	public void setLabelSizes(Dimension size) {
		jLabeluser.setPreferredSize(size);
		jLabelPassword.setPreferredSize(size);
	}

	public JComponent get(String comp) {
		if (comp.equals(LBLNEEDAUTH)) {
			return jLabelNeedAuthentication;
		}

		if (comp.equals(CBNEEDAUTH)) {
			return jCheckBoxNeedAuthentication;
		}

		if (comp.equals(LBLUSER)) {
			return jLabeluser;
		}

		return null;
	}

	public String getUser() {
		return jTextFieldUser.getText();
	}

	public String getPw() {
		return new String(((JPasswordField) jTextFieldPassword).getPassword());
	}

	private void initLayout() {
		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()

				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(jLabeluser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE))
						.addGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
								.addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE)))
				.addGroup(layout.createParallelGroup()
						.addComponent(jTextFieldUser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(jTextFieldPassword, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabeluser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)
						.addComponent(jTextFieldUser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createParallelGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelPassword, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE).addComponent(jTextFieldPassword, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addGap(Globals.GAP_SIZE)));
	}
}