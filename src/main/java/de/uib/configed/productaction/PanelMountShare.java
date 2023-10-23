/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productaction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.NameProducer;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class PanelMountShare extends JPanel {
	private static List<PanelMountShare> instances = new ArrayList<>();

	private static final int FIRST_LABEL_WIDTH = Globals.FIRST_LABEL_WIDTH;

	private JFrame rootFrame;

	private JButton buttonMountShare;
	private JLabel mountShareDescriptionLabel;

	private final boolean isWindows;

	private boolean smbMounted;

	private int leftBound = -1;

	private NameProducer np;

	public PanelMountShare(NameProducer np, JFrame root) {
		this(np, root, -1);
	}

	public PanelMountShare(NameProducer np, JFrame root, int leftBound) {
		instances.add(this);
		this.rootFrame = root;
		this.np = np;
		this.leftBound = leftBound;

		isWindows = Utils.isWindows();
		smbMounted = false;

		initComponents();
		defineLayout();
	}

	private void initComponents() {

		buttonMountShare = new JButton("", Utils.createImageIcon("images/windows16.png", ""));
		buttonMountShare.setSelectedIcon(Utils.createImageIcon("images/windows16.png", ""));
		buttonMountShare.setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		if (isWindows) {
			buttonMountShare.setToolTipText(
					Configed.getResourceValue("PanelMountShare.mountShareDescription") + " " + np.produceName());
		}

		buttonMountShare.setEnabled(isWindows);

		buttonMountShare.addActionListener((ActionEvent e) -> callMountShare());
	}

	private void defineLayout() {

		mountShareDescriptionLabel = new JLabel(
				Configed.getResourceValue("PanelMountShare.mountShareResult0") + " " + np.getDefaultName());

		checkConnectionToShare();

		JPanel panel = this;
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGap(Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonMountShare, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(mountShareDescriptionLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
						Globals.LINE_HEIGHT));

		if (leftBound >= 0) {
			layout.setHorizontalGroup(
					layout.createSequentialGroup().addGap(leftBound, leftBound, leftBound)
							.addComponent(buttonMountShare, Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
									Globals.GRAPHIC_BUTTON_WIDTH)
							.addGap(Globals.HFIRST_GAP, Globals.HFIRST_GAP, Globals.HFIRST_GAP)
							.addComponent(mountShareDescriptionLabel, Globals.BUTTON_WIDTH * 2,
									Globals.BUTTON_WIDTH * 3, Short.MAX_VALUE)
							.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE));
		} else {
			layout.setHorizontalGroup(
					layout.createSequentialGroup().addGap(Globals.HFIRST_GAP, Globals.HFIRST_GAP, Globals.HFIRST_GAP)
							.addGap(FIRST_LABEL_WIDTH, FIRST_LABEL_WIDTH, FIRST_LABEL_WIDTH)
							.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
							.addComponent(buttonMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addGap(Globals.HFIRST_GAP, Globals.HFIRST_GAP, Globals.HFIRST_GAP)
							.addComponent(mountShareDescriptionLabel, Globals.BUTTON_WIDTH * 2,
									Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
							.addGap(5, 5, 5).addGap(Globals.GAP_SIZE, Globals.GAP_SIZE * 3, Short.MAX_VALUE));
		}
	}

	public void mount(boolean mounted) {
		smbMounted = mounted;
		if (!mounted) {
			callMountShare();
		}

		setShareMountedInfo(smbMounted);
	}

	private void callMountShare() {
		if (!isWindows) {
			return;
		}

		String call;
		call = "explorer.exe " + " \"" + np.produceName() + "\"";

		Logging.info(this, "windows call: " + call);

		try {
			Runtime.getRuntime().exec(new String[] { call });
		} catch (IOException ioex) {
			Logging.error("io-Error: " + ioex, ioex);
		}

		mountShareDescriptionLabel
				.setText(Configed.getResourceValue("PanelMountShare.mountShareResult1") + " " + np.produceName());

		checkConnectionToShare(240);

		// runtime
	}

	private static void checkAllConnections() {
		for (PanelMountShare panel : instances) {
			panel.checkConnectionToShare();
		}
	}

	private void setShareMountedInfo(boolean mounted) {
		if (mounted) {
			mountShareDescriptionLabel
					.setText(Configed.getResourceValue("PanelMountShare.mountShareResult2") + " " + np.produceName());
		} else {
			mountShareDescriptionLabel.setText(Configed.getResourceValue("PanelMountShare.mountShareResult0"));
		}
	}

	protected boolean checkConnectionToShare() {
		boolean found = false;

		if (np.produceName() == null || np.produceName().isEmpty()) {
			Logging.info(this, "checkConnectionToShare no filename " + np.produceName());
		} else {
			File f = new File(np.produceName());
			if (!f.exists()) {
				Logging.info(this, "checkConnectionToShare no existing filename " + np.produceName());
			} else {
				found = f.isDirectory();
				if (!found) {
					Logging.info(this, "checkConnectionToShare no directory " + np.produceName());
				}
			}
		}

		setShareMountedInfo(found);

		if (!smbMounted && found) {
			initialMount();
		}

		smbMounted = found;

		return smbMounted;
	}

	private void initialMount() {
		// for overriding
	}

	private void checkConnectionToShare(final int seconds) {
		new Thread() {
			@Override
			public void run() {

				for (int i = 0; !smbMounted && i < seconds; i++) {

					try {
						Logging.debug(this, "trying to find dir, count " + i);
						sleep(1000);
						checkConnectionToShare();
						rootFrame.toFront();
					} catch (InterruptedException ex) {
						Logging.debug(this, "Exception " + ex);
						Thread.currentThread().interrupt();
					}
				}

				if (smbMounted) {
					checkAllConnections();
				}
			}
		}.start();
	}
}
