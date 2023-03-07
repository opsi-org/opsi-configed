package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.HealthInfo;
import de.uib.utilities.logging.Logging;

public class HealthCheckDialog extends FGeneralDialog {
	private final StyleContext styleContext = StyleContext.getDefaultStyleContext();
	private final AttributeSet blackAttributeSet = styleContext.addAttribute(styleContext.getEmptySet(),
			StyleConstants.Foreground, Color.BLACK);

	private JTextPane textPane = new JTextPane();
	private DefaultStyledDocument styledDocument = new DefaultStyledDocument();

	public HealthCheckDialog() {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("HealthCheckDialog.title"), true,
				new String[] { Configed.getResourceValue("FGeneralDialog.ok") },
				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", "") }, 1, 700, 500, true,
				null);
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "start allLayout");

		allpane.setBackground(Globals.BACKGROUND_COLOR_7);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		northPanel = createNorthPanel();
		centerPanel = createCenterPanel();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(northPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE)
				.addComponent(centerPanel).addGap(Globals.HGAP_SIZE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(northPanel, 100, 300, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)));
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setOpaque(false);

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		styledDocument.setDocumentFilter(new CustomDocumentFilter());
		textPane.setStyledDocument(styledDocument);

		textPane.setAutoscrolls(false);
		textPane.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBackground(Globals.F_GENERAL_DIALOG_BACKGROUND_COLOR);
		scrollPane.setOpaque(false);

		northLayout.setHorizontalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));
		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addComponent(scrollPane));

		return northPanel;
	}

	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setOpaque(false);

		GroupLayout centerPanelLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerPanelLayout);

		JCheckBox jCheckBoxShowDetailedInformation = new JCheckBox(
				Configed.getResourceValue("HealthCheckDialog.showDetailedInformation"));
		JButton jButtonCopyHealthInformation = new JButton(
				Configed.getResourceValue("HealthCheckDialog.copyHealthInformation"));

		centerPanelLayout.setHorizontalGroup(centerPanelLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(jCheckBoxShowDetailedInformation, 10, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(
						jButtonCopyHealthInformation, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		centerPanelLayout.setVerticalGroup(centerPanelLayout.createSequentialGroup()
				.addGap(0, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(centerPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jCheckBoxShowDetailedInformation, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCopyHealthInformation, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.HGAP_SIZE));

		jCheckBoxShowDetailedInformation.addActionListener(
				event -> setMessage(HealthInfo.getHealthData(jCheckBoxShowDetailedInformation.isSelected())));
		jButtonCopyHealthInformation.addActionListener(event -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(textPane.getText()), null));

		return centerPanel;
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		return southPanel;
	}

	public void setMessage(String message) {
		try {
			styledDocument.remove(0, styledDocument.getLength());
			styledDocument.insertString(styledDocument.getLength(), message, blackAttributeSet);
		} catch (BadLocationException e) {
			Logging.warning("could not insert message into health check dialog, ", e);
		}
		textPane.setCaretPosition(0);
	}

	private class CustomDocumentFilter extends DocumentFilter {
		@Override
		public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet)
				throws BadLocationException {
			super.insertString(fb, offset, text, attributeSet);

			handleTextChanged();
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);

			handleTextChanged();
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet)
				throws BadLocationException {
			super.replace(fb, offset, length, text, attributeSet);

			handleTextChanged();
		}

		private void handleTextChanged() {
			SwingUtilities.invokeLater(this::updateTextStyles);
		}

		private void updateTextStyles() {
			Pattern pattern = Pattern.compile("OK|WARNING|ERROR");
			Matcher matcher = pattern.matcher(textPane.getText());
			while (matcher.find()) {
				Style style = getStyle(matcher.group());
				styledDocument.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
			}
		}

		private Style getStyle(String token) {
			Style style = null;

			switch (token) {
			case "OK":
				style = styleContext.addStyle("ok", null);
				StyleConstants.setForeground(style, Color.GREEN);
				break;
			case "WARNING":
				style = styleContext.addStyle("warning", null);
				StyleConstants.setForeground(style, Globals.logColorWarning);
				break;
			case "ERROR":
				style = styleContext.addStyle("error", null);
				StyleConstants.setForeground(style, Globals.logColorError);
				break;
			default:
				Logging.warning(this, "unsupported token: " + token);
			}

			return style;
		}
	}
}
