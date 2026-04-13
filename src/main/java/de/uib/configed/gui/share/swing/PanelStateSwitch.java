/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

/*
*	PanelStateSwitch.java
*	for a given enum, this class builds a panel 
*	with radio buttons to switch between the Enums
*/
public class PanelStateSwitch<E extends Enum<E>> extends JPanel {
	private Enum<E> producedValue;
	private Enum<E> startValue;
	private Class<?> myenumClass;
	private Enum<E>[] values;
	private Map<Enum<E>, String> labels;
	private String title;
	private Map<Enum<E>, JRadioButton> groupedButtons;
	private Consumer<Enum<E>> enumSetter;

	private List<ChangeListener> changeListeners;

	public PanelStateSwitch(String title, Enum<E> startValue, Enum<E>[] values, Class<?> myenum,
			Consumer<Enum<E>> enumSetter) {
		this(title, startValue, values, null, myenum, enumSetter);
	}

	public PanelStateSwitch(String title, Enum<E> startValue, Enum<E>[] values, String[] labels, Class<?> myenum,
			Consumer<Enum<E>> enumSetter) {
		Logging.info(this, " my enum ", myenum);

		this.title = title;

		changeListeners = new ArrayList<>();

		if (labels != null && labels.length < values.length) {
			Logging.warning(this, "missing label");
		}

		this.labels = new HashMap<>();

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
			Logging.info(this, " type of myenum ", myenumClass.getTypeName());

			Logging.info(this, " enum constants ", Arrays.toString(myenumClass.getEnumConstants()));

			int i = 0;
			for (Object constant : myenumClass.getEnumConstants()) {
				if (i == 0) {
					producedValue = (Enum) constant;
				}
				i++;
				Logging.info(this, " enum constant  ", constant, " class ", constant.getClass());
			}
		}

		this.startValue = startValue;

		Logging.info(this, " string val of start value ", startValue.toString());

		initComponents();

		initLayout();
	}

	public void addChangeListener(ChangeListener cl) {
		changeListeners.add(cl);
	}

	protected void notifyChangeListeners(ChangeEvent e) {
		Logging.info(this, "notifyChangeListeners ", e);
		for (ChangeListener cl : changeListeners) {
			cl.stateChanged(e);
		}
	}

	private void initComponents() {
		ButtonGroup buttonGroup = new ButtonGroup();
		groupedButtons = new HashMap<>();

		for (Enum<E> val : values) {
			JRadioButton button = new JRadioButton(labels.get(val));
			button.setHorizontalTextPosition(SwingConstants.RIGHT);

			buttonGroup.add(button);

			groupedButtons.put(val, button);
			button.addActionListener((ActionEvent ae) -> {
				producedValue = val;
				if (enumSetter != null) {
					enumSetter.accept(val);
				}

				Logging.debug(this, "actionEvent with result ", val);
				notifyChangeListeners(new ChangeEvent(this));
			});
		}

		producedValue = startValue;

		groupedButtons.get(startValue).setSelected(true);

		setValueByString(startValue.toString());
	}

	private void initLayout() {
		JLabel labelTitle = new JLabel(title);

		this.setLayout(new MigLayout("insets 0, wrap 1", "[pref!]", "[]0"));

		if (title != null) {
			add(labelTitle, "wmin 20");
		}

		for (Enum<E> val : values) {
			add(groupedButtons.get(val), "wmin 20");
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

		Logging.info(this, "setValueByString ", producedValue);

		if (enumSetter != null) {
			enumSetter.accept(producedValue);
		}
	}
}
