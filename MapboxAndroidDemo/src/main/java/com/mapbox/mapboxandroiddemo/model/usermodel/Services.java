package com.mapbox.mapboxandroiddemo.model.usermodel;

/**
 * Created by LangstonSmith on 5/9/17.
 */

public class Services {
  private UserDirections directions;

  private Mapviews mapviews;

  private Geocodes geocodes;

  public UserDirections getDirections() {
    return directions;
  }

  public void setDirections(UserDirections directions) {
    this.directions = directions;
  }

  public Mapviews getMapviews() {
    return mapviews;
  }

  public void setMapviews(Mapviews mapviews) {
    this.mapviews = mapviews;
  }

  public Geocodes getGeocodes() {
    return geocodes;
  }

  public void setGeocodes(Geocodes geocodes) {
    this.geocodes = geocodes;
  }
}