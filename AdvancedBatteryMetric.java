package ru.msu.cs.graphics.veqeclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class AdvancedBatteryMetric extends BatteryMetric {
	private static final long serialVersionUID = 1L;
	//private static final String path = "/sys/class/power_supply/battery/device/FG_Battery_CurrentConsumption";
	private static final String path = "/sys/class/power_supply/battery/current_now";
	//private static final String path = "/sys/class/power_supply/battery/batt_current";
	
	File fileWithCurr = null;
	
	protected ArrayList<Double> volt, curr;
	private static double pInit = 0;
	
	long gtime = 0, glast = 0, gprev = 0, gtotal = 0;
	double gval = 0, vstart, cstart;
	
	transient private BattBR mBatInfoReceiver = new BattBR();
	
	private class BattBR extends BroadcastReceiver{
		int numRecv = 0;
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			numRecv++;			
		}
		
		public boolean changed() {
			return numRecv > 2;
		}
	};
	
	public AdvancedBatteryMetric() {
		App.getContext().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		fileWithCurr = new File(path);
		volt = new ArrayList<Double>();
		curr = new ArrayList<Double>();
		pInit = initialMeasurement();
	}
	
	@Override
	protected double initialMeasurement() {
		while(!mBatInfoReceiver.changed()) {
			try { Thread.sleep(100); } catch (InterruptedException e) {}
		}
		App.getContext().unregisterReceiver(mBatInfoReceiver);
		long time = -System.nanoTime();
		long last = -time, prev = 0;
		double val = 0;
		for(int i = 0; i < 40; ++i) {
			prev = -last;
			vstart = getVoltage();
			cstart = getCurrent();
			volt.add(vstart);
			curr.add(cstart);
			//Log.e("BatteryInfo_init", String.format("Voltage: %g. Current: %g. Val: %g", vstart, cstart, val));
			last = System.nanoTime();
			val += vstart*cstart*(last + prev);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
		}		
		time += System.nanoTime();
		Log.e("BatteryInfo", String.format("Result: %g", val / time));
		volt.add(10000000.0);
		curr.add(10000000.0);
		return val / time;
	}
	
	@Override
	public double probe() {
		double v = getVoltage();
		double c = getCurrent();
		if((c != cstart) || (v != vstart)) {
			if(gtime == 0) {
				gtime -= System.nanoTime();
				glast = -gtime;
			}
			gprev = -glast;
			volt.add(v);
			curr.add(c);
			//Log.e("BatteryInfo_coding", String.format("Voltage: %g. Current: %g. Val: %g", v, c, gval));
			glast = System.nanoTime();
			gtotal = glast + gtime;
			gval += v*c*(glast + gprev);
		}
		return v*c;
	}
	
	@Override
	public double getResult() {
		return gtime == 0 ? 0 : (gval - pInit*gtotal)/1000000000.0;
	}
	
	@Override
	public void print(OutputStreamWriter where) {
		try {
			where.write(String.format("P_0: %f\ngVal: %f\ntime: %d\n", pInit, gval, gtotal));
			for(int i = 0; i < volt.size(); i += 20) {
				where.write(String.format("%f;%f\n", volt.get(i), curr.get(i)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected double getCurrent() {
		double c = 0;
		if(fileWithCurr != null) {
			if(fileWithCurr.exists()) {
                try {      
                    FileReader sr = new FileReader(fileWithCurr);
                    BufferedReader br = new BufferedReader(sr);                     
            
                    String text = br.readLine();
                    Long value = Long.parseLong(text);
                    //c = value / -1000.0;
                    c = value / 1000000.0;//TODO
                    
                    br.close();
                    sr.close();                       
	            }
	            catch (Exception ex) {
	            	Log.e("Current", ex.getMessage());
	            	ex.printStackTrace();
	            }
			}
		}
		return c;
	}
	
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

}
