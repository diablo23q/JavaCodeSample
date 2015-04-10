package ru.msu.cs.graphics.veqeclient;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.support.v4.app.NavUtils;

public class DecodersActivity extends ListActivity {
	
	public static String decNamesTag = "DecoderNames";
	private DecodersListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		mAdapter = new DecodersListAdapter(this, getIntent().getStringArrayExtra(CodecsActivity.codecNamesTag));
		
		setListAdapter(mAdapter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.decoders, menu);
		menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(DecodersActivity.this, BitrateActivity.class);
				intent.putExtra(DecodersActivity.decNamesTag, mAdapter.getSelectedNames());
				intent.putExtra(CodecsActivity.codecNamesTag, 
						getIntent().getStringArrayExtra(CodecsActivity.codecNamesTag));
				DecodersActivity.this.startActivity(intent);
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
