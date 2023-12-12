package com.example.makar.data.route;

public class TransferInfo {
    //환승 시에 필요한 정보
    private int fromLine;
    private int toLine;
    private String transferStation;
    private int transferTime;

    public TransferInfo() {
    }

    public TransferInfo(int fromLine, int toLine, String transferStation, int transferTime) {
        this.fromLine = fromLine;
        this.toLine = toLine;
        this.transferStation = transferStation;
        this.transferTime = transferTime;
    }

    public int getFromLine() {
        return fromLine;
    }

    public int getToLine() {
        return toLine;
    }

    public String getTransferStation() {
        return transferStation;
    }

    public int getTransferTime() {
        return transferTime;
    }

    @Override
    public String toString() {
        return "TransferInfo{" +
                "fromLine=" + fromLine +
                ", toLine=" + toLine +
                ", transferStation='" + transferStation + '\'' +
                ", transferTime=" + transferTime +
                '}';
    }
}

