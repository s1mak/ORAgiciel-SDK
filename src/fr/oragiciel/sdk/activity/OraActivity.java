package fr.oragiciel.sdk.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import fr.oragiciel.sdk.touch.OraTouchDetector;
import fr.oragiciel.sdk.touch.OraTouchListener;

public class OraActivity extends Activity {

	private LinearLayout rootLayout;
	private LinearLayout worldLayout;
	private RelativeLayout oraLayout;
	private RelativeLayout touchLayout;

    private int worldBackGroudColor = Color.GREEN;

	private OraTouchDetector oraTouchDetector;
	private OraScreenCalcul oraScreenCalcul;

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

		worldLayout = new LinearLayout(this);
		worldLayout.setBackgroundColor(worldBackGroudColor);
        worldLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        worldLayout.setVerticalGravity(Gravity.CENTER_VERTICAL);
		LayoutParams worldParams = new LinearLayout.LayoutParams(
				oraScreenCalcul.getWorldWidth(), LayoutParams.MATCH_PARENT);
		rootLayout.addView(worldLayout, worldParams);

		oraLayout = new RelativeLayout(this);
        oraLayout.setBackgroundColor(Color.TRANSPARENT);
		RelativeLayout.LayoutParams oraLayoutparams = new RelativeLayout.LayoutParams(
				640, 480);
		worldLayout.addView(oraLayout, oraLayoutparams);

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

        SeekBar scaleBar = new SeekBar(this);
        scaleBar.setMax((int) (oraScreenCalcul.getWorldScaleMax() * 10 - 10));
        scaleBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            private int progress;

            private void scale() {
                float scale = (float) ((progress + 10.0) / 10.0);
                oraLayout.setScaleX(scale);
                oraLayout.setScaleY(scale);
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
        panelControl.addView(scaleBar);

		touchLayout = new RelativeLayout(this);
        LayoutParams touchLayoutParams = new LinearLayout.LayoutParams(
                oraScreenCalcul.getTouchRect().width(), oraScreenCalcul
                .getTouchRect().height());
        touchLayout.setBackgroundColor(Color.LTGRAY);

        panelSimulator.addView(touchLayout, touchLayoutParams);

    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// super.onTouchEvent(event);

		oraTouchDetector.onTouchEvent(event);
		return false;
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

}
