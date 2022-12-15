package de.uib.utilities.swing.tabbedpane;

import javax.swing.JPanel;

import de.uib.utilities.logging.logging;

public class TabClientAdapter extends JPanel implements TabClient {

	public TabClientAdapter() {
		super();
		// logging.debug("-- TabClientAdapter created and made visible ");
	}

	public void reset() {
		logging.info(this, "TabClientAdapter.reset() ");
	}

	public boolean mayLeave() {
		boolean result = true;
		logging.debug(this, "TabClientAdapter.mayLeave() " + result);
		return result;
	}

}
