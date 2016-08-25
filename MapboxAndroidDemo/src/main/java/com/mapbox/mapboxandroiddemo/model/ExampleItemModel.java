package com.mapbox.mapboxandroiddemo.model;

import android.content.Intent;

public class ExampleItemModel {
  // Just a model for the detailed item recycler

  public int title;
  public int description;
  public int imageUrl;
  public Intent activity;
  public boolean showNewIcon;


  public int getTitle() {
    return title;
  }

  public void setTitle(int title) {
    this.title = title;
  }

  public int getDescription() {
    return description;
  }

  public void setDescription(int description) {
    this.description = description;
  }

  public int getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(int imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Intent getActivity() {
    return activity;
  }

  public void setActivity(Intent activity) {
    this.activity = activity;
  }

  public boolean getShowNewIcon() {
    return showNewIcon;
  }

  public void setShowNewIcon(boolean showNewIcon) {
    this.showNewIcon = showNewIcon;
  }

  public ExampleItemModel(int title, int description, Intent activity, int imageUrl, boolean showNewIcon) {
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.activity = activity;
    this.showNewIcon = showNewIcon;
  }

  public ExampleItemModel(int title, int description, Intent activity, int imageUrl) {
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.activity = activity;
    this.showNewIcon = false;
  }
}