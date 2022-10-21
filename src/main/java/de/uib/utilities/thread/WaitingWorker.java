package de.uib.utilities.thread;


/**
 * WaitingWorker
 * Copyright:     Copyright (c) 2016
 * Organisation:  uib
 * @author Rupert Röder
 */

import de.uib.configed.*;
import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class WaitingWorker extends SwingWorker<Void, Long> 
{
	//
	// Main task. Executed in background thread.
	//
	private boolean ready = false;
	private boolean stopped  = false;
	protected final JLabel statusLabel;
	protected final JProgressBar progressBar;
	private final long startActionMillis;
	private boolean timeoutReached;  
	
	
	WaitingSleeper waitingSleeper;
	
	
	
	public WaitingWorker(WaitingSleeper waitingSleeper)
	{
		this.waitingSleeper = waitingSleeper;
		this.progressBar = waitingSleeper.getProgressBar();
		this.statusLabel = waitingSleeper.getLabel();
		startActionMillis =  waitingSleeper.getStartActionMillis();
		timeoutReached = false;
		
	}
		
	
	public void setReady()
	{
		ready = true;
	}
	
	public boolean isReady()
	{
		return ready;
	}
	
	
	public void stop()
	{
		logging.info(this, "stop");
		stopped = true;
		cancel(true);
	}
		
	
	@Override
	public Void doInBackground() {
		
		//startAnotherProcess()
		//int progress = 0;
		//setProgress( progress );
		
		//int noOfSteps = 100;
		//long timeStepMillis = (long) (waitingMillis / noOfSteps );
		
		long timeStepMillis = (long) 500;
		
		//long noOfSteps =  (long) (waitingMillis/ timeStepMillis);
		
		logging.debug(this, " doInBackground waitingMillis " + waitingSleeper.getWaitingMillis() );   
		 
		
		long elapsedMillis = 0;
		long elapsedMins = 0;
		
		
		//while (progress < 100   && !stopped)
		timeoutReached = (elapsedMillis >= waitingSleeper.getWaitingMillis()); 
		while ( !ready && !timeoutReached  && !stopped )
		{
			try {
				Thread.sleep( timeStepMillis );
			} 
			catch (InterruptedException ignore) 
			{
				logging.info(this, "InterruptedException");
			}
			
			long nowMillis = new GregorianCalendar().getTimeInMillis();
			//elapsedMillis =  timeStepMillis * progress;;
				
			elapsedMillis = nowMillis - startActionMillis;
			elapsedMins = (elapsedMillis / 1000) / 60;
			
			logging.debug(this, " doInBackground progress  elapsedMillis " + elapsedMillis);
			logging.debug(this, " doInBackground progress totalTimeElapsed  [min] " + elapsedMins );  
			
			publish(elapsedMillis);
			
			timeoutReached = (elapsedMillis >= waitingSleeper.getWaitingMillis());
			
			//firePropertyChange("elapsedMins", 0, elapsedMins);
			
			
			//progress++;
			//setProgress( progress );
			
			//setElapsedMins(elapsedMins);
		}
		
		
		logging.info(this, " doInBackground finished: ready, stopped, elapsedMillis < waitingSleeper.getWaitingMillis() "
			+ ready + ", " +  stopped + ", " + (elapsedMillis >= waitingSleeper.getWaitingMillis()));
		
		if (timeoutReached)
			logging.warning(this, " doInBackground finished, timeoutReached");
		
		return null;
	}
		
	
	//
	// Executed in event dispatching thread
	//
	@Override
	protected void process( java.util.List<Long> listOfMillis )
	{
		//update the steps which are done
		logging.debug(this, "process, we have got list " + listOfMillis);
		
		long millis =  listOfMillis.get( listOfMillis.size() - 1);
		//logging.info(this, "process :: millis " + millis);
		statusLabel.setText(
			//"passed " + giveTimeSpan( millis)  +  
			waitingSleeper.setLabellingStrategy(millis));
			//" " + configed .getResourceValue("FStartWakeOnLan.timeLeft") + "  " + de.uib.utilities.Globals.giveTimeSpan( waitingSleeper.getWaitingMillis() - millis ) );
		
		 int barLength = progressBar.getMaximum() - progressBar.getMinimum();
		 
		 //logging.info(this, "progressBar.getMaximum() " + progressBar.getMaximum() + ":: progressBar.getMinimum() " + progressBar.getMinimum()
		 //	 + ":: millis " + millis + " :: waitingMillis " + waitingMillis + " :: min + " + ((int) ((barLength * millis) / waitingMillis)));
			 
		 logging.debug(this, "process, millis " +millis);
		 double proportion = ((double) millis) / (double)  waitingSleeper.getOneProgressBarLengthWaitingMillis();
		 logging.info(this, "process, millis/estimatedTotalWaitMillis  " + proportion) ;
		 //double portion = (barLength * millis) / waitingSleeper.getWaitingMillis();
		 int portion = (int) (barLength * proportion );
		 portion = portion % barLength;
		 
		 logging.debug(this, "portion "  + portion + " barLength  " + barLength);

		 progressBar.setValue( progressBar.getMinimum() + portion  );		 
		 
		 
		 //progressBar.setValue( ( int ) (progressBar.getMinimum() + (int) ( (barLength * millis) / waitingSleeper.getWaitingMillis() )) ) ;  
		
	}

	//
	// Executed in event dispatching thread
	//
	@Override
	public void done() {
		logging.info(this, "done,  stopped is " + stopped );
		if (!stopped) waitingSleeper.actAfterWaiting();
	}
	
	public boolean isTimeoutReached()
	{
		return timeoutReached;
	}
}
