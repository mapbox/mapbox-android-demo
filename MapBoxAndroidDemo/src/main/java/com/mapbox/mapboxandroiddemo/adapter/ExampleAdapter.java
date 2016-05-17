package com.mapbox.mapboxandroiddemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ViewHolder> {

    private List<ExampleItemModel> dataSource;
    private Context mContext;

    public ExampleAdapter(Context context, List<ExampleItemModel> dataSource) {
        this.dataSource = dataSource;
        this.mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExampleItemModel detailItem = dataSource.get(position);

        if (detailItem.getImageUrl() != null) {
            Picasso.with(mContext)
                    .load(detailItem.getImageUrl())
                    .into(holder.imageView);
        }

        holder.titleTextView.setText(detailItem.getTitle());
        holder.descriptionTextView.setText(detailItem.getDescription());

    }

    @Override
    public int getItemCount() {
        return (null != dataSource ? dataSource.size() : 0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView imageView;

        public ViewHolder(final View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.example_image);
            titleTextView = (TextView) itemView.findViewById(R.id.example_title);
            descriptionTextView = (TextView) itemView.findViewById(R.id.example_description);
        }
    }
}