package com.android;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class AddItemActivity extends Activity implements OnTouchListener, CvCameraViewListener{
	
	
	   private static final String  TAG              = "OCVSample::Activity";

	    private Mat                  mRgba;
	    private Scalar               mBlobColorRgba;
	    private Scalar               mBlobColorHsv;
	    private ColorBlobDetector    mDetector;
	    private Mat                  mSpectrum;
	    private Size                 SPECTRUM_SIZE;
	    private Scalar               CONTOUR_COLOR;

	    private CameraBridgeViewBase mOpenCvCameraView;

	    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
	        @Override
	        public void onManagerConnected(int status) {
	            switch (status) {
	                case LoaderCallbackInterface.SUCCESS:
	                {
	                    Log.i(TAG, "OpenCV loaded successfully");
	                    mOpenCvCameraView.enableView();
	                    mOpenCvCameraView.setOnTouchListener(AddItemActivity.this);
	                } break;
	                default:
	                {
	                    super.onManagerConnected(status);
	                } break;
	            }
	        }
	    };
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
		super.onCreate(savedInstanceState);
	}

	public void onCameraViewStarted(int width, int height) {
		
	    mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
	}

	public void onCameraViewStopped() {
		 mRgba.release();
		
	}

	

	public Mat onCameraFrame(Mat inputFrame) {
		inputFrame.copyTo(mRgba);
		//No processing
		return inputFrame;
	}


	public boolean onTouch(View arg0, MotionEvent event) {
		
		
	       int cols = mRgba.cols();
	        int rows = mRgba.rows();

	        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
	        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

	        int x = (int)event.getX() - xOffset;
	        int y = (int)event.getY() - yOffset;

	        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

	        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

	        Rect touchedRect = new Rect();

	        touchedRect.x = (x>4) ? x-4 : 0;
	        touchedRect.y = (y>4) ? y-4 : 0;

	        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
	        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

	        Mat touchedRegionRgba = mRgba.submat(touchedRect);

	        Mat touchedRegionHsv = new Mat();
	        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

	        // Calculate average color of touched region
	        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
	        int pointCount = touchedRect.width*touchedRect.height;
	        for (int i = 0; i < mBlobColorHsv.val.length; i++)
	            mBlobColorHsv.val[i] /= pointCount;

	        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

	        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
	                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

	        mDetector.setHsvColor(mBlobColorHsv);
	        mDetector.setTouchPoint(new Point(x, y));

	        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

	        mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            if ( contours.size() > 0)
            {
            	Rect boundingRect = Imgproc.boundingRect(contours.get(0));
                Core.rectangle(mRgba, boundingRect.tl(), boundingRect.br(),this.mBlobColorRgba);
                
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                
                LayoutInflater factory = LayoutInflater.from(AddItemActivity.this);
                final ImageView view = (ImageView) factory.inflate(R.layout.item_preview, null);
                Mat subimg = mRgba.submat(boundingRect);
                final Bitmap bmp = Bitmap.createBitmap((int)subimg.size().width, (int)subimg.size().height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(subimg , bmp );
                view.setImageBitmap(bmp);
                subimg.release();             
                
                builder.setView(view);
                
                
                builder.setTitle(R.string.add_ask);
             builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        	Intent i = new Intent(getBaseContext(),AndroidLearnerActivity.class);

            				i.putExtra("new_item_image", bmp);
            				
            				setResult(RESULT_OK, i);
            				finish();
                        }
                    });
             builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        	dialog.dismiss();
                        }
                    });

             
             AlertDialog dialog = builder.create();
             
             dialog.show();
                
            }
  
	        touchedRegionRgba.release();
	        touchedRegionHsv.release();

	        return false; // don't need subsequent touch events
	}
	
	
	  private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
	        Mat pointMatRgba = new Mat();
	        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
	        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

	        return new Scalar(pointMatRgba.get(0, 0));
	    }
	
	
	
	
    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
		

}