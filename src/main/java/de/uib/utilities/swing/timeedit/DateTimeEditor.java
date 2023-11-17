/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing.timeedit;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.DateSelectionModel;
import org.jdesktop.swingx.event.DateSelectionEvent;
import org.jdesktop.swingx.event.DateSelectionListener;

import de.uib.utilities.logging.Logging;
import utils.PopupMouseListener;

public class DateTimeEditor extends JPanel implements DateSelectionListener {
	private static final int BUTTON_H = 25;
	private static final int BUTTON_W = 30;

	private JXMonthView monthView;
	private TimeEditor timeSetter;
	private LocalDateTime calendar;
	private boolean withMovingSelectionDate = true;
	private boolean withTime = true;

	public DateTimeEditor(boolean withTime) {
		super();
		this.withTime = withTime;

		init();
	}

	private void init() {
		JPopupMenu popup = new JPopupMenu();

		JMenuItem menuItemNow = new JMenuItem("Jetzt");
		menuItemNow.addActionListener(actionEvent -> setDate(true));

		popup.add(menuItemNow);

		JMenuItem menuItemNull = new JMenuItem("Kein Datum");
		menuItemNull.addActionListener((ActionEvent actionEvent) -> {
			monthView.getSelectionModel().clearSelection();
			monthView.commitSelection();
		});

		popup.add(menuItemNull);

		super.addMouseListener(new PopupMouseListener(popup));

		calendar = LocalDateTime.now();
		if (!withTime) {
			setToMidnight();
		}

		monthView = new JXMonthView();

		monthView.setSelectionMode(DateSelectionModel.SelectionMode.SINGLE_SELECTION);

		// observe monthview
		addDateSelectionListener(this);

		timeSetter = new TimeEditor(calendar.getHour(), calendar.getMinute());
		if (!withTime) {
			timeSetter.setVisible(false);
		}

		setDate(false);

		monthView.addMouseListener(new PopupMouseListener(popup));

		SpinnerModel monthSpinnerModel = new SpinnerListModel();
		JSpinner monthSpinner = new JSpinner(monthSpinnerModel);
		monthSpinner.setPreferredSize(new Dimension(17, 27));

		JButton buttonBack = new JButton("<");
		buttonBack.setBorder(new EmptyBorder(1, 1, 1, 1));
		buttonBack.addActionListener(actionEvent -> switchMonth(-1));
		JButton buttonForward = new JButton(">");
		buttonForward.setBorder(new EmptyBorder(1, 1, 1, 1));
		buttonForward.addActionListener(actionEvent -> switchMonth(+1));

		JButton buttonYearBack = new JButton("<<");
		buttonYearBack.setBorder(new EmptyBorder(1, 1, 1, 1));
		buttonYearBack.addActionListener(actionEvent -> switchYear(-1));
		JButton buttonYearForward = new JButton(">>");
		buttonYearForward.setBorder(new EmptyBorder(1, 1, 1, 1));
		buttonYearForward.addActionListener(actionEvent -> switchYear(+1));

		GroupLayout layout = new GroupLayout(this);
		super.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(1, 1, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(buttonBack, BUTTON_W, BUTTON_W, BUTTON_W)
						.addComponent(buttonYearBack, BUTTON_W, BUTTON_W, BUTTON_W))

				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(timeSetter, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
						.addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(buttonForward, BUTTON_W, BUTTON_W, BUTTON_W)
						.addComponent(buttonYearForward, BUTTON_W, BUTTON_W, BUTTON_W))
				.addGap(1, 1, Short.MAX_VALUE));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addComponent(buttonBack, BUTTON_H, BUTTON_H, BUTTON_H)
						.addComponent(buttonYearBack, BUTTON_H, BUTTON_H, BUTTON_H))
				.addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup().addComponent(buttonForward, BUTTON_H, BUTTON_H, BUTTON_H)
						.addComponent(buttonYearForward, BUTTON_H, BUTTON_H, BUTTON_H))
				.addComponent(timeSetter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	private void setToMidnight() {
		calendar = calendar.withHour(0).withMinute(0).withSecond(0);
	}

	private void switchMonth(int d) {
		calendar = calendar.plusMonths(d);

		Date newDate = Timestamp.valueOf(calendar);
		monthView.ensureDateVisible(newDate);

		if (withMovingSelectionDate) {
			setSelectionDate(newDate);
		}
	}

	private void switchYear(int d) {
		calendar = calendar.plusYears(d);

		Date newDate = Timestamp.valueOf(calendar);
		monthView.ensureDateVisible(newDate);

		if (withMovingSelectionDate) {
			setSelectionDate(newDate);
		}
	}

	@Override
	public void requestFocus() {
		Logging.debug(this, "requestFocus");
		monthView.requestFocus();
	}

	public void setDate(boolean select) {
		calendar = LocalDateTime.now();

		if (!withTime) {
			setToMidnight();
		}

		monthView.setFirstDisplayedDay(Timestamp.valueOf(calendar));
		if (select) {
			monthView.setSelectionDate(Timestamp.valueOf(calendar));
		} else {
			monthView.getSelectionModel().clearSelection();
			monthView.commitSelection();
		}
		Logging.debug(this, " ------- setDate,  hour  " + calendar.getHour() + ", min " + calendar.getMinute());
		timeSetter.setHour(calendar.getHour());
		timeSetter.setMin(calendar.getMinute());
	}

	public void setSelectionDate(Date d) {
		Logging.debug(this, " setSelectionDate " + d);
		if (d != null) {
			monthView.ensureDateVisible(d);

			calendar = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			monthView.setSelectionDate(d);
			timeSetter.setHour(calendar.getHour());
			timeSetter.setMin(calendar.getMinute());
		}
	}

	public Timestamp getSelectedSqlTime() {
		if (monthView.getFirstSelectionDate() == null) {
			return null;
		}

		calendar = monthView.getFirstSelectionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		calendar = calendar.plusHours(timeSetter.getHour());
		calendar = calendar.plusMinutes(timeSetter.getMin());
		return Timestamp.valueOf(calendar);
	}

	public void addDateSelectionListener(DateSelectionListener listener) {
		monthView.getSelectionModel().addDateSelectionListener(listener);
	}

	@Override
	public synchronized void addKeyListener(KeyListener listener) {
		monthView.addKeyListener(listener);
	}

	public void addMonthViewMouseListener(MouseListener listener) {
		monthView.addMouseListener(listener);
	}

	// DateSelectionListener
	@Override
	public void valueChanged(DateSelectionEvent ev) {
		if (withMovingSelectionDate) {
			if (Timestamp.valueOf(calendar).equals(monthView.getFirstSelectionDate())) {
				// avoid recursion
			} else {
				if (monthView.getFirstSelectionDate() != null) {
					calendar = monthView.getFirstSelectionDate().toInstant().atZone(ZoneId.systemDefault())
							.toLocalDateTime();
				}
			}
		}
	}
}
