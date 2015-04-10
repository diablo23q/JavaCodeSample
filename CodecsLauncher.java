package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayDeque;
import java.util.ArrayList;

import android.content.Intent;

public class CodecsLauncher {
	public interface UIUpdater {
		void setProg(int percent);
		void setStatus(String msg);
		void setDetailedProg(int percent);
		void setDetailedStatus(String msg);
	}
	
	public interface ResultsStorage {
		void addResults(LaunchResults res);
		void noMoreResults();
	}
	
	private boolean isCanceled = false;
	private int totalLaunches = 0;
	private ArrayDeque<CodecsRunner> queue = new ArrayDeque<CodecsRunner>();
	private ResultsStorage mRS;
	private UIUpdater mUI;
	
	//global parameters
	private int fps;
	private int iframe;
	private int launches;
	
	public CodecsLauncher(Intent params, ResultsStorage rs, UIUpdater ui) {
		mRS = rs;
		mUI = ui;
		
		fps = params.getIntExtra(ParametersActivity.fpsTag, 30);
		iframe = params.getIntExtra(ParametersActivity.iframeTag, 10);
		launches = params.getIntExtra(ParametersActivity.launchesTag, 10);
		
		boolean[] seq = params.getBooleanArrayExtra(ParametersActivity.seqTag); //TODO refactor later
		
		String[] encNames = params.getStringArrayExtra(CodecsActivity.codecNamesTag);
		String[] decNames = params.getStringArrayExtra(DecodersActivity.decNamesTag);
		ArrayList<String> rates = params.getStringArrayListExtra(BitrateActivity.bitratesTag);
		for(int i = 0; i < encNames.length; ++i) {
			for(int j = 0; j < rates.size(); ++j) {
				if(seq[0]) {
					queue.add(new CodecsRunner(this, Integer.valueOf(rates.get(j))*1000, encNames[i], decNames[i], 
							SequenceManager.bus, new MetricsCalculator.YPSNR()));	
					totalLaunches++;
				}
							
				if(seq[1]) {
					queue.add(new CodecsRunner(this, Integer.valueOf(rates.get(j))*1000, encNames[i], decNames[i], 
							SequenceManager.crew, new MetricsCalculator.YPSNR()));	
					totalLaunches++;
				}
				
				if(seq[2]) {
					queue.add(new CodecsRunner(this, Integer.valueOf(rates.get(j))*1000, encNames[i], decNames[i], 
							SequenceManager.soccer, new MetricsCalculator.YPSNR()));	
					totalLaunches++;
				}
			}
		}			
		runNextInQueueImpl();
	}
	
	public ResultsStorage getResStorage() {
		return mRS;
	}
	
	public UIUpdater getUIUpdater() {
		return mUI;
	}
	
	public int getFPS() {
		return fps;
	}
	
	public int getIframeInterval() {
		return iframe;
	}
	
	public int getLaunchNum() {
		return launches;
	}
	
	// Simulating friend class. See http://stackoverflow.com/a/18634125
	public void runNextInQueue(CodecsRunner.RunnerSignature s) {
		s.hashCode();
		runNextInQueueImpl();
	}
	
	public void cancel() {
		isCanceled = true;
	}
	
	private void runNextInQueueImpl() {
		CodecsRunner r = queue.poll();
		if((r != null) && !isCanceled) {
			mUI.setProg((totalLaunches - queue.size() - 1) * 100 / totalLaunches);
			mUI.setStatus("Running " + r.getRunDesc());
			r.run();
		} else {
			mRS.noMoreResults();
		}
	}
}
