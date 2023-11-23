/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.logpane;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class AdaptingSlider extends JSlider implements ChangeListener, MouseWheelListener {
	private LogPane logPane;

	public AdaptingSlider(LogPane logPane, int min, int max, int value) {
		super(min, max, value);

		init(logPane);
	}

	private void init(LogPane logPane) {
		this.logPane = logPane;

		super.addChangeListener(this);
		addMouseWheelListener(this);

		produceLabels();

		super.setPaintLabels(true);
		super.setSnapToTicks(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.debug(this, "change event from sliderLevel, " + getValue());
		if (getValueIsAdjusting()) {
			return;
		}
		setCursor(Globals.WAIT_CURSOR);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Logging.debug(this, "activateShowLevel call");

				logPane.activateShowLevel();

				setCursor(null);
			}
		});
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Logging.debug(this, "MouseWheelEvent " + e);

		int newIndex = getValue() - e.getWheelRotation();

		Logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

		if (newIndex >= getMaximum()) {
			newIndex = getMaximum() - 1;
		} else if (newIndex < 0) {
			newIndex = 0;
		} else {
			// Do nothing when newIndex is inside valid Values
		}

		Logging.debug(this, "MouseWheelEvent newIndex " + newIndex);

		setValue(newIndex);
	}

	public void produceLabels() {
		Map<Integer, JLabel> levelMap = new LinkedHashMap<>();

		for (int i = getMinimum(); i <= logPane.getMaxExistingLevel(); i++) {
			levelMap.put(i, new JLabel("" + i));
		}

		for (int i = logPane.getMaxExistingLevel() + 1; i <= getMaximum(); i++) {
			levelMap.put(i, new JLabel(" . "));
		}

		setLabelTable(new Hashtable<>(levelMap));
	}
}
