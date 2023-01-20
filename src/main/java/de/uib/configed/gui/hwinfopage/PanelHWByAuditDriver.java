package de.uib.configed.gui.hwinfopage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.JTextShowField;

public class PanelHWByAuditDriver extends JPanel {
	protected JLabel jLabelTitle;

	protected int hGap = Globals.HGAP_SIZE / 2;
	protected int vGap = Globals.VGAP_SIZE / 2;
	protected int hLabel = Globals.BUTTON_HEIGHT;

	protected String byAuditPath;

	ButtonGroup selectionGroup;
	JRadioButton selectionCOMPUTER_SYSTEN;
	JRadioButton selectionBASE_BOARD;

	protected JTextField fieldVendor;
	protected JTextField fieldLabel;

	protected JTextField fieldVendor2;
	protected JTextField fieldLabel2;

	protected String title;

	JButton buttonUploadDrivers;

	FDriverUpload fDriverUpload;
	ConfigedMain main;

	public PanelHWByAuditDriver(String title, ConfigedMain main) {
		this.title = title;
		this.main = main;
		buildPanel();
	}

	protected void buildPanel() {
		jLabelTitle = new JLabel(title);
		jLabelTitle.setOpaque(true);

		fieldVendor = new JTextShowField();
		fieldVendor.setBackground(Globals.BACKGROUND_COLOR_3);
		fieldLabel = new JTextShowField();
		fieldLabel.setBackground(Globals.BACKGROUND_COLOR_3);

		fieldVendor2 = new JTextShowField();
		fieldVendor2.setBackground(Globals.BACKGROUND_COLOR_3);

		fieldLabel2 = new JTextShowField();
		fieldLabel2.setBackground(Globals.BACKGROUND_COLOR_3);

		JLabel labelInfo = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels"));

		JLabel labelSeparator = new JLabel(" / ");
		JLabel labelSeparator2 = new JLabel(" / ");
		JLabel labelVendor = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsVendor"));
		JLabel labelProduct = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProduct"));

		buttonUploadDrivers = new JButton("", Globals.createImageIcon("images/upload2product.png", ""));
		buttonUploadDrivers.setSelectedIcon(Globals.createImageIcon("images/upload2product.png", ""));
		buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelHWInfo.uploadDrivers"));

		buttonUploadDrivers.addActionListener(actionEvent -> startDriverUploadFrame());

		selectionCOMPUTER_SYSTEN = new JRadioButton("", true);
		selectionBASE_BOARD = new JRadioButton("");
		selectionGroup = new ButtonGroup();
		selectionGroup.add(selectionCOMPUTER_SYSTEN);
		selectionGroup.add(selectionBASE_BOARD);

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = Globals.LINE_HEIGHT - 4;
		layoutByAuditInfo.setVerticalGroup(layoutByAuditInfo.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup().addComponent(labelInfo, lh, lh, lh)
						.addComponent(labelVendor, lh, lh, lh).addComponent(labelProduct, lh, lh, lh)
						.addGap(hGap, hGap, hGap).addComponent(buttonUploadDrivers, lh, lh, lh))
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup()
								.addGap(hGap / 2 + 1, hGap / 2 + 1, hGap / 2 + 1)
								.addComponent(selectionCOMPUTER_SYSTEN))
						.addComponent(fieldVendor, lh, lh, lh).addComponent(labelSeparator, lh, lh, lh)
						.addComponent(fieldLabel, lh, lh, lh)

				).addGap(vGap / 2, vGap / 2, vGap / 2).addGroup(layoutByAuditInfo.createParallelGroup()

						.addGroup(layoutByAuditInfo.createSequentialGroup()
								.addGap(hGap / 2 + 1, hGap / 2 + 1, hGap / 2 + 1).addComponent(selectionBASE_BOARD))
						.addComponent(fieldVendor2, lh, lh, lh).addComponent(labelSeparator2, lh, lh, lh)
						.addComponent(fieldLabel2, lh, lh, lh)

				).addGap(vGap, vGap, vGap));

		layoutByAuditInfo.setHorizontalGroup(layoutByAuditInfo.createSequentialGroup()

				.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(hGap * 2, hGap * 2, hGap * 2)
						.addComponent(labelInfo, 5, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(2, hGap * 4, hGap * 4)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(selectionCOMPUTER_SYSTEN, 2, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(selectionBASE_BOARD, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(2, 2, 2).addComponent(labelVendor,
								Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2))
						.addComponent(fieldVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldVendor2, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo
						.createParallelGroup().addComponent(labelSeparator).addComponent(labelSeparator2))
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(2, 2, 2).addComponent(labelProduct,
								Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2))
						.addComponent(fieldLabel, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldLabel2, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))

				.addGap(5 * hGap, 10 * hGap, 10 * hGap).addComponent(buttonUploadDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
						Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
				.addGap(2 * hGap, 4 * hGap, Short.MAX_VALUE));
		setBackground(Globals.BACKGROUND_COLOR_7);
		setBorder(BorderFactory.createLineBorder(Globals.greyed));
	}

	public void setTitle(String s) {
		title = s;
	}

	public void emptyByAuditStrings() {
		byAuditPath = "";
		fieldVendor.setText("");
		fieldLabel.setText("");
		fieldVendor2.setText("");
		fieldLabel2.setText("");

		if (fDriverUpload != null)
			fDriverUpload.setUploadParameters("");
	}

	private String eliminateIllegalPathChars(String path) {
		final String toReplace = "<>?\":|\\/*";
		final char replacement = '_';

		if (path == null)
			return null;

		char[] chars = path.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (toReplace.indexOf(chars[i]) > -1)
				chars[i] = replacement;
		}

		// requires bootimage >= 4.0.6
		if (chars.length > 0 && (chars[chars.length - 1] == '.' || chars[chars.length - 1] == ' ')) {
			chars[chars.length - 1] = replacement;
		}

		return new String(chars);
	}

	public void setByAuditFields(String vendorStringComputerSystem, String vendorStringBaseBoard, String modelString,
			String productString) {
		fieldVendor.setText(vendorStringComputerSystem);
		fieldLabel.setText(modelString);

		fieldVendor2.setText(vendorStringBaseBoard);
		fieldLabel2.setText(productString);

		if (fDriverUpload != null)
			fDriverUpload.setUploadParameters(byAuditPath);

	}

	private void startDriverUploadFrame() {
		if (selectionBASE_BOARD.isSelected()) {
			byAuditPath = eliminateIllegalPathChars(fieldVendor2.getText()) + "/"
					+ eliminateIllegalPathChars(fieldLabel2.getText());
		} else {
			byAuditPath = eliminateIllegalPathChars(fieldVendor.getText()) + "/"
					+ eliminateIllegalPathChars(fieldLabel.getText());
		}

		if (fDriverUpload == null) {
			fDriverUpload = new FDriverUpload(main, main.getPersistenceController(), null);
		}

		fDriverUpload.setSize(Globals.helperFormDimension);
		fDriverUpload.setVisible(true);
		fDriverUpload.centerOnParent();

		fDriverUpload.setUploadParameters(byAuditPath);
	}
}