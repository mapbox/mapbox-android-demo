package com.mapbox.mapboxandroiddemo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IssModel {

  @SerializedName("iss_position")
  @Expose
  private IssPosition issPosition;
  @SerializedName("message")
  @Expose
  private String message;
  @SerializedName("timestamp")
  @Expose
  private Integer timestamp;

  /**
   * @return The issPosition
   */
  public IssPosition getIssPosition() {
    return issPosition;
  }

  /**
   * @param issPosition The iss_position
   */
  public void setIssPosition(IssPosition issPosition) {
    this.issPosition = issPosition;
  }

  /**
   * @return The message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message The message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @return The timestamp
   */
  public Integer getTimestamp() {
    return timestamp;
  }

  /**
   * @param timestamp The timestamp
   */
  public void setTimestamp(Integer timestamp) {
    this.timestamp = timestamp;
  }

}
