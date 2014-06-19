package fr.oragiciel.sdk.color;

public interface ColorDialogListener {

	/**
	 * Call when color is selected.
	 * @param color : color in RGB.
	 */
	void onSelectedColor(int color);
}
