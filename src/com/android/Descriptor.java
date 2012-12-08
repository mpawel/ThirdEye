package com.android;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.graphics.Bitmap;

public class Descriptor {
	

	//R G B  histograms
	public int[] rh, gh, bh;
	
	//contour
	public MatOfPoint contour;
	
	//printable image
	public Bitmap img;
	
	//string name
	public String name;
	
	//blob color
	public Scalar rgba;
	

}
