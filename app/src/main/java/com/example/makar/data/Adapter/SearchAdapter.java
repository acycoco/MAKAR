package com.example.makar.data.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.LineNumImage;
import com.example.makar.data.Station;
import com.example.makar.databinding.SearchRecyclerViewItemBinding;
import com.example.makar.route.OnItemClickListener;

import java.util.HashMap;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private List<Station> items;
    private Context context;
    private Station station;
    private LineNumImage lineNumImage = new LineNumImage();
    public SearchAdapter(Context context, List<Station> items) {
        this.context = context;
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        SearchRecyclerViewItemBinding binding = SearchRecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        station = items.get(position);
        lineNumImage.addLineNum();

        String lineNum = station.getLineNum();

        if (LineNumImage.lineNumMap.containsKey(lineNum)) {
            holder.binding.lineImageView.setImageResource(LineNumImage.lineNumMap.get(lineNum));
        } else {
            holder.binding.lineImageView.setImageResource(R.drawable.ic_line0);
        }

        holder.binding.lineTextView.setText(station.getStationName() + "역 " + station.getLineNum());
        holder.binding.lineTextView.setOnClickListener(new View.OnClickListener() {
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        SearchRecyclerViewItemBinding binding;

        public ViewHolder(SearchRecyclerViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
