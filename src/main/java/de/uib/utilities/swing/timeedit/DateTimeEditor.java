package de.uib.utilities.swing.timeedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.calendar.*;
import java.util.Calendar;
import java.text.DateFormat;
import java.sql.Timestamp;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;



public class DateTimeEditor extends JPanel
	implements  
		org.jdesktop.swingx.event.DateSelectionListener
		
	
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
	
	//protected Vector<DateEventObserver> dateEventObservers;
	
	
	public DateTimeEditor()
	{
		this(true);
	}
	
	public DateTimeEditor(boolean withTime)
	{
		super();
		this.withTime = withTime;
		//setBorder(new javax.swing.border.EtchedBorder());
		
		//dateEventObservers = new Vector<DateEventObserver>();
		
		JPopupMenu popup = new JPopupMenu();
		
		JMenuItem menuItemNow = new JMenuItem("Jetzt");
		menuItemNow.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				setDate();
			}
				
		});
		
		popup.add(menuItemNow);
		
		
		
		JMenuItem menuItemNull = new JMenuItem("Kein Datum");;
		menuItemNull.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//System.out.println( " action menu null ");
				monthView.getSelectionModel().clearSelection(); 
				monthView.commitSelection();
			}
				
		});
		
		popup.add(menuItemNull);
		
		addMouseListener(new utils.PopupMouseListener(popup));
		
		
		calendar = Calendar.getInstance();
		if (!withTime) 
			setToMidnight();
		hourDateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
		
		monthView = new JXMonthView();
		//monthView.setAntialiased(true); old jxswing version
		monthView.setSelectionMode(DateSelectionModel.SelectionMode.SINGLE_SELECTION);
	
		addDateSelectionListener(this); //observe monthview
		
		timeSetter = new TimeEditor(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
		if (!withTime)
			timeSetter.setVisible(false);
		
		setDate(false);
		
		monthView.addMouseListener(new utils.PopupMouseListener(popup));
		
		//monthSpinnerModel = new SpinnerDateModel(); //new Date(), null, null, Calendar.MONTH);
		//monthSpinnerModel.setCalendarField(Calendar.MONTH);
		//String[] monthStrings = getMonthStrings();
		
		monthSpinnerModel = new SpinnerListModel();
		monthSpinner = new JSpinner(monthSpinnerModel);
		monthSpinner.setPreferredSize(new Dimension(17,27));
		
		buttonBack = new JButton("<");
		buttonBack.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
		buttonBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				switchMonth(-1);
			}
		});
		buttonForward = new JButton(">");
		buttonForward.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
		buttonForward.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				switchMonth(+1);
			}
		});
		
		buttonYearBack = new JButton("<<");
		buttonYearBack.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
		buttonYearBack.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				switchYear(-1);
			}
		});
		buttonYearForward = new JButton(">>");
		buttonYearForward.setBorder(new javax.swing.border.EmptyBorder(1,1,1,1));
		buttonYearForward.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				switchYear(+1);
			}
		});
		
		
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
		
		
		
		layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            		.addGap(1, 1, Short.MAX_VALUE)
            		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
            			.addComponent(buttonBack, buttonW,buttonW,buttonW)
            			.addComponent(buttonYearBack, buttonW,buttonW,buttonW)
            		)
				//GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(timeSetter, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
            			.addComponent(buttonForward, buttonW,buttonW,buttonW)
            			.addComponent(buttonYearForward, buttonW,buttonW,buttonW)
				)
				.addGap(1, 1, Short.MAX_VALUE)
			)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                		.addGroup(layout.createSequentialGroup()
                			.addComponent(buttonBack, buttonH, buttonH, buttonH)
                			.addComponent(buttonYearBack, buttonH, buttonH, buttonH)
                		)
                    .addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                    		.addComponent(buttonForward, buttonH, buttonH, buttonH)
                    		.addComponent(buttonYearForward, buttonH, buttonH, buttonH)
                    	)
                    .addComponent(timeSetter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
        );
		
		
		/*
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    //.addComponent(monthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()(
						.addComponent(buttonBack, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(timeSetter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonForwared, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					)
                    .addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                //.addComponent(monthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(
					
				.addComponent(monthView, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(timeSetter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
        );
		
		*/
		
	}
	
	protected void setToMidnight()
	{
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	protected void switchMonth(int d)
	{
		//calendar.setTime(monthView.getFirstDisplayedDay());
		calendar.add(Calendar.MONTH, d);
		
		Date newDate = calendar.getTime();
		monthView.ensureDateVisible(newDate);
		
		if (withMovingSelectionDate)
		{
			setSelectionDate(newDate);
			//communicateDateEvent(new DateEvent(this, newDate));
		}
	}
	
	protected void switchYear(int d)
	{
		//calendar.setTime(monthView.getFirstDisplayedDay());
		calendar.add(Calendar.YEAR, d);
		
		Date newDate = calendar.getTime();
		monthView.ensureDateVisible(newDate);
		
		if (withMovingSelectionDate)
		{
			setSelectionDate(newDate);
			//communicateDateEvent(new DateEvent(this, newDate));
		}
	}

	public void setEditable(boolean b)
	{
		editable = b;
		//timeSetter.setEditable(b);
		//monthView.setEditable(b);
	}
	
	
	public void requestFocus()
	{
		logging.debug(this, "requestFocus");
		monthView.requestFocus();
	}

		
	
	public void setDate()
	{
		setDate(true);
	}
	
	public void setDate(boolean select)
	{
		now = new Date();
		calendar.setTime(now);
		
		if (!withTime)
		{
			setToMidnight();
		}
		
		
		monthView.setFirstDisplayedDay(now);
		if (select) 
			monthView.setSelectionDate(now);
		else
		{
			monthView.getSelectionModel().clearSelection(); 
			monthView.commitSelection();
		}
		logging.debug(this," ------- setDate,  hour  " + calendar.get(Calendar.HOUR_OF_DAY) + ", min " +  calendar.get(Calendar.MINUTE));
		timeSetter.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		timeSetter.setMin(calendar.get(Calendar.MINUTE));
		//communicateDateEvent(new DateEvent(this, now));
	}
	
	public void setHour(int h)
	{
		timeSetter.setHour(h);
	}
	
	public void setMin(int m)
	{
		timeSetter.setMin(m);
	}
	
	public void setSelectionDate(Date d)
	{
		logging.debug(this, " setSelectionDate " + d);
		if (d != null) monthView.ensureDateVisible(d);
		calendar.setTime(d);
		monthView.setSelectionDate(d);
		timeSetter.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		timeSetter.setMin(calendar.get(Calendar.MINUTE));
		//monthView.repaint();
	}
	
	public int getHour()
	{
		return timeSetter.getHour();
	}
	
	public int getMin()
	{
		return timeSetter.getHour();
	}
	
	public Date getSelectionDate()
	{
		return monthView.getFirstSelectionDate();
	}
	
	public java.sql.Timestamp getSelectedSqlTime()
	{
		if ( monthView.getFirstSelectionDate() == null )
			return null;
		
		calendar.setTime(monthView.getFirstSelectionDate());
		calendar.add( Calendar.HOUR, timeSetter.getHour() );
		calendar.add( Calendar.MINUTE, timeSetter.getMin() );
		return new java.sql.Timestamp(calendar.getTimeInMillis());
		
	}
	
	public void addDateSelectionListener(org.jdesktop.swingx.event.DateSelectionListener listener)
	{
		monthView.getSelectionModel().addDateSelectionListener(listener);
	}
	
	public void addActionListener(ActionListener listener)
	{
		monthView.addActionListener(listener);
	}
	
	public void addKeyListener(KeyListener listener)
	{
		monthView.addKeyListener(listener);
	}
	
	public void addMonthViewMouseListener(MouseListener listener)
	{
		monthView.addMouseListener(listener);
	}
	
	
	//DateSelectionListener
	public void valueChanged(org.jdesktop.swingx.event.DateSelectionEvent ev)
	{
		if (withMovingSelectionDate)
		{
			if (calendar.getTime().equals(monthView.getFirstSelectionDate()))
			{
				// avoid recursion
				//System.out.println(" new time equal old time ");
			}
			else 
			{
				if (monthView.getFirstSelectionDate() != null)
					calendar.setTime(monthView.getFirstSelectionDate());
			}
		}
	}
	
	
	/*
	public void registerDateEventObserver(DateEventObserver o)
	{
		dateEventObservers.add(o);
	}
	
	public void communicateDateEvent(DateEvent e)
	{
		
		if (dateEventObservers == null)
			return;
		
		for (int i = 0; i < dateEventObservers.size(); i++)
		{
			dateEventObservers.get(i).dateChanged(e);
		}
		
			
	}
	*/
	
	
	/**
     * DateFormatSymbols returns an extra, empty value at the
     * end of the array of months.  Remove it.
     */
    static protected String[] getMonthStrings() {
        String[] months = new java.text.DateFormatSymbols().getMonths();
        int lastIndex = months.length - 1;

        if (months[lastIndex] == null
           || months[lastIndex].length() <= 0) { //last item empty
            String[] monthStrings = new String[lastIndex];
            System.arraycopy(months, 0,
                             monthStrings, 0, lastIndex);
            return monthStrings;
        } else { //last item not empty
            return months;
        }
    }
	
	
}

