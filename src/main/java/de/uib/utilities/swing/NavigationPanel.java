package de.uib.utilities.swing;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class NavigationPanel extends JPanel implements ActionListener {

	protected JButton nextButton;
	protected JButton previousButton;
	protected JButton firstButton;
	protected JButton lastButton;

	protected List<JButton> buttons;

	protected boolean hasNext;
	protected boolean hasPrevious;

	public NavigationPanel() {
		initComponents();

		buttons = new ArrayList<>();
		buttons.add(nextButton);
		buttons.add(previousButton);
		buttons.add(firstButton);
		buttons.add(lastButton);
	}

	@Override
	public void setEnabled(boolean b) {
		if (buttons == null)
			return;

		for (JButton button : buttons) {
			button.setEnabled(b);
		}
	}

	protected void initComponents() {

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
	}

	public void previous() {
	}

	public void last() {
	}

	public void first() {
	}

	public void setActivation() {
	}

	public void setHasNext(boolean b) {
		hasNext = b;
		nextButton.setEnabled(b);
		lastButton.setEnabled(b);
	}

	public void setHasPrevious(boolean b) {
		previousButton.setEnabled(b);
		firstButton.setEnabled(b);
	}

	// interface
	// ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (e.getSource() == nextButton)
			next();

		else if (e.getSource() == previousButton)
			previous();

		else if (e.getSource() == lastButton)
			last();

		else if (e.getSource() == firstButton)
			first();
	}

}