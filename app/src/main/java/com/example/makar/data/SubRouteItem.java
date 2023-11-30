package com.example.makar.data;

public class SubRouteItem {
    private SubRoute subRoute;
    private TransferInfo transferInfo;

    public SubRouteItem() {
    }
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

