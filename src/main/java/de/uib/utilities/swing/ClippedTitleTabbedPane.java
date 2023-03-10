// from http://terai.xrea.jp/Swing/ClippedTabLabel.html
// thanks to Terai Atsuhiro !
// a little bit extended by uib, 2008 

package de.uib.utilities.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public class ClippedTitleTabbedPane extends JTabbedPane {

	private double proportionOfTotalWidth = 1;

	private int calcWidth() {
		return (int) (getWidth() * proportionOfTotalWidth);
	}

	public void setProportionOfTotalWidth(double prop) {
		proportionOfTotalWidth = prop;
	}

	public ClippedTitleTabbedPane() {
		super();
		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				initTabWidth();
			}
		});

		super.addChangeListener(changeEvent -> initTabWidth());
	}

	private Insets getTabInsets() {
		Insets i = UIManager.getInsets("TabbedPane.tabInsets");
		if (i != null) {
			return i;
		} else {
			SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
			SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
			return style.getInsets(context, null);
		}
	}

	private Insets getTabAreaInsets() {
		Insets i = UIManager.getInsets("TabbedPane.tabAreaInsets");
		if (i != null) {
			return i;
		} else {
			SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
			SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
			return style.getInsets(context, null);
		}
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip == null ? title : tip, index);
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		Dimension dim = label.getPreferredSize();
		Insets tabInsets = getTabInsets();
		label.setPreferredSize(new Dimension(0, dim.height + tabInsets.top + tabInsets.bottom));
		setTabComponentAt(index, label);
		initTabWidth();
	}

	public void initTabWidth() {
		Insets tabInsets = getTabInsets();
		Insets tabAreaInsets = getTabAreaInsets();
		Insets insets = getInsets();
		int areaWidth = calcWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
		int tabCount = getTabCount();
		int tabWidth = 0;
		int gap = 0;
		switch (getTabPlacement()) {
		case LEFT:
		case RIGHT:
			tabWidth = areaWidth / 4;
			gap = 0;
			break;
		case BOTTOM:
		case TOP:
		default:
			tabWidth = areaWidth / tabCount;
			gap = areaWidth - (tabWidth * tabCount);
		}

		tabWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
		for (int i = 0; i < tabCount; i++) {
			JLabel l = (JLabel) getTabComponentAt(i);
			if (l == null)
				break;
			l.setPreferredSize(new Dimension(tabWidth + (i < gap ? 1 : 0), l.getPreferredSize().height));
		}
		revalidate();
	}
}
