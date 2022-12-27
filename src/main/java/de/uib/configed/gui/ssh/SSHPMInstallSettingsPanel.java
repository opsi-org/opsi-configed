package de.uib.configed.gui.ssh;

import java.util.List;
import java.util.Vector;

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

	private JLabel lbl_on = new JLabel();
	private JLabel lbl_updateInstalled = new JLabel();
	private JLabel lbl_setupInstalled = new JLabel();
	private JLabel lbl_properties = new JLabel();
	private JLabel lbl_verbosity = new JLabel();

	private JComboBox cb_verbosity;
	private JTextField tf_selecteddepots;
	private JButton btn_depotselection;
	private JCheckBox cb_properties;
	private JCheckBox checkb_updateInstalled;
	private JCheckBox checkb_setupInstalled;

	public FDepotselectionList fDepotList;
	private Vector<String> depots;

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

		lbl_on.setText(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOn"));
		lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		lbl_properties.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.lbl_properties"));

		btn_depotselection = new JButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
		btn_depotselection.addActionListener(actionEvent -> {
			initDepots();
			if (btn_depotselection != null)
				fDepotList.centerOn(btn_depotselection);
			fDepotList.setVisible(true);
		});

		tf_selecteddepots = new JTextField();
		tf_selecteddepots.setEditable(false);

		cb_verbosity = new JComboBox<>();
		cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
		for (int i = 0; i < 5; i++)
			cb_verbosity.addItem(i);
		cb_verbosity.setSelectedItem(1);

		cb_properties = new JCheckBox();
		cb_properties.setSelected(true);
		lbl_updateInstalled.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.updateInstalled"));
		checkb_updateInstalled = new JCheckBox();

		lbl_setupInstalled.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.setupInstalled"));
		checkb_setupInstalled = new JCheckBox();

	}

	public void setFDepotList(JDialog dia) {
		fDepotList = new FDepotselectionList(dia) {
			@Override
			public void setListData(Vector<? extends String> v) {
				if (v == null || v.isEmpty()) {
					setListData(new Vector<>());
					jButton1.setEnabled(false);
				} else {
					super.setListData(v);
					jButton1.setEnabled(true);
				}
			}

			@Override
			public void doAction1() {
				tf_selecteddepots.setText(produceDepotParameter());
				super.doAction1();
			}
		};
	}

	private void initLayout() {
		this.setBackground(Globals.backLightBlue);

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE).addGroup(layout
				.createParallelGroup(center)
				.addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addComponent(lbl_on, PREF, PREF, PREF)
								.addGap(Globals.GAP_SIZE).addComponent(tf_selecteddepots, PREF, PREF, Short.MAX_VALUE))
						.addComponent(lbl_verbosity, PREF, PREF, PREF).addComponent(lbl_properties, PREF, PREF, PREF)
						.addComponent(lbl_setupInstalled, PREF, PREF, PREF)
						.addComponent(lbl_updateInstalled, PREF, PREF, PREF)).addGap(Globals.GAP_SIZE)
						.addGroup(layout.createParallelGroup().addComponent(btn_depotselection, PREF, PREF, PREF)

								.addComponent(cb_verbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
								.addComponent(cb_properties, PREF, PREF, PREF)
								.addComponent(checkb_setupInstalled, PREF, PREF, PREF)
								.addComponent(checkb_updateInstalled, PREF, PREF, PREF))
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, MAX)))
				.addGap(Globals.GAP_SIZE));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_on, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(tf_selecteddepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(btn_depotselection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_verbosity, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_properties, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_properties, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_setupInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(checkb_setupInstalled, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_updateInstalled, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(checkb_updateInstalled, leading, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));
	}

	protected Vector<String> getAllowedInstallTargets() {
		Vector<String> result = new Vector<>();

		if (persist.isDepotsFullPermission()) {
			tf_selecteddepots.setEditable(true);
			result.add(PersistenceController.DEPOT_SELECTION_NODEPOTS);
			result.add(PersistenceController.DEPOT_SELECTION_ALL);
		} else
			tf_selecteddepots.setEditable(false);

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
			btn_depotselection.setVisible(false);
		}
		tf_selecteddepots.setText("" + depots.get(0));
	}

	public CommandOpsiPackageManagerInstall updateCommand(CommandOpsiPackageManagerInstall basicCommand) {
		// settings for command c:
		basicCommand.setVerbosity((int) cb_verbosity.getSelectedItem());
		basicCommand.setProperty(cb_properties.isSelected());
		basicCommand.setUpdateInstalled(checkb_updateInstalled.isSelected());
		basicCommand.setSetupInstalled(checkb_setupInstalled.isSelected());
		if (tf_selecteddepots.getText().contains(
				configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS")))
			basicCommand.setDepotForPInstall("");
		else
			basicCommand.setDepotForPInstall(tf_selecteddepots.getText());
		return basicCommand;
	}
}