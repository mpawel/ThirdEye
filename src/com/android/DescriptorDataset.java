package com.android;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DescriptorDataset {

	public static List<Descriptor> training = new ArrayList<Descriptor>();

	public static void normalizaHistogram(int[] h) {

		double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int i = 0; i < h.length; ++i) {
			max = Math.max(max, h[i]);
			min = Math.min(min, h[i]);
		}

		double width = max - min;
		for (int i = 0; i < h.length; ++i) {
			h[i] = (int) ((((double) h[i] - min) / width) * 255);
		}

	}

	public static double getMinDist(Descriptor d) {

		double minDist = Double.MAX_VALUE;

		for (Descriptor b : training) {
			minDist = Math.min(minDist, dist(d, b));
		}

		return minDist;

	}

	private static double dist(Descriptor a, Descriptor b) {

		double distance = 0;

		for (int i = 0; i < a.rh.length; ++i) {
			distance += Math.abs(a.rh[i] - b.rh[i]);
			distance += Math.abs(a.gh[i] - b.gh[i]);
			distance += Math.abs(a.bh[i] - b.bh[i]);
		}

		double mse = 0;
		if (a.rgba != null && b.rgba != null)
			for (int i = 0; i < a.rgba.val.length; ++i) {
				mse += Math.pow(a.rgba.val[i] - b.rgba.val[i], 2);
			}
		distance += Math.sqrt(mse);

		return distance;
	}

	public static double minArea() {

		double minArea = Double.MAX_VALUE;

		for (Descriptor d : training) {
			minArea = Math.min(Imgproc.contourArea(d.contour), minArea);
		}

		return minArea;
	}

	public static Scalar avgHsv() {
		Scalar avg = new Scalar(training.get(0).rgba.val);

		for (int i = 0; i < avg.val.length; ++i)
			avg.val[i] = 0.0d;

		for (int i = 0; i < avg.val.length; ++i) {
			for (Descriptor d : training) {
				avg.val[i] += d.rgba.val[i];
			}
			avg.val[i] /= (double) training.size();
		}

		return avg;
	}

}
