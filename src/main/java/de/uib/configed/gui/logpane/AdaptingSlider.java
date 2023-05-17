package de.uib.configed.gui.logpane;

import java.awt.Cursor;
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

import de.uib.configed.ConfigedMain;
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

		if (!ConfigedMain.FONT) {
			super.setFont(Globals.defaultFont);
		}

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

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Logging.debug(this, "activateShowLevel call");
				Cursor startingCursor = getCursor();
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				try {
					logPane.activateShowLevel();
				} catch (Exception ex) {
					Logging.debug(this, "Exception in activateShowLevel " + ex);
				}
				setCursor(startingCursor);
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

		try {
			setLabelTable(new Hashtable<>(levelMap));
		} catch (Exception ex) {
			Logging.info(this, "setLabelTable levelDict " + levelMap + " ex " + ex);
		}
	}
}
