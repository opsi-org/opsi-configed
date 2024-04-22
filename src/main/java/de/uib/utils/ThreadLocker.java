/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.uib.utils.logging.Logging;

public class ThreadLocker {
	private CountDownLatch locker;

	public void lock() {
		try {
			locker = new CountDownLatch(1);
			locker.await();
		} catch (InterruptedException ie) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	public void lock(int timeout) {
		try {
			locker = new CountDownLatch(1);
			if (locker.await(timeout, TimeUnit.MILLISECONDS)) {
				Logging.info(this, "thread was unblocked");
			} else {
				Logging.info(this, "time ellapsed");
			}
		} catch (InterruptedException ie) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	public void unlock() {
		locker.countDown();
	}
}
