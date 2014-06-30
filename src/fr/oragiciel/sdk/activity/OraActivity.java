package fr.oragiciel.sdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.Space;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import fr.oragiciel.sdk.camera.CameraManager;
import fr.oragiciel.sdk.camera.SubDivisionProportion;
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

	private CameraManager cameraManager;
	private ToggleButton cameraButton;
	protected SubDivisionProportion subDivisionProportion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		oraTouchDetector = new OraTouchDetector(this);
		oraScreenCalcul = new OraScreenCalcul();
		cameraManager = new CameraManager(this);

		setActivitySetting();

		checkScreenSize();

		oraTouchDetector.setCaptureZone(oraScreenCalcul.getTouchRect());
		oraTouchDetector.setSimulator(oraScreenCalcul.isTouchSimulator());

		if (oraScreenCalcul.isTouchSimulator()) {
			createSimulator();
		} else {
			createOra();
		}

	}

	private void createOra() {
		worldLayout = new RelativeLayout(this);
		LayoutParams worldParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		super.setContentView(worldLayout, worldParams);

		LayoutParams surfaceParams = new LinearLayout.LayoutParams(200, 100);
		worldCamera = new SurfaceView(this);
		worldLayout.addView(worldCamera, surfaceParams);
		cameraManager.setSdkSurfaceView(worldCamera);
		subDivisionProportion = new SubDivisionProportion(0.3f, 0.31f, 0.3f, 0.34f);
		cameraManager.setSubDivisionProportion(subDivisionProportion);

		oraLayout = new RelativeLayout(this);
		oraLayout.setBackgroundColor(Color.BLACK);
		RelativeLayout.LayoutParams oraLayoutparams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		worldLayout.addView(oraLayout, oraLayoutparams);

	}

	private void createSimulator() {
		rootLayout = new LinearLayout(this);
		rootLayout.setOrientation(LinearLayout.HORIZONTAL);
		rootLayout.refreshDrawableState();
		LayoutParams rootLayoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		super.setContentView(rootLayout, rootLayoutParams);

		worldLayout = new RelativeLayout(this);
		// worldLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
		// worldLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
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
				oraScreenCalcul.getOraScreenWidth(),
				oraScreenCalcul.getOraScreenHeight());
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

		
		cameraButton = createCamera();
		panelBackground.addView(cameraButton,new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
//		panelBackground.addView(panelBackgroundCamera, new LinearLayout.LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

		panelControl.addView(panelBackground);

		touchLayout = new RelativeLayout(this);
		LayoutParams touchLayoutParams = new LinearLayout.LayoutParams(
				oraScreenCalcul.getTouchRect().width(), oraScreenCalcul
						.getTouchRect().height());
		touchLayout.setBackgroundColor(Color.LTGRAY);

		panelSimulator.addView(touchLayout, touchLayoutParams);

	}

	private ToggleButton createCamera() {
		ToggleButton cameraButton = new ToggleButton(this);
		// Switch switchButton = new Switch(this);
		// cameraButton.setPadding(1, 1, 1, 1);
		cameraButton.setText("Off");
		cameraButton.setTextOff("Off");
		cameraButton.setTextOn("On");

		cameraButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			private boolean previewing;

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					worldCamera.setBackgroundResource(0);
					cameraManager.startCamera();
					cameraManager.setSubDivisionProportion(subDivisionProportion);
				} else {
					cameraManager.stopCamera();
					worldCamera.setBackgroundColor(worldBackGroudColor);
				}

			}

		});

		return cameraButton;
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
				float realScale = oraScreenCalcul.getRealScale(scale);
				oraLayout.setScaleX(realScale);
				oraLayout.setScaleY(realScale);
				subDivisionProportion = new SubDivisionProportion((float) ((1-realScale)*0.5), (float) ((1-realScale)*0.5), realScale, realScale);
				cameraManager.setSubDivisionProportion(subDivisionProportion);
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
	protected void onResume() {
		super.onResume();
		cameraManager.setSdkSurfaceView(worldCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();
		cameraManager.stopCamera();
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

	public Point getScreenSize() {
		return new Point(oraScreenCalcul.getOraScreenWidth(),
				oraScreenCalcul.getOraScreenHeight());
	}

	public CameraManager getCameraManager() {
		return cameraManager;
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

	public void cameraStarted() {
		if (oraScreenCalcul.isTouchSimulator()) {
			worldCamera.setBackgroundResource(0);
			cameraButton.setChecked(true);			
		}
	}
	
	public void cameraStopped() {
		if (oraScreenCalcul.isTouchSimulator()) {
			worldCamera.setBackgroundColor(worldBackGroudColor);
			cameraButton.setChecked(false);
		}
	}

	public OraTouchDetector getOraTouchDetector() {
		return oraTouchDetector;
	}
	
}
