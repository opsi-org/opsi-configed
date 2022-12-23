package de.uib.utilities.thread;

import java.awt.Component;
import java.awt.Cursor;
import java.util.concurrent.atomic.AtomicInteger;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.ActivityPanel;

public class WaitCursor {

	
	private static AtomicInteger objectCounting = new AtomicInteger();
	private static boolean allStopped = false;

	int objectNo;

	boolean ready = false;

	Cursor saveCursor;
	Component c;
	String callLocation;

	public WaitCursor() {
		this(null, new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public WaitCursor(Component c_calling) {
		this(c_calling, new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public WaitCursor(Component c_calling, String callLocation) {
		this(c_calling, new Cursor(Cursor.DEFAULT_CURSOR), callLocation);
	}

	public WaitCursor(Component c_calling, Cursor saveCursor) {
		this(c_calling, saveCursor, "(not specified)");
	}

	public WaitCursor(Component c_calling, Cursor saveCursor, String callLocation) {
		
		// objectCounting++;
		objectNo = objectCounting.addAndGet(1);
		allStopped = false;

		this.saveCursor = saveCursor;
		this.callLocation = callLocation;

		if (c_calling == null) {
			try {
				c = Globals.mainContainer;
			} catch (Exception ex) {
				logging.info(this, "retrieveBasePane " + ex);
				c = null;
			}
		} else
			c = c_calling;

		logging.debug(this,
				"adding instance " + objectNo + "-- call location at (" + callLocation + ") on component " + c);

		/*
		 * javax.swing.SwingUtilities.invokeLater(
		 * new Thread(){
		 * public void run()
		 * {
		 * ActivityPanel.setActing(true);
		 * 
		 * 
		 * 
		 * while (!ready && !allStopped)
		 * {
		 * try
		 * {
		 * Thread.sleep (200);
		 * 
		 * }
		 * catch (InterruptedException ex)
		 * {}
		 * }
		 * }
		 * }
		 * );
		 */

		if (java.awt.EventQueue.isDispatchThread()) {
			new Thread() {
				@Override
				public void run() {
					ActivityPanel.setActing(true);

					
					while (!ready && !allStopped) {
						try {
							Thread.sleep(200);
							
						} catch (InterruptedException ex) {
						}
					}
				}
			}.start();
		} else {
			javax.swing.SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					ActivityPanel.setActing(true);

					
					while (!ready && !allStopped) {
						try {
							Thread.sleep(200);
							
						} catch (InterruptedException ex) {
						}
					}
				}
			});
		}

	}

	public void stop() {
		logging.info(this, " stop wait cursor " + objectNo + ", was located at (" + callLocation + ")");
		ready = true;

		javax.swing.SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {

				if (c != null)
					c.setCursor(saveCursor);

				if (isStopped()) {
					objectCounting.decrementAndGet();
					logging.debug(this, "removing instance " + objectNo);
					if (objectCounting.get() <= 0)
					// if (instances.isEmpty())
					{
						logging.info(this, "seemed to be last living instance");
						ActivityPanel.setActing(false);
					} else {
						logging.debug(this, " stopped wait cursor " + " instance " + objectNo + ", " + " still active  "
								+ objectCounting + " the stopped cursor was initiated from " + callLocation
						// + getInstancesNumbers()
						);
					}
				}
			}
		});

	}

	/*
	 * public void stop()
	 * {
	 * logging.info(this, " stop wait cursor " + objectNo + ", was located at (" +
	 * callLocation + ")");
	 * ready = true;
	 * 
	 * if (c != null) c.setCursor(saveCursor);
	 * 
	 * objectCounting.decrementAndGet();
	 * logging.debug(this, "removing instance " + objectNo);
	 * if (objectCounting.get() == 0)
	 * //if (instances.isEmpty())
	 * {
	 * logging.debug(this, "seemed to be last living instance");
	 * ActivityPanel.setActing(false);
	 * }
	 * else
	 * {
	 * logging.info(this, " stopped wait cursor "
	 * + " instance " + objectNo + ", "
	 * + " still active  " + objectCounting
	 * //+ getInstancesNumbers()
	 * );
	 * }
	 * 
	 * }
	 */

	public boolean isStopped() {
		return ready;
	}

	public static void stopAll() {
		allStopped = true;

		/*
		 * Vector<WaitCursor> instancesCopy = new Vector<>();
		 * for (WaitCursor instance : instances)
		 * {
		 * instancesCopy.add(instance);
		 * }
		 * for (WaitCursor instance : instancesCopy)
		 * {
		 * instance.stop();
		 * }
		 */
	}

	/*
	 * private static String getInstancesNumbers()
	 * {
	 * StringBuffer listing = new StringBuffer("[ ");
	 * for (WaitCursor inst : instances)
	 * {
	 * listing.append(inst.getObjectNo());
	 * listing.append(" ");
	 * }
	 * listing.append("]");
	 * 
	 * return listing.toString();
	 * }
	 */
}
