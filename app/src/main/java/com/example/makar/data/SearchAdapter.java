package com.example.makar.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.makar.R;
import com.example.makar.data.Station;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<Station> items;
    private Context context;

    public SearchAdapter(Context context, List<Station> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Station station = items.get(position);

        String lineNum = station.getLineNum();

        if ("1호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line1);
        } else if ("2호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line2);
        } else if ("3호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line3);
        } else if ("4호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line4);
        } else if ("5호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line5);
        } else if ("6호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line6);
        } else if ("7호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line7);
        } else if ("8호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line8);
        } else if ("9호선".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line9);
        } else if ("경의중앙".equals(lineNum)) {
            holder.imageView.setImageResource(R.drawable.ic_line_k);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_line0);
        }

        holder.textView.setText(station.getStationName() + "역 " + station.getLineNum());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.line_image_view2);
            textView = itemView.findViewById(R.id.line_text_view2);
        }
    }
}
