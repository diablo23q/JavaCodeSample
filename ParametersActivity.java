package ru.msu.cs.graphics.veqeclient;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class ParametersActivity extends Activity {
	public static final String fpsTag = "FPS", iframeTag = "I-frameInterval", 
			launchesTag = "LaunchNum", seqTag = "Sequnces3";
	
	private EditText fps = null, iframe = null, launches = null;
	private CheckBox cBus = null, cCrew = null, cSoccer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parameters);
		setupActionBar();
		
		fps = (EditText) findViewById(R.id.fpsEdit);
		iframe = (EditText) findViewById(R.id.iframeEdit);
		launches = (EditText) findViewById(R.id.lnumEdit);
		
		cBus = (CheckBox) findViewById(R.id.cBus);
		cCrew = (CheckBox) findViewById(R.id.cCrew);
		cSoccer = (CheckBox) findViewById(R.id.cSoccer);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.parameters, menu);
		menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(ParametersActivity.this, RunActivity.class);
				Intent recvd = ParametersActivity.this.getIntent();
				
				intent.putExtra(CodecsActivity.codecNamesTag, recvd.getStringArrayExtra(CodecsActivity.codecNamesTag));
				intent.putExtra(DecodersActivity.decNamesTag, recvd.getStringArrayExtra(DecodersActivity.decNamesTag));
				intent.putStringArrayListExtra(BitrateActivity.bitratesTag, 
						recvd.getStringArrayListExtra(BitrateActivity.bitratesTag));
				intent.putExtra(fpsTag, Integer.valueOf(fps.getText().toString()));
				intent.putExtra(iframeTag, Integer.valueOf(iframe.getText().toString()));
				intent.putExtra(launchesTag, Integer.valueOf(launches.getText().toString()));
				
				boolean[] sel = new boolean[3]; //TODO refactor later
				sel[0] = cBus.isChecked();
				sel[1] = cCrew.isChecked();
				sel[2] = cSoccer.isChecked();
				intent.putExtra(seqTag, sel);
				
				ParametersActivity.this.startActivity(intent);
				return true;
			}
		});
		return true;
	}

	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = NavUtils.getParentActivityIntent(this); 
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
