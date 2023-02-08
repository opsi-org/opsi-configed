/*
 * PanelEditLicences.java
 * for backend editing of three tables 
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * @author roeder
 */
public class PanelEditLicences extends MultiTablePanel {
	public PanelGenEditTable panelKeys;
	public PanelGenEditTable panelSoftwarelicences;
	public PanelGenEditTable panelLicencecontracts;

	private int splitPaneHMargin = 1;

	protected int minVSize = 100;

	/** Creates new form PanelEditLicences */
	public PanelEditLicences(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {

		panelKeys = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceOptionsView"), 0, true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // with tablesearchpane
		);
		panelKeys.setMasterFrame(Globals.frame1);
		panelKeys.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelKeys.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panelKeys.setFiltering(true);

		panelSoftwarelicences = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSoftwarelicence"), 0, true, 2, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // with tablesearchpane
		);
		panelSoftwarelicences.setMasterFrame(Globals.frame1);
		panelSoftwarelicences.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		panelSoftwarelicences.setFiltering(true);
		panelSoftwarelicences.showFilterIcon(true);

		panelLicencecontracts = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencecontract"), 0, true, 2, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // with tablesearchpane
		);
		panelLicencecontracts.setMasterFrame(Globals.frame1);
		panelLicencecontracts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelLicencecontracts.showFilterIcon(true); // supply implementation of SearchTargetModelFromTable.setFiltered
		panelLicencecontracts.setFiltering(true);
		panelLicencecontracts.setAwareOfTableChangedListener(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.5f);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		javax.swing.GroupLayout layoutTopPane = new javax.swing.GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup().addGap(10, 10, 10).addGroup(layoutTopPane
				.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
				// for testing purposes:

				// Short.MAX_VALUE)
				.addComponent(panelKeys, javax.swing.GroupLayout.Alignment.LEADING,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelSoftwarelicences, javax.swing.GroupLayout.Alignment.LEADING,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(10, 10, 10));
		layoutTopPane.setVerticalGroup(layoutTopPane.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)

				.addComponent(panelKeys, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(panelSoftwarelicences, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		javax.swing.GroupLayout layoutBottomPane = new javax.swing.GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
				.addGroup(layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
						panelLicencecontracts, javax.swing.GroupLayout.Alignment.LEADING,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addGap(10, 10, 10));
		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(panelLicencecontracts, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin));

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

	}

}
