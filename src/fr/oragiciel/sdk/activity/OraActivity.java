package fr.oragiciel.sdk.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import fr.oragiciel.sdk.color.ColorDialog;
import fr.oragiciel.sdk.color.ColorDialogListener;
import fr.oragiciel.sdk.touch.OraTouchDetector;
import fr.oragiciel.sdk.touch.OraTouchListener;

public class OraActivity extends Activity {

	private LinearLayout rootLayout;
	private RelativeLayout worldLayout;
	private RelativeLayout oraLayout;
	private RelativeLayout touchLayout;

	private int worldBackGroudColor = Color.BLACK;

	private OraTouchDetector oraTouchDetector;
	private OraScreenCalcul oraScreenCalcul;
	private SurfaceView worldCamera;

	private Camera camera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		oraTouchDetector = new OraTouchDetector(this);
		oraScreenCalcul = new OraScreenCalcul();

		setActivitySetting();

		checkScreenSize();

		oraTouchDetector.setCaputeZone(oraScreenCalcul.getTouchRect());
		oraTouchDetector.setSimulator(oraScreenCalcul.isTouchSimulator());

		if (oraScreenCalcul.isTouchSimulator()) {
			createSimulator();
		} else {
			createOra();
		}

	}
private class MyRelativeLayout extends RelativeLayout {

	public MyRelativeLayout(Context context) {
		super(context);
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return true;
	}
	 @Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}
}
	private void createOra() {
		oraLayout = new RelativeLayout(this);
		oraLayout.setBackgroundColor(Color.BLACK);
		RelativeLayout.LayoutParams oraLayoutparams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		super.setContentView(oraLayout, oraLayoutparams);

	}

	private void createSimulator() {
		rootLayout = new LinearLayout(this);
		rootLayout.setOrientation(LinearLayout.HORIZONTAL);
		rootLayout.refreshDrawableState();
		LayoutParams rootLayoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		super.setContentView(rootLayout, rootLayoutParams);

		worldLayout = new RelativeLayout(this);
//		worldLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
//		worldLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
		LayoutParams worldParams = new LinearLayout.LayoutParams(
				oraScreenCalcul.getWorldWidth(), LayoutParams.MATCH_PARENT);
		rootLayout.addView(worldLayout, worldParams);

		worldCamera = new SurfaceView(this);
		 worldCamera.setBackgroundColor(worldBackGroudColor);
		worldLayout.addView(worldCamera, rootLayoutParams);

		RelativeLayout worldUpperLayout = new RelativeLayout(this);
		worldUpperLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
		worldUpperLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);

		worldLayout.addView(worldUpperLayout, rootLayoutParams);

		oraLayout = new MyRelativeLayout(this);
		oraLayout.setBackgroundColor(Color.TRANSPARENT);
		RelativeLayout.LayoutParams oraLayoutparams = new RelativeLayout.LayoutParams(
				oraScreenCalcul.getOraScreenWidth(), oraScreenCalcul.getOraScreenHeight());
		worldUpperLayout.addView(oraLayout, oraLayoutparams);
		createPanelSimulator();
	}

	private void checkScreenSize() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		Point realSize = new Point();
		getWindowManager().getDefaultDisplay().getRealSize(realSize);
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		try {
			oraScreenCalcul.setScreenMetrics(displayMetrics, realSize);
		} catch (RuntimeException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.getMessage());
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	private void setActivitySetting() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void createPanelSimulator() {
		LinearLayout panelSimulator = new LinearLayout(this);
		LayoutParams panelSimulatorParams = new LinearLayout.LayoutParams(
				oraScreenCalcul.getSimulatorWidth(), LayoutParams.MATCH_PARENT);
		rootLayout.addView(panelSimulator, panelSimulatorParams);

		panelSimulator.setOrientation(LinearLayout.VERTICAL);
		panelSimulator.setBackgroundColor(Color.BLACK);
		panelSimulator.setPadding(oraScreenCalcul.getSimulatorPadding(),
				oraScreenCalcul.getSimulatorPadding(),
				oraScreenCalcul.getSimulatorPadding(),
				oraScreenCalcul.getSimulatorPadding());

		LinearLayout panelControl = new LinearLayout(this);
		panelControl.setOrientation(LinearLayout.VERTICAL);
		LayoutParams panelControlParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 2);
		panelSimulator.addView(panelControl, panelControlParams);

		SeekBar scaleBar = createScaleBar();
		panelControl.addView(scaleBar);

		LinearLayout panelBackground = new LinearLayout(this);
		panelBackground.setOrientation(LinearLayout.HORIZONTAL);

		Button bgButton = createBGButton();
		panelBackground.addView(bgButton);

