package com.android;

import java.util.concurrent.ExecutionException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidLearnerActivity extends Activity implements SensorEventListener {

	AndroidLearnerActivity mThis;
	ImageView mitemView;
	TextView mItemName;
	Button mSearchButt;

	private SensorManager mSensorManager;
	private Sensor mAccelerometr;

	public final static int VOICE_CHOOSER = 1;
	public final static int ADD_NEW = 2;
	public final static int SEARCH = 3;
	
	
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			super.onManagerConnected(status);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mThis = this;

		mitemView = (ImageView) findViewById(R.id.mainImgView);
		mItemName = (TextView) findViewById(R.id.name);
		mSearchButt = (Button) findViewById(R.id.searchItem);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometr = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		Button b = (Button) findViewById(R.id.chooseItem);
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), VoiceRecognition.class);
				startActivityForResult(i, VOICE_CHOOSER);
			}
		});
		b = (Button) findViewById(R.id.cameraPreview);
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), AddItemActivity.class);
				startActivityForResult(i, ADD_NEW);
			}
		});
		
		b = (Button) findViewById(R.id.searchItem);
		b.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(),MatchItemActivity.class));
			}
		});
		
		
		
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case VOICE_CHOOSER:
			if (resultCode == RESULT_OK && data != null && data.getExtras().get("item_name") != null) {

				final String name = (String) data.getExtras().get("item_name");


				String result = "";
				
				DBLoader loader = new DBLoader();
				loader.execute(name);
				

				
			}
			else 
			{
				mSearchButt.setEnabled(false);
				mItemName.setText(R.string.none_selected);
			}

			break;
		case ADD_NEW:
			if ( resultCode == RESULT_OK && data != null && data.getExtras().get("status") != null)
			{

				String status = (String) data.getExtras().get("status");
				if (status.contains("ok")) {
					mitemView.setImageBitmap((Bitmap) DescriptorHandler.descriptors.get(0).img);
					scaleImage();
					
					DescriptorDataset.training = DescriptorHandler.descriptors;
					
					mItemName.setText(DescriptorHandler.descriptors.get(0).name);
					mSearchButt.setEnabled(true);

				}
				else
					Toast.makeText(mThis, this.getString(R.string.loading_error), Toast.LENGTH_SHORT).show();

			}
			else
			{
				mSearchButt.setEnabled(false);
				mItemName.setText(R.string.none_selected);
			}
			break;
		case SEARCH:

			break;

		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometr, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		if (x > 7.555f) {
			Intent i = new Intent(getApplicationContext(), VoiceRecognition.class);
			startActivityForResult(i, VOICE_CHOOSER);
		} else if (x < -7.555f) {
			Intent i = new Intent(getApplicationContext(), AddItemActivity.class);
			startActivityForResult(i, ADD_NEW);
		}

	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// none

	}
	
	private void scaleImage()
	{
	    // Get the ImageView and its bitmap
	    ImageView view = mitemView;
	    Drawable drawing = view.getDrawable();
	    if (drawing == null) {
	        return; // Checking for null & return, as suggested in comments
	    }
	    Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

	    // Get current dimensions AND the desired bounding box
	    int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int bounding = dpToPx(250);
	    Log.i("Test", "original width = " + Integer.toString(width));
	    Log.i("Test", "original height = " + Integer.toString(height));
	    Log.i("Test", "bounding = " + Integer.toString(bounding));

	    // Determine how much to scale: the dimension requiring less scaling is
	    // closer to the its side. This way the image always stays inside your
	    // bounding box AND either x/y axis touches it.  
	    float xScale = ((float) bounding) / width;
	    float yScale = ((float) bounding) / height;
	    float scale = (xScale <= yScale) ? xScale : yScale;
	    Log.i("Test", "xScale = " + Float.toString(xScale));
	    Log.i("Test", "yScale = " + Float.toString(yScale));
	    Log.i("Test", "scale = " + Float.toString(scale));

	    // Create a matrix for the scaling and add the scaling data
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);

	    // Create a new bitmap and convert it to a format understood by the ImageView 
	    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    width = scaledBitmap.getWidth(); // re-use
	    height = scaledBitmap.getHeight(); // re-use
	    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
	    Log.i("Test", "scaled width = " + Integer.toString(width));
	    Log.i("Test", "scaled height = " + Integer.toString(height));

	    // Apply the scaled bitmap
	    view.setImageDrawable(result);

	    // Now change ImageView's dimensions to match the scaled image
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams(); 
	    params.width = width;
	    params.height = height;
	    view.setLayoutParams(params);

	    Log.i("Test", "done");
	}

	private int dpToPx(int dp)
	{
	    float density = getApplicationContext().getResources().getDisplayMetrics().density;
	    return Math.round((float)dp * density);
	}
	
	protected class DBLoader extends AsyncTask<String, Void, String> {
		
		private ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(mThis);
			progress.setMessage(mThis.getText(R.string.loading_object));
			progress.setCancelable(false);
			progress.show();
			
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			return DescriptorHandler.get(params[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			DescriptorDataset.training = DescriptorHandler.descriptors;

			
			if ( ! result.contains("ok") )
				Toast.makeText(mThis, mThis.getString(R.string.loading_error), Toast.LENGTH_SHORT).show();
			else 
			{
				mitemView.setImageBitmap((Bitmap) DescriptorDataset.training.get(0).img);
				scaleImage();
				mItemName.setText(DescriptorDataset.training.get(0).name);
				mSearchButt.setEnabled(true);
			}
			
			progress.dismiss();

			super.onPostExecute(result);
		}
		
	}
	
}