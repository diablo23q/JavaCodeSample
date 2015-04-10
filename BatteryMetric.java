package ru.msu.cs.graphics.veqeclient;

import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryMetric implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Double> measures;
	
	public BatteryMetric() {
		measures = new ArrayList<Double>();
		//measures.add(initialMeasurement());//TODO refactor
	}
	
	protected double initialMeasurement() {
		return getVoltage();
	}
	
	public double probe() {
		double volt = getVoltage();
		measures.add(volt);
		return 0;
	}

	protected double getVoltage() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    Intent intent = App.getContext().registerReceiver(null, ifilter);
	    return intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)/1000.0;
	}
	
	public double getResult() {
		return measures.get(0) - measures.get(measures.size() - 1);
	}
	
	public void print(OutputStreamWriter where) {
		
	}
}
