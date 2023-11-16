package com.example.makar.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.makar.R;
import com.example.makar.data.Station;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Station> {
    public CustomAdapter(Context context, List<Station> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_station_list_item, parent, false);
        }

        Station station = getItem(position);

        ImageView imageView = convertView.findViewById(R.id.line_image_view);
        String lineNum = station.getLineNum();

        if ("1호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line1);
        } else if ("2호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line2);
        } else if ("3호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line3);
        } else if ("4호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line4);
        } else if ("5호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line5);
        } else if ("6호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line6);
        } else if ("7호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line7);
        } else if ("8호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line8);
        } else if ("9호선".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line9);
        } else if ("경의중앙".equals(lineNum)) {
            imageView.setImageResource(R.drawable.ic_line_k);
        } else {
            imageView.setImageResource(R.drawable.ic_line0);
        }

        TextView textView = convertView.findViewById(R.id.line_text_view);
        textView.setText(station.getStationName() + "역 " + station.getLineNum());
        return convertView;
    }
}