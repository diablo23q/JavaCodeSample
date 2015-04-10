package ru.msu.cs.graphics.veqeclient;

public class SequenceManager {
	
	public static SequenceInfo bus = new SequenceInfo(352, 288, 150, "video/raw", "bus.yuv", R.raw.bus); //TODO create list
	public static SequenceInfo crew = new SequenceInfo(352, 288, 150, "video/raw", "crew.yuv", R.raw.crew);
	public static SequenceInfo soccer = new SequenceInfo(352, 288, 150, "video/raw", "soccer.yuv", R.raw.soccer);
	
}
