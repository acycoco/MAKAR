package com.example.makar.data.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.data.BriefStation;
import com.example.makar.data.Route;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.main.OnRouteListClickListener;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {
    //즐겨찾는 경로, 최근 경로
    private Context context;
    private List<Route> items;
    private Route route;
    private OnRouteListClickListener listener;

    public RouteListAdapter(Context context, List<Route> routeList) {
        this.context = context;
        this.items = routeList;
    }

    public void setOnRouteClickListener(OnRouteListClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RouteRecyclerViewItemBinding binding = RouteRecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new RouteListAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        route = items.get(position);
        List<BriefStation> briefStations = route.getBriefRoute();
        String text = briefStations.get(0).getStationName();

        for(int i=1; i<briefStations.size(); i++){
            String stationName = briefStations.get(i).getStationName();
            text = text +" > "+stationName;
        }

        holder.binding.lineTextView.setText(text);


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

        public ViewHolder(RouteRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

    }
}
