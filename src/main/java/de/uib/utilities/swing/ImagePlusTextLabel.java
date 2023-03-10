package de.uib.utilities.swing;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class ImagePlusTextLabel extends JPanel {
	JLabel textlabel;
	JLabel imagefield;
	int imageWidth;

	public ImagePlusTextLabel(int imageWidth) {
		super();
		this.imageWidth = imageWidth;
		initComponents();
	}

	private void initComponents() {

		textlabel = new JLabel();
		textlabel.setHorizontalAlignment(SwingConstants.LEFT);
		imagefield = new JLabel();
		imagefield.setHorizontalAlignment(SwingConstants.CENTER);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(2)
						.addComponent(imagefield, imageWidth, imageWidth, imageWidth).addGap(2)
						.addComponent(textlabel, 10, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(2)))

		;

		layout.setVerticalGroup(layout.createSequentialGroup()

				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(imagefield, Globals.LINE_HEIGHT - 5, Globals.LINE_HEIGHT - 5,
								Globals.LINE_HEIGHT - 5)
						.addComponent(textlabel, Globals.LINE_HEIGHT - 5, Globals.LINE_HEIGHT - 5,
								Globals.LINE_HEIGHT - 5)));

	}

	public void setIconVisible(boolean b) {
		imagefield.setVisible(b);
	}

	public void setText(String text) {
		textlabel.setText(text);
	}

	public void setIcon(Icon icon) {
		imagefield.setIcon(icon);
	}

	/*
	 * without questioning if the childcomponent is null it leads to exception
	 * because it is set deeply into the Swing machine beforehand
	 */
	@Override
	public void setFont(Font font) {
		if (textlabel != null) {
			textlabel.setFont(font);
		}
	}

	@Override
	public Font getFont() {
		if (textlabel != null) {
			return textlabel.getFont();
		} else {
			return super.getFont();
		}
	}

	@Override
	public void setBackground(Color bg) {
		if (!ConfigedMain.OPSI_4_3) {
			if (textlabel != null) {
				textlabel.setBackground(bg);
			}
			if (imagefield != null) {
				imagefield.setBackground(bg);
			}
		}
	}

	@Override
	public void setForeground(Color fg) {
		if (textlabel != null) {
			textlabel.setForeground(fg);
		}
		if (imagefield != null) {
			imagefield.setForeground(fg);
		}
	}

}
