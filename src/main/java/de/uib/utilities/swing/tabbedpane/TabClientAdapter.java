package de.uib.utilities.swing.tabbedpane;

import javax.swing.JPanel;

import de.uib.utilities.logging.logging;

public class TabClientAdapter extends JPanel implements TabClient {

	public TabClientAdapter() {
		super();
		// logging.debug("-- TabClientAdapter created and made visible ");
	}

	@Override
	public void reset() {
		logging.info(this, "TabClientAdapter.reset() ");
	}

	@Override
	public boolean mayLeave() {
		boolean result = true;
		logging.debug(this, "TabClientAdapter.mayLeave() " + result);
		return result;
	}

}
