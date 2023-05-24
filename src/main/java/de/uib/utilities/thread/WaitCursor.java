/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.thread;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.ActivityPanel;

public class WaitCursor {

	private static AtomicInteger objectCounting = new AtomicInteger();
	private static boolean allStopped;

	private int objectNo;

	private boolean ready;

	private Cursor saveCursor;
	private Component c;
	private String callLocation;

	public WaitCursor() {
		this(null, new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public WaitCursor(Component componentCalling) {
		this(componentCalling, new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public WaitCursor(Component componentCalling, String callLocation) {
		this(componentCalling, new Cursor(Cursor.DEFAULT_CURSOR), callLocation);
	}

	public WaitCursor(Component componentCalling, Cursor saveCursor) {
		this(componentCalling, saveCursor, "(not specified)");
	}

	public WaitCursor(Component componentCalling, Cursor saveCursor, String callLocation) {

		objectNo = objectCounting.addAndGet(1);
		allStopped = false;

		this.saveCursor = saveCursor;
		this.callLocation = callLocation;

		if (componentCalling == null) {
			try {
				c = ConfigedMain.getMainFrame();
			} catch (Exception ex) {
				Logging.info(this, "retrieveBasePane " + ex);
				c = null;
			}
		} else {
			c = componentCalling;
		}

		Logging.debug(this,
				"adding instance " + objectNo + "-- call location at (" + callLocation + ") on component " + c);

		if (EventQueue.isDispatchThread()) {
			new Thread() {
				@Override
				public void run() {
					ActivityPanel.setActing(true);

					while (!ready && !allStopped) {
						Globals.threadSleep(this, 200);
					}
				}
			}.start();
		} else {
			SwingUtilities.invokeLater(() -> {
				ActivityPanel.setActing(true);

				while (!ready && !allStopped) {
					Globals.threadSleep(this, 200);
				}
			});
		}

	}

	public void stop() {
		Logging.info(this, " stop wait cursor " + objectNo + ", was located at (" + callLocation + ")");
		ready = true;

		SwingUtilities.invokeLater(() -> {

			if (c != null) {
				c.setCursor(saveCursor);
			}

			if (isStopped()) {
				objectCounting.decrementAndGet();
				Logging.debug(this, "removing instance " + objectNo);
				if (objectCounting.get() <= 0) {

					Logging.info(this, "seemed to be last living instance");
					ActivityPanel.setActing(false);
				} else {
					Logging.debug(this, " stopped wait cursor " + " instance " + objectNo + ", " + " still active  "
							+ objectCounting + " the stopped cursor was initiated from " + callLocation);
				}
			}
		});

	}

	public boolean isStopped() {
		return ready;
	}

	public static void stopAll() {
		allStopped = true;
	}

}
