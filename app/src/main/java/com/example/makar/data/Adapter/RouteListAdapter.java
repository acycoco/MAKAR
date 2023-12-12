package com.example.makar.data.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.route.BriefStation;
import com.example.makar.data.LineNumImage;
import com.example.makar.data.route.Route;
import com.example.makar.databinding.RouteListRecyclerViewItemBinding;
import com.example.makar.route.listener.OnRouteListClickListener;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {
    //즐겨찾는 경로, 최근 경로
    private Context context;
    private LineNumImage lineNumImage = new LineNumImage();
    private List<Route> items;
    private Route route;
    private OnRouteListClickListener listener;
    private BriefStation sourceStation;
    private BriefStation destinationStation;
    private BriefStation transferStation;

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
        RouteListRecyclerViewItemBinding binding = RouteListRecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new RouteListAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        route = items.get(position);
        lineNumImage.addLineNum();
        int size = route.getBriefRoute().size();
        sourceStation = route.getBriefRoute().get(0);
        destinationStation = route.getBriefRoute().get(size - 1);

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

        if (LineNumImage.lineNumMap.containsKey(sourceStation.getLineNumToString())) {
            holder.binding.sourceStationImageView.setImageResource(LineNumImage.lineNumMap.get(sourceStation.getLineNumToString()));
        } else {
            holder.binding.sourceStationImageView.setImageResource(R.drawable.ic_line0);
        }

        if (LineNumImage.lineNumMap.containsKey((route.getDestinationStation().getLineNum()))) {
            holder.binding.destinationStationImageView.setImageResource(LineNumImage.lineNumMap.get((route.getDestinationStation().getLineNum())));
        } else {
            holder.binding.destinationStationImageView.setImageResource(R.drawable.ic_line0);
        }

        holder.binding.sourceStationTextView.setText(sourceStation.getStationName() + "역 >");
        holder.binding.destinationStationTextView.setText(destinationStation.getStationName() + "역");

        holder.binding.routeListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //선택된 route 넘겨줌
                listener.onRouteListClick(items.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (items.size() == 0) {
            return 0;
        } else {
            return 1;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        RouteListRecyclerViewItemBinding binding;

        public ViewHolder(RouteListRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
