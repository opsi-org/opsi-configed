package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// import java.nio.charset.Charset;
// import java.util.regex.*;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FDepotselectionList;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.IconAsButton;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JComboBoxSimpleToolTip;
import de.uib.utilities.thread.WaitCursor;

public class SSHPackageManagerUninstallParameterDialog
		extends /* javax.swing.JDialog */ SSHPackageManagerParameterDialog {

	private GroupLayout gpl;
	private JPanel uninstallPanel = new JPanel();

	private JLabel lbl_uninstall = new JLabel();
	private JLabel lbl_product = new JLabel();
	private JLabel lbl_on = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();
	private JLabel lbl_keepFiles = new JLabel();

	private JComboBox cb_opsiproducts;
	private JComboBox cb_verbosity;
	// private JComboBox cb_depots;
	private JCheckBox checkb_keepFiles;

	private JTextField tf_freeInput;
	private JTextField tf_product;
	private JTextField tf_selecteddepots;

	private IconAsButton buttonUpdateList;
	private JButton btn_depotselection;

	PersistenceController persist;

	FDepotselectionList fDepotList;

	private Vector<String> possibleDepots;

	private CommandOpsiPackageManagerUninstall commandPMUninstall = new CommandOpsiPackageManagerUninstall();

	public SSHPackageManagerUninstallParameterDialog() {
		this(null);
	}

	public SSHPackageManagerUninstallParameterDialog(ConfigedMain m) {
		super(Globals.APPNAME + "  "
				+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"));
		frameWidth = 850;
		frameHeight = 350;

		persist = PersistenceControllerFactory.getPersistenceController();
		if (persist == null)
			logging.info(this, "init PersistenceController null");

		WaitCursor waitCursor = new WaitCursor(this.getContentPane());
		main = m;

		fDepotList = new FDepotselectionList(this) {
			@Override
			public void setListData(Vector<? extends String> v) {
				if (v == null || v.size() == 0) {
					setListData(new Vector<String>());
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

		init();

		btn_execute.setEnabled(false); // requires valid depot selection
		tf_selecteddepots.setText("");
		// tf_selecteddepots.setEnabled(false);

		pack();
		this.setSize(frameWidth, frameHeight);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setComponentsEnabled(!Globals.isGlobalReadOnly());
		waitCursor.stop();
		this.setVisible(true);
	}

	@Override
	protected void setComponentsEnabled(boolean value) {
		super.setComponentsEnabled(value);
		cb_opsiproducts.setEnabled(true);
		cb_opsiproducts.setEditable(false);
		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);

		checkb_keepFiles.setEnabled(value);

		btn_depotselection.setEnabled(value);
		btn_execute.setEnabled(false);
	}

	protected String produceDepotParameter() {
		String depotParameter = "";
		java.util.List<String> selectedDepots = fDepotList.getSelectedDepots();

		logging.debug(this, "produceDepotParameter, selectedDepots " + selectedDepots);

		if (selectedDepots.size() == 0) {
			if (persist.isDepotsFullPermission()) {
				depotParameter = persist.DEPOT_SELECTION_NODEPOTS;
			} else if (possibleDepots.size() > 0) {
				depotParameter = possibleDepots.get(0);
			} else
				btn_execute.setEnabled(false);

		} else {
			btn_execute.setEnabled(true);
			// logging.info(this, " we have something selected ");

			if (selectedDepots.contains(persist.DEPOT_SELECTION_NODEPOTS)) {
				depotParameter = persist.DEPOT_SELECTION_NODEPOTS;
			} else if (selectedDepots.contains(persist.DEPOT_SELECTION_ALL_WHERE_INSTALLED)) {
				StringBuffer sb = new StringBuffer();
				int startIndex = possibleDepots.indexOf(persist.DEPOT_SELECTION_ALL_WHERE_INSTALLED);
				// logging.debug(this, " we have special selection in possibleDepots " +
				// possibleDepots);
				// logging.info(this, " we have special selection starting at " + startIndex);
				for (int i = startIndex + 1; i < possibleDepots.size() - 1; i++) {
					// logging.info(this, "append i " + i + " " + possibleDepots.get(i) );
					sb.append(possibleDepots.get(i));
					sb.append(",");
				}
				sb.append(possibleDepots.get(possibleDepots.size() - 1));
				depotParameter = sb.toString();
			} else {
				StringBuffer sb = new StringBuffer();
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

	protected Vector<String> getPossibleDepots() {
		String selectedProduct = (String) cb_opsiproducts.getSelectedItem();

		Vector<String> result = new java.util.Vector<String>();

		if (persist.isDepotsFullPermission()) {
			tf_selecteddepots.setEditable(true);
			result.add(persist.DEPOT_SELECTION_NODEPOTS);
			result.add(persist.DEPOT_SELECTION_ALL_WHERE_INSTALLED);
		} else
			tf_selecteddepots.setEditable(false);

		for (String depot : persist.getHostInfoCollections().getDepotNamesList()) {
			if (persist.getDepotPermission(depot)) {
				// logging.info(this, " depot " + depot + " has products " +
				// persist.getDepot2LocalbootProducts().get(depot).keySet());

				if ((persist.getDepot2LocalbootProducts().get(depot) != null
						&& persist.getDepot2LocalbootProducts().get(depot).keySet().contains(selectedProduct))
						|| (persist.getDepot2NetbootProducts().get(depot) != null
								&& persist.getDepot2NetbootProducts().get(depot).keySet().contains(selectedProduct))) {
					logging.info(this, "taking this depot " + depot);
					result.add(depot);
				}
			}
		}

		logging.info(this, "getPossibleDepots " + result);

		return result;
	}

	protected void initDepots() {
		possibleDepots = getPossibleDepots();
		fDepotList.setListData(possibleDepots);
		if (possibleDepots.size() == 0)
		// probably no permission
		{
			btn_execute.setVisible(false);
			tf_selecteddepots.setText("");
		} else
			tf_selecteddepots.setText("" + possibleDepots.get(0));

	}

	protected void init() {
		uninstallPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(uninstallPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setBorder(BorderFactory.createTitledBorder(""));
		uninstallPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{
			lbl_uninstall.setText(configed
					.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelUninstall"));
		}
		{
			cb_verbosity = new JComboBox();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++)
				cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(itemEvent -> changeVerbosity());
		}
		{
			// tf_freeInput = new JTextField();
			// tf_freeInput.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
			// tf_freeInput.getDocument().addDocumentListener(new DocumentListener()
			// {
			// public void changedUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// public void insertUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// public void removeUpdate(DocumentEvent documentEvent) { changeFreeInput(); }
			// });
			lbl_keepFiles.setText(configed
					.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelKeepFiles"));
			checkb_keepFiles = new JCheckBox();
			checkb_keepFiles.addItemListener(itemEvent -> changeKeepFiles());
		}

		{

			lbl_product.setText(configed
					.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelProduct"));

			tf_product = new JTextField();
			tf_product.setBackground(Globals.backLightYellow);
			tf_product.setEditable(false);

			cb_opsiproducts = new JComboBoxSimpleToolTip();
			cb_opsiproducts.setRenderer(new DefaultListCellRenderer());
			cb_opsiproducts.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);

			/*
			 * cb_opsiproducts.addActionListener(new ActionListener(){
			 * public void actionPerformed(ActionEvent e)
			 * {
			 * tf_product.setText(
			 * (String) cb_opsiproducts.getSelectedItem()
			 * );
			 * }
			 * }
			 * );
			 */

			cb_opsiproducts.addItemListener(itemEvent -> {
				tf_selecteddepots.setText("");
				// tf_selecteddepots.setEnabled(false);
				btn_execute.setEnabled(false);
				tf_product.setText((String) cb_opsiproducts.getSelectedItem());
			});

			buttonUpdateList = new IconAsButton("buttonUpdateList", "images/reload16.png", "images/reload16.png",
					"images/reload16.png", "images/reload16.png");
			buttonUpdateList.setBackground(Globals.backgroundLightGrey);
			buttonUpdateList.setToolTipText(configed.getResourceValue(
					"SSHConnection.ParameterDialog.opsipackagemanager_uninstall.JButtonUpdateList.tooltip"));

			buttonUpdateList.addActionListener(actionEvent -> {
				logging.info(this, "actionPerformed");
				resetProducts();
			});

		}

		{
			lbl_on.setText(
					configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn"));

			btn_depotselection = new JButton(
					configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager.depotselection"));
			btn_depotselection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					initDepots();
					if (btn_depotselection != null)
						fDepotList.centerOn(btn_depotselection);
					fDepotList.setVisible(true);
				}
			});

			tf_selecteddepots = new JTextField();
			tf_selecteddepots.setEditable(false);
		}

		initLabels();
		initButtons(this);
		initLayout();
		resetProducts();
		changeProduct("");
		changeVerbosity();
		// changeFreeInput();
	}

	@Override
	protected void consolidate() {
		super.consolidate();
		resetProducts();
	}

	private void resetProducts() {
		logging.info(this, "resetProducts in cb_opsiproducts");
		cb_opsiproducts.removeAllItems();
		if (persist == null)
			logging.error(this, "resetProducts PersistenceController null");
		else {
			TreeSet<String> productnames = persist.getProductIds();
			for (String item : productnames)
				cb_opsiproducts.addItem(item);
		}
	}

	private void updateCommand() {
		lbl_fullCommand.setText(commandPMUninstall.getCommand());
	}

	private void changeKeepFiles() {
		commandPMUninstall.setKeepFiles((boolean) checkb_keepFiles.isSelected());
		updateCommand();
	}

	// private void changeFreeInput( )
	// {
	// commandPMUninstall.setFreeInput(tf_freeInput.getText().trim());
	// updateCommand();
	// }
	private void changeDepot() {
		if (tf_selecteddepots.getText()
				.equals(configed.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_NODEPOTS")))
			commandPMUninstall.setDepot(null);
		else if (tf_selecteddepots.getText().equals(configed
				.getResourceValue("SSHConnection.command.opsipackagemanager.DEPOT_SELECTION_ALL_WHERE_INSTALLED")))
			commandPMUninstall.setDepot("all");
		else
			commandPMUninstall.setDepot(tf_selecteddepots.getText());

		updateCommand();
	}

	private void changeVerbosity() {
		logging.info(this, "changeVerbosity , selected " + cb_verbosity.getSelectedItem());
		commandPMUninstall.setVerbosity((int) cb_verbosity.getSelectedItem());
		updateCommand();
	}

	private void changeProduct(String prod) {
		commandPMUninstall.setOpsiproduct(prod);
		updateCommand();
	}

	boolean execFinished;
	/* This method is called when button 1 is pressed */

	private boolean confirmAction() {
		FShowList fConfirmAction = new FShowList(Globals.mainFrame,
				Globals.APPNAME + " "
						+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.title"),
				true, new String[] { configed.getResourceValue("buttonCANCEL"), configed.getResourceValue("buttonOK") },
				400, 200);

		fConfirmAction.setMessage(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.confirm") + "\n"
						+ tf_product.getText()
						// + cb_opsiproducts.getSelectedItem()
						+ "\n\n" + configed
								.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_uninstall.jLabelOn")
						// + "from depot(s)"
						+ "\n\n" + tf_selecteddepots.getText());

		fConfirmAction.centerOn(this);
		fConfirmAction.setAlwaysOnTop(true);
		fConfirmAction.setVisible(true);

		if (fConfirmAction.getResult() != 2)
			return false;

		return true;
	}

	@Override
	public void doAction1() {
		changeDepot();
		final String prod = tf_product.getText();
		logging.info(this, "doAction1 uninstall  " + prod);

		changeProduct(prod);

		if (!commandPMUninstall.checkCommand())
			return;

		if (!confirmAction())
			return;

		Thread execThread = new Thread() {
			@Override
			public void run() {
				try {
					logging.info(this, "start exec thread ");

					SSHConnectExec ssh = new SSHConnectExec((SSHCommand) commandPMUninstall);
					// ssh.exec((SSHCommand) commandPMUninstall);
					// cb_opsiproducts.removeItem( prod );
					execFinished = true;
					logging.debug(this, "end exec thread");
				} catch (Exception e) {
					logging.warning(this, "doAction1, exception occurred", e);
				}
			}
		};
		/*
		 * Thread reloadThread = new Thread()
		 * {
		 * public void run()
		 * {
		 * try
		 * {
		 * boolean ready = false;
		 * logging.info (this, "start reload thread ");
		 * 
		 * while (!ready)
		 * {
		 * if (execFinished )
		 * {
		 * logging.info(this, "start reload from doAction1");
		 * main.reload();
		 * ready = true;
		 * }
		 * else Thread.sleep(1000);
		 * }
		 * logging.info (this, "end reload thread ");
		 * }
		 * catch (Exception e)
		 * {
		 * }
		 * }
		 * };
		 * reload should be called manually
		 */
		try {
			execThread.start();
			// reloadThread.start();
		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred", e);
		}
	}

	@Override
	public void doAction2() {
		// productMissing=false;
		execFinished = true;
		this.setVisible(false);
		this.dispose();
	}

	@Override
	public void leave() {
		fDepotList.exit();
		super.leave();
	}

	private void initLayout() {
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		JLabel empty_lbl = new JLabel();
		GroupLayout uninstallPanelLayout = new GroupLayout((JComponent) uninstallPanel);
		uninstallPanel.setLayout(uninstallPanelLayout);
		uninstallPanelLayout.setHorizontalGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lbl_uninstall, pref, pref, max).addGap(Globals.GAP_SIZE * 2)
				.addGroup(uninstallPanelLayout
						.createSequentialGroup().addGroup(uninstallPanelLayout.createParallelGroup()
								// .addComponent(lbl_product,250, 250, 250)
								.addGroup(uninstallPanelLayout.createSequentialGroup().addGap(5, 10, 20)
										.addComponent(cb_opsiproducts, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												2 * Globals.BUTTON_WIDTH)
										.addGap(5, 5, 5)
										// .addComponent(buttonUpdateList, 30, 30, 30)
										.addGap(5, 10, 20))
								.addGroup(uninstallPanelLayout.createSequentialGroup()
										.addComponent(lbl_on, pref, pref, pref).addGap(5, 10, 10)
										.addComponent(btn_depotselection, pref, pref, pref))
								.addComponent(lbl_verbosity, pref, pref, pref)
								.addComponent(lbl_keepFiles, pref, pref, pref))
						.addGap(Globals.GAP_SIZE)
						.addGroup(uninstallPanelLayout.createParallelGroup(leading)
								.addComponent(tf_product, pref, pref, max)
								.addComponent(tf_selecteddepots, pref, pref, max)
								.addComponent(cb_verbosity, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
								// .addGroup(uninstallPanelLayout.createParallelGroup()
								// .addGroup(uninstallPanelLayout.createSequentialGroup()
								// .addGap(Globals.gapSize*2 + Globals.minGapSize-2)
								// .addComponent(empty_lbl,pref, pref, max)
								.addComponent(checkb_keepFiles, pref, pref, pref)
						// )
						// )
						)
				// )
				// .addGroup(uninstallPanelLayout.createSequentialGroup()
				// .addGroup(uninstallPanelLayout.createParallelGroup()
				// )
				// .addGap(Globals.gapSize)
				)
		// .addComponent(lbl_fullCommand, pref, pref,max)
		);

		uninstallPanelLayout.setVerticalGroup(uninstallPanelLayout.createSequentialGroup().addComponent(lbl_uninstall)
				.addGap(Globals.GAP_SIZE)

				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(cb_opsiproducts, pref, pref, pref)
						// .addComponent(buttonUpdateList, Globals.lineHeight, Globals.lineHeight,
						// Globals.lineHeight)
						.addComponent(tf_product, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))

				.addGap(3 * Globals.GAP_SIZE)

				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_on, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(btn_depotselection, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(tf_selecteddepots, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))

				.addGap(3 * Globals.GAP_SIZE)
				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_keepFiles, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						// .addGroup(uninstallPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						// .addComponent(empty_lbl,pref,pref,pref)
						.addComponent(checkb_keepFiles, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
				// )
				).addGap(Globals.GAP_SIZE)
		// .addComponent(lbl_fullCommand)
		);
	}
}
