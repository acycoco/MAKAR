package com.example.makar.route;

import android.util.Log;

import com.example.makar.data.SubRoute;
import com.example.makar.data.TransferInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.CompletableFuture;

public class TransferManager {

    private static final int DEFAULT_TRANSFER_TIME = 4; //기본 환승 소요시간
    private final FirebaseFirestore firebaseFirestore;

    public TransferManager() {
        this.firebaseFirestore = FirebaseFirestore.getInstance();
    }
    public CompletableFuture<TransferInfo> searchTransferInfoAsync(SubRoute currentSubRoute, SubRoute nextSubRoute) {
        Log.d("MAKAR", "환승소요시간 검색 : " + currentSubRoute.getEndStationName() + " "
                + currentSubRoute.getLineNum() + "->" + nextSubRoute.getLineNum());
        int fromStationID = currentSubRoute.getEndStationCode();
        int toStationID = nextSubRoute.getStartStationCode();
        CompletableFuture<TransferInfo> future = new CompletableFuture<>();

        firebaseFirestore.collection("transfer")
                .whereEqualTo("fromStationID", fromStationID)
                .whereEqualTo("toStationID", toStationID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            DocumentSnapshot documentSnapshot = result.getDocuments().get(0);
                            int fromLine = ((Long) documentSnapshot.get("fromLine")).intValue();
                            int toLine = ((Long) documentSnapshot.get("toLine")).intValue();
                            String odsayStationName = (String) documentSnapshot.get("odsayStationName");
                            int time = ((Long) documentSnapshot.get("time")).intValue();

                            TransferInfo transferInfo = new TransferInfo(fromLine, toLine, odsayStationName, time);
                            future.complete(transferInfo);
                        } else {
                            // 조회결과가 없을 경우 기본 환승소요시간으로 생성
                            TransferInfo defaultTransferInfo = new TransferInfo(currentSubRoute.getLineNum(),
                                    nextSubRoute.getLineNum(), currentSubRoute.getEndStationName(), DEFAULT_TRANSFER_TIME);
                            future.complete(defaultTransferInfo);
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

}
