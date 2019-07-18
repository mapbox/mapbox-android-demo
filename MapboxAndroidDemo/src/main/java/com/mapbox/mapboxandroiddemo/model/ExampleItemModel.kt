package com.mapbox.mapboxandroiddemo.model

import android.content.Intent

// Just a model for the detailed item recycler
class ExampleItemModel(
  val categoryId: Int,
  val title: Int,
  val description: Int,
  val javaActivity: Intent?,
  val kotlinActivity: Intent?,
  val imageUrl: Int,
  val showNewIcon: Boolean,
  val minSdkVersion: Int
)