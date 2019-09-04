package com.mapbox.mapboxandroiddemo.utils;

import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;

public class OffsettingHelper extends DefaultOffsettingHelper {

  /**
   * How much should we scale the icon at most.
   */
  private static final float MAX_ICON_PROGRESS = 0.65f;


  public OffsettingHelper() {
  }

  @Override
  public void updateChild(View child, WearableRecyclerView parent) {
    super.updateChild(child, parent);


    // Figure out % progress from top to bottom
    float centerOffset = ((float) child.getHeight() / 2.0f) /  (float) parent.getHeight();
    float verticalRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

    // Normalize for center
    float progressToCenter = Math.abs(0.5f - verticalRelativeToCenterOffset);
    // Adjust to the maximum scale
    progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);

    child.setScaleX(1 - progressToCenter);
    child.setScaleY(1 - progressToCenter);

  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Override
  public void adjustAnchorOffsetXY(View child, float[] anchorOffsetXY) {
    anchorOffsetXY[0] = child.getHeight() / 2.0f;
  }
}


