/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class NavigationPanel extends JPanel implements ActionListener {

	private JButton nextButton;
	private JButton previousButton;
	private JButton firstButton;
	private JButton lastButton;

	private List<JButton> buttons;

	private PanelGenEditTable associatedPanel;

	public NavigationPanel(PanelGenEditTable associatedPanel) {
		this.associatedPanel = associatedPanel;

		initComponents();

		buttons = new ArrayList<>();
		buttons.add(nextButton);
		buttons.add(previousButton);
		buttons.add(firstButton);
		buttons.add(lastButton);
	}

	@Override
	public void setEnabled(boolean b) {
		if (buttons == null) {
			return;
		}

		for (JButton button : buttons) {
			button.setEnabled(b);
		}
	}

	private void initComponents() {

		Logging.info(this, "initComponents");

		Dimension navButtonDimension = new Dimension(30, Globals.BUTTON_HEIGHT - 6);
		nextButton = new JButton();
		nextButton.setIcon(Globals.createImageIcon("images/arrows/arrow_red_16x16-right.png", ""));

		nextButton.setToolTipText("nächste Datenzeile");
		nextButton.setPreferredSize(navButtonDimension);
		nextButton.addActionListener(this);

		previousButton = new JButton();
		previousButton.setIcon(Globals.createImageIcon("images/arrows/arrow_red_16x16-left.png", ""));
		previousButton.setToolTipText("vorherige Datenzeile");
		previousButton.setPreferredSize(navButtonDimension);
		previousButton.addActionListener(this);

		firstButton = new JButton();
		firstButton.setIcon(Globals.createImageIcon("images/arrows/arrow_red_16x16-doubleleft.png", ""));
		firstButton.setToolTipText("erste Datenzeile");
		firstButton.setPreferredSize(navButtonDimension);
		firstButton.addActionListener(this);

		lastButton = new JButton();
		lastButton.setIcon(Globals.createImageIcon("images/arrows/arrow_red_16x16-doubleright.png", ""));
		lastButton.setToolTipText("letzte Datenzeile");
		lastButton.setPreferredSize(navButtonDimension);
		lastButton.addActionListener(this);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(firstButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lastButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)));

	}

	public void next() {
		associatedPanel.advanceCursor(+1);
	}

	public void previous() {
		associatedPanel.advanceCursor(-1);
	}

	public void first() {
		associatedPanel.setCursorToFirstRow();
	}

	public void last() {
		associatedPanel.setCursorToLastRow();
	}

	// interface
	// ActionListener
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nextButton) {
			next();
		} else if (e.getSource() == previousButton) {
			previous();
		} else if (e.getSource() == lastButton) {
			last();
		} else if (e.getSource() == firstButton) {
			first();
		} else {
			Logging.warning(this, "unexpected action performed on source " + e.getSource());
		}
	}
}
