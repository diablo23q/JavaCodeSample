package ru.msu.cs.graphics.veqeclient;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.NetworkOnMainThreadException;
import android.widget.Toast;

public class ServerInfo {
	InetAddress ip;
	int port;
	String fullAddr;
	boolean sequences = false;
	
	ServerInfo(InetAddress _ip, int _port) {
		ip = _ip;
		port = _port;
		byte[] addr = ip.getAddress();
		fullAddr = String.format("http://%d.%d.%d.%d:%d", 
				addr[0] & 0xFF, addr[1] & 0xFF, addr[2] & 0xFF, addr[3] & 0xFF, port);
	}
	
	ServerInfo(String _ip, int _port) throws UnknownHostException {
		try {
			ip = InetAddress.getByName(_ip);
		} catch (NetworkOnMainThreadException e) {
			throw new UnknownHostException();
			/* If _ip is a numeric IP representation then
			 * no network happens on the main thread, else
			 * _ip is assumed to be a hostname which needs
			 * to be resolved. For now we only accept valid
			 * numeric IP address representation.
			 */
		}
		port = _port;
		byte[] addr = ip.getAddress();
		fullAddr = String.format("http://%d.%d.%d.%d:%d", 
				addr[0] & 0xFF, addr[1] & 0xFF, addr[2] & 0xFF, addr[3] & 0xFF, port);
	}
	
	public void setCapabilities(byte cap) {
		sequences = (cap & 0x01) != 0;
	}
	
	 private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
		 private final String respPref = "MSUCA14HTTP";
		 private final int readTimeoutMs = 2500;
		 
		 protected Boolean doInBackground(Void...voids) {
			URL url = null;
			HttpURLConnection urlConnection = null;
			try {
				url = new URL(fullAddr);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(readTimeoutMs);
				urlConnection.connect();
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				byte[] buf = new byte[32];
				in.read(buf);
				String response = new String(buf);
				if(response.startsWith(respPref)) {
					ServerInfo.this.setCapabilities(buf[respPref.length()]);
					urlConnection.disconnect();
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				urlConnection.disconnect();
			}
			return false;	
	     }

	     protected void onPostExecute(Boolean success) {
	    	 App.setServer(success ? ServerInfo.this : null);
	    	 Toast.makeText(App.getContext(), success ? "Connected" : "Connection failed", Toast.LENGTH_SHORT).show();
	     }
	 }
	
	public void connect() {
		new ConnectTask().execute();
	}
}
