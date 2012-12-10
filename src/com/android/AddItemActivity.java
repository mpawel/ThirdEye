package com.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class AddItemActivity extends Activity implements OnTouchListener, CvCameraViewListener {

	private static final String TAG = "OCVSample::Activity";

	private Mat mRgba;

	private Scalar mBlobColorHsv;
	private ColorBlobDetector mDetector;
	private Scalar CONTOUR_COLOR;

	private CameraBridgeViewBase mOpenCvCameraView;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(AddItemActivity.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
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

		DescriptorHandler.descriptors = new ArrayList<Descriptor>();

		super.onCreate(savedInstanceState);
	}

	public void onCameraViewStarted(int width, int height) {

		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mDetector = new ColorBlobDetector();
		mBlobColorHsv = new Scalar(255);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
	}

	public void onCameraViewStopped() {
		mRgba.release();

	}

	public Mat onCameraFrame(Mat inputFrame) {
		inputFrame.copyTo(mRgba);
		// No processing
		return inputFrame;
	}

	public boolean onTouch(View arg0, MotionEvent event) {

		int cols = mRgba.cols();
		int rows = mRgba.rows();

		int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

		int x = (int) event.getX() - xOffset;
		int y = (int) event.getY() - yOffset;

		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
			return false;

		Rect touchedRect = new Rect();

		touchedRect.x = (x > 4) ? x - 4 : 0;
		touchedRect.y = (y > 4) ? y - 4 : 0;

		touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
		touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		int pointCount = touchedRect.width * touchedRect.height;
		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mDetector.setHsvColor(mBlobColorHsv);
		mDetector.setTouchPoint(new Point(x, y));

		mDetector.process(mRgba);
		final List<MatOfPoint> contours = mDetector.getContours();
		Log.e(TAG, "Contours count: " + contours.size());

		if (contours.size() > 0) {
			Rect boundingRect = Imgproc.boundingRect(contours.get(0));
			
			int margin = 3;
			boundingRect.x -= ( (boundingRect.x - margin) >= 0 ? margin : 0);
			boundingRect.y -= ( (boundingRect.y - margin) >= 0 ? margin : 0);
			boundingRect.width += ( (boundingRect.width + 10) < mRgba.width() ? margin : 0 );
			boundingRect.height += ( (boundingRect.height + 10) < mRgba.height() ? margin : 0 );

	

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			LayoutInflater factory = LayoutInflater.from(AddItemActivity.this);
			final ImageView view = (ImageView) factory.inflate(R.layout.item_preview, null);
			final Mat subimg = new Mat();
			mRgba.submat(boundingRect).copyTo(subimg);

			Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
			final Mat subimg_contour = mRgba.submat(boundingRect);

			final Bitmap bmp = Bitmap.createBitmap((int) subimg_contour.size().width,
					(int) subimg_contour.size().height, Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(subimg_contour, bmp);
			view.setImageBitmap(bmp);

			builder.setView(view);

			builder.setTitle(R.string.add_ask);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// create descriptor
					// add to descriptors list
					MatOfPoint contour = contours.get(0);

					

					DescriptorHandler.descriptors.add(DescriptorHandler
							.createDescriptor(subimg, contour, mBlobColorHsv));

					
					dialog.dismiss();
					subimg.release();
					subimg_contour.release();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					subimg.release();
					subimg_contour.release();
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

	@Override
	public void onPause() {
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.add_item, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_save:
			// ask for name - run voice for result
			Intent i = new Intent(getApplicationContext(), VoiceRecognition.class);
			startActivityForResult(i, AndroidLearnerActivity.VOICE_CHOOSER);

			return true;
		case R.id.menu_abort:
			// return empty result
			Intent intent = new Intent(getBaseContext(), AndroidLearnerActivity.class);
			intent.putExtra("status", "bad");

			setResult(RESULT_CANCELED, intent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case AndroidLearnerActivity.VOICE_CHOOSER:
			Intent intent = new Intent(getBaseContext(), AndroidLearnerActivity.class);
			if (resultCode == RESULT_OK && data != null && data.getExtras().get("item_name") != null)
			{

				ProgressDialog progress = ProgressDialog.show(this, this.getString(R.string.save_object), "");

				progress.show();
				progress.setMax(DescriptorHandler.descriptors.size());
				int prog = 0;

				List<String> responses = new ArrayList<String>();
				String name = (String) data.getExtras().get("item_name");

				for (Descriptor d : DescriptorHandler.descriptors) {
					d.name = name;
					Map<String, String> m = DescriptorHandler.toString(d);
					List<BasicNameValuePair> desc = new ArrayList<BasicNameValuePair>();
					for (Map.Entry<String, String> e : m.entrySet()) {
						desc.add(new BasicNameValuePair(e.getKey(), e.getValue()));
					}
					responses.add(DescriptorHandler.sendPost(desc));
					progress.setProgress(++prog);
				}

				progress.dismiss();
				intent.putExtra("status", responses.get(0));
				setResult(RESULT_OK, intent);
				finish();

			}
			else
			{
				Toast.makeText(this, R.string.none_selected, Toast.LENGTH_SHORT).show();
			}

			intent.putExtra("status", "bad");
			setResult(RESULT_CANCELED, intent);
			finish();

		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
