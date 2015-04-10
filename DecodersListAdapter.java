package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DecodersListAdapter extends BaseAdapter {

	private final String[] mArray;
	private final Activity mContext;
	
	public DecodersListAdapter(Activity cont, String[] arr) {
		mArray = arr;
		mContext = cont;
	}
	
	@Override
	public int getCount() {
		return mArray.length;
	}

	@Override
	public String getItem(int position) {
		View v = getView(position, null, null);
		Spinner sp = (Spinner) v.findViewById(R.id.decSpinner);
		if(sp != null) return (String) sp.getSelectedItem();
		return "";
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(R.layout.decoders_list_item, null);
		TextView tv = (TextView) item.findViewById(R.id.codecName);
		tv.setText(mArray[position]);
		
		Spinner sp = (Spinner) item.findViewById(R.id.decSpinner);
		ArrayList<String> decNames = new ArrayList<String>();
		MediaCodecInfo encInfo = CodecsManager.getCodecInfo(mArray[position]);
		
		ArrayList<MediaCodecInfo> decInfos = new ArrayList<MediaCodecInfo>();
		if(encInfo != null) 
			decInfos = CodecsManager.filterMime(CodecsManager.getSystemDecoders(), encInfo.getSupportedTypes()[0]);
		for(MediaCodecInfo info : decInfos) {
			decNames.add(info.getName());
		}
		
		String[] decArr = new String[decNames.size()];
		decArr = decNames.toArray(decArr);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, decArr);		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		
		sp.setAdapter(adapter);
		return item;
	}

	public String [] getSelectedNames() {
		String [] res = new String[mArray.length];
		for(int i = 0; i < res.length; ++i) {
			res[i] = getItem(i);
		}
		return res;
	}

}
