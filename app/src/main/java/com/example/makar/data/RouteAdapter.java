package com.example.makar.data;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.OnRouteClickListener;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {
    private Context context;
    private List<Route> items;
    private Route route;
    private OnRouteClickListener listener;

    public RouteAdapter(Context context, List<Route> routeList) {
        this.context = context;
        this.items = routeList;
    }
    public void setOnRouteClickListener(OnRouteClickListener listener) {
        this.listener = listener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RouteRecyclerViewItemBinding binding = RouteRecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new RouteAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        route = items.get(position);
        List<BriefStation> briefStations = route.getBriefRoute();

        holder.subRouteAdapter.setData(briefStations);
        holder.subRouteAdapter.notifyDataSetChanged();

        holder.binding.lineTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //선택된 route 넘겨줌
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
        public SubRouteAdapter subRouteAdapter;

        public ViewHolder(RouteRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            subRouteAdapter = new SubRouteAdapter(new ArrayList<>());
        }

    }
}
