package fr.oragiciel.sdk.activity;

import android.os.Bundle;
import fr.oragiciel.sdk.touch.OraTouchListener;

public class OraSimpleTouchActivity extends OraActivity implements
		OraTouchListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addGlassListener(this);
	}

	@Override
	public void onTouch() {
	}

	@Override
	public void onLongPress() {
	}

	@Override
	public void onDoubleTouch() {
	}

	@Override
	public void onMoveUp() {
	}

	@Override
	public void onMoveDown() {
	}

	@Override
	public void onMoveForward() {
	}

	@Override
	public void onMoveBackward() {
	}

}
