package com.mapbox.mapboxandroiddemo.adapter;

import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ExampleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final List<ExampleItemModel> dataSource = new ArrayList<>();
  private final Context context;
  private int viewType;

  public ExampleAdapter(Context context) {
    this.context = context;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    // create a new view
    View view;
    switch (viewType) {
      case 1:
        view = LayoutInflater.from(parent.getContext()).inflate(
          R.layout.layout_description_item, parent, false);
        break;
      case 2:
        view = LayoutInflater.from(parent.getContext()).inflate(
          R.layout.layout_javaservices_description_card, parent, false);
        break;
      case 3:
        view = LayoutInflater.from(parent.getContext()).inflate(
          R.layout.layout_query_description_card, parent, false);
        break;
      default:
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
    }

    return new ViewHolder(view);
  }

  @Override
  public int getItemViewType(int position) {
    return position == 0 ? viewType : 0;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder.getItemViewType() != 0) {
      return;
    }

    ExampleItemModel detailItem = dataSource.get(position);
    ViewHolder viewHolder = (ViewHolder) holder;

    String imageUrl = context.getString(detailItem.getImageUrl());

    if (imageUrl.isEmpty()) {
      viewHolder.imageView.setImageDrawable(null);
    } else {
      Picasso.with(context)
        .load(imageUrl)
        .into(viewHolder.imageView);
    }

    if (detailItem.getShowNewIcon()) {
      viewHolder.newIconImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.new_icon));
    } else {
      viewHolder.newIconImageView.setImageDrawable(null);
    }

    viewHolder.titleTextView.setText(context.getString(detailItem.getTitle()));
    viewHolder.descriptionTextView.setText(context.getString(detailItem.getDescription()));
  }

  @Override
  public int getItemCount() {
    return dataSource.size();
  }

  public void updateDataSet(List<ExampleItemModel> examples, @IdRes int categoryId) {
    dataSource.clear();
    dataSource.addAll(examples);

    viewType = getViewType(categoryId);
    if (viewType > 0) {
      dataSource.add(0, null);
    }

    notifyDataSetChanged();
  }

  public ExampleItemModel getItemAt(int position) {
    if (position < 0 || position >= dataSource.size()) {
      return null;
    }
    return dataSource.get(position);
  }

  private int getViewType(@IdRes int categoryId) {
    if (categoryId == R.id.nav_lab) {
      return 1;
    } else if (categoryId == R.id.nav_java_services) {
      return 2;
    } else if (categoryId == R.id.nav_query_map) {
      return 3;
    }

    return 0;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    TextView titleTextView;
    TextView descriptionTextView;
    ImageView imageView;
    ImageView newIconImageView;

    ViewHolder(final View itemView) {
      super(itemView);

      imageView = itemView.findViewById(R.id.example_image);
      titleTextView = itemView.findViewById(R.id.example_title);
      descriptionTextView = itemView.findViewById(R.id.example_description);
      newIconImageView = itemView.findViewById(R.id.new_icon_image_view);
    }
  }
}