package ru.msu.cs.graphics.veqeclient;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

@SuppressWarnings("deprecation")
public class ConnectActivity extends Activity implements App.ConnectionStatusListener {
	
	private static int VERSION = 1;
	private static int TIMEOUT = 10; 
	private static String PREFIX = "MSUCA14";
	private static String RESPPREF = PREFIX + "RE";	
	
	private ProgressBar mProg;
	private ServerListAdapter mAdapter;
	private Button mUpdBtn;
	private Thread updThread;
	
	private class ServerListAdapter extends BaseAdapter {

		private ArrayList<ServerInfo> mServers;
		
		ServerListAdapter() {
			mServers = new ArrayList<ServerInfo>();
		}
		
		public void clear() {
			mServers.clear();
			notifyDataSetChanged();
		}
		
		public void add(ServerInfo info) {
			mServers.add(info);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mServers.size();
		}

		@Override
		public Object getItem(int pos) {
			return mServers.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View arg1, ViewGroup arg2) {
			LayoutInflater inflater = (LayoutInflater)App.getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
			Resources res = App.getContext().getResources();
			
			View tlli = inflater.inflate(R.layout.codec_list_item, null);
			
			TextView tv1 = (TextView) tlli.findViewById(R.id.tv1);
			tv1.setTextColor(res.getColor(R.color.textColor));
			TextView tv2 = (TextView) tlli.findViewById(R.id.tv2);
			tv2.setTextColor(res.getColor(R.color.subtextColor));
			
			final ServerInfo info = mServers.get(pos);
					
			tv1.setText(info.fullAddr);
			tv2.setText(String.format("Capabilities: %s", info.sequences ? "store sequences" : ""));
			tlli.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					info.connect();					
				}
			});
			return tlli;
		}
		
	}
	
	private void updateServ() {
		try {
			mUpdBtn.setEnabled(false);
			mAdapter.clear();				
			
			
			WifiManager wifi = (WifiManager) App.getContext().getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcp = wifi.getDhcpInfo();
		    if(dhcp == null) {
				mUpdBtn.setEnabled(true);				
		    	return;
		    }
	
		    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		    byte[] quads = new byte[4];
		    for (int k = 0; k < 4; k++)
		      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    
		
			final InetAddress addr = InetAddress.getByAddress(quads);		
			
			updThread = new Thread(new Runnable(){
		        @Override
		        public void run() {
		        	DatagramSocket socket = null; 
				    try {
				    	mProg.setProgress(0);
				    	
				    	socket = new DatagramSocket(5054);						
						socket.setBroadcast(true);
						String data = PREFIX + String.valueOf(VERSION) + '\0';
						DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), addr, 5055);
						socket.send(packet); 
						
						socket.setSoTimeout(1000);
						byte[] buf = new byte[32];
						for(int t = 1; !Thread.interrupted() && t <= TIMEOUT; ++t) {
							try {
								final DatagramPacket rpacket = new DatagramPacket(buf, buf.length);
								socket.receive(rpacket); 
								String rdata = new String(rpacket.getData(), rpacket.getOffset(), rpacket.getLength());
								if(rdata.startsWith(RESPPREF)) {
									ByteBuffer bb = ByteBuffer.wrap(rpacket.getData(), rpacket.getOffset() + RESPPREF.length(), 3);
									bb.order(ByteOrder.LITTLE_ENDIAN);
									bb.compact();
									final int port = bb.getShort(0) & 0xFFFF;
									final byte cap = bb.get(2);
									ConnectActivity.this.runOnUiThread(new Runnable() {										
										@Override
										public void run() {
											ServerInfo serv = new ServerInfo(rpacket.getAddress(), port);
											serv.setCapabilities(cap);
											mAdapter.add(serv);
										}
									});
								}
								
								//Log.i("RECIEVE", new String(rdata));
							} catch (InterruptedIOException e) {
								//Log.i("RECIEVE", "Nothing");
							}
							mProg.setProgress(t*mProg.getMax()/TIMEOUT);							
						}
						
						socket.close(); 
		            } catch (Exception e) {
		                e.printStackTrace();
		                if(socket != null) socket.close();
		            }
				    mProg.setProgress(mProg.getMax());
				    ConnectActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mUpdBtn.setEnabled(true);				
						}
					});
		        }
		    });

			updThread.start(); 
		} catch (Exception e) {
			e.printStackTrace();
			mProg.setProgress(mProg.getMax());
			mUpdBtn.setEnabled(true);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);
		setupActionBar();
		//TODO save last successful manual ip
		
		//Status TextView
		onStatusChanged(App.getServer());
		
		//ProgressBar
		mProg = (ProgressBar) findViewById(R.id.pbUpdate);
		
		//ListView
		ListView lv = (ListView) findViewById(R.id.lvServers);
		mAdapter = new ServerListAdapter();
		lv.setAdapter(mAdapter);
		
		//Update button
		mUpdBtn = (Button) findViewById(R.id.buttonUpdate);
		mUpdBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateServ();
			}
		});
		
		//Manual connection
		Button connectBtn = (Button) findViewById(R.id.buttonManual);
		final EditText ipportTV = (EditText) findViewById(R.id.etConnect);
		connectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String ipport = ipportTV.getText().toString();
				if(ipport.contains(":")) {
					String pts[] = ipport.split(":");
					try {
						ServerInfo serv = new ServerInfo(pts[0], Integer.valueOf(pts[1]));
						serv.connect();
					} catch (NumberFormatException e) {
						Toast.makeText(App.getContext(), "Invalid address", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					} catch (UnknownHostException e) {
						Toast.makeText(App.getContext(), "Invalid address", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					
				} else {
					Toast.makeText(App.getContext(), "Invalid address", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		App.attachCSListener(this);
		updateServ();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		App.detachCSListener(this);
		if(updThread != null) {
			if(updThread.isAlive()) updThread.interrupt();
		}
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

	@Override
	public void onStatusChanged(ServerInfo serv) {
		TextView status = (TextView) findViewById(R.id.tvConnStatus);
		status.setText((serv == null) ? "Offline" : String.format("Connected to %s", serv.ip.getHostAddress()));
	}

}
