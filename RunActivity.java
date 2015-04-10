package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import ru.msu.cs.graphics.veqeclient.CodecsLauncher.ResultsStorage;
import ru.msu.cs.graphics.veqeclient.CodecsLauncher.UIUpdater;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;


public class RunActivity extends Activity implements UIUpdater, ResultsStorage {
	public static String launchResultsTag = "LaunchResults";
	
	private ProgressBar mbProg, msProg;
	private TextView mbStatus, msStatus;
	
	private ArrayList<LaunchResults> results = new ArrayList<LaunchResults>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);
		setupActionBar();	
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mbProg = (ProgressBar) findViewById(R.id.bigProgress);
		msProg = (ProgressBar) findViewById(R.id.smallProgress);
		mbStatus = (TextView) findViewById(R.id.bigStatus);
		msStatus = (TextView) findViewById(R.id.smallStatus);
		
		new CodecsLauncher(getIntent(), this, this);
	}
	
	@Override
	public void onBackPressed() {
		//TODO are you sure?
	}
	
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
		getActionBar().setCustomView(pb);
	}

	@Override
	public void addResults(LaunchResults res) {
		results.add(res);		
	}

	@Override
	public void noMoreResults() {
		Intent intent = new Intent(this, ResultsActivity.class);
		intent.putExtra(launchResultsTag, results);
		startActivity(intent);
	}

	@Override
	public void setStatus(final String msg) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mbStatus.setText(msg);
			}
		});		
	}
	
	@Override
	public void setDetailedStatus(final String msg) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				msStatus.setText(msg);	
			}
		});	
	}
	
	@Override
	public void setProg(final int percent) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mbProg.setProgress(percent);
			}
		});			
	}

	@Override
	public void setDetailedProg(final int percent) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				msProg.setProgress(percent);	
			}
		});		
	}

}
