package fr.orasgiciel.sdk.touch;

import android.view.GestureDetector;
import android.view.MotionEvent;

class OraMoveDetector extends GestureDetector.SimpleOnGestureListener {

	private static final int X = 0;
	private static final int Y = 1;

	private static final int PRECISION = 25;
	private static final int PRECISION_INVERSE = -PRECISION;

	private OraTouchListener glassListener;
	private boolean simulator;

	private float startX;
	private float startY;

	public OraMoveDetector() {
		reset();
	}

	private void reset() {
		startX = -1;
		startY = -1;
	}

	public void addGlassListener(OraTouchListener glassListener) {
		this.glassListener = glassListener;
	}

    public void setSimulator(boolean simulator) {
		this.simulator = simulator;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		startX = e.getX();
		startY = e.getY();
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		reset();
		if (glassListener != null) {
			glassListener.onDoubleTouch();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		reset();
		if (glassListener != null) {
            glassListener.onLongPress();
        }
		super.onLongPress(e);
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		reset();
		if (glassListener != null) {
            glassListener.onTouch();
        }
		return true;
	}

	private boolean isHorizontalMove(float[] delta) {
		if (simulator) {
            return Math.abs(delta[X]) < Math.abs(delta[Y]);
		} else {
			return Math.abs(delta[X]) > Math.abs(delta[Y]);
		}
	}

	private boolean isVerticalMove(float[] delta) {
		return !isHorizontalMove(delta);
	}

	private boolean isMove() {
		return glassListener != null && startX != -1 && startY != -1;
	}

    private float horizontalDelta(float[] delta) {
        if (simulator) {
            return -delta[Y];
        } else {
            return delta[X];
        }
    }

	private float verticalDelta(float[] delta) {
        if (simulator) {
            return delta[X];
        } else {
            return delta[Y];
        }
	}

	private float[] delta(MotionEvent event) {
		float[] delta = new float[2];
		delta[X] = event.getX() - startX;
		delta[Y] = event.getY() - startY;
		return delta;
	}

	public boolean isMoveForward(MotionEvent event) {
		if (isMove()) {
			float[] delta = delta(event);

            return isHorizontalMove(delta)
                    && horizontalDelta(delta) > PRECISION;

        }
		return false;
	}

	public boolean isMoveBackward(MotionEvent event) {
		if (isMove()) {
			float[] delta = delta(event);

			return isHorizontalMove(delta)
					&& horizontalDelta(delta) < PRECISION_INVERSE;
		}
		return false;
	}

	public boolean isMoveUp(MotionEvent event) {
		if (isMove()) {
			float[] delta = delta(event);

			return isVerticalMove(delta)
                    && verticalDelta(delta) < PRECISION_INVERSE;
		}
		return false;
	}

    public boolean isMoveDown(MotionEvent event) {
		if (isMove()) {
			float[] delta = delta(event);

			return isVerticalMove(delta) && verticalDelta(delta) > PRECISION;
		}
		return false;
	}
}
