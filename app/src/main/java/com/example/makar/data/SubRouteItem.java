package com.example.makar.data;

public class SubRouteItem {
    private final SubRoute subRoute;
    private final TransferInfo transferInfo;

    public SubRouteItem(SubRoute subpath, TransferInfo transferInfo) {
        this.subRoute = subpath;
        this.transferInfo = transferInfo;
    }

    public SubRoute getSubPath() {
        return subRoute;
    }

    public TransferInfo getTransferInfo() {
        return transferInfo;
    }

    @Override
    public String toString() {
        return "SubRouteItem{" +
                "subRoute=" + subRoute +
                ", transferInfo=" + transferInfo +
                '}';
    }
}

