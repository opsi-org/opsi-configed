package de.uib.configed.gui.productpage;

import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.AbstractPainter;

import de.uib.Main;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.datapanel.DefaultEditMapPanel;

public abstract class AbstractPanelEditProperties extends JXPanel {
	protected DefaultEditMapPanel productPropertiesPanel;

	protected int minHSize = 50;
	protected int prefHSize = 80;

	protected ConfigedMain mainController;

	protected String productEdited;

	protected AbstractPanelEditProperties(ConfigedMain mainController, DefaultEditMapPanel productPropertiesPanel) {
		super();
		this.mainController = mainController;
		this.productPropertiesPanel = productPropertiesPanel;

		if (!Main.THEMES) {
			super.setBackgroundPainter(new AbstractPainter<AbstractPanelEditProperties>() {
				@Override
				public void doPaint(Graphics2D g, AbstractPanelEditProperties obj, int width, int height) {
					g.setPaint(Globals.BACKGROUND_COLOR_7);
					g.fillRect(0, 0, width, height);
				}
			});
		}
	}

	public abstract JPanel getTitlePanel();

	public abstract void setTitlePanelActivated(boolean actived);

	public JLabel getDepotLabel() {
		return null;
	}
}
