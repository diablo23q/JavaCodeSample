package ru.msu.cs.graphics.veqeclient;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ListView;
import android.app.ListActivity;

public class CodecsActivity extends ListActivity {
	
	public static String codecNamesTag = "CodecNames";
	private CodecsListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mAdapter = new CodecsListAdapter(this, false);
		
		setListAdapter(mAdapter);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getListView().setMultiChoiceModeListener(mAdapter);
		
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
