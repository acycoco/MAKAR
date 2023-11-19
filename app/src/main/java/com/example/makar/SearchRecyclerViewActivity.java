package com.example.makar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.makar.data.Station;
import com.example.makar.databinding.ActivitySearchRecyclerViewBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchRecyclerViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySearchRecyclerViewBinding searchRecyclerViewBinding = ActivitySearchRecyclerViewBinding.inflate(getLayoutInflater());
        setContentView(searchRecyclerViewBinding.getRoot());

        List<Station> stationList = new ArrayList<>();
        SearchListAdapter adapter = new SearchListAdapter(stationList);
        searchRecyclerViewBinding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        searchRecyclerViewBinding.searchRecyclerView.setAdapter(new SearchListAdapter(stationList));
        searchRecyclerViewBinding.searchRecyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        searchRecyclerViewBinding.searchViewHome2.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.isEmpty()) {
                    CollectionReference collectionRef = db.collection("stations"); // 컬렉션 이름에 맞게 변경하세요.

                    Query query = collectionRef.orderBy("stationName")
                            .startAt(newText)
                            .endAt(newText + "\uf8ff");

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                stationList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Station station = document.toObject(Station.class);
                                    stationList.add(station);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d("MAKAR", "검색 중 오류 발생: ", task.getException());
                            }
                        }
                    });

                }
                return true;
            }
        });
    }

    public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {
        private List<Station> items;

        public SearchListAdapter(List<Station> items) {
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

}
