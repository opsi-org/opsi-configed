package de.uib.configed.gui.ssh;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.Empty_Command;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.utilities.logging.logging;

public class SSHPMInstallWgetPanel extends SSHPMInstallPanel {
	private JLabel lbl_url = new JLabel();
	private JTextField tf_url;

	private JLabel lbl_dir = new JLabel();
	private JComboBox cb_autocompletion;
	private JButton btn_autocompletion;

	private JLabel lbl_includeZsync = new JLabel();
	private JCheckBox cb_includeZsync;
	private JLabel lbl_includeZsync2 = new JLabel();

	private JLabel lbl_compareMd5Sum = new JLabel();
	private JCheckBox cb_compareMD5;
	SSHCompletionComboButton autocompletion;

	SSHWgetAuthenticationPanel wgetAuthPanel;

	private String mainProduct;
	private String mainDir;
	private String url_def_text;

	public SSHPMInstallWgetPanel() {
		super();
		autocompletion = new SSHCompletionComboButton(additional_default_paths);
		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		url_def_text = configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");
		initComponents();
		initLayout();

		cb_autocompletion.setEnabled(true);
		btn_autocompletion.setEnabled(true);

		cb_autocompletion.setSelectedItem(workbench);
		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();
	}

	private void initComponents() {
		lbl_dir.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetDir"));
		lbl_url.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl"));
		lbl_includeZsync.setText(configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync"));
		lbl_includeZsync2.setText(configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync2"));
		lbl_compareMd5Sum.setText(configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetCompareMD5Sum"));

		cb_autocompletion = autocompletion.getCombobox();
		cb_autocompletion.addItem(workbench);
		cb_autocompletion.setSelectedItem(workbench);
		btn_autocompletion = autocompletion.getButton();

		tf_url = new JTextField(url_def_text);
		tf_url.setBackground(Globals.backLightYellow);

		tf_url.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (tf_url.getText().equals(url_def_text)) {
					tf_url.setSelectionStart(0);
					tf_url.setSelectionEnd(tf_url.getText().length());
				}
			}
		});

		// perfekt für PMInstall
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH * 2, Globals.BUTTON_HEIGHT);

		cb_includeZsync = new JCheckBox();
		cb_includeZsync.setSelected(true);
		cb_includeZsync.setToolTipText(configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxIncludeZsync.tooltip"));
		cb_includeZsync.addItemListener(itemEvent -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				cb_compareMD5.setSelected(true);
				cb_compareMD5.setEnabled(true);
			} else {
				cb_compareMD5.setSelected(false);
				cb_compareMD5.setEnabled(false);
			}
		});

		cb_compareMD5 = new JCheckBox();
		cb_compareMD5.setSelected(true);
		cb_compareMD5.setToolTipText(configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxCompareMD5.tooltip"));
	}

	public SSHCommand_Template getCommand(SSHCommand_Template commands) {
		if ((tf_url.getText() == null) || (tf_url.getText().trim().equals(""))
				|| (tf_url.getText().trim().equals(url_def_text)))
			return null;

		CommandWget wget = getWgetCommand();
		if (wget != null) {
			if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
				wget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
						+ wgetAuthPanel.getPw() + " ");
			} else
				wget.setAuthentication(" ");
			commands.addCommand((SSHCommand) wget);
			logging.info(this, "doAction1 wget " + wget);
		}
		if (cb_compareMD5.isSelected()) {
			String product = mainDir + "/" + getFilenameFromUrl(mainProduct);
			// ToDo: Folgender Parameter String (befehl) muss noch in die klasse
			// sshcommandfactory ausgelagert werden

			commands.addCommand(new Empty_Command("md5_vergleich", " if [ -z $((cat " + product + ".md5" + ") | "
					+ "grep $(md5sum " + product + "  | head -n1 | cut -d \" \" -f1)) ] ; " + " then echo \""
					+ configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual")
					+ "\"; else echo \""
					+ configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")
					+ "\"; fi", "", false));
		}
		return commands;
	}

	private CommandWget getWgetCommand() {
		String d = workbench;
		String u = "";
		String additionalProds = "";
		String wgetDir = ((String) cb_autocompletion.getSelectedItem());

		String tmp_tf_dir = "<" + configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory") + ">";
		if ((wgetDir != "") || (wgetDir != tmp_tf_dir))
			d = wgetDir;
		else
			return null;

		String tmp_tf_url = "<"
				+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl")
						.replace(":", "")
				+ ">";
		if ((tf_url.getText() != "") || (tf_url.getText() != tmp_tf_url))
			u = tf_url.getText();
		else
			return null;

		mainProduct = u;
		mainDir = d;
		if (cb_includeZsync.isSelected()) {
			if (additionalProds.contains(".opsi")) {
				additionalProds = " " + u.replace(".opsi", ".opsi.zsync");
				additionalProds = additionalProds + " " + u.replace(".opsi", ".opsi.md5");
			}
		}

		CommandWget wget = new CommandWget(d, u, additionalProds);
		return wget;
	}

	public String getProduct() {
		return (String) cb_autocompletion.getSelectedItem() + getFilenameFromUrl(tf_url.getText());
	}

	private static String getFilenameFromUrl(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private void initLayout() {
		this.setBackground(Globals.backLightBlue);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(baseline)
						.addComponent(lbl_url, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(tf_url, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(baseline)
						.addComponent(lbl_dir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(cb_autocompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(btn_autocompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(baseline)
						.addComponent(lbl_includeZsync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_includeZsync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(lbl_includeZsync2, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(baseline)
						.addComponent(lbl_compareMd5Sum, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_compareMD5, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(baseline)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH), Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(baseline).addComponent(wgetAuthPanel, PREF, PREF, PREF))
				.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup().addComponent(lbl_url, PREF, PREF, PREF)
						.addComponent(lbl_dir, PREF, PREF, PREF).addComponent(lbl_includeZsync, PREF, PREF, PREF)
						.addComponent(lbl_compareMd5Sum, PREF, PREF, PREF)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), PREF, PREF, PREF))
				.addGap(Globals.GAP_SIZE)
				.addGroup(
						layout.createParallelGroup()
								.addGroup(layout.createSequentialGroup().addComponent(tf_url, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH, MAX))
								.addGroup(layout.createSequentialGroup()
										.addComponent(cb_autocompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												MAX)
										.addComponent(btn_autocompletion, PREF, PREF, PREF))
								.addGroup(layout.createSequentialGroup().addComponent(cb_includeZsync, PREF, PREF, PREF)
										.addGap(Globals.GAP_SIZE).addComponent(lbl_includeZsync2, PREF, PREF, PREF))
								.addGroup(layout.createSequentialGroup().addComponent(cb_compareMD5, PREF, PREF, PREF))
								.addGroup(layout.createSequentialGroup().addComponent(
										wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH), PREF, PREF, PREF)))
				.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createSequentialGroup().addComponent(wgetAuthPanel, PREF, PREF, MAX))
				.addGap(Globals.GAP_SIZE));
	}
}