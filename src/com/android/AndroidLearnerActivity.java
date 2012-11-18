package com.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
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
				startActivityForResult(i,ADD_NEW);
			}
		});

	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode)
		{
		case VOICE_CHOOSER :
			if ( resultCode == RESULT_OK )
			{
				mItemName.setText((String) data.getExtras().get("item_name"));
				mSearchButt.setEnabled(true);
				
				//TODO add loading image connected with recognized name
				
			}
			else
			{
				mSearchButt.setEnabled(false);
				mItemName.setText(R.string.none_selected);
			}
				
			
			break;
		case ADD_NEW :
			mitemView.setImageBitmap((Bitmap) data.getParcelableExtra("new_item_image"));
			
			break;
		case SEARCH :
			
			
			break;
			
		default : break;
			
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

			if ( x > 7.555f)
			{
				Intent i = new Intent(getApplicationContext(), VoiceRecognition.class);
				startActivityForResult(i, VOICE_CHOOSER);
			}
			else if ( x < -7.555f)
			{
				Intent i = new Intent(getApplicationContext(), AddItemActivity.class);
				startActivityForResult(i, ADD_NEW);
			}
					
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// none
		
	}
}