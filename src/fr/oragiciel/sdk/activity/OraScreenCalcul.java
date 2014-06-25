package fr.oragiciel.sdk.activity;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

public class OraScreenCalcul {

	private static final float ORA_DPI = 213.0f;
	
	private DisplayMetrics displayMetrics;
	private Point realSize;

	private int worldWidth;
	private float worldScaleMax;
	private boolean touchSimulator;
	private Rect touchRect;
	private int simulatorWidth;
	private int simulatorPadding;
	
	private int oraScreenWidth;
	private int oraScreenHeight;
	private float ratioDpi;

	public void setScreenMetrics(DisplayMetrics displayMetrics, Point realSize) {
		this.displayMetrics = displayMetrics;
		this.realSize = realSize;

		ratioDpi = displayMetrics.densityDpi / ORA_DPI;
		checkScreen();
	}

	private void checkScreen() {
		int screenHeight = realSize.y;
		int screenWidth = realSize.x;

		if (screenHeight < 440 || screenWidth < 640) {
			throw new RuntimeException(
					"Screen Resolution Invalid ! Minimum 640x480");
		} else if (screenHeight < 480 && screenWidth == 640) {
			touchSimulator = false;
			touchRect = new Rect(0, 0, 640, screenHeight);
		} else if (screenHeight == 480 && screenWidth == 640) {
			touchSimulator = false;
			touchRect = new Rect(0, 0, 640, 480);
		} else {
			touchSimulator = true;

			performScreen();
		}
	}

	private void performScreen() {
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        if (width < 764) { // 764 = 640 visu + 120 touch + 2*2 padding
            int touchWidth = width - 640;
            int touchHeight = 480 * touchWidth / 640;
            touchRect = new Rect(640, 0, 640 + touchWidth, 0 + touchHeight);
            simulatorPadding = 0;
            worldScaleMax = 1;

            oraScreenWidth = 640;
            oraScreenHeight = 480; 
        } else {

            int simulatorRatio = (int) Math.ceil(1 / (width - 640.0) / 496.0);
            simulatorWidth = (int) Math.ceil(496.0 / simulatorRatio);
            simulatorPadding = (int) Math.ceil(8.0 / simulatorRatio);
            worldWidth = width - simulatorWidth;

            int touchLeft = worldWidth + simulatorPadding;
            int touchTop = height - simulatorPadding - 640 / simulatorRatio;
            touchRect = new Rect(touchLeft, touchTop, touchLeft + 480
                    / simulatorRatio, touchTop + 640 / simulatorRatio);

            double tmpScaleMax = Math.min(worldWidth / 640.0, height / 480.0);
            worldScaleMax = (float) (Math.floor(tmpScaleMax * 10) / 10.0);   


            oraScreenWidth = (int) Math.round(640 * ratioDpi);
//            if (oraScreenWidth > worldWidth) {
//            	ratioDpi = worldWidth / 640;
//                oraScreenWidth = (int) Math.round(640 * ratioDpi);
//            } 
            
            oraScreenHeight = (int) Math.round(480 * ratioDpi);           
            
        }
    }
	
	/**
	 * Retourne le scale réel en fonction des DPI par rapport au scale désiré qui va de 1 à worldScaleMax.
	 * @param scale : scale désiré
	 * @return scale réel
	 */
	public float getRealScale(float scale) {
		return scale / ratioDpi;
	}

    public int getWorldWidth() {
        return worldWidth;
    }

    public float getWorldScaleMax() {
        return worldScaleMax;
    }

    public boolean isTouchSimulator() {
        return touchSimulator;
    }

    public Rect getTouchRect() {
        return touchRect;
    }

    public int getSimulatorWidth() {
        return simulatorWidth;
    }

    public int getSimulatorPadding() {
        return simulatorPadding;
    }

	public DisplayMetrics getDisplayMetrics() {
		return displayMetrics;
	}

	public Point getRealSize() {
		return realSize;
	}

	public int getOraScreenWidth() {
		return oraScreenWidth;
	}

	public int getOraScreenHeight() {
		return oraScreenHeight;
	}

	
	
}
