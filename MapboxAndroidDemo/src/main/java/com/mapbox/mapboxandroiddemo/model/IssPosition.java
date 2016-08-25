package com.mapbox.mapboxandroiddemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssPosition {

  @SerializedName("latitude")
  @Expose
  private Double latitude;
  @SerializedName("longitude")
  @Expose
  private Double longitude;

  /**
   * @return The latitude
   */
  public Double getLatitude() {
    return latitude;
  }

  /**
   * @param latitude The latitude
   */
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   * @return The longitude
   */
  public Double getLongitude() {
    return longitude;
  }

  /**
   * @param longitude The longitude
   */
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

}