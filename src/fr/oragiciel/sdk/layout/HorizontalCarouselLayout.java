package fr.oragiciel.sdk.layout;


/*
 * Copyright (C) 2012 
 * Arindam Nath (strider2023@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import fr.oragiciel.sdk.touch.OraTouchDetector;
import fr.oragiciel.sdk.touch.OraTouchListener;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;

public class HorizontalCarouselLayout extends ViewGroup implements OraTouchListener {

	/* Scale ratio for each "layer" of children */
	private final float SCALE_RATIO = 0.9f;
	/* Gesture sensibility */
	private int mGestureSensitivity = 80;
	/* Animation time */
	private int DURATION = 200;

	/* Number of pixel between the top of two Views */
	private int mSpaceBetweenViews = 20;
	/* Rotation between two Views */
	private int mRotation;
	/* Status of rotation */
	private boolean mRotationEnabled = false;
	/* Tanslation between two Views */
	private int mTranslate;
	/* Status of translatation */
	private boolean mTranslatateEnbabled = false;
	/* Transparency of incative child view */
	private float mSetInactiveViewTransparency = 1.0f;

	/* Number of internal Views */
	private int mHowManyViews = 99;
	/* Size of internal Views */
	private float mChildSizeRatio = 0.6f;
	/* Adapter */
	private BaseAdapter mAdapter = null;
	/* Item index of center view */
	private int mCurrentItem = 0;
	/* Index of center view in the ViewGroup */
	private int mCenterView = 0;
	/* Width of all children */
	private int mChildrenWidth;
	/* Width / 2 */
	private int mChildrenWidthMiddle;
	/* Height of all children */
	private int mChildrenHeight;
	/* Height / 2 */
	private int mChildrenHeightMiddle;
	/* Height center of the ViewGroup */
	private int mHeightCenter;
	/* Width center of the ViewGroup */
	private int mWidthCenter;
	/* Number of view below/above center view */
	private int mMaxChildUnderCenter;
	/* Inactive child view zoom out factor */
	private float mViewZoomOutFactor = 0.0f;
	/* Inactive child view coverflow rotation */
	private int mCoverflowRotation = 0;
	/* Collect crap views */
	private Collector mCollector = new Collector();
	/* Avoid multiple allocation */
	private Matrix mMatrix = new Matrix();

	private Context mContext;

	/* Gap between fixed position (for animation) */
	private float mGap;
	/* is animating */
	private boolean mIsAnimating = false;
	/* Avoid multiple allocation */
	private long mCurTime;
	/* Animation start time */
	private long mStartTime;
	/* Final item to reach (for animation from mCurrentItem to mItemToReach) */
	private int mItemtoReach = 0;

	private CarouselInterface mCallback;
	
	private OraTouchDetector touchDetector;

	/**
	 * 
	 * @author Arindam Nath
	 *
	 */
	public interface CarouselInterface {
		public void onItemChangedListener(View v, int position);
	}

	/* Animation Task */
	private Runnable animationTask = new Runnable() {
		public void run() {
			mCurTime = SystemClock.uptimeMillis();
			long totalTime = mCurTime - mStartTime;
			// Animation end
			if (totalTime > DURATION) {
				// Add new views
				if (mItemtoReach > mCurrentItem) {
					fillBottom();
				} else {
					fillTop();
				}
				// Register value to stop animation
				mCurrentItem = mItemtoReach;
				mGap = 0;
				mIsAnimating = false;
				// Calculate the new center view in the ViewGroup
				mCenterView = mCurrentItem;
				if (mCurrentItem >= mMaxChildUnderCenter) {
					mCenterView = mMaxChildUnderCenter;
				}
				removeCallbacks(animationTask);
				mCallback.onItemChangedListener(mAdapter.getView(mCurrentItem, null, HorizontalCarouselLayout.this), mCurrentItem);
				// Animate
			} else {
				float perCent = ((float) totalTime) / DURATION;
				mGap = (mCurrentItem - mItemtoReach) * perCent;
				post(this);
			}
			// Layout children
			childrenLayout(mGap);
			invalidate();
		}
	};
	
	// ~--- constructors -------------------------------------------------------
	public HorizontalCarouselLayout(Context context) {
		super(context);
		mContext = context;
		initSlidingAnimation();
	}

	public HorizontalCarouselLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initSlidingAnimation();
	}

	public HorizontalCarouselLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initSlidingAnimation();
	}

	// ~--- set methods --------------------------------------------------------
	public void disableRotation() {
		mRotationEnabled = false;
	}

	public void disableTranslate() {
		mTranslatateEnbabled = false;
	}

	public void setOnCarouselViewChangedListener(CarouselInterface carouselInterface) {
		this.mCallback = carouselInterface;
	}

	/**
	 * @param gestureSensitivity
	 *            the mAJOR_MOVE to set
	 */
	public void setGestureSensitivity(int gestureSensitivity) {
		mGestureSensitivity = gestureSensitivity;
	}

	public void setStyle(HorizontalCarouselStyle style) {
		mSetInactiveViewTransparency = style.getInactiveViewTransparency();
		mSpaceBetweenViews = style.getSpaceBetweenViews();
		mRotation = style.getRotation();
		mRotationEnabled = style.isRotationEnabled();
		mTranslate = style.getTranslate();
		mTranslatateEnbabled = style.isTranslatateEnbabled();
		mHowManyViews = style.getHowManyViews();
		mChildSizeRatio = style.getChildSizeRatio();
		mCoverflowRotation = style.getCoverflowRotation();
		mViewZoomOutFactor = style.getViewZoomOutFactor();
		DURATION = style.getAnimationTime();
	}

	/**
	 * Set adapter
	 * @param adapter
	 */
	public void setAdapter(BaseAdapter adapter) {
		if (adapter != null) {
			mAdapter = adapter;
			mCenterView = mCurrentItem = 0;
			// even
			if ((mHowManyViews % 2) == 0) {
				// TODO : Fix it (for the moment work only with odd
				// mHowManyViews)
				mMaxChildUnderCenter = (mHowManyViews / 2);
				// odd
			} else {
				mMaxChildUnderCenter = (mHowManyViews / 2);
			}
			// Populate the ViewGroup
			for (int i = 0; i <= mMaxChildUnderCenter; i++) {
				if (i > (mAdapter.getCount() - 1)) {
					break;
				}
				final View v = mAdapter.getView(i, null, this);
				addView(v);
			}
			childrenLayout(0);
			invalidate();
		}
	}
	
	public void setTouchDetector(OraTouchDetector touchDetector) {
		this.touchDetector = touchDetector;
		this.touchDetector.addGlassListener(this);
	}

	// ~--- methods ------------------------------------------------------------
	/* fillTop if required and garbage old views out of screen */
	private void fillTop() {
		// Local (below center): too many children
		if (mCenterView < mMaxChildUnderCenter) {
			if (getChildCount() > mMaxChildUnderCenter + 1) {
				View old = getChildAt(getChildCount() - 1);
				detachViewFromParent(old);
				mCollector.collect(old);
			}
		}
		// Global : too many children
		if (getChildCount() >= mHowManyViews) {
			View old = getChildAt(mHowManyViews - 1);
			detachViewFromParent(old);
			mCollector.collect(old);
		}
		final int indexToRequest = mCurrentItem - (mMaxChildUnderCenter + 1);
		// retrieve if required
		if (indexToRequest >= 0) {
			Log.v("UITEST", "Fill top with " + indexToRequest);
			View recycled = mCollector.retrieve();
			View v = mAdapter.getView(indexToRequest, recycled, this);
			if (recycled != null) {
				attachViewToParent(v, 0, generateDefaultLayoutParams());
				v.measure(mChildrenWidth, mChildrenHeight);
			} else {
				addView(v, 0);
			}
		}
	}

	/* fillBottom if required and garbage old views out of screen */
	private void fillBottom() {
		// Local (above center): too many children
		if (mCenterView >= mMaxChildUnderCenter) {
			View old = getChildAt(0);
			detachViewFromParent(old);
			mCollector.collect(old);
		}
		// Global : too many children
		if (getChildCount() >= mHowManyViews) {
			View old = getChildAt(0);
			detachViewFromParent(old);
			mCollector.collect(old);
		}
		final int indexToRequest = mCurrentItem + (mMaxChildUnderCenter + 1);
		if (indexToRequest < mAdapter.getCount()) {
			Log.v("UITEST", "Fill bottom with " + indexToRequest);
			View recycled = mCollector.retrieve();
			View v = mAdapter.getView(indexToRequest, recycled, this);
			if (recycled != null) {
				Log.v("UITEST", "view attached");
				attachViewToParent(v, -1, generateDefaultLayoutParams());
				v.measure(mChildrenWidth, mChildrenHeight);
			} else {
				Log.v("UITEST", "view added");
				addView(v, -1);
			}
		}
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	private void initSlidingAnimation() {
		setChildrenDrawingOrderEnabled(true);
		setStaticTransformationsEnabled(true);
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				touchDetector.onTouchEvent(event);
				return true;
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int count = getChildCount();
		final int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
		final int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
		mWidthCenter = specWidthSize / 2;
		mHeightCenter = specHeightSize / 2;
		mChildrenWidth = (int) (specWidthSize * mChildSizeRatio);
		mChildrenHeight = (int) (specHeightSize * mChildSizeRatio);
		mChildrenWidthMiddle = mChildrenWidth / 2;
		mChildrenHeightMiddle = mChildrenHeight / 2;
		// Measure all children
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			measureChild(child, mChildrenWidth, mChildrenHeight);
		}
		setMeasuredDimension(specWidthSize, specHeightSize);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		childrenLayout(0);
	}

	/* Fix position of all children */
	private void childrenLayout(float gap) {
		final int leftCenterView = mWidthCenter - (mChildrenWidth / 2);
		final int topCenterView = mHeightCenter - (mChildrenHeight / 2);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			final float offset = mCenterView - i - gap;
			final int left = (int) (leftCenterView - (mSpaceBetweenViews * offset));
			child.layout(left, topCenterView, left + mChildrenWidth,
					topCenterView + mChildrenHeight);
		}
	}

	// ~--- get methods --------------------------------------------------------
	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int centerView = mCenterView;
		if (mGap > 0.5f) {
			centerView--;
		} else if (mGap < -0.5f) {
			centerView++;
		}
		// before center view
		if (i < centerView) {
			return i;
			// after center view
		} else if (i > centerView) {
			return centerView + (childCount - 1) - i;
			// center view
		} else {
			return childCount - 1;
		}
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final Camera camera = new Camera();
		final int leftCenterView = mWidthCenter - mChildrenWidthMiddle;
		final float offset = (-child.getLeft() + leftCenterView)
				/ (float) mSpaceBetweenViews;
		if (offset != 0) {
			final float absOffset = Math.abs(offset);
			float scale = (float) Math.pow(SCALE_RATIO, absOffset);
			t.clear();
			t.setTransformationType(Transformation.TYPE_MATRIX);
			t.setAlpha(mSetInactiveViewTransparency);
			final Matrix m = t.getMatrix();
			m.setScale(scale, scale);
			if (mTranslatateEnbabled) {
				m.setTranslate(0, mTranslate * absOffset);
			}
			// scale from right
			if (offset > 0) {
				camera.save();
				camera.translate(0.0f, 0.0f, (mViewZoomOutFactor*offset));
				camera.rotateY(mCoverflowRotation);
				camera.getMatrix(m);
				camera.restore();
				m.preTranslate(-mChildrenWidthMiddle, -mChildrenHeight);
				m.postTranslate(mChildrenWidthMiddle, mChildrenHeight);
				// scale from left
			} else {
				camera.save();
				camera.translate(0.0f, 0.0f, -(mViewZoomOutFactor*offset));
				camera.rotateY(-mCoverflowRotation);
				camera.getMatrix(m);
				camera.restore();
				m.preTranslate(-mChildrenWidthMiddle, -mChildrenHeight);
				m.postTranslate(mChildrenWidthMiddle, mChildrenHeight);
			}
			mMatrix.reset();
			if (mRotationEnabled) {
				mMatrix.setRotate(mRotation * offset);
			}
			mMatrix.preTranslate(-mChildrenWidthMiddle, -mChildrenHeightMiddle);
			mMatrix.postTranslate(mChildrenWidthMiddle, mChildrenHeightMiddle);
			m.setConcat(m, mMatrix);
		}
		return true;
	}

	@Override
	public void onTouch() {
		this.callOnClick();
	}

	@Override
	public void onDoubleTouch() {
	}

	@Override
	public void onLongPress() {
	}

	@Override
	public void onMoveUp() {
	}

	@Override
	public void onMoveDown() {
	}

	@Override
	public void onMoveForward() {
		if (mCurrentItem < (mAdapter.getCount() - 1)) {
			mItemtoReach = mCurrentItem + 1;
			mStartTime = SystemClock.uptimeMillis();
			mIsAnimating = true;
			post(animationTask);
		}
	}

	@Override
	public void onMoveBackward() {
		if (mCurrentItem > 0) {
			mItemtoReach = mCurrentItem - 1;
			mStartTime = SystemClock.uptimeMillis();
			mIsAnimating = true;
			post(animationTask);
		}
	}	
}