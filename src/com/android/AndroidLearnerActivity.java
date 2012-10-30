package com.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class AndroidLearnerActivity extends Activity {
	
	AndroidLearnerActivity mThis;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mThis = this;
        View w = findViewById(R.id.clickme);
        w.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				
				Toast.makeText(mThis, "("+e.getX()+","+e.getY()+")", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
        Button b = (Button) findViewById(R.id.trySpeak);
        b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), VoiceRecognitionDemo.class);
				startActivity(i);
			}
		});
        b = (Button) findViewById(R.id.tryPhoto);
        b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), MyCameraActivity.class);
				startActivity(i);
			}
		});
        
        
    }
}