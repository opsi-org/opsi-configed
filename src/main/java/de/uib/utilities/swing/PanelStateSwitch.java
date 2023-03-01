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
import java.util.Map;

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
import de.uib.utilities.logging.Logging;

public class PanelStateSwitch<E extends Enum<E>> extends JPanel {

	protected Enum<E> producedValue;
	protected Enum<E> startValue;
	protected Class<?> myenumClass;
	protected Enum<E>[] values;
	protected Map<Enum<E>, String> labels;
	protected String title;
	protected Map<Enum<E>, JRadioButton> groupedButtons;
	protected Enumsetter<E> enumSetter;
	protected Font primaryFont;
	protected int vGap;
	protected int hGap;

	protected List<ChangeListener> changeListeners;

	@FunctionalInterface
	public interface Enumsetter<E extends Enum<E>> {
		public void setValue(Enum<E> val);
	}

	public PanelStateSwitch(Enum<E> startValue, Enum<E>[] values, Class<?> myenum, Enumsetter<E> enumSetter) {
		this(null, startValue, values, null, myenum, enumSetter);
	}

	public PanelStateSwitch(String title, Enum<E> startValue, Enum<E>[] values, Class<?> myenum,
			Enumsetter<E> enumSetter) {
		this(title, startValue, values, null, myenum, enumSetter);
	}

	public PanelStateSwitch(String title, Enum<E> startValue, Enum<E>[] values, String[] labels, Class<?> myenum,
			Enumsetter<E> enumSetter) {
		this(title, startValue, values, labels, myenum, enumSetter, 0, 0);
	}

	public PanelStateSwitch(String title, Enum<E> startValue, Enum<E>[] values, String[] labels, Class<?> myenum,
			Enumsetter<E> enumSetter, int hGap, int vGap) {

		Logging.info(this, " my enum " + myenum);

		this.title = title;

		this.hGap = hGap;
		this.vGap = vGap;

		changeListeners = new ArrayList<>();

		if (labels != null && labels.length < values.length) {
			Logging.warning(this, "missing label");
		}

		this.labels = new LinkedHashMap<>();

		for (int i = 0; i < values.length; i++) {
			if (labels == null || i > labels.length - 1) {
				this.labels.put(values[i], values[i].toString());
			} else {
				this.labels.put(values[i], labels[i]);
			}
		}

		myenumClass = myenum;
		this.values = values;
		this.enumSetter = enumSetter;

		if (myenumClass != null && myenumClass.isEnum()) {
			Logging.info(this, " type of myenum " + myenumClass.getTypeName());

			Logging.info(this, " enum constants " + Arrays.toString(myenumClass.getEnumConstants()));

			int i = 0;
			for (Object constant : myenumClass.getEnumConstants()) {
				if (i == 0) {
					producedValue = (Enum) constant;
				}
				i++;
				Logging.info(this, " enum constant  " + constant + " class " + constant.getClass());
			}
		}

		this.startValue = startValue;

		Logging.info(this, " string val of start value " + startValue.toString());

		initComponents();

		initLayout();

	}

	public void addChangeListener(ChangeListener cl) {
		changeListeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		changeListeners.remove(cl);
	}

	protected void notifyChangeListeners(ChangeEvent e) {
		Logging.info(this, "notifyChangeListeners " + e);
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}

	private void initComponents() {
		primaryFont = Globals.defaultFont;
		ButtonGroup buttonGroup = new ButtonGroup();
		groupedButtons = new LinkedHashMap<>();

		ImageIcon activatedIcon = Globals.createImageIcon("images/checked_withoutbox.png", "");
		ImageIcon deactivatedIcon = Globals.createImageIcon("images/checked_empty_withoutbox.png", "");

		for (Enum<E> val : values) {
			JRadioButton button = new JRadioButton(labels.get(val));

			button.setIcon(deactivatedIcon);
			button.setSelectedIcon(activatedIcon);
			button.setHorizontalTextPosition(SwingConstants.RIGHT);
			button.setFont(primaryFont);

			buttonGroup.add(button);

			groupedButtons.put(val, button);
			button.addActionListener((ActionEvent ae) -> {
				producedValue = val;
				if (enumSetter != null) {
					enumSetter.setValue(val);
				}

				Logging.debug(this, "actionEvent with result " + val);
				notifyChangeListeners(new ChangeEvent(this));
			})

			;

			// hack to get the icons behaving as expected
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {

					if (!button.isSelected()) {
						button.setSelectedIcon(deactivatedIcon);
					}
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

		setValueByString(startValue.toString());
	}

	private void initLayout() {
		setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		JLabel labelTitle = new JLabel("");
		if (title != null) {
			labelTitle.setText(title);
		}

		labelTitle.setFont(primaryFont);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		// this.setBorder(new

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setVerticalGroup(vGroup);

		vGroup.addGap(vGap);

		if (title != null) {
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(labelTitle,
					Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT));
		}

		for (Enum<E> val : values) {
			vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
					groupedButtons.get(val), Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)

			);
		}

		vGroup.addGap(vGap);

		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		layout.setHorizontalGroup(hGroup);

		if (title != null) {
			hGroup.addGroup(layout.createSequentialGroup().addGap(hGap)
					.addComponent(labelTitle, 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(hGap));
		}

		for (Enum<E> val : values) {
			hGroup.addGroup(layout.createSequentialGroup().addGap(hGap)
					.addComponent(groupedButtons.get(val), 20, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(hGap));
		}

	}

	public Enum<E> getValue() {
		return producedValue;
	}

	public void setValueByString(String valS) {
		// keeps old produced value if valS does not match
		for (Enum<E> val : values) {
			if (val.name().equals(valS)) {
				producedValue = val;
				break;
			}
		}

		groupedButtons.get(producedValue).setSelected(true);

		Logging.info(this, "setValueByString " + producedValue);

		if (enumSetter != null) {
			enumSetter.setValue(producedValue);
		}
	}
}
