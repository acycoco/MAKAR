package com.example.makar.route;

import com.example.makar.data.Route;
import com.example.makar.data.Station;

public interface OnItemClickListener {
    void onItemClick(Station station);
    void onItemClick(Route route);
}
