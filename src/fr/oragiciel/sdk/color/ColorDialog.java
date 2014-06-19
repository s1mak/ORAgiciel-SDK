package fr.oragiciel.sdk.color;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;

public class ColorDialog extends Dialog {

	private GridLayout gridLayout;
	private int margin = 5;
	private int size = 60;
	private int maxColumn = 13;
	private int maxRowGradient = 7;
	private ColorDialogListener colorDialogListener;

	public ColorDialog(Context context,
			ColorDialogListener colorDialogListener, Point screenSize) {
		super(context);
		this.colorDialogListener = colorDialogListener;

		int sizeX = (screenSize.x - margin * 2 * (maxColumn - 1) - 50) / maxColumn;
		int sizeY = (screenSize.y - margin * 2 * (maxRowGradient + 1) - 50)
				/ (maxRowGradient + 2);
		size = Math.min(sizeX, sizeY);

	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		gridLayout = new GridLayout(getContext());
		gridLayout.setColumnCount(maxColumn);
		gridLayout.setRowCount(maxRowGradient + 2);

		gridLayout.setOrientation(GridLayout.VERTICAL);

		addLongButton(Color.WHITE);
		addGradientButton(Color.rgb(127, 127, 127));
		addLongButton(Color.BLACK);

		addGradientButton(Color.rgb(255, 0, 0));
		addGradientButton(Color.rgb(255, 0, 127));
		addGradientButton(Color.rgb(255, 0, 255));
		addGradientButton(Color.rgb(127, 0, 255));
		addGradientButton(Color.rgb(0, 0, 255));
		addGradientButton(Color.rgb(0, 127, 255));
		addGradientButton(Color.rgb(0, 255, 255));
		addGradientButton(Color.rgb(0, 255, 127));
		addGradientButton(Color.rgb(0, 255, 0));
		addGradientButton(Color.rgb(127, 255, 0));
		addGradientButton(Color.rgb(255, 255, 0));
		addGradientButton(Color.rgb(255, 127, 0));

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(5, 5);
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		setContentView(gridLayout, params);
	}

	private void addGradientButton(int rgb) {
		int initRed = Color.red(rgb);
		int initGreen = Color.green(rgb);
		int initBlue = Color.blue(rgb);
		int red = 0;
		int green = 0;
		int blue = 0;
		int nbLightGradient = (maxRowGradient - 1) / 2;
		int nbDarkGradient = maxRowGradient - 1 - nbLightGradient;

		for (int i = 0; i < nbLightGradient; i++) {
			if (initRed == 0) {
				red = 255 - (255 / (nbLightGradient + 1)) * (i + 1);
			} else if (initRed == 127) {
				red = 255 - (127 / (nbLightGradient + 1)) * (i + 1);
			} else {
				red = initRed;
			}
			if (initGreen == 0) {
				green = 255 - (255 / (nbLightGradient + 1)) * (i + 1);
			} else if (initGreen == 127) {
				green = 255 - (127 / (nbLightGradient + 1)) * (i + 1);
			} else {
				green = initGreen;
			}
			if (initBlue == 0) {
				blue = 255 - (255 / (nbLightGradient + 1)) * (i + 1);
			} else if (initBlue == 127) {
				blue = 255 - (127 / (nbLightGradient + 1)) * (i + 1);
			} else {
				blue = initBlue;
			}
			addButton(Color.rgb(red, green, blue));
		}
		addButton(rgb);
		for (int i = 0; i < nbDarkGradient; i++) {
			if (initRed == 255) {
				red = 255 - (255 / (nbDarkGradient + 1)) * (i + 1);
			} else if (initRed == 127) {
				red = 127 - (127 / (nbDarkGradient + 1)) * (i + 1);
			} else {
				red = initRed;
			}
			if (initGreen == 255) {
				green = 255 - (255 / (nbDarkGradient + 1)) * (i + 1);
			} else if (initGreen == 127) {
				green = 127 - (127 / (nbDarkGradient + 1)) * (i + 1);
			} else {
				green = initGreen;
			}
			if (initBlue == 255) {
				blue = 255 - (255 / (nbDarkGradient + 1)) * (i + 1);
			} else if (initBlue == 127) {
				blue = 127 - (127 / (nbDarkGradient + 1)) * (i + 1);
			} else {
				blue = initBlue;
			}
			addButton(Color.rgb(red, green, blue));
		}
	}

	private void addLongButton(int color) {
		GridLayout.LayoutParams minusButton = new GridLayout.LayoutParams();

		minusButton.height = size;
		minusButton.width = size * maxColumn + margin * 2 * (maxColumn - 1);
		minusButton.leftMargin = margin;
		minusButton.rightMargin = margin;
		minusButton.topMargin = margin;
		minusButton.bottomMargin = margin;

		minusButton.columnSpec = GridLayout.spec(0, maxColumn);

		Button button = new Button(getContext());
		button.setBackgroundColor(color);
		button.setOnClickListener(new ColorListener());

		gridLayout.addView(button, minusButton);

	}

	private void addButton(int color) {
		GridLayout.LayoutParams minusButton = new GridLayout.LayoutParams();
		minusButton.height = size;
		minusButton.width = size;
		minusButton.leftMargin = margin;
		minusButton.rightMargin = margin;
		minusButton.topMargin = margin;
		minusButton.bottomMargin = margin;

		Button button = new Button(getContext());
		button.setBackgroundColor(color);
		button.setOnClickListener(new ColorListener());

		gridLayout.addView(button, minusButton);
	}

	private class ColorListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			Button button = (Button) v;
			ColorDrawable colorDrawable = (ColorDrawable) button
					.getBackground();
			colorDialogListener.onSelectedColor(colorDrawable.getColor());
			dismiss();
		}

	}
}
