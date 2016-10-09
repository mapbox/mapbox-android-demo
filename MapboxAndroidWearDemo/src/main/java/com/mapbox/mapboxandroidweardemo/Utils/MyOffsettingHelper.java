package com.mapbox.mapboxandroidweardemo.utils;

import android.support.wearable.view.DefaultOffsettingHelper;
import android.view.View;

public class MyOffsettingHelper extends DefaultOffsettingHelper {

  private static final float MAX_ICON_PROGRESS = 0.65f;

  private float progressToCenter;

  public MyOffsettingHelper() {}

  @Override
  public void updateChild(View child, int layoutHeight, int layoutWidth) {
    super.updateChild(child, layoutHeight, layoutWidth);

    // Figure out % progress from top to bottom
    float centerOffset = ((float) child.getHeight() / 2.0f) /  (float) mParentView.getHeight();
    float yRelativeToCenterOffset = (child.getY() / mParentView.getHeight()) + centerOffset;

    // Normalize for center
    progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
    // Adjust to the maximum scale
    progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);

    child.setScaleX(1 - progressToCenter);
    child.setScaleY(1 - progressToCenter);
  }

  @Override
  protected void adjustAnchorOffsetXY(View child, float[] anchorOffsetXY) {
    anchorOffsetXY[0] = child.getHeight() / 2.0f;
  }

}