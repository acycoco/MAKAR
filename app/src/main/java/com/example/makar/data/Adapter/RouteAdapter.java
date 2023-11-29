package com.example.makar.data.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.BriefStation;
import com.example.makar.data.LineNumImage;
import com.example.makar.data.Route;
import com.example.makar.databinding.RouteRecyclerViewItemBinding;
import com.example.makar.route.OnRouteClickListener;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private OnRouteClickListener listener;
    private List<Route> items;
    private Context context;
    private Route route;
    private String totalTime;
    private LineNumImage lineNumImage = new LineNumImage();
    private BriefStation sourceStation;
    private BriefStation destinationStation;
    private BriefStation transferStation;


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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        route = items.get(position);
        lineNumImage.addLineNum();
        int size = route.getBriefRoute().size();
        sourceStation = route.getBriefRoute().get(0);
        destinationStation = route.getBriefRoute().get(size - 1);
        totalTime = String.valueOf(route.getTotalTime());

        if (size > 2) {
            holder.binding.transferStationImageView1.setVisibility(View.VISIBLE);
            holder.binding.transferStationTextView1.setVisibility(View.VISIBLE);

            transferStation = route.getBriefRoute().get(1);
            if (LineNumImage.lineNumMap.containsKey(transferStation.getLineNum() + "호선")) {
                holder.binding.transferStationImageView1.setImageResource(LineNumImage.lineNumMap.get(transferStation.getLineNum() + "호선"));
            } else {
                holder.binding.transferStationImageView1.setImageResource(R.drawable.ic_line0);
            }
            holder.binding.transferStationTextView1.setText(transferStation.getStationName() + "역 >");
        } else {
            holder.binding.transferStationImageView1.setVisibility(View.GONE);
            holder.binding.transferStationTextView1.setVisibility(View.GONE);
        }

        if (LineNumImage.lineNumMap.containsKey(route.getSourceStation().getLineNum())) {
            holder.binding.sourceStationImageView.setImageResource(LineNumImage.lineNumMap.get(route.getSourceStation().getLineNum()));
        } else {
            holder.binding.sourceStationImageView.setImageResource(R.drawable.ic_line0);
        }

        if (LineNumImage.lineNumMap.containsKey((route.getDestinationStation().getLineNum()))) {
            holder.binding.destinationStationImageView.setImageResource(LineNumImage.lineNumMap.get((route.getDestinationStation().getLineNum())));
        } else {
            holder.binding.destinationStationImageView.setImageResource(R.drawable.ic_line0);
        }

        holder.binding.totalTimeTextView.setText(totalTime + "분");
        holder.binding.sourceStationTextView.setText(sourceStation.getStationName() + "역 >");
        holder.binding.destinationStationTextView.setText(destinationStation.getStationName() + "역");

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
