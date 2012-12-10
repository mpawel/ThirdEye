package com.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvNormalBayesClassifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public final class DescriptorHandler {
	
	public final static String BASE_URL = "http://stud.ics.p.lodz.pl/~pawelm/ve/";
	public final static String URL_ADD = BASE_URL + "add.php";
	public final static String URL_GET = BASE_URL + "get.php";
	

	public static List<Descriptor> descriptors = new LinkedList<Descriptor>();

	public static Map<String, String> toString(Descriptor desc) {

		Map<String, String> str = new TreeMap<String, String>();

		String rhist = "", ghist = "", bhist = "";

		String contour = "";

		
		for (int i = 0; i < desc.rh.length; ++i) {
			String sep = (i < desc.rh.length - 1 ? "," : "");
			rhist += desc.rh[i] + sep;
			ghist += desc.gh[i] + sep;
			bhist += desc.bh[i] + sep;
		}
		
		Point[] cl = desc.contour.toArray();

		for (int i = 0; i < cl.length; ++i) {
			String sep = (i < cl.length - 1 ? "," : "");
			contour += cl[i].x + ":" + cl[i].y + sep;
		}
		
		
		String rgba ="";
		for ( int i=0; i<desc.rgba.val.length; ++i) {
			String sep = (i < desc.rgba.val.length - 1 ? "," : "");
			rgba += desc.rgba.val[i] + sep;
		}
		

		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		desc.img.compress(Bitmap.CompressFormat.JPEG, 90, bao);

		byte[] ba = bao.toByteArray();

		String img = Base64.encodeBytes(ba);

		str.put("rh", rhist);
		str.put("gh", ghist);
		str.put("bh", bhist);
		str.put("contour", contour);
		str.put("img", img);
		str.put("rgba", rgba);
		str.put("name", desc.name);

		return str;

	}

	public static Descriptor fromString(TreeMap<String, String> str) {

		Descriptor desc = new Descriptor();
		
		desc.name = str.get("name");
		int[][] h = new int[3][256];
		String[][] cstr = new String[3][];
		cstr[0]=str.get("rh").split(",");cstr[1]=str.get("gh").split(",");cstr[2]=str.get("bh").split(",");
		
		for(int j=0; j<3; ++j)
		for( int i=0 ;i<cstr.length; ++i) {
			h[j][i]=Integer.parseInt(cstr[j][i]);
		}
		
		desc.rh = h[0];
		desc.gh = h[1];
		desc.bh = h[2];
		
		String[] sstr = str.get("contour").split(",");
		Point[] contour = new Point[sstr.length];
		for( int i=0; i<sstr.length; ++i) {
			Point p = new Point();
			String[] tmp = sstr[i].split(":");
			p.x = Double.parseDouble(tmp[0]);
			p.y = Double.parseDouble(tmp[1]);
			contour[i]=p;
		}
		
		desc.contour = new MatOfPoint();
		desc.contour.fromArray(contour);
		
		sstr = str.get("rgba").split(",");
		double[] sval = new double[sstr.length];
		for( int i=0; i<sstr.length; ++i)
			sval[i]=Double.parseDouble(sstr[i]);
		
		
		desc.rgba = new Scalar(sval);
		
		byte[] bitmapdata = null;
		try {
			bitmapdata = Base64.decode(str.get("img"));
		} catch (IOException e) {
			Log.e("log_tag", e.toString());
		}
		 desc.img = BitmapFactory.decodeByteArray(bitmapdata , 0, bitmapdata .length);
		
		return desc;
	}

	public static String sendPost(List<BasicNameValuePair> desc) {

		try {

			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(URL_ADD);

			httppost.setEntity(new UrlEncodedFormEntity(desc));

			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();

			
			InputStream is = entity.getContent();
			
			return streamToString(is);

		} catch (Exception e) {

			Log.e("log_tag", "Error in http connection " + e.toString());

		}
		
		return "error";
	}
	
	
	
	public static String get(String name) {
		
				
		try {
		
			HttpClient httpClient = new DefaultHttpClient();
			
			HttpPost httpGet = new HttpPost(URL_GET);
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			
			param.add(new BasicNameValuePair("name", name));
			
			httpGet.setEntity(new UrlEncodedFormEntity(param));
			
			HttpResponse response = httpClient.execute(httpGet);
			
			HttpEntity entity = response.getEntity();
			
			
			InputStream is = entity.getContent();
			String sstring = streamToString(is);
			JSONObject list = new JSONObject(sstring);
			JSONArray entries = list.getJSONArray("list");
			
			List<TreeMap<String, String>> data = new ArrayList< TreeMap<String, String> >();
			
			for ( int i=0; i<entries.length(); ++i  ) {
				TreeMap<String, String> entry = new TreeMap<String, String>();
				
				
				entry.put("name", entries.getJSONObject(i).getString("name"));
				entry.put("rh", entries.getJSONObject(i).getString("rh"));
				entry.put("gh", entries.getJSONObject(i).getString("gh"));
				entry.put("bh", entries.getJSONObject(i).getString("bh"));
				entry.put("contour", entries.getJSONObject(i).getString("contour"));
				entry.put("rgba", entries.getJSONObject(i).getString("rgba"));
				entry.put("img", entries.getJSONObject(i).getString("img"));
				data.add(entry);
			}
			
			descriptors = new ArrayList<Descriptor>();
			
			for ( TreeMap<String, String> entry : data) {
				descriptors.add(fromString(entry));
			}
			
			
			return "ok";
			
		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
			e.printStackTrace();
		}
		
		return "error";
		
	}
	
	
	private static String streamToString(InputStream stream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {
		
			while ((line = br.readLine()) != null) {
			  sb.append(line);
			}
		

			br.close();
		} catch (IOException e) {
			Log.e("log_tag", "Error in http result stream " + e.toString());
		}
		
		return sb.toString();
	}
	
	
	public static Descriptor createDescriptor(Mat img, MatOfPoint contour, Scalar rgba) {
		Descriptor desc = new Descriptor();
		
		
		int[] rh = new int[256];
		int[] gh = new int[256];
		int[] bh = new int[256];
		 
		for (int i=0; i<img.width(); ++i) {
			for(int j=0; j<img.height(); ++j) {
				rh[((int)Math.floor(img.get(j, i)[0]))]++;
				gh[((int)Math.floor(img.get(j, i)[1]))]++;
				bh[((int)Math.floor(img.get(j, i)[2]))]++;
			}
		}

		desc.rh = rh;
		desc.gh = gh;
		desc.bh = bh;
		
		DescriptorDataset.normalizaHistogram(desc.rh);
		DescriptorDataset.normalizaHistogram(desc.gh);
		DescriptorDataset.normalizaHistogram(desc.bh);
	
		desc.name= "";
		desc.contour = contour;
		Bitmap bmp = Bitmap.createBitmap((int) img.size().width, (int) img.size().height,Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(img, bmp);
		desc.img = bmp;
		desc.rgba = rgba;
		
		
		
		return desc;
	}
	
	
	
	

}
