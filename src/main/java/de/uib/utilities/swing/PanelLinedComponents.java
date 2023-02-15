//Titel:		PanelLinedComponents
//Copyright: 	Copyright (c) 2011
//Autor:		Martina Hammel, Rupert Röder
//Organisation:	uib
//Beschreibung:		

package de.uib.utilities.swing;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import de.uib.configed.Globals;

public class PanelLinedComponents extends JPanel {

	protected JComponent[] components;

	protected int myHeight;

	public PanelLinedComponents(JComponent[] components) {
		setComponents(components);
	}

	public PanelLinedComponents() {
		super();
	}

	public void setComponents(JComponent[] components, int height) {
		this.components = components;
		myHeight = height;
		defineLayout();
	}

	private void setComponents(JComponent[] components) {
		setComponents(components, Globals.LINE_HEIGHT);
	}

	protected void defineLayout() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGap(Globals.HGAP_SIZE);
		if (components != null) {
			for (int j = 0; j < components.length; j++) {
				hGroup.addComponent(components[j], 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
				hGroup.addGap(Globals.HGAP_SIZE);
			}
		}
		layout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGap(0, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2);

		GroupLayout.ParallelGroup vGroup1 = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER);

		if (components != null) {
			for (int j = 0; j < components.length; j++) {
				vGroup1.addComponent(components[j], myHeight, myHeight, myHeight);
			}
		}

		vGroup.addGroup(vGroup1);

		vGroup.addGap(0, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2);
		layout.setVerticalGroup(vGroup);
	}
}
