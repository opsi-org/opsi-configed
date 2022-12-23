/*
*	PanelStateSwitch.java
*	for a given enum, this class builds a panel 
*	with radio buttons to switch between the Enums
*
* By uib, www.uib.de, 2017
* Author: Rupert Röder
*/

package de.uib.utilities.swing;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;

public class PanelStateSwitch extends JPanel {

	protected Enum producedValue;
	protected Enum startValue;
	protected Class myenumClass;
	protected Enum[] values;
	protected LinkedHashMap<Enum, String> labels;
	protected String title;
	protected LinkedHashMap<Enum, JRadioButton> groupedButtons;
	protected Enumsetter enumSetter;
	protected Font primaryFont;
	protected int vGap;
	protected int hGap;

	protected List<ChangeListener> changeListeners;

	@FunctionalInterface
	public interface Enumsetter {
		public void setValue(Enum val);
	}

	/*
	 * 
	 * public PanelStateSwitch(Enum startValue, Enum[]values)
	 * {
	 * 
	 * this( startValue, values, null);
	 * 
	 * }
	 */

	public PanelStateSwitch(Enum startValue, Enum[] values, Class myenum, Enumsetter enumSetter) {
		this(null, startValue, values, null, myenum, enumSetter);
	}

	public PanelStateSwitch(String title, Enum startValue, Enum[] values, Class myenum, Enumsetter enumSetter) {
		this(title, startValue, values, null, myenum, enumSetter);
	}

	public PanelStateSwitch(String title, Enum startValue, Enum[] values, String[] labels, Class myenum,
			Enumsetter enumSetter) {
		this(title, startValue, values, labels, myenum, enumSetter, 0, 0);
	}

	public PanelStateSwitch(String title, Enum startValue, Enum[] values, String[] labels, Class myenum,
			Enumsetter enumSetter, int hGap, int vGap) {

		logging.info(this, " my enum " + myenum);

		this.title = title;

		this.hGap = hGap;
		this.vGap = vGap;

		changeListeners = new ArrayList<>();

		if (labels != null && labels.length < values.length)
			logging.warning(this, "missing label");

		this.labels = new LinkedHashMap<>();

		for (int i = 0; i < values.length; i++) {
			if (labels == null || i > labels.length - 1)
				this.labels.put(values[i], values[i].toString());
			else
				this.labels.put(values[i], labels[i]);
		}

		myenumClass = myenum;
		this.values = values;
		this.enumSetter = enumSetter;

		if (myenumClass != null) {
			if (myenumClass.isEnum()) {
				logging.info(this, " type of myenum " + myenumClass.getTypeName());

				logging.info(this, " enum constants " + Arrays.toString(myenumClass.getEnumConstants()));

				int i = 0;
				for (Object constant : myenumClass.getEnumConstants()) {
					if (i == 0)
						producedValue = (Enum) constant;
					i++;
					logging.info(this, " enum constant  " + constant + " class " + constant.getClass());
				}

			}
		}

		this.startValue = startValue;

		logging.info(this, " string val of start value " + startValue.toString());

		initComponents();

		setValueByString(startValue.toString());

		initLayout();

	}

	public void addChangeListener(ChangeListener cl) {
		changeListeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		changeListeners.remove(cl);
	}

	protected void notifyChangeListeners(ChangeEvent e) {
		logging.info(this, "notifyChangeListeners " + e);
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}

	protected void initComponents() {
		primaryFont = Globals.defaultFont;
		ButtonGroup buttonGroup = new ButtonGroup();
		groupedButtons = new LinkedHashMap<>();

		ImageIcon activatedIcon = Globals.createImageIcon("images/checked_withoutbox.png", "");
		ImageIcon deactivatedIcon = Globals.createImageIcon("images/checked_empty_withoutbox.png", "");

		for (Enum val : values) {
			JRadioButton button = new JRadioButton(labels.get(val));

			button.setIcon(deactivatedIcon);
			button.setSelectedIcon(activatedIcon);
			button.setHorizontalTextPosition(SwingConstants.RIGHT);
			button.setFont(primaryFont);

			buttonGroup.add(button);

			groupedButtons.put(val, button);
			button.addActionListener((ActionEvent ae) -> {
				producedValue = val;
				if (enumSetter != null)
					enumSetter.setValue(val);
				logging.debug(this, "actionEvent with result " + val);
				notifyChangeListeners(new ChangeEvent(this));
			})

			;

			// hack to get the icons behaving as expected
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {

					if (!button.isSelected())
						button.setSelectedIcon(deactivatedIcon);
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					button.setSelectedIcon(activatedIcon);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					button.setSelectedIcon(activatedIcon);
				}
			});
		}

		producedValue = startValue;

		groupedButtons.get(startValue).setSelected(true);
	}

	protected void initLayout() {
		setBackground(Globals.backgroundWhite);

		JLabel labelTitle = new JLabel("");
		if (title != null)
			labelTitle.setText(title);

		labelTitle.setFont(primaryFont);

		GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		// this.setBorder(new

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setVerticalGroup(vGroup);

		vGroup.addGap(vGap);

		if (title != null)
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(labelTitle,
					Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT));

		for (Enum val : values) {
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
					groupedButtons.get(val), Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)

			);
		}

		vGroup.addGap(vGap);

		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		layout.setHorizontalGroup(hGroup);

		if (title != null)
			hGroup.addGroup(layout.createSequentialGroup().addGap(hGap)
					.addComponent(labelTitle, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(hGap));

		for (Enum val : values) {
			hGroup.addGroup(layout.createSequentialGroup().addGap(hGap)
					.addComponent(groupedButtons.get(val), 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(hGap));
		}

	}

	public Enum getValue() {
		return producedValue;
	}

	public void setValueByString(String valS)
	// keeps old produced value if valS does not match
	{
		for (Enum val : values) {
			if (val.name().equals(valS)) {
				producedValue = val;
				break;
			}
		}

		groupedButtons.get(producedValue).setSelected(true);

		logging.info(this, "setValueByString " + producedValue);

		if (enumSetter != null)
			enumSetter.setValue(producedValue);
	}
}