//		LinearLayout panelBackgroundCamera = new LinearLayout(this);
//		panelBackgroundCamera.setOrientation(LinearLayout.VERTICAL);
//		TextView textCamera = new TextView(this);
//		textCamera.setGravity(Gravity.CENTER_HORIZONTAL);
//		textCamera.setText("Camera");
//		textCamera.setTextSize(14f);
//		Button cameraButton = createCamera();
//		panelBackgroundCamera.addView(textCamera);
//		panelBackgroundCamera.addView(cameraButton);
//		panelBackground.addView(panelBackgroundCamera);

		panelControl.addView(panelBackground);

		touchLayout = new RelativeLayout(this);
		LayoutParams touchLayoutParams = new LinearLayout.LayoutParams(
				oraScreenCalcul.getTouchRect().width(), oraScreenCalcul
						.getTouchRect().height());
		touchLayout.setBackgroundColor(Color.LTGRAY);

		panelSimulator.addView(touchLayout, touchLayoutParams);

	}

	private Button createCamera() {
		// ToggleButton toggleButton = new ToggleButton(this);
		Switch switchButton = new Switch(this);
		switchButton.setPadding(1, 1, 1, 1);
		switchButton.setTextOff("Off");
		switchButton.setTextOn("On");

		switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			private boolean previewing;

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					SurfaceHolder surfaceHolder = worldCamera.getHolder();
					// surfaceHolder.addCallback(new CameraCallBack());
					worldCamera.setBackgroundResource(0);
					camera = Camera.open();
					if (previewing) {
						camera.stopPreview();
						previewing = false;
					}

					if (camera != null) {
						try {
							camera.setDisplayOrientation(getOrientationDegree());
							camera.setPreviewDisplay(surfaceHolder);
							camera.startPreview();
							previewing = true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					camera.stopPreview();
					camera.release();
					camera = null;
					previewing = false;
					worldCamera.setBackgroundColor(worldBackGroudColor);
				}

			}

			private int getOrientationDegree() {
				int rotation = getWindowManager().getDefaultDisplay()
						.getRotation();
				int degrees = 0;
				switch (rotation) {
				case Surface.ROTATION_0:
					degrees = 0;
					break;
				case Surface.ROTATION_90:
					degrees = 90;
					break;
				case Surface.ROTATION_180:
					degrees = 180;
					break;
				case Surface.ROTATION_270:
					degrees = 270;
					break;
				}

				int result;
				result = (90-degrees + 360) % 360;

				return result;
			}
		});

		return switchButton;
	}

	private Button createBGButton() {
		Button bgButton = new Button(this);
		bgButton.setText("BG");
		final ColorDialogListener bgColorListener = new ColorDialogListener() {
			@Override
			public void onSelectedColor(int color) {
				worldBackGroudColor = color;
				worldCamera.setBackgroundColor(color);
			}
		};

		bgButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ColorDialog(v.getContext(), bgColorListener,
						oraScreenCalcul.getRealSize()).show();
			}
		});
		return bgButton;
	}

	private SeekBar createScaleBar() {
		SeekBar scaleBar = new SeekBar(this);
		scaleBar.setMax((int) (oraScreenCalcul.getWorldScaleMax() * 10 - 10));
		scaleBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			private int progress;

			private void scale() {
				float scale = (float) ((progress + 10.0) / 10.0);
				oraLayout.setScaleX(oraScreenCalcul.getRealScale(scale));
				oraLayout.setScaleY(oraScreenCalcul.getRealScale(scale));
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				scale();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				this.progress = progress;
				scale();
			}
		});
		scaleBar.setProgress(scaleBar.getMax());
		return scaleBar;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		oraTouchDetector.onTouchEvent(event);
		return true;
	}

	public void addGlassListener(OraTouchListener glassListener) {
		oraTouchDetector.addGlassListener(glassListener);
	}

	@Override
	public void setContentView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(this);
		inflater.inflate(layoutResID, oraLayout);

	}

	@Override
	public void setContentView(View view) {
		if (oraLayout.getChildCount() > 1) {
			oraLayout.removeAllViews();
		}
		oraLayout.addView(view);
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		if (oraLayout.getChildCount() > 1) {
			oraLayout.removeAllViews();
		}
		oraLayout.addView(view, params);
	}

	private class CameraCallBack implements SurfaceHolder.Callback {

		private boolean previewing;

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

		}

	}
	
	public Point getScreenSize() {
		return new Point(oraScreenCalcul.getOraScreenWidth(), oraScreenCalcul.getOraScreenHeight());
	}
}
