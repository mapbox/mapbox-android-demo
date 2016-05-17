package com.mapbox.mapboxandroiddemo.model;

import android.content.Context;
import android.content.Intent;

public class ExampleItemModel {
    // Just a model for the detailed item recycler

    public String title;
    public String description;
    public String imageUrl;
    public Intent activity;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Intent getActivity(){
        return activity;
    }

    public void setActivity(Intent activity){
        this.activity = activity;
    }

    public ExampleItemModel(String title, String description, Intent activity, String imageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.activity = activity;
    }
}