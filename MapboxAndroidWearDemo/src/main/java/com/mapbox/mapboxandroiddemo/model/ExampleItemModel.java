package com.mapbox.mapboxandroiddemo.model;

import android.content.Intent;

public class ExampleItemModel {

  public int title;
  public Intent activity;
  public int image;

  public int getTitle() {
    return title;
  }

  public void setTitle(int title) {
    this.title = title;
  }

  public int getImage() {
    return image;
  }

  public void setImage(int image) {
    this.image = image;
  }

  public Intent getActivity() {
    return activity;
  }

  public void setActivity(Intent activity) {
    this.activity = activity;
  }

  public ExampleItemModel(int title, int image, Intent activity) {
    this.title = title;
    this.image = image;
    this.activity = activity;
  }
}