package ru.msu.cs.graphics.veqeclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.media.MediaCodecInfo;
//import android.os.Build;

public class ColorConverter {
	
	/*
	 * Supported colorspaces in priority order. Accordance with
	 * I420, NV12 colorspaces taken form here (Q5 in FAQ) - 
	 * http://bigflake.com/mediacodec/
	 */
	public static final ArrayList<Integer> supportedCSpaces = new ArrayList<Integer>();
	static {
		supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
		supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar);//TODO test
		/*if(Build.VERSION.SDK_INT >= 18) {		
			supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface); //TODO support surface input		
		} */
		supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);//TODO test
		supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar);//TODO test
		supportedCSpaces.add(MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar);//TODO test
	}
	
	public static void RGBAtoYUV444(byte[] RGBAFrame, int width, int height, byte[] YPlane, byte[] UPlane, byte[] VPlane) {
		//TODO check sizes, throw exceptions
		int width4x = width << 2;		
        for(int j = 0; j < height; ++j) {
        	for(int i = 0; i < width4x; i += 4) {
        		int index = j * width4x + i;
        		int r = RGBAFrame[index] & 0xFF;
        		int g = RGBAFrame[index + 1] & 0xFF;
        		int b = RGBAFrame[index + 2] & 0xFF;
        		index = j * width + (i >> 2);
        		int t = (8322 * r + 16425 * g +  3285 * b + 0x84000) >> 15;
        		YPlane[index] = (byte) t;
        		t = (28448 * b - 18816 * g - 9632 * r + 0x808000) >> 16;
        		UPlane[index] = (byte) t;
        		t = (28448 * r - 23968 * g - 4480 * b + 0x808000) >> 16;
        		VPlane[index] = (byte) t;           		
        	}
        }
	}
	
	public static void YUV444toI420Frame(byte[] YPlane, byte[] UPlane, byte[] VPlane, int width, int height, byte[] I420Frame) {
		//TODO check sizes, throw exceptions
		int shiftU = width * height;
		int shiftV = width * height * 5 >> 2;
		System.arraycopy(YPlane, 0, I420Frame, 0, width * height);
        for(int j = 0; j < height; j += 2) {
        	for(int i = 0; i < width; i += 2) {
        		int index = j * width + i;
        		int uvindex = (j * width >> 2) + (i >> 1);
        		int t = ((UPlane[index] & 0xFF) +
        				 (UPlane[index + 1] & 0xFF) +
        				 (UPlane[index + width] & 0xFF) +
        				 (UPlane[index + width + 1] & 0xFF)) >> 2;
        		I420Frame[shiftU + uvindex] = (byte) t;
        		
        		t = ((VPlane[index] & 0xFF) +
       				 (VPlane[index + 1] & 0xFF) +
       				 (VPlane[index + width] & 0xFF) +
       				 (VPlane[index + width + 1] & 0xFF)) >> 2;
       			I420Frame[shiftV + uvindex] = (byte) t;
        	}
        }
	}
	
	public static int selectColorSpace(MediaCodecInfo info) {
		int cspaces[] = info.getCapabilitiesForType(info.getSupportedTypes()[0]).colorFormats;
        for(int i = 0; i < ColorConverter.supportedCSpaces.size(); ++i) {
        	int supported = ColorConverter.supportedCSpaces.get(i);
        	for(int j : cspaces) {
        		if(j == supported) {
        			return j;
        		}
        	}
        }
        return 0;
	}
	
	public static void I420toNV12(byte[] I420Frame, int width, int height, byte[] NV12Frame) {
		System.arraycopy(I420Frame, 0, NV12Frame, 0, width * height);
		int shiftU = width * height;
		int shiftV = width * height * 5 >> 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {	
            	NV12Frame[shiftU + 2*(y/2)*(width/2) + 2*(x/2)] = I420Frame[shiftU + (y/2)*(width/2) + (x/2)];
            	NV12Frame[shiftU + 2*(y/2)*(width/2) + 2*(x/2) + 1] = I420Frame[shiftV + (y/2)*(width/2) + (x/2)];
            }
        }
	}
	
	public static void NV12toI420(byte[] NV12Frame, int width, int height, byte[] I420Frame) {
		System.arraycopy(NV12Frame, 0, I420Frame, 0, width * height);
		int shiftU = width * height;
		int shiftV = width * height * 5 >> 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
            	I420Frame[shiftU + (y/2)*(width/2) + (x/2)] = NV12Frame[shiftU + 2*(y/2)*(width/2) + 2*(x/2)];
            	I420Frame[shiftV + (y/2)*(width/2) + (x/2)] = NV12Frame[shiftU + 2*(y/2)*(width/2) + 2*(x/2) + 1];
            }
        }
	}
	
	public static SequenceInfo convertColorSpace(SequenceInfo src, int srcCS, int dstCS, boolean pad) {
		if(srcCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ||
		   srcCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
			
			if(dstCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ||
			   dstCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) { //no conversion
				return src; 
			} else { //I420 to NV12 conversion required
				SequenceInfo dst = new SequenceInfo(src.getWidth(), src.getHeight(), 0,	src.getMime(),
						"converted_" + src.getDesc(), "converted_" + src.getDesc() + ".yuv", null);

	        	try {
	        		byte srcFrame[] = new byte[src.getWidth()*src.getHeight()*3/2];
	        		byte dstFrame[] = new byte[src.getWidth()*src.getHeight()*3/2];
	        		
	        		int padding = pad ? (src.getWidth() * src.getHeight()) % 2048 : 0;
	        		byte[] inputFrameBufferWithPadding = new byte[padding + src.getWidth()*src.getHeight()*3/2];
	        		int offset = src.getWidth() * src.getHeight();
	        		
	        		
	        		BufferedInputStream is = src.getInputStream();
	        		BufferedOutputStream os = dst.getOutputStream(false);
	        		for(int i = 0; i < src.getFrameNum(); ++i) {
		        		is.read(srcFrame);
		        		I420toNV12(srcFrame, src.getWidth(), src.getHeight(), dstFrame);
		        		System.arraycopy(dstFrame, 0, inputFrameBufferWithPadding, 0, dstFrame.length);
		        		System.arraycopy(dstFrame, offset, inputFrameBufferWithPadding, offset + padding, dstFrame.length - offset);
						os.write(inputFrameBufferWithPadding);
						dst.incFrameNum();
	        		}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return dst;
			}
			
		} else {
			
			if(dstCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ||
			   dstCS == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) { //NV12 to I420 conversion required
				SequenceInfo dst = new SequenceInfo(src.getWidth(), src.getHeight(), 0,	src.getMime(),
						"converted_" + src.getDesc(), "converted_" + src.getDesc() + ".yuv", null);

	        	try {
	        		byte srcFrame[] = new byte[src.getWidth()*src.getHeight()*3/2];
	        		byte dstFrame[] = new byte[src.getWidth()*src.getHeight()*3/2];
	        		
	        		int padding = pad ? (src.getWidth() * src.getHeight()) % 2048 : 0;
	        		byte[] inputFrameBufferWithPadding = new byte[padding + src.getWidth()*src.getHeight()*3/2];
	        		int offset = src.getWidth() * src.getHeight();
	        		
	        		BufferedInputStream is = src.getInputStream();
	        		BufferedOutputStream os = dst.getOutputStream(false);
	        		for(int i = 0; i < src.getFrameNum(); ++i) {
		        		is.read(srcFrame);
		        		NV12toI420(srcFrame, src.getWidth(), src.getHeight(), dstFrame);
		        		System.arraycopy(dstFrame, 0, inputFrameBufferWithPadding, 0, dstFrame.length);
		        		System.arraycopy(dstFrame, offset, inputFrameBufferWithPadding, offset + padding, dstFrame.length - offset);
						os.write(inputFrameBufferWithPadding);
						dst.incFrameNum();
	        		}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return dst;
			} else { //no conversion
				return src; 
			}
			
		}
		
	}

}
