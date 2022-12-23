package de.uib.configed.gui;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uib.configed.Globals;

/**
 * This class is intended to show a list in text area
 */
public class FShowListWithComboSelect extends FShowList {
	String[] choices;
	JComboBox combo;
	JLabel labelChoice;

	public FShowListWithComboSelect(JFrame owner, String title, boolean modal, String choiceTitle, String[] choices,
			String[] buttonList) {
		super(owner, title, modal, buttonList);
		
		labelChoice = new JLabel(choiceTitle + ": ");
		labelChoice.setOpaque(true);
		labelChoice.setBackground(Globals.backgroundLightGrey);
		northPanel.add(labelChoice);
		combo = new JComboBox<>(choices);
		combo.setFont(Globals.defaultFontBold);
		northPanel.add(combo);
		
		
		// HorizontalPositioner northPanel = new HorizontalPositioner (new
		
		
		pack();
		
		

	}

	public Object getChoice() {
		return combo.getSelectedItem();
	}
}
