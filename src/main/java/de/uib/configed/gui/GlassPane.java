/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Globals;
import de.uib.configed.dashboard.ComponentStyler;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class GlassPane extends JComponent implements KeyListener {
	private ProgressIndicator wheel;
	private Label jLabelInfo;

	public GlassPane() {
		super.setBackground(initBackground());
		super.setOpaque(false);

		initFX();
		addEventCatchers();
	}

	private void addEventCatchers() {
		addMouseListener(new MouseAdapter() {
		});
		addMouseMotionListener(new MouseMotionAdapter() {
		});
		addKeyListener(this);
		setFocusTraversalKeysEnabled(false);
	}

	private static Color initBackground() {
		Color base;

		if (FlatLaf.isLafDark()) {
			base = Color.BLACK;
		} else {
			base = Color.WHITE;
		}

		return new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
	}

	private void initFX() {
		JFXPanel jfxPanel = new JFXPanel();
		jfxPanel.setOpaque(false);
		jfxPanel.setBackground(initBackground());
		setLayout(new BorderLayout());
		add(jfxPanel, BorderLayout.CENTER);

		Platform.setImplicitExit(false);
		Platform.runLater(() -> initFXComponents(jfxPanel));
	}

	private void initFXComponents(JFXPanel jfxPanel) {
		wheel = new ProgressIndicator();
		wheel.setScaleX(0.5);
		wheel.setScaleY(0.5);
		wheel.setStyle(
				"-fx-progress-color: " + ComponentStyler.getHexColor(UIManager.getColor("ProgressBar.foreground")));
		jLabelInfo = new Label();
		ComponentStyler.styleLabelComponent(jLabelInfo);

		VBox vbox = new VBox();
		vbox.getChildren().add(wheel);
		vbox.getChildren().add(jLabelInfo);
		vbox.setAlignment(Pos.CENTER);
		vbox.setStyle("-fx-background-color: transparent;");

		Scene scene = new Scene(vbox, javafx.scene.paint.Color.TRANSPARENT);
		jfxPanel.setScene(scene);
	}

	public void setInfoText(String s) {
		Platform.runLater(() -> jLabelInfo.setText(s));
	}

	/*
	 *  The component is transparent but we want to paint the background
	 *  to give it the disabled look.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
	}

	/*
	 *  Make the glass pane and wheel visible, and change the cursor to the wait cursor.
	 */
	public void activate(boolean toggle) {
		Platform.runLater(() -> wheel.setVisible(toggle));
		setVisible(toggle);
		setCursor(getCursor());
		if (isVisible()) {
			requestFocusInWindow();
		} else {
			setInfoText(null);
		}
	}

	public void activateLoadingCursor() {
		setCursor(Globals.WAIT_CURSOR);
	}

	public void deactivateLoadingCursor() {
		setCursor(null);
	}

	@Override
	public Cursor getCursor() {
		return this.isVisible() ? Globals.WAIT_CURSOR : null;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
