package com.mapbox.mapboxandroiddemo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.MainActivity;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ExampleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<ExampleItemModel> dataSource;
  private Context context;

  public ExampleAdapter(Context context, List<ExampleItemModel> dataSource) {
    this.dataSource = dataSource;
    this.context = context;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    // create a new view
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
    View view1 = LayoutInflater.from(parent.getContext()).inflate(
      R.layout.layout_description_item, parent, false);
    View view2 = LayoutInflater.from(parent.getContext()).inflate(
      R.layout.layout_mas_description_card, parent, false);
    View view3 = LayoutInflater.from(parent.getContext()).inflate(
      R.layout.layout_query_description_card, parent, false);

    switch (viewType) {
      case 1:
        return new ViewHolderDescription(view1);
      case 2:
        return new ViewHolderDescription(view2);
      case 3:
        return new ViewHolderDescription(view3);
      default:
        return new ViewHolder(view);
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (((MainActivity) context).getCurrentCategory() == R.id.nav_lab && position == 0) {
      return 1;
    } else if (((MainActivity) context).getCurrentCategory() == R.id.nav_mas && position == 0) {
      return 2;
    } else if (((MainActivity) context).getCurrentCategory() == R.id.nav_query_map && position == 0) {
      return 3;
    }
    return 0;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (holder.getItemViewType() == 0) {
      ExampleItemModel detailItem = dataSource.get(position);
      ViewHolder viewHolder = (ViewHolder) holder;

      String imageUrl = context.getString(detailItem.getImageUrl());

      if (!imageUrl.isEmpty()) {
        Picasso.with(context)
          .load(imageUrl)
          .into(viewHolder.imageView);
      } else {
        viewHolder.imageView.setImageDrawable(null);
      }

      if (detailItem.getShowNewIcon()) {
        viewHolder.newIconImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.new_icon));
      } else {
        viewHolder.newIconImageView.setImageDrawable(null);
      }

      viewHolder.titleTextView.setText(context.getString(detailItem.getTitle()));
      viewHolder.descriptionTextView.setText(context.getString(detailItem.getDescription()));
    }
  }

  @Override
  public int getItemCount() {
    return (null != dataSource ? dataSource.size() : 0);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public TextView titleTextView;
    public TextView descriptionTextView;
    public ImageView imageView;
    public ImageView newIconImageView;

    public ViewHolder(final View itemView) {
      super(itemView);

      imageView = (ImageView) itemView.findViewById(R.id.example_image);
      titleTextView = (TextView) itemView.findViewById(R.id.example_title);
      descriptionTextView = (TextView) itemView.findViewById(R.id.example_description);
      newIconImageView = (ImageView) itemView.findViewById(R.id.new_icon_image_view);
    }
  }

  public static class ViewHolderDescription extends RecyclerView.ViewHolder {

    public ViewHolderDescription(final View itemView) {
      super(itemView);
    }
  }
}