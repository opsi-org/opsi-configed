package de.uib.configed.gui.ssh;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;

public class SSHPMInstallSettingsPanel extends SSHPMInstallPanel {

	private JLabel jLabelOn = new JLabel();
	private JLabel jLabelUpdateInstalled = new JLabel();
	private JLabel jLabelSetupInstalled = new JLabel();
	private JLabel jLabelProperties = new JLabel();
	private JLabel jLabelVerbosity = new JLabel();

	private JComboBox<Integer> jComboBoxVerbosity;
	private JTextField jTextFieldSelecteddepots;
	private JButton jButtonDepotselection;
	private JCheckBox jCheckBoxProperties;
	private JCheckBox jCheckBoxUpdateInstalled;
	private JCheckBox jCheckBoxSetupInstalled;

	private FDepotselectionList fDepotList;
	private List<String> depots;

	public SSHPMInstallSettingsPanel() {
		this(null);
	}

	public SSHPMInstallSettingsPanel(JDialog dia) {

		if (dia != null)
			setFDepotList(dia);
		initComponents();
		initLayout();
		initDepots();
	}

	private void initComponents() {

		jLabelOn.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
		jLabelVerbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jLabelProperties.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties"));

		jButtonDepotselection = new JButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
		jButtonDepotselection.addActionListener(actionEvent -> {
			initDepots();
			if (jButtonDepotselection != null)
				fDepotList.setLocationRelativeTo(jButtonDepotselection);
			fDepotList.setVisible(true);
		});

		jTextFieldSelecteddepots = new JTextField();
		jTextFieldSelecteddepots.setEditable(false);

		jComboBoxVerbosity = new JComboBox<>();
		jComboBoxVerbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++)
			jComboBoxVerbosity.addItem(i);
		jComboBoxVerbosity.setSelectedItem(1);

		jCheckBoxProperties = new JCheckBox();
		jCheckBoxProperties.setSelected(true);
		jLabelUpdateInstalled.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.updateInstalled"));
		jCheckBoxUpdateInstalled = new JCheckBox();

		jLabelSetupInstalled.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.setupInstalled"));
		jCheckBoxSetupInstalled = new JCheckBox();

	}

	public void setFDepotList(JDialog dia) {
		fDepotList = new FDepotselectionList(dia) {
			@Override
			public void setListData(List<String> v) {
				if (v == null || v.isEmpty()) {
					setListData(new ArrayList<>());
					jButton1.setEnabled(false);
				} else {
					super.setListData(v);
					jButton1.setEnabled(true);
				}
			}

			@Override
			public void doAction1() {
				jTextFieldSelecteddepots.setText(produceDepotParameter());
				super.doAction1();
			}
		};
	}

	private void initLayout() {
		this.setBackground(Globals.BACKGROUND_COLOR_7);

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup(center)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup()
												.addComponent(jLabelOn, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
												.addComponent(jTextFieldSelecteddepots, PREF, PREF, Short.MAX_VALUE))
												.addComponent(jLabelVerbosity, PREF, PREF, PREF)
												.addComponent(jLabelProperties, PREF, PREF, PREF)
												.addComponent(jLabelSetupInstalled, PREF, PREF, PREF)
												.addComponent(jLabelUpdateInstalled, PREF, PREF, PREF))
										.addGap(Globals.GAP_SIZE)
										.addGroup(layout.createParallelGroup()
												.addComponent(jButtonDepotselection, PREF, PREF, PREF)

												.addComponent(jComboBoxVerbosity, Globals.ICON_WIDTH,
														Globals.ICON_WIDTH, Globals.ICON_WIDTH)
												.addComponent(jCheckBoxProperties, PREF, PREF, PREF)
												.addComponent(jCheckBoxSetupInstalled, PREF, PREF, PREF)
												.addComponent(jCheckBoxUpdateInstalled, PREF, PREF, PREF))
										.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, MAX)))
						.addGap(Globals.GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelOn, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(jTextFieldSelecteddepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(jButtonDepotselection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelVerbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxVerbosity, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelProperties, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxProperties, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelSetupInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxSetupInstalled, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelUpdateInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jCheckBoxUpdateInstalled, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));
	}

	protected List<String> getAllowedInstallTargets() {
		List<String> result = new ArrayList<>();

		if (persist.isDepotsFullPermission()) {
			jTextFieldSelecteddepots.setEditable(true);
			result.add(PersistenceController.DEPOT_SELECTION_NODEPOTS);
			result.add(PersistenceController.DEPOT_SELECTION_ALL);
		} else
			jTextFieldSelecteddepots.setEditable(false);

		for (String depot : persist.getHostInfoCollections().getDepotNamesList()) {
			if (persist.getDepotPermission(depot))
				result.add(depot);
		}

		logging.info(this, "getAllowedInstallTargets " + result);

		return result;
	}

	protected String produceDepotParameter() {
		String depotParameter = "";
		List<String> selectedDepots = fDepotList.getSelectedDepots();

		if (selectedDepots.isEmpty()) {
			if (persist.isDepotsFullPermission()) {
				depotParameter = PersistenceController.DEPOT_SELECTION_NODEPOTS;
			} else if (!depots.isEmpty()) {
				depotParameter = depots.get(0);
			}
		} else {
			if (selectedDepots.contains(

					PersistenceController.DEPOT_SELECTION_NODEPOTS)

			) {
				depotParameter = "";
			} else if (selectedDepots.contains(PersistenceController.DEPOT_SELECTION_ALL)) {
				depotParameter = "all";
			} else {
				StringBuilder sb = new StringBuilder();
				for (String s : selectedDepots) {
					sb.append(s);
					sb.append(",");
				}
				depotParameter = sb.toString();
				depotParameter = depotParameter.substring(0, depotParameter.length() - 1);
			}
		}

		logging.info(this, "produce depot parameter " + depotParameter);
		return depotParameter;
	}

	protected void initDepots() {
		depots = getAllowedInstallTargets();
		logging.info(this, "depots: " + depots.toString());
		fDepotList.setListData(depots);
		if (depots.isEmpty())
		// probably no permission
		{
			// To DO:
			jButtonDepotselection.setVisible(false);
		}
		jTextFieldSelecteddepots.setText("" + depots.get(0));
	}

	public CommandOpsiPackageManagerInstall updateCommand(CommandOpsiPackageManagerInstall basicCommand) {
		// settings for command c:
		basicCommand.setVerbosity((int) jComboBoxVerbosity.getSelectedItem());
		basicCommand.setProperty(jCheckBoxProperties.isSelected());
		basicCommand.setUpdateInstalled(jCheckBoxUpdateInstalled.isSelected());
		basicCommand.setSetupInstalled(jCheckBoxSetupInstalled.isSelected());
		if (jTextFieldSelecteddepots.getText().contains(
				configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS")))
			basicCommand.setDepotForPInstall("");
		else
			basicCommand.setDepotForPInstall(jTextFieldSelecteddepots.getText());
		return basicCommand;
	}
}