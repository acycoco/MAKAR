package com.example.makar.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.route.OnRouteClickListener;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private OnRouteClickListener listener;
    private List<Route> items;
    private Context context;
    private Route route;

    public RouteAdapter(Context context, List<Route> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnRouteClickListener(OnRouteClickListener listener) {
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        RouteRecyclerViewItemBinding binding = RouteRecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    public void setRoutes(List<Route> routes) {
        this.items = routes;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        route = items.get(position);

        int totalTime = route.getTotalTime();

        holder.binding.totalTimeTextView.setText(totalTime);
        holder.binding.routeRecyclerViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRouteClick(items.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RouteRecyclerViewItemBinding binding;

        public ViewHolder(RouteRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
