/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionState;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.serverdata.CacheManager;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SeparatedDocument;
import de.uib.utils.userprefs.UserPreferences;

public class LoginDialog extends JFrame {
	private ConfigedMain configedMain;
	private OpsiServiceNOMPersistenceController persistenceController;

	private GlassPane glassPane;

	private JLabel jLabelTitle;
	private JLabel jLabelVersion;
	private JLabel jLabelLogo;

	private FlatTextField fieldUser = new FlatTextField();

	private FlatPasswordField passwordField = new FlatPasswordField();
	private FlatPasswordField fieldOTP = new FlatPasswordField();

	private FlatComboBox<String> fieldHost = new FlatComboBox<>();

	private JCheckBox checkUseOTP;

	private JButton jButtonCancel;
	private JButton jButtonCommit;

	private KeyListener newKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				tryConnecting();
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				endProgram();
			} else {
				// Do nothing with other keys
			}
		}
	};

	public LoginDialog(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;
		initGuiElements();
		setupLayout();
		setServers();

		finishAndMakeVisible();

		initGlassPane();
	}

	private void initGlassPane() {
		glassPane = new GlassPane();

		setGlassPane(glassPane);
	}

	public void setHost(String host) {
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();
	}

	private void setServers() {
		List<String> savedServers = readLocallySavedServerNames();

		if (savedServers.isEmpty()) {
			savedServers.add("localhost");
		}

		fieldHost.setModel(new DefaultComboBoxModel<>(savedServers.toArray(new String[0])));
	}

	public void setUser(String user) {
		if (user == null) {
			user = "";
		}
		fieldUser.setText(user);
		passwordField.requestFocus();
	}

	public void setPassword(String password) {
		if (password == null) {
			password = "";
		}
		passwordField.setText(password);
	}

	public void setOTP(String otp) {
		if (otp == null) {
			otp = "";
		}
		checkUseOTP.setSelected(!otp.isEmpty());
		fieldOTP.setText(otp);
	}

	private void setActivated(boolean active) {
		Logging.info(this, "activate");

		glassPane.activate(!active);

		if (active) {
			glassPane.setInfoText(null);
		} else {
			glassPane.setInfoText(Configed.getResourceValue("LoginDialog.WaitInfo.label"));
		}
	}

	private void initGuiElements() {
		setTitle(Configed.getResourceValue("LoginDialog.title"));
		setIconImage(Icons.getMainIcon());

		jLabelLogo = new JLabel(Icons.getOpsiLogoWide());

		jLabelTitle = new JLabel(Globals.APPNAME);
		jLabelVersion = new JLabel(Configed.getResourceValue("LoginDialog.version") + "  " + Globals.VERSION + "  ("
				+ Globals.VERDATE + ") ");

		fieldHost.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderHost"));
		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.getEditor().getEditorComponent().addKeyListener(newKeyListener);

		fieldUser.setPlaceholderText(Configed.getResourceValue("username"));
		fieldUser.addKeyListener(newKeyListener);
		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		passwordField.setPlaceholderText(Configed.getResourceValue("password"));
		passwordField.addKeyListener(newKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));

		fieldOTP.setDocument(new SeparatedDocument(new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }, 6,
				Character.MIN_VALUE, 6, true));
		fieldOTP.setPlaceholderText(Configed.getResourceValue("LoginDialog.placeholderOTP"));
		fieldOTP.addKeyListener(newKeyListener);
		fieldOTP.setVisible(false);
		fieldOTP.setPreferredSize(new Dimension(0, 0));

		checkUseOTP = new JCheckBox(Configed.getResourceValue("LoginDialog.checkUseOTP"));
		checkUseOTP.setToolTipText(Configed.getResourceValue("LoginDialog.checkUseOTP.toolTip"));
		checkUseOTP.addItemListener((ItemEvent event) -> {
			showOTPField(checkUseOTP.isSelected());
			UserPreferences.setBoolean(UserPreferences.OTP, checkUseOTP.isSelected());
		});
		checkUseOTP.setSelected(UserPreferences.getBoolean(UserPreferences.OTP));

		jButtonCancel = new JButton(Configed.getResourceValue("LoginDialog.jButtonCancel"));
		jButtonCancel.addActionListener(actionEvent -> endProgram());

		jButtonCommit = new JButton(Configed.getResourceValue("LoginDialog.jButtonCommit"));
		jButtonCommit.addActionListener(actionEvent -> tryConnecting());
	}

	private void showOTPField(boolean show) {
		if (show) {
			fieldOTP.setVisible(true);
			fieldOTP.setPreferredSize(new Dimension(Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));
			setSize(getPreferredSize());
		} else {
			fieldOTP.setVisible(false);
			fieldOTP.setPreferredSize(new Dimension(0, 0));
			setSize(getPreferredSize());
		}
	}

	private void setupLayout() {
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		((JComponent) getContentPane()).setBorder(padding);

		GroupLayout groupLayout = new GroupLayout(getContentPane());

		groupLayout.setHonorsVisibility(false);
		getContentPane().setLayout(groupLayout);

		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
				.addComponent(jLabelLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(Globals.LINE_HEIGHT)

				.addGap(2).addComponent(fieldHost, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.LINE_HEIGHT)
				.addComponent(fieldUser, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(passwordField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(fieldOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(checkUseOTP, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)

				.addGap(Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2, Globals.LINE_HEIGHT / 2)

				.addGroup(groupLayout.createParallelGroup()
						.addComponent(jButtonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE)
						.addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, 100, Short.MAX_VALUE))

				.addComponent(fieldHost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(fieldUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(fieldOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(checkUseOTP, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addGroup(groupLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jButtonCancel, 120, 120, 120).addGap(0, 0, Short.MAX_VALUE)
						.addComponent(jButtonCommit, 120, 120, 120).addGap(Globals.GAP_SIZE)));
	}

	private void finishAndMakeVisible() {
		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		Logging.notice(" OS ", strOS, "  Version ", osVersion);

		fieldHost.requestFocus();

		// Sets the window on the main screen
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void actAfterWaiting() {
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED
				&& ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
			glassPane.setInfoText(Configed.getResourceValue("LoadingObserver.start"));

			// we can finish
			Logging.info(this, "connected with persis ", persistenceController);
			configedMain.setPersistenceController(persistenceController);
			configedMain.loadDataAndGo();
		} else {
			// Clear cache
			CacheManager.getInstance().clearAllCachedData();
			// return to Passwordfield
			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
				// return to password dialog
				Logging.info(this, "interrupted");
			} else {
				Logging.info(this, "not connected, timeout or not authorized");

				String message;

				if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.TIMEOUT) {
					message = Configed.getResourceValue("LoginDialog.timeoutReached");
				} else if (!ServerFacade.getOpsiServerVersionRetriever().isServerVersionAtLeast("4.3")) {
					message = Configed.getResourceValue("LoginDialog.oldServerVersion");
				} else {
					message = new MessageFormat(
							Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.content")).format(
									new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() });
				}

				JOptionPane.showMessageDialog(this, message,
						Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.title"),
						JOptionPane.ERROR_MESSAGE);
			}

			if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf("authorized") > -1) {
				Logging.info(this, "(not) authorized");

				fieldUser.requestFocus();
				fieldUser.setCaretPosition(fieldUser.getText().length());
			} else {
				fieldHost.requestFocus();
			}

			setActivated(true);
		}
	}

	public void tryConnecting() {
		Logging.info(this, "started  tryConnecting");
		setActivated(false);

		ConfigedMain.setHost((String) fieldHost.getSelectedItem());
		String user = fieldUser.getText().toLowerCase(Locale.ROOT);
		ConfigedMain.setUser(user);
		ConfigedMain.setPassword(String.valueOf(passwordField.getPassword()));
		Logging.info(this, "invoking PersistenceControllerFactory host, user, ", fieldHost.getSelectedItem(), ", ",
				user);

		Configed.setHost((String) fieldHost.getSelectedItem());
		Configed.initSavedStates();

		Logging.info(this, "we are in EventDispatchThread ", SwingUtilities.isEventDispatchThread());
		Logging.info(this, "  Thread.currentThread() ", Thread.currentThread());
		Logging.info(this, "starting thread");

		new Thread() {
			@Override
			public void run() {
				Logging.info(this, "get persis");
				persistenceController = PersistenceControllerFactory.getNewPersistenceController(
						(String) fieldHost.getSelectedItem(), user, String.valueOf(passwordField.getPassword()),
						String.valueOf(fieldOTP.getPassword()));

				Logging.info(this, "got persis, == null ", persistenceController == null);

				Logging.info(this, "waitingTask can be set to ready");
				actAfterWaiting();
			}
		}.start();
	}

	private static void endProgram() {
		ConfigedMain.finishApp(false, 0);
	}

	private List<String> readLocallySavedServerNames() {
		TreeMap<Long, String> sortingmap = new TreeMap<>();
		File savedStatesLocation = null;
		// the following is nearly a double of initSavedStates

		boolean success = true;

		if (Configed.getSavedStatesLocationName() != null) {
			Logging.info(this, "trying to find saved states in ", Configed.getSavedStatesLocationName());

			savedStatesLocation = new File(Configed.getSavedStatesLocationName());
			savedStatesLocation.mkdirs();
			success = savedStatesLocation.setReadable(true);
		}

		if (!success) {
			Logging.warning(this, "cannot not find saved states in ", Configed.getSavedStatesLocationName());
		}

		if (Configed.getSavedStatesLocationName() == null || !success) {
			Logging.info(this, "searching saved states in ", Utils.getSavedStatesDefaultLocation());
			savedStatesLocation = new File(Utils.getSavedStatesDefaultLocation());
			savedStatesLocation.mkdirs();
		}

		Logging.info(this, "saved states location ", savedStatesLocation);

		File[] subdirs = null;

		if (savedStatesLocation != null) {
			subdirs = savedStatesLocation.listFiles(File::isDirectory);

			for (File folder : subdirs) {
				File checkFile = new File(folder + File.separator + Configed.SAVED_STATES_FILENAME);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);

				if (elementname.lastIndexOf("_") > -1) {
					elementname = elementname.replace("_", ":");
				}

				sortingmap.put(checkFile.lastModified(), elementname);
			}
		}

		List<String> result = new ArrayList<>();
		for (Long l : sortingmap.descendingKeySet()) {
			result.add(sortingmap.get(l));
		}

		Logging.info(this, "readLocallySavedServerNames  result ", result);

		return result;
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			endProgram();
		}
	}
}
