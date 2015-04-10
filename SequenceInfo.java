package ru.msu.cs.graphics.veqeclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;

public class SequenceInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int mWidth = 0, mHeight = 0, mFrameNum = 0;
	private String mMime = "";
	private boolean mIsInternal = true;
	private int mResource = 0;
	private String mFilename = "";
	private String mMD5Checksum = null;
	private String mDesc = "";
	
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getFrameNum() {
		return mFrameNum;
	}
	
	public String getMime() {
		return mMime;
	}
	
	public String getDesc() {
		return mDesc;
	}
	
	public SequenceInfo(int width, int height, int framenum, String mime, String description, 
			int resource) {
		mIsInternal = true;
		mWidth = width;
		mHeight = height;
		mFrameNum = framenum;
		mMime = mime;
		mDesc = description;
		mResource = resource;
	}
	
	public SequenceInfo(int width, int height, int framenum, String mime, String description, 
			String filename, String checksum) {
		mIsInternal = false;
		mWidth = width;
		mHeight = height;
		mFrameNum = framenum;
		mMime = mime;
		mDesc = description;
		mFilename = filename;
		mMD5Checksum = checksum;
	}
	
	public int incFrameNum() {
		return ++mFrameNum;
	}
	
	public BufferedInputStream getInputStream() throws IOException {
		BufferedInputStream bis = null;
		if(mIsInternal) {
			bis = new BufferedInputStream(App.getContext().getResources().openRawResource(mResource));
		} else {
			FileInputStream is = new FileInputStream(new File(App.getContext().getExternalFilesDir("sequences"), mFilename));
			if(mMD5Checksum != null) {
				String realMD5 = fileToMD5(is);
				if(realMD5 != null) {
					if(!realMD5.equals(mMD5Checksum)) {
						//TODO checksum mismatch warning
					}
				} else {
					//TODO unable to calculate checksum warning
				}
			}
			bis = new BufferedInputStream(is);
		}			
		return bis;
	}
	
	public BufferedOutputStream getOutputStream(boolean append) throws IOException {
		BufferedOutputStream bos = null;
		if(mIsInternal) {
			throw new IOException("Cannot write to app's internal resource");
		} else {
			File outputFile = new File(App.getContext().getExternalFilesDir("sequences"), mFilename);
        	if (!outputFile.exists()) {
	    		outputFile.createNewFile();
	    		mFrameNum = 0;
	    	}
        	bos = new BufferedOutputStream(new FileOutputStream(outputFile, append));
        	if(!append) mFrameNum = 0;
		}			
		return bos;
	}
	
	//TODO untested!
	private String fileToMD5(FileInputStream inputStream) {
		try {
			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = inputStream.read(buffer);
				if (numRead > 0)
					digest.update(buffer, 0, numRead);
			}
			byte [] md5Bytes = digest.digest();
			return convertHashToString(md5Bytes);
		} catch (Exception e) {
			return null;
		}
	}

	private String convertHashToString(byte[] md5Bytes) {
		String returnVal = "";
		for (int i = 0; i < md5Bytes.length; i++) {
			returnVal += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
		}
		return returnVal;
	}
}
