package ru.msu.cs.graphics.veqeclient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class MetricsCalculator {

	public static class MetricInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private double mSum = 0;
		private ArrayList<Double> mFrameData = new ArrayList<Double>();
		private ArrayList<Double> mSortedData = null;
		private boolean mSorted = true;
		private String mName = "";
		
		public void add(double val) {
			mSorted = false;
			mFrameData.add(Double.valueOf(val));
			mSum += val;
		}
		
		public void setName(Metric m) {
			mName = m.getName();
		}
		
		public String getName() {
			return mName;
		}
		
		public double get(int index) {
			return mFrameData.get(index);
		}
		
		public double getAverage() {
			return mSum/mFrameData.size();
		}
		
		public int getFrameCount() {
			return mFrameData.size();
		}
		
		@SuppressWarnings("unchecked")
		public double getMedian() {
			if(!mSorted) {
				mSortedData = (ArrayList<Double>) mFrameData.clone();
				Collections.sort(mSortedData);
				mSorted = true;
			}
			return mSortedData.get(mSortedData.size()/2);
		}	
	}
	
	static interface Metric {
		
		public String getName();
		
		public MetricInfo calculate(SequenceInfo first, SequenceInfo second);
		
		public double calcForFrame(byte[] first, byte[] second, int width, int height);
		
	}
	
	public static class YPSNR implements Metric {

		@Override
		public MetricInfo calculate(SequenceInfo first, SequenceInfo second) {
			if(first.getWidth() != second.getWidth() ||
					first.getHeight() != second.getHeight()) {
				//TODO sizes mismatch error
			}
			if(!first.getMime().equals("video/raw") ||
					!second.getMime().equals("video/raw")) {
				//TODO only raw for this metric error
			}
			
			int minFrameNum = first.getFrameNum();
			if(first.getFrameNum() != second.getFrameNum()) {
				//TODO frame count mismatch warning
				if(second.getFrameNum() < minFrameNum) {
					minFrameNum = second.getFrameNum();
				}
			}
			
			MetricInfo res = new MetricInfo();
			res.setName(this);
			BufferedInputStream firstBIS = null, secondBIS = null;
			try {				
				firstBIS = first.getInputStream();
				secondBIS = second.getInputStream();
		
				byte[] firstFBuf = new byte[first.getWidth()*first.getHeight()*3/2];
				byte[] secondFBuf = new byte[first.getWidth()*first.getHeight()*3/2];
				
				for(int k = 0; k < minFrameNum; ++k) {
					firstBIS.read(firstFBuf);
					secondBIS.read(secondFBuf);
					res.add(calcForFrame(firstFBuf, secondFBuf, first.getWidth(), first.getHeight()));
				}				
			} catch (IOException e) {
				//TODO file error
				e.printStackTrace();
			}
			
			return res;
		}

		@Override
		public double calcForFrame(byte[] first, byte[] second, int width, int height) {
			if(first.length < width*height || second.length < width*height) {
				return 0;
			}
			final double PEAK = 65025;
			double sum = 0;
			for(int j = 0; j < height; ++j) {
				for(int i = 0; i < width; ++i) {
					int index = j * width + i;
					int diff = (first[index] & 0xFF) - (second[index] & 0xFF);
					sum += diff*diff;
				}
			}
			sum /= width*height;
			return 10*Math.log10(PEAK/sum);
		}
		
		@Override
		public String getName() {
			return "PSNR-Y";
		}
		
	}
	
}
