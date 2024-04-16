/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

/*
 * FEditDate.java
 *
 */

package de.uib.utils.swing;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;

import com.formdev.flatlaf.FlatLaf;

import de.uib.utils.logging.Logging;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;

public class FEditDate extends FEdit implements MouseListener {
	public static final Dimension AREA_DIMENSION = new Dimension(380, 300);
	private DatePicker datePicker;

	public FEditDate(String initialText) {
		super(initialText);
		areaDimension = AREA_DIMENSION;

		initFX();
		setStartText(this.initialText);
	}

	private void initFX() {
		JFXPanel jfxPanel = new JFXPanel();

		Platform.setImplicitExit(false);
		Platform.runLater(() -> showDatePicker(jfxPanel));

		editingArea.add(jfxPanel);
		jfxPanel.addKeyListener(this);
	}

	private void showDatePicker(JFXPanel jfxPanel) {
		datePicker = new DatePicker();
		datePicker.setOnAction((ActionEvent event) -> {
			setDataChanged(true);
			updateCaller(datePicker.getValue().toString());
		});
		DatePickerSkin skin = new DatePickerSkin(datePicker);
		StackPane pane = new StackPane(skin.getPopupContent());
		Scene scene = new Scene(pane);
		if (FlatLaf.isLafDark()) {
			scene.getStylesheets().add(getClass().getResource("/css/date-picker-dark.css").toExternalForm());
		} else {
			scene.getStylesheets().add(getClass().getResource("/css/date-picker-light.css").toExternalForm());
		}
		jfxPanel.setScene(scene);
	}

	@Override
	public final void setStartText(String s) {
		super.setStartText(s);

		Logging.info(this, "setStartText(): " + s);

		if (s != null && !s.isEmpty()) {
			datePicker.setValue(LocalDate.parse(s));
			setDataChanged(false);
		}
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			Platform.runLater(() -> datePicker.requestFocus());
		}
		super.setVisible(b);
		setSize(areaDimension);
		if (b) {
			setStartText(initialText);
		}
	}

	@Override
	public String getText() {
		Logging.info(this, "getText initialText was " + initialText);
		initialText = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
		Logging.info(this, "getText initialText changed to  " + initialText);
		return initialText;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Logging.debug(this, " key event " + e);
		super.keyPressed(e);

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			cancel();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			commit();
		} else {
			// Do nothing on other keys
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
			commit();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}
}
