package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class App extends Application {

	private static Context mContext;
	private static Activity mActivity;
	private static ServerInfo mServer;
	private static ArrayList<ConnectionStatusListener> mCSListeners;
	
	public interface ConnectionStatusListener {
		void onStatusChanged(ServerInfo serv);
	}
	
	public static void attachCSListener(ConnectionStatusListener listener) {
		if(listener != null) {
			mCSListeners.add(listener);
		}
	}
	
	public static void detachCSListener(ConnectionStatusListener listener) {
		mCSListeners.remove(listener);
	}
	
	private static void notifyCSListeners(ServerInfo serv) {
		for(ConnectionStatusListener listener : mCSListeners) {
			listener.onStatusChanged(mServer);
		}
	}

    @Override
    public void onCreate() {
        super.onCreate();
        mCSListeners = new ArrayList<App.ConnectionStatusListener>();
        mContext = this;
        mActivity = new Activity();
        mServer = null;
    }

    public static Context getContext(){
        return mContext;
    }
    
    public static Activity getActivity(){
        return mActivity;
    }
    
    public static void runOnUI(Runnable r) {
    	getActivity().runOnUiThread(r);
    }

	public static ServerInfo getServer() {
		return mServer;
	}

	public static void setServer(ServerInfo serv) {
		mServer = serv;
		notifyCSListeners(mServer);
	}
    
}
