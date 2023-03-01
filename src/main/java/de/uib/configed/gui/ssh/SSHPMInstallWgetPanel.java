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

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.utilities.logging.Logging;

public class SSHPMInstallWgetPanel extends SSHPMInstallPanel {
	private JLabel jLabelURL = new JLabel();
	private JTextField jTextFieldURL;

	private JLabel jLabelDir = new JLabel();
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;

	private JLabel jLabelIncludeZsync = new JLabel();
	private JCheckBox jCheckBoxIncludeZSync;
	private JLabel jLabelIncludeZSync2 = new JLabel();

	private JLabel jLabelCompareMD5Sum = new JLabel();
	private JCheckBox jCheckBoxCompareMD5;
	SSHCompletionComboButton autocompletion;

	SSHWgetAuthenticationPanel wgetAuthPanel;

	private String mainProduct;
	private String mainDir;
	private String urlDefText;

	public SSHPMInstallWgetPanel() {
		super();
		autocompletion = new SSHCompletionComboButton(additionalDefaultPaths);
		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		urlDefText = Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");
		initComponents();
		initLayout();

		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion.setEnabled(true);

		jComboBoxAutoCompletion.setSelectedItem(workbench);
		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();
	}

	private void initComponents() {
		jLabelDir.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetDir"));
		jLabelURL.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl"));
		jLabelIncludeZsync.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync"));
		jLabelIncludeZSync2.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetIncludeZsync2"));
		jLabelCompareMD5Sum.setText(Configed
				.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetCompareMD5Sum"));

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.addItem(workbench);
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jButtonAutoCompletion = autocompletion.getButton();

		jTextFieldURL = new JTextField(urlDefText);
		jTextFieldURL.setBackground(Globals.BACKGROUND_COLOR_9);

		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText().equals(urlDefText)) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});

		// perfekt für PMInstall
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH * 2, Globals.BUTTON_HEIGHT);

		jCheckBoxIncludeZSync = new JCheckBox();
		jCheckBoxIncludeZSync.setSelected(true);
		jCheckBoxIncludeZSync.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxIncludeZsync.tooltip"));
		jCheckBoxIncludeZSync.addItemListener(itemEvent -> {
			if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
				jCheckBoxCompareMD5.setSelected(true);
				jCheckBoxCompareMD5.setEnabled(true);
			} else {
				jCheckBoxCompareMD5.setSelected(false);
				jCheckBoxCompareMD5.setEnabled(false);
			}
		});

		jCheckBoxCompareMD5 = new JCheckBox();
		jCheckBoxCompareMD5.setSelected(true);
		jCheckBoxCompareMD5.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.opsipackagemanager_install.jCheckBoxCompareMD5.tooltip"));
	}

	public SSHCommandTemplate getCommand(SSHCommandTemplate commands) {
		if ((jTextFieldURL.getText() == null) || (jTextFieldURL.getText().trim().equals(""))
				|| (jTextFieldURL.getText().trim().equals(urlDefText))) {
			return null;
		}

		CommandWget wget = getWgetCommand();
		if (wget != null) {
			if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
				wget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
						+ wgetAuthPanel.getPw() + " ");
			} else {
				wget.setAuthentication(" ");
			}

			commands.addCommand(wget);
			Logging.info(this, "doAction1 wget " + wget);
		}
		if (jCheckBoxCompareMD5.isSelected()) {
			String product = mainDir + "/" + getFilenameFromUrl(mainProduct);
			// ToDo: Folgender Parameter String (befehl) muss noch in die klasse
			// sshcommandfactory ausgelagert werden

			commands.addCommand(new EmptyCommand("md5_vergleich", " if [ -z $((cat " + product + ".md5" + ") | "
					+ "grep $(md5sum " + product + "  | head -n1 | cut -d \" \" -f1)) ] ; " + " then echo \""
					+ Configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreNotEqual")
					+ "\"; else echo \""
					+ Configed.getResourceValue(
							"SSHConnection.ParameterDialog.opsipackagemanager_install.md5sumsAreEqual")
					+ "\"; fi", "", false));
		}
		return commands;
	}

	private CommandWget getWgetCommand() {
		String d;
		String u = "";
		String additionalProds = "";
		String wgetDir = ((String) jComboBoxAutoCompletion.getSelectedItem());

		String tempTextFieldDir = "<" + Configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory")
				+ ">";
		if (!wgetDir.equals("") || !wgetDir.equals(tempTextFieldDir)) {
			d = wgetDir;
		} else {
			return null;
		}

		String tempTextFieldURL = "<"
				+ Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetUrl")
						.replace(":", "")
				+ ">";
		if (!jTextFieldURL.getText().equals("") || !jTextFieldURL.getText().equals(tempTextFieldURL)) {
			u = jTextFieldURL.getText();
		} else {
			return null;
		}

		mainProduct = u;
		mainDir = d;

		if (jCheckBoxIncludeZSync.isSelected() && additionalProds.contains(".opsi")) {
			additionalProds = " " + u.replace(".opsi", ".opsi.zsync");
			additionalProds = additionalProds + " " + u.replace(".opsi", ".opsi.md5");
		}

		return new CommandWget(d, u, additionalProds);
	}

	public String getProduct() {
		return (String) jComboBoxAutoCompletion.getSelectedItem() + getFilenameFromUrl(jTextFieldURL.getText());
	}

	private static String getFilenameFromUrl(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private void initLayout() {
		this.setBackground(Globals.BACKGROUND_COLOR_7);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup(baseline)
								.addComponent(jLabelURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jTextFieldURL, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(baseline)
								.addComponent(jLabelDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jButtonAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(baseline)
								.addComponent(jLabelIncludeZsync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxIncludeZSync, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jLabelIncludeZSync2, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(baseline)
								.addComponent(jLabelCompareMD5Sum, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jCheckBoxCompareMD5, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(baseline)
								.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
										Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
								.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
										Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
						.addGroup(layout.createParallelGroup(baseline).addComponent(wgetAuthPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
				.addGap(2 * Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelIncludeZsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelCompareMD5Sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addComponent(jTextFieldURL, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jCheckBoxIncludeZSync, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE).addComponent(jLabelIncludeZSync2, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup().addComponent(jCheckBoxCompareMD5,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup().addComponent(
								wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH), GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(Globals.GAP_SIZE))
				.addGroup(layout.createSequentialGroup().addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));
	}
}
