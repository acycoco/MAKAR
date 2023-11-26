package com.example.makar.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.databinding.ActivityMainBinding;
import com.example.makar.route.OnItemClickListener;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {
    private OnItemClickListener listener;

    private Context context;
    private List<Route> items;
    private Route route;

    public RouteAdapter(Context context, List<Route> routeList) {
        this.context = context;
        this.items = routeList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ActivityMainBinding binding = ActivityMainBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new RouteAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        route = items.get(position);


        holder.binding.recentRouteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(items.get(position));
            }
        });

        holder.binding.favoriteRouteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(items.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
       ActivityMainBinding binding;
        public ViewHolder(ActivityMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
