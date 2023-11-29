package com.example.makar.data;

import com.example.makar.R;
import java.util.HashMap;

public class LineNumImage {
    public static HashMap<String, Integer> lineNumMap = new HashMap<>();

    public LineNumImage() {
        addLineNum();
    }
    public static void addLineNum() {
        lineNumMap.put("1호선", R.drawable.ic_line1);
        lineNumMap.put("2호선", R.drawable.ic_line2);
        lineNumMap.put("3호선", R.drawable.ic_line3);
        lineNumMap.put("4호선", R.drawable.ic_line4);
        lineNumMap.put("5호선", R.drawable.ic_line5);
        lineNumMap.put("6호선", R.drawable.ic_line6);
        lineNumMap.put("7호선", R.drawable.ic_line7);
        lineNumMap.put("8호선", R.drawable.ic_line8);
        lineNumMap.put("9호선", R.drawable.ic_line9);
        lineNumMap.put("경의중앙", R.drawable.ic_line_k);
        lineNumMap.put("공항철도", R.drawable.ic_line_a);
    }
}
