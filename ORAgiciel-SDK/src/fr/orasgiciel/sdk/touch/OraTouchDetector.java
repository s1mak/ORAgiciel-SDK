package fr.orasgiciel.sdk.touch;

import android.app.Activity;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class OraTouchDetector {

	private GestureDetector gestureDetector;
    private OraMoveDetector oraMoveDetector;
    private OraTouchListener oraListener;
	private Rect caputeZone;

    public void addGlassListener(OraTouchListener glassListener) {
        this.oraListener = glassListener;
        oraMoveDetector.addGlassListener(glassListener);
    }

    public OraTouchDetector(Activity activity) {
        oraMoveDetector = new OraMoveDetector();
        gestureDetector = new GestureDetector(activity, oraMoveDetector);
    }

    public void onTouchEvent(MotionEvent event) {
		if (oraListener != null
                && caputeZone.contains((int) event.getX(), (int) event.getY())) {
			gestureDetector.onTouchEvent(event);

			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (oraMoveDetector.isMoveUp(event)) {
					oraListener.onMoveUp();
				} else if (oraMoveDetector.isMoveDown(event)) {
					oraListener.onMoveDown();
				} else if (oraMoveDetector.isMoveForward(event)) {
					oraListener.onMoveForward();
				} else if (oraMoveDetector.isMoveBackward(event)) {
					oraListener.onMoveBackward();
				}
			}
		}
    }

    public void setCaputeZone(Rect caputeZone) {
		this.caputeZone = caputeZone;
    }

    public void setSimulator(boolean simulator) {
        oraMoveDetector.setSimulator(simulator);
    }
}
