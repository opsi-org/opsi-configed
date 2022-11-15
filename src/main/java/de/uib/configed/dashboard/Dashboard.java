package de.uib.configed.dashboard;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import javafx.application.*;
import javafx.embed.swing.*;

import de.uib.configed.*;
import de.uib.configed.dashboard.view.*;
import de.uib.utilities.logging.*;

public class Dashboard
{
	public static final String MAIN_VIEW = "main";
	public static final String CLIENT_VIEW = "client";
	public static final String PRODUCT_VIEW = "product";

	private static final int WINDOW_HEIGHT = 1200;
	private static final int WINDOW_WIDTH = 860;

	private final JFrame frame = new JFrame();

	private MainView mainView;

	public void initAndShowGUI()
	{
		final JFXPanel fxPanel = new JFXPanel();
		frame.add(fxPanel);
		frame.setIconImage(Globals.mainIcon);
		frame.setTitle(configed.getResourceValue("Dashboard.title"));
		frame.setMinimumSize(new Dimension(WINDOW_HEIGHT, WINDOW_WIDTH));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				frame.setVisible(false);
				Platform.runLater(() -> ViewManager.displayView(MAIN_VIEW));
			}
		});

		Platform.runLater(() ->
		{
			try
			{
				initFX(fxPanel);
			}
			catch (IOException ioE)
			{
				ioE.printStackTrace();
				logging.error(this, "Unable to open fxml file");
			}
		});
	}

	public void initFX(final JFXPanel fxPanel) throws IOException
	{
		mainView = new MainView(fxPanel);
		ClientView clientView = new ClientView(fxPanel);
		ProductView productView = new ProductView(fxPanel);

		ViewManager.addView(MAIN_VIEW, mainView);
		ViewManager.addView(CLIENT_VIEW, clientView);
		ViewManager.addView(PRODUCT_VIEW, productView);
		ViewManager.displayView(MAIN_VIEW);

		mainView.init();
	}

	public void show()
	{
		frame.setVisible(true);

		if (mainView != null)
		{
			mainView.init();
		}
	}
}
