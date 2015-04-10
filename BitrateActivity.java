package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.support.v4.app.NavUtils;

public class BitrateActivity extends ListActivity {
	public static String bitratesTag = "Bitrates";
	
	ArrayAdapter<String> mAdapter;
	ArrayList<String> mBitrates;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		ListView lv = getListView();
		
		if(savedInstanceState != null) {
			mBitrates = (ArrayList<String>) savedInstanceState.getSerializable(bitratesTag);
		} else {
			mBitrates = new ArrayList<String>();
		}
		
		mAdapter = new ArrayAdapter<String>(this, R.layout.bitrates_list_item, R.id.bitrate, mBitrates);
		
		ImageButton addButton = (ImageButton) getLayoutInflater().inflate(R.layout.bitrates_footer, null);
		addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View button) {
				AddBitratesDialogFragment df = new AddBitratesDialogFragment();
				df.show(getFragmentManager(), "ABDialog");	
			}
		});
		
		if(mAdapter.getCount() == 0) {//TODO remove later
			mAdapter.add("500");
			mAdapter.add("1000");
			mAdapter.add("1600");
			mAdapter.add("2300");			
		}
		//lv.setFooterDividersEnabled(false);
		lv.addFooterView(addButton);
		setListAdapter(mAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putSerializable(bitratesTag, mBitrates);
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.bitrate, menu);
		menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(BitrateActivity.this, ParametersActivity.class);
				intent.putStringArrayListExtra(bitratesTag, mBitrates);
				Intent recvd = BitrateActivity.this.getIntent();
				intent.putExtra(CodecsActivity.codecNamesTag, recvd.getStringArrayExtra(CodecsActivity.codecNamesTag));
				intent.putExtra(DecodersActivity.decNamesTag, recvd.getStringArrayExtra(DecodersActivity.decNamesTag));
				BitrateActivity.this.startActivity(intent);
				return true;
			}
		});
		return true;
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
		
	private class AddBitratesDialogFragment extends DialogFragment {

		private void finalizeTextViews(ViewGroup parent) {
	        final int count = parent.getChildCount();
	        for (int i = 0; i < count; i++) {
	            final View child = parent.getChildAt(i);
	            if (child instanceof ViewGroup) {
	            	finalizeTextViews((ViewGroup) child);
	            } else if (child instanceof EditText) {
	                ((EditText) child).onEditorAction(EditorInfo.IME_ACTION_DONE);
	            }
	        }
	    }
		
		//TODO soft keyboard, delete, edit, next only with 2 bitrates (adv adapter), exclude same bitrates, save entered rates
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        final NumberPicker np = (NumberPicker) getActivity().getLayoutInflater().inflate(R.layout.bitrates_picker, null);
	        np.setMinValue(0);
	        np.setMaxValue(1000000);
	        np.setValue(1000);
	    	
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setView(np).setMessage(R.string.title_dialog_bitrate)
	               .setPositiveButton(R.string.add_btn, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   finalizeTextViews(np);
	                       mAdapter.add(String.valueOf(np.getValue()));
	                   }
	               })
	               .setNegativeButton(R.string.cancel_btn, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       
	                   }
	               });
	        return builder.create();
	    }
	}

}
