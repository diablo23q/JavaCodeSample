package ru.msu.cs.graphics.veqeclient;

import java.util.ArrayList;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

public class CodecsManager {
	
	public static ArrayList<MediaCodecInfo> getSystemEncoders() {
		int numCodecs = MediaCodecList.getCodecCount();
		
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String[] types = info.getSupportedTypes();
			if(types.length > 0) {
				if(types[0].indexOf("video") != -1) {
					if(info.isEncoder()) {
						codecList.add(info);
					}
				}
			}
		}	
		return codecList;
	}
	
	public static ArrayList<MediaCodecInfo> getAudioEncoders() {
		int numCodecs = MediaCodecList.getCodecCount();
		
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String[] types = info.getSupportedTypes();
			if(types.length > 0) {
				if(types[0].indexOf("audio") != -1) {
					if(info.isEncoder()) {
						codecList.add(info);
					}
				}
			}
		}	
		return codecList;
	}
	
	public static ArrayList<MediaCodecInfo> getSystemDecoders() {
		int numCodecs = MediaCodecList.getCodecCount();
		
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String[] types = info.getSupportedTypes();
			if(types.length > 0) {
				if(types[0].indexOf("video") != -1) {
					if(!info.isEncoder()) {
						codecList.add(info);
					}
				}
			}
		}	
		return codecList;
	}
	
	public static ArrayList<MediaCodecInfo> getAudioDecoders() {
		int numCodecs = MediaCodecList.getCodecCount();
		
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String[] types = info.getSupportedTypes();
			if(types.length > 0) {
				if(types[0].indexOf("audio") != -1) {
					if(!info.isEncoder()) {
						codecList.add(info);
					}
				}
			}
		}	
		return codecList;
	}
	
	public static MediaCodecInfo getCodecInfo(String name) {
		int numCodecs = MediaCodecList.getCodecCount();		
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			if(info.getName().equals(name)) return info;
		}	
		return null;
	}
	
	public static ArrayList<MediaCodecInfo> filterHardware(ArrayList<MediaCodecInfo> in, boolean hard) {
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		for(MediaCodecInfo info : in) {
			if(info.getName().indexOf("google") != -1) {
				if(!hard) codecList.add(info);
			} else {
				if(hard) codecList.add(info);
			}
		}
		return codecList;
	}
	
	public static ArrayList<MediaCodecInfo> filterMime(ArrayList<MediaCodecInfo> in, String mime) {
		ArrayList<MediaCodecInfo> codecList = new ArrayList<MediaCodecInfo>();	
		for(MediaCodecInfo info : in) {
			if(info.getSupportedTypes()[0].equals(mime)) {
				codecList.add(info);
			}
		}
		return codecList;
	}
	
}
