package ru.msu.cs.graphics.veqeclient;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final MainActivity act = this;
		
		Button list = (Button) findViewById(R.id.buttonList);
		list.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(act, CodecListActivity.class);
				act.startActivity(intent);
				
			}
		});
		Button analyze = (Button) findViewById(R.id.buttonAnalyze);
		analyze.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(act, CodecsActivity.class);
				act.startActivity(intent);
				
			}
		});
		Button connect = (Button) findViewById(R.id.buttonConnect);
		connect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(act, ConnectActivity.class);
				act.startActivity(intent);
				
			}
		});
		Button help = (Button) findViewById(R.id.buttonHelp);
		help.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(act, HelpActivity.class);
				act.startActivity(intent);
				
			}
		});
		
	}

}
