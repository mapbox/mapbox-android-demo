package com.mapbox.mapboxandroiddemo.utils;

import static androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;

public class ItemClickSupport {
  private final RecyclerView recyclerView;
  private OnItemClickListener onItemClickListener;
  private OnItemLongClickListener onItemLongClickListener;
  private View.OnClickListener onClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      if (onItemClickListener != null) {
        ViewHolder holder = recyclerView.getChildViewHolder(view);
        onItemClickListener.onItemClicked(recyclerView, holder.getAdapterPosition(), view);
      }
    }
  };
  private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
      if (onItemLongClickListener != null) {
        ViewHolder holder = recyclerView.getChildViewHolder(view);
        return onItemLongClickListener.onItemLongClicked(recyclerView, holder.getAdapterPosition(), view);
      }
      return false;
    }
  };
  private OnChildAttachStateChangeListener attachListener = new OnChildAttachStateChangeListener() {
    @Override
    public void onChildViewAttachedToWindow(View view) {
      if (onItemClickListener != null) {
        view.setOnClickListener(onClickListener);
      }
      if (onItemLongClickListener != null) {
        view.setOnLongClickListener(onLongClickListener);
      }
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {
    }
  };

  private ItemClickSupport(RecyclerView recyclerView) {
    this.recyclerView = recyclerView;
    this.recyclerView.setTag(R.id.item_click_support, this);
    this.recyclerView.addOnChildAttachStateChangeListener(attachListener);
  }

  public static ItemClickSupport addTo(RecyclerView view) {
    ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
    if (support == null) {
      support = new ItemClickSupport(view);
    }
    return support;
  }

  public static ItemClickSupport removeFrom(RecyclerView view) {
    ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
    if (support != null) {
      support.detach(view);
    }
    return support;
  }

  public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
    onItemClickListener = listener;
    return this;
  }

  public ItemClickSupport setOnItemLongClickListener(OnItemLongClickListener listener) {
    onItemLongClickListener = listener;
    return this;
  }

  private void detach(RecyclerView view) {
    view.removeOnChildAttachStateChangeListener(attachListener);
    view.setTag(R.id.item_click_support, null);
  }

  public interface OnItemClickListener {

    void onItemClicked(RecyclerView recyclerView, int position, View view);
  }

  public interface OnItemLongClickListener {

    boolean onItemLongClicked(RecyclerView recyclerView, int position, View view);
  }
}
