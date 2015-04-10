package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaCodecInfo;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.MultiChoiceModeListener;

public class CodecsListAdapter extends BaseAdapter implements MultiChoiceModeListener {
	
	private ArrayList<String> headers = new ArrayList<String>();
	private ArrayList<ArrayList<MediaCodecInfo>> sections = new ArrayList<ArrayList<MediaCodecInfo>>();
	private final Activity mContext;
	private final boolean mMode;
	
	public CodecsListAdapter(Activity cont, boolean fullMode) {	
		mContext = cont;
		mMode = fullMode;
		ArrayList<MediaCodecInfo> enc = CodecsManager.getSystemEncoders();
		if(!mMode) {
			addSection("Hardware Encoders", CodecsManager.filterHardware(enc, true));
			addSection("Software Encoders", CodecsManager.filterHardware(enc, false));
		} else {
			addSection("Hardware Video Encoders", CodecsManager.filterHardware(enc, true));
			addSection("Software Video Encoders", CodecsManager.filterHardware(enc, false));
			
			ArrayList<MediaCodecInfo> dec = CodecsManager.getSystemDecoders();
			addSection("Hardware Video Decoders", CodecsManager.filterHardware(dec, true));
			addSection("Software Video Decoders", CodecsManager.filterHardware(dec, false));
			
			ArrayList<MediaCodecInfo> audenc = CodecsManager.getAudioEncoders();
			addSection("Audio Encoders", audenc);
			
			ArrayList<MediaCodecInfo> auddec = CodecsManager.getAudioDecoders();
			addSection("Audio Decoders", auddec);
		}
		
	}
	
	public void addSection(String caption, ArrayList<MediaCodecInfo> data) {
		if(!data.isEmpty()) {
			headers.add(caption);
			sections.add(data);
		}
	}

    public String getItem(int position)
    {
    	View v = getView(position, null, null);
    	TextView tv = (TextView) v.findViewById(R.id.tv1);
    	if(tv != null) return tv.getText().toString();
        return "";
    }

    public int getCount()
    {
    	int totalSize = 0;
    	for(ArrayList<MediaCodecInfo> infos : sections) {
    		totalSize += infos.size();
    	}
        return headers.size() + totalSize;
    }

	@Override
	public int getViewTypeCount()
    {
        return headers.size() + sections.size(); //assert == headers.size()*2
    }

	@Override
	public int getItemViewType(int position)
    {
		int tPos = position, type = -1;
        for(int i = 0; i < sections.size(); ++i) {
        	type++;
        	if(tPos == 0) return type;
        	tPos -= sections.get(i).size() + 1;
        	type++;
        	if(tPos < 0) return type;
        }
        return -1;
    }

	public boolean areAllItemsSelectable()
    {
        return false;
    }
	

	@Override
	public boolean isEnabled(int position)
    {
		int type = getItemViewType(position);
        return type%2 != 0;
    }

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent)
    {
		int type = getItemViewType(position);
		int index = type/2;
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
		Resources res = mContext.getResources();
		if(type%2 == 0) { //header
			TextView tv = (TextView) inflater.inflate(R.layout.section_header, null);
			tv.setBackgroundColor(res.getColor(R.color.headerBGColor));
			tv.setTextColor(res.getColor(R.color.headerColor));
			tv.setText(headers.get(index));
			return tv;
		} else { //data
			View tlli = inflater.inflate(R.layout.codec_list_item, null);
			TextView tv1 = (TextView) tlli.findViewById(R.id.tv1);
			tv1.setTextColor(res.getColor(R.color.textColor));
			TextView tv2 = (TextView) tlli.findViewById(R.id.tv2);
			tv2.setTextColor(res.getColor(R.color.subtextColor));
			int tPos = position;
			for(int i = 0; i < index; ++i) {
				tPos -= sections.get(i).size() + 1;
			}
			final MediaCodecInfo info = sections.get(index).get(tPos-1);
			tv1.setText(info.getName());
			tv2.setText(info.getSupportedTypes()[0]);
			
			if(!mMode) {
				tlli.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						((ListView)parent).setItemChecked(position, !((ListView)parent).isItemChecked(position));
					}
				});
			}
			return tlli;
		}
    }

	@Override
	public long getItemId(int position)
    {
       return position;
    }

	//MultiChoiceModeListener methods
	private int selectedCount = 0;
	private ArrayList<Integer> selectedPos = new ArrayList<Integer>();
	
	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		mode.finish();
		Intent intent = new Intent(mContext, DecodersActivity.class);
		String [] codecNames = new String[selectedPos.size()];
		int i = 0;
		for(Integer pos : selectedPos) {
			codecNames[i] = getItem(pos);
			i++;
		}
		intent.putExtra(CodecsActivity.codecNamesTag, codecNames);
		mContext.startActivity(intent);
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		selectedCount = 0;
		selectedPos.clear();
        mode.setTitle(String.format("Selected: %d", selectedCount));
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		selectedCount = 0;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}
	
	private static final int minSelected = 1;

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
            long id, boolean checked) {
		if(selectedCount == minSelected && !checked) mode.getMenu().clear();
		if(selectedCount == (minSelected - 1) && checked) {
			MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.codecs, mode.getMenu());	
		}
		if(checked) {
			selectedPos.add(position);			
		} else {
			selectedPos.remove(Integer.valueOf(position));
		}
		selectedCount = selectedPos.size();
		mode.setTitle(String.format("Selected: %d", selectedCount));		
	}
}
