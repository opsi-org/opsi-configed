package de.uib.configed.gui;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.configed.HostsStatusInfo;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.Containership;

public class HostsStatusPanel extends JPanel implements HostsStatusInfo {
	public static final int maxClientnamesInField = 10;

	JLabel labelAllClientsCount;
	JTextField fieldGroupActivated;

	JTextField fieldSelectedClientsNames;
	JTextField fieldActivatedClientsCount;
	JTextField fieldInvolvedDepots;

	public HostsStatusPanel() {
		super();
		createGui();
	}

	@Override
	public void setGroupName(String s) {
		logging.info(this, "setGroupName " + s);
		resetReportedClients();
		fieldGroupActivated.setText(s);
	}

	@Override
	public String getSelectedClientNames() {
		return fieldSelectedClientsNames.getText();
	}

	@Override
	public String getInvolvedDepots() {
		return fieldInvolvedDepots.getText();
	}

	@Override
	public String getGroupName() {
		return fieldGroupActivated.getText();
	}

	private void resetReportedClients() {
		fieldActivatedClientsCount.setText("");
		fieldSelectedClientsNames.setText("");
		fieldSelectedClientsNames.setToolTipText("");

		fieldInvolvedDepots.setText("");
	}

	private void initializeValues() {
		labelAllClientsCount.setText(configed.getResourceValue("MainFrame.labelClientsTotal") + " ");
		resetReportedClients();
		fieldInvolvedDepots.setText("");
		fieldInvolvedDepots.setToolTipText("");
	}

	@Override
	public void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots) {
		logging.info(this,
				"updateValues clientsCount, selectedClientsCount " + clientsCount + ", " + selectedClientsCount);
		logging.info(this,
				"updateValues clientsCount, selectedClientsCount " + clientsCount + ", " + selectedClientsCount);

		labelAllClientsCount.setText(configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount);

		setFieldClientsCount(selectedClientsCount);

		if (selectedClientNames == null) {
			fieldSelectedClientsNames.setText("");
			fieldSelectedClientsNames.setToolTipText(null);
		} else

		{

			/*
			 * String oldSelectedClientNames = fieldSelectedClientsNames.getText();
			 * if (oldSelectedClientNames != null
			 * &&
			 * selectedClientNames.indexOf(oldSelectedClientNames) > -1
			 * )
			 * {
			 * fieldSelectedClientsNames.setSelectionColor(Color.yellow);
			 * fieldSelectedClientsNames.setSelectionStart(oldSelectedClientNames.length());
			 * logging.debug(" sel start " + oldSelectedClientNames.length());
			 * fieldSelectedClientsNames.setSelectionEnd(selectedClientNames.length());
			 * logging.debug(" sel end " + selectedClientNames.length());
			 * }
			 */

			fieldSelectedClientsNames.setText(selectedClientNames);

			fieldSelectedClientsNames.setToolTipText(
					"<html><body><p>" + selectedClientNames.replace(";\n", "<br\\ >") + "</p></body></html>");

		}

		if (involvedDepots != null) {
			fieldInvolvedDepots.setText(involvedDepots);
			fieldInvolvedDepots.setToolTipText(
					"<html><body><p>" + involvedDepots.replace(";\n", "<br\\ >") + "</p></body></html>");
		}
	}

	@Override
	public void setGroupClientsCount(int n) {
		String newS = null;
		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(0, bracketIndex);
			newS = keep + "(" + n + ")";
		} else
			newS = "(" + n + ")";
		fieldActivatedClientsCount.setText(newS);
	}

	private void setFieldClientsCount(Integer n) {
		String newS = null;
		if (n != null)
			newS = "" + n + " ";
		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(bracketIndex);
			newS = newS + keep;
		}

		fieldActivatedClientsCount.setText(newS);
	}

	private void createGui() {
		Containership csStatusPane = new Containership(this);

		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		JLabel labelActivated = new JLabel(configed.getResourceValue("MainFrame.activated"));

		JLabel labelGroupActivated = new JLabel(configed.getResourceValue("MainFrame.groupActivated"));

		fieldGroupActivated = new JTextField("");

		fieldGroupActivated.setPreferredSize(Globals.counterfieldDimension);
		fieldGroupActivated.setEditable(false);

		labelAllClientsCount = new JLabel("");
		labelAllClientsCount.setPreferredSize(Globals.labelDimension);

		JLabel labelSelectedClientsCount = new JLabel(configed.getResourceValue("MainFrame.labelSelected"));

		JLabel labelSelectedClientsNames = new JLabel(configed.getResourceValue("MainFrame.labelNames"));

		JLabel labelInvolvedDepots = new JLabel(configed.getResourceValue("MainFrame.labelInDepot"));

		JLabel labelInvolvedDepots2 = new JLabel(configed.getResourceValue("MainFrame.labelInDepot2"));

		// fieldAllClientsCount = new JTextField("");

		fieldActivatedClientsCount = new JTextField("");
		fieldActivatedClientsCount.setPreferredSize(Globals.counterfieldDimension);
		fieldActivatedClientsCount.setEditable(false);

		fieldSelectedClientsNames = new JTextField("");

		fieldSelectedClientsNames.setPreferredSize(Globals.counterfieldDimension);
		fieldSelectedClientsNames.setEditable(false);
		fieldSelectedClientsNames.setDragEnabled(true);

		// JScrollPane viewSelectedClientsNames = new

		fieldInvolvedDepots = new JTextField("");
		fieldInvolvedDepots.setPreferredSize(Globals.counterfieldDimension);
		fieldInvolvedDepots.setEditable(false);

		initializeValues();

		layoutStatusPane.setHorizontalGroup(layoutStatusPane.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelAllClientsCount, 0, Globals.COUNTERFIELD_WIDTH, Globals.COUNTERFIELD_WIDTH)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelGroupActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(fieldGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(labelSelectedClientsNames, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addComponent(fieldSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE)
				.addComponent(fieldActivatedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(labelInvolvedDepots, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(fieldInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(labelInvolvedDepots2, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE));

		layoutStatusPane.setVerticalGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutStatusPane.createSequentialGroup()
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
						.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(labelAllClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelGroupActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldGroupActivated, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelSelectedClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldActivatedClientsCount, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelSelectedClientsNames, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldSelectedClientsNames, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelInvolvedDepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(labelInvolvedDepots2, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT)
								.addComponent(fieldInvolvedDepots, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
										Globals.LINE_HEIGHT))
						.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)));

		/*
		 * csStatusPane.doForAllContainedCompisOfClass
		 * ("setOpaque", new Object[]{true}, javax.swing.JLabel.class);
		 * csStatusPane.doForAllContainedCompisOfClass
		 * ("setBackground", new Object[]{Globals.backLightBlue},
		 * javax.swing.JLabel.class);
		 */
		csStatusPane.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backgroundLightGrey },
				javax.swing.text.JTextComponent.class);
	}
}
