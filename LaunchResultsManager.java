package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LaunchResultsManager {
	public static ArrayList<ArrayList<LaunchResults>> splitBySequenceName(ArrayList<LaunchResults> list) {
		ArrayList<ArrayList<LaunchResults>> ret = new ArrayList<ArrayList<LaunchResults>>();
		ArrayList<String> names = new ArrayList<String>();
		for(LaunchResults res : list) {
			if(!names.contains(res.seq.getDesc())) {
				names.add(res.seq.getDesc());
			}
		}
		
		for(String name : names) {
			ArrayList<LaunchResults> resultsForThisName = new ArrayList<LaunchResults>();
			for(LaunchResults res : list) {
				if(res.seq.getDesc().equals(name)) {
					resultsForThisName.add(res);
				}
			}
			ret.add(resultsForThisName);
		}
			
		return ret;
	}
	
	public static ArrayList<ArrayList<LaunchResults>> splitByEncoderName(ArrayList<LaunchResults> list) {
		ArrayList<ArrayList<LaunchResults>> ret = new ArrayList<ArrayList<LaunchResults>>();
		ArrayList<String> names = new ArrayList<String>();
		for(LaunchResults res : list) {
			if(!names.contains(res.encName)) {
				names.add(res.encName);
			}
		}
		
		for(String name : names) {
			ArrayList<LaunchResults> resultsForThisName = new ArrayList<LaunchResults>();
			for(LaunchResults res : list) {
				if(res.encName.equals(name)) {
					resultsForThisName.add(res);
				}
			}
			ret.add(resultsForThisName);
		}
			
		return ret;
	}
	
	public static class BitrateComparator implements Comparator<LaunchResults> {
		@Override
		public int compare(LaunchResults a, LaunchResults b) {
			return a.targetRate - b.targetRate;
		}
	}
	
	public static void sortByBitrate(ArrayList<LaunchResults> list) {
		Collections.sort(list, new BitrateComparator());
	}
}
