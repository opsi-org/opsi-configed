package de.uib.utilities.swing.timeedit;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.DateSelectionModel;

import de.uib.utilities.logging.logging;

public class DateTimeEditor extends JPanel implements org.jdesktop.swingx.event.DateSelectionListener

{
	protected JXMonthView monthView;
	protected TimeEditor timeSetter;
	protected Calendar calendar;
	protected java.text.DateFormat hourDateFormat;
	protected javax.swing.JSpinner monthSpinner;
	protected SpinnerModel monthSpinnerModel;
	protected JButton buttonBack;
	protected JButton buttonForward;
	protected JButton buttonYearBack;
	protected JButton buttonYearForward;
	protected final int buttonH = 25;
	protected final int buttonW = 30;
	private Date now;
	protected boolean editable = true;
	protected boolean withMovingSelectionDate = true;
	protected boolean withTime = true;

	public DateTimeEditor() {
		this(true);
	}

	public DateTimeEditor(boolean withTime) {
		super();
		this.withTime = withTime;

		JPopupMenu popup = new JPopupMenu();

		JMenuItem menuItemNow = new JMenuItem("Jetzt");
		menuItemNow.addActionListener(actionEvent -> setDate());

		popup.add(menuItemNow);

		JMenuItem menuItemNull = new JMenuItem("Kein Datum");;
		menuItemNull.addActionListener(actionEvent -> {

			monthView.getSelectionModel().clearSelection();
			monthView.commitSelection();
		});

		popup.add(menuItemNull);

		addMouseListener(new utils.PopupMouseListener(popup));

		calendar = Calendar.getInstance();
		if (!withTime)
			setToMidnight();
		hourDateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");

		monthView = new JXMonthView();

		monthView.setSelectionMode(DateSelectionModel.SelectionMode.SINGLE_SELECTION);

		addDateSelectionListener(this); // observe monthview

		timeSetter = new TimeEditor(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
		if (!withTime)
			timeSetter.setVisible(false);

		setDate(false);

		monthView.addMouseListener(new utils.PopupMouseListener(popup));

		monthSpinnerModel = new SpinnerListModel();
		monthSpinner = new JSpinner(monthSpinnerModel);
		monthSpinner.setPreferredSize(new Dimension(17, 27));

		buttonBack = new JButton("<");
		buttonBack.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
		buttonBack.addActionListener(actionEvent -> switchMonth(-1));
		buttonForward = new JButton(">");
		buttonForward.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
		buttonForward.addActionListener(actionEvent -> switchMonth(+1));

		buttonYearBack = new JButton("<<");
		buttonYearBack.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
		buttonYearBack.addActionListener(actionEvent -> switchYear(-1));
		buttonYearForward = new JButton(">>");
		buttonYearForward.setBorder(new javax.swing.border.EmptyBorder(1, 1, 1, 1));
		buttonYearForward.addActionListener(actionEvent -> switchYear(+1));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(1, 1, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(buttonBack, buttonW, buttonW, buttonW)
								.addComponent(buttonYearBack, buttonW, buttonW, buttonW))
						
						
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(timeSetter, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
								.addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(buttonForward, buttonW, buttonW, buttonW)
								.addComponent(buttonYearForward, buttonW, buttonW, buttonW))
						.addGap(1, 1, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addComponent(buttonBack, buttonH, buttonH, buttonH)
								.addComponent(buttonYearBack, buttonH, buttonH, buttonH))
						.addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createSequentialGroup().addComponent(buttonForward, buttonH, buttonH, buttonH)
								.addComponent(buttonYearForward, buttonH, buttonH, buttonH))
						.addComponent(timeSetter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))));

	}

	protected void setToMidnight() {

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}

	protected void switchMonth(int d) {

		calendar.add(Calendar.MONTH, d);

		Date newDate = calendar.getTime();
		monthView.ensureDateVisible(newDate);

		if (withMovingSelectionDate) {
			setSelectionDate(newDate);

		}
	}

	protected void switchYear(int d) {

		calendar.add(Calendar.YEAR, d);

		Date newDate = calendar.getTime();
		monthView.ensureDateVisible(newDate);

		if (withMovingSelectionDate) {
			setSelectionDate(newDate);

		}
	}

	public void setEditable(boolean b) {
		editable = b;

	}

	@Override
	public void requestFocus() {
		logging.debug(this, "requestFocus");
		monthView.requestFocus();
	}

	public void setDate() {
		setDate(true);
	}

	public void setDate(boolean select) {
		now = new Date();
		calendar.setTime(now);

		if (!withTime) {
			setToMidnight();
		}

		monthView.setFirstDisplayedDay(now);
		if (select)
			monthView.setSelectionDate(now);
		else {
			monthView.getSelectionModel().clearSelection();
			monthView.commitSelection();
		}
		logging.debug(this, " ------- setDate,  hour  " + calendar.get(Calendar.HOUR_OF_DAY) + ", min "
				+ calendar.get(Calendar.MINUTE));
		timeSetter.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		timeSetter.setMin(calendar.get(Calendar.MINUTE));

	}

	public void setHour(int h) {
		timeSetter.setHour(h);
	}

	public void setMin(int m) {
		timeSetter.setMin(m);
	}

	public void setSelectionDate(Date d) {
		logging.debug(this, " setSelectionDate " + d);
		if (d != null)
			monthView.ensureDateVisible(d);
		calendar.setTime(d);
		monthView.setSelectionDate(d);
		timeSetter.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		timeSetter.setMin(calendar.get(Calendar.MINUTE));

	}

	public int getHour() {
		return timeSetter.getHour();
	}

	public int getMin() {
		return timeSetter.getHour();
	}

	public Date getSelectionDate() {
		return monthView.getFirstSelectionDate();
	}

	public java.sql.Timestamp getSelectedSqlTime() {
		if (monthView.getFirstSelectionDate() == null)
			return null;

		calendar.setTime(monthView.getFirstSelectionDate());
		calendar.add(Calendar.HOUR, timeSetter.getHour());
		calendar.add(Calendar.MINUTE, timeSetter.getMin());
		return new java.sql.Timestamp(calendar.getTimeInMillis());

	}

	public void addDateSelectionListener(org.jdesktop.swingx.event.DateSelectionListener listener) {
		monthView.getSelectionModel().addDateSelectionListener(listener);
	}

	public void addActionListener(ActionListener listener) {
		monthView.addActionListener(listener);
	}

	@Override
	public void addKeyListener(KeyListener listener) {
		monthView.addKeyListener(listener);
	}

	public void addMonthViewMouseListener(MouseListener listener) {
		monthView.addMouseListener(listener);
	}

	// DateSelectionListener
	@Override
	public void valueChanged(org.jdesktop.swingx.event.DateSelectionEvent ev) {
		if (withMovingSelectionDate) {
			if (calendar.getTime().equals(monthView.getFirstSelectionDate())) {
				// avoid recursion

			} else {
				if (monthView.getFirstSelectionDate() != null)
					calendar.setTime(monthView.getFirstSelectionDate());
			}
		}
	}

	/**
	 * DateFormatSymbols returns an extra, empty value at the end of the array
	 * of months. Remove it.
	 */
	protected static String[] getMonthStrings() {
		String[] months = new java.text.DateFormatSymbols().getMonths();
		int lastIndex = months.length - 1;

		if (months[lastIndex] == null || months[lastIndex].length() <= 0) { // last item empty
			String[] monthStrings = new String[lastIndex];
			System.arraycopy(months, 0, monthStrings, 0, lastIndex);
			return monthStrings;
		} else { // last item not empty
			return months;
		}
	}

}
