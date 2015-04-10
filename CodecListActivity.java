package ru.msu.cs.graphics.veqeclient;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class CodecListActivity extends ListActivity {
	
	private CodecsListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		
		mAdapter = new CodecsListAdapter(this, true);
		
		setListAdapter(mAdapter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
